package br.dev.erickfthz.finn.core.command;

import br.dev.erickfthz.finn.core.command.annotations.Command;
import br.dev.erickfthz.finn.core.command.annotations.Option;
import br.dev.erickfthz.finn.core.command.annotations.SubCommand;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CommandManager {

    private final Map<String, Class<? extends ICommand>> COMMANDS = new HashMap<>();
    private final List<SlashCommandData> commandData = new ArrayList<>();

    private final JDA jda;

    public CommandManager(JDA jda) {
        this.jda = jda;
    }

    {
        findCommands();
    }

    public void publicCommands() {
        System.out.println(commandData);
        jda.updateCommands().addCommands(commandData).queue();
    }

    public Class<? extends ICommand> getCommand(String commandName){
        return this.getCOMMANDS().get(commandName);
    }


    private void findCommands() {
        Reflections reflections = new Reflections("br.dev.erickfthz.finn.commands");

        for (Class<?> aClass : reflections.getTypesAnnotatedWith(Command.class)) {
            Command command = aClass.getAnnotation(Command.class);

            if (command != null) {
                COMMANDS.computeIfAbsent(command.name(), (name) -> name.equalsIgnoreCase(command.name()) ? (Class<? extends ICommand>) aClass : null);
                registerCommands(command);
            }

        }
    }

    public void registerCommands(Command command) {
        List<OptionData> optionData = new ArrayList<>();
        if (command != null) {

            for (Option commandOption : command.options()) {
                optionData.add(new OptionData(commandOption.option(), commandOption.name(),
                        commandOption.description()));
            }

            commandData.add(Commands.slash(command.name(), command.description()).addOptions(optionData));
        }
    }

    public void registerSubCommand(Command command, SubCommand subCommand) {
        List<OptionData> optionData = new ArrayList<>();
        if (command != null && subCommand != null) {

            for (Option commandOption : subCommand.options()) {
                optionData.add(new OptionData(commandOption.option(), commandOption.name(),
                        commandOption.description()));
            }

            commandData.add(Commands.slash(command.name(), command.description())
                    .addSubcommands(new SubcommandData(subCommand.name(), subCommand.description()).addOptions(optionData))
                    .addOptions(optionData));
        }
    }
}
