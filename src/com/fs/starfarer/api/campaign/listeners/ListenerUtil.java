package com.fs.starfarer.api.campaign.listeners;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.input.InputEventAPI;

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
	
}












