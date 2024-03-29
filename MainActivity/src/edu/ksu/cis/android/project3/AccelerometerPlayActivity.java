/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.ksu.cis.android.project3;

import java.util.ArrayList;
import java.util.List;

import edu.ksu.cis.android.project3.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 * 
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */

public class AccelerometerPlayActivity extends Activity {

	final Context context = this;
	public SimulationView mSimulationView;
	private SensorManager mSensorManager;
	private PowerManager mPowerManager;
	private WindowManager mWindowManager;
	private Display mDisplay;
	private WakeLock mWakeLock;
    private boolean pause = false;
    private Button toggleAlarm;
    private boolean alarmOn;
    public boolean setResult = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();

		// Create a bright wake lock
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

		// instantiate our simulation view and set it as the activity's content
		mSimulationView = new SimulationView(this);
		Toast.makeText(this, "You've fallen down to level "+((Integer)mSimulationView.level).toString()+"!", 0).show();
		setContentView(mSimulationView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mSimulationView.TouchControl){mSimulationView.mBalls[0].TouchMode=5;}
		for(int i = 0; i<mSimulationView.NUM_PARTICLES;++i){
			int mx = mSimulationView.mBalls[i].mBoxX;
			int my = mSimulationView.mBalls[i].mBoxY;
			mSimulationView.Boxes[mx][my].isTrap = false;
			mSimulationView.mBalls[i].enabled = true;
			mSimulationView.mBalls[i].mAccelX=0;
			mSimulationView.mBalls[i].mAccelY=0;
		}
		mSimulationView.mLastT = mSimulationView.mSensorTimeStamp + (System.nanoTime() - mSimulationView.mCpuTimeStamp);
		pause = false;
		/*
		 * when the activity is resumed, we acquire a wake-lock so that the
		 * screen stays on, since the user will likely not be fiddling with the
		 * screen or buttons.
		 */
		mWakeLock.acquire();

		// Start the simulation
		mSimulationView.startSimulation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		pause = true;
		/*
		 * When the activity is paused, we make sure to stop the simulation,
		 * release our sensor resources and wake locks
		 */

		// Stop the simulation
		mSimulationView.stopSimulation();

		// and release our wake-lock
		mWakeLock.release();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode){
		case 1://risen as hoped. == request code.
			Toast.makeText(this,"You've risent to level "+((Integer)mSimulationView.level).toString() + "!",Toast.LENGTH_SHORT).show();
			break;
		case 0://no result code! Let everything die!
			finish();
			break;
		case -2:
			//There she is! Set for stun!
			setResult(resultCode);
			finish();
			break;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e){
		float x = e.getX();
		float y = e.getY();
		if(mSimulationView.TouchControl){
			x = (x-mSimulationView.metrics.widthPixels/2)/mSimulationView.xs;
			y = -(y-mSimulationView.metrics.heightPixels/2)/mSimulationView.ys;
			mSimulationView.mBalls[0].mLastPosX = mSimulationView.mBalls[0].mPosX;
			mSimulationView.mBalls[0].mLastPosY = mSimulationView.mBalls[0].mPosY;
			mSimulationView.mBalls[0].mPosX = x;
			mSimulationView.mBalls[0].mPosY = y;
			mSimulationView.mBalls[0].mAccelX = 0;
			mSimulationView.mBalls[0].mAccelY = 0;
			mSimulationView.mBalls[0].TouchMode = 5;
		}
		if(e.getY()>mSimulationView.getHeight()-mSimulationView.DisplayHeight){
			if(mSimulationView.AlarmModeCoolDown==0){
				if(!TimingService.isOn){
					startService(new Intent(AccelerometerPlayActivity.this,TimingService.class));
				}else{
					stopService(new Intent(AccelerometerPlayActivity.this,TimingService.class));
				}
			}
			mSimulationView.AlarmModeCoolDown = 5;
		}
		
		
		//Toast.makeText(this,"("+((Float)x).toString()+","+((Float)y).toString()+")",0).show();
		return false;
	}
	
	class SimulationView extends View implements SensorEventListener {		
		// friction of the virtual table and air
		private Sensor mAccelerometer;
		private long mLastT;
		private float mLastDeltaT;
		private float mXDpi;
		private float mYDpi;
		private float mMetersToPixelsX;
		private float mMetersToPixelsY;
		private Bitmap mBitmap;
		private Bitmap mWood;
		private Paint lineUp;
		private Paint lineAcross;
		private Paint TrapPaint;
		private Paint DisablePaint1;
		private Paint DisablePaint2;
		private float mXOrigin;
		private float mYOrigin;
		private float mSensorX;
		private float mSensorY;
		private long mSensorTimeStamp;
		private long mCpuTimeStamp;
		private float mHorizontalBound;
		private float mVerticalBound;
		private float xc;
		private float yc;
		private float xs;
		private float ys;
		private ParticleSystem mParticleSystem;
		private Box[][] Boxes;
		private int CellCountX;
		private int CellCountY;
		private float BallSize;
		private float sFriction;
		private float sBallDiameter;
		private float sBallDiameter2;
		public Particle mBalls[];
		private int TrapCount;
		private float TrapBoxRatio;
		private float boxHeight;
		private float boxWidth;
		private DisplayMetrics metrics;
		private float mazeHeightPixels;
		private float mazeWidthPixels;
		private int wallWidth;
		private int wallHeight;
		public int NUM_PARTICLES;
		private int level;
		private int AlarmModeCoolDown;
		private boolean AutomaticBorders;
		private boolean TouchControl;
		private long start;
		private long end;
		
		private int DisplayHeight;

		/*
		 * Each of our particle holds its previous and current position, its
		 * acceleration. for added realism each particle has its own friction
		 * coefficient.
		 */
		public class Particle {
			private float mPosX;
			private float mPosY;
			private float mAccelX;
			private float mAccelY;
			private float mLastPosX;
			private float mLastPosY;
			private float mOneMinusFriction;

			private int mBoxX;
			private int mBoxY;
			@SuppressWarnings("unused")
			private int mLastBoxX;
			@SuppressWarnings("unused")
			private int mLastBoxY;
			public int TouchMode;
			public boolean enabled;

			Particle() {
				// make each particle a bit different by randomizing its
				// coefficient of friction
				final float r = ((float) Math.random() - 0.5f) * 0.2f;
				mOneMinusFriction = 1f - sFriction + r;
				//Start particles out directly outside 0,0
				mPosX = -mHorizontalBound;
				mPosY = mVerticalBound;
				mLastPosX = -mHorizontalBound;
				mLastPosY = mVerticalBound;
				enabled = true;
				TouchMode = 0;
			}

			public void computePhysics(float sx, float sy, float dT, float dTC) {
				/*
				 * Time-corrected Verlet integration The position Verlet
				 * integrator is defined as x(t+�t) = x(t) + x(t) - x(t-�t) +
				 * a(t)�t�2 However, the above equation doesn't handle variable
				 * �t very well, a time-corrected version is needed: x(t+�t) =
				 * x(t) + (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add
				 * a simple friction term (f) to the equation: x(t+�t) = x(t) +
				 * (1-f) * (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2
				 */
				final float dTdT = dT * dT;
				final float x = mPosX + mOneMinusFriction * dTC	* (mPosX - mLastPosX) + mAccelX * dTdT;
				final float y = mPosY + mOneMinusFriction * dTC	* (mPosY - mLastPosY) + mAccelY * dTdT;
				mLastPosX = mPosX;
				mLastPosY = mPosY;
				mPosX = x;
				mPosY = y;
				mAccelX = -sx;
				mAccelY = -sy;
			}
				
			public void resolveCollisionWithBounds() {				
				if(!enabled){return;}
				float w = mazeWidthPixels;
				float h = mazeHeightPixels;
				float BoxW = w/CellCountX;
				float BoxH = h/CellCountY;
				float maxXPixels,minXPixels,maxYPixels,minYPixels;

				if(!Boxes[mBoxX][mBoxY].hasRight){maxXPixels = (mBoxX+1)*BoxW - w/2-1;}
				else{maxXPixels = (mBoxX+2)*BoxW - w/2-1;}
				if(!Boxes[mBoxX][mBoxY].hasLeft){minXPixels = (mBoxX)*BoxW - w/2+1;}
				else{minXPixels = (mBoxX-1)*BoxW - w/2+1;}
				if(!Boxes[mBoxX][mBoxY].hasUp){maxYPixels = (mBoxY)*BoxH - h/2+1;}
				else{maxYPixels = (mBoxY-1)*BoxH - h/2+1;}
				if(!Boxes[mBoxX][mBoxY].hasDown){minYPixels = (mBoxY+1)*BoxH - h/2+1;}
				else{minYPixels = (mBoxY+2)*BoxH - h/2+1;}

				float xmax = Math.min(maxXPixels/xs - sBallDiameter/2,mHorizontalBound);
				float xmin = Math.max(minXPixels/xs + sBallDiameter/2,-mHorizontalBound);
				float ymax = Math.min(-maxYPixels/ys - sBallDiameter/2,mVerticalBound);
				float ymin = Math.max(-minYPixels/ys + sBallDiameter/2,-mVerticalBound);

				float x = mPosX;
				float y = mPosY;
				mLastPosX=mPosX;
				mLastPosY=mPosY;
				//Might switch this back to main edges, or change to allow outside of bounds area.
				if (x > xmax) {	mPosX = xmax; } 
				else if (x < xmin) { mPosX = xmin; }
				if (y > ymax) {	mPosY = ymax; } 
				else if (y < ymin) { mPosY = ymin; }
				//Need to figure out which boxes must be passed through and then which borders need to be checked.
				@SuppressWarnings("unused")
				int NewXBox = getBoxXFromPixel(xc + x*xs);
				@SuppressWarnings("unused")
				int NewYBox = getBoxYFromPixel(yc - y*ys);
				mLastBoxX = mBoxX;
				mLastBoxY = mBoxY;
				mBoxX = getBoxXFromPixel(xc + mPosX*xs);
				mBoxY = getBoxYFromPixel(yc - mPosY*ys);
				if(Boxes[mBoxX][mBoxY].isTrap){
					if(level==7 && mBoxX==0 && mBoxY == CellCountY-1){
						float a = mPosX;
						float a1 = -mHorizontalBound;
						float b = mPosY;
						float b1 = -mVerticalBound;
						if(mPosX<-mHorizontalBound+sBallDiameter &&mPosY<-mVerticalBound+sBallDiameter){
							//Boxes[mBoxX][mBoxY].isTrap=false;
							stopService(new Intent(AccelerometerPlayActivity.this,TimingService.class));
						}
					}
					else{
						enabled = false;
						if(!pause){
							boolean more = false;
							for(int i=0;i<NUM_PARTICLES;++i){
								more|=mBalls[i].enabled;
							}
							if(!more){
								Intent levelDown = new Intent(context, AccelerometerPlayActivity.class);
								Bundle parem = SetParameters();
								levelDown.putExtras(parem);
								end = mSensorTimeStamp	+ (System.nanoTime() - mCpuTimeStamp);
								((Activity)context).startActivityForResult(levelDown,1);
								pause=true;
							}
						}
					}
				}
				if(Boxes[mBoxX][mBoxY].isGoal){
					if(!pause){
						pause = true;
						Intent intent = getIntent();
						setResult(1, intent);
						setResult = true;
						finish();
					}
				}
			}
		
			public void resolveCollisionWithBoundsClassic() {
                final float xmax = mHorizontalBound;
                final float ymax = mVerticalBound;
                final float x = mPosX;
                final float y = mPosY;
                if (x > xmax) {
                    mPosX = xmax;
                } else if (x < -xmax) {
                    mPosX = -xmax;
                }
                if (y > ymax) {
                    mPosY = ymax;
                } else if (y < -ymax) {
                    mPosY = -ymax;
                }
            }
		}

		/*
		 * A particle system is just a collection of particles
		 */
		class ParticleSystem {
			ParticleSystem() {
				/*
				 * Initially our particles have no speed or acceleration
				 */
				mBalls = new Particle[NUM_PARTICLES];
				for (int i = 0; i < NUM_PARTICLES; i++) {
					mBalls[i] = new Particle();
				}
			}

			/*
			 * Update the position of each particle in the system using the
			 * Verlet integrator.
			 */
			private void updatePositions(float sx, float sy, long timestamp) {
				final long t = timestamp;
				if (mLastT != 0) {
					final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
					if (mLastDeltaT != 0) {
						final float dTC = dT / mLastDeltaT;
						final int count = mBalls.length;
						for (int i = 0; i < count && !pause; i++) {
							if(mBalls[i].TouchMode>0){mBalls[i].TouchMode --;}
							else if(mBalls[i].enabled){mBalls[i].computePhysics(sx, sy, dT, dTC);}
						}
					}
					mLastDeltaT = dT;
				}
				mLastT = t;
			}

			/*
			 * Performs one iteration of the simulation. First updating the
			 * position of all the particles and resolving the constraints and
			 * collisions.
			 */
			public void update(float sx, float sy, long now) {
				// update the system's positions
				updatePositions(sx, sy, now);

				// We do no more than a limited number of iterations
				final int NUM_MAX_ITERATIONS = 10;

				/*
				 * Resolve collisions, each particle is tested against every
				 * other particle for collision. If a collision is detected the
				 * particle is moved away using a virtual spring of infinite
				 * stiffness.
				 */
				boolean more = true;
				final int count = mBalls.length;
				for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
					more = false;
					for (int i = 0; i < count; i++) {
						Particle curr = mBalls[i];
						if(curr.enabled){
							for (int j = i + 1; j < count; j++) {
								if(mBalls[j].enabled){
									Particle ball = mBalls[j];
									if(!ball.enabled){continue;}
									float dx = ball.mPosX - curr.mPosX;
									float dy = ball.mPosY - curr.mPosY;
									float dd = dx * dx + dy * dy;
									// Check for collisions
									if (dd <= sBallDiameter2) {
										/*
									 	* add a little bit of entropy, after nothing is
									 	* perfect in the universe.
									 	*/
										dx += ((float) Math.random() - 0.5f) * 0.0001f;
										dy += ((float) Math.random() - 0.5f) * 0.0001f;
										dd = dx * dx + dy * dy;
										// simulate the spring
										final float d = (float) Math.sqrt(dd);
										final float c = (0.5f * (sBallDiameter - d))
												/ d;
										curr.mPosX -= dx * c;
										curr.mPosY -= dy * c;
										ball.mPosX += dx * c;
										ball.mPosY += dy * c;
										more = true;
									}
								}
							}
							/*
							 * Finally make sure the particle doesn't intersects
							 * with the walls.
							 */
							curr.resolveCollisionWithBounds();
						}
					}
				}
			}

			public int getParticleCount() {return mBalls.length;}
			public float getPosX(int i) {return mBalls[i].mPosX;}
			public float getPosY(int i) {return mBalls[i].mPosY;}
			public int getBoxX(int i) {return mBalls[i].mBoxX;}
			public int getBoxY(int i) {return mBalls[i].mBoxY;}
		}

		public void startSimulation() {
			/*
			 * It is not necessary to get accelerometer events at a very high
			 * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
			 * automatic low-pass filter, which "extracts" the gravity component
			 * of the acceleration. As an added benefit, we use less power and
			 * CPU resources.
			 */
			mSensorManager.registerListener(this, mAccelerometer,
					(SensorManager.SENSOR_DELAY_UI));
		}
		public int getBoxXFromMeter(float i) {return (int) (i *xs / (mazeWidthPixels/CellCountX));}
		public int getBoxYFromMeter(float i) {return (int) (i *ys/ (mazeHeightPixels/CellCountY));}
		public int getBoxXFromPixel(float i) {return (int) (i / (mazeWidthPixels/CellCountX));}
		public int getBoxYFromPixel(float i) {return (int) (i / (mazeHeightPixels/CellCountY));}

		public void stopSimulation() {mSensorManager.unregisterListener(this);}
		public SimulationView(Context context) {
			super(context);
			metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			mXDpi = metrics.xdpi;
			mYDpi = metrics.ydpi;
			mMetersToPixelsX = mXDpi / 0.0254f;
			mMetersToPixelsY = mYDpi / 0.0254f;
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sFriction = 0.1f;
			GetParameters();
			// Create graphics array
			Boxes = new Box[CellCountX][CellCountY];
			//Generate Maze
			GenerateMaze2(CellCountX,CellCountY, TrapCount, Boxes);
			Options opts = new Options();
			opts.inDither = true;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			mWood = BitmapFactory.decodeResource(getResources(),R.drawable.wood, opts);
			mWood = Bitmap.createScaledBitmap(mWood, (int)mazeWidthPixels,(int)mazeHeightPixels, true);
		}

		private void GetParameters() {
			start = mSensorTimeStamp	+ (System.nanoTime() - mCpuTimeStamp);
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			CellCountX = bundle.getInt("CellCountX");
			CellCountY = bundle.getInt("CellCountY");
			CellCountX = Math.max(Math.max(CellCountX, 4),CellCountY/2);
			CellCountY = Math.max(Math.max(CellCountY, 6),CellCountX/2);
			NUM_PARTICLES = bundle.getInt("NUM_PARTICLES");
			
			TrapBoxRatio = bundle.getFloat("TrapBoxRatio");
			if(TrapBoxRatio==0){TrapCount = 0;}
			else{TrapCount = (int)(CellCountX*CellCountY/TrapBoxRatio);}
			DisplayHeight = bundle.getInt("DisplayHeight");
			level = bundle.getInt("level");
			
			mazeHeightPixels = metrics.heightPixels - DisplayHeight;
			mazeWidthPixels = metrics.widthPixels;
			
			TouchControl = bundle.getBoolean("TouchControl");
			AutomaticBorders = bundle.getBoolean("AutomaticBorders");
			if(AutomaticBorders){
				wallWidth = (int)(mazeWidthPixels/(10*CellCountX));
				wallHeight = (int)(mazeHeightPixels/(10*CellCountY));
			}else{
				wallWidth = bundle.getInt("wallWidth");
				wallHeight = bundle.getInt("wallHeight");
			}
			boxHeight = ((mazeHeightPixels-wallHeight) / CellCountY)-wallHeight;
			boxWidth = (mazeWidthPixels - wallWidth) / CellCountX - wallWidth;
			BallSize = bundle.getFloat("BallSize");
			sBallDiameter = Math.min(boxHeight/mMetersToPixelsY,boxWidth/mMetersToPixelsX)*BallSize;
			sBallDiameter2 = sBallDiameter * sBallDiameter;
			
			lineUp = new Paint();
			lineUp.setColor(Color.YELLOW);
			lineUp.setStrokeWidth(wallWidth);
			lineAcross = new Paint();
			lineAcross.setColor(Color.YELLOW);
			lineAcross.setStrokeWidth(wallHeight);
			TrapPaint = new Paint();
			TrapPaint.setColor(Color.BLACK);
			DisablePaint1 = new Paint();
			DisablePaint2 = new Paint();
			DisablePaint1.setStrokeWidth(1);
			DisablePaint2.setStrokeWidth(1);
			DisablePaint1.setColor(Color.BLACK);
			DisablePaint2.setColor(Color.GREEN);
			
			int ballHeight = (int) (sBallDiameter * mMetersToPixelsY + .5f);
			int ballWidth = (int) (sBallDiameter * mMetersToPixelsX + .5f);
			// rescale the ball so it's about 0.5 cm on screen
			Bitmap ball = BitmapFactory.decodeResource(getResources(),R.drawable.ball);
			mBitmap = Bitmap.createScaledBitmap(ball, ballWidth, ballHeight,true);
			
			mXOrigin = (mazeWidthPixels - mBitmap.getWidth()) * 0.5f;
			mYOrigin = (mazeHeightPixels - mBitmap.getHeight()) * 0.5f;
			mHorizontalBound = (((mazeWidthPixels-2*wallWidth)/ mMetersToPixelsX - sBallDiameter) * 0.5f);
			mVerticalBound = (((mazeHeightPixels-2*wallHeight) / mMetersToPixelsY - sBallDiameter) * 0.5f);
			xc = mXOrigin;
			yc = mYOrigin;
			xs = mMetersToPixelsX;
			ys = mMetersToPixelsY;
			
			mParticleSystem = new ParticleSystem();
		}
		private Bundle SetParameters() {
			Bundle parem = new Bundle();
			parem.putInt("CellCountY", (int)(CellCountY-2));
			parem.putInt("CellCountX", (int)(CellCountX-2));
			parem.putInt("NUM_PARTICLES",NUM_PARTICLES);
			parem.putFloat("BallSize", BallSize);
			parem.putFloat("TrapBoxRatio", TrapBoxRatio);
			parem.putBoolean("AutomaticBorders", AutomaticBorders);
			parem.putBoolean("TouchControl", TouchControl);
			parem.putInt("level",level+1);
			parem.putInt("DisplayHeight", DisplayHeight);
			parem.putInt("wallHeight",wallHeight);
			parem.putInt("wallWidth",wallWidth);
			return parem;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			// compute the origin of the screen relative to the origin of
			// the bitmap
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
			/*
			 * record the accelerometer data, the event's timestamp as well as
			 * the current time. The latter is needed so we can calculate the
			 * "present" time during rendering. In this application, we need to
			 * take into account how the screen is rotated with respect to the
			 * sensors (which always return data in a coordinate space aligned
			 * to with the screen in its native orientation).
			 */

			switch (mDisplay.getRotation()) {
			case Surface.ROTATION_0:
				mSensorX = event.values[0];
				mSensorY = event.values[1];
				break;
			case Surface.ROTATION_90:
				mSensorX = -event.values[1];
				mSensorY = event.values[0];
				break;
			case Surface.ROTATION_180:
				mSensorX = -event.values[0];
				mSensorY = -event.values[1];
				break;
			case Surface.ROTATION_270:
				mSensorX = event.values[1];
				mSensorY = -event.values[0];
				break;
			}

			mSensorTimeStamp = event.timestamp;
			mCpuTimeStamp = System.nanoTime();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if(AlarmModeCoolDown>0){AlarmModeCoolDown --;}
			/*
			 * draw the background
			 */
			canvas.drawBitmap(mWood, 0, 0, null);
			
			// Draw Walls
			float boxWidth = this.boxWidth;
			float boxHeight = this.boxHeight;
			for (int i = 0; i < Boxes.length; i++) {
				for (int j = 0; j < Boxes[0].length; j++) {
					if (!Boxes[i][j].hasDown)
					{
						canvas.drawLine(
							(boxWidth+wallWidth)*i,								(boxHeight+wallHeight)*(j+1)+wallHeight/2,
							(boxWidth+wallWidth)*(i+1)+wallWidth-1f,			(boxHeight+wallHeight)*(j+1)+wallHeight/2,lineAcross
								);
					}
					if (!Boxes[i][j].hasRight){
						canvas.drawLine(
							(boxWidth+wallWidth)*(i+1)+wallWidth/2,				(boxHeight+wallHeight)*j,
							(boxWidth+wallWidth)*(i+1)+wallWidth/2,				(boxHeight+wallHeight)*(j+1)+wallHeight-1f,lineUp
							);
					}
					//Traps!
					if(Boxes[i][j].isTrap&&!(level==7&&i==0&&j==CellCountY-1)){
						canvas.drawCircle(i*(wallWidth+boxWidth)+wallWidth + boxWidth/2, j*(wallHeight+boxHeight)+wallHeight+boxHeight/2, Math.min(boxWidth/2,boxHeight/2), TrapPaint);
					}
				}
			}
			if(level == 7 && TimingService.isOn){
				for(int i = (int)(Math.min(boxWidth,boxHeight)/4); i>0;i-=3){
					if(i%2==0){canvas.drawCircle(wallWidth + boxWidth/2, (CellCountY-1)*(wallHeight+boxHeight)+wallHeight+boxHeight/2, i, DisablePaint1);}
					else{canvas.drawCircle(wallWidth + boxWidth/2, (CellCountY-1)*(wallHeight+boxHeight)+wallHeight+boxHeight/2, i, DisablePaint2);}
				}
			}
			// Draw Borders
			canvas.drawLine(0, wallHeight/2, mazeWidthPixels, wallHeight/2, lineAcross);
			canvas.drawLine(wallWidth/2, 0, wallWidth/2, mazeHeightPixels, lineUp);

			//Start and end text.
			canvas.drawText("START", boxWidth/2, boxHeight/2, lineUp);
			canvas.drawText("END!!", mazeWidthPixels-boxWidth/2, mazeHeightPixels-boxHeight/2, lineUp);

			/*
			 * compute the new position of our object, based on accelerometer
			 * data and present time.
			 */

			final ParticleSystem particleSystem = mParticleSystem;
			final long now = mSensorTimeStamp
					+ (System.nanoTime() - mCpuTimeStamp);
			final float sx = mSensorX;
			final float sy = mSensorY;
			particleSystem.update(sx, sy, now);

			final Bitmap bitmap = mBitmap;
			for (int i = 0; i < NUM_PARTICLES; i++) {
				/*
				 * We transform the canvas so that the coordinate system matches
				 * the sensors coordinate system with the origin in the center
				 * of the screen and the unit is the meter.
				 */
				float x = 0;
				float y = 0;
				if(mBalls[i].enabled){
					x = xc + particleSystem.getPosX(i) * xs;
					y = yc - particleSystem.getPosY(i) * ys;
					canvas.drawBitmap(bitmap, x, y, null);
				}
				String s = "";
				s += "Level "+((Integer)level).toString()+" ";
				s += "("
					+ ((Integer)((Float)x).intValue()).toString()+ ","
					+ ((Integer)((Float)y).intValue()).toString() + ") ("
					+ ((Integer) particleSystem.getBoxX(i)).toString()+ ","
					+ ((Integer) particleSystem.getBoxY(i)).toString()+ ") ";
				//s+="("+((Float)mBalls[0].mPosX).toString()+", "+((Float)mBalls[0].mPosY).toString()+") ";
				s+=((Long)((now-start)/1000000000)).toString() + " seconds.";
				String s2;
				if(TimingService.isOn){s2=" Timer Service is On. Slide down here to Deactivate.";}
				else{s2=" Timer Service is Off. Slide down here to Activate.";}
				canvas.drawText(s, 0, mazeHeightPixels+13, lineUp);
				canvas.drawText(s2,mazeWidthPixels/2-150,mazeHeightPixels+13,lineUp);

			}

			// and make sure to redraw asap
			invalidate();
		}
		
		public void GenerateMaze2(int CellCountX, int CellCountY, int TrapCount, Box[][] Boxes ) {
			boolean valid;
			do{
				valid = false;
				for (int i = 0; i < CellCountX; i++) {
					for (int j = 0; j < CellCountY; j++) {
						Boxes[i][j]=new Box(i,j);
					}
				}
				for(int i = 0; i<CellCountX;++i){
					for(int j=0;j<CellCountY;++j){
						if(i!=CellCountX-1){Boxes[i][j].Right = Boxes[i+1][j]; Boxes[i+1][j].Left = Boxes[i][j];}
						if(j!=CellCountY-1){Boxes[i][j].Down = Boxes[i][j+1]; Boxes[i][j+1].Up = Boxes[i][j];}
					}
				}
				Boxes[CellCountX-1][CellCountY-1].isGoal = true;
				int DoNotWorryAboutThisItsFineImSureItsNothing;
				if(level == 7){DoNotWorryAboutThisItsFineImSureItsNothing=1;}
				else{DoNotWorryAboutThisItsFineImSureItsNothing=0;}
				for(int i = 0; i < TrapCount+DoNotWorryAboutThisItsFineImSureItsNothing; ++i){
					boolean again;
					do{
						int x = (int)(Math.random()*CellCountX);
						int y = (int)(Math.random()*CellCountY);
						if(level==7 && i == 1){x = 0; y = CellCountY-1;}
						again = (x==0&&y==0)||
								(x == CellCountX-1 && y == CellCountY-1) && !Boxes[x][y].isTrap ||
								(x>0 && Boxes[x-1][y].isTrap) ||
								(x<CellCountX-1 && Boxes[x+1][y].isTrap) ||
								(y>0 && Boxes[x][y-1].isTrap) ||
								(y<CellCountY-1 && Boxes[x][y+1].isTrap);
						if (!again){
							Boxes[x][y].isTrap=true;
							if(x>0){
								Boxes[x][y].addNeighbor(Boxes[x-1][y]);
								Boxes[x-1][y].addNeighbor(Boxes[x][y]);
							}if(x<CellCountX-1){
								Boxes[x][y].addNeighbor(Boxes[x+1][y]);
								Boxes[x+1][y].addNeighbor(Boxes[x][y]);
							}if(y>0){
								Boxes[x][y].addNeighbor(Boxes[x][y-1]);
								Boxes[x][y-1].addNeighbor(Boxes[x][y]);
							}if(y<CellCountY-1){
								Boxes[x][y].addNeighbor(Boxes[x][y+1]);
								Boxes[x][y+1].addNeighbor(Boxes[x][y]);
							}
						}
					} while(again);
				}
				List<Box> Nodes = new ArrayList<Box>();
				Nodes.add(Boxes[0][0]);
				Boxes[0][0].visited = true;
				while (!Nodes.isEmpty()){
					int i = (int)(Math.random()*Nodes.size());
					Box Node = Nodes.get(i);
					Box NextNode = Node.getRandomUnvisitedNeighbor(CellCountX,CellCountY);
					if(NextNode.x==-1){
						Nodes.remove(i);
					}else{
						NextNode.visited = true;
						valid |= NextNode.isGoal;
						Node.addNeighbor(NextNode);
						NextNode.addNeighbor(Node);
						Nodes.add(NextNode);
					}
				}
			}while(!valid);
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}

	public class Box{
		public boolean visited;
		public boolean isGoal;
		public boolean hasLeft;
		public boolean hasRight;
		public boolean hasUp;
		public boolean hasDown;
		public boolean isTrap;
		public float TrapRadius;
		public Box Left;
		public Box Right;
		public Box Up;
		public Box Down;
		public int numNeighbors;
		int x,y;

		public Box(int i, int j){
			visited = false;
			isGoal = false;
			isTrap=false;
			x=i;
			y=j;
		}
		public void addLeftNeighbor(Box i){
			Left = i;
			hasLeft = (i!=null);
		}
		public void addRightNeighbor(Box i){
			Right = i;
			hasRight = (i!=null);
		}
		public void addUpNeighbor(Box i){
			Up = i;
			hasUp = (i!=null);
		}
		public void addDownNeighbor(Box i){
			Down = i;
			hasDown = (i!=null);
		}
		public void addNeighbor(Box i){//Please don't pass incorrect boxes.
			if(x>i.x){addLeftNeighbor(i);}
			else if(x<i.x){addRightNeighbor(i);
			}else if (y<i.y){addDownNeighbor(i);}
			else{addUpNeighbor(i);}
		}
		public boolean Validate() {
			visited = true;
			return !isTrap && 
					(isGoal || 
					(hasLeft && !Left.visited && !Left.isTrap && Left.Validate()) || 
					(hasRight && !Right.visited && !Right.isTrap && Right.Validate())||
					(hasUp && !Up.visited && !Up.isTrap && Up.Validate()) ||
					(hasDown && !Down.visited && !Down.isTrap && Down.Validate()));		
		}
		public Box getRandomUnvisitedNeighbor(int CellCountX, int CellCountY){
			int count = 0;
			Box[] Neighbors = new Box[4];
			if(!hasLeft && x>0 &&!Left.visited && !Left.isTrap){
				Neighbors[count++] = Left;}
			if(!hasRight && x<CellCountX-1 && !Right.visited && !Right.isTrap){
				Neighbors[count++] = Right;}
			if(!hasUp && y>0 && !Up.visited && !Up.isTrap){
				Neighbors[count++] = Up;}
			if(!hasDown && y<CellCountY-1 && !Down.visited && !Down.isTrap){
				Neighbors[count++] = Down;}
			if(count==0){Neighbors[0] = new Box(-1,-1);}
			return Neighbors[(int)(Math.random()*count)];
		}
	}
	@Override
	public void onBackPressed(){
		setResult(-2,getIntent());
		super.onBackPressed();
	}
	
}