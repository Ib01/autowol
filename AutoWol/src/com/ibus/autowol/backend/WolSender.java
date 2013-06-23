package com.ibus.autowol.backend;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.ibus.autowol.ui.OnWolSendCompleteListener;
import com.ibus.autowol.ui.OnWolSendProgressListener;

public class WolSender extends AsyncTask<Device, Device, Void>  
{
	private final String TAG = "WolSender";
	public final int PORT = 9; 
	List<OnWolSendCompleteListener> _wolSendCompleteListeners; 
	List<OnWolSendProgressListener> _wolSendProgressListeners;
	
	public WolSender()
	{
		_wolSendCompleteListeners = new ArrayList<OnWolSendCompleteListener>();
		_wolSendProgressListeners = new ArrayList<OnWolSendProgressListener>(); 
	}
	
	@Override
	protected Void doInBackground(Device... params) 
	{
        for(Device d : params)
        	SendPacket(d);
        
        return null;
	}
	
	
	private void SendPacket(Device device)
	{
		String ip = "10.0.0.255";
		
		try 
		{
            byte[] macBytes = MacAddress.getBytesFromString(device.getMacAddress());
            
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) 
                bytes[i] = (byte) 0xff;
            
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            
            InetAddress address = InetAddress.getByName(ip);
            
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            
            Log.i(TAG, "Magic packet sent successfully to: " + ip + " " + device.getMacAddress());
        }
        catch (Exception e) 
        {
        	Log.e(TAG, "failed to send to Magic packet to: " + ip + " " + device.getMacAddress(),  e);
        }
	}
	
	
	
	@Override
	protected void onProgressUpdate(Device... device)
	{
		for(OnWolSendProgressListener l : _wolSendProgressListeners)
			l.onWolSendProgress(device[0]);
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		for(OnWolSendCompleteListener l : _wolSendCompleteListeners)
			l.onWolSendComplete();
	}
	
	 
	 
	public void addOnWolSendCompleteListener(OnWolSendCompleteListener listener) {
		_wolSendCompleteListeners.add(listener);
    }
	public void addOnWolSendProgressListener(OnWolSendProgressListener listener) {
		_wolSendProgressListeners.add(listener);
    }
	
	
}












