package com.android.grafika.initialize;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.grafika.LiveDebugActivity;

import cz.fmo.R;

/**
 * Use the {@link InitializeDoneSelectingCornersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializeDoneSelectingCornersFragment extends InitializeSelectingCornersFragment {
    private static final String MAX_CORNER_PARAM = "MAX_CORNERS";
    private static final String SELECTED_CORNERS_PARAM = "SELECTED_CORNERS";
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";

    private Button btnStart;
    private int[] corners;

    public InitializeDoneSelectingCornersFragment() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_done;
    }

    public static InitializeSelectingCornersFragment newInstance(String amountOfSelectedCorners, String maxCorners, Point[] corners) {
        InitializeSelectingCornersFragment fragment = new InitializeDoneSelectingCornersFragment();
        Bundle args = new Bundle();
        args.putString(MAX_CORNER_PARAM, maxCorners);
        args.putString(SELECTED_CORNERS_PARAM, amountOfSelectedCorners);
        args.putIntArray(CORNERS_PARAM, InitializeDoneSelectingCornersFragment.pointsToIntArray(corners));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.corners = getArguments().getIntArray(CORNERS_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.btnStart = view.findViewById(R.id.init_startGameBtn);
        this.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LiveDebugActivity.class);
                Bundle bundle = new Bundle();
                bundle.putIntArray(CORNERS_PARAM, corners);
                intent.putExtras(bundle);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return view;
    }

    private static int[] pointsToIntArray(Point[] points) {
        int[] ints = new int[points.length*2];
        for(int i = 0; i<points.length; i++) {
            Point point = points[i];
            ints[i*2] = point.x;
            ints[i*2+1] = point.y;
        }
        return ints;
    }
}