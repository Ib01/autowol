package com.ibus.autowol.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.MainActivity;
import com.ibus.autowol.R;
import com.ibus.autowol.backend.ConnectionInfo;
import com.ibus.autowol.backend.Database;
import com.ibus.autowol.backend.Device;
import com.ibus.autowol.backend.Factory;
import com.ibus.autowol.backend.IConnectionInfo;
import com.ibus.autowol.backend.IHostEnumerator;
import com.ibus.autowol.backend.INetwork;
import com.ibus.autowol.backend.IPinger;
import com.ibus.autowol.backend.IWolSender;
import com.ibus.autowol.backend.Network;
import com.ibus.autowol.backend.Router;
import com.ibus.autowol.backend.ThreadResult;
import com.ibus.autowol.backend.WolSender;

public class LocalNetworkFragment extends SherlockFragment 
implements OnScanProgressListener, OnScanStartListener, OnScanCompleteListener, OnPingProgressListener, OnPingCompleteListener, OnDeviceWakeListener 
{
	private final static String TAG = "AutoWol-DevicesListFragment";
	IHostEnumerator _hostEnumerator;
	IPinger _pinger;
	INetwork _network;
	IConnectionInfo _connectionInfo;
	//IWolSender _wolSender;
	DeviceListView _deviceListView;
	ActionMode _scanActionMode;
	
	public LocalNetworkFragment()
	{
		_connectionInfo = Factory.getConnectionInfo();
		
		//_wolSender = Factory.getWolSender(_network.getBroadcastAddress());
		
		_hostEnumerator = Factory.getHostEnumerator();
		_hostEnumerator.addOnScanProgressListener(this);
		_hostEnumerator.addOnScanCompleteListener(this);
		
		_pinger = Factory.getPinger();
        _pinger.addOnPingCompleteListener(this);
        _pinger.addOnPingProgressListener(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        View v = inflater.inflate(R.layout.local_network_fragment_layout, container, false);
        return v; 
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) 
	{
		Log.i(TAG, "DevicesListFragment.onActivityCreated is executing");
		super.onActivityCreated(savedInstanceState);
		
		_deviceListView = (DeviceListView) getActivity().findViewById(R.id.local_network_fragment_device_list);
		_deviceListView.setOnItemClickListener(new DeviceListClickListener()); 
		_deviceListView.addOnDeviceWakeListener(this);
		
		if(_connectionInfo.isWifiConnected(getActivity())) 
		{
			_network = Factory.getNetwork(getActivity());
			if(_network.infoIsValid())
			{
				Router r = _network.getRouter();
				saveRouter(r);
				boolean devicesFound = loadDevicesList(r, r.getBssid());
				setNetworkHeader(r);
				
				if(!devicesFound)
				{
					//TODO: prompt user? do scan in dialogue?
					ScanNetwork();	
				}
				
				return;
			}
		}

		//TODO: Display network not available
		Toast.makeText(getActivity(), "Cannot display network devices: you are not connected to a network", Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	public void onResume() 
	{
		super.onResume();
		
		_pinger.start(getDevicesListCopy(_deviceListView.getDevices()));
		Log.i(TAG, "onResume");
	}

	@Override
	public void onStop() 
	{
		//fragment is hidden here
		super.onStop();

		_pinger.stop();
		_hostEnumerator.stop();
		Log.i(TAG, "onStop");
	}
		
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onScanStart() 
	{
		if(_connectionInfo.isWifiConnected(getActivity())) 
		{
			_network = Factory.getNetwork(getActivity());
			if(_network.infoIsValid())
			{
				Router r = _network.getRouter();
				saveRouter(r);
				setNetworkHeader(r);
				
				ScanNetwork();
				return;	
			}
		}
		
		Toast.makeText(getActivity(), "Network scan aborted: you are not connected to a network", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onScanProgress(ThreadResult result) 
	{
		//note: we dont have to check validity of router since our router was valid before our scan. if wifi was interupted during a 
		//scan then presumably it will stop? (TODO:CHECK THIS ASSUMPTION) 
		if(result.device != null)
		{
			_deviceListView.addOrUpdateDevice(result.device);
			saveDevice(result.device, _network.getRouter().getBssid());
			refreshPinger();
			setDevicesLiveCount(_network.getRouter());
		}
	}
	
	@Override
	public void onScanComplete() 
	{
		//_scanActionMode  may be set to null if user pressed back button
		if(_scanActionMode != null)
			_scanActionMode.finish();
	}

	
	@Override
	public void onPingProgress(ThreadResult result) 
	{
		Log.i(TAG, "Updating view for device");
		
		//thread may call into this method after onDestroy is called!!!!!
		if(getActivity() == null){
			Log.i(TAG, "getActivity is null ");
			return;
		}
		
		//note: we dont have to check validity of router since our router was valid during the last scan. if wifi was interupted during a 
		//scan then ping should continue but do nothing? (TODO:CHECK THIS ASSUMPTION)
		Device d = _deviceListView.setDeviceLive(result.device.getMacAddress(),result.success);
		saveDevice(d, _network.getRouter().getBssid());
		setDevicesLiveCount(_network.getRouter());
	}
	
	@Override
	public void onPingComplete(boolean success) 
	{
		Log.i(TAG, "Ping complete");
	}

	
	@Override
	public void onDeviceWake(Device device) 
	{
		wakeDevice(device);
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	//called after AddDeviceActivity has completed successfully
	public void addOrUpdateDevice(int devicePk) 
	{
		Device d = getDevice(devicePk);
		_deviceListView.addOrUpdateDevice(d);
		refreshPinger();
	}
	
	public void refreshPinger()
	{
		_pinger.setDevices(getDevicesListCopy(_deviceListView.getDevices()));
	}
	
	
	public List<Device> getDevicesListCopy(List<Device> list)
	{
		List<Device> dl = new ArrayList<Device>(); 
		for(Device d : list)
			dl.add(d.getCopy());
		
		return dl;
	}
	
	
	public void deleteDevices(List<Device> devices) 
	{
		Database database = new Database(getActivity());
		database.open();
		database.deleteDevices(devices);
		database.close();
	}
	
	
	public Device getDevice(int devicePk) 
	{
		Database database = new Database(getActivity());
		database.open();
		Device d = database.getDevice(devicePk);
		database.close();
		
		return d;
	}
	
	
	public void saveDevice(Device device, String routerBssid) 
	{
		Database database = new Database(getActivity());
		database.open();
		Router r = database.getRouterForBssid(routerBssid);
		database.saveDeviceForMac(device, r.getPrimaryKey()); 
		database.close();
	}
	
	//Router router = _network.getRouter();
	public void saveRouter(Router router) 
	{
		Database database = new Database(getActivity());
		database.open();
		database.saveRouterForBssid(router);
		database.close();
	}
	
	public boolean loadDevicesList(Router router, String bssid)
	{
		List<Device> devices = new ArrayList<Device>();
		
		Database database = new Database(getActivity());
		database.open();
		
		devices = database.getDevicesForRouter(bssid);
		_deviceListView.setDevices(devices);
		
		Log.i(TAG, String.format("%d devices found for router with bssid: %s", devices.size(), bssid));
	
		database.close();
		
		if(devices.size() <= 0)
			return false;
		
		return true; 
	}
	
	
	public void setNetworkHeader(Router router)
	{
		TextView network = (TextView) getActivity().findViewById(R.id.local_network_fragment_network);
	
		//TODO: display larger network unavailable message instead of setting the header?
		if(_connectionInfo.isWifiConnected(getActivity())){
			network.setText("WiFi Unavailable");
			network.setTag("");
		}
		
		network.setText(router.getSsid());
		network.setTag(router.getBssid());
		setDevicesLiveCount(router);
		setLastScanned(router);
		return;
	}
	
	public void setDevicesLiveCount(Router router)
	{
		TextView count = (TextView) getActivity().findViewById(R.id.local_network_fragment_live_count);
		count.setText(String.format("%d/%d", _deviceListView.getLiveDevicesCount(), _deviceListView.getDevicesCount())); 
	}

	public void setLastScanned(Router router)
	{
		TextView lastScanned = (TextView) getActivity().findViewById(R.id.local_network_fragment_last_scanned);
		lastScanned.setText(_network.getRouter().getLastScanned());
	}
	
	
	private void ScanNetwork()
	{
		_deviceListView.setEnabled(false);
		if(_scanActionMode == null)
			_scanActionMode = ((SherlockFragmentActivity)getActivity()).startActionMode(new ScanActionModeCallback());
    	
		_hostEnumerator.start(_network);
	}
	
    private class ScanActionModeCallback implements ActionMode.Callback
	{
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) 
		{
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.network_scan_context_menu, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
		{
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) 
		{
			_scanActionMode = null;
			_hostEnumerator.stop();
			_deviceListView.setEnabled(true);
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) 
		{
			return false;
		}
	}

	//
	//Devices list item clicked
	//
	public class DeviceListClickListener extends ListClickListener
	{
		public DeviceListClickListener() 
		{
			super((SherlockFragmentActivity)getActivity(), R.menu.local_network_fragment_context_menu);
		}

		@Override
		public boolean actionItemClicked(ActionMode mode, MenuItem item) 
		{
			if(item.getItemId() == R.id.device_list_context_menu_edit)
			{
				//TODO: WE ARE EDITING THE FIRST ITEM ONLY BUT THE USER CAN SELECT MULTIPLE ITEMS 
				List<Device> dl = getSelectedItems();
				int devId = dl.get(0).getPrimaryKey();
				
				Intent myIntent = new Intent();
		        myIntent.setClass(getActivity(), AddDeviceActivity.class);
		        myIntent.putExtra("DeviceId", devId);		        
		        getActivity().startActivityForResult(myIntent, MainActivity.UpdateDeviceActivityRequest); 
			}
			else if(item.getItemId() == R.id.device_list_context_menu_delete)
			{
            	List<Device> dl = getSelectedItems();
            	
            	_deviceListView.removeDevices(dl);
            	deleteDevices(dl);
            	refreshPinger();
            	
            	mode.finish();
            	
                return false;
			}
			else if(item.getItemId() == R.id.device_list_context_menu_wake)
			{
				List<Device> dl = getSelectedItems();
				wakeDevices(dl);
			}
			
			return true;
		}
	}
	
	private void wakeDevice(Device device)
	{
		List<Device> l = new ArrayList<Device>();
		l.add(device);
		
		wakeDevices(l);
	}
	
	
	private void wakeDevices(List<Device> devices)
	{
		if(_connectionInfo.isWifiConnected(getActivity())) 
		{
			//to get here devices must be displaying. and if so then presumably we must have valid 
			//Network info if object? 
			IWolSender sender = Factory.getWolSender(_network.getBroadcastAddress());
			sender.start(getDevicesListCopy(devices));
		}
	}


	
	
}












