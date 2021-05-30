package ch.m3ts.display;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import ch.m3ts.FragmentWithReplaceCallback;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.util.Side;
import cz.fmo.R;

/**
 * Fragment which implements the selection of the match settings. Currently the match type (length)
 * and the starting side can be selected. Both of these selected settings will be passed over to the
 * next fragment via Intent.
 */
public class MatchSettingsFragment extends FragmentWithReplaceCallback implements View.OnClickListener {
    private static final String[] MATCH_TYPE = {MatchType.BO1.toString(), MatchType.BO3.toString(), MatchType.BO5.toString()};
    private static final String[] SERVING_SIDES = {Side.LEFT.toString(), Side.RIGHT.toString()};
    private static final String TAG_MATCH_INIT = "MATCH_INIT";
    private final ImageView[] VIEWS_FOR_SIDES = new ImageView[2];
    private final ImageView[] VIEWS_FOR_TYPE = new ImageView[3];
    private int selectedMatchType;
    private int selectedStartingServer;
    private ImageView viewMatchTypeBO1;
    private ImageView viewMatchTypeBO3;
    private ImageView viewMatchTypeBO5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_match_settings, container, false);
        Button doneButton = view.findViewById(R.id.init_sideAndMatchTypeDoneBtn);
        ImageView viewServerLeftSide = view.findViewById(R.id.left_side_server_icon);
        ImageView viewServerRightSide = view.findViewById(R.id.right_side_server_icon);
        viewMatchTypeBO1 = view.findViewById(R.id.match_type_bo1);
        viewMatchTypeBO3 = view.findViewById(R.id.match_type_bo3);
        viewMatchTypeBO5 = view.findViewById(R.id.match_type_bo5);
        VIEWS_FOR_SIDES[0] = viewServerLeftSide;
        VIEWS_FOR_SIDES[1] = viewServerRightSide;
        VIEWS_FOR_TYPE[0] = viewMatchTypeBO1;
        VIEWS_FOR_TYPE[1] = viewMatchTypeBO3;
        VIEWS_FOR_TYPE[2] = viewMatchTypeBO5;

        for (int i = 0; i < VIEWS_FOR_SIDES.length; i++) {
            final int index = i;
            VIEWS_FOR_SIDES[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedStartingServer = index;
                    updateServerIcons();
                }
            });
        }

        for (int i = 0; i < VIEWS_FOR_TYPE.length; i++) {
            final int index = i;
            VIEWS_FOR_TYPE[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedMatchType = index;
                    updateMatchTypeIcons();
                }
            });
        }
        updateServerIcons();
        updateMatchTypeIcons();
        doneButton.setOnClickListener(this);
        return view;
    }

    private void updateServerIcons() {
        for (ImageView v : VIEWS_FOR_SIDES) {
            v.setImageDrawable(getActivity().getDrawable(R.drawable.player_not_server));
        }

        if (SERVING_SIDES[selectedStartingServer].equals(Side.LEFT.toString())) {
            VIEWS_FOR_SIDES[selectedStartingServer].setImageDrawable(getActivity().getDrawable(R.drawable.player_server));
        } else {
            VIEWS_FOR_SIDES[selectedStartingServer].setImageDrawable(getActivity().getDrawable(R.drawable.player_server_right));
        }
    }

    private void updateMatchTypeIcons() {
        if (MATCH_TYPE[selectedMatchType].equals(MatchType.BO1.toString())) {
            viewMatchTypeBO1.setImageDrawable(getActivity().getDrawable(R.drawable.bo1_selected));
        } else {
            viewMatchTypeBO1.setImageDrawable(getActivity().getDrawable(R.drawable.bo1));
        }

        if (MATCH_TYPE[selectedMatchType].equals(MatchType.BO3.toString())) {
            viewMatchTypeBO3.setImageDrawable(getActivity().getDrawable(R.drawable.bo3_selected));
        } else {
            viewMatchTypeBO3.setImageDrawable(getActivity().getDrawable(R.drawable.bo3));
        }

        if (MATCH_TYPE[selectedMatchType].equals(MatchType.BO5.toString())) {
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
        getFragmentReplaceCallback().replaceFragment(fragment, TAG_MATCH_INIT);
    }
}