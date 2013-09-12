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
				R.id.device_list_item_ip_address, new ArrayList<Device>());
		setAdapter(_deviceListadapter);
	}

	public void setDevices(List<Device> devices) {
		_deviceListadapter.clear();
		_deviceListadapter.addAll(devices);
		_deviceListadapter.notifyDataSetChanged();
	}
	
	
	public Device setDeviceLive(String macAddresses, boolean isLive)
	{
		Device ret = null;
		
		for(Device d : _devices)
		{
			if(d.getMacAddress().equals(macAddresses)){
				d.setIsLive(isLive);
				ret = d;
			}
		}		
		
		refreshViewStyle(macAddresses);
		
		return ret;
	}
	
	private void refreshViewStyle(String macAddresses) 
	{
		for (int i = 0; i < this.getChildCount(); ++i) 
		{
			View v = this.getChildAt(i);
			Device dev = (Device) v.getTag();
			
			if (dev.getMacAddress().equals(macAddresses)) 
				setViewStyle(v);
		}
	}
	
	public List<String> getLiveDevices()
	{
		List<String> dl = new ArrayList<String>();
		
		for (Device d : _devices) 
		{
			if (d.getIsLive()) 
			{
				dl.add(d.getMacAddress());
			}
		}
		
		return dl;
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
	
	public boolean isInList(String deviceMac)
	{
		Device d = GetDeviceForMac(deviceMac);
		return (d != null);
	}
	
	
	public void addDevice(Device device)
	{
		if(!isInList(device.getMacAddress()))
		{
			_deviceListadapter.add(device);
		}
		else{
			Device d = GetDeviceForMac(device.getMacAddress());
			d.copyFromScannedDevice(device);
		}
		
		_deviceListadapter.notifyDataSetChanged();
	}
	
	public void removeDevices(List<Device> devices)
	{
		for(Device d : devices)
		{
			_deviceListadapter.remove(d);	
		}
		
		_deviceListadapter.notifyDataSetChanged();
	}

	
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////

	private void setViewStyle(View v)
	{
		if(((Device)v.getTag()).getIsLive())
			setViewOn(v);
		else
			setViewOff(v); 
	}
	
	
	private void setViewOff(View v) 
	{
		TextView ip = (TextView) v.findViewById(R.id.device_list_item_ip_address);
		TextView mac = (TextView) v
				.findViewById(R.id.device_list_item_mac_address);
		TextView pcName = (TextView) v
				.findViewById(R.id.device_list_item_pc_name);
		ImageView img = (ImageView) v
				.findViewById(R.id.device_list_item_device_image);

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
		TextView ip = (TextView) v.findViewById(R.id.device_list_item_ip_address);
		TextView mac = (TextView) v
				.findViewById(R.id.device_list_item_mac_address);
		TextView pcName = (TextView) v
				.findViewById(R.id.device_list_item_pc_name);
		ImageView img = (ImageView) v
				.findViewById(R.id.device_list_item_device_image);

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
				v = inflater.inflate(R.layout.device_list_item_layout, null);
			}

			Device host = _devices.get(position); 
			
			if (host != null) 
			{
				//ImageView img = (ImageView) v.findViewById(R.id.host_list_item_device_image);
				TextView ip = (TextView) v.findViewById(R.id.device_list_item_ip_address);
				TextView mac = (TextView) v.findViewById(R.id.device_list_item_mac_address);
				TextView pcName = (TextView) v.findViewById(R.id.device_list_item_pc_name);

				ip.setText(host.getIpAddress());
				mac.setText(host.getMacAddress());
				pcName.setText(host.getName());
				
				v.setTag(host);
				setViewStyle(v);
			}
			
			return v;
		}
		
	}
	

}











