package br.dev.erickfthz.finn.core.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public abstract class SlashSubcommand {
    private final String name;
    private final String description;
    private final List<OptionData> options;

    public SlashSubcommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.options = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<OptionData> getOptions() {
        return options;
    }

    public void addOption(OptionData option) {
        options.add(option);
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public SubcommandData toSubCommandData() {
        return new SubcommandData(getName(), getDescription()).addOptions(getOptions());
    }
}