package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.ai.AbilityAIPlugin;
import com.fs.starfarer.api.campaign.ai.AssignmentModulePlugin;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.NavigationModulePlugin;
import com.fs.starfarer.api.campaign.ai.StrategicModulePlugin;
import com.fs.starfarer.api.campaign.ai.TacticalModulePlugin;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin;


/**
 * Extend this class instead of implementing CampaignPlugin for convenience if you do not
 * intend to implement all the methods. This will also help avoid your mod breaking
 * when new methods are added to CampaignPlugin, since default implemenations will be
 * added here and your implementation will inherit them.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2013 Fractal Softworks, LLC
 */
public class BaseCampaignPlugin implements CampaignPlugin {

	public String getId() {
		return null;
	}

	public boolean isTransient() {
		return true;
	}

	public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent) {
		return null;
	}

	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		return null;
	}
	
	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(Object param, SectorEntityToken interactionTarget) {
		return null;
	}

	public PluginPick<BattleAutoresolverPlugin> pickBattleAutoresolverPlugin(BattleAPI battle) {
		return null;
	}
	
	public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, String factionId) {
		return null;
	}

	
	public void updateEntityFacts(SectorEntityToken entity, MemoryAPI memory) {
		
	}

	public void updateFactionFacts(FactionAPI faction, MemoryAPI memory) {
		
	}

	public void updateGlobalFacts(MemoryAPI memory) {
		
	}

	public void updatePersonFacts(PersonAPI person, MemoryAPI memory) {
		
	}

	public void updatePlayerFacts(MemoryAPI memory) {
		
	}

	public void updateMarketFacts(MarketAPI market, MemoryAPI memory) {
		
	}

	public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, PersonAPI person) {
		return null;
	}

	public PluginPick<AbilityAIPlugin> pickAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
		return null;
	}

	public PluginPick<AssignmentModulePlugin> pickAssignmentAIModule(
			CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
		return null;
	}

	public PluginPick<NavigationModulePlugin> pickNavigationAIModule(
			CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
		return null;
	}

	public PluginPick<StrategicModulePlugin> pickStrategicAIModule(
			CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
		return null;
	}

	public PluginPick<TacticalModulePlugin> pickTacticalAIModule(
			CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
		return null;
	}

	public PluginPick<FleetStubConverterPlugin> pickStubConverter(FleetStubAPI stub) {
		return null;
	}

	public PluginPick<FleetStubConverterPlugin> pickStubConverter(CampaignFleetAPI fleet) {
		return null;
	}

	public PluginPick<AutofitPlugin> pickAutofitPlugin(FleetMemberAPI member) {
		return null;
	}

	public PluginPick<InteractionDialogPlugin> pickRespawnPlugin() {
		return null;
	}

	public PluginPick<ImmigrationPlugin> pickImmigrationPlugin(MarketAPI market) {
		return null;
	}

	public PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String commodityId) {
		return null;
	}

	/**
	 * @param fleet  
	 * @param params 
	 */
	public PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params) {
		return null;
	}

	public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
		return null;
	}

}



