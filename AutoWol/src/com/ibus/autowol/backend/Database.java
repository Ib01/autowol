package com.ibus.autowol.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class Database {
	
	  private SQLiteDatabase db;
	  private final Context context;
	  private myDbHelper dbHelper;
	  
	  private static final int DATABASE_VERSION = 9;
	  private static final String DATABASE_NAME = "AutoWol2.db";
	  	
	  //
	  // DEVICE TABLE
	  //
	  public static final String TABLE_DEVICE = "device";
	  public static final String COLUMN_DEVICE_ID="device_id";
	  public static final String COLUMN_DEVICE_ROUTER_ID="router_id";
	  public static final String COLUMN_DEVICE_NAME="name";
	  public static final String COLUMN_DEVICE_DISPLAY_NAME="display_name";
	  public static final String COLUMN_DEVICE_IP="ip";
	  public static final String COLUMN_DEVICE_MAC="mac";
	  public static final String COLUMN_DEVICE_NIC_VENDOR="nic_vendor";
	  
	  
	  
	  private static final String SQL_CREATE_TABLE_DEVICE = 
		  "create table " 
		  + TABLE_DEVICE 
		  + " (" 
		  + COLUMN_DEVICE_ID + " integer primary key autoincrement, " 
		  + COLUMN_DEVICE_ROUTER_ID +  " integer not null, "
		  + COLUMN_DEVICE_NAME + " text, "
		  + COLUMN_DEVICE_DISPLAY_NAME + " text, "
		  + COLUMN_DEVICE_IP + " text not null, "
		  + COLUMN_DEVICE_MAC + " text not null, "
		  + COLUMN_DEVICE_NIC_VENDOR +  " text"
		  + ");";
	  
	  private static final String SQL_DROP_TABLE_DEVICE = "DROP TABLE IF EXISTS " + TABLE_DEVICE;
	  
	  
	  public static final int ORDINAL_DEVICE_ID = 0;
	  public static final int ORDINAL_DEVICE_ROUTER_ID = 1;
	  public static final int ORDINAL_DEVICE_NAME = 2;
	  public static final int ORDINAL_DEVICE_DISPLAY_NAME = 3;
	  public static final int ORDINAL_DEVICE_IP = 4;
	  public static final int ORDINAL_DEVICE_MAC = 5;
	  public static final int ORDINAL_DEVICE_NIC_VENDOR = 6;
	  
	  
	  //
	  // ROUTER TABLE
	  //
	  public static final String TABLE_ROUTER = "router";
	  public static final String COLUMN_ROUTER_ID="router_id";
	  public static final String COLUMN_ROUTER_SSID="SSID"; //descriptive name of wifi hotspot
	  public static final String COLUMN_ROUTER_IP="ip";
	  public static final String COLUMN_ROUTER_MAC="mac";
	  public static final String COLUMN_ROUTER_BSSID="bssid";
	  public static final String COLUMN_ROUTER_NIC_VENDOR="nic_vendor";
	  
	  
	  private static final String SQL_CREATE_TABLE_ROUTER = 
		  "create table " 
		  + TABLE_ROUTER 
		  + " (" 
		  + COLUMN_ROUTER_ID + " integer primary key autoincrement, " 
		  + COLUMN_ROUTER_SSID + " text not null, "
		  + COLUMN_ROUTER_IP + " text, "
		  + COLUMN_ROUTER_MAC + " text, "
		  + COLUMN_ROUTER_BSSID + " text not null, "
		  + COLUMN_ROUTER_NIC_VENDOR +  " text"
		  + ");";
	  
	  private static final String SQL_DROP_TABLE_ROUTER = "DROP TABLE IF EXISTS " + TABLE_ROUTER;
	  
	  
	  public static final int ORDINAL_ROUTER_ID = 0;
	  public static final int ORDINAL_ROUTER_SSID = 1;
	  public static final int ORDINAL_ROUTER_IP = 2;
	  public static final int ORDINAL_ROUTER_MAC = 3;
	  public static final int ORDINAL_ROUTER_BSSID = 4;
	  public static final int ORDINAL_ROUTER_NIC_VENDOR = 5;
	  
	  
	  //
	  //TRIGGERS
	  //
	  public static final String TRIG_ROUTER_DELETE = "delete_"+ TABLE_ROUTER;
	  
	  private static final String SQL_CREATE_TRIGER_ROUTER_DELETE =
	  "CREATE TRIGGER ["+TRIG_ROUTER_DELETE+"]" +
	  " BEFORE DELETE" +
	  " ON [" + TABLE_ROUTER + "]" +
	  " FOR EACH ROW" +
	  " BEGIN" +
	  " DELETE FROM " + TABLE_DEVICE + " WHERE " + TABLE_DEVICE + "." + COLUMN_DEVICE_ROUTER_ID + " = old." + COLUMN_ROUTER_ID + ";" +
	  " END";
	  
	  private static final String SQL_DROP_TRIGER_ROUTER_DELETE = "DROP TRIGGER IF EXISTS " + TRIG_ROUTER_DELETE;
	  
	  
	  //
	  //Queries
	  //
	  private static final String SQL_GET_All_DEVICES = 
			  "select * from " + TABLE_DEVICE;
	  
	  private static final String SQL_GET_All_ROUTERS = 
			  "select * from " + TABLE_ROUTER;
	  
	  private static final String SQL_GET_ROUTER = 
			  "select * from " + TABLE_ROUTER + " where " + COLUMN_ROUTER_ID + "=?";
	  
	  private static final String SQL_GET_ROUTER_FOR_BSSID = 
			  "select * from " + TABLE_ROUTER + " where " + COLUMN_ROUTER_BSSID + "=?";
	  
	  
	  private static final String SQL_GET_DEVICE = 
			  "select * from " + TABLE_DEVICE + " where " + COLUMN_DEVICE_ID + "=?";
	  
	  private static final String SQL_GET_DEVICE_FOR_MAC = 
			  "select * from " + TABLE_DEVICE + " where " + COLUMN_DEVICE_MAC + "=?";
	  
	  private static final String SQL_GET_DEVICES_FOR_ROUTER = 
			  "select * from " + TABLE_DEVICE + " where " + COLUMN_DEVICE_ROUTER_ID + "=?";
	  
	  
	  //CONSTRUCTORS
	  
	  public Database(Context _context, int dbVersion) {
		  context = _context;
		  dbHelper = new myDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	  
	  public Database(Context _context) {
		  context = _context;
		  dbHelper = new myDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	  
	  public Database open() throws SQLException {
		  db = dbHelper.getWritableDatabase();
		  return this;
	  }
	  
	  
	  
	  //
	  // PUBLIC METHODS /////////////////////////////////////////////////////////
	  //
	  
	  public boolean isOpen() {
		  return db.isOpen();
	  }
		  
	  public void close() {
		  db.close();
	  }
	  
	  
	  public int addDevice(Device device, int routerId)
	  {
		  device.setRouterId(routerId);
		  ContentValues contentValues = getDeviceContentValues(device);
		  return (int)db.insert(TABLE_DEVICE, null, contentValues);
	  }
	  
	  
	  public int updateDevice(Device device)
	  {
		  ContentValues cv = getDeviceContentValues(device);
		  
		  String[] params = {device.getMacAddress()};  
		  return (int)db.update(TABLE_DEVICE, cv, COLUMN_DEVICE_MAC + "=?", params);
	  }
	  
	  
	  public int saveDevice(Device device, int routerPk)
	  {
		  Device d = getDeviceForMac(device.getMacAddress());
		  if(d == null)
		  {
			  return addDevice(device, routerPk);
		  }
		  else
		  {
			  device.setRouterId(routerPk);
			  return updateDevice(device);
		  }
	  }
	  
	  
	  public Device getDeviceForMac(String mac) 
	  {
		  String[] params = {mac};
		  Cursor cursor = db.rawQuery(SQL_GET_DEVICE_FOR_MAC, params);
		  
		  if(cursor.moveToFirst())
			  return cursorRowToDevice(cursor);
		  
		  return null;
	  }
	  
	  
	  public ArrayList<Device> getAllDevices() 
	  {
		  ArrayList<Device> devices = new ArrayList<Device>();
		 
		  Cursor cursor = db.rawQuery(SQL_GET_All_DEVICES, null);
		  devices = cursorToDevices(cursor);
		  cursor.close();
			
		  return devices;
	  }
	  
	  public List<Device> getDevicesForRouter(String routerBssid) 
	  {
		  Router r = getRouterForBssid(routerBssid);
		  return getDevicesForRouter(r.primaryKey);
	  }
	  
	  
	  public List<Device> getDevicesForRouter(int routerId) 
	  {
		  ArrayList<Device> devices = new ArrayList<Device>();
		  String[] params = {((Integer)routerId).toString()};
		  
		  Cursor cursor = db.rawQuery(SQL_GET_DEVICES_FOR_ROUTER, params);
		  devices = cursorToDevices(cursor);
		  cursor.close();
		  
		  return devices;
	  }
	  
	  public Device getDevice(int deviceId) 
	  {
		  String[] params = {((Integer)deviceId).toString()};
		  Cursor cursor = db.rawQuery(SQL_GET_DEVICE, params);
		  
		  if(cursor.moveToFirst())
			  return cursorRowToDevice(cursor);
		  
		  return null;
	  }
	 
	  
	  public int saveRouter(Router router)
	  {
		  Router r = getRouterForBssid(router.getBssid());
		  if(r == null)
		  {
			  return addRouter(router);
		  }
		  else
		  {
			  return updateRouter(router);
		  }
	  }
	  
	  public int addRouter(Router router)
	  {
		  ContentValues cv = getRouterContentValues(router);;
		  return (int)db.insert(TABLE_ROUTER, null, cv);
	  }
	  
	  public int updateRouter(Router router)
	  {
		  ContentValues cv = getRouterContentValues(router);
		  
		  String[] params = {router.getBssid()};  
		  return (int)db.update(TABLE_ROUTER, cv, COLUMN_ROUTER_BSSID + "=?", params);
	  }
	  
	  
	  public ArrayList<Router> getAllRouters() 
	  {
		  
		  ArrayList<Router> pal = new ArrayList<Router>();
		  Cursor cursor = db.rawQuery(SQL_GET_All_ROUTERS, null);
		  
		  if(cursor.moveToFirst())
		  {
			do {
				
				Router r = cursorRowToRouter(cursor);
				pal.add(r);
		  	} 
			while(cursor.moveToNext());
			  
		  }
		  cursor.close();
			
		  return pal;
	  }
	  
	  
	  public int deleteRouter(int routerId) 
	  {
		  
		  String[] params = {((Integer)routerId).toString()};
		  
		  //delete operation useses cascade delete triggers so we don't have 
		  //to do anything other than delete the router. this will also delete 
		  //any related actions and devices
		  return db.delete(TABLE_ROUTER, COLUMN_ROUTER_ID + "=?" , params);
	  }
	 
	  
	  public Router getRouter(int routerId) 
	  {
		  String[] params = {((Integer)routerId).toString()};
		  Cursor cursor = db.rawQuery(SQL_GET_ROUTER, params);
		  
		  if(cursor.moveToFirst())
			  return cursorRowToRouter(cursor);
		  
		  return null;
	  }
	  
	  public Router getRouterForBssid(String bssid) 
	  {
		  String[] params = {bssid};

		  Cursor cursor = db.rawQuery(SQL_GET_ROUTER_FOR_BSSID, params);
		  if(cursor.moveToFirst())
			  return cursorRowToRouter(cursor);
		  
		  return null;
	  }
	  
	  public int deleteDevicesForRouter(int routerId) 
	  {
		  String[] params = {((Integer)routerId).toString()};
		  
		  //delete operation useses cascade delete triggers so we don't have 
		  //to do anything other than delete the location. this will also delete 
		  //any attached actions and plugins
		  return db.delete(TABLE_DEVICE, COLUMN_DEVICE_ROUTER_ID + "=?" , params);
	  }

	  
	  public void saveDevicesForRouter(List<Device> devices, int routerId) 
	  {
		  for(Device d : devices)
		  {
			  saveDevice(d, routerId);
		  }
	  }
	  
	  
	  
	  //utilities /////////////////////////////////////////////////////////////
	  

	  
	  private ArrayList<Device> cursorToDevices(Cursor cursor)
	  {
		  ArrayList<Device> pal = new ArrayList<Device>();
		  
		  if(cursor.moveToFirst())
		  {
			do {
				Device h = cursorRowToDevice(cursor);
				pal.add(h);
		  	} 
			while(cursor.moveToNext());
			  
		  }
		  cursor.close();
			
		  return pal;
	  }
	  
	  private Device cursorRowToDevice(Cursor cursor)
	  {
			Device h = new Device();
			h.setPrimaryKey(cursor.getInt(ORDINAL_DEVICE_ID));
			h.setRouterId(cursor.getInt(ORDINAL_DEVICE_ROUTER_ID));
			h.setName(cursor.getString(ORDINAL_DEVICE_NAME));
			h.setDisplayName(cursor.getString(ORDINAL_DEVICE_DISPLAY_NAME));
			h.setIpAddress(cursor.getString(ORDINAL_DEVICE_IP));
			h.setMacAddress(cursor.getString(ORDINAL_DEVICE_MAC));
			h.setNicVendor(cursor.getString(ORDINAL_DEVICE_NIC_VENDOR));
			
			return  h;
	  }
	  

	  private Router cursorRowToRouter(Cursor cursor) 
	  {
		  	Router h = new Router();
			h.setPrimaryKey(cursor.getInt(ORDINAL_ROUTER_ID));
			h.setSsid(cursor.getString(ORDINAL_ROUTER_SSID));
			h.setIpAddress(cursor.getString(ORDINAL_ROUTER_IP));
			h.setMacAddress(cursor.getString(ORDINAL_ROUTER_MAC));
			h.setBssid(cursor.getString(ORDINAL_ROUTER_BSSID));
			h.setNicVendor(cursor.getString(ORDINAL_ROUTER_NIC_VENDOR));
			
			return h;
	  }
	  
	  
	  private ContentValues getRouterContentValues(Router router)
	  {
		  ContentValues contentValues = new ContentValues();
		  
		  contentValues.put(COLUMN_ROUTER_SSID, router.getSsid());
		  contentValues.put(COLUMN_ROUTER_IP, router.getIpAddress());
		  contentValues.put(COLUMN_ROUTER_MAC, router.getMacAddress());
		  contentValues.put(COLUMN_ROUTER_BSSID, router.getBssid());
		  contentValues.put(COLUMN_ROUTER_NIC_VENDOR, router.getNicVendor());
		  
		  return contentValues;
	  }
	  

	  
	  public ContentValues getDeviceContentValues(Device device)
	  {
		  ContentValues contentValues = new ContentValues();
		  
		  contentValues.put(COLUMN_DEVICE_ROUTER_ID, device.getRouterId());
		  contentValues.put(COLUMN_DEVICE_NAME, device.getName());
		  contentValues.put(COLUMN_DEVICE_DISPLAY_NAME, device.getDisplayName());
		  contentValues.put(COLUMN_DEVICE_IP, device.getIpAddress());
		  contentValues.put(COLUMN_DEVICE_MAC, device.getMacAddress());
		  contentValues.put(COLUMN_DEVICE_NIC_VENDOR, device.getNicVendor());
		  
		  return contentValues;
	  }
	  
	  
	// Helper /////////////////////////////////////////////////////////////
	  
	  
	//just for testing
	  public void deleteDB() {
		  
		  //returns rows effected
		  db.delete(TABLE_DEVICE, null, null);
		  db.delete(TABLE_ROUTER, null, null);
	  }
	  
	  
	  
	  private static class myDbHelper extends SQLiteOpenHelper {
		  
		  	public myDbHelper(Context context, String name,
	                          CursorFactory factory, int version) {
	
		  		super(context, name, factory, version);
		    }
		  	
		    // Called when no database exists in
		    // disk and the helper class needs
		    // to create a new one.
		    @Override
		    public void onCreate(SQLiteDatabase _db) 
		    {
		    	_db.execSQL(SQL_CREATE_TABLE_DEVICE);
		    	_db.execSQL(SQL_CREATE_TABLE_ROUTER);
		    	
				//create cascade delete triggers
				_db.execSQL(SQL_CREATE_TRIGER_ROUTER_DELETE);
		    }
		    
		    
		    
		    // Called when there is a database version mismatch meaning that
		    // the version of the database on disk needs to be upgraded to
		    // the current version.
		    @Override
		    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) 
		    {
		      
		    	_db.execSQL(SQL_DROP_TABLE_DEVICE);
		    	_db.execSQL(SQL_DROP_TABLE_ROUTER);
		    	
				//drop cascade delete triggers
				_db.execSQL(SQL_DROP_TRIGER_ROUTER_DELETE);
				  
				// Create a new db.
				onCreate(_db);
		    }
		    
	  }
	  
	  
} 
	  
