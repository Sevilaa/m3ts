package ch.m3ts.eventbus;

public interface Subscribable {
    void handle(Event<?> event);
}
