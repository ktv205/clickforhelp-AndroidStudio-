package com.example.clickforhelp.controllers.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by krishnateja on 4/20/2015.
 */
public class SingleLocationUpdateService extends IntentService implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = SingleLocationUpdateService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private static final String UPDATE = "u";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SingleLocationUpdateService() {
        super(SingleLocationUpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "alarm to send update");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        sendLocationUpdate(location);
        mGoogleApiClient.disconnect();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void sendLocationUpdate(Location location) {
        RequestParams locationParams = CommonFunctions
                .buildLocationUpdateParams(
                        CommonFunctions.getEmail(getApplicationContext()),
                        location.getLatitude(),
                        location.getLongitude(),
                        new String[]{
                                AppPreferences.SharedPrefActivityRecognition.STILL,
                                UPDATE}, getApplicationContext());
        new SendLocationsAsyncTask().execute(locationParams);
    }
}
