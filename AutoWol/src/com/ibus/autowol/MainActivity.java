package com.ibus.autowol;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.ui.ActivityListItem;
import com.ibus.autowol.ui.AddDeviceActivity;
import com.ibus.autowol.ui.AddScheduleActivity;
import com.ibus.autowol.ui.LocalNetworkFragment;
import com.ibus.autowol.ui.NavigationSpinnerAdapter;
import com.ibus.autowol.ui.OnScanCompleteListener;
import com.ibus.autowol.ui.OnScanStartListener;
import com.ibus.autowol.ui.SchedulesListFragment;

public class MainActivity extends SherlockFragmentActivity 
{	
	public static final int AddScheduleActivityRequest = 1;
	public static final int AddDeviceActivityRequest = 2;
	public static final int UpdateDeviceActivityRequest = 3;
	private static final String TAG = "MainActivity";
	private ActionBarNavigationListener _actionBarNavigationListener;
	List<OnScanStartListener> _scanStartListeners; 
	private int _optionsMenu;
	 

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        _optionsMenu = R.menu.main_activity_options_menu;
        
        InitialiseActionBar();
        _scanStartListeners = new ArrayList<OnScanStartListener>();
        
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getSupportMenuInflater().inflate(_optionsMenu, menu);    	
        return true;   
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        { 
            case R.id.devices_list_fragment_scan:
            	startScan();  
            	break;
            case R.id.devices_list_fragment_add:
            	GoToAddDeviceActivity();  
            	break;
            case R.id.schedules_list_fragment_add:
            	GoToAddScheduleActivity();
            	break;
        }
        
        return true;
    }    
   
   

	private void startScan()
    {
    	for (OnScanStartListener listener : _scanStartListeners) 
		{
			listener.onScanStart();
        }
    }

	
	
	
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }

	@Override
	protected void onNewIntent(Intent intent) 
	{
	    super.onNewIntent(intent);
	    intent.getStringExtra("DATA");
	}
	
	public void addScanStartListener(OnScanStartListener listener) {
    	_scanStartListeners.add(listener); 
    }
    
    private void InitialiseActionBar()
    {
    	List<ActivityListItem> ar = new ArrayList<ActivityListItem>();
        ar.add(new ActivityListItem("Devices", "Devices"));
        ar.add(new ActivityListItem("Rules", "Rules"));
        ar.add(new ActivityListItem("Settings", "Settings"));
    	
        _actionBarNavigationListener = new ActionBarNavigationListener();
        
    	ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(new NavigationSpinnerAdapter(ar, this), _actionBarNavigationListener);
    }
    
    
    public void GoToAddScheduleActivity()
    {
    	/*Intent intent = new Intent(this, AddScheduleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
    	
    	//Intent.FLAG_ACTIVITY_CLEAR_TOP
    	
        Intent myIntent = new Intent();
        myIntent.setClass(MainActivity.this,AddScheduleActivity.class);
        startActivityForResult(myIntent,AddScheduleActivityRequest);
    }
    
    private void GoToAddDeviceActivity() 
    {
		Intent myIntent = new Intent();
        myIntent.setClass(MainActivity.this,AddDeviceActivity.class);
        startActivityForResult(myIntent,AddDeviceActivityRequest);
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode) 
		{
			case AddScheduleActivityRequest:
				if (resultCode == RESULT_OK) {
					_actionBarNavigationListener.getSchedulesListFragment().addSchedule("");					
				}
				break;
			case UpdateDeviceActivityRequest:
			case AddDeviceActivityRequest:
				if (resultCode == RESULT_OK) 
				{
					int pk = data.getIntExtra("DeviceId", -1);
					if(pk != -1)
					{
						_actionBarNavigationListener.getLocalNetworkFragment().addOrUpdateDevice(pk);
					}
					//TODO: else???
				}
				break;
		}
	}

	
    /*
	 @Override        
	 public void onSaveInstanceState(Bundle SavedInstanceState) 
	 {
		 super.onSaveInstanceState(SavedInstanceState);     
		 Log.i(TAG, "Saving instance state");
	 }*/
	
	 /*
	 @Override    
	 public void onRestoreInstanceState(Bundle savedInstanceState) {
		 super.onRestoreInstanceState(savedInstanceState);
		 Log.i(TAG, "Restoring instance state");
	 }*/ 
	
    
	

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Navigation listener //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	public class ActionBarNavigationListener implements ActionBar.OnNavigationListener
	{
		private LocalNetworkFragment _localNetworkFragment;
		private SchedulesListFragment _schedulesListFragment;
		
		

		public ActionBarNavigationListener()
		{
		}
		
		@Override
		public boolean onNavigationItemSelected(int position, long itemId) 
		{
			if(position == 0)
			{
				_optionsMenu = R.menu.main_activity_options_menu;
				MainActivity.this.invalidateOptionsMenu();
				return displayLocalNetworkFragment();
			}
			else
			{
				_optionsMenu = R.menu.schedules_list_fragment_options_menu;
				MainActivity.this.invalidateOptionsMenu();
				return displaySchedulesListFragment();
			}
		}
		
		
		private boolean displayLocalNetworkFragment()
		{
			LocalNetworkFragment devicesListFragment = getLocalNetworkFragment();
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, devicesListFragment);
			ft.commit();
			
			return true;  
		}
		
		private boolean displaySchedulesListFragment()
		{
			SchedulesListFragment fragment = getSchedulesListFragment();
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, fragment);
			ft.commit();
			
			return true;  
		}

		public LocalNetworkFragment getLocalNetworkFragment()
		{
			if(_localNetworkFragment == null)
			{
				_localNetworkFragment = (LocalNetworkFragment)SherlockFragment.instantiate(MainActivity.this, LocalNetworkFragment.class.getName()); 
				addScanStartListener(_localNetworkFragment);
			}
			
			return _localNetworkFragment;
		}
		
		public SchedulesListFragment getSchedulesListFragment()
		{
			if(_schedulesListFragment == null)
			{
				_schedulesListFragment = (SchedulesListFragment)SherlockFragment.instantiate(MainActivity.this, SchedulesListFragment.class.getName()); 
			}
			
			return _schedulesListFragment;
		}

	}

	
	
}
    





    
   








