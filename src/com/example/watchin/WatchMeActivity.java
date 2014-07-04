package com.example.watchin;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class WatchMeActivity extends ActionBarActivity {

	protected static final String TAG = WatchMeActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watch_me);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.watch_me, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

		// UI Variables
		Spinner mTimeSpinner;
		Button mNextButton;

		// Maps Variables
		private GoogleMap mMap;
		private SupportMapFragment fragment;
		private LocationClient mLocationClient;
		private Location currentLocation = null;
		private static final LocationRequest REQUEST = LocationRequest.create()
				.setFastestInterval(1000) // 16ms = 60fps
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LatLng sourcePosition;
		LatLng destPosition;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_watch_me,
					container, false);

			// UI Declaration
			mNextButton = (Button) rootView.findViewById(R.id.nextButton);
			mTimeSpinner = (Spinner) rootView.findViewById(R.id.timeSpinner);

			// On click listener
			mNextButton.setOnClickListener(nextButtonListener);

			// Get LatLng Destination
			getDestPosition();

			// Adding Location Manager
			LocationManager locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			// Setup GPS
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(getActivity(), "GPS is Enabled in your devide",
						Toast.LENGTH_SHORT).show();
			} else {
				showGPSDisabledAlertToUser();
			}

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			if (mMap == null) {
				mMap = fragment.getMap();
				mMap.setMyLocationEnabled(true);
			}
			mMap.clear();
			setUpLocationClientIfNeeded();
			mLocationClient.connect();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			FragmentManager fm = getChildFragmentManager();
			fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
			if (fragment == null) {
				fragment = SupportMapFragment.newInstance();
				fm.beginTransaction().replace(R.id.map, fragment).commit();
			}
		}

		@Override
		public void onPause() {
			super.onPause();
			if (mLocationClient != null) {
				mLocationClient.disconnect();
			}
		}

		// Added function

		/*
		 * Get Current Position
		 */

		protected LatLng getCurrentPosition() {
			if (mLocationClient.isConnected() == false) {
				mLocationClient.connect();
			}
			// Get User Current Location
			currentLocation = mLocationClient.getLastLocation();
			sourcePosition = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());

			return sourcePosition;
		}

		/*
		 * Get The Destination Lat and Lang
		 */

		protected LatLng getDestPosition() {
			// Get User Destination Location
			Intent intent = getActivity().getIntent();
			double[] temp;
			temp = intent.getDoubleArrayExtra(ParseConstants.KEY_LOCATION);
			destPosition = new LatLng(temp[0], temp[1]);
			return destPosition;

		}

		/*
		 * Drawing Route to the Maps
		 */
		protected void drawMaps(LatLng start, LatLng end) {
			start = getCurrentPosition();
			end = getDestPosition();
			mMap.addPolyline(new PolylineOptions().add(start).add(end).width(4)
					.color(Color.RED));
			mMap.addMarker(new MarkerOptions().position(end).title(
					"Destination"));
		}

		// /*
		// * Draw route if all connected
		// */
		//
		// protected void drawRoute(LatLng start, LatLng end) {
		// start = getCurrentPosition();
		// end = getDestPosition();
		//
		// drawMaps(sourcePosition, destPosition);
		// }

		/*
		 * Display family in the maps
		 */

		private void displayBackupLocation() {
			String userId = ParseUser.getCurrentUser().getObjectId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
			query.include(ParseConstants.KEY_FOLLOWING);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e) {
					if (e == null) {
						for (ParseObject object : objects) {
							ParseObject user = object
									.getParseObject(ParseConstants.KEY_FOLLOWING);
							ParseGeoPoint userPoint = user
									.getParseGeoPoint(ParseConstants.KEY_LOCATION);
							String userName = user
									.getString(ParseConstants.KEY_NAME);
							Toast.makeText(getActivity(),
									userName + " " + userPoint.toString(),
									Toast.LENGTH_SHORT).show();
							if (userPoint != null) {
								LatLng userPos = new LatLng(userPoint
										.getLatitude(), userPoint
										.getLongitude());

								// draw marker
								mMap.addMarker(new MarkerOptions()
										.position(userPos)
										.title(userName)
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
							}

						}
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

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

		// Check GPS
		private void showGPSDisabledAlertToUser() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			alertDialogBuilder
					.setMessage(
							"GPS is disabled in your device. Would you like to enable it?")
					.setCancelable(false)
					.setPositiveButton("Goto Settings Page To Enable GPS",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivity(callGPSSettingIntent);
								}
							});
			alertDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = alertDialogBuilder.create();
			alert.show();
		}

		/*
		 * Go to watchMeCheckIn class
		 */

		private void goToCheckInClass(Integer duration) {
			Intent intent = new Intent(getActivity(), CheckIn.class);
			intent.putExtra(ParseConstants.KEY_START_DATE, duration);
			double[] temp;
			Intent prevIntent = getActivity().getIntent();
			temp = prevIntent.getDoubleArrayExtra(ParseConstants.KEY_LOCATION);
			intent.putExtra(ParseConstants.KEY_LOCATION, temp);
			startActivity(intent);
		}

		/*
		 * On Click Listener for next button
		 */

		OnClickListener nextButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String duration = (String) mTimeSpinner.getSelectedItem();
				goToCheckInClass(Integer.parseInt(duration));
			}
		};

		/*
		 * Map Functionality
		 */

		private void setUpLocationClientIfNeeded() {
			if (mLocationClient == null) {
				mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
						this); // OnConnectionFailedListener
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			// Do Nothing
		}

		@Override
		public void onConnected(Bundle connectionHint) {
			mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
			Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT)
					.show();

			// Get Variables
			sourcePosition = getCurrentPosition();
			destPosition = getDestPosition();

			// Draw routes to the maps
			drawMaps(sourcePosition, destPosition);

			// Display Backup Location
			displayBackupLocation();
			Location temp = mLocationClient.getLastLocation();
			mapZoom(temp);
		}

		private void mapZoom(Location location) {
			currentLocation = location;
			LatLng latLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					latLng, 10);
			mMap.animateCamera(cameraUpdate);
		}

		@Override
		public void onDisconnected() {
			// Do nothing
		}

		@Override
		public void onConnectionFailed(ConnectionResult result) {
			// Do nothing
		}

	}
}
