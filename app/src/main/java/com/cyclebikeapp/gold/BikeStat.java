package com.cyclebikeapp.gold;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;

import java.util.concurrent.TimeUnit;

import static com.cyclebikeapp.gold.Constants.MAX_GPS_SPEED;
import static com.cyclebikeapp.gold.Constants.PREFS_NAME;
import static com.cyclebikeapp.gold.Constants.TIME_STR_FORMAT;
import static com.cyclebikeapp.gold.Constants.msecPerSec;

/*
 * Copyright  2013 cyclebikeapp. All Rights Reserved.
*/


class BikeStat {
    private final String tripTimeString;
	/** in m */
	private double gpsTripDistance;
	/** speed value from ANT speed-cadence sensor in meters per sec */
	private double speed = 0.;
	/** in meters per sec by dividing tripDistance by tripTime */
	private double avgSpeed;
	/** in meters per sec */
	private double maxSpeed;
    /** is gps Speed current */
    boolean gpsSpeedCurrent;
	/** current bike Location (Latitude, Longitude, Altitude, time) */
	private Location lastGoodWP = new Location(LocationManager.GPS_PROVIDER);
	/** previous bike Location */
	private Location prevGoodWP = new Location(LocationManager.GPS_PROVIDER);

	/** Time in seconds since the current trip started */
	private double gpsRideTime;
	/** if we're paused, allow screen to dim, write app message
	calculate DOT using magnetic sensor and don't increment the ride time clock */
	private boolean paused = true;

	/** this will be the tcx log file */
    final TCXLogFile tcxLog;
	/** this will be the fit log file */
    final FITLogFile fitLog;
    private SharedPreferences.Editor editor;
    private final String logtag = this.getClass().getSimpleName();
    private int satellitesInUse = 0;
    private float locationAccuracy;
    private boolean GPSCellNeeded;
    private boolean GPSWiFiNeeded;
    Iterable<GpsSatellite> satellites;
    boolean isNetworkEnabled;
    boolean isGPSEnabled;
    private boolean networkCellNeeded;
    private boolean networkWiFiNeeded;
    GnssStatus gpsSatelliteStatus;
    // use System time to indicate loss of GPS after 3 seconds
    long newFusedLocSysTimeStamp;
    long newGPSLocSysTimeStamp;

    /**
	 * BikeStat contains all bike related information: trip distance, time,
	 * speeds, and control access to the log file
	 * @param context is the main activity context
	 */
    @SuppressLint("CommitPrefEdits")
    BikeStat(Context context) {
        gpsSatelliteStatus = null;
        GPSCellNeeded = false;
        GPSWiFiNeeded = false;
        networkCellNeeded = false;
        networkWiFiNeeded = false;
        isGPSEnabled = false;
        isNetworkEnabled = false;
		gpsRideTime = 0.4;
		tcxLog = new TCXLogFile(context);
		fitLog = new FITLogFile(context);
        editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        tripTimeString = context.getString(R.string.tripTimeStr);
    }
	
	/** the newLocation Handler should call this routine with the new position data.
	 *  If this is the first location of this Trip, put the new Location in both lastGoodWP & prevGoodWP 
	 * @param firstLocation true if this is the first location of the trip
	 * @param myPlace the new Location data
	 * */
	void setLastGoodWP(Location myPlace, boolean firstLocation) {
		if (firstLocation) {
            lastGoodWP = myPlace;
            lastGoodWP.setTime(myPlace.getTime() + 2);
			prevGoodWP = myPlace;
			prevGoodWP.setTime(myPlace.getTime() + 1);
		} else {
			// swap lastGoodWP into prevGoodWP and copy newLocation to lastGoodWP
			prevGoodWP = lastGoodWP;
			lastGoodWP = myPlace;
			calcTripDistSpeed();
		}
		setGpsSpeed(myPlace.getSpeed());
        gpsSpeedCurrent = true;
        locationAccuracy = myPlace.getAccuracy();

	}
	
/** given a new, valid Location re-calculate all the fields affected by the new location measurement; time is in seconds
 * */
private void calcTripDistSpeed() {
		float[] results = {0};
		//distanceBetween returns in meters
		Location.distanceBetween(lastGoodWP.getLatitude(), lastGoodWP.getLongitude(),
				prevGoodWP.getLatitude(), prevGoodWP.getLongitude(), results);
		double deltaDistance = (double) results[0];

		double deltaTime = Math.abs(lastGoodWP.getTime() - prevGoodWP.getTime()) / msecPerSec;
		if (deltaTime < 0.001)
			deltaTime = .05;
		if (!isPaused()) {
			gpsTripDistance += deltaDistance;
			gpsRideTime += deltaTime;
			//save in sharedPrefs
			editor.putString(Constants.TRIP_TIME, Double.toString(getGPSRideTime()));
			editor.putString(Constants.TRIP_DISTANCE, Double.toString(getGPSTripDistance()));
			editor.putString(Constants.MAX_SPEED, Double.toString(getMaxSpeed()));
			editor.apply();
		}
		avgSpeed = gpsTripDistance / gpsRideTime;
		// rideTime was initialized to .4 sec to prevent / zero errors
		if (!isPaused()) {
			if (getSpeed() < MAX_GPS_SPEED && getSpeed() > maxSpeed) {
				maxSpeed = getSpeed();
			}
		} else {//paused, set speed to 0
			setSpeed(0);
		}
	}

	public void reset() {
		this.gpsTripDistance = 0.0;
		this.gpsRideTime = 0.1;
		this.maxSpeed = 0.0;
		this.avgSpeed = 0.0;
		this.setSpeed(0.);
	}

	double getGPSTripDistance() { return gpsTripDistance; }

	double getAvgSpeed() {
		return avgSpeed;
	}

	double getMaxSpeed() {
		return maxSpeed;
	}

	Location getLastGoodWP() {
		return lastGoodWP; // a pointer to this Location
	}

	double getGPSRideTime() {
		return gpsRideTime;
	}

	/** method to display trip time as a string hours:minutes:seconds */
    @SuppressLint("DefaultLocale")
	String getTripTimeStr() {
        long longTime = (long) gpsRideTime;
        int timeDay = (int) TimeUnit.SECONDS.toDays(longTime);
        int timeHours = (int) TimeUnit.SECONDS.toHours(longTime);
        int timeMinutes = (int) TimeUnit.SECONDS.toMinutes(longTime);

        int hours = timeHours - (timeDay * 24);
        int minutes = timeMinutes - (timeHours * 60);
        int seconds = (int) TimeUnit.SECONDS.toSeconds(longTime) - (timeMinutes * 60);
        return String.format(tripTimeString,
                String.format(TIME_STR_FORMAT, hours),
                String.format(TIME_STR_FORMAT, minutes),
                String.format(TIME_STR_FORMAT, seconds));
	}

	void setGPSTripTime(double d) {
		this.gpsRideTime = d;
	}

	void setGPSTripDistance(double d) {
		this.gpsTripDistance = d;
	}

	void setMaxSpeed(double d) {
		this.maxSpeed = d;
	}

	boolean isPaused() {
		return paused;
	}

	void setPaused(boolean paused) {
		this.paused = paused;
	}

	Location getLastLocation() {
		return lastGoodWP;
	}

	void setSpeed(double speed) {
		this.speed = speed;
	}

	double getSpeed() {
		return speed;
	}

	private void setGpsSpeed(double speed) {
		this.speed = speed;
	}
    void setSatellitesInUse(int satellitesInUse) {
        this.satellitesInUse = satellitesInUse;
    }

    float getLocationAccuracy() {
        return locationAccuracy;
    }

    int getSatellitesInUse() {
        return satellitesInUse;
    }

    boolean getGPSCellNeeded() {
        return GPSCellNeeded;
    }

    boolean getGPSWiFiNeeded() {
        return GPSWiFiNeeded;
    }

    public void setGPSCellNeeded(boolean GPSCellNeeded) {
        this.GPSCellNeeded = GPSCellNeeded;
    }

    public void setGPSWiFiNeeded(boolean GPSWiFiNeeded) {
        this.GPSWiFiNeeded = GPSWiFiNeeded;
    }

    public void setNetworkCellNeeded(boolean networkCellNeeded) {
        this.networkCellNeeded = networkCellNeeded;
    }

    public void setNetworkWiFiNeeded(boolean networkWiFiNeeded) {
        this.networkWiFiNeeded = networkWiFiNeeded;
    }

    public boolean isNetworkCellNeeded() {
        return networkCellNeeded;
    }

    public boolean isNetworkWiFiNeeded() {
        return networkWiFiNeeded;
    }

}
