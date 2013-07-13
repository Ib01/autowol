package com.ibus.autowol.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibus.autowol.R;
import com.ibus.autowol.backend.Device;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListView extends ListView 
{
	HostListAdapter _deviceListadapter;
	private List<Device> _devices;
	private String _routerBssid = "";

	public String getRouterBssid() {
		return _routerBssid;
	}

	public void setRouterBssid(String _routerBssid) {
		this._routerBssid = _routerBssid;
	}

	public DeviceListView(Context context) {
		super(context);
		init();
	}

	// This example uses this method since being built from XML
	public DeviceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	// Build from XML layout
	public DeviceListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		setChoiceMode(DeviceListView.CHOICE_MODE_SINGLE);
		_deviceListadapter = new HostListAdapter(getContext(),
				R.id.host_list_item_ip_address, new ArrayList<Device>());
		setAdapter(_deviceListadapter);
	}

	public void setDevices(List<Device> devices) {
		_deviceListadapter.clear();
		_deviceListadapter.addAll(devices);
		_deviceListadapter.notifyDataSetChanged();
	}
	
	public void setDevices(List<Device> devices, List<String> macAddresses) {
		_deviceListadapter.clear();
		_deviceListadapter.addAll(devices);
		setLiveDevices(macAddresses);
		_deviceListadapter.notifyDataSetChanged();
	}
	

	//this status will be picked up when the view is created in our adapter
	public void setLiveDevices(List<String> macAddresses)
	{
		for(Device d : _devices)
		{
			for(String m : macAddresses)
			{
				if(d.getMacAddress().equals(m))
					d.setLive(true);
			}
		}
	}
	
	public List<String> getLiveDevices()
	{
		List<String> dl = new ArrayList<String>();
		
		for (Device d : _devices) 
		{
			if (d.isLive()) 
			{
				dl.add(d.getMacAddress());
			}
		}
		
		return dl;
	}
	
	//only usefull if the device is in the list which it wont be 
	//if we call this straight after adding it  
	public void showDeviceOn(Device device) 
	{
		for (int i = 0; i < this.getChildCount(); ++i) 
		{
			View v = this.getChildAt(i);
			Device dev = (Device) v.getTag();
			
			if (dev.getMacAddress().equals(device.getMacAddress())) 
			{
				setViewOn(v);
				dev.setLive(true);
			}
		}
	}

	//only usefull if the device is in the list which it wont be 
	//if we call this straight after adding it
	public void showDeviceOff(Device device) 
	{
		for (int i = 0; i < this.getChildCount(); ++i) 
		{
			View v = this.getChildAt(i);
			Device dev = (Device) v.getTag();
			
			if (dev.getMacAddress().equals(device.getMacAddress())) 
			{
				setViewOff(v);
				dev.setLive(false);
			}
		}
	}

	public List<Device> GetItems()
	{
		return _devices;
	}
	
	public int GetPositionForMac(String deviceMac)
	{
		Device d= GetDeviceForMac(deviceMac);
		if(d == null)
			return -1;
		
		return _devices.indexOf(d);
	}

	public Device GetDeviceForMac(String deviceMac)
	{
		for(Device d : _devices)
		{
			if(d.getMacAddress().equals(deviceMac))
				return d;
		}
		
		return null;
	}
	
	public void removeDevices(List<Device> devices)
	{
		for(Device d : devices)
		{
			Device dref = GetDeviceForMac(d.getMacAddress());
			_deviceListadapter.remove(dref);	
		}
		_deviceListadapter.notifyDataSetChanged();
	}
	
	
	public void addDevice(Device device)
	{
		_deviceListadapter.add(device);
		_deviceListadapter.notifyDataSetChanged();
	}
	
	public boolean isInList(String deviceMac)
	{
		Device d = GetDeviceForMac(deviceMac);
		return (d != null);
	}
	
	public void updateDevice(Device device)
	{
		Device d = GetDeviceForMac(device.getMacAddress());
		d.copyFromScannedDevice(device);
		_deviceListadapter.notifyDataSetChanged();
	}
	
	
	
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////

	private void setViewOff(View v) 
	{
		TextView ip = (TextView) v.findViewById(R.id.host_list_item_ip_address);
		TextView mac = (TextView) v
				.findViewById(R.id.host_list_item_mac_address);
		TextView pcName = (TextView) v
				.findViewById(R.id.host_list_item_pc_name);
		ImageView img = (ImageView) v
				.findViewById(R.id.host_list_item_device_image);

		img.setImageDrawable(getContext().getResources().getDrawable(
				R.drawable.ic_pc_dissabled));
		ip.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));
		mac.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));
		pcName.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));
	}

	private void setViewOn(View v) 
	{
		TextView ip = (TextView) v.findViewById(R.id.host_list_item_ip_address);
		TextView mac = (TextView) v
				.findViewById(R.id.host_list_item_mac_address);
		TextView pcName = (TextView) v
				.findViewById(R.id.host_list_item_pc_name);
		ImageView img = (ImageView) v
				.findViewById(R.id.host_list_item_device_image);

		img.setImageDrawable(getContext().getResources().getDrawable(
				R.drawable.ic_pc));
		ip.setTextColor(getContext().getResources().getColor(R.color.text));
		mac.setTextColor(getContext().getResources().getColor(R.color.text));
		pcName.setTextColor(getContext().getResources().getColor(R.color.text));
	}
	
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// Adapter
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	
	public class HostListAdapter extends ArrayAdapter<Device> 
	{
		public HostListAdapter(Context context, int textViewResourceId, List<Device> objects) 
		{
			super(context, textViewResourceId, objects);
			_devices = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;

			// first check to see if the view is null. if so, we have to inflate it.
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.host_list_item, null);
			}

			Device host = _devices.get(position); 
			
			if (host != null) 
			{
				//ImageView img = (ImageView) v.findViewById(R.id.host_list_item_device_image);
				TextView ip = (TextView) v.findViewById(R.id.host_list_item_ip_address);
				TextView mac = (TextView) v.findViewById(R.id.host_list_item_mac_address);
				TextView pcName = (TextView) v.findViewById(R.id.host_list_item_pc_name);

				ip.setText(host.getIpAddress());
				mac.setText(host.getMacAddress());
				pcName.setText(host.getName());
				
				if(host.isLive())
					setViewOn(v);
				else
					setViewOff(v);
				
				v.setTag(host);
			}
			
			return v;
		}
		
	}
	

}











