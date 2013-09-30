package com.ibus.autowol.backend;

import java.util.List;

import com.ibus.autowol.ui.OnPingCompleteListener;
import com.ibus.autowol.ui.OnPingProgressListener;

public interface IPinger 
{
	public void start(List<Device> devices);
	public void stop();
	public void addOnPingCompleteListener(OnPingCompleteListener listener);
	public void addOnPingProgressListener(OnPingProgressListener listener);
	public void setDevices(List<Device> devices);
	
}
