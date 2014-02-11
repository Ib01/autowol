package com.ibus.autowol.backend;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Network implements INetwork
{
    private final String TAG = "Network";
    private Context _context;
    private Router _router; 
    private String _netmaskIp;
    private String _networkEndIp;
    private String _networkStartIp;
    private String _broadcastAddress;
    
    public Router getRouter(){
    	return _router;
    }
    public String getNetmaskIp(){
    	return _netmaskIp;
    }
    public String getNetworkEndIp() {
		return _networkEndIp;
	}
	public String getNetworkStartIp() {
		return _networkStartIp;
	}
	public String getBroadcastAddress(){
		return _broadcastAddress;
	}
	
    public Network(Context context)
   	{
   		_context = context;
   		Refresh();
   	}
   
	private void Refresh()
    {
		WifiManager wifiManager = getWifiManager();
		WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
	
		_router = new Router();
		_router.setBssid(wifiInfo.getBSSID()); 	
		_router.setSsid(wifiInfo.getSSID());		
		_router.setIpAddress(IpAddressUtil.getStringFromIntSigned(wifiManager.getDhcpInfo().gateway));
		_router.setLastScanned(android.text.format.DateFormat.getDateFormat(_context).format(new Date()));
		
		_netmaskIp = IpAddressUtil.getStringFromIntSigned(wifiManager.getDhcpInfo().netmask); //will we always have this info?
		_broadcastAddress = getBroadcastAddress(wifiManager.getDhcpInfo().ipAddress, wifiManager.getDhcpInfo().netmask);
		
		Device device = getDevice();
		if(device != null){
			_networkEndIp = IpAddressUtil.getStringFromLongUnsigned(GetNetworkBound(false, device));
			_networkStartIp = IpAddressUtil.getStringFromLongUnsigned(GetNetworkBound(true, device));
		}
		
		if(_router.getBssid() == null )
			Log.w(TAG, "Bssid is null in Network.Refresh()");
		if(_router.getSsid() == null )
			Log.w(TAG, "ssid is null in Network.Refresh()");	
		if(!IpAddressUtil.isValidIp(_router.getIpAddress()))
			Log.w(TAG, "gatewayIp generated in Network.Refresh() is not a valid ip");
		if(!IpAddressUtil.isValidNetworkMask(_netmaskIp))
			Log.w(TAG, "_netmaskIp generated in Network.Refresh() is not a valid netmask address");
		if(!IpAddressUtil.isValidIp(_networkEndIp))
			Log.w(TAG, "_networkEndIp generated in Network.Refresh() is not valid ip");
		if(!IpAddressUtil.isValidIp(_networkStartIp))
			Log.w(TAG, "_networkStartIp generated in Network.Refresh() is not valid ip");
		if(!IpAddressUtil.isValidIp(_broadcastAddress))
			Log.w(TAG, "_broadcastAddress generated in Network.Refresh() is not valid ip");
    }

	
	public boolean infoIsValid()
	{
		if(_router == null)
			return false;
		
		return (_router.getBssid() != null && !_router.getBssid().isEmpty()  
				&& _router.getSsid() != null && !_router.getSsid().isEmpty()
				&& IpAddressUtil.isValidIp(_router.getIpAddress()) 
				&& IpAddressUtil.isValidNetworkMask(_netmaskIp)
				&& IpAddressUtil.isValidIp(_networkEndIp)
				&& IpAddressUtil.isValidIp(_networkStartIp)
				&& IpAddressUtil.isValidIp(_broadcastAddress));
	}
	
	
	
    
    //set ip of the device to the first valid ip found a network interface
    private Device getDevice() 
    {
        try 
        {
        	for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements();) 
        	{
                NetworkInterface nic = nics.nextElement();
                
                String ip = getInterfaceFirstIp(nic);
                if (IpAddressUtil.isValidIp(ip)) 
                {
                	//this can be null staight after reconnection of the network 9even thought the network shows as up) grrr.
                	if(nic.getHardwareAddress() != null)
                	{
                		Device device = new Device();
                		device.setName(nic.getName());
                    	device.setIpAddress(ip);
                    	device.setMacAddress(MacAddressUtil.getStringFromBytes(nic.getHardwareAddress()));
                
                    	return device;	
                	}
                	else
                	{
                		Log.e(TAG, "nic.getHardwareAddress() IS null");
                	}
                }
            }
            
        } catch (SocketException e) 
        {
            Log.e(TAG, "Could not get device in Network.getDevice()", e);
            throw new RuntimeException("Could not get device in Network.getDevice(). SocketException thrown");    
        }
        
		return null; 
    }
    
    
    private String getInterfaceFirstIp(NetworkInterface ni) 
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
                        Log.i(TAG, "IPv6 address detected in getInterfaceFirstIp");
                        continue;
                    }
                    
                    return ia.getHostAddress();
                }
            }
        }
        
        return null;
    }
    
	
	private long GetNetworkBound(boolean getStart, Device device )
	{
		Cidr _cidr = new Cidr(_netmaskIp);
    	long numericDeviceIp = IpAddressUtil.getUnsignedLongFromString(device.getIpAddress()); 
    	
    	// Detected IP
        int shift = (32 - _cidr.getCidr());
        long start;
        long end;
        
        if (_cidr.getCidr() < 31) {
        	start =  (numericDeviceIp >> shift << shift) + 1;
        	end =  (start | ((1 << shift) - 1)) - 1;
        } 
        else 
        {
        	start =  (numericDeviceIp >> shift << shift);
        	end =  (start | ((1 << shift) - 1));
        }
        
        if(getStart)
        	return start;
        else
        	return end;
	}
	
	
	private WifiManager getWifiManager()
	{
		WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager == null) //this should never happen?
			throw new RuntimeException("Could not get router. Could not retrieve the system service: WIFI_SERVICE");
		  
		return wifiManager;
	}
	  
	private String getBroadcastAddress(int dhcpIp, int dhcpNetmask)
	{
    	int broadcast = (dhcpIp & dhcpNetmask) | ~dhcpNetmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
		  quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		
		InetAddress ad;
		try {
			ad = InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Could not retrieve broadcast address in Network.Refresh()");
		}
	
		return ad.getHostAddress();
	}

	
}
    
    
    

/**
 * Calculate the broadcast IP we need to send the packet along. If we send it
 * to 255.255.255.255, it never gets sent. I guess this has something to do
 * with the mobile network not wanting to do broadcast.
 */
/*  public String getBroadcastAddress() throws IOException 
{
		WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);

		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		if (dhcp == null) {
		  Log.d(TAG, "Could not get dhcp info");
		  return null;
		}
		
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
		  quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		
		InetAddress ad = InetAddress.getByAddress(quads);
		return ad.getHostAddress();
}*/



/*public String getNetmaskIp() 
{
	WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
	if (wifiManager != null) 
	{	
		String _netmaskIp = IpAddressUtil.getStringFromIntSigned(wifiManager.getDhcpInfo().netmask);
		return _netmaskIp;
	}
	
	throw new RuntimeException("Could not retrieve netmask. could not retrieve the system service: WIFI_SERVICE");
}*/


/*public String getNetworkEndIp() 
{
	return GetNetworkBound(false);
}

public String getNetworkStartIp() 
{
	return GetNetworkBound(true);
}*/


/*public boolean IsGateway(String ipAddress)
{
	if(ipAddress == null)
		return false;
	
	return ipAddress.equals(getRouter().getIpAddress());
}*/













  /*  private boolean setWifiInfo(Context context) 
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) 
        {
        	_netmaskIp = IpAddress.getStringFromIntSigned(wifiManager.getDhcpInfo().netmask);
        	
        	wifiInfo = wifiManager.getConnectionInfo();
        	String gatewayIp = IpAddress.getStringFromIntSigned(wifiManager.getDhcpInfo().gateway);
        	router = new Router();
        	router.setBssid(wifiInfo.getBSSID());
        	router.setSsid(wifiInfo.getSSID());
        	router.setIpAddress(gatewayIp);
        	
        	
        	 * Note: 
        	 * 
        	 * router.setIpAddress(IpAddress.getStringFromIntSigned(wifiInfo.getIpAddress()));
        	 * and 
        	 * router.setMacAddress(wifiInfo.getMacAddress());
        	 * 
        	 * are the ip and mac of the local devices wireless adapter (i.e the phone)
        	 *  
        	
        	
            return true;
        }
        return false;
    }
    */
    
  /*  private void setHostBounds()
    {
    	_cidr = new Cidr(_netmaskIp);
    	
    	long numericDeviceIp = IpAddress.getUnsignedLongFromString(_device.getIpAddress()); 
    	
    	// Detected IP
        int shift = (32 - _cidr.getCidr());
        long start;
        long end;
        if (_cidr.getCidr() < 31) {
        	start =  (numericDeviceIp >> shift << shift) + 1;
        	end =  (start | ((1 << shift) - 1)) - 1;
        } 
        else 
        {
        	start =  (numericDeviceIp >> shift << shift);
        	end =  (start | ((1 << shift) - 1));
        }
        
        _networkStartIp = IpAddress.getStringFromLongUnsigned(start);
    	_networkEndIp = IpAddress.getStringFromLongUnsigned(end);
    }
*/
   


    
    
    
  
    
    


   /* ConnectivityManager connManager1 = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo mMobile = connManager1.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if (mMobile.isConnected()) {
        //if internet connected
    }
    */
    
    
    
    //* UNUSED 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888 */
    


    /*public boolean getMobileInfo(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            _carrier = tm.getNetworkOperatorName();
        }
        return false;
    }

    public String getNetIp() 
    {
        int shift = (32 - _cidr.getCidr());
        int start = ((int) IpAddress.getUnsignedLongFromString(_device.getIpAddress()) >> shift << shift);
        return IpAddress.getStringFromLongUnsigned((long) start);
    }
*/

    
    
    
   /* public String getCurrentSsid(Context context) {

    	  String ssid = null;
    	  ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	  NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	  
    	  if (networkInfo.isConnected()) 
    	  {
    	    final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	    
    	    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
    	    if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
    	        //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
    	      ssid = connectionInfo.getSSID();
    	    }
    	    
    	 // Get WiFi status MARAKANA
    	    WifiInfo info = wifiManager.getConnectionInfo();
    	    String textStatus = "";
    	    textStatus += "\n\nWiFi Status: " + info.toString();
    	    String BSSID = info.getBSSID();
    	    String MAC = info.getMacAddress();

    	    
    	    
    	    
    	    
    	    
    	    List<ScanResult> results = wifiManager.getScanResults();
    	    ScanResult bestSignal = null;
    	    int count = 1;
    	    String etWifiList = "";
    	    for (ScanResult result : results) {
    	        etWifiList += count++ + ". " + result.SSID + " : " + result.level + "\n" +
    	                result.BSSID + "\n" + result.capabilities +"\n" +
    	                "\n=======================\n";
    	    }
    	    Log.v(TAG, "from SO: \n"+etWifiList);

    	    // List stored networks
    	    List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
    	    for (WifiConfiguration config : configs) {
    	        textStatus+= "\n\n" + config.toString();
    	    }
    	    Log.v(TAG,"from marakana: \n"+textStatus);
    	  }
    	  return ssid;
    	}*/
    
    

    
    
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

   /* public static SupplicantState getSupplicantState() {
        return wifiConnectionInfo.getSupplicantState();
    }*/

    

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

