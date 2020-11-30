package cz.fmo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.grafika.tracker.InitTrackerActivity;

import cz.fmo.display.MatchActivity;

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

    public void onOpenMenu(View toggle) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onUseAsDisplay(View toggle) {
        startActivity(new Intent(this, MatchActivity.class));
    }

    public void onUseAsTracker(View toggle) {
        startActivity(new Intent(this, InitTrackerActivity.class));
    }
}