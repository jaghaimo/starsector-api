package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BattleAutoresolverPlugin;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.FleetStubConverterPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.ai.AbilityAIPlugin;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.tutorial.CampaignTutorialScript;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialRespawnDialogPluginImpl;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.plugins.AutofitPlugin;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.util.Misc;

public class CoreCampaignPluginImpl extends BaseCampaignPlugin {

	public String getId() {
		return "coreCampaignPluginImpl";
	}
	
	public boolean isTransient() {
		return false;
	}

	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		if (interactionTarget.hasTag(Tags.OBJECTIVE) || interactionTarget.getMarket() != null) {
			return new PluginPick<InteractionDialogPlugin>(new RuleBasedInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		
		if (interactionTarget.hasTag(Tags.GATE)) {
			return new PluginPick<InteractionDialogPlugin>(new RuleBasedInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		
		if (interactionTarget.hasTag(Tags.STATION)) {
			return new PluginPick<InteractionDialogPlugin>(new RuleBasedInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		
		if (interactionTarget.hasTag(Tags.HAS_INTERACTION_DIALOG)) {
			return new PluginPick<InteractionDialogPlugin>(new RuleBasedInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		
//		if ((interactionTarget instanceof OrbitalStationAPI ||
//				interactionTarget.hasTag("station")) ||
//				interactionTarget.getMarket() != null) {
		if (interactionTarget.getMarket() != null) {
			return new PluginPick<InteractionDialogPlugin>(new OrbitalStationInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		
		if (interactionTarget instanceof CampaignFleetAPI) {
			return new PluginPick<InteractionDialogPlugin>(new FleetInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		if (interactionTarget instanceof JumpPointAPI) {
			return new PluginPick<InteractionDialogPlugin>(new JumpPointInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		if (interactionTarget instanceof PlanetAPI) {
			return new PluginPick<InteractionDialogPlugin>(new PlanetInteractionDialogPluginImpl(), PickPriority.CORE_GENERAL);
		}
		return null;
	}
	
	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(Object param, SectorEntityToken interactionTarget) {
		return null;
	}
	

	@Override
	public PluginPick<AbilityAIPlugin> pickAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
		if (ability == null) return null;
		String id = ability.getId();
		if (id == null) return null;
		
		if (ability.getSpec().getAIPluginClass() != null) {
			return new PluginPick<AbilityAIPlugin>(ability.getSpec().getNewAIPluginInstance(ability), PickPriority.CORE_GENERAL);
		} else {
			return null;
		}
		
//		if (id.equals(Abilities.GO_DARK)) {
//			return new PluginPick<AbilityAIPlugin>(new GoDarkAbilityAI(ability, ai), PickPriority.CORE_GENERAL);
//		} else if (id.equals(Abilities.EMERGENCY_BURN)) {
//			return new PluginPick<AbilityAIPlugin>(new EmergencyBurnAbilityAI(ability, ai), PickPriority.CORE_GENERAL);
//		} else if (id.equals(Abilities.SENSOR_BURST)) {
//			return new PluginPick<AbilityAIPlugin>(new SensorBurstAbilityAI(ability, ai), PickPriority.CORE_GENERAL);
//		} else if (id.equals(Abilities.TRANSPONDER)) {
//			return new PluginPick<AbilityAIPlugin>(new TransponderAbilityAI(ability, ai), PickPriority.CORE_GENERAL);
//		}
//		return null;
	}

	
	
	
	public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent) {
		if (opponent instanceof CampaignFleetAPI) {
			return new PluginPick<BattleCreationPlugin>(new BattleCreationPluginImpl(), PickPriority.CORE_GENERAL);
		}
		return null;
	}
	
	
	public PluginPick<BattleAutoresolverPlugin> pickBattleAutoresolverPlugin(BattleAPI battle) {
		return new PluginPick<BattleAutoresolverPlugin>(
						new BattleAutoresolverPluginImpl(battle),
						PickPriority.CORE_GENERAL
				   );
	}
	

	@Override
	public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, String factionId) {
		if (action instanceof RepActions || action instanceof RepActionEnvelope) {
			return new PluginPick<ReputationActionResponsePlugin>(
							new CoreReputationPlugin(),
							PickPriority.CORE_GENERAL
					   );
		}
		return null;
	}
	
	@Override
	public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, PersonAPI person) {
		if (action instanceof RepActions || action instanceof RepActionEnvelope) {
			return new PluginPick<ReputationActionResponsePlugin>(
							new CoreReputationPlugin(),
							PickPriority.CORE_GENERAL
					   );
		}
		return null;
	}

	
	@Override
	public void updateEntityFacts(SectorEntityToken entity, MemoryAPI memory) {
		for (String tag : entity.getTags()) {
			memory.set("$tag:" + tag, true, 0);
		}
		
		String onOrAt = "on";
		if (entity.hasTag(Tags.STATION)) {
			onOrAt = "at";
		}
		memory.set("$onOrAt", onOrAt, 0);
		
		if (entity.getStarSystem() != null && 
				entity.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
			memory.set("$systemCutOffFromHyper", true, 0);
		}
		
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		memory.set("$id", entity.getId(), 0);
		
		memory.set("$transponderOn", entity.isTransponderOn(), 0);
		
		memory.set("$name", entity.getName(), 0);
		memory.set("$fullName", entity.getFullName(), 0);
		
		memory.set("$inHyperspace", entity.isInHyperspace(), 0);
		
		
		if (entity.getCustomEntityType() != null) {
			memory.set("$customType", entity.getCustomEntityType(), 0);
			
			if (entity.getCustomEntitySpec() != null) {
				CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
				memory.set("$nameInText", spec.getNameInText(), 0);
				memory.set("$shortName", spec.getShortName(), 0);
				memory.set("$isOrAre", spec.getIsOrAre(), 0);
				memory.set("$aOrAn", spec.getAOrAn(), 0);
			}
		}
		
		if (entity instanceof CampaignTerrainAPI) {
			CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
			CampaignTerrainPlugin plugin = terrain.getPlugin();
			memory.set("$terrainId", plugin.getSpec().getId(), 0);
		}
		
		
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			if (fleet.getAI() instanceof CampaignFleetAIAPI) {
				CampaignFleetAIAPI ai = (CampaignFleetAIAPI) fleet.getAI();
				EncounterOption option = ai.pickEncounterOption(null, playerFleet, true);
				BattleAPI b = fleet.getBattle();
				if (b != null && b.isPlayerInvolved()) {
					b.genCombined();
					option = b.getCombinedFor(fleet).getAI().pickEncounterOption(null, b.getCombinedFor(Global.getSector().getPlayerFleet()), true);
				}
				
				switch (option) {
				case DISENGAGE:
					memory.set("$relativeStrength", -1, 0);
					break;
				case ENGAGE:
					memory.set("$relativeStrength", 1, 0);
					break;
				case HOLD:
					memory.set("$relativeStrength", 0, 0);
					break;
				case HOLD_VS_STRONGER:
					memory.set("$weakerThanPlayerButHolding", true, 0);
					memory.set("$relativeStrength", -1, 0);
					break;
				}
				
				memory.set("$isHostile", ai.isHostileTo(playerFleet), 0);
				
				memory.set("$fleetPoints", fleet.getFleetPoints(), 0);
			}
			
			memory.set("$isStation", fleet.isStationMode(), 0); 
			
			memory.set("$supplies", fleet.getCargo().getSupplies(), 0);
			memory.set("$fuel", fleet.getCargo().getFuel(), 0);
			
			memory.set("$knowsWhoPlayerIs", fleet.knowsWhoPlayerIs(), 0);
			
			if (!playerFleet.isTransponderOn() && !memory.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF)) {
				memory.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF, true, 0);
			}
			
			if (!Misc.isPermaKnowsWhoPlayerIs(fleet) && playerFleet.isTransponderOn()) {
				memory.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true, 7f + (float) Math.random() * 7f);
			}
		}
		
		if (entity.getFaction() != null && !memory.contains("$isHostile")) {
			float rel = entity.getFaction().getRelationship(Factions.PLAYER);
			RepLevel level = RepLevel.getLevelFor(rel);
			if (level.isAtWorst(RepLevel.INHOSPITABLE)) {
				memory.set("$isHostile", false, 0);
			} else {
				memory.set("$isHostile", true, 0);
			}
		}
		
		
		MarketAPI market = entity.getMarket();
		if (market != null) {
//			for (MarketConditionAPI mc : market.getConditions()) {
//				memory.set("$mc:" + mc.getId(), true, 0);
//			}
			if (!market.isPlanetConditionMarketOnly()) {
				memory.set("$hasMarket", true, 0);
			}
			
			if (Misc.getStationFleet(market) != null) {
				memory.set("$hasStation", true, 0);
			}
			
			memory.set("$marketSize", market.getSize(), 0);
			memory.set("$stability", (int) market.getStabilityValue(), 0);
		}
		
		if (entity instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) entity;
			memory.set("$planetType", planet.getTypeId(), 0);
		}
	}
	
	public void updateMarketFacts(MarketAPI market, MemoryAPI memory) {
		if (market != null) {
			for (MarketConditionAPI mc : market.getConditions()) {
				memory.set("$mc:" + mc.getId(), true, 0);
			}
			for (Industry ind : market.getIndustries()) {
				memory.set("$ind:" + ind.getId(), true, 0);
			}
			
			memory.set("$id", market.getId(), 0);
			memory.set("$size", market.getSize(), 0);
			memory.set("$stability", (int) market.getStabilityValue(), 0);
			
			memory.set("$isSurveyed", market.getSurveyLevel() == SurveyLevel.FULL, 0);
			memory.set("$surveyLevel", market.getSurveyLevel().name(), 0);
			memory.set("$isPlanetConditionMarketOnly", market.isPlanetConditionMarketOnly(), 0);
			
			memory.set("$isHidden", market.isHidden(), 0);
			
			memory.set("$isPlayerOwned", market.isPlayerOwned(), 0);
			
			boolean hasRuins = false;
			if (Misc.hasRuins(market)) {
				memory.set("$hasRuins", true, 0);
				hasRuins = true;
			}
			
			//boolean ruinsExlored = memory.getBoolean("$ruinsExplored");
			
			memory.set("$hasUnexploredRuins", Misc.hasUnexploredRuins(market), 0);
			
			
			float suspicionLevel = computeSmugglingSuspicionLevel(market);
			memory.set(MemFlags.MEMORY_MARKET_SMUGGLING_SUSPICION_LEVEL, suspicionLevel, 0);
		}
	}
	
//	public static boolean hasUnexploredRuins(MarketAPI market) {
//		return market != null && market.isPlanetConditionMarketOnly() &&
//			hasRuins(market) && !market.getMemoryWithoutUpdate().getBoolean("$ruinsExplored");
//	}
//	public static boolean hasRuins(MarketAPI market) {
//		return market != null && 
//			   (market.hasCondition(Conditions.RUINS_SCATTERED) || 
//			   market.hasCondition(Conditions.RUINS_WIDESPREAD) ||
//			   market.hasCondition(Conditions.RUINS_EXTENSIVE) ||
//			   market.hasCondition(Conditions.RUINS_VAST));
//	}
	
	
	public static final float computeSmugglingSuspicionLevel(MarketAPI market) {
		float smugglingTotal = 0f;
		float tradeTotal = 0f;
		for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
			SubmarketPlugin plugin = submarket.getPlugin();
			if (!plugin.isParticipatesInEconomy()) continue;
			
			PlayerTradeDataForSubmarket tradeData =  SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);
			if (submarket.getFaction().isHostileTo(market.getFaction()) || submarket.getPlugin().isBlackMarket()) {
				smugglingTotal += tradeData.getTotalPlayerTradeValue();
			} else {
				tradeTotal += tradeData.getTotalPlayerTradeValue();
			}
		}
		
		float suspicionLevel = 0f;
		
		if (smugglingTotal + tradeTotal > 0) {
			suspicionLevel = smugglingTotal / (smugglingTotal + tradeTotal);
			
			//float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
			//float threshold = 30000f * playerImpactMult * market.getSize();
			float threshold = 10000f * market.getSize();
			suspicionLevel *= Math.min(1f, (smugglingTotal + tradeTotal) / threshold);
		}
		
		float extra = market.getMemoryWithoutUpdate().getFloat(MemFlags.MARKET_EXTRA_SUSPICION);
		suspicionLevel += extra;
		suspicionLevel *= Math.min(1f, suspicionLevel);
		
		return suspicionLevel;
	}
	
	public void updatePersonFacts(PersonAPI person, MemoryAPI memory) {
		memory.set("$id", person.getId(), 0);
		
		for (String tag : person.getTags()) {
			memory.set("$tag:" + tag, true, 0);
		}
		
		//int rel = (int)Math.round(person.getRelToPlayer().getRel() * 100f);
		float rel = person.getRelToPlayer().getRel();
		memory.set("$rel", rel, 0);
		
		if (Misc.isMercenary(person)) {
			memory.set("$mercContractDur", (int)Global.getSettings().getFloat("officerMercContractDur"), 0);
			memory.set("$mercContractDurStr", "" + (int)Global.getSettings().getFloat("officerMercContractDur"), 0);
		}
		
		memory.set("$isPerson", true, 0);
		memory.set("$name", person.getName().getFullName(), 0);
		memory.set("$personName", person.getName().getFullName(), 0);
		
		memory.set("$rankId", person.getRankId(), 0);
		memory.set("$postId", person.getPostId(), 0);
		
		if (person.isAICore()) {
			memory.set("$aiCoreId", person.getAICoreId(), 0);
			memory.set("$isAICore", true, 0);
		}
		
		memory.set("$rankAOrAn", person.getRankArticle(), 0);
		memory.set("$postAOrAn", person.getPostArticle(), 0);

		if (person.getRank() != null) {
			memory.set("$rank", person.getRank().toLowerCase(), 0);
			memory.set("$Rank", Misc.ucFirst(person.getRank()), 0);
		}
		if (person.getPost() != null) {
			memory.set("$post", person.getPost().toLowerCase(), 0);
			memory.set("$Post", Misc.ucFirst(person.getPost()), 0);
		}
		memory.set("$importance", person.getImportance().name(), 0);
		
		memory.set("$level", person.getStats().getLevel(), 0);
		
		memory.set("$personality", person.getPersonalityAPI().getId(), 0);
		
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		PersonDataAPI data = ip.getData(person);
		if (data != null) {
			if (data.getLocation().getMarket() != null) {
				MarketAPI market = data.getLocation().getMarket();
				memory.set("$hostileToMarket", person.getFaction().isHostileTo(market.getFaction()), 0);
			}
		}
	}

	@Override
	public void updateFactionFacts(FactionAPI faction, MemoryAPI memory) {
		memory.set("$id", faction.getId(), 0);
		
		float rel = faction.getRelationship(Factions.PLAYER);
		RepLevel level = RepLevel.getLevelFor(rel);
		if (level.isAtWorst(RepLevel.FAVORABLE)) {
			memory.set("$friendlyToPlayer", true, 0);
		} else if (level.isAtBest(RepLevel.HOSTILE)) {
			memory.set("$hostileToPlayer", true, 0);
		} else {
			memory.set("$neutralToPlayer", true, 0);
		}
		
		if (level.isAtWorst(RepLevel.INHOSPITABLE)) {
			memory.set("$isHostile", false, 0);
		} else {
			memory.set("$isHostile", true, 0);
		}
		
		if (JSONObject.getNames(faction.getCustom()) != null) {
			for (String key : JSONObject.getNames(faction.getCustom())) {
				String val = faction.getCustom().optString(key);
				memory.set("$c:" + key, val, 0);
			}
		}
		Color c = faction.getColor();
		memory.set("$color", c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha(), 0);
		
		c = faction.getBaseUIColor();
		memory.set("$baseColor", c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha(), 0);
		
		c = faction.getBrightUIColor();
		memory.set("$brightColor", c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha(), 0);
		
		c = faction.getDarkUIColor();
		memory.set("$darkColor", c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha(), 0);
		
		c = faction.getGridUIColor();
		memory.set("$gridColor", c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha(), 0);
		
		
		memory.set("$isNeutralFaction", faction.isNeutralFaction(), 0);
		
		memory.set("$relValue", rel, 0);
		memory.set("$rel", level.name(), 0);
		//memory.set("$relAdjective", level.getDisplayName().toLowerCase(), 0);
	}

	@Override
	public void updateGlobalFacts(MemoryAPI memory) {
		if (Global.getSettings().isDevMode()) {
			memory.set("$isDevMode", true, 0);
		}
		if (TutorialMissionIntel.isTutorialInProgress()) {
			memory.set("$isInTutorial", true, 0);	
		}
		
		if (!memory.getBoolean(GateEntityPlugin.PLAYER_CAN_USE_GATES)) {
			memory.set(GateEntityPlugin.PLAYER_CAN_USE_GATES, GateEntityPlugin.canUseGates(), 0);
		}
		
		memory.set("$daysSinceStart", PirateBaseManager.getInstance().getUnadjustedDaysSinceStart(), 0);	
	}

	@Override
	public void updatePlayerFacts(MemoryAPI memory) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

		PersonAPI person = Global.getSector().getPlayerPerson();
		memory.set("$firstName", person.getName().getFirst(), 0);
		memory.set("$lastName", person.getName().getLast(), 0);
		memory.set("$name", person.getName().getFullName(), 0);
		
		
		
		if (fleet.getContainingLocation() != null) {
			memory.set("$locationId", fleet.getContainingLocation().getId(), 0);
			
			for (String tag : fleet.getContainingLocation().getTags()) {
				memory.set("$locTag:" + tag, true, 0);	
			}
		}
		
		
		
		
		memory.set("$fleetId", fleet.getId(), 0);
		
		memory.set("$transponderOn", fleet.isTransponderOn(), 0);
		
		memory.set("$supplies", (int)fleet.getCargo().getSupplies(), 0);
		memory.set("$fuel", (int)fleet.getCargo().getFuel(), 0);
		memory.set("$machinery", (int)fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY), 0);
		
		
		memory.set("$marines", (int)fleet.getCargo().getMarines(), 0);
		
		memory.set("$crew", (int)fleet.getCargo().getCrew(), 0);
		
		memory.set("$crewRoom", (int)(fleet.getCargo().getMaxPersonnel() - fleet.getCargo().getTotalPersonnel()), 0);
		memory.set("$fuelRoom", (int)fleet.getCargo().getMaxFuel() - (int)fleet.getCargo().getFuel(), 0);
		memory.set("$cargoRoom", (int)fleet.getCargo().getMaxCapacity() - (int)fleet.getCargo().getSpaceUsed(), 0);
		
		memory.set("$crewRoomStr", Misc.getWithDGS((int)(fleet.getCargo().getMaxPersonnel() - fleet.getCargo().getTotalPersonnel())), 0);
		memory.set("$fuelRoomStr", Misc.getWithDGS((int)fleet.getCargo().getMaxFuel() - (int)fleet.getCargo().getFuel()), 0);
		memory.set("$cargoRoomStr", Misc.getWithDGS((int)fleet.getCargo().getMaxCapacity() - (int)fleet.getCargo().getSpaceUsed()), 0);
		
		memory.set("$credits", (int)fleet.getCargo().getCredits().get(), 0);
		memory.set("$creditsStr", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()), 0);
		memory.set("$creditsStrC", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()) + Strings.C, 0);
		
//		for (CommoditySpecAPI spec : Global.getSettings().getAllCommoditySpecs()) {
//			if (spec.isMeta()) continue;
//		}
		Set<String> seen = new HashSet<String>();
		for (CargoStackAPI stack : fleet.getCargo().getStacksCopy()) {
			String id = stack.getCommodityId();
			if (id == null) continue;
			if (seen.contains(id)) continue;
			seen.add(id);
			
			int quantity = (int) fleet.getCargo().getCommodityQuantity(id);
			String key = "$" + id;
			
			if (!memory.contains(key)) {
				memory.set(key, quantity, 0);
			}
		}
		
		
		MonthlyReport report = SharedData.getData().getPreviousReport();
		boolean debt = report.getDebt() > 0;
		boolean longDebt = report.getDebt() > 0 && report.getPreviousDebt() > 0;
		memory.set("$inDebt", debt, 0);
		memory.set("$inLongDebt", longDebt, 0);
		
		int fleetSizeCount = fleet.getFleetSizeCount();
		int maxSize = 0;
		int maxCombatSize = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			HullSize size = member.getHullSpec().getHullSize();
			int val = 1;
			switch (size) {
			case CAPITAL_SHIP:
				val = 4;
				break;
			case CRUISER:
				val = 3;
				break;
			case DESTROYER:
				val = 2;
				break;
			case FIGHTER:
			case FRIGATE:
			case DEFAULT:
				val = 1;
				break;
			}
			if (val > maxSize) {
				maxSize = val;;
			}
			if (val > maxCombatSize && !member.isCivilian()) {
				maxCombatSize = val;
			}
		}
		
		memory.set("$maxHullSize", maxSize, 0);
		memory.set("$maxCombatHullSize", maxCombatSize, 0);
		
		memory.set("$fleetSizeCount", fleetSizeCount, 0);
		memory.set("$numShips", fleet.getFleetData().getMembersListCopy().size(), 0);
		memory.set("$fleetPoints", fleet.getFleetPoints(), 0);
		
		if (fleet.getFlagship() != null) {
			memory.set("$flagshipName", fleet.getFlagship().getShipName(), 0);
		}
		
		for (String id : fleet.getAbilities().keySet()) {
			memory.set("$ability:" + id, true, 0);
		}
	}

	@Override
	public PluginPick<FleetStubConverterPlugin> pickStubConverter(CampaignFleetAPI fleet) {
		return new PluginPick<FleetStubConverterPlugin>(new FleetStubConverterPluginImpl(), PickPriority.CORE_GENERAL);
	}

	@Override
	public PluginPick<FleetStubConverterPlugin> pickStubConverter(FleetStubAPI stub) {
//		if (stub != null && stub.getParams() instanceof FleetParams) {
//			return new PluginPick<FleetStubConverterPlugin>(new FleetStubConverterPluginImpl(), PickPriority.CORE_GENERAL);
//		}
		return null;
	}
	
	
	public PluginPick<AutofitPlugin> pickAutofitPlugin(FleetMemberAPI member) {
		PersonAPI commander = null;
		if (member != null) {
			commander = member.getFleetCommanderForStats();
			if (commander == null) commander = member.getFleetCommander();
		}
		return new PluginPick<AutofitPlugin>(new CoreAutofitPlugin(commander), PickPriority.CORE_GENERAL);
	}
	

	public PluginPick<InteractionDialogPlugin> pickRespawnPlugin() {
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CampaignTutorialScript.USE_TUTORIAL_RESPAWN)) {
			return new PluginPick<InteractionDialogPlugin>(new TutorialRespawnDialogPluginImpl(), PickPriority.MOD_SPECIFIC);
		}
		//return new PluginPick<InteractionDialogPlugin>(new StandardRespawnDialogPluginImpl(), PickPriority.CORE_GENERAL);
		return null;
	}
	
	
	@Override
	public PluginPick<ImmigrationPlugin> pickImmigrationPlugin(MarketAPI market) {
		return new PluginPick<ImmigrationPlugin>(new CoreImmigrationPluginImpl(market), PickPriority.CORE_GENERAL);
	}
	
	public PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String commodityId) {
		if (Commodities.ALPHA_CORE.equals(commodityId)) {
			return new PluginPick<AICoreAdminPlugin>(new AICoreAdminPluginImpl(), PickPriority.CORE_GENERAL);
		}
		return null;
	}
	
	public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
		if (Commodities.OMEGA_CORE.equals(commodityId) ||
				Commodities.ALPHA_CORE.equals(commodityId) ||
				Commodities.BETA_CORE.equals(commodityId) ||
				Commodities.GAMMA_CORE.equals(commodityId)) {
			return new PluginPick<AICoreOfficerPlugin>(new AICoreOfficerPluginImpl(), PickPriority.CORE_GENERAL);
		}
		return null;
	}
	
	public PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params) {
		if (params instanceof DefaultFleetInflaterParams) {
			DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) params;
			return new PluginPick<FleetInflater>(new DefaultFleetInflater(p), PickPriority.CORE_GENERAL);
		}
		return null;
	}
	
}








