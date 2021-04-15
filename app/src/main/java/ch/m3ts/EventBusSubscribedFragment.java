package ch.m3ts;

import android.app.Fragment;

import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEventBus;

public abstract class EventBusSubscribedFragment extends Fragment implements Subscribable {

    @Override
    public void onResume() {
        super.onResume();
        TTEventBus.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        TTEventBus.getInstance().unregister(this);
    }
}
