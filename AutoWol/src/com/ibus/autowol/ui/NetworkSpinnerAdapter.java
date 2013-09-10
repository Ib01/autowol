package com.ibus.autowol.ui;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ibus.autowol.R;
import com.ibus.autowol.backend.Router;


public class NetworkSpinnerAdapter extends BaseAdapter implements SpinnerAdapter
{
	private List<Router> _items;
	private LayoutInflater _inflater;

	public NetworkSpinnerAdapter(List<Router> items, LayoutInflater inflater)
	{
		_items = items;
		_inflater = inflater;
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
		 return  getDropDownView(position, convertView, parent); 
	}
	
	//item displayed in the spinner dropdown when opened
	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) 
	{
		View view = convertView;
		
        if (view == null)
        {
        	view= _inflater.inflate(R.layout.network_list_item_layout, null); 
        }
        
        TextView n=(TextView)view.findViewById(R.id.network_list_item_name);
        n.setText(_items.get(position).getSsid());
        
        view.setTag(_items.get(position).getBssid());
 
        return view; 
    }
	
	public int GetPositionForMac(String deviceMac)
	{
		for(Router d : _items)
		{
			if(d.getMacAddress().equals(deviceMac))
				return _items.indexOf(d);
		}
		
		return -1;
	}
	
	public int GetPositionForBssid(String bssid)
	{
		for(Router d : _items)
		{
			if(d.getBssid().equals(bssid))
				return _items.indexOf(d);
		}
		
		return -1;
	}
	
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}
	
	public void Add(Router device)
	{
		_items.add(device);
	}
	
}













