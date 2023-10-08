package br.dev.erickfthz.finn.core.music.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class PlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public PlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(4096);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.provide(frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        ((Buffer) buffer).flip();
        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

}
