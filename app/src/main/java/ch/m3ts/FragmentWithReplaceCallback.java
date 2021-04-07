package ch.m3ts;

import android.app.Fragment;
import android.content.Context;

import ch.m3ts.display.FragmentReplaceCallback;

public class FragmentWithReplaceCallback extends Fragment {
    private FragmentReplaceCallback fragmentReplaceCallback;

    public FragmentWithReplaceCallback() {
    }

    public FragmentReplaceCallback getFragmentReplaceCallback() {
        return fragmentReplaceCallback;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            fragmentReplaceCallback = (FragmentReplaceCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentReplaceListener");
        }
    }
}
