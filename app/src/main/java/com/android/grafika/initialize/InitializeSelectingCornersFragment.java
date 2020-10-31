package com.android.grafika.initialize;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.fmo.R;

/**
 * Use the {@link InitializeSelectingCornersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializeSelectingCornersFragment extends android.app.Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MAX_CORNER_PARAM = "MAX_CORNERS";
    private static final String SELECTED_CORNERS_PARAM = "SELECTED_CORNERS";
    private TextView txtMaxCorners;
    private TextView txtSelectedCorners;
    private String maxCorners;
    private String selectedCorners;
    protected int layout;

    public InitializeSelectingCornersFragment() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_selecting_corners;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSelectingCornersFragment.
     */
    public static InitializeSelectingCornersFragment newInstance(String amountOfSelectedCorners, String maxCorners) {
        InitializeSelectingCornersFragment fragment = new InitializeSelectingCornersFragment();
        Bundle args = new Bundle();
        args.putString(MAX_CORNER_PARAM, maxCorners);
        args.putString(SELECTED_CORNERS_PARAM, amountOfSelectedCorners);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maxCorners = getArguments().getString(MAX_CORNER_PARAM);
            selectedCorners = getArguments().getString(SELECTED_CORNERS_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(this.layout, container, false);
        this.txtMaxCorners = view.findViewById(R.id.init_cornersSelectedMaxTxt);
        this.txtSelectedCorners = view.findViewById(R.id.init_cornersSelectedTxt);
        this.txtMaxCorners.setText(maxCorners);
        this.txtSelectedCorners.setText(selectedCorners);
        return view;
    }

    public void setSelectedCornersText(int selectedCorners) {
        this.txtSelectedCorners.setText(String.valueOf(selectedCorners));
    }

    public void setMaxCornersText(int maxCorners) {
        this.txtMaxCorners.setText(String.valueOf(maxCorners));
    }
}