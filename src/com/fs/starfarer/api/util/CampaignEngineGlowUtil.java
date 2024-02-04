package com.fs.starfarer.api.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.CampaignEntityMovementUtil.EngineGlowControls;

public class CampaignEngineGlowUtil implements EngineGlowControls {

	public static String KEY1 = "key1";
	public static String KEY2 = "key2";
	public static String KEY3 = "key3";
	public static String KEY4 = "key4";
	public static String KEY5 = "key5";
	public static String KEY6 = "key6";
	
	protected List<CampaignEngineGlowIndividualEngine> engines = new ArrayList<CampaignEngineGlowIndividualEngine>();
	protected SectorEntityToken entity;
	
	protected ValueShifterUtil glowCoreMult = new ValueShifterUtil(1f);
	protected ValueShifterUtil glowFringeMult = new ValueShifterUtil(1f);
	protected ValueShifterUtil glowMult = new ValueShifterUtil(0f);
	protected ValueShifterUtil lengthMult = new ValueShifterUtil(0f);
	protected ValueShifterUtil widthMult = new ValueShifterUtil(0f);
	protected ValueShifterUtil flickerMult = new ValueShifterUtil(0f);
	protected ValueShifterUtil flickerRateMult = new ValueShifterUtil(1f);
	protected ValueShifterUtil textureScrollMult = new ValueShifterUtil(1f);
	protected ColorShifterUtil glowColorCore;
	protected ColorShifterUtil glowColorFringe;
	protected ColorShifterUtil flameColor;
	private float shiftRate;
	
	public CampaignEngineGlowUtil(SectorEntityToken entity, 
						Color fringe, Color core, Color flame, float shiftRate) {
		this.entity = entity;
		this.shiftRate = shiftRate;
		glowColorCore = new ColorShifterUtil(core);
		glowColorFringe = new ColorShifterUtil(fringe);
		flameColor = new ColorShifterUtil(flame);
	}

	public void addEngine(CampaignEngineGlowIndividualEngine engine) {
		engines.add(engine);
	}

	
	public void showOtherAction() {
		glowMult.shift(KEY1, 1.5f, 1f, 1f, 1f);
		lengthMult.shift(KEY1, 0.33f, 1f, 1f, 1f);
		widthMult.shift(KEY1, 1f, 1f, 1f, 1f);
	}
	public void showAccelerating() {
		glowMult.shift(KEY2, 1f, 1f, 1f, 1f);
		lengthMult.shift(KEY2, 1f, 1f, 1f, 1f);
		widthMult.shift(KEY2, 1f, 1f, 1f, 1f);
//		glowMult.shift(KEY2, 2f, 1f, 1f, 1f);
//		lengthMult.shift(KEY2, 2f, 1f, 1f, 1f);
//		widthMult.shift(KEY2, 2f, 1f, 1f, 1f);
	}
	public void showIdling() {
		//if (true) {
//		if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
//			showSuppressed();
//			return;
//		}
//		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
//			showOtherAction();
//			return;
//		}
//		if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
//			showAccelerating();
//			return;
//		}
		
		glowMult.shift(KEY3, 0.5f, 1f, 1f, 1f);
		glowFringeMult.shift(KEY3, 1.5f, 1f, 1f, 1f);
		lengthMult.shift(KEY3, 0.25f, 1f, 1f, 1f);
		widthMult.shift(KEY3, 1.5f, 1f, 1f, 1f);

		glowColorCore.shift(KEY3, glowColorFringe.getBase(), 1f, 1f, 0.5f);
		glowColorFringe.shift(KEY3, Color.black, 1f, 1f, 0.5f);
		flameColor.shift(KEY3, Color.black, 1f, 1f, 0.75f);
		
		//flickerMult.shift(KEY3, 0.5f, 1f, 1f, 1f);
	}
	
	public void showSuppressed() {
		glowMult.shift(KEY4, 0.5f, 1f, 1f, 1f);
		glowFringeMult.shift(KEY4, 0.5f, 1f, 1f, 1f);
		lengthMult.shift(KEY4, 0.25f, 1f, 1f, 1f);
		widthMult.shift(KEY4, 1f, 1f, 1f, 1f);
		
//		glowMult.shift(KEY4, 0.5f, 1f, 1f, 1f);
//		glowFringeMult.shift(KEY4, 1.5f, 1f, 1f, 1f);
//		lengthMult.shift(KEY4, 0.25f, 1f, 1f, 1f);
//		widthMult.shift(KEY4, 1.5f, 1f, 1f, 1f);
		
		float in = 0.5f;
		if (lengthMult.getCurr() <= 0f || widthMult.getCurr() <= 0f) {
			in = 0f;
		}
		glowColorCore.shift(KEY4, Color.black, in, 1f, 1f);
		glowColorFringe.shift(KEY4, Color.black, in, 1f, 1f);
		flameColor.shift(KEY4, Color.black, in, 1f, 1f);
	}
	
	
	public void advance(float amount) {
		float shiftAmount = amount * shiftRate;
		//shiftAmount *= 5f;
		
		glowMult.advance(shiftAmount);
		glowFringeMult.advance(shiftAmount);
		glowCoreMult.advance(shiftAmount);
		lengthMult.advance(shiftAmount);
		widthMult.advance(shiftAmount);
		flickerMult.advance(shiftAmount);
		flickerRateMult.advance(shiftAmount);
		textureScrollMult.advance(shiftAmount);
		
		glowColorCore.advance(shiftAmount);
		glowColorFringe.advance(shiftAmount);
		flameColor.advance(shiftAmount);
		
		for (CampaignEngineGlowIndividualEngine engine : engines) {
			engine.advance(amount);
		}
	}
	
	
	public void render(float alphaMult) {
		for (CampaignEngineGlowIndividualEngine engine : engines) {
			engine.render(entity.getLocation(), entity.getFacing(), alphaMult);
		}
	}

	public ValueShifterUtil getGlowMult() {
		return glowMult;
	}

	public ValueShifterUtil getLengthMult() {
		return lengthMult;
	}

	public ValueShifterUtil getWidthMult() {
		return widthMult;
	}

	public ValueShifterUtil getTextureScrollMult() {
		return textureScrollMult;
	}

	public ColorShifterUtil getGlowColorCore() {
		return glowColorCore;
	}

	public ColorShifterUtil getGlowColorFringe() {
		return glowColorFringe;
	}

	public ColorShifterUtil getFlameColor() {
		return flameColor;
	}

	public ValueShifterUtil getFlickerMult() {
		return flickerMult;
	}
	public ValueShifterUtil getFlickerRateMult() {
		return flickerRateMult;
	}

	public ValueShifterUtil getGlowCoreMult() {
		return glowCoreMult;
	}

	public ValueShifterUtil getGlowFringeMult() {
		return glowFringeMult;
	}
	
}








