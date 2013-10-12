package com.ibus.autowol.backend;

import java.net.UnknownHostException;

import jcifs.netbios.NbtAddress;
import android.util.Log;

public abstract class Jcifs 
{
	//this will only work on pcs that have samaba / cifs enabled
	public static Device GetHost(String ipAddress)
	{
		throwOnIllegalIp(ipAddress);
		
		Device host = null;
		try 
		{
			NbtAddress[] nbEntry = NbtAddress.getAllByAddress(ipAddress);
			
			for(int i=0; i < nbEntry.length; i++)
			{
				if(!nbEntry[i].isGroupAddress())
				{
					host = new Device();
					host.setIpAddress(ipAddress);
					host.setName(nbEntry[i].getHostName());
					host.setMacAddress(MacAddressUtil.getStringFromBytes(nbEntry[i].getMacAddress()));
					
					Log.i("NetbiosAddress", String.format("Netbios Address found. ip: %s, name: %s, mac: %s ", host.getIpAddress(), host.getName(), host.getMacAddress()));
				}
			}
			
		} catch (UnknownHostException e) 
		{
			Log.e("NetbiosAddress", "an UnknownHostException occured while attempting to get host name for: " +  ipAddress );
		}
		
		return host;
	}
	
	public static String getHostName(String ipAddress)
	{
		Device host = GetHost(ipAddress);
		
		if(host != null)
			return host.getName();
		
		return null;
	}
	
	
	
	
	private static void throwOnIllegalIp(String ipAsString)
	{
		if(ipAsString == null)
    		throw new IllegalArgumentException("ipAsString cannot be null");
    	else if(!IpAddressUtil.isValidIp(ipAsString))
    		throw new IllegalArgumentException("ipAsString is not a valid ip address");
	}

}
