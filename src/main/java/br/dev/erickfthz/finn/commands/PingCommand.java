package br.dev.erickfthz.finn.commands;

import br.dev.erickfthz.finn.core.command.SlashCommand;
import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import br.dev.erickfthz.finn.core.command.annotations.RegisterCommand;

@RegisterCommand
public class PingCommand extends SlashCommand {

    public PingCommand() {
        super("ping", "Ping do Bot");
    }

    @Override
    public void execute(SlashCommandEvent event) {


        event.reply("Pong! %s ms", event.getJda().getGatewayPing())
                .setEphemeral(true)
                .queue();

    }

}
