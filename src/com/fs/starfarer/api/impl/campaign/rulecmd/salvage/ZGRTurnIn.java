package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class ZGRTurnIn extends BaseCommandPlugin {
	
	public static float VALUE_MULT = 3f;
	public static float REP_MULT = 0.2f;
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected FactionAPI faction;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		if (command.equals("selectSellableItems")) {
			selectSellableItems();
		} else if (command.equals("playerHasSellableItems")) {
			return playerHasSellableItems();
		}
		
		return true;
	}
	

	protected void selectSellableItems() {
		CargoAPI copy = getSellableItems();
		
		final float width = 310f;
		dialog.showCargoPickerDialog("Select items to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
			public void pickedCargo(CargoAPI cargo) {
				if (cargo.isEmpty()) {
					cancelledCargoSelection();
					return;
				}
				
				cargo.sort();
				
				float bountyThreat = 0;
				float bountyMonster = 0;
				MemoryAPI mem = Global.getSector().getPlayerMemoryWithoutUpdate();
				
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
					int num = (int) stack.getSize();
					AddRemoveCommodity.addStackLossText(stack, text);
					if (isThreatStack(stack)) {
						bountyThreat += num * stack.getBaseValuePerUnit() * VALUE_MULT;
					} else if (isMonsterStack(stack)) {
						bountyMonster += num * stack.getBaseValuePerUnit() * VALUE_MULT;
					}
				}
				
				float repChange = computeReputationValue(cargo);

				int bounty = (int) (bountyThreat + bountyMonster);
				if (bounty > 0) {
					playerCargo.getCredits().add(bounty);
					AddRemoveCommodity.addCreditsGainText((int)bounty, text);
					
					String soldTotalKey = "$itemValueSoldToZGRThreat";
					int curr = mem.getInt(soldTotalKey);
					curr += bountyThreat;
					mem.set(soldTotalKey, curr);
					
					soldTotalKey = "$itemValueSoldToZGRMonster";
					curr = mem.getInt(soldTotalKey);
					curr += bountyMonster;
					mem.set(soldTotalKey, curr);
					
					soldTotalKey = "$itemValueSoldToZGRTotal";
					curr = mem.getInt(soldTotalKey);
					curr += bounty;
					mem.set(soldTotalKey, curr);
					
				}
				
				if (repChange >= 1f) {
					CustomRepImpact impact = new CustomRepImpact();
					impact.delta = repChange * 0.01f;
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.CUSTOM, impact,
												  null, text, true), 
												  faction.getId());
					
					impact.delta *= 0.25f;
					if (impact.delta >= 0.01f) {
						Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.CUSTOM, impact,
													  null, text, true), 
													  person);
					}
				}
				
				mem.set("$itemValueSoldToZGRJustNowThreat", (int)bountyThreat, 0);
				mem.set("$itemValueSoldToZGRJustNowMonster", (int)bountyMonster, 0);
				mem.set("$itemValueSoldToZGRJustNowTotal", (int)bounty, 0);
				FireBest.fire(null, dialog, memoryMap, "ZGRItemsTurnedIn");
			}
			public void cancelledCargoSelection() {
			}
			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
			
				float bounty = 0f;
				for (CargoStackAPI stack : combined.getStacksCopy()) {
					int num = (int) stack.getSize();
					bounty += num * stack.getBaseValuePerUnit() * VALUE_MULT;
				}
				
				float repChange = computeReputationValue(combined);
				
				float pad = 3f;
				float small = 5f;
				float opad = 10f;

				panel.setParaFontOrbitron();
				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
				//panel.addTitle(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor());
				//panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
				//panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName() + ")", faction.getBaseUIColor(), opad);
				panel.setParaFontDefault();
				
				panel.addImage(faction.getLogo(), width * 1f, 3f);
				
				panel.addPara("If you turn in the selected items, you will receive a %s bounty " +
						"and your standing with " + faction.getDisplayNameWithArticle() + " will improve by %s points.",
						opad * 1f, Misc.getHighlightColor(),
						Misc.getWithDGS(bounty) + Strings.C,
						"" + (int) repChange);
			}
		});
	}

	protected float computeReputationValue(CargoAPI cargo) {
		float rep = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			rep += getBaseRepValue(stack) * stack.getSize();
		}
		rep *= REP_MULT;
		return rep;
	}
	
	public static float getBaseRepValue(CargoStackAPI stack) {
		if (stack.isWeaponStack()) {
			switch (stack.getWeaponSpecIfWeapon().getSize()) {
			case LARGE: return 3f;
			case MEDIUM: return 2f;
			case SMALL: return 1f;
			}
		}
		if (stack.isSpecialStack()) {
			return 5f;
		}
		return 1f;
	}
	
	
	protected boolean playerHasSellableItems() {
		return !getSellableItems().isEmpty();
	}
	
	public static boolean isThreatStack(CargoStackAPI stack) {
		boolean match = false;
		match |= stack.isWeaponStack() && stack.getWeaponSpecIfWeapon().hasTag(Tags.THREAT); 
		match |= stack.isSpecialStack() && stack.getSpecialItemSpecIfSpecial().hasTag(Tags.THREAT);
		return match;
	}
	public static boolean isMonsterStack(CargoStackAPI stack) {
		boolean match = false;
		match |= stack.isWeaponStack() && stack.getWeaponSpecIfWeapon().hasTag(Tags.MONSTER); 
		match |= stack.isSpecialStack() && stack.getSpecialItemSpecIfSpecial().hasTag(Tags.MONSTER);
		return match;
	}
	
	protected CargoAPI getSellableItems() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			boolean match = isThreatStack(stack) || isMonsterStack(stack);
			if (match) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
		return copy;
	}
	
	
}















