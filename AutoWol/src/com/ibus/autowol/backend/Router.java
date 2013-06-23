package com.ibus.autowol.backend;

import java.io.Serializable;


public class Router extends Device implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String bssid;
	private String ssid;
	
	// Properties //////////////////////////////////////////////////////////

	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getBssid() {
		return bssid;
	}
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	
	// Constructors //////////////////////////////////////////////////////////
	
	public Router(){
	}
	
	public Router(String ip)
	{
		this();
		setIpAddress(ip);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bssid == null) ? 0 : bssid.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		//NOTE: I am assuming that BSSID's are unique and that SSID's could change.  
		//hence 2 routers / hotspots are different if they have different bssids 
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Router other = (Router) obj;
		if (bssid == null) {
			if (other.bssid != null)
				return false;
		} else if (!bssid.equals(other.bssid))
			return false;
		return true;
	}
	
	
	
	
}

   
