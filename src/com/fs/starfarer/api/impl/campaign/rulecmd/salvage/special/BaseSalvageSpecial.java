package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.util.Misc;

public class BaseSalvageSpecial implements SalvageSpecialPlugin {

	public static final String EXTRA_SALVAGE = "$extraSpecialSalvage";
	public static final String TEMP_EXTRA_SALVAGE = "$tempExtraSpecialSalvage";
	public static class ExtraSalvage {
		public CargoAPI cargo;
		public ExtraSalvage(CargoAPI cargo) {
			this.cargo = cargo;
		}
	}
	
	protected boolean done = false;
	protected boolean endWithContinue = true;
	protected InteractionDialogAPI dialog;
	protected Random random;
	protected SectorEntityToken entity;
	
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	protected Map<String, MemoryAPI> memoryMap;
	
	protected CampaignFleetAPI playerFleet;
	
	protected void addTempExtraSalvage(CargoAPI cargo) {
		ExtraSalvage extra = getTempExtraSalvage(memoryMap);
		if (extra == null) {
			extra = new ExtraSalvage(cargo);
		} else {
			extra.cargo.addAll(cargo);
		}
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(dialog.getPlugin().getMemoryMap());
		memory.set(TEMP_EXTRA_SALVAGE, extra, 0f);
	}
	
	public static void addExtraSalvage(SectorEntityToken entity, CargoAPI cargo) {
		addExtraSalvage(cargo, entity.getMemoryWithoutUpdate(), -1);
	}
	public static void addExtraSalvage(CargoAPI cargo, MemoryAPI memory, float expire) {
		ExtraSalvage extra = getExtraSalvage(memory);
		if (extra == null) {
			extra = new ExtraSalvage(cargo);
		} else {
			extra.cargo.addAll(cargo);
		}
		memory.set(EXTRA_SALVAGE, extra, expire);
	}

	public static CargoAPI getCombinedExtraSalvage(Map<String, MemoryAPI> memoryMap) {
		ExtraSalvage extra = getExtraSalvage(memoryMap);
		ExtraSalvage temp = getTempExtraSalvage(memoryMap);
		CargoAPI cargo = Global.getFactory().createCargo(true);
		if (extra != null && extra.cargo != null) cargo.addAll(extra.cargo);
		if (temp != null && temp.cargo != null) cargo.addAll(temp.cargo);
		return cargo;
	}
	public static CargoAPI getCombinedExtraSalvage(SectorEntityToken entity) {
		ExtraSalvage extra = getExtraSalvage(entity);
		ExtraSalvage temp = getTempExtraSalvage(entity);
		CargoAPI cargo = Global.getFactory().createCargo(true);
		if (extra != null && extra.cargo != null) cargo.addAll(extra.cargo);
		if (temp != null && temp.cargo != null) cargo.addAll(temp.cargo);
		return cargo;
	}
	public static ExtraSalvage getTempExtraSalvage(SectorEntityToken entity) {
		return getTempExtraSalvage(entity.getMemoryWithoutUpdate());
	}
	public static ExtraSalvage getExtraSalvage(SectorEntityToken entity) {
		return getExtraSalvage(entity.getMemoryWithoutUpdate());
	}
	public static ExtraSalvage getExtraSalvage(MemoryAPI memory) {
		if (memory.contains(EXTRA_SALVAGE)) {
			return (ExtraSalvage) memory.get(EXTRA_SALVAGE);
		}
		return null;
	}
	public static ExtraSalvage getTempExtraSalvage(MemoryAPI memory) {
		if (memory.contains(TEMP_EXTRA_SALVAGE)) {
			return (ExtraSalvage) memory.get(TEMP_EXTRA_SALVAGE);
		}
		return null;
	}
	public static ExtraSalvage getTempExtraSalvage(Map<String, MemoryAPI> memoryMap) {
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(memoryMap);
		return getTempExtraSalvage(memory);
	}
	public static ExtraSalvage getExtraSalvage(Map<String, MemoryAPI> memoryMap) {
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(memoryMap);
		return getExtraSalvage(memory);
	}
	public static void clearExtraSalvage(Map<String, MemoryAPI> memoryMap) {
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(memoryMap);
		clearExtraSalvage(memory);
	}
	public static void clearExtraSalvage(MemoryAPI memory) {
		memory.unset(EXTRA_SALVAGE);
		memory.unset(TEMP_EXTRA_SALVAGE);
	}
	public static void clearExtraSalvage(SectorEntityToken entity) {
		clearExtraSalvage(entity.getMemoryWithoutUpdate());
	}
	
	public void init(InteractionDialogAPI dialog, Object specialData) {
		this.dialog = dialog;
		
		playerFleet = Global.getSector().getPlayerFleet();
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();
		memoryMap = dialog.getPlugin().getMemoryMap();
		
		entity = dialog.getInteractionTarget();
		
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(dialog.getPlugin().getMemoryMap());
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		if (seed == 0) {
			random = new Random();
		} else {
			random = Misc.getRandom(seed, 50);
		}
	}

	public void optionSelected(String optionText, Object optionData) {
		
	}
	
	public void initNothing() {
		setDone(true);
		setEndWithContinue(false);
		setShowAgain(false);
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean endWithContinue() {
		return endWithContinue;
	}

	public void setEndWithContinue(boolean endWithContinue) {
		this.endWithContinue = endWithContinue;
	}

	public String getString(String format) {
		return Misc.getStringWithTokenReplacement(format, entity, memoryMap);
	}
	
	public void addText(String format) {
		text.addParagraph(getString(format));
	}

	private boolean showAgain = false;
	public boolean shouldShowAgain() {
		return showAgain;
	}

	public void setShowAgain(boolean showAgain) {
		this.showAgain = showAgain;
	}

	private boolean shouldAbortSalvageAndRemoveEntity;
	public boolean shouldAbortSalvageAndRemoveEntity() {
		return shouldAbortSalvageAndRemoveEntity;
	}

	public void setShouldAbortSalvageAndRemoveEntity(boolean shouldAbortSalvageAndRemoveEntity) {
		this.shouldAbortSalvageAndRemoveEntity = shouldAbortSalvageAndRemoveEntity;
	}
	
	
}



