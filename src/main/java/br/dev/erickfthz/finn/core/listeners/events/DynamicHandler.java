package br.dev.erickfthz.finn.core.listeners.events;

import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Predicate;

@Getter
@SuppressWarnings("unchecked")
public abstract class DynamicHandler<T> implements IDynamicHandler<T> {

    private final Predicate<T> eventPredicate;

    private boolean persist = true;

    public DynamicHandler(Predicate<T> eventPredicate) {
        this.eventPredicate = eventPredicate;
    }

    public DynamicHandler(Predicate<T> eventPredicate, boolean persist) {
        this.eventPredicate = eventPredicate;
        this.persist = persist;
    }


    public final boolean validateEvent(GenericEvent event) {
        try {
            if (eventPredicate.test((T) event)) {
                onEvent((T) event);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }


}
