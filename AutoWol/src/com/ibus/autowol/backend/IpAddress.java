package com.ibus.autowol.backend;


public abstract class IpAddress  
{
	private static final String NOIP = "0.0.0.0";
	private static final String NOMASK = "255.255.255.255";

	public static String GetEmptyIp()
	{
		return NOIP;
	}
    
    public static boolean isValidIp(String ipAddress)
    {
    	if(ipAddress == null)
    		return false;
    	
    	return !ipAddress.equals(NOIP);
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







