package ch.m3ts.display;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

import ch.m3ts.Log;
import ch.m3ts.pubnub.DisplayPubNub;
import ch.m3ts.pubnub.PubNubFactory;
import cz.fmo.R;

@SuppressWarnings("squid:S110")
public class MatchActivity extends FragmentActivity implements FragmentReplaceCallback {
    private DisplayPubNub pubNub;
    private Random random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            initPubNub(bundle.getString("room"));
            this.pubNub.onRestartMatch();
            nextFragment = new MatchScoreFragment();
        } else {
            initPubNub(getRandomRoomID(8));
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.mainBackground, nextFragment);
        transaction.commit();
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
}
