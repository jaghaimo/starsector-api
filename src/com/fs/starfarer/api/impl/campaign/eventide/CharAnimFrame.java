package com.fs.starfarer.api.impl.campaign.eventide;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

public class CharAnimFrame implements Cloneable {
	public float dur;
	public float tx, ty, tw, th;
	public float width, height;
	public Vector2f move = new Vector2f();
	
	public List<HitArea> hittableArea = new ArrayList<HitArea>();
	public List<HitArea> attackArea = new ArrayList<HitArea>();
	public List<HitArea> blockArea = new ArrayList<HitArea>();
	
	public List<String> soundIds = new ArrayList<String>();
	public boolean attackCanActuallyHit = true;
	
	public int hitDamage = 1;
	
	public void setHittable(float x, float w) {
		HitArea area = new HitArea();
		area.y = -height/2f;
		area.x = x;
		area.h = height;
		area.w = w;
		hittableArea.add(area);
	}
	
	public void setAttack(float x, float w) {
		HitArea area = new HitArea();
		area.y = -height/2f;
		area.x = x;
		area.h = height;
		area.w = w;
		attackArea.add(area);
	}
	
	public void setBlock(float x, float w) {
		HitArea area = new HitArea();
		area.y = -height/2f;
		area.x = x;
		area.h = height;
		area.w = w;
		blockArea.add(area);
	}

	
	protected CharAnimFrame clone() {
		try {
			CharAnimFrame copy = (CharAnimFrame) super.clone();
			copy.move = new Vector2f(move);
			copy.attackArea = new ArrayList<HitArea>();
			for (HitArea curr : attackArea) {
				copy.attackArea.add(curr.clone());
			}
			copy.blockArea = new ArrayList<HitArea>();
			for (HitArea curr : blockArea) {
				copy.blockArea.add(curr.clone());
			}
			copy.hittableArea = new ArrayList<HitArea>();
			for (HitArea curr : hittableArea) {
				copy.hittableArea.add(curr.clone());
			}
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
