package ch.m3ts.display;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import cz.fmo.R;

/**
 * Fragment which implements the selection of the match settings. Currently the match type (length)
 * and the starting side can be selected. Both of these selected settings will be passed over to the
 * next fragment via Intent.
 */
public class MatchSettingsFragment extends android.app.Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String[] MATCH_TYPE = {MatchType.BO1.toString(), MatchType.BO3.toString(), MatchType.BO5.toString()};
    private static final String[] SERVING_SIDES = {Side.LEFT.toString(), Side.RIGHT.toString()};
    private int selectedMatchType;
    private int selectedStartingServer;
    private FragmentReplaceCallback callback;
    private static final String TAG_MATCH_INIT = "MATCH_INIT";

    public MatchSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.init_spinnerSelectMatchType) {
            selectedMatchType = i;
        } else if (adapterView.getId() == R.id.init_spinnerSelectServingSide) {
            selectedStartingServer = i;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (FragmentReplaceCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentReplaceListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_match_settings, container, false);
        Button doneButton = view.findViewById(R.id.init_sideAndMatchTypeDoneBtn);
        doneButton.setOnClickListener(this);
        fillSpinners(view);
        return view;
    }

    private void fillSpinners(View view) {
        Spinner spinnerMatchType = view.findViewById(R.id.init_spinnerSelectMatchType);
        Spinner spinnerServingSide = view.findViewById(R.id.init_spinnerSelectServingSide);
        spinnerServingSide.setOnItemSelectedListener(this);
        spinnerMatchType.setOnItemSelectedListener(this);
        setArrayAdapter(MATCH_TYPE, spinnerMatchType);
        setArrayAdapter(SERVING_SIDES, spinnerServingSide);
    }

    @SuppressWarnings("squid:S2293")
    private void setArrayAdapter(String[] items, Spinner spinner) {
        ArrayAdapter<CharSequence> aaMatchType = new ArrayAdapter<CharSequence>(this.getContext(), R.layout.spinner_item, items);
        aaMatchType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aaMatchType);
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = new MatchInitFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", Integer.toString(selectedMatchType));
        bundle.putString("server", Integer.toString(selectedStartingServer));
        fragment.setArguments(bundle);
        callback.replaceFragment(fragment, TAG_MATCH_INIT);
    }
}