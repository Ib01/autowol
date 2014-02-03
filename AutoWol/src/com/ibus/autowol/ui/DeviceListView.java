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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceListView extends ListView 
{
	HostListAdapter _deviceListadapter;
	private List<Device> _devices;
	List<OnDeviceWakeListener> _deviceWakeListeners = new ArrayList<OnDeviceWakeListener>(); 
	
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
	
	//
	private void refreshViewStyle(String macAddresses) 
	{
		for (int i = 0; i < this.getChildCount(); ++i) 
		{
			View v = this.getChildAt(i);
			Device dev = (Device) v.getTag();
		
			//TODO: INSTEAd of calling setViewstle call _deviceListadapter.notifyDataSetChanged();??
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
	

	public List<Device> getDevices()
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
	
	public boolean deviceIsInList(String deviceMac)
	{
		Device d = GetDeviceForMac(deviceMac);
		return (d != null);
	}
	
	
	public void addOrUpdateDevice(Device device)
	{
		if(!deviceIsInList(device.getMacAddress()))
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
	
	public int getDevicesCount()
	{
		return _devices.size();
	}
	
	public int getLiveDevicesCount()
	{
		return getLiveDevices().size();
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
				.findViewById(R.id.device_list_item_power_image);
		TextView powerStatus = (TextView) v
				.findViewById(R.id.device_list_item_power_status);
		
		powerStatus.setText("Off");
		img.setImageDrawable(getContext().getResources().getDrawable(
				R.drawable.ic_power_button));
		
		/*ip.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));
		mac.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));
		pcName.setTextColor(getContext().getResources().getColor(
				R.color.dissabled_text));*/
	}

	private void setViewOn(View v) 
	{
		TextView ip = (TextView) v.findViewById(R.id.device_list_item_ip_address);
		TextView mac = (TextView) v
				.findViewById(R.id.device_list_item_mac_address);
		TextView pcName = (TextView) v
				.findViewById(R.id.device_list_item_pc_name);
		ImageView img = (ImageView) v
				.findViewById(R.id.device_list_item_power_image);
		TextView powerStatus = (TextView) v
				.findViewById(R.id.device_list_item_power_status);
		
		powerStatus.setText("On");
		img.setImageDrawable(getContext().getResources().getDrawable(
				R.drawable.ic_power_button_on));
		
		
		/*ip.setTextColor(getContext().getResources().getColor(R.color.text));
		mac.setTextColor(getContext().getResources().getColor(R.color.text));
		pcName.setTextColor(getContext().getResources().getColor(R.color.text));*/
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
				View powerlayout = (View) v.findViewById(R.id.device_list_item_power_layout);
				TextView ip = (TextView) v.findViewById(R.id.device_list_item_ip_address);
				TextView mac = (TextView) v.findViewById(R.id.device_list_item_mac_address);
				TextView pcName = (TextView) v.findViewById(R.id.device_list_item_pc_name);
				
				ip.setText(host.getIpAddress());
				mac.setText(host.getMacAddress());
				
				if(host.getName() == null || host.getName().isEmpty())
					pcName.setText("Unknown Host Name");
				else
					pcName.setText(host.getName());
				
				v.setTag(host);
				powerlayout.setTag(host);
				powerlayout.setOnClickListener(new PowerButtonListener());
				
				setViewStyle(v);
			}
			
			return v;
		}
		
	}
	
	public class PowerButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v) 
		{
			for(OnDeviceWakeListener l : _deviceWakeListeners){
				l.onDeviceWake((Device)v.getTag());
			}
			
		}	
	}
	
	
	public void addOnDeviceWakeListener(OnDeviceWakeListener listener) 
	{
		_deviceWakeListeners.add(listener);
    }
	

}











