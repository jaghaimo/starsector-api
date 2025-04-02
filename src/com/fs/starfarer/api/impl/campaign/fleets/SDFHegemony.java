package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
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

public class SDFHegemony extends SDFBase {
	
	public SDFHegemony() {
	}

	@Override
	protected String getFactionId() {
		return Factions.HEGEMONY;
	}
	
	protected SkillPickPreference getCommanderShipSkillPreference() {
		return SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE;
	}

	@Override
	protected MarketAPI getSourceMarket() {
		return Global.getSector().getEconomy().getMarket("coatl");
	}

	@Override
	protected String getDefeatTriggerToUse() {
		return "SDFHegemonyDefeated";
	}


	@Override
	public CampaignFleetAPI spawnFleet() {
		
		MarketAPI coatl = getSourceMarket();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = coatl.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_1, getFactionId(), FleetTypes.PATROL_LARGE, loc);
		
		m.triggerSetFleetSizeFraction(1.25f);
		
		m.triggerSetFleetOfficers( OfficerNum.ALL_SHIPS, OfficerQuality.HIGHER);
		m.triggerSetFleetDoctrineComp(5, 0, 0);
		m.triggerSetFleetCommander(getPerson());

		m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
		m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
		m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		m.triggerFleetAddCommanderSkill(Skills.OFFICER_TRAINING, 1);
		
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, coatl);
		//m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Core Worlds Armada");
		m.triggerPatrolAllowTransponderOff();
		//m.triggerFleetSetPatrolActionText("patrolling");
		m.triggerOrderFleetPatrol(coatl.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		coatl.getContainingLocation().addEntity(fleet);
		fleet.setLocation(coatl.getPlanetEntity().getLocation().x, coatl.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.getHullId().equals("legion")) {
				member.setVariant(getVariant("legion_xiv_Elite"), false, false);
			} else if (member.getHullId().equals("onslaught")) {
				member.setVariant(getVariant("onslaught_xiv_Elite"), false, false);
			} else if (member.getHullId().equals("eagle")) {
				member.setVariant(getVariant("eagle_xiv_Elite"), false, false);
			} else if (member.getHullId().equals("falcon")) {
				if (random.nextFloat() < 0.5f) {
					member.setVariant(getVariant("falcon_xiv_Elite"), false, false);
				} else {
					member.setVariant(getVariant("falcon_xiv_Escort"), false, false);
				}
			} else if (member.getHullId().equals("dominator")) {
				member.setVariant(getVariant("dominator_XIV_Elite"), false, false);
			} else if (member.getHullId().equals("enforcer")) {
				member.setVariant(getVariant("enforcer_XIV_Elite"), false, false);
			}
			
//				member.setVariant(member.getVariant().clone(), false, false);
//				member.getVariant().setSource(VariantSource.REFIT);
//				member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
//				member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		}
		
		return fleet;
	}
}




