package br.dev.erickfthz.finn.core;

import br.dev.erickfthz.finn.core.command.CommandManager;
import br.dev.erickfthz.finn.core.listeners.CommandListener;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.List;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

@Getter
public class FinnCore {

    private final JDA jda;

    private final CommandManager commandManager;

    public FinnCore(){
        this.jda = createBot();
        commandManager = new CommandManager(jda);
    }

    public void inicialize(){
        commandManager.publicCommands();
    }

    private JDA createBot(){
        JDABuilder builder = JDABuilder.create(System.getenv("BOT_TOKEN"),
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .addEventListeners(new CommandListener(this))
                .disableCache(List.of(ACTIVITY, VOICE_STATE, EMOJI, STICKER, CLIENT_STATUS, ONLINE_STATUS, SCHEDULED_EVENTS));

        return builder.build();
    }

}
