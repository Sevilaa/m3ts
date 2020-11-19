package cz.fmo.display;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.fmo.R;

public class MatchWonFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_match_won, container, false);
        // Inflate the layout for this fragment
        String winner = getArguments().getString("winner");
        TextView txtView = v.findViewById(R.id.winner_name);
        txtView.setText(winner);
        return v;
    }
}
