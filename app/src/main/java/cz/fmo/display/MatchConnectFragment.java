package cz.fmo.display;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import cz.fmo.R;

public class MatchConnectFragment extends Fragment implements View.OnClickListener {
    private FragmentReplaceCallback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_match_connect, container, false);
        Button b = v.findViewById(R.id.join_btn);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (FragmentReplaceCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentReplaceListener");
        }
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = new MatchScoreFragment();
        Bundle bundle = new Bundle();
        EditText matchID = getView().findViewById(R.id.pubnub_id);
        bundle.putString("matchID", matchID.getText().toString());
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment);
    }
}
