package com.cyclebikeapp.gold;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class RWGPSCB extends Activity{
	private static final String[] RWGPS_EMAIL = {"upload@rwgps.com"};
	private static final String UPLOAD_FILENAME = "upload_filename";

	@Override
//	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final String uploadFilename = intent.getStringExtra(UPLOAD_FILENAME);
		setResult(Activity.RESULT_CANCELED);
		if (uploadFilename == null) {
			Toast.makeText(getApplicationContext(),
						   "No file specified", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		Uri fileUri = Uri.fromFile(new File(uploadFilename));
		final String bodyText = intent.getStringExtra(Intent.EXTRA_TEXT);
		final String subjectText = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		//Log.i(APP_NAME,"subjectText: " + subjectText);
		//Log.i(APP_NAME,"bodyText: " + bodyText);
		//Log.i(APP_NAME,"uploadFilename: " + uploadFilename);
		Intent uploadFileIntent;
		uploadFileIntent = new Intent(Intent.ACTION_SEND);
		uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		uploadFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		uploadFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		uploadFileIntent.putExtra(Intent.EXTRA_EMAIL, RWGPS_EMAIL);
		uploadFileIntent.putExtra(Intent.EXTRA_SUBJECT, subjectText);
		uploadFileIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		uploadFileIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
//TODO does this type work with .fit files?
		uploadFileIntent.setType("abc/xyz");
		startActivity((uploadFileIntent));
		finish();
//	startActivity(Intent.createChooser(uploadFileIntent, getString(R.string.upload_file)));
	}
}
