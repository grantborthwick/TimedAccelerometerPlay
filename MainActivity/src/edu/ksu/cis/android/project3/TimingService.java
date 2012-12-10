package edu.ksu.cis.android.project3;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TimingService extends Service implements SensorEventListener{
	private static NotificationManager mNotificationManager;
	private static SensorManager mSensorManager;
	private static long last;
	private static long step;
	private static long mazeStart;
	private static int TimeToComplete = 74;
	public static boolean isEnding;
	public static boolean isOn;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate(){
		super.onCreate();
		if(!isOn){
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ball)
			.setContentTitle("Timing Service")
			.setContentText("You'll never exit!")
			.setOngoing(true);
			isEnding = false;
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
			Toast.makeText(this,"Timing Service Started. Good Luck."+((Integer)android.os.Process.myPid()).toString(),Toast.LENGTH_SHORT).show();
			isOn = true;
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			last = System.currentTimeMillis();
			step = last+11000;
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	public static void mazeStart(){
		mazeStart = System.currentTimeMillis();
	}
	public static void mazeEnd(Context context){
		long now = System.currentTimeMillis();
		Toast.makeText(context, "You've escaped! Congratulations! It took you "+((Long)((now-mazeStart)/1000)).toString()+" seconds to complete the maze.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			long now = System.currentTimeMillis();
			if(now>step){
				if((now-last)/1000 > TimeToComplete){
					if(isEnding){
						mSensorManager.unregisterListener(this);
						android.os.Process.killProcess(android.os.Process.myPid());
					}else{
						isEnding = true;
						MediaPlayer mp = MediaPlayer.create(getBaseContext(),R.raw.beedleohhh);
						mp.start();
						step+=2000;
					}
				}else{
					long number = TimeToComplete - (now-last)/1000;
					String s = "Warning: you have "+((Long)number).toString()+" seconds to enter the sequence.";
					Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
					step+=4000;
				}
			}
		}
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		//Toast.makeText(this,"Service ended...", Toast.LENGTH_LONG).show();
		isOn = false;
		mNotificationManager.cancel(0);
		Toast.makeText(this,"Timing Service has Ended. Good Job!.",Toast.LENGTH_SHORT).show();
		mSensorManager.unregisterListener(this);
	}
	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
