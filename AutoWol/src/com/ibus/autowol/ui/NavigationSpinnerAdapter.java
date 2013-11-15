package com.ibus.autowol.ui;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ibus.autowol.R;


public class NavigationSpinnerAdapter extends BaseAdapter implements SpinnerAdapter
{
	private List<ActivityListItem> _items;
	private SherlockFragmentActivity _activity;
	
	public NavigationSpinnerAdapter(List<ActivityListItem> items, SherlockFragmentActivity activity)
	{
		_items = items;
		_activity = activity;
	}
	
	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public java.lang.Object getItem(int position) {
		return _items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	//item displayed on the action bar
	@Override
	public android.view.View getView(int position, View convertView, ViewGroup parent) 
	{
		View view = convertView;
		
        if (view == null)
        {
        	view= _activity.getLayoutInflater().inflate(R.layout.navigation_list_item_layout, null); 
        }
        
        TextView tv=(TextView)view.findViewById(R.id.navigation_list_item_description);
        tv.setText(_items.get(position).Title);
 
        return view; 
	}
	
	//item displayed in the spinner dropdown when opened
	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) 
	{
		View view = convertView;
		
        if (view == null)
        {
        	view= _activity.getLayoutInflater().inflate(R.layout.navigation_open_list_item_layout, null); 
        }
        
        TextView tv=(TextView)view.findViewById(R.id.navigation_list_item_description);
        tv.setText(_items.get(position).Title);
 
        return view; 
    }
}













