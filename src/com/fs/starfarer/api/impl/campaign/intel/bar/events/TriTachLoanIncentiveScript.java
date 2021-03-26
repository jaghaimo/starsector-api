package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.StarSystemAPI;
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

public class TriTachLoanIncentiveScript implements EveryFrameScript {

	protected float delayDays;
	protected boolean sentFleet;
	//protected TriTachLoanIntel intel;
	protected Gender gender;
	protected long seed;
	public TriTachLoanIncentiveScript(TriTachLoanIntel intel) {
		//this.intel = intel;
		gender = intel.getEvent().getPerson().getGender();
		seed = Misc.genRandomSeed();
		
		// revenge is a dish best served after at least this many days
		delayDays = 200f + (float) Math.random() * 100f;
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
		
		
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		float distFromCore = playerFleet.getLocationInHyperspace().length();
		if (distFromCore > 30000f) {
			daysInSystem = 0f;
			systemPlayerIsIn = null;
			return;
		}
			
		if (!(playerFleet.getContainingLocation() instanceof StarSystemAPI)) {
			if ((daysInSystem > 7f || DebugFlags.BAR_DEBUG) && systemPlayerIsIn != null) {
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
			
			hunter.getMemoryWithoutUpdate().set("$ttli_bountyHunter", true);
			//PersonAPI person = hunter.getCommander();
			//person.getMemoryWithoutUpdate().set("$mpm_eventRef", this, mission.getBaseDuration());
		}
		
	}

	protected CampaignFleetAPI createBountyHunter() {
		float pts = 200f;
		FleetParamsV3 params = new FleetParamsV3(
				null,
				Global.getSector().getPlayerFleet().getLocationInHyperspace(),
				Factions.TRITACHYON,
				1f, 
				FleetTypes.MERC_BOUNTY_HUNTER,
				pts, // combatPts
				0f, // freighterPts 
				pts * 0.1f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);
		params.officerNumberBonus = 4;
		params.officerLevelBonus = 3;
		params.doctrineOverride = Global.getSector().getFaction(Factions.TRITACHYON).getDoctrine().clone();
		params.doctrineOverride.setWarships(3);
		params.doctrineOverride.setPhaseShips(3);
		params.doctrineOverride.setCarriers(1);
		params.random = new Random(seed);
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet.isEmpty()) fleet = null;
		
		if (fleet != null) {
			fleet.setFaction(Factions.INDEPENDENT, true);
			Misc.makeLowRepImpact(fleet, "ttli");

			fleet.addScript(new AutoDespawnScript(fleet));
			
			MemoryAPI memory = fleet.getMemoryWithoutUpdate();
			memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			
			String hisOrHer = "his";
			if (gender == Gender.FEMALE) hisOrHer = "her";
			memory.set("$ttli_hisOrHer", hisOrHer);
			
			AbilityPlugin eb = fleet.getAbility(Abilities.EMERGENCY_BURN);
			if (eb != null) eb.activate();
		}
		return fleet;
	}
	
	

	public boolean isDone() {
		return sentFleet;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
