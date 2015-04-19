package com.example.clickforhelp.controllers.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask.GetOtherUsersLocations;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelperActivity extends ActionBarActivity implements
        OnConnectionFailedListener, ConnectionCallbacks, GetOtherUsersLocations {

    // private static final String TAG = HelperActivity.class.getSimpleName();
    private Context mContext;
    public static final int SLEEP_AUTHENTICATION = 3000;
    private Timer mTimer;
    private Handler mHandler;
    private GoogleApiClient mGoogleApiClient;
    private String SENDER_ID = AppPreferences.GOOGLEREGID;
    private GoogleCloudMessaging mGcm;
    private String mRegid;
    // private static final String UPDATE_HOME = "uh";
    private static final String TAG = HelperActivity.class.getSimpleName();
    private String userEmail;
    private static final String UPDATE_GCM = "updategcm";
    private static final String UPDATE = "u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        overridePendingTransition(0, 0);

        setContentView(R.layout.activity_helper);
        //setActionBar();
        mHandler = new Handler();
        mTimer = new Timer();
        if (CommonFunctions.checkLoggedIn(mContext)) {
            if (CommonFunctions.isMyServiceRunning(
                    LocationUpdateService.class, mContext)) {
                stopService(new Intent(this, LocationUpdateService.class));
            }
            userEmail = CommonFunctions.getEmail(mContext);
            if (CommonFunctions.checkIfGCMInfoIsSent(mContext)) {
                // nothing to do if already info is sent
                startGoogleApiClient();
            } else {
                registerInBackground();
                startGoogleApiClient();
            }

        } else {
            pauseActiviy();
        }


    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        super.overridePendingTransition(enterAnim, exitAnim);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

	/*
     *
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 * 
	 * GoogleApiClient.isConnect() is called it will give you three methods that
	 * returns the success or failure or suspended
	 */

    @Override
    public void onConnected(Bundle arg0) {
        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (location != null) {
            RequestParams locationParams = CommonFunctions
                    .buildLocationUpdateParams(
                            userEmail,
                            location.getLatitude(),
                            location.getLongitude(),
                            new String[]{
                                    AppPreferences.SharedPrefActivityRecognition.WALKING,
                                    UPDATE},mContext);
                new SendLocationsAsyncTask(HelperActivity.this)
                        .execute(locationParams);
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
       Log.d(TAG,"onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
           Log.d(TAG,"onConnectionFailed");
    }

    // user defined methods

	/*
	 * changing the title icon back to white from the launcher's purple
	 * setActionBar()
	 */

    public void setActionBar() {
        ActionBar bar = getActionBar();
        bar.setIcon(R.drawable.nyu_white);
    }



	/*
	 * The method goToAuthentication will send the user to sign up page if he is
	 * not logged in pauseActivity() is to let the activity wait for a while so
	 * that the transaction wont take place fast
	 */

    public void goToAuthenticationActivity() {
        startActivity(new Intent(mContext, AuthenticationActivity.class));
        finish();
    }

    public void pauseActiviy() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        goToAuthenticationActivity();
                    }
                });

            }
        };
        mTimer.schedule(timerTask, SLEEP_AUTHENTICATION);

    }

	/*
	 * this method is to create a GoogleApiClient and call connect on it
	 */

    public void startGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

	/*
	 * just sending the user to mainactivity after grabbing his initial location
	 * 
	 */

    @Override
    public void getData(int Code,ArrayList<LocationDetailsModel> locations) {
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
        finish();

    }

    // getting and Sending gcm to server

    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    mRegid = mGcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + mRegid;
                    Log.d(TAG, msg);
                    sendRegistrationIdToBackend(mRegid);
                    CommonFunctions.storeRegistrationId(mContext, mRegid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

        }.execute();
    }

    /*
     * This the method where we create the params that are required to build the
     * url to be called to send the regid to the server
     */
    private void sendRegistrationIdToBackend(String regid) {
        String[] paths = {UPDATE_GCM, userEmail, regid};
        RequestParams params = CommonFunctions.setParams(paths,mContext);
        HttpManager.sendUserData(params);
    }
}
