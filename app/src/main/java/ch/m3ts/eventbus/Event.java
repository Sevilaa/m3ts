package ch.m3ts.eventbus;

public interface Event<T> {
    T getData();
}