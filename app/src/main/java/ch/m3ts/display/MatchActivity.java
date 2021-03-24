package ch.m3ts.display;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

import ch.m3ts.Log;
import ch.m3ts.connection.ConnectionHelper;
import ch.m3ts.connection.DisplayConnection;
import ch.m3ts.connection.NearbyDisplayConnection;
import ch.m3ts.helper.QuitAlertDialogHelper;
import ch.m3ts.pubnub.DisplayPubNub;
import ch.m3ts.pubnub.PubNubFactory;
import cz.fmo.R;
import cz.fmo.util.Config;

/**
 * Activity which implements main the features of the device used as a display.
 * Features include such as initialising a match, selecting table corners and displaying the
 * current match state as a scoreboard.
 */
@SuppressWarnings("squid:S110")
public class MatchActivity extends FragmentActivity implements FragmentReplaceCallback {
    private DisplayPubNub pubNub;
    private Random random = new SecureRandom();
    private AlertDialog alertDialog;
    private NearbyDisplayConnection nearbyDisplayConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.alertDialog = QuitAlertDialogHelper.makeDialog(this);
        setContentView(R.layout.activity_match);
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Bundle bundle = getIntent().getExtras();
        boolean isRestartedMatch = bundle.getBoolean("isRestartedMatch");
        Fragment nextFragment = new MatchSettingsFragment();
        if(isRestartedMatch) {
            initRestartedMatch(bundle);
            nextFragment = new MatchScoreFragment();
        } else {
            initConnection();
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.mainBackground, nextFragment);
        transaction.commit();
    }

    private void initRestartedMatch(Bundle bundle) {
        Config mConfig = new Config(this);
        if(mConfig.isUsingPubnub()) {
            initPubNub(bundle.getString("room"));
            this.pubNub.onRestartMatch();
        } else {
            this.nearbyDisplayConnection = NearbyDisplayConnection.getInstance();
            this.nearbyDisplayConnection.init(this);
            this.nearbyDisplayConnection.onRestartMatch();
        }
    }

    private void initConnection() {
        Config mConfig = new Config(this);
        if(mConfig.isUsingPubnub()) {
            initPubNub(getRandomRoomID(8));
        } else {
            if (!hasPermissions(this, ConnectionHelper.REQUIRED_PERMISSIONS)) {
                requestPermissions(ConnectionHelper.REQUIRED_PERMISSIONS, ConnectionHelper.REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                this.nearbyDisplayConnection = NearbyDisplayConnection.getInstance();
                this.nearbyDisplayConnection.init(this);
            }
        }
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != ConnectionHelper.REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this,"error", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.mainBackground, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public DisplayPubNub getPubNub() {
        return pubNub;
    }

    public DisplayConnection getConnection() {
        if(new Config(this).isUsingPubnub()) {
            return pubNub;
        } else {
            return nearbyDisplayConnection;
        }
    }

    private void initPubNub(String pubnubRoom) {
        Properties properties = new Properties();
        try (InputStream is = this.getAssets().open("app.properties")) {
            properties.load(is);
            this.pubNub = PubNubFactory.createDisplayPubNub(this.getApplicationContext(), pubnubRoom);
        } catch (IOException ex) {
            Log.d("Failed to load pubnub keys");
        }
    }

    private String getRandomRoomID(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + random.nextInt(rightLimit - leftLimit + 1);
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @Override
    protected void onPause() {
        alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        this.alertDialog.show();
    }

    public NearbyDisplayConnection getNearbyDisplayConnection() {
        return nearbyDisplayConnection;
    }

}
