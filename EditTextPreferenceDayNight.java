package com.mintedtech.tipcalc_fall18.classes;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.mintedtech.tipcalc_fall18.R;


/**
 * Extends EditTextPreference to make its dialog match day or night
 */

public class EditTextPreferenceDayNight extends EditTextPreference
{

    public EditTextPreferenceDayNight (Context context, AttributeSet attrs, int defStyleAttr)
    {
        super (context, attrs, defStyleAttr);
        context.setTheme (R.style.AppCompatAlertDialogStyle);
    }

    public EditTextPreferenceDayNight (Context context, AttributeSet attrs)
    {
        super (context, attrs);
        context.setTheme (R.style.AppCompatAlertDialogStyle);
    }

    public EditTextPreferenceDayNight (Context context)
    {
        super (context);
        context.setTheme (R.style.AppCompatAlertDialogStyle);
    }


    @TargetApi (Build.VERSION_CODES.LOLLIPOP)
    public EditTextPreferenceDayNight (Context context, AttributeSet attrs, int defStyleAttr,
                                       int defStyleRes)
    {
        super (context, attrs, defStyleAttr, defStyleRes);
        context.setTheme (R.style.AppCompatAlertDialogStyle);
    }
}
