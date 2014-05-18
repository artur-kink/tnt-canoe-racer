package com.merccoder.canoeracer;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Main game thread.
 * All game logic is managed here.
 */
public class GameThread extends Thread{
	
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
	
	public int tiles[][];
	public int tileY;
	
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
	}
	
	/**
	 * Set thread running state.
	 */
	public void setRunning(boolean r){
		running = r;
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
		
		tiles = new int[1280/16][800/16];
		
		screenMultiplier = MainActivity.surface.getWidth()/((float)screenWidth);
		
		playerX = screenWidth/2;
		playerY = 0;
		worldY = 0;
		tileY = 0;
		playerAnimationTime = 0;
		playerFrame = 0;
		
		
		gates.add(new Gate(100, screenHeight/2, 100, 4));
		gates.get(0).number = 1;
		
		gateCounter = 1;
		
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
				playerAngle += (angleDelta-360)*0.2f;
			else if(angleDelta < -180)
				playerAngle += (angleDelta+360)*0.2f;
			else
				playerAngle += angleDelta*0.2f;
			
			//Fix angles if not in [0-360)
			if(playerAngle < 0){
				playerAngle += 360.0f;
			}else if(playerAngle > 360)
				playerAngle -= 360.0f;
			
			//Move towards point.
			playerX += Math.cos(Math.toRadians(playerAngle-90))*7;
			playerY += Math.sin(Math.toRadians(playerAngle-90))*7;
			
			if(playerY < 0)
				playerY = 0;
			
			if(playerY - worldY > screenHeight/2){
				worldY = playerY - screenHeight/2;
			}
			
			for(int i = 0; i < gates.size(); i++){
				if(gates.get(i).y < worldY){
					gates.remove(i);
					i--;
				}else{
					if(gates.get(i).passed == false){
						if(Math.abs((playerY + 32) - gates.get(i).y) < 5
							&& gates.get(i).x < playerX + 32 && gates.get(i).x + gates.get(i).width > playerX + 32){
							gates.get(i).passed = true;
							gatesPassed++;
						}
					}
				}
			}
			
			if(gates.size() < 5){
				gates.add(createNewGate());
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

	public Gate createNewGate(){
		gateCounter++;
		Gate gate = new Gate((int) (Math.random()*(screenWidth-100)), gates.lastElement().y + (screenHeight/3), 100, 4);
		gate.number = gateCounter;
		return gate;
	}
	
	public float getAngle(float x1, float y1, float x2, float y2) {
	    return (float) Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
	}
	
	/**
	 * Back button pressed handler.
	 * @return true if back was handled, else false.
	 */
	public boolean onBackPressed(){
		return false;
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
}