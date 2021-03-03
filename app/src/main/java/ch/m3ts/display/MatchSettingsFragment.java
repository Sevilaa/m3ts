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
import android.widget.ImageView;
import android.widget.Spinner;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import cz.fmo.R;

/**
 * Fragment which implements the selection of the match settings. Currently the match type (length)
 * and the starting side can be selected. Both of these selected settings will be passed over to the
 * next fragment via Intent.
 */
public class MatchSettingsFragment extends android.app.Fragment implements View.OnClickListener {
    private static final String[] MATCH_TYPE = {MatchType.BO1.toString(), MatchType.BO3.toString(), MatchType.BO5.toString()};
    private static final String[] SERVING_SIDES = {Side.LEFT.toString(), Side.RIGHT.toString()};

    private int selectedMatchType;
    private int selectedStartingServer;
    private FragmentReplaceCallback callback;
    private ImageView viewServerLeftSide;
    private ImageView viewServerRightSide;
    private ImageView viewMatchTypeBO1;
    private ImageView viewMatchTypeBO3;
    private ImageView viewMatchTypeBO5;
    private static final String TAG_MATCH_INIT = "MATCH_INIT";

    public MatchSettingsFragment() {
        // Required empty public constructor
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
        viewServerLeftSide = view.findViewById(R.id.left_side_server_icon);
        viewServerRightSide = view.findViewById(R.id.right_side_server_icon);
        viewMatchTypeBO1 = view.findViewById(R.id.match_type_bo1);
        viewMatchTypeBO3 = view.findViewById(R.id.match_type_bo3);
        viewMatchTypeBO5 = view.findViewById(R.id.match_type_bo5);

        viewServerLeftSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStartingServer = 0;
                updateServerIcons();
            }
        });

        viewServerRightSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStartingServer = 1;
                updateServerIcons();
            }
        });
        viewMatchTypeBO1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMatchType = 0;
                updateMatchTypeIcons();
            }
        });
        viewMatchTypeBO3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMatchType = 1;
                updateMatchTypeIcons();
            }
        });
        viewMatchTypeBO5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMatchType = 2;
                updateMatchTypeIcons();
            }
        });
        updateServerIcons();
        doneButton.setOnClickListener(this);
        return view;
    }

    private void updateServerIcons() {
        if (SERVING_SIDES[selectedStartingServer].equals(Side.LEFT.toString())) {
            viewServerLeftSide.setImageDrawable(getActivity().getDrawable(R.drawable.player_server));
        } else {
            viewServerLeftSide.setImageDrawable(getActivity().getDrawable(R.drawable.player_not_server));
        }

        if (SERVING_SIDES[selectedStartingServer].equals(Side.RIGHT.toString())) {
            viewServerRightSide.setImageDrawable(getActivity().getDrawable(R.drawable.player_server_right));
        } else {
            viewServerRightSide.setImageDrawable(getActivity().getDrawable(R.drawable.player_not_server));
        }
    }

    private void updateMatchTypeIcons() {
       if(MATCH_TYPE[selectedMatchType].equals(MatchType.BO1.toString())) {
           viewMatchTypeBO1.setImageDrawable(getActivity().getDrawable(R.drawable.bo1_selected));
       } else {
           viewMatchTypeBO1.setImageDrawable(getActivity().getDrawable(R.drawable.bo1));
       }

        if(MATCH_TYPE[selectedMatchType].equals(MatchType.BO3.toString())) {
            viewMatchTypeBO3.setImageDrawable(getActivity().getDrawable(R.drawable.bo3_selected));
        } else {
            viewMatchTypeBO3.setImageDrawable(getActivity().getDrawable(R.drawable.bo3));
        }

        if(MATCH_TYPE[selectedMatchType].equals(MatchType.BO5.toString())) {
            viewMatchTypeBO5.setImageDrawable(getActivity().getDrawable(R.drawable.bo5_selected));
        } else {
            viewMatchTypeBO5.setImageDrawable(getActivity().getDrawable(R.drawable.bo5));
        }
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