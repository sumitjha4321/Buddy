package com.example.sumit.buddy.gallery;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class ScrollSlidePagerAdapter extends FragmentStatePagerAdapter {

    private List<Integer> mediaList;


    public ScrollSlidePagerAdapter(FragmentManager fm, List<Integer> medias) {
        super(fm);
        mediaList = medias;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.getInstance(mediaList.get(position));
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }


}
