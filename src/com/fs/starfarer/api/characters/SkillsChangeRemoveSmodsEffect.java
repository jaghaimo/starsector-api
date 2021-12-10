package com.fs.starfarer.api.characters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.plog.SModRecord;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

public class SkillsChangeRemoveSmodsEffect extends BaseSkillsChangeEffect {

	public static class SmodRemovalEffectData {
		FleetMemberAPI member;
		List<SModRecord> records = new ArrayList<SModRecord>();
		int maxBefore;
		int maxAfter;
		int numSmods;
		int remove;
		boolean offerChoice = false;
		List<String> removeList = new ArrayList<String>();
		
		List<ButtonAPI> buttons = new ArrayList<ButtonAPI>();
	}
	
	public static class SmodDataMap {
		Map<FleetMemberAPI, SmodRemovalEffectData> map = new LinkedHashMap<FleetMemberAPI, SmodRemovalEffectData>();
	}
	public void setMap(SmodDataMap map, Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		dataMap.put(key, map);
	}
	public SmodDataMap getMap(Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		SmodDataMap map = (SmodDataMap)dataMap.get(key);
		if (map == null) {
			map = new SmodDataMap();
			dataMap.put(key, map);
		}
		return map;
	}
	
	public SmodDataMap getEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		SmodDataMap result = new SmodDataMap();
		
		
		for (SModRecord record : PlaythroughLog.getInstance().getSModsInstalled()) {
			FleetMemberAPI member = record.getMember();
			if (member == null) continue;
			if (record.getSMods().isEmpty()) continue;
			String modId = record.getSMods().get(0);
			if (!member.getVariant().getSMods().contains(modId)) continue;
			if (!member.getVariant().getHullMods().contains(modId)) continue;
			
			SmodRemovalEffectData data = result.map.get(member);
			if (data == null) {
				data = new SmodRemovalEffectData();
				data.member = member;
				data.numSmods = member.getVariant().getSMods().size();
				data.maxBefore = Misc.getMaxPermanentMods(member, from);
				data.maxAfter = Misc.getMaxPermanentMods(member, to);
				result.map.put(member, data);
			}
			data.records.add(record);
		}
		
		for (FleetMemberAPI member : new ArrayList<FleetMemberAPI>(result.map.keySet())) {
			SmodRemovalEffectData data = result.map.get(member);
			if (data.maxBefore <= data.maxAfter || data.records.isEmpty() ||
					data.numSmods <= data.maxAfter) {
				result.map.remove(member);
			} else {
				data.remove = Math.min(data.numSmods - data.maxAfter, data.records.size());
				data.offerChoice = data.remove <= 1 && 
						Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(data.member);
				if (!data.offerChoice) {
					List<SModRecord> copy = new ArrayList<SModRecord>(data.records);
					for (int i = 0; i < data.remove; i++) {
						int minCost = 10000;
						SModRecord minCostMod = null;
						for (SModRecord record : data.records) {
							HullModSpecAPI mod = Global.getSettings().getHullModSpec(record.getSMods().get(0));
							int cost = mod.getCostFor(member.getHullSpec().getHullSize());
							if (cost < minCost) {
								minCostMod = record;
								minCost = cost;
							}
						}
						if (minCostMod != null) {
							data.removeList.add(minCostMod.getSMods().get(0));
							copy.remove(minCostMod);
						}
					}
				}
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
		
		SmodDataMap map = getEffects(from, to);
		setMap(map, dataMap);
		
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		float initPad = 15f;
		info.addSectionHeading("S-mods", base, dark, Alignment.MID, initPad);
		initPad = opad;
//		for (SmodRemovalEffectData data : map.map.values()) {
//			if (!data.offerChoice) {
				info.addPara("Ships that you've built s-mods into that now have more than their maximum"
						+ " number of s-mods will have the cheapest s-mods removed.", initPad, 
						Misc.getNegativeHighlightColor(),
						"cheapest s-mods removed");
				initPad = 15f;
				info.addPara("If only one s-mod is being removed, and the ship is currently in your fleet, "
						+ "you can select which s-mod to remove. S-mods you did not build into a ship will not be removed, even if the ship is over the limit.", opad);
//				break;
//			}
//		}
		
		for (SmodRemovalEffectData data : map.map.values()) {
			final FleetMemberAPI member = data.member;
			
			if (data.offerChoice) {
				String str = "The " + member.getShipName() +
				" (" + member.getHullSpec().getHullNameWithDashClass() + "), will lose its built-in...";
				info.addPara(str, initPad);
				
				float bw = 470;
				float bh = 25;
				float indent = 40f;
				UIComponentAPI prev = null;
				int minCost = 10000;
				ButtonAPI minCostButton = null;
				for (SModRecord record : data.records) {
					HullModSpecAPI mod = Global.getSettings().getHullModSpec(record.getSMods().get(0));
					float p = opad;
					if (prev != null) p = pad;
					int cost = mod.getCostFor(member.getHullSpec().getHullSize());
					ButtonAPI b = info.addAreaCheckbox(
							mod.getDisplayName() + " (" + cost + " OP)", new Object(), base, dark, bright, bw, bh, p, true);
					data.buttons.add(b);

					if (prev == null) {
						b.getPosition().setXAlignOffset(indent);
					}
					prev = b;
					if (cost < minCost) {
						minCostButton = b;
						minCost = cost;
					}
				}
				if (minCostButton != null) {
					minCostButton.setChecked(true);
				}
				
				info.addSpacer(0).getPosition().setXAlignOffset(-indent);
			}
		}
	}
	
	@Override
	public void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap) {
		SmodDataMap map = getMap(dataMap);
		
		for (SmodRemovalEffectData data : map.map.values()) {
			boolean found = false;
			for (ButtonAPI b : data.buttons) {
				if (b == button) {
					found = true;
					break;
				}
			}
			if (found) {
				for (ButtonAPI b : data.buttons) {
					if (b == button) {
						b.setChecked(true);
					} else {
						b.setChecked(false);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap) {
		SmodDataMap map = getMap(dataMap);
		
		for (SmodRemovalEffectData data : map.map.values()) {
			ShipVariantAPI variant = data.member.getVariant();
			variant = variant.clone();
			variant.setSource(VariantSource.REFIT);
			
			if (data.offerChoice) {
				int index = 0;
				for (ButtonAPI b : data.buttons) {
					if (b.isChecked()) {
						SModRecord record = data.records.get(index);
						data.removeList.add(record.getSMods().get(0));
					}
					index++;
				}
			}
			for (String modId : data.removeList) {
				variant.removePermaMod(modId);
			}
			data.member.setVariant(variant, false, false);
		}
	}
	
}







