package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class DistressResponse extends BaseCommandPlugin {
	
	//public static float REP_NORMAL = -0.05f;
	public static float REP_PER_USE = -0.02f;
	public static float REP_SCAM = -0.15f;
	
	protected CampaignFleetAPI playerFleet;
	protected CampaignFleetAPI fleet;
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

		fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

		if (command.equals("playerNeedsHelp")) {
			return playerNeedsHelp();
		} else if (command.equals("didNotNeedHelp")) {
			didNotNeedHelp();
		}
		else if (command.equals("acceptHelp")) {
			acceptAid();
		}
		else if (command.equals("neverMind")) {
			neverMind();
		}
		else if (command.equals("isCargoPodsScam")) {
			return isCargoPodsScam();
		}
		else if (command.equals("cargoPodsScam")) {
			cargoPodsScam();
		}
		else if (command.equals("unrespond")) {
			unrespond();
		}
		else if (command.equals("init")) {
			init();
		}
		else if (command.equals("pay")) {
			pay();
		}
		else if (command.equals("thank")) {
			thank();
		}
		
		return true;
	}
	
	protected void init() {
		person.getMemoryWithoutUpdate().set("$distressUsesLastCycle", getNumUses(), 0);
		
		int fuel = getNeededFuel();
		int supplies = getNeededSupplies();
		int maxFuel = getMaxFuel();
		int maxSupplies = getMaxSupplies();
		
		boolean adequate = fuel <= maxFuel * 1.5f && supplies <= maxSupplies * 1.5f;
		person.getMemoryWithoutUpdate().set("$distressHelpAdequate", adequate, 0);
		

		int distressPayment = getPayment();
		person.getMemoryWithoutUpdate().set("$distressPayment", Misc.getWithDGS(distressPayment), 0);
		person.getMemoryWithoutUpdate().set("$distressPaymentC", Misc.getWithDGS(distressPayment) + Strings.C, 0);
		person.getMemoryWithoutUpdate().set("$distressCanAfford", distressPayment <= playerCargo.getCredits().get(), 0);
	}
	
	protected int getPayment() {
		int fuel = getNeededFuel();
		int supplies = getNeededSupplies();
		int maxFuel = getMaxFuel();
		int maxSupplies = getMaxSupplies();
		if (fuel > maxFuel) fuel = maxFuel;
		if (supplies > maxSupplies) supplies = maxSupplies;
		
		CommoditySpecAPI fuelComm = Global.getSettings().getCommoditySpec(Commodities.FUEL);
		CommoditySpecAPI suppliesComm = Global.getSettings().getCommoditySpec(Commodities.FUEL);
		
		float distressPayment = fuel * fuelComm.getBasePrice() + supplies * suppliesComm.getBasePrice();
		distressPayment = (float) Math.ceil(distressPayment / 10000f) * 10000f;
		distressPayment *= 2f;
		
		return (int) distressPayment;
	}
	
	protected int getNumUses() {
		AbilityPlugin plugin = playerFleet.getAbility(Abilities.DISTRESS_CALL);
		if (plugin != null) {
			int uses = ((DistressCallAbility) plugin).getNumUsesInLastPeriod();
			return uses;
		}
		return 0;
	}

	protected boolean playerNeedsHelp() {
		return getNeededFuel() > 0 || getNeededSupplies() > 0;
	}
	
	protected void cargoPodsScam() {
		Misc.adjustRep(REP_SCAM, RepLevel.INHOSPITABLE, fleet.getFaction().getId(),
				   	   REP_SCAM, RepLevel.INHOSPITABLE, person, text);
		
		unrespond();	
	}
	
	
	protected boolean isCargoPodsScam () {
		for (SectorEntityToken entity : fleet.getContainingLocation().getAllEntities()) {
			if (Entities.CARGO_PODS.equals(entity.getCustomEntityType())) {
				VisibilityLevel level = entity.getVisibilityLevelTo(fleet);
				if (level == VisibilityLevel.COMPOSITION_DETAILS ||
						level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
					if (entity.getCargo().getFuel() >= 10 || entity.getCargo().getSupplies() >= 10) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected int getNeededFuel() {
		return getNeededFuel(playerFleet);
	}
	
	public static int getNeededFuel(CampaignFleetAPI playerFleet) {
		float returnDistLY = Misc.getDistanceLY(new Vector2f(), playerFleet.getLocationInHyperspace());
		int fuel = (int) (returnDistLY * Math.max(1, playerFleet.getLogistics().getFuelCostPerLightYear()));
		fuel *= 0.75f;
		if (fuel < 10) fuel = 10;
		fuel -= playerFleet.getCargo().getFuel();
		if (fuel < 0) fuel = 0;
		
		return fuel;
	}
	
	protected int getNeededSupplies() {
		//int supplies = (int) (playerCargo.getMaxCapacity() * 0.25f);
		float supplies = 0f;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			supplies += member.getStats().getSuppliesPerMonth().getModifiedValue();
		}
		//supplies *= 2;
		
		if (supplies < 5) supplies = 5;
		supplies -= playerCargo.getSupplies();
		if (supplies < 0) supplies = 0;
		
		return (int) supplies;
	}

	protected float getRepPenalty() {
		float uses = getNumUses();
		return REP_PER_USE * (uses + 1);
	}
	
	protected void didNotNeedHelp() {
		float penalty = getRepPenalty();
		Misc.adjustRep(penalty, RepLevel.INHOSPITABLE, fleet.getFaction().getId(),
					   penalty * 2f, RepLevel.INHOSPITABLE, person, text);
		
		unrespond();
	}

	protected void neverMind() {
		float penalty = getRepPenalty();
		Misc.adjustRep(penalty * 0.5f, RepLevel.INHOSPITABLE, fleet.getFaction().getId(),
				   	   penalty, RepLevel.INHOSPITABLE, person, text);
	}

	protected int getMaxFuel() {
		return (int) Math.max(10, (fleet.getCargo().getMaxFuel() * 0.25f));
	}
	protected int getMaxSupplies() {
		return (int) Math.max(10, (fleet.getCargo().getMaxCapacity() * 0.1f));
	}
	
	protected void pay() {
		//int credits = getPayment();
		int credits = (int) person.getMemoryWithoutUpdate().getFloat("$distressPayment");
		playerCargo.getCredits().subtract(credits);
		AddRemoveCommodity.addCreditsLossText(credits, text);
		
		float uses = getNumUses();
		if (uses <= 4) {
			Misc.adjustRep(0.01f, RepLevel.WELCOMING, fleet.getFaction().getId(),
			   	   	   	   0.02f, RepLevel.WELCOMING, person, text);
		}
	}
	
	protected void thank() {
		float penalty = getRepPenalty();
		Misc.adjustRep(penalty, RepLevel.INHOSPITABLE, fleet.getFaction().getId(),
			   	   	   penalty * 2f, RepLevel.INHOSPITABLE, person, text);
	}
	
	protected void acceptAid() {
		int fuel = getNeededFuel();
		int supplies = getNeededSupplies();
		
		int maxFuel = getMaxFuel();
		int maxSupplies = getMaxSupplies();
		
		if (fuel > maxFuel) fuel = maxFuel;
		if (supplies > maxSupplies) supplies = maxSupplies;

//		float penalty = getRepPenalty();
//		Misc.adjustRep(penalty, RepLevel.INHOSPITABLE, fleet.getFaction().getId(),
//			   	   	   penalty * 2f, RepLevel.INHOSPITABLE, person, text);
		
		if (fuel > 0) {
			playerCargo.addFuel(fuel);
			AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel, text);
		}
		if (supplies > 0) {
			playerCargo.addSupplies(supplies);
			AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies, text);
		}

		unrespond();
	}
	
	protected void unrespond() {
		Misc.clearTarget(fleet, true);
		Misc.makeUnimportant(fleet, "distressResponse");
		fleet.getMemoryWithoutUpdate().unset("$distressResponse");
		Misc.giveStandardReturnToSourceAssignments(fleet);
	}


	
}















