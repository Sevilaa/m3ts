package ch.m3ts.eventbus;

public class TTEvent<T> implements Event<T> {
    private final T t;

    public TTEvent(T t) {
        this.t = t;
    }

    @Override
    public T getData() {
        return t;
    }
}
