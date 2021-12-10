package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

@SuppressWarnings("unchecked")
public class PhaseField extends BaseHullMod implements HullModFleetEffect {

//	private static Map mag = new HashMap();
//	static {
//		mag.put(HullSize.FRIGATE, Global.getSettings().getFloat("baseSensorFrigate"));
//		mag.put(HullSize.DESTROYER, Global.getSettings().getFloat("baseSensorDestroyer"));
//		mag.put(HullSize.CRUISER, Global.getSettings().getFloat("baseSensorCruiser"));
//		mag.put(HullSize.CAPITAL_SHIP, Global.getSettings().getFloat("baseSensorCapital"));
//	}
	
	public static float MIN_CR = 0.1f;
	public static String MOD_KEY = "core_PhaseField";
	
	public static float PROFILE_MULT = 0.5f;
	
	public static float MIN_FIELD_MULT = 0.25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
		//stats.getDynamic().getMod(Stats.PHASE_FIELD_SENSOR_PROFILE_MOD).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - PROFILE_MULT) * 100f) + "%";
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
		float mult = getPhaseFieldMultBaseProfileAndTotal(fleet, null, 0f, 0f)[0];
		if (fleet.isTransponderOn()) mult = 1f;
		if (mult <= 0) {
			fleet.getDetectedRangeMod().unmodifyMult(MOD_KEY);
		} else {
			fleet.getDetectedRangeMod().modifyMult(MOD_KEY, mult, "Phase ships in fleet");
		}
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
		
		int numProfileShips = Global.getSettings().getInt("maxSensorShips");
		tooltip.addPara("In addition, the fleet's detected-at range is reduced by a multiplier based on the total "
				+ "sensor profile of the %s highest-profile ships in the fleet, and the total sensor strength of the %s "
				+ "phase ships with the highest sensor strength values. This effect only applies when the "
				+ "fleet's transponder is turned off.", opad,
				h,
				"" + numProfileShips, "" + numProfileShips);
		
		tooltip.addPara("Fleetwide sensor strength increases - such as from High Resolution Sensors - do not factor into "
				+ "this calculation.", opad);
		
		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
		
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float [] data = getPhaseFieldMultBaseProfileAndTotal(fleet, null, 0f, 0f);
		float [] dataWithOneMore = getPhaseFieldMultBaseProfileAndTotal(fleet, null,
				ship.getMutableStats().getSensorProfile().getModifiedValue(),
				ship.getMutableStats().getSensorStrength().getModifiedValue());
		float [] dataWithOneLess = getPhaseFieldMultBaseProfileAndTotal(fleet, ship.getFleetMemberId(), 0f, 0f);
		
		float mult = data[0];
		float profile = data[1];
		float sensors = data[2];
		
		tooltip.addPara("The sensor profile of the %s top ships in your fleet is %s. The sensor strength of the top %s phase ships "
				+ "is %s.", opad, h,
				"" + numProfileShips,
				"" + (int)Math.round(profile),
				"" + numProfileShips,
				"" + (int)Math.round(sensors)
				);
		
		tooltip.addPara("The detected-at range multiplier for your fleet is %s. The fleet's transponder must be off "
				+ "for the multiplier to be applied.",
				opad, h,
				Strings.X + Misc.getRoundedValueFloat(mult),
				"transponder must be off");
		
		float cr = ship.getCurrentCR();
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getId().equals(ship.getFleetMemberId())) {
				cr = member.getRepairTracker().getCR();
			}
		}
		
		if (cr < MIN_CR) {
			LabelAPI label = tooltip.addPara("This ship's combat readiness is below %s " +
					"and the phase field's fleetwide effect can not be utilized. Bringing this ship into readiness " +
					"would improve the multiplier to %s.",
					opad, h,
					"" + (int) Math.round(MIN_CR * 100f) + "%",
					Strings.X + Misc.getRoundedValueFloat(dataWithOneMore[0]));
			label.setHighlightColors(bad, h);
			label.setHighlight("" + (int) Math.round(MIN_CR * 100f) + "%", Strings.X + Misc.getRoundedValueFloat(dataWithOneMore[0]));
		} else {
			tooltip.addPara("Removing this ship would change the multiplier to %s. Adding another ship with the same sensor strength " +
					"would improve it to %s.", opad, h,
					Strings.X + Misc.getRoundedValueFloat(dataWithOneLess[0]),
					Strings.X + Misc.getRoundedValueFloat(dataWithOneMore[0]));
		}
	}

//	public static float getAdjustedPhaseFieldModifier(CampaignFleetAPI fleet, String skipId, float add) {
//		float max = 0f;
//		float total = 0f;
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//			if (member.isMothballed()) continue;
//			if (member.getRepairTracker().getCR() < MIN_CR) continue;
//			
//			if (member.getId().equals(skipId)) { 
//				continue;
//			}
//			float v = member.getStats().getDynamic().getMod(Stats.PHASE_FIELD_SENSOR_PROFILE_MOD).computeEffective(0f);
//			if (v <= 0) continue;
//			
//			if (v > max) max = v;
//			total += v;
//		}
//		if (add > max) max = add;
//		total += add;
//		
//		if (max <= 0) return 0f;
//		float units = total / max;
//		if (units <= 1) return max;
//		float mult = Misc.logOfBase(2.5f, units) + 1f;
//		float result = total * mult / units;
//		if (result <= 0) {
//			result = 0;
//		} else {
//			result = Math.round(result * 100f) / 100f;
//			result = Math.max(result, 1f);
//		}
//		return result;
//	}
	
	
	public static float [] getPhaseFieldMultBaseProfileAndTotal(CampaignFleetAPI fleet, String skipId, float addProfile, float addSensor) {
		List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> phase = new ArrayList<FleetMemberAPI>();
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.getId().equals(skipId)) { 
				continue;
			}
			members.add(member);
			
			if (member.isMothballed()) continue;
			if (member.getRepairTracker().getCR() < MIN_CR) continue;
			if (member.getVariant().hasHullMod(HullMods.PHASE_FIELD)) {
				phase.add(member);
			}
		}
		
		float [] profiles = new float [members.size()];
		if (addProfile <= 0) {
			profiles = new float [members.size()];
		} else {
			profiles = new float [members.size() + 1];
		}
		float [] phaseSensors;
		if (addSensor <= 0) {
			phaseSensors = new float [phase.size()];
		} else {
			phaseSensors = new float [phase.size() + 1];
		}
		
		int i = 0;
		for (FleetMemberAPI member : members) {
			profiles[i] = member.getStats().getSensorProfile().getModifiedValue();
			i++;
		}
		if (addProfile > 0) profiles[i] = addProfile;
		i = 0;
		for (FleetMemberAPI member : phase) {
			phaseSensors[i] = member.getStats().getSensorStrength().getModifiedValue();
			i++;
		}
		if (addSensor > 0) phaseSensors[i] = addSensor;
		
		int numProfileShips = Global.getSettings().getInt("maxSensorShips");
		int numPhaseShips = numProfileShips;
		
		float totalProfile = getTopKValuesSum(profiles, numProfileShips);
		float totalPhaseSensors = getTopKValuesSum(phaseSensors, numPhaseShips);
		
		float total = Math.max(totalProfile + totalPhaseSensors, 1f);
		
		float mult = totalProfile / total;
		
		if (mult < MIN_FIELD_MULT) mult = MIN_FIELD_MULT;
		if (mult > 1f) mult = 1f;
		
		return new float[] {mult, totalProfile, totalPhaseSensors};
	}
	

	public static float getTopKValuesSum(float [] arr, int k) {
		k = Math.min(k, arr.length);
		
		float kVal = Misc.findKth(arr, arr.length - k);
		float total = 0;
		int found = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > kVal) {
				found++;
				total += arr[i];
			}
		}
		if (k > found) {
			total += (k - found) * kVal;
		}
		return total;
	}
}



