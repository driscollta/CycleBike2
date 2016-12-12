package com.cyclebikeapp.gold;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.cyclebikeapp.gold.Constants.TRACKPOINT;
/*
 * Copyright  2013 cyclebikeapp. All Rights Reserved.
*/

class CrazyAdapter extends BaseAdapter {
//displays the ListView for the turn-by-turn directions

	private final ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	CrazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		data = d;
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.crazy_list_row, null);

		TextView street = (TextView) vi.findViewById(R.id.street_name); // title
		TextView distance = (TextView) vi.findViewById(R.id.distance);
		TextView distanceUnit = (TextView) vi.findViewById(R.id.distance_unit);
		ImageView turnIcon = (ImageView) vi.findViewById(R.id.turn_icon);
		ImageView bearingIcon = (ImageView) vi.findViewById(R.id.arrow_icon);
		HashMap<String, String> fileItem;
		fileItem = data.get(position);
		// Setting all values in listview
		String streetText = fileItem.get(Constants.KEY_STREET);
		street.setText(streetText);
		street.setTypeface(null, Typeface.NORMAL);
		if (!streetText.toUpperCase().contains(TRACKPOINT)) {
			street.setTypeface(null, Typeface.BOLD);			
		}
		//color value for street, distance, etc text_dim, white, gpsgreen, hiViz yellow
		int dimLevel = Integer.valueOf(fileItem.get(Constants.KEY_DIM));
		street.setTextColor(dimLevel);
		distanceUnit.setText(fileItem.get(Constants.KEY_UNIT));
		distanceUnit.setTextColor(dimLevel);
		distance.setText(fileItem.get(Constants.KEY_DISTANCE));
		distance.setTextColor(dimLevel);
		int imageLevel = Integer.valueOf(fileItem.get(Constants.KEY_TURN));
		turnIcon.setImageLevel(imageLevel);
		imageLevel = Integer.valueOf(fileItem.get(Constants.KEY_BEARING));
		bearingIcon.setImageLevel(imageLevel);
		return vi;
	}
}
