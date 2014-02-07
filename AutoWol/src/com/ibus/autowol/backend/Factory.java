package com.ibus.autowol.backend;

import android.content.Context;

import com.ibus.autowol.mock.MockNetwork;

public class Factory 
{
	//ONLY SET TO TRUE IF WE ARE IN AN EMULATOR. 
	private static boolean inEmulator = false;
	
	/*private static INetwork _network;
	private static IHostEnumerator _hostEnumerator;
		  
	public static void setNetwork(INetwork network) {
		_network = network;
	}
	  
	public static void setHostEnumerator(IHostEnumerator hostEnumerator) {
		_hostEnumerator = hostEnumerator;
	}*/
	
	
	public static INetwork getNetwork(Context context) 
	{
		if(inEmulator)
			return new MockNetwork(); //if we are in an emulator
		
		return (INetwork) new Network(context);
	}
	  
	public static IHostEnumerator getHostEnumerator() 
	{
		return (IHostEnumerator) new NetworkScanner();
	}
	  
	public static IPinger getPinger()
	{
		return new PersistantPinger();
	}

	public static IWolSender getWolSender(String broadcastAddress)
	{
		return new WolSender(broadcastAddress);
	}
	
	public static IConnectionInfo getConnectionInfo()
	{
		return new ConnectionInfo();
	}
}
