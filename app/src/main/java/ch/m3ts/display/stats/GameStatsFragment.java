package ch.m3ts.display.stats;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

import ch.m3ts.display.stats.data.DetectionData;
import ch.m3ts.display.stats.data.GameData;
import ch.m3ts.display.stats.data.MatchData;
import ch.m3ts.display.stats.data.PlayerData;
import ch.m3ts.display.stats.data.PointData;
import ch.m3ts.display.stats.data.TrackData;
import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.R;

public class GameStatsFragment extends Fragment implements SurfaceHolder.Callback {
    private GameData game;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_stats, container, false);
        setViews(v);
        return v;
    }

    private void setViews(View v) {
        int gameIndex = getArguments().getInt("game");
        MatchData stats = ((StatsActivity) getActivity()).getStats();
        this.game = stats.getGameStats().get(gameIndex);
        Map<Side, PlayerData> playerStats = this.game.getPlayerStats();
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
        initHistory(v);
        initHeatMap((SurfaceView) v.findViewById(R.id.heatmap));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initHistory(View v) {
        final ScrollView scroll = v.findViewById(R.id.game_stats_scroll);
        final ExpandableListView history = v.findViewById(R.id.game_history);
        GameHistoryAdapter adapter = new GameHistoryAdapter(getContext(), this.game, ((StatsActivity) getActivity()).getStats().getPlayerNames());
        history.setAdapter(adapter);
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

    private void initHeatMap(SurfaceView surface) {
        surface.setZOrderOnTop(true);
        SurfaceHolder holder = surface.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawHeatMap(holder);
    }

    private void drawHeatMap(SurfaceHolder holder) {
        HeatMapHolder heatMapHolder = new HeatMapHolder(getActivity().findViewById(R.id.game_stats), holder, getActivity().getColor(R.color.primary_light), ((StatsActivity) getActivity()).getStats().getTableCorners());
        for (PointData point : this.game.getPoints()) {
            for (TrackData track : point.getTracks()) {
                for (DetectionData detection : track.getDetections()) {
                    heatMapHolder.addDetection(detection);
                }
            }
        }
        heatMapHolder.draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
