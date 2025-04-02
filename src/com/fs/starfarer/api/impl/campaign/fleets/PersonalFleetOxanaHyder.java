package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;

public class PersonalFleetOxanaHyder extends PersonalFleetScript {
	
	public PersonalFleetOxanaHyder() {
		super(People.HYDER);
		setMinRespawnDelayDays(10f);
		setMaxRespawnDelayDays(20f);
	}

	@Override
	protected MarketAPI getSourceMarket() {
		return Global.getSector().getEconomy().getMarket("sindria");
	}
	
	@Override
	public CampaignFleetAPI spawnFleet() {
		
		MarketAPI sindria = getSourceMarket();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = sindria.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.HUGE, FleetQuality.DEFAULT, Factions.DIKTAT, FleetTypes.PATROL_LARGE, loc);
		m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.DEFAULT);
		m.triggerSetFleetCommander(getPerson());
		m.triggerSetFleetFaction(Factions.DIKTAT);
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, sindria);
		m.triggerFleetSetNoFactionInName();
		m.triggerPatrolAllowTransponderOff();
		m.triggerFleetSetName("Askonia System Defense Armada");
		//m.triggerFleetSetPatrolActionText("patrolling");
		m.triggerOrderFleetPatrol(sindria.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		sindria.getContainingLocation().addEntity(fleet);
		fleet.setLocation(sindria.getPlanetEntity().getLocation().x, sindria.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		return fleet;
	}

	@Override
	public boolean canSpawnFleetNow() {
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		if (sindria == null || sindria.hasCondition(Conditions.DECIVILIZED)) return false;
		if (!sindria.getFactionId().equals(Factions.DIKTAT)) return false;
		return true;
	}

	@Override
	public boolean shouldScriptBeRemoved() {
		return false;
	}

}




