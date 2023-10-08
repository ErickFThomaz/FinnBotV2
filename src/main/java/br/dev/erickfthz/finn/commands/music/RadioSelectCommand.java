package br.dev.erickfthz.finn.commands.music;

import br.dev.erickfthz.finn.core.command.SlashCommand;
import br.dev.erickfthz.finn.core.command.SlashCommandEvent;
import br.dev.erickfthz.finn.core.command.annotations.RegisterCommand;
import br.dev.erickfthz.finn.core.listeners.events.FinnDynamicEventHandler;
import br.dev.erickfthz.finn.core.listeners.events.handlers.SelectRadioHandler;
import br.dev.erickfthz.finn.utils.retrofit.RadioResource;
import br.dev.erickfthz.finn.utils.retrofit.RadioService;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RegisterCommand
public class RadioSelectCommand extends SlashCommand {

    public RadioSelectCommand() {
        super("radio", "Radio's list to play");
    }

    @Override
    public void execute(SlashCommandEvent event) throws IOException {
        StringSelectMenu.Builder radios = StringSelectMenu.create("radios");
        radios.setMaxValues(1);
        radios.setMinValues(1);
        findAllRadios().getRadios().forEach(radioName -> {
            radioName = radioName.substring(0, 1).toUpperCase() + radioName.substring(1);
            radios.addOption(radioName.toLowerCase(), radioName);
        });
        StringSelectMenu menu = radios.build();


        event.reply("Selecione a radio que deseja ouvir")
                .setEphemeral(true)
                .setActionRow(menu)
                .queue();

        FinnDynamicEventHandler.getInstance().addListener(new SelectRadioHandler(menu, selectEvent -> {
                }), 30, TimeUnit.SECONDS, () -> {
                });

    }


    private RadioResource findAllRadios() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:3066")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RadioService service = retrofit.create(RadioService.class);
        try {
            return service.listRadios().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
