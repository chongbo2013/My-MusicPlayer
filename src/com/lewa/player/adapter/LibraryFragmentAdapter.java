package com.lewa.player.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 13-12-3.
 */
public class LibraryFragmentAdapter extends FragmentPagerAdapter {

    //这个是存放三个Fragment的数组，待会从LibraryActivity中传过来就行了
    private List<Fragment> fragmentArray;
    private Fragment mCurrentPrimaryItem = null;

    //自己添加一个构造函数从LibraryActivity中接收这个Fragment数组
    public LibraryFragmentAdapter(FragmentManager fm, List<Fragment> fragmentArray) {
        this(fm);
        this.fragmentArray = fragmentArray;
    }

    public LibraryFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    //这个函数的作用是当切换到第arg0个页面的时候调用。
    @Override
    public Fragment getItem(int position) {
        return this.fragmentArray.get(position);
    }

    @Override
    public int getCount() {
        return this.fragmentArray.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (position != 0 && fragment != mCurrentPrimaryItem) {
            fragment.onResume();
            mCurrentPrimaryItem = fragment;
        }
    }
    
    
}
