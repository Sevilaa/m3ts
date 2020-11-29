package cz.fmo.display;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

import com.android.grafika.Log;
import com.pubnub.api.Pubnub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import cz.fmo.DisplayPubNub;
import cz.fmo.R;

public class MatchActivity extends FragmentActivity implements FragmentReplaceCallback {
    private DisplayPubNub pubNub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        initPubNub();
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.mainBackground, new MatchSettingsFragment());
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

    private void initPubNub() {
        Properties properties = new Properties();
        try (InputStream is = this.getAssets().open("app.properties")) {
            properties.load(is);
            this.pubNub = new DisplayPubNub(getRandomRoomID(8), properties.getProperty("pub_key"), properties.getProperty("sub_key"));
        } catch (IOException ex) {
            Log.d("Failed to load pubnub keys");
        }
    }

    private String getRandomRoomID(int length) {
        Random random = new Random();
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
