package com.merseyside.admin.player.Utilities;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.merseyside.admin.player.ActivitesAndFragments.MainActivity;
import com.merseyside.admin.player.R;

/**
 * Created by Admin on 09.03.2017.
 */

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            PrintString.printLog("Widget", "onUpdate");
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.cover, pendingIntent);
            views.setOnClickPendingIntent(R.id.info, pendingIntent);

            Intent previousIntent = new Intent(context, PlaybackManager.class);
            previousIntent.setAction(ServiceConstants.ACTION.PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(context, 0,
                    previousIntent, 0);

            Intent playIntent = new Intent(context, PlaybackManager.class);
            playIntent.setAction(ServiceConstants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(context, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(context, PlaybackManager.class);
            nextIntent.setAction(ServiceConstants.ACTION.NEXT_ACTION);
            PendingIntent pnextIntent = PendingIntent.getService(context, 0,
                    nextIntent, 0);

            views.setOnClickPendingIntent(R.id.play, pplayIntent);
            views.setOnClickPendingIntent(R.id.prev, ppreviousIntent);
            views.setOnClickPendingIntent(R.id.forward, pnextIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
