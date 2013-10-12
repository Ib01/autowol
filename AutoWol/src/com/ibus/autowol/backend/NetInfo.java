/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

//am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings
package com.ibus.autowol.backend;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public abstract class NetInfo 
{
    //private SharedPreferences prefs;
	private static Context ctxt;
    private static final String TAG = "NetInfo";
    
    public static Nic deviceInterface;
    public static String deviceIp;
    public static String network_start;
    public static String network_end;
    public static String gatewayIp;
    public static String netmaskIp;
    public static WifiInfo wifiConnectionInfo;
    public static Cidr cidr;    
    public static String carrier = null;

    public static void refresh(final Context ctxt)
    {
    	NetInfo.ctxt = ctxt;
    	setDeviceIp();
    	setWifiInfo();
    	cidr = new Cidr(netmaskIp);
    	setHostBounds();
    }
    
    /* NETWORK DISCOVERY METHODS 88888888888888888888888888888888888888888888888888888888888888888888888888888888888 */
    
    //set ip of the device to the first valid ip found a network interface
    private static void setDeviceIp() 
    {
    	deviceInterface = new Nic();
        try 
        {
        	for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
        	{
                NetworkInterface ni = en.nextElement();
                String intfName = ni.getName();
                String intfIp = getInterfaceFirstIp(ni);
                
                if (IpAddressUtil.isValidIp(intfIp)) 
                {
                	deviceInterface.setName(intfName);
                	deviceInterface.setAddress(intfIp);
                	deviceIp = intfIp;
                    break;
                }
            }
            
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage());
        }
        
    }
    

    private static boolean setWifiInfo() 
    {
        WifiManager wifi = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
        	wifiConnectionInfo = wifi.getConnectionInfo();
        	gatewayIp = IpAddressUtil.getStringFromIntSigned(wifi.getDhcpInfo().gateway);
        	netmaskIp = IpAddressUtil.getStringFromIntSigned(wifi.getDhcpInfo().netmask);
            return true;
        }
        return false;
    }
    
    
    private static void setHostBounds()
    {
    	long numericDeviceIp = IpAddressUtil.getUnsignedLongFromString(deviceInterface.getAddress()); 
    	
    	// Detected IP
        int shift = (32 - cidr.getCidr());
        long start;
        long end;
        if (cidr.getCidr() < 31) {
        	start =  (numericDeviceIp >> shift << shift) + 1;
        	end =  (start | ((1 << shift) - 1)) - 1;
        } 
        else 
        {
        	start =  (numericDeviceIp >> shift << shift);
        	end =  (start | ((1 << shift) - 1));
        }
        
        network_start = IpAddressUtil.getStringFromLongUnsigned(start);
    	network_end = IpAddressUtil.getStringFromLongUnsigned(end);
    }

    
    
    /* UTILITIES 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888 */
    
    private static String getInterfaceFirstIp(NetworkInterface ni) 
    {
        if (ni != null) 
        {
            for (Enumeration<InetAddress> nis = ni.getInetAddresses(); nis.hasMoreElements();) 
            {
                InetAddress ia = nis.nextElement();
                if (!ia.isLoopbackAddress()) 
                {
                    if (ia instanceof Inet6Address) 
                    {
                        Log.i(TAG, "IPv6 detected and not supported yet!");
                        continue;
                    }
                    
                    return ia.getHostAddress();
                }
            }
        }
        
        return null;
    }

    
    //* UNUSED 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888 */
    


    public static boolean getMobileInfo() {
        TelephonyManager tm = (TelephonyManager) ctxt.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            carrier = tm.getNetworkOperatorName();
        }
        return false;
    }

    public static String getNetIp() 
    {
        int shift = (32 - cidr.getCidr());
        int start = ((int) IpAddressUtil.getUnsignedLongFromString(deviceIp) >> shift << shift);
        return IpAddressUtil.getStringFromLongUnsigned((long) start);
    }

    /*
     * public String getIp() { return getIpFromIntSigned(dhcp.ipAddress); }
     * public int getNetCidr() { int i = dhcp.netmask; i = i - ((i >> 1) &
     * 0x55555555); i = (i & 0x33333333) + ((i >> 2) & 0x33333333); return ((i +
     * (i >> 4) & 0xF0F0F0F) * 0x1010101) >> 24; // return 24; } public String
     * getNetIp() { return getIpFromIntSigned(dhcp.ipAddress & dhcp.netmask); }
     */
    // public String getNetmask() {
    // return getIpFromIntSigned(dhcp.netmask);
    // }

    // public String getBroadcastIp() {
    // return getIpFromIntSigned((dhcp.ipAddress & dhcp.netmask) |
    // ~dhcp.netmask);
    // }

    // public Object getGatewayIp() {
    // return getIpFromIntSigned(dhcp.gateway);
    // }

    public static SupplicantState getSupplicantState() {
        return wifiConnectionInfo.getSupplicantState();
    }

    public static boolean isConnected(Context ctxt) {
        NetworkInfo nfo = ((ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (nfo != null) {
            return nfo.isConnected();
        }
        return false;
    }

    

    // public int getIntFromInet(InetAddress ip_addr) {
    // return getIntFromIp(ip_addr.getHostAddress());
    // }

    // private InetAddress getInetFromInt(int ip_int) {
    // byte[] quads = new byte[4];
    // for (int k = 0; k < 4; k++)
    // quads[k] = (byte) ((ip_int >> k * 8) & 0xFF); // 0xFF=255
    // try {
    // return InetAddress.getByAddress(quads);
    // } catch (java.net.UnknownHostException e) {
    // return null;
    // }
    // }
}





/*public int speed = 0;
public String ssid = null;
public String bssid = null;
public String macAddress = NOMAC;
public String gatewayIp = NOIP;
public String netmaskIp = NOMASK;*/

/*info = wifi.getConnectionInfo();
// Set wifi variables
speed = info.getLinkSpeed();
ssid = info.getSSID();
bssid = info.getBSSID();
macAddress = info.getMacAddress();
gatewayIp = getIpFromIntSigned(wifi.getDhcpInfo().gateway);
// broadcastIp = getIpFromIntSigned((dhcp.ipAddress & dhcp.netmask)
// | ~dhcp.netmask);
netmaskIp = getIpFromIntSigned(wifi.getDhcpInfo().netmask);*/




//
////sets the start and end address of the local network as a long
//private void setHostBounds()
//{
//	long network_ip = deviceIp.getNumericAddress(); 
//	network_start = new IP
//	
//	// Detected IP
//  int shift = (32 - cidr);
//  if (cidr < 31) {
//      network_start = (network_ip >> shift << shift) + 1;
//      network_end = (network_start | ((1 << shift) - 1)) - 1;
//  } else {
//      network_start = (network_ip >> shift << shift);
//      network_end = (network_start | ((1 << shift) - 1));
//  }
//}
//

