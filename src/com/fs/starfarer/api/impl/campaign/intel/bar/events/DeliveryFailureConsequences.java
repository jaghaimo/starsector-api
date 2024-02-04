package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

public class DeliveryFailureConsequences implements EveryFrameScript, FleetEventListener {

	public static float RADIUS_FROM_CORE = 30000f; // may send fleet when within this radius from core
	public static float DAYS_IN_SYSTEM = 7f;
	
	protected float delayDays;
	protected boolean sentFleet;
	protected String name;
	protected String commodity;
	protected Gender gender;
	protected float reward;
	protected long seed;
	public DeliveryFailureConsequences(DeliveryMissionIntel intel) {
		name = intel.getEvent().getPerson().getNameString();
		gender = intel.getEvent().getPerson().getGender();
		commodity = Global.getSettings().getCommoditySpec(intel.getEvent().getCommodityId()).getLowerCaseName();
		
		reward = intel.getEvent().getReward();
		
		seed = Misc.genRandomSeed();
		
		delayDays = 100f + (float) Math.random() * 200f;
		
		if (DebugFlags.BAR_DEBUG) {
			delayDays = 0f;
		}
	}
	
	protected StarSystemAPI systemPlayerIsIn = null;
	protected float daysInSystem = 0f;
	public void advance(float amount) {
		if (sentFleet) return;
		
		float days = Misc.getDays(amount);
		//days *= 1000f;
		delayDays -= days;
		if (delayDays > 0) return;
		
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		float distFromCore = playerFleet.getLocationInHyperspace().length();
		if (distFromCore > RADIUS_FROM_CORE) {
			daysInSystem = 0f;
			systemPlayerIsIn = null;
			return;
		}
			
		if (!(playerFleet.getContainingLocation() instanceof StarSystemAPI)) {
			if ((daysInSystem > DAYS_IN_SYSTEM || DebugFlags.BAR_DEBUG) && systemPlayerIsIn != null) {
				float dist = Misc.getDistance(systemPlayerIsIn.getLocation(), playerFleet.getLocationInHyperspace());
				if (dist < 3000f) {
					sendFleet();
				}
			}
			daysInSystem = 0f;
			systemPlayerIsIn = null;
			return;
		}
		
		systemPlayerIsIn = (StarSystemAPI)playerFleet.getContainingLocation();
		daysInSystem += days;
	}
	
	protected void sendFleet() {
		if (sentFleet) return;
		sentFleet = true;
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI hunter = createBountyHunter();
		if (hunter != null) {
			Global.getSector().getHyperspace().addEntity(hunter);
			Vector2f hunterLoc = Misc.getPointAtRadius(playerFleet.getLocationInHyperspace(), 500f);
			hunter.setLocation(hunterLoc.x, hunterLoc.y);
			hunter.getAI().addAssignmentAtStart(FleetAssignment.INTERCEPT, playerFleet, 1000f, null);
			Misc.giveStandardReturnToSourceAssignments(hunter, false);
		}
		
	}

	protected CampaignFleetAPI createBountyHunter() {
		Random random = new Random(seed);
		String faction = Factions.INDEPENDENT;
		
		float pts = reward / 400;
		pts *= 0.8f + 0.4f * random.nextFloat();
		if (pts < 30) pts = 30;
		if (pts > 150) pts = 150; //maxes out at a 60k mission reward
		
		float qMod = reward / 100000f; // but the quality keeps going up
		
		String hunter = "bounty hunter";
		
		if (random.nextFloat() < 0.5f) {
			faction = Factions.PIRATES;
			pts *= 1.5f;
			hunter = "pirate";
		}
		
		FleetParamsV3 params = new FleetParamsV3(
				null,
				Global.getSector().getPlayerFleet().getLocationInHyperspace(),
				faction,
				null,
				FleetTypes.MERC_BOUNTY_HUNTER,
				pts, // combatPts
				0f, // freighterPts 
				pts * 0.1f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				qMod // qualityMod
		);
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet.isEmpty()) fleet = null;
		
		if (fleet != null) {
			EncounterOption option = fleet.getAI().pickEncounterOption(null, Global.getSector().getPlayerFleet());
			if (option == EncounterOption.DISENGAGE) {
				fleet = null;
			}
		}
		
		if (fleet != null) {
			//fleet.setFaction(Factions.INDEPENDENT, true);
			Misc.makeLowRepImpact(fleet, "dmi");

			fleet.addScript(new AutoDespawnScript(fleet));
			fleet.addEventListener(this);
			
			MemoryAPI memory = fleet.getMemoryWithoutUpdate();
			memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			memory.set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
			
			String hisOrHer = "his";
			String himOrHer = "him";
			if (gender == Gender.FEMALE) {
				hisOrHer = "her";
				himOrHer = "her";
			}
			memory.set("$dmi_bountyHunter", true);
			memory.set("$dmi_hisOrHer", hisOrHer);
			memory.set("$dmi_himOrHer", himOrHer);
			memory.set("$dmi_name", name);
			memory.set("$dmi_commodity", commodity);
			memory.set("$dmi_hunter", hunter);

			if (reward >= 50000) {
				AbilityPlugin eb = fleet.getAbility(Abilities.EMERGENCY_BURN);
				if (eb != null) eb.activate();
			}
		}
		return fleet;
	}
	
	

	public boolean isDone() {
		return sentFleet;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
		memory.unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}

}
