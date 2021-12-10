package com.fs.starfarer.api.campaign;

import java.util.Random;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;


public interface AICoreOfficerPlugin {
	
	/**
	 * In person memory, how many points worth it counts for when installed
	 * on an automated ship, for the purposes of the Automated Ships skill ONLY.
	 */
	public static String AUTOMATED_POINTS_VALUE = "$autoPointsValue";
	
	/**
	 * In person memory, by how much it multiplies the automated points cost
	 * on an automated ship, for the purposes of the Automated Ships skill ONLY.
	 */
	public static String AUTOMATED_POINTS_MULT = "$autoPointsMult";
	
	public PersonAPI createPerson(String aiCoreId, String factionId, Random random);
	public void createPersonalitySection(PersonAPI person, TooltipMakerAPI tooltip);
	//StoryPointActionDelegate createIntegrateDelegate(PersonAPI person, FleetMemberAPI member);
}
