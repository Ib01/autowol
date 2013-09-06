/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

//am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings
package com.ibus.autowol.backend;

import android.content.Context;

public interface INetwork 
{
    public Router getRouter();
	public String getNetmaskIp(); 
	public String getNetworkEndIp();
	public String getNetworkStartIp();
	
    public boolean IsGateway(String ipAddress);
    
    public boolean isMobileNetworkConnected(Context ctxt); 
    
    public boolean isWifiConnected(Context ctxt);
    
    /*public boolean getMobileInfo();
    public String getNetIp();*/
}
