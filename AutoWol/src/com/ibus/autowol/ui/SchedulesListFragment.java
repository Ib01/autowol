package com.ibus.autowol.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ibus.autowol.R;

public class SchedulesListFragment extends SherlockFragment 
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.schedules_list_fragment_layout, container, false);
		
        return v;
	}
	
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
	}
	
	
	@Override
	public void onResume() 
	{
		//fragment is visible here
		super.onResume();
		
	}
	
	
	public void addSchedule(String scheduleId)
	{
	 
	}

	
	
}
