package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;

public class TestDwellerShipCreator extends BaseDwellerShipCreator {

	@Override
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		DwellerCombatPlugin plugin = super.createPlugin(ship);
		
		List<DwellerShipPart> parts = plugin.getParts();
		
		WobblyPart part = new WobblyPart("shroud", 0.3f, 1f, new Vector2f(0, 0), 0f);
		parts.add(part);
		
		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
		
		part = new WobblyPart("clusterA", 1f, 3, 3, 2f, new Vector2f(70, 0), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.setWeaponActivated();
		parts.add(part);
		
		part = new WobblyPart("clusterB", 1f, 3, 3, 2f, new Vector2f(-10, 0), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.setFluxActivated();
		parts.add(part);
		
		part = new WobblyPart("coronet_stalks", 0.5f, 3, 3, 2f, new Vector2f(100, 0), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.setShieldActivated();
		parts.add(part);
		
		return plugin;
	}
	
}


