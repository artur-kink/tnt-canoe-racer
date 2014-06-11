package com.merccoder.canoeracer;

import com.merccoder.canoeracer.GameThread.TileType;

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
	
	public float getXVelocity(){
		return (float) Math.sin(y);
	}
	
	public Item(float x, float y){
		this.x = x;
		this.y = y;
		
		width = height = 16;
		
		xVelocity = 0;
		yVelocity = 5;
	}
	
	public void update(){
		//Check for collision
		if(y - MainActivity.thread.worldY + height > 0 && y + height < MainActivity.thread.worldY + MainActivity.thread.screenHeight){
			float tempY = y;
			TileType yTile = MainActivity.thread.tiles[(int) ((y - (int)MainActivity.thread.worldY + height)/16)][(int) ((x + width/2)/16)];
			if(yTile == TileType.Water){
				y += yVelocity;
			}else if(yTile == TileType.Rapid){
				y += 2*yVelocity;
			}
			
			TileType newTile = MainActivity.thread.tiles[(int) ((y-(int)MainActivity.thread.worldY + height)/16)][(int) ((x + width/2)/16)];
			if(newTile != TileType.Water && newTile != TileType.Rapid){
				y = tempY;
			}
			
			x += getXVelocity();
		}else{
			y += yVelocity;
			x += xVelocity;
		}
		
	}
	
	public void draw(Canvas canvas){
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		canvas.drawRect(new Rect((int)x, (int)y - (int)MainActivity.thread.worldY, (int)x + width,
			(int)y + height - (int)MainActivity.thread.worldY), p);
	}
}
