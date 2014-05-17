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
	
	public long playerAnimationTime;
	public int playerFrame;
	
	public float playerAngle;
	public float playerX;
	public float playerY;
	
	public int touchX;
	public int touchY;
	
	public GameThread(SurfaceHolder holder){
		surfaceHolder = holder;

		updateCallCount = 0;
		ups = 0;
		lastUpdateCallReset = 0;
		setRunning(false);
		
		playerX = 0;
		playerY = 0;
		playerAngle = 90;
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
		
		playerX = MainActivity.surface.getWidth()/2;
		playerY = MainActivity.surface.getHeight()/2;
		playerAnimationTime = 0;
		playerFrame = 0;
		
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
			
			playerX += Math.cos(Math.toRadians(playerAngle-90))*3;
			playerY += Math.sin(Math.toRadians(playerAngle-90))*3;
			
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
			touchX = (int) event.getX();
			touchY = (int) event.getY();
		}
	}
}