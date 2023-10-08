package br.dev.erickfthz.finn.core.music.utils;

import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AudioUtils {

    public static boolean connect(SlashCommandEvent tc, Member member, boolean isSpeech) {
        AtomicBoolean connected = new AtomicBoolean(false);

        Member selfMember = tc.getGuild().getSelfMember();
        tc.defer().queue(interactionHook -> {



            if (!member.getVoiceState().inAudioChannel() && !isSpeech) {
                interactionHook.editOriginal(":x: Você não está conectado a um canal de voz!").queue();
                return;
            } else if (tc.getGuild().getAudioManager().isConnected()) {
                if (!tc.getGuild().getAudioManager().getConnectedChannel().equals(member.getVoiceState().getChannel())) {
                    interactionHook.editOriginal(" Você não está conectado ao canal que estou conectado!").queue();
                    return;
                }
                connected.set(true);
                return;
            }

            AudioChannel vc = member.getVoiceState().getChannel();

            if(vc instanceof VoiceChannel){
                if(!selfMember.hasPermission(Permission.MANAGE_CHANNEL) && vc.getUserLimit() <= vc.getMembers().size()
                && vc.getUserLimit() > 0){
                    interactionHook.editOriginal("A call está lotada não consigo entrar na call.").queue();
                    return;
                }
            }

            if (vc instanceof StageChannel stageChannel) {
                if (!selfMember.hasPermission(Permission.REQUEST_TO_SPEAK)) {
                    interactionHook.editOriginal("Não tenho permissão para pedir para falar no canal de palco.")
                            .queue();
                    return;
                }
                var instance = stageChannel.getStageInstance();

                if(Objects.isNull(instance)){
                    interactionHook.editOriginal("Nenhum canal de palco para conectar.")
                            .queue();
                    return;
                }
            }

            tc.getGuild().getAudioManager().openAudioConnection(vc);
            interactionHook.editOriginal("Connecting...").completeAfter(2, TimeUnit.SECONDS)
                    .editMessageFormat(":white_check_mark:  Connected in %s.", vc.getName()).queue();
            connected.set(true);
        });
        return connected.get();
    }

    public static String formatDuration(long time) {
        if (time < 1000) {
            return "menos que um segundo";
        }

        var days = TimeUnit.MILLISECONDS.toDays(time);
        var hours = TimeUnit.MILLISECONDS.toHours(time) % TimeUnit.DAYS.toHours(1);
        var minutes = TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1);
        var seconds = TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1);

        var parts = Stream.of(
                formatUnit(days, "dia"), formatUnit(hours, "hora"),
                formatUnit(minutes, "minuto"), formatUnit(seconds, "segundo")
        ).filter(i -> !i.isEmpty()).iterator();

        var sb = new StringBuilder();
        var multiple = false;

        while (parts.hasNext()) {
            sb.append(parts.next());
            if (parts.hasNext()) {
                multiple = true;
                sb.append(", ");
            }
        }

        if (multiple) {
            var last = sb.lastIndexOf(", ");
            sb.replace(last, last + 2, " e ");
        }

        return sb.toString();
    }

    private static String formatUnit(long amount, String baseName) {
        if (amount == 0) {
            return "";
        }

        if (amount == 1) {
            return "1 " + baseName;
        }

        return amount + " " + baseName + "s";
    }

    private final static String BLOCK_INACTIVE = "\u25AC";
    private final static String BLOCK_ACTIVE = "\uD83D\uDD18";
    private static final int TOTAL_BLOCKS = 20;

    public static String getProgressBar(long now, long total) {
        int activeBlocks = (int) ((float) now / total * TOTAL_BLOCKS);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < TOTAL_BLOCKS; i++)
            builder.append(activeBlocks == i ? BLOCK_ACTIVE : BLOCK_INACTIVE);

        return builder.append(BLOCK_INACTIVE).toString();
    }

    public static String getTimestamp(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int days = (int) ((milliseconds / (1000 * 60 * 60 * 24)));

        if (days > 0) {
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}