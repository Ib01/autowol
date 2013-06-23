package com.ibus.autowol.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Cidr 
{
	private static final String TAG = "Cidr";
    private static final String CMD_IP = " -f inet addr show %s";
    private static final String PTN_IP1 = "\\s*inet [0-9\\.]+\\/([0-9]+) brd [0-9\\.]+ scope global %s$";
    private static final String PTN_IP2 = "\\s*inet [0-9\\.]+ peer [0-9\\.]+\\/([0-9]+) scope global %s$"; // FIXME: Merge with PTN_IP1
    private static final String PTN_IF = "^%s: ip [0-9\\.]+ mask ([0-9\\.]+) flags.*";
    private static final int BUF = 8 * 1024;
    private static final int DEFAULT_CIDR = 24;
    private static final String DEFAULT_INTERFACE_NAME = "eth0";
    private int _cidr;
    
    // Properties ///////////////////////////////////////////////
    
    public int getCidr() {
		return _cidr;
	}
	
    // Constructors ///////////////////////////////////////////////

	public Cidr(){
		_cidr = DEFAULT_CIDR;
    }
	
	public Cidr(String netmaskIp)
    {
		SetCidr(netmaskIp);
    }
	
    // Methods ///////////////////////////////////////////////
	
	public void SetCidr(String netmaskIp){
		_cidr = Cidr.getCidr(netmaskIp);
    }
	
	//sets the CIDR for the devices IP.  the CIDR is the number of bits which make up the host portion 
    //of an IP address.  for most home networks this will be 24 bits
    public static int getCidr(String netmaskIp) 
    {
    	int cidr = DEFAULT_CIDR;
    	
        if (IpAddress.isValidNetworkMask(netmaskIp)) 
        {
            cidr = IpToCidr(netmaskIp);
        } else {
            String match;
            // Running ip tools
            try {
                if ((match = runCommand("/system/xbin/ip", String.format(CMD_IP, DEFAULT_INTERFACE_NAME), String.format(PTN_IP1, DEFAULT_INTERFACE_NAME))) != null) {
                    cidr = Integer.parseInt(match);
                } else if ((match = runCommand("/system/xbin/ip", String.format(CMD_IP, DEFAULT_INTERFACE_NAME), String.format(PTN_IP2, DEFAULT_INTERFACE_NAME))) != null) {
                    cidr = Integer.parseInt(match);
                } else if ((match = runCommand("/system/bin/ifconfig", " " + DEFAULT_INTERFACE_NAME, String.format(PTN_IF, DEFAULT_INTERFACE_NAME))) != null) {
                    cidr = IpToCidr(match);
                } else {
                    Log.i(TAG, "cannot find cidr, using default /24");
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, e.getMessage()+ " -> cannot find cidr, using default /24");
            }
        }
        
        return cidr;
    }
    
    private static int IpToCidr(String ip) 
    {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }
	
   
    private static String runCommand(String path, String cmd, String ptn) 
    {
        try {
            if (new File(path).exists() == true) {
                String line;
                Matcher matcher;
                Pattern ptrn = Pattern.compile(ptn);
                Process p = Runtime.getRuntime().exec(path + cmd);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()), BUF);
                while ((line = r.readLine()) != null) {
                    matcher = ptrn.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't use native command: " + e.getMessage());
            return null;
        }
        return null;
    }

}
