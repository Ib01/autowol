package com.ibus.autowol.backend;

import com.ibus.autowol.ui.OnPingCompleteListener;
import com.ibus.autowol.ui.OnPingProgressListener;
import com.ibus.autowol.ui.OnScanCompleteListener;
import com.ibus.autowol.ui.OnScanProgressListener;

public interface IHostEnumerator  
{
	void start(INetwork network);
	void stop();
	void addOnScanProgressListener(OnScanProgressListener listener);
	void addOnScanCompleteListener(OnScanCompleteListener listener);
	
}








