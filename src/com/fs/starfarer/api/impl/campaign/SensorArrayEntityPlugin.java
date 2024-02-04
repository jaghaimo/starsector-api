package com.fs.starfarer.api.impl.campaign;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoInDirection;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SensorArrayEntityPlugin extends BaseCampaignObjectivePlugin {

	public static float SENSOR_BONUS = 700f;
	public static float SENSOR_BONUS_MAKESHIFT = 400f;
	//public static float SENSOR_PENALTY_MULT_FROM_HACK = 0.75f;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		if (entity.getContainingLocation() == null || entity.isInHyperspace()) return;
		//if (isReset()) return;
		boolean reset = isReset();
		
		String id = getModId();
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			if (fleet.isInHyperspaceTransition()) continue;
			
			if (fleet.getFaction() == entity.getFaction() || (isHacked() && fleet.getFaction().isPlayerFaction())) {
				if (reset && !fleet.getFaction().isPlayerFaction()) {
					respondToFalseSensorReadings(fleet);
				} else if (reset && fleet.isPlayerFleet()) {
					spawnPlayerSensorReading(fleet);
				}
				
				String desc = "Sensor array";
				float bonus = SENSOR_BONUS;
				if (isMakeshift()) {
					desc = "Makeshift sensor array";
					bonus = SENSOR_BONUS_MAKESHIFT;
				}
				
//				if (fleet.getFaction() == entity.getFaction() && isHacked() && !entity.getFaction().isPlayerFaction()) {
//					fleet.getStats().addTemporaryModMult(0.1f, id,
//							desc, SENSOR_PENALTY_MULT_FROM_HACK, 
//							fleet.getStats().getSensorRangeMod());
//				}
				
				StatMod curr = fleet.getStats().getSensorRangeMod().getFlatBonus(id);
				if (curr == null || curr.value <= bonus) {
					fleet.getStats().addTemporaryModFlat(0.1f, id,
							desc, bonus, 
							fleet.getStats().getSensorRangeMod());
				}
			}
		}
		
	}
	
	protected boolean isMakeshift() {
		return entity.hasTag(Tags.MAKESHIFT);
	}
	
	public void printEffect(TooltipMakerAPI text, float pad) {
		int bonus = (int) SENSOR_BONUS;
		if (isMakeshift()) {
			bonus = (int) SENSOR_BONUS_MAKESHIFT;
		}
		
		text.addPara(BaseIntelPlugin.INDENT + "%s sensor range for all same-faction fleets in system",
				pad, Misc.getHighlightColor(), "+" + bonus);
		if (isReset()) {
			//text.addPara(BaseIntelPlugin.INDENT + "Auto-calibrating after factory reset; non-functional", 3f);
			text.addPara(BaseIntelPlugin.INDENT + "Generating false readings", 3f);
		}
		
//		text.addPara(BaseIntelPlugin.INDENT + "%s sensor range to same-faction fleets when hacked",
//				0f, Misc.getHighlightColor(), "-" + (int) Math.round((1f - SENSOR_PENALTY_MULT_FROM_HACK) * 100f) + "%");
	}

	public void printNonFunctionalAndHackDescription(TextPanelAPI text) {
		if (entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) {
			text.addPara("This one, however, does not appear to be transmitting a sensor telemetry broadcast. The cause of its lack of function is unknown.");
		}
		if (isHacked()) {
			text.addPara("You have a hack running on this sensor array.");
		}
//		if (isReset()) {
//			text.addPara("This sensor array is auto-calibrating after a factory reset and is effectively non-functional.");
//		}
	}
	
	
	
	@Override
	public void addHackStatusToTooltip(TooltipMakerAPI text, float pad) {
		int bonus = (int) SENSOR_BONUS;
		if (isMakeshift()) {
			bonus = (int) SENSOR_BONUS_MAKESHIFT;
		}
		text.addPara("%s sensor range for in-system fleets",
				pad, Misc.getHighlightColor(), "+" + bonus);
		
//		text.addPara("%s%% sensor range when hacked",
//				pad, Misc.getHighlightColor(), "-" + (int) Math.round((1f - SENSOR_PENALTY_MULT_FROM_HACK) * 100f));
		
		super.addHackStatusToTooltip(text, pad);
	}

	protected String getModId() {
		return "sensor_array";
	}
	

	public static String GHOST_RESPONSE = "ghost_response"; // custom value added to assignments so we know which to clear

	protected void spawnPlayerSensorReading(CampaignFleetAPI fleet) {
		Random random = Misc.random;
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.getBoolean(MemFlags.FLEET_NOT_CHASING_GHOST)) {
			return;
		}
		if (mem.getBoolean(MemFlags.FLEET_CHASING_GHOST)) {
			return;
		}
		boolean spawnReading = random.nextFloat() < 0.5f;
		//spawnReading = true;
		if (!spawnReading) {
			mem.set(MemFlags.FLEET_NOT_CHASING_GHOST, true, 1f + 2f * random.nextFloat());
			return;
		}
		
		float dur = 3f + 3f + random.nextFloat();
		mem.set(MemFlags.FLEET_NOT_CHASING_GHOST, true, dur * 0.5f);
		
		BaseSensorGhost g = new BaseSensorGhost(null, 0);
		
		float r = random.nextFloat();
		int maxBurn;
		if (r < 0.25f) {
			g.initEntity(g.genMediumSensorProfile(), g.genSmallRadius(), 0, fleet.getContainingLocation());
			maxBurn = 9 + random.nextInt(3);
		} else if (r < 0.6f) {
			g.initEntity(g.genLargeSensorProfile(), g.genMediumRadius(), 0, fleet.getContainingLocation());
			maxBurn = 8 + random.nextInt(3);
		} else {
			g.initEntity(g.genLargeSensorProfile(), g.genLargeRadius(), 0, fleet.getContainingLocation());
			maxBurn = 7 + random.nextInt(3);
		}
		
		
		if (!g.placeNearPlayer()) {
			return;
		}
		//g.setDespawnRange(200f);
		
		
		float speed = Misc.getSpeedForBurnLevel(maxBurn);
		float accelMult = speed / Misc.getSpeedForBurnLevel(20f);
		if (accelMult < 0.1f) accelMult = 0.1f;
		g.setAccelMult(1f/ accelMult);
		
		float dir = Misc.getAngleInDegrees(g.getEntity().getLocation(), fleet.getLocation());
		float sign = Math.signum(random.nextFloat() - 0.5f);
		dir += sign * (30f + random.nextFloat() * 60f);
		
		
		g.addBehavior(new GBGoInDirection(dur, dir, maxBurn));
		
		fleet.getContainingLocation().addScript(g);
		
	}
	
	protected void respondToFalseSensorReadings(CampaignFleetAPI fleet) {
		if (fleet.isStationMode()) return;
		if (fleet.getAI() == null) {
			return;
		}
		if (fleet.getAI().getAssignmentsCopy() == null) {
			return;
		}
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.getBoolean(MemFlags.FLEET_NOT_CHASING_GHOST)) {
			return;
		}
		if (mem.getBoolean(MemFlags.FLEET_CHASING_GHOST)) {
			return;
		}
		if (mem.getBoolean(MemFlags.FLEET_BUSY)) {
			return;
		}
		boolean patrol = mem.getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
		boolean warFleet = mem.getBoolean(MemFlags.MEMORY_KEY_WAR_FLEET);
		boolean pirate = mem.getBoolean(MemFlags.MEMORY_KEY_PIRATE);
		if (!patrol && !warFleet && !pirate) {
			return;
		}
		
		Random random = (Random) mem.get(MemFlags.FLEET_CHASING_GHOST_RANDOM);
		if (random == null) {
			random = Misc.getRandom(Misc.getSalvageSeed(fleet), 7);
			mem.set(MemFlags.FLEET_CHASING_GHOST_RANDOM, random, 30f);
		}
		
		boolean willRespond = random.nextFloat() < 0.75f;
		//willRespond = true;
		if (!willRespond) {
			mem.set(MemFlags.FLEET_NOT_CHASING_GHOST, true, 1f + 1f * random.nextFloat());
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
					   MemFlags.FLEET_CHASING_GHOST, GHOST_RESPONSE, false, 0f);
			for (FleetAssignmentDataAPI curr : fleet.getAI().getAssignmentsCopy()) {
				if (GHOST_RESPONSE.equals(curr.getCustom())) {
					fleet.getAI().removeAssignment(curr);
				}
			}
			return;
		}
		
		float chaseDur = (2.5f + (float) Math.random()) * 2f;
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
				MemFlags.FLEET_CHASING_GHOST, GHOST_RESPONSE, true, chaseDur);
		mem.set(MemFlags.FLEET_BUSY, true, chaseDur);
		mem.set(MemFlags.FLEET_NOT_CHASING_GHOST, true, chaseDur + 8f + 4f * random.nextFloat());
		
		float angle = Misc.getAngleInDegrees(fleet.getLocation()); // away from center of system;
		float arc = 270f;
		angle += arc/2f - arc * random.nextFloat();
		float dist = 3000f + 3000f * random.nextFloat();
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
		loc.scale(dist);
		Vector2f.add(loc, fleet.getLocation(), loc);
		

		String actionText = "investigating anomalous sensor reading";
		
		SectorEntityToken target = fleet.getContainingLocation().createToken(loc);
		fleet.addAssignmentAtStart(FleetAssignment.PATROL_SYSTEM, target, 3f, actionText, null);
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(GHOST_RESPONSE);
		}
		
		if (dist > 2000f) {
			fleet.addAssignmentAtStart(FleetAssignment.GO_TO_LOCATION, target, 3f, actionText, null);
			curr = fleet.getCurrentAssignment();
			if (curr != null) {
				curr.setCustom(GHOST_RESPONSE);
			}
		}
	}
	
//	protected void unrespond(CampaignFleetAPI fleet) {
//		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
//							   MemFlags.FLEET_CHASING_GHOST, GHOST_RESPONSE, false, 0f);
//		for (FleetAssignmentDataAPI curr : fleet.getAI().getAssignmentsCopy()) {
//			if (GHOST_RESPONSE.equals(curr.getCustom())) {
//				fleet.getAI().removeAssignment(curr);
//			}
//		}
//	}
//	
//	protected void respond(CampaignFleetAPI fleet) {
//		unrespond(fleet);
//		
//		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
//								MemFlags.FLEET_CHASING_GHOST, GHOST_RESPONSE, true, (1.5f + (float) Math.random()) * 0.2f);
//		
//		fleet.addAssignmentAtStart(FleetAssignment.PATROL_SYSTEM, params.target, 3f, params.actionText, null);
//		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
//		if (curr != null) {
//			curr.setCustom(GHOST_RESPONSE);
//		}
//		
//		float dist = Misc.getDistance(params.target, fleet);
//		if (dist > 2000f) {
//			fleet.addAssignmentAtStart(FleetAssignment.GO_TO_LOCATION, params.target, 3f, params.travelText, null);
//			//fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, params.target, 3f, params.travelText, null);
//			curr = fleet.getCurrentAssignment();
//			if (curr != null) {
//				curr.setCustom(GHOST_RESPONSE);
//			}
//		}
//		
//		//Global.getSector().addPing(fleet, Pings.DANGER);
//	}
}



