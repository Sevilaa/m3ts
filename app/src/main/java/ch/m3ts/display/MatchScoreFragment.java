package ch.m3ts.display;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

import ch.m3ts.EventBusSubscribedFragment;
import ch.m3ts.connection.DisplayConnection;
import ch.m3ts.connection.pubnub.PubNubDisplayConnection;
import ch.m3ts.event.Event;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEvent;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.StatusUpdateData;
import ch.m3ts.event.data.scoremanipulation.PauseMatch;
import ch.m3ts.event.data.scoremanipulation.PointAddition;
import ch.m3ts.event.data.scoremanipulation.PointDeduction;
import ch.m3ts.event.data.scoremanipulation.ResumeMatch;
import ch.m3ts.event.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import cz.fmo.R;
import cz.fmo.util.Config;

/**
 * Fragment which implements the features of a digital table tennis scoreboard.
 * Displays f.e. the current score, current amount of games won by each sides, the current servers.
 */
public class MatchScoreFragment extends EventBusSubscribedFragment implements DisplayUpdateListener, Subscribable {
    private final int MAX_SCORE = 11;
    private String ttsWin;
    private String ttsSide;
    private String ttsTo;
    private String ttsReadyToServe;
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private DisplayConnection connection;
    private boolean isPaused = false;
    private int gamesNeededToWin;
    private int scoreLeft;
    private int scoreRight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_match_score, container, false);
        this.connection = ((MatchActivity) getActivity()).getConnection();
        initTTS();
        this.mediaPlayer = MediaPlayer.create(getContext(), R.raw.success);
        ImageButton refreshButton = v.findViewById(R.id.btnDisplayRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connection.requestStatusUpdate();
            }
        });

        final ImageButton pauseResumeButton = v.findViewById(R.id.btnPauseResumeReferee);
        pauseResumeButton.setTag("Play");
        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaused) {
                    TTEventBus.getInstance().dispatch(new TTEvent<>(new ResumeMatch()));
                    pauseResumeButton.setTag("Play");
                    pauseResumeButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    TTEventBus.getInstance().dispatch(new TTEvent<>(new PauseMatch()));
                    pauseResumeButton.setTag("Pause");
                    pauseResumeButton.setImageResource(android.R.drawable.ic_media_play);
                }
                isPaused = !isPaused;
            }
        });
        setOnSwipeListener(v);
        return v;
    }

    @Override
    public void onDestroy() {
        if (this.tts != null) {
            tts.stop();
            this.tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onMatchEnded(String winnerName) {
        showMatchEndDialog(winnerName);
    }

    @Override
    public void onScore(final Side side, final int score, final Side nextServer, final Side lastServer) {
        Activity activity = getActivity();
        if (side == Side.LEFT) {
            scoreLeft = score;
        } else {
            scoreRight = score;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setScoreOnTextView(side, score);
                updateIndicationNextServer(nextServer);
                playScoreTTS(nextServer, lastServer);
            }
        });
    }

    @Override
    public void onWin(final Side side, final int wins) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setWinsOnTextView(side, wins);
                if (gamesNeededToWin != wins) {
                    connection.requestStatusUpdate();
                    setScoreOnTextView(Side.LEFT, 0);
                    setScoreOnTextView(Side.RIGHT, 0);
                    tts.speak(ttsWin + " " + getPlayerNameBySide(side), TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }

    @Override
    public void onReadyToServe(Side server) {
        playSound(R.raw.success);
        Activity activity = getActivity();
        if (activity == null) return;

        Side otherSide = Side.getOpposite(server);
        TextView txtView = getServerLabelTextView(activity, server);
        colorTextViewAsActive(txtView);
        txtView = getServerLabelTextView(activity, otherSide);
        colorTextViewAsInactive(txtView);
    }

    @Override
    public void onNotReadyButPlaying() {
        playSound(R.raw.error);
    }

    private void playSound(int audioFileID) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getContext(), audioFileID);
        mediaPlayer.start();
    }

    private void playScoreTTS(Side nextServer, Side lastServer) {
        String scoreLeft = String.valueOf(Integer.parseInt(((TextView) getActivity().findViewById(R.id.left_score)).getText().toString()));
        String scoreRight = String.valueOf(Integer.parseInt(((TextView) getActivity().findViewById(R.id.right_score)).getText().toString()));
        String name;
        if (nextServer == Side.RIGHT) {
            tts.speak(scoreRight + ttsTo + scoreLeft, TextToSpeech.QUEUE_FLUSH, null, null);
            name = getPlayerNameBySide(Side.RIGHT);
        } else {
            tts.speak(scoreLeft + ttsTo + scoreRight, TextToSpeech.QUEUE_FLUSH, null, null);
            name = getPlayerNameBySide(Side.LEFT);
        }

        if (nextServer != lastServer && !isWinningPoint()) {
            tts.speak(ttsReadyToServe + " " + name, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    private boolean isWinningPoint() {
        return Math.abs(scoreRight - scoreLeft) > 1 && (scoreLeft >= MAX_SCORE || scoreRight >= MAX_SCORE);
    }

    private String getPlayerNameBySide(Side side) {
        int nameId;
        if (side == Side.RIGHT) {
            nameId = R.id.right_name;
        } else {
            nameId = R.id.left_name;
        }
        return ((TextView) getActivity().findViewById(nameId)).getText().toString();
    }

    private void initTTS() {
        ttsWin = getResources().getString(R.string.ttsWin);
        ttsSide = getResources().getString(R.string.ttsSide);
        ttsTo = getResources().getString(R.string.ttsTo);
        ttsReadyToServe = getResources().getString(R.string.ttsReadyServe);
        this.tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });
    }

    private void updateIndicationNextServer(Side nextServer) {
        Activity activity = getActivity();
        if (activity == null) return;

        Side otherSide = Side.getOpposite(nextServer);
        TextView txtView = getServerLabelTextView(activity, nextServer);
        SpannableString content = new SpannableString(txtView.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtView.setText(content);
        colorTextViewAsInactive(txtView);
        txtView = getServerLabelTextView(activity, otherSide);
        txtView.setText(txtView.getText().toString());
        colorTextViewAsInactive(txtView);
    }

    private TextView getServerLabelTextView(@NonNull Activity activity, Side server) {
        int idServer = R.id.right_score;
        if (server == Side.LEFT) {
            idServer = R.id.left_score;
        }
        return activity.findViewById(idServer);
    }

    private void colorTextViewAsInactive(TextView txtView) {
        txtView.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_light));
    }

    private void colorTextViewAsActive(TextView txtView) {
        txtView.setTextColor(ContextCompat.getColor(getContext(), R.color.display_serving));
    }

    private void setTextInTextView(int id, final String text) {
        Activity activity = getActivity();
        if (activity == null) return;
        TextView txtView = activity.findViewById(id);
        txtView.setText(text);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnSwipeListener(View v) {
        if (connection != null) {
            v.setOnTouchListener(new OnSwipeListener(this.getContext()) {
                @Override
                public void onSwipeDown(Side swipeSide) {
                    TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(swipeSide)));
                }

                @Override
                public void onSwipeUp(Side swipeSide) {
                    TTEventBus.getInstance().dispatch(new TTEvent<>(new PointAddition(swipeSide)));
                }
            });
        }
    }

    private void setScoreOnTextView(Side side, int score) {
        String scoreToDisplay = String.valueOf(score);
        if (score < 10) scoreToDisplay = "0" + scoreToDisplay;
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_score, scoreToDisplay);
        } else {
            setTextInTextView(R.id.right_score, scoreToDisplay);
        }
    }

    private void setWinsOnTextView(Side side, int wins) {
        String winsToDisplay = "0" + wins;
        if (side == Side.LEFT) {
            setTextInTextView(R.id.left_games, winsToDisplay);
        } else {
            setTextInTextView(R.id.right_games, winsToDisplay);
        }
    }

    private void switchToMatchWonActivity(String winnerName) {
        Intent intent = new Intent(getActivity(), MatchWonActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("winner", winnerName);
        if (new Config(getContext()).isUsingPubnub()) {
            bundle.putString("room", ((PubNubDisplayConnection) this.connection).getRoomID());
        }
        intent.putExtras(bundle);
        while (tts.isSpeaking()) {
        }
        startActivity(intent);
        getActivity().finish();
    }

    private void showMatchEndDialog(final String winnerName) {
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(String.format(getString(R.string.msDialogTitle), winnerName));
                builder.setMessage(getString(R.string.msDialogMsg));
                builder.setPositiveButton(R.string.msDialogContinue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switchToMatchWonActivity(winnerName);
                    }
                });
                builder.setNegativeButton(R.string.msDialogReplay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(getPlayerNameBySide(Side.LEFT).equals(winnerName) ? Side.LEFT : Side.RIGHT)));
                    }
                });
                builder.create().show();
            }
        });
    }

    public void onStatusUpdate(final String playerNameLeft, final String playerNameRight, final int pointsLeft, final int pointsRight, final int gamesLeft, final int gamesRight, final Side nextServer, final int gamesNeeded) {
        Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTextInTextView(R.id.left_name, playerNameLeft);
                setTextInTextView(R.id.right_name, playerNameRight);
                setScoreOnTextView(Side.LEFT, pointsLeft);
                setScoreOnTextView(Side.RIGHT, pointsRight);
                setWinsOnTextView(Side.LEFT, gamesLeft);
                setWinsOnTextView(Side.RIGHT, gamesRight);
                updateIndicationNextServer(nextServer);
                gamesNeededToWin = gamesNeeded;
            }
        });
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData displayData = (ToDisplayData) data;
            displayData.call(this);
        } else if (data instanceof StatusUpdateData) {
            StatusUpdateData updateData = (StatusUpdateData) data;
            this.onStatusUpdate(updateData.getPlayerNameLeft(), updateData.getPlayerNameRight(),
                    updateData.getPointsLeft(), updateData.getPointsRight(), updateData.getGamesLeft(),
                    updateData.getGamesRight(), updateData.getNextServer(), updateData.getGamesNeededToWin());
        }
    }
}
