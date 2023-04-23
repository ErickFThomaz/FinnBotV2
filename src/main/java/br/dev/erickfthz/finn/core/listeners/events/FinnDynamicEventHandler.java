package br.dev.erickfthz.finn.core.listeners.events;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FinnDynamicEventHandler implements EventListener {

    private static FinnDynamicEventHandler instance;

    ReentrantLock lock = new ReentrantLock(true);

    public static FinnDynamicEventHandler getInstance() {
        if (instance == null) {
            instance = new FinnDynamicEventHandler();
        }
        return instance;
    }

    private final Set<DynamicHandler<?>> handlers = Collections.synchronizedSet(new LinkedHashSet<>());


    public <T> void addListener(DynamicHandler<T> event) {
        handlers.add(event);
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        try {
            synchronized (handlers) {
                Iterator<DynamicHandler<?>> iterator = handlers.stream().iterator();

                while (iterator.hasNext()) {
                    DynamicHandler<?> next = iterator.next();
                    if (next != null) {
                        if (next.validateEvent(event) && !next.isPersist()) {
                            System.out.println(handlers);
                            handlers.remove(next);
                            System.out.println(handlers);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <J> void listenOf(IDynamicHandler<J> handler) {
        FinnDynamicEventHandler.getInstance().addListener(new DynamicHandler<J>(j -> true) {
            @Override
            public void onEvent(J event) {
                handler.onEvent(event);
            }
        });
    }

}
