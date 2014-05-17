package com.merccoder.canoeracer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback  {

	/** Counter for fps. */
	private int drawCallCount;
	
	/** The fps of the previous second. */
	private int fps;
	
	/** Last time fps second had elapsed. */
	private long lastDrawCallReset;
	
	public static Bitmap canoe;
	
	public GameSurface(Context context){
		super(context);
		getHolder().addCallback(this);
		
		//Initialize thread.
		MainActivity.thread = new GameThread(getHolder());
		setFocusable(true);
		
		canoe = BitmapFactory.decodeResource(getResources(), R.drawable.canoe);
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(!MainActivity.thread.isAlive()){
			MainActivity.thread.setRunning(true);
			MainActivity.thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}
	
	@Override
	/**
	 * GameSurface draw method. Game is drawn in this method.
	 */
	protected void onDraw(Canvas canvas) {
		
		//Clear screen
		Paint paint = new Paint();
		paint.setARGB(255, 0, 0, 255);
		canvas.drawPaint(paint);
		
		paint.setARGB(255, 255, 255, 255);
		
		Matrix transform = new Matrix();
		
		transform.reset();
		transform.setTranslate(MainActivity.thread.playerX, MainActivity.thread.playerY);
		transform.preRotate(MainActivity.thread.playerAngle, 32, 32);
		
		canvas.save();
		canvas.setMatrix(transform);
		canvas.drawBitmap(canoe,
			new Rect(MainActivity.thread.playerFrame*64, 0, MainActivity.thread.playerFrame*64+64, 64),
			new Rect(0, 0, 64, 64),
			paint);
		canvas.restore();
		
		//Debug draw.
		if(BuildConfig.DEBUG){
			
			//Update fps
			drawCallCount++;
			if(System.currentTimeMillis() - lastDrawCallReset > 1000){
				lastDrawCallReset = System.currentTimeMillis();
				fps = drawCallCount;
				drawCallCount = 0;
			}
			
			//Draw fps
			paint.setTextSize(20);
			paint.setARGB(128, 255, 255, 255);
			canvas.drawText("FPS: " + fps, 20, 20, paint);
			
			//Draw ups
			canvas.drawText("UPS: " + MainActivity.thread.ups, 20, 40, paint);
			
			canvas.drawText("Player Angle: " + MainActivity.thread.playerAngle, 20, 60, paint);
			canvas.drawText((int)MainActivity.thread.playerX + "x" + (int)MainActivity.thread.playerY, 20, 80, paint);
			
			//Draw press position
			canvas.drawRect(new Rect(MainActivity.thread.touchX, MainActivity.thread.touchY,
				MainActivity.thread.touchX + 5, MainActivity.thread.touchY + 5), paint);
		}
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
}
