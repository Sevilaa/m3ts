package ch.m3ts.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;

import ch.m3ts.display.FragmentReplaceCallback;
import cz.fmo.R;

@SuppressWarnings("squid:S110")
public class TutorialActivity extends FragmentActivity implements FragmentReplaceCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Fragment nextFragment = new ChooseTutorialFragment();
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


    public void onGoBack(View view) {
        this.onBackPressed();
    }
}
