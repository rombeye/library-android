package com.alphaapps.instapickercropper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.alphaapps.instapickercropper.controllers.ArabicTypeFactory;
import com.alphaapps.instapickercropper.controllers.EnglishTypeFactory;

import java.util.Locale;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    private int typefaceType;
    public static Context appContext;
    private static EnglishTypeFactory englishTypeFactory;
    private static ArabicTypeFactory arabicTypeFactory;

    public void setTypefaceType(int typefaceType) {
        this.typefaceType = typefaceType;
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        appContext = this.getContext();
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTextView,
                0, 0);
        try {
            typefaceType = array.getInteger(R.styleable.CustomTextView_font_name, 0);
        } finally {
            array.recycle();
        }
        if (!isInEditMode()) {
            if (isArabic()) {
                setTypeface(getArabicTypeFace(typefaceType));
            } else {
                setTypeface(getEnglishTypeFace(typefaceType));
            }
        }
    }

    public void setCustomTypeFaceType(int typefaceType) {
        if (isArabic()) {
            setTypeface(getArabicTypeFace(typefaceType));
        } else {
            setTypeface(getEnglishTypeFace(typefaceType));
        }
    }

    public static boolean isArabic() {
        return !Locale.getDefault().getLanguage().contentEquals("en");
    }

    public static Context getAppContext() {
        return appContext;
    }
    public static Typeface getEnglishTypeFace(int type) {
        if (englishTypeFactory == null)
            englishTypeFactory = new EnglishTypeFactory(getAppContext());

        switch (type) {
            case App.Constants.REGULAR:
                return englishTypeFactory.getRegular();

            case App.Constants.BOLD:
                return englishTypeFactory.getBold();

            case App.Constants.SEMIBOLD:
                return englishTypeFactory.getSemibold();

            case App.Constants.LIGHT:
                return englishTypeFactory.getLight();

            default:
                return englishTypeFactory.getRegular();
        }
    }

    public static Typeface getArabicTypeFace(int type) {
        if (arabicTypeFactory == null)
            arabicTypeFactory = new ArabicTypeFactory(getAppContext());

        switch (type) {
            case App.Constants.REGULAR:
                return arabicTypeFactory.getRegular();

            case App.Constants.BOLD:
                return arabicTypeFactory.getBold();

            default:
                return arabicTypeFactory.getRegular();
        }
    }

    public interface Constants {
        int REGULAR = 1,
                BOLD = 2,
                SEMIBOLD = 3,
                LIGHT = 4;
    }
}