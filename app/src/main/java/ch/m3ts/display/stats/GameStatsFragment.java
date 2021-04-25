package ch.m3ts.display.stats;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.R;

public class GameStatsFragment extends Fragment {
    private GameStats game;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_stats, container, false);
        setViews(v);
        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setViews(View v) {
        final ExpandableListView history = v.findViewById(R.id.game_history);
        int gameIndex = getArguments().getInt("game");
        MatchStats stats = ((StatsActivity) getActivity()).getStats();
        this.game = stats.getGameStats().get(gameIndex);
        GameHistoryAdapter adapter = new GameHistoryAdapter(getContext(), this.game, stats.getPlayerNames());
        history.setAdapter(adapter);
        Map<Side, PlayerStats> playerStats = this.game.getPlayerStats();
        ((TextView) v.findViewById(R.id.gstTitle)).setText(String.format(getString(R.string.gstTitle), gameIndex + 1));
        ((TextView) v.findViewById(R.id.score)).setText(String.format(getString(R.string.mstScore), playerStats.get(Side.LEFT).getPoints(), playerStats.get(Side.RIGHT).getPoints()));
        ((TextView) v.findViewById(R.id.player_right)).setText(stats.getPlayerName(Side.RIGHT));
        ((TextView) v.findViewById(R.id.player_left)).setText(stats.getPlayerName(Side.LEFT));
        ((TextView) v.findViewById(R.id.fastest_strike_left)).setText(String.format(getString(R.string.mhKmh), (int) playerStats.get(Side.LEFT).getFastestStrike()));
        ((TextView) v.findViewById(R.id.fastest_strike_right)).setText(String.format(getString(R.string.mhKmh), (int) playerStats.get(Side.RIGHT).getFastestStrike()));
        ((TextView) v.findViewById(R.id.strikes_left)).setText(String.valueOf(playerStats.get(Side.LEFT).getStrikes()));
        ((TextView) v.findViewById(R.id.strikes_right)).setText(String.valueOf(playerStats.get(Side.RIGHT).getStrikes()));
        ((TextView) v.findViewById(R.id.total_duration)).setText(String.format(getString(R.string.mstSeconds), this.game.getDuration()));
        ((TextView) v.findViewById(R.id.average_duration)).setText(String.format(getString(R.string.mstSeconds), this.game.getAveragePointDuration()));
        ((TextView) v.findViewById(R.id.error_rate)).setText(String.format(getString(R.string.mstPercentage), this.game.getErrorRatePercentage()));

        final ScrollView scroll = v.findViewById(R.id.game_stats_scroll);
        history.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scroll.requestDisallowInterceptTouchEvent(true);
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_UP) {
                    scroll.requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }
}
