package br.dev.erickfthz.finn.commands.music;

import br.dev.erickfthz.finn.core.command.SlashCommand;
import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import br.dev.erickfthz.finn.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;

@RegisterCommand
public class Play extends SlashCommand {

    public Play() {
        super("play", "Tocar música");

        addOption(new OptionData(OptionType.STRING, "url", "Nome ou URL da música"));
    }

    @Override
    public void execute(SlashCommandEvent event) throws IOException {
        String url = event.getStringOption("url");

        event.getCore().getMusicManager().play(event, url);
    }
}
