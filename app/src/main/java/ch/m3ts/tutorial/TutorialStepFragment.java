package ch.m3ts.tutorial;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.m3ts.MainActivity;
import cz.fmo.R;

/**
 * Runs the tutorials step by step.
 * Layout of all tutorials are the same, they have a title, an image and a text view for
 * additional explanation (optional).
 * <p>
 * Content of each tutorial needs to be passed into Bundle / Arguments of this Fragment (see ChooseTutorialFragment).
 */
public class TutorialStepFragment extends Fragment {
    private int currentStep = 0;
    private int[] images;
    private int[] descriptions;
    private ImageView tutorialImg;
    private TextView tutorialDesc;

    public TutorialStepFragment() {
        // needs an empty constructor...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_step, container, false);
        images = getArguments().getIntArray(getString(R.string.tutorialImagesKey));
        descriptions = getArguments().getIntArray(getString(R.string.tutorialDescriptionsKey));
        tutorialImg = view.findViewById(R.id.tutorialImg);
        tutorialDesc = view.findViewById(R.id.tutorialDescriptionTxt);
        if (descriptions == null) tutorialDesc.setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.tutorialTitleTxt)).setText(getArguments().getString(getString(R.string.tutorialTitleKey)));
        setContentToViews();
        view.findViewById(R.id.tutorialContinueBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStep >= images.length - 1) {
                    getActivity().startActivity(new Intent(getContext(), MainActivity.class));
                } else {
                    ++currentStep;
                    setContentToViews();
                }
            }
        });

        view.findViewById(R.id.tutorialGoBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStep > 0) {
                    --currentStep;
                    setContentToViews();
                }
            }
        });

        return view;
    }

    private void setContentToViews() {
        tutorialImg.setImageDrawable(getActivity().getDrawable(images[currentStep]));
        if (descriptions != null)
            tutorialDesc.setText(getActivity().getString(descriptions[currentStep]));
    }
}
