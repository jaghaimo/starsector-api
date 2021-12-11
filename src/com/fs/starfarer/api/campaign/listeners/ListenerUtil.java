package com.fs.starfarer.api.campaign.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener.RaidResultData;
import com.fs.starfarer.api.campaign.listeners.SubmarketInteractionListener.SubmarketInteractionType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamManager;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ListenerUtil {
	
	public static List<CampaignInputListener> getSortedInputListeners() {
		List<CampaignInputListener> list = Global.getSector().getListenerManager().getListeners(CampaignInputListener.class);
		Collections.sort(list, new Comparator<CampaignInputListener>() {
			public int compare(CampaignInputListener o1, CampaignInputListener o2) {
				return o2.getListenerInputPriority() - o1.getListenerInputPriority();
			}
		});
		return list;
	}
	public static void processCampaignInputPreCore(List<InputEventAPI> events) {
		for (CampaignInputListener x : getSortedInputListeners()) {
			x.processCampaignInputPreCore(events);
		}
	}
	public static void processCampaignInputPostCore(List<InputEventAPI> events) {
		for (CampaignInputListener x : getSortedInputListeners()) {
			x.processCampaignInputPostCore(events);
		}
	}
	public static void processCampaignInputPreFleetControl(List<InputEventAPI> events) {
		for (CampaignInputListener x : getSortedInputListeners()) {
			x.processCampaignInputPreFleetControl(events);
		}
	}
	
	public static void reportPlayerSurveyedPlanet(PlanetAPI planet) {
		for (SurveyPlanetListener x : Global.getSector().getListenerManager().getListeners(SurveyPlanetListener.class)) {
			x.reportPlayerSurveyedPlanet(planet);
		}
	}
	
	public static void reportPlayerColonizedPlanet(PlanetAPI planet) {
		for (PlayerColonizationListener x : Global.getSector().getListenerManager().getListeners(PlayerColonizationListener.class)) {
			x.reportPlayerColonizedPlanet(planet);
		}
	}
	
	public static void reportPlayerAbandonedColony(MarketAPI colony) {
		for (PlayerColonizationListener x : Global.getSector().getListenerManager().getListeners(PlayerColonizationListener.class)) {
			x.reportPlayerAbandonedColony(colony);
		}
	}
	
	public static void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
		for (ShowLootListener x : Global.getSector().getListenerManager().getListeners(ShowLootListener.class)) {
			x.reportAboutToShowLootToPlayer(loot, dialog);
		}
	}
	
	
	public static void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		for (FleetEventListener x : Global.getSector().getListenerManager().getListeners(FleetEventListener.class)) {
			x.reportFleetDespawnedToListener(fleet, reason, param);
		}
	}
	
	public static void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		for (FleetEventListener x : Global.getSector().getListenerManager().getListeners(FleetEventListener.class)) {
			x.reportBattleOccurred(fleet, primaryWinner, battle);
		}
	}
	
	public static void reportEconomyTick(int iterIndex) {
		for (EconomyTickListener x : Global.getSector().getListenerManager().getListeners(EconomyTickListener.class)) {
			x.reportEconomyTick(iterIndex);
		}
	}
	public static void reportEconomyMonthEnd() {
		for (EconomyTickListener x : Global.getSector().getListenerManager().getListeners(EconomyTickListener.class)) {
			x.reportEconomyMonthEnd();
		}
	}
	
	public static void reportEntityDiscovered(SectorEntityToken entity) {
		for (DiscoverEntityListener x : Global.getSector().getListenerManager().getListeners(DiscoverEntityListener.class)) {
			x.reportEntityDiscovered(entity);
		}
	}
	
	
	public static void reportObjectiveChangedHands(SectorEntityToken objective, FactionAPI from, FactionAPI to) {
		for (ObjectiveEventListener x : Global.getSector().getListenerManager().getListeners(ObjectiveEventListener.class)) {
			x.reportObjectiveChangedHands(objective, from, to);
		}
	}
	public static void reportObjectiveDestroyed(SectorEntityToken objective, SectorEntityToken stableLocation, FactionAPI enemy) {
		for (ObjectiveEventListener x : Global.getSector().getListenerManager().getListeners(ObjectiveEventListener.class)) {
			x.reportObjectiveDestroyed(objective, stableLocation, enemy);
		}
	}
	
	public static void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, CargoAPI cargo) {
		for (ColonyPlayerHostileActListener x : Global.getSector().getListenerManager().getListeners(ColonyPlayerHostileActListener.class)) {
			x.reportRaidForValuablesFinishedBeforeCargoShown(dialog, market, actionData, cargo);
		}
	}
	public static void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
		for (ColonyPlayerHostileActListener x : Global.getSector().getListenerManager().getListeners(ColonyPlayerHostileActListener.class)) {
			x.reportRaidToDisruptFinished(dialog, market, actionData, industry);
		}
	}
	
	public static void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		for (ColonyPlayerHostileActListener x : Global.getSector().getListenerManager().getListeners(ColonyPlayerHostileActListener.class)) {
			x.reportTacticalBombardmentFinished(dialog, market, actionData);
		}
	}
	
	public static void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		for (ColonyPlayerHostileActListener x : Global.getSector().getListenerManager().getListeners(ColonyPlayerHostileActListener.class)) {
			x.reportSaturationBombardmentFinished(dialog, market, actionData);
		}
	}
	
	public static void reportExtraSalvageShown(SectorEntityToken entity) {
		for (ExtraSalvageShownListener x : Global.getSector().getListenerManager().getListeners(ExtraSalvageShownListener.class)) {
			x.reportExtraSalvageShown(entity);
		}
	}
	
	public static void reportPlayerOpenedSubmarket(SubmarketAPI submarket, SubmarketInteractionType type) {
		for (SubmarketInteractionListener x : Global.getSector().getListenerManager().getListeners(SubmarketInteractionListener.class)) {
			x.reportPlayerOpenedSubmarket(submarket, type);
		}
	}
	
	public static void reportPlayerOpenedMarket(MarketAPI market) {
		for (ColonyInteractionListener x : Global.getSector().getListenerManager().getListeners(ColonyInteractionListener.class)) {
			x.reportPlayerOpenedMarket(market);
		}
	}
	
	public static void reportPlayerClosedMarket(MarketAPI market) {
		for (ColonyInteractionListener x : Global.getSector().getListenerManager().getListeners(ColonyInteractionListener.class)) {
			x.reportPlayerClosedMarket(market);
		}
	}
	
	public static void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
		for (ColonyInteractionListener x : Global.getSector().getListenerManager().getListeners(ColonyInteractionListener.class)) {
			x.reportPlayerOpenedMarketAndCargoUpdated(market);
		}
	}
	
	public static void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		for (ColonyInteractionListener x : Global.getSector().getListenerManager().getListeners(ColonyInteractionListener.class)) {
			x.reportPlayerMarketTransaction(transaction);
		}
	}
	
	public static void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
		for (GroundRaidObjectivesListener x : Global.getSector().getListenerManager().getListeners(GroundRaidObjectivesListener.class)) {
			x.modifyRaidObjectives(market, entity, objectives, type, marineTokens, priority);
		}
	}
	
	public static void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		for (GroundRaidObjectivesListener x : Global.getSector().getListenerManager().getListeners(GroundRaidObjectivesListener.class)) {
			x.reportRaidObjectivesAchieved(data, dialog, memoryMap);
		}	
	}
	
	public static void addCommodityTooltipSectionAfterPrice(TooltipMakerAPI info, float width, boolean expanded, CargoStackAPI stack) {
		for (CommodityTooltipModifier x : Global.getSector().getListenerManager().getListeners(CommodityTooltipModifier.class)) {
			x.addSectionAfterPrice(info, width, expanded, stack);
		}	
	}

	public static void reportCargoScreenOpened() {
		for (CargoScreenListener x : Global.getSector().getListenerManager().getListeners(CargoScreenListener.class)) {
			x.reportCargoScreenOpened();
		}
	}
	
	public static void reportPlayerLeftCargoPods(SectorEntityToken entity) {
		for (CargoScreenListener x : Global.getSector().getListenerManager().getListeners(CargoScreenListener.class)) {
			x.reportPlayerLeftCargoPods(entity);
		}
	}
	
	public static void reportPlayerNonMarketTransaction(PlayerMarketTransaction transaction, InteractionDialogAPI dialog) {
		for (CargoScreenListener x : Global.getSector().getListenerManager().getListeners(CargoScreenListener.class)) {
			x.reportPlayerNonMarketTransaction(transaction, dialog);
		}
	}
	
	public static void reportSubmarketOpened(SubmarketAPI submarket) {
		for (CargoScreenListener x : Global.getSector().getListenerManager().getListeners(CargoScreenListener.class)) {
			x.reportSubmarketOpened(submarket);
		}
	}
	
	public static void printOtherFactors(TooltipMakerAPI text, SectorEntityToken entity) {
		for (ColonyOtherFactorsListener x : Global.getSector().getListenerManager().getListeners(ColonyOtherFactorsListener.class)) {
			x.printOtherFactors(text, entity);
		}
	}
	
	public static void modifyMarineLossesStatPreRaid(MarketAPI market, List<GroundRaidObjectivePlugin> objectives, MutableStat stat) {
		for (MarineLossesStatModifier x : Global.getSector().getListenerManager().getListeners(MarineLossesStatModifier.class)) {
			x.modifyMarineLossesStatPreRaid(market, objectives, stat);
		}
	}
	
	public static void reportFleetTransitingGate(CampaignFleetAPI fleet, SectorEntityToken gateFrom, SectorEntityToken gateTo) {
		for (GateTransitListener x : Global.getSector().getListenerManager().getListeners(GateTransitListener.class)) {
			x.reportFleetTransitingGate(fleet, gateFrom, gateTo);
		}
	}
	
	public static void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog) {
		for (ShipRecoveryListener x : Global.getSector().getListenerManager().getListeners(ShipRecoveryListener.class)) {
			x.reportShipsRecovered(ships, dialog);
		}
	}
	
	public static void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
		for (CurrentLocationChangedListener x : Global.getSector().getListenerManager().getListeners(CurrentLocationChangedListener.class)) {
			x.reportCurrentLocationChanged(prev, curr);
		}
	}
	
	public static void reportColonyAboutToBeDecivilized(MarketAPI market, boolean fullyDestroyed) {
	for (ColonyDecivListener x : Global.getSector().getListenerManager().getListeners(ColonyDecivListener.class)) {
		x.reportColonyAboutToBeDecivilized(market, fullyDestroyed);
	}
}
	public static void reportColonyDecivilized(MarketAPI market, boolean fullyDestroyed) {
		for (ColonyDecivListener x : Global.getSector().getListenerManager().getListeners(ColonyDecivListener.class)) {
			x.reportColonyDecivilized(market, fullyDestroyed);
		}
	}
	
	public static void updateSlipstreamBlockers(CollisionGridAPI grid, SlipstreamManager manager) {
		for (SlipstreamBlockerUpdater x : Global.getSector().getListenerManager().getListeners(SlipstreamBlockerUpdater.class)) {
			x.updateSlipstreamBlockers(grid, manager);
		}
	}
	public static void updateSlipstreamConfig(String prevConfig, WeightedRandomPicker<String> nextConfigPicker,
											  SlipstreamManager manager) {
		for (SlipstreamConfigUpdater x : Global.getSector().getListenerManager().getListeners(SlipstreamConfigUpdater.class)) {
			x.updateSlipstreamConfig(prevConfig, nextConfigPicker, manager);
		}
	}
	
	public static int countOtherFactors(SectorEntityToken entity) {
		int count = 0;
		for (ColonyOtherFactorsListener x : Global.getSector().getListenerManager().getListeners(ColonyOtherFactorsListener.class)) {
			if (x.isActiveFactorFor(entity)) count++;
		}
		return count;
	}
	public static boolean hasOtherFactors(SectorEntityToken entity) {
		return countOtherFactors(entity) > 0;
	}
	
	public static List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
		List<EncounterPoint> result = new ArrayList<EncounterPoint>();
		if (where == null) return result;
		for (EncounterPointProvider x : Global.getSector().getListenerManager().getListeners(EncounterPointProvider.class)) {
			List<EncounterPoint> curr = x.generateEncounterPoints(where);
			if (curr != null) {
				result.addAll(curr);
			}
		}
		return result;
	}
	
//	public static void reportFleetSpawnedToListener(CampaignFleetAPI fleet) {
//		for (FleetSpawnListener x : Global.getSector().getListenerManager().getListeners(FleetSpawnListener.class)) {
//			x.reportFleetSpawnedToListener(fleet);
//		}
//	}
}












