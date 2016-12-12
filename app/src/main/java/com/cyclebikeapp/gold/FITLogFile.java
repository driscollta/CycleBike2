package com.cyclebikeapp.gold;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.Event;
import com.garmin.fit.EventType;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.LapMesg;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.cyclebikeapp.gold.Constants.FIT;
import static com.cyclebikeapp.gold.Constants.FIT_MANUFACTURER_DEVELOPMENT;
import static com.cyclebikeapp.gold.Constants.FIT_RUNTIME_EXCEPTION;
import static com.cyclebikeapp.gold.Constants.FORMAT_4_3F;
import static com.cyclebikeapp.gold.Constants.IO_EXCEPTION;
import static com.cyclebikeapp.gold.Constants.PARSER_CONFIG;
import static com.cyclebikeapp.gold.Constants.TCX;
import static com.cyclebikeapp.gold.Constants.semicircle_per_degrees;

@SuppressWarnings("ResultOfMethodCallIgnored")
class FITLogFile {

	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()
			+"/Android/data/com.cyclebikeapp/files/";
    private FileEncoder encode;
	private String error = "";
	private String sdError = "";
	private final MyTCXSAXHandler tcxSAXHandler = new MyTCXSAXHandler();
	private final Context context;
	private DateTime activityEndTime;
	private DateTime activityStartTime;
	private float totalTimerTime;
	private float totalDistance;
	private float avgSpeed;
	private float maxSpeed;
	private float avgCadence;
	private float maxCadence;
	private float avgHeartRate;
	private float maxHeartRate;
	private boolean fileEncoderBusy = false;
	
	FITLogFile(Context context) {
		this.context = context;
		initSummaryValues();
	}
		
	private void initSummaryValues() {
		activityEndTime = new DateTime(0);
		activityStartTime = new DateTime(0);
		totalTimerTime = 0;
		totalDistance = 0;
		avgSpeed = 0;
		maxSpeed = 0;
		avgCadence = 0;
		maxCadence = 0;
		avgHeartRate = 0;
		maxHeartRate = 0;		
	}

	/**
	 * When opening a new activity file, use the first Location from BikeStat to
	 * open a new fit file. Assume the tcx file has just been opened and the
	 * outFileName has been composed with the new timestamp.
	 * 
	 * @param bs BikeStat class contains all the data
	 *
	 * @return any error from opening a new FileEncoder
	 */

    String openNewFIT(BikeStat bs) {
		//Log.i(this.getClass().getName(), "openNewFIT()");
		setError("");
		//reset the summary
		initSummaryValues();
		// retrieve the new tcx file name
		String fitOutFileName = bs.tcxLog.outFileName;
		// get the new data record. We don't open a new file
		// until there is a new record from Location Listener
		Location tempLoc = bs.getLastLocation();
		boolean fileHasPermission = updateExternalStorageState();
		if (fileHasPermission) {
			try {				
				// replace the ".tcx" with ".fit"
				fitOutFileName = stripFilePath(delTCXFITSuffix(fitOutFileName) + FIT);
				// get tcx filepath
				File tcxDir = new File(Environment.getExternalStorageDirectory()
						+ "/Android/data/com.cyclebikeapp/files/");
                if (!tcxDir.exists()) {
                    tcxDir.mkdirs();
                }
                fileEncoderBusy = true;
                encode = new FileEncoder(new File(tcxDir, fitOutFileName));
			} catch (FitRuntimeException e) {
				setError("Error opening file " + fitOutFileName);
				e.printStackTrace();
                fileEncoderBusy = false;
				return getError();
			}
			activityStartTime = new DateTime(tempLoc.getTime()/1000 - DateTime.OFFSET/1000);
			activityEndTime = activityStartTime;
			// compose FileIdMesg and place it at the start of the fit file 
			// add first RecordMesgs from BikeStat new location
			try {
				encode.write(composeFileIdMesg(activityStartTime)); // Encode the FileIDMesg
			} catch(FitRuntimeException e) {
				setError(FIT_RUNTIME_EXCEPTION);
                fileEncoderBusy = false;
			}
            fileEncoderBusy = false;
			writeRecordMesg(bs);					
		} else {
			setError(getSDError());
		}
		return error;		
	}

	/**
	 * When re-opening an activity file, read in the data from the tcx file and
	 * write over the old fit file. A fit file cannot just be appended. Returns any error
	 * 
	 * @param outFileName
	 *            is the name of the activity file in tcx format. String
	 *            includes the entire path.
	 * @return any error from parsing the tcx file
	 */
    String reOpenFitFile(String outFileName) {
		String error;
		File appFiles = new File(pathName);
        try {
            if (!appFiles.exists()) {
                appFiles.mkdirs();
            }
        } catch (SecurityException ignore) {
        }		error = parseTCXFile(outFileName);
		//write summary values from tcxSAXHandler
		writeSummaryValuesFromParsing();
		// encode all the RecordMesg's from the tcxSAX handler
        fileEncoderBusy = true;
		encodeFITLogFileFromTCX(outFileName);
        fileEncoderBusy = false;
		return error;
	}

	/**
	 * When re-opening an activity file, read in the data from the tcx file and
	 * write over the old fit file. A fit file cannot just be appended. Use SAX
	 * parser to read the given .tcx file and extract data to an ArrayList of
	 * RecordMesg; returns any error
	 * 
	 * @param outFileName
	 *            is the name of the activity file in tcx format. 
	 * @return any error from parsing the tcx file
	 */
	private String parseTCXFile(String outFileName) {		
		long timingStartTime = System.nanoTime();
		setError("");
		BufferedReader r = null;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			xr.setContentHandler(tcxSAXHandler);
			xr.setErrorHandler(tcxSAXHandler);
			// uses the entire path, not just filename
			r = new BufferedReader(new FileReader(new File(pathName, outFileName)));
			xr.parse(new InputSource(r));
		} catch (SAXException e) {
			e.printStackTrace();
			setError(context.getString(R.string.invalid_file_) + context.getString(R.string._saxexception));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			setError(context.getString(R.string.file_not_found) + " " + outFileName);
		} catch (IOException e) {
			e.printStackTrace();
			setError(context.getString(R.string.invalid_file_) + IO_EXCEPTION);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			setError(context.getString(R.string.invalid_file_) + PARSER_CONFIG);
		} catch (NumberFormatException e) {
			setError(context.getString(R.string.invalid_file_)
					+ context.getString(R.string._this_file_has_lat_long_errors));
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // finally
		if (MainActivity.debugFITFile) {
			Log.d(this.getClass().getName(), "parseTCXLogFile - # records: " + tcxSAXHandler.handlersRecordMesgs.size());
			Log.d(this.getClass().getName(), "parseTCXLogFile - duration: " + String.format(FORMAT_4_3F, (System.nanoTime() - timingStartTime) / 1000000000.) + " s");
		}
		return error;
	}

	/**
	 * When re-opening the fit file, update the summary values so they can be
	 * written when the file is closed. The data comes from the tcxSAXHandler's
	 * summary data
	 */
	private void writeSummaryValuesFromParsing() {		
		totalTimerTime = tcxSAXHandler.totalTimerTime;
		totalDistance = tcxSAXHandler.totalDistance;
		avgSpeed = tcxSAXHandler.avgSpeed;
		maxSpeed = tcxSAXHandler.maxSpeed;
		avgCadence = tcxSAXHandler.avgCadence;
		maxCadence = tcxSAXHandler.maxCadence;
		avgHeartRate = tcxSAXHandler.avgHeartRate;
		maxHeartRate = tcxSAXHandler.maxHeartRate;
	}

	/**
	 * Write all information to the actual .fit output file from parsing the tcx
	 * file. This is done when re-opening the tcx file on restarting the app if
	 * the tcx file isn't old
	 * 
	 * @param fitOutFileName
	 *            is the name of the tcx activity file
	 */
	private void encodeFITLogFileFromTCX(String fitOutFileName) {
		//Log.i(this.getClass().getName(), "encodeFITLogFileFromTCX() - " + "fitOutFileName: " + fitOutFileName);
		// find startTime from timestamp of 1st trackPoint, end time from last record
		// then compose fileIdMesg
		//Then do encode.write fileIdMesg
		//return if no data in handlersRecordMesgs from parsing
		long timingStartTime = System.nanoTime();
		// test file storage
		setError("");
		boolean fileHasPermission = updateExternalStorageState();
		if (fileHasPermission) {

			try {
				// replace the ".tcx" with ".fit"
				fitOutFileName = stripFilePath(delTCXFITSuffix(fitOutFileName) + FIT);
				//Log.i(this.getClass().getName(), "encodeFITLogFileFromTCX() - " + "mod fitOutFileName: " + fitOutFileName);
				// get tcx filepath from shared prefs, if null, use default
				File appFiles = new File(pathName);
                if (!appFiles.exists()) {
                    appFiles.mkdirs();
                }
                encode = new FileEncoder(new java.io.File(pathName, fitOutFileName));
			} catch (FitRuntimeException e) {
				setError("Error opening file " + fitOutFileName);
				e.printStackTrace();
				return;
			}
			if (tcxSAXHandler.handlersRecordMesgs == null
					|| tcxSAXHandler.handlersRecordMesgs.size() == 0) {
				//Log.i(this.getClass().getName(), "encodeFITLogFileFromTCX() - no records in tcx file");
				return;
			}
			setActivityStartTime(tcxSAXHandler.handlersRecordMesgs.get(0));
			setActivityEndTime(tcxSAXHandler.handlersRecordMesgs.get(tcxSAXHandler.handlersRecordMesgs.size()-1));
			// compose FileIdMesg and place it at the start of the fit file 
			// add RecordMesgs from parsing tcx file
			try {
				encode.write(composeFileIdMesg(activityStartTime)); // Encode the FileIDMesg
			} catch(FitRuntimeException e) {
				setError(FIT_RUNTIME_EXCEPTION);
			}
			encodeFITRecordsFromParsing(tcxSAXHandler.handlersRecordMesgs);
			// encode.write(composeDeviceIdMesg(activityStartTime));
			if (MainActivity.debugFITFile) {
				Log.d(this.getClass().getName(), "encodeFITLogFile - # records: " + tcxSAXHandler.handlersRecordMesgs.size());
				Log.d(this.getClass().getName(), "encodeFITLogFile - duration: " + String.format(FORMAT_4_3F, (System.nanoTime() - timingStartTime) / 1000000000.) + " s");
			}
		} else {
			setError(getSDError());
		}
	}

	/**
	 * Encode all the RecordMesgs to the fit file when reopening the tcx file.
	 * 
	 * @param handlersRecordMesgs
	 *            is an ArrayList of RecordMesg received from the SAXParser
	 */
	private void encodeFITRecordsFromParsing(ArrayList<RecordMesg> handlersRecordMesgs) {
		try {
			for (RecordMesg mRecordMesg : handlersRecordMesgs) {
				encode.write(mRecordMesg);
			}
		} catch(FitRuntimeException e) {
			setError(FIT_RUNTIME_EXCEPTION);
		}
	}

	/**
	 * Write data values from BikeStat and the new Location to a RecordMesg that
	 * can be written to the fit file
	 * 
	 * @param bs
	 *            the BikeStat structure containing the new data
	 */
    void writeRecordMesg(BikeStat bs) {
		// Location could get events before we've opened a new fit file, or re-opended and parsed fit file
		// ignore attempts to write until encode FileEncoder has been initialized
		if (encode == null || isFileEncoderBusy()) {
			return;
		}
		//write summary values like TripTime, TripDistance, averages. maximums every time
		RecordMesg aRecordMesg;
		aRecordMesg = composeRecordMesg(bs);
		setActivityEndTime(aRecordMesg);
		saveSummaryValuesFromNewRecord(bs);
		try {
			encode.write(aRecordMesg);			
		} catch(FitRuntimeException e) {
			setError(FIT_RUNTIME_EXCEPTION);
		}
	}

	/**
	 * Compose data values from BikeStat and the new Location to a RecordMesg that
	 * can be written to the fit file
	 * 
	 * @param bs
	 *            the BikeStat structure containing the new data
	 */
	private RecordMesg composeRecordMesg(BikeStat bs) {
		RecordMesg aRecordMesg = new RecordMesg();
		Location tempLoc = bs.getLastGoodWP();
		// write time
		int time = (int) (tempLoc.getTime() / 1000 - DateTime.OFFSET / 1000);
		aRecordMesg.setTimestamp(new DateTime(time));
		// write position
		int latSC = (int) Math.round(tempLoc.getLatitude() * semicircle_per_degrees);
		aRecordMesg.setPositionLat(latSC);
		int lonSC = (int) Math.round(tempLoc.getLongitude() * semicircle_per_degrees);
		aRecordMesg.setPositionLong(lonSC);
		// write altitude
		aRecordMesg.setAltitude((float) tempLoc.getAltitude());
		// write distance
		aRecordMesg.setDistance((float) bs.getGPSTripDistance());
		aRecordMesg.setSpeed((float) bs.getSpeed());
		return aRecordMesg;
	}

	/**
	 * When leaving the main activity, close the fit file so it will be valid.
	 * This includes writing all the summary messages
	 */
	String closeFitFile() {
		// if encode is null, just return
		if (encode == null) {
			//Log.w(this.getClass().getName(), "closeFitFile() - encode null");
			return "encode null";
		}
		try {
			encode.write(composeFITLapMesg());
			encode.write(composeFITSessionMesg());
			encode.write(composeFITActivityMesg());
			encode.close();
		} catch (FitRuntimeException e) {
			setError("Error closing encode.");
			e.printStackTrace();
		}
		return getError();
	}

	/**
	 * set the end time of this activity when parsing an old tcx file, or adding
	 * a record to the file
	 * 
	 * @param theRecordMesg
	 *            is the last RecordMesg in the activity file
	 * 
	 */
	private void setActivityEndTime(RecordMesg theRecordMesg) {
		activityEndTime = theRecordMesg.getTimestamp();
	}

	/**
	 * Set the start time of this activity when parsing an old tcx file, or
	 * opening a new tcx file.
	 * 
	 * @param theRecordMesg
	 *            is the first RecordMesg in the activity file
	 * 
	 */
	private void setActivityStartTime(RecordMesg theRecordMesg) {
		activityStartTime = theRecordMesg.getTimestamp();
	}
	
	/**
	 * For each new record we write, update the summary values so they can be
	 * written when the files is closed
	 * 
	 * @param bs
	 *            BikeStat structure containing all the data
	 */
	private void saveSummaryValuesFromNewRecord(BikeStat bs) {		
		totalTimerTime = (float) bs.getGPSRideTime();
		totalDistance = (float) bs.getGPSTripDistance();
		avgSpeed = (float) bs.getAvgSpeed();
		maxSpeed = (float) bs.getMaxSpeed();

	}

	/**
	 * We'll write the activity message just before closing the encoded file

	 * @return the ActivityMesg
	 */
	private ActivityMesg composeFITActivityMesg() {
		ActivityMesg mActivityMesg = new ActivityMesg();
		// to upload to Strava the Timestamp may have to be the start time
		mActivityMesg.setTimestamp(activityStartTime);
		mActivityMesg.setEvent(Event.SESSION);
		mActivityMesg.setEventType(EventType.STOP);
		mActivityMesg.setTotalTimerTime(totalTimerTime);
		return mActivityMesg;
	}

	/**
	 * We'll write the lap message just before closing the encoded file
	 * @return the LapMesg
	 */
	private LapMesg composeFITLapMesg() {
// use summary values from handler
		//These are the ones used by Strava
//		lap 	timestamp
//				total_elapsed_time
//				total_timer_time
//				total_distance
//				total_ascent
		LapMesg mLapMesg = new LapMesg();
		mLapMesg.setStartTime(new DateTime(activityStartTime));
		// to upload to Strava the Timestamp may have to be the start time
		mLapMesg.setTimestamp(new DateTime(activityStartTime));
		float totalElapsedTime = (float) (activityEndTime.getTimestamp() - activityStartTime.getTimestamp());
		mLapMesg.setTotalElapsedTime(totalElapsedTime);
		// TotalTimerTime excludes pauses; "ride-time"
		mLapMesg.setTotalTimerTime(totalTimerTime);
		mLapMesg.setTotalDistance(totalDistance);
		mLapMesg.setAvgSpeed(avgSpeed);
		mLapMesg.setMaxSpeed(maxSpeed);
		mLapMesg.setAvgCadence((short) avgCadence);
		mLapMesg.setMaxCadence((short) maxCadence);
		mLapMesg.setAvgHeartRate((short) avgHeartRate);
		mLapMesg.setMaxHeartRate((short) maxHeartRate);
		mLapMesg.setEvent(Event.SESSION);
		mLapMesg.setEventType(EventType.STOP);
		return mLapMesg;
	}

	/**
	 * Here's the data summary written just before closing the file

	 * @return the SessionMesg
	 */
	private SessionMesg composeFITSessionMesg() {
		//use summary values from handler
		//These are the ones used by Strava
//		session 	sport
//					total_elapsed_time
//					total_timer_time
//					total_distance
//					total_ascent
		SessionMesg mSessionMesg = new SessionMesg();
		mSessionMesg.setSport(com.garmin.fit.Sport.CYCLING);
		float totalElapsedTime = (float) (activityEndTime.getTimestamp() - activityStartTime.getTimestamp());
		mSessionMesg.setTotalElapsedTime(totalElapsedTime);
		// TotalTimerTime excludes pauses; "ride-time"
		mSessionMesg.setTotalTimerTime(totalTimerTime);
		mSessionMesg.setTotalDistance(totalDistance);
		// to upload to Strava the Timestamp may have to be the start time
		mSessionMesg.setTimestamp(new DateTime(activityStartTime));
		mSessionMesg.setStartTime(new DateTime(activityStartTime));
		mSessionMesg.setAvgSpeed(avgSpeed);
		mSessionMesg.setMaxSpeed(maxSpeed);
		mSessionMesg.setAvgCadence((short) avgCadence);
		mSessionMesg.setMaxCadence((short) maxCadence);
		mSessionMesg.setEvent(Event.SESSION);
		mSessionMesg.setEventType(EventType.STOP);
		return mSessionMesg;
	}

	private FileIdMesg composeFileIdMesg(DateTime activityStartTime) {
		//Generate FileIdMessage
		 // Every FIT file MUST contain a 'File ID' message as the first message
		FileIdMesg fileIdMesg = new FileIdMesg();
		fileIdMesg.setManufacturer(FIT_MANUFACTURER_DEVELOPMENT);
		fileIdMesg.setType(com.garmin.fit.File.ACTIVITY);
		fileIdMesg.setProduct(1);
		PackageInfo pInfo;
		long versionNum = 0L;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionNum = (long) pInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		fileIdMesg.setSerialNumber(versionNum);
		fileIdMesg.setTimeCreated(activityStartTime);
		return fileIdMesg;
	}

	private DeviceInfoMesg composeDeviceIdMesg(DateTime activityStartTime) {
		// deviceInfo - needed for Garmin Connect upload...
		DeviceInfoMesg deviceInfo = new DeviceInfoMesg();
		deviceInfo.setTimestamp(activityStartTime);
		deviceInfo.setManufacturer(FIT_MANUFACTURER_DEVELOPMENT);
		return deviceInfo;
	}

	/**
	 * delete path prefix; private storage names can't have path symbols.
	 * Filename returned from ShowFileList has path characters in it
	 * 
	 * @param fileName
	 *            the file name that possibly has path characters in it
	 * @return the fileName without path characters
	 */
	String stripFilePath(String fileName) {

		if (fileName != null) {
			int start = fileName.lastIndexOf("/") + 1;
			int end = fileName.length();
			if ((end - start) <= 0) {
				fileName = "";
			} else {
				fileName = fileName.substring(start, end);
			}
		}
		return fileName;
	}

	/**
	 * Delete the .tcx or .fit suffix on the file name. Use the same filename
	 * for tcx and fit files, but will add the right suffix when encoding fit or
	 * tcx files.
	 * 
	 * @param fileName
	 *            the file name to strip
	 * @return the filenam stripped of the suffix
	 */
	String delTCXFITSuffix(String fileName) {
		// delete the .tcx suffix on the fileName
		int end = fileName.indexOf(TCX, 0);
		if (end > 0) {
			fileName = fileName.substring(0, end);
		}
		// now test for ".fit" suffix and delete that
		end = fileName.indexOf(FIT, 0);
		if (end > 0) {
			fileName = fileName.substring(0, end);
		}
		return fileName;
	}

	private boolean updateExternalStorageState() {
		boolean mExtStorAvailable = false;
		boolean mExtStorWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExtStorAvailable = mExtStorWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExtStorAvailable = true;
			mExtStorWriteable = false;
		} else if (Environment.MEDIA_SHARED.equals(state)
				|| Environment.MEDIA_REMOVED.equals(state)
				|| Environment.MEDIA_BAD_REMOVAL.equals(state)
				|| Environment.MEDIA_NOFS.equals(state)
				|| Environment.MEDIA_UNMOUNTED.equals(state)
				|| Environment.MEDIA_UNMOUNTABLE.equals(state)
				|| Environment.MEDIA_CHECKING.equals(state)){
			mExtStorAvailable = mExtStorWriteable = false;
		}
//		Log.i(this.getClass().getName(), "extStorageState: " + state);
		return handleExternalStorageState(mExtStorAvailable,
				mExtStorWriteable);
	}

	private boolean handleExternalStorageState(
			boolean mExtStorageAvailable,
			boolean mExtStorageWriteable) {
		setStorageError("");
		if (!mExtStorageWriteable || !mExtStorageAvailable) {
			setStorageError(context.getString(R.string.can_t_write_to_external_storage));
		}
		return mExtStorageAvailable & mExtStorageWriteable;
	}

	private void setStorageError(String sdError) {
		this.sdError= sdError;		
	}
	
	private String getSDError() {
		return sdError;
	}
	
	public String getError() {
		return error;
	}

	private void setError(String error) {
		this.error = error;
	}

    /**
     * Small activity files can accumulate. Delete any fit file with size < 500 bytes
     *
     * @param bs                 BikeStat Class to retrieve the current logOut filename, which we don't want to purge
     * @param chosenActivityFile the file we are trying to share;
     *                           it may be small so don't purge it until it has been shared
     */
    void purgeSmallActivityFiles(final BikeStat bs, final String chosenActivityFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //get a list of .fit Activity files, excluding the current output file
                List<File> fitFiles = mLoadActivityFileList();
                String logOutName = delTCXFITSuffix(bs.tcxLog.outFileName);
                String bareChosenActivityFile = stripFilePath(delTCXFITSuffix(chosenActivityFile));
                if (MainActivity.debugFITFile) {
                    Log.w(this.getClass().getSimpleName(), "purgeSmall - logOutName: " + logOutName);
                    Log.w(this.getClass().getSimpleName(), "purgeSmall - chosenActivityFile: " + bareChosenActivityFile);
                }
                for (File aFitFile : fitFiles) {
                    if (MainActivity.debugFITFile) {
                        Log.w(this.getClass().getSimpleName(), "found .fit file: " + aFitFile.toString()
                                + " fileSize: " + aFitFile.length());
                    }
                    if (!aFitFile.isDirectory()
                            // don't purge the current logout file
                            && !aFitFile.getName().contains(logOutName)
                            // don't purge the file we're sharing, exclude this test if no ActivityFile chosen
                            && (!aFitFile.getName().contains(bareChosenActivityFile) || ("").equals(bareChosenActivityFile))) {

                        try {
                            if (MainActivity.debugFITFile) {
                                Log.w(this.getClass().getSimpleName(), "deleting .fit file: " + aFitFile.toString()
                                + " fileSize: " + aFitFile.length());
                            }
                            aFitFile.delete();
                            // also delete the .tcx file
                            String theTCXFile = delTCXFITSuffix(aFitFile.toString()) + ".tcx";
                            if (MainActivity.debugFITFile) {
                                Log.w(this.getClass().getSimpleName(), "deleting .tcx file: " + theTCXFile);
                            }
                            new File(theTCXFile).delete();
                        } catch (SecurityException ignore) {

                        }
                    }
                }
            }
        }).start();
    }

	/**
	 * Get a list of .fit activity files in the directory. We only need to look at .fit files,
	 * the corresponding .tcx file will also be deleted.
	 * @return a List of the Files
	 */
	private List<File> mLoadActivityFileList() {
		File activityFilePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/Android/data/com.cyclebikeapp/files/");
		List<File> fileFileList = new ArrayList<>();
        try {
            if (!activityFilePath.exists()){
                activityFilePath.mkdirs();
            }
        } catch (SecurityException ignore) {
        }
        if (activityFilePath.exists() && activityFilePath.canRead() && activityFilePath.isDirectory()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // look for .fit files with size < .5 kB
                    Log.w(this.getClass().getSimpleName(), "filename: " + filename
                            + " fileSize: " + sel.length());
                    return filename.contains(FIT) && (sel.length() < 500);
                }
            };
            fileFileList = Arrays.asList(activityFilePath.listFiles(filter));
        }
        return fileFileList;
    }// loadActivityFileList()


	boolean isFileEncoderBusy() {
		return fileEncoderBusy;
	}

	void setFileEncoderBusy(boolean fileEncoderBusy) {
		this.fileEncoderBusy = fileEncoderBusy;
	}
			
}
