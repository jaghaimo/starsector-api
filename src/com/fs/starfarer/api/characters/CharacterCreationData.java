/**
 * 
 */
package com.fs.starfarer.api.characters;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;

public interface CharacterCreationData {
	public static String HYPERSPACE_NAME_TOKEN = "hyperspace";

	String getStartingLocationName();
	void setStartingLocationName(String startingLocationName);
	Vector2f getStartingCoordinates();
	
	PersonAPI getPerson();
	void clearAdditionalShips();
	void addStartingFleetMember(String specId, FleetMemberType type);
	void removeStartingFleetMember(String specId);
	CargoAPI getStartingCargo();
	
	CharacterDataAPI getCharacterData();
	void setDone(boolean done);
	boolean isDone();
	String getDifficulty();
	void setDifficulty(String difficulty);
	void addScript(Script script);
	List<Script> getScripts();
	
	String getSeedString();
	void setSeedString(String seedString);
	long getSeed();
	void setSeed(long seed);
	StarAge getSectorAge();
	void setSectorAge(StarAge sectorAge);
	String getSectorSize();
	void setSectorSize(String sectorSize);
	
	Map<String, Object> getCustomData();
	List<String> getStartingShips();
	
	boolean isWithTimePass();
	void setWithTimePass(boolean withTimePass);
	
	void addScriptBeforeTimePass(Script script);
	List<Script> getScriptsBeforeTimePass();
}