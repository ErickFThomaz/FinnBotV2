package br.dev.erickfthz.finn.commands;

import br.dev.erickfthz.finn.core.command.SlashCommand;
import br.dev.erickfthz.finn.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@RegisterCommand
public class PingCommand extends SlashCommand {

    public PingCommand() {
        super("ping", "Ping do Bot");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {


        event.replyFormat("Pong! %s ms", event.getJDA().getGatewayPing())
                .setEphemeral(true)
                .queue();

    }

}
