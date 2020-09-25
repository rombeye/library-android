package com.zhihu.matisse.controllers;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class EnglishTypeFactory {
    final String OPENSANS_REGULAR = "fonts/OpenSans-Regular.ttf";
    final String OPENSANS_SEMIBOLD = "fonts/OpenSans-SemiBold.ttf";
    final String OPENSANS_BOLD = "fonts/OpenSans-Bold.ttf";
    final String OPENSANS_LIGHT = "fonts/OpenSans-Light.ttf";
    Typeface regular, semibold, bold, light;

    public EnglishTypeFactory(Context context) {
        regular = Typeface.createFromAsset(context.getAssets(), OPENSANS_REGULAR);
        bold = Typeface.createFromAsset(context.getAssets(), OPENSANS_BOLD);
        semibold = Typeface.createFromAsset(context.getAssets(), OPENSANS_SEMIBOLD);
        light = Typeface.createFromAsset(context.getAssets(), OPENSANS_LIGHT);
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
