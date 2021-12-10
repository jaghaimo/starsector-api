package com.fs.starfarer.api.characters;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SkillsChangeRemoveVentsCapsEffect extends BaseSkillsChangeEffect {

	public static class VentsCapsEffectData {
		FleetMemberAPI member;
		int maxVents;
		int maxCaps;
	}
	
	public static class VentsCapsDataMap {
		Map<FleetMemberAPI, VentsCapsEffectData> map = new LinkedHashMap<FleetMemberAPI, VentsCapsEffectData>();
	}
	public void setMap(VentsCapsDataMap map, Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		dataMap.put(key, map);
	}
	public VentsCapsDataMap getMap(Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		VentsCapsDataMap map = (VentsCapsDataMap)dataMap.get(key);
		if (map == null) {
			map = new VentsCapsDataMap();
			dataMap.put(key, map);
		}
		return map;
	}
	
	public static int getMaxVents(HullSize size, MutableCharacterStatsAPI stats) {
		int maxVents = CoreAutofitPlugin.getBaseMax(size);
		if (stats != null) {
			maxVents = (int) stats.getMaxVentsBonus().computeEffective(maxVents);
		}
		return maxVents;
	}
	
	public static int getMaxCaps(HullSize size, MutableCharacterStatsAPI stats) {
		int maxCapacitors = CoreAutofitPlugin.getBaseMax(size);
		if (stats != null) {
			maxCapacitors = (int) stats.getMaxCapacitorsBonus().computeEffective(maxCapacitors);
		}
		return maxCapacitors;
	}
	
	
	public VentsCapsDataMap getEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		VentsCapsDataMap result = new VentsCapsDataMap();
		
		
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			VentsCapsEffectData data = new VentsCapsEffectData();
			int maxVentsPre = getMaxVents(member.getHullSpec().getHullSize(), from);
			int maxCapsPre = getMaxCaps(member.getHullSpec().getHullSize(), from);
			
			int maxVentsPost = getMaxVents(member.getHullSpec().getHullSize(), to);
			int maxCapsPost = getMaxCaps(member.getHullSpec().getHullSize(), to);
			
			int caps = member.getVariant().getNumFluxCapacitors();
			int vents = member.getVariant().getNumFluxVents();
			
			data.member = member;
			data.maxVents = maxVentsPost;
			data.maxCaps = maxCapsPost;
			
			boolean add = false;
			if (maxVentsPre > maxVentsPost && vents > maxVentsPost) {
				add = true;
			}
			if (maxCapsPre > maxCapsPost && caps > maxCapsPost) {
				add = true;
			}
			if (add) {
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
		
		VentsCapsDataMap map = getEffects(from, to);
		setMap(map, dataMap);
		
		info.addSectionHeading("Excess vents and capacitors", base, dark, Alignment.MID, 15f);
		
		info.addPara("Ships that have more than the new maximum number of flux vents "
				+ "and capacitors will have the excess vents and capacitors removed.", 10f,
				Misc.getNegativeHighlightColor(),
				"excess vents and capacitors removed"
				);
	}
	
	@Override
	public void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap) {
	}
	
	@Override
	public void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap) {
		VentsCapsDataMap map = getMap(dataMap);
		
		for (VentsCapsEffectData data : map.map.values()) {
			clampNumVentsAndCaps(data.member, to);
		}
	}
	
	public static void clampNumVentsAndCaps(FleetMemberAPI member, MutableCharacterStatsAPI stats) {
		int maxVents = getMaxVents(member.getHullSpec().getHullSize(), stats);
		int maxCaps = getMaxCaps(member.getHullSpec().getHullSize(), stats);
		ShipVariantAPI variant = member.getVariant();
		if (variant.getNumFluxVents() > maxVents || variant.getNumFluxCapacitors() > maxCaps) {
			variant = variant.clone();
			variant.setSource(VariantSource.REFIT);
			variant.setNumFluxVents(Math.min(variant.getNumFluxVents(), maxVents));
			variant.setNumFluxCapacitors(Math.min(variant.getNumFluxCapacitors(), maxCaps));
			member.setVariant(variant, false, false);
		}
	}
	
}







