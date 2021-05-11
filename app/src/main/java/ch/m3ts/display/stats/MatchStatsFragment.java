package ch.m3ts.display.stats;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import ch.m3ts.EventBusSubscribedFragment;
import ch.m3ts.connection.NearbyDisplayConnection;
import ch.m3ts.connection.pubnub.PubNubDisplayConnection;
import ch.m3ts.connection.pubnub.PubNubFactory;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.EventBus;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.RequestStatsData;
import ch.m3ts.eventbus.data.StatsData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.util.Log;
import cz.fmo.R;
import cz.fmo.util.Config;

public class MatchStatsFragment extends EventBusSubscribedFragment implements SurfaceHolder.Callback {
    private PubNubDisplayConnection pubNub;
    private NearbyDisplayConnection nearbyDisplayConnection;
    private String pubnubRoom;
    private MatchStats stats;
    private ProgressDialog loadingSpinner;
    private SurfaceHolder heatMapSurfaceHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_match_stats, container, false);
        this.stats = ((StatsActivity) getActivity()).getStats();
        initHeatMap((SurfaceView) v.findViewById(R.id.heatmap));
        createLoadingSpinner();
        return v;
    }

    private void retrieveStats(Bundle bundle) {
        if (new Config(getActivity()).isUsingPubnub()) {
            this.pubnubRoom = bundle.getString("room");
            initPubNub(this.pubnubRoom);
        } else {
            this.nearbyDisplayConnection = NearbyDisplayConnection.getInstance();
            this.nearbyDisplayConnection.init(getActivity());
        }
        registerEventBus();
        TTEventBus.getInstance().dispatch(new TTEvent<>(new RequestStatsData()));
    }

    private void createLoadingSpinner() {
        loadingSpinner = new ProgressDialog(getActivity());
        loadingSpinner.setMessage(getString(R.string.mstLoadingMsg));
        loadingSpinner.setTitle(R.string.mstLoadingTitle);
        loadingSpinner.setIndeterminate(false);
        loadingSpinner.setCancelable(true);
        loadingSpinner.show();
    }

    private void registerEventBus() {
        Config mConfig = new Config(getActivity());
        EventBus eventBus = TTEventBus.getInstance();
        if (mConfig.isUsingPubnub()) {
            eventBus.register(this.pubNub);
        } else {
            eventBus.register(this.nearbyDisplayConnection);
        }
        eventBus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stats == null) {
            retrieveStats(this.getArguments());
        } else {
            setStatViews(getView());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Config mConfig = new Config(getActivity());
        EventBus eventBus = TTEventBus.getInstance();
        if (mConfig.isUsingPubnub()) {
            eventBus.unregister(this.pubNub);
        } else {
            eventBus.unregister(this.nearbyDisplayConnection);
        }
        eventBus.unregister(this);
        if (this.pubNub != null) this.pubNub.unsubscribe();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        SurfaceView surfaceView = getView().findViewById(R.id.heatmap);
        Canvas heatMapCanvas = surfaceView.getHolder().lockCanvas();
        heatMapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        surfaceView.getHolder().unlockCanvasAndPost(heatMapCanvas);
        tryToDisplayHeatMap();
    }

    private void initPubNub(String pubnubRoom) {
        Properties properties = new Properties();
        try (InputStream is = getActivity().getAssets().open("app.properties")) {
            properties.load(is);
            this.pubNub = PubNubFactory.createDisplayPubNub(getActivity().getApplicationContext(), pubnubRoom);
        } catch (IOException ex) {
            Log.d("Failed to load pubnub keys");
        }
    }

    private void createGameOverviews(View v) {
        if (v.findViewById(R.id.game_stats_overview) == null) {
            for (final GameStats game : this.stats.getGameStats()) {
                LinearLayout container = v.findViewById(R.id.game_overviews);
                View overview = LayoutInflater.from(getActivity()).inflate(R.layout.game_stats_overview, null);
                Button button = overview.findViewById(R.id.show_game_stats);
                final int gameIndex = stats.getGameStats().indexOf(game);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchToGameStats(gameIndex);
                    }
                });
                ((TextView) overview.findViewById(R.id.game_id)).setText(String.format(getString(R.string.gstTitle), this.stats.getGameStats().indexOf(game) + 1));
                ((TextView) overview.findViewById(R.id.game_winner)).setText(String.format(getString(R.string.gstWinner), this.stats.getPlayerName(game.getWinner())));
                container.addView(overview);
            }
        }
    }

    private void switchToGameStats(int gameIndex) {
        GameStatsFragment nextFragment = new GameStatsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("game", gameIndex);
        nextFragment.setArguments(bundle);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.background, nextFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setStatViews(final View v) {
        ((TextView) v.findViewById(R.id.player_left)).setText(stats.getPlayerName(Side.LEFT));
        ((TextView) v.findViewById(R.id.player_right)).setText(stats.getPlayerName(Side.RIGHT));
        ((TextView) v.findViewById(R.id.score)).setText(String.format(getString(R.string.mstScore), stats.getWins(Side.LEFT), stats.getWins(Side.RIGHT)));
        ((TextView) v.findViewById(R.id.fastest_strike_left)).setText(String.format(getString(R.string.mhKmh), (int) stats.getFastestStrike(Side.LEFT)));
        ((TextView) v.findViewById(R.id.fastest_strike_right)).setText(String.format(getString(R.string.mhKmh), (int) stats.getFastestStrike(Side.RIGHT)));
        ((TextView) v.findViewById(R.id.total_duration)).setText(String.format(getString(R.string.mstSeconds), stats.getDuration()));
        ((TextView) v.findViewById(R.id.average_duration)).setText(String.format(getString(R.string.mstSeconds), stats.getAveragePointDuration()));
        ((TextView) v.findViewById(R.id.error_rate)).setText(String.format(getString(R.string.mstPercentage), stats.getErrorRatePercentage()));
        ((TextView) v.findViewById(R.id.strikes_left)).setText(String.valueOf(stats.getStrikes().get(Side.LEFT)));
        ((TextView) v.findViewById(R.id.strikes_right)).setText(String.valueOf(stats.getStrikes().get(Side.RIGHT)));
        createGameOverviews(v);
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof StatsData) {
            StatsData statsData = (StatsData) data;
            this.stats = statsData.getStats();
            ((StatsActivity) getActivity()).setStats(stats);
            final Activity activity = getActivity();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setStatViews(getView());
                }
            });
        }
    }

    private void initHeatMap(SurfaceView surface) {
        surface.setZOrderOnTop(true);
        heatMapSurfaceHolder = surface.getHolder();
        heatMapSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        heatMapSurfaceHolder.addCallback(this);
    }

    private void displayHeatMap(View v) {
        if (this.stats != null) {
            HeatMapHolder heatMapHolder = new HeatMapHolder(v.findViewById(R.id.match_stats), heatMapSurfaceHolder, getActivity().getColor(R.color.primary_light), ((StatsActivity) getActivity()).getStats().getTableCorners());
            for (GameStats game : this.stats.getGameStats()) {
                for (PointData point : game.getPoints()) {
                    for (TrackData track : point.getTracks()) {
                        for (DetectionData detection : track.getDetections()) {
                            heatMapHolder.addDetection(detection);
                        }
                    }
                }
            }
            heatMapHolder.draw();
            loadingSpinner.dismiss();
        }
    }

    private void tryToDisplayHeatMap() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (stats != null) {
                    displayHeatMap(getView());
                    timer.cancel();
                }
            }
        }, 0, 500);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        tryToDisplayHeatMap();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // no implementation needed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // no implementation needed
    }
}
