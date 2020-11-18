package cz.fmo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.grafika.initialize.InitializeActivity;

import cz.fmo.display.MatchActivity;

public class MainActivity extends Activity {
    private static final String NO_INTERNET_MESSAGE = "No internet connection!";
    private Button useTrackerBtn;
    private Button useDisplayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useTrackerBtn = findViewById(R.id.mainUseAsTrackerBtn);
        useDisplayBtn = findViewById(R.id.mainUseAsDisplayBtn);
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        checkIfInternetIsAvailable();
    }

    public void onOpenMenu(View toggle) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onUseAsDisplay(View toggle) {
        startActivity(new Intent(this, MatchActivity.class));
    }

    public void onUseAsTracker(View toggle) {
        startActivity(new Intent(this, InitializeActivity.class));
    }

    private void checkIfInternetIsAvailable() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
        if(connected) {
            hasInternetState();
        } else {
            noInternetState();
        }
    }

    private void noInternetState() {
        Toast.makeText(getApplicationContext(), NO_INTERNET_MESSAGE,
                Toast.LENGTH_LONG).show();
        useTrackerBtn.setEnabled(false);
        useDisplayBtn.setEnabled(false);
    }

    private void hasInternetState() {
        useTrackerBtn.setEnabled(true);
        useDisplayBtn.setEnabled(true);
    }
}