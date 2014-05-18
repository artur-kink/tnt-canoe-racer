package com.merccoder.canoeracer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Gate {

	public int x;
	public int y;
	
	public int width;
	public int height;
	
	public boolean passed;
	public int number;
	
	public Gate(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		
		width = w;
		height = h;
		
		passed = false;
	}
	
	public void draw(Canvas canvas){
		Paint paint = new Paint();
		paint.setARGB(255, 255, 255, 255);
		canvas.drawRect(new Rect(x, (int) (y - MainActivity.thread.worldY), x+20, (int) (y - MainActivity.thread.worldY)+13), paint);
		canvas.drawRect(new Rect(x + width, (int) (y - MainActivity.thread.worldY), x+width+14, (int) (y - MainActivity.thread.worldY)+13), paint);
	
		paint.setColor(Color.BLACK);
		paint.setTextSize(13);
		paint.setTypeface(MainActivity.pixelFont);
		canvas.drawText("" + number, x + 1, (int) (y - MainActivity.thread.worldY) + 12, paint);
	}
}
