package ch.m3ts.display.stats;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.visualization.ZPosVisualizer;
import cz.fmo.R;

public class GameStatsFragment extends Fragment implements SurfaceHolder.Callback {
    private GameStats game;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_stats, container, false);
        setViews(v);
        return v;
    }

    private void setViews(View v) {
        int gameIndex = getArguments().getInt("game");
        MatchStats stats = ((StatsActivity) getActivity()).getStats();
        this.game = stats.getGameStats().get(gameIndex);
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
        Canvas canvas = holder.lockCanvas();
        Paint tablePaint = new Paint();
        tablePaint.setColor(getActivity().getColor(R.color.primary_light));
        tablePaint.setStrokeWidth(5f);
        Paint bouncePaint = new Paint();
        bouncePaint.setColor(getActivity().getColor(R.color.primary_light));
        bouncePaint.setAlpha(20);
        RelativeLayout container = getActivity().findViewById(R.id.game_stats);
        int height = container.getHeight();
        float canvasWidth = container.getWidth() * 0.6f;
        ZPosVisualizer zPosVisualizer = new ZPosVisualizer(bouncePaint, tablePaint, (float) container.getWidth() / 2 - canvasWidth / 2, height * 0.01f, canvasWidth);
        zPosVisualizer.drawTableBirdView(canvas);

        Map<Side, Integer> tableCorners = ((StatsActivity) getActivity()).getStats().getTableCorners();
        for (PointData point : this.game.getPoints()) {
            for (TrackData track : point.getTracks()) {
                for (DetectionData detection : track.getDetections()) {
                    if (detection.wasBounce()) {
                    }

                    zPosVisualizer.drawZPos(canvas, detection, tableCorners.get(Side.LEFT), tableCorners.get(Side.RIGHT));
                }
            }
        }
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
