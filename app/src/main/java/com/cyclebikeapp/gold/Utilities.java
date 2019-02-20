package com.cyclebikeapp.gold;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import static com.cyclebikeapp.gold.Constants.ACTIVITY_FILE_PATH;
import static com.cyclebikeapp.gold.Constants.AUTO_PAUSE;
import static com.cyclebikeapp.gold.Constants.BYTE_PER_MB;
import static com.cyclebikeapp.gold.Constants.HI_VIZ;
import static com.cyclebikeapp.gold.Constants.KEY_REQUESTING_LOCATION_UPDATES;
import static com.cyclebikeapp.gold.Constants.NAG_TYPE;
import static com.cyclebikeapp.gold.Constants.NO_INFO;
import static com.cyclebikeapp.gold.Constants.NUM_LAUNCHES;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_LATITUDE;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_LONGITUDE;
import static com.cyclebikeapp.gold.Constants.PREFS_DEFAULT_TIME;
import static com.cyclebikeapp.gold.Constants.PREFS_NAME;
import static com.cyclebikeapp.gold.Constants.PREF_SAVED_LOC_TIME;
import static com.cyclebikeapp.gold.Constants.SAVED_LAT;
import static com.cyclebikeapp.gold.Constants.SAVED_LON;
import static com.cyclebikeapp.gold.Constants.TCX_ACTIVITY_TYPE;
import static com.cyclebikeapp.gold.Constants.ZERO;


/**
 * Created by TommyD on 4/18/2016.
 *
 */
class Utilities {

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }
    /**
     * Returns the {@code location} object as a human readable string.
     */
    static String getLocationText() {
        return "Tracking location...";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    static boolean hasWifiInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        boolean hasInternetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && hasInternetPermission
                && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        boolean hasInternetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && hasInternetPermission;
    }

    static boolean isScreenWidthSmall(Context context){
        int[] screenSize = getScreenSize(context);
        // find smaller dimension of screen size, call that the width
        int screenWidth = screenSize[0] < screenSize[1] ? screenSize[0] : screenSize[1];
        return (screenWidth < Constants.SMALL_SCREEN_WIDTH);
    }

    static boolean isGPSLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            if (lm != null) {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        } catch (SecurityException ignored) {
        }
        return gps_enabled;
    }

    static boolean hasGPSPermission (Context context) {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    private static boolean isNetworkLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = false;
        try {
            if (lm != null) {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException ignored) {
        }
        return network_enabled;
    }

    /* Checks if external storage is available for read and write */
    static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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

    /**
     * Read user preference for type of activity file
     * @return an integer indicating activity file type 0 = .tcx file, 1 = .fit file
     */
    static int readActivityFileType(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String theString = sharedPref.getString(
                context.getResources().getString(R.string.pref_activity_file_key), TCX_ACTIVITY_TYPE);
        return Integer.parseInt(theString);
    }

    static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    static boolean hasStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private static final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + ACTIVITY_FILE_PATH;

    static boolean isFileSpaceAvailable(int mBRequired) {
        File temp = new File(pathName);
        long usuableSpaceMB = mBRequired + 1;
        try {
            if (temp.mkdirs()) {
                usuableSpaceMB = temp.getUsableSpace() / BYTE_PER_MB;
                temp.delete();
            }
        } catch (SecurityException ignore) {

        }
        //Log.w(APP_NAME, "External storage space usuable (MB): " + usuableSpaceMB);
        return mBRequired < usuableSpaceMB;
    }

    static void incrementLaunchNumber(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(NUM_LAUNCHES, settings.getLong(NUM_LAUNCHES, 0) + 1).apply();
    }
    private static long getNumLaunches(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong(NUM_LAUNCHES, 0);
    }

    /**
     * If user responds to nag dialog with "not now", nag type is 5, or repeat nag after 5 launches
     * if "remind me later" nag type is 7, or nag after 7 launches
     * @param context application context
     */
    static void setNagNum(Context context, int nagType) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(NAG_TYPE, nagType).apply();
    }

    /**
     * Start off with 5 launches before the hard sell
     * @param context app context to get Shared Prefs
     * @return number of launches before we can nag user
     */
    static int getNagNum(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(NAG_TYPE, 5);
    }

    static boolean hasBLE(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    static boolean canANTNagUser(Context context, boolean hasANT) {
        return hasANT && (getNumLaunches(context) > getNagNum(context));
    }
    static boolean canBLENagUser(Context context, boolean hasBLE) {
        return hasBLE && (getNumLaunches(context) > getNagNum(context));
    }

    static String composeGPSDialogTitle(Context mContext) {
        return mContext.getString(R.string.location_status_title);
    }

    private static String composeSatellitesInUseMessage(BikeStat bs) {
        String satInUseStatus;
        StringBuilder returnStringBuilder = new StringBuilder(Constants.SATELLITES_USED);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                if (bs.gpsSatelliteStatus != null) {
                    int numSatellites = bs.gpsSatelliteStatus.getSatelliteCount();
                    for (int i = 0; i < numSatellites; i++) {
                        if (bs.gpsSatelliteStatus.usedInFix(i)) {
                            satInUseStatus = String.format("%02d", bs.gpsSatelliteStatus.getSvid(i)) + " |"
                                    + (bs.gpsSatelliteStatus.hasAlmanacData(i) ? " a |" : NO_INFO)
                                    + (bs.gpsSatelliteStatus.hasEphemerisData(i) ? " e | " : NO_INFO)
                                    + String.valueOf(Math.round(bs.gpsSatelliteStatus.getCn0DbHz(i))) + " | "
                                    + getSatelliteType(bs.gpsSatelliteStatus.getConstellationType(i))
                                    + "\n";
                            returnStringBuilder.append(satInUseStatus);
                        }
                    }
                }
            } else {
                if (bs.satellites != null) {
                    for (GpsSatellite gpsSatellite : bs.satellites) {
                        if (gpsSatellite.usedInFix()) {
                            satInUseStatus = String.format("%02d", gpsSatellite.getPrn()) + " |"
                                    + (gpsSatellite.hasAlmanac() ? " a |" : " - |")
                                    + (gpsSatellite.hasEphemeris() ? " e |" : " - | ")
                                    + String.valueOf(Math.round(gpsSatellite.getSnr())) + " | "
                                    + Utilities.getSatelliteTypeFromPRN(gpsSatellite.getPrn())
                                    + "\n";
                            returnStringBuilder.append(satInUseStatus);
                        }
                    }
                }
            }
        return returnStringBuilder.toString();
    }

    private static String getSatelliteTypeFromPRN(int prn) {
        String satType = Constants.UNK;
        if (prn >= 1 && prn <= 32) {
            satType = Constants.GPS;
        } else if (prn >= 33 && prn < 55){
            satType = Constants.SBAS;
        } else if (prn >= 65 && prn <= 96){
            satType = Constants.GLO;
        } else if (prn >= 193 && prn <= 200){
            satType = Constants.QZSS;
        } else if (prn >= 201 && prn <= 235){
            satType = Constants.BEID;
        } else if (prn >= 301 && prn <= 330){
            satType = Constants.GAL;
        }
        return satType;
    }

    private static CharSequence getSatelliteType(int constellation) {
        String satType = "";
        switch (constellation) {
            case GnssStatus.CONSTELLATION_GPS:
                satType = Constants.GPS;
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                satType = Constants.SBAS;
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                satType = Constants.GLO;
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                satType = Constants.QZSS;
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                satType = Constants.BEID;
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                satType = Constants.GAL;
                break;
            case GnssStatus.CONSTELLATION_UNKNOWN:
                satType = Constants.UNK;
                break;
        }
        return satType;

    }
    static CharSequence composeGPSDialogMessage(Context mContext, BikeStat myBikeStat) {
        //# satellites in use, GPS accuracy, requires cell?, network?, enabled?
        // Fused location status info - accuracy, requires cell?, network?, enabled?
        // For each satellite, satellite #, "a", "e", "u", snr, reset almanac button to inject code.
        String yesString = mContext.getString(R.string.yes);
        String noString = mContext.getString(R.string.no);
        String locationNumSats = Constants.NUM_SATELLITES_USED + myBikeStat.getSatellitesInUse();
        String gpsLocationStatusTitle = mContext.getString(R.string.gps_status_title);
        String gpsLocationAccuracy = Constants.ACCURACY + String.valueOf(Math.round(myBikeStat.getLocationAccuracy())) + " m";

        String gpsLocationEnabled = Constants.GPS_ENABLED + (Utilities.isGPSLocationEnabled(mContext) ? yesString : noString);
        String networkLocationStatusTitle = mContext.getString(R.string.network_status_title);
        String networkLocationEnabled = Constants.NETWORK_ENABLED + (Utilities.isNetworkLocationEnabled(mContext) ? yesString : noString);
        String satelliteStatus = composeSatellitesInUseMessage(myBikeStat);

        return networkLocationStatusTitle + "\n" + networkLocationEnabled + "\n\n"
                + gpsLocationStatusTitle + "\n" + gpsLocationEnabled + "\n"
                + locationNumSats + "\n" + gpsLocationAccuracy + "\n"
                + satelliteStatus;
    }
    static boolean isBatterySaverActive(Context context){
        boolean savingBattery = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                savingBattery = !pm.isIgnoringBatteryOptimizations(context.getPackageName());
                Log.i("Utilities", "testing battery saver for "+context.getPackageName()+" - pm !null");
            }
        }
        Log.i("Utilities", "testing battery saver - build version: " + Build.VERSION.SDK_INT
                + " battery saver active: " + (savingBattery? "yes":"no"));
        return savingBattery;
    }

    private static int[] getScreenSize(Context context){
        Point displaymetrics = new Point(500,500);
        WindowManager mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWM != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mWM.getDefaultDisplay().getRealSize(displaymetrics);
            }
        }
        int h = displaymetrics.y;
        int w = displaymetrics.x;
        return new int[]{w,h};
    }

    static float getScreenDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
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
    static boolean isColorSchemeHiViz(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(HI_VIZ, false);
    }
    static boolean isAutoPause(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(AUTO_PAUSE, false);
    }
    static int getDistanceType(Context context) {
        SharedPreferences dSP = PreferenceManager.getDefaultSharedPreferences(context);
        String distanceType = context.getResources().getString(R.string.pref_distance_key);
        return Integer.parseInt(dSP.getString(distanceType, ZERO));
    }

    static int getDistanceUnit(Context context) {
        String unitDefault = context.getResources().getString(R.string.pref_unit_key);
        SharedPreferences dSP = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(dSP.getString(unitDefault, ZERO));
    }
}
