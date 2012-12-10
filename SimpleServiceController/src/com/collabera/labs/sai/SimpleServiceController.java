package com.collabera.labs.sai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SimpleServiceController extends Activity {

	   @Override
		protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        
	        Button start = (Button)findViewById(R.id.serviceButton);
	        Button stop = (Button)findViewById(R.id.cancelButton);
	        
	        start.setOnClickListener(startListener);
	        stop.setOnClickListener(stopListener);
	        
	   }
	   
       private OnClickListener startListener = new OnClickListener() {
       	public void onClick(View v){
       		startService(new Intent(SimpleServiceController.this,SimpleService.class));
       	}	        	
       };
       
       private OnClickListener stopListener = new OnClickListener() {
          	public void onClick(View v){
          		stopService(new Intent(SimpleServiceController.this,SimpleService.class));
          	}	        	
          };
}
