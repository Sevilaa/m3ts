package com.android.grafika.initialize;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.grafika.LiveDebugActivity;

import cz.fmo.R;
import cz.fmo.tabletennis.MatchType;
import cz.fmo.tabletennis.Side;

/**
 * Use the {@link InitializeFragmentFactory#newDoneInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializeDoneFragment extends InitializeSelectingCornersFragment implements Button.OnClickListener {
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private static final String MATCH_TYPE_PARAM = "MATCH_TYPE";
    private static final String SERVING_SIDE_PARAM = "SERVING_SIDE";
    private static final String MATCH_ID = "MATCH_ID";
    private TextView txtMatchType;
    private TextView txtServerSide;
    private TextView txtMatchID;
    private Point[] corners;
    private MatchType selectedMatchType;
    private Side selectedServerSide;
    private String selectedMatchId;

    public InitializeDoneFragment() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_done;
    }

    @Override
    public void onStateChanged() {
        super.onStateChanged();
        InitializeActivity activity = (InitializeActivity) getActivity();
        this.corners = activity.getTableCorners();
        this.selectedMatchType = MatchType.values()[activity.getSelectedMatchType()];
        this.selectedServerSide = Side.values()[activity.getSelectedServingSide()];
        this.selectedMatchId = activity.getMatchID();
    }

    @Override
    public void updateViews() {
        super.updateViews();
        this.txtMatchType.setText(this.selectedMatchType.toString());
        this.txtServerSide.setText(this.selectedServerSide.toString());
        this.txtMatchID.setText(this.selectedMatchId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Button btnStart = view.findViewById(R.id.init_startGameBtn);
        btnStart.setOnClickListener(this);
        this.txtMatchType = view.findViewById(R.id.init_selectedMatchType);
        this.txtServerSide = view.findViewById(R.id.init_selectedServerSide);
        this.txtMatchID = view.findViewById(R.id.init_selectedMatchId);
        updateViews();
        return view;
    }

    @Override
    public void onClick(View view) {
        InitializeActivity activity = (InitializeActivity) getActivity();
        Intent intent = new Intent(getContext(), LiveDebugActivity.class);
        Bundle bundle = new Bundle();
        bundle.putIntArray(CORNERS_PARAM, pointsToIntArray(corners));
        bundle.putInt(MATCH_TYPE_PARAM, activity.getSelectedMatchType());
        bundle.putInt(SERVING_SIDE_PARAM, activity.getSelectedServingSide());
        bundle.putString(MATCH_ID, activity.getMatchID());
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().finish();
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