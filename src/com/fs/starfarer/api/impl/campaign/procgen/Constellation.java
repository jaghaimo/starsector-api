package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames.NamePick;

public class Constellation {

	public static enum ConstellationType {
		NORMAL,
		NEBULA,
	}
	protected StarAge age;
	protected transient Map<SectorEntityToken, PlanetAPI> lagrangeParentMap = new HashMap<SectorEntityToken, PlanetAPI>();
	protected transient Map<SectorEntityToken, List<SectorEntityToken>> allEntitiesAdded = new HashMap<SectorEntityToken, List<SectorEntityToken>>();
	protected NamePick namePick;
	protected List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
	protected ConstellationType type = ConstellationType.NORMAL;
	
	protected transient boolean leavePickedNameUnused = false;
	
	public Constellation(ConstellationType type, StarAge age) {
		this.type = type;
		this.age = age;
	}
	
	public boolean isLeavePickedNameUnused() {
		return leavePickedNameUnused;
	}

	public void setLeavePickedNameUnused(boolean leavePickedNameUnused) {
		this.leavePickedNameUnused = leavePickedNameUnused;
	}

	public StarAge getAge() {
		return age;
	}

	public void setAge(StarAge age) {
		this.age = age;
	}

	public Vector2f getLocation() {
		Vector2f loc = new Vector2f();
		if (systems.isEmpty()) return loc;
		
		for (StarSystemAPI system : systems) {
			Vector2f.add(loc, system.getLocation(), loc);
		}
		
		loc.scale(1f / (float) systems.size());
		return loc;
	}
	
	public ConstellationType getType() {
		return type;
	}

	public void setType(ConstellationType type) {
		this.type = type;
	}

	public String getName() {
		return namePick.nameWithRomanSuffixIfAny;
	}
	
	public String getNameWithType() {
		String constellationText = getName() + " Constellation";
		if (getType() == ConstellationType.NEBULA) {
			constellationText = getName() + " Nebula";
		}
		return constellationText;
	}
	
	public String getNameWithLowercaseType() {
		String name = getNameWithType();
		name = name.replaceAll("Constellation", "constellation");
		name = name.replaceAll("Nebula", "nebula");
		return name;
	}
	
	public NamePick getNamePick() {
		return namePick;
	}
	public void setNamePick(NamePick namePick) {
		this.namePick = namePick;
	}
	public List<StarSystemAPI> getSystems() {
		return systems;
	}

	public Map<SectorEntityToken, PlanetAPI> getLagrangeParentMap() {
		return lagrangeParentMap;
	}

	public void setLagrangeParentMap(Map<SectorEntityToken, PlanetAPI> lagrangeParentMap) {
		this.lagrangeParentMap = lagrangeParentMap;
	}

	public Map<SectorEntityToken, List<SectorEntityToken>> getAllEntitiesAdded() {
		return allEntitiesAdded;
	}

	public void setAllEntitiesAdded(
			Map<SectorEntityToken, List<SectorEntityToken>> allEntitiesAdded) {
		this.allEntitiesAdded = allEntitiesAdded;
	}

	public StarSystemAPI getSystemWithMostPlanets() {
		int most = -1;
		StarSystemAPI result = null;
		for (StarSystemAPI curr : systems) {
			int count = curr.getPlanets().size();
			if (count > most) {
				most = count;
				result = curr;
			}
		}
		return result;
	}
}




