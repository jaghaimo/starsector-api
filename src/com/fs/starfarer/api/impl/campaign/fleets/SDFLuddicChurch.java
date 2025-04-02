package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;

public class SDFLuddicChurch extends SDFBase {
	
	public SDFLuddicChurch() {
	}

	@Override
	protected String getFactionId() {
		return Factions.LUDDIC_CHURCH;
	}

	protected SkillPickPreference getCommanderShipSkillPreference() {
		return SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE;
	}
	
	@Override
	protected MarketAPI getSourceMarket() {
		return Global.getSector().getEconomy().getMarket("hesperus");
	}

	@Override
	protected String getDefeatTriggerToUse() {
		return "SDFLuddicChurchDefeated";
	}


	@Override
	public CampaignFleetAPI spawnFleet() {
		
		MarketAPI hesperus = getSourceMarket();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = hesperus.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.HIGHER, getFactionId(), FleetTypes.PATROL_LARGE, loc);
		
		m.triggerSetFleetSizeFraction(1.6f);
		
		m.triggerSetFleetOfficers( OfficerNum.DEFAULT, OfficerQuality.DEFAULT);
		m.triggerSetFleetDoctrineComp(5, 2, 0);
		m.triggerSetFleetCommander(getPerson());
		
		m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
		m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		
		
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, hesperus);
		//m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Armada of the Ecumene");
		m.triggerPatrolAllowTransponderOff();
		//m.triggerFleetSetPatrolActionText("patrolling");
		m.triggerOrderFleetPatrol(hesperus.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		hesperus.getContainingLocation().addEntity(fleet);
		fleet.setLocation(hesperus.getPlanetEntity().getLocation().x, hesperus.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//		}
		
		return fleet;
	}
}




