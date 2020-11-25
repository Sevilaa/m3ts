package cz.fmo.display;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.grafika.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import cz.fmo.DisplayPubNub;
import cz.fmo.R;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;
import helper.OnSwipeListener;

public class MatchScoreFragment extends Fragment implements UICallback, DisplayEventCallback {
    private static final String TAG_MATCH_ENDED = "MATCH_WON";
    private String ttsWin;
    private String ttsPoints;
    private String ttsPoint;
    private String ttsSide;
    private String ttsReadyToServe;
    private TextToSpeech tts;
    private FragmentReplaceCallback callback;
    private DisplayPubNub pubNub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_match_score, container, false);
        initPubNub(v.getContext(), getArguments().getString("matchID"));
        initTTS();
        ImageButton refreshButton = v.findViewById(R.id.btnDisplayRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pubNub.requestStatusUpdate();
            }
        });
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
        callback.replaceFragment(fragment, TAG_MATCH_ENDED);
    }

    @Override
    public void onScore(Side side, int score, Side nextServer) {
        setScoreTextViews(side, score);
        updateIndicationNextServer(nextServer, false);
        if (score == 1) tts.speak(score + ttsPoint +side.toString()+ ttsSide, TextToSpeech.QUEUE_FLUSH, null, null);
        else tts.speak(score + ttsPoints +side.toString()+ ttsSide, TextToSpeech.QUEUE_FLUSH, null, null);
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
        tts.speak(ttsWin +side+ ttsSide, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onReadyToServe(Side server) {
        tts.speak(server.toString() + ttsSide + ttsReadyToServe, TextToSpeech.QUEUE_ADD, null, null);
        Activity activity = getActivity();
        if (activity == null) return;

        Side otherSide = Side.RIGHT;
        if (server == Side.RIGHT) otherSide = Side.LEFT;

        TextView txtView = getServerLabelTextView(activity, server);
        colorTextViewAsActive(txtView);
        txtView = getServerLabelTextView(activity, otherSide);
        colorTextViewAsInactive(txtView);
    }

    private void initTTS() {
        ttsWin = getResources().getString(R.string.ttsWin);
        ttsPoints = getResources().getString(R.string.ttsPoints);
        ttsPoint = getResources().getString(R.string.ttsPoint);
        ttsSide = getResources().getString(R.string.ttsSide);
        ttsReadyToServe = getResources().getString(R.string.ttsReadyServe);
        this.tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });
    }

    private void updateIndicationNextServer(Side nextServer, boolean init) {
        Activity activity = getActivity();
        if (activity == null) return;

        Side otherSide = Side.RIGHT;
        if (nextServer == Side.RIGHT) otherSide = Side.LEFT;

        TextView txtView = getServerLabelTextView(activity, nextServer);
        SpannableString content = new SpannableString(txtView.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtView.setText(content);
        if (init) colorTextViewAsActive(txtView);
        else colorTextViewAsInactive(txtView);
        txtView = getServerLabelTextView(activity, otherSide);
        txtView.setText(txtView.getText().toString());
        colorTextViewAsInactive(txtView);
    }

    private TextView getServerLabelTextView(@NonNull Activity activity, Side server) {
        int idServer = R.id.right_name;
        if (server == Side.LEFT) {
            idServer = R.id.left_name;
        }
        return activity.findViewById(idServer);
    }

    private void colorTextViewAsInactive(TextView txtView) {
        txtView.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
    }

    private void colorTextViewAsActive(TextView txtView) {
        txtView.setTextColor(getResources().getColor(R.color.display_serving));
    }

    private void initPubNub(Context context, String matchID) {
        Properties properties = new Properties();
        try (InputStream is = context.getAssets().open("app.properties")) {
            properties.load(is);
            this.pubNub = new DisplayPubNub(matchID, properties.getProperty("pub_key"), properties.getProperty("sub_key"), this, this);
        } catch (IOException ex) {
            Log.d("Using MatchScoreFragment in without app.properties file!");
        }
    }

    private void setTextInTextView(int id, final String text) {
        Activity activity = getActivity();
        if (activity == null) return;
        TextView txtView = activity.findViewById(id);
        txtView.setText(text);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnSwipeListener(View v) {
        if(pubNub != null) {
            v.setOnTouchListener(new OnSwipeListener(this.getContext()) {
                @Override
                public void onSwipeDown(Side swipeSide) {
                    if(pubNub != null) {
                        pubNub.onPointDeduction(swipeSide);
                    }
                }

                @Override
                public void onSwipeUp(Side swipeSide) {
                    if(pubNub != null) {
                        pubNub.onPointAddition(swipeSide);
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


    @Override
    public void onStatusUpdate(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight, Side nextServer) {
        setTextInTextView(R.id.left_name, playerNameLeft);
        setTextInTextView(R.id.right_name, playerNameRight);
        setTextInTextView(R.id.left_score, String.valueOf(pointsLeft));
        setTextInTextView(R.id.right_score, String.valueOf(pointsRight));
        setTextInTextView(R.id.left_games, String.valueOf(gamesLeft));
        setTextInTextView(R.id.right_games, String.valueOf(gamesRight));
        updateIndicationNextServer(nextServer, true);
    }

    @Override
    public void onImageReceived(JSONObject jsonObject) {
        // TODO implement corner selection in here
    }
}
