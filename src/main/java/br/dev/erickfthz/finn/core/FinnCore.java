package br.dev.erickfthz.finn.core;

import br.dev.erickfthz.finn.core.command.CommandManager;
import br.dev.erickfthz.finn.core.listeners.events.FinnDynamicEventHandler;
import br.dev.erickfthz.finn.core.listeners.CommandListener;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

    private final CommandManager commandManager;

    private final static Logger logger = LoggerFactory.getLogger(FinnCore.class);

    public FinnCore() {
        this.jda = createBot();
        commandManager = new CommandManager(jda);
    }

    public void inicialize() {
        commandManager.publicCommands();

        FinnDynamicEventHandler.getInstance().addListener(new CommandListener(this));
    }

    private JDA createBot() {
        JDABuilder builder = JDABuilder.create(System.getenv("BOT_TOKEN"),
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .addEventListeners(FinnDynamicEventHandler.getInstance())
                .disableCache(List.of(ACTIVITY, VOICE_STATE, EMOJI, STICKER, CLIENT_STATUS, ONLINE_STATUS, SCHEDULED_EVENTS));

        return builder.build();
    }

}
