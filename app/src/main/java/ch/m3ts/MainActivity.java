package ch.m3ts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.audio.core.Recorder;

import ch.m3ts.detection.audio.AudioBounceDetectionDebug;
import ch.m3ts.display.MatchActivity;
import ch.m3ts.tracker.init.InitTrackerActivity;
import ch.m3ts.tutorial.TutorialActivity;
import cz.fmo.R;

/**
 * Starting Activity of the App.
 * A player can here specify whether to use his/her device as a Display or Tracker.
 */
public class MainActivity extends Activity {
    private static final String[] perms = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private Recorder audioRecorder;

    private EditText hololensIP;

    public static final String IP_PREFERENCE_KEY = "hololensIP";
    public static final String PREFERENCES_FILE_NAME = "hololensSettings";

    private boolean areAllPermissionsGranted() {
        boolean hasPermission = true;
        for (String permission : perms) {
            int permissionStatus = ContextCompat.checkSelfPermission(this, permission);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        return hasPermission;
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != 0) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getString(R.string.errorMsgPermissions), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!areAllPermissionsGranted()) {
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
        hololensIP = findViewById(R.id.hololensIPText);
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String ip = sharedPref.getString(IP_PREFERENCE_KEY, "");
        hololensIP.setText(ip);
        this.audioRecorder = new Recorder(new AudioBounceDetectionDebug(
                findViewById(R.id.txtPlayMovieFrequency),
                findViewById(R.id.txtAudioBounce)
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
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(IP_PREFERENCE_KEY, hololensIP.getText().toString());
        editor.apply();
        startActivity(new Intent(this, InitTrackerActivity.class));
    }

    public void onStartTutorial(View view) {
        startActivity(new Intent(this, TutorialActivity.class));
    }
}