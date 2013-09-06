package com.ibus.autowol.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.ibus.autowol.R;
import com.ibus.autowol.backend.Database;
import com.ibus.autowol.backend.Device;
import com.ibus.autowol.backend.Factory;
import com.ibus.autowol.backend.IHostEnumerator;
import com.ibus.autowol.backend.INetwork;
import com.ibus.autowol.backend.IPinger;
import com.ibus.autowol.backend.Router;
import com.ibus.autowol.backend.ThreadResult;
import com.ibus.autowol.backend.WolSender;

public class DevicesListFragment extends SherlockFragment 
implements OnScanProgressListener, OnScanCompleteListener, OnScanStartListener, OnPingProgressListener, OnPingCompleteListener
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
		
        View v = inflater.inflate(R.layout.host_fragment, container, false);
        return v; 
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) 
	{
		Log.i(TAG, "DevicesListFragment.onActivityCreated is executing");
		super.onActivityCreated(savedInstanceState);
		
		_deviceListView = (DeviceListView) getActivity().findViewById(R.id.host_list);
		_deviceListView.setOnItemClickListener(new DeviceListClickListener()); 
		
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
			_pinger.start(_deviceListView.GetItems());
	
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
	public void onAttach (Activity activity)
	{
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach ()
	{
		super.onDetach();
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

	
	
	
	


	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public void saveDevice(Device device) 
	{
		Database database = new Database(getActivity());
		database.open();
		Router r = database.getRouterForBssid(_network.getRouter().getBssid());
		database.saveDevice(device, r.getPrimaryKey());
		database.close();
	}
	
	//Router router = _network.getRouter();
	public void saveRouter(Router router) 
	{
		if(_network.isWifiConnected(getActivity()))
		{
			Database database = new Database(getActivity());
			database.open();
			database.saveRouter(router);
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
		TextView network = (TextView) getActivity().findViewById(R.id.host_fragment_networks);
		
		if(_network.isWifiConnected(getActivity()))
		{
			network.setText(router.getSsid());
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
			super((SherlockFragmentActivity)getActivity(), R.menu.device_list_context_menu);
		}

		@Override
		public boolean actionItemClicked(ActionMode mode, MenuItem item) 
		{
			if(item.getItemId() == R.id.device_list_context_menu_delete)
			{
	            	mode.finish();
	                return false;
			}
			else if(item.getItemId() == R.id.device_list_context_menu_wake)
			{
				List<Device> dl = getSelectedItems();
				Device[] da = dl.toArray(new Device[dl.size()]);
				
				WolSender sender = new WolSender();
				sender.execute(da);
			}
			
			return true;
		}
	}


	//called after AddDeviceActivity has completed successfully
	public void addDevice(String string) 
	{
		
	}
	
	
}












