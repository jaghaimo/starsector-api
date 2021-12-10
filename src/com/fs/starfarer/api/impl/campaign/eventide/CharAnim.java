package com.fs.starfarer.api.impl.campaign.eventide;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class CharAnim implements Cloneable {
	
	public String textureId;
	public SpriteAPI sprite;
	public float scale = 1f;
	public float frameHeight;
	public float widthSoFar = 0f;
	public float totalTime = 0f;
	public List<CharAnimFrame> frames = new ArrayList<CharAnimFrame>();
	public CharAnimFrame last;
	public Float initialRelativeOffset = null;
	public Vector2f moveToIdle = new Vector2f();
	public String action;
	
	public Set<String> interruptableBy = new LinkedHashSet<String>();
	
	@Override
	public CharAnim clone() {
		try {
			CharAnim copy = (CharAnim) super.clone();
			copy.frames = new ArrayList<CharAnimFrame>();
			for (CharAnimFrame frame : frames) {
				CharAnimFrame cf = frame.clone();
				copy.frames.add(cf);
			}
			copy.sprite = Global.getSettings().getSprite(textureId);
			copy.totalTime = totalTime;
			copy.interruptableBy = new LinkedHashSet<String>(interruptableBy);
			copy.moveToIdle = new Vector2f(moveToIdle);
			copy.last = last.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void removeFirstFrame() {
		CharAnimFrame frame = frames.remove(0);
		totalTime -= frame.dur;
	}

	public CharAnim(String textureId, String action, float frameHeight) {
		this.textureId = textureId;
		this.action = action;
		sprite = Global.getSettings().getSprite(textureId);
		this.frameHeight = frameHeight;
	}
	
	public boolean hasBlockFrames() {
		for (CharAnimFrame frame : frames) {
			if (!frame.blockArea.isEmpty()) return true;
		}
		return false;
	}
	public boolean hasAttackFrames() {
		for (CharAnimFrame frame : frames) {
			if (!frame.attackArea.isEmpty()) return true;
		}
		return false;
	}
	
	public void interruptableBy(String ...actions) {
		for (String action : actions) {
			interruptableBy.add(action);
		}
	}
	
//	public CharAnim createReverse(String action) {
//		CharAnim rev = new CharAnim(textureId, action, frameHeight);
//		for (CharAnimFrame frame : frames) {
//			CharAnimFrame copy = frame.clone();
//			copy.move.negate();
//			rev.frames.add(0, copy);
//		}
//		rev.last = rev.frames.get(rev.frames.size() - 1);
//		rev.scale = scale;
//		rev.totalTime = totalTime;
//		rev.widthSoFar = widthSoFar;
//		//rev.moveToIdle = // depends on the new last frame; can't auto-figure-this-out
//		return rev;
//	}

	public void skip(CharAnim anim) {
		widthSoFar += anim.widthSoFar;
	}
	public void skip(float frameWidth) {
		widthSoFar += frameWidth;
	}
	public void addFrame(float y, float frameWidth, float dur) {
		float txPerPixel = sprite.getTextureWidth() / sprite.getWidth();
		float tyPerPixel = sprite.getTextureHeight() / sprite.getHeight();
		
		CharAnimFrame frame = new CharAnimFrame();
		frame.dur = dur;
		frame.tx = widthSoFar * txPerPixel; 
		frame.ty = y * tyPerPixel;
		frame.tw = frameWidth * txPerPixel;
		frame.th = frameHeight * tyPerPixel;
		frame.width = frameWidth;
		frame.height = frameHeight;
		widthSoFar += frameWidth;
		
		totalTime += dur;
		
		frames.add(frame);
		last = frame;
	}
	
	
	public float getTotalTime() {
		return totalTime;
	}
	
	public void updateTextureScale(float scale) {
		for (CharAnimFrame frame : frames) { 
			frame.tx *= scale;
			frame.ty *= scale;
			frame.tw *= scale;
			frame.th *= scale;
		}
	}
}






