package com.merccoder.canoeracer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
	
	public static Canvas surface;
	public static Bitmap surfaceBitmap;
	
	public GameSurface(Context context){
		super(context);
		getHolder().addCallback(this);
		
		//Initialize thread.
		MainActivity.thread = new GameThread(getHolder());
		setFocusable(true);
		
		canoe = BitmapFactory.decodeResource(getResources(), R.drawable.canoe);
		
		surface = new Canvas();
		surfaceBitmap = Bitmap.createBitmap(800, 1280, Config.ARGB_8888);
		surface.setBitmap(surfaceBitmap);
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
		surface.drawPaint(paint);
		
		paint.setARGB(255, 255, 255, 255);
		
		//Draw tileset
		for(int r = 0; r < MainActivity.thread.tilesHeight; r++){
			for(int c = 0; c < MainActivity.thread.tilesWidth; c++){
				//Draw code.
			}
		}
		
		//Draw gates
		for(int i = 0; i < MainActivity.thread.gates.size(); i++){
			MainActivity.thread.gates.get(i).draw(surface);
		}
		
		//Draw player
		Matrix transform = new Matrix();
		
		transform.reset();
		transform.setTranslate(MainActivity.thread.playerX, MainActivity.thread.playerY - MainActivity.thread.worldY);
		transform.preRotate(MainActivity.thread.playerAngle, 32, 32);
		
		surface.save();
		surface.setMatrix(transform);
		surface.drawBitmap(canoe,
			new Rect(MainActivity.thread.playerFrame*64, 0, MainActivity.thread.playerFrame*64+64, 64),
			new Rect(0, 0, 64, 64),
			paint);
		surface.restore();
		
		
		
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
			surface.drawText("FPS: " + fps, 20, 20, paint);
			
			//Draw ups
			surface.drawText("UPS: " + MainActivity.thread.ups, 20, 40, paint);
			
			surface.drawText("Gates: "  + MainActivity.thread.consecutiveGatesPassed + "/" + MainActivity.thread.gatesPassed + "/" + MainActivity.thread.gateCounter, 20, 60, paint);
			
			//Draw press position
			surface.drawRect(new Rect(MainActivity.thread.touchX, MainActivity.thread.touchY - (int)MainActivity.thread.worldY,
				MainActivity.thread.touchX + 5, MainActivity.thread.touchY - (int)MainActivity.thread.worldY + 5), paint);
		}
		paint.setARGB(255, 255, 255, 255);
		
		canvas.drawBitmap(surfaceBitmap,
			new Rect(0, 0, 800, 1280),
			new Rect(0, 0, getWidth(), getHeight()), paint);
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
}
