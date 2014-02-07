package com.ibus.autowol.mock;

import java.io.IOException;

import android.content.Context;

import com.ibus.autowol.backend.INetwork;
import com.ibus.autowol.backend.IpAddressUtil;
import com.ibus.autowol.backend.MacAddressUtil;
import com.ibus.autowol.backend.Router;

public class MockNetwork implements INetwork
{
	String _netmaskIp;
	String _gatewayIp;
	String _networkEndIp;
	String _networkStartIp;
	Router _router = new Router();
	boolean _isConnectedToNetwork;
	boolean _isMobileNetworkConnected;
	boolean _isWifiNetworkConnected;
	
	public MockNetwork()
	{
		_netmaskIp = "255.255.255.0";
		_gatewayIp = "10.0.0.140";
		_networkStartIp= "10.0.0.1";
		_networkEndIp = "10.0.0.254";
		_isConnectedToNetwork = true;
		
		_router.setBssid(MacAddressUtil.getEmptyMac());
    	_router.setSsid("Fakety fake");
    	_router.setDisplayName("Fake");
    	_router.setIpAddress(IpAddressUtil.GetEmptyIp());
    	_router.setMacAddress(MacAddressUtil.getEmptyMac());
    	_router.setName("Fake");
    	_router.setNicVendor("Fake");
    	_router.setPrimaryKey(0);
	}
	
    public Router getRouter() 
    {
		return _router;
	}
    
    public void setRouter(Router router) 
    {
		_router = router;
	}
    
    
	public String getNetmaskIp() {
		return _netmaskIp;
	}
	public void setNetmaskIp(String ip) {
		_netmaskIp = ip;
	}
	
	public String getGatewayIp() {
		return _gatewayIp;
	}
	public void setGatewayIp(String ip) {
		_gatewayIp = ip;
	}
	
	public String getNetworkEndIp() {
		return _networkEndIp;
	}
	public void setNetworkEndIp(String ip) {
		_networkEndIp = ip;
	}
	
	public String getNetworkStartIp() {
		return _networkStartIp;
	}
	public void setNetworkStartIp(String ip) {
		_networkStartIp = ip;	
	}
	
	public void refresh(Context context){}
	
	public boolean IsGateway(String ipAddress)
    {
    	if(ipAddress == null)
    		return false;
    	
    	return ipAddress.equals(_router.getIpAddress());
    }



	@Override
	public String getBroadcastAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean infoIsValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
}




