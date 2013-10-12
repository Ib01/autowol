package com.ibus.autowol.backend;

import java.io.IOException;

import android.util.Log;

public abstract class Shell 
{
	public static boolean ping(String ipAddress)
	{
		if(ipAddress == null)
    		throw new IllegalArgumentException("ipAddress cannot be null");
    	else if(!IpAddressUtil.isValidIp(ipAddress))
    		throw new IllegalArgumentException("ipAddress is not a valid ip address");
		
		Process p1;
		try 
		{
			p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 " + ipAddress);
			int returnVal = p1.waitFor();
			boolean reachable = (returnVal==0);
			
			if(reachable){
				Log.i("HostEnumerator.Pinged", ipAddress +  " was pinged successfully");
				return true;
			}
			else
				Log.i("HostEnumerator", ipAddress +  " was NOT pinged successfully");
			
		} catch (IOException e) {
			Log.e("HostEnumerator", "an IOException occured while attempting to ping: " + ipAddress );
		} catch (InterruptedException e) {
			Log.e("HostEnumerator", "an InterruptedException occured while attempting to ping: " + ipAddress );
		}
		
		
		return false;
		
	}
	
	
	
}
