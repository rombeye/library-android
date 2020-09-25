package com.alphaapps.instapickercropper.controllers;

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
        regular = Typeface.DEFAULT;
        bold = Typeface.DEFAULT_BOLD;
        semibold = Typeface.DEFAULT;
        light = Typeface.DEFAULT;
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