package com.fs.starfarer.api.characters;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SkillsChangeRemoveExcessOPEffect extends BaseSkillsChangeEffect {

	public static class OPEffectData {
		FleetMemberAPI member;
		int maxOP;
		int currOP;
	}
	
	public static class OPDataMap {
		Map<FleetMemberAPI, OPEffectData> map = new LinkedHashMap<FleetMemberAPI, OPEffectData>();
	}
	public void setMap(OPDataMap map, Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		dataMap.put(key, map);
	}
	public OPDataMap getMap(Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		OPDataMap map = (OPDataMap)dataMap.get(key);
		if (map == null) {
			map = new OPDataMap();
			dataMap.put(key, map);
		}
		return map;
	}
	
	public static int getMaxOP(ShipHullSpecAPI hull, MutableCharacterStatsAPI stats) {
		return hull.getOrdnancePoints(stats);
	}
	
	
	public OPDataMap getEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		OPDataMap result = new OPDataMap();
		
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			OPEffectData data = new OPEffectData();
			int maxOPPre = getMaxOP(member.getHullSpec(), from);
			int maxOPPost = getMaxOP(member.getHullSpec(), to);
			
			int op = member.getVariant().computeOPCost(to);
			
			data.member = member;
			data.maxOP = maxOPPost;
			data.currOP = op;
			
			if (maxOPPost < maxOPPre && op > maxOPPost) {
				result.map.put(member, data);
			}
		}
		return result;
	}
	
	
	@Override
	public boolean hasEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		return !getEffects(from, to).map.isEmpty();
	}

	@Override
	public void printEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, TooltipMakerAPI info, Map<String, Object> dataMap) {
		super.prepare();
		
		OPDataMap map = getEffects(from, to);
		setMap(map, dataMap);
		
		info.addSectionHeading("Excess ordnance points", base, dark, Alignment.MID, 15f);
		
		info.addPara("Ships using more than their maximum ordnance points will have "
				+ "vents, capacitors, hullmods, and other equipment removed to bring them under the limit.", 10f,
				Misc.getNegativeHighlightColor(),
				"vents, capacitors, hullmods, and other equipment removed"
				);
	}
	
	@Override
	public void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap) {
	}
	
	@Override
	public void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap) {
		OPDataMap map = getMap(dataMap);
		
		for (OPEffectData data : map.map.values()) {
			clampOP(data.member, to);
		}
	}
	
	public static void clampOP(FleetMemberAPI member, MutableCharacterStatsAPI stats) {
		int maxOP = getMaxOP(member.getHullSpec(), stats);
		int op = member.getVariant().computeOPCost(stats);
		int remove = op - maxOP;
		if (remove > 0) {
			ShipVariantAPI variant = member.getVariant();
			variant = variant.clone();
			variant.setSource(VariantSource.REFIT);
			
			int caps = variant.getNumFluxCapacitors();
			int curr = Math.min(caps, remove);
			variant.setNumFluxCapacitors(caps - curr);
			remove -= curr;
			if (remove > 0) {
				int vents = variant.getNumFluxVents();
				curr = Math.min(vents, remove);
				variant.setNumFluxVents(vents - curr);
				remove -= curr;
			}
			if (remove > 0) {
				for (String modId : variant.getNonBuiltInHullmods()) {
					HullModSpecAPI mod = Global.getSettings().getHullModSpec(modId);
					curr = mod.getCostFor(member.getHullSpec().getHullSize());
					variant.removeMod(modId);
					remove -= curr;
					if (remove <= 0) break;
				}
			}
			// would need to add these to player cargo; gets potentially messy - don't do it
//			if (remove > 0) {
//				for (String slotId : variant.getNonBuiltInWeaponSlots()) {
//					WeaponSpecAPI spec = variant.getWeaponSpec(slotId);
//					curr = (int)Math.round(spec.getOrdnancePointCost(stats, member.getStats()));
//					variant.clearSlot(slotId);
//					remove -= curr;
//					if (remove <= 0) break;
//				}
//			}
//			if (remove > 0) {
//				for (int i = 0; i < variant.getWings().size(); i++) {
//					if (i < variant.getHullSpec().getBuiltInWings().size()) continue;
//					FighterWingSpecAPI spec = variant.getWing(i);
//					curr = (int)Math.round(spec.getOpCost(member.getStats()));
//					variant.setWingId(i, "");
//					remove -= curr;
//					if (remove <= 0) break;
//				}
//			}
			
			member.setVariant(variant, false, false);
		}
	}
	
}







