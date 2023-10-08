package br.dev.erickfthz.finn.core.music;

import br.dev.erickfthz.finn.core.music.requester.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
public class GuildMusicManager {

    private final String guildId;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> leaveTask = null;

    private boolean waitingDeath;

    public GuildMusicManager(String guildId, AudioPlayer audioPlayer){
        this.guildId = guildId;
        this.audioPlayer = audioPlayer;
        this.trackScheduler = new TrackScheduler(audioPlayer, guildId);
        this.audioPlayer.addListener(trackScheduler);
    }

    private void leave(){
        Guild guild = trackScheduler.getGuild();
        if(guild == null){
            audioPlayer.destroy();
            return;
        }

        waitingDeath = false;

        final GuildMessageChannel requestedTextChannel = trackScheduler.getChannel();
        final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();

//        if (requestedTextChannel != null && requestedTextChannel.canTalk() && voiceState != null && voiceState.getChannel() != null) {
//
//        }
    }

    public void scheduleLeave() {
        if (leaveTask != null) {
            return;
        }

        leaveTask = scheduledExecutor.schedule(this::leave, 2, TimeUnit.MINUTES);
    }

    public void cancelLeave() {
        if (leaveTask == null) {
            return;
        }

        leaveTask.cancel(true);
        leaveTask = null;
    }

    public void destroy(){
        getAudioPlayer().removeListener(trackScheduler);
        getAudioPlayer().destroy();
    }

}
