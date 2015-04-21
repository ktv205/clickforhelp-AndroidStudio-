package com.example.clickforhelp.controllers.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.services.UpdateWidgetService;
import com.example.clickforhelp.controllers.ui.MainActivity;

public class HelpWidgetReceiver extends AppWidgetProvider {
    private static final String TAG = HelpWidgetReceiver.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onupdate is called on appwidget");
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.help_appwidget);
            remoteViews.setTextViewText(R.id.help_widget_textview, "searching...");

            Intent helpIntent = new Intent(context, MainActivity.class);
            helpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            helpIntent.putExtra(Intent.ACTION_SEND, true);
            helpIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent helpPendingIntent = PendingIntent.getActivity(context, 0, helpIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.help_widget_button, helpPendingIntent);
            Intent peopleIntent = new Intent(context, UpdateWidgetService.class);
            peopleIntent.putExtra(Intent.ACTION_VIEW, true);
            peopleIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.startService(peopleIntent);
            Intent anotherPeopleIntent=new Intent(context,UpdateWidgetService.class);
            anotherPeopleIntent.putExtra(Intent.ACTION_VIEW, true);
            anotherPeopleIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent peoplePendingIntent = PendingIntent.getService(context, 0, anotherPeopleIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.help_widget_help_button, peoplePendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled is called on appwidget");

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted is called on appwidget");

    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled is called on appwidget");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }
}
