package com.ibus.autowol.ui;

import android.content.Intent;
import android.os.Bundle;
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
	
	 @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device_activity);

        _network = Factory.getNetwork(this);
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
    }

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getSupportMenuInflater().inflate(R.menu.dialog_activity_menu, menu);    	
        return true;   
    }
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
		
        switch (item.getItemId()) 
        { 
            case R.id.dialog_activity_add:
            	int pk = SaveDevice();
            	Intent newIntent= new Intent();
        		newIntent.putExtra("DeviceId", pk);
        		setResult(RESULT_OK, newIntent);
        		finish();
            	break;
            default:
        		setResult(RESULT_CANCELED);
        		finish();
            	break;
        }
        
        return true;
    }    
	
	
	
	private int SaveDevice()
	{
		Database database = new Database(this);
		database.open();
		
		EditText name = (EditText) findViewById(R.id.add_device_activity_host_name);
		EditText ip = (EditText) findViewById(R.id.add_device_activity_ip_address);
		EditText mac = (EditText) findViewById(R.id.add_device_activity_mac_address);
		
		Device d = new Device();
		d.setName(name.getText().toString().trim());
		d.setIpAddress(ip.getText().toString().trim());
		d.setMacAddress(mac.getText().toString().trim());
		
		Router router = _network.getRouter();
		
		int pk = database.addDevice(d, router.getBssid());
		
		database.close();
		return pk;
	}
	 	
}








