package br.dev.erickfthz.finn.commands;

import br.dev.erickfthz.finn.core.command.ICommand;
import br.dev.erickfthz.finn.core.command.annotations.Command;
import br.dev.erickfthz.finn.core.command.annotations.SubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(name = "ping", description = "Mostra o ping do bot")
public class PingCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyFormat("Pong! %s ms", event.getJDA().getGatewayPing()).setEphemeral(true).queue();
    }

    @SubCommand(name = "teste", description = "Teste do SubCommand", command = PingCommand.class)
    class PingSubCommand implements ICommand {

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            event.reply("Funcionou o subCommand").setEphemeral(true).queue();
        }
    }
}
