package ch.m3ts.display;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import ch.m3ts.MainActivity;
import ch.m3ts.display.stats.StatsActivity;
import cz.fmo.R;
import cz.fmo.util.Config;

/**
 * Activity which displays the winner of a table tennis match (info in Intent).
 * Represents the "End State" of the display device where a user needs to decide whether to exit
 * the app or to play another match.
 */
public class MatchWonActivity extends Activity {
    private AnimationDrawable animationDrawable;
    private String pubnubRoom;
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_won);
        Bundle bundle = getIntent().getExtras();
        String winner = bundle.getString("winner");
        if (new Config(this).isUsingPubnub()) {
            this.pubnubRoom = bundle.getString("room");
        }
        TextView txtView = findViewById(R.id.winner_name);
        txtView.setText(winner);
        RelativeLayout relativeLayout = findViewById(R.id.won_background);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        playMatchWonTTS(winner);
    }

    /*
     * OnClick of the play again button
     */
    @SuppressWarnings("squid:S1172")
    public void playAgain(View view) {
        animationDrawable.stop();
        Intent intent = new Intent(this, MatchActivity.class);
        Bundle bundle = new Bundle();
        if (this.pubnubRoom != null) {
            bundle.putString("room", this.pubnubRoom);
        }
        bundle.putBoolean("isRestartedMatch", true);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    public void showStats(View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        Bundle bundle = new Bundle();
        if (new Config(this).isUsingPubnub()) {
            bundle.putString("room", this.pubnubRoom);
        }
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /*
     * OnClick of the main menu button
     */
    @SuppressWarnings("squid:S1172")
    public void backToMenu(View view) {
        animationDrawable.stop();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        animationDrawable.stop();
        startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        this.finish();
    }

    @Override
    public void onPause() {
        if (this.tts != null) {
            this.tts.shutdown();
        }
        super.onPause();
    }

    private void playMatchWonTTS(final String winner) {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    final String ttsMatchWon = getResources().getString(R.string.ttsMatchWin);
                    tts.setLanguage(Locale.ENGLISH);
                    tts.speak(winner + ttsMatchWon, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }
}
