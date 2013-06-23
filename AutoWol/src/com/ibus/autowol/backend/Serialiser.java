package com.ibus.autowol.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class Serialiser 
{
	private static final String TAG = "Serialiser";
	private static final String _devicesFile = "devices.bin";
	
	
	public static void AddHosts(HashSet<Device> hosts, SherlockFragmentActivity activity)
	{
		@SuppressWarnings("unchecked")
		HashSet<Device> savedDevices = GetHosts(activity);
		savedDevices.addAll(hosts);
		
		Serialise(savedDevices, _devicesFile, activity);
	}
	
	public static void AddHosts(List<Device> hosts, SherlockFragmentActivity activity)
	{
		@SuppressWarnings("unchecked")
		HashSet<Device> savedDevices = GetHosts(activity);
		savedDevices.addAll(hosts);
		
		Serialise(savedDevices, _devicesFile, activity);
	}
	
	public static void DeleteHost(Device host, SherlockFragmentActivity activity)
	{
		@SuppressWarnings("unchecked")
		HashSet<Device> savedDevices = GetHosts(activity);
		savedDevices.remove(host);
		
		Serialise(savedDevices, _devicesFile, activity);
	}
	
	public static void AddHost(Device host, SherlockFragmentActivity activity)
	{
		@SuppressWarnings("unchecked")
		HashSet<Device> savedDevices = GetHosts(activity);
		savedDevices.add(host);
		
		Serialise(savedDevices, _devicesFile, activity);
	}
	
	@SuppressWarnings("unchecked")
	public static HashSet<Device> GetHosts(SherlockFragmentActivity activity)
	{
		HashSet<Device> hl = (HashSet<Device>)Serialiser.Deserialise(_devicesFile, activity);
		if(hl == null)
			hl = new HashSet<Device>();
		
		return hl;
	}
	
	
	
	public static void Serialise(Object objectToSave, String fileName, SherlockFragmentActivity activity)
	{
		ObjectOutputStream oos;
		try 
		{
			FileOutputStream fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(objectToSave); // write the class as an 'object'
			oos.flush(); // flush the stream to insure all of the information was written
			oos.close();// close the stream
		} 
		catch (FileNotFoundException e) 
		{
			Log.e(TAG, "Could not serialise object", e);	
		} 
		catch (IOException e) 
		{
			Log.e(TAG, "Could not serialise object", e);
		} 	
		finally
		{
			
		}
		
	}
	
	public static Object Deserialise(String fileName, SherlockFragmentActivity activity)
	{
		ObjectInputStream ois;
		try 
		{
			FileInputStream inp = activity.openFileInput(fileName);
			ois = new ObjectInputStream(inp);
			Object o = ois.readObject();
			return o;
		} 
		catch (StreamCorruptedException e) {
			Log.e(TAG, "Could not deserialise object", e);	
		} 
		catch (FileNotFoundException e) {
			Log.e(TAG, "Could not deserialise object", e);	
		} 
		catch (IOException e) {
			Log.e(TAG, "Could not deserialise object", e);	
		} 
		catch (ClassNotFoundException e) {
			Log.e(TAG, "Could not deserialise object", e);	
		}
		
		return null;
	}
	
	
	
}
