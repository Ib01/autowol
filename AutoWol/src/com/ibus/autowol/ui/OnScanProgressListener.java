package com.ibus.autowol.ui;

import com.ibus.autowol.backend.ThreadResult;

public interface OnScanProgressListener 
{
	public void onScanProgress (ThreadResult thread);
}
