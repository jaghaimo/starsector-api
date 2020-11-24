package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;

public class BlueprintSpecial extends BaseSalvageSpecial {

	public static float MAX_RANGE = 16000f;
	
	public static enum SurveyDataSpecialType {
		SCRAMBLED, // used when the planet is already surveyed or doesn't exist anymore or nothing is found etc
		PLANET_INTERESTING_PROPERTY,
		PLANET_SURVEY_DATA,
		SYSTEM_PRELIMINARY_SURVEY,
		AUTO_PICK, // generate one of the above automatically, for a nearby planet or system
		AUTO_PICK_NOT_SYSTEM, // pick either property or data, but not full system
	}
	
	
	public static class BlueprintSpecialData implements SalvageSpecialData {
		public BlueprintSpecialData() {
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new BlueprintSpecial();
		}
	}
	
	protected BlueprintSpecialData data;
	
	public BlueprintSpecial() {
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (BlueprintSpecialData) specialData;
		
		//random = new Random();
		
		CargoAPI bp = generateBP(random);
		bp.sort();
		
		if (bp.getStacksCopy().size() <= 0) {
			initNothing();
		} else {
			initBP(bp.getStacksCopy().get(0));
		}

	}

	public void initNothing() {
		addText("Your salvage crews find a likely-looking safe, but it's unfortunately empty.");
		setDone(true);
	}
	
	
	protected void initBP(CargoStackAPI stack) {
		if (!stack.isSpecialStack()) {
			initNothing();
			return;
		}
		
		addText("Your salvage crews find a well-hidden safe. After an EMP pulse and some delicate work " +
				"with a plasma cutter, the safe yields its contents.");
		
		playerFleet.getCargo().addFromStack(stack);
		AddRemoveCommodity.addStackGainText(stack, text, false);
		
		setDone(true);
	}
	

	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
	}

	
	public CargoAPI generateBP(Random random) {
		
		List<DropData> dropRandom = new ArrayList<DropData>();
		
		DropData d = new DropData();
		d.chances = 1;
		d.group = "blueprints_guaranteed";
		dropRandom.add(d);
		
		CargoAPI result = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, null, dropRandom);
		
		return result;
	}
	
}
