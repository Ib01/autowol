package com.ibus.autowol.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

import com.ibus.autowol.backend.Database;
import com.ibus.autowol.backend.Device;
import com.ibus.autowol.backend.Router;

public class DatabaseTests extends AndroidTestCase {

	Database db;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		db = new Database(getContext());
		db.open();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		db.deleteDB();
		db.close();
	}

	
	public void testSaveDevice()
	{
		Device host = GetRandomDevice();
		db.addDevice(host, 1);
		List<Device> hl = db.getAllDevices();
        
		assertTrue(hl.size() > 0);
	}
	
	
	
	public void testSaveRouter()
	{
		Router r = GetRandomRouter();
		db.addRouter(r);
		List<Router> hl = db.getAllRouters();
        
		assertTrue(hl.size() > 0);
	}
	
	public void testGetDevicesForRouter()
	{
		Router r = GetRandomRouter();
		Device d1 = GetRandomDevice();
		Device d2 = GetRandomDevice();
		
		int pk = db.addRouter(r);
		
		db.addDevice(d1, pk);
		db.addDevice(d2, pk);
		
		List<Device> dl = db.getDevicesForRouter(pk);
		
		assertTrue(dl.size() == 2);
	}
	
	
	public void testRouterCascadeDelete()
	{
		Router r = GetRandomRouter();
		Device d1 = GetRandomDevice();
		Device d2 = GetRandomDevice();
		
		int rpk = db.addRouter(r);
		int d1pk = db.addDevice(d1, rpk);
		int d2pk = db.addDevice(d2, rpk);
		
		List<Device> dl = db.getDevicesForRouter(rpk);
		
		assertTrue(dl.size() == 2);
		
		db.deleteRouter(rpk);
		
		Router r2 = db.getRouter(rpk);
		Device d3 = db.getDevice(d1pk);
		Device d4 = db.getDevice(d2pk);
		
		assertTrue(r2 == null);
		assertTrue(d3 == null);
		assertTrue(d4 == null);
	}
	
	public void testGetCursorForMac()
	{
		Router r1 = GetRandomRouter();
		r1.setMacAddress("1 1");
		
		db.addRouter(r1);
		
		Router r2 = db.getRouterForBssid("1 1");
		assertTrue(r2 != null);
		
		Router r3 = db.getRouterForBssid("0");
		assertTrue(r3 == null);
		
	}
	
	
	public void testDeleteDevicesForRoputer()
	{
		Router r = GetRandomRouter();
		Device d1 = GetRandomDevice();
		Device d2 = GetRandomDevice();
		
		int rpk = db.addRouter(r);
		int d1pk = db.addDevice(d1, rpk);
		int d2pk = db.addDevice(d2, rpk);
		
		Router r2 = db.getRouter(rpk);
		Device d3 = db.getDevice(d1pk);
		Device d4 = db.getDevice(d2pk);
		
		assertTrue(r2 != null);
		assertTrue(d3 != null);
		assertTrue(d4 != null);
		
		db.deleteDevicesForRouter(rpk);
		
		Device d5 = db.getDevice(d1pk);
		Device d6 = db.getDevice(d2pk);
		
		assertTrue(d5 == null);
		assertTrue(d6 == null);
	
	}
	
	public void testSaveDevicesForRoputer()
	{
		List<Device> devl= new ArrayList<Device>();
		
		Router r = GetRandomRouter();
		devl.add(GetRandomDevice());
		devl.add(GetRandomDevice());
		
		int rpk = db.addRouter(r);
		db.saveDevicesForRouter(devl, rpk);
		
		List<Device> devl2 = db.getDevicesForRouter(rpk);
		
		assertTrue(devl2.size() == 2);
	}
	
	
	private Router GetRandomRouter()
	{
		Router r = new Router();
        r.setName("name");
        r.setDisplayName("displayName");
        r.setIpAddress("12.12.12.12");
        r.setMacAddress("00:00:00:00:00:00");
        r.setNicVendor("nicName");
        r.setBssid("00:00:00:00:00:xx");
        r.setSsid("Ssid");
        
        return r;
	}
	
	private Device GetRandomDevice()
	{
		Device host = new Device();
		host.setName("name");
		host.setDisplayName("displayName");
		host.setIpAddress("12.12.12.12");
		host.setMacAddress("00:00:00:00:00:00");
		host.setNicVendor("nicName");
		
		return host;
	}
	
	
}





