package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.combat.ShipAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface AfterShipCreationSkillEffect extends ShipSkillEffect {
	void applyEffectsAfterShipCreation(ShipAPI ship, String id);
	void unapplyEffectsAfterShipCreation(ShipAPI ship, String id);
}
