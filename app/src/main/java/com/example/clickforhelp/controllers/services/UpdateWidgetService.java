package com.example.clickforhelp.controllers.services;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;

import java.util.ArrayList;

/**
 * Created by krishnateja on 4/20/2015.
 */
public class UpdateWidgetService extends Service implements SendLocationsAsyncTask.GetOtherUsersLocations {

    private static final String TAG =UpdateWidgetService.class.getSimpleName();
    private int[] mAppIds;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(intent!=null && intent.hasExtra(Intent.ACTION_VIEW)) {
            mAppIds=intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            RequestParams homeParams = CommonFunctions.setParams(new String[]{"home", CommonFunctions.getEmail(getApplicationContext())},getApplicationContext());
            new SendLocationsAsyncTask(this).execute(homeParams);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void getData(int code, ArrayList<LocationDetailsModel> arrayList) {
        int len=arrayList.size();
        AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(this);
        for(int appId:mAppIds){
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.help_appwidget);
            remoteViews.setTextViewText(R.id.help_widget_textview, len +" people around");
            appWidgetManager.updateAppWidget(appId,remoteViews);
        }
        stopSelf();
    }
}
