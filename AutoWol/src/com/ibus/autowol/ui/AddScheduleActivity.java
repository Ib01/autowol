package com.ibus.autowol.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.ibus.autowol.R;

public class AddScheduleActivity extends Activity implements OnClickListener
{
	 @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule_activity);
        

		Button b = (Button) findViewById(R.id.add_schedule_activity_add);
		b.setOnClickListener((android.view.View.OnClickListener) this);
        
    }

	@Override
	public void onClick(View v) 
	{
		Intent newIntent = new Intent();
		newIntent.putExtra("Selected","testing");
		
		setResult(RESULT_OK, newIntent);
		finish();
		
	}
	 
	
	 	
}
