package br.dev.erickfthz.finn.core.listeners.events;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FinnDynamicEventHandler implements EventListener {

    private static FinnDynamicEventHandler instance;

    private final ScheduledExecutorService threadpool = new ScheduledThreadPoolExecutor(2);

    private final static Logger logger = LoggerFactory.getLogger(FinnDynamicEventHandler.class);

    public static FinnDynamicEventHandler getInstance() {
        if (instance == null) {
            instance = new FinnDynamicEventHandler();
        }
        return instance;
    }

    private final Set<DynamicHandler<?>> handlers = Collections.synchronizedSet(new LinkedHashSet<>());


    public <T> void addListener(DynamicHandler<T> event) {
        addListener(event, -1, null, null);
    }

    public <T> void addListener(DynamicHandler<T> event, long timeout, TimeUnit unit, Runnable timeoutAction) {
        handlers.add(event);

        if (timeout > 0 && unit != null) {
            threadpool.schedule(() -> {
                try {
                    if (handlers.remove(event) && timeoutAction != null) timeoutAction.run();
                } catch (Exception e) {
                    logger.error("Erro ao rodar evento {}", event.getClass().getSimpleName(), e);
                }
            }, timeout, unit);
        }
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
                            handlers.remove(next);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao remover evento", e);
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
