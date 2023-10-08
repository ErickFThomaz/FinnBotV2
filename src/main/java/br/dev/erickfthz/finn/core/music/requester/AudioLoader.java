package br.dev.erickfthz.finn.core.music.requester;

import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import br.dev.erickfthz.finn.core.music.GuildMusicManager;
import br.dev.erickfthz.finn.core.music.utils.AudioUtils;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class AudioLoader implements AudioLoadResultHandler {

    private final SlashCommandEvent event;

    private final GuildMusicManager musicManager;

    @Override
    public void trackLoaded(AudioTrack track) {
        loadSingle(track, false);
    }


    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            onSearch(playlist);
            return;
        }

        try {
            event.reply("`%1$s` musics from playlist: **%2$s** (%3$s)", playlist.getTracks().size(), playlist.getName(),
                            AudioUtils.formatDuration(playlist.getTracks().stream().mapToLong(temp -> temp.getInfo().length).sum()))
                    .queue();
            playlist.getTracks().forEach(this::loadSingle);
        } catch (Exception ex) {
            log.error("Houve um problema ao tentar rodar algumas músicas", ex);
        }
    }


    @Override
    public void noMatches() {
        event.getChannel().sendMessageFormat("Nothing found.").queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        exception.printStackTrace();
    }

    private void loadSingle(AudioTrack track) {
        loadSingle(track, false);
    }

    private void loadSingle(AudioTrack track, boolean silent) {

        TrackScheduler scheduler = musicManager.getTrackScheduler();

        track.setUserData(event.getAuthor().getId());

        String title = track.getInfo().title;
        long length = track.getInfo().length;

        String duration = AudioUtils.formatDuration(length);

        scheduler.queue(track);
        scheduler.setMessageChannel(event.getChannel().getId());

        if (!silent) {
            event.getChannel().sendMessageFormat("Music add in current queue: `%1$s` **(%2$s)**", title, duration)
                    .queue(message -> message.delete().queueAfter(120, TimeUnit.SECONDS, null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS)));
        }
    }

    private void onSearch(@NotNull AudioPlaylist playlist) {
        AudioTrack[] options = playlist.getTracks().stream().limit(5).toArray(AudioTrack[]::new);
        OrderedMenu.Builder menu = new OrderedMenu.Builder()
                .allowTextInput(true)
                .setColor(Color.yellow)
                .setEventWaiter(event.getCore().getEventWaiter())
                .useCancelButton(true)
                .setUsers(event.getAuthor())
                .setText("Found songs:")
                .setTimeout(1, TimeUnit.MINUTES);
        for (AudioTrack track : options) {
//            if (track instanceof ISRCAudioTrack) {
//                menu.addChoice(String.format("**[%1$s - %2$s](%3$s)** (%4$s)", track.getInfo().title, track.getInfo().author, track.getInfo().uri, AudioUtils.formatDuration(track.getDuration())));
//            } else {
            menu.addChoice(String.format("**[%1$s](%2$s)** (%3$s)", track.getInfo().title, track.getInfo().uri, AudioUtils.formatDuration(track.getDuration())));
//            }
        }
        menu.setSelection((message, integer) -> {
            loadSingle(options[integer - 1], false);
        }).setCancel(message -> {
            musicManager.scheduleLeave();
        }).build().display(event.getChannel());
    }
}