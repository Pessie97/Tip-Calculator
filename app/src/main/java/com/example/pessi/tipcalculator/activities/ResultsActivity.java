package com.example.pessi.tipcalculator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.pessi.tipcalculator.R;
import com.example.pessi.tipcalculator.classes.TipCalculator;

import com.example.pessi.tipcalculator.classes.Utils;


import static com.example.pessi.tipcalculator.classes.Utils.sREQUEST_CODE_SETTINGS;


public class ResultsActivity extends AppCompatActivity
{

    private TipCalculator mCurrentCalc;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_results);
        setupActionBar ();
        setupContent ();
        setupFAB ();
    }

    private void setupActionBar ()
    {
        Toolbar toolbar = findViewById (R.id.toolbar);
        getDelegate ().setSupportActionBar (toolbar);
        ActionBar currentAB = getDelegate ().getSupportActionBar ();

        if (currentAB != null) {
            currentAB.setDisplayHomeAsUpEnabled (true);
        }
    }

    private void setupContent ()
    {
        Intent intent = getIntent ();
        mCurrentCalc = TipCalculator.restoreObjectFromJSONString
                (intent.getStringExtra ("CURRENT_CALC"));
        // just in case
        mCurrentCalc = mCurrentCalc != null ? mCurrentCalc : new TipCalculator ();

        restoreDataFromIntent (intent);
    }

    private void setupFAB ()
    {
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick (View view)
            {
                finish ();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        // The only item we have here, aside for Home/Back, is About.
        getMenuInflater ().inflate (R.menu.menu_results, menu);

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
            case android.R.id.home: {
                finish ();
                return true;
            }
            case R.id.menu_settings:
                showSettings ();
                return true;
            default:
                return super.onOptionsItemSelected (item);
        }
    }

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if (requestCode == sREQUEST_CODE_SETTINGS) {
            finish ();
        }
        else {
            super.onActivityResult (requestCode, resultCode, data);
        }
    }


    // The following method is called only from the onOptionsItemSelected method
    private void showSettings ()
    {
        // Here, we open up our settings activity
        Intent intent = new Intent (getApplicationContext (), SettingsActivity.class);
        startActivityForResult (intent, sREQUEST_CODE_SETTINGS);
    }

    @SuppressWarnings ("UnusedParameters") public void showAbout (MenuItem item)
    {
        Utils.showInfoDialog (ResultsActivity.this, R.string.about_dialog_title,
                              R.string.about_dialog_banner);
    }


    @SuppressWarnings ("UnusedParameters") private void restoreDataFromIntent (Intent intent)
    {
// Please make sure the IDs you use here match those in this Activity's Layout XML
        TextView TV_PayersAmount = findViewById (R.id.results_payers_amount);

        TextView TV_SubtotalAmount = findViewById (R.id.results_subtotal_amount);
        TextView TV_SubtotalPerPerson = findViewById (R.id.results_subtotal_per_person_amount);

        TextView TV_TipPercentage = findViewById (R.id.results_tip_percentage_amount);
        TextView TV_TipTotal = findViewById (R.id.results_tip_total_amount);
        TextView TV_TipPerPerson = findViewById (R.id.results_tip_per_person_amount);

        TextView TV_TaxPercentage = findViewById (R.id.results_tax_percentage_amount);
        TextView TV_TaxTotal = findViewById (R.id.results_tax_total_amount);
        TextView TV_TaxPerPerson = findViewById (R.id.results_tax_per_person_amount);

        TextView TV_TotalAmount = findViewById (R.id.results_total_amount);
        TextView TV_TotalPerPerson = findViewById (R.id.results_total_per_person_amount);

        // Please ensure that the keys specified here match those in Main Activity.
        String payers = mCurrentCalc.getPayersString ();
        String subtotal = mCurrentCalc.getSubTotalFormattedWithCurrencySymbol ();
        String subtotalPerPerson = mCurrentCalc.getSubTotalPerPersonFormattedWithCurrencySymbol ();
        String tipPercent = mCurrentCalc.getTipPercentFormatted ();
        String tipTotal = mCurrentCalc.getTipAmountFormattedWithCurrencySymbol ();
        String tipPerPerson = mCurrentCalc.getTipAmountPerPersonFormattedWithCurrencySymbol ();
        String taxPercent = mCurrentCalc.getTaxPercentFormatted ();
        String taxTotal = mCurrentCalc.getTaxAmountFormattedWithCurrencySymbol ();
        String taxPerPerson = mCurrentCalc.getTaxAmountPerPersonFormattedWithCurrencySymbol ();
        String total = mCurrentCalc.getTotalFormattedWithCurrencySymbol ();
        String totalPerPerson = mCurrentCalc.getTotalPerPersonFormattedWithCurrencySymbol ();

        // Set the TextViews to contain the incoming data
        assert TV_PayersAmount != null;
        TV_PayersAmount.setText (payers);
        assert TV_SubtotalAmount != null;
        TV_SubtotalAmount.setText (subtotal);
        assert TV_SubtotalPerPerson != null;
        TV_SubtotalPerPerson.setText (subtotalPerPerson);
        assert TV_TipPercentage != null;
        TV_TipPercentage.setText (tipPercent);
        assert TV_TipTotal != null;
        TV_TipTotal.setText (tipTotal);
        assert TV_TipPerPerson != null;
        TV_TipPerPerson.setText (tipPerPerson);
        assert TV_TaxPercentage != null;
        TV_TaxPercentage.setText (taxPercent);
        assert TV_TaxPerPerson != null;
        TV_TaxPerPerson.setText (taxPerPerson);
        assert TV_TaxTotal != null;
        TV_TaxTotal.setText (taxTotal);
        assert TV_TotalAmount != null;
        TV_TotalAmount.setText (total);
        assert TV_TotalPerPerson != null;
        TV_TotalPerPerson.setText (totalPerPerson);
    }
}
