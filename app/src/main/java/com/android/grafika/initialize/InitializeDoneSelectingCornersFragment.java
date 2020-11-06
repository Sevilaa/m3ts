package com.android.grafika.initialize;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.grafika.LiveDebugActivity;

import java.lang.ref.WeakReference;

import cz.fmo.R;

/**
 * Use the {@link InitializeDoneSelectingCornersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializeDoneSelectingCornersFragment extends InitializeSelectingCornersFragment implements Button.OnClickListener {
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private static final String MATCH_TYPE_PARAM = "MATCH_TYPE";
    private static final String SERVING_SIDE_PARAM = "SERVING_SIDE";

    private Button btnStart;
    private Point[] corners;

    public InitializeDoneSelectingCornersFragment() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_done;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSelectingCornersFragment.
     */
    public static InitializeDoneSelectingCornersFragment newInstance(InitializeActivity activity) {
        InitializeDoneSelectingCornersFragment fragment = new InitializeDoneSelectingCornersFragment();
        fragment.setActivityWeakReference(new WeakReference<>(activity));
        return fragment;
    }

    @Override
    public void onStateChanged() {
        super.onStateChanged();
        InitializeActivity activity = this.activityWeakReference.get();
        if (activity != null) {
            this.corners = activity.getTableCorners();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.btnStart = view.findViewById(R.id.init_startGameBtn);
        this.btnStart.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        InitializeActivity activity = activityWeakReference.get();
        if (activity != null) {
            Intent intent = new Intent(getContext(), LiveDebugActivity.class);
            Bundle bundle = new Bundle();
            bundle.putIntArray(CORNERS_PARAM, pointsToIntArray(corners));
            bundle.putInt(MATCH_TYPE_PARAM, activity.getSelectedMatchType());
            bundle.putInt(SERVING_SIDE_PARAM, activity.getSelectedServingSide());
            intent.putExtras(bundle);
            startActivity(intent);
            getActivity().finish();
        }
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