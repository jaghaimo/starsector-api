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
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CargoScan extends BaseCommandPlugin {

	public static String PODS_FOUND = "$scan_podsFound";
	public static String CONTRABAND_FOUND = "$scan_contrabandFound";
	public static String SUSPICOUS_CARGO_FOUND = "$scan_suspiciousCargoFound";
	//public static String DEMAND_BOARDING = "$scan_demandBoarding";
	public static String RESULT_KEY = "$scan_cargoScanResult";
	
	public static float INSPECTION_DAMAGE_MULT = 0.2f;
	public static float CHANCE_TO_FIND_ILLEGAL_MULT = 2f;
	
	public static class CargoScanResult {
		protected CargoAPI legalFound, illegalFound;
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
		
		protected List<FleetMemberAPI> shipsToDamage = new ArrayList<FleetMemberAPI>();
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		FactionAPI faction = other.getFaction();
		
		CargoScanResult result = new CargoScanResult();
		
		float totalLegal = 0;
		float totalLegalCargo = 0;
		float totalIllegal = 0;
		float totalIllegalFound = 0;
		CargoAPI legalFound = Global.getFactory().createCargo(true); 
		CargoAPI illegalFound = Global.getFactory().createCargo(true);
		CargoAPI all = Global.getFactory().createCargo(true);
		
		float totalCargo = playerFleet.getCargo().getSpaceUsed();
		float totalCrew = playerFleet.getCargo().getTotalPersonnel();
		float totalFuel = playerFleet.getCargo().getFuel();
		
		for (CargoStackAPI stack : playerFleet.getCargo().getStacksCopy()) {
			boolean legal = !faction.isIllegal(stack);
			if (legal) {
				totalLegal += stack.getSize();
				totalLegalCargo += stack.getCargoSpace();
			} else {
				totalIllegal += stack.getSize();
			}
			all.addFromStack(stack);
		}
		//float guiltMult = InvestigationEvent.getPlayerRepGuiltMult(faction);
		float guiltMult = 1f;
		
		float shieldedFraction = Misc.getShieldedCargoFraction(playerFleet);
		float unshieldedFraction = 1f - shieldedFraction;
		
		float shieldedMult = (0.25f + 0.75f * unshieldedFraction);
		
		MarketAPI market = Misc.getSourceMarket(other);
		float level = market.getMemory().getFloat(MemFlags.MEMORY_MARKET_SMUGGLING_SUSPICION_LEVEL);
		float suspicionMult = 0f;
		if (market != null) {
			if (level >= 0.05f) {
				suspicionMult = 0.5f + 0.5f * level;
			}
		}
		
		totalLegalCargo *= shieldedMult;
		
		
		boolean suspicious = false;
		boolean suspiciousDueToLevel = false;
		if (totalLegalCargo > 50 || totalLegalCargo > playerFleet.getCargo().getMaxCapacity() * 0.5f) {
			totalLegalCargo *= suspicionMult;
			suspicious = totalLegalCargo * guiltMult * (float) Math.random() > 
							 	playerFleet.getCargo().getMaxCapacity() * (float) Math.random();
		}
		
//		suspicious = false;
//		level = 10f;
		
		if (!suspicious && level >= 0.5f) {
			float r = (float) Math.random();
			suspicious |= r * r < level;
			if (suspicious) {
				suspiciousDueToLevel = true;
			}
		}
		
		if (totalLegal + totalIllegal > 0) {
			List<CargoStackAPI> stacks = all.getStacksCopy();
			Collections.shuffle(stacks);
			//float illegalSoFar = 0;
			float illegalCargoSoFar = 0;
			float illegalCrewSoFar = 0;
			float illegalFuelSoFar = 0;
			for (CargoStackAPI stack : stacks) {
				if (stack.getSize() <= 0) continue;
				boolean legal = !faction.isIllegal(stack);
				float chanceToFind = 0f;
				if (stack.isPersonnelStack()) {
					if (totalCrew > 0) {
						if (!legal) illegalCrewSoFar += stack.getSize();
						chanceToFind = illegalCrewSoFar / totalCrew;
					}
				} else if (stack.isFuelStack()) {
					if (totalFuel > 0) {
						if (!legal) illegalFuelSoFar += stack.getSize();
						chanceToFind = illegalFuelSoFar / totalFuel;
					}
				} else {
					if (totalCargo > 0) {
						if (!legal) illegalCargoSoFar += stack.getCargoSpace();
						chanceToFind = illegalCargoSoFar / totalCargo;
					}
				}
				
				chanceToFind *= guiltMult;
				chanceToFind *= shieldedMult;
				chanceToFind *= CHANCE_TO_FIND_ILLEGAL_MULT;
				
//				if (chanceToFind > 0 && !legal) {
//					System.out.println("fwefwef");
//				}
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
		//illegalFound.clear();
		
		//boolean boarding = !suspicious && level >= 0.5f && illegalFound.isEmpty();
		if (suspicious && illegalFound.isEmpty()) {
			WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
			for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
				if (member.isMothballed() && member.getRepairTracker().getBaseCR() < 0.2f) continue;
				picker.add(member, member.getFleetPointCost());
			}
			if (picker.isEmpty()) {
				suspicious = false;
			} else {
				float totalDamage = Math.min(playerFleet.getFleetPoints(), other.getFleetPoints()) * INSPECTION_DAMAGE_MULT;
				float picked = 0f;
				while (picked < totalDamage && !picker.isEmpty()) {
					FleetMemberAPI pick = picker.pickAndRemove();
					result.shipsToDamage.add(pick);
					picked += pick.getFleetPointCost();
				}
			}
		}
		
		result.setLegalFound(legalFound);
		result.setIllegalFound(illegalFound);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		memory.set(CONTRABAND_FOUND, !illegalFound.isEmpty(), 0);
		memory.set(SUSPICOUS_CARGO_FOUND, suspicious, 0);
		memory.set(RESULT_KEY, result, 0);
		
		float maxPodsDist = 1500f;
		OUTER: for (SectorEntityToken entity : other.getContainingLocation().getAllEntities()) {
			if (Entities.CARGO_PODS.equals(entity.getCustomEntityType())) {
				VisibilityLevel vLevel = entity.getVisibilityLevelTo(other);
				if (entity.getCustomPlugin() instanceof CargoPodsEntityPlugin) {
					float dist = Misc.getDistance(playerFleet, entity);
					if (dist > maxPodsDist) continue;
					
					CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin) entity.getCustomPlugin();
					if (plugin.getElapsed() <= 1f && entity.getCargo() != null) {
						if (vLevel == VisibilityLevel.COMPOSITION_DETAILS ||
								vLevel == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
							for (CargoStackAPI stack : entity.getCargo().getStacksCopy()) {
								boolean legal = !faction.isIllegal(stack);
								if (!legal) {
									memory.set(PODS_FOUND, true, 0);
									Misc.fadeAndExpire(entity);
									break OUTER;									
								}
							}

						}
					}
				}
			}
		}
		
		TextPanelAPI text = dialog.getTextPanel();
		
		//text.setFontVictor();
		text.setFontSmallInsignia();
		
		Color hl = Misc.getHighlightColor();
		Color red = Misc.getNegativeHighlightColor();
		text.addParagraph("-----------------------------------------------------------------------------");
		
		if (!illegalFound.isEmpty()) {
			text.addParagraph("Contraband found!", red);
			String para = "";
			List<String> highlights = new ArrayList<String>();
			for (CargoStackAPI stack : illegalFound.getStacksCopy()) {
				para += stack.getDisplayName() + " " + Strings.X + " " + (int)stack.getSize() + "\n";
				highlights.add("" + (int)stack.getSize());
			}
			para = para.substring(0, para.length() - 1);
			text.addParagraph(para);
			text.highlightInLastPara(hl, highlights.toArray(new String [0]));
		} else if (suspicious) {
			if (suspiciousDueToLevel) {
				text.addParagraph("Vessels flagged for inspection due to overall suspicion level!", hl);
			} else {
				text.addParagraph("Suspicious cargo found!", hl);
			}
		} else {
			text.addParagraph("No contraband or suspicious cargo found.");
		}
		
		text.addParagraph("-----------------------------------------------------------------------------");
		
		text.setFontInsignia();
		
		for (CargoStackAPI stack : illegalFound.getStacksCopy()) {
			totalIllegalFound += stack.getSize();
		}
		
		float capacity = playerFleet.getCargo().getMaxCapacity();
		float repLoss = totalIllegalFound / 5f * totalIllegalFound / capacity;
		repLoss = Math.round(repLoss);
		if (repLoss > 5) repLoss = 5f;
		if (repLoss == 0 && totalIllegalFound > 0) repLoss = 1f;
		if (suspicious) {
			repLoss = 5f;
		}
		if (repLoss > 0) {
			RepActionEnvelope envelope = new RepActionEnvelope(RepActions.CUSTOMS_CAUGHT_SMUGGLING, repLoss, dialog.getTextPanel());
			Global.getSector().adjustPlayerReputation(envelope, faction.getId());
			
			envelope = new RepActionEnvelope(RepActions.CUSTOMS_CAUGHT_SMUGGLING, repLoss * 2f, dialog.getTextPanel());
			Global.getSector().adjustPlayerReputation(envelope, dialog.getInteractionTarget().getActivePerson());
		}
		
		return true;
	}

}















