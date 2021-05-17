package ch.m3ts.display.stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.R;

public class GameHistoryAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<PointData> history;
    private GameStats stats;
    private Map<Side, String> playerNames;

    public GameHistoryAdapter(Context context, GameStats stats, Map<Side, String> playerNames) {
        this.context = context;
        this.stats = stats;
        this.playerNames = playerNames;
        this.history = stats.getPoints();
        int o = 0;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.history.get(listPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        PointData point = (PointData) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.game_history_item, null);
        }
        ((TextView) convertView.findViewById(R.id.winner)).setText(playerNames.get(point.getWinner()));
        ((TextView) convertView.findViewById(R.id.server)).setText(playerNames.get(point.getServer()));
        ((TextView) convertView.findViewById(R.id.decision)).setText(point.getRefereeDecision());
        ((TextView) convertView.findViewById(R.id.strikes)).setText(String.valueOf(point.getStrikes().get(Side.LEFT) + point.getStrikes().get(Side.RIGHT)));
        ((TextView) convertView.findViewById(R.id.duration)).setText(String.format(context.getString(R.string.mstSeconds), point.getDuration()));
        ((TextView) convertView.findViewById(R.id.fastest_strike)).setText(String.format(context.getString(R.string.mhKmh), (int) point.getFastestStrike()));
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.history.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.history.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        PointData point = (PointData) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.game_history_group, null);
        }
        ((TextView) convertView.findViewById(R.id.history_score)).setText(String.format(context.getString(R.string.mhScore), point.getScore(Side.LEFT), point.getScore(Side.RIGHT)));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
