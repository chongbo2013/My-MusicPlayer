package com.lewa.player;

import com.lewa.player.activity.SettingActivity;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class ChooseAccountPreferenceFragment extends PreferenceFragment {
	        @Override
	        public void onCreate(Bundle savedInstanceState) {  
	            super.onCreate(savedInstanceState);  
	            ((SettingActivity)getActivity()).initAccountPreferenceFragment(this);
	        } 
	    }
