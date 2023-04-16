package br.dev.erickfthz.finn.core.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ICommand {

    void execute(SlashCommandInteractionEvent event);
}
