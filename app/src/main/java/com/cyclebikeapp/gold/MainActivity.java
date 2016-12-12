package com.cyclebikeapp.gold;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

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
import static com.cyclebikeapp.gold.Constants.ACTIVITY_FILE_TYPE;
import static com.cyclebikeapp.gold.Constants.APP_NAME;
import static com.cyclebikeapp.gold.Constants.KEY_AUTH_NO_NETWORK_INTENT;
import static com.cyclebikeapp.gold.Constants.BONUS_MILES;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_GPX_DIRECTORY;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_GPX_FILE;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_TCX_DIRECTORY;
import static com.cyclebikeapp.gold.Constants.CHOOSER_TYPE_TCX_FILE;
import static com.cyclebikeapp.gold.Constants.CURR_WP;
import static com.cyclebikeapp.gold.Constants.DEG_PER_BEARING_ICON;
import static com.cyclebikeapp.gold.Constants.DIALOG_ERROR;
import static com.cyclebikeapp.gold.Constants.DISTANCE_TYPE_METRIC;
import static com.cyclebikeapp.gold.Constants.DISTANCE_TYPE_MILE;
import static com.cyclebikeapp.gold.Constants.DOUBLE_ZERO;
import static com.cyclebikeapp.gold.Constants.EXCEPTION;
import static com.cyclebikeapp.gold.Constants.FILE_NOT_FOUND;
import static com.cyclebikeapp.gold.Constants.FIRST_LIST_ELEM;
import static com.cyclebikeapp.gold.Constants.FIT_ACTIVITY_TYPE;
import static com.cyclebikeapp.gold.Constants.FIVE_SEC;
import static com.cyclebikeapp.gold.Constants.FOOT;
import static com.cyclebikeapp.gold.Constants.FORMAT_1F;
import static com.cyclebikeapp.gold.Constants.FORMAT_3D;
import static com.cyclebikeapp.gold.Constants.FORMAT_3_1F;
import static com.cyclebikeapp.gold.Constants.FORMAT_4_1F;
import static com.cyclebikeapp.gold.Constants.GPX;
import static com.cyclebikeapp.gold.Constants.JAN_1_2000;
import static com.cyclebikeapp.gold.Constants.KEY_BEARING;
import static com.cyclebikeapp.gold.Constants.KEY_CHOOSER_CODE;
import static com.cyclebikeapp.gold.Constants.KEY_CHOSEN_GPXFILE;
import static com.cyclebikeapp.gold.Constants.KEY_CHOSEN_TCXFILE;
import static com.cyclebikeapp.gold.Constants.KEY_DIM;
import static com.cyclebikeapp.gold.Constants.KEY_DISTANCE;
import static com.cyclebikeapp.gold.Constants.KEY_FORCE_NEW_TCX;
import static com.cyclebikeapp.gold.Constants.KEY_STREET;
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
import static com.cyclebikeapp.gold.Constants.PREF_HI_VIZ;
import static com.cyclebikeapp.gold.Constants.PREF_SAVED_LOC_TIME;
import static com.cyclebikeapp.gold.Constants.QUESTION;
import static com.cyclebikeapp.gold.Constants.RC_SHOW_FILE_LIST;
import static com.cyclebikeapp.gold.Constants.REQUEST_CHECK_SETTINGS;
import static com.cyclebikeapp.gold.Constants.REQUEST_RESOLVE_ERROR;
import static com.cyclebikeapp.gold.Constants.ROUTE_DISTANCE_TYPE;
import static com.cyclebikeapp.gold.Constants.ROUTE_FILE_TYPE;
import static com.cyclebikeapp.gold.Constants.RWGPS_EMAIL;
import static com.cyclebikeapp.gold.Constants.SAVED_LAT;
import static com.cyclebikeapp.gold.Constants.SAVED_LON;
import static com.cyclebikeapp.gold.Constants.SHOW_SHARING;
import static com.cyclebikeapp.gold.Constants.SPEED_TRIPLE_X;
import static com.cyclebikeapp.gold.Constants.STATE_RESOLVING_ERROR;
import static com.cyclebikeapp.gold.Constants.TCX;
import static com.cyclebikeapp.gold.Constants.TCX_ACTIVITY_TYPE;
import static com.cyclebikeapp.gold.Constants.TCX_LOG_FILE_FOOTER_LENGTH;
import static com.cyclebikeapp.gold.Constants.TCX_LOG_FILE_NAME;
import static com.cyclebikeapp.gold.Constants.TEN_SEC;
import static com.cyclebikeapp.gold.Constants.THIRTY_SEC;
import static com.cyclebikeapp.gold.Constants.TMP_CB_ROUTE;
import static com.cyclebikeapp.gold.Constants.TP_DENSITY;
import static com.cyclebikeapp.gold.Constants.TRIP_DISTANCE;
import static com.cyclebikeapp.gold.Constants.TRIP_TIME;
import static com.cyclebikeapp.gold.Constants.TWENTYFOUR_HOURS;
import static com.cyclebikeapp.gold.Constants.UPLOAD_FILENAME;
import static com.cyclebikeapp.gold.Constants.UPLOAD_FILE_SEND_REQUEST_CODE;
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

/**
 * Copyright  2013, 2014 cyclebikeapp. All Rights Reserved.
 */

@SuppressLint({"DefaultLocale", "NewApi"})
public class MainActivity extends AppCompatActivity {

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
	private TextView tripDistLabel, tripDistTitle;
	private TextView avgSpeedLabel, maxSpeedLabel, gpsSpeedLabel;
	private TextView avgSpeedTitle, maxSpeedTitle, gpsSpeedTitle;
	private TextView tripTimeLabel, tripTimeTitle;
	private TextView appMessage;
    private Snackbar mLocationSettingsSnackBar;
	private Context context;
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
	/** use System time to indicate loss of GPS after 10 seconds */
	private long newLocSysTimeStamp;
    private int prefDistanceUnit;
    private int prefDistanceType;
    private int prefTextColor;
    private int prefBackgroundColor;
    private boolean prefIsColorSchemeHiViz = false;
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
	/** if last location is older than 10 seconds, this will be false and
	 // speedo display will show xx.x; miles to turns will show ?? */
	private boolean locationCurrent;
	/** alternate satAcq message with this switch */
	private boolean satAcqMess = true;
	/** only open new tcx and FIT file in Location Listener; use this tag to force a new tcx and fit
	 rather than re-open the old one */
	private boolean forceNewTCX_FIT = false;
	/** if we're resuming the route and loading a file, need a switch
	 to open a new or re-open the old tcx and file */
    private boolean resumingRoute = false;
    // all the Location functions
    private LocationHelper mLocationHelper;
	private PowerManager pm = null;
    private boolean hasWritePermission = false;
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
		turnByturnList = (ListView) findViewById(R.id.list);
		/* should the app keep the screen on - not if we're paused */
		turnByturnList.setKeepScreenOn(true);
		/* handles the scrolling action */
		turnByturnList.setOnScrollListener(scrollListener);
		/* responds to long-press in turn list */
		turnByturnList.setOnItemLongClickListener(longClickListener);
		context = getApplicationContext();
		textColorWhite = ContextCompat.getColor(context, R.color.white);
        textColorHiViz = ContextCompat.getColor(context, R.color.texthiviz);
		myBikeStat = new BikeStat(context);
		myNavRoute = new NavRoute(context);
		initializeScreen();
		initializeMyPlace();
        mLocationSettingsSnackBar = Snackbar.make(
                appMessage,
                getString(R.string.open_location_settings),
                Snackbar.LENGTH_INDEFINITE);
        // keep track of number of launches to nag user to upgrade
        Utilities.incrementLaunchNumber(context);
        // ANTSupportChecker will find if user's phone has ANT chip
        if (Utilities.canNagUser(context, AntSupportChecker.hasAntFeature(context))){
            upgradeNAG();
        }
        mLocationHelper = new LocationHelper(getApplicationContext());
        mLocationHelper.mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		autoResumeRoute();
	}// onCreate


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSensors();
                    writeAppMessage("", textColorWhite);
                    if (!mLocationHelper.mGoogleApiClient.isConnected()) {
                        // reconnect() will also do .startLocationUpdates() in onConnect() callback
                        mLocationHelper.mGoogleApiClient.reconnect();
                    } else {
                        mLocationHelper.stopLocationUpdates();
                        mLocationHelper.startLocationUpdates(mLocationHelper.createLocationRequest());
                    }
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
		boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(settings
				.getString(TCX_LOG_FILE_NAME, ""), getTCXFileAutoReset());
		if (!old) {
			if (debugAppState) Log.i(logtag, "autoResumeRoute() - tcx file is not old");
			restoreSharedPrefs();
			myNavRoute.mChosenFile = new File(chosenGPXFile);
			refreshScreen();
			resumingRoute = true;
			//load file in async task with progress bar in case file is big
			//it would generate ANR error
			LoadData task = new LoadData();
			task.execute();
			prefChanged = true;
		} else {//output file is old
			if (debugAppState) Log.i(logtag, "autoResumeRoute() - tcx file is old");
			// also delete all cached route files
			deleteAllTmpRouteFiles();
			resetData();
			//have to put this in shared prefs, or the old file name is loaded in onResume
			chosenGPXFile = "";
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

	private final OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
			// only respond to long-click if location is current & there is a route
			if ((myNavRoute.mergedRoute_HashMap.size() < 1) | !locationCurrent) {
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
		editor.putString(TCX_LOG_FILE_NAME, myBikeStat.tcxLog.outFileName);
		editor.putInt(TCX_LOG_FILE_FOOTER_LENGTH, myBikeStat.tcxLog.outFileFooterLength);
		editor.putBoolean(KEY_FORCE_NEW_TCX, forceNewTCX_FIT);
		editor.apply();
	}

    @Override
    protected void onResume() {
        if (debugAppState) {
            Log.i(logtag, "onResume()");
            String connectedText;
            if (mLocationHelper.mGoogleApiClient.isConnecting()) {
                connectedText = "connecting to GoogleApiClient";
            } else if (mLocationHelper.mGoogleApiClient.isConnected()) {
                connectedText = "connected to GoogleApiClient";
            } else {
                connectedText = "GoogleApiClient Status??";
            }
            Log.i(logtag, connectedText);
        }
        if (!mLocationHelper.mGoogleApiClient.isConnected()) {
            // reconnect() will also do .startLocationUpdates() in onConnect() callback
            mLocationHelper.mGoogleApiClient.reconnect();
        } else {
            mLocationHelper.stopLocationUpdates();
            mLocationHelper.startLocationUpdates(mLocationHelper.createLocationRequest());
        }
        //save the name of the route file temporarily until its validated
        prevChosenFile = chosenGPXFile;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        chosenGPXFile = settings.getString(KEY_CHOSEN_GPXFILE, "");
        myBikeStat.tcxLog.outFileName = settings.getString(TCX_LOG_FILE_NAME, "");
        myBikeStat.tcxLog.outFileFooterLength = settings.getInt(TCX_LOG_FILE_FOOTER_LENGTH, 1);
        //if we're returning from SettingsActivity, test if Track Point density
        // has changed. If so we must re-load chosenFile
        // see if SharedPreferences value of trackDensity is different than DefaultSharedPreferences
        int trackDensity = settings.getInt(KEY_TRACK_DENSITY, 0);
        myNavRoute.defaultTrackDensity = 0;
        SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String defTrackDensity = defaultSettings.getString(getResources()
                .getString(R.string.pref_trackpoint_density_key), ZERO);
        myNavRoute.defaultTrackDensity = Integer.valueOf(defTrackDensity);
        if (trackDensity != myNavRoute.defaultTrackDensity && myNavRoute.mergedRoute_HashMap.size() > 0) {
            // save RouteMiles @ firstListElem so we can recalculate firstListElem with new track density
            myNavRoute.routeMilesatFirstListElem = myNavRoute.mergedRoute_HashMap.get(
                    myNavRoute.firstListElem).getRouteMiles();
            editor.putInt(KEY_TRACK_DENSITY, myNavRoute.defaultTrackDensity);
            editor.apply();
            new ChangeTrackDensityBackground().execute();
            // save the route here in a background task
            new SaveRouteFileBackground().execute();
        }
        hasWritePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        prefIsColorSchemeHiViz = defaultSettings.getBoolean(PREF_HI_VIZ, false);
        if (prefIsColorSchemeHiViz) {
            prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_black);
            prefTextColor = textColorHiViz;
        } else {
            prefTextColor = textColorWhite;
            prefBackgroundColor = ContextCompat.getColor(context, R.color.bkgnd_gray);
        }
        prefDistanceUnit = Integer.parseInt(defaultSettings.getString(getResources().getString(R.string.pref_unit_key), ZERO));
        // distancePref = 0 for Route distance display; 1 for direct distance display
        prefDistanceType = Integer.parseInt(defaultSettings.getString(getResources().getString(R.string.pref_distance_key), ZERO));
        prefChanged = true;
        startSensors();
        super.onResume();
    }

    private void startSensors() {
        if (debugAppState) {
            Log.i(logtag, "startSensors()");
        }
        locationCurrent = false;
        stopLocationWatchdog();
        startLocationWatchdog();
    }

    @Override
    protected void onPause() {
        stopLocationWatchdog();
        super.onPause();
    }

    @Override
    protected void onStop() {
        saveState();
        super.onStop();
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
        mLocationHelper.stopLocationUpdates();
        mLocationHelper.mGoogleApiClient.disconnect();
	}

	@Override
	protected void onStart() {
		// This verification should be done during onStart() because the system
		// calls this method when the user returns to the activity, which
		// ensures the desired location provider is enabled each time the
		// activity resumes from the stopped state.
		super.onStart();
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

    private void upgradeNAG() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.upgrade_button_text,
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
        builder.setNeutralButton(R.string.remind_later,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 25; remind user after 25 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_LATER);
                    }
                });
        builder.setNegativeButton(R.string.not_now,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // set nagType to 10; remind user after 10 more launches
                        Utilities.setNagNum(context, Utilities.getNagNum(context) + NAG_TYPE_NOT_NOW);
                    }
                });
        // Set other dialog properties
        builder.setMessage(R.string.nag_message).setTitle(R.string.nag_title).show();
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
				task.execute(this);
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
		findViewById(R.id.action_share);
		mLayout = findViewById(R.id.RelativeLayout101);
		initHashMap();
		tripDistTitle = (TextView) findViewById(R.id.textView24);
		tripDistLabel = (TextView) findViewById(R.id.textView25);
		tripTimeTitle = (TextView) findViewById(R.id.textView38);
		tripTimeLabel = (TextView) findViewById(R.id.textView39);
		avgSpeedTitle = (TextView) findViewById(R.id.textView26);
		avgSpeedLabel = (TextView) findViewById(R.id.textView27);
		maxSpeedTitle = (TextView) findViewById(R.id.textView28);
		maxSpeedLabel = (TextView) findViewById(R.id.textView29);
		appMessage = (TextView) findViewById(R.id.textView40);
		gpsSpeedTitle = (TextView) findViewById(R.id.textView32);
		gpsSpeedLabel = (TextView) findViewById(R.id.textView33);
	}// initializeScreen()

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
		if (prefChanged) {
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
	}// refreshScreen()

	/**
	 * Read the user specified time to declare an activity file "old", so a new one can be opened
	 * @return the reset time (hours)
	 */
	private int getTCXFileAutoReset() {
		int[] resetTimes = { 2, 4, 6, 8 };
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String theString = sharedPref.getString(
				getResources().getString(R.string.pref_tcx_idle_time_key), "3");
		Integer tcxFileResetTimeIndex = Integer.parseInt(theString);
		return resetTimes[tcxFileResetTimeIndex];
	}

	private void refreshMergedRouteHashMap() {
		double distMultiplier = mile_per_meter;
		String unit;
		int j = turnByturnList.getFirstVisiblePosition();
		while ((j < routeHashMap.size())
				&& (j < myNavRoute.mergedRoute_HashMap.size())
				&& (j < turnByturnList.getLastVisiblePosition() + 1)) {
			HashMap<String, String> hmItem = new HashMap<>();
			float result[];
			result = distFromMyPlace2WPMR(j);
			// results returns in miles; convert to meters, if needed
			double distance = result[0] * distMultiplier;
			int turnDirIcon = myNavRoute.mergedRoute_HashMap.get(j).turnIconIndex;
			String streetName = myNavRoute.mergedRoute_HashMap.get(j).getStreetName();
			String distanceString;
			if (locationCurrent) {
				distanceString = String.format(FORMAT_1F, distance);
			} else {
                distanceString = QUESTION;
            }
			if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
				unit = MILE;
			} else {
				distMultiplier = km_per_meter;
				unit = KM;
			}
			if (distance < 0.1) {// switch to display in feet / m
				int dist;
				// increment in multiples of 20', a likely resolution limit
				if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
					dist = (int) Math.floor(distance * 264) * 20;
					unit = FOOT;
				} else {
					dist = (int) Math.floor(distance * 100) * 10;
					unit = METER;
				}
				if (locationCurrent) {
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
			if (prefIsColorSchemeHiViz) {
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
		if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
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
			if (prefIsColorSchemeHiViz) {
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
				result = distFromMyPlace2WPMR(i);
				// results returns in miles; convert to meters, if needed
				double distMultiplier = mile_per_meter;
				if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
					unit = MILE;
				} else {
					distMultiplier = km_per_meter;
					unit = KM;
				}
				double distance = result[0] * distMultiplier;
				if (locationCurrent) {
					distanceString = String.format(FORMAT_1F, distance);
				}
				if (distance < 0.1) {// switch to display in feet / m
					int dist;
					// increment in multiples of 20', a likely resolution limit
					if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
						dist = (int) Math.floor(distance * 264) * 20;
						unit = FOOT;
					} else {
						dist = (int) Math.floor(distance * 100) * 10;
						unit = METER;
					}
					if (locationCurrent) {
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
		if (prefDistanceUnit == DISTANCE_TYPE_MILE) {
			tripDistString = getResources().getString(R.string.trip_dist_mi);
			avgSpeedString = getResources().getString(R.string.avg_speed_mph);
			maxSpeedString = getResources().getString(R.string.max_speed_mph);
			gpsSpeedString = getResources().getString(R.string.curr_gps_speed_mph);
		} else {
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
        if (prefDistanceUnit == DISTANCE_TYPE_METRIC) {
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
        if (locationCurrent) {
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
        } else if (!Utilities.isGPSLocationEnabled(getApplicationContext())) {
            color = textColorWhite;
            shouldShowAppMessageBox = true;
            message = getResources().getString(R.string.open_location_settings);
        } else if (!locationCurrent) {
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

	private float[] distFromMyPlace2WPMR(int index) {
		// prefDistanceType = ROUTE_DISTANCE_TYPE for Route distance display; DIRECT_DISTANCE_TYPE for direct distance display
		float[] results;
		if (prefDistanceType == ROUTE_DISTANCE_TYPE) {
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

    class LocationHelper implements GoogleApiClient.OnConnectionFailedListener,
                                    GoogleApiClient.ConnectionCallbacks,
                                    com.google.android.gms.location.LocationListener {

        boolean mResolvingError;
        ConnectionResult connectionFailureResult;
        private Status locationSettingsResultStatus;
        final GoogleApiClient mGoogleApiClient;

        LocationHelper(Context context) {
            //test if GooglePlay Services is available and up to date
            googlePlayAvailable(context);
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mResolvingError = false;
        }

        /**
         * Use Google Location API to get a user location
         *
         * @param mLocationRequest specifies update interval and accuracy
         */
        void startLocationUpdates(LocationRequest mLocationRequest) {
            try {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (IllegalStateException ignore) {
            }

        }

        /**
         * Disable locationListener when Display Activity is destroyed
         */
        void stopLocationUpdates() {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }

        LocationRequest createLocationRequest() {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(ONE_SEC);
            mLocationRequest.setSmallestDisplacement(0f);
            mLocationRequest.setFastestInterval(ONE_SEC);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            return mLocationRequest;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                myPlace = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException ignore) {
            }
            if (myPlace != null) {
                Utilities.saveLocSharedPrefs(myPlace, context);
            } else {
                myPlace = Utilities.getLocFromSharedPrefs(context);
            }
            final LocationRequest mLocationRequest = createLocationRequest();
            final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            startLocationUpdates(mLocationRequest);
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location requests here.
                            try {
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                                        mLocationRequest, LocationHelper.this);
                            } catch (IllegalStateException e) {
                                // we connected, but something went wrong, try again
                                mGoogleApiClient.reconnect();
                                e.printStackTrace();
                            }
                            connectionFailureResult = null;
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            mLocationSettingsSnackBar = Snackbar.make(
                                    appMessage,
                                    getString(R.string.open_location_settings),
                                    Snackbar.LENGTH_INDEFINITE);
                            mLocationSettingsSnackBar.setAction(R.string.allow, new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        mLocationHelper.locationSettingsResultStatus.startResolutionForResult(
                                                MainActivity.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException ignore) {
                                    }
                                }
                            }).show();
                            locationSettingsResultStatus = status;
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            locationSettingsResultStatus = status;
                            break;
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {
            if (debugLocation) {
                Log.w(logtag, "ConnectionSuspended");
            }
        }

        @Override
        public void onLocationChanged(Location location) {

            if (debugLocation) {
                Log.w(logtag, "onLocationChanged(): long: " +
                        String.format("%7.5f", location.getLongitude())
                        + " lat: " + String.format("%7.5f", location.getLatitude())
                        + " alt: " + String.format("%3.1f", location.getAltitude())
                        + " accuracy: " + String.format("%4.1f",  (double) location.getAccuracy()));
            }
            // if location time-stamp is weird, skip the data
            // also demand "goodEnoughLocationAccuracy" before using the data
            if (location.getTime() < JAN_1_2000
                    || location.getAccuracy() > goodEnoughLocationAccuracy
                    || Math.abs(System.currentTimeMillis() - location.getTime()) >  TWENTYFOUR_HOURS) {
                return;
            }
            newLocSysTimeStamp = SystemClock.elapsedRealtime();
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
            locationCurrent = true;
            myBikeStat.gpsSpeedCurrent = true;
            myBikeStat.setSpeed(location.getSpeed());
            myBikeStat.setLastGoodWP(location, gpsFirstLocation);
            // this also calculates trip distance, ride time, etc
            myNavRoute.refreshRouteWayPoints(location, myBikeStat.getGPSTripDistance());
            // firstLocation is used to start the track record; it is true after a reset()
            if (gpsFirstLocation) {
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

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (debugLocation) {
                Log.w(logtag, "onConnectionFailed: (message) " + connectionResult.getErrorMessage());
                Log.w(logtag, "onConnectionFailed: (error) " + connectionResult.getErrorCode());
            }
            if (connectionResult.hasResolution() && !mResolvingError) {
                try {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(MainActivity.this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else if (!mResolvingError) {
                // Show dialog using GoogleApiAvailability.getErrorDialog()
                showErrorDialog(connectionResult.getErrorCode());
                mResolvingError = true;
            }
        }

        private boolean googlePlayAvailable(Context context) {
            int googlePlayAvailableResponse = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (googlePlayAvailableResponse == ConnectionResult.SUCCESS) {
                return true;
            } else {
                GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, googlePlayAvailableResponse, 0).show();
            }
            return false;
        }

        /* Creates a dialog for an error message */
        private void showErrorDialog(int errorCode) {
            try {
                // Create a fragment for the error dialog
                ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
                // Pass the error that should be displayed
                Bundle args = new Bundle();
                args.putInt(DIALOG_ERROR, errorCode);
                dialogFragment.setArguments(args);
                dialogFragment.show(getSupportFragmentManager(), "errordialog");
            } catch (IllegalStateException ignore) {
            }
        }

    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    private void onDialogDismissed() {
        mLocationHelper.mResolvingError = false;
        mLocationHelper.connectionFailureResult = null;
    }

    /**
     * test for paused condition so we won't write log entries, allow screen to dim, etc
     */
    private void testZeroPaused() {
        boolean paused = false;
        if (!locationCurrent
                || ((myBikeStat.getSpeed() < speedPausedVal) && (myNavRoute.getDeltaDOT() < dotPausedVal))) {
            paused = true;
        }
        myBikeStat.setPaused(paused);
    }

	private void writeTrackRecord() {

        if (!hasWritePermission) {
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
//			long startTime = System.nanoTime();
			locationWatchdogHandler.post(new Runnable() {
				@Override
				public void run() {
					//long startTime = System.nanoTime();
					myBikeStat.tcxLog.writeTCXRecord(myBikeStat, myNavRoute);
					myBikeStat.fitLog.writeRecordMesg(myBikeStat);
				}
			});

			if (!myBikeStat.fitLog.getError().equals("")){
				openReopenTCX_FIT();
			}
			// if file not open, try to re-open; getError() will return SD card error if !fileHasPermission
//			Log.i(logtag, "writeTrackRecord() takes " + String.format(FORMAT_4_3F, (System.nanoTime() - startTime) / 1000000000.) + " s");
			if (myBikeStat.tcxLog.getError().equals("")) {
				// no error, successfully wrote record
				myNavRoute.trackClosed = false;
			} else {
				forceNewTCX_FIT = true;
				openReopenTCX_FIT();
			}
		}
	}

	/**
	 * Whenever we close the fit file, the FileEncoder writes
	 * the fit file in a background task. When finished, set a flag that allows
	 * us to accept new data.
	 */
	public class CloseActivityFilesBackground extends AsyncTask<String, String, String> {
		ProgressDialog progressDialog;
		@Override
		protected void onPreExecute() {
			if (!MainActivity.this.isFinishing()) {
				progressDialog = ProgressDialog.show(MainActivity.this,
													 "Processing...", "Closing files...", false);
			}
			myBikeStat.fitLog.setFileEncoderBusy(true);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
            if (!MainActivity.this.isFinishing() && progressDialog != null){progressDialog.setMessage(values[0]);}
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
            if (!MainActivity.this.isFinishing()) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
			myBikeStat.fitLog.setFileEncoderBusy(false);
			if (!("").equals(sharingFileName)) {
				uploadFileSend(sharingFileName);
			}
		}
	}

	/**
	 * Whenever we re-open the fit file, have to parse the tcx file and re-write
	 * the fit file in a background task. When finished, set a flag that allows
	 * us to accept new data.
	 */
    private class OpenNewFitFileBackground extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// encode the .fit file once we've re-opened a tcx file
			// (chosenTCXFile will always have a .tcx suffix since we only show
			// tcx file types in ShowFileList chooser
			myBikeStat.fitLog.setFileEncoderBusy(true);
			String error = myBikeStat.fitLog.openNewFIT(myBikeStat);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// set a flag that says we've re-encoded the fit file and we're
			// ready to write new data
			myBikeStat.fitLog.setFileEncoderBusy(false);
		}
	}

	/**
	 * Whenever we re-open the fit file, have to parse the tcx file and re-write
	 * the fit file in a background task. When finished, set a flag that allows
	 * us to accept new data.
	 */
    private class ReopenFitFileBackground extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// encode the .fit file once we've re-opened a tcx file
			// (chosenTCXFile will always have a .tcx suffix since we only show
			// tcx file types in ShowFileList chooser
			myBikeStat.fitLog.setFileEncoderBusy(true);
			String error = myBikeStat.fitLog.reOpenFitFile(myBikeStat.tcxLog.outFileName);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// set a flag that says we've re-encoded the fit file and we're ready to write new data
			myBikeStat.fitLog.setFileEncoderBusy(false);
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

	/** once the route file has been loaded and prepared, save mergedRoute in private storage.
	 *  This could be a long task for a large route file, so put in background task */
    private class SaveRouteFileBackground extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			saveRouteFile(chosenGPXFile);
			return null;
		}

	}

	/** in revertChosen file, do the restore operation in background for faster response on UI
	 * In LoadData, want to wait for restoreFile() to finish before moving on to .loadNavRoute
	 *  This could be a long task for a large route file, so put in background task */
    private class RestoreRouteFileBackground extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			restoreRouteFile(chosenGPXFile);
			return null;
		}

	}

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
            new SaveRouteFileBackground().execute();
            refreshScreen();
        }
    }

    /**
     * use an asynchronous, background thread to load the file with a progress bar in
     * case it's a big file
     */
    public class LoadData extends AsyncTask<Context, String, Void> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            myNavRoute.setError("");
            //display the progress dialog
            progressDialog = ProgressDialog.show(MainActivity.this,
                    LOADING_FILE, LOOKING_FOR_ROUTE_DATA, false);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    progressDialog.dismiss();
                    myNavRoute.setError("user-canceled");
                }
            });
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected Void doInBackground(Context... params) {
            // See if we have this file cached; if so, load from cache
            if (!("").equals(restoreRouteFile(chosenGPXFile))) {
                // couldn't find the file in cache, so use the SAX parser via .loadNavRoute
                if (!("").equals(chosenGPXFile)) {
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
                if (myNavRoute.getError().equals("") && !("").equals(chosenGPXFile)) {
                    progressDialog.setCancelable(false);
                    publishProgress("Initializing Route");
                    myNavRoute.prepareRoute(getApplicationContext());
                }
            }
            return null;
        }

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//all UI altering tasks have to go in the post-execute method
			progressDialog.dismiss();
			if (!("").equals(chosenGPXFile)) {
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
			//TODO if there was no prevChosenFile set title to App_Name
			new RestoreRouteFileBackground().execute();
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
				boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(myBikeStat.tcxLog.outFileName, getTCXFileAutoReset());
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
			new SaveRouteFileBackground().execute();
		}
	}//LoadData class

	/**
	 * Bypass the SAX parser when restoring a route or changing TrackPoint density and load the route
	 * ArrayList from private file storage. Returns an error if the route is
	 * not in private storage; then we'll have to use the SAX parser
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
	 * Save the route ArrayList to private storage so we can bypass the SAX
	 * parser when restoring route or changing TrackPoint density. Saves some time when using a big
	 * Trackpoint file
	 * @param fileName the route file
	 **/
	private void saveRouteFile(String fileName) {
		// add prefix denoting track point density and removing path characters
		fileName = adjustFileName(fileName);
		String[] routeFiles = fileList();
		boolean fileAlreadyExists = false;
		for (String file : routeFiles) {
			if (file.equals(fileName)) {
				fileAlreadyExists = true;
				break;
			}
		}
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

	/**
	 * delete path prefix; private storage names can't have path symbols,
	 * filename returned from ShowFileList has path characters in it
	 *
	 * @param fileName the route filename to be stored
	 * @return the adjusted filename without path and a unique prefix and suffix
	 */
	private String adjustFileName(String fileName) {

		if (fileName != null) {
			int start = fileName.lastIndexOf("/") + 1;
			int end = fileName.length();
			if ((end - start) <= 0) {
				fileName = "";
			} else {
				fileName = fileName.substring(start, end);
			}
		}
		// add prefix denoting track point density from Settings Preferences
		SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(context);
		String defTrackDensity = defaultSettings.getString(context.getResources().getString(R.string.pref_trackpoint_density_key), "0");
		return TP_DENSITY + defTrackDensity + fileName + TMP_CB_ROUTE;
	}

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
							new LoadData().execute(this);
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
						if (readActivityFileType() == Integer.valueOf(FIT_ACTIVITY_TYPE)) {
							// replace the suffix to indicate a fit file instead of a tcx file
							sharingFileName = myBikeStat.fitLog.delTCXFITSuffix(mChosenTCXFile) + ".fit";
						}
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
		} else if (requestCode == REQUEST_RESOLVE_ERROR) {
            mLocationHelper.mResolvingError = false;
            mLocationHelper.connectionFailureResult = null;
            // Make sure the app is not already connected or attempting to connect
            if (!mLocationHelper.mGoogleApiClient.isConnecting() &&
                    !mLocationHelper.mGoogleApiClient.isConnected()) {
                mLocationHelper.mGoogleApiClient.connect();
            }
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

		return readActivityFileType() == Integer.valueOf(FIT_ACTIVITY_TYPE)
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
	 * Read user preference for type of activity file
	 * @return an integer indicating activity file type 0 = .tcx file, 1 = .fit file
	 */
	private int readActivityFileType() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String theString = sharedPref.getString(
				getResources().getString(R.string.pref_activity_file_key), TCX_ACTIVITY_TYPE);
		Integer activityFileType = Integer.parseInt(theString);
		if (debugAppState) Log.i(logtag, "activityFileType: " + activityFileType);
		return activityFileType;
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
		Uri fileUri = Uri.fromFile(new File(uploadFilename));
//		Log.i(logtag, fileUri.toString());
		String bodyText = "Uploading new file";
		String subjectText = myBikeStat.fitLog.stripFilePath(uploadFilename);
		Intent uploadFileIntent;
		uploadFileIntent = new Intent(Intent.ACTION_SEND);
		uploadFileIntent.putExtra(UPLOAD_FILENAME, uploadFilename);
		uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		uploadFileIntent.putExtra(Intent.EXTRA_EMAIL, RWGPS_EMAIL);
		uploadFileIntent.putExtra(Intent.EXTRA_SUBJECT, subjectText);
		uploadFileIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		uploadFileIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
		uploadFileIntent.setType("text/cbtype");
		startActivityForResult(Intent.createChooser(uploadFileIntent, getString(R.string.upload_file)), UPLOAD_FILE_SEND_REQUEST_CODE);
	}

	/**
	 * Give user a chance to cancel sharing because sharing current output file will close that file
	 * @param uploadFilename activity file to share
	 */
	private void doShowSharingAlert(final String uploadFilename) {
		View checkBoxView = View.inflate(this, R.layout.sharing_checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
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
			TextView v = (TextView) nearToast.getView().findViewById(message);
			v.setTextColor(ContextCompat.getColor(context, R.color.gpsgreen));
			v.setTextSize(16);
			nearToast.show();
		} else {
			str = streetString + getString(R.string._is_not_close_enough);
			Toast nearToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
			TextView v = (TextView) nearToast.getView().findViewById(message);
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
		boolean old = myBikeStat.tcxLog.readTCXFileLastModTime(myBikeStat.tcxLog.outFileName, getTCXFileAutoReset());
		myBikeStat.tcxLog.outFileFooterLength = settings.getInt(TCX_LOG_FILE_FOOTER_LENGTH, 1);
		// open a new tcx file if the previous one is old, we force a new one
		// thru reset, or loading a new route and clearing data, or the file was
		// not found when testing last modified date
		if (old || forceNewTCX_FIT || !myBikeStat.tcxLog.getError().equals("")) {
			if (debugAppState) Log.i(logtag, "openReopenTCX_FIT() - file old or forceNew");
			// compose filename using current date-time
			// need to do this before calling .fitLog.openNewFIT,
			// because the .fit file has the same name
			resetData();
			forceNewTCX_FIT = false;
			myBikeStat.tcxLog.outFileName = myBikeStat.tcxLog.composeTCXFileName();
			myBikeStat.tcxLog.openNewTCX(myBikeStat, myNavRoute);
			// open a new fit file as a background task because closing old fit file may take a while
			new OpenNewFitFileBackground().execute();
		} else {
			if (debugAppState) Log.i(logtag, "openReopenTCX_FIT() - file not old & not forceNew");
			// not old and not forceNewTCX, so re-open tcx & fit
			// restore outfilefooterlength before re-opening
			myBikeStat.tcxLog.reopenTCX(myBikeStat, myNavRoute);
			// re-open the fit file
			new ReopenFitFileBackground().execute();
		}
		editor.putString(TCX_LOG_FILE_NAME, myBikeStat.tcxLog.outFileName);
		editor.putInt(TCX_LOG_FILE_FOOTER_LENGTH, myBikeStat.tcxLog.outFileFooterLength).apply();
	}

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
                        checkPermissions();
                        checkLocCurrent();
                        writeAppMessage("", textColorWhite);
                    }// UI run

                    private void checkPermissions() {
                        // do we have Location Permission? If not, ask for location permission
                        // if so, and location is current, and we don't have write permission, ask for Write permission
                        if (askLocationPermission()
                                && (SystemClock.elapsedRealtime() - newLocSysTimeStamp) < TEN_SEC) {
                            askWritePermission();
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

                    private boolean askLocationPermission() {
                        if (Utilities.hasFineLocationPermission(getApplicationContext())) {
                            return true;
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                            writeAppMessage(getString(R.string.loc_permission_denied),
                                    ContextCompat.getColor(context, R.color.gpsred));
                            return false;
                        }
                    }

                    /**
                     * used to indicate loss of GPS location data in the display speed will read
                     * XX.x, distance to way points will show ??
                     */
                    private void checkLocCurrent() {
                        locationCurrent = true;
                        if ((SystemClock.elapsedRealtime() - newLocSysTimeStamp) > TEN_SEC) {
                            locationCurrent = false;
                            refreshScreen();
                            // try reconnecting, or restarting LocationUpdates to check on Location Settings
                            // if user has not responded to snackbar, don't try to reconnect
                            if ((SystemClock.elapsedRealtime() - newLocSysTimeStamp) > THIRTY_SEC
                                    && !mLocationSettingsSnackBar.isShown()) {
                                mLocationHelper.mGoogleApiClient.reconnect();
                            }
                            // user may have corrected Settings
                        } else if (mLocationSettingsSnackBar.isShown()) {
                            mLocationSettingsSnackBar.dismiss();
                        }
                    }

                });// locationWatchdog.post Runnable
            }//run in TimerTask
        };// TimerTask()
        // wait 5 sec before starting this take to give recalc TrackPoint density time to finish
        //so turn-by-turn list doesn't jump
        locationWatchdogTimer.schedule(testLocationCurrent, FIVE_SEC, FIVE_SEC);
    }

	private void stopLocationWatchdog() {
		if (testLocationCurrent != null) {
			testLocationCurrent.cancel();
		}
	}

}
