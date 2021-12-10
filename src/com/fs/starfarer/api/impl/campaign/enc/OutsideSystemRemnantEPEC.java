package com.fs.starfarer.api.impl.campaign.enc;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.util.Misc;

public class OutsideSystemRemnantEPEC extends BaseEPEncounterCreator {
	
	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		if (!(point.custom instanceof RemnantStationFleetManager)) return;
		
		Random random = manager.getRandom();
		RemnantStationFleetManager fm = (RemnantStationFleetManager) point.custom;
		
		int difficulty = 0;
		int max = 10;
		float mult = 1f;
		if (fm.getSource() != null && fm.getSource().getStarSystem() != null && 
				fm.getSource().getStarSystem().hasTag(Tags.THEME_REMNANT_SUPPRESSED)) {
			max = 3;
			mult = 0.25f;
		}
		
		difficulty += (int) Math.min(fm.getTotalLost() * mult, max);
		difficulty += random.nextInt(4);
		if (difficulty > 10) difficulty = 10;
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = point.getLocInHyper();
		
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.VERY_HIGH;
		OfficerQuality oQuality = OfficerQuality.AI_MIXED;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		String type = FleetTypes.PATROL_SMALL;
		
		if (difficulty <= 1) {
			size = FleetSize.VERY_SMALL;
			type = FleetTypes.PATROL_SMALL;
			oQuality = OfficerQuality.AI_GAMMA;
		} else if (difficulty <= 2) {
			size = FleetSize.SMALL;
			type = FleetTypes.PATROL_SMALL;
			oQuality = OfficerQuality.AI_GAMMA;
		} else if (difficulty <= 5) {
			size = FleetSize.MEDIUM;
			type = FleetTypes.PATROL_SMALL;
			oQuality = OfficerQuality.AI_BETA_OR_GAMMA;
		} else if (difficulty <= 7) {
			size = FleetSize.LARGE;
			type = FleetTypes.PATROL_MEDIUM;
			oQuality = OfficerQuality.AI_BETA_OR_GAMMA;
		} else if (difficulty == 8) {
			size = FleetSize.LARGE;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_MIXED;
		} else if (difficulty == 9) {
			size = FleetSize.LARGER;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_ALPHA;
		} else {
			size = FleetSize.VERY_LARGE;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_ALPHA;
		}
		
		m.triggerCreateFleet(size, quality, Factions.REMNANTS, type, loc);
		m.triggerSetFleetOfficers(oNum, oQuality);
		m.triggerSetRemnantConfigActive();
		m.triggerFleetUnsetAllowLongPursuit();
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			point.where.addEntity(fleet);
			fleet.setLocation(point.loc.x, point.loc.y);
			Vector2f spawnLoc = Misc.getPointWithinRadius(point.loc, 1000f);
			SectorEntityToken e = point.where.createToken(spawnLoc);
			String actionText = "patrolling";
			if (difficulty <= 2) actionText = "reconnoitering";
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, e, 30f * random.nextFloat(), actionText);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, fm.getSource(), 1000f, "returning to " + fm.getSource().getName());
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, fm.getSource(), 3f + random.nextFloat() * 2f, "uploading encrypted data");
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, fm.getSource(), 10f);
			fleet.addScript(new AutoDespawnScript(fleet));
		}
	}

	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		if (!EncounterManager.EP_TYPE_OUTSIDE_SYSTEM.equals(point.type)) return 0f;
		if (!(point.custom instanceof RemnantStationFleetManager)) return 0f;
		RemnantStationFleetManager fm = (RemnantStationFleetManager) point.custom;
		
		float mult = 0.5f;
		if (fm.getSource() != null && fm.getSource().getStarSystem() != null && 
				fm.getSource().getStarSystem().hasTag(Tags.THEME_REMNANT_SUPPRESSED)) {
			mult = 0.1f;
		}
		
		return 10f * (float) Math.min(10f, fm.getTotalLost()) * mult;
	}
	
	
	
}





