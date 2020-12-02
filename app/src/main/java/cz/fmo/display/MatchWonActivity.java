package cz.fmo.display;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cz.fmo.R;

public class MatchWonActivity extends Activity {
    private AnimationDrawable animationDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_won);
        Bundle bundle = getIntent().getExtras();
        String winner = bundle.getString("winner");
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
    public void playAgain(View view) {
        animationDrawable.stop();
        this.finish();
        onBackPressed();
    }
}
