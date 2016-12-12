package com.cyclebikeapp.gold;

final class Constants {
	 Constants() {}
    static final String STATE_RESOLVING_ERROR = "resolving_error";
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 924;
    static final int MY_PERMISSIONS_REQUEST_WRITE = 824;
    static final int REQUEST_CHECK_SETTINGS = 94;
    static final int REQUEST_RESOLVE_ERROR = 1001;
    static final int UPLOAD_FILE_SEND_REQUEST_CODE = 2000;
    //counter for how many time we've lauched; to post nag upgrade dialog
    static final String NUM_LAUNCHES = "num_launches";
    static final String NAG_TYPE = "nag_type";
    static final int NAG_TYPE_LATER = 25;
    static final int NAG_TYPE_NOT_NOW = 10;
    static final String FIT_ACTIVITY_TYPE  = "1";
    static final String TCX_ACTIVITY_TYPE  = "0";
    static final String KEY_AUTH_NO_NETWORK_INTENT = "88";
    static final String CHOOSER_TYPE = "type";
    static final int CHOOSER_TYPE_GPX_DIRECTORY = 100;
    static final int CHOOSER_TYPE_GPX_FILE = 200;
    static final int CHOOSER_TYPE_TCX_DIRECTORY = 300;
    static final int CHOOSER_TYPE_TCX_FILE = 400;
    // Unique tag for the error dialog fragment
    static final String DIALOG_ERROR = "dialog_error";
    static final String EXCEPTION = "Exception";
    static final String NO_LOGOUT = "no logout";

    static final String FILE_NOT_FOUND = "file not found";
    static final String NO_ROUTE_DATA_IN_FILE = "No route data in file!";
    static final String LOOKING_FOR_ROUTE_DATA = "Looking for route data";
    static final String LOADING_FILE = "Loading File";
    static final String TMP_CB_ROUTE = ".tmpCBRoute";
    static final String CB_HISTORY = "CB_history";
    static final String TP_DENSITY = "tpDensity_";
    static final String XML = ".xml";
    static final String TCX = ".tcx";
    static final String GPX = ".gpx";
    static final String FIT = ".fit";
    static final String DOUBLE_ZERO = "0.0";
    static final short WILDCARD = 0;

    static final String UPLOAD_FILENAME = "upload_filename";
    static final String TCX_LOG_FILE_NAME = "tcxLogFileName";
    static final String TCX_LOG_FILE_FOOTER_LENGTH = "tcxLogFileFooterLength";
    static final String FILENAME_SUFFIX = "_CB_history.tcx";

    static final String CURR_WP = "curr_WP";
    static final String FIRST_LIST_ELEM = "first_ListElem";
    static final String MAX_SPEED = "maxSpeed";
    static final String TRIP_DISTANCE = "tripDistance";
    static final String TRIP_TIME = "tripTime";
    static final String SAVED_LAT = "savedLat";
    static final String SAVED_LON = "savedLon";
    static final String PREF_SAVED_LOC_TIME = "prefsavedTime";
    static final int _360 = 360;
    static final String PREFS_NAME = "MyPrefsFile";
    static final String APP_NAME = "CycleBike";
    static final String BONUS_MILES = "bonusMiles ";
    static final String KEY_FILENAME = "filename";
    static final String KEY_THUMB = "icon_type";
    static final String KEY_FILESIZE = "filesize";
    static final String IC_XML_FILE = "6";// xml file icon
    static final String IC_FIT_FILE = "4";//ic_file_fit
    static final String IC_TCX_FILE = "3";//tcx file icon
    static final String IC_FOLDER_UP = "2";// ic_folder_up
    static final String IC_FILE = "1";//ic_file
    static final String IC_FOLDER = "0";//ic_folder
    static final String KEY_GPXPATH = "gpxPath";
    static final String KEY_CHOSEN_GPXFILE = "chosenGPXFile";
    static final String KEY_CHOSEN_TCXFILE = "chosenTCXFile";
    static final String KEY_FORCE_NEW_TCX = "force_new_tcx";
    static final String KEY_CHOOSER_CODE = "chooserCode";
    static final String SHOW_SHARING = "show_sharing_alert";
    static final String[] RWGPS_EMAIL = {"upload@rwgps.com"};
    static final Integer FIT_MANUFACTURER_DEVELOPMENT = 0x00ff;
    static final String PARSER_CONFIG = " ParserConfig";
    static final String IO_EXCEPTION = " IOException";
    static final String FIT_RUNTIME_EXCEPTION = "Fit Runtime Exception";
    static final String TIME_STR_FORMAT = "%02d";
    static final String FORMAT_7F = "%.7f";
    static final String FORMAT_2F = "%.2f";
    static final String FORMAT_4_3F = "%4.3f";
    static final String FORMAT_3D = "%3d";
    static final String FORMAT_4_1F = "%4.1f";
    static final String FORMAT_3_1F = "%3.1f";
    static final String FORMAT_1F = "%.1f";
    static final double googleLon = -122.085144;
    static final double googleLat = 37.422151;
    static final String MILE = "mi";
    static final String KM = "km";
    static final String METER = "m";
    static final String FOOT = "ft";
    static final String ZERO = "0";
    static final double DEG_PER_BEARING_ICON = 22.5;
    static final double msecPerSec = 1000.;
    /** a reasonable maximum speed for a bicycle meters per second */
    static final double MAX_GPS_SPEED = 45;
    /** distance conversions */
    static final double mph_per_mps = 2.23694;
    static final double kph_per_mps = 3.6;
    static final double km_per_meter = 0.001;
    static final double mile_per_meter = 0.00062137119224;
    static final double semicircle_per_degrees = 11930464.711111111111111111111111;
    static final String TRACKPOINT = "TRACKPOINT";
    /** types of GPXRoutePoints in the merged array */
    static final int clusterTrkPtKind = 99;
    static final int importantClusterTrkPtKind = 98;
    static final int trkPtKind = 100;
    static final int routePtKind = 1;
    static final String KEY_TRACK_DENSITY = "track_density";
    /** street is the text string of the street name */
    static final String KEY_STREET = "street";
    /** street unit is the units to display (ft, mi, m, km) */
    static final String KEY_UNIT = "street_unit";
    /** distance is the distance to the next turn, updated as locations are received */
    static final String KEY_DISTANCE = "distance";
    /** turn level is the numeric value that defines the turn icon to display
     defined in the turn_levels.xml document in res/drawable */
    static final String KEY_TURN = "turn_level";
    /** level is the numeric value that defines the bearing arrow to display
     the icons are defined in the arrow_levels.xml document in res/drawable */
    static final String KEY_BEARING = "bearing_level";
    /** dimmed is an indication of how to display the data, dimmed when a way
     point has been passed or just within reach */
    static final String KEY_DIM = "dimmed";
    static final long ONE_SEC = 1000;
    /** set location current if no older than this (in millisec)*/
    static final long TEN_SEC = 10 * 1000;
    /** initial delay for location watchdog */
    static final long FIVE_SEC = 5 * 1000;
    static final long THIRTY_SEC = 30 * ONE_SEC;
    static final long ONE_HOUR = 60 * 60 * 1000;
    static final long TWENTYFOUR_HOURS = 24 * ONE_HOUR;
    static final long JAN_1_2000 = 975596581L;
    //key to last modified time of write new track
    static final String LAST_MODIFIED = "last_modified_time";
    // threshold for totalDistance defining an orphan activity file
    static final double FILE_TOO_SHORT = 200;
    // MB of file storage required
    static final int requiredStorageSpace = 20;
    /** when re-starting nav from long-pressed WP make sure we're nearEnough (meters) */
    static final double nearEnough = 402.25;
    /**detection threshold for the paused condition: speed less than */
    static final double speedPausedVal = 0.5 / mph_per_mps;
    /**detection threshold for the paused condition: delta direction of travel less than */
    static final double dotPausedVal = .01;
    /** gps speed at which we can trust that the direction of travel bearing is accurate (m/sec)*/
    static final double accurateGPSSpeed = 2. / mph_per_mps;
    /**want good enough location accuracy when writing locations to track file (meters) */
    static final float goodEnoughLocationAccuracy = 50;
    public static final String KEY_DIST_TYPE = "distanceType";
    static final String SPEED_TRIPLE_X = "XX.x";
    static final String QUESTION = "??";
    static final int DISTANCE_TYPE_MILE = 0;
    static final int DISTANCE_TYPE_METRIC = 1;
    /** DIST_TYPE = 0 is normal, route distance; DIST_TYPE = 1 is direct distance */
    static final int ROUTE_DISTANCE_TYPE = 0;
    static final int DIRECT_DISTANCE_TYPE = 1;
    /** RequestCode for ShowFileList activity */
    static final int RC_SHOW_FILE_LIST = 66;
    static final int ACTIVITY_FILE_TYPE = 1;
    static final int ROUTE_FILE_TYPE = 0;
    static final String PREF_HI_VIZ = "hi_viz";
    static final String PREFS_DEFAULT_LATITUDE = "37.1";
    static final String PREFS_DEFAULT_LONGITUDE = "-122.1";
    static final long PREFS_DEFAULT_TIME = 123456;

}
