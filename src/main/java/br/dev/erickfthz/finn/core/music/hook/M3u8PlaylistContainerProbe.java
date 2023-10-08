package br.dev.erickfthz.finn.core.music.hook;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.container.playlists.HlsStreamSegmentUrlProvider;
import com.sedmelluq.discord.lavaplayer.container.playlists.M3uPlaylistContainerProbe;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection.checkNextBytes;
import static com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult.*;
import static com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult.refer;

public class M3u8PlaylistContainerProbe extends M3uPlaylistContainerProbe {

    private static final Logger log = LoggerFactory.getLogger(M3u8PlaylistContainerProbe.class);

    private static final String TYPE_HLS_OUTER = "hls-outer";
    private static final String TYPE_HLS_INNER = "hls-inner";

    private static final int[] M3U_HEADER_TAG = new int[] { '#', 'E', 'X', 'T', 'M', '3', 'U' };
    private static final int[] M3U_ENTRY_TAG = new int[] { '#', 'E', 'X', 'T', 'I', 'N', 'F' };


    @Override
    public String getName() {
        return "m3e8";
    }

    @Override
    public boolean matchesHints(MediaContainerHints hints) {
        List<String> m3e8 = List.of("application/x-mpegURL", "vnd.apple.mpegURL", "application/vnd.apple.mpegURL");

        if(hints.mimeType != null && m3e8.contains(hints.mimeType))
            return true;

        return super.matchesHints(hints);
    }

    @Override
    public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {

        if (!checkNextBytes(inputStream, M3U_HEADER_TAG) && !checkNextBytes(inputStream, M3U_ENTRY_TAG)) {
            return null;
        }

        MediaPlaylistParser parser = new MediaPlaylistParser();

        log.debug("Track {} is an M3U playlist file.", reference.identifier);

        MediaPlaylist playlist = parser.readPlaylist(inputStream);
        String[] lines = DataFormatTools.streamToLines(inputStream, StandardCharsets.UTF_8);

        System.out.println(playlist.mediaSegments());
        String hlsStreamUrl = HlsStreamSegmentUrlProvider.findHlsEntryUrl(lines);

        if (hlsStreamUrl != null) {
            AudioTrackInfoBuilder infoBuilder = AudioTrackInfoBuilder.create(reference, inputStream);
            AudioReference httpReference = HttpAudioSourceManager.getAsHttpReference(reference);

            if (httpReference != null) {
                return supportedFormat(this, TYPE_HLS_OUTER, infoBuilder.setIdentifier(httpReference.identifier).build());
            } else {
                return refer(this, new AudioReference(hlsStreamUrl, infoBuilder.getTitle(), new MediaContainerDescriptor(this, TYPE_HLS_INNER)));
            }
        }

        MediaContainerDetectionResult result = loadSingleItemPlaylist(reference, lines);
        if (result != null) {
            return result;
        }

        return unsupportedFormat(this, "The playlist file contains no links.");
    }

    private MediaContainerDetectionResult loadSingleItemPlaylist(AudioReference reference, String[] lines) {

        String trackTitle = "RADIO";

        for (String line : lines) {
            if (line.startsWith("#EXTINF")) {
                trackTitle = extractTitleFromInfo(line);
            } else if (!line.startsWith("#") && line.length() > 0) {

                if (line.startsWith("http://") || line.startsWith("https://") || line.startsWith("icy://")) {
                    return refer(this, new AudioReference(line.trim(), trackTitle));
                }else{
                    String url = reference.identifier;

                    if(!url.endsWith(line)){

                        if(!line.startsWith("/"))
                            line = "/" + line;

                        url = url.substring(0, url.lastIndexOf("/")) + line;
                    }

                    return refer(this, new AudioReference(url.trim(), trackTitle));
                }
            }
        }

        return null;
    }

    private String extractTitleFromInfo(String infoLine) {
        String[] splitInfo = infoLine.split(",", 2);
        return splitInfo.length == 2 ? splitInfo[1] : null;
    }

}

