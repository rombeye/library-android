package com.yalantis.ucrop.controllers;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class ArabicTypeFactory {
    final String DUBAI_REGULAR = "fonts/Dubai-Regular.ttf";
    final String DUBAI_LIGHT = "fonts/Dubai-Light.ttf";
    final String DUBAI_BOLD = "fonts/Dubai-Bold.ttf";
    final String DUBAI_MEDIUM = "fonts/Dubai-Medium.ttf";
    Typeface regular, semibold, bold, light;

    public ArabicTypeFactory(Context context) {
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