package com.example.watchin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CheckIn extends ActionBarActivity {

	public static final String TAG = CheckIn.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_in);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_in, menu);
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

		// Initiate the UI
		Chronometer chronometer;
		TextView mTextViewTimeLeft;
		TextView mTextViewMinutes;

		// Chronometer Variables
		long timeWhenStopped = 0;

		// Variables
		int duration;

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
		final Location tempLocation = new Location("");

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_check_in,
					container, false);

			// Adding Location Manager
			LocationManager locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			// Initiate UI
			// chronometer = (Chronometer) rootView
			// .findViewById(R.id.chronometer1);

			mTextViewMinutes = (TextView) rootView
					.findViewById(R.id.textViewMinutes);
			mTextViewTimeLeft = (TextView) rootView
					.findViewById(R.id.textViewTimeLeft);

			tempLocation.setLatitude(destPosition.latitude);
			tempLocation.setLongitude(destPosition.longitude);

			// Other function
			getDuration();
			getDestLocation();

			mTextViewTimeLeft.setText(duration + "");

			// Debug
			Toast.makeText(getActivity(), duration + " ", Toast.LENGTH_SHORT)
					.show();
			Toast.makeText(getActivity(), destPosition.toString(),
					Toast.LENGTH_SHORT).show();

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

		/*
		 * Added Function
		 */

		// getter from previous intent

		public Integer getDuration() {
			duration = getActivity().getIntent().getIntExtra(
					ParseConstants.KEY_START_DATE, 30);
			return duration;
		}

		public LatLng getDestLocation() {
			Intent intent = getActivity().getIntent();
			double[] temp;
			temp = intent.getDoubleArrayExtra(ParseConstants.KEY_LOCATION);
			destPosition = new LatLng(temp[0], temp[1]);
			return destPosition;

		}

		// Main Stuff ??

		private void watchMe() {

			// Set Location
			final Location location = mLocationClient.getLastLocation();

			int longmilis = duration * 60000;

			// logic flow
			// 1.check distance dulu baru timer ? ato
			// 2.timer dulu baru distance

			new CountDownTimer(longmilis, 1000) {

				public void onTick(long millisUntilFinished) {
					Toast.makeText(getActivity(), millisUntilFinished + " ",
							Toast.LENGTH_SHORT).show();
					int minutes = (int) (millisUntilFinished) / 60000;
					int seconds = (int) (millisUntilFinished - minutes * 60000) / 1000;
					mTextViewTimeLeft.setText(minutes + " : " + seconds);

				}

				public void onFinish() {
					// Save user location
					saveUserLastLocation(location);

					// check if user already near the target location or not
					if (location.distanceTo(tempLocation) < 100) {
						// user already near the location
					} else {
						// prompt alert dialog
					}
				}
			}.start();

		}

		// get duration

		// Save user last position
		private void saveUserLastLocation(Location location) {
			final ParseGeoPoint temp = new ParseGeoPoint(
					location.getLatitude(), location.getLongitude());
			ParseUser user = ParseUser.getCurrentUser();
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.getInBackground(user.getObjectId(),
					new GetCallback<ParseUser>() {

						@Override
						public void done(ParseUser currentUser, ParseException e) {
							if (e == null) {
								currentUser.add(ParseConstants.KEY_LOCATION,
										temp);
								currentUser.saveEventually();
							} else {
								errorAlertDialog(e);
							}
						}
					});
		}

		/*
		 * Error Dialog
		 */
		private void errorAlertDialog(ParseException e) {
			// failed
			Log.e(TAG, e.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(e.getMessage()).setTitle("Error")
					.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}

		/*
		 * Chronometer Function
		 */

		// // Start Chronometer
		// public void startChronometer() {
		// chronometer
		// .setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		// chronometer.start();
		//
		// }
		//
		// // Reset Chronometer
		// public void resetChronometer() {
		// chronometer.setBase(SystemClock.elapsedRealtime());
		// timeWhenStopped = 0;
		// }
		//
		// // Stop Chronometer
		// public void stopChronometer() {
		// timeWhenStopped = chronometer.getBase()
		// - SystemClock.elapsedRealtime();
		// chronometer.stop();
		// }

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
		public void onLocationChanged(Location arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnected(Bundle arg0) {
			mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
			Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT)
					.show();
			// run main function
			watchMe();
		}

		@Override
		public void onDisconnected() {
			// TODO Auto-generated method stub

		}
	}

}
