package com.alphaapps.instapickercropper.controllers;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class EnglishTypeFactory {
    final String DUBAI_REGULAR = "fonts/Nunito-Regular.ttf";
    final String DUBAI_LIGHT = "fonts/Nunito-Regular.ttf";
    final String DUBAI_BOLD = "fonts/Nunito-SemiBold.ttf";
    final String DUBAI_MEDIUM = "fonts/Nunito-Regular.ttf";
    Typeface regular, semibold, bold, light;

    public EnglishTypeFactory(Context context) {
        regular = Typeface.createFromAsset(context.getAssets(), DUBAI_REGULAR);
        bold = Typeface.createFromAsset(context.getAssets(), DUBAI_BOLD);
        semibold = Typeface.createFromAsset(context.getAssets(), DUBAI_MEDIUM);
        light = Typeface.createFromAsset(context.getAssets(), DUBAI_LIGHT);
    }

    public Typeface getRegular() {
        return regular;
    }

    public Typeface getSemibold() {
        return semibold;
    }

    public Typeface getBold() {
        return bold;
    }

    public Typeface getLight() {
        return light;
    }
}
