package ch.m3ts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import ch.m3ts.display.MatchActivity;
import ch.m3ts.tracker.init.InitTrackerActivity;
import cz.fmo.R;

/**
 * Starting Activity of the App.
 * A player can here specify whether to use his/her device as a Display or Tracker.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    @SuppressWarnings("squid:S1172")
    public void onOpenMenu(View toggle) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @SuppressWarnings("squid:S1172")
    public void onUseAsDisplay(View toggle) {
        Intent intent = new Intent(this, MatchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRestartedMatch", false);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @SuppressWarnings("squid:S1172")
    public void onUseAsTracker(View toggle) {
        startActivity(new Intent(this, InitTrackerActivity.class));
    }
}