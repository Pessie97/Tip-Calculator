package com.example.pessi.tipcalculator.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.pessi.tipcalculator.R;
import com.example.pessi.tipcalculator.classes.KeyPadController;
import com.example.pessi.tipcalculator.classes.TipCalculator;
import com.example.pessi.tipcalculator.classes.Utils;
import com.google.common.primitives.Doubles;


import java.util.Locale;

import static com.example.pessi.tipcalculator.classes.Utils.sREQUEST_CODE_LOCATION_PERMISSION;
import static com.example.pessi.tipcalculator.classes.Utils.sREQUEST_CODE_SETTINGS;



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

    @SuppressWarnings ("FieldCanBeLocal")
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
        updateCurrentTipCalcObjectOrRevertText ((EditText) mCurrentView);

        if (mCurrentTipCalculator.getSubtotal () > 0) {
            syncAllFieldsWithCurrentTC ();
            if (mCurrentView == mFieldSubTotal && mDefaultTaxPercentage != 0) {
                mFieldTaxAmount.requestFocus ();
                mFieldSubTotal.requestFocus ();
            }
            showResultsSnackBar ();
        }
        else {
            if (fromButton) {
                mSnackBar.setText
                        (getString (R.string.error_calc_subtotal_must_not_be_blank_or_zero))
                        .show ();
            }
        }
    }

    private void syncAllFieldsWithCurrentTC ()
    {
        if (mFieldTaxAmount.getText ().toString ().length () == 0) {
            attemptSetTaxAmountToDefault ();
        }
        if (mFieldTipPercent.getText ().toString ().length () == 0) {
            attemptSetTipPercentToDefault ();
        }
        if (mFieldPayers.getText ().toString ().length () == 0) {
            attemptSetPayersToDefault ();
        }
    }

    private void attemptSetTaxAmountToDefault ()
    {
        saveCurrentViewAndTextBeforeChange (mFieldTaxAmount);
        setTaxAmountToDefault ();
        updateCurrentTipCalcObjectOrRevertText (mFieldTaxAmount);
    }

    private void setTaxAmountToDefault ()
    {
        if (mFieldTaxAmount != null) {
            mFieldTaxAmount.setText (
                    String.format (Locale.getDefault (), mPCT_FORMAT_STRING,
                                   mCurrentTipCalculator.getSubtotal () *
                                           (mDefaultTaxPercentage * .01)));
        }
    }

    private void attemptSetTipPercentToDefault ()
    {
        saveCurrentViewAndTextBeforeChange (mFieldTipPercent);
        setTipPercentETToDefault ();
        updateCurrentTipCalcObjectOrRevertText (mFieldTipPercent);
    }

    private void setTipPercentETToDefault ()
    {
        if (mFieldTipPercent != null) {
            mFieldTipPercent.setText (String.format (Locale.getDefault (),
                                                     mPCT_FORMAT_STRING, mDefaultTipPercentage));
        }
    }

    private void attemptSetPayersToDefault ()
    {
        saveCurrentViewAndTextBeforeChange (mFieldPayers);
        mFieldPayers.setText ("1");
        updateCurrentTipCalcObjectOrRevertText (mFieldPayers);
    }

    private void showResultsSnackBar ()
    {
        Snackbar.make (mSBContainer,
                       getString (R.string.tip_colon_space) +
                               mCurrentTipCalculator.getTipAmountFormattedWithCurrencySymbol (),
                       Snackbar.LENGTH_LONG)
                .setAction (R.string.show_full_results, mLaunchResultsClickListener)
                .show ();
    }

    private void setupContent (Bundle savedInstanceState)
    {
        setupOrRestoreCurrentTCObject (savedInstanceState);
        setupSnackBarAndListener ();
        hideKeyboard ();
        setupFields ();
        setupListenerForEditTexts ();
        mCurrentView = mFieldSubTotal;
        mBackground = findViewById (R.id.img_background);
    }

    private void setupOrRestoreCurrentTCObject (Bundle savedInstanceState)
    {
        mCurrentTipCalculator = savedInstanceState == null ?
                                new TipCalculator () :
                                TipCalculator.restoreObjectFromJSONString
                                        (savedInstanceState.getString (mKEY_CURRENT_CALC));
    }

    private void setupSnackBarAndListener ()
    {
        mSBContainer = findViewById (R.id.activity_main);

        mLaunchResultsClickListener = new View.OnClickListener ()
        {
            @Override public void onClick (View v)
            {
                showResultsActivity ();
            }
        };

        mSnackBar = Snackbar.make (mSBContainer,
                                   R.string.show_full_results,
                                   Snackbar.LENGTH_INDEFINITE);
    }

    private void showResultsActivity ()
    {
        Intent resultsIntent = new Intent (this, ResultsActivity.class);
        try {
            resultsIntent.putExtra (mKEY_CURRENT_CALC,
                                    TipCalculator.getJSONStringFromObject (mCurrentTipCalculator));
            startActivity (resultsIntent);
        }
        catch (IllegalArgumentException iae) {
            //Log.d ("TipCalc", "Data missing.");
        }
    }

    private void setupFields ()
    {
        // EditText Fields
        mFieldSubTotal = findViewById (R.id.editText_subTotal);
        mFieldTipPercent = findViewById (R.id.editText_tipPercent);
        mFieldTaxAmount = findViewById (R.id.editText_taxAmount);
        mFieldPayers = findViewById (R.id.editText_payers);

        mFields = new EditText[] {mFieldSubTotal, mFieldTipPercent, mFieldTaxAmount, mFieldPayers};

        // Arrow buttons
        Button arrowUpSubTotal = findViewById (R.id.subtotal_up);
        Button arrowUpTipPct = findViewById (R.id.tipPercent_up);
        Button arrowUpTaxAmt = findViewById (R.id.taxAmount_up);
        Button arrowUpPayers = findViewById (R.id.payers_up);

        Button arrowDownSubTotal = findViewById (R.id.subtotal_down);
        Button arrowDownTipPct = findViewById (R.id.tipPercent_down);
        Button arrowDownTaxAmt = findViewById (R.id.taxAmount_down);
        Button arrowDownPayers = findViewById (R.id.payers_down);

        mArrowUpButtons = new Button[] {arrowUpSubTotal,
                                        arrowUpTipPct,
                                        arrowUpTaxAmt,
                                        arrowUpPayers};
        mArrowDownButtons = new Button[] {arrowDownSubTotal,
                                          arrowDownTipPct,
                                          arrowDownTaxAmt,
                                          arrowDownPayers};
    }

    private void setupListenerForEditTexts ()
    {
        createListenerForEditTexts ();
        registerListenerWithEditTexts ();
    }

    private void createListenerForEditTexts ()
    {
        mFocusChangeListener = new View.OnFocusChangeListener ()
        {
            @Override public void onFocusChange (View v, boolean hasFocus)
            {
                if (hasFocus) {
                    saveCurrentViewAndTextBeforeChange (v);
                }
                else {
                    updateCurrentTipCalcObjectOrRevertText ((EditText) v);
                    if (v == mFieldSubTotal) {
                        attemptSetTaxAmountToDefault ();
                    }

                    if (mUseAutoCalculate) {
                        calculate (false);
                    }
                }
            }
        };
    }

    private void saveCurrentViewAndTextBeforeChange (View v)
    {
        mCurrentView = v;
        mCurrentTextBeforeChange = ((EditText) v).getText ().toString ();
    }

    private void updateCurrentTipCalcObjectOrRevertText (EditText et)
    {
        String currentText = et.getText ().toString ();
        currentText = currentText.equals ("") ? "0" : currentText;

        if (Doubles.tryParse (currentText) == null) {
            et.setText (mCurrentTextBeforeChange);
        }
        else {
            switch (getCurrentETElement (et)) {
                case 0:
                    mCurrentTipCalculator.setSubtotal (currentText);
                    break;
                case 1:
                    mCurrentTipCalculator.setTipPercent (currentText);
                    break;
                case 2:
                    mCurrentTipCalculator.setTaxAmount (currentText);
                    break;
                case 3:
                    mCurrentTipCalculator.setPayers (currentText);
                    break;
            }
        }
    }

    private int getCurrentETElement (EditText et)
    {
        int i, currentET;
        for (i = 0, currentET = -1; i < mFields.length && currentET == -1; i++) {
            if (et == mFields[i]) {
                currentET = i;
            }
        }
        return currentET;
    }

    private void registerListenerWithEditTexts ()
    {
        for (EditText field : mFields) {
            field.setOnFocusChangeListener (mFocusChangeListener);
        }
    }

    private void hideKeyboard ()
    {
        getWindow ().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                               WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void setDefaultValuesForPreferences ()
    {
        // Before restoring anything, let's set defaults, in case nothing was stored
        PreferenceManager.setDefaultValues (this, R.xml.pref_general_items, false);
        PreferenceManager.setDefaultValues (this, R.xml.pref_display_items, false);
        PreferenceManager.setDefaultValues (this, R.xml.pref_local_items, false);
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

    @Override protected void onStart ()
    {
        super.onStart ();
        restoreFieldsFromSP ();
        restorePreferences ();
        applyPreferences ();
        mFieldSubTotal.requestFocus ();
    }

    private void applyPreferences ()
    {
        Utils.applyNightModePreference (this, mUseNightMode);
        Utils.showHideBackground (mUsePicBackground, mBackground);

        attemptSetTipPercentToDefault ();
        attemptSetTaxAmountToDefault ();
    }

    private void restoreFieldsFromSP ()
    {
        String currentString;
        SharedPreferences settings = getSharedPreferences (mPREFS_FIELDS, MODE_PRIVATE); //MP==0

        currentString =
                settings.getString (mSUBTOTAL_PREF_KEY, mFieldSubTotal.getText ().toString ());

        saveCurrentViewAndTextBeforeChange(mFieldSubTotal);
        mFieldSubTotal.setText (currentString);
        updateCurrentTipCalcObjectOrRevertText (mFieldSubTotal);

        currentString = settings.getString (mPAYERS_PREF_KEY, mFieldPayers.getText ().toString ());

        saveCurrentViewAndTextBeforeChange(mFieldPayers);
        mFieldPayers.setText (currentString);
        updateCurrentTipCalcObjectOrRevertText (mFieldPayers);
    }

    @SuppressWarnings ("ConstantConditions")
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

    @Override protected void onStop ()
    {
        super.onStop ();
        saveFieldsToSP ();
    }

    private void saveFieldsToSP ()
    {
        SharedPreferences settings = getSharedPreferences (mPREFS_FIELDS, MODE_PRIVATE); //MP==0
        SharedPreferences.Editor settingsEditor = settings.edit ();

        settingsEditor.clear ();

        // Tax and tip are derived from values stored automatically via Settings Activity
        // So we need to store only the other two EditTexts
        settingsEditor.putString (mSUBTOTAL_PREF_KEY, mFieldSubTotal.getText ().toString ());
        settingsEditor.putString (mPAYERS_PREF_KEY, mFieldPayers.getText ().toString ());

        settingsEditor.apply ();
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

        switch (id) {
            case R.id.menu_settings:
                showSettings ();
                return true;
            case R.id.menu_calculate:
                calculate (true);
                return true;
            case R.id.menu_clearField:
                clearCurrent ((EditText) mCurrentView);
                return true;
            case R.id.menu_resetAll:
                resetAll ();
                return true;
            default:
                return super.onOptionsItemSelected (item);
        }
    }

    // The following method is called only from the onOptionsItemSelected method
    private void showSettings ()
    {
        // Here, we open up our settings activity
        Intent intent = new Intent (getApplicationContext (), SettingsActivity.class);
        startActivityForResult (intent, sREQUEST_CODE_SETTINGS);
    }

    @SuppressWarnings ("UnusedParameters")
    public void showAbout (MenuItem item)
    {
        Utils.showInfoDialog (MainActivity.this, R.string.about_dialog_title,
                              R.string.about_dialog_banner);
    }

    @SuppressWarnings ("unused")
    public void cmdKeypad (View view)
    {
        handleKeyPadPress ((Button) view);
    }

    private void handleKeyPadPress (Button currentButton)
    {
        dismissSnackBarIfShown ();
        String currentButtonTextString = currentButton.getText ().toString ();
        EditText currentField = mCurrentView == null ? mFieldSubTotal : (EditText) mCurrentView;
        saveCurrentViewAndTextBeforeChange (mCurrentView);
        createOrUpdateKeypadController (currentButtonTextString, currentField, -1);
        mKeyPadController.process ();
    }

    private void createOrUpdateKeypadController (String currentButtonTextString,
                                                 EditText currentField, int currentPosition)
    {
        if (mKeyPadController == null) {
            mKeyPadController = new KeyPadController (getApplicationContext (),
                                                      currentButtonTextString,
                                                      currentField,
                                                      currentPosition);
        }
        else {
            mKeyPadController.setCurrentButtonTextString (currentButtonTextString);
            mKeyPadController.setCurrentET (currentField);
            mKeyPadController.setCurrentPosition (currentPosition);
        }
    }

    private void dismissSnackBarIfShown ()
    {
        if (mSnackBar.isShown ()) {
            mSnackBar.dismiss ();
        }
    }

    @SuppressWarnings ("unused")
    public void cmdArrowUp (View view)
    {
        handleUpDownPress (view, mPLUS_SIGN);
    }

    @SuppressWarnings ("unused")
    public void cmdArrowDown (View view)
    {
        handleUpDownPress (view, mMINUS_SIGN);
    }

    private void handleUpDownPress (View view, String currentButtonTextString)
    {
        dismissSnackBarIfShown ();
        int currentPosition = getElementNumber (view, currentButtonTextString);
        EditText currentField = mFields[currentPosition];
        currentField.requestFocus ();
        saveCurrentViewAndTextBeforeChange (currentField);
        createOrUpdateKeypadController (currentButtonTextString, currentField, currentPosition);
        mKeyPadController.process ();
    }

    private int getElementNumber (View view, String currentButtonTextString)
    {
        boolean upButtonPressed = currentButtonTextString.equals (mPLUS_SIGN);

        // Pick correct array of buttons: all up or all down buttons
        final Button[] CURRENT_ARROWS_ARRAY = upButtonPressed ? mArrowUpButtons : mArrowDownButtons;

        // Get current element in the arrow button array
        // returns ID of calling View, or -1 if button not found in array, which should never happen
        int currentElement = -1;

        // loop through all buttons of either up or down, depending on which was pressed
        for (int i = 0; i < CURRENT_ARROWS_ARRAY.length; i++) {
            if (CURRENT_ARROWS_ARRAY[i] == view) {
                currentElement = i;
            }
        }
        return currentElement;
    }


    @SuppressWarnings ("UnusedParameters") public void cmdClearCurrent (View view)
    {
        clearCurrent (mCurrentView == null ? mFieldSubTotal : (EditText) mCurrentView);
    }

    private void clearCurrent (EditText currentField)
    {
        dismissSnackBarIfShown ();
        clearField (currentField);
    }

    private void clearField (EditText currentField)
    {
        if (currentField == mFieldTipPercent) {
            setTipPercentETToDefault ();
        }
        else if (currentField == mFieldPayers) {
            currentField.setText (getString (R.string.default_payers));
        }
        else if (currentField == mFieldTaxAmount) {
            currentField.setText (getString (R.string.zero_point_zero_zero));
        }
        else {
            currentField.setText ("");
        }
    }

    @SuppressWarnings ("UnusedParameters") public void cmdResetAll (View view)
    {
        resetAll ();
    }

    private void resetAll ()
    {
        dismissSnackBarIfShown ();

        for (EditText field : mFields) {
            clearField (field);
        }

        mFieldSubTotal.requestFocus ();
    }

    // LG work-around for select older devices
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        boolean isOldLG = ((keyCode == KeyEvent.KEYCODE_MENU) &&
                                   (Build.VERSION.SDK_INT <= 16) &&
                                   (Build.MANUFACTURER.compareTo ("LGE") == 0));

        //noinspection SimplifiableConditionalExpression
        return isOldLG ? true : super.onKeyDown (keyCode, event);
    }

    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo ("LGE") == 0)) {
            openOptionsMenu ();
            return true;
        }
        return super.onKeyUp (keyCode, event);
    }
}
