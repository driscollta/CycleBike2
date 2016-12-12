package com.cyclebikeapp.gold;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.cyclebikeapp.gold.Constants.KEY_FILENAME;
import static com.cyclebikeapp.gold.Constants.KEY_FILESIZE;
import static com.cyclebikeapp.gold.Constants.KEY_THUMB;
/*
 * Copyright  2013 cyclebikeapp. All Rights Reserved.
*/

class LazyAdapter extends BaseAdapter {
	private final ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		data = d;
		inflater = (LayoutInflater) a
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			vi = inflater.inflate(R.layout.lazy_list_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title); // title
		TextView size = (TextView) vi.findViewById(R.id.filesize); // filesize
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);
		HashMap<String, String> fileItem;
		fileItem = data.get(position);

		// Setting all values in listview
		size.setText(fileItem.get(KEY_FILESIZE));
		title.setText(fileItem.get(KEY_FILENAME));
		int imageLevel = Integer.valueOf(fileItem.get(KEY_THUMB));
		thumb_image.setImageLevel(imageLevel);
		return vi;
	}
}
