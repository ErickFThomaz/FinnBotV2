package br.dev.erickfthz.finn.core.command.interaction.event;

@FunctionalInterface
public interface IDynamicHandler<T> {
    void onEvent(T event);
}
