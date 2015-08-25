package com.lewa.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetUtils {
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkValid(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if (info != null) {
			return info.isAvailable();
		}
		return false;
	}
	
	
	 public static boolean isWiFiActive(Context context) {
	        WifiManager mWifiManager = (WifiManager) context
	                .getSystemService(Context.WIFI_SERVICE);
	        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
	        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
	        if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
	            return true;
	        } else {
	            return false;
	        }
	    }
}
