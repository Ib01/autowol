package com.ibus.autowol.backend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;


public abstract class Udp 
{
	private final String TAG = "UdpProber";
	private static final short   NETBIOS_UDP_PORT = 137;
	private static final int TIMEOUT_MILISECONDS = 200;
	
	// NBT UDP PACKET: QUERY; REQUEST; UNICAST
	private static final byte[]  NETBIOS_REQUEST  = 
	{ 
		(byte)0x82, (byte)0x28, (byte)0x0,  (byte)0x0,  (byte)0x0, 
		(byte)0x1,  (byte)0x0,  (byte)0x0,  (byte)0x0,  (byte)0x0, 
		(byte)0x0,  (byte)0x0,  (byte)0x20, (byte)0x43, (byte)0x4B, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, (byte)0x41, 
		(byte)0x0,  (byte)0x0,  (byte)0x21, (byte)0x0,  (byte)0x1
	};
	
	public static void probe(String ipAddress)
	{
		InetAddress ip;
		try {
			ip = InetAddress.getByName(ipAddress);
			DatagramSocket socket  = new DatagramSocket();
			DatagramPacket packet  = new DatagramPacket( NETBIOS_REQUEST, NETBIOS_REQUEST.length, ip, NETBIOS_UDP_PORT );

			socket.setSoTimeout(TIMEOUT_MILISECONDS);
			socket.send(packet);    	  
			socket.close();
			
			Log.i( "NETBIOS", "Sent Udp packet successfully to: " + ipAddress );
			
		} catch (UnknownHostException e) {
			Log.e("UdpProber", "UnknownHostException occured", e );
		} catch (SocketException e) {
			Log.e("UdpProber", "SocketException occured", e );
		} catch (IOException e) {
			Log.e("UdpProber", "IOException occured", e );
		}
	}
	
	
	public static String getHostName(String ip)
	{
		String name = null;
		InetAddress mAddress = null;
		DatagramSocket mSocket = null;
		byte[] buffer = new byte[128];
		
		try 
		{
			mAddress = InetAddress.getByName(ip);
			mSocket = new DatagramSocket();
			mSocket.setSoTimeout(TIMEOUT_MILISECONDS);
			
			DatagramPacket packet  = new DatagramPacket( buffer, buffer.length, mAddress, NETBIOS_UDP_PORT );
			DatagramPacket query   = new DatagramPacket( NETBIOS_REQUEST, NETBIOS_REQUEST.length, mAddress, NETBIOS_UDP_PORT );
			
			mSocket.send(query);
			mSocket.receive(packet);
			
			byte[] data = packet.getData();
			if( data != null && data.length >= 74 )
			{
				String response = new String(data, "ASCII" );

				// i know this is horrible, but i really need only the netbios name
				name = response.substring( 57, 73 ).trim();	
				if(name != null)
					Log.i( "NETBIOS", ip + " was resolved to " + name );
				else
					Log.i( "NETBIOS", ip + " could not be resolve");
			}
			
		} catch (UnknownHostException e1) {
			Log.e("Udp", "UnknownHostException occured in GetNetbiosName", e1 );
		} catch (SocketException e) {
			Log.e("Udp", "SocketException occured in GetNetbiosName", e );
		} catch (IOException e) {
			Log.e("Udp", "IOException occured in GetNetbiosName", e );
		}
		finally
		{
			mSocket.close();
		}
		
		return name;
	}
	
	
	
}







