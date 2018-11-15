package com.example.pessi.tipcalculator.activities;


import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.example.pessi.tipcalculator.R;
import com.example.pessi.tipcalculator.activities_lib.AppCompatPreferenceActivity;


import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener ()
            {
                @Override
                public boolean onPreferenceChange (Preference preference, Object value)
                {
                    String stringValue = value.toString ();
                    preference.setSummary (stringValue);
                    return true;
                }
            };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     * @param context
     */
    private static boolean isXLargeTablet (SettingsActivity context)
    {
        return (context.getResources ().getConfiguration ().screenLayout
                        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    @SuppressWarnings ("ConstantConditions")
    private static void bindPreferenceSummaryToValue (Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener (sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        // For us, depending on the Preference type, this will be either a boolean or EditText
        if (preference instanceof SwitchPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange (
                    preference, PreferenceManager.getDefaultSharedPreferences (
                            preference.getContext ()).getBoolean (preference.getKey (), true));
        }
        else {              // EditTextPreference
            sBindPreferenceSummaryToValueListener.onPreferenceChange (
                    preference, PreferenceManager.getDefaultSharedPreferences (
                            preference.getContext ()).getString (preference.getKey (), ""));
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setupActionBar ();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar ()
    {
        ActionBar actionBar = getSupportActionBar ();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled (true);
        }
    }

    @Override
    protected void onPostCreate (Bundle savedInstanceState)
    {
        super.onPostCreate (savedInstanceState);
        if (!isXLargeTablet (this)) {
            // Instead of the Headers Fragment, use our all-in-one PreferenceFragment
            getFragmentManager ()
                    .beginTransaction ()
                    .replace (android.R.id.content, new AllInOnePreferenceFragment ())
                    .commit ();
        }
    }


    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        int id = item.getItemId ();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. We want to end the Activity, if pressed...
            finish ();
            return true;
        }
        return super.onOptionsItemSelected (item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane ()
    {
        return isXLargeTablet (this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders (List<PreferenceActivity.Header> target)
    {
        if (isXLargeTablet (this)) {
            loadHeadersFromResource (R.xml.pref_headers, target);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment (String fragmentName)
    {
        return PreferenceFragment.class.getName ().equals (fragmentName)
                || GeneralPreferenceFragment.class.getName ().equals (fragmentName)
                || LocalPreferenceFragment.class.getName ().equals (fragmentName)
                || DisplayPreferenceFragment.class.getName ().equals (fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate (Bundle savedInstanceState)
        {
            super.onCreate (savedInstanceState);
            addPreferencesFromResource (R.xml.pref_general_items);

            bindPreferenceSummaryToValue (findPreference ("AutoCalculate"));
        }
    }

    /**
     * This fragment shows local preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public static class LocalPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate (Bundle savedInstanceState)
        {
            super.onCreate (savedInstanceState);
            addPreferencesFromResource (R.xml.pref_local_items);

            bindPreferenceSummaryToValue (findPreference ("DefaultTaxPercentage"));
            bindPreferenceSummaryToValue (findPreference ("DefaultTipPercentage"));
        }
    }

    /**
     * This fragment shows display preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public static class DisplayPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate (Bundle savedInstanceState)
        {
            super.onCreate (savedInstanceState);
            addPreferencesFromResource (R.xml.pref_display_items);

            bindPreferenceSummaryToValue (findPreference ("ShowBackground"));
            bindPreferenceSummaryToValue (findPreference ("AutoNightMode"));
        }
    }

    public static class AllInOnePreferenceFragment extends PreferenceFragment
    {

        @Override
        public void onCreate (Bundle savedInstanceState)
        {
            super.onCreate (savedInstanceState);

            addPreferencesFromResources ();
            bindPreferenceSummariesToValues ();
        }

        private void addPreferencesFromResources ()
        {
            // general
            addPreferencesFromResource (R.xml.pref_general_header);
            addPreferencesFromResource (R.xml.pref_general_items);

            // local
            addPreferencesFromResource (R.xml.pref_local_header);
            addPreferencesFromResource (R.xml.pref_local_items);

            //display
            addPreferencesFromResource (R.xml.pref_display_header);
            addPreferencesFromResource (R.xml.pref_display_items);
        }

        private void bindPreferenceSummariesToValues ()
        {
            // Bind the summaries of EditText/Switch preferences to their values.
            // When their values change, their summaries (i.e. sub-texts on screen)
            // are updated to reflect the new value, per the Android Design guidelines.
            bindPreferenceSummaryToValue (findPreference ("AutoCalculate"));
            bindPreferenceSummaryToValue (findPreference ("DefaultTaxPercentage"));
            bindPreferenceSummaryToValue (findPreference ("DefaultTipPercentage"));
            bindPreferenceSummaryToValue (findPreference ("ShowBackground"));
            bindPreferenceSummaryToValue (findPreference ("AutoNightMode"));
        }
    }

}
