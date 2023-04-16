package br.dev.erickfthz.finn.core.listeners;

import br.dev.erickfthz.finn.core.FinnCore;
import br.dev.erickfthz.finn.core.command.ICommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

    private final FinnCore finnCore;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Class<? extends ICommand> command = finnCore.getCommandManager().getCommand(event.getFullCommandName());

        if(command != null){
            try {
              command.getDeclaredConstructor().newInstance().execute(event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
