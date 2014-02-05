package com.ibus.autowol.backend;

import android.content.Context;

public interface IConnectionInfo {

	public boolean isMobileNetworkConnected(Context context); 
    public boolean isWifiConnected(Context context);
}
