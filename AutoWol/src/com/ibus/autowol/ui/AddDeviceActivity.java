package com.ibus.autowol.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.R;
import com.ibus.autowol.backend.Database;
import com.ibus.autowol.backend.Device;
import com.ibus.autowol.backend.Factory;
import com.ibus.autowol.backend.INetwork;
import com.ibus.autowol.backend.Router;

public class AddDeviceActivity extends SherlockActivity 
{
	INetwork _network;
	int devicePk = -1;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device_activity_layout);

        _network = Factory.getNetwork(this);
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		
		Bundle bundle=this.getIntent().getExtras();
		
		if(bundle != null && bundle.containsKey("DeviceId")){
			devicePk=bundle.getInt("DeviceId");
			Device d = getDevice(devicePk);
			populateForm(d);
		}
		
		
		
		EditText ip = (EditText) findViewById(R.id.add_device_activity_ip_address);
		ip.setFilters(new InputFilter[] {new IpAddressTextFilter()});
    }

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getSupportMenuInflater().inflate(R.menu.dialog_activity_options_menu, menu);    	
        return true;   
    }
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
		
        switch (item.getItemId()) 
        { 
            case R.id.dialog_activity_add:
            	if(formIsValid())
            	{
	            	int pk = SaveDevice();
	            	Intent newIntent= new Intent();
	        		newIntent.putExtra("DeviceId", pk);
	        		setResult(RESULT_OK, newIntent);
	        		finish();
            	}
            	break;
            default:
        		setResult(RESULT_CANCELED);
        		finish();
            	break;
        }
        
        return true;
    }    
	
	
	public boolean formIsValid()
	{
		EditText name = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ip = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText mac = (EditText) findViewById(R.id.add_device_activity_mac_address);
		
		name.setError(null);
		if(name.getText().length() == 0)
			name.setError("Host name cannot be left empty");
		
		ip.setError(null);
		if(ip.getText().length() == 0)
			ip.setError("Ip Address cannot be left empty");
		
		mac.setError(null);
		if(mac.getText().length() == 0)
			mac.setError("Mac Address cannot be left empty");
		
		
		
		boolean ret = (!name.getText().toString().trim().equals("") &&
				!ip.getText().toString().trim().equals("") &&
				!mac.getText().toString().trim().equals(""));
		
		return ret;
	}
	
	
	
	 //TODO: LAYOUT NEEDS SORTING OUT
	private int SaveDevice()
	{
		Database database = new Database(this);
		database.open();
		
		EditText name = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ip = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText mac = (EditText) findViewById(R.id.add_device_activity_mac_address);
		
		int pk;
		Device d = new Device();
		if(devicePk != -1)
		{
			d = database.getDevice(devicePk);
			
			d.setName(name.getText().toString().trim());
			d.setIpAddress(ip.getText().toString().trim());
			d.setMacAddress(mac.getText().toString().trim());
			
			database.updateDevice(d);
			pk = d.getPrimaryKey();
		}
		else
		{
			d.setName(name.getText().toString().trim());
			d.setIpAddress(ip.getText().toString().trim());
			d.setMacAddress(mac.getText().toString().trim());
			
			Router router = _network.getRouter();
			pk = database.addDevice(d, router.getBssid());
		}
		
		database.close();
		return pk;
	}
	
	
	public Device getDevice(int devicePk)
	{
		Database database = new Database(this);
		database.open();
		Device d = database.getDevice(devicePk);
		database.close();
		
		return d;
	}
	
	public void populateForm(Device device)
	{
		EditText name = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ip = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText mac = (EditText) findViewById(R.id.add_device_activity_mac_address);
		
		name.setText(device.getName());
		ip.setText(device.getIpAddress());
		mac.setText(device.getMacAddress());
	}
	
	 	
}








