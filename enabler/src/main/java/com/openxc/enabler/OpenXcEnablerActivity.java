package com.openxc.enabler;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bugsnag.android.Bugsnag;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxcplatform.enabler.BuildConfig;
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

    private EnablerFragmentAdapter mAdapter;
    private ViewPager mPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String bugsnagToken = BuildConfig.BUGSNAG_TOKEN;
        if(bugsnagToken != null && !bugsnagToken.isEmpty()) {
            try {
                Bugsnag.init(this, bugsnagToken);
            } catch(NoClassDefFoundError e) {
                Log.w(TAG, "Busgnag is unsupported when building from Eclipse", e);
            }
        } else {
            Log.i(TAG, "No Bugsnag token found in AndroidManifest, not enabling Bugsnag");
        }

        Log.i(TAG, "OpenXC Enabler created");
        setContentView(R.layout.main);
        mAdapter = new EnablerFragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt("tab", 0));
        }

        startService(new Intent(this, VehicleManager.class));
        startService(new Intent(this, PreferenceManagerService.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putInt("tab", mPager.getCurrentItem());
        }catch (NoClassDefFoundError e){
            Log.w(TAG, "Failing to get current page ");
            e.printStackTrace();
        }
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

    public static class EnablerFragmentAdapter extends FragmentPagerAdapter {
        private static final String[] mTitles = { "Status", "Dashboard",
                "CAN", "Diagnostic", "DTC", "Send CAN", "Send Command"};

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
                return new DTCRequestFragment();
            } else if(position == 5) {
                return new SendCanMessageFragment();
            } else if (position == 6) {
                return new SendCommandMessageFragment();
            }

            // For position 0 or anything unrecognized, go to Status
            return new StatusFragment();
        }
    }

    //required to display DTC responses only in DTC screen
    private boolean dtcScanning = false;
    public boolean isDTCScanning() {
        return dtcScanning;
    }
    public void setDTCScanning(boolean set) {
        dtcScanning = set;
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            checkForCrashes();
        }catch (NoClassDefFoundError e){
            Log.w(TAG, "Failed checkForCrashes call");
            e.printStackTrace();
        }
    }

    private void checkForCrashes() {
        AppCenter.start(getApplication(), "f9cdd141-2cb9-4cb5-a756-8bdea1b20b4a",
                Analytics.class, Crashes.class);
    }
}
