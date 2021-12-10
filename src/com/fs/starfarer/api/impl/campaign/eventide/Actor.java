package com.fs.starfarer.api.impl.campaign.eventide;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class Actor {
	
	public AnimAction currAction;
	public AnimAction nextAction;
	public float facing = 1f;
	public Vector2f loc = new Vector2f();
	
	public int maxHealth;
	public int health;
	
	public String texId;
	
	public Map<String, String> actionRemap1 = new LinkedHashMap<String, String>();
	public Map<String, String> actionRemap2 = new LinkedHashMap<String, String>();
	public Map<String, Float> actionSpeedMult = new LinkedHashMap<String, Float>();
	
	
	public Actor() {
		this(null);
	}
	public Actor(String texId) {
		this.texId = texId;
	}
	
	public String getActionId() {
		if (currAction == null) return "";
		return currAction.anim.action;
	}
	
	public void endCurrentAnimation() {
		if (currAction != null) {
			currAction.makeCurrentFrameLast = true;
		}
	}

	protected float freeze = 0f;
	public void freeze(float sec) {
		freeze = sec;
	}
	public void advance(float amount) {
		freeze -= amount;
		if (freeze > 0) return;
		
		//amount *= 0.25f;
		if (currAction != null) {
			Float mult  = actionSpeedMult.get(currAction.anim.action);
			if (mult == null) mult = 1f;
			currAction.advance(amount * mult);
			if (currAction.isDone()) {
				currAction = null;
			}
		}
		if (currAction == null) {
			if (nextAction != null) {
				currAction = nextAction;
				nextAction = null;
			} else {
				//currAction = new AnimAction(this, Actions.IDLE);
				doAction(Actions.IDLE, true);
			}
			currAction.advance(0f);
		}
	}
	
	public CharAnimFrame getCurrentFrame() {
		if (currAction == null) return null;
		return currAction.curr;
	}
	
	public void doAction(String action, boolean forceInterruptCurrent) {
		if (action == Actions.ATTACK && 
				currAction != null && (currAction.performedBlock)) {
			action = Actions.RIPOSTE;
			forceInterruptCurrent = true;
		} else if (currAction != null && currAction.anim != null && currAction.anim.interruptableBy.contains(action)) {
			forceInterruptCurrent = true;
		}

		if (actionRemap1.containsKey(action)) {
			action = actionRemap1.get(action);
		}
		if (actionRemap2.containsKey(action)) {
			action = actionRemap2.get(action);
		}
		
		if (forceInterruptCurrent) {
			nextAction = null;
			currAction = new AnimAction(this, action, getCurrentFrame());
			if (currAction.anim == null) {
				currAction = null;
			} else {
				currAction.advance(0f);
			}
		} else {
			if (nextAction == null || nextAction.anim == null) {
				nextAction = null;
			}
			nextAction = new AnimAction(this, action, getCurrentFrame());
		}
	}
	
	public void render(float alphaMult) {
		boolean renderShadow = true;
		renderShadow = false;
		if (renderShadow && currAction != null && currAction.anim != null && currAction.curr != null &&
				 !currAction.curr.hittableArea.isEmpty()) {
			HitArea area = currAction.curr.hittableArea.get(0);
			area = area.getAdjustedForAction(currAction);
			SpriteAPI shadow = Global.getSettings().getSprite("graphics/fx/hit_glow.png");
			shadow.setNormalBlend();
			shadow.setColor(Color.black);
			
			float sw = currAction.curr.width * 1.25f;
			if (sw > 200) {
				sw = 200 + (sw - 200) * 0.33f;
			}
			shadow.setSize(sw, 40f);
			
			shadow.setAlphaMult(0.5f);
			shadow.renderAtCenter(area.x + area.w/2f, loc.y - currAction.anim.frameHeight / 2f + 5f);
		}
		
		currAction.render(alphaMult);
	}
}





