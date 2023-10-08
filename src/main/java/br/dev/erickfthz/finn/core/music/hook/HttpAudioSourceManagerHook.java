package br.dev.erickfthz.finn.core.music.hook;

import br.dev.erickfthz.finn.Main;
import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.*;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult.refer;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.getHeaderValue;

@Slf4j
public class HttpAudioSourceManagerHook extends ProbingAudioSourceManager implements HttpConfigurable {
    private final HttpInterfaceManager httpInterfaceManager;

    /**
     * Create a new instance with default media container registry.
     */
    public HttpAudioSourceManagerHook() {
        this(Main.getFinnCore().getMediaContainerRegistry());
    }

    /**
     * Create a new instance.
     */
    public HttpAudioSourceManagerHook(MediaContainerRegistry containerRegistry) {
        super(containerRegistry);

        httpInterfaceManager = new ThreadLocalHttpInterfaceManager(
                HttpClientTools
                        .createSharedCookiesHttpBuilder()
                        .setRedirectStrategy(new HttpClientTools.NoRedirectsStrategy()),
                HttpClientTools.DEFAULT_REQUEST_CONFIG
        );
    }

    @Override
    public String getSourceName() {
        return "http";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        AudioReference httpReference = getAsHttpReference(reference);
        if (httpReference == null) {
            return null;
        }

        if (httpReference.containerDescriptor != null) {
            return createTrack(AudioTrackInfoBuilder.create(reference, null).build(), httpReference.containerDescriptor);
        } else {
            System.out.println(httpReference.identifier);
            return handleLoadResult(detectContainer(httpReference));
        }
    }

    @Override
    protected AudioTrack createTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerDescriptor) {
        return new HttpAudioTrack(trackInfo, containerDescriptor, this);
    }

    /**
     * @return Get an HTTP interface for a playing track.
     */
    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        httpInterfaceManager.configureBuilder(configurator);
    }

    public static AudioReference getAsHttpReference(AudioReference reference) {
        if (reference.identifier.startsWith("https://") || reference.identifier.startsWith("http://")) {
            return reference;
        } else if (reference.identifier.startsWith("icy://")) {
            return new AudioReference("http://" + reference.identifier.substring(6), reference.title);
        }
        return null;
    }

    private MediaContainerDetectionResult detectContainer(AudioReference reference) {
        MediaContainerDetectionResult result;

        try (HttpInterface httpInterface = getHttpInterface()) {
            result = detectContainerWithClient(httpInterface, reference);
        } catch (IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", SUSPICIOUS, e);
        }

        return result;
    }

    private MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface, AudioReference reference) throws IOException {
        try (PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(reference.identifier), Units.CONTENT_LENGTH_UNKNOWN)) {
            int statusCode = inputStream.checkStatusCode();
            String redirectUrl = getRedirectLocation(reference.identifier, inputStream.getCurrentResponse());

            if (redirectUrl != null) {
                return refer(null, new AudioReference(redirectUrl, null));
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", COMMON, new IllegalStateException("Status code " + statusCode));
            }

            MediaContainerHints hints = MediaContainerHints.from(getHeaderValue(inputStream.getCurrentResponse(), "Content-Type"), null);
            return new MediaContainerDetection(containerRegistry, reference, inputStream, hints).detectContainer();
        } catch (URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", COMMON, e);
        }
    }

    private String getRedirectLocation(String requestUrl, HttpResponse response) {

        Header header = response.getFirstHeader("Location");
        if (header == null) {
            return null;
        }

        String location = header.getValue();

        try {
            return new URI(requestUrl).resolve(location).toString();
        } catch (URISyntaxException e) {
            log.debug("Failed to parse URI.", e);
            return location;
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        encodeTrackFactory(((HttpAudioTrack) track).getContainerTrackFactory(), output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        MediaContainerDescriptor containerTrackFactory = decodeTrackFactory(input);

        if (containerTrackFactory != null) {
            return new HttpAudioTrack(trackInfo, containerTrackFactory, this);
        }

        return null;
    }

    @Override
    public void shutdown() {
        // Nothing to shut down
    }
}
