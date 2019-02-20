package com.cyclebikeapp.gold;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.AntSupportChecker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.id.message;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.cyclebikeapp.gold.Constants.ACTIVITY_FILE_TYPE;
import static com.cyclebikeapp.gold.Constants.APP_NAME;
import static com.cyclebikeapp.gold.Constants.BONUS_MILES;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_GPX_DIRECTORY;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_GPX_FILE;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_TCX_DIRECTORY;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_TCX_FILE;
import static com.cyclebikeapp.gold.Constants.CURR_WP;
import static com.cyclebikeapp.gold.Constants.DEG_PER_BEARING_ICON;
import static com.cyclebikeapp.gold.Constants.DISTANCE_TYPE_METRIC;
import static com.cyclebikeapp.gold.Constants.DISTANCE_TYPE_MILE;
import static com.cyclebikeapp.gold.Constants.DOUBLE_ZERO;
import static com.cyclebikeapp.gold.Constants.EXCEPTION;
import static com.cyclebikeapp.gold.Constants.FILE_NOT_FOUND;
import static com.cyclebikeapp.gold.Constants.FIRST_LIST_ELEM;
import static com.cyclebikeapp.gold.Constants.FIT_ACTIVITY_TYPE;
import static com.cyclebikeapp.gold.Constants.FOOT;
import static com.cyclebikeapp.gold.Constants.FORMAT_1F;
import static com.cyclebikeapp.gold.Constants.FORMAT_3D;
import static com.cyclebikeapp.gold.Constants.FORMAT_3_1F;
import static com.cyclebikeapp.gold.Constants.FORMAT_4_1F;
import static com.cyclebikeapp.gold.Constants.GPX;
import static com.cyclebikeapp.gold.Constants.HI_VIZ;
import static com.cyclebikeapp.gold.Constants.INITIALIZING_ROUTE;
import static com.cyclebikeapp.gold.Constants.JAN_1_2000;
import static com.cyclebikeapp.gold.Constants.KEY_AUTH_NO_NETWORK_INTENT;
import static com.cyclebikeapp.gold.Constants.KEY_BEARING;
import static com.cyclebikeapp.gold.Constants.KEY_CHOOSER_CODE;
import static com.cyclebikeapp.gold.Constants.KEY_CHOSEN_GPXFILE;
import static com.cyclebikeapp.gold.Constants.KEY_CHOSEN_TCXFILE;
import static com.cyclebikeapp.gold.Constants.KEY_DIM;
import static com.cyclebikeapp.gold.Constants.KEY_DISTANCE;
import static com.cyclebikeapp.gold.Constants.KEY_FORCE_NEW_TCX;
import static com.cyclebikeapp.gold.Constants.KEY_STREET;
import static com.cyclebikeapp.gold.Constants.KEY_TRACKPOINT_DENSITY_DEFAULT;
import static com.cyclebikeapp.gold.Constants.KEY_TRACK_DENSITY;
import static com.cyclebikeapp.gold.Constants.KEY_TURN;
import static com.cyclebikeapp.gold.Constants.KEY_UNIT;
import static com.cyclebikeapp.gold.Constants.KM;
import static com.cyclebikeapp.gold.Constants.LOADING_FILE;
import static com.cyclebikeapp.gold.Constants.LOOKING_FOR_ROUTE_DATA;
import static com.cyclebikeapp.gold.Constants.MAX_SPEED;
import static com.cyclebikeapp.gold.Constants.METER;
import static com.cyclebikeapp.gold.Constants.MILE;
import static com.cyclebikeapp.gold.Constants.MY_PERMISSIONS_REQUEST_LOCATION;
import static com.cyclebikeapp.gold.Constants.MY_PERMISSIONS_REQUEST_WRITE;
import static com.cyclebikeapp.gold.Constants.NAG_TYPE_LATER;
import static com.cyclebikeapp.gold.Constants.NAG_TYPE_NOT_NOW;
import static com.cyclebikeapp.gold.Constants.NO_ROUTE_DATA_IN_FILE;
import static com.cyclebikeapp.gold.Constants.ONE_SEC;
import static com.cyclebikeapp.gold.Constants.PREFS_NAME;
import static com.cyclebikeapp.gold.Constants.PREF_SAVED_LOC_TIME;
import static com.cyclebikeapp.gold.Constants.QUESTION;
import static com.cyclebikeapp.gold.Constants.RC_SHOW_FILE_LIST;
import static com.cyclebikeapp.gold.Constants.REQUEST_CHANGE_LOCATION_SETTINGS;
import static com.cyclebikeapp.gold.Constants.ROUTE_DISTANCE_TYPE;
import static com.cyclebikeapp.gold.Constants.ROUTE_FILE_TYPE;
import static com.cyclebikeapp.gold.Constants.RWGPS_EMAIL;
import static com.cyclebikeapp.gold.Constants.SAVED_LAT;
import static com.cyclebikeapp.gold.Constants.SAVED_LON;
import static com.cyclebikeapp.gold.Constants.SHOW_SHARING;
import static com.cyclebikeapp.gold.Constants.SPEED_SIZE_PIXELS;
import static com.cyclebikeapp.gold.Constants.SPEED_TRIPLE_X;
import static com.cyclebikeapp.gold.Constants.TCX;
import static com.cyclebikeapp.gold.Constants.TCX_LOG_FILE_FOOTER_LENGTH;
import static com.cyclebikeapp.gold.Constants.TCX_LOG_FILE_NAME;
import static com.cyclebikeapp.gold.Constants.TEN_SEC;
import static com.cyclebikeapp.gold.Constants.THREE_SEC;
import static com.cyclebikeapp.gold.Constants.TITLE_SIZE_PIXELS;
import static com.cyclebikeapp.gold.Constants.TMP_CB_ROUTE;
import static com.cyclebikeapp.gold.Constants.TP_DENSITY;
import static com.cyclebikeapp.gold.Constants.TRIP_DISTANCE;
import static com.cyclebikeapp.gold.Constants.TRIP_TIME;
import static com.cyclebikeapp.gold.Constants.TWENTYFOUR_HOURS;
import static com.cyclebikeapp.gold.Constants.UPLOAD_FILENAME;
import static com.cyclebikeapp.gold.Constants.UPLOAD_FILE_SEND_REQUEST_CODE;
import static com.cyclebikeapp.gold.Constants.USER_CANCELED;
import static com.cyclebikeapp.gold.Constants.VALUE_SIZE_PIXELS;
import static com.cyclebikeapp.gold.Constants.WILDCARD;
import static com.cyclebikeapp.gold.Constants.XML;
import static com.cyclebikeapp.gold.Constants.ZERO;
import static com.cyclebikeapp.gold.Constants._360;
import static com.cyclebikeapp.gold.Constants.accurateGPSSpeed;
import static com.cyclebikeapp.gold.Constants.dotPausedVal;
import static com.cyclebikeapp.gold.Constants.goodEnoughLocationAccuracy;
import static com.cyclebikeapp.gold.Constants.googleLat;
import static com.cyclebikeapp.gold.Constants.googleLon;
import static com.cyclebikeapp.gold.Constants.km_per_meter;
import static com.cyclebikeapp.gold.Constants.kph_per_mps;
import static com.cyclebikeapp.gold.Constants.mile_per_meter;
import static com.cyclebikeapp.gold.Constants.mph_per_mps;
import static com.cyclebikeapp.gold.Constants.nearEnough;
import static com.cyclebikeapp.gold.Constants.speedPausedVal;
import static com.cyclebikeapp.gold.LocationUpdatesService.EXTRA_LOCATION_STATUS;
import static com.cyclebikeapp.gold.LocationUpdatesService.EXTRA_LOCATION_STATUS_TYPE;
import static com.cyclebikeapp.gold.Utilities.canANTNagUser;
import static com.cyclebikeapp.gold.Utilities.canBLENagUser;
import static com.cyclebikeapp.gold.Utilities.getScreenDensity;
import static com.cyclebikeapp.gold.Utilities.getTCXFileAutoReset;
import static com.cyclebikeapp.gold.Utilities.hasBLE;
import static com.cyclebikeapp.gold.Utilities.hasWifiInternetConnection;
import static com.cyclebikeapp.gold.Utilities.isGPSLocationEnabled;
import static com.cyclebikeapp.gold.Utilities.isScreenWidthSmall;
import static com.cyclebikeapp.gold.Utilities.readActivityFileType;

/**
 * Copyright  2013, 2014 cyclebikeapp. All Rights Reserved.
 */

@SuppressLint({"DefaultLocale"})
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /** indicator of which Alert dialog is calling from the menu */
	private int dialogType = 0;
    private static int textColorWhite;
    private static int textColorHiViz;
	/** this HashMap contains the data in the turn list. It is updated in the
	 refreshHashMap method and passed to CrazyAdapter to display on the
	 screen. The associated tags are below
	 Also use this HashMap when long-pressing item to extract the distance */
	private final ArrayList<HashMap<String, String>> routeHashMap = new ArrayList<>();
	private View mLayout;
    private View myCoordinatorLayout;
    private TextView tripDistLabel, tripDistTitle;
	private TextView avgSpeedLabel, maxSpeedLabel, gpsSpeedLabel;
	private TextView avgSpeedTitle, maxSpeedTitle, gpsSpeedTitle;
	private TextView tripTimeLabel, tripTimeTitle;
	private TextView appMessage;
    private Snackbar mLocationSettingsSnackBar;
    // Tracks the bound state of the service.
    private boolean mLocationServiceBound = false;
    // A reference to the service used to get location updates.
    private LocationUpdatesService mLocationService;
	private Context context;
    private PowerManager.WakeLock myWakeLock;
    /** name of the route file being followed */
	private String chosenGPXFile = "";
	/** name of the previous route file being followed
	 need this in case the chosen file doesn't load, and we have to revert */
	private String prevChosenFile = "";
	/** BikeStat contains all bike related information: trip distance, time,
	 speeds, and control access to the log file */
	private BikeStat myBikeStat;
	/** NavRoute contains all route information, turn list, locations */
	private NavRoute myNavRoute;
	/** the current location as received from the GPS sensor */
	private Location myPlace = new Location(LocationManager.GPS_PROVIDER);

    private int prefTextColor;
    private int prefBackgroundColor;
	/** if this is the first location received, do something different
	 in BikeStat to calculate distance */
	private boolean gpsFirstLocation = true;
	/** the scrolling turn-by-turn list */
	private ListView turnByturnList;
	/** a means of assembling icons and text in each row of the turn list */
	private CrazyAdapter turnByturnAdapter;
	/** is the list still scrolling? */
    private boolean scrolling = false;
	/** have the unit preferences changed? */
	private boolean prefChanged = true;
	/** alternate satAcq message with this switch */
	private boolean satAcqMess = true;
	/** only open new tcx and FIT file in Location Listener; use this tag to force a new tcx and fit
	 rather than re-open the old one */
	private boolean forceNewTCX_FIT = false;
	/** if we're resuming the route and loading a file, need a switch
	 to open a new or re-open the old tcx and file */
    private boolean resumingRoute = false;
    private boolean testedBatterySaver = false;
    // The BroadcastReceiver used to listen for broadcasts from the Location service.
    private MyLocationReceiver myLocationsReceiver;
    // all the Location functions
    private LocationHelper mLocationHelper;
    static boolean apiOkay = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH;
    private PowerManager pm = null;
	static final boolean debugAppState = false;
	static final boolean debugFITFile = false;
    private static final boolean debugLocation = false;
    private final String logtag = this.getClass().getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (debugAppState) Log.i(logtag, "onCreate()");
		setContentView(R.layout.activity_scroller);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		turnByturnList = findViewById(R.id.list);
		/* should the app keep the screen on - not if we're paused */
		turnByturnList.setKeepScreenOn(true);
		/* handles the scrolling action */
		turnByturnList.setOnScrollListener(scrollListener);
		/* responds to long-press in turn list */
		turnByturnList.setOnItemLongClickListener(longClickListener);
		turnByturnList.setOnItemClickListener(turnListClickListener);
		context = getApplicationContext();
        Utilities.setRequestingLocationUpdates(context, false);
        textColorWhite = ContextCompat.getColor(context, R.color.white);
        textColorHiViz = ContextCompat.getColor(context, R.color.texthiviz);
		myBikeStat = new BikeStat(context);
		myNavRoute = new NavRoute(context);
        myLocationsReceiver = new MyLocationReceiver();
        initializeScreen();
		initializeMyPlace();
        mLocationSettingsSnackBar = Snackbar.make(
                myCoordinatorLayout,
                getString(R.string.open_location_settings),
                Snackbar.LENGTH_INDEFINITE);
        // keep track of number of launches to nag user to upgrade
        Utilities.incrementLaunchNumber(context);
        // ANTSupportChecker will find if user's phone has ANT chip
        if (canANTNagUser(context, AntSupportChecker.hasAntFeature(context))){
            upgradeANTNAG();
        } else if(canBLENagUser(context, hasBLE(context))) {
            upgradeBLENAG();
        }
        mLocationHelper = new LocationHelper(getApplicationContext());
        googlePlayAvailable(context);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm != null) {
                myWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CycleBike:mywakelock");
                myWakeLock.acquire(Constants.TWENTYFOUR_HOURS);
            }
        }
		autoResumeRoute();
	}// onCreate

    private boolean askLocationPermission() {
        if (Utilities.hasFineLocationPermission(getApplicationContext())) {
            if (debugAppState) Log.i(logtag, "ask Location permission: has location permission");
            return true;
        } else {
            // Request permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            writeAppMessage(getString(R.string.loc_permission_denied),
                    ContextCompat.getColor(context, R.color.gpsred));
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeAppMessage("", textColorWhite);
                    mLocationService.requestLocationUpdates();
                } else {
                    // we'll ask again in Location Watchdog
                    writeAppMessage(getString(R.string.loc_permission_denied), ContextCompat.getColor(context, R.color.gpsred));
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeAppMessage("", textColorWhite);
                } else {
                    writeAppMessage(getString(R.string.write_permission_denied), ContextCompat.getColor(context, R.color.gpsred));
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            // other 'case' lines to check for other permissions this app might request
        }
    }

	/** if tcx file is not old, resume previous route */
	private void autoResumeRoute() {
		// called from onCreate()
		if (debugAppState) Log.i(logtag, "autoResumeRoute()");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String outfile = settings.getString(TCX_LOG_FILE_NAME, "");
        if (debugAppState) Log.i(logtag, "autoResumeRoute() - outfile: " + outfile);
        boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(outfile, getTCXFileAutoReset(context));
		if (!old) {
			if (debugAppState) Log.i(logtag, "autoResumeRoute() - tcx file is not old");
			restoreSharedPrefs();
            if (debugAppState) Log.i(logtag, "autoResumeRoute() - chosenGPXFile: " + chosenGPXFile);
            myNavRoute.mChosenFile = new File(chosenGPXFile);
            prefChanged = true;
			refreshScreen();
			resumingRoute = true;
			//load file in async task with progress bar in case file is big
			//it would generate ANR error
			LoadData task = new LoadData();
			task.execute();
		} else {//output file is old
			if (debugAppState) Log.i(logtag, "autoResumeRoute() - tcx file is old");
			// also delete all cached route files
			deleteAllTmpRouteFiles();
			resetData();
			//have to put this in shared prefs, or the old file name is loaded in onResume
			chosenGPXFile = "";
            if (debugAppState) Log.i(logtag, "autoResumeRoute() - chosenGPXFile: " + chosenGPXFile);
            settings.edit().putString(KEY_CHOSEN_GPXFILE, chosenGPXFile).apply();
		}
	}

	private void initializeMyPlace() {
		// see if there is a last-known-location, default to Google Cafe
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String s = settings.getString(SAVED_LAT, Double.toString(googleLat));
		myPlace.setLatitude(Double.valueOf(s));
		s = settings.getString(SAVED_LON, Double.toString(googleLon));
		myPlace.setLongitude(Double.valueOf(s));
		// use preferences to set this last known location
	}

	// this operates the turn-list scroller
    private final OnScrollListener scrollListener = new OnScrollListener() {
		public void onScroll(AbsListView view, int firstItem,
							 int visibleItemCount, int totalItemCount) {
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					if (scrolling) {
						myNavRoute.firstListElem = view.getFirstVisiblePosition();
						scrolling = false;
					}
					refreshScreen();
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				case OnScrollListener.SCROLL_STATE_FLING:
					scrolling = true;
					break;
				default:
					break;
			}
		}
	};

	private final AdapterView.OnItemClickListener turnListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mLocationSettingsSnackBar.isShown()) {
                mLocationSettingsSnackBar.dismiss();
            }
        }
    };

	private final OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
			// only respond to long-click if location is current & there is a route
			if ((myNavRoute.mergedRoute_HashMap.size() < 1) || (!isGPSLocationCurrent() && !isFusedLocationCurrent())) {
				return true;
			}
			if (checkNearEnough(pos)) {
				// only if we're near to the clicked way point
				// Now do all the nitty-gritty of re-navigating from here
				// set bonus miles so logic will recognize way points
				myNavRoute.setBonusMiles(myBikeStat.getGPSTripDistance()
						- myNavRoute.mergedRoute_HashMap.get(pos).getRouteMiles());
				// set .beenThere = false for all way points
				for (int index = 0; index < myNavRoute.mergedRoute_HashMap.size(); index++) {
					GPXRoutePoint tempRP;
					tempRP = myNavRoute.mergedRoute_HashMap.get(index);
					tempRP.setBeenThere(false);
					myNavRoute.mergedRoute_HashMap.set(index, tempRP);
				}// for all way points in the route
			}
			return true;
		}
	};

    private boolean isGPSLocationCurrent() {
        return (System.currentTimeMillis() - myBikeStat.newGPSLocSysTimeStamp) < TEN_SEC;
    }

    private boolean isFusedLocationCurrent() {
        return (System.currentTimeMillis() - myBikeStat.newFusedLocSysTimeStamp) < TEN_SEC;
    }

	private void saveState() {
		if (debugAppState) Log.i(logtag, "saveState()");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SAVED_LAT, Double.toString(myPlace.getLatitude()));
		editor.putString(SAVED_LON, Double.toString(myPlace.getLongitude()));
		editor.putLong(PREF_SAVED_LOC_TIME, myPlace.getTime());
		editor.putString(TRIP_TIME, Double.toString(myBikeStat.getGPSRideTime()));
		editor.putString(TRIP_DISTANCE, Double.toString(myBikeStat.getGPSTripDistance()));
		editor.putString(MAX_SPEED, Double.toString(myBikeStat.getMaxSpeed()));
		editor.putString(BONUS_MILES, Double.toString(myNavRoute.getBonusMiles()));
		editor.putInt(CURR_WP, myNavRoute.currWP);
		editor.putInt(FIRST_LIST_ELEM, myNavRoute.firstListElem);
		editor.putString(KEY_CHOSEN_GPXFILE, chosenGPXFile);
        if (debugAppState) Log.i(logtag, "saveState() - chosenGPXFile: " + chosenGPXFile);
		editor.putString(TCX_LOG_FILE_NAME, myBikeStat.tcxLog.outFileName);
		editor.putInt(TCX_LOG_FILE_FOOTER_LENGTH, myBikeStat.tcxLog.outFileFooterLength);
		editor.putBoolean(KEY_FORCE_NEW_TCX, forceNewTCX_FIT);
		editor.apply();
	}

    @Override
    protected void onResume() {
        if (debugAppState) { Log.i(logtag, "onResume()"); }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myLocationsReceiver);
        LocalBroadcastManager.getInstance(this).registerReceiver(myLocationsReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        //save the name of the route file temporarily until its validated
        prevChosenFile = chosenGPXFile;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        chosenGPXFile = settings.getString(KEY_CHOSEN_GPXFILE, "");
        if (debugAppState) Log.i(logtag, "onResume() - chosenGPXFile: " + chosenGPXFile);
        super.onResume();
        myBikeStat.tcxLog.outFileName = settings.getString(TCX_LOG_FILE_NAME, "");
        myBikeStat.tcxLog.outFileFooterLength = settings.getInt(TCX_LOG_FILE_FOOTER_LENGTH, 1);
        refreshScreen();
        startSensors();
        if (askLocationPermission()) {
            if (mLocationServiceBound){
                mLocationService.requestLocationUpdates();
                if (debugLocation) { Log.i(logtag, "mLocationService.requestLocationUpdates()"
                        + (Utilities.requestingLocationUpdates(context)?" not requesting":" are already requesting")); }
            }
            mLocationHelper.stopLocationUpdates();
            mLocationHelper.startLocationUpdates();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        if (debugAppState) { Log.i(logtag, "onSharedPreferenceChanged() - key: " + key); }
        switch (key) {
            case HI_VIZ:
                if (Utilities.isColorSchemeHiViz(context)) {
                    prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_black);
                    prefTextColor = textColorHiViz;
                } else {
                    prefTextColor = textColorWhite;
                    prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_gray);
                }
                break;

            case KEY_TRACKPOINT_DENSITY_DEFAULT:
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                //if we're returning from SettingsActivity, test if Track Point density
                // has changed. If so we must re-load chosenFile
                // see if SharedPreferences value of trackDensity is different than DefaultSharedPreferences
                int trackDensity = settings.getInt(KEY_TRACK_DENSITY, 0);
                myNavRoute.defaultTrackDensity = 0;
                SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(this);
                String defTrackDensity = defaultSettings.getString(KEY_TRACKPOINT_DENSITY_DEFAULT, ZERO);
                myNavRoute.defaultTrackDensity = Integer.valueOf(defTrackDensity);
                if (trackDensity != myNavRoute.defaultTrackDensity && myNavRoute.mergedRoute_HashMap.size() > 0) {
                    // save RouteMiles @ firstListElem so we can recalculate firstListElem with new track density
                    myNavRoute.routeMilesatFirstListElem = myNavRoute.mergedRoute_HashMap.get(
                            myNavRoute.firstListElem).getRouteMiles();
                    editor.putInt(KEY_TRACK_DENSITY, myNavRoute.defaultTrackDensity);
                    editor.apply();
                    new ChangeTrackDensityBackground().execute();
                }
                break;
        }
        // distancePref = 0 for Route distance display; 1 for direct distance display
        prefChanged = true;
    }

    private void startSensors() {
        if (debugAppState) { Log.i(logtag, "startSensors()"); }
        stopLocationWatchdog();
        startLocationWatchdog();
    }

    @Override
    protected void onPause() {
        stopLocationWatchdog();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myLocationsReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        saveState();
        super.onStop();
        if (mLocationServiceBound) {
            // Unbind from the Location Service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mLocationServiceConnection);
            mLocationServiceBound = false;
        }
        if (mLocationSettingsSnackBar != null) {
            mLocationSettingsSnackBar.dismiss();
        }
    }

	@Override
	protected void onDestroy() {
		if (debugAppState) Log.i(logtag, "onDestroy()");
		//stop listening to locations, stop writing to log file and refreshing the screen
        //we're not uploading the file, so "" means just close file
        new CloseActivityFilesBackground().execute("");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        mLocationHelper.stopLocationUpdates();
        mLocationService.removeLocationUpdates();
        try {
            myWakeLock.release();
        } catch (Exception ignore){}
	}

	@Override
	protected void onStart() {
		// This verification should be done during onStart() because the system
		// calls this method when the user returns to the activity, which
		// ensures the desired location provider is enabled each time the
		// activity resumes from the stopped state.
		super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mLocationServiceConnection, BIND_AUTO_CREATE);
	}

	private void dealWithDialog(int message, int title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						handleDialogAction();
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		// Set other dialog properties
		builder.setMessage(message).setTitle(title).show();
	}

    private void upgradeANTNAG() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setNegativeButton(R.string.upgrade_button_text,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 10; remind user after 10 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_NOT_NOW);
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.cyclebikeapp.plus1")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            Intent browseIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.cyclebikeapp.plus1"));
                            PackageManager packageManager = getPackageManager();
                            if (browseIntent.resolveActivity(packageManager) != null) {
                                startActivity(browseIntent);
                            } else {
                                Log.w(logtag, getString(R.string.no_browser));
                            }
                        }
                    }
                });
        builder.setPositiveButton(R.string.remind_later,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 25; remind user after 25 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_LATER);
                    }
                });

        // Set other dialog properties
        builder.setMessage(R.string.ant_nag_message).setTitle(R.string.ant_nag_title).show();
    }
    private void upgradeBLENAG() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setNegativeButton(R.string.upgrade_button_text,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 10; remind user after 10 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_NOT_NOW);
                        try {

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.cyclebikeapp.cyclebike_ble")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            Intent browseIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.cyclebikeapp.cyclebike_ble"));
                            PackageManager packageManager = getPackageManager();
                            if (browseIntent.resolveActivity(packageManager) != null) {
                                startActivity(browseIntent);
                            } else {
                                Log.w(logtag, getString(R.string.no_browser));
                            }
                        }
                    }
                });

        builder.setPositiveButton(R.string.not_now,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 10; remind user after 10 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_NOT_NOW);
                    }
                });
        // Set other dialog properties
        builder.setMessage(R.string.ble_nag_message).setTitle(R.string.ble_nag_title).show();
    }

    private void testBatteryOptimization() {
        // test for battery saver and show dialog to turn off
        if (Utilities.isBatterySaverActive(getApplicationContext()) && !testedBatterySaver){
            testedBatterySaver = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                writeAppMessage(Constants.TESTING_BATTERY_SAVER, textColorWhite);
                Bundle dialogBundle = new Bundle();
                dialogBundle.putCharSequence(Constants.DDF_KEY_TITLE, getString(R.string.bat_optimization_title));
                dialogBundle.putCharSequence(Constants.DDF_KEY_MESSAGE, getString(R.string.bat_optimization));
                MADeviceDialogFragment newFragment = MADeviceDialogFragment.newInstance(dialogBundle);
                newFragment.show(getFragmentManager(), "MainbatteryOptimizationSettings");
            }
        }
    }
    private void handleDialogAction() {// OK button pressed in Alert Dialog
		switch (dialogType) {
			case 100:// menu toggle airplane mode
				toggleAirplaneMode();
				break;
			case 200: // menu reset type
				doMenuReset();
				break;
			case 300: // GPS enable type
				enableLocationSettings();
				break;
			case 400: // menu restore type
				restoreSharedPrefs();
				myNavRoute.mChosenFile = new File(chosenGPXFile);
				refreshScreen();
				resumingRoute = true;
				//load file in async task with progress bar
				LoadData task = new LoadData();
				task.execute();
				break;
			default:
				break;
		}
	}

	private void doMenuReset() {
        new CloseActivityFilesBackground().execute("");
		resetData();
		//clear the NavRoute
		myNavRoute.mergedRoute.clear();
		myNavRoute.mergedRoute_HashMap.clear();
		chosenGPXFile = "";
		createTitle("");
		//clear the turn list
		initHashMap();
		gpsFirstLocation = true;
		refreshScreen();
		// open a new tcx log file when we get the next location
		forceNewTCX_FIT = true;
	}

	private void toggleAirplaneMode() {
		Intent settingsIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
		startActivity(settingsIntent);
	}

	/** clear all the trip data */
	private void resetData() {
		myBikeStat.reset();
		myNavRoute.setBonusMiles(0);
		forceNewTCX_FIT = true;
		SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor1 = settings1.edit();
		editor1.putString(BONUS_MILES, Double.toString(myNavRoute.getBonusMiles()));
		editor1.putString(TRIP_TIME, Double.toString(myBikeStat.getGPSRideTime()));
		editor1.putString(TRIP_DISTANCE, Double.toString(myBikeStat.getGPSTripDistance()));
		editor1.putString(MAX_SPEED, Double.toString(myBikeStat.getMaxSpeed()));
		editor1.apply();
	}

	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

    private void restoreSharedPrefs() {
        // called from autoResumeRoute() and menu item RestoreRoute
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String s = settings.getString(TRIP_TIME, Double.toString(0.1));
        myBikeStat.setGPSTripTime(Double.valueOf(s));
        s = settings.getString(TRIP_DISTANCE, DOUBLE_ZERO);
        myBikeStat.setGPSTripDistance(Double.valueOf(s));
        s = settings.getString(MAX_SPEED, DOUBLE_ZERO);
        myBikeStat.setMaxSpeed(Double.valueOf(s));
        myNavRoute.currWP = settings.getInt(CURR_WP, WILDCARD);
        myNavRoute.firstListElem = settings.getInt(FIRST_LIST_ELEM, WILDCARD);
        s = settings.getString(BONUS_MILES, DOUBLE_ZERO);
        myNavRoute.setBonusMiles(Double.valueOf(s));
        chosenGPXFile = settings.getString(KEY_CHOSEN_GPXFILE, "");
        myBikeStat.tcxLog.outFileName = settings.getString(TCX_LOG_FILE_NAME, "");
        myBikeStat.tcxLog.outFileFooterLength = settings.getInt(TCX_LOG_FILE_FOOTER_LENGTH, 1);
        forceNewTCX_FIT = settings.getBoolean(KEY_FORCE_NEW_TCX, false);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_layout, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_load:
				Intent loadFileIntent = new Intent(this, ShowFileList.class);
				//tell the ShowFileList chooser to display route files
				loadFileIntent.putExtra(CHOOSER_TYPE, ROUTE_FILE_TYPE);
				startActivityForResult(loadFileIntent, RC_SHOW_FILE_LIST);
				break;
			case R.id.menu_settings:
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				// use this to tell refreshScreen to re-write the titles
				prefChanged = true;
				break;
			case R.id.menu_reset:
				dialogType = 200;
				dealWithDialog(R.string.reset_message, R.string.reset_title);
				break;
			case R.id.menu_reset_strava:
				// call stravashare with empty filename to just authorize, test for empty filename before doing upload
				Intent stravaUploadIntent = new Intent(this, StravaShareCB.class);
				stravaUploadIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				stravaUploadIntent.putExtra(UPLOAD_FILENAME, "");
				startActivityForResult(stravaUploadIntent, UPLOAD_FILE_SEND_REQUEST_CODE);
				break;
			case R.id.action_share:
				// this intent displays a list of files for the user to select
				Intent shareFileIntent = new Intent(MainActivity.this, ShowFileList.class);
				// change "type" parameter to display .tcx or .fit files in ShowFileList()
				shareFileIntent.putExtra(CHOOSER_TYPE, ACTIVITY_FILE_TYPE);
				startActivityForResult(shareFileIntent, RC_SHOW_FILE_LIST);
				break;
			case R.id.menu_about:
				Intent i11 = new Intent(this, AboutScroller.class);
				startActivity(i11);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}// onOptionsItemSelected()

	private void initializeScreen() {
        boolean isSmallScreen = isScreenWidthSmall(context);

        if (Utilities.isColorSchemeHiViz(context)) {
            prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_black);
            prefTextColor = textColorHiViz;
        } else {
            prefTextColor = textColorWhite;
            prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_gray);
        }
		mLayout = findViewById(R.id.RelativeLayout101);
        mLayout.setBackgroundColor(prefBackgroundColor);
        myCoordinatorLayout = findViewById(R.id.myCoordinatorLayout);
        View distanceCell = findViewById(R.id.distanceLayout);
        distanceCell.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                showGPSDialog();
                return true;
            }
        });

		initHashMap();
		tripDistTitle = findViewById(R.id.textView24);
		tripDistLabel = findViewById(R.id.textView25);
		tripTimeTitle = findViewById(R.id.textView38);
		tripTimeLabel = findViewById(R.id.textView39);
		avgSpeedTitle = findViewById(R.id.textView26);
		avgSpeedLabel = findViewById(R.id.textView27);
		maxSpeedTitle = findViewById(R.id.textView28);
		maxSpeedLabel = findViewById(R.id.textView29);
        gpsSpeedTitle = findViewById(R.id.textView32);
        gpsSpeedLabel = findViewById(R.id.textView33);
        float screenDensity = getScreenDensity(context);
        float titleSize = TITLE_SIZE_PIXELS/screenDensity;
        float valueSize = VALUE_SIZE_PIXELS/screenDensity;
        float speedSize = SPEED_SIZE_PIXELS/screenDensity;
        if (isSmallScreen) {
            avgSpeedTitle.setTextSize(COMPLEX_UNIT_DIP, titleSize);
            avgSpeedLabel.setTextSize(COMPLEX_UNIT_DIP, valueSize);
            maxSpeedTitle.setTextSize(COMPLEX_UNIT_DIP, titleSize);
            maxSpeedLabel.setTextSize(COMPLEX_UNIT_DIP, valueSize);
            gpsSpeedTitle.setTextSize(COMPLEX_UNIT_DIP, titleSize);
            gpsSpeedLabel.setTextSize(COMPLEX_UNIT_DIP, speedSize);
        }
		appMessage = findViewById(R.id.textView40);
        prefChanged = true;
        testBatteryOptimization();
	}// initializeScreen()

    public void doCancelClick() {
        // Do stuff here.
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void doShowBatterySettings() {
        Intent myIntent = new Intent();
        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivity(myIntent);
    }

    private void showGPSDialog() {
        Bundle dialogBundle = new Bundle();

        String title = Utilities.composeGPSDialogTitle(this);
        dialogBundle.putCharSequence(Constants.DDF_KEY_TITLE, title);
        dialogBundle.putCharSequence(Constants.DDF_KEY_MESSAGE, Utilities.composeGPSDialogMessage(this, myBikeStat));
        MADeviceDialogFragment newFragment = MADeviceDialogFragment.newInstance(dialogBundle);
        newFragment.show(getFragmentManager(), "Main-GPS");
    }
	/** the HashMap is the turn-by-turn rows in the scrolling list */
	private void initHashMap() {
		routeHashMap.clear();
		HashMap<String, String> hmItem = new HashMap<>();
		for (int j = 0; j < 7; j++) {
			// just put an X for turn and north direction arrow in the list
			hmItem.put(KEY_TURN, Integer.toString(99));
			hmItem.put(KEY_STREET, "");
			hmItem.put(KEY_DISTANCE, "");
			hmItem.put(KEY_UNIT, "");
			hmItem.put(KEY_BEARING, ZERO);
			hmItem.put(KEY_DIM, ZERO);
			routeHashMap.add(hmItem);
		}
		turnByturnAdapter = new CrazyAdapter(this, routeHashMap);
		turnByturnList.setAdapter(turnByturnAdapter);
	}

	private void refreshScreen() {
        if (debugAppState) Log.i(logtag, "refreshScreen() ");
        boolean screenOn = pm != null && ((apiOkay && pm.isInteractive()) || pm.isScreenOn());
        if (screenOn) {
            if (prefChanged) {
                if (debugAppState) Log.i(logtag, "refreshScreen() -prefChanged");
                mLayout.invalidate();
                mLayout.setBackgroundColor(prefBackgroundColor);
                // if units changed, refresh units in titles
                refreshTitles();
                // if display pref changed, update CAD, HR, Power display
                prefChanged = false;
            }
            setScreenDim();
            refreshMergedRouteHashMap();
            refreshBikeStatRow();
        }
	}// refreshScreen()

	private void refreshMergedRouteHashMap() {
		double distMultiplier = mile_per_meter;
		String unit;
        int distanceUnit = Utilities.getDistanceUnit(context);
        int distanceType = Utilities.getDistanceType(context);
		int j = turnByturnList.getFirstVisiblePosition();
		while ((j < routeHashMap.size())
				&& (j < myNavRoute.mergedRoute_HashMap.size())
				&& (j < turnByturnList.getLastVisiblePosition() + 1)) {
			HashMap<String, String> hmItem = new HashMap<>();
			float result[];
			result = distFromMyPlace2WPMR(j, distanceType);
			// results returns in miles; convert to meters, if needed
			double distance = result[0] * distMultiplier;
			int turnDirIcon = myNavRoute.mergedRoute_HashMap.get(j).turnIconIndex;
			String streetName = myNavRoute.mergedRoute_HashMap.get(j).getStreetName();
			String distanceString;
			if (isGPSLocationCurrent() || isFusedLocationCurrent()) {
				distanceString = String.format(FORMAT_1F, distance);
			} else {
                distanceString = QUESTION;
            }
			if (distanceUnit == DISTANCE_TYPE_MILE) {
				unit = MILE;
			} else {
				distMultiplier = km_per_meter;
				unit = KM;
			}
			if (distance < 0.1) {// switch to display in feet / m
				int dist;
				// increment in multiples of 20', a likely resolution limit
				if (distanceUnit == DISTANCE_TYPE_MILE) {
					dist = (int) Math.floor(distance * 264) * 20;
					unit = FOOT;
				} else {
					dist = (int) Math.floor(distance * 100) * 10;
					unit = METER;
				}
				if (isGPSLocationCurrent() || isFusedLocationCurrent()) {
					distanceString = String.format(FORMAT_3D, dist);
				}
			}// if dist<0.1
			double bearing = (result[1] + _360) % _360;
			// we only get an accurate DOT when we're moving at a speed greater than "accurateGPSSpeed"
			// this will use the last accurate DOT from GPS location
			double relBearing = (bearing - myNavRoute.accurateDOT + _360) % _360;
			// convert between relative bearing and the bearing icon (arrow) want north arrow for rel bearing between
			// (360 - 11.5) to (360 + 11.5) or 348deg to 11.5deg; north arrow icon is #0, nne arrow is #1, etc
			int bearingIcon = (int) Math.floor((((relBearing + DEG_PER_BEARING_ICON / 2) % _360) / DEG_PER_BEARING_ICON));
			int dimLevel = textColorWhite;
			if (Utilities.isColorSchemeHiViz(context)) {
				dimLevel = textColorHiViz;
				// don't put hiViz "X" icon; other hiViz incons have iconLevel +18 for high-viz color
				if (turnDirIcon != 99) {
					turnDirIcon = myNavRoute.mergedRoute_HashMap.get(j).turnIconIndex + 18;
				}
			}
			boolean dimmed = (myNavRoute.mergedRoute_HashMap.get(j).isBeenThere());
			if (dimmed) {
				dimLevel = ContextCompat.getColor(context, R.color.textdim);
				// if icons are dimmed the icon level is at +9
				turnDirIcon = myNavRoute.mergedRoute_HashMap.get(j).turnIconIndex + 9;
				// dimmed bearing icon levels are +16
				bearingIcon += 16;
			}
			if (myNavRoute.isProximate() & (j == myNavRoute.currWP)) {
				dimLevel = ContextCompat.getColor(context, R.color.gpsgreen);
			}
			// change distance, distance unit, bearing icon level
			hmItem.put(KEY_TURN, Integer.toString(turnDirIcon));
			hmItem.put(KEY_STREET, streetName);
			hmItem.put(KEY_DISTANCE, distanceString);
			hmItem.put(KEY_UNIT, unit);
			hmItem.put(KEY_BEARING, Integer.toString(bearingIcon));
			hmItem.put(KEY_DIM, Integer.toString(dimLevel));
			routeHashMap.set(j, hmItem);
			j++;
		}// while visible item
		// after all edits are done
		 turnByturnAdapter.notifyDataSetChanged();
		// Decide which element should be at the top of the list and how to scroll there if
        // we're close to a WayPoint, but it's not at the top of the list, smooth scroll to top
        // and set 1st element to the current WayPoint; this could be because we've manually scrolled the list away from currWP
		if (myNavRoute.isProximate()) {
			//convert myNavRoute.currWP to a hash map index
			myNavRoute.firstListElem = myNavRoute.currWP;
		}
		// After we've moved away from the Proximate Way Point, need to bump the scroll list up one row
		turnByturnList.setSelectionFromTop(myNavRoute.firstListElem, 0);
	}

	private void initializeMergedRouteTurnList() {
		if (debugAppState) Log.v(logtag, "initializeMergedRouteTurnList() ");
	// called from changeTrackDensityBackground and LoadData
		// Don't calculate the route distances for the whole list here maybe do that in a background task,
        // but it has to be re-done for refresh hashmap anyway
		String unit;
		int dimLevel = textColorWhite;
        int distanceUnit = Utilities.getDistanceUnit(context);
        if (distanceUnit == DISTANCE_TYPE_MILE) {
			unit = MILE;
		} else {
			unit = KM;
		}
		// this is the TrackPoint density-reduce array that matches the index of the routeHashMap
		// this makes it easy to match index in refreshHashMap
		myNavRoute.mergedRoute_HashMap.clear();
		routeHashMap.clear();// clears the HashMap
		// go thru the mergedRoute and convert to HashMap
		// called from dealWithGoodData after LoadData
		if (myNavRoute.mergedRoute != null) {
			for (int i = 0; i < myNavRoute.mergedRoute.size(); i++) {
				// only add waypoints that are not marked 'delete'
				if (!myNavRoute.mergedRoute.get(i).delete) {
					GPXRoutePoint tempRP;
					tempRP = myNavRoute.mergedRoute.get(i);
					// copying to new GPXRoutePoint ArrayList
					myNavRoute.mergedRoute_HashMap.add(tempRP);
				}
			}
		} else {// mergedRoute == null
			return;
		}
		for (int i = 0; i < myNavRoute.mergedRoute_HashMap.size(); i++) {
			HashMap<String, String> hmItem = new HashMap<>();
			int turnDirIcon = myNavRoute.mergedRoute_HashMap.get(i).turnIconIndex;
			if (Utilities.isColorSchemeHiViz(context)) {
				dimLevel = textColorHiViz;
				// don't put hiViz "X" icon; other hiViz incons have iconLevel +18 for high-viz color
				if (turnDirIcon != 99){
					turnDirIcon += 18;
				}
			}
			String streetName = myNavRoute.mergedRoute_HashMap.get(i).getStreetName();
			String distanceString = QUESTION;
			if ((i > turnByturnList.getFirstVisiblePosition() - 1)
					&& (i < turnByturnList.getLastVisiblePosition() + 1)){
				float result[];
				result = distFromMyPlace2WPMR(i, Utilities.getDistanceType(context));
				// results returns in miles; convert to meters, if needed
				double distMultiplier = mile_per_meter;
				if (distanceUnit == DISTANCE_TYPE_MILE) {
					unit = MILE;
				} else {
					distMultiplier = km_per_meter;
					unit = KM;
				}
				double distance = result[0] * distMultiplier;
				if (isGPSLocationCurrent() || isFusedLocationCurrent()) {
					distanceString = String.format(FORMAT_1F, distance);
				}
				if (distance < 0.1) {// switch to display in feet / m
					int dist;
					// increment in multiples of 20', a likely resolution limit
					if (distanceUnit == DISTANCE_TYPE_MILE) {
						dist = (int) Math.floor(distance * 264) * 20;
						unit = FOOT;
					} else {
						dist = (int) Math.floor(distance * 100) * 10;
						unit = METER;
					}
					if (isGPSLocationCurrent() || isFusedLocationCurrent()) {
						distanceString = String.format(FORMAT_3D, dist);
					}
				}// if dist<0.1
			}//only calculate distance for visible turns; this will be re-done
			// in refreshHashMap, but it may take a while in large lists
			int bearingIcon = myNavRoute.mergedRoute_HashMap.get(i).relBearIconIndex;
			// creating new HashMap
			hmItem.put(KEY_TURN, Integer.toString(turnDirIcon));
			hmItem.put(KEY_STREET, streetName);
			hmItem.put(KEY_DISTANCE, distanceString);
			hmItem.put(KEY_UNIT, unit);
			hmItem.put(KEY_BEARING, Integer.toString(bearingIcon));
			hmItem.put(KEY_DIM, Integer.toString(dimLevel));
			routeHashMap.add(hmItem);
		}// for loop
		// add a blank item to the bottom of the list
		HashMap<String, String> hmItem = new HashMap<>();
		hmItem.put(KEY_TURN, Integer.toString(99));
		hmItem.put(KEY_STREET, "");
		hmItem.put(KEY_DISTANCE, "");
		hmItem.put(KEY_UNIT, "");
		hmItem.put(KEY_BEARING, Integer.toString(0));
		hmItem.put(KEY_DIM, ZERO);
		routeHashMap.add(hmItem);
		// Getting adapter by passing files data ArrayList
		turnByturnAdapter = new CrazyAdapter(this, routeHashMap);
		turnByturnList.setAdapter(turnByturnAdapter);
		turnByturnList.setSelectionFromTop(myNavRoute.firstListElem, 0);
	}

	private void refreshTitles() {
		// only refresh titles if unit preference changed
		String tripDistString, avgSpeedString, maxSpeedString, gpsSpeedString;
		if (Utilities.getDistanceUnit(context) == DISTANCE_TYPE_MILE) {
            if (debugAppState) Log.i(logtag, "refreshTitles() type=mile");

            tripDistString = getResources().getString(R.string.trip_dist_mi);
			avgSpeedString = getResources().getString(R.string.avg_speed_mph);
			maxSpeedString = getResources().getString(R.string.max_speed_mph);
			gpsSpeedString = getResources().getString(R.string.curr_gps_speed_mph);
		} else {
            if (debugAppState) Log.i(logtag, "refreshTitles() type=km");

            tripDistString = getResources().getString(R.string.trip_dist_km);
			avgSpeedString = getResources().getString(R.string.avg_speed_kph);
			maxSpeedString = getResources().getString(R.string.max_speed_kph);
			gpsSpeedString = getResources().getString(R.string.curr_gps_speed_kph);
		}
		tripDistTitle.setText(tripDistString);
		avgSpeedTitle.setText(avgSpeedString);
		maxSpeedTitle.setText(maxSpeedString);
		gpsSpeedTitle.setText(gpsSpeedString);
		tripTimeTitle.setText(getResources().getString(R.string.trip_time));
	}

	private void refreshBikeStatRow() {
        double value, maxValue;
		double distMultiplier = mile_per_meter;
		double speedMultiplier = mph_per_mps;
        if (Utilities.getDistanceUnit(context) == DISTANCE_TYPE_METRIC) {
            speedMultiplier = kph_per_mps;
            distMultiplier = km_per_meter;
        }
		tripTimeLabel.setText(myBikeStat.getTripTimeStr());
		tripTimeLabel.setTextColor(prefTextColor);
		value = myBikeStat.getGPSTripDistance() * distMultiplier;
		tripDistLabel.setText(String.format(FORMAT_4_1F, value));
		tripDistLabel.setTextColor(prefTextColor);
		value = myBikeStat.getAvgSpeed() * speedMultiplier;
		avgSpeedLabel.setText(String.format(FORMAT_3_1F, value));
		avgSpeedLabel.setTextColor(prefTextColor);
		String speedString;
		maxValue = myBikeStat.getMaxSpeed() * speedMultiplier;
		value = myBikeStat.getSpeed() * speedMultiplier;
        if (isGPSLocationCurrent() || isFusedLocationCurrent()) {
            speedString = String.format(FORMAT_3_1F, value);
        } else {
            speedString = SPEED_TRIPLE_X;
        }
        maxSpeedLabel.setText(String.format(FORMAT_3_1F, maxValue));
        maxSpeedLabel.setTextColor(prefTextColor);
        gpsSpeedLabel.setText(speedString);
        gpsSpeedLabel.setTextColor(prefTextColor);
    }

	/**
	 * let the screen dim as per user settings when paused
	 */
	private void setScreenDim() {
		if (myBikeStat.isPaused()) {
			findViewById(R.id.list).setKeepScreenOn(false);
		} else {
			findViewById(R.id.list).setKeepScreenOn(true);
		}
	}

	private void writeAppMessage(String message, int color) {

        boolean shouldShowAppMessageBox;
        // write a message in the App message area
        if (!Utilities.hasGPSPermission(getApplicationContext())){
            shouldShowAppMessageBox = true;
            message = getString(R.string.loc_permission_denied);
            color = ContextCompat.getColor(context, R.color.gpsred);
        } else if (!Utilities.hasStoragePermission(getApplicationContext())) {
            shouldShowAppMessageBox = true;
            message = getString(R.string.write_permission_denied);
            color = ContextCompat.getColor(context, R.color.gpsred);
        } else if (!isGPSLocationEnabled(getApplicationContext())) {
            color = textColorWhite;
            shouldShowAppMessageBox = true;
            message = getResources().getString(R.string.open_location_settings);
        } else if (!isGPSLocationCurrent() && !isFusedLocationCurrent()) {
            color = textColorWhite;
            shouldShowAppMessageBox = true;
            if (satAcqMess) {
                satAcqMess = false;
                message = getResources().getString(R.string.acq_satellites1);
            } else {
                satAcqMess = true;
                message = getResources().getString(R.string.acq_satellites2);
            }
        } else {
            shouldShowAppMessageBox = false;
        }
        showAppMessBox(shouldShowAppMessageBox);
        appMessage.setText(message);
        appMessage.setTextColor(color);
	}

	/**
	 * if there is no gps status message to show, delete the box so we have more
	 * space to show the turn-by-turn list
	 *
	 * @param b if b==true, show the message box
	 */
	private void showAppMessBox(boolean b) {
		int visibility = android.view.View.GONE;
		if (b) {
			visibility = android.view.View.VISIBLE;
		}
		appMessage.setVisibility(visibility);
	}

	private float[] distFromMyPlace2WPMR(int index, int distanceType) {
		// prefDistanceType = ROUTE_DISTANCE_TYPE for Route distance display; DIRECT_DISTANCE_TYPE for direct distance display
		float[] results;
		if (distanceType == ROUTE_DISTANCE_TYPE) {
			results = calcMRRouteDistance(index);
		} else {
			results = calcMRDirectDistance(index);
		}
		return results;
	}// distFromMyPlace2WP()

	private float[] calcMRDirectDistance(int index) {
		Location there = new Location(LocationManager.GPS_PROVIDER);
		float[] results = { 0, 0 };
		if (index > myNavRoute.mergedRoute_HashMap.size())
			return results;
		there.setLatitude(myNavRoute.mergedRoute_HashMap.get(index).lat);
		there.setLongitude(myNavRoute.mergedRoute_HashMap.get(index).lon);
		results[0] = myPlace.distanceTo(there);
		results[1] = myPlace.bearingTo(there);
		return results;
	}

	private float[] calcMRRouteDistance(int index) {
		// in meters
		Location there = new Location(LocationManager.GPS_PROVIDER);
		double distance = 0, distCurrWP;
		float[] results = { 0, 0 };
		if (index > myNavRoute.mergedRoute_HashMap.size())
			return results;
        if (index >= myNavRoute.currWP) {
			// calculate distance from myPlace to currWP
			there.setLatitude(myNavRoute.mergedRoute_HashMap.get(myNavRoute.currWP).lat);
			there.setLongitude(myNavRoute.mergedRoute_HashMap.get(myNavRoute.currWP).lon);
			distCurrWP = myPlace.distanceTo(there);
			if (index == myNavRoute.currWP) {
				distance = distCurrWP;
			} else {
				// distance to any entry in the list is difference in RouteMiles
				// between that entry and the RouteMiles at the currWP, plus the distance from
				// where we are to the currWP (calculated before)
				distance += (myNavRoute.mergedRoute_HashMap.get(index).getRouteMiles()
						- myNavRoute.mergedRoute_HashMap.get(myNavRoute.currWP).getRouteMiles());
				// If currWP.beenThere is false, add distCurrWP to running total
				// because we're before the currWP
				// If currWP.beenThere is true subtract distCurrWP from running
				// distance total for other WPs because we're after the currWP,
				// just not farEnough away to increment the currWP
				if (myNavRoute.mergedRoute_HashMap.get(myNavRoute.currWP).isBeenThere()) {
					distance -= distCurrWP;
				} else {
					distance += distCurrWP;
				}
			}
			results[0] = (float) distance;
			there.setLatitude(myNavRoute.mergedRoute_HashMap.get(index).lat);
			there.setLongitude(myNavRoute.mergedRoute_HashMap.get(index).lon);
			results[1] = myPlace.bearingTo(there);
		}
		return results;
	}

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusType = intent.getStringExtra(EXTRA_LOCATION_STATUS_TYPE);
            switch (statusType) {
                case EXTRA_LOCATION_STATUS:
                    Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
                    if (debugLocation) {
                        Log.i(logtag, "onFusedLocationChanged()");
                        Log.i(logtag, "location.getAccuracy() " + location.getAccuracy()
                                + "location.hasSpeed() " + location.hasSpeed()
                                + " location.getTime() " + location.getTime()
                                + " location.getProvider() " + location.getProvider()
                                + " gpsLocationOld " + (!isGPSLocationCurrent()? "yes" : "no"));
                    }
                    // if location time-stamp is weird, skip the data
                    // also demand "goodEnoughLocationAccuracy" before using the data
                    if (debugLocation && Math.abs(System.currentTimeMillis() - location.getTime()) > TWENTYFOUR_HOURS) {
                        Log.wtf(logtag, "Location Error");
                    }
                    if (location.getTime() < JAN_1_2000
                            || location.getAccuracy() > goodEnoughLocationAccuracy
                            || Math.abs(System.currentTimeMillis() - location.getTime()) > TWENTYFOUR_HOURS) {
                        return;
                    }
                    myBikeStat.newFusedLocSysTimeStamp = System.currentTimeMillis();
                    // only use fused location if we don't have a current gps location; it's probably the same anyway
                    if (!isGPSLocationCurrent()) {
                        dealWithNewLocation(location);
                    }
                    break;
            }

        }
    }

    @SuppressWarnings("Convert2Lambda")
    private class LocationHelper {
        GpsListener gpsListener;
        MyLocationListener mLocationListener;
        LocationManager mLocationManager;

        LocationHelper(Context context) {
            mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            mLocationListener = new MyLocationListener();
            gpsListener = new GpsListener();
        }

        /**
         * Use Location Manager to handle location updates unless CycleBike is in the background.
         * Then the Location Service kicks-in
         */
        void startLocationUpdates() {
            if (debugLocation) { Log.i(logtag, "startLocationUpdates() - getting Providers"); }
            boolean locationAllowed = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!locationAllowed) {
                if (debugLocation) {Log.wtf(logtag, "startLocationUpdates() - location not allowed");}
                return;
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mLocationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                        /**
                         * Called periodically to report GNSS satellite status.
                         *
                         * @param gpsSatelliteStatus the current status of all satellites.
                         */
                        @Override
                        public void onSatelliteStatusChanged(GnssStatus gpsSatelliteStatus) {
                            super.onSatelliteStatusChanged(gpsSatelliteStatus);
                            myBikeStat.gpsSatelliteStatus = gpsSatelliteStatus;
                        }
                    });
                }
                if (mLocationManager != null) {
                    mLocationManager.addGpsStatusListener(gpsListener);
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            Constants.MIN_TIME_BW_UPDATES,
                            Constants.MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    myBikeStat.isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    myBikeStat.isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } else {
                    if (debugLocation) { Log.wtf(logtag, "locationManager is null!"); }
                }
            } catch (IllegalStateException ignore) {
            }
            if (debugLocation) {
                Log.i(logtag, "startLocationUpdates()"
                        + " isGPSEnabled? " + (myBikeStat.isGPSEnabled ? "yes" : "no")
                        + " isNetworkEnabled? " + (myBikeStat.isNetworkEnabled? "yes" : "no"));
            }
        }
        class MyLocationListener implements LocationListener {
            /**
             * Called when the location has changed.
             * <p>
             * <p> There are no restrictions on the use of the supplied Location object.
             *
             * @param location The new location, as a Location object.
             */
            @Override
            public void onLocationChanged(Location location) {
                if (debugLocation) {
                    Log.i(logtag, "GPS location changed");
                    Log.i(logtag, "location.getAccuracy() " + location.getAccuracy()
                            + "location.hasSpeed() " + location.hasSpeed()
                            + " location.getTime() " + location.getTime()
                            + " location.getProvider() " + location.getProvider()
                            + " location.getTime() < JAN_1_2000? " + ((location.getTime() < JAN_1_2000) ? "yes" : "no")
                            + " System.currentTimeMillis() " + System.currentTimeMillis()
                            + " Math.abs(System.currentTimeMillis() - location.getTime()) "
                            + Math.abs(System.currentTimeMillis() - location.getTime()));
                }
                // if location time-stamp is weird, skip the data
                // also demand "goodEnoughLocationAccuracy" before using the data
                if (location.getTime() < JAN_1_2000
                        || location.getAccuracy() > goodEnoughLocationAccuracy
                        || Math.abs(System.currentTimeMillis() - location.getTime()) > TWENTYFOUR_HOURS) {
                    return;
                }
                myBikeStat.newGPSLocSysTimeStamp = System.currentTimeMillis();
                dealWithNewLocation(location);
            }

            /**
             * Called when the provider status changes. This method is called when
             * a provider is unable to fetch a location or if the provider has recently
             * become available after a period of unavailability.
             *
             * @param provider the name of the location provider associated with this
             *                 update.
             * @param status the status
             * @param extras   an optional Bundle which will contain provider specific
             *                 status variables.
             *                 <p>
             *                 <p> A number of common key/value pairs for the extras Bundle are listed
             *                 below. Providers that use any of the keys on this list must
             *                 provide the corresponding value as described below.
             *                 <p>
             *                 <ul>
             *                 <li> satellites - the number of satellites used to derive the fix
             */
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (debugLocation) {
                    Log.i(logtag, "onStatusChanged()" + " provider: " + provider + " status: " + status);
                }
            }

            /**
             * Called when the provider is enabled by the user.
             *
             * @param provider the name of the location provider associated with this
             *                 update.
             */
            @Override
            public void onProviderEnabled(String provider) {
            }

            /**
             * Called when the provider is disabled by the user. If requestLocationUpdates
             * is called on an already disabled provider, this method is called
             * immediately.
             *
             * @param provider the name of the location provider associated with this
             *                 update.
             */
            @Override
            public void onProviderDisabled(String provider) {
            }

        }
        class GpsListener implements GpsStatus.Listener {
            /**
             * event event number for this notification
             */
            GpsStatus mGnssStatus;
            @Override
            public void onGpsStatusChanged(int event) {
                if (debugLocation) { Log.i(logtag, "onGpsStatusChanged()" + event); }
                int iCountInView = 0;
                int iCountInUse = 0;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                myBikeStat.satellites = mLocationManager.getGpsStatus(mGnssStatus).getSatellites();
                if (myBikeStat.satellites != null) {
                    for (GpsSatellite gpsSatellite : myBikeStat.satellites) {
                        iCountInView++;
                        if (gpsSatellite.usedInFix()) {
                            iCountInUse++;
                        }
                    }
                }
                if (debugLocation) {
                    Log.i(logtag, "# satellites in view: " + iCountInView + " # satellites in use: " + iCountInUse);
                }
                myBikeStat.setSatellitesInUse(iCountInUse);
            }
        }
        /**
         * Disable locationListener when Main Activity is destroyed
         */
        void stopLocationUpdates() {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager.removeGpsStatusListener(gpsListener);
            }
        }

    }

    void googlePlayAvailable(Context context) {

        if (!hasWifiInternetConnection(context)) {
            return;
        }
        int googlePlayAvailableResponse = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (googlePlayAvailableResponse != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, googlePlayAvailableResponse, 0).show();
        }
    }

    private void dealWithNewLocation(final Location location) {
        // if location time-stamp is weird, skip the data
        // also demand "goodEnoughLocationAccuracy" before using the data
        if (location.getTime() < JAN_1_2000
                || location.getAccuracy() > goodEnoughLocationAccuracy
                || Math.abs(System.currentTimeMillis() - location.getTime()) > TWENTYFOUR_HOURS) {
            return;
        }
        myPlace = location;
        myNavRoute.setPrevDOT(myNavRoute.getDOT());
        myNavRoute.setDOT(location.getBearing());
        // DOT bearing from gps locations is not too accurate if traveling slowly
        // To display relative bearing to waypoints in refreshHashMap use the last accurate DOT
        if (location.getSpeed() > accurateGPSSpeed) {
            myNavRoute.accurateDOT = location.getBearing();
        }
        myNavRoute.setDeltaDOT(Math.abs(myNavRoute.getDOT() - myNavRoute.getPrevDOT()));
        // This doesn't account for time ticking while loc not current.
        // Situation where GPS is switched off, or drops-out, or rider went inside for lunch.
        // When new location is received, distance may only be 10', but elapsed time may be an hour.
        // Average speed now includes this elapsed time. When new location received,
        // use the new deltaDistance, but calculate delta time based on current average speed.
        // This only affects the display, data written to file is not changed.
        if (location.hasSpeed()){
            myBikeStat.gpsSpeedCurrent = true;
            myBikeStat.setSpeed(location.getSpeed());
        }
        new ThreadPerTaskExecutor().execute(dealWithLocationRunnable);
        // firstLocation is used to start the track record; it is true after a reset()
        if (gpsFirstLocation) {
            if (debugAppState) Log.i(logtag, "gpsFirstLocation");
            writeAppMessage("", textColorWhite);
            gpsFirstLocation = false;
            // pause briefly to write the first track record
            myBikeStat.setPaused(true);
            // Re-open or create a new tcx file if it's old. Also we can force a new tcx file with the boolean
            openReopenTCX_FIT();
            // now un-pause
            myBikeStat.setPaused(false);
        }// first location
        // write to tcx file
        writeTrackRecord();
        testZeroPaused();
        if (!scrolling && (pm != null) && pm.isScreenOn()) {
            refreshScreen();
        }
    }

    // Use a background Thread to deal with a new Location, calculating Route waypoint distances, etc
    // This prevents blocking the UI Thread
    Runnable dealWithLocationRunnable = new Runnable() {
        @Override
        public void run() {
            myBikeStat.setLastGoodWP(myPlace, gpsFirstLocation);
            @SuppressLint({"NewApi", "LocalSuppress"})
            boolean screenOn = pm != null && ((apiOkay && pm.isInteractive()) || pm.isScreenOn());
            if (screenOn) {
                // update the Route Waypoint distances if screen is on
                myNavRoute.refreshRouteWayPoints(myPlace, myBikeStat.getGPSTripDistance());
            }
        }
    };

    /**
     * test for paused condition so we won't write log entries, allow screen to dim, etc
     */
    private void testZeroPaused() {
        boolean paused = false;
        if (!isGPSLocationCurrent() && !isFusedLocationCurrent()
                || ((myBikeStat.getSpeed() < speedPausedVal) && (myNavRoute.getDeltaDOT() < dotPausedVal))) {
            paused = true;
        }
        myBikeStat.setPaused(paused);
        if (debugAppState) Log.i(logtag, "testZeroPaused() paused: " + (myBikeStat.isPaused()?"yes":"no"));
    }

	private void writeTrackRecord() {

        if (!Utilities.hasStoragePermission(getApplicationContext())) {
            // Should we show an explanation? Check box "don't show again" override.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                writeAppMessage(getString(R.string.write_permission_denied),
                        ContextCompat.getColor(context, R.color.gpsred));
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
        //if we're rebuilding or closing the fit file from re-opening tcx, or sharing a file,
		//don't write a new tcx record, just return
		// this method only takes about 20 ms, so it's okay on the UI thread
		if (myBikeStat.fitLog.isFileEncoderBusy()) {
			return;
		}
		if (myBikeStat.isPaused()) {
			// close the current track on pause()
			myNavRoute.trackClosed = true;
		} else {
            new ThreadPerTaskExecutor().execute(writeTrackRecordRunnable);
			if (!myBikeStat.fitLog.getError().equals("")){
				openReopenTCX_FIT();
			}
			// if file not open, try to re-open; getError() will return SD card error if !fileHasPermission
			if (myBikeStat.tcxLog.getError().equals("")) {
				// no error, successfully wrote record
				myNavRoute.trackClosed = false;
			} else {
				forceNewTCX_FIT = true;
				openReopenTCX_FIT();
			}
		}
	}

    // Use a background Thread to write the data when we get a new Location.
    // This prevents blocking the UI Thread
    Runnable writeTrackRecordRunnable = new Runnable() {
        @Override
        public void run() {
            if (!myBikeStat.fitLog.isFileEncoderBusy()) {
                synchronized (myBikeStat.tcxLog){
                    myBikeStat.tcxLog.writeTCXRecord(myBikeStat, myNavRoute);
                }
                synchronized (myBikeStat.fitLog){
                    myBikeStat.fitLog.writeRecordMesg(myBikeStat);
                }
            }
        }
    };

	/**
	 * Whenever we close the fit file, the FileEncoder writes
	 * the fit file in a background task. When finished, set a flag that allows
	 * us to accept new data.
	 */
	public class CloseActivityFilesBackground extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {

			myBikeStat.fitLog.setFileEncoderBusy(true);
		}

		@Override
		protected String doInBackground(String... params) {
			myBikeStat.tcxLog.closeTCXLogFile();
			myBikeStat.fitLog.closeFitFile();
			return params[0];
        }

        @Override
		protected void onPostExecute(String sharingFileName) {
			super.onPostExecute(sharingFileName);

			myBikeStat.fitLog.setFileEncoderBusy(false);
			if (!("").equals(sharingFileName)) {
				uploadFileSend(sharingFileName);
			}
		}
	}

	/** Since route files could be changed by user and the name could be the same,
	 * we can't keep route files in private storage for very long.
	 * We'll delete all files in private storage when autoResumeRoute() decides that .tcx file is old.
	 * Loading routes from private storage was only intended to avoid the long LoadData() process
	 * when navigating the app
	 * */
	private void deleteAllTmpRouteFiles() {
		// Delete all cached route files if .tcx file is old
		String[] routeFiles = fileList();
		for (String file : routeFiles) {
			if (file.contains(TMP_CB_ROUTE)){
				deleteFile(file);
			}
		}
	}// deleteAllRouteFiles()

    /**
     * change trackdensity in hash map from a prepared mergedRoute. This could
     * be a long task for a large route file, so put in background task
     */
    private class ChangeTrackDensityBackground extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // save RouteMiles @ firstListElem so we can recalculate firstListElem with new track density
            myNavRoute.routeMilesatFirstListElem = myNavRoute.mergedRoute_HashMap.get(
                    myNavRoute.firstListElem).getRouteMiles();
            // save RouteMiles @ currWP so we can recalculate currWP with new track density
            myNavRoute.changeTrkPtDensity(myNavRoute.defaultTrackDensity);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //all UI altering tasks have to go in the post-execute method
            initializeMergedRouteTurnList();// set-up the HashMap for street turns
            // recalculate firstListElem using previous RouteMiles at top of list
            myNavRoute.recalcFirstListElem();
            // recalculate currWP using TripDistance plus bonus miles
            myNavRoute.recalcCurrWP(myBikeStat.getGPSTripDistance() - myNavRoute.getBonusMiles());
            //		    setProgressBarIndeterminateVisibility(Boolean.FALSE);
            // save the route here in a background task
            new ThreadPerTaskExecutor().execute(saveRouteFileRunnable);
            refreshScreen();
        }
    }

    private Runnable restoreRouteFileRunnable = new Runnable() {
        @Override
        public void run() {
            restoreRouteFile(chosenGPXFile);
        }
    };

    private Runnable saveRouteFileRunnable = new Runnable() {
        @Override
        public void run() {
            saveRouteFile(chosenGPXFile);
        }
    };

	/**
	 * Bypass the SAX parser when restoring a route or changing TrackPoint
	 * density and load the route ArrayList from private file storage. Returns
	 * an error if the route is not in private storage; then we'll have to use
	 * the SAX parser
	 *
	 * @param fileName the file to restore
	 **/
	@SuppressWarnings("unchecked")
	private String restoreRouteFile(String fileName) {

		FileInputStream fis;
		String error = "";
		// add prefix denoting track point density and removing path characters
		fileName = adjustFileName(fileName);
		try {
			fis = openFileInput(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			myNavRoute.mergedRoute = (ArrayList<GPXRoutePoint>) ois.readObject();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			error = FILE_NOT_FOUND;
		} catch (Exception e) {
			error = EXCEPTION;
			e.printStackTrace();
		}
		return error;
	}

	/**
	 * save the route ArrayList to private storage so we can bypass the SAX
	 * parser when restoring route or changing TrackPoint density. Saves some
	 * time when using a big Trackpoint file
	 *
	 * @param fileName the route to save
	 **/
	private void saveRouteFile(String fileName) {
		// add prefix denoting track point density and removing path characters
		fileName = adjustFileName(fileName);
		// see if the file already exists
		String[] routeFiles = fileList();
		boolean fileAlreadyExists = false;
		for (String file : routeFiles) {
			if (file.equals(fileName)) {
				fileAlreadyExists = true;
				break;
			}
		}
		// if the file doesn't exist, write it; otherwise don't write it
		if (!fileAlreadyExists) {
			try {
				FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(myNavRoute.mergedRoute);
				oos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String adjustFileName(String fileName) {
		// delete path prefix; private storage names can't have path symbols
		// filename returned from ShowFileList has path characters in it
		if (fileName != null) {
			int start = fileName.lastIndexOf("/") + 1;
			int end = fileName.length();
			if ((end - start) <= 0) {
				fileName = "";
			} else {
				fileName = fileName.substring(start, end);
			}
		}
		// add prefix denoting track point density
		SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(context);
		String defTrackDensity = defaultSettings.getString(getResources().getString(R.string.pref_trackpoint_density_key), "0");
		return TP_DENSITY + defTrackDensity + fileName + TMP_CB_ROUTE;
	}


 	/**
	 * Use an asynchronous, background thread to load the file with a progress
	 * bar in case it's a big file
	 */
    private class LoadData extends AsyncTask<Void, String, Void> {
		ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            myNavRoute.setError("");
            // display the progress dialog
            if (!chosenGPXFile.equals("")) {
                progressDialog = ProgressDialog.show(MainActivity.this,
                        LOADING_FILE, LOOKING_FOR_ROUTE_DATA, false);
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressDialog.dismiss();
                        myNavRoute.setError(USER_CANCELED);
                    }
                });
            }
        }

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (progressDialog != null){
                progressDialog.setMessage(values[0]);
            }
		}

		@Override
		protected Void doInBackground(Void... voids) {
			// See if we have this file cached; if so, load from cache
			if (!("").equals(restoreRouteFile(chosenGPXFile))) {
				// couldn't find the file in cache, so use the SAX parser via .loadNavRoute
				if (!chosenGPXFile.equals("")) {
					myNavRoute.loadNavRoute(getApplicationContext());
					if ((myNavRoute.handler.handlersGPXRoute.size() == 0)
							&& (myNavRoute.handler.handlersTrackPtRoute.size() == 0)) {
						if (myNavRoute.getError().equals("")) {
							// don't obscure another error
							myNavRoute.setError(NO_ROUTE_DATA_IN_FILE);
						}
					}
				}
				// if there was no SAX error, lat/long error, etc and there is
				// route data, initialize the route
				if (myNavRoute.getError().equals("") && (!chosenGPXFile.equals(""))) {
					if (progressDialog != null){
                        progressDialog.setCancelable(false);
                        publishProgress(INITIALIZING_ROUTE);
                    }
					myNavRoute.prepareRoute(getApplicationContext());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// all UI altering tasks have to go in the post-execute method
			if (progressDialog != null) {
                progressDialog.dismiss();
            }
			if (!chosenGPXFile.equals("")) {
				if (!myNavRoute.getError().equals("")) {
					revertChosenFile();
					// either "no route data...", or invalid lat/lon data, or user-canceled
					Toast.makeText(getApplicationContext(), myNavRoute.getError(), Toast.LENGTH_LONG).show();
					myNavRoute.setError("");
				} else {
					dealWithGoodData();
				}
			} else {//there was no filename specified
				myNavRoute.mergedRoute.clear();
				initHashMap();
			}
		}

		private void revertChosenFile() {
			//the file selected in the Chooser isn't valid, return the
			//last good file to Shared Preferences and NavRoute.chosenFile
			myNavRoute.mChosenFile = new File(prevChosenFile);
			chosenGPXFile = prevChosenFile;
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(KEY_CHOSEN_GPXFILE, prevChosenFile);
			editor.apply();
            new ThreadPerTaskExecutor().execute(restoreRouteFileRunnable);
			createTitle(prevChosenFile);
		}

		private void dealWithGoodData() {
			// this forces the Location Listener to determine whether to opn the old Activity files
			gpsFirstLocation = true;
			myNavRoute.firstListElem = 0;
			myNavRoute.currWP = 0;
			myNavRoute.refreshRouteWayPoints(myPlace, myBikeStat.getGPSTripDistance());
			myNavRoute.setProximate(false);
			// once we know the file is good, save filename and path in shared prefs
			createTitle(chosenGPXFile);
			initializeMergedRouteTurnList();// set-up the HashMap for street turns
			forceNewTCX_FIT = false;
			if (!resumingRoute) { // loading a new route file
				// if tcx file isn't old (>2 hours), ask to
				// open a new tcx file for the new route and zero-out data
				boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(myBikeStat.tcxLog.outFileName, getTCXFileAutoReset(context));
				if (!old) {
					doAskResetPermission();
				}
			} else {// we are resuming route via menu item or autoResumeRoute
				// in which case we should open the current tcx file
				// LoadData sets firstListElem = 0; must restore this value
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				myNavRoute.currWP = settings.getInt(CURR_WP, WILDCARD);
				myNavRoute.firstListElem = settings.getInt(FIRST_LIST_ELEM, WILDCARD);
				turnByturnList.setSelectionFromTop(myNavRoute.firstListElem, 0);
			}
			// save the route here in a background task
            new ThreadPerTaskExecutor().execute(saveRouteFileRunnable);
        }// Deal with good data
	}// LoadData class

	private void doAskResetPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String buttonTextID[] = { getString(R.string.ok), getString(R.string.no)};
		builder.setPositiveButton(buttonTextID[0], new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// a new file will be opened when a new location is received
				forceNewTCX_FIT = true;
				resetData();
				refreshScreen();
			}
		});
		builder.setNegativeButton(buttonTextID[1], new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User pressed the no button
				forceNewTCX_FIT = false;
			}
		});
		// Set other dialog properties
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				forceNewTCX_FIT = false;
			}
		});
		builder.setMessage(getString(R.string.okay_to_reset_trip_data_)).setTitle(getString(R.string.reset_data)).show();
	}

	/**
	 * include the route name in the window title
	 * @param chosenFile is the route file we're following
	 */
	private void createTitle(String chosenFile) {
		if (!chosenFile.equals("")) {
			int start = chosenFile.lastIndexOf("/") + 1;
			int end = chosenFile.length();
			if (chosenFile.endsWith(GPX)) {
				end = chosenFile.lastIndexOf(GPX);
			} else if (chosenFile.endsWith(TCX)) {
				end = chosenFile.lastIndexOf(TCX);
			} else if (chosenFile.endsWith(XML)) {
				end = chosenFile.lastIndexOf(XML);
			}
			String title = chosenFile.substring(start, end);
			setTitle(title);
		} else {
			setTitle(APP_NAME);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if (requestCode == RC_SHOW_FILE_LIST) {
			if (resultCode == RESULT_OK) {
				switch (data.getExtras().getInt(KEY_CHOOSER_CODE)) {
					case CHOOSER_TYPE_GPX_DIRECTORY:
						// intent -> start chooser activity
						Intent loadFileIntent = new Intent(this, ShowFileList.class);
						//indicate the chooser type is choosing gpx file
						loadFileIntent.putExtra(CHOOSER_TYPE, ROUTE_FILE_TYPE);
						loadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivityForResult(loadFileIntent, RC_SHOW_FILE_LIST);
						break;
					case CHOOSER_TYPE_GPX_FILE:
						prevChosenFile = chosenGPXFile;
						chosenGPXFile = settings.getString(KEY_CHOSEN_GPXFILE, "");
						if (!chosenGPXFile.equals("")) {
							myNavRoute.mChosenFile = new File(chosenGPXFile);
							//refresh the screen to indicate we've moved out of Chooser
							refreshScreen();
							//we're not trying to restore the route and force a new tcx file
							resumingRoute = false;
							//load file in async task with progress bar
							new LoadData().execute();
						}
						break;
					case CHOOSER_TYPE_TCX_DIRECTORY:
						// we don't actually let user choose a different directory
						// when searching for an activity file.
						// If we did, this is how we would go back to the chooser
						Intent loadFileIntent1 = new Intent(this, ShowFileList.class);
						//indicate the chooser type is choosing tcx file
						loadFileIntent1.putExtra(CHOOSER_TYPE, ACTIVITY_FILE_TYPE);
						loadFileIntent1.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivityForResult(loadFileIntent1, RC_SHOW_FILE_LIST);
						break;
					case CHOOSER_TYPE_TCX_FILE:
						String mChosenTCXFile = settings.getString(KEY_CHOSEN_TCXFILE, "");
						String sharingFileName = mChosenTCXFile;
						//Log.i(logtag, "onActivityResult()- TCX choice: " + mChosenTCXFile);
						if (readActivityFileType(context) == Integer.valueOf(FIT_ACTIVITY_TYPE)) {
							// replace the suffix to indicate a fit file instead of a tcx file
							sharingFileName = myBikeStat.fitLog.delTCXFITSuffix(mChosenTCXFile) + ".fit";
						}
                        // save the sharingFileName in SharedPrefs so we can access it in the ThhreadPerTask executor
                        String sharingName_noPath = myBikeStat.fitLog.delTCXFITSuffix(myBikeStat.fitLog.stripFilePath(sharingFileName));
                        if (mustCloseFit(myBikeStat.tcxLog.outFileName, sharingName_noPath)){
							// sharing the current log files
							// close the activity files before uploading them
							// give CFFB the sharing filename so it can pass it on to UploadFileSend when finished closing
							new CloseActivityFilesBackground().execute(sharingFileName);
						} else {
							uploadFileSend(sharingFileName);
						}
						break;
					default:
						break;
				}//returned from ShowFileList Activity
			}
		} else if (requestCode == UPLOAD_FILE_SEND_REQUEST_CODE) {
			// this hasn't really finished the sharing operation. the Intent returns after user selects an app to use for sharing
			// we've closed the activity files while sharing. Now re-open them. Now we can re-write track data to the files
			// If we've come back from turning-on WiFi when authorizing StravaShare, go back
			String sharingFileName = settings.getString(KEY_CHOSEN_TCXFILE, "");
			try {
				if ((data != null) && data.hasExtra(KEY_AUTH_NO_NETWORK_INTENT)) {
					goBackToStravaShare(resultCode, data, sharingFileName);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			sharingFileName = myBikeStat.fitLog.delTCXFITSuffix(myBikeStat.fitLog.stripFilePath(sharingFileName));
			String tcxLogFileName = myBikeStat.fitLog.delTCXFITSuffix(myBikeStat.fitLog.stripFilePath(myBikeStat.tcxLog.outFileName));
			// can't re-open the current outFile if we're trying to share it, so we have to reset
			// if we're sharing an old file there is no need to reset the current data
			forceNewTCX_FIT = tcxLogFileName.contains(sharingFileName);
			openReopenTCX_FIT();
		}
	}

	/**
	 * Test if we have to close the FileEncoder. Only have to close before sharing
	 * if we're sharing the current log file and we're sharing a fit file
	 * @param tcxLogFileName current log file
	 * @param sharingFileName file we're sharing
	 * @return true if we have to close FileEncoder
	 */
	private boolean mustCloseFit(String tcxLogFileName, String sharingFileName) {
		return readActivityFileType(context) == Integer.valueOf(FIT_ACTIVITY_TYPE)
                && tcxLogFileName.contains(sharingFileName);
	}

	/**
	 * If we needed to Authorize user Strava account, and we didn't have WiFi, user had to go to Settings
	 * Settings intent would return here, so send user back to try Authorize again
	 * @param resultCode Intent reuslt: OKAY or CANCELLED
	 * @param data extras to let us know we were asking for WiFi
	 * @param sharingFileName file to upload to Strava
	 */
	private void goBackToStravaShare(int resultCode, Intent data, String sharingFileName) {
		int extras = 0;
		if ((data != null) && data.hasExtra(KEY_AUTH_NO_NETWORK_INTENT)){
			extras = data.getExtras().getInt(KEY_AUTH_NO_NETWORK_INTENT);
		}
		if (resultCode == Activity.RESULT_OK && extras == 1) {
			Intent stravaUploadIntent = new Intent(this, StravaShareCB.class);
			stravaUploadIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			stravaUploadIntent.putExtra(UPLOAD_FILENAME, sharingFileName);
			startActivityForResult(stravaUploadIntent, UPLOAD_FILE_SEND_REQUEST_CODE);
		}
	}


	/**
	 * intermediate step to alert user if activity file will be closed
	 * If file to share is not the current output file, just proceed to sharingIntent
	 * @param uploadFilename user choice of file to share
	 */
	private void uploadFileSend(String uploadFilename) {
		//Log.i(logtag, "uploadFileSend()");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean showSharingAlert = settings.getBoolean(SHOW_SHARING, true);
		String sharingFileName = myBikeStat.fitLog.delTCXFITSuffix(myBikeStat.fitLog.stripFilePath(settings.getString(KEY_CHOSEN_TCXFILE, "")));
		String tcxLogFileName = myBikeStat.fitLog.delTCXFITSuffix(myBikeStat.fitLog.stripFilePath(myBikeStat.tcxLog.outFileName));
		if (tcxLogFileName.contains(sharingFileName) && showSharingAlert) {
			// warn user that a new activity will start
			doShowSharingAlert(uploadFilename);
		} else {
			doUploadIntent(uploadFilename);
		}
	}

	/**
	 * Now that we've closed the FileEncoder if we're sharing the current log file
	 * and the user has agreed to restart (if sharing current activity) let user choose how to share the file.
	 * We've made intent filters for RWGPS and Strava that we can intercept, or let user attach file to e-mail
	 * or upload to DropBox, Drive, etc. Those implicit actions are handled by those apps
	 * @param uploadFilename file to share
	 */
    private void doUploadIntent(String uploadFilename) {
		// Depending on where we're sending the file either use OAuth, an e-mail intent, etc
		//Log.i(logtag, "now doing upload Intent");

        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getApplicationContext()
                        .getPackageName() + ".provider", new File(uploadFilename));
        // Log.i(logtag, fileUri.toString());
        String bodyText = Constants.UPLOADING_NEW_FILE;
        String subjectText = myBikeStat.fitLog.stripFilePath(uploadFilename);
        Intent uploadFileIntent;
        uploadFileIntent = new Intent(Intent.ACTION_SEND);
        uploadFileIntent.setDataAndType(fileUri, "text/cbtype");
        uploadFileIntent.putExtra(UPLOAD_FILENAME, uploadFilename);
        uploadFileIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        uploadFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        uploadFileIntent.putExtra(Intent.EXTRA_EMAIL, RWGPS_EMAIL);
        uploadFileIntent.putExtra(Intent.EXTRA_SUBJECT, subjectText);
        uploadFileIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        startActivityForResult(Intent.createChooser(uploadFileIntent, getString(R.string.upload_file)), UPLOAD_FILE_SEND_REQUEST_CODE);
	}

	/**
	 * Give user a chance to cancel sharing because sharing current output file will close that file
	 * @param uploadFilename activity file to share
	 */
	private void doShowSharingAlert(final String uploadFilename) {
		View checkBoxView = View.inflate(this, R.layout.sharing_checkbox, null);
		CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(SHOW_SHARING, !isChecked).apply();
			}});
		checkBox.setText(R.string.dont_remind);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Set the dialog title
		builder.setTitle(R.string.sharing_alert)
				.setMessage(R.string.sharing_text)
				.setView(checkBoxView)

						// Set the action buttons
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK, so save the mSelectedItems results somewhere
						// or return them to the component that opened the dialog
						doUploadIntent(uploadFilename);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	/**
	 * when long-clicking a way point in the list, make sure it's close enough
	 * check myPlace.distanceTo(Way point at pos-position in list) < nearEnough
	 * set message either now navigating from..., or not close enough
	 */
	private boolean checkNearEnough(int pos) {
		if (pos >= myNavRoute.mergedRoute_HashMap.size()) {
			return false;
		}
		GPXRoutePoint tempRP;
		tempRP = myNavRoute.mergedRoute_HashMap.get(pos);
		Location loc = new Location(myPlace);
		loc.setLatitude(tempRP.lat);
		loc.setLongitude(tempRP.lon);
		double dist = myPlace.distanceTo(loc);
		String streetString = tempRP.getStreetName();
		String str = getString(R.string.now_navigating_from_) + streetString;
		boolean near = (dist < nearEnough);
		if (near) {
			Toast nearToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
			TextView v = nearToast.getView().findViewById(message);
			v.setTextColor(ContextCompat.getColor(context, R.color.gpsgreen));
			v.setTextSize(16);
			nearToast.show();
		} else {
			str = streetString + getString(R.string._is_not_close_enough);
			Toast nearToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
			TextView v = nearToast.getView().findViewById(message);
			v.setTextColor(ContextCompat.getColor(context, R.color.gpsred));
			v.setTextSize(16);
			nearToast.show();
		}
		return near;
	}

	private void openReopenTCX_FIT() {
		if (debugAppState) Log.i(logtag, "openReopenTCX_FIT()");
		//called from onLocationChanged, when firstLocation is true,
		// and in writeTrackRecord() if an error occurred
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
		myBikeStat.fitLog.purgeSmallActivityFiles(myBikeStat, settings.getString(KEY_CHOSEN_TCXFILE, ""));
        boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(myBikeStat.tcxLog.outFileName, getTCXFileAutoReset(context));
		myBikeStat.tcxLog.outFileFooterLength = settings.getInt(TCX_LOG_FILE_FOOTER_LENGTH, 1);
		// open a new tcx file if the previous one is old, we force a new one
		// thru reset, or loading a new route and clearing data, or the file was
		// not found when testing last modified date
        boolean locationTimestampOK = myBikeStat.getLastLocation().getTime() > Constants.JAN_1_2000;
        if ((old || forceNewTCX_FIT || !myBikeStat.tcxLog.getError().equals(""))
                && locationTimestampOK) {
			// compose filename using current date-time
			// need to do this before calling .fitLog.openNewFIT,
			// because the .fit file has the same name
			resetData();
			forceNewTCX_FIT = false;
			myBikeStat.tcxLog.outFileName = myBikeStat.tcxLog.composeTCXFileName();
			myBikeStat.tcxLog.openNewTCX(myBikeStat, myNavRoute);
			// open a new fit file as a background task because closing old fit file may take a while
            new ThreadPerTaskExecutor().execute(openNewFitFileBackgroundRunnable);
		} else if (locationTimestampOK){
			// not old and not forceNewTCX, so re-open tcx & fit
			// restore outfilefooterlength before re-opening
			myBikeStat.tcxLog.reopenTCX(myBikeStat, myNavRoute);
			// re-open the fit file
            new ThreadPerTaskExecutor().execute(reopenFitFileBackgroundRunnable);
		}
		editor.putString(TCX_LOG_FILE_NAME, myBikeStat.tcxLog.outFileName);
		editor.putInt(TCX_LOG_FILE_FOOTER_LENGTH, myBikeStat.tcxLog.outFileFooterLength).apply();
	}
    private Runnable openNewFitFileBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            String error = myBikeStat.fitLog.openNewFIT(myBikeStat);
        }
    };

    private Runnable reopenFitFileBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            String error = myBikeStat.fitLog.reOpenFitFile(myBikeStat.tcxLog.outFileName);
        }
    };

	private TimerTask testLocationCurrent;
	private final Handler locationWatchdogHandler = new Handler();
	private final Timer locationWatchdogTimer = new Timer();

	/** a watchdog timer to check if location is current and test permissions for Location and write */
    private void startLocationWatchdog() {
        // location watchdog
        testLocationCurrent = new TimerTask() {
            @Override
            public void run() {
                locationWatchdogHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        checkFusedLocationService();
                        checkPermissions();
                        checkLocCurrent();
                        writeAppMessage("", textColorWhite);
                    }// UI run

                    private void checkPermissions() {
                        if (debugAppState) { Log.i(logtag, "checkPermissions()"); }
                        // do we have Location Permission? If not, ask for location permission
                        // if so, and location is current, and we don't have write permission, ask for Write permission
                        boolean isLocationCurrent = isFusedLocationCurrent() || isGPSLocationCurrent();
                        if (askLocationPermission() && isLocationCurrent) {
                            askWritePermission();
                        }
                    }

                    private void checkFusedLocationService() {
                        if (askLocationPermission()) {
                            if (!Utilities.requestingLocationUpdates(context) && mLocationServiceBound) {
                                mLocationService.requestLocationUpdates();
                            }
                            if (debugLocation) {
                                Log.i(logtag, "checkFusedLocationService()"
                                        + (Utilities.requestingLocationUpdates(context) ? " are already requesting" : " not requesting"));
                            }
                        }
                    }

                    private void askWritePermission() {
                        if (Utilities.hasStoragePermission(getApplicationContext())) {
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE);
                        }
                        writeAppMessage(getString(R.string.write_permission_denied),
                                ContextCompat.getColor(context, R.color.gpsred));
                    }

                    /**
                     * used to indicate loss of GPS location data in the display speed will read
                     * XX.x, distance to way points will show ??
                     */
                    private void checkLocCurrent() {
                        if (debugAppState) { Log.i(logtag, "checkLocCurrent()"); }

                        testZeroPaused();
                        if (!isGPSLocationCurrent()) {
                            if (!isGPSLocationEnabled(getApplicationContext())
                                    && !mLocationSettingsSnackBar.isShown()){
                                mLocationSettingsSnackBar.setAction(getString(R.string.enable), new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResult(viewIntent, REQUEST_CHANGE_LOCATION_SETTINGS);
                                    }
                                }).show();
                            }
                            refreshScreen();
                            // user may have corrected Settings
                        } else if (mLocationSettingsSnackBar.isShown()) {
                            mLocationSettingsSnackBar.dismiss();
                        }
                    }
                });// locationWatchdog.post Runnable
            }//run in TimerTask
        };// TimerTask()

        locationWatchdogTimer.schedule(testLocationCurrent, ONE_SEC, THREE_SEC);
    }

	private void stopLocationWatchdog() {
        locationWatchdogHandler.removeCallbacksAndMessages(null);
        if (testLocationCurrent != null) {
			testLocationCurrent.cancel();
		}
	}
    // Monitors the state of the connection to the service.
    private final ServiceConnection mLocationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mLocationService = binder.getService();
            if (debugLocation){Log.e(logtag, "onLocationServiceConnected()");}
            mLocationServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
            mLocationServiceBound = false;
            Utilities.setRequestingLocationUpdates(context, false);
            if (debugLocation){Log.e(logtag, "onLocationServiceDisconnected()");}
        }
    };
}
