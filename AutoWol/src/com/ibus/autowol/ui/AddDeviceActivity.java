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
import com.ibus.autowol.backend.IpAddressUtil;
import com.ibus.autowol.backend.MacAddressUtil;
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
            	TrimFields();
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
		EditText nameInput = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ipInput = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText macInput = (EditText) findViewById(R.id.add_device_activity_mac_address);
		String name = nameInput.getText().toString();
		String ip = ipInput.getText().toString();
		String mac = macInput.getText().toString();
		
		boolean isValid = true;
		nameInput.setError(null);
		ipInput.setError(null);
		macInput.setError(null);
		
		if(name.length() == 0){
			nameInput.setError("Please enter a Host Name");
			isValid = false;
		}
		
		if(ip.length() == 0){
			ipInput.setError("Please enter an Ip Address");
			isValid = false;
		}
		else if(!IpAddressUtil.isValidIp(ip)){
			ipInput.setError("The Ip address you entered is not a valid IP address");
			isValid = false;
		}
		
		if(mac.length() == 0){
			macInput.setError("Please enter a Mac Address");
			isValid = false;
		}
		else if(!MacAddressUtil.isValidMac(mac)){
			macInput.setError("The Mac address you entered is not a valid Mac address");
			isValid = false;
		}
		
		return isValid;
	}
	
	
	private void TrimFields()
	{
		EditText nameInput = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ipInput = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText macInput = (EditText) findViewById(R.id.add_device_activity_mac_address);
		
		String name = nameInput.getText().toString().trim();
		nameInput.setText(name);
		String ip = ipInput.getText().toString().trim();
		ipInput.setText(ip);
		String mac = macInput.getText().toString().trim();
		macInput.setText(mac);
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








