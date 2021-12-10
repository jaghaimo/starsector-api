package com.fs.starfarer.api.impl.campaign.eventide;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public class AnimAction {
	public CharAnim anim;
	public CharAnimFrame curr;
	public float progress;
	public Actor actor;
	public boolean done = false;
	
	public boolean performedBlock = false;
	public boolean wasBlocked = false;
	public boolean scoredHit = false;
	
	public boolean makeCurrentFrameLast = false;
	
	public SpriteAPI sprite;
	public CharAnimFrame preFrame;
	public float returnToIdleXCorrection = 0f;
	
	public Set<CharAnimFrame> playedSoundsFor = new LinkedHashSet<CharAnimFrame>();
	
	public AnimAction(Actor actor, Object animKey, CharAnimFrame preFrame) {
		this.actor = actor;
		this.preFrame = preFrame;
		this.anim = Actions.ANIMATIONS.get(animKey);
		this.sprite = Global.getSettings().getSprite(anim.textureId);
		if (actor.texId != null) {
			this.sprite = Global.getSettings().getSprite(actor.texId);
		}
	}
	
//	public AnimAction(Actor actor, CharAnim anim) {
//		this.actor = actor;
//		this.anim = anim;
//	}
	public int framesUntilAttackFrame() {
		int index = 0;
		for (CharAnimFrame frame : anim.frames) {
			if (!frame.attackArea.isEmpty()) {
				index = anim.frames.indexOf(frame);
			}
		}
		int currIndex = anim.frames.indexOf(curr);
		
		return index - currIndex;
	}
	
	public void undoLastMove() {
		if (curr != null) {
			actor.loc.x -= curr.move.x * anim.scale * actor.facing;
			actor.loc.y -= curr.move.y * anim.scale * actor.facing;
		}
	}
	
	public void advance(float amount) {
		progress += amount;
		float total = 0f;
		for (CharAnimFrame f : anim.frames) {
			total += f.dur;
			if (total > progress) {
				if (curr != f) {
					if (makeCurrentFrameLast) {
						progress = anim.getTotalTime();
					} else {
						curr = f;
						actor.loc.x += curr.move.x * anim.scale * actor.facing;
						actor.loc.y += curr.move.y * anim.scale * actor.facing;
						
						if (!playedSoundsFor.contains(curr)) {
							playedSoundsFor.add(curr);
							for (String soundId : curr.soundIds) {
								if (soundId == null || soundId.isEmpty()) continue;
								//Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
								Vector2f soundLoc = new Vector2f(actor.loc);
								soundLoc.x *= DuelPanel.SOUND_LOC_MULT;
								Global.getSoundPlayer().playSound(soundId, 1f, 1f, soundLoc, new Vector2f());
							}
						}
						
						if (curr == anim.frames.get(0) && preFrame != null && anim.initialRelativeOffset != null) {
							float preW = preFrame.width * anim.scale;
							float w = curr.width * anim.scale;
							returnToIdleXCorrection = (preW - w) / 2f * actor.facing + anim.initialRelativeOffset * anim.scale;
							actor.loc.x -= returnToIdleXCorrection;
							returnToIdleXCorrection = 0f;
						}
					}
				}
				break;
			}
		}
		if (progress >= anim.getTotalTime()) {
			actor.loc.x += anim.moveToIdle.x * anim.scale * actor.facing + returnToIdleXCorrection;
			actor.loc.y += anim.moveToIdle.y * anim.scale * actor.facing;
			done = true;
		}
	}
	
	public void render(float alphaMult) {
		if (curr == null) return;
		
//		if (actor.facing > 0 && curr != null && curr == anim.frames.get(0)) {
//			System.out.println("Progress: " + progress);
//		}
		
		float x = actor.loc.x;
		float y = actor.loc.y;
		sprite.setAlphaMult(alphaMult);
		sprite.setSize(curr.width * anim.scale, curr.height * anim.scale);
		
		//sprite.setBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
		
		if (actor.facing > 0) {
			sprite.setTexX(curr.tx);
			sprite.setTexY(curr.ty);
			sprite.setTexWidth(curr.tw);
			sprite.setTexHeight(curr.th);
		} else {
			sprite.setTexX(curr.tx + curr.tw);
			sprite.setTexY(curr.ty);
			sprite.setTexWidth(-curr.tw);
			sprite.setTexHeight(curr.th);
		}
		sprite.renderAtCenter(x, y);
		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		Misc.renderQuad(x - curr.width / 2f, y - curr.height / 2f, curr.width, curr.height, Color.red, 0.5f);
		
		if (DuelPanel.DEBUG) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			for (HitArea area : curr.hittableArea) {
				area = area.getAdjustedForAction(this);
				Misc.renderQuad(area.x, area.y, area.w, area.h, new Color(120,120,120,127), alphaMult);
			}
			for (HitArea area : curr.attackArea) {
				area = area.getAdjustedForAction(this);
				Misc.renderQuad(area.x, area.y, area.w, area.h, new Color(200,60,60,127), alphaMult);
			}
			for (HitArea area : curr.blockArea) {
				area = area.getAdjustedForAction(this);
				Misc.renderQuad(area.x, area.y, area.w, area.h, new Color(60,200,60,127), alphaMult);
			}
		}
	}

	public boolean isDone() {
		return done;
	}
	
}





