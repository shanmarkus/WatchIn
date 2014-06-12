package com.example.watchin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class HomeFragment extends Fragment {

	// Variables
	protected static final String TAG = HomeFragment.class.getSimpleName();
	ProgressDialog progressDialog;
	GeoPoint p;
	ArrayList<Integer> myGeoPoints = new ArrayList<Integer>();

	// UI Variables
	EditText mHomeDestinationEditText;
	Button mHomeSubmitButton;
	Button mHomeCheckButton;

	// Fixed Variables
	Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	ArrayList<HashMap<String, String>> userActivities = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> userActivity = new HashMap<String, String>();
	ArrayList<String> promotionsId = new ArrayList<String>();

	// Parse Constants
	String userId;
	ParseObject currentUser;

	public static HomeFragment newInstance(String param1, String param2) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public HomeFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		if (ParseUser.getCurrentUser() == null) {
			navigateToLogin();
		} else {
			userId = ParseUser.getCurrentUser().getObjectId();
			currentUser = ParseUser.createWithoutData(
					ParseConstants.TABLE_USER, userId);
		}
		// UI Declaration
		mHomeDestinationEditText = (EditText) rootView
				.findViewById(R.id.homeDestinationEditText);
		mHomeCheckButton = (Button) rootView.findViewById(R.id.homeCheckButton);
		mHomeSubmitButton = (Button) rootView
				.findViewById(R.id.homeSubmitButton);

		mHomeCheckButton.setOnClickListener(checkLocation);

		mHomeSubmitButton.setEnabled(false);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/*
	 * Navigate to Login
	 */
	private void navigateToLogin() {
		Intent intent = new Intent(getActivity(),
				ParseStarterProjectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Progress Dialog initiate
	 */

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	/*
	 * Get User Information include number of check in, follower, following
	 */

	// Get All

	// Map Class
	public class MapOverlay extends com.google.android.maps.Overlay {
		public boolean draw(Canvas canvas,
				com.google.android.maps.MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			// ---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(p, screenPts);

			// ---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.drawer_shadow);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 32, null);
			return true;
		}
	}

	/*
	 * Check Location to google maps if it track able or not
	 */

	OnClickListener checkLocation = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Clear Arraylist
			myGeoPoints.clear();

			List<Address> addresses;
			String temp = mHomeDestinationEditText.getText().toString();
			Geocoder geoCoder;
			geoCoder = new Geocoder(getActivity(), Locale.getDefault());

			try {
				addresses = geoCoder.getFromLocationName(temp, 1);

				while (addresses.size() == 0) {
					addresses = geoCoder.getFromLocationName(temp, 1);
				}

				if (addresses.size() > 0) {
					p = new GeoPoint(
							(int) (addresses.get(0).getLatitude() * 1E6),
							(int) (addresses.get(0).getLongitude() * 1E6));
					mHomeDestinationEditText.setText("");

					// add to arraylist
					myGeoPoints.add(p.getLatitudeE6());
					myGeoPoints.add(p.getLongitudeE6());

					// Setup UI
					mHomeSubmitButton.setEnabled(true);
					mHomeSubmitButton.setOnClickListener(submitLocation);

				} else {
					AlertDialog.Builder adb = new AlertDialog.Builder(
							getActivity());
					adb.setTitle("Google Map");
					adb.setMessage("Please Provide the Proper Place");
					adb.setPositiveButton("Close", null);
					adb.show();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	/*
	 * If track able than go to the setting page which has google maps
	 */

	OnClickListener submitLocation = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), WatchMeActivity.class);
			intent.putExtra(ParseConstants.KEY_LOCATION, myGeoPoints);
			startActivity(intent);
		}
	};

	/*
	 * Error Dialog Parse
	 */
	private void errorAlertDialog(ParseException e) {
		// failed
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
