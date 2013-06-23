package com.ibus.autowol.backend;


public abstract class MacAddress 
{
	private final static String TAG = "MacAddress";
	private static final String NOMAC = "00:00:00:00:00:00";
								
	public static String getEmptyMac()
	{
		return NOMAC;
	}
	
	
	public static boolean isValidMac(String macString)
	{
		if(macString == null)
			return false;
		
		return !(macString.equals(NOMAC));
	}
	
	public static String getStringFromBytes(byte[] macBytes)
	{
		StringBuilder sb = new StringBuilder(18);
		
	    for (byte b : macBytes) 
	    {
	        if (sb.length() > 0)
	            sb.append('-');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
	
	public static byte[] getBytesFromString(String macStr) throws IllegalArgumentException 
	{
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		
		if (hex.length != 6) 
		    throw new IllegalArgumentException("Invalid MAC address.");
		
		try 
		{
		    for (int i = 0; i < 6; i++) 
		        bytes[i] = (byte) Integer.parseInt(hex[i], 16);	            
		}
		catch (NumberFormatException e) {
		    throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}
	
	
	
}










