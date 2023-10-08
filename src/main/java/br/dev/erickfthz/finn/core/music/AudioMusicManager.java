package br.dev.erickfthz.finn.core.music;

import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import br.dev.erickfthz.finn.core.music.hook.HttpAudioSourceManagerHook;
import br.dev.erickfthz.finn.core.music.requester.AudioLoader;
import br.dev.erickfthz.finn.core.music.utils.AudioUtils;
import br.dev.erickfthz.finn.core.music.utils.PlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AudioMusicManager {

    private final Map<String, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    public AudioMusicManager() {

        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

//        SpotifyConfig spotifyConfig = new SpotifyConfig();
//        Config.Spotify spotify = NovaEraCore.getInstance().getConfig().getSpotify();
//        spotifyConfig.setClientId(spotify.getClientId());
//        spotifyConfig.setClientSecret(spotify.getClientSecret());
//        spotifyConfig.setCountryCode(spotify.getCountryCode());

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManagerHook());
//        playerManager.registerSourceManager(new SpotifySourceManager(new String[]{}, spotifyConfig, playerManager));
    }

    public GuildMusicManager remove(Guild guild) {
        guild.getAudioManager().setSendingHandler(null);
        return musicManagers.remove(guild.getId());
    }

    public void resetMusicManagerFor(String id) {
        GuildMusicManager previousManager = musicManagers.get(id);
        if (previousManager == null)
            return;

        previousManager.destroy();
        musicManagers.remove(id);
    }


    public GuildMusicManager getMusicManager(Guild guild) {
        AudioPlayer audioPlayer = playerManager.createPlayer();
        if (guild.getAudioManager().getSendingHandler() == null){
            guild.getAudioManager().setSendingHandler(new PlayerSendHandler(audioPlayer));
        }

        return musicManagers.computeIfAbsent(guild.getId(), id -> new GuildMusicManager(guild.getId(), audioPlayer));
    }

    public void play(SlashCommandEvent event, String trackUrl){
        var musicManager = getMusicManager(event.getGuild());
        var scheduler = musicManager.getTrackScheduler();

        scheduler.getAudioPlayer().setPaused(false);

        var state = scheduler.getGuild().getSelfMember().getVoiceState();

        if(AudioUtils.connect(event, event.getMember(), false)) {
            if (state != null && state.getChannel() != null && state.getChannel() instanceof StageChannel stageChannel) {
                try {
                    stageChannel.requestToSpeak().queue();
                } catch (IllegalStateException ignored) {
                }
            }
            var loader = new AudioLoader(event, musicManager);
            playerManager.loadItemOrdered(musicManager, trackUrl, loader);
        }
    }
}
