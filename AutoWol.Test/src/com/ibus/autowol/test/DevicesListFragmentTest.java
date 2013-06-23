package com.ibus.autowol.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.Spinner;

import com.ibus.autowol.MainActivity;
import com.ibus.autowol.R;
import com.ibus.autowol.backend.Factory;
import com.ibus.autowol.backend.HostListAdapter;
import com.ibus.autowol.mock.MockNetwork;
import com.ibus.autowol.mock.MockNetworkScanner;

public class DevicesListFragmentTest extends ActivityInstrumentationTestCase2<MainActivity>
{
	/*MainActivity _activity;
	Spinner _netorkSpinner;
	ListView _deviceList;*/
	private final String TAG = "DevicesListFragmentTest";
	
	@SuppressWarnings("deprecation")
	public DevicesListFragmentTest()
	{
		super("com.ibus.autowol.MainActivity", MainActivity.class);
	}
	

	@Override
	protected void setUp() throws Exception 
	{
	    super.setUp();
	    setActivityInitialTouchMode(false);
	} 
	
	
	
	 
	public void testRecreateInNewNetwork() 
	{
		//clear db
		/*Database db = new Database(getInstrumentation().getTargetContext());
	    db.open();
	    db.deleteDB();
	    db.close();*/
	    
	    //start activity and do scan
	    /*MainActivity _activity = getActivity(); //starts the activity also
	    WaitForScan();
	    
	    //start new activity with a new mock network
		_activity.finish();*/

		MockNetwork mockNetwork = new MockNetwork();
		MockNetworkScanner mockNetworkScanner = new MockNetworkScanner();
		Factory.setNetwork(mockNetwork);
		Factory.setHostEnumerator(mockNetworkScanner);

		MainActivity _activity = getActivity(); //starts the activity also
		
	    Spinner _netorkSpinner = (Spinner) _activity.findViewById(R.id.host_fragment_networks);
	    ListView _deviceList = (ListView) _activity.findViewById(R.id.host_list);
	    
		WaitForScan();


		assertNotNull(_netorkSpinner);
		
		HostListAdapter adap = (HostListAdapter)_deviceList.getAdapter();
		assertTrue(_netorkSpinner.getAdapter().getCount() == 2);
		
		//NetworkSpinnerAdapter adap = (NetworkSpinnerAdapter)_deviceList.getAdapter();
		
		
		//assertTrue( adap.GetPositionForBssid(bssid));
	}
	 
	 
	 
	/*protected void tearDown ()
	{
		try {
			super.tearDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	/*public void testPreConditions() 
	{
		assertNotNull(_netorkSpinner);
		assertTrue(_netorkSpinner.getAdapter().getCount() == 1);
		assertTrue(_deviceList.getAdapter().getCount() > 0);
	} 
	*/
	 
	 private void WaitForScan()
	 {
		//give it time for the lists to populate after scan
		try 
		{
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 
	 
	//Factory.setNetwork(creditCardProcessor)
	 
	 
	/* public void testSpinnerUI() {

		    mActivity.runOnUiThread(
		      new Runnable() {
		        public void run() {
		          mSpinner.requestFocus();
		          mSpinner.setSelection(INITIAL_POSITION);
		        } // end of run() method definition
		      } // end of anonymous Runnable object instantiation
		    );
	 
	 
	  mPos = mSpinner.getSelectedItemPosition();
    mSelection = (String)mSpinner.getItemAtPosition(mPos);
    TextView resultView =
      (TextView) mActivity.findViewById(
        com.android.example.spinner.R.id.SpinnerResult
      );

    String resultText = (String) resultView.getText();

    assertEquals(resultText,mSelection);

	 
	*/
}











