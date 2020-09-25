package com.ahmedadeltito.photoeditorsdk.controllers;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class ArabicTypeFactory {
    final String REGULAR = "fonts/SuisseIntl-Regular.otf";
    final String BOLD = "fonts/SuisseIntl-SemiBold.otf";
    Typeface regular, bold;

    public ArabicTypeFactory(Context context) {
        regular = Typeface.createFromAsset(context.getAssets(), REGULAR);
        bold = Typeface.createFromAsset(context.getAssets(), BOLD);
    }

    public Typeface getRegular() {
        return regular;
    }

    public Typeface getBold() {
        return bold;
    }
}