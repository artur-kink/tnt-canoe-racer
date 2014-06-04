package com.merccoder.canoeracer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Item {
	public float x;
	public float y;
	
	public int width;
	public int height;
	
	public float xVelocity;
	public float yVelocity;
	
	public Item(float x, float y){
		this.x = x;
		this.y = y;
		
		width = height = 16;
		
		xVelocity = 0;
		yVelocity = 5;
	}
	
	public void update(){
		x += xVelocity;
		y += yVelocity;
	}
	
	public void draw(Canvas canvas){
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		canvas.drawRect(new Rect((int)x, (int)y - (int)MainActivity.thread.worldY, (int)x + width,
			(int)y + height - (int)MainActivity.thread.worldY), p);
	}
}
