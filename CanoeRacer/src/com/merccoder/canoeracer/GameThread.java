package com.merccoder.canoeracer;

import java.util.Vector;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Main game thread.
 * All game logic is managed here.
 */
public class GameThread extends Thread
	implements OnClickListener{
	
	/** Counter for updates per second(ups). */
	private int updateCallCount;
	
	/** The updates per second of the previous second. */
	public int ups;
	
	/** Last time fps second had elapsed. */
	private long lastUpdateCallReset;
	
	/** Last time game was updated. */
	private long lastUpdate;
	
	/** GameSurface Surface Holder. */
	private SurfaceHolder surfaceHolder;
	
	/** Thread running state. */
	public boolean running;
	
	/** View for ads. */
	private AdView adView;
	
	public static boolean SoundOn;
	
	public int screenWidth;
	public int screenHeight;
	
	public float screenMultiplier;
	
	public long playerAnimationTime;
	public int playerFrame;
	
	public float playerAngle;
	public float playerX;
	public float playerY;
	
	public float worldY;
	
	public int touchX;
	public int touchY;
	
	public Vector<Gate> gates;
	
	public int gateCounter;
	public int gatesPassed;
	public int consecutiveGatesPassed;
	public int lastGatePassed;
	public int nextGatePosition;
	
	public enum TileType{
		Water,
		Grass,
		Rapid,
		Stone
	}
	
	public TileType tiles[][];
	public int tilesWidth;
	public int tilesHeight;
	public float tileY;
	
	public boolean gameExists;
	
	/** List of screen types. */
	public enum Screen{
		START,
		GAME
	}
	/** Current screen of game. */
	public Screen currentScreen;
	
	/** Layout for UI elements. Above the game surface. */
	public RelativeLayout uiLayout;
	
	private RelativeLayout startScreen;
	private Button startButton;
	private Button resumeButton;
	private Button highscoresButton;
	private Button achievementsButton;
	private Button optionsButton;
	
	public GameThread(SurfaceHolder holder){
		surfaceHolder = holder;

		updateCallCount = 0;
		ups = 0;
		lastUpdateCallReset = 0;
		setRunning(false);
		
		playerX = 0;
		playerY = 0;
		playerAngle = 90;
		
		gates = new Vector<Gate>();
		
		//Create the ad
	    adView = new AdView(MainActivity.context);
	    adView.setAdSize(AdSize.BANNER);
	    adView.setAdUnitId(MainActivity.context.getResources().getString(R.string.AdId));
	    
	    AdRequest.Builder adBuilder = new AdRequest.Builder()
	        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
	    
	    if(BuildConfig.DEBUG){
		    for(int i = 0; i < MainActivity.context.getResources().getStringArray(R.array.TestDevices).length; i++){
		    	adBuilder.addTestDevice(MainActivity.context.getResources().getStringArray(R.array.TestDevices)[i]);
		    }
	    }
	    
	    AdRequest adRequest = adBuilder.build();

	    //Load ad
	    if(adRequest != null){
	    	adView.loadAd(adRequest);
	    }
	    adView.setId(1);
	    
	    SharedPreferences settings = MainActivity.context.getSharedPreferences("settings", 0);
		SoundOn = settings.getBoolean("sound", true);
		MainActivity.registerred = settings.getBoolean("registerred", false);
		if(MainActivity.registerred)
			MainActivity.context.loggedIn();
	    
		LayoutInflater inflater = (LayoutInflater)MainActivity.context.getSystemService(MainActivity.context.LAYOUT_INFLATER_SERVICE );
	    startScreen = (RelativeLayout) inflater.inflate(R.layout.start_screen, uiLayout);
	    
	    startButton = (Button) startScreen.findViewById(R.id.start_button);
	    startButton.setOnClickListener(this);
		
	    resumeButton = (Button) startScreen.findViewById(R.id.resume_button);
	    resumeButton.setOnClickListener(this);
	    
	    highscoresButton = (Button) startScreen.findViewById(R.id.highscores_button);
	    highscoresButton.setOnClickListener(this);
	    
	    achievementsButton = (Button) startScreen.findViewById(R.id.achievements_button);
	    achievementsButton.setOnClickListener(this);
	    
		optionsButton = (Button) startScreen.findViewById(R.id.options_button);
		optionsButton.setOnClickListener(this);
		
		setRunning(false);
		gameExists = false;
	}
	
	/**
	 * Set thread running state.
	 */
	public void setRunning(boolean r){
		running = r;
	}
	
	/**
	 * Called when the game can be initialized.
	 * This means the graphics and application has been setup
	 * and ready to be used.
	 */
	public void initGame(){
		setScreen(Screen.START);
	}
	
	public void newGame(){
		
		//Create shore
		tiles[0][0] = TileType.Grass;
		tiles[0][tilesWidth - 1] = TileType.Grass;
		
		worldY = -tilesHeight*16;
		for(int i = 1; i < tilesHeight; i++){
			createNewLine(i);
			worldY += 16;
		}
		worldY = 0;
		
		playerX = screenWidth/2;
		playerY = 10;
		worldY = 0;
		tileY = 0;
		playerAnimationTime = 0;
		playerFrame = 0;
		
		gates.clear();
		gates.add(new Gate(screenWidth/2, screenHeight/2, 100, 4));
		gates.get(0).number = 1;
		nextGatePosition = screenHeight;
		
		touchX = screenWidth/2;
		touchY = screenHeight/2;
		
		gateCounter = 1;
		consecutiveGatesPassed = 0;
		gatesPassed = 0;
		lastGatePassed = 0;
		
		gameExists = true;
	}
	
	@SuppressLint("WrongCall")
	private void drawCall(Canvas gameCanvas){
		//Draw game state.
		gameCanvas = surfaceHolder.lockCanvas();
		if(gameCanvas != null){
			synchronized (surfaceHolder) {
				MainActivity.surface.onDraw(gameCanvas);
			}
			surfaceHolder.unlockCanvasAndPost(gameCanvas);
		}
	}
	
	@Override
	/**
	 * Main game loop.
	 */
	public void run() {

		Canvas gameCanvas = null;
		
		screenWidth = 800;//MainActivity.surface.getWidth();
		screenHeight = 1280;//MainActivity.surface.getHeight();
		
		tilesHeight = 1280/16 + 1;
		tilesWidth = 800/16;
		tiles = new TileType[tilesHeight][tilesWidth];
		
		screenMultiplier = MainActivity.surface.getWidth()/((float)screenWidth);
		
		//Game loop.
		while (running) {
			
			//Check if game state should be updated.
			if(System.currentTimeMillis() - lastUpdate <= 33){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				continue;
			}
			lastUpdate = System.currentTimeMillis();
			
			
			
			if(currentScreen == Screen.GAME){
				//Animate player.
				if(lastUpdate - playerAnimationTime > 600){
					playerAnimationTime = lastUpdate;
					playerFrame++;
					if(playerFrame > 1){
						playerFrame = 0;
					}
				}
				
				//Turn player towards touch position.
				float angleDelta = ((-(getAngle(playerX+32.0f, playerY+32.0f, (float)touchX, (float)touchY) - 180))
					- playerAngle);
				if(angleDelta > 180)
					playerAngle += (angleDelta-360)*0.05f;
				else if(angleDelta < -180)
					playerAngle += (angleDelta+360)*0.05f;
				else
					playerAngle += angleDelta*0.05f;
				
				//Fix angles if not in [0-360)
				if(playerAngle < 0){
					playerAngle += 360.0f;
				}else if(playerAngle > 360)
					playerAngle -= 360.0f;
				
				//Check collision.
				float playerXTip = (float) (playerX + 32 + Math.cos(Math.toRadians(playerAngle-90))*7);
				float playerYTip1 = (float) (playerY + 4 + Math.sin(Math.toRadians(playerAngle-90))*7 + 1.5f) - worldY;
				float playerYTip2 = (float) (playerY + 60 + Math.sin(Math.toRadians(playerAngle-90))*7 + 1.5f) - worldY;
				
				if(playerYTip1 > 0 && playerYTip2 > 0){
					if(tiles[(int) (playerYTip1/16)][(int) (playerXTip/16)] == TileType.Water &&
						tiles[(int) (playerYTip2/16)][(int) (playerXTip/16)] == TileType.Water){
						//Move towards point.
						playerX += Math.cos(Math.toRadians(playerAngle-90))*7;
						playerY += Math.sin(Math.toRadians(playerAngle-90))*7 + 1.5f;
					}else if((tiles[(int) (playerYTip1/16)][(int) (playerXTip/16)] == TileType.Rapid ||
						tiles[(int) (playerYTip1/16)][(int) (playerXTip/16)] == TileType.Water) &&
						(tiles[(int) (playerYTip2/16)][(int) (playerXTip/16)] == TileType.Rapid ||
						tiles[(int) (playerYTip2/16)][(int) (playerXTip/16)] == TileType.Water)){
						//Move towards point.
						playerX += Math.cos(Math.toRadians(playerAngle-90))*7;
						playerY += Math.sin(Math.toRadians(playerAngle-90))*7 + 5f;
					}else{
						if(consecutiveGatesPassed > 0){
							MainActivity.context.submitScore(R.string.leaderboard_consecutive_gates_passed, consecutiveGatesPassed);
						}
						if(gatesPassed > 0){
							MainActivity.context.submitScore(R.string.leaderboard_gates_passed, gatesPassed);
						}else{
							MainActivity.context.giveAchievement(R.string.achievement_dead_in_the_water);
						}
						
						if(worldY > 0){
							MainActivity.context.submitScore(R.string.leaderboard_distance_travelled, gatesPassed);
						}
						gameExists = false;
						setScreen(Screen.START);
					}
				}
				
				//Update world position.
				if(playerY - worldY > screenHeight/2){
					float tWorldY = worldY;
					worldY = playerY - screenHeight/2;
					
					tileY += (worldY - tWorldY);
					
					while(tileY >= 16){
						tileY -= 16;
						
						//Shift tilemap down once
						for(int r = 0; r < tilesHeight-1; r++){
							for(int c = 0; c < tilesWidth; c++){
								tiles[r][c] = tiles[r+1][c];
							}
						}
						
						createNewLine(tilesHeight-1);
					}
				}
				
				//Update gates
				for(int i = 0; i < gates.size(); i++){
					//Remove passed gates
					if(gates.get(i).y < worldY){
						gates.remove(i);
						i--;
					}else{
						if(gates.get(i).number > lastGatePassed && gates.get(i).passed == false){
							if(Math.abs((playerY + 32) - gates.get(i).y) < 5
								&& gates.get(i).x < playerX + 32 && gates.get(i).x + gates.get(i).width > playerX + 32){
								gates.get(i).passed = true;
								if((gates.get(i).reverse && Math.sin(Math.toRadians(playerAngle-90))*7 + 1.5f < 0) ||
									(!gates.get(i).reverse && Math.sin(Math.toRadians(playerAngle-90))*7 + 1.5f > 0)){
									
									AudioPlayer.playSound(AudioPlayer.gate);
									
									if(lastGatePassed + 1 == gates.get(i).number){
										consecutiveGatesPassed++;
									}else{
										if(consecutiveGatesPassed > 0){
											MainActivity.context.submitScore(R.string.leaderboard_consecutive_gates_passed, consecutiveGatesPassed);
										}
										consecutiveGatesPassed = 0;
									}
									
									lastGatePassed = gates.get(i).number;
									gatesPassed++;
								}
							}
						}
					}
				}
				
				//Create new gates
				if(nextGatePosition <= worldY + screenHeight){
					gates.add(createNewGate());
					nextGatePosition = (int) (gates.lastElement().y + (screenHeight/3) + Math.random()*800);
				}
				
				//Update debug parameters.
				if(BuildConfig.DEBUG){
					updateCallCount++;
					if(System.currentTimeMillis() - lastUpdateCallReset >= 1000){
						lastUpdateCallReset = System.currentTimeMillis();
						ups = updateCallCount;
						updateCallCount = 0;
					}
				}
				drawCall(gameCanvas);
			}
		}
	}

	public Gate createNewGate(){
		gateCounter++;
		
		int yTile = (int) ((nextGatePosition - worldY)/16);
		
		int xPosition = 0;
		do{
			xPosition = (int) (Math.random()*screenWidth);
		}while(tiles[yTile][xPosition/16] != TileType.Water ||
			xPosition + 100 >= screenWidth ||
			tiles[yTile][xPosition/16 + 100/16] != TileType.Water);
		
		Gate gate = new Gate(xPosition, nextGatePosition, 100, 4);
		gate.number = gateCounter;
		return gate;
	}
	
	public void createNewLine(int i){
		for(int w = 0; w < tilesWidth; w++){
			tiles[i][w] = TileType.Water;
		}
		
		float degree = (float) Math.toRadians(worldY/6);
		
		for(int w = 0; w < (Math.sin(degree) +
			Math.sin(2*degree) + Math.cos(3*degree) - Math.sin(2*degree) - Math.cos(2*degree) + 3)*1.5f; w++){
			tiles[i][w] = TileType.Grass;
		}
		
		for(int w = 1; w <= Math.max(1, (Math.cos(2*degree) +
			Math.cos(3*degree) + Math.sin(2*degree) - Math.cos(2*degree) - Math.sin(degree) + 3)*1.5f); w++){
			tiles[i][tilesWidth-w] = TileType.Grass;
		}
		
		if(i - 1 >= 0){
			for(int w = 1; w < tilesWidth; w++){
				if(tiles[i-1][w] == TileType.Rapid && tiles[i][w] == TileType.Water){
					if(Math.random() > 0.5)
						tiles[i][w] = TileType.Rapid;
				}else if(tiles[i][w - 1] == TileType.Rapid && tiles[i][w] == TileType.Water){
					if(Math.random() > 0.45)
						tiles[i][w] = TileType.Rapid;
				}else if(tiles[i][w] == TileType.Water){
					if(Math.random() > 0.99)
						tiles[i][w] = TileType.Rapid;
					else if(Math.random() > 0.995 && i > 10){
						tiles[i][w] = TileType.Stone;
					}
				}
			}
		}
	}
	
	/**
	 * Get angle between two points.
	 */
	public float getAngle(float x1, float y1, float x2, float y2) {
	    return (float) Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
	}
	
	/**
	 * Change screens.
	 * @param screen Screen to change to.
	 */
	public void setScreen(Screen screen){
		currentScreen = screen;
		
		//Make sure running on UI thread.
		MainActivity.context.runOnUiThread(new Runnable() {
			 @Override
		     public void run() {
		    	uiLayout.removeAllViews();
		    	
		    	LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		    	params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		    	uiLayout.addView(adView, params);
		    	 
		 		if(currentScreen == Screen.START){
		 			if(gameExists == true){
		 				resumeButton.setVisibility(View.VISIBLE);
		 			}else{
		 				resumeButton.setVisibility(View.INVISIBLE);
		 			}
		 			
		 			uiLayout.addView(startScreen, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		 		}
		    }
		});
	}
	
	@Override
	/**
	 * Button clicked handler.
	 */
	public void onClick(View v) {
		if(currentScreen == Screen.START){
			if(v == startButton){
				newGame();
				setScreen(Screen.GAME);
			}else if(v == resumeButton){
				setScreen(Screen.GAME);
			}else if(v == highscoresButton){
				MainActivity.context.openHighscores();
			}else if(v == achievementsButton){
				MainActivity.context.openAchievements();
			}
		}
	}
	
	/**
	 * Back button pressed handler.
	 * @return true if back was handled, else false.
	 */
	public boolean onBackPressed(){
		if(currentScreen == Screen.START){
			return false;
		}else{
			setScreen(Screen.START);
			return true;
		}
	}

	/**
	 * Screen touch handler.
	 */
	public void onTouchEvent(MotionEvent event){
		
		//Record current touch location.
		if(event.getAction() == MotionEvent.ACTION_DOWN ||
			event.getAction() == MotionEvent.ACTION_MOVE){
			touchX = (int) (event.getX()/screenMultiplier);
			touchY = (int) ((event.getY()/screenMultiplier) + worldY);
		}
	}
	
	/**
	 * Save game settings.
	 */
	public void saveSettings(){
		SharedPreferences settings = MainActivity.context.getSharedPreferences("settings", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sound", SoundOn);
		editor.putBoolean("registerred", MainActivity.registerred);
		editor.commit();
	}
}