package com.ibus.autowol.ui;

public class ActivityListItem
{
	public ActivityListItem(){};
	public ActivityListItem(String title, String iconId)
	{
		this.Title = title;
		this.IconId = iconId;
	}
	
	public String Title;
	public String IconId;
}