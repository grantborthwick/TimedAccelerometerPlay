package edu.ksu.cis.android.project3;

import edu.ksu.cis.android.project3.R;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Context context = this;
	TextView textRows;
	TextView textColumns;
	TextView textBalls;
	TextView textTraps;
	TextView textBallSize;
	TextView textDisplayHeight;
	TextView textWallWidth;
	TextView textWallHeight;
	CheckBox checkAutomaticBorders;
	CheckBox checkTouchControl;
	Button buttonAlarm;
	private int width;
	private int height;
	public int maxCellsX;
	public int maxCellsY;
	public int minCellsX;
	public int minCellsY;
	private DisplayMetrics metrics;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		textRows = (TextView) findViewById(R.id.editText2);
		textColumns = (TextView) findViewById(R.id.editText1);
		textBalls = (TextView) findViewById(R.id.editText3);
		textTraps = (TextView) findViewById(R.id.editText4);
		textBallSize = (TextView) findViewById(R.id.editText5);
		textDisplayHeight = (TextView) findViewById(R.id.editText6);
		textWallWidth = (TextView) findViewById(R.id.editText7);
		textWallHeight = (TextView) findViewById(R.id.editText8);
		checkAutomaticBorders = (CheckBox) findViewById(R.id.checkBox2);
		checkTouchControl = (CheckBox) findViewById(R.id.checkBox3);
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {	
		public void onClick(View arg0){			
			TimingService.mazeStart();
			Intent levelDown = new Intent(context, AccelerometerPlayActivity.class);
			Bundle parem = setParameters();
			levelDown.putExtras(parem);
			((Activity)context).startActivityForResult(levelDown, 1);
			}			
		});
		Button buttonQuit = (Button) findViewById(R.id.button2);
		buttonQuit.setOnClickListener(new OnClickListener() {public void onClick(View arg0){finish();}});
		buttonAlarm = (Button) findViewById(R.id.button3);
		buttonAlarm.setOnClickListener(new OnClickListener(){public void onClick(View v) {startService(new Intent(MainActivity.this,TimingService.class));}});
	}
	
	public Bundle setParameters(){
		Bundle parem = new Bundle();
		int CellCountX,CellCountY,NUM_PARTICLES,DisplayHeight,wallWidth, wallHeight;
		float TrapBoxRatio,BallSize;
		try{CellCountX = Integer.parseInt(textRows.getText().toString());}
		catch (Exception e){CellCountX = 15;}
		
		try{CellCountY = Integer.parseInt(textColumns.getText().toString());}
		catch (Exception e){CellCountY = 10;}
		
		try{NUM_PARTICLES = Integer.parseInt(textBalls.getText().toString());}
		catch(Exception e){NUM_PARTICLES = 1;}
		
		try{TrapBoxRatio = Float.parseFloat(textTraps.getText().toString());}
		catch(Exception e){TrapBoxRatio=0;}
		
		try{BallSize = Float.parseFloat(textBallSize.getText().toString());}
		catch(Exception e){BallSize = 1/1.75f;}
		
		try{DisplayHeight = Integer.parseInt(textDisplayHeight.getText().toString());}
		catch(Exception e){DisplayHeight = 0;}
		
		try{wallWidth = Integer.parseInt((textWallWidth.getText().toString()));}
		catch(Exception e){wallWidth = 3;}
		
		try{wallHeight = Integer.parseInt((textWallHeight).getText().toString());}
		catch(Exception e){wallHeight = 3;}
		
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		maxCellsX = (int)(width/12);
		maxCellsY = (int)(height/12);
		minCellsX = 4;
		minCellsY = 6;
		
		CellCountX = Math.min(Math.max(CellCountX,minCellsX),maxCellsX);
		CellCountY = Math.min(Math.max(CellCountY,minCellsY),maxCellsY);
		CellCountX = Math.max(Math.max(CellCountX, 4),(int)(CellCountY/1.5f));
		CellCountY = Math.max(Math.max(CellCountY, 6),(int)(CellCountX));
		NUM_PARTICLES = Math.min(Math.max(1,NUM_PARTICLES),1000);
		if(TrapBoxRatio>0 && TrapBoxRatio < 4){TrapBoxRatio=4;}
		if(BallSize>0.8){BallSize=.8f;}
		if(BallSize<0.2){BallSize=0.2f;}
		if(DisplayHeight>0 && DisplayHeight<15){DisplayHeight = 15;}
		else{DisplayHeight = Math.max(Math.min(DisplayHeight,100), 0);}
		if(wallHeight>20 || wallHeight < 1){wallHeight = 1;}
		if(wallWidth>20 || wallWidth < 1){wallWidth = 1;}
				
		textRows.setText(((Integer)CellCountX).toString());
		textColumns.setText(((Integer)CellCountY).toString());
		textBalls.setText(((Integer)NUM_PARTICLES).toString());
		textTraps.setText(((Float)TrapBoxRatio).toString());
		textBallSize.setText(((Float)BallSize).toString());
		textDisplayHeight.setText(((Integer)DisplayHeight).toString());
		textWallWidth.setText(((Integer)wallWidth).toString());
		textWallHeight.setText(((Integer)wallHeight).toString());
		
		parem.putBoolean("AutomaticBorders", checkAutomaticBorders.isChecked());
		parem.putBoolean("TouchControl",checkTouchControl.isChecked());
		
		parem.putInt("CellCountX", CellCountX);
		parem.putInt("CellCountY", CellCountY);
		parem.putInt("NUM_PARTICLES", NUM_PARTICLES);
		parem.putInt("DisplayHeight", DisplayHeight);
		parem.putInt("wallWidth",wallWidth);
		parem.putInt("wallHeight",wallHeight);
		parem.putFloat("TrapBoxRatio", TrapBoxRatio);
		parem.putFloat("BallSize", BallSize);
		parem.putInt("level", 1);
		
		return parem;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode){
		case 0:
			finish();
			break;
		case 1:
			TimingService.mazeEnd(this);
		case -2:
			Toast.makeText(this,"You quit. Lame.",Toast.LENGTH_LONG).show();
		}		
	}
}
