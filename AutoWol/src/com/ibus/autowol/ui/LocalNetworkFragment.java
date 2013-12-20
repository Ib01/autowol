package com.ibus.autowol.ui;

import java.io.IOException;
import java.util.ArrayList;
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
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.MainActivity;
import com.ibus.autowol.R;
import com.ibus.autowol.backend.Database;
import com.ibus.autowol.backend.Device;
import com.ibus.autowol.backend.Factory;
import com.ibus.autowol.backend.IHostEnumerator;
import com.ibus.autowol.backend.INetwork;
import com.ibus.autowol.backend.IPinger;
import com.ibus.autowol.backend.IWolSender;
import com.ibus.autowol.backend.Network;
import com.ibus.autowol.backend.Router;
import com.ibus.autowol.backend.ThreadResult;
import com.ibus.autowol.backend.WolSender;

public class LocalNetworkFragment extends SherlockFragment 
implements OnScanProgressListener, OnScanCompleteListener, OnScanStartListener, OnPingProgressListener, OnPingCompleteListener, OnDeviceWakeListener
{
	private final static String TAG = "AutoWol-DevicesListFragment";
	ProgressDialog _progressDialog;
	IHostEnumerator _hostEnumerator;
	IPinger _pinger;
	INetwork _network;
	DeviceListView _deviceListView;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		//need to create network here because our activity context has not yet been created
		_network = Factory.getNetwork(getActivity());
		
		_hostEnumerator = Factory.getHostEnumerator();
		_hostEnumerator.addOnScanProgressListener(this);
		_hostEnumerator.addOnScanCompleteListener(this);
		
		_pinger = Factory.getPinger();
        _pinger.addOnPingCompleteListener(this);
        _pinger.addOnPingProgressListener(this);
		
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
		
		Router r = _network.getRouter();
		saveRouter(r);
		setNetworkHeader(r);
		
		if(_network.isWifiConnected(getActivity()))
		{
			boolean devicesFound = loadDevicesList(r);
			if(!devicesFound)
			{
				//scan network if our network is up and we don't currently have any devices for it in our db 
				ScanNetwork();	
			}
		}
		
	}
	
	
	@Override
	public void onResume() 
	{
		//fragment is visible here
		super.onResume();
		
		if(_network.isWifiConnected(getActivity()))
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
		Router r = _network.getRouter();
		saveRouter(r);
		setNetworkHeader(r);
		
		if(_network.isWifiConnected(getActivity()))
		{
			ScanNetwork();
			return;
		}
		Toast.makeText(getActivity(), "Network scan aborted: you are not connected to a network", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onScanProgress(ThreadResult result) 
	{
		if(result.device != null)
		{
			_deviceListView.addDevice(result.device);
			saveDevice(result.device);
			refreshPinger();
		}
	}
	
	@Override
	public void onScanComplete() 
	{
		_progressDialog.dismiss();
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
		
		Device d = _deviceListView.setDeviceLive(result.device.getMacAddress(),result.success);
		saveDevice(d);
	}
	
	@Override
	public void onPingComplete(boolean success) 
	{
		Log.i(TAG, "Ping complete");
	}

	
	@Override
	public void onDeviceWake(Device device) 
	{
		int i = 0;
		
	}
	
	


	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	
	public void saveDevice(Device device) 
	{
		Database database = new Database(getActivity());
		database.open();
		Router r = database.getRouterForBssid(_network.getRouter().getBssid());

		//TODO: error occurring here.  r is null. _network.getRouter().getBssid() above is probably returning null
		//possibilities we need to cater for:
		//
		//1) network is down (may only be a split second).  then either _network.getRouter() throws 
		//or _network.getRouter().getBssid() returns null.  we should show "network unavailable"? 
		//but what then: all pinging stops, so then how does getRouter get called again without a manual scan? 
		//
		//2) the network bssid has changed. how do we know it has changed? and if it has we will need to reload the 
		//whole screen and then save this item?  
		
		database.saveDeviceForMac(device, r.getPrimaryKey()); 
		database.close();
	}
	
	//Router router = _network.getRouter();
	public void saveRouter(Router router) 
	{
		if(_network.isWifiConnected(getActivity()))
		{
			Database database = new Database(getActivity());
			database.open();
			database.saveRouterForBssid(router);
			database.close();
		}
	}
	
	public boolean loadDevicesList(Router router)
	{
		List<Device> devices = new ArrayList<Device>();
		
		Database database = new Database(getActivity());
		database.open();
		
		String bssid = router.getBssid();
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
		
		if(_network.isWifiConnected(getActivity()))
		{
			network.setText(router.getSsid());
			network.setTag(router.getBssid());
			return;
		}
		
		network.setText("WiFi Unavailable");
	}
	
	
	private void ScanNetwork()
	{
		_progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_DARK);
		_progressDialog.setTitle("Scanning network...");
		_progressDialog.setMessage("Please wait.");
		_progressDialog.setCancelable(false);
		_progressDialog.setIndeterminate(true);
		_progressDialog.show();
		
		_hostEnumerator.start(_network);
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
				try 
				{
					IWolSender sender = Factory.getWolSender(_network.getBroadcastAddress());
					sender.start(getDevicesListCopy(dl));
				} catch (IOException e) {

					Log.e(TAG, "could not get brodacast address for unknown reason", e);
				}
				
			}
			
			return true;
		}
	}
	
	


	//called after AddDeviceActivity has completed successfully
	public void addOrUpdateDevice(int devicePk) 
	{
		Device d = getDevice(devicePk);
		_deviceListView.addDevice(d);
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

	
	
	
}












