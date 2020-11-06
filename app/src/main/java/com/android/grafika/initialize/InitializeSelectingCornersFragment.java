package com.android.grafika.initialize;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import cz.fmo.R;

public class InitializeSelectingCornersFragment extends android.app.Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private TextView txtMaxCorners;
    private TextView txtSelectedCorners;
    private String maxCorners;
    private String selectedCorners;
    protected WeakReference<InitializeActivity> activityWeakReference;
    protected int layout;

    public InitializeSelectingCornersFragment() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_selecting_corners;
    }

    void setActivityWeakReference(WeakReference<InitializeActivity> activityWeakReference) {
        this.activityWeakReference = activityWeakReference;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.activityWeakReference!= null) onStateChanged();
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

    public void onStateChanged() {
        InitializeActivity activity = this.activityWeakReference.get();
        if (activity != null) {
            maxCorners = String.valueOf(activity.getTableCorners().length);
            selectedCorners = String.valueOf(activity.getCurrentCornerIndex());
        }
    }

    public void updateViews() {
        this.txtSelectedCorners.setText(selectedCorners);
        this.txtMaxCorners.setText(maxCorners);
    }
}