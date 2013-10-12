package com.ibus.autowol.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class IpAddressUtil  
{
	private static final String NOIP = "0.0.0.0";
	private static final String NOMASK = "255.255.255.255";
	private static final String ValidIpPattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	 
	public static String GetEmptyIp()
	{
		return NOIP;
	}
    
    public static boolean isEmptyIp(String ipAddress)
    {
    	if(ipAddress == null)
    		return true;
    	return ipAddress.equals(NOIP);
    }
    
    
    public static boolean isValidIp(String ipAddress)
    {
    	if(ipAddress == null)
    		return false;
    	
    	if(ipAddress.equals(NOIP))
    		return false;
    	
    	Pattern pat = Pattern.compile(ValidIpPattern);
    	return pat.matcher(ipAddress).matches();
    }
    
    public static boolean isValidNetworkMask(String maskAddress)
    {
    	if(maskAddress == null)
    		return false;
    	
    	return !maskAddress.equals(NOMASK);
    }
    
    public static long getUnsignedLongFromString(String ipAsString) 
    {
    	throwOnIllegalIp(ipAsString);
    	
        String[] a = ipAsString.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }

    public static String getStringFromIntSigned(int ipAsInt) 
    {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ipAsInt >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

	public static String getStringFromLongUnsigned(long ipAsLOng) 
    {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ipAsLOng >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }
	
	
	private static void throwOnIllegalIp(String ipAsString)
	{
		if(ipAsString == null)
    		throw new IllegalArgumentException("ipAsString cannot be null");
    	else if(!isValidIp(ipAsString))
    		throw new IllegalArgumentException("ipAsString is not a valid ip address");
	}
	
}







