package com.wiredfactory.bluewave.activity;

import java.util.Locale;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.R.string;
import com.wiredfactory.bluewave.fragment.BeaconFragment;
import com.wiredfactory.bluewave.fragment.LLSettingsFragment;
import com.wiredfactory.bluewave.fragment.MacroFragment;
import com.wiredfactory.bluewave.interfaces.IFragmentListener;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class FragmentAdapter extends FragmentPagerAdapter {
	
	public static final String TAG = "FragmentAdapter";
	
	// Total count
	public static final int FRAGMENT_COUNT = 3;
	
    // Fragment position
    public static final int FRAGMENT_POS_BEACON = 0;
    public static final int FRAGMENT_POS_MACRO = 1;
    public static final int FRAGMENT_POS_SETTINGS = 2;
    
	public static final String ARG_SECTION_NUMBER = "section_number";
    
    // System
    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;
    private Handler mActivityHandler = null;
    
    private Fragment mTimelineFragment = null;
    private Fragment mGraphFragment = null;
    private Fragment mLLSettingsFragment = null;
    
    public FragmentAdapter(FragmentManager fm, Context c, IFragmentListener l, Handler h) {
		super(fm);
		mContext = c;
		mFragmentListener = l;
		mActivityHandler = h;
	}
    
	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		Fragment fragment;
		//boolean needToSetArguments = false;
		
		if(position == FRAGMENT_POS_BEACON) {
			if(mTimelineFragment == null) {
				mTimelineFragment = new BeaconFragment(mContext, mFragmentListener, mActivityHandler);
				//needToSetArguments = true;
			}
			fragment = mTimelineFragment;

		} else if(position == FRAGMENT_POS_MACRO) {
			if(mGraphFragment == null) {
				mGraphFragment = new MacroFragment(mContext, mFragmentListener, mActivityHandler);
				//needToSetArguments = true;
			}
			fragment = mGraphFragment;
			
		} else if(position == FRAGMENT_POS_SETTINGS) {
			if(mLLSettingsFragment == null) {
				mLLSettingsFragment = new LLSettingsFragment(mContext, mFragmentListener, mActivityHandler);
				//needToSetArguments = true;
			}
			fragment = mLLSettingsFragment;
			
		} else {
			fragment = null;
		}
		
		// TODO: If you have something to notify to the fragment.
		/*
		if(needToSetArguments) {
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
		}
		*/
		
		return fragment;
	}

	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case FRAGMENT_POS_BEACON:
			return mContext.getString(R.string.title_beacon).toUpperCase(l);
		case FRAGMENT_POS_MACRO:
			return mContext.getString(R.string.title_macro).toUpperCase(l);
		case FRAGMENT_POS_SETTINGS:
			return mContext.getString(R.string.title_ll_settings).toUpperCase(l);
		}
		return null;
	}
    
    
}
