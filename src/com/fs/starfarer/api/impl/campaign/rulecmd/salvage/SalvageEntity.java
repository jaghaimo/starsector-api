package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.RepairGantry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DropGroupRow;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class SalvageEntity extends BaseCommandPlugin {
	
	public static float SALVAGE_DETECTION_MOD_FLAT = 1000;
	
	public static int FIELD_RADIUS_FOR_BASE_REQ = 200;
	public static int FIELD_RADIUS_FOR_MAX_REQ = 1000;
	public static int FIELD_RADIUS_MAX_REQ_MULT = 10;
	public static float FIELD_MIN_SALVAGE_MULT = 0.01f;
	
	
	
	//public static float FIELD_SALVAGE_FRACTION_PER_ATTEMPT = 0.5f;
	public static float FIELD_SALVAGE_FRACTION_PER_ATTEMPT = 1f;
	
	public static float FIELD_CONTENT_MULTIPLIER_AFTER_SALVAGE = 0.25f;
	//public static float FIELD_CONTENT_MULTIPLIER_AFTER_DEMOLITION = 0.65f;
	public static float FIELD_CONTENT_MULTIPLIER_AFTER_DEMOLITION = 1f;
	
	public static int BASE_MACHINERY = 10;
	public static int BASE_CREW = 30;
	public static int MIN_MACHINERY = 5;
	
	public static float COST_HEIGHT = 67;
	
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected SalvageEntityGenDataSpec spec;
	protected CargoAPI cargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	private DebrisFieldTerrainPlugin debris;
	private Map<String, MemoryAPI> memoryMap;

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		
		String specId = entity.getCustomEntityType();
		if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
			specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
		}
		spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		cargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		Object test = entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_DEBRIS_FIELD);
		if (test instanceof DebrisFieldTerrainPlugin) {
			debris = (DebrisFieldTerrainPlugin) test;
		}
		
		if (command.equals("showCost")) {
			if (debris == null) {
				showCost();
			} else {
				//showCost();
				showCostDebrisField();
			}
		} else if (command.equals("performSalvage")) {
			performSalvage();
		} else if (command.equals("descDebris")) {
			showDebrisDescription();
		} else if (command.equals("checkAccidents")) {
			checkAccidents();
		} else if (command.equals("demolish")) {
			demolish();
		} else if (command.equals("canBeMadeRecoverable")) {
			return canBeMadeRecoverable();
		} else if (command.equals("showRecoverable")) {
			showRecoverable();
		}
		
		return true;
	}

	private void demolish() {
		boolean isDebrisField = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (!isDebrisField) {
			convertToDebrisField(FIELD_CONTENT_MULTIPLIER_AFTER_DEMOLITION);
			
			Global.getSoundPlayer().playSound("hit_heavy", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			
			dialog.dismiss();
			
//			text.addParagraph("Salvage crews set targeting beacons at key points in the structure, " +
//					"and you give the order to fire once everyone is safely off.");
//			text.addParagraph("Salvage crews set targeting beacons at key points in the structure.");			
//			options.clearOptions();
//			options.addOption("Leave", "defaultLeave");
//			options.setShortcut("defaultLeave", Keyboard.KEY_ESCAPE, false, false, false, true);
		}
	}

	private float getAccidentProbability() {
		if (debris == null) return 0f;
		float accidentProbability = 0.2f + 0.8f * (1f - debris.getParams().density);
		if (accidentProbability > 0.9f) accidentProbability = 0.9f;
		return accidentProbability;
	}
	
	private void checkAccidents() {
		if (debris == null) {
			memory.set("$option", "salPerform");
			FireBest.fire(null, dialog, memoryMap, "DialogOptionSelected");
			return;
		}
		
		float accidentProbability = getAccidentProbability();
		//accidentProbability = 1f;
		
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 175);
		
		if (random.nextFloat() > accidentProbability) {
			memory.set("$option", "salPerform");
			FireBest.fire(null, dialog, memoryMap, "DialogOptionSelected");
			return;
		}
		
		Color color = playerFaction.getColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color highlight = Misc.getHighlightColor();
		
		Map<String, Integer> requiredRes = computeRequiredToSalvage(entity);
		float reqCrew = (int) requiredRes.get(Commodities.CREW);
		float reqMachinery = (int) requiredRes.get(Commodities.HEAVY_MACHINERY);

		float crew = playerFleet.getCargo().getCrew();
		float machinery = playerFleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
		float fCrew = crew / reqCrew;
		if (fCrew < 0) fCrew = 0;
		if (fCrew > 1) fCrew = 1;
		
		float fMachinery = machinery / reqMachinery;
		if (fMachinery < 0) fMachinery = 0;
		if (fMachinery > 1) fMachinery = 1;
		
		
//		CommoditySpecAPI crewSpec = Global.getSector().getEconomy().getCommoditySpec(Commodities.CREW);
//		CommoditySpecAPI machinerySpec = Global.getSector().getEconomy().getCommoditySpec(Commodities.HEAVY_MACHINERY);
		
		float lossValue = reqCrew * fCrew * 5f;
		lossValue += (1f - debris.getParams().density / debris.getParams().baseDensity) * 500f;
		lossValue *= 0.5f + random.nextFloat();
		//lossValue *= StarSystemGenerator.getNormalRandom(random, 0.5f, 1.5f);
		
		WeightedRandomPicker<String> lossPicker = new WeightedRandomPicker<String>(random);
		lossPicker.add(Commodities.CREW, 10f + 100f * (1f - fMachinery));
		lossPicker.add(Commodities.HEAVY_MACHINERY, 10f + 100f * fMachinery);
		
		CargoAPI losses = Global.getFactory().createCargo(true);
		float loss = 0;
		while (loss < lossValue) {
			String id = lossPicker.pick();
			CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(id);
			loss += spec.getBasePrice();
			losses.addCommodity(id, 1f);
		}
		losses.sort();
		
		int crewLost = losses.getCrew();
		if (crewLost > 0) {
			losses.removeCrew(crewLost);
			crewLost *= playerFleet.getStats().getDynamic().getValue(Stats.NON_COMBAT_CREW_LOSS_MULT);
			if (crewLost < 1) crewLost = 1;
			losses.addCrew(crewLost);
		}
		
		int machineryLost = (int) losses.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
		if (crewLost > crew) crewLost = (int) crew;
		if (machineryLost > machinery) machineryLost = (int) machinery;
		
		if (crewLost <= 0 && machineryLost <= 0) {
			memory.set("$option", "salPerform");
			FireBest.fire(null, dialog, memoryMap, "DialogOptionSelected");
		}
		
		
		for (CargoStackAPI stack : losses.getStacksCopy()) {
			cargo.removeCommodity(stack.getCommodityId(), stack.getSize());
		}
		
		
		
		text.setFontInsignia();
		text.addParagraph("An accident during the operation has resulted in the loss of ");
		
		if (crewLost <= 0) {
			text.appendToLastParagraph("" + machineryLost + " heavy machinery.");
			text.highlightInLastPara(highlight, "" + machineryLost);
		} else if (machineryLost <= 0) {
			text.appendToLastParagraph("" + crewLost + " crew.");
			text.highlightInLastPara(highlight, "" + crewLost);
		} else {
			text.appendToLastParagraph("" + crewLost + " crew and " + machineryLost + " heavy machinery.");
			text.highlightInLastPara(highlight, "" + crewLost, "" + machineryLost);
		}
		
		
		Global.getSoundPlayer().playSound("hit_solid", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
		
		options.clearOptions();
		options.addOption("Continue", "salPerform");
		//FireBest.fire(null, dialog, memoryMap, "PerformSalvage");
		//FireBest.fire(null, dialog, memoryMap, "PerformSalvage");
	}

	private void showDebrisDescription() {
		if (debris == null) return;

		float daysLeft = debris.getDaysLeft();
		if (daysLeft >= 1000) {
			text.addParagraph("The field appears stable and will not drift apart any time soon.");
		} else {
			String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
			text.addParagraph("The field is unstable, but should not drift apart for " + atLeastTime + ".");
		}
		
//		boolean stillHot = debris.getGlowDaysLeft() > 0;
//		switch (debris.getParams().source) {
//		case BATTLE:
//			text.addParagraph("Pieces of ships, weapons, and escape pods litter the starscape.");
//			break;
//		case MIXED:
//			text.addParagraph("Pieces of ships, weapons, and escape pods litter the starscape.");
//			break;
//		case PLAYER_SALVAGE:
//			break;
//		case SALVAGE:
//			break;
//		}
		
//		if (stillHot) {
//			text.appendToLastParagraph(" Some of the pieces of debris are still radiating heat, making any salvage operations more dangerous.");
//		}
		
		float lootValue = 0;
		for (DropData data : debris.getEntity().getDropValue()) {
			lootValue += data.value;
		}
		for (DropData data : debris.getEntity().getDropRandom()) {
			if (data.value > 0) {
				lootValue += data.value;
			} else {
				lootValue += 500; // close enough
			}
		}
		float d = debris.getParams().density;
		
		lootValue *= d;
		
		// doesn't work because "extra" expires
//		ExtraSalvage extra = BaseSalvageSpecial.getExtraSalvage(memoryMap);
//		if (extra != null) {
//			for (CargoStackAPI stack : extra.cargo.getStacksCopy()) {
//				lootValue += stack.getBaseValuePerUnit() * stack.getSize();
//			}
//		}
		
		if (lootValue < 500) {
			text.appendToLastParagraph(" Long-range scans indicate it's unlikely anything of much value would be found inside.");
			text.highlightLastInLastPara("unlikely", Misc.getNegativeHighlightColor());
		} else if (lootValue < 2500) {
			text.appendToLastParagraph(" Long-range scans indicate it's possible something of value could be found inside.");
			text.highlightLastInLastPara("possible", Misc.getHighlightColor());
		} else {
			text.appendToLastParagraph(" Long-range scans indicate it's likely something of value could be found inside.");
			text.highlightLastInLastPara("likely", Misc.getPositiveHighlightColor());
		}
		
		float accidentProbability = getAccidentProbability();
		if (accidentProbability <= 0.2f) {
			//text.addParagraph("There are indications of some easy pickings to be had, and the risk of an accident during a salvage operation is low.");
			text.addPara("There are indications of some easy pickings to be had, and the risk of an accident during a salvage operation is low.",
					Misc.getPositiveHighlightColor(), "low");
		} else if (accidentProbability < 0.7f) {
			text.addPara("There are indications that what salvage is to be had may not be easy to get to, " +
						 "and there's %s risk involved in running a salvage operation.", Misc.getHighlightColor(), "significant");
		} else {
			text.addPara("The salvage that remains is extremely difficult to get to, " +
					 	 "and there's %s risk involved in running a salvage operation.", Misc.getNegativeHighlightColor(), "high");
		}
	}

	public static Map<String, Integer> computeRequiredToSalvage(SectorEntityToken entity) {
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		
		String specId = entity.getCustomEntityType();
		if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
			specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
		}
		SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);
		float mult = 1f + spec.getSalvageRating() * 9f;
		
		Object test = entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_DEBRIS_FIELD);
		if (test instanceof DebrisFieldTerrainPlugin) {
			DebrisFieldTerrainPlugin debris = (DebrisFieldTerrainPlugin) test;
			mult = getDebrisReqMult(debris);
		}
		
		int crew = Math.round((int) (BASE_CREW * mult) / 10f) * 10;
		int machinery = Math.round((int) (BASE_MACHINERY * mult) / 10f) * 10;
		
		result.put(Commodities.CREW, crew);
		result.put(Commodities.HEAVY_MACHINERY, machinery);
		
		return result;
	}
	
	protected MutableStat getValueRecoveryStat(boolean withSkillMultForRares) {
		Map<String, Integer> requiredRes = computeRequiredToSalvage(entity);
		MutableStat valueRecovery = new MutableStat(1f);
		int i = 0;
		
		float machineryContrib = 0.75f;
		valueRecovery.modifyPercent("base", -100f);
		if (machineryContrib < 1f) {
			valueRecovery.modifyPercent("base_positive", (int) Math.round(100f - 100f * machineryContrib), "Base effectiveness");
		}
		//valueRecovery.modifyPercent("base", -75f);
		
		float per = 0.5f;
		per = 1f;
		for (String commodityId : requiredRes.keySet()) {
			float required = requiredRes.get(commodityId);
			float available = (int) cargo.getCommodityQuantity(commodityId);
			if (required <= 0) continue;
			CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(commodityId);
			
			float val = Math.min(available / required, 1f) * per;
			int percent = (int) Math.round(val * 100f);
			//valueRecovery.modifyPercent("" + i++, percent, Misc.ucFirst(spec.getLowerCaseName()) + " requirements met");
			if (Commodities.HEAVY_MACHINERY.equals(commodityId)) {
				val = Math.min(available / required, machineryContrib) * per;
				percent = (int) Math.round(val * 100f);
				valueRecovery.modifyPercentAlways("" + i++, percent, Misc.ucFirst(spec.getLowerCaseName()) + " available");
			} else {
				valueRecovery.modifyMultAlways("" + i++, val, Misc.ucFirst(spec.getLowerCaseName()) + " available");
			}
//			float val = Math.max(1f - available / required, 0f) * per;
//			int percent = -1 * (int) Math.round(val * 100f);
//			valueRecovery.modifyPercent("" + i++, percent, "Insufficient " + spec.getLowerCaseName());
		}
		
		boolean modified = false;
		if (withSkillMultForRares) {
			for (StatMod mod : playerFleet.getStats().getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).getFlatMods().values()) {
				modified = true;
				valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(mod.value * 100f), mod.desc);
			}
		}
		
		{
			for (StatMod mod : playerFleet.getStats().getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).getFlatMods().values()) {
				modified = true;
				valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(mod.value * 100f), mod.desc);
			}
		}
		if (!modified) {
			valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(0f), "Salvaging skill");
		}
		
		float fleetSalvageShips = getPlayerShipsSalvageModUncapped();
		valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(fleetSalvageShips * 100f), "Fleetwide salvaging capability");
		
		return valueRecovery;
	}
	
//	protected StatBonus getRareRecoveryStat() {
//		StatBonus rareRecovery = new StatBonus();
//		int i = 0;
//		for (StatMod mod : playerFleet.getStats().getDynamic().getMod(Stats.SALVAGE_MAX_RATING).getFlatBonuses().values()) {
//			rareRecovery.modifyPercent("" + i++, (int) Math.round(mod.value * 100f), mod.desc);
//		}
//		return rareRecovery;
//	}
	
	public void showCost() {
		Color color = playerFaction.getColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color highlight = Misc.getHighlightColor();
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		
		Map<String, Integer> requiredRes = computeRequiredToSalvage(entity);
		
		text.addParagraph("You receive a preliminary assessment of a potential salvage operation from the exploration crews.");
		
		ResourceCostPanelAPI cost = text.addCostPanel("Crew & machinery: required (available)", COST_HEIGHT,
				color, playerFaction.getDarkUIColor());
		cost.setNumberOnlyMode(true);
		cost.setWithBorder(false);
		cost.setAlignment(Alignment.LMID);
		
		for (String commodityId : requiredRes.keySet()) {
			int required = requiredRes.get(commodityId);
			int available = (int) cargo.getCommodityQuantity(commodityId);
			Color curr = color;
			if (required > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				curr = bad;
			}
			cost.addCost(commodityId, "" + required + " (" + available + ")", curr);
		}
		cost.update();
		
		
		MutableStat valueRecovery = getValueRecoveryStat(true);
		
		//rareRecovery.unmodify();
		int valuePercent = (int)Math.round(valueRecovery.getModifiedValue() * 100f);
		if (valuePercent < 0) valuePercent = 0;
		String valueString = "" + valuePercent + "%";
		Color valueColor = highlight;

		if (valuePercent < 100) {
			valueColor = bad;
		}
		
		TooltipMakerAPI info = text.beginTooltip();
		info.setParaSmallInsignia();
		info.addPara("Resource recovery effectiveness: %s", 0f, valueColor, valueString);
		if (!valueRecovery.isUnmodified()) {
			info.addStatModGrid(300, 50, opad, small, valueRecovery, true, getModPrinter());
		}
		text.addTooltip();
		
		printSalvageModifiers();
	}
	
	protected StatModValueGetter getModPrinter() {
		return new StatModValueGetter() {
			boolean percent = false;
			public String getPercentValue(StatMod mod) {
				percent = true;
				
				 // should make it not shown; it's a "base" value that has to be applied to make the calculations work with multipliers
				if (mod.desc == null || mod.desc.isEmpty()) return "";
				
				String prefix = mod.getValue() >= 0 ? "+" : "";
				return prefix + (int)(mod.getValue()) + "%";
			}
			public String getMultValue(StatMod mod) {percent = false; return null;}
			public String getFlatValue(StatMod mod) {percent = false; return null;}
			public Color getModColor(StatMod mod) {
				if ((!percent && mod.getValue() < 1f) || mod.getValue() < 0) return Misc.getNegativeHighlightColor();
				return null;
			}
		};
	}
	
	protected void printSalvageModifiers() {
		
		float fuelMult = playerFleet.getStats().getDynamic().getValue(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET);
		String fuelStr = "" + (int)Math.round((fuelMult - 1f) * 100f) + "%";
		
		float rareMult = playerFleet.getStats().getDynamic().getValue(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE);
		String rareStr = "" + (int)Math.round((rareMult - 1f) * 100f) + "%";
		
		if (fuelMult > 1f && rareMult > 1f) {
			text.addPara("Your fleet also has a %s bonus to the amount of fuel recovered, and " +
					"a %s bonus to the number of rare items found.",
					 Misc.getHighlightColor(), fuelStr, rareStr);
		} else if (fuelMult > 1) {
			text.addPara("Your fleet also has a %s bonus to the amount of fuel recovered.",
					 Misc.getHighlightColor(), fuelStr);
		} else if (rareMult > 1) {
			text.addPara("Your fleet also has a %s bonus to the number of rare items found.",
					 Misc.getHighlightColor(), rareStr);
		}
		
		if (debris != null) {
			text.addParagraph("The density of the debris field affects both the amount resources and the number of rare items found.");
		} else {
			text.addPara("The recovery effectiveness does not affect the chance of finding rare and valuable items.");
		}

	}
	
	public void showCostDebrisField() {
		Color color = playerFaction.getColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color highlight = Misc.getHighlightColor();
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		
		Map<String, Integer> requiredRes = computeRequiredToSalvage(entity);
		
		//text.addParagraph("You receive a preliminary assessment of a potential salvage operation from the exploration crews.");
		
		ResourceCostPanelAPI cost = text.addCostPanel("Crew & machinery: required (available)", COST_HEIGHT,
				color, playerFaction.getDarkUIColor());
		cost.setNumberOnlyMode(true);
		cost.setWithBorder(false);
		cost.setAlignment(Alignment.LMID);
		
		for (String commodityId : requiredRes.keySet()) {
			int required = requiredRes.get(commodityId);
			int available = (int) cargo.getCommodityQuantity(commodityId);
			Color curr = color;
			if (required > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				curr = bad;
			}
			cost.addCost(commodityId, "" + required + " (" + available + ")", curr);
		}
		cost.update();
		
		
		MutableStat valueRecovery = getValueRecoveryStat(true);
		float overallMult = computeOverallMultForDebrisField();
		valueRecovery.modifyMult("debris_mult", overallMult, "Debris field density");
		//rareRecovery.unmodify();
		int valuePercent = (int)Math.round(valueRecovery.getModifiedValue() * 100f);
		if (valuePercent < 0) valuePercent = 0;
		String valueString = "" + valuePercent + "%";
		Color valueColor = highlight;

		if (valuePercent < 100) {
			valueColor = bad;
		}
		
		TooltipMakerAPI info = text.beginTooltip();
		info.setParaSmallInsignia();
		info.addPara("Scavenging effectiveness: %s", 0f, valueColor, valueString);
		if (!valueRecovery.isUnmodified()) {
			info.addStatModGrid(300, 50, opad, small, valueRecovery, true, getModPrinter());
		}
		text.addTooltip();
		
//		text.addParagraph("The density of the debris field affects both the amount resources and the number of rare items found.");
		
//		text.addParagraph("It's possible to scavenge using fewer crew and less machinery than required, but using fewer crew will reduce " + 
//						  "the amount of salvage recovered, while having less machinery will increase the danger to crew.");
		
		printSalvageModifiers();
		
	}
	
	protected float computeOverallMultForDebrisField() {
		float overallMult = 1f;
		if (debris != null) {
//			Map<String, Integer> reqs = computeRequiredToSalvage(entity);
//			float crewMax = 1f;
//			if (reqs.get(Commodities.CREW) != null) {
//				crewMax = reqs.get(Commodities.CREW);
//			}
//			float crew = playerFleet.getCargo().getCrew();
//			float f = crew / crewMax;
//			if (f < 0) f = 0;
//			if (f > 1) f = 1;
//			
//			//if (Global.getSettings().isDevMode()) f = 1f;
			
			float f = 1f;
			DebrisFieldParams params = debris.getParams();
			if (params.baseDensity > 0) {
				overallMult = params.density / params.baseDensity * f * FIELD_SALVAGE_FRACTION_PER_ATTEMPT;
			} else {
				overallMult = 0f;
			}
			if (overallMult < FIELD_MIN_SALVAGE_MULT) overallMult = FIELD_MIN_SALVAGE_MULT;
		}
		return overallMult;
	}
	
	
	public void performSalvage() {
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);
		
		Misc.stopPlayerFleet();
		
//		if (Global.getSettings().isDevMode()) {
//			random = Misc.random;
//		}
		
//		float salvageRating = spec.getSalvageRating();
//		float valueMultFleet = playerFleet.getStats().getDynamic().getValue(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE);
//		float valueModShips = getPlayerShipsSalvageMod(salvageRating);
		
		MutableStat valueRecovery = getValueRecoveryStat(true);
		float valueMultFleet = valueRecovery.getModifiedValue();
		float rareItemSkillMult = playerFleet.getStats().getDynamic().getValue(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE);

		List<DropData> dropValue = new ArrayList<DropData>(spec.getDropValue());
		List<DropData> dropRandom = new ArrayList<DropData>(spec.getDropRandom());
		dropValue.addAll(entity.getDropValue());
		dropRandom.addAll(entity.getDropRandom());
		
//		DropData d = new DropData();
//		d.group = "misc_test";
//		d.chances = 1500;
//		dropRandom.add(d);
		
		
		float overallMult = computeOverallMultForDebrisField();
		if (debris != null) {
			// to avoid same special triggering over and over while scavenging through
			// the same debris field repeatedly
			BaseCommandPlugin.getEntityMemory(memoryMap).unset(MemFlags.SALVAGE_SPECIAL_DATA);
		}
		
		float fuelMult = playerFleet.getStats().getDynamic().getValue(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET);
		CargoAPI salvage = generateSalvage(random, valueMultFleet, rareItemSkillMult, overallMult, fuelMult, dropValue, dropRandom);
		
		//ExtraSalvage extra = BaseSalvageSpecial.getExtraSalvage(memoryMap);
		CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(memoryMap);
		salvage.addAll(extra);
		BaseSalvageSpecial.clearExtraSalvage(memoryMap);
		if (!extra.isEmpty()) {
			ListenerUtil.reportExtraSalvageShown(entity);
		}
		
		//salvage.addCommodity(Commodities.ALPHA_CORE, 1);
		
		if (debris != null) {
			debris.getParams().density -= overallMult;
			if (debris.getParams().density < 0) debris.getParams().density = 0;
			
			debris.getEntity().getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, random.nextLong());
			//System.out.println("Post-salvage density: " + debris.getParams().density);
			debris.setScavenged(true);
		}
		
		//if (loot)
		if (!salvage.isEmpty()) {
			dialog.getVisualPanel().showLoot("Salvaged", salvage, false, true, true, new CoreInteractionListener() {
				public void coreUIDismissed() {
					long xp = 0;
					if (entity.hasSalvageXP()) {
						xp = (long) (float) entity.getSalvageXP();
					} else if (spec != null && spec.getXpSalvage() > 0) {
						xp = (long) spec.getXpSalvage();
					}
					if (!memory.contains("$doNotDismissDialogAfterSalvage")) {
						dialog.dismiss();
						dialog.hideTextPanel();
						dialog.hideVisualPanel();
						
						if (xp > 0) {
							Global.getSector().getPlayerPerson().getStats().addXP(xp);
						}
					} else {
						if (xp > 0) {
							Global.getSector().getPlayerPerson().getStats().addXP(xp, dialog.getTextPanel());
						}
					}
//					if (entity.hasSalvageXP()) {
//						Global.getSector().getPlayerPerson().getStats().addXP((long) (float) entity.getSalvageXP());
//					} else if (spec != null && spec.getXpSalvage() > 0) {
//						Global.getSector().getPlayerPerson().getStats().addXP((long) spec.getXpSalvage());
//					}
					//Global.getSector().setPaused(false);
				}
			});
			options.clearOptions();
			dialog.setPromptText("");
		} else {
			text.addParagraph("Operations conclude with nothing of value found.");
			options.clearOptions();
			String leave = "Leave";
			if (memory.contains("$salvageLeaveText")) {
				leave = memory.getString("$salvageLeaveText");
			}
			options.addOption(leave, "defaultLeave");
			options.setShortcut("defaultLeave", Keyboard.KEY_ESCAPE, false, false, false, true);
		}
		

		boolean isDebrisField = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (!isDebrisField) {
			if (!spec.hasTag(Tags.SALVAGE_ENTITY_NO_DEBRIS)) {
				convertToDebrisField(FIELD_CONTENT_MULTIPLIER_AFTER_SALVAGE);
			} else {
				if (!spec.hasTag(Tags.SALVAGE_ENTITY_NO_REMOVE)) { 
					Misc.fadeAndExpire(entity, 1f);
				}
			}
		}
		
		if (playerFleet != null) {
			playerFleet.getStats().addTemporaryModFlat(0.25f, "salvage_ops", 
					"Recent salvage operation", SALVAGE_DETECTION_MOD_FLAT,
					playerFleet.getStats().getDetectedRangeMod());
			Global.getSector().addPing(playerFleet, "noticed_player");
		}
	}
	
	
	public void convertToDebrisField(float valueMult) {
		convertToDebrisField(null, valueMult);
	}
	
	public void convertToDebrisField(Random random, float valueMult) {
		if (random == null) random = new Random();
		
		Misc.fadeAndExpire(entity, 1f);
		
		float salvageRating = spec.getSalvageRating();
		//entity.addTag(Tags.NON_CLICKABLE);
		
		float debrisFieldRadius = 200f + salvageRating * 400f;
		
		float density = 0.5f + salvageRating * 0.5f;
		density = 1f;
		if (valueMult <= FIELD_CONTENT_MULTIPLIER_AFTER_SALVAGE) {
			density = 0.5f + salvageRating * 0.5f;
		}
		
		float duration = 10f + salvageRating * 20f;
		
		DebrisFieldParams params = new DebrisFieldParams(debrisFieldRadius, density, duration, duration * 0.5f);
		params.source = DebrisFieldSource.PLAYER_SALVAGE;
		
//		params.minSize = 12;
//		params.maxSize = 16;
//		params.defenderProb = 1;
//		params.minStr = 20;
//		params.maxStr = 30;
//		params.maxDefenderSize = 1;
		
		float xp = spec.getXpSalvage() * 0.25f;
		if (entity.hasSalvageXP()) {
			xp = entity.getSalvageXP() * 0.25f;
		}
		if (xp >= 10) {
			params.baseSalvageXP = (long) xp;
		}
		
		SectorEntityToken debris = Misc.addDebrisField(entity.getContainingLocation(), params, null);
		
		//ExtraSalvage extra = BaseSalvageSpecial.getExtraSalvage(memoryMap);
		CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(memoryMap);
		if (extra != null && !extra.isEmpty()) {
			// don't prune extra cargo - it could have come from not recovering ships,
			// and so could've been gotten by recovering and then stripping/scuttling them
			// so shouldn't punish shortcutting that process
			// (this can happen when "pound into scrap" vs ship derelict)
//			CargoAPI extraCopy = Global.getFactory().createCargo(true);
//			for (CargoStackAPI stack : extra.cargo.getStacksCopy()) {
//				float qty = stack.getSize();
//				qty *= valueMult;
//				if (qty < 1) {
//					if (random.nextFloat() >= qty) continue;
//					qty = 1;
//				} else {
//					qty = (int) qty;
//				}
//				extraCopy.addItems(stack.getType(), stack.getData(), qty);
//			}
//			BaseSalvageSpecial.setExtraSalvage(extraCopy, debris.getMemoryWithoutUpdate(), -1f);
			//BaseSalvageSpecial.addExtraSalvage(extra.cargo, debris.getMemoryWithoutUpdate(), -1f);
			BaseSalvageSpecial.addExtraSalvage(extra, debris.getMemoryWithoutUpdate(), -1f);
		}
		
//		int count = 0;
//		for (CampaignTerrainAPI curr : entity.getContainingLocation().getTerrainCopy()) {
//			if (curr.getPlugin() instanceof DebrisFieldTerrainPlugin) {
//				count++;
//			}
//		}
		//System.out.println("DEBRIS: " + count);
		
		debris.setSensorProfile(null);
		debris.setDiscoverable(null);
		//debris.setDiscoveryXP(123f);
		
		debris.setFaction(entity.getFaction().getId());
		
		debris.getDropValue().clear();
		debris.getDropRandom().clear();
		
		for (DropData data : spec.getDropValue()) {
			DropData copy = data.clone();
			copy.valueMult = valueMult;
			debris.addDropValue(data.clone());
		}
		for (DropData data : spec.getDropRandom()) {
			DropData copy = data.clone();
			copy.valueMult = valueMult;
			debris.addDropRandom(copy);
		}
		
		for (DropData data : entity.getDropValue()) {
			DropData copy = data.clone();
			copy.valueMult = valueMult;
			debris.addDropValue(data.clone());
		}
		for (DropData data : entity.getDropRandom()) {
			DropData copy = data.clone();
			copy.valueMult = valueMult;
			debris.addDropRandom(copy);
		}
		//debris.addDropRandom("weapons_test", 10);
		
		if (entity.getOrbit() != null) {
			debris.setOrbit(entity.getOrbit().makeCopy());
		} else {
			debris.getLocation().set(entity.getLocation());
		}
		
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		if (seed != 0) {
			debris.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, Misc.getRandom(seed, 150).nextLong());
		}
	}
	
	
	
	
	public static float getPlayerShipsSalvageModUncapped() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		//float valueModShips = Misc.getFleetwideTotalMod(playerFleet, Stats.SALVAGE_VALUE_MULT_MOD, 0f);
		float valueModShips = RepairGantry.getAdjustedGantryModifier(playerFleet, null, 0);
		return valueModShips;
	}
//	public static float getPlayerShipsSalvageMod(float salvageRating) {
//		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//		float valueModShips = Misc.getFleetwideTotalMod(playerFleet, Stats.SALVAGE_VALUE_MULT_MOD, 0f);
//		if (valueModShips > salvageRating) {
//			valueModShips = salvageRating;
//		}
//		return valueModShips;
//	}
	
	public static float getDebrisReqMult(DebrisFieldTerrainPlugin field) {
//		public static int FIELD_RADIUS_FOR_BASE_REQ = 200;
//		public static int FIELD_RADIUS_FOR_MAX_REQ = 1000;
//		public static int FIELD_RADIUS_MAX_REQ_MULT = 10;
		float r = field.getParams().bandWidthInEngine;
		float f = (r - FIELD_RADIUS_FOR_BASE_REQ) / (FIELD_RADIUS_FOR_MAX_REQ - FIELD_RADIUS_FOR_BASE_REQ);
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		
		float mult = 1f + (FIELD_RADIUS_MAX_REQ_MULT - 1f) * f;
		return mult;
	}
	
//	public static CargoAPI generateSalvage(Random random, float valueMult, List<DropData> dropValue, List<DropData> dropRandom) {
//		return generateSalvage(random, valueMult, 1f, dropValue, dropRandom);
//	}
	public static CargoAPI generateSalvage(Random random, float valueMult, float overallMult, float fuelMult, List<DropData> dropValue, List<DropData> dropRandom) {
		return generateSalvage(random, valueMult, 1f, overallMult, fuelMult, dropValue, dropRandom);
	}
	public static CargoAPI generateSalvage(Random random, float valueMult, float randomMult, 
			float overallMult, float fuelMult, List<DropData> dropValue, List<DropData> dropRandom) {
		if (random == null) random = new Random();
		CargoAPI result = Global.getFactory().createCargo(true);
		
		
		if (Misc.isEasy()) {
			overallMult *= Global.getSettings().getFloat("easySalvageMult");
		}
//		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		//overallMult = 1f;
		
//		float valueMultFleet = playerFleet.getStats().getDynamic().getValue(Stats.SALVAGE_VALUE_MULT_FLEET);
//		float valueModShips = getPlayerShipsSalvageMod(salvageRating);
		
		// check dropRandom first so that changing the drop value by dropping off crew/machinery
		// does not change the RNG for dropRandom
		if (dropRandom != null) {
			for (DropData data : dropRandom) {
				//if (random.nextFloat() < data.valueMult) continue;
				
				int chances = data.chances;
				if (data.maxChances > chances) {
					chances = chances + random.nextInt(data.maxChances - chances + 1);
				}
//				if (data.group.endsWith("misc_test")) {
//					System.out.println("fewfwefwe");
//				}
				//WeightedRandomPicker<DropGroupRow> picker = DropGroupRow.getPicker(data.group);
				
				float modifiedChances = chances;
				modifiedChances *= overallMult;
				if (data.value <= 0) {
					modifiedChances *= randomMult;
				}
				modifiedChances *= data.valueMult;
				float rem = modifiedChances - (int) modifiedChances;
				
				chances = (int) modifiedChances + (random.nextFloat() < rem ? 1 : 0);
				
				WeightedRandomPicker<DropGroupRow> picker = data.getCustom();
				if (picker == null && data.group == null) continue; // meant for custom, but empty
				if (picker == null) {
					picker = DropGroupRow.getPicker(data.group);
				}
				
				Random innerRandom = Misc.getRandom(random.nextLong(), 5);
				//innerRandom = random;
				picker.setRandom(innerRandom);
				for (int i = 0; i < chances; i++) {
//					if (random.nextFloat() > overallMult) continue;
//					if (random.nextFloat() > data.valueMult) continue;
					
					DropGroupRow row = picker.pick();
					if (row.isMultiValued()) {
						row = row.resolveToSpecificItem(innerRandom);
					}
					
					if (row.isNothing()) continue;
					
					float baseUnitValue = row.getBaseUnitValue();
					
					float qty = 1f;
					if (data.value > 0) {
						float randMult = StarSystemGenerator.getNormalRandom(innerRandom, 0.5f, 1.5f);
						//qty = (data.value * randMult * valueMult * overallMult) / baseUnitValue;
						// valueMult and overallMult are considered in figuring out number of chances to roll
						qty = (data.value * valueMult * randMult) / baseUnitValue;
						qty = (int) qty;
						if (valueMult <= 0) continue;
						if (qty < 1) qty = 1;
					}
					
					
					if (row.isWeapon()) {
						result.addWeapons(row.getWeaponId(), (int) qty);
//					} else if (row.isHullMod()) {
//						result.addItems(CargoItemType.MOD_SPEC, row.getHullModId(), (int) qty);
					} else if (row.isFighterWing()) {
						result.addItems(CargoItemType.FIGHTER_CHIP, row.getFighterWingId(), (int) qty);
					} else if (row.isSpecialItem()) { 
						if (Items.MODSPEC.equals(row.getSpecialItemId()) && 
								result.getQuantity(CargoItemType.SPECIAL, 
										new SpecialItemData(row.getSpecialItemId(), row.getSpecialItemData())) > 0) {
							continue;
						}
						result.addItems(CargoItemType.SPECIAL, 
								new SpecialItemData(row.getSpecialItemId(), row.getSpecialItemData()), (int) qty);
					} else {
						result.addCommodity(row.getCommodity(), qty);
					}
				}
			}
		}
		
		
		if (dropValue != null) {
			
			for (DropData data : dropValue) {
				//if (random.nextFloat() < data.valueMult) continue;
				
				float maxValue = data.value;
				
				// if value is 1, it's a "guaranteed pick one out of this usually-dropRandom group"
				// so still allow it even if valueMult is 0 due to a lack of heavy machinery
				// since dropRandom works w/ no machinery, too
				if (data.value > 1) {
					maxValue *= valueMult;
				}
				
				maxValue *= overallMult;
				maxValue *= data.valueMult;
				
				float randMult = StarSystemGenerator.getNormalRandom(random, 0.5f, 1.5f);
				maxValue *= randMult;
				
				
				WeightedRandomPicker<DropGroupRow> picker = data.getCustom();
				if (picker == null && data.group == null) continue; // meant for custom, but empty
				if (picker == null) {
					picker = DropGroupRow.getPicker(data.group);
				}
				picker.setRandom(random);
				float value = 0f;
				int nothingInARow = 0;
				while (value < maxValue && nothingInARow < 10) {
					DropGroupRow row = picker.pick();
					if (row.isMultiValued()) {
						row = row.resolveToSpecificItem(random);
					}
					if (row.isNothing()) {
						nothingInARow++;
						continue;
					} else {
						nothingInARow = 0;
					}
					//System.out.println(nothingInARow);
					
					float baseUnitValue = row.getBaseUnitValue();
					
					float qty = 1f;
					float currValue = baseUnitValue * qty;
					value += currValue;
					
					if (row.isWeapon()) {
						if (value <= maxValue) {
							result.addWeapons(row.getWeaponId(), (int) qty);
						}
//					} else if (row.isHullMod()) {
//						if (value <= maxValue) {
//							result.addHullmods(row.getHullModId(), (int) qty);
//						}
					} else if (row.isFighterWing()) {
						if (value <= maxValue) {
							result.addItems(CargoItemType.FIGHTER_CHIP, row.getFighterWingId(), (int) qty);
						}
					} else if (row.isSpecialItem()) {
						if (Items.MODSPEC.equals(row.getSpecialItemId()) && 
								result.getQuantity(CargoItemType.SPECIAL, 
										new SpecialItemData(row.getSpecialItemId(), row.getSpecialItemData())) > 0) {
							continue;
						}
						result.addItems(CargoItemType.SPECIAL, 
									new SpecialItemData(row.getSpecialItemId(), row.getSpecialItemData()), (int) qty);
					} else {
						if (value <= maxValue) {
							result.addCommodity(row.getCommodity(), qty);
						}
					}
				}
			}
		}
		
		
		float fuel = result.getFuel();
		if (fuelMult > 1f) {
			result.addFuel((int) Math.round(fuel * (fuelMult - 1f)));
		}
		
		result.sort();
		
		return result;
	}
	
	
	public boolean canBeMadeRecoverable() {
		if (entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
			
			//if (Misc.getSalvageSpecial(entity) != null) return false;
			
			if (Misc.getSalvageSpecial(entity) instanceof ShipRecoverySpecialData) {
				return false;
			}
			if (entity.hasTag(Tags.UNRECOVERABLE)) {
				return false;
			}
			
//			int room = Global.getSettings().getMaxShipsInFleet() - 
//			   		   Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size();
//			if (room < 1) return false;
			
			DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
			ShipVariantAPI variant = plugin.getData().ship.getVariant();
			if (variant != null && !Misc.isUnboardable(variant.getHullSpec())) {
				return true;
			}
		}
		return false;
	}

	
	public void showRecoverable() {
		
		Object prev = Misc.getSalvageSpecial(entity);
		if (prev != null) {
			Misc.setPrevSalvageSpecial(entity, prev);
		}
		
		ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
		DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
		data.addShip(plugin.getData().ship.clone());
		data.storyPointRecovery = true;
		Misc.setSalvageSpecial(entity, data);
		
		long seed = Misc.getSalvageSeed(entity);
		entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
	}
	
}







