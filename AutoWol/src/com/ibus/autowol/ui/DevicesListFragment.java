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
	Spinner _netorkSpinner;
	DeviceListView _deviceListView;
	List<String> _savedLiveDevicesMac = new ArrayList<String>();
	String _savedRouterBssid;
	
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
		
		//create device list
		_deviceListView = (DeviceListView) getActivity().findViewById(R.id.host_list);
		_deviceListView.setOnItemClickListener(new DeviceListClickListener()); 
		
		//create network spinner
		_netorkSpinner = (Spinner) getActivity().findViewById(R.id.host_fragment_networks);
		_netorkSpinner.setOnItemSelectedListener(new NetorkSelectedListener());
		
		if(!initialiseNetworkSpinner())
		{
			Database database = new Database(getActivity());
			database.open();
			populateRouterSpinner(database);
			database.close();
		}
	}
	
	
	@Override
	public void onResume() 
	{
		//fragment is visible here
		super.onResume();
		PingDevices();
	
		Log.i(TAG, "onResume");
	}

	//
	//Network Spinner selection changed
	//
	public class NetorkSelectedListener implements OnItemSelectedListener
	{
		//get or create devices on our network.  
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			//after memory clear arg1 is null. Looks like the activiy starts and this is called as part of initialisation and when it is called 
			//arg1 is null. then straight after destroy is called and the activity is started again normally.
			//TODO: figure out what is going on here 
			if(arg1 == null)
				return;
			
			String routerBssid = (String)arg1.getTag();
			
			Database database = new Database(getActivity());
			database.open();
			
			List<Device> devices = database.getDevicesForRouter(routerBssid);
			Log.i(TAG, String.format("%d devices found for router with bssid: %s", devices.size(), routerBssid));
			
			boolean isConnected =_network.isWifiNetworkConnected(getActivity());
			if(isConnected)
			{
				if(devices.size() == 0)
				{
					//if this is the current network and it has no devices or we have not scanned for devices yet then scan
					//TODO: prompt user "you currently dont have any devices listed for the network, would you like to scan for devices now?"
					if(routerBssid.equals(_network.getRouter().getBssid()))
						ScanNetwork();
				}
				else
				{
				
					//We have selected the current network. restore last known live routers 
					if(routerBssid.equals(_network.getRouter().getBssid()))
					{
						_deviceListView.setRouterBssid(routerBssid);
						_deviceListView.setDevices(devices, _savedLiveDevicesMac);
						_pinger.start(devices);
					}
					else
					{
						//last selected network was the current network. get a list of devices known to be live so we can 
						//quickly show live routers when we come back
						if(_deviceListView.getRouterBssid().equals(_network.getRouter().getBssid()))
							_savedLiveDevicesMac = _deviceListView.getLiveDevices();	
						
						_deviceListView.setRouterBssid(routerBssid);
						_deviceListView.setDevices(devices);
						
						/*TODO: we should be pausing the thread here not terminating*/
						_pinger.stop();
					}
				}
			}
			else
			{
				_deviceListView.setRouterBssid(routerBssid);
				_deviceListView.setDevices(devices);
			}
			
			database.close();
		}
	
		
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	@Override
	public void onScanStart() 
	{
		if(initialiseNetworkSpinner())
			ScanNetwork();
	}

	@Override
	public void onPingComplete(boolean success) 
	{
		Log.i(TAG, "Ping complete");
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
		
		if(result.success)
		{
			_deviceListView.showDeviceOn(result.device);
		}
		else
			_deviceListView.showDeviceOff(result.device);
	}
	
	@Override
	public void onStop() 
	{
		//fragment is hidden here
		super.onStop();

		_pinger.stop();		
		Log.i(TAG, "onStop");
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_hostEnumerator.stop();
	}
	
	@Override
	public void onScanProgress(ThreadResult thread) 
	{
		if(thread.device != null)
		{
			UpdateDevicelist(thread.device);
		}
	}
		
	@Override
	public void onScanComplete() 
	{
		Database database = new Database(getActivity());
		database.open();
		
		Router r = database.getRouterForBssid(_network.getRouter().getBssid());
		
		//add or update all devices in our adapter to our db 
		database.saveDevicesForRouter(_deviceListView.GetItems(), r.getPrimaryKey());
		
		database.close();
		
		_progressDialog.dismiss();
	}

	//called after AddDeviceActivity has completed successfully
	public void addDevice(int deviceId) 
	{
		Database database = new Database(getActivity());
		database.open();
		
		Device d = database.getDevice(deviceId);
		UpdateDevicelist(d);
		
		database.close();
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public void UpdateDevicelist(Device device)
	{
		if(_deviceListView.isInList(device.getMacAddress()))
			_deviceListView.updateDevice(device);
		else
			_deviceListView.addDevice(device);
	}
	
	
	public boolean initialiseNetworkSpinner()
	{
		boolean isConnected =_network.isWifiNetworkConnected(getActivity());
		
		if(isConnected)
		{
			Database database = new Database(getActivity());
			database.open();
			
			//update or create router
			Router router = _network.getRouter();
			database.saveRouter(router);
	
			//populate network spinner with all of our routers again since the list might have changed
			populateRouterSpinner(database);
			
			//Select the router of our current network. Note the NetorkSelectedListener will fire regardless of 
			//whether we select anything 
			selectRouter(router.getBssid());
			
			database.close();
			return true;
		}
		
		Toast.makeText(getActivity(), "Network scan aborted: you are not connected to a network", Toast.LENGTH_LONG).show();
		return false;
	}
	
	
	private void PingDevices()
	{
		boolean isConnected =_network.isWifiNetworkConnected(getActivity());
		if(!isConnected)
			return;
		
		Router r =  (Router)_netorkSpinner.getSelectedItem();
		
		//ping displayed devices if they belong to the network we are in
		if(r.getBssid().equals(_network.getRouter().getBssid()))
		{
	        _pinger.start(_deviceListView.GetItems());
		}
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
	
	private void populateRouterSpinner(Database database)
	{
		 List<Router> rl = database.getAllRouters();
		 NetworkSpinnerAdapter ntwkAdapter =new NetworkSpinnerAdapter(rl, getActivity().getLayoutInflater()); 
		 _netorkSpinner.setAdapter(ntwkAdapter);
	}


	private void selectRouter(String bssid)
	{ 
		 NetworkSpinnerAdapter ntwkAdapter = (NetworkSpinnerAdapter)_netorkSpinner.getAdapter();
		 
		 int pos = ntwkAdapter.GetPositionForBssid(bssid);
		 if(pos != -1)
		 {
			 _netorkSpinner.setSelection(pos);
			 Log.i(TAG, "router found in network spinner and selected");
		 }
		 else
			 Log.i(TAG, "router was not found in our netowork spinner. SOMETHING HAS GONE WRONG.");
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
			List<Device> dl = getSelectedItems();
			
			if(item.getItemId() == R.id.device_list_context_menu_delete)
			{
				Database database = new Database(getActivity());
				database.open();
				
				database.deleteDevices(dl);
				_deviceListView.removeDevices(dl);
			
            	mode.finish();
                return false;
			}
			else if(item.getItemId() == R.id.device_list_context_menu_wake)
			{
				Device[] da = dl.toArray(new Device[dl.size()]);
				WolSender sender = new WolSender();
				sender.execute(da);
			}
			
			return true;
		}
	}


	
	
	


	/* @Override        
	 public void onSaveInstanceState(Bundle SavedInstanceState) 
	 {
		 super.onSaveInstanceState(SavedInstanceState);
		 
		 Router r =  (Router)_netorkSpinner.getSelectedItem();
		 if(r != null)
			 SavedInstanceState.putString("CurrentRouterBssid", String.format("%d", _savedLiveDevicesMac.size()));
		 
		 if(_savedLiveDevicesMac.size() > 0)
		 {
			 SavedInstanceState.putString("LiveDevicesCount", Integer.toString(_savedLiveDevicesMac.size()));
			 
			 for(int i=0; i < _savedLiveDevicesMac.size(); ++i)
				 SavedInstanceState.putString("Device_" + Integer.toString(i), _savedLiveDevicesMac.get(i));	 
		 }
		 
		 Log.i(TAG, "Saved instance state");
	 }
	 
	 public void restoreSavedState(Bundle SavedInstanceState)
	 {
		 if(SavedInstanceState != null)
		 {
			 _savedRouterBssid =  SavedInstanceState.getString("CurrentRouterBssid");
			 
			 if(SavedInstanceState.getString("LiveDevicesCount") != null)
			 {
				 int devicesCount = Integer.parseInt(SavedInstanceState.getString("LiveDevicesCount"));
				 
				 for(int i=0; i < devicesCount; ++i)
				 {
					 _savedLiveDevicesMac.add(SavedInstanceState.getString("Device_" + Integer.toString(i)));
				 }
			 }
		 }
		 
	 }*/
	 
	
	
	
	
}




/*private void PromptNetworkScan()
{
	PromptNetworkScanDialog dlg = new PromptNetworkScanDialog();
	dlg.setCancelable(false);
	dlg.onAttach(this);
	dlg.show(((SherlockFragmentActivity)getActivity()).getSupportFragmentManager(), "PromptNetworkScanDialog");
}*/


/*@Override
public void onPromptNetworkScanPositiveClick(DialogInterface dialog) 
{
	GoToNetworkScanActivity();
}

@Override
public void onPromptNetworkScanNegativeClick(DialogInterface dialog) 
{
}*/

/*public void GoToNetworkScanActivity()
{
	Intent intent = new Intent(getActivity(), NetworkScanActivity.class);
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    getActivity().startActivity(intent);
}*/












