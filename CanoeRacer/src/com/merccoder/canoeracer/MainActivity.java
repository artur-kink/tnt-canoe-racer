package com.merccoder.canoeracer;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.merccoder.canoeracer.GameThread.Screen;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends BaseGameActivity {


	public static GameSurface surface;
	public static GameThread thread;
	
	public static MainActivity context;
	
	public static Typeface pixelFont;
	
	public static boolean registerred;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//Remove title.
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	super.onCreate(savedInstanceState);
    	context = this;
    	
    	//Do not log in on startup.
    	getGameHelper().setConnectOnStart(false);
        
        //Set as full screen and set main view to GameSurface.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        pixelFont = Typeface.createFromAsset(MainActivity.context.getAssets(), "font/pixelart.ttf");
        
        //Create frame layout to contain game surface and ui layout.
      	FrameLayout baseLayout = new FrameLayout(this);
      	//Create ui layout.
      	RelativeLayout uiLayout = new RelativeLayout(this);

      	surface = new GameSurface(this);
      	
      	baseLayout.addView(surface);
      	baseLayout.addView(uiLayout);
      	
      	setContentView(baseLayout);
      	
      	thread.uiLayout = uiLayout;
      	
      	AudioPlayer.context = this;
		AudioPlayer.initSounds();
    }
    
    @Override
	public void onPause(){
		super.onPause();
		thread.setScreen(Screen.START);
	}
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
	    thread.onTouchEvent(event);
	    return true;
	}
    

	public boolean loggedIn(){
		if(!isSignedIn()){
			beginUserInitiatedSignIn();
		}
		return isSignedIn();
	}
	
	public void giveAchievement(int achievementId){
		if(registerred && isSignedIn())
			Games.Achievements.unlock(getApiClient(), getResources().getString(achievementId));
		
	}
	
	public void submitScore(int scoreId, int score){
		if(registerred && isSignedIn())
			Games.Leaderboards.submitScore(getApiClient(), getResources().getString(scoreId), score);
	}
	
	public void openAchievements(){
		if(loggedIn()){
			startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 0);
		}
	}
	
	public void openHighscores(){
		if(loggedIn()){
			startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), 1);
		}
	}
	
	@Override
	public void onBackPressed (){
		if(thread.onBackPressed() == false){
			super.onBackPressed();
		}
	}
	
	@Override
	public void onSignInFailed() {
		registerred = false;
		thread.saveSettings();
	}

	@Override
	public void onSignInSucceeded() {
		registerred = true;
		thread.saveSettings();
	}
}
