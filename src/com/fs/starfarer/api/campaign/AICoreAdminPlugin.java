package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.characters.PersonAPI;


public interface AICoreAdminPlugin {
	public PersonAPI createPerson(String aiCoreId, String factionId, long seed);
}
