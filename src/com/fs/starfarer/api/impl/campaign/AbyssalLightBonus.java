package com.fs.starfarer.api.impl.campaign;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTAbyssalLightFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class AbyssalLightBonus implements EveryFrameScript {

	public static float BURN_BONUS_MULT = 1f;
	public static float BURN_BONUS_FLAT = 5f;
	public static float LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS = 3f;
	
	public static float SENSOR_BONUS_FLAT = 1000f;
	public static float LIGHT_YEARS_PER_UNIT_OF_SENSOR_BONUS = 3f;
	
	
	public static AbyssalLightBonus get(CampaignFleetAPI fleet) {
		for (EveryFrameScript s : fleet.getScripts()) {
			if (s instanceof AbyssalLightBonus) {
				return (AbyssalLightBonus) s;
			}
		}
		AbyssalLightBonus script = new AbyssalLightBonus(fleet);
		fleet.addScript(script);
		return script;
	}
	
	protected CampaignFleetAPI fleet;
	protected float burnBonus = 0f;
	protected float sensorBonus = 0f;
	
	protected int framesSpentPending = 0;
	protected float pendingBurnBonus = 0;
	protected float pendingSensorBonus = 0;
	protected int pendingTopographyPoints = 0;
	protected int pendingLightsCount = 0;
	protected Vector2f prevLoc = null;
	
	public AbyssalLightBonus(CampaignFleetAPI fleet) {
		this.fleet = fleet;
		prevLoc = new Vector2f(fleet.getLocation());
	}
	
	public void advance(float amount) {
		if (fleet.isPlayerFleet()) {
			checkPendingAndSendMessages();
		}
		
		float depth = Misc.getAbyssalDepth(fleet);
		if (depth < 1f) {
			burnBonus = 0f;
			sensorBonus = 0f;
			return;
		}
		
		Vector2f loc = fleet.getLocation();
		float distLY = Misc.getDistanceLY(loc, prevLoc);
		prevLoc.set(fleet.getLocation());
		
		burnBonus -= distLY / LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS;
		sensorBonus -= distLY / LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS;
		
		if (burnBonus < 0) burnBonus = 0;
		if (sensorBonus < 0) sensorBonus = 0;
		
		if (burnBonus > 0) {
			if (BURN_BONUS_MULT != 1f) {
				float burnMult = BURN_BONUS_MULT;
				if (burnBonus < 1f) {
					burnMult = 1f + (burnMult - 1f) * burnBonus;
					if (burnMult < 1.1f) burnMult = 1.1f;
				}
				String ly = Misc.getRoundedValueOneAfterDecimalIfNotWhole(getBurnBonusLYRemaining(burnBonus));
				fleet.getStats().addTemporaryModMult(0.1f, "abyssalLight_1",
						"Abyssal light (for " + ly + " more light-years)", burnMult, 
						fleet.getStats().getFleetwideMaxBurnMod());
			}
			if (BURN_BONUS_FLAT > 0) {
				float burnFlat = BURN_BONUS_FLAT / HyperspaceTerrainPlugin.ABYSS_BURN_MULT;
				if (burnBonus < 1f / LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS) {
					burnFlat *= burnBonus * LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS;
				}
				burnFlat = Math.round(burnFlat);
				if (burnFlat < 1f) burnFlat = 1f;
				
				String ly = Misc.getRoundedValueOneAfterDecimalIfNotWhole(getBurnBonusLYRemaining(burnBonus));
				fleet.getStats().addTemporaryModFlat(0.1f, "abyssalLight_2",
						"Abyssal light (for " + ly + " more light-years)", burnFlat, 
						fleet.getStats().getFleetwideMaxBurnMod());
			}
		}
		
		if (sensorBonus > 0) {
			if (SENSOR_BONUS_FLAT > 0) {
				float sensorFlat = SENSOR_BONUS_FLAT / HyperspaceTerrainPlugin.ABYSS_SENSOR_RANGE_MULT;
				if (sensorBonus < 1f / LIGHT_YEARS_PER_UNIT_OF_SENSOR_BONUS) {
					sensorFlat *= sensorBonus * LIGHT_YEARS_PER_UNIT_OF_SENSOR_BONUS;
				}
				sensorFlat = Math.round(sensorFlat);
				if (sensorFlat < 1f) sensorFlat = 1f;
				
				String ly = Misc.getRoundedValueOneAfterDecimalIfNotWhole(getSensorBonusLYRemaining(sensorBonus));
				fleet.getStats().addTemporaryModFlat(0.1f, "abyssalLight_3",
						"Abyssal light (for " + ly + " more light-years)", sensorFlat, 
						fleet.getStats().getSensorRangeMod());
			}
		}
	}
	
	public float getBurnBonusLYRemaining(float bonus) {
		return bonus * LIGHT_YEARS_PER_UNIT_OF_BURN_BONUS;
	}
	
	public float getSensorBonusLYRemaining(float bonus) {
		return bonus * LIGHT_YEARS_PER_UNIT_OF_SENSOR_BONUS;
	}
	
	public void addBurnBonus(float bonus) {
		if (fleet.isPlayerFleet()) {
			pendingBurnBonus += bonus;
			framesSpentPending = 0;
		} else {
			burnBonus += bonus;
		}
	}
	
	public void addSensorBonus(float bonus) {
		if (fleet.isPlayerFleet()) {
			pendingSensorBonus += bonus;
			framesSpentPending = 0;
		} else {
			sensorBonus += bonus;
		}
	}
	
	public void addTopographyPoints(int points) {
		pendingTopographyPoints += points;
		pendingLightsCount++;
		framesSpentPending = 0;
	}
	
	
	public void checkPendingAndSendMessages() {
		framesSpentPending++;
		if (framesSpentPending > 1) {
			framesSpentPending = 0;

			if (pendingTopographyPoints > 0) {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTAbyssalLightFactor(pendingTopographyPoints, pendingLightsCount > 1), null);
				pendingTopographyPoints = 0;
				pendingLightsCount = 0;
			}
			if (pendingBurnBonus > 0) {
				MessageIntel msg = new MessageIntel();
				String ly = Misc.getRoundedValueOneAfterDecimalIfNotWhole(getSensorBonusLYRemaining(pendingBurnBonus));
				msg.addLine("Increased burn level for %s light-years", Misc.getTextColor(),
						new String[] {"+" + ly}, Misc.getHighlightColor());
				msg.setSound(Sounds.NONE);
				Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
				burnBonus += pendingBurnBonus;
				pendingBurnBonus = 0;
			}
			
			if (pendingSensorBonus > 0) {
				MessageIntel msg = new MessageIntel();
				String ly = Misc.getRoundedValueOneAfterDecimalIfNotWhole(getSensorBonusLYRemaining(pendingSensorBonus));
				msg.addLine("Increased sensor range for %s light-years", Misc.getTextColor(),
						new String[] {"+" + ly}, Misc.getHighlightColor());
				msg.setSound(Sounds.NONE);
				Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
				sensorBonus += pendingSensorBonus;
				pendingSensorBonus = 0;
			}
			
		}
	}
	
	
	public float getBurnBonus() {
		return burnBonus;
	}

	public void setBurnBonus(float burnBonus) {
		this.burnBonus = burnBonus;
	}

	public float getSensorBonus() {
		return sensorBonus;
	}

	public void setSensorBonus(float sensorBonus) {
		this.sensorBonus = sensorBonus;
	}

	public boolean isDone() {
		return burnBonus <= 0 && sensorBonus <= 0 && 
				pendingBurnBonus <= 0 && pendingSensorBonus <= 0 && pendingTopographyPoints <= 0;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
