package ch.m3ts.tutorial;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.m3ts.FragmentWithReplaceCallback;
import cz.fmo.R;

public class ChooseTutorialFragment extends FragmentWithReplaceCallback {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_tutorial, container, false);
        view.findViewById(R.id.tutorialDisplayBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View unused) {
                onHowToUseDisplay();
            }
        });
        view.findViewById(R.id.tutorialTrackerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View unused) {
                onHowToUseTracker();
            }
        });
        view.findViewById(R.id.tutorialPlayBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View unused) {
                onHowToPlay();
            }
        });
        return view;
    }

    private void onHowToUseDisplay() {
        Fragment fragment = new TutorialStepFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.tutorialTitleKey), getString(R.string.tutorialDisplayBtnLabel));
        bundle.putIntArray(getString(R.string.tutorialImagesKey), new int[]{
                R.drawable.tutorial_display_0,
                R.drawable.tutorial_display_1,
                R.drawable.tutorial_display_2,
                R.drawable.tutorial_display_3,
                R.drawable.tutorial_display_4,
        });
        fragment.setArguments(bundle);
        getFragmentReplaceCallback().replaceFragment(fragment, "TUTORIAL_DISPLAY");
    }

    private void onHowToUseTracker() {
        Fragment fragment = new TutorialStepFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.tutorialTitleKey), getString(R.string.tutorialTrackerBtnLabel));
        bundle.putIntArray(getString(R.string.tutorialImagesKey), new int[]{
                R.drawable.tutorial_tracker_setup_0,
                R.drawable.tutorial_tracker_setup_1,
                R.drawable.tutorial_tracker_setup_2,
        });
        bundle.putIntArray(getString(R.string.tutorialDescriptionsKey), new int[]{
                R.string.tutorialTrackerSetupDescription0,
                R.string.tutorialTrackerSetupDescription1,
                R.string.tutorialTrackerSetupDescription2,
        });
        fragment.setArguments(bundle);
        getFragmentReplaceCallback().replaceFragment(fragment, "TUTORIAL_TRACKER");
    }

    private void onHowToPlay() {
        Fragment fragment = new TutorialStepFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.tutorialTitleKey), getString(R.string.tutorialPlayBtnLabel));
        bundle.putIntArray(getString(R.string.tutorialImagesKey), new int[]{
                R.drawable.tutorial_serve_0
        });
        bundle.putIntArray(getString(R.string.tutorialDescriptionsKey), new int[]{
                R.string.tutorialServeDescription0
        });
        fragment.setArguments(bundle);
        getFragmentReplaceCallback().replaceFragment(fragment, "TUTORIAL_SERVE");
    }
}