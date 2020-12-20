package ch.m3ts.display;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ch.m3ts.MainActivity;
import cz.fmo.R;

/**
 * Activity which displays the winner of a table tennis match (info in Intent).
 * Represents the "End State" of the display device where a user needs to decide whether to exit
 * the app or to play another match.
 */
public class MatchWonActivity extends Activity {
    private AnimationDrawable animationDrawable;
    private String pubnubRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_won);
        Bundle bundle = getIntent().getExtras();
        String winner = bundle.getString("winner");
        this.pubnubRoom = bundle.getString("room");
        TextView txtView = findViewById(R.id.winner_name);
        txtView.setText(winner);
        RelativeLayout relativeLayout = findViewById(R.id.won_background);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    /*
     * OnClick of the play again button
     */
    @SuppressWarnings("squid:S1172")
    public void playAgain(View view) {
        animationDrawable.stop();
        Intent intent = new Intent(this, MatchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("room", this.pubnubRoom);
        bundle.putBoolean("isRestartedMatch", true);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    /*
     * OnClick of the main menu button
     */
    @SuppressWarnings("squid:S1172")
    public void backToMenu(View view) {
        animationDrawable.stop();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }
}
