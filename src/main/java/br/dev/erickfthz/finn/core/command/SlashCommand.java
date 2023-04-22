package br.dev.erickfthz.finn.core.command;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class SlashCommand {

    private final String name;
    private final String description;
    private final List<OptionData> options;
    private final Map<String, SlashSubcommand> subcommands;

    public SlashCommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.options = new ArrayList<>();
        this.subcommands = new HashMap<>();
    }


    public Map<String, SlashSubcommand> getSubcommands() {
        return subcommands;
    }

    public void addSubcommand(SlashSubcommand subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    public void addOption(OptionData option) {
        options.add(option);
    }

    public abstract void execute(SlashCommandInteractionEvent event);

}
