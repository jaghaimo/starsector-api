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


public interface CampaignPlugin {

	public static enum PickPriority {
		/**
		 * Lowest priority. Should only be used by core code, a modded plugin with this priority may not end up being used
		 * anywhere, as what gets picked when multiple plugins have the same priority is undefined.
		 */
		CORE_GENERAL,
		
		/**
		 * Should be used by mods for wholesale replacement of campaign features.
		 */
		MOD_GENERAL,
		
		/**
		 * Should only be used by core code.
		 */
		CORE_SET,
		
		/**
		 * For a plugin that handles a set of circumstances. For example "interaction with all jungle worlds".
		 * Overrides any _GENERAL prioritiy implementations (i.e. "interaction with all planets").
		 * Is overriden by _SPECIFIC priority ("interaction with this particular planet").
		 */
		MOD_SET,
		
		/**
		 * Should be used by core code only for specific encounters. For example, a "special" planet or fleet
		 * could have their own dialog, and the priority of this would override a mod that replaces the general interactions
		 * with all planets or fleets.
		 */
		CORE_SPECIFIC,
		
		/**
		 * Should be used by mods for specific encounters, that is, encounters that aren't handled by
		 * any of the _GENERAL and _SET priority plugins. For example, if a specific fleet has a special encounter dialog, it would be
		 * returned using this priority.
		 */
		MOD_SPECIFIC,
		
		/**
		 * Absolute highest priority; shouldn't be used without good reason.
		 * A mod compilation might use this to resolve conflicts introduced by mods it contains.
		 */
		HIGHEST,
		
	}
	
	/**
	 * Used for unregistering plugins, should be unique.
	 * Can be null, but shouldn't. If not null, the game will ensure that only one copy of the
	 * plugin can be registered - new registrations will override prior ones.
	 * @return
	 */
	String getId();
	
	/**
	 * If the plugin is transient, its data won't be included in save games and it needs to be re-added to
	 * the game every time (in ModPlugin.onGameLoad()).
	 * 
	 * If a plugin is not transient, its data is saved and it can be added in ModPlugin.onNewGame().
	 * 
	 * Plugins should be transient unless they need to save data, to improve the ability to add/remove mods
	 * from an existing game.
	 * 
	 * @return
	 */
	boolean isTransient();
	
	
	/**
	 * Returns the dialog plugin to be used to drive the interaction dialog for the particular entity.
	 * 
	 * Return null if this CampaignPlugin implementation doesn't provide one.
	 * 
	 * @param interactionTarget
	 * @return
	 */
	PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget);
	
	
	/**
	 * Used for:
	 *  - interaction dialogs created by clicking on a comm message action icon 
	 * 
	 * Return null if this CampaignPlugin implementation doesn't provide a dialog for the above use case(s).
	 * 
	 * @param interactionTarget
	 * @return
	 */
	PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(Object param, SectorEntityToken interactionTarget);
	
	
	/**
	 * Returns a plugin that is used to generate the battlefield. Mods could use this to create a custom
	 * battlefield for a special opponent, for example, without having to override the core
	 * BattleCreationPlugin implementation.
	 * 
	 * Return null if this CampaignPlugin implementation doesn't provide one.
	 * @param opponent
	 * @return
	 */
	PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent);
	
	
	/**
	 * Returns a plugin used to quickly resolve a battle outcome.
	 * 
	 * 
	 * Return null if this CampaignPlugin implementation doesn't provide one.
	 * @param one
	 * @param two
	 * @return
	 */
	PluginPick<BattleAutoresolverPlugin> pickBattleAutoresolverPlugin(BattleAPI battle);
	
	
	PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, String factionId);
	PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, PersonAPI person);
	
	
	/**
	 * Update the "this is known by the entity about the world" facts.
	 * Any variables set here should have an expiration time of 0, since
	 * this method will be called every time the getMemory() method is called.
	 * 
	 * Having facts not expire would clutter up the memory.
	 * 
	 * Mod-added facts should have their variable names use a mod-specific prefix to
	 * avoid conflicts.
	 * 
	 * @param memory
	 */
	void updateEntityFacts(SectorEntityToken entity, MemoryAPI memory);
	void updatePersonFacts(PersonAPI person, MemoryAPI memory);
	void updateFactionFacts(FactionAPI faction, MemoryAPI memory);
	void updateGlobalFacts(MemoryAPI memory);
	void updatePlayerFacts(MemoryAPI memory);
	void updateMarketFacts(MarketAPI market, MemoryAPI memory);
	
	
	
	
	
	
	/**
	 * See ModularFleetAIAPI documentation for details.
	 * @param fleet
	 * @param ai
	 * @return
	 */
	PluginPick<AssignmentModulePlugin> pickAssignmentAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai);
	
	/**
	 * See ModularFleetAIAPI documentation for details.
	 * @param fleet
	 * @param ai
	 * @return
	 */
	PluginPick<StrategicModulePlugin> pickStrategicAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai);
	
	/**
	 * See ModularFleetAIAPI documentation for details.
	 * @param fleet
	 * @param ai
	 * @return
	 */
	PluginPick<TacticalModulePlugin> pickTacticalAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai);
	
	/**
	 * See ModularFleetAIAPI documentation for details.
	 * @param fleet
	 * @param ai
	 * @return
	 */
	PluginPick<NavigationModulePlugin> pickNavigationAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai);
	
	
	
	/**
	 * AI for campaign abilities - transponder, go dark, emergency burn, etc.
	 * @param ability
	 * @return
	 */
	PluginPick<AbilityAIPlugin> pickAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai);
	
	
	PluginPick<FleetStubConverterPlugin> pickStubConverter(FleetStubAPI stub);
	PluginPick<FleetStubConverterPlugin> pickStubConverter(CampaignFleetAPI fleet);

	
	/**
	 * member will be null when picking plugin to assign idle officers from fleet screen.
	 * Only used for autofit in the refit screen. For NPC fleets, see: DefaultFleetInflater.
	 * @param member
	 * @return
	 */
	PluginPick<AutofitPlugin> pickAutofitPlugin(FleetMemberAPI member);

	PluginPick<InteractionDialogPlugin> pickRespawnPlugin();

	PluginPick<ImmigrationPlugin> pickImmigrationPlugin(MarketAPI market);

	PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String commodityId);
	PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId);

	PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params);

}








