package br.dev.erickfthz.finn.core.listeners.events;

@FunctionalInterface
public interface IDynamicHandler<T> {
    void onEvent(T event);
}
