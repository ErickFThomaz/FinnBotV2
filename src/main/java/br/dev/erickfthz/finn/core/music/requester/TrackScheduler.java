package br.dev.erickfthz.finn.core.music.requester;

import br.dev.erickfthz.finn.Main;
import br.dev.erickfthz.finn.core.music.utils.AudioUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
public class TrackScheduler extends AudioEventAdapter {

    private final String guildId;

    private String messageChannel;

    private AudioTrack currentTrack;

    private AudioTrack previousTrack;

    private final List<String> voteSkip;

    private final List<String> voteStop;

    private final AudioPlayer audioPlayer;

    private final ConcurrentLinkedDeque<AudioTrack> queue;

    public TrackScheduler(AudioPlayer audioPlayer, String guildId) {
        this.audioPlayer = audioPlayer;
        this.guildId = guildId;
        this.queue = new ConcurrentLinkedDeque<>();
        this.voteSkip = new ArrayList<>();
        this.voteStop = new ArrayList<>();
    }

    public void queue(AudioTrack track) {
        if (getAudioPlayer().getPlayingTrack() != null) {
            queue.offer(track);
        } else {
            getAudioPlayer().playTrack(track);
            currentTrack = track;
        }
    }

    public void nextTrack() {
        getVoteSkip().clear();

        //Repeat

        if (currentTrack != null) {
            previousTrack = currentTrack;
        }

        currentTrack = queue.poll();

        if (currentTrack != null) {
            getAudioPlayer().playTrack(currentTrack);
        }

        //Skip

    }

    public void onTrackStart() {
        if (currentTrack == null) {
            onStop();
            return;
        }

        final Guild guild = getGuild();

        if (guild == null) {
            onStop();
            return;
        }

        if (getMessageChannel() != null && getChannel() != null) {
            GuildVoiceState voiceState = getChannel().getGuild().getSelfMember().getVoiceState();

            if (voiceState == null) {
                getAudioPlayer().destroy();
                return;
            }

            if (getChannel().canTalk()) {
                AudioTrackInfo information = currentTrack.getInfo();
                EmbedBuilder builder = new EmbedBuilder();
                AudioChannel vc = guild.getAudioManager().getConnectedChannel();

                String title = information.title;
                long trackLength = information.length;

                nextTrack(vc, information, title, trackLength, builder);

                getChannel().sendMessageEmbeds(builder.build()).queue();

                //Modulo Spotify off
//                if (currentTrack.getTrack() instanceof ISRCAudioTrack) {
//                    spotifyNextTrack(vc, info.title, trackLength, builder);
//                } else {

//                }
            }
        }
    }

    private void onStop(){
        getVoteSkip().clear();
        getVoteStop().clear();

        Guild guild = getGuild();
        if(guild == null){
            getAudioPlayer().destroy();
            return;
        }
        getChannel().sendMessage("Queue has ended. Come back to more parties!").queue();

        messageChannel = null;
        currentTrack = null;
        previousTrack = null;

        if(getAudioPlayer().getPlayingTrack() != null){
            getAudioPlayer().getPlayingTrack().stop();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext){
            nextTrack();
            onTrackStart();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        getChannel().sendMessage("Music stuck! Sorry").queue();
    }

    public int getRequiredVotes() {
        var listeners = (int) getGuild().getChannelById(AudioChannel.class, messageChannel)
                .getMembers().stream()
                .filter(m -> m.getVoiceState() != null) // Shouldn't happen?
                .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened())
                .count();
        return (int) Math.ceil(listeners * .55);
    }

    public void shuffle() {
        List<AudioTrack> tempList = new ArrayList<>(getQueue());
        Collections.shuffle(tempList);

        queue.clear();
        queue.addAll(tempList);
    }

    public void stop() {
        queue.clear();
        onStop();
    }


    public Guild getGuild() {
        return Main.getFinnCore().getGuildById(getGuildId());
    }

    public GuildMessageChannel getChannel() {
        return messageChannel != null ? Main.getFinnCore().getChannelById(GuildMessageChannel.class, getMessageChannel()) : null;
    }

    public void setMessageChannel(String messageChannel) {
        this.messageChannel = messageChannel;
    }

    private void nextTrack(AudioChannel voiceChannel, AudioTrackInfo information, String title, long trackLength, EmbedBuilder builder) {
        builder.setThumbnail("https://i.ytimg.com/vi/" + information.identifier + "/hqdefault.jpg");
        builder.setAuthor(String.format("Playing now in %1$s", voiceChannel.getName()));
        builder.setDescription(String.format("Playing now: `%1$s`\n" +
                "Duration: `%2$s`", title, AudioUtils.formatDuration(trackLength)));
        builder.setTimestamp(Instant.now());
        builder.setColor(Color.red);
    }
}
