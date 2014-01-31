package com.ibus.autowol.backend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionInfo 
{
	public static boolean isMobileNetworkConnected(Context context) 
    {
        return isConnectedTo(context, ConnectivityManager.TYPE_MOBILE);
    }
    
    public static boolean isWifiConnected(Context context) 
    {
        return isConnectedTo(context, ConnectivityManager.TYPE_WIFI);
    }
	
  
    private static boolean isConnectedTo(Context context, int connectionType) 
    {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager == null) //will this ever be the case?
			throw new RuntimeException("Could not retrieve the system service: CONNECTIVITY_SERVICE");
        
        NetworkInfo ninfo = connManager.getNetworkInfo(connectionType);
        return ninfo.isConnected();
    }
	    
}
