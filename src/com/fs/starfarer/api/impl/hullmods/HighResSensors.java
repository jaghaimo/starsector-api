package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

@SuppressWarnings("unchecked")
public class HighResSensors extends BaseLogisticsHullMod  implements HullModFleetEffect {

//	public static float BONUS = 60;
//	public static float CAPITAL_BONUS = 100;
	
	public static float MIN_CR = 0.1f;
	public static String MOD_KEY = "core_HighResSensors";
	
	private static Map mag = new HashMap();
//	static {
//		mag.put(HullSize.FRIGATE, Global.getSettings().getFloat("baseSensorFrigate"));
//		mag.put(HullSize.DESTROYER, Global.getSettings().getFloat("baseSensorDestroyer"));
//		mag.put(HullSize.CRUISER, Global.getSettings().getFloat("baseSensorCruiser"));
//		mag.put(HullSize.CAPITAL_SHIP, Global.getSettings().getFloat("baseSensorCapital"));
//	}
	static {
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 75f);
		mag.put(HullSize.CRUISER, 100f);
		mag.put(HullSize.CAPITAL_SHIP, 150f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getSensorStrength().modifyPercent(id, ((Float) mag.get(hullSize)).intValue());
//		float bonus = BONUS;
//		if (hullSize == HullSize.CAPITAL_SHIP) bonus = CAPITAL_BONUS;
//		stats.getSensorStrength().modifyFlat(id, bonus);
		
		stats.getDynamic().getMod(Stats.HRS_SENSOR_RANGE_MOD).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
//		if (index == 0) return "" + (int) BONUS;
//		if (index == 1) return "" + (int) CAPITAL_BONUS;
//		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
//		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
//		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		//if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}

	public void advanceInCampaign(CampaignFleetAPI fleet) {
	}
	public boolean withAdvanceInCampaign() {
		return false;
	}
	public boolean withOnFleetSync() {
		return true;
	}

	public void onFleetSync(CampaignFleetAPI fleet) {
		float modifier = getAdjustedHRSModifier(fleet, null, 0f);
		if (modifier <= 0) {
			fleet.getSensorRangeMod().unmodifyFlat(MOD_KEY);
		} else {
			fleet.getSensorRangeMod().modifyFlat(MOD_KEY, modifier, "Ships with high resolution sensors");
		}
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false; // no description from the csv
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("A ship with a high resolution sensors increases the fleet's sensor range by %s/%s/%s/%s," +
				" depending on hull size. " +
				"Each additional ship with high resolution sensors provides diminishing returns. " +
				"The higher the highest sensor range increase from a single ship in the fleet, the later diminishing returns kick in.", 
				opad, h,
				"" + ((Float) mag.get(HullSize.FRIGATE)).intValue(),
				"" + ((Float) mag.get(HullSize.DESTROYER)).intValue(),
				"" + ((Float) mag.get(HullSize.CRUISER)).intValue(),
				"" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue()
				);
		
		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float fleetMod = getAdjustedHRSModifier(fleet, null, 0f);
		float currShipMod = (Float) mag.get(hullSize);
		
		float fleetModWithOneMore = getAdjustedHRSModifier(fleet, null, currShipMod);
		float fleetModWithoutThisShip = getAdjustedHRSModifier(fleet, ship.getFleetMemberId(), 0f);
		
		tooltip.addPara("The total sensor strength increase for your fleet is %s.", opad, h,
				"" + (int)Math.round(fleetMod));
		
		float cr = ship.getCurrentCR();
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getId().equals(ship.getFleetMemberId())) {
				cr = member.getRepairTracker().getCR();
			}
		}
		
		if (cr < MIN_CR) {
			LabelAPI label = tooltip.addPara("This ship's combat readiness is below %s " +
					"and its high resolution sensors can not be utilized. Bringing this ship into readiness " +
					"would increase the fleetwide bonus to %s.",
					opad, h,
					"" + (int) Math.round(MIN_CR * 100f) + "%",
					"" + (int)Math.round(fleetModWithOneMore));
			label.setHighlightColors(bad, h);
			label.setHighlight("" + (int) Math.round(MIN_CR * 100f) + "%", "" + (int)Math.round(fleetModWithOneMore));
		} else {
			if (fleetMod > currShipMod) {
				tooltip.addPara("Removing this ship would decrease it to %s. Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithoutThisShip),
						"" + (int)Math.round(fleetModWithOneMore));
			} else {
				tooltip.addPara("Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithOneMore));
			}
		}
	}

	public static float getAdjustedHRSModifier(CampaignFleetAPI fleet, String skipId, float add) {
		float max = 0f;
		float total = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			if (member.getRepairTracker().getCR() < MIN_CR) continue;
			
			if (member.getId().equals(skipId)) { 
				continue;
			}
			float v = member.getStats().getDynamic().getMod(Stats.HRS_SENSOR_RANGE_MOD).computeEffective(0f);
			if (v <= 0) continue;
			
			if (v > max) max = v;
			total += v;
		}
		if (add > max) max = add;
		total += add;
		
		if (max <= 0) return 0f;
		float units = total / max;
		if (units <= 1) return max;
		float mult = Misc.logOfBase(2.5f, units) + 1f;
		float result = total * mult / units;
		if (result <= 0) {
			result = 0;
		} else {
			result = Math.round(result * 100f) / 100f;
			result = Math.max(result, 1f);
		}
		return result;
	}


}
