package com.mintedtech.tipcalc_fall18.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.mintedtech.tipcalc_fall18.R;
import com.mintedtech.tipcalc_fall18.classes.KeyPadController;
import com.mintedtech.tipcalc_fall18.classes.TipCalculator;
import com.mintedtech.tipcalc_fall18.classes.Utils;

import static com.mintedtech.tipcalc_fall18.classes.Utils.sREQUEST_CODE_LOCATION_PERMISSION;
import static com.mintedtech.tipcalc_fall18.classes.Utils.sREQUEST_CODE_SETTINGS;

public class MainActivity extends AppCompatActivity
{
    private TipCalculator mCurrentTipCalculator;
    private KeyPadController mKeyPadController;
    private View mCurrentView, mSBContainer;

    private ImageView mBackground;
    private EditText mFieldSubTotal, mFieldTipPercent, mFieldTaxAmount, mFieldPayers;

    private EditText[] mFields;
    private Button[] mArrowUpButtons, mArrowDownButtons;
    private Snackbar mSnackBar;

    private View.OnFocusChangeListener mFocusChangeListener;

    private View.OnClickListener mLaunchResultsClickListener;
    private String mCurrentTextBeforeChange;

    // fields and key names for the Settings/Preferences
    private boolean mUsePicBackground, mUseNightMode, mUseAutoCalculate;

    private double mDefaultTaxPercentage, mDefaultTipPercentage;

    private final String mPLUS_SIGN = "+", mMINUS_SIGN = "-";
    private final String mPCT_FORMAT_STRING = "%.2f";
    private final String mKEY_CURRENT_CALC = "CURRENT_CALC";
    private final String mSUBTOTAL_PREF_KEY = "SUBTOTAL", mPAYERS_PREF_KEY = "PAYERS";
    private final String mPREFS_FIELDS = "PREFS_FIELDS";

    @Override protected void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState (outState);
        outState.putString (mKEY_CURRENT_CALC,
                            TipCalculator.getJSONStringFromObject (mCurrentTipCalculator));
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        setDefaultValuesForPreferences ();                      // Set defaults before restoring
        restorePreferences ();                                  // This will set mUseNightMode
        Utils.getLocationPermission (this,
                                     sREQUEST_CODE_LOCATION_PERMISSION);  // get actual sunset
        AppCompatDelegate.setDefaultNightMode (mUseNightMode ?
                                               AppCompatDelegate.MODE_NIGHT_AUTO :
                                               AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        setupToolbar ();
        setupContent (savedInstanceState);
        setupFAB ();
    }

    private void setupContent (Bundle savedInstanceState)
    {
    }

    private void setDefaultValuesForPreferences ()
    {
    }

    private void setupToolbar ()
    {
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
    }

    private void setupFAB ()
    {
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick (View view)
            {
                calculate (true);
            }
        });
    }

    private void calculate (boolean fromButton)
    {
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId ();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected (item);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults)
    {
        boolean permissionDenied = grantResults[0] == -1;

        if (requestCode == sREQUEST_CODE_LOCATION_PERMISSION) {
            if (permissionDenied) {
                Utils.promptToAllowPermissionRequest (this);
            }
        }
        else {
            super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        }
    }

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if (requestCode == sREQUEST_CODE_SETTINGS) {
            restorePreferences ();
            applyPreferences ();
        }
        else {
            super.onActivityResult (requestCode, resultCode, data);
        }
    }

    private void restorePreferences ()
    {
        String currentKey;
        String currentDefaultValue;

        // Get handle to custom preferences (not from settings menu)
        // Used for persisting state to storage

        // First, get handle to user settings/preferences
        SharedPreferences defaultSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences (getApplicationContext ());

        // Show Background Picture Preference
        currentKey = getString (R.string.showBackgroundKey);
        mUsePicBackground = defaultSharedPreferences.getBoolean (currentKey, true);

        // Use Night Mode Preference
        currentKey = getString (R.string.useAutoNightModeKey);
        mUseNightMode = defaultSharedPreferences.getBoolean (currentKey, true);

        // Use Auto-Calculate Preference
        currentKey = getString (R.string.useAutoCalculateKey);
        mUseAutoCalculate = defaultSharedPreferences.getBoolean (currentKey, true);

        // Default Tax Percentage Preference
        currentKey = getString (R.string.defaultTaxPercentageKey);
        currentDefaultValue = getString (R.string.defaultTaxPercentageDefaultValue);

        mDefaultTaxPercentage = Double.parseDouble (
                defaultSharedPreferences.getString (currentKey, currentDefaultValue));

        // Default Tip Percentage Preference
        currentKey = getString (R.string.defaultTipPercentageKey);
        currentDefaultValue = getString (R.string.defaultTipPercentageDefaultValue);

        mDefaultTipPercentage = Double.parseDouble (
                defaultSharedPreferences.getString (currentKey, currentDefaultValue));
    }


    private void applyPreferences ()
    {
        Utils.applyNightModePreference (this, mUseNightMode);
        Utils.showHideBackground (mUsePicBackground, mBackground);

        attemptSetTipPercentToDefault ();
        attemptSetTaxAmountToDefault ();
    }

    private void attemptSetTipPercentToDefault ()
    {
    }

    private void attemptSetTaxAmountToDefault ()
    {
    }


}
