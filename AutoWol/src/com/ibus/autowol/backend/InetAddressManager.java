package com.ibus.autowol.backend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;

public abstract class InetAddressManager 
{
	private static final String TAG = "InetAddressManager";
	
	//this should get a pc name for an IP address regardless of whether InetAddress thinks it is reachable!!
	public static String GetHostName(String ipAddress)
	{
		String name = null;
		try 
		{
			InetAddress in = InetAddress.getByName(ipAddress);
			
			name = in.getHostName();
			if(name != null)
				Log.i(TAG, "The host name of " + ipAddress + " was resolved to " + name + " using InetAddress");
			else
				Log.i(TAG, "The host name of " + ipAddress + " could not be resolved using InetAddress");
			
		} catch (UnknownHostException e) {
			Log.e(TAG, "an UnknownHostException occured while attempting to ping: " + ipAddress );
		} catch (IOException e) {
			Log.e(TAG, "an IOException occured while attempting to ping: " + ipAddress );
		}
			
		return name;
	}
	

	/*this seems to work differently to the native ping available in a linux / windows console.  will fail on a windows host that has a a firewall up*/
	public static boolean ping(String ip, int timeOUt)
	{
		try 
		{
			InetAddress in = InetAddress.getByName(ip);
			
			 if (in.isReachable(timeOUt)) 
			 {
				 Log.i("HostEnumerator.Pinged", ip +  " was pinged successfully"); 
				 return true;
			 }
			 else
			 {
				 Log.i("HostEnumerator", ip +  " was NOT pinged successfully");
			 }
			
		} catch (UnknownHostException e) {
			Log.e("HostEnumerator", "an UnknownHostException occured while attempting to ping: " + ip );
		} catch (IOException e) {
			Log.e("HostEnumerator", "an IOException occured while attempting to ping: " + ip );
		}
		
		return false;
	}
	
	

}
