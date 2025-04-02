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

public class SDFTriTachyon extends SDFBase {
	
	public SDFTriTachyon() {
		super();
	}

	@Override
	protected String getFactionId() {
		return Factions.TRITACHYON;
	}
	
	protected SkillPickPreference getCommanderShipSkillPreference() {
		return SkillPickPreference.YES_ENERGY_NO_BALLISTIC_NO_MISSILE_YES_DEFENSE;
	}

	@Override
	protected MarketAPI getSourceMarket() {
		return Global.getSector().getEconomy().getMarket("culann");
	}

	@Override
	protected String getDefeatTriggerToUse() {
		return "SDFTriTachyonDefeated";
	}


	@Override
	public CampaignFleetAPI spawnFleet() {
		
		MarketAPI culann = getSourceMarket();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = culann.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_3, getFactionId(), FleetTypes.PATROL_LARGE, loc);
		
		m.triggerSetFleetSizeFraction(1.15f);
		
		m.triggerSetFleetOfficers( OfficerNum.DEFAULT, OfficerQuality.DEFAULT);
		m.triggerSetFleetDoctrineComp(5, 0, 3); // will get some Astrals anyway
		m.triggerSetFleetCommander(getPerson());
		
		m.triggerFleetAddCommanderSkill(Skills.FIGHTER_UPLINK, 1);
		m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
		m.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
		m.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
		m.triggerFleetAddCommanderSkill(Skills.CYBERNETIC_AUGMENTATION, 1);
		
		
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, culann);
		//m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Capital Assurance Reserve"); // Armada");
		m.triggerPatrolAllowTransponderOff();
		//m.triggerFleetSetPatrolActionText("patrolling");
		m.triggerOrderFleetPatrol(culann.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		culann.getContainingLocation().addEntity(fleet);
		fleet.setLocation(culann.getPlanetEntity().getLocation().x, culann.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//		}
		
		return fleet;
	}
}




