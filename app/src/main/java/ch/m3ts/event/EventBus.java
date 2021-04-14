package ch.m3ts.event;

public interface EventBus {
    void register(Subscribable subscribable);

    void unregister(Subscribable subscribable);

    void dispatch(Event<?> event);
}