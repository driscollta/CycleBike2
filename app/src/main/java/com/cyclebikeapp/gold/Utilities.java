package com.cyclebikeapp.gold;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;

import static com.cyclebikeapp.gold.Constants.NAG_TYPE;
import static com.cyclebikeapp.gold.Constants.NUM_LAUNCHES;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_LATITUDE;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_LONGITUDE;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_TIME;
import static com.cyclebikeapp.gold.Constants.PREFS_NAME;
import static com.cyclebikeapp.gold.Constants.PREF_SAVED_LOC_TIME;
import static com.cyclebikeapp.gold.Constants.SAVED_LAT;
import static com.cyclebikeapp.gold.Constants.SAVED_LON;


/**
 * Created by TommyD on 4/18/2016.
 *
 */
class Utilities {

    static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean hasInternetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && hasInternetPermission;
    }

    static boolean hasWifiInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean hasInternetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && hasInternetPermission
                && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    static boolean isGPSLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (SecurityException ignored) {
        }
        return gps_enabled;
    }
    static boolean hasGPSPermission (Context context) {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    static boolean isNetworkLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = false;
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException ignored) {
        }
        return network_enabled;
    }
    static boolean areLocationServicesAvailable(Context context) {
        return isGPSLocationEnabled(context);
    }
    /* Checks if external storage is available for read and write */
    static boolean isExternalStorageWritable(Context context) {
        boolean hasWritePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return isExternalStorageReadable() && hasWritePermission;
    }

    /* Checks if external storage is available for read and write */
    static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
     static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    static void saveLocSharedPrefs(Location location, Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SAVED_LAT, Double.toString(location.getLatitude()));
        editor.putString(SAVED_LON, Double.toString(location.getLongitude()));
        editor.putLong(PREF_SAVED_LOC_TIME, location.getTime()).apply();
    }
    static Location getLocFromSharedPrefs(Context context) {
        Location aLoc = new Location(LocationManager.NETWORK_PROVIDER);
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        aLoc.setLongitude(Double.parseDouble(settings.getString(SAVED_LON, PREFS_DEFAULT_LONGITUDE)));
        aLoc.setLatitude(Double.parseDouble(settings.getString(SAVED_LAT, PREFS_DEFAULT_LATITUDE)));
        aLoc.setAltitude(0);
        // this is just temporary until we get a location from LocationHelper
        aLoc.setTime(settings.getLong(PREF_SAVED_LOC_TIME, PREFS_DEFAULT_TIME));
        return aLoc;
    }
    static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    static boolean hasStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private static final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Android/data/com.cyclebikeapp.plus/files/";

    static boolean isFileSpaceAvailable(int mBRequired) {
        File temp = new File(pathName);
        long usuableSpaceMB = temp.getUsableSpace() / 1000000;
        //Log.w(APP_NAME, "External storage space usuable (MB): " + usuableSpaceMB);
        return mBRequired < usuableSpaceMB;
    }

    /**
     * read the tcx file reset value from preferences
     */
    static long getTCXFileAutoReset(Context context) {
        int[] resetTimes = {2, 4, 6, 8};
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String theString = sharedPref.getString(
                context.getResources().getString(R.string.pref_tcx_idle_time_key), "3");
        Integer tcxFileResetTimeIndex = Integer.parseInt(theString);
        return resetTimes[tcxFileResetTimeIndex];
    }
    /**
     * Gets the state of Airplane Mode.
     *
     * @param context application context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    @SuppressWarnings("deprecation")
    private static String readAirplaneModeToggleableList(Context context) {
        String mList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mList = Settings.Global.getString(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_RADIOS);
        } else {
            mList = Settings.System.getString(context.getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS);
        }
        //Log.w(APP_NAME, "Airplane mode radios: " + mList);
        return mList;
    }

    static boolean antIsOnToggleableList(Context context) {
        return readAirplaneModeToggleableList(context).contains("ant");
    }

    static void incrementLaunchNumber(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(NUM_LAUNCHES, settings.getLong(NUM_LAUNCHES, 0) + 1).apply();
    }
    static long getNumLaunches(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong(NUM_LAUNCHES, 0);
    }

    /**
     * If user responds to nag dialog with "not now", nag type is 10, or repeat nag after 10 launches
     * if "remind me later" nag type is 25, or nag after 25 launches
     * @param context application context
     */
    static void setNagNum(Context context, int nagType) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(NAG_TYPE, nagType).apply();
    }

    /**
     * Start off with 9 launches before the hard sell
     * @param context app context to get Shared Prefs
     * @return number of launches before we can nag user
     */
    static int getNagNum(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(NAG_TYPE, 9);
    }

    static boolean canNagUser(Context context, boolean hasANT) {
        return hasANT && (getNumLaunches(context) > getNagNum(context));
    }
}
