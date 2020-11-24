package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.InvestigationEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class CustomsInspectionGenerateResult extends BaseCommandPlugin {

	public static enum CargoInspectionResultType {
		TOLL,
		TOLL_AND_FINE,
	}
	
	public static class CargoInspectionResult {
		private CargoInspectionResultType type;
		private float tollAmount;
		private CargoAPI legalFound, illegalFound;

		public float getTollAmount() {
			return tollAmount;
		}
		public void setTollAmount(float tollAmount) {
			this.tollAmount = tollAmount;
		}
		public CargoInspectionResultType getType() {
			return type;
		}
		public void setType(CargoInspectionResultType type) {
			this.type = type;
		}
		public CargoAPI getLegalFound() {
			return legalFound;
		}
		public void setLegalFound(CargoAPI legalFound) {
			this.legalFound = legalFound;
		}
		public CargoAPI getIllegalFound() {
			return illegalFound;
		}
		public void setIllegalFound(CargoAPI illegalFound) {
			this.illegalFound = illegalFound;
		}
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		FactionAPI faction = other.getFaction();
		
		CargoInspectionResult result = new CargoInspectionResult();
		float totalLegal = 0;
		float totalIllegal = 0;
		float totalIllegalFound = 0;
		CargoAPI legalFound = Global.getFactory().createCargo(true); 
		CargoAPI illegalFound = Global.getFactory().createCargo(true);
		CargoAPI all = Global.getFactory().createCargo(true);
		
		for (CargoStackAPI stack : playerFleet.getCargo().getStacksCopy()) {
			boolean legal = !faction.isIllegal(stack);
			if (legal) {
				totalLegal += stack.getSize();
			} else {
				totalIllegal += stack.getSize();
			}
			all.addFromStack(stack);
		}
		float guiltMult = InvestigationEvent.getPlayerRepGuiltMult(faction);
		//totalIllegal *= guiltMult;
		float capacity = playerFleet.getCargo().getMaxCapacity();
		
		float shieldedFraction = Misc.getShieldedCargoFraction(playerFleet);
		float unshieldedFraction = 1f - shieldedFraction;
		
		float shieldedMult = (0.5f + 0.5f * unshieldedFraction);
		
		if (totalLegal + totalIllegal > 0) {
			List<CargoStackAPI> stacks = all.getStacksCopy();
			Collections.shuffle(stacks);
			float illegalSoFar = 0;
			for (CargoStackAPI stack : stacks) {
				if (stack.getSize() <= 0) continue;
				boolean legal = !faction.isIllegal(stack);
				illegalSoFar += stack.getSize();
				float chanceToFind = illegalSoFar / (totalLegal + totalIllegal);
				chanceToFind *= guiltMult;
				chanceToFind *= shieldedMult;
				if (legal) {
					legalFound.addFromStack(stack);
				} else if ((float) Math.random() < chanceToFind) {
					float qty = stack.getSize();
					qty = qty * (0.33f + (float) Math.random() * 0.67f);
					qty *= shieldedMult;
					qty = Math.round(qty);
					if (qty < 1) qty = 1;
					illegalFound.addItems(stack.getType(), stack.getData(), qty);
				}
			}
		}

		result.setLegalFound(legalFound);
		result.setIllegalFound(illegalFound);
		if (illegalFound.isEmpty()) {
			result.setType(CargoInspectionResultType.TOLL);
		} else {
			result.setType(CargoInspectionResultType.TOLL_AND_FINE);
		}
		
		//float shipTollAmount = playerFleet.getFleetPoints() * 50f;
		float shipTollAmount = 0f; //playerFleet.getFleetPoints() * 50f;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			shipTollAmount += member.getBaseSellValue() * 0.125f * faction.getTollFraction();
		}
		shipTollAmount = (int)shipTollAmount;
		
		float tollFraction = faction.getTollFraction();
		float fineFraction = faction.getFineFraction();
		
		float toll = 0;
		float fine = 0;
		for (CargoStackAPI stack : legalFound.getStacksCopy()) {
			toll += stack.getSize() * stack.getBaseValuePerUnit() * tollFraction * shieldedMult;
		}
		for (CargoStackAPI stack : illegalFound.getStacksCopy()) {
			fine += stack.getSize() * stack.getBaseValuePerUnit() * fineFraction;
			totalIllegalFound += stack.getSize();
		}
		
		float totalTollAndFine = shipTollAmount + toll + fine;
		
		toll = (int)toll;
		fine = (int)fine;
		
		//totalTollAndFine *= 70f;
		
		result.setTollAmount(totalTollAndFine);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		memory.set("$tollAmount", "" + (int)result.getTollAmount(), 0);
		memory.set("$inspectionResultType", result.getType().name(), 0);
		memory.set("$playerCanAffordPayment", playerFleet.getCargo().getCredits().get() >= result.getTollAmount(), 0);
		memory.set("$cargoInspectionResult", result, 0);
		
		
		TextPanelAPI text = dialog.getTextPanel();
		
		text.setFontVictor();
		text.setFontSmallInsignia();
		
		Color hl = Misc.getHighlightColor();
		Color red = Misc.getNegativeHighlightColor();
		text.addParagraph("-----------------------------------------------------------------------------");
		
		text.addParagraph("Fleet size toll: " + (int) shipTollAmount);
		text.highlightInLastPara(hl, "" + (int) shipTollAmount);
		text.addParagraph("Cargo toll: " + (int) toll);
		text.highlightInLastPara(hl, "" + (int) toll);
		
		if (!illegalFound.isEmpty()) {
			text.addParagraph("Contraband found!", red);
			String para = "";
			List<String> highlights = new ArrayList<String>();
			for (CargoStackAPI stack : illegalFound.getStacksCopy()) {
				para += stack.getDisplayName() + " x " + (int)stack.getSize() + "\n";
				highlights.add("" + (int)stack.getSize());
			}
			para = para.substring(0, para.length() - 1);
			text.addParagraph(para);
			text.highlightInLastPara(hl, highlights.toArray(new String [0]));
			
			text.addParagraph("Fine: " + (int) fine);
			text.highlightInLastPara(hl, "" + (int) fine);
		}
		
		text.addParagraph("Total: " + (int) totalTollAndFine + " credits");
		text.highlightInLastPara(hl, "" + (int) totalTollAndFine);
		
		text.addParagraph("-----------------------------------------------------------------------------");
		
		text.setFontInsignia();
		
//		float repLoss = totalIllegalFound / 10f * totalIllegalFound / capacity;
//		repLoss = Math.round(repLoss);
//		if (repLoss > 5) repLoss = 5f;
//		if (repLoss == 0 && totalIllegalFound > 0) repLoss = 1f;
//		if (repLoss > 0) {
//			RepActionEnvelope envelope = new RepActionEnvelope(RepActions.CUSTOMS_CAUGHT_SMUGGLING, repLoss, text);
//			Global.getSector().adjustPlayerReputation(envelope, faction.getId());
//		}
		
		
		return true;
	}

}















