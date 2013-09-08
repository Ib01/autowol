package com.ibus.autowol.backend;

import java.io.Serializable;


public class Device implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	enum WakeProtocol
	{
		UDP,
		TCP
	}
	
	protected String name;
	protected String displayName;	
	protected int primaryKey;
	protected String ipAddress;
	protected String macAddress;	
	protected String nicVendor;
	private int routerId;
	private boolean isLive;
	
	//TODO: Figure out these methods of wake.  will any of them enable wake on wan
	private String wakePort;
	private WakeProtocol wakeProtocol;
	private boolean broadcastWake;
	
	// Properties //////////////////////////////////////////////////////////

	public boolean getBroadcastWake() {
		return broadcastWake;
	}
	public void setBroadcastWake(boolean broadcastWake) {
		this.broadcastWake = broadcastWake;
	}
	public WakeProtocol getWakeProtocol() {
		return wakeProtocol;
	}
	public void setWakeProtocol(WakeProtocol wakeProtocol) {
		this.wakeProtocol = wakeProtocol;
	}
	public String getWakePort() {
		return wakePort;
	}
	public void setWakePort(String wakePort) {
		this.wakePort = wakePort;
	}
	public boolean getIsLive() {
		return isLive;
	}
	public void setIsLive(boolean isLive) {
		this.isLive = isLive;
	}
	public int getRouterId() {
		return routerId;
	}
	public void setRouterId(int routerId) {
		this.routerId = routerId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(int primaryKey) {
		this.primaryKey = primaryKey;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String hardwareAddress) {
		this.macAddress = hardwareAddress;
	}
	public String getNicVendor() {
		return nicVendor;
	}
	public void setNicVendor(String nicName) {
		this.nicVendor = nicName;
	}
	
	// Constructors //////////////////////////////////////////////////////////
	
	public Device(){
	}
	
	public Device(String ip)
	{
		this();
		setIpAddress(ip);
	}

	
	public void copyFromScannedDevice(Device device)
	{
		//does not overwrite display name or primary key 
		name = device.getName();
		ipAddress = device.getIpAddress();
		macAddress = device.getMacAddress();
		nicVendor = device.getNicVendor();
	}

	public Device getCopy()
	{
		Device d = new Device(); 
		
		d.setDisplayName(displayName);
		d.setIpAddress(ipAddress);
		d.setMacAddress(macAddress);
		d.setName(displayName);
		d.setNicVendor(nicVendor);
		d.setPrimaryKey(primaryKey);
		d.setRouterId(routerId);
		
		return d;
	}
	
	
}














   
