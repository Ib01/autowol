package com.ibus.autowol.backend;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.ibus.autowol.ui.OnPingCompleteListener;
import com.ibus.autowol.ui.OnPingProgressListener;

public class DevicePinger implements IPinger
{
	private final String TAG = "AutoWol-NetworkScanner";

	
	
	public class Pinger extends AsyncTask<Device, ThreadResult, Boolean>
	{
		List<OnPingCompleteListener> _pingCompleteListeners;
		List<OnPingProgressListener> _pingProgressListeners;
		
		public Pinger()
		{
			_pingCompleteListeners = new ArrayList<OnPingCompleteListener>();
			_pingProgressListeners = new ArrayList<OnPingProgressListener>();
		}
		
		@Override
		public Boolean doInBackground(Device... params) 
		{
			for(Device d : params)
			{
				boolean s = Shell.ping(d.getIpAddress());
				publishProgress(new ThreadResult(d, s));
				
				if(isCancelled())
					return false;
			}
			
		    return true;
		}	
		
		@Override
		public void onProgressUpdate (ThreadResult... result)
		{
			Log.i(TAG, "Ping complete");
			
			for (OnPingProgressListener listener : _pingProgressListeners) 
			{
				listener.onPingProgress(result[0]);
	        }
		}
		
		@Override
		public void onPostExecute (Boolean result)
		{
			Log.i(TAG, "Ping complete");
			
			for (OnPingCompleteListener listener : _pingCompleteListeners) 
			{
				listener.onPingComplete(result);
	        }
		}
		
		public void addOnPingCompleteListener(OnPingCompleteListener listener) {
			_pingCompleteListeners.add(listener);
	    }
		
		public void addOnPingProgressListener(OnPingProgressListener listener) {
			_pingProgressListeners.add(listener);
	    }
	}
	
	Pinger pinger = new Pinger();
	
	
	public void ping(List<Device> devices, OnPingCompleteListener completeListener, OnPingProgressListener progressListener)
	{
		if(pinger.getStatus() == AsyncTask.Status.RUNNING){
			Log.i(TAG, "Ping failed: ping thread already running");
			return;
		}
		
		List<Device> dl = new ArrayList<Device>();
		for(Device d : devices)
			dl.add(d.getCopy());
		
		pinger = new Pinger();
		pinger.addOnPingCompleteListener(completeListener);
		pinger.addOnPingProgressListener(progressListener);
		pinger.execute(dl.toArray(new Device[dl.size()]));
	}
	
	@Override
	public void start(List<Device> devices)
	{
		
	}

	
	
	@Override
	public void stop() 
	{
		if(pinger.getStatus() == AsyncTask.Status.RUNNING)
			pinger.cancel(true);
		
		Log.i(TAG, "Network scan canceled");
	}

	@Override
	public void addOnPingCompleteListener(OnPingCompleteListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOnPingProgressListener(OnPingProgressListener listener) {
		// TODO Auto-generated method stub
		
	}

	
}








