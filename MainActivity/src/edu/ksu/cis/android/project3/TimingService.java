package edu.ksu.cis.android.project3;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class TimingService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate(){
		super.onCreate();
		Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ball)
		.setContentTitle("My Notification")
		.setContentText("Hello World!");
		Intent resultIntent = new Intent(this, AccelerometerPlayActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(AccelerometerPlayActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		Toast.makeText(this,"Service ended...", Toast.LENGTH_LONG).show();
	}
}
