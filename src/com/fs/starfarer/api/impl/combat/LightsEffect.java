package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class LightsEffect implements EveryFrameWeaponEffectPlugin {

	private Color base = null;
	private FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f);
	private FaderUtil pulse = new FaderUtil(1f, 2f, 2f, true, true);
	
	public LightsEffect() {
		fader.fadeIn();
		pulse.fadeIn();
	}


	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;

		fader.advance(amount);
		pulse.advance(amount);
		
		SpriteAPI sprite = weapon.getSprite();
		if (base == null) {
			base = sprite.getColor();
		}
		
		ShipAPI ship = weapon.getShip();
		if (ship.isHulk()) {
			fader.fadeOut();
		} else {
			if (ship.getFluxTracker().isVenting()) {
				fader.fadeOut();
			} else {
				fader.fadeIn();
			}
		}
		
		float alphaMult = fader.getBrightness() * (0.75f + pulse.getBrightness() * 0.25f);
		if (ship.getFluxTracker().isOverloaded()) {
			alphaMult = (float) Math.random() * fader.getBrightness();
		}
		
		Color color = Misc.scaleAlpha(base, alphaMult);
		//System.out.println(alphaMult);
		sprite.setColor(color);
	}
	
	
	
	
}
