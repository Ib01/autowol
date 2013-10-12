package com.ibus.autowol.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class Arp 
{
	static String arpFileLocation = "/proc/net/arp";
	private final static String TAG = "AutoWol-Arp";
    private final static int BUF = 8 * 1024;
	
	
	/*
		Format of the arp file that we need to parse:
		
		IP address       HW type     Flags       HW address            Mask     Device
		10.0.0.138       0x1         0x2         08:76:ff:02:7c:c0     *        wlan0
		10.0.0.5         0x1         0x0         00:00:00:00:00:00     *        wlan0
	*/
	public static List<Device> EnumerateHosts()
	{
		List<Device> hosts = new ArrayList<Device>();
		BufferedReader bufferedReader = null;
		
		//String hw = NOMAC;
		try 
		{
	        bufferedReader = new BufferedReader(new FileReader(arpFileLocation), BUF);
	        boolean firstLine = true;
	        String line;
	        
	        while ((line = bufferedReader.readLine()) != null) 
	        {
	        	//skip first line as it only contains the header
	        	if(!firstLine)
	        	{
	        		String[] parts = line.split("\\s+");
	        		
	        		Device h = new Device();
	        		h.setIpAddress(parts[0]);
	        		h.setMacAddress(parts[3]);
	        		h.setNicVendor(parts[5]);
	        		
	        		//will be many devices in arp without a mac. not sure why. but i suspect the router will reply to 
	        		//arp queries where no other device does? 
	        		if(MacAddressUtil.isValidMac(h.getMacAddress())) 
	        		{
	        			hosts.add(h);
	        			Log.i(TAG, "Device found in arp cache ip: " + h.getIpAddress() + ". mac:" + h.getMacAddress());
	        		}
	        	}
	        	
	        	firstLine = false;	        	
	        }
	    
		} catch (IOException e) {
		    Log.e(TAG, "Can't read Arp file: " + e.getMessage());
		}
		finally
		{
			try {
				bufferedReader.close();
			} catch (IOException e) {
				Log.e(TAG, "IOException occured while attempting to close arp file" + e.getMessage());
			}
		}
		
		return hosts;
		
	}
	
}











