package com.alphaapps.instapickercropper.internal.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Juad on 11/1/2017.
 */

public class GalleryAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<Fragment> fragments;
    private String[] tabTitles = new String[]{"Gallery", "Camera"};

    public GalleryAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }


}
