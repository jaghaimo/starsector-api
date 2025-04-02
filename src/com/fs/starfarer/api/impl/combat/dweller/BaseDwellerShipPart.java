package com.fs.starfarer.api.impl.combat.dweller;

import java.util.LinkedHashSet;
import java.util.Set;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.ValueShifterUtil;

public class BaseDwellerShipPart implements DwellerShipPart {
	public String id;
	public Set<String> tags = new LinkedHashSet<>();
	public Vector2f offset;
	public float facingOffset;
	public float alphaMult = 1f;
	public float currBrightness;
	public ValueShifterUtil brightness;
	
	public Color color = Color.white;
	public boolean additiveBlend = false;
	public boolean negativeBlend = false;
	public FaderUtil fader;
	
	public BaseDwellerShipPart(Vector2f offset, float facingOffset) {
		this.offset = offset;
		this.facingOffset = facingOffset;
		
		fader = new FaderUtil(0f, 1f, 1f);
		fader.fadeIn();
		
		brightness = new ValueShifterUtil(1f);
	}
	
	public void setSystemActivated() {
		setActivated(DwellerCombatPlugin.SYSTEM_ACTIVATED);
	}
	public void setWeaponActivated() {
		setActivated(DwellerCombatPlugin.WEAPON_ACTIVATED);
	}
	public void setShieldActivated() {
		setActivated(DwellerCombatPlugin.SHIELD_ACTIVATED);
	}
	public void setFluxActivated() {
		setActivated(DwellerCombatPlugin.FLUX_ACTIVATED);
	}
	
	public void setActivated(String ...tags) {
		for (String tag : tags) {
			this.tags.add(tag);
		}
	}
			
	
	public void advance(float amount) {
		fader.advance(amount);
		brightness.advance(amount);
		
		float desired = brightness.getCurr();
		currBrightness = Misc.approach(currBrightness, desired, 1f, 1f, amount);
	}
	
	public void render(float entityX, float entityY, float alphaMult, float angle, CombatEngineLayers layer) {
		alphaMult *= this.alphaMult;
		alphaMult *= fader.getBrightness();
		alphaMult *= currBrightness;
		
		Vector2f rot = Misc.rotateAroundOrigin(offset, angle + 90);
		renderImpl(entityX + rot.x, entityY + rot.y, alphaMult, angle, layer);
		
	}
	
	protected void renderImpl(float x, float y, float alphaMult, float angle, CombatEngineLayers layer) {
		
	}
	
	@Override
	public void fadeOut() {
		fader.fadeOut();
	}
	@Override
	public void fadeIn() {
		fader.fadeIn();
	}
	@Override
	public FaderUtil getFader() {
		return fader;
	}
	@Override
	public float getAlphaMult() {
		return alphaMult;
	}
	@Override
	public void setAlphaMult(float alphaMult) {
		this.alphaMult = alphaMult;
	}
	@Override
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public Set<String> getTags() {
		return tags;
	}
	@Override
	public void addTag(String tag) {
		tags.add(tag);
	}
	@Override
	public void removeTag(String tag) {
		tags.remove(tag);
		
	}
	@Override
	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
	
	@Override
	public ValueShifterUtil getBrightness() {
		return brightness;
	}
	@Override
	public Color getColor() {
		return color;
	}
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	
}

