package com.example.clickforhelp.controllers.ui;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.clickforhelp.controllers.services.ActivityRecognitionService;
import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.services.SingleLocationUpdateService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask.GetOtherUsersLocations;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.clickforhelp.controllers.utils.CommonFunctions.checkLoggedIn;
import static com.example.clickforhelp.controllers.utils.CommonFunctions.isMyServiceRunning;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback,
        OnMapLoadedCallback, ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener, LocationSource, ServerResponse,
        GetOtherUsersLocations {

    // TAG for debugging
    private static final String TAG = MainActivity.class.getSimpleName();

    // Application context to be used through out the activity
    private Context mContext;

    // GoogleMap object
    private GoogleMap mGoogleMap;

    // googleapiclient object
    private GoogleApiClient mGoogleApiClient;

    // boolean to check and set location reqeust to be high accuracy or low
    // accuracy
    private static boolean mIsHighAccuracy = false;

    // OnLocationChangeListener object
    private OnLocationChangedListener mOnLocationChangeListener;

    // Markers object
    private ArrayList<Marker> mMarkers;

    // people text view
    private TextView mPeopleTextView;

    // help button
    private Button mHelpButton;

    // paths for server urls
    private static final String ASK_HELP_PATH = "askhelp";
    private static final String HELPED_PATH = "helped";
    private static final String HELP_RECEIVED_PATH = "helpreceived";

    // help button texts and flags
    private static final String ASK_HELP = "Ask For Help";
    private static final String ASKED_HELP = "Asked For Help(click here after receiving help)";
    private static final String HELPING = "Helping a Friend(click here after helping)";

    private static final int ASK_HELP_FLAG = 0;
    private static final int ASKED_HELP_FLAG = 1;
    private static final int HELPING_FLAG = 2;
    private static int mHelpFlag = 0;

    private final static String ASK_HELP_TEXT = "notifying nearby people";
    private final static String HELPED_TEXT = "notifying others";

    // Animation object
    private AlphaAnimation mAnimation;

    // userEmail
    private String mUserEmail = "example@nyu.edu";

    // type of update location
    private static final String UPDATE_HOME = "uh";
    private static final String UPDATE = "u";

    // paths for helperlist,victimlist
    private static final String HELPER_LIST = "helperlist";
    private static final String TRACK_VICTIM = "trackvictim";
    // private static final String HOME = "home";

    // victim useremail
    private String mVictimUserEmail = "example@nyu.edu";

    // Key for savedInstance of flag
    private final static String KEY_STATE = "state_of_user";

    //Linear layout for no network
    private LinearLayout mNoNetworkLinearLayout;

    //field for constant NO_CONNECTION to check for response from the httpmanager
    private static final int NO_CONNECTION = 999;

    //check if Activity recognition is enabled
    private boolean mIsEnabled = false;

    //alarm manager to send location updates every 30 min no matter what
    private PendingIntent mPendingIntent;

    //boolean to check if he is still
    private boolean mIsStill = false;

    //String to get the activity type
    private String mActivityType = AppPreferences.SharedPrefActivityRecognition.WALKING;


    //

	/*
     * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) {
            mContext = getApplicationContext();
        }
        if (isMyServiceRunning(LocationUpdateService.class, mContext)) {
            stopService(new Intent(mContext, LocationUpdateService.class));
        }
        if (checkLoggedIn(mContext)) {
            setContentView(R.layout.test_map);

            // getting the userEmail
            mUserEmail = CommonFunctions.getEmail(mContext);

            // Markers
            mMarkers = new ArrayList<>();

            // animating button
            buttonAnimation();

            // initialize button and textview
            acessViews();

            // InitializeMap
            initializeMap();

            // get Intent from the notification
            Intent intent = getIntent();
            if (intent != null) {
                retriveIntentExtras(intent);
            }

            //get ActivityRecognition status from saved preferences
            mIsEnabled = getActivityRecognitionStatus();

            //register a alarm manager
            setUpStillLocationUpdateAlarmManager();

        } else {
            startActivity(new Intent(mContext, AuthenticationActivity.class));
            finishAffinity();
        }

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPeopleTextView.setText("searching..");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityBroadcastReceiver,
                new IntentFilter("activity"));
        if (isMyServiceRunning(LocationUpdateService.class,
                mContext)) {
            stopService(new Intent(mContext, LocationUpdateService.class));
        }
        if (!checkLoggedIn(mContext)) {
            stopLocationUpdates();
            stopAlarmManager();
            Intent intent = new Intent(mContext, AuthenticationActivity.class);
            startActivity(intent);
            finishAffinity();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_STATE, mHelpFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isMyServiceRunning(LocationUpdateService.class,
                mContext) && checkLoggedIn(mContext)) {
            CommonFunctions
                    .settingUserPreferenceLocationUpdates(mContext, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
    }

    // creating and accessing menu options
    /*
     * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

	/*
     * (non-Javadoc)
	 *
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.action_deactivate) {
            this.deactivate();
            mGoogleMap.setMyLocationEnabled(false);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } else if (id == R.id.action_activate) {
            this.activate(mOnLocationChangeListener);
            mGoogleMap.setMyLocationEnabled(true);
            settingUpMapLocationSource();

        }
        return true;
    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.maps.OnMapReadyCallback#onMapReady(com.google.
	 * android.gms.maps.GoogleMap)
	 */

    // call back method from the interface onMapReady()

    @Override
    public void onMapReady(GoogleMap arg0) {
        mGoogleMap = arg0;
        mGoogleMap.setOnMapLoadedCallback(this);
    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback#onMapLoaded()
	 */

    // call back method from the interface onMapLoaded

    @Override
    public void onMapLoaded() {
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setLocationSource(this);
        buildGoogleApiClient();
    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
	 * #onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */

    // callbacks for the googleapiclient

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 */

    @Override
    public void onConnected(Bundle arg0) {
        settingUpMapLocationSource();
        if (!mIsEnabled) {
            settingUpActivityRecognition();
        }

    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnectionSuspended(int)
	 */

    @Override
    public void onConnectionSuspended(int arg0) {

    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.maps.LocationSource#activate(com.google.android
	 * .gms.maps.LocationSource.OnLocationChangedListener)
	 */

    // call backs for location source interface

    @Override
    public void activate(OnLocationChangedListener arg0) {
        mOnLocationChangeListener = arg0;

    }

	/*
     * (non-Javadoc)
	 *
	 * @see com.google.android.gms.maps.LocationSource#deactivate()
	 */

    @Override
    public void deactivate() {
        mOnLocationChangeListener = null;
    }

	/*
     * (non-Javadoc)
	 *
	 * @see
	 * com.google.android.gms.location.LocationListener#onLocationChanged(android
	 * .location.Location)
	 */

    // call back for interface location listener

    @Override
    public void onLocationChanged(Location arg0) {
        if (mOnLocationChangeListener != null && arg0 != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0
                    .getLatitude(), arg0.getLongitude())));
            mOnLocationChangeListener.onLocationChanged(arg0);
            RequestParams helpParams = null, helpingParams = null;
            double lat = arg0.getLatitude();
            double lng = arg0.getLongitude();
            if (mHelpFlag == ASK_HELP_FLAG && mActivityType != null && !mActivityType.equals(AppPreferences.SharedPrefActivityRecognition.STILL)) {
                Log.d(TAG, "not still and idle");
                Toast.makeText(this, "not still and idle", Toast.LENGTH_SHORT).show();
                mIsStill = false;
                sendLocationUpdate(lat, lng, AppPreferences.SharedPrefActivityRecognition.WALKING, UPDATE_HOME);

            } else if (mHelpFlag == ASKED_HELP_FLAG) {
                sendLocationUpdate(lat, lng, AppPreferences.SharedPrefActivityRecognition.WALKING, UPDATE);
                helpParams = CommonFunctions.setParams(new String[]{
                        HELPER_LIST, mUserEmail}, mContext);
                new SendLocationsAsyncTask(MainActivity.this).execute(helpParams);

            } else if (mHelpFlag == HELPING_FLAG) {
                sendLocationUpdate(lat, lng, AppPreferences.SharedPrefActivityRecognition.WALKING, UPDATE);
                helpingParams = CommonFunctions.setParams(new String[]{
                        TRACK_VICTIM, mUserEmail, mVictimUserEmail}, mContext);
                new SendLocationsAsyncTask(this).execute(helpingParams);

            } else if (mHelpFlag == ASK_HELP_FLAG &&
                    mActivityType != null &&
                    mActivityType.equals(AppPreferences.SharedPrefActivityRecognition.STILL)) {
                Log.d(TAG, "still and idle");
                if (!mIsStill) {
                    Log.d(TAG, "1st time sending still update");
                    sendLocationUpdate(lat, lng, AppPreferences.SharedPrefActivityRecognition.STILL, UPDATE_HOME);
                    mIsStill = true;
                } else {
                    Log.d(TAG, "later calling home");
                    RequestParams homeParams = CommonFunctions.setParams(new String[]{"home", mUserEmail}, mContext);
                    new SendLocationsAsyncTask(this).execute(homeParams);
                }
            }
        }
    }

    /*
     * call back method of interface ServerResponse in CommonResultAsyncTask
     */
    @Override
    public void IntegerResponse(int response, int flag) {
        if (response == NO_CONNECTION) {
            setNoConnectionView();
        }
        if (flag == ASK_HELP_FLAG) {

        } else if (flag == ASKED_HELP_FLAG) {

        } else if (flag == HELPING_FLAG) {

        }

    }

    /*
     * get other user location. This is a callback for the interface in the
     * sendlocations async task
     */
    @Override
    public void getData(int code, ArrayList<LocationDetailsModel> arrayList) {
        if (code == NO_CONNECTION) {
            setNoConnectionView();
        } else {
            if (mNoNetworkLinearLayout.getVisibility() == View.VISIBLE) {
                mNoNetworkLinearLayout.setVisibility(View.GONE);
            }
            fillMap(arrayList);
        }
    }


    // user defined methods


    // get a reference to the map id from the xml and connect to map object by
    // calling getMapAsync
    public void initializeMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(
                R.id.map);
        mapFragment.getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // building the googleapi
    public void buildGoogleApiClient() {
        Builder builder = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);
        if (!mIsEnabled) {
            builder.addApi(ActivityRecognition.API);
        }

        mGoogleApiClient = builder.build();
        mGoogleApiClient.connect();
    }

    // setting up location request
    public void settingUpMapLocationSource() {
        LocationRequest locationRequest = new LocationRequest();

        if (mIsHighAccuracy) {
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    // accessing views and button
    public void acessViews() {
        mNoNetworkLinearLayout = (LinearLayout) findViewById(R.id.map_linear_no_network);
        mNoNetworkLinearLayout.setVisibility(View.GONE);
        mPeopleTextView = (TextView) findViewById(R.id.text_people);
        mPeopleTextView.setText("searching...");
        mHelpButton = (Button) findViewById(R.id.button_help);
        mHelpButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHelpFlag == ASK_HELP_FLAG) {

                    if (mAnimation != null) {
                        mHelpButton.startAnimation(mAnimation);
                    }
                    removeMarkers();
                    mIsHighAccuracy = true;

                    resetAccuracyOfLocation();
                    RequestParams params = CommonFunctions.helpParams(
                            ASK_HELP_PATH, mUserEmail, mContext);
                    changeTextOfButton(ASKED_HELP);
                    mHelpFlag = ASKED_HELP_FLAG;
                    new CommonResultAsyncTask(MainActivity.this, ASK_HELP_TEXT,
                            ASK_HELP_FLAG).execute(params);
                    mPeopleTextView.setText("searching...");
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Intent viewIntent = new Intent(MainActivity.this,
                            MainActivity.class);
                    viewIntent.putExtra(AppPreferences.IntentExtras.HELP_EXTRA,
                            mHelpFlag);
                    PendingIntent viewPendingIntent = PendingIntent
                            .getActivity(MainActivity.this, 0, viewIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    Intent cancelIntent = new Intent(MainActivity.this,
                            MainActivity.class);
                    cancelIntent.putExtra(
                            AppPreferences.IntentExtras.HELP_EXTRA, 0);

                    PendingIntent cancelPendingIntent = PendingIntent
                            .getService(MainActivity.this, 0, cancelIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);

                    Notification notification = new Notification.Builder(
                            MainActivity.this)
                            .setContentTitle("Asked for help")
                            .setContentText(
                                    "click on cancel if you no longer need help. \n"
                                            + " click on view to locate helpers")
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setAutoCancel(false)
                            .addAction(R.drawable.cancel, "cancel",
                                    cancelPendingIntent)
                            .addAction(R.drawable.view, "view",
                                    viewPendingIntent).build();
                    notificationManager.notify(
                            AppPreferences.Flags.NOTIFICATION_FLAG,
                            notification);
                    notification.flags |= Notification.FLAG_NO_CLEAR;
                } else if (mHelpFlag == ASKED_HELP_FLAG) {
                    removeMarkers();
                    RequestParams params = CommonFunctions.helpParams(
                            HELP_RECEIVED_PATH, mUserEmail, mContext);
                    changeTextOfButton(ASK_HELP);
                    mHelpFlag = ASK_HELP_FLAG;
                    mIsHighAccuracy = false;
                    resetAccuracyOfLocation();
                    mHelpButton.clearAnimation();
                    new CommonResultAsyncTask(MainActivity.this, HELPED_TEXT,
                            ASKED_HELP_FLAG).execute(params);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager
                            .cancel(AppPreferences.Flags.NOTIFICATION_FLAG);
                    mPeopleTextView.setText("searching...");

                } else if (mHelpFlag == HELPING_FLAG) {
                    removeMarkers();
                    changeTextOfButton(ASK_HELP);
                    mHelpFlag = ASK_HELP_FLAG;
                    mIsHighAccuracy = false;
                    resetAccuracyOfLocation();
                    mHelpButton.clearAnimation();
                    RequestParams params = CommonFunctions.helpParams(
                            HELPED_PATH, mUserEmail, mContext);
                    new CommonResultAsyncTask(MainActivity.this, HELPED_TEXT,
                            HELPING_FLAG).execute(params);
                    mPeopleTextView.setText("searching...");
                }

            }
        });
    }

    /*
     * button animation method
     */
    public void buttonAnimation() {
        mAnimation = new AlphaAnimation(1, 0); // Change alpha from fully
        // visible
        // to invisible
        mAnimation.setDuration(500); // duration - half a second
        mAnimation.setInterpolator(new LinearInterpolator()); // do not alter
        // animation
        // rate
        mAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation
        // infinitely
        mAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
        // end so the button
        // will
        // fade back in

    }

    /*
     * resetting the text on button
     */
    public void changeTextOfButton(String text) {
        mHelpButton.setText(text);
    }

    /*
     * method to get stuff from intents
     */
    public void retriveIntentExtras(Intent intent) {
        if (intent.hasExtra(AppPreferences.IntentExtras.COORDINATES)) {
            mHelpFlag = HELPING_FLAG;
            mHelpButton.setAnimation(mAnimation);
            mIsHighAccuracy = true;
            changeTextOfButton(HELPING);
            mVictimUserEmail = intent
                    .getStringExtra(AppPreferences.IntentExtras.USERID);
            resetAccuracyOfLocation();
        } else if (intent.hasExtra(AppPreferences.IntentExtras.HELP_EXTRA)) {
            mHelpFlag = intent.getExtras().getInt(
                    AppPreferences.IntentExtras.HELP_EXTRA);
            settingTextOfButton(mHelpFlag);
        } else if (intent.hasExtra(Intent.ACTION_SEND)) {
            mHelpFlag = ASKED_HELP_FLAG;
            mHelpButton.startAnimation(mAnimation);
            mIsHighAccuracy = true;
            changeTextOfButton(ASKED_HELP);
            resetAccuracyOfLocation();
            RequestParams params = CommonFunctions.helpParams(
                    ASK_HELP_PATH, mUserEmail, mContext);
            new CommonResultAsyncTask(MainActivity.this, ASK_HELP_TEXT,
                    ASK_HELP_FLAG).execute(params);
            mPeopleTextView.setText("searching...");


        }

    }

	/*
     * method to reset the accuracy of how precise we want the location update
	 */

    public void resetAccuracyOfLocation() {
        stopLocationUpdates();
        startLocationUpdates();

    }

    public void startLocationUpdates() {
        if (mGoogleMap != null) {
            this.activate(mOnLocationChangeListener);
            mGoogleMap.setMyLocationEnabled(true);
            settingUpMapLocationSource();
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(false);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            this.deactivate();
        }
    }

    /*
     * filling map with users
     */
    public void fillMap(ArrayList<LocationDetailsModel> locations) {
        int count = locations.size();
        removeMarkers();

        if (count == 0) {
            if (mHelpFlag == ASK_HELP_FLAG) {
                mPeopleTextView.setText("0 people around you");
            } else if (mHelpFlag == ASKED_HELP_FLAG
                    || mHelpFlag == HELPING_FLAG) {
                mPeopleTextView.setText("0 people responded");
            }
        } else {
            if (count == 1) {
                if (mHelpFlag == ASK_HELP_FLAG) {
                    mPeopleTextView.setText("1 person around you");
                } else if (mHelpFlag == ASKED_HELP_FLAG
                        || mHelpFlag == HELPING_FLAG) {
                    mPeopleTextView.setText("0 people responded");
                }
            } else {
                if (mHelpFlag == ASK_HELP_FLAG) {
                    mPeopleTextView.setText(count + " persons around you");
                } else if (mHelpFlag == ASKED_HELP_FLAG
                        || mHelpFlag == HELPING_FLAG) {
                    if (count - 1 == 1) {
                        mPeopleTextView.setText("1" + " person responded");
                    } else {
                        mPeopleTextView.setText(count + " persons responded");
                    }

                }
            }
            for (LocationDetailsModel location : locations) {
                int color = location.getColor();
                if (color == AppPreferences.Flags.USER_COLOR_FLAG) {
                    Marker marker = mGoogleMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(location.getLatitude(),
                                                    location.getLongitude()))
                                    .title("user")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    mMarkers.add(marker);
                } else if (color == AppPreferences.Flags.HELPER_COLOR_FLAG
                        && !location.getUser_email().equals(mUserEmail)) {
                    Marker marker = mGoogleMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(location.getLatitude(),
                                                    location.getLongitude()))
                                    .title("helper")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMarkers.add(marker);
                } else if (color == AppPreferences.Flags.VICTIM_COLOR_FLAG
                        && !location.getUser_email().equals(mUserEmail)) {
                    Marker marker = mGoogleMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(location.getLatitude(),
                                                    location.getLongitude()))
                                    .title("victim")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMarkers.add(marker);
                }

            }

        }

    }

    public void settingTextOfButton(int flag) {
        if (flag == HELPING_FLAG) {
            mHelpButton.startAnimation(mAnimation);
            mHelpButton.setText(HELPING);
        } else if (flag == ASKED_HELP_FLAG) {
            mHelpButton.startAnimation(mAnimation);
            mHelpButton.setText(ASKED_HELP);
        } else if (flag == ASK_HELP_FLAG) {
            mHelpButton.setText(ASK_HELP);
        }
    }

    public void removeMarkers() {
        if (mMarkers.size() != 0) {
            for (Marker marker : mMarkers) {
                marker.remove();
            }
        }
    }

    public void setNoConnectionView() {
        mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
        mPeopleTextView.setText("No network connection");
        Button button = (Button) mNoNetworkLinearLayout.findViewById(R.id.map_button_retry);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonFunctions.isConnected(mContext)) {
                    mNoNetworkLinearLayout.setVisibility(View.GONE);
                    mPeopleTextView.setText("Searching...");
                }
            }
        });
    }

    public void settingUpActivityRecognition() {
        if (!mIsEnabled) {
            Toast.makeText(mContext, "enabling", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityRecognitionService.class);
            PendingIntent callbackIntent = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi
                    .requestActivityUpdates(mGoogleApiClient, // your connected
                            // GoogleApiClient
                            300000, // how often you want callbacks
                            callbackIntent); // the PendingIntent which will
            // receive updated activities
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "success in registering with activity recognition");
                        CommonFunctions
                                .saveActivityRecognitionPreference(mContext);
                    }
                }
            });
        }
    }

    public boolean getActivityRecognitionStatus() {
        return CommonFunctions.getSharedPreferences
                (mContext, AppPreferences.SharedPrefActivityRecognition.name).
                getBoolean(AppPreferences.SharedPrefActivityRecognition.enabled, false);
    }

    private BroadcastReceiver mActivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "activity received");
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mActivityType = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.d(TAG, "mActivityType->" + mActivityType);

            }
        }
    };

    public void setUpStillLocationUpdateAlarmManager() {
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(this, SingleLocationUpdateService.class);
        mPendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 1000 * 30, mPendingIntent);
    }

    public void stopAlarmManager() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mPendingIntent);
    }

    public void sendLocationUpdate(double lat, double lng, String activityType, String mode) {
        RequestParams updateLocationParams = CommonFunctions.buildLocationUpdateParams(mUserEmail, lat, lng, new String[]{activityType, mode}, mContext);
        if (mode.equals(UPDATE_HOME)) {
            new SendLocationsAsyncTask(MainActivity.this).execute(updateLocationParams);
        } else {
            new SendLocationsAsyncTask().execute(updateLocationParams);
        }

    }


}
