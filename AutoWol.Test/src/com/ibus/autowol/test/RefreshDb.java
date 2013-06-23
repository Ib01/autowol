package com.ibus.autowol.test;

import android.test.AndroidTestCase;

import com.ibus.autowol.backend.Database;

public class RefreshDb extends AndroidTestCase {

	Database db;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		db = new Database(getContext());
		db.open();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		db.close();
	}

	
	public void testDeleteDb()
	{
		db.deleteDB();
		assertTrue(true);
	}
	
	
	
}





