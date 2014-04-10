package com.ibus.autowol.backend;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings 
{	
	public static final String SETTINGS_FILE = "autoWolSettings";
	boolean propmptNewNetworkScan = true;
	
	public static boolean getPropmptNewNetworkScan(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILE, 0);
	    boolean val = settings.getBoolean("propmptNewNetworkScan", false);
	    
	    return val;
	}
	
	
	public static void SaveSettings(Context context, boolean propmptNewNetworkScan)
	{
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILE, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean("propmptNewNetworkScan", propmptNewNetworkScan);
		
		editor.commit(); 
	}
}
