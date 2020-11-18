package cz.fmo.display;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.fmo.R;
import cz.fmo.tabletennis.Side;
import helper.OnSwipeListener;

public class MatchScoreFragment extends Fragment {
    private FragmentReplaceCallback callback;
    private String pubnub = "test";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_match_score, container, false);
        setOnSwipeListener(v);
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

    private void onPoint(Side side, int score) {
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_score, String.valueOf(score));
        } else {
            setTextInTextView(R.id.right_score, String.valueOf(score));
        }
    }

    private void onGameWin(Side side, int wins) {
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_games, String.valueOf(wins));
        } else {
            setTextInTextView(R.id.right_games, String.valueOf(wins));
        }
        onPoint(Side.LEFT, 0);
        onPoint(Side.LEFT, 0);
    }

    private void onMatchWin(Side side) {
        Bundle bundle = new Bundle();
        bundle.putString("winner", "tester");
        Fragment fragment = new MatchWonFragment();
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment);
    }

    private void setTextInTextView(int id, final String text) {
        TextView txtView = getView().findViewById(id);
        txtView.setText(text);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnSwipeListener(View v) {
        if(pubnub != null) {
            v.setOnTouchListener(new OnSwipeListener(this.getContext()) {
                @Override
                public void onSwipeDown(Side swipeSide) {
                    if(pubnub != null) {
                        //pubnub.send("onPointDeduction",swipeSide);
                    }
                }

                @Override
                public void onSwipeUp(Side swipeSide) {
                    if(pubnub != null) {
                        //pubnub.send("onPointAddition",swipeSide);
                    }
                }
            });
        }
    }
}
