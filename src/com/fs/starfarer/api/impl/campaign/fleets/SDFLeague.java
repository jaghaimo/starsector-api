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
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SDFLeague extends SDFBase {
	
	public SDFLeague() {
	}

	@Override
	protected String getFactionId() {
		return Factions.PERSEAN;
	}
	
	protected SkillPickPreference getCommanderShipSkillPreference() {
		return SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE;
	}

	@Override
	protected MarketAPI getSourceMarket() {
		return Global.getSector().getEconomy().getMarket("kazeron");
	}

	@Override
	protected String getDefeatTriggerToUse() {
		return "SDFLeagueDefeated";
	}


	@Override
	public CampaignFleetAPI spawnFleet() {
		
		WeightedRandomPicker<String> capitalShipNames = new WeightedRandomPicker<>(random);
		capitalShipNames.add("Pride of Hannan");
		capitalShipNames.add("Pride of Kato");
		capitalShipNames.add("Pride of Yaribay");
		capitalShipNames.add("Reynard's Gift");
		capitalShipNames.add("Fortuna's Gift");
		capitalShipNames.add("Mairaath Remembered");
		capitalShipNames.add("Navarch Arnulf Hannan");
		capitalShipNames.add("Navarch Mars Kato");
		capitalShipNames.add("Navarch Leandro Tethys");
		capitalShipNames.add("Navarch Herman Zhou");
		
		MarketAPI kazeron = getSourceMarket();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = kazeron.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_2, getFactionId(), FleetTypes.PATROL_LARGE, loc);
		
		m.triggerSetFleetSizeFraction(1.25f);
		
		m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.HIGHER);
		m.triggerSetFleetDoctrineComp(5, 0, 0);
		m.triggerSetFleetCommander(getPerson());

		m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
		m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
		m.triggerFleetAddCommanderSkill(Skills.SUPPORT_DOCTRINE, 1);
		m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
		
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, kazeron);
		//m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("All-League Aegis"); // Defense Fleet");
		m.triggerPatrolAllowTransponderOff();
		//m.triggerFleetSetPatrolActionText("patrolling");
		m.triggerOrderFleetPatrol(kazeron.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		kazeron.getContainingLocation().addEntity(fleet);
		fleet.setLocation(kazeron.getPlanetEntity().getLocation().x, kazeron.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isCapital()) {
				String name = capitalShipNames.pickAndRemove();
				if (name != null) {
					member.setShipName(name);
				}
			}
		}
		
		return fleet;
	}
}




