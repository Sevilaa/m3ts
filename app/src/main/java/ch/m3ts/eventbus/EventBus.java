package ch.m3ts.eventbus;

public interface EventBus {
    void register(Subscribable subscribable);

    void unregister(Subscribable subscribable);

    void dispatch(Event<?> event);
}