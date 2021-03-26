package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RepairGantry extends BaseHullMod {
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 10f);
		mag.put(HullSize.DESTROYER, 25f);
		mag.put(HullSize.CRUISER, 30f);
		mag.put(HullSize.CAPITAL_SHIP, 40f);
	}
	
	public static final float BATTLE_SALVAGE_MULT = .2f;
	public static final float MIN_CR = 0.1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD).modifyFlat(id, SALVAGE_MODIFIER);
		stats.getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD).modifyFlat(id, (Float) mag.get(hullSize) * 0.01f);
		//stats.getDynamic().getMod(Stats.BATTLE_SALVAGE_VALUE_MULT_MOD).modifyFlat(id, (Float) mag.get(hullSize) * 0.01f * (Float) mag.get(hullSize) * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) (SALVAGE_MODIFIER * 100f);
		//if (index == 1) return "" + (int) (BATTLE_SALVAGE_MODIFIER * 100f);
		
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		if (index == 4) return "" + (int)Math.round(BATTLE_SALVAGE_MULT * 100f) + "%";
		
		return null;
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		
	}
	
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("Each additional ship with a salvage gantry provides diminishing returns. " +
				"The higher the highest recovery bonus from a single ship in the fleet, the later diminishing returns kick in.", opad);
		
		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float fleetMod = getAdjustedGantryModifier(fleet, null, 0f);
		float currShipMod = (Float) mag.get(hullSize) * 0.01f;
		
		float fleetModWithOneMore = getAdjustedGantryModifier(fleet, null, currShipMod);
		float fleetModWithoutThisShip = getAdjustedGantryModifier(fleet, ship.getFleetMemberId(), 0f);
		
		tooltip.addPara("The total resource recovery bonus for your fleet is %s.", opad, h,
				"" + (int)Math.round(fleetMod * 100f) + "%");
		
		float cr = ship.getCurrentCR();
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getId().equals(ship.getFleetMemberId())) {
				cr = member.getRepairTracker().getCR();
			}
		}
		
		if (cr < MIN_CR) {
			LabelAPI label = tooltip.addPara("This ship's combat readiness is below %s " +
					"and the gantry can not be utilized. Bringing this ship into readiness " +
					"would increase the fleetwide bonus to %s.",
					opad, h,
					"" + (int) Math.round(MIN_CR * 100f) + "%",
					"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			label.setHighlightColors(bad, h);
			label.setHighlight("" + (int) Math.round(MIN_CR * 100f) + "%", "" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			
//			tooltip.addPara("Bringing this ship into readiness " +
//					"would increase the fleet's bonus to %s.", opad, h,
//					"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
		} else {
			if (fleetMod > currShipMod) {
				tooltip.addPara("Removing this ship would decrease it to %s. Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithoutThisShip * 100f) + "%",
						"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			} else {
				tooltip.addPara("Adding another ship of the same type " +
						"would increase it to %s.", opad, h,
						"" + (int)Math.round(fleetModWithOneMore * 100f) + "%");
			}
		}
		
		tooltip.addPara("The fleetwide post-battle salvage bonus is %s.", opad, h,
				"" + (int)Math.round(getAdjustedGantryModifierForPostCombatSalvage(fleet) * 100f) + "%");
//				"" + Misc.getRoundedValueMaxOneAfterDecimal(
//						getAdjustedGantryModifierForPostCombatSalvage(fleet) * 100f) + "%");
		
	}

	
	public static float getAdjustedGantryModifierForPostCombatSalvage(CampaignFleetAPI fleet) {
		return getAdjustedGantryModifier(fleet, null, 0) * BATTLE_SALVAGE_MULT;
	}
	
	public static float getAdjustedGantryModifier(CampaignFleetAPI fleet, String skipId, float add) {
		//List<Pair<FleetMemberAPI, Float>> values = new ArrayList<Pair<FleetMemberAPI,Float>>();
		
		float max = 0f;
		float total = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			if (member.getRepairTracker().getCR() < MIN_CR) continue;
			
			if (member.getId().equals(skipId)) { 
				continue;
			}
			float v = member.getStats().getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD).computeEffective(0f);
			if (v <= 0) continue;
			
//			Pair<FleetMemberAPI, Float> p = new Pair<FleetMemberAPI, Float>(member, v);
//			values.add(p);
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
			result = Math.max(result, 0.01f);
		}
		return result;
	}
	
	
}









