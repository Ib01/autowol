package com.ibus.autowol.backend;

import java.lang.ref.WeakReference;
import java.util.List;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.ibus.autowol.ui.OnScanCompleteListener;
import com.ibus.autowol.ui.OnScanProgressListener;

public class NetworkScanner implements Runnable, IHostEnumerator
{
	private final String TAG = "AutoWol-NetworkScanner";
	private final MessageHandler _messageHandler = new MessageHandler();
	private Thread _scannerThread;
	private boolean _continue;
	//private List<Device> _devices = new ArrayList<Device>();
	private static int UPDATE_PROGRESS = 1;
	private static int UPDATE_COMPLETE = 2;
	private String _networkStartIp;
    private String _networkEndIp;
	
	//handler used by the thread below to marshel messages back to the main ui thread. it is probably not neccessary 
	//to declare this class static since it is not within scope of the activity so it shouldnt leak the activity?
	//weak references ensure that our progress listeners can get garbage collected and dont leak
	private static class MessageHandler extends Handler 
	{
		private final String TAG = "AutoWol-NetworkScanner";
		private WeakReference<OnScanProgressListener> _progressListenerReference;
		private WeakReference<OnScanCompleteListener> _completeListenerReference;
		
		@Override
		public void handleMessage(Message msg) 
		{
			Log.i(TAG, "Entering handleMessage");	
			
			if(msg.what == UPDATE_PROGRESS)
			{
				OnScanProgressListener l = _progressListenerReference.get();
				if (l != null) 
				{
					Log.i(TAG, "progress listener found");
					ThreadResult res = (ThreadResult)msg.obj;
					l.onScanProgress(res);
				}
				else
				{
					//the view that this handler updates no longer exists
					Log.i(TAG, "progress listener is null / not found");
				}
			}
			else
			{
				OnScanCompleteListener l = _completeListenerReference.get();
				if (l != null) 
				{
					Log.i(TAG, "progress listener found");
					l.onScanComplete();
				}
				else
				{
					//the view that this handler updates no longer exists
					Log.i(TAG, "progress listener is null / not found");
				}
			}
		}
		
		public void addOnScanProgressListener(OnScanProgressListener listener) {
			_progressListenerReference = new WeakReference<OnScanProgressListener>(listener);
	    }
		
		public void addOnScanCompleteListener(OnScanCompleteListener listener) {
			_completeListenerReference = new WeakReference<OnScanCompleteListener>(listener);
	    }
	}
	
	
	@Override
	public void run() 
	{
		Log.i(TAG, "Network scan thread entered");
		
		Long start = IpAddressUtil.getUnsignedLongFromString(_networkStartIp);
		Long end = IpAddressUtil.getUnsignedLongFromString(_networkEndIp);
		Message msg;
		
		Log.i(TAG, "starting udp probe with ip start: " + _networkStartIp + ". ip end:" + _networkEndIp);
		for (long i = start; i <= end; i++) 
		{	
			Udp.probe(IpAddressUtil.getStringFromLongUnsigned(i));
			
			try 
			{
				Thread.sleep(10);
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			if(!getContinue())
			{
				msg = _messageHandler.obtainMessage(UPDATE_COMPLETE);
				_messageHandler.sendMessage(msg);
				return;
			}
				
	    }
		
		Log.i(TAG, "getting hosts from arp and resolving host names");
		
		List<Device> hosts = Arp.EnumerateHosts();
		for(Device h : hosts)
		{
			String n = InetAddressManager.GetHostName(h.getIpAddress());
			
			if(n == null || n.isEmpty() || n.equals(h.getIpAddress()))
				n = Jcifs.getHostName(h.getIpAddress());
			
			if(n == null || n.isEmpty() || n.equals(h.getIpAddress()))
				h.setName(n); //set name if it does not equal the ip 
			
			h.setIsLive(true);
		
			Log.i(TAG, String.format("found host. Ip: %s. Mac: %s.", h.getIpAddress(), h.getMacAddress()));
			
			msg = _messageHandler.obtainMessage(UPDATE_PROGRESS, new ThreadResult(h));
			_messageHandler.sendMessage(msg);
			
			if(!getContinue())
			{
				msg = _messageHandler.obtainMessage(UPDATE_COMPLETE);
				_messageHandler.sendMessage(msg);
				return;
			}
		}
		
		Log.i(TAG, "successfully completed search for devices in arp cache");
	
		msg = _messageHandler.obtainMessage(UPDATE_COMPLETE);
		_messageHandler.sendMessage(msg);
		return;
		
	}	
	
	
	

	@Override
	public void start(INetwork network) 
	{
		if(_scannerThread == null || !_scannerThread.isAlive())
		{
			_networkStartIp =  network.getNetworkStartIp();
			_networkEndIp = network.getNetworkEndIp();
			
			setContinue(true);
			_scannerThread = new Thread(this);
			_scannerThread.setDaemon(true);
			_scannerThread.start();
		} 
	}

	@Override
	public void stop() 
	{
		//TODO: should we implement this?
		/*_messageHandler.removeMessages(UPDATE_PROGRESS);
		_messageHandler.removeMessages(UPDATE_COMPLETE);*/
		setContinue(false);
	}
	
	@Override
	public void addOnScanProgressListener(OnScanProgressListener listener) {
		_messageHandler.addOnScanProgressListener(listener);
    }
	
	@Override
	public void addOnScanCompleteListener(OnScanCompleteListener listener) {
		_messageHandler.addOnScanCompleteListener(listener);
    }
	
	private boolean getContinue()
	{
		//when a thread enteres here no other thread may enter in 
		//getContinue or setContinue
		synchronized (this)
		{
			return _continue;
		}
	}
	
	private void setContinue(boolean continueThread)
	{
		synchronized (this)
		{
			_continue = continueThread;
		}
	}
	
}






