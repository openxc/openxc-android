package com.openxc.enabler;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import com.bugsnag.android.Bugsnag;
import com.openxcplatform.enabler.BuildConfig;
import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.interfaces.bluetooth.BluetoothModemVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothV2XVehicleInterface;
import com.openxcplatform.enabler.R;

/** The OpenXC Enabler app is primarily for convenience, but it also increases
 * the reliability of OpenXC by handling background tasks on behalf of client
 * applications.
 *
 * The Enabler provides a common location to control which data sources and
 * sinks are active, e.g. if the a trace file should be played back or recorded.
 * It's preferable to be able to change the data source on the fly, and not have
 * to programmatically load a trace file in any application under test.
 *
 * With the Enabler installed, the {@link com.openxc.remote.VehicleService} is
 * also started automatically when the Android device boots up. A simple data
 * sink like a trace file uploader can start immediately without any user
 * interaction.
 *
 * As a developer, you can also appreciate that because the Enabler takes care
 * of starting the {@link com.openxc.remote.VehicleService}, you don't need to
 * add much to your application's AndroidManifest.xml - just the
 * {@link com.openxc.VehicleManager} service.
*/
public class OpenXcEnablerActivity extends FragmentActivity {
    private static String TAG = "OpenXcEnablerActivity";

    // private EnablerFragmentAdapter mAdapter;
    private ViewPager mPager;
    TabsAdapter mTabsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String bugsnagToken = BuildConfig.BUGSNAG_TOKEN;
        if(bugsnagToken != null && !bugsnagToken.isEmpty()) {
            try {
                Bugsnag.register(this, bugsnagToken);
            } catch(NoClassDefFoundError e) {
                Log.w(TAG, "Busgnag is unsupported when building from Eclipse", e);
            }
        } else {
            Log.i(TAG, "No Bugsnag token found in AndroidManifest, not enabling Bugsnag");
        }

        Log.i(TAG, "OpenXC Enabler created");
        setContentView(R.layout.main);
        //  mAdapter = new EnablerFragmentAdapter(getSupportFragmentManager());
        final ActionBar bar = getActionBar();
	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
	mPager = (ViewPager) findViewById(R.id.pager);
        // mPager.setAdapter(mAdapter);

	mTabsAdapter = new TabsAdapter(this, mPager);
	        
	mTabsAdapter.addTab(bar.newTab().setText("Status"),
	    		StatusFragment.class, null);
	mTabsAdapter.addTab(bar.newTab().setText("Dashboard"),
	      		VehicleDashboardFragment.class, null);
	mTabsAdapter.addTab(bar.newTab().setText("CAN"),
	      		CanMessageViewFragment.class, null );
	mTabsAdapter.addTab(bar.newTab().setText("Diagnostic"),
	      		DiagnosticRequestFragment.class, null);
					        
	mPager.setAdapter(mTabsAdapter);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt("tab", 0));
        }

        startService(new Intent(this, VehicleManager.class));
        startService(new Intent(this, PreferenceManagerService.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", mPager.getCurrentItem());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


// Existing Implementation replaced for the sake of dynamic GUI changes -tab swaps
/*
    public static class EnablerFragmentAdapter extends FragmentPagerAdapter {
        private static final String[] mTitles = { "Status", "Dashboard",
            "CAN", "Diagnostic", "Send CAN" };

        public EnablerFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 1) {
                return new VehicleDashboardFragment();
            } else if(position == 2) {
                return new CanMessageViewFragment();
            } else if(position == 3) {
                return new DiagnosticRequestFragment();
            } else if(position == 4) {
                return new SendCanMessageFragment();
            }

            // For position 0 or anything unrecognized, go to Status
            return new StatusFragment();
        }
    }
*/
    

   public static class TabsAdapter extends FragmentPagerAdapter
   implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
       	private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

	    TabInfo(Class<?> _class, Bundle _args) {
	         clss = _class;
	         args = _args;
	    }
	}
        public TabsAdapter(OpenXcEnablerActivity openXcEnablerActivity, ViewPager mPager) {
	    super(openXcEnablerActivity.getSupportFragmentManager());
	    mContext = openXcEnablerActivity;
	    mActionBar = openXcEnablerActivity.getActionBar();
	    mViewPager = mPager;
	    mViewPager.setAdapter(this);
	    mViewPager.setOnPageChangeListener(this);
	}

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
	    TabInfo info = new TabInfo(clss, args);
	    tab.setTag(info);
	    tab.setTabListener(this);
	    mTabs.add(info);
	    mActionBar.addTab(tab);
	    notifyDataSetChanged();
	}

        @Override
	public int getCount() {
	    return mTabs.size();
	}

	@Override
	public Fragment getItem(int position) {
	    TabInfo info = mTabs.get(position);
	    return Fragment.instantiate(mContext, info.clss.getName(), info.args);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

        @Override
        public void onPageSelected(int position) {
	    mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction arg1) {
	    Object tag = tab.getTag();
			
	    for (int i=0; i<mTabs.size(); i++) {
	         if (mTabs.get(i) == tag) {
	             mViewPager.setCurrentItem(i);
	         }
	    }
								    			    
	    if(RegisterDevice.getDevice()!=null && RegisterDevice.getDevice().startsWith(BluetoothV2XVehicleInterface.DEVICE_NAME_PREFIX))
	     { 
	      	if(mActionBar!=null && getCount()==4)
		            		
		 {
	   		addTab(mActionBar.newTab().setText("V2X Diag"),
																										        V2XDiagnosticsFragment.class, null);
																								            		addTab(mActionBar.newTab().setText("Send CAN"),
																							                    		SendCanMessageFragment.class, null);

		 }		
		else if(mActionBar!=null && getCount()==5)
	 	{
																								            		addTab(mActionBar.newTab().setText("V2X Diag"),
																						    				        V2XDiagnosticsFragment.class, null);
																								            	}
																								            	
	
	     }
	 
	    else if(RegisterDevice.getDevice()!=null && RegisterDevice.getDevice().startsWith(BluetoothModemVehicleInterface.DEVICE_NAME_PREFIX))
	
	    {
	
		    if(mActionBar!=null && getCount()==4)
		
		    {
		
			    addTab(mActionBar.newTab().setText("Modem Diag"),
			
		            ModemDiagnosticsFragment.class, null);
		     	    addTab(mActionBar.newTab().setText("Send CAN"),
			
		            SendCanMessageFragment.class, null);

		    }
		
		    else if(mActionBar!=null && getCount()==5)
																									            	{
																												addTab(mActionBar.newTab().setText("Modem Diag"),
																												ModemDiagnosticsFragment.class, null);
																											}
																								            }
	    else{
	
		    if(mActionBar!=null && getCount()==4 )
		    {
		     	addTab(mActionBar.newTab().setText("Send CAN"),
			SendCanMessageFragment.class, null);
			
		    }
		
	    }
	
	}


	@Override
	public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {

	}

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }


        @Override
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
            // TODO Auto-generated method stub

        }

    static String getBugsnagToken(Context context) {
        String key = null;
        try {
            Context appContext = context.getApplicationContext();
            ApplicationInfo appInfo = appContext.getPackageManager().getApplicationInfo(
                    appContext.getPackageName(), PackageManager.GET_META_DATA);
            if(appInfo.metaData != null) {
                key = appInfo.metaData.getString("com.bugsnag.token");
            }
        } catch (NameNotFoundException e) {
            // Should not happen since the name was determined dynamically from the app context.
            Log.e(TAG, "Unexpected NameNotFound.", e);
        }
        return key;
    }
}
