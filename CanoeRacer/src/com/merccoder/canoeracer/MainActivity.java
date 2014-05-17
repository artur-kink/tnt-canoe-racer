package com.merccoder.canoeracer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {


	public static GameSurface surface;
	public static GameThread thread;
	
	public static MainActivity context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	context = this;
    	
        //Remove title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        //Set as full screen and set main view to GameSurface.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        surface = new GameSurface(this);
        
        //Create frame layout to contain game surface and ui layout.
      	FrameLayout baseLayout = new FrameLayout(this);
      	//Create ui layout.
      	RelativeLayout uiLayout = new RelativeLayout(this);

      	baseLayout.addView(surface);
      	baseLayout.addView(uiLayout);
      		
      	setContentView(baseLayout);
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
	    thread.onTouchEvent(event);
	    return true;
	}
}
