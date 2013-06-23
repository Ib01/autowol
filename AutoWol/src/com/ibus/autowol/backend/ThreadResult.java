package com.ibus.autowol.backend;

public class ThreadResult 
{
	public ThreadResult(Device device, boolean success)
	{
		this.device = device;
		this.success = success;
	}
	public ThreadResult(Device device)
	{
		this.device = device;
	}
	
	public Device device;
	public boolean success;
}
