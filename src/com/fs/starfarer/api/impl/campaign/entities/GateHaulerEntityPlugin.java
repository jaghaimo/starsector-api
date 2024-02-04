package com.fs.starfarer.api.impl.campaign.entities;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.CampaignEngineGlowIndividualEngine;
import com.fs.starfarer.api.util.CampaignEngineGlowUtil;
import com.fs.starfarer.api.util.CampaignEntityMovementUtil;
import com.fs.starfarer.api.util.Misc;

public class GateHaulerEntityPlugin extends BaseCustomEntityPlugin { // implements EngineGlowControls {

	public static float MAX_SPEED = 1000f;
	public static float ACCELERATION = 5f;
	
	protected CampaignEntityMovementUtil movement;
	protected CampaignEngineGlowUtil engineGlow;
	protected boolean longBurn = false; 
	//protected boolean acceleratedThisFrame = false;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		if (engineGlow == null) {
			Color fringe = new Color(255, 0, 0, 255);
			Color flame = new Color(255, 100, 100, 255);
			Color core = new Color(255, 255, 255, 255);
			
			engineGlow = new CampaignEngineGlowUtil(entity, fringe, core, flame, 0.25f);
			CampaignEngineGlowIndividualEngine engine = new CampaignEngineGlowIndividualEngine(
					90f, 300f, 50f, 400f, new Vector2f(-115f, 0f), engineGlow);
			engine.setFlameTexSpanMult(0.5f);
			engineGlow.addEngine(engine);
		}
		
		if (movement == null) {
			movement = new CampaignEntityMovementUtil(entity, 0.5f, 3f, ACCELERATION, MAX_SPEED);
			movement.setEngineGlow(engineGlow);
		}
		
		return this;
	}
	
	public CampaignEntityMovementUtil getMovement() {
		return movement;
	}

	public CampaignEngineGlowUtil getEngineGlow() {
		return engineGlow;
	}
	
	public boolean isActivated() {
		return entity.getMemoryWithoutUpdate().getBoolean("$activated");
	}
	public boolean isActivating() {
		return entity.getMemoryWithoutUpdate().getBoolean("$activating");
	}
	public boolean isInTransit() {
		return entity.getMemoryWithoutUpdate().getBoolean("$inTransit");
	}
	public float getRemainingActivationDays() {
		return entity.getMemoryWithoutUpdate().getExpire("$activating");
	}

	public void advance(float amount) {
		if (entity.isInCurrentLocation() || isInTransit()) {
			engineGlow.advance(amount);
			
			float soundVolume = engineGlow.getLengthMult().getCurr() * 0.5f;
			if (soundVolume > 0.5f) soundVolume = 0.5f;
			
			if (isActivating()) {
				// full time is 1 day, set in rules
				float remaining = getRemainingActivationDays();
				float f = remaining/1f;
				if (f < 0f) f = 0f;
				if (f > 1f) f = 1f;
				engineGlow.getFlickerRateMult().shift(this, 1f + 25f * f, 0f, 0.1f, 1f);
				engineGlow.getFlickerMult().shift(this, f, 0f, 0.1f, 1f);
			} if (!isActivated()) {
				engineGlow.showSuppressed();
			} else if (isActivated()) {
				if (longBurn && movement.isDesiredFacingSet()) {
					float angleDiff = Misc.getAngleDiff(movement.getDesiredFacing(), entity.getFacing());
					if (angleDiff < 2f) {
						Vector2f dir = Misc.getUnitVectorAtDegreeAngle(movement.getDesiredFacing());
						float speedInDesiredDir = Vector2f.dot(dir, entity.getVelocity());
						if (movement.isFaceInOppositeDirection()) {
							speedInDesiredDir *= -1f;
						}
						float speed = entity.getVelocity().length();
						
						if (speedInDesiredDir > 10f && speedInDesiredDir > speed * 0.7f) {
							float speedForMaxEngineLength = 100f;
							float f = speedInDesiredDir / speedForMaxEngineLength;
							if (f < 0f) f = 0f;
							if (f > 1f) f = 1f;
							
							soundVolume = Math.min(soundVolume + f * 0.5f, 1f);
							
							//System.out.println("longBurn factor: " + f);
							
							float flickerZone = 0.5f;
							if (f < flickerZone) {
								engineGlow.getFlickerRateMult().shift(this, 5f, 0f, 0.1f, 1f);
								engineGlow.getFlickerMult().shift(this, 0.33f - 0.33f * f / flickerZone, 0f, 0.1f, 1f);
							}
							
							engineGlow.getGlowMult().shift(this, 2f, 1f, 1f, f);
							engineGlow.getLengthMult().shift(this, 5f, 1f, 1f, f);
							engineGlow.getWidthMult().shift(this, 3f, 1f, 1f, f);
						}
						
					}
				}
			}
			
			if (soundVolume > 0) {
				if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet()) {
					Global.getSoundPlayer().playLoop("gate_hauler_engine_loop", entity, 
								1f, soundVolume,
								entity.getLocation(), entity.getVelocity());
				}
			}
		}
		
		if (isActivated()) {
			movement.advance(amount);
		}
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness(); 
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0f) return;
		
		engineGlow.render(alphaMult);
	}
	
	public float getRenderRange() {
		return entity.getRadius() + 3000f; // for engine glow/trails
	}

	public boolean isLongBurn() {
		return longBurn;
	}

	public void setLongBurn(boolean longBurn) {
		this.longBurn = longBurn;
	}

//	public void showAccelerating() {
//		engineGlow.showAccelerating();
//		acceleratedThisFrame = true;
//	}
//
//	public void showIdling() {
//		engineGlow.showIdling();
//	}
//
//	public void showSuppressed() {
//		engineGlow.showSuppressed();
//	}
//
//	public void showOtherAction() {
//		engineGlow.showOtherAction();		
//	}
	
	

}








