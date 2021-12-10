package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 */
public class DemandCargo extends BaseCommandPlugin {
	
	public static class DemandData {
		public CargoAPI cargo;//Global.getFactory().createCargo(true);
		public List<FleetMemberAPI> ships = new ArrayList<FleetMemberAPI>();
		public int credits = 0;
		public boolean hasAnythingWorthwhile = true;
	}
	
	
	
	protected CampaignFleetAPI playerFleet;
	protected CampaignFleetAPI otherFleet;
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

	protected boolean buysAICores;
	protected float valueMult;
	protected float repMult;
	
	protected DemandData data = new DemandData();
	
	public DemandCargo() {
		super();
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
//		Pirate interaction flow:
//			They hail you OR you open the comm link and "perhaps we can avoid fighting"
//			<some greeting/setup text>
//			-> Continue
//			<negotiating text, list of stuff>
//			-> Cut the comm link, OR
//			-> Order the transfer
//			<lost X> text
//			-> Continue?
//			<ending text, allow you to disengage>
//			-> Cut the comm link
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		otherFleet = (CampaignFleetAPI) entity;
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		if (person != null) {
			faction = person.getFaction();
		} else {
			faction = entityFaction;
		}
		
		String key = "$DemandCargo_temp";
		MemoryAPI mem = otherFleet.getMemoryWithoutUpdate();
		if (mem.contains(key)) {
			data = (DemandData) mem.get(key);
		} else {
			data = new DemandData();
			data.cargo = Global.getFactory().createCargo(true);;
			mem.set(key, data, 0f);
		}
		
		if (command.equals("selectCargo")) {
			selectCores();
		} else if (command.equals("playerHasValuableCargo")) {
			//return playerHasCores();
		}
		
		return true;
	}
	
	protected Random getRandom() {
		String key = "$DemandCargo_random";
		MemoryAPI mem = otherFleet.getMemoryWithoutUpdate();
		Random random = null;
		if (mem.contains(key)) {
			random = (Random) mem.get(key);
		} else {
			//random = new Random(Misc.getSalvageSeed(otherFleet));
			long seed = Misc.getSalvageSeed(otherFleet);
			seed /= 321L;
			seed *= (Global.getSector().getClock().getMonth() + 10);
			random = new Random(seed);
		}
		mem.set(key, random, 30f);
		
		return random;
	}

	protected void computeAndPrintDemands() {
		int fleetValue = 0;
		
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			fleetValue += member.getHullSpec().getBaseValue();
		}
		
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			fleetValue += stack.getBaseValuePerUnit() * stack.getSize();
		}
		
		float demandFraction = 0.2f;
		
		float rel = otherFleet.getFaction().getRelToPlayer().getRel();
		
		if (rel > 0) {
			demandFraction *= (0.2f + 0.8f * rel);
		}
		
		int demandValue = (int) (fleetValue * demandFraction);
		
		Random random = getRandom();
		
		
		
	}
	
	

	protected void selectCores() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		//copy.addAll(cargo);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
		
//		final float width = 310f;
//		dialog.showCargoPickerDialog("Select AI cores to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
//			public void pickedCargo(CargoAPI cargo) {
//				cargo.sort();
//				for (CargoStackAPI stack : cargo.getStacksCopy()) {
//					playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
//					if (stack.isCommodityStack()) { // should be always, but just in case
//						AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
//					}
//				}
//				
//				float bounty = computeCoreCreditValue(cargo);
//				float repChange = computeCoreReputationValue(cargo);
//
//				if (bounty > 0) {
//					playerCargo.getCredits().add(bounty);
//					AddRemoveCommodity.addCreditsGainText((int)bounty, text);
//				}
//				
//				if (repChange >= 1f) {
//					CustomRepImpact impact = new CustomRepImpact();
//					impact.delta = repChange * 0.01f;
//					Global.getSector().adjustPlayerReputation(
//							new RepActionEnvelope(RepActions.CUSTOM, impact,
//												  null, text, true), 
//												  faction.getId());
//					
//					impact.delta *= 0.25f;
//					if (impact.delta >= 0.01f) {
//						Global.getSector().adjustPlayerReputation(
//								new RepActionEnvelope(RepActions.CUSTOM, impact,
//													  null, text, true), 
//													  person);
//					}
//				}
//				
//				FireBest.fire(null, dialog, memoryMap, "AICoresTurnedIn");
//			}
//			public void cancelledCargoSelection() {
//			}
//			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
//			
//				float bounty = computeCoreCreditValue(combined);
//				float repChange = computeCoreReputationValue(combined);
//				
//				float pad = 3f;
//				float small = 5f;
//				float opad = 10f;
//
//				panel.setParaFontOrbitron();
//				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
//				//panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
//				//panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName() + ")", faction.getBaseUIColor(), opad);
//				panel.setParaFontDefault();
//				
//				panel.addImage(faction.getLogo(), width * 1f, pad);
//				
//				
//				//panel.setParaFontColor(Misc.getGrayColor());
//				//panel.setParaSmallInsignia();
//				//panel.setParaInsigniaLarge();
//				panel.addPara("Compared to dealing with other factions, turning AI cores in to " + 
//						faction.getDisplayNameLongWithArticle() + " " +
//						"will result in:", opad);
//				panel.beginGridFlipped(width, 1, 40f, 10f);
//				//panel.beginGrid(150f, 1);
//				panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
//				panel.addToGrid(0, 1, "Reputation gain", "" + (int)(repMult * 100f) + "%");
//				panel.addGrid(pad);
//				
//				panel.addPara("If you turn in the selected AI cores, you will receive a %s bounty " +
//						"and your standing with " + faction.getDisplayNameWithArticle() + " will improve by %s points.",
//						opad * 1f, Misc.getHighlightColor(),
//						Misc.getWithDGS(bounty) + Strings.C,
//						"" + (int) repChange);
//				
//				
//				//panel.addPara("Bounty: %s", opad, Misc.getHighlightColor(), Misc.getWithDGS(bounty) + Strings.C);
//				//panel.addPara("Reputation: %s", pad, Misc.getHighlightColor(), "+12");
//			}
//		});
	}



	
	
}















