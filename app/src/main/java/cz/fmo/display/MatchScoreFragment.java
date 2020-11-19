package cz.fmo.display;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cz.fmo.DisplayPubNub;
import cz.fmo.R;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;
import helper.OnSwipeListener;

public class MatchScoreFragment extends Fragment implements UICallback {
    private FragmentReplaceCallback callback;
    private DisplayPubNub pubnub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_match_score, container, false);
        initPubNub(v.getContext(),  getArguments().getString("matchID"));
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

    @Override
    public void onMatchEnded(String winnerName) {
        Bundle bundle = new Bundle();
        bundle.putString("winner", winnerName);
        Fragment fragment = new MatchWonFragment();
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment);
    }

    @Override
    public void onScore(Side side, int score) {
        setScoreTextViews(side, score);
    }

    @Override
    public void onWin(Side side, int wins) {
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_games, String.valueOf(wins));
        } else {
            setTextInTextView(R.id.right_games, String.valueOf(wins));
        }
        setScoreTextViews(Side.LEFT, 0);
        setScoreTextViews(Side.LEFT, 0);
    }

    private void initPubNub(Context context, String matchID) {
        Properties properties = new Properties();
        try (InputStream is = context.getAssets().open("app.properties")) {
            properties.load(is);
        } catch (IOException ex) {
            throw new RuntimeException("No app.properties file found!");
        }
        this.pubnub = new DisplayPubNub(matchID, properties.getProperty("pub_key"), properties.getProperty("sub_key"), this);
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
                        pubnub.onPointDeduction(swipeSide);
                    }
                }

                @Override
                public void onSwipeUp(Side swipeSide) {
                    if(pubnub != null) {
                        pubnub.onPointAddition(swipeSide);
                    }
                }
            });
        }
    }

    private void setScoreTextViews(Side side, int score) {
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_score, String.valueOf(score));
        } else {
            setTextInTextView(R.id.right_score, String.valueOf(score));
        }
    }
}
