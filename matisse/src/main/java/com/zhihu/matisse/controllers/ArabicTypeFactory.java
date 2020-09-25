package com.zhihu.matisse.controllers;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class ArabicTypeFactory {
    final String KUFI_REGULAR = "fonts/DroidKufiRegular.ttf";
    final String KUFI_BOLD = "fonts/DroidKufiBold.ttf";
    Typeface regular,bold;

    public ArabicTypeFactory(Context context) {
        regular = Typeface.createFromAsset(context.getAssets(), KUFI_REGULAR);
        bold = Typeface.createFromAsset(context.getAssets(), KUFI_BOLD);
    }

    public Typeface getRegular() {
        return regular;
    }


    public Typeface getBold() {
        return bold;
    }
}
