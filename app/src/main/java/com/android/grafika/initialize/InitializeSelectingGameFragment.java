package com.android.grafika.initialize;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.lang.ref.WeakReference;

import cz.fmo.R;
import cz.fmo.tabletennis.MatchType;
import cz.fmo.tabletennis.Side;

/**
 * Use the {@link InitializeSelectingGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializeSelectingGameFragment extends android.app.Fragment implements AdapterView.OnItemSelectedListener, Button.OnClickListener {
    private static final String[] MATCH_TYPE = {MatchType.BO1.toString(), MatchType.BO3.toString(), MatchType.BO5.toString()};
    private static final String[] SERVING_SIDES = {Side.LEFT.toString(), Side.RIGHT.toString()};
    private WeakReference<InitializeActivity> activityWeakReference;

    public InitializeSelectingGameFragment() {
        // Required empty public constructor
    }

    public static InitializeSelectingGameFragment newInstance(InitializeActivity activity) {
        InitializeSelectingGameFragment fragment = new InitializeSelectingGameFragment();
        fragment.setActivityWeakReference(new WeakReference<>(activity));
        return fragment;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        InitializeActivity activity = this.activityWeakReference.get();
        if (activity != null) {
            if (adapterView.getId() == R.id.init_spinnerSelectMatchType) {
                activity.setSelectedMatchType(i);
            } else if (adapterView.getId() == R.id.init_spinnerSelectServingSide) {
                activity.setSelectedServingSide(i);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_init_select_side, container, false);
        Button doneButton = view.findViewById(R.id.init_sideAndMatchTypeDoneBtn);
        doneButton.setOnClickListener(this);
        fillSpinners(view);
        return view;
    }

    void setActivityWeakReference(WeakReference<InitializeActivity> activityWeakReference) {
        this.activityWeakReference = activityWeakReference;
    }

    private void fillSpinners(View view) {
        Spinner spinnerMatchType = view.findViewById(R.id.init_spinnerSelectMatchType);
        Spinner spinnerServingSide = view.findViewById(R.id.init_spinnerSelectServingSide);
        spinnerServingSide.setOnItemSelectedListener(this);
        spinnerMatchType.setOnItemSelectedListener(this);
        setArrayAdapter(MATCH_TYPE, spinnerMatchType);
        setArrayAdapter(SERVING_SIDES, spinnerServingSide);
    }

    private void setArrayAdapter(String[] items, Spinner spinner) {
        ArrayAdapter<CharSequence> aaMatchType = new ArrayAdapter(this.getContext(), R.layout.spinner_item, items);
        aaMatchType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aaMatchType);
    }

    @Override
    public void onClick(View view) {
        InitializeActivity activity = activityWeakReference.get();
        if (activity != null) {
            activity.onSideAndMatchSelectDone();
        }
    }
}