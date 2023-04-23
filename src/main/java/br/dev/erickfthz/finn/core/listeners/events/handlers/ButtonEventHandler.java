package br.dev.erickfthz.finn.core.listeners.events.handlers;

import br.dev.erickfthz.finn.core.listeners.events.DynamicHandler;
import br.dev.erickfthz.finn.core.listeners.events.IDynamicHandler;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
public class ButtonEventHandler extends DynamicHandler<ButtonInteractionEvent> {

    private final IDynamicHandler<ButtonInteractionEvent> handler;


    private ButtonEventHandler(Button button, IDynamicHandler<ButtonInteractionEvent> handler) {
        super(event -> Objects.equals(event.getButton().getId(), button.getId()), false);
        this.handler = handler;

    }

    public static ButtonEventHandler handle(Button button, IDynamicHandler<ButtonInteractionEvent> handler) {
        return new ButtonEventHandler(button, handler);
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        handler.onEvent(event);
    }
}