package com.alphaapps.instapickercropper;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.alphaapps.instapickercropper.controllers.ArabicTypeFactory;
import com.alphaapps.instapickercropper.controllers.EnglishTypeFactory;

import java.util.Locale;

//import com.squareup.leakcanary.LeakCanary;

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
//        setupLeakCanary();

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

    public static int getPXSize(int dp) {
        int px = dp;
        try {
            float density = getAppContext().getResources().getDisplayMetrics().density;
            px = Math.round((float) dp * density);
        } catch (Exception ignored) {
        }
        return px;
    }
//
//
//    protected void setupLeakCanary() {
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        enabledStrictMode();
//        refWatcher = LeakCanary.install(this);
//    }
//
//    private static void enabledStrictMode() {
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
//                .detectAll() //
//                .penaltyLog() //
//                .penaltyDialog()
////                .penaltyDeath() //
//                .build());
//    }
}
