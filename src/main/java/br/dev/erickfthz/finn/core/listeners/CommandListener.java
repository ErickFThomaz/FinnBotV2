package br.dev.erickfthz.finn.core.listeners;

import br.dev.erickfthz.finn.core.FinnCore;
import br.dev.erickfthz.finn.core.command.SlashCommand;
import br.dev.erickfthz.finn.core.command.SlashSubcommand;
import br.dev.erickfthz.finn.core.command.interaction.event.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandListener extends DynamicHandler<SlashCommandInteractionEvent> {

    private final FinnCore finnCore;

    public CommandListener(FinnCore finnCore) {
        super(event -> true);
        this.finnCore = finnCore;
    }

    @Override
    public void onEvent(SlashCommandInteractionEvent event) {
        SlashCommand command = finnCore.getCommandManager().getCommand(event.getName());

        if (command == null) {
            return;
        }

        String subcommandName = event.getSubcommandName();

        if (subcommandName != null) {
            SlashSubcommand subcommand = command.getSubcommands().get(subcommandName);

            if (subcommand == null) {
                event.reply("Invalid subcommand").setEphemeral(true).queue();
                return;
            }

            try {
                subcommand.execute(event);
            } catch (Exception e) {
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }
        } else {
            try {
                command.execute(event);
            } catch (Exception e) {
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }
        }
    }
}
