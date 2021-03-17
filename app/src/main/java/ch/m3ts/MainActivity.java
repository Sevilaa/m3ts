package ch.m3ts;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.audio.core.Recorder;

import ch.m3ts.display.MatchActivity;
import ch.m3ts.tracker.init.InitTrackerActivity;
import ch.m3ts.tracker.visualization.live.ImplAudioRecorderCallback;
import cz.fmo.R;

/**
 * Starting Activity of the App.
 * A player can here specify whether to use his/her device as a Display or Tracker.
 */
public class MainActivity extends Activity {
    private Recorder audioRecorder;

    private boolean isAudioPermissionDenied() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permissionStatus != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isAudioPermissionDenied()) {
            String[] perms = new String[]{Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(this, perms, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        this.audioRecorder = new Recorder(new ImplAudioRecorderCallback(
                (TextView) findViewById(R.id.txtPlayMovieAmp),
                (TextView) findViewById(R.id.txtPlayMovieFrequency)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        audioRecorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioRecorder.stop();
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