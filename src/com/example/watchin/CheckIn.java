package com.example.watchin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
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
		boolean isCheckIn;
		ArrayList<Integer> familyPhonesNumber = new ArrayList<Integer>();
		String address;

		// Maps Variables
		private GoogleMap mMap;
		private SupportMapFragment fragment;
		private LocationClient mLocationClient;
		private Location mLastLocation;
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

			// Other function
			getDuration();
			getDestLocation();

			tempLocation.setLatitude(destPosition.latitude);
			tempLocation.setLongitude(destPosition.longitude);

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

		// Main Algorithm

		private void watchMe() {
			int longmilis = duration * 60000;

			// Set Location
			final Location location = mLocationClient.getLastLocation();

			// decode the address
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(getActivity(), Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);
				address = addresses.get(0).getAddressLine(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mTextViewMinutes.setText("Time left for check-in");

			// init outer countdown timer
			new CountDownTimer(30000, 1000) {

				public void onTick(long millisUntilFinished) {
					int minutes = (int) (millisUntilFinished) / 60000;
					int seconds = (int) (millisUntilFinished - minutes * 60000) / 1000;
					mTextViewTimeLeft.setText(minutes + " : " + seconds);

				}

				public void onFinish() {
					float distanceLeft = location.distanceTo(tempLocation);

					// Save user location
					saveUserLastLocation(location);

					// check if user already near the target location or not
					if (location.distanceTo(tempLocation) < 100) {
						// user already near the location
						Toast.makeText(getActivity(), "Arrive Safely",
								Toast.LENGTH_SHORT);

						// Send user back to main menu
						Intent intent = new Intent(getActivity(),
								MainActivity.class);
						startActivity(intent);
						getActivity().finish();

					} else {
						isCheckIn = false;
						// set Notification
						setNotification(getActivity());

						// prompt alert dialog for check in
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(
								"Check In Please "
										+ " you will arrive at your destination in "
										+ distanceLeft + " meters")
								.setPositiveButton("Yes",
										dialogInnerClickListener).show();

						// Change The UI
						mTextViewMinutes.setText("seconds to check in");

						// init inner countdown timer
						CountDownTimer inner = new CountDownTimer(15000, 1000) {
							@Override
							public void onTick(long millisUntilFinished) {
								mTextViewTimeLeft.setText(millisUntilFinished
										/ 1000 + "");
							}

							@Override
							public void onFinish() {
								if (isCheckIn == true) {
									// start over
									Toast.makeText(getActivity(),
											isCheckIn + " ", Toast.LENGTH_SHORT)
											.show();
									watchMe();
								} else {
									// warn the family
									Toast.makeText(getActivity(),
											isCheckIn + " ", Toast.LENGTH_SHORT)
											.show();
									warnTheOther();
								}
							}

						};
						inner.start();
					}
				}
			}.start();

		}

		// Warn the OTHER !!!

		private void warnTheOther() {
			String userId = ParseUser.getCurrentUser().getObjectId();
			final String userName = ParseUser.getCurrentUser().getString(
					ParseConstants.KEY_NAME);
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

							String phoneNumber = user
									.getString(ParseConstants.KEY_PHONE);
							String familyName = user
									.getString(ParseConstants.KEY_NAME);
							String time = user.getUpdatedAt().toString();

							String message = "Hey !! " + userName
									+ " is missing from our grid, "
									+ "his/her last position are in " + address;

							String temo = "Hi "
									+ familyName
									+ ", this is WatchMe Application sending you alert on behalf of "
									+ userName
									+ "He/she has not responded to the safe confirmation message "
									+ "we sent in the past #numberofminutes. Our app detected his/her last "
									+ "location"
									+ "activity and his/her last tracked location is in"
									+ address
									+ ". Thank you."
									+ "P/S: This is automated alert message and will be sent again in"
									+ duration
									+ " minutes if no responses from the user are acquired";

							Toast.makeText(getActivity(), phoneNumber + " - ",
									Toast.LENGTH_LONG).show();
							//
							Toast.makeText(getActivity(),
									phoneNumber + " - " + message,
									Toast.LENGTH_LONG).show();

							// Send Message
							// sendSMS(phoneNumber, message);

							// startActivity(new Intent(Intent.ACTION_VIEW, Uri
							// .parse(message
							// + Integer.parseInt(phoneNumber))));
						}

						Intent intent = new Intent(getActivity(),
								DangerActivity.class);
						startActivity(intent);
						getActivity().finish();

					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		// Dialog Box Action Listener

		DialogInterface.OnClickListener dialogInnerClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					isCheckIn = true;
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// Do nothing
					break;
				}
			}
		};

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
								currentUser.put(ParseConstants.KEY_LOCATION,
										temp);
								currentUser.saveEventually();
								Toast.makeText(getActivity(), "save location",
										Toast.LENGTH_SHORT).show();
							} else {
								errorAlertDialog(e);
							}
						}
					});
		}

		/*
		 * Send Message
		 */

		protected void sendSMS(String phoneNo, String message) {
			Log.i("Send SMS", "");
			Toast.makeText(getActivity(), "send sms", Toast.LENGTH_SHORT)
					.show();
			SmsManager.getDefault().sendTextMessage(phoneNo, null, message,
					null, null);
		}

		/*
		 * Vibration and light
		 */

		@SuppressLint("InlinedApi")
		private static void setNotification(Context context) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context);
			// Vibration
			builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

			// LED
			builder.setLights(android.R.color.holo_blue_bright, 3000, 3000);

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
			LatLng latLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					latLng, 17);
			mMap.animateCamera(cameraUpdate);

			if (mLastLocation == null)
				mLastLocation = location;

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
