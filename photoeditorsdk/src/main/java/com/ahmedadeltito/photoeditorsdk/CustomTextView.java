package com.ahmedadeltito.photoeditorsdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.ahmedadeltito.photoeditorsdk.controllers.EnglishTypeFactory;

import java.util.Locale;

/**
 * Created by alaaalshammaa on 20/06/2017.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    private int typefaceType;
    public static Context appContext;
    private static EnglishTypeFactory englishTypeFactory;

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
            } else {
                setTypeface(getEnglishTypeFace(typefaceType));
            }
        }
    }

    public void setCustomTypeFaceType(int typefaceType) {
        if (isArabic()) {
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
            case Constants.REGULAR:
                return englishTypeFactory.getRegular();

            case Constants.BOLD:
                return englishTypeFactory.getBold();
            default:
                return englishTypeFactory.getRegular();
        }
    }

    public interface Constants {
        int REGULAR = 1,
                BOLD = 2;
    }

}