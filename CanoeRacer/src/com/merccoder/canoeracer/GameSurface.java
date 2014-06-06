package com.merccoder.canoeracer;

import com.merccoder.canoeracer.GameThread.TileType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
	public static Bitmap water[];
	public int waterFrame;
	public long waterUpdateTime;
	
	public static Canvas surface;
	public static Bitmap surfaceBitmap;
	
	public GameSurface(Context context){
		super(context);
		getHolder().addCallback(this);
		
		//Initialize thread.
		MainActivity.thread = new GameThread(getHolder());
		setFocusable(true);
		
		canoe = BitmapFactory.decodeResource(getResources(), R.drawable.canoe);
		water = new Bitmap[16];
		water[0] = BitmapFactory.decodeResource(getResources(), R.drawable.water0);
		water[1] = BitmapFactory.decodeResource(getResources(), R.drawable.water1);
		water[2] = BitmapFactory.decodeResource(getResources(), R.drawable.water2);
		water[3] = BitmapFactory.decodeResource(getResources(), R.drawable.water3);
		water[4] = BitmapFactory.decodeResource(getResources(), R.drawable.water4);
		water[5] = BitmapFactory.decodeResource(getResources(), R.drawable.water5);
		water[6] = BitmapFactory.decodeResource(getResources(), R.drawable.water6);
		water[7] = BitmapFactory.decodeResource(getResources(), R.drawable.water7);
		water[8] = BitmapFactory.decodeResource(getResources(), R.drawable.water8);
		water[9] = BitmapFactory.decodeResource(getResources(), R.drawable.water9);
		water[10] = BitmapFactory.decodeResource(getResources(), R.drawable.water10);
		water[11] = BitmapFactory.decodeResource(getResources(), R.drawable.water11);
		water[12] = BitmapFactory.decodeResource(getResources(), R.drawable.water12);
		water[13] = BitmapFactory.decodeResource(getResources(), R.drawable.water13);
		water[14] = BitmapFactory.decodeResource(getResources(), R.drawable.water14);
		water[15] = BitmapFactory.decodeResource(getResources(), R.drawable.water15);
		
		waterFrame = 0;
		
		surface = new Canvas();
		surfaceBitmap = Bitmap.createBitmap(640, 1024, Config.ARGB_8888);
		surface.setBitmap(surfaceBitmap);
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(!MainActivity.thread.isAlive()){
			MainActivity.thread.initGame();
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
		
		Paint paint = new Paint();
		paint.setARGB(255, 255, 255, 255);
		
		//Update water animation.
		if(System.currentTimeMillis() - waterUpdateTime > 100){
			waterFrame++;
			if(waterFrame > 15)
				waterFrame = 0;
			waterUpdateTime = System.currentTimeMillis();
		}
		
		
		Rect waterRect = new Rect((waterFrame%8)*16, (waterFrame/8)*16, (waterFrame%8)*16 + 16, (waterFrame/8)*16+16);
		//Draw tileset
		for(int r = 0; r < MainActivity.thread.tilesHeight; r++){
			for(int c = 0; c < MainActivity.thread.tilesWidth; c++){
				if(MainActivity.thread.tiles[r][c] == null)
					continue;
				
				switch(MainActivity.thread.tiles[r][c]){
					case Grass:
						paint.setARGB(255, 0, 255, 0);
						surface.drawRect(
							new Rect(c*16, r*16 - (int)MainActivity.thread.tileY, c*16 + 16, r*16 + 16 - (int)MainActivity.thread.tileY), paint);
						break;
					case Rapid:
						paint.setARGB(128, 255, 255, 255);
						surface.drawRect(
							new Rect(c*16, r*16 - (int)MainActivity.thread.tileY, c*16 + 16, r*16 + 16 - (int)MainActivity.thread.tileY), paint);
						break;
					case Stone:
						paint.setARGB(255, 64, 64, 64);
						surface.drawRect(
							new Rect(c*16, r*16 - (int)MainActivity.thread.tileY, c*16 + 16, r*16 + 16 - (int)MainActivity.thread.tileY), paint);
						break;
					case Water:
						paint.setARGB(255, 255, 255, 255);
						surface.drawBitmap(water[waterFrame], c*16, r*16 - (int)MainActivity.thread.tileY, paint);
						break;
					default:
						break;
				}
			}
		}
		
		//Draw gates
		for(int i = 0; i < MainActivity.thread.gates.size(); i++){
			MainActivity.thread.gates.get(i).draw(surface);
		}
		
		//Draw items
		for(int i = 0; i < MainActivity.thread.items.size(); i++){
			MainActivity.thread.items.get(i).draw(surface);
		}
		
		//Draw player
		Matrix transform = new Matrix();
		
		transform.reset();
		transform.setTranslate(MainActivity.thread.playerX, MainActivity.thread.playerY - MainActivity.thread.worldY);
		transform.preRotate(MainActivity.thread.playerAngle + 180, 32, 32);
		
		surface.save();
		surface.setMatrix(transform);
		surface.drawBitmap(canoe,
			new Rect(MainActivity.thread.playerFrame*64, 0, MainActivity.thread.playerFrame*64+64, 64),
			new Rect(0, 0, 64, 64),
			paint);
		surface.restore();
		
		paint.setARGB(255, 255, 0, 0);
		surface.drawRect(new Rect((int)MainActivity.thread.playerPoint1.x, (int)MainActivity.thread.playerPoint1.y,
			(int)MainActivity.thread.playerPoint1.x + 3, (int)MainActivity.thread.playerPoint1.y + 3), paint);
		
		surface.drawRect(new Rect((int)MainActivity.thread.playerPoint2.x, (int)MainActivity.thread.playerPoint2.y,
				(int)MainActivity.thread.playerPoint2.x + 3, (int)MainActivity.thread.playerPoint2.y + 3), paint);
		
		surface.drawRect(new Rect((int)MainActivity.thread.playerPoint3.x, (int)MainActivity.thread.playerPoint3.y,
				(int)MainActivity.thread.playerPoint3.x + 3, (int)MainActivity.thread.playerPoint3.y + 3), paint);
		
		
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
			new Rect(0, 0, 640, 1024),
			new Rect(0, 0, getWidth(), getHeight()), paint);
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
}
