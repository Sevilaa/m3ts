package ch.m3ts.event;

import java.util.HashSet;
import java.util.Set;

/**
 * Bus for Table Tennis relevant event transmission.
 */
public class TTEventBus implements EventBus {
    private final Set<Subscribable> subscribables;
    private static TTEventBus instance;

    private TTEventBus() {
        subscribables = new HashSet<>();
    }

    public static TTEventBus getInstance() {
        if (instance == null) {
            instance = new TTEventBus();
        }
        return instance;
    }

    @Override
    public void register(Subscribable subscribable) {
        subscribables.add(subscribable);
    }

    @Override
    public void unregister(Subscribable subscribable) {
        this.subscribables.remove(subscribable);
    }

    /**
     * Dispatches an event to all subscribers (1 to many).
     *
     * @param event to be sent to subscribers
     */
    @Override
    public void dispatch(Event<?> event) {
        for (Subscribable s : subscribables) {
            // subscribers are in separate threads -> make new object for each
            s.handle(new TTEvent<>(event.getData()));
        }
    }
}
