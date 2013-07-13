package com.ibus.autowol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.R;

public class AddDeviceActivity extends SherlockActivity implements OnClickListener
{
	 @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device_activity);

    }

	@Override
	public void onClick(View v) 
	{
		
		
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
		Intent newIntent;
        switch (item.getItemId()) 
        { 
            case R.id.dialog_activity_add:
            	//do save 
            	newIntent = new Intent();
        		newIntent.putExtra("Selected","testing");
        		setResult(RESULT_OK, newIntent);
        		finish();
            	break;
            case R.id.dialog_activity_cancel:
        		setResult(RESULT_CANCELED);
        		finish();
            	break;
        }
        
        return true;
    }    
	
	 	
}
