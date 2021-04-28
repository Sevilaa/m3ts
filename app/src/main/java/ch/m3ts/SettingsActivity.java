package ch.m3ts;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.List;

import cz.fmo.R;

/**
 * Settings Activity, loads the settings from XML values.
 */
@SuppressWarnings("squid:S110")
public class SettingsActivity extends PreferenceActivity {
    private static final SummaryUpdater sSummaryUpdater = new SummaryUpdater();

    private static void bindToSummaryUpdater(Preference preference, SummaryUpdater updater) {
        preference.setOnPreferenceChangeListener(updater);
        updater.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindToSummaryUpdaterBoolean(Preference preference, SummaryUpdater updater) {
        preference.setOnPreferenceChangeListener(updater);
        updater.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || DebugPreferenceFragment.class.getName().equals(fragmentName)
                || TrackerPreferenceFragment.class.getName().equals(fragmentName)
                || GamePreferenceFragment.class.getName().equals(fragmentName);
    }

    private static class SummaryUpdater implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PreferenceFragmentBase extends PreferenceFragment {
        private int mXmlResourceId = -1;

        void setXmlResourceId(int xmlResourceId) {
            mXmlResourceId = xmlResourceId;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(mXmlResourceId);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class GamePreferenceFragment extends PreferenceFragmentBase {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.setXmlResourceId(R.xml.pref_game);
            super.onCreate(savedInstanceState);
            bindToSummaryUpdater(findPreference(getString(R.string.prefPlayer1Key)), sSummaryUpdater);
            bindToSummaryUpdater(findPreference(getString(R.string.prefPlayer2Key)), sSummaryUpdater);
        }
    }

    public static class TrackerPreferenceFragment extends PreferenceFragmentBase {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.setXmlResourceId(R.xml.pref_tracker);
            super.onCreate(savedInstanceState);
            bindToSummaryUpdaterBoolean(findPreference(getString(R.string.prefUseAudioKey)), sSummaryUpdater);
            bindToSummaryUpdaterBoolean(findPreference(getString(R.string.prefUseBlackSideKey)), sSummaryUpdater);
        }
    }

    public static class DebugPreferenceFragment extends PreferenceFragmentBase {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.setXmlResourceId(R.xml.pref_debug);
            super.onCreate(savedInstanceState);
            bindToSummaryUpdaterBoolean(findPreference(getString(R.string.prefDisplayDebugKey)), sSummaryUpdater);
            bindToSummaryUpdaterBoolean(findPreference(getString(R.string.prefRecordKey)), sSummaryUpdater);
            bindToSummaryUpdaterBoolean(findPreference(getString(R.string.prefPubnubKey)), sSummaryUpdater);
        }
    }
}
