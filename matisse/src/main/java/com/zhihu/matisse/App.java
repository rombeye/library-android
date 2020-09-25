package com.zhihu.matisse;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.zhihu.matisse.controllers.ArabicTypeFactory;
import com.zhihu.matisse.controllers.EnglishTypeFactory;

import java.util.Locale;

/**
 * Created by Juad on 8/14/2017.
 */

public class App extends Application {
    public static Context appContext;
    private static EnglishTypeFactory englishTypeFactory;
    private static ArabicTypeFactory arabicTypeFactory;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        Log.v("hello", "in on create app");
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

            case Constants.SEMIBOLD:
                return englishTypeFactory.getSemibold();
            case Constants.LIGHT:
                return englishTypeFactory.getLight();

            default:
                return englishTypeFactory.getRegular();
        }
    }

    public static Typeface getArabicTypeFace(int type) {
        if (arabicTypeFactory == null)
            arabicTypeFactory = new ArabicTypeFactory(getAppContext());

        switch (type) {
            case Constants.REGULAR:
                return arabicTypeFactory.getRegular();

            case Constants.BOLD:
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
