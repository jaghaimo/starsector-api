package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For planetkiller ("PK") related tasks.
 * 
 *	PK_CMD <action> <parameters>
 */
public class PK_CMD extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
//		MarketAPI market = dialog.getInteractionTarget().getMarket();
//		StarSystemAPI system = null;
//		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
//			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
//		}
				
		if ("removePKDefenses".equals(action)) {
			return removePKDefenses(dialog, memory);
		} else if ("convertSentinelToColony".equals(action)) {
			return convertSentinelToColony(text, cargo);
		} else if ("rightPostToAcceptPK".equals(action)) {
			return rightPostToAcceptPK(dialog);
		} else if ("rightPostToTellAboutSentinel".equals(action)) {
			return rightPostToTellAboutSentinel(dialog);
		} else if ("giveExecutor".equals(action)) {
			giveExecutor(dialog, params, memoryMap);
		}
		
		return false;
	}


	protected boolean convertSentinelToColony(TextPanelAPI text, CargoAPI cargo) {
		//int crew = 100;
		//cargo.removeCrew(crew);
		//AddRemoveCommodity.addCommodityLossText(Commodities.CREW, crew, text);
		// Crew removal handled in rules.
		
		PlanetAPI planet = RecoverAPlanetkiller.getTundra();
		MarketAPI market = planet.getMarket();
		
		//market.addTag(Tags.MARKET_NO_OFFICER_SPAWN);

		market.setSurveyLevel(SurveyLevel.FULL);
		for (MarketConditionAPI mc : market.getConditions()) {
			mc.setSurveyed(true);
		}
		
		market.setName(planet.getName());
		planet.setFaction(Factions.HEGEMONY);
		
		market.setDaysInExistence(0);
		market.setPlanetConditionMarketOnly(false);
		market.setFactionId(Factions.HEGEMONY);
		market.addCondition(Conditions.POPULATION_3);
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.FARMING);
		
		market.setSize(3);
		
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		//((StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
		
		market.getSubmarket(Submarkets.SUBMARKET_OPEN).getPlugin().updateCargoPrePlayerInteraction();
		market.getSubmarket(Submarkets.SUBMARKET_OPEN).getPlugin().getCargo().clear();
		if (market.getSubmarket(Submarkets.SUBMARKET_OPEN).getPlugin().getCargo().getMothballedShips() != null) {
			market.getSubmarket(Submarkets.SUBMARKET_OPEN).getPlugin().getCargo().getMothballedShips().clear();
		}
		market.getSubmarket(Submarkets.SUBMARKET_BLACK).getPlugin().updateCargoPrePlayerInteraction();
		market.getSubmarket(Submarkets.SUBMARKET_BLACK).getPlugin().getCargo().clear();
		if (market.getSubmarket(Submarkets.SUBMARKET_BLACK).getPlugin().getCargo().getMothballedShips() != null) {
			market.getSubmarket(Submarkets.SUBMARKET_BLACK).getPlugin().getCargo().getMothballedShips().clear();
		}
		
		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
		
		planet.setMarket(market);
		market.setPrimaryEntity(planet);
		market.setPlayerOwned(false);
		
		Global.getSector().getEconomy().addMarket(market, false);
		Global.getSector().getEconomy().tripleStep();
		market.advance(0f);
		market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
		
		//market.getConditions();
		
		SharedData.getData().getMarketsWithoutTradeFleetSpawn().add(market.getId());
		
		PersonAPI skiron = People.getPerson(People.SKIRON);
		market.setAdmin(skiron);
		market.getCommDirectory().addPerson(skiron, 0);
		market.addPerson(skiron);
		
		market.getPrimaryEntity().setInteractionImage("illustrations", "sentinel2");

		return true;
	}



	protected void giveExecutor(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		boolean removeSpecial = params.get(1).getBoolean(memoryMap);
		
		ShipVariantAPI v = Global.getSettings().getVariant("executor_Hull").clone();
		if (removeSpecial) {
			v.addSuppressedMod(HullMods.ANDRADA_MODS);
		}
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}


	protected boolean rightPostToAcceptPK(InteractionDialogAPI dialog) {
		PersonAPI person = dialog.getInteractionTarget().getActivePerson();
		if (person.getMemoryWithoutUpdate().getBoolean(LuddicPathBaseIntel.PATHER_BASE_COMMANDER)) {
			return true;
		}
		return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
				   Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
				   //Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) || // just military, no regular admins
				   Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
	}
	
	protected boolean rightPostToTellAboutSentinel(InteractionDialogAPI dialog) {
		PersonAPI person = dialog.getInteractionTarget().getActivePerson();
		return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
				Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
				Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
				Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
	}
	
	protected boolean removePKDefenses(InteractionDialogAPI dialog, MemoryAPI memory) {
		CampaignFleetAPI defenders = memory.getFleet("$defenderFleet");
		if (defenders != null) {
			memory.unset("$defenderFleet");
			memory.set("$defenderFleetDefeated", true);
			memory.set("$hasDefenders", false, 0);
			dialog.getInteractionTarget().getContainingLocation().addEntity(defenders);
			defenders.setAI(null);
			defenders.setNullAIActionText("dormant");
			Vector2f loc = dialog.getInteractionTarget().getLocation();
			defenders.setLocation(loc.x, loc.y + 100f);
			defenders.setCircularOrbit(dialog.getInteractionTarget().getOrbitFocus(), 90f, 100f, 20f);
			defenders.getMemoryWithoutUpdate().set("$pkDefenderFleet", true);
			defenders.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
			//Misc.addDefeatTrigger(defenders, "PK14thDefeated"); // handled elsewhere so the trigger is set in all cases
			Misc.makeNoRepImpact(defenders, "pk");
		}
		return true;
	}

	
	
}
