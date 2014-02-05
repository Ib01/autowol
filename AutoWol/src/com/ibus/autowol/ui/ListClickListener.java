package com.ibus.autowol.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ibus.autowol.R;


/*
 * base class for managing callbacks for list items and action bar buttons and for 
 * managing visual state of selected items. overide to change the implementation of the 
 * context menu
 */
public abstract class ListClickListener implements AdapterView.OnItemClickListener 
{
	int _selectionContextMenu;
	HashSet<View> _selectedItems = new HashSet<View>();
	ActionMode _actionMode;
	SherlockFragmentActivity _activity;
	private boolean _isEnabled = true;
	
	public boolean isEnabled() {
		return _isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		_isEnabled = isEnabled;
	}


	public ListClickListener(SherlockFragmentActivity activity, int selectionContextMenu)
	{
		_activity = activity;
		_selectionContextMenu = selectionContextMenu;
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) 
	{
		if(!_isEnabled)
			return;
		
		LinearLayout border = (LinearLayout)view.findViewById(R.id.list_item_border);
		
		if(_selectedItems.contains(view))
		{
			border.setBackgroundResource(R.drawable.card);
			_selectedItems.remove(view);
		}
		else
		{
			border.setBackgroundResource(R.drawable.card_selected);
			_selectedItems.add(view);
		}
		
		if(_selectedItems.size() <= 0)
		{
			_actionMode.finish();
		}
		else 
			if(_actionMode == null)
				_actionMode = _activity.startActionMode(new ActionModeCallback());
	}
	
	
	private class ActionModeCallback implements ActionMode.Callback
	{
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) 
		{
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(_selectionContextMenu, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
		{
			return actionItemClicked(mode, item);
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) 
		{
			clearSelectedItems();
			_actionMode = null;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) 
		{
			return false;
		}
		
		public void clearSelectedItems()
		{
			for (View item : _selectedItems) 
			{
				LinearLayout border = (LinearLayout)item.findViewById(R.id.list_item_border);
				border.setBackgroundResource(R.drawable.card);
			}
			
			_selectedItems.clear();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getSelectedItems() 
	{
		List<View> vl = new ArrayList<View>(_selectedItems);
		List<T> ol = new ArrayList<T>();
		
		for(View v : vl){
			ol.add((T)v.getTag());
		}
		
		return ol;
	}
	
	
	private void setBackground(View v, int color)
	{
		GradientDrawable drawable = (GradientDrawable) v.getBackground();
		drawable.setColor(color);
	}
	
	
	public abstract boolean actionItemClicked(ActionMode mode, MenuItem item);
	

}





