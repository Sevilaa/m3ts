package ch.m3ts.event;

public interface Subscribable {
    void handle(Event<?> event);
}
