package br.dev.erickfthz.finn.core;

import br.dev.erickfthz.finn.core.command.CommandManager;
import br.dev.erickfthz.finn.core.listeners.CommandListener;
import br.dev.erickfthz.finn.core.listeners.events.FinnDynamicEventHandler;
import br.dev.erickfthz.finn.core.music.AudioMusicManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

@Getter
public class FinnCore {

    private final JDA jda;

    private AudioMusicManager musicManager;

    private final CommandManager commandManager;

    private final EventWaiter eventWaiter = new EventWaiter();

    private final MediaContainerRegistry mediaContainerRegistry;

    private final static Logger logger = LoggerFactory.getLogger(FinnCore.class);

    public FinnCore() {
        this.jda = createBot();
        commandManager = new CommandManager(jda);
        mediaContainerRegistry = new MediaContainerRegistry(MediaContainerRegistry.DEFAULT_REGISTRY.getAll());
    }

    public void inicialize() {
        commandManager.publicCommands();
        FinnDynamicEventHandler.getInstance().addListener(new CommandListener(this));

        musicManager = new AudioMusicManager();
    }

    private JDA createBot() {
        JDABuilder builder = JDABuilder.create("NzY2NzY3MDY4NDY3OTUzNjk2.GQ00gH.hYyIBaDhcPXPGFL3Vcmw9ckPWZ9CRqZd9jwHAo",
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MODERATION, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(FinnDynamicEventHandler.getInstance(), eventWaiter)
                .disableCache(List.of(EMOJI, CLIENT_STATUS, ACTIVITY, SCHEDULED_EVENTS));

        return builder.build();
    }

    public Guild getGuildById(String guildId){
        return jda.getGuildById(guildId);
    }

    public <T extends Channel> T getChannelById(Class<T> type, String channelId){
        return jda.getChannelById(type, channelId);
    }

}
