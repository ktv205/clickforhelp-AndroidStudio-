package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationUpdateService extends Service implements
        OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static final String SEND_SERVICE = "com.example.clickforhelp.controllers.LocationUpdateService";
    public boolean mIsHighAccuracy = false;
    private Context mContext;
    private static final String UPDATE = "u";
    private String mUserEmail = "example@nyu.edu";
    private static final String TAG = "LocationUpdateService";
    private String mActivityType=AppPreferences.SharedPrefActivityRecognition.WALKING;
    private boolean mIsStill = false;

    public int onStartCommand(Intent intent, int flags, int startId) {

        mContext = getApplicationContext();
        mUserEmail = CommonFunctions.getEmail(mContext);
        buildGoogleApiClient();
        registerActivityBroadcastReceiver();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        if (mIsHighAccuracy) {
            mLocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            mLocationRequest
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        startLocationUpdates();

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "onConnectionSuspended");


    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Log.d(TAG, "onConnectionFailed");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        try {
            this.unregisterReceiver(mActivityBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "illegal argument exception");
        }
    }

    @Override
    public void onLocationChanged(Location arg0) {
        if (!mActivityType.equals(AppPreferences.SharedPrefActivityRecognition.STILL)) {
            mIsStill = false;
            Log.d(TAG, "not still");
            sendLocationUpdate(arg0.getLatitude(), arg0.getLongitude(), AppPreferences.SharedPrefActivityRecognition.WALKING);
        } else {
            if (!mIsStill) {
                sendLocationUpdate(arg0.getLatitude(), arg0.getLongitude(), AppPreferences.SharedPrefActivityRecognition.STILL);
                mIsStill = true;
            }

            Log.d(TAG, "still in service");
        }

    }

    public void registerActivityBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityBroadcastReceiver,
                new IntentFilter("activity"));
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

    private void sendLocationUpdate(double lat, double lng, String activityType) {
        RequestParams locationParams = CommonFunctions
                .buildLocationUpdateParams(
                        mUserEmail,
                        lat,
                        lng,
                        new String[]{
                                activityType,
                                UPDATE}, mContext);
        new SendLocationsAsyncTask().execute(locationParams);
    }

}
