package com.example.clickforhelp.controllers.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by krishnateja on 4/19/2015.
 */
public class ActivityRecognitionService extends IntentService {

    private static final String TAG = ActivityRecognitionService.class.getSimpleName();
    private Context mContext;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ActivityRecognitionService() {
        super(ActivityRecognitionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mContext == null) {
            mContext = getApplicationContext();
        }
        String activity = null;
        if (intent != null) {
            ActivityRecognitionResult result = ActivityRecognitionResult
                    .extractResult(intent);
            if (result != null) {
                Log.d(TAG, "result is not null");

                DetectedActivity detectedActivity = result
                        .getMostProbableActivity();
                if (detectedActivity.getType() == DetectedActivity.IN_VEHICLE) {
                    activity = AppPreferences.SharedPrefActivityRecognition.VEHICLE;
                } else if (detectedActivity.getType() == DetectedActivity.ON_FOOT) {
                    activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
                } else if (detectedActivity.getType() == DetectedActivity.RUNNING) {
                    activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
                } else if (detectedActivity.getType() == DetectedActivity.STILL) {
                    activity = AppPreferences.SharedPrefActivityRecognition.STILL;
                } else if (detectedActivity.getType() == DetectedActivity.ON_BICYCLE) {
                    activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
                } else if (detectedActivity.getType() == DetectedActivity.UNKNOWN) {

                } else if (detectedActivity.getType() == DetectedActivity.WALKING) {
                    activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
                } else if (detectedActivity.getType() == DetectedActivity.TILTING) {

                }
                boolean serviceRunning = CommonFunctions.isMyServiceRunning(
                        LocationUpdateService.class, mContext);
                boolean activityRunning = CommonFunctions
                        .isActivityRunning(mContext);
                if ((serviceRunning || activityRunning) && activity!=null) {

                    Intent activityIntent = new Intent("activity");
                    activityIntent.putExtra(Intent.EXTRA_TEXT, activity);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(activityIntent);
                }


            }


        }
    }
}
