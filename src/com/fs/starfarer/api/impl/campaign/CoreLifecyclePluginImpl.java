package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction.TransactionLineItem;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.listeners.CoreDiscoverEntityPlugin;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.TowCable.TowCableBuff;
import com.fs.starfarer.api.impl.campaign.abilities.BaseAbilityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallAbility;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallResponseAssignmentAI;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallResponsePirateAssignmentAI;
import com.fs.starfarer.api.impl.campaign.abilities.EmergencyBurnAbility;
import com.fs.starfarer.api.impl.campaign.abilities.FractureJumpAbility;
import com.fs.starfarer.api.impl.campaign.abilities.GoDarkAbility;
import com.fs.starfarer.api.impl.campaign.abilities.GraviticScanAbility;
import com.fs.starfarer.api.impl.campaign.abilities.GraviticScanData;
import com.fs.starfarer.api.impl.campaign.abilities.InterdictionPulseAbility;
import com.fs.starfarer.api.impl.campaign.abilities.RemoteSurveyAbility;
import com.fs.starfarer.api.impl.campaign.abilities.ScavengeAbility;
import com.fs.starfarer.api.impl.campaign.abilities.SensorBurstAbility;
import com.fs.starfarer.api.impl.campaign.abilities.SustainedBurnAbility;
import com.fs.starfarer.api.impl.campaign.abilities.TransponderAbility;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallAbility.AbilityUseData;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallAbility.DistressCallOutcome;
import com.fs.starfarer.api.impl.campaign.abilities.DistressCallAbility.DistressResponseData;
import com.fs.starfarer.api.impl.campaign.abilities.GraviticScanData.GSPing;
import com.fs.starfarer.api.impl.campaign.abilities.InterdictionPulseAbility.IPReactionScript;
import com.fs.starfarer.api.impl.campaign.abilities.ai.BaseAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.EmergencyBurnAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.GoDarkAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.InterdictionPulseAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.SensorBurstAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.SustainedBurnAbilityAI;
import com.fs.starfarer.api.impl.campaign.abilities.ai.TransponderAbilityAI;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.AbandonedStation;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.CottageIndustry;
import com.fs.starfarer.api.impl.campaign.econ.Decivilized;
import com.fs.starfarer.api.impl.campaign.econ.Dissident;
import com.fs.starfarer.api.impl.campaign.econ.FoodShortage;
import com.fs.starfarer.api.impl.campaign.econ.FreeMarket;
import com.fs.starfarer.api.impl.campaign.econ.Frontier;
import com.fs.starfarer.api.impl.campaign.econ.Headquarters;
import com.fs.starfarer.api.impl.campaign.econ.Hydroponics;
import com.fs.starfarer.api.impl.campaign.econ.LargeRefugeePopulation;
import com.fs.starfarer.api.impl.campaign.econ.LuddicMajority;
import com.fs.starfarer.api.impl.campaign.econ.OrbitalBurns;
import com.fs.starfarer.api.impl.campaign.econ.OrganizedCrime;
import com.fs.starfarer.api.impl.campaign.econ.Outpost;
import com.fs.starfarer.api.impl.campaign.econ.Population;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.econ.RegionalCapital;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.econ.RuralPolity;
import com.fs.starfarer.api.impl.campaign.econ.ShipbreakingCenter;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.econ.Smuggling;
import com.fs.starfarer.api.impl.campaign.econ.StealthMinefields;
import com.fs.starfarer.api.impl.campaign.econ.SystemBounty;
import com.fs.starfarer.api.impl.campaign.econ.TradeCenter;
import com.fs.starfarer.api.impl.campaign.econ.TradeDisruption;
import com.fs.starfarer.api.impl.campaign.econ.UrbanizedPolity;
import com.fs.starfarer.api.impl.campaign.econ.ViceDemand;
import com.fs.starfarer.api.impl.campaign.econ.WorldArid;
import com.fs.starfarer.api.impl.campaign.econ.WorldBarrenMarginal;
import com.fs.starfarer.api.impl.campaign.econ.WorldDesert;
import com.fs.starfarer.api.impl.campaign.econ.WorldIce;
import com.fs.starfarer.api.impl.campaign.econ.WorldJungle;
import com.fs.starfarer.api.impl.campaign.econ.WorldTerran;
import com.fs.starfarer.api.impl.campaign.econ.WorldTundra;
import com.fs.starfarer.api.impl.campaign.econ.WorldTwilight;
import com.fs.starfarer.api.impl.campaign.econ.WorldUninhabitable;
import com.fs.starfarer.api.impl.campaign.econ.WorldWater;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryorevival;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.econ.impl.FuelProduction;
import com.fs.starfarer.api.impl.campaign.econ.impl.GroundDefenses;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.LightIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.LionsGuardHQ;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.econ.impl.Mining;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.econ.impl.Refining;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.econ.impl.Spaceport;
import com.fs.starfarer.api.impl.campaign.econ.impl.TechMining;
import com.fs.starfarer.api.impl.campaign.econ.impl.Waystation;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.impl.campaign.events.CoreEventProbabilityManager;
import com.fs.starfarer.api.impl.campaign.events.FactionHostilityEvent;
import com.fs.starfarer.api.impl.campaign.events.FoodShortageEvent;
import com.fs.starfarer.api.impl.campaign.events.InvestigationEventGoodRepWithOther;
import com.fs.starfarer.api.impl.campaign.events.InvestigationEventSmugglingV2;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.PriceUpdate;
import com.fs.starfarer.api.impl.campaign.events.RecentUnrestEvent;
import com.fs.starfarer.api.impl.campaign.events.RepTrackerEvent;
import com.fs.starfarer.api.impl.campaign.events.TradeInfoUpdateEvent;
import com.fs.starfarer.api.impl.campaign.events.FactionHostilityEvent.FactionHostilityPairKey;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.AvailableOfficer;
import com.fs.starfarer.api.impl.campaign.events.RepTrackerEvent.FactionTradeRepData;
import com.fs.starfarer.api.impl.campaign.events.nearby.DistressCallNormalAssignmentAI;
import com.fs.starfarer.api.impl.campaign.events.nearby.DistressCallPirateAmbushAssignmentAI;
import com.fs.starfarer.api.impl.campaign.events.nearby.DistressCallPirateAmbushTrapAssignmentAI;
import com.fs.starfarer.api.impl.campaign.events.nearby.NearbyEventsEvent;
import com.fs.starfarer.api.impl.campaign.events.nearby.NearbyEventsEvent.NESpawnData;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.CustomFleets;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableAggroAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableLuddicPathFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposablePirateFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParams;
import com.fs.starfarer.api.impl.campaign.fleets.MercAssignmentAIV2;
import com.fs.starfarer.api.impl.campaign.fleets.MercFleetManagerV2;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManagerV2;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.BaseLimitedFleetManager.ManagedFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManager.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager.SeededFleet;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.intel.AnalyzeEntityIntelCreator;
import com.fs.starfarer.api.impl.campaign.intel.AnalyzeEntityMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityIntel;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityManager;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyManager;
import com.fs.starfarer.api.impl.campaign.intel.ProcurementMissionCreator;
import com.fs.starfarer.api.impl.campaign.intel.ProcurementMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.SurveyPlanetIntelCreator;
import com.fs.starfarer.api.impl.campaign.intel.SurveyPlanetMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel.MissionResult;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.CorruptPLClerkSuppliesBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.CorruptPLClerkSuppliesBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryFailureConsequences;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DiktatLobsterBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DiktatLobsterBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.LuddicCraftBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.LuddicCraftBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.LuddicFarmerBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.LuddicFarmerBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.MercsOnTheRunBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.MercsOnTheRunBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PirateBaseRumorBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PlanetaryShieldBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.QuartermasterCargoSwapBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.QuartermasterCargoSwapBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.ScientistAICoreBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.ScientistAICoreBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachLoanBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachLoanBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachLoanIncentiveScript;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachMajorLoanBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachMajorLoanBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCells;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateActivity;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateActivityIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PlayerRelatedPirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.BaseBountyData;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker.MarketDecivData;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionManager;
import com.fs.starfarer.api.impl.campaign.intel.misc.DistressCallIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.TradeFleetDepartureIntel;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager;
import com.fs.starfarer.api.impl.campaign.missions.BaseCampaignMission;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantStationInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.DomainSurveyDerelictSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial.ExtraSalvage;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial.BreadcrumbSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.CargoManifestSpecial.CargoManifestSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.DomainSurveyDerelictSpecial.DomainSurveyDerelictSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.DomainSurveyDerelictSpecial.SpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SleeperPodsSpecial.SleeperPodsSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SleeperPodsSpecial.SleeperSpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial.TransmitterTrapSpecialData;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeProfitabilityData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.shared.CommodityStatTracker.CommodityStats;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeProfitabilityData.CommodityData;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker.ReputationChangeData;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.LocalResourcesSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidImpact;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidSource;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.CRLossPerSecondBuff;
import com.fs.starfarer.api.impl.campaign.terrain.CRRecoveryBuff;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.EventHorizonPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager;
import com.fs.starfarer.api.impl.campaign.terrain.HyperStormBoost;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MaxBurnBuff;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PeakPerformanceBuff;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.RadioChatterTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.SlipstreamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaAkaMainyuTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin.AsteroidBeltParams;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager.Flare;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.CellStateTracker;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.RadioChatterTerrainPlugin.RadioChatterParams;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams;
import com.fs.starfarer.api.impl.campaign.tutorial.CampaignTutorialScript;
import com.fs.starfarer.api.impl.campaign.tutorial.GalatiaMarketScript;
import com.fs.starfarer.api.impl.campaign.tutorial.GalatianAcademyStipend;
import com.fs.starfarer.api.impl.campaign.tutorial.RogueMinerMiscFleetManager;
import com.fs.starfarer.api.impl.campaign.tutorial.SaveNagScript;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialLeashAssignmentAI;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.impl.campaign.tutorial.CampaignTutorialScript.CampaignTutorialStage;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionEvent.TutorialMissionStage;
import com.fs.starfarer.api.loading.CampaignPingSpec;
import com.fs.starfarer.api.plugins.impl.CoreBuildObjectiveTypePicker;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.FlickerUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.RollingAverageTracker;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.TimeoutTracker.ItemData;
import com.thoughtworks.xstream.XStream;

public class CoreLifecyclePluginImpl extends BaseModPlugin {

	@Override
	public void onGameLoad(boolean newGame) {
		econPostSaveRestore();
		
		// the token replacement generators don't get saved
		// add them on every game load
		Global.getSector().getRules().addTokenReplacementGenerator(new CoreRuleTokenReplacementGeneratorImpl());
		
		if (!newGame) {
			addJunk();
			regenAsteroids();
		}
		
		addScriptsIfNeeded();
		
		verifyFactionData();
	}
	
	public static void verifyFactionData() {
		for (FactionAPI faction : Global.getSector().getAllFactions()) {
			verifyFactionData(faction);
		}
	}
	
	public static void verifyFactionData(FactionAPI faction) {
		for (String id : faction.getKnownShips()) {
			if (Global.getSettings().getHullSpec(id) == null) {
				throw new RuntimeException("Hull with id [" + id + "] not found");
			}
		}
		for (String id : faction.getKnownHullMods()) {
			if (Global.getSettings().getHullModSpec(id) == null) {
				throw new RuntimeException("Hullmod with id [" + id + "] not found");
			}
		}
		for (String id : faction.getKnownIndustries()) {
			if (Global.getSettings().getIndustrySpec(id) == null) {
				throw new RuntimeException("Industry with id [" + id + "] not found");
			}
		}
		for (String id : faction.getKnownFighters()) {
			if (Global.getSettings().getFighterWingSpec(id) == null) {
				throw new RuntimeException("Fighter wing with id [" + id + "] not found");
			}
		}
		for (String id : faction.getKnownWeapons()) {
			if (Global.getSettings().getWeaponSpec(id) == null) {
				throw new RuntimeException("Weapon with id [" + id + "] not found");
			}
		}
	}
	
	protected void addScriptsIfNeeded() {
		ShipQuality.getInstance();
		//ConditionManager.getInstance();
		
		SectorAPI sector = Global.getSector();
		GenericPluginManagerAPI plugins = sector.getGenericPlugins();
		if (!plugins.hasPlugin(SalvageDefenderModificationPluginImpl.class)) {
			plugins.addPlugin(new SalvageDefenderModificationPluginImpl(), true);
		}
		if (!plugins.hasPlugin(CoreDiscoverEntityPlugin.class)) {
			plugins.addPlugin(new CoreDiscoverEntityPlugin(), true);
		}
		if (!plugins.hasPlugin(CoreBuildObjectiveTypePicker.class)) {
			plugins.addPlugin(new CoreBuildObjectiveTypePicker(), true);
		}
		if (!plugins.hasPlugin(AbandonMarketPluginImpl.class)) {
			plugins.addPlugin(new AbandonMarketPluginImpl(), true);
		}
		if (!plugins.hasPlugin(StabilizeMarketPluginImpl.class)) {
			plugins.addPlugin(new StabilizeMarketPluginImpl(), true);
		}
		
		
		if (!sector.hasScript(WarSimScript.class)) {
			sector.addScript(new WarSimScript());
		}
		if (!sector.hasScript(PersonBountyManager.class)) {
			sector.addScript(new PersonBountyManager());
		}
		if (!sector.hasScript(SystemBountyManager.class)) {
			sector.addScript(new SystemBountyManager());
		}
		
		if (!sector.hasScript(PirateBaseManager.class)) {
			sector.addScript(new PirateBaseManager());
		}
		if (!sector.hasScript(PlayerRelatedPirateBaseManager.class)) {
			sector.addScript(new PlayerRelatedPirateBaseManager());
		}
		
		if (!sector.hasScript(LuddicPathBaseManager.class)) {
			sector.addScript(new LuddicPathBaseManager());
		}
		if (!sector.hasScript(HegemonyInspectionManager.class)) {
			sector.addScript(new HegemonyInspectionManager());
		}
		if (!sector.hasScript(PunitiveExpeditionManager.class)) {
			sector.addScript(new PunitiveExpeditionManager());
		}
		if (!sector.hasScript(DecivTracker.class)) {
			sector.addScript(new DecivTracker());
		}
		
		if (!sector.hasScript(FactionHostilityManager.class)) {
			sector.addScript(new FactionHostilityManager());
			
			FactionHostilityManager.getInstance().startHostilities(Factions.HEGEMONY, Factions.TRITACHYON);
			FactionHostilityManager.getInstance().startHostilities(Factions.HEGEMONY, Factions.PERSEAN);
			FactionHostilityManager.getInstance().startHostilities(Factions.TRITACHYON, Factions.LUDDIC_CHURCH);
		}
		
		
		if (!sector.hasScript(GenericMissionManager.class)) {
			sector.addScript(new GenericMissionManager());
		}
		GenericMissionManager manager = GenericMissionManager.getInstance();
		if (!manager.hasMissionCreator(ProcurementMissionCreator.class)) {
			manager.addMissionCreator(new ProcurementMissionCreator());
		}
		if (!manager.hasMissionCreator(AnalyzeEntityIntelCreator.class)) {
			manager.addMissionCreator(new AnalyzeEntityIntelCreator());
		}
		if (!manager.hasMissionCreator(SurveyPlanetIntelCreator.class)) {
			manager.addMissionCreator(new SurveyPlanetIntelCreator());
		}
		
		addBarEvents();
		
		if (!sector.hasScript(SmugglingScanScript.class)) {
			sector.addScript(new SmugglingScanScript());
		}
		
	}
	
	protected void addBarEvents() {
		SectorAPI sector = Global.getSector();
		if (!sector.hasScript(PortsideBarData.class)) {
			sector.addScript(new PortsideBarData());
		}
		if (!sector.hasScript(BarEventManager.class)) {
			sector.addScript(new BarEventManager());
		}
		
		BarEventManager bar = BarEventManager.getInstance();
		if (!bar.hasEventCreator(LuddicFarmerBarEventCreator.class)) {
			bar.addEventCreator(new LuddicFarmerBarEventCreator());
		}
		if (!bar.hasEventCreator(LuddicCraftBarEventCreator.class)) {
			bar.addEventCreator(new LuddicCraftBarEventCreator());
		}
		if (!bar.hasEventCreator(DiktatLobsterBarEventCreator.class)) {
			bar.addEventCreator(new DiktatLobsterBarEventCreator());
		}
		if (!bar.hasEventCreator(MercsOnTheRunBarEventCreator.class)) {
			bar.addEventCreator(new MercsOnTheRunBarEventCreator());
		}
		if (!bar.hasEventCreator(CorruptPLClerkSuppliesBarEventCreator.class)) {
			bar.addEventCreator(new CorruptPLClerkSuppliesBarEventCreator());
		}
		if (!bar.hasEventCreator(QuartermasterCargoSwapBarEventCreator.class)) {
			bar.addEventCreator(new QuartermasterCargoSwapBarEventCreator());
		}
		if (!bar.hasEventCreator(TriTachLoanBarEventCreator.class)) {
			bar.addEventCreator(new TriTachLoanBarEventCreator());
		}
		if (!bar.hasEventCreator(TriTachMajorLoanBarEventCreator.class)) {
			bar.addEventCreator(new TriTachMajorLoanBarEventCreator());
		}
		if (!bar.hasEventCreator(ScientistAICoreBarEventCreator.class)) {
			bar.addEventCreator(new ScientistAICoreBarEventCreator());
		}
		if (!bar.hasEventCreator(DeliveryBarEventCreator.class)) {
			bar.addEventCreator(new DeliveryBarEventCreator());
		}
		if (!bar.hasEventCreator(PlanetaryShieldBarEventCreator.class)) {
			bar.addEventCreator(new PlanetaryShieldBarEventCreator());
		}
		
	}

	@Override
	public void onNewGame() {
		junkList.clear();
	}
	
	
	@Override
	public void onNewGameAfterTimePass() {
		new CustomFleets().spawn();

		
		EveryFrameScript script = new AnalyzeEntityIntelCreator().createMissionIntel();
		if (script instanceof BaseIntelPlugin) {
			((BaseIntelPlugin)script).setPostingLocation(null);
			GenericMissionManager.getInstance().addActive(script);
		}
		
		script = new SurveyPlanetIntelCreator().createMissionIntel();
		if (script instanceof BaseIntelPlugin) {
			((BaseIntelPlugin)script).setPostingLocation(null);
			GenericMissionManager.getInstance().addActive(script);
		}
		
			
		
		for (EveryFrameScript s : PersonBountyManager.getInstance().getActive()) {
			PersonBountyIntel intel = (PersonBountyIntel) s;
			intel.setElapsedDays(intel.getElapsedDays() * (float) Math.random() * 0.25f);
		}
		
		// only leave bounties at ancyra, jangala, and one other market at game start
		boolean first = true;
		for (EveryFrameScript s : SystemBountyManager.getInstance().getActive()) {
			SystemBountyIntel intel = (SystemBountyIntel) s;
			if (intel.getMarket().getId().equals("ancyra_market") ||
					intel.getMarket().getId().equals("jangala")) {
				intel.setElapsedDays(intel.getElapsedDays() * (float) Math.random() * 0.25f);
				continue;
			}
			
			if (first) {
				first = false;
				intel.setElapsedDays(intel.getElapsedDays() * (float) Math.random() * 0.25f);
				continue;
			}
			
			intel.endImmediately();
		}
		
		
		MarketAPI jangala = Global.getSector().getEconomy().getMarket("jangala");
		if (jangala != null) {
			SystemBountyManager.getInstance().addOrResetBounty(jangala);
		}
		
		
//		CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
		

		
//		SectorEntityToken jangalaPlanet = Global.getSector().getEntityById("jangala");
//		Global.getSector().getMissionBoard().makeAvailableAt(new TestCampaignMission(jangalaPlanet), "jangala");
//		FactionAPI hegemony = Global.getSector().getFaction(Factions.HEGEMONY);
//		jangalaPlanet.initCommDirectory();
//		jangalaPlanet.getCommDirectory().addPerson(hegemony.createRandomPerson());
//		jangalaPlanet.getCommDirectory().addPerson(hegemony.createRandomPerson());
//		jangalaPlanet.getCommDirectory().addPerson(hegemony.createRandomPerson());
//		jangalaPlanet.getCommDirectory().addPerson(hegemony.createRandomPerson());

//		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			//if (market.getFactionId().equals(Factions.PIRATES)) {
//			if (market.getFaction().getCustom().optBoolean(Factions.CUSTOM_EXEMPT_FOOD_SHORTAGES)) {
//				continue;
//			}
//			EventProbabilityAPI ep = eventManager.getProbability(Events.FOOD_SHORTAGE, market);
//			if (eventManager.isOngoing(ep)) continue;
//			if (ep.getProbability() < 0.05f) continue;
//			
//			picker.add(market, ep.getProbability());
//		}
//		
//		MarketAPI pick = picker.pick();
//		if (pick != null) {
//			eventManager.startEvent(new CampaignEventTarget(pick), Events.FOOD_SHORTAGE, null);
//		}

//		if (Global.getSettings().getBoolean("runDefaultEasyStartScript") && Misc.isEasy()) {
//			easyStart();
//		}
		
//		SectorAPI sector = Global.getSector();  
//		if (Global.getSector().isInNewGameAdvance()) return;  
//
//		// teleport player to start on top of the planet  
//		SectorEntityToken entity = sector.getEntityById("culann");  
//		CampaignFleetAPI playerFleet = sector.getPlayerFleet();  
//		Vector2f loc = entity.getLocation();  
//		playerFleet.setLocation(loc.x, loc.y);  
	}
	
//	protected void easyStart() {
//		PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getPlayerFaction(), 1, true);
//		officer.setPersonality(Personalities.CAUTIOUS);
//		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//		playerFleet.getFleetData().addOfficer(officer);
//		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
//			if (!member.isFlagship() && member.getCaptain().isDefault()) {
//				member.setCaptain(officer);
//				break;
//			}
//		}
//	}

	@Override
	public void onNewGameAfterEconomyLoad() {
		//SharedData.getData().getMarketsWithoutPatrolSpawn().add("jangala");
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			SharedData.getData().getMarketsWithoutPatrolSpawn().add(market.getId());
//		}

		addJunk();
		
		// Do not need to regen asteroids - they've already been generated during sector generation
		// Only need to regenerate on game load, since they're (mostly) not persisted.
		//regenAsteroids();
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			//market.getCommDirectory().addMissionBoard();
			
			if (market.getContainingLocation() != null && market.getPrimaryEntity() != null) {
				float radius = 300f + market.getSize() * 200f;
				market.getContainingLocation().addRadioChatter(market.getPrimaryEntity(), radius);
			}
		}
		
		createInitialPeople();
		
		addScriptsIfNeeded();
		
		updateKnownPlanets();
		
		//addBaseBlueprints();
	}
	
//	protected void addBaseBlueprints() {
//		
//		Set<String> tags = new HashSet<String>();
//		tags.add(Items.TAG_BASE_BP);
//		
//		List<String> ships = MultiBlueprintItemPlugin.getShipIds(tags);
//		List<String> weapons = MultiBlueprintItemPlugin.getWeaponIds(tags);
//		List<String> fighters = MultiBlueprintItemPlugin.getWingIds(tags);
//		
//		FactionAPI pf = Global.getSector().getPlayerFaction();
//		for (String id : ships) {
//			pf.addKnownShip(id);
//		}
//		for (String id : weapons) {
//			pf.addKnownWeapon(id);
//		}
//		for (String id : fighters) {
//			pf.addKnownFighter(id);
//		}
//	}

	protected void updateKnownPlanets() {
		//Set<String> seen = new HashSet<String>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isPlanetConditionMarketOnly()) continue; // shouldn't be in the markets list in this case, but, well.
			if (market.getContainingLocation() instanceof StarSystemAPI) {
				StarSystemAPI system = (StarSystemAPI) market.getContainingLocation();
				system.setEnteredByPlayer(true);
				//String name = system.getName();
				//Misc.setAllPlanetsKnown(system);
				Misc.setAllPlanetsSurveyed(system);
				market.setSurveyLevel(SurveyLevel.FULL); // could also be a station, not a planet
			}
		}
		
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (market.getContainingLocation() instanceof StarSystemAPI) {
//				StarSystemAPI system = (StarSystemAPI) market.getContainingLocation();
//				CoreScript.markSystemAsEntered(system, false);
//			}
//		}
		
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
//			if (system.getName().toLowerCase().contains("galatia")) {
//				System.out.println("wfwefwe234234");
//			}
			if (system.getTags().isEmpty()) {
				boolean galatia = system.getBaseName().toLowerCase().equals("galatia");
				if (Misc.getMarketsInLocation(system).isEmpty() && !galatia) {
					system.addTag(Tags.THEME_CORE_UNPOPULATED);
				} else {
					system.addTag(Tags.THEME_CORE_POPULATED);
				}
			}
		}
	}
	
	
	public static void addJunk(MarketAPI market) {
		SectorEntityToken entity = market.getPrimaryEntity();
		if (entity == null) return;
		LocationAPI location = entity.getContainingLocation();
		if (location == null) return;
		
		int numJunk = 5 + market.getSize() * 5;
		if (market.getSize() < 5) {
			numJunk = (int) Math.max(1, numJunk * 0.5f);
		}
		float radius = entity.getRadius() + 100f;
		float minOrbitDays = radius / 20;
		float maxOrbitDays = minOrbitDays + 10f;
		
		location.addOrbitalJunk(entity,
				 "orbital_junk", // from custom_entities.json 
				 numJunk, // num of junk
				 12, 20, // min/max sprite size (assumes square)
				 radius, // orbit radius
				 //70, // orbit width
				 110, // orbit width
				 minOrbitDays, // min orbit days
				 maxOrbitDays, // max orbit days
				 60f, // min spin (degress/day)
				 360f); // max spin (degrees/day)
	}
	
	protected void addJunk() {
		junkList.clear();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			addJunk(market);
		}
		
		for (SectorEntityToken planet : Global.getSector().getEntitiesWithTag(Tags.PLANET)) {
			LocationAPI location = planet.getContainingLocation();
			if (location == null) continue;
			
			if (planet.getMarket() == null || !planet.getMarket().isPlanetConditionMarketOnly()) continue;
			
			boolean r1 = planet.getMarket().hasCondition(Conditions.RUINS_SCATTERED);
			boolean r2 = planet.getMarket().hasCondition(Conditions.RUINS_WIDESPREAD);
			boolean r3 = planet.getMarket().hasCondition(Conditions.RUINS_EXTENSIVE);
			boolean r4 = planet.getMarket().hasCondition(Conditions.RUINS_VAST);
			
			if (!(r1 || r2 || r3 || r4)) continue;
			
			int numJunk = 5;
			if (r2) numJunk += 5;
			if (r3) numJunk += 15;
			if (r4) numJunk += 40;
			
			//System.out.println("With ruins: " + planet.getName() + ", " + location.getNameWithLowercaseType());
			
			float radius = planet.getRadius() + 100f;
			float minOrbitDays = radius / 20;
			float maxOrbitDays = minOrbitDays + 10f;
			
			location.addOrbitalJunk(planet,
					 "orbital_junk", // from custom_entities.json 
					 numJunk, // num of junk
					 12, 20, // min/max sprite size (assumes square)
					 radius, // orbit radius
					 //70, // orbit width
					 110, // orbit width
					 minOrbitDays, // min orbit days
					 maxOrbitDays, // max orbit days
					 60f, // min spin (degress/day)
					 360f); // max spin (degrees/day)
		}
	}
	
	protected void regenAsteroids() {
		for (LocationAPI loc : Global.getSector().getAllLocations()) {
			for (CampaignTerrainAPI terrain : loc.getTerrainCopy()) {
				if (terrain.getPlugin() instanceof AsteroidSource) {
					AsteroidSource source = (AsteroidSource) terrain.getPlugin();
					source.regenerateAsteroids();
				}
			}
		}
	}
	
	
	protected Map<SectorEntityToken, LocationAPI> asteroidList = new HashMap<SectorEntityToken, LocationAPI>();
	protected Map<SectorEntityToken, LocationAPI> junkList = new HashMap<SectorEntityToken, LocationAPI>();
	@Override
	public void beforeGameSave() {
		junkList.clear();
//		Set<LocationAPI> seen = new HashSet<LocationAPI>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			LocationAPI loc = market.getContainingLocation();
//			if (loc == null || seen.contains(loc)) continue;
//			seen.add(loc);
		for (LocationAPI loc : Global.getSector().getAllLocations()) {
			for (SectorEntityToken junk : loc.getEntitiesWithTag(Tags.ORBITAL_JUNK)) {
				loc.removeEntity(junk);
				junkList.put(junk, loc);
			}
		}
		
		asteroidList.clear();
		for (LocationAPI loc : Global.getSector().getAllLocations()) {
			for (SectorEntityToken asteroid : new ArrayList<SectorEntityToken>(loc.getAsteroids())) {
				AsteroidSource source = Misc.getAsteroidSource(asteroid);
				if (source == null || !asteroid.getMemoryWithoutUpdate().isEmpty()) {
					if (source != null) {
						source.reportAsteroidPersisted(asteroid);
						Misc.clearAsteroidSource(asteroid);
					}
					continue;
				} else {
					asteroidList.put(asteroid, loc);
					loc.removeEntity(asteroid);
				}
			}
		}
		
		econPreSaveCleanup();
	}
	
	
	protected void econPreSaveCleanup() {
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			for (Industry ind : market.getIndustries()) {
				ind.doPreSaveCleanup();
			}
		}
	}
	public static void econPostSaveRestore() {
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			for (Industry ind : market.getIndustries()) {
				ind.doPostSaveRestore();
			}
		}
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			market.reapplyConditions();
			market.reapplyIndustries();
		}
	}
	
	@Override
	public void afterGameSave() {
		restoreRemovedEntities();
		econPostSaveRestore();
	}
	
	@Override
	public void onGameSaveFailed() {
		restoreRemovedEntities();
		econPostSaveRestore();
	}
	
	
	
	protected void restoreRemovedEntities() {
		for (SectorEntityToken junk : junkList.keySet()) {
			((LocationAPI)junkList.get(junk)).addEntity(junk);
		}
		junkList.clear();
		for (SectorEntityToken asteroid : asteroidList.keySet()) {
			((LocationAPI)asteroidList.get(asteroid)).addEntity(asteroid);
		}
		asteroidList.clear();
	}
	
	

	private void initSlipstream() {
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			SectorEntityToken slipstream = system.addTerrain(Terrain.SLIPSTREAM, null);
			slipstream.getLocation().set(0, 0);
			system.getPersistentData().put(SlipstreamTerrainPlugin.LOCATION_SLIPSTREAM_KEY, slipstream);
		}
		SectorEntityToken slipstream = Global.getSector().getHyperspace().addTerrain(Terrain.SLIPSTREAM, null);
		slipstream.getLocation().set(0, 0);
		Global.getSector().getHyperspace().getPersistentData().put(SlipstreamTerrainPlugin.LOCATION_SLIPSTREAM_KEY, slipstream);
	}
	
	private void createInitialPeople() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_DO_NOT_INIT_COMM_LISTINGS)) continue;
			boolean addedPerson = false;
			
			PersonAPI admin = null;
			
			if (market.hasIndustry(Industries.MILITARYBASE) || market.hasIndustry(Industries.HIGHCOMMAND)) {
				PersonAPI person = market.getFaction().createRandomPerson();
				String rankId = Ranks.GROUND_MAJOR;
				if (market.getSize() >= 6) {
					rankId = Ranks.GROUND_GENERAL;
				} else if (market.getSize() >= 4) {
					rankId = Ranks.GROUND_COLONEL;
				}
				person.setRankId(rankId);
				person.setPostId(Ranks.POST_BASE_COMMANDER);
				
				market.getCommDirectory().addPerson(person);
				market.addPerson(person);
				ip.addPerson(person);
				ip.getData(person).getLocation().setMarket(market);
				ip.checkOutPerson(person, "permanent_staff");
				addedPerson = true;
			}
			
			boolean hasStation = false;
			for (Industry curr : market.getIndustries()) {
				if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
					hasStation = true;
					break;
				}
			}
			if (hasStation) {
				PersonAPI person = market.getFaction().createRandomPerson();
				String rankId = Ranks.SPACE_COMMANDER;
				if (market.getSize() >= 6) {
					rankId = Ranks.SPACE_ADMIRAL;
				} else if (market.getSize() >= 4) {
					rankId = Ranks.SPACE_CAPTAIN;
				}
				person.setRankId(rankId);
				person.setPostId(Ranks.POST_STATION_COMMANDER);
				
				market.getCommDirectory().addPerson(person);
				market.addPerson(person);
				ip.addPerson(person);
				ip.getData(person).getLocation().setMarket(market);
				ip.checkOutPerson(person, "permanent_staff");
				addedPerson = true;
				
				if (market.getPrimaryEntity().hasTag(Tags.STATION)) {
					admin = person;
				}
			}
			
			if (market.hasSpaceport()) {
				PersonAPI person = market.getFaction().createRandomPerson();
				//person.setRankId(Ranks.SPACE_CAPTAIN);
				person.setPostId(Ranks.POST_PORTMASTER);
				
				market.getCommDirectory().addPerson(person);
				market.addPerson(person);
				ip.addPerson(person);
				ip.getData(person).getLocation().setMarket(market);
				ip.checkOutPerson(person, "permanent_staff");
				addedPerson = true;
			}
			
			if (addedPerson) {
				PersonAPI person = market.getFaction().createRandomPerson();
				person.setRankId(Ranks.SPACE_COMMANDER);
				person.setPostId(Ranks.POST_SUPPLY_OFFICER);
				
				market.getCommDirectory().addPerson(person);
				market.addPerson(person);
				ip.addPerson(person);
				ip.getData(person).getLocation().setMarket(market);
				ip.checkOutPerson(person, "permanent_staff");
				addedPerson = true;
			}
			
			if (!addedPerson || admin == null) {
				PersonAPI person = market.getFaction().createRandomPerson();
				person.setRankId(Ranks.CITIZEN);
				person.setPostId(Ranks.POST_ADMINISTRATOR);
				
				market.getCommDirectory().addPerson(person);
				market.addPerson(person);
				ip.addPerson(person);
				ip.getData(person).getLocation().setMarket(market);
				ip.checkOutPerson(person, "permanent_staff");
				admin = person;
			}
			
			if (admin != null) {
				addSkillsAndAssignAdmin(market, admin);
			}
		}
		
		assignCustomAdmins();
	}
	
	protected void assignCustomAdmins() {
		{
		MarketAPI market = null;
		
		market =  Global.getSector().getEconomy().getMarket("sindria");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setFaction(Factions.DIKTAT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.getName().setFirst("Phillip");
			person.getName().setLast("Andrada");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "andrada"));
			person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("kantas_den");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.FEMALE);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setRankId(Ranks.SPACE_ADMIRAL);
			person.getName().setFirst("Kanta");
			person.getName().setLast("");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "kanta"));
			person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eochu_bres");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.getName().setFirst("Artemisia");
			person.getName().setLast("Sun");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sun"));
			person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.getName().setFirst("Baikal");
			person.getName().setLast("Daud");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baikal"));
			person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("station_kapteyn");
		if (market != null) {
			PersonAPI person = market.getFaction().createRandomPerson();
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}
		}
		
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (Factions.TRITACHYON.equals(market.getFactionId()) &&
					//(market.getId().equals("eochu_bres") ||
							(market.getId().equals("culann"))) {
				PersonAPI person = market.getFaction().createRandomPerson();
				person.setRankId(Ranks.CITIZEN);
				person.setPostId(Ranks.POST_ADMINISTRATOR);
				
				// totally not a front for an Alpha Core
				person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
				person.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
				person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
				person.setAICoreId(Commodities.ALPHA_CORE);
				
				market.setAdmin(person);
				market.getCommDirectory().addPerson(person, 0);
				market.addPerson(person);
			}
		}
		
	}

	protected void addSkillsAndAssignAdmin(MarketAPI market, PersonAPI admin) {
		List<String> skills = Global.getSettings().getSortedSkillIds();
		if (!skills.contains(Skills.PLANETARY_OPERATIONS) ||
				!skills.contains(Skills.FLEET_LOGISTICS) ||
				!skills.contains(Skills.INDUSTRIAL_PLANNING)) {
			return;
		}
		
		int size = market.getSize();
		if (size <= 4) return;
		
		int industries = 0;
		int defenses = 0;
		boolean military = market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);
		
		for (Industry curr : market.getIndustries()) {
			if (curr.isIndustry()) {
				industries++;
			}
			if (curr.getSpec().hasTag(Industries.TAG_GROUNDDEFENSES)) {
				defenses++;
			}
		}
		
		
		admin.getStats().setSkipRefresh(true);
		
		int num = 0;
		if (industries >= 2 || (industries == 1 && defenses == 1)) {
			admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			num++;
		}
		
		if (num == 0 || size >= 7) {
			if (military) {
				admin.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
			} else if (defenses > 0) {
				admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			} else {
				// nothing else suitable, so just make sure there's at least one skill, if this wasn't already set
				admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			}
		}
		
		admin.getStats().setSkipRefresh(false);
		admin.getStats().refreshCharacterStatsEffects();
		
		market.setAdmin(admin);
	}
	
	
	
	

	@Override
	public void configureXStream(XStream x) {
		x.alias("AssignmentModulePlugin", com.fs.starfarer.api.campaign.ai.AssignmentModulePlugin.class);
		x.alias("ModularFleetAIAPI", com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI.class);
		x.alias("NavigationModulePlugin", com.fs.starfarer.api.campaign.ai.NavigationModulePlugin.class);
		x.alias("StrategicModulePlugin", com.fs.starfarer.api.campaign.ai.StrategicModulePlugin.class);
		x.alias("TacticalModulePlugin", com.fs.starfarer.api.campaign.ai.TacticalModulePlugin.class);
		
		x.alias("NTP", NebulaTerrainPlugin.class);
		x.alias("AuroraRenderer", AuroraRenderer.class);
		x.alias("CRLPSBuff", CRLossPerSecondBuff.class);
		x.aliasAttribute(CRLossPerSecondBuff.class, "id", "id");
		x.aliasAttribute(CRLossPerSecondBuff.class, "mult", "m");
		x.aliasAttribute(CRLossPerSecondBuff.class, "dur", "d");
		
		x.alias("PPBuff", PeakPerformanceBuff.class);
		x.aliasAttribute(PeakPerformanceBuff.class, "id", "id");
		x.aliasAttribute(PeakPerformanceBuff.class, "mult", "m");
		x.aliasAttribute(PeakPerformanceBuff.class, "dur", "d");
		
		
		x.alias("FParams", FleetParams.class);
		x.aliasAttribute(FleetParams.class, "hyperspaceLocation", "hL");
		x.aliasAttribute(FleetParams.class, "market", "m");
		x.aliasAttribute(FleetParams.class, "factionId", "fId");
		x.aliasAttribute(FleetParams.class, "fleetType", "fT");
		x.aliasAttribute(FleetParams.class, "combatPts", "cP");
		x.aliasAttribute(FleetParams.class, "freighterPts", "fP");
		x.aliasAttribute(FleetParams.class, "tankerPts", "taP");
		x.aliasAttribute(FleetParams.class, "transportPts", "trP");
		x.aliasAttribute(FleetParams.class, "linerPts", "lP");
		x.aliasAttribute(FleetParams.class, "civilianPts", "civP");
		x.aliasAttribute(FleetParams.class, "utilityPts", "uP");
		x.aliasAttribute(FleetParams.class, "qualityBonus", "qB");
		x.aliasAttribute(FleetParams.class, "qualityOverride", "qO");
		x.aliasAttribute(FleetParams.class, "officerNumMult", "oNM");
		x.aliasAttribute(FleetParams.class, "officerLevelBonus", "oLB");
		x.aliasAttribute(FleetParams.class, "levelLimit", "lL");
		x.aliasAttribute(FleetParams.class, "commander", "c");
		x.aliasAttribute(FleetParams.class, "factionIdForShipPicking", "fIDSP");
		x.aliasAttribute(FleetParams.class, "random", "r");
		x.aliasAttribute(FleetParams.class, "withOfficers", "wO");
		x.aliasAttribute(FleetParams.class, "maxShipSize", "mSS");


		
		x.alias("MaxBurnBuff", MaxBurnBuff.class);
		x.alias("PeakPerformanceBuff", PeakPerformanceBuff.class);
		x.alias("RingSystemTerrainPlugin", RingSystemTerrainPlugin.class);
		x.alias("StarCoronaAkaMainyuTerrainPlugin", StarCoronaAkaMainyuTerrainPlugin.class);
		x.alias("StarCoronaTerrainPlugin", StarCoronaTerrainPlugin.class);
		
		x.alias("PulsarBeamTerrainPlugin", PulsarBeamTerrainPlugin.class);
		x.alias("EventHorizonPlugin", EventHorizonPlugin.class);
		
		x.alias("FlareManager", FlareManager.class);
		x.aliasAttribute(FlareManager.class, "flareTracker", "fT");
		x.aliasAttribute(FlareManager.class, "flares", "f");
		x.aliasAttribute(FlareManager.class, "delegate", "d");
		
		x.alias("Flare", Flare.class);
		x.aliasAttribute(Flare.class, "direction", "d");
		x.aliasAttribute(Flare.class, "arc", "a");
		x.aliasAttribute(Flare.class, "extraLengthMult", "eLM");
		x.aliasAttribute(Flare.class, "extraLengthFlat", "eLF");
		x.aliasAttribute(Flare.class, "shortenFlatMod", "sFM");
		x.aliasAttribute(Flare.class, "c", "c");
		x.aliasAttribute(Flare.class, "fader", "f");
		
		
		
		x.alias("MagneticFieldTerrainPlugin", MagneticFieldTerrainPlugin.class);
		x.aliasAttribute(MagneticFieldTerrainPlugin.class, "entity", "e");
		//x.aliasAttribute(MagneticFieldTerrainPlugin.class, "params", "p");
		x.aliasAttribute(MagneticFieldTerrainPlugin.class, "renderer", "r");
		x.aliasAttribute(MagneticFieldTerrainPlugin.class, "flareManager", "fM");
		
		x.alias("MagneticFieldParams", MagneticFieldParams.class);
		x.aliasAttribute(MagneticFieldParams.class, "baseColor", "bC");
		x.aliasAttribute(MagneticFieldParams.class, "c", "c");
		x.aliasAttribute(MagneticFieldParams.class, "auroraFrequency", "aF");
		x.aliasAttribute(MagneticFieldParams.class, "innerRadius", "iR");
		x.aliasAttribute(MagneticFieldParams.class, "outerRadius", "oR");
		
		x.alias("", AuroraRenderer.class);
		x.aliasAttribute(AuroraRenderer.class, "phaseAngle", "a");
		x.aliasAttribute(AuroraRenderer.class, "delegate", "d");
		
		
		x.alias("FlickerUtil", FlickerUtil.class);
		x.aliasAttribute(FlickerUtil.class, "angle", "a");
		x.aliasAttribute(FlickerUtil.class, "brightness", "b");
		x.aliasAttribute(FlickerUtil.class, "currTime", "c");
		x.aliasAttribute(FlickerUtil.class, "currMaxBurstTime", "cMBT");
		x.aliasAttribute(FlickerUtil.class, "currMaxBrightness", "cMB");
		x.aliasAttribute(FlickerUtil.class, "maxBurstTime", "mBT");
		x.aliasAttribute(FlickerUtil.class, "peakTime", "pT");
		x.aliasAttribute(FlickerUtil.class, "peakDur", "pD");
		x.aliasAttribute(FlickerUtil.class, "stop", "s");
		
//		x.alias("FTr", FlickerTracker.class);
//	
//		x.aliasAttribute(FlickerTracker.class, "leadTime", "f");
//		x.aliasAttribute(FlickerTracker.class, "shiver", "sh");
//		x.aliasAttribute(FlickerTracker.class, "highlight", "hi");
//		x.aliasAttribute(FlickerTracker.class, "flicker", "f");
//		x.aliasAttribute(FlickerTracker.class, "stopped", "s");
//		x.aliasAttribute(FlickerTracker.class, "checkedBurst", "cB");
		
		x.alias("FUtil", FaderUtil.class);
		x.aliasAttribute(FaderUtil.class, "currBrightness", "b");
		x.aliasAttribute(FaderUtil.class, "durationIn", "i");
		x.aliasAttribute(FaderUtil.class, "durationOut", "o");
		x.aliasAttribute(FaderUtil.class, "state", "s");
		x.aliasAttribute(FaderUtil.class, "bounceDown", "d");
		x.aliasAttribute(FaderUtil.class, "bounceUp", "u");
		
		
		x.alias("IUtil", IntervalUtil.class);
		x.aliasAttribute(IntervalUtil.class, "minInterval", "i");
		x.aliasAttribute(IntervalUtil.class, "maxInterval", "a");
		x.aliasAttribute(IntervalUtil.class, "currInterval", "c");
		x.aliasAttribute(IntervalUtil.class, "elapsed", "e");
		x.aliasAttribute(IntervalUtil.class, "intervalElapsed", "ie");
		
		x.alias("IDt", TimeoutTracker.ItemData.class);
		x.aliasAttribute(ItemData.class, "item", "i");
		x.aliasAttribute(ItemData.class, "remaining", "r");
		
		
		x.alias("TrA", TransponderAbility.class);
		x.alias("TrAI", TransponderAbilityAI.class);
		
		x.alias("EmB", EmergencyBurnAbility.class);
		x.alias("EmBAI", EmergencyBurnAbilityAI.class);
		
		x.alias("GoDA", GoDarkAbility.class);
		x.alias("GoDAAI", GoDarkAbilityAI.class);
		
		x.alias("SeBA", SensorBurstAbility.class);
		x.alias("SeBAAI", SensorBurstAbilityAI.class);
		
		x.alias("SuBA", SustainedBurnAbility.class);
		x.alias("SuBAAI", SustainedBurnAbilityAI.class);
		
		x.alias("InPA", InterdictionPulseAbility.class);
		x.alias("InPAAI", InterdictionPulseAbilityAI.class);
		
		x.alias("IPReactionScript", IPReactionScript.class);
		x.aliasAttribute(IPReactionScript.class, "delay", "e");
		x.aliasAttribute(IPReactionScript.class, "done", "d");
		x.aliasAttribute(IPReactionScript.class, "other", "o");
		x.aliasAttribute(IPReactionScript.class, "fleet", "f");
		x.aliasAttribute(IPReactionScript.class, "activationDays", "aD");
		
		x.alias("ScA", ScavengeAbility.class);
		x.alias("FJA", FractureJumpAbility.class);
		x.alias("RSA", RemoteSurveyAbility.class);
		
		x.alias("GScan", GraviticScanAbility.class);
		x.alias("GSDat", GraviticScanData.class);
		x.alias("GSPing", GSPing.class);
		x.aliasAttribute(GSPing.class, "arc", "a");
		x.aliasAttribute(GSPing.class, "angle", "n");
		x.aliasAttribute(GSPing.class, "grav", "g");
		x.aliasAttribute(GSPing.class, "fader", "f");
		x.aliasAttribute(GSPing.class, "withSound", "s");
		
		
		x.alias("ProcgenUsedNames", ProcgenUsedNames.class);
		x.alias("SmugglingScanScript", SmugglingScanScript.class);
		
		//x.alias("BaseToggleAbility", BaseToggleAbility.class);
		
		
		x.aliasAttribute(BaseAbilityPlugin.class, "entity", "e");
		x.aliasAttribute(BaseAbilityPlugin.class, "id", "id");
		x.aliasAttribute(BaseAbilityPlugin.class, "disableFrames", "dF");
		
		x.aliasAttribute(BaseToggleAbility.class, "turnedOn", "tO");
		x.aliasAttribute(BaseToggleAbility.class, "cooldownLeft", "cL");
		x.aliasAttribute(BaseToggleAbility.class, "isActivateCooldown", "iAC");
		x.aliasAttribute(BaseToggleAbility.class, "level", "l");
		
		x.aliasAttribute(BaseDurationAbility.class, "turnedOn", "tO");
		x.aliasAttribute(BaseDurationAbility.class, "activeDaysLeft", "aDL");
		x.aliasAttribute(BaseDurationAbility.class, "cooldownLeft", "cL");
		x.aliasAttribute(BaseDurationAbility.class, "level", "l");
		x.aliasAttribute(BaseDurationAbility.class, "loopFadeLeft", "lFF");
		x.aliasAttribute(BaseDurationAbility.class, "fadingOut", "fO");
		
		x.aliasAttribute(BaseAbilityAI.class, "fleet", "f");
		x.aliasAttribute(BaseAbilityAI.class, "ability", "a");
		
		x.aliasAttribute(GoDarkAbilityAI.class, "interval", "i");
		x.aliasAttribute(TransponderAbilityAI.class, "interval", "i");
		x.aliasAttribute(EmergencyBurnAbilityAI.class, "interval", "i");
		x.aliasAttribute(SensorBurstAbilityAI.class, "interval", "i");
		
		
		x.alias("RAT", RollingAverageTracker.class);
		x.aliasAttribute(RollingAverageTracker.class, "timer", "t");
		x.aliasAttribute(RollingAverageTracker.class, "f", "f");
		x.aliasAttribute(RollingAverageTracker.class, "elaspedFractionOverride", "e");
		x.aliasAttribute(RollingAverageTracker.class, "curr", "c");
		x.aliasAttribute(RollingAverageTracker.class, "avg", "a");
		
		
//		x.alias("SSAT", StarSystemActivityTracker.class);
//		x.aliasAttribute(StarSystemActivityTracker.class, "econInterval", "eI");
//		x.aliasAttribute(StarSystemActivityTracker.class, "timer", "t");
//		x.aliasAttribute(StarSystemActivityTracker.class, "seen", "sn");
//		x.aliasAttribute(StarSystemActivityTracker.class, "system", "sy");
//		x.aliasAttribute(StarSystemActivityTracker.class, "points", "pt");
//		x.aliasAttribute(StarSystemActivityTracker.class, "fleets", "fl");
//		x.aliasAttribute(StarSystemActivityTracker.class, "ships", "sh");
		
		x.alias("PTDFS", PlayerTradeDataForSubmarket.class);
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "playerBought", "pB");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "playerSold", "pS");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "accumulatedPlayerTradeValueForPositive", "accP");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "accumulatedPlayerTradeValueForNegative", "accN");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "totalPlayerTradeValue", "tPV");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "tracker", "t");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "playerBoughtShips", "pBS");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "playerSoldShips", "pSS");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "market", "m");
		x.aliasAttribute(PlayerTradeDataForSubmarket.class, "submarket", "s");
		
		
		x.alias("MPFD", com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData.class);
		x.aliasAttribute( com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData.class, "type", "t");
		x.aliasAttribute( com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData.class, "spawnFP", "fp");
		
		x.alias("PatrolFleetData", PatrolFleetData.class);
		x.alias("PatrolFleetManager", PatrolFleetManager.class);
		x.alias("PatrolFleetManagerV2", PatrolFleetManagerV2.class);
		x.alias("PatrolAssignmentAI", PatrolAssignmentAI.class);
		x.alias("SharedData", SharedData.class);
		x.alias("HyperspaceTerrainPlugin", HyperspaceTerrainPlugin.class);
		x.alias("FoodShortageEvent", FoodShortageEvent.class);
		x.alias("CoreEventProbabilityManager", CoreEventProbabilityManager.class);
		x.alias("BlackMarketPlugin", BlackMarketPlugin.class);
		x.alias("PlayerTradeDataForSubmarket", PlayerTradeDataForSubmarket.class);
		x.alias("PriceUpdate", PriceUpdate.class);
		x.alias("JumpDestination", JumpDestination.class);
		x.alias("OpenMarketPlugin", OpenMarketPlugin.class);
		x.alias("CampaignEventTarget", CampaignEventTarget.class);
		//x.alias("EconomyFleetManager", EconomyFleetManager.class);
		//x.alias("StarSystemActivityTracker", StarSystemActivityTracker.class);
//		x.alias("StoragePlugin", StoragePlugin.class);
//		x.alias("MilitarySubmarketPlugin", MilitarySubmarketPlugin.class);
		x.alias("OfficerManagerEvent", OfficerManagerEvent.class);
		x.alias("AvailableOfficer", AvailableOfficer.class);
		x.alias("ManagedFleetData", ManagedFleetData.class);
		
		x.alias("TradeDisruption", TradeDisruption.class);
		x.alias("CommodityStats", CommodityStats.class);
		x.alias("TradeInfoUpdateEvent", TradeInfoUpdateEvent.class);
		x.alias("TransactionLineItem", TransactionLineItem.class);
		x.alias("Population", Population.class);
		//x.alias("MagneticFieldParams", MagneticFieldParams.class);
		//x.alias("MercFleetManager", MercFleetManager.class);
		x.alias("MercFleetManagerV2", MercFleetManagerV2.class);
		x.alias("InvestigationEventGoodRepWithOther", InvestigationEventGoodRepWithOther.class);
		//x.alias("SmugglingFactionChangeScript", SmugglingFactionChangeScript.class);
		x.alias("RepTrackerEvent", RepTrackerEvent.class);
		x.alias("Hydroponics", Hydroponics.class);
		x.alias("WorldUninhabitable", WorldUninhabitable.class);
		x.alias("ReputationChangeData", ReputationChangeData.class);
		x.alias("FactionTradeRepData", FactionTradeRepData.class);
		x.alias("RecentUnrest", RecentUnrest.class);
		x.alias("Outpost", Outpost.class);
		x.alias("CellStateTracker", CellStateTracker.class);
		x.alias("CoreScript", CoreScript.class);
		
		x.alias("CoronaParams", CoronaParams.class);
		x.aliasAttribute(CoronaParams.class, "windBurnLevel", "wBL");
		x.aliasAttribute(CoronaParams.class, "flareProbability", "fP");
		x.aliasAttribute(CoronaParams.class, "crLossMult", "crLM");
		
		x.alias("RingParams", RingParams.class);
		x.aliasAttribute(RingParams.class, "bandWidthInEngine", "bWIE");
		x.aliasAttribute(RingParams.class, "middleRadius", "mR");
		x.aliasAttribute(RingParams.class, "name", "n");
		x.aliasAttribute(RingParams.class, "relatedEntity", "e");
		
		x.alias("AsteroidBeltParams", AsteroidBeltParams.class);
		x.aliasAttribute(AsteroidBeltParams.class, "numAsteroids", "nA");
		x.aliasAttribute(AsteroidBeltParams.class, "minOrbitDays", "minO");
		x.aliasAttribute(AsteroidBeltParams.class, "maxOrbitDays", "maxO");
		x.aliasAttribute(AsteroidBeltParams.class, "minSize", "minS");
		x.aliasAttribute(AsteroidBeltParams.class, "maxSize", "maxS");
		
		x.alias("AsteroidFieldParams", AsteroidFieldParams.class);
		x.aliasAttribute(AsteroidFieldParams.class, "minRadius", "minR");
		x.aliasAttribute(AsteroidFieldParams.class, "maxRadius", "maxR");
		x.aliasAttribute(AsteroidFieldParams.class, "numAsteroids", "nA");
		x.aliasAttribute(AsteroidFieldParams.class, "minAsteroids", "minA");
		x.aliasAttribute(AsteroidFieldParams.class, "maxAsteroids", "maxA");
		x.aliasAttribute(AsteroidFieldParams.class, "minSize", "minS");
		x.aliasAttribute(AsteroidFieldParams.class, "maxSize", "maxS");
		
		x.alias("AsteroidBeltTerrainPlugin", AsteroidBeltTerrainPlugin.class);
		x.aliasAttribute(AsteroidBeltTerrainPlugin.class, "needToCreateAsteroids", "nTCA");
		
		x.alias("AsteroidFieldTerrainPlugin", AsteroidFieldTerrainPlugin.class);
		
		
		x.alias("BaseTerrain", BaseTerrain.class);
		x.aliasAttribute(BaseTerrain.class, "terrainId", "tid");
		x.aliasAttribute(BaseTerrain.class, "name", "n");
		
		x.alias("BaseRingTerrain", BaseRingTerrain.class);
		x.alias("AbandonedStation", AbandonedStation.class);
		x.alias("WorldTundra", WorldTundra.class);
		x.alias("Frontier", Frontier.class);
		x.alias("RadioChatterTerrainPlugin", RadioChatterTerrainPlugin.class);
		x.alias("RadioChatterParams", RadioChatterParams.class);
		x.alias("WorldBarrenMarginal", WorldBarrenMarginal.class);
		x.alias("UrbanizedPolity", UrbanizedPolity.class);
		x.alias("OrganizedCrime", OrganizedCrime.class);
		x.alias("Headquarters", Headquarters.class);
		x.alias("ViceDemand", ViceDemand.class);
		x.alias("SystemBounty", SystemBounty.class);
		x.alias("WorldTwilight", WorldTwilight.class);
		x.alias("RuralPolity", RuralPolity.class);
		x.alias("FreeMarket", FreeMarket.class);
		x.alias("WorldIce", WorldIce.class);
		x.alias("Dissident", Dissident.class);
		x.alias("TradeCenter", TradeCenter.class);
		x.alias("CottageIndustry", CottageIndustry.class);
		x.alias("LuddicMajority", LuddicMajority.class);
		x.alias("WorldArid", WorldArid.class);
		x.alias("Decivilized", Decivilized.class);
		x.alias("StealthMinefields", StealthMinefields.class);
		x.alias("Smuggling", Smuggling.class);
		x.alias("ShipbreakingCenter", ShipbreakingCenter.class);
		x.alias("FoodShortage", FoodShortage.class);
		x.alias("CRRecoveryBuff", CRRecoveryBuff.class);
		x.alias("LargeRefugeePopulation", LargeRefugeePopulation.class);
		x.alias("WorldWater", WorldWater.class);
		x.alias("RegionalCapital", RegionalCapital.class);
		x.alias("OrbitalBurns", OrbitalBurns.class);
		x.alias("WorldJungle", WorldJungle.class);
		x.alias("WorldDesert", WorldDesert.class);
		
		x.alias("BaseHazardCondition", BaseHazardCondition.class);
		x.alias("WorldTerran", WorldTerran.class);
		
		x.alias("DebrisFieldTerrainPlugin", DebrisFieldTerrainPlugin.class);
		x.alias("DebrisFieldParams", DebrisFieldParams.class);
		x.aliasAttribute(DebrisFieldParams.class, "density", "d");
		x.aliasAttribute(DebrisFieldParams.class, "baseDensity", "bD");
		x.aliasAttribute(DebrisFieldParams.class, "glowsDays", "gD");
		x.aliasAttribute(DebrisFieldParams.class, "lastsDays", "lD");
		x.aliasAttribute(DebrisFieldParams.class, "minSize", "min1");
		x.aliasAttribute(DebrisFieldParams.class, "maxSize", "max1");
		x.aliasAttribute(DebrisFieldParams.class, "glowColor", "gC");
		x.aliasAttribute(DebrisFieldParams.class, "defFaction", "dF");
		x.aliasAttribute(DebrisFieldParams.class, "defenderProb", "dP");
		x.aliasAttribute(DebrisFieldParams.class, "minStr", "min2");
		x.aliasAttribute(DebrisFieldParams.class, "maxStr", "max2");
		x.aliasAttribute(DebrisFieldParams.class, "maxDefenderSize", "mDS");
		x.aliasAttribute(DebrisFieldParams.class, "baseSalvageXP", "xp");
		x.aliasAttribute(DebrisFieldParams.class, "source", "s");
		
		
		
		x.alias("PlayerTradeProfitabilityData", PlayerTradeProfitabilityData.class);
		x.alias("CommodityData", CommodityData.class);
		x.alias("CoreCampaignPluginImpl", CoreCampaignPluginImpl.class);
		x.alias("FactionHostilityEvent", FactionHostilityEvent.class);
		x.alias("FactionHostilityPairKey", FactionHostilityPairKey.class);
		x.alias("InvestigationEventSmugglingV2", InvestigationEventSmugglingV2.class);
		x.alias("RecentUnrestEvent", RecentUnrestEvent.class);
		//x.alias("BountyPirateFleetManager", BountyPirateFleetManager.class);
		
		
		
		x.alias("AsteroidBeltParams", AsteroidBeltParams.class);
		
		
		x.alias("DomainSurveyDerelictSpecial", DomainSurveyDerelictSpecial.class);
		
		x.alias("BaseCustomEntityPlugin", BaseCustomEntityPlugin.class);
		x.aliasAttribute(BaseCustomEntityPlugin.class, "entity", "e");
		
		x.alias("DerelictShipEntityPlugin", DerelictShipEntityPlugin.class);
		x.aliasAttribute(DerelictShipEntityPlugin.class, "data", "d");
		x.aliasAttribute(DerelictShipEntityPlugin.class, "elapsed", "el");
		x.aliasAttribute(DerelictShipEntityPlugin.class, "angVel", "aV");
		
		x.alias("DerelictShipData", DerelictShipData.class);
		x.aliasAttribute(DerelictShipData.class, "ship", "s");
		x.aliasAttribute(DerelictShipData.class, "durationDays", "d");
		x.aliasAttribute(DerelictShipData.class, "canHaveExtraCargo", "c");
		
		x.alias("PerShipData", PerShipData.class);
		x.aliasAttribute(PerShipData.class, "condition", "c");
		x.aliasAttribute(PerShipData.class, "variantId", "vId");
		x.aliasAttribute(PerShipData.class, "variant", "v");
		x.aliasAttribute(PerShipData.class, "shipName", "sN");
		x.aliasAttribute(PerShipData.class, "addDmods", "d");
		x.aliasAttribute(PerShipData.class, "pruneWeapons", "p");
		x.alias("ShipCondition", ShipCondition.class);
		
		
		x.alias("ShipRecoverySpecialData", ShipRecoverySpecialData.class);
		x.aliasAttribute(ShipRecoverySpecialData.class, "ships", "s");
		x.aliasAttribute(ShipRecoverySpecialData.class, "desc", "d");
		
		x.alias("BreadcrumbSpecialData", BreadcrumbSpecialData.class);
		x.aliasAttribute(BreadcrumbSpecialData.class, "targetId", "tI");
		x.aliasAttribute(BreadcrumbSpecialData.class, "targetName", "tN");
			
		x.alias("DerSpecialType", SpecialType.class);
		x.alias("DomainSurveyDerelictSpecialData", DomainSurveyDerelictSpecialData.class);
		x.aliasAttribute(DomainSurveyDerelictSpecialData.class, "type", "t");
		x.aliasAttribute(DomainSurveyDerelictSpecialData.class, "entityId", "eI");
		x.aliasAttribute(DomainSurveyDerelictSpecialData.class, "secondaryId", "sI");
		
		
		x.alias("SleeperSpecialType", SleeperSpecialType.class);
		x.alias("SleeperPodsSpecialData", SleeperPodsSpecialData.class);
		x.aliasAttribute(SleeperPodsSpecialData.class, "type", "t");
		//x.aliasAttribute(SleeperPodsSpecialData.class, "quantity", "q");
		x.aliasAttribute(SleeperPodsSpecialData.class, "officer", "o");
		
		
		x.alias("SDSpecialType", SurveyDataSpecialType.class);
		x.alias("SurveyDataSpecialData", SurveyDataSpecialData.class);
		x.aliasAttribute(SurveyDataSpecialData.class, "type", "t");
		x.aliasAttribute(SurveyDataSpecialData.class, "entityId", "eI");
		x.aliasAttribute(SurveyDataSpecialData.class, "secondaryId", "sI");
		x.aliasAttribute(SurveyDataSpecialData.class, "includeRuins", "r");
		
		
		x.alias("TransmitterTrapSpecialData", TransmitterTrapSpecialData.class);
		x.aliasAttribute(TransmitterTrapSpecialData.class, "prob", "c");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "fleetId", "fID");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "nearbyFleetFaction", "nFF");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "useClosestFleetInRange", "uCFIR");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "useAllFleetsInRange", "uAFIR");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "params", "p");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "minRange", "min");
		x.aliasAttribute(TransmitterTrapSpecialData.class, "maxRange", "max");
		
		
		x.alias("CargoManifestSpecialData", CargoManifestSpecialData.class);
		x.aliasAttribute(CargoManifestSpecialData.class, "commodityId", "c");
		
		
		
		x.alias("DDOv", DefenderDataOverride.class);
		x.aliasAttribute(DefenderDataOverride.class, "probDefenders", "p");
		x.aliasAttribute(DefenderDataOverride.class, "minStr", "i");
		x.aliasAttribute(DefenderDataOverride.class, "maxStr", "a");
		x.aliasAttribute(DefenderDataOverride.class, "maxDefenderSize", "d");
		x.aliasAttribute(DefenderDataOverride.class, "probStation", "t");
		x.aliasAttribute(DefenderDataOverride.class, "stationRole", "s");
		x.aliasAttribute(DefenderDataOverride.class, "defFaction", "f");

		
		x.alias("SeededFleetManager", SeededFleetManager.class);
		x.aliasAttribute(SeededFleetManager.class, "fleets", "f");
		x.aliasAttribute(SeededFleetManager.class, "inflateRangeLY", "iRLY");
		x.aliasAttribute(SeededFleetManager.class, "system", "s");
		
		x.alias("SeededFleet", SeededFleet.class);
		x.aliasAttribute(SeededFleet.class, "seed", "s");
		x.aliasAttribute(SeededFleet.class, "points", "p");
		x.aliasAttribute(SeededFleet.class, "fleet", "f");
		
		x.alias("RemnantSeededFleetManager", RemnantSeededFleetManager.class);
		x.aliasAttribute(RemnantSeededFleetManager.class, "minPts", "i");
		x.aliasAttribute(RemnantSeededFleetManager.class, "maxPts", "a");
		x.aliasAttribute(RemnantSeededFleetManager.class, "activeChance", "c");

		x.alias("RFICGen", RemnantFleetInteractionConfigGen.class);
		x.alias("RSICGen", RemnantStationInteractionConfigGen.class);
		
		x.alias("SourceBasedFleetManager", SourceBasedFleetManager.class);
		x.aliasAttribute(SourceBasedFleetManager.class, "fleets", "f");
		x.aliasAttribute(SourceBasedFleetManager.class, "thresholdLY", "t");
		x.aliasAttribute(SourceBasedFleetManager.class, "source", "s");
		x.aliasAttribute(SourceBasedFleetManager.class, "minFleets", "i");
		x.aliasAttribute(SourceBasedFleetManager.class, "maxFleets", "a");
		x.aliasAttribute(SourceBasedFleetManager.class, "respawnDelay", "r");
		x.aliasAttribute(SourceBasedFleetManager.class, "destroyed", "d");
		x.aliasAttribute(SourceBasedFleetManager.class, "sourceLocation", "sL");

		x.alias("RemnantStationFleetManager", RemnantStationFleetManager.class);
		x.aliasAttribute(RemnantStationFleetManager.class, "minPts", "iA");
		x.aliasAttribute(RemnantStationFleetManager.class, "maxPts", "aA");
		x.aliasAttribute(RemnantStationFleetManager.class, "totalLost", "tL");
		
		x.alias("RemnantAssignmentAI", RemnantAssignmentAI.class);
		x.aliasAttribute(RemnantAssignmentAI.class, "homeSystem", "h");
		x.aliasAttribute(RemnantAssignmentAI.class, "fleet", "f");
		x.aliasAttribute(RemnantAssignmentAI.class, "source", "s");

		
		x.alias("WarningBeaconEntityPlugin", WarningBeaconEntityPlugin.class);
		x.aliasAttribute(WarningBeaconEntityPlugin.class, "phase", "p");
		x.aliasAttribute(WarningBeaconEntityPlugin.class, "freqMult", "f");
		x.aliasAttribute(WarningBeaconEntityPlugin.class, "sincePing", "s");
		
		
		
		x.alias("RouteManager", RouteManager.class);
		x.aliasAttribute(RouteManager.class, "routes", "r");
		
		x.alias("RouteData", RouteData.class);
		x.aliasAttribute(RouteData.class, "extra", "x");
		x.aliasAttribute(RouteData.class, "delay", "a");
		x.aliasAttribute(RouteData.class, "source", "o");
		x.aliasAttribute(RouteData.class, "market", "m");
		x.aliasAttribute(RouteData.class, "seed", "s");
		x.aliasAttribute(RouteData.class, "timestamp", "t");
		x.aliasAttribute(RouteData.class, "segments", "e");
		x.aliasAttribute(RouteData.class, "activeFleet", "f");
		x.aliasAttribute(RouteData.class, "daysSinceSeenByPlayer", "d");
		x.aliasAttribute(RouteData.class, "custom", "c");
		x.aliasAttribute(RouteData.class, "current", "r");
		x.aliasAttribute(RouteData.class, "spawner", "p");
		
		x.alias("RouteFleetSpawner", RouteFleetSpawner.class);
		
		x.alias("BaseRouteFleetManager", BaseRouteFleetManager.class);
		x.aliasAttribute(BaseRouteFleetManager.class, "interval", "i");
		x.alias("RuinsFleetRouteManager", RuinsFleetRouteManager.class);
		x.aliasAttribute(RuinsFleetRouteManager.class, "system", "s");
		//x.aliasAttribute(RuinsFleetRouteManager.class, "interval", "i");

		
		x.alias("BaseAssignmentAI", BaseAssignmentAI.class);
		x.aliasAttribute(BaseAssignmentAI.class, "capTracker", "cT");
		x.aliasAttribute(BaseAssignmentAI.class, "buildTracker", "bT");
		
		x.alias("RouteFleetAssignmentAI", RouteFleetAssignmentAI.class);
		x.aliasAttribute(RouteFleetAssignmentAI.class, "gaveReturnAssignments", "gRA");
		
		x.alias("ScavengerFleetAssignmentAI", ScavengerFleetAssignmentAI.class);
		x.alias("DistressCallNormalAssignmentAI", DistressCallNormalAssignmentAI.class);
		x.alias("DistressCallPirateAmbushAssignmentAI", DistressCallPirateAmbushAssignmentAI.class);
		x.alias("DistressCallPirateAmbushTrapAssignmentAI", DistressCallPirateAmbushTrapAssignmentAI.class);
		x.alias("DistressCallResponsePirateAssignmentAI", DistressCallResponsePirateAssignmentAI.class);
		x.alias("DistressCallResponseAssignmentAI", DistressCallResponseAssignmentAI.class);
		x.alias("TutorialLeashAssignmentAI", TutorialLeashAssignmentAI.class);
		
		x.alias("DistressResponseData", DistressResponseData.class);
		x.alias("NESpawnData", NESpawnData.class);
		
		x.alias("OptionalFleetData", OptionalFleetData.class);
		x.aliasAttribute(OptionalFleetData.class, "strength", "s");
		x.aliasAttribute(OptionalFleetData.class, "quality", "q");
		x.aliasAttribute(OptionalFleetData.class, "factionId", "f");
		x.aliasAttribute(OptionalFleetData.class, "fleetType", "t");
		x.aliasAttribute(OptionalFleetData.class, "damage", "d");
		
		
		x.aliasAttribute(BaseAssignmentAI.class, "fleet", "f");
		x.aliasAttribute(BaseAssignmentAI.class, "done", "d");
		//x.aliasAttribute(BaseAssignmentAI.class, "giveInitial", "gI");
		x.aliasAttribute(RouteFleetAssignmentAI.class, "route", "r");
		x.aliasAttribute(ScavengerFleetAssignmentAI.class, "pirate", "p");
		x.aliasAttribute(ScavengerFleetAssignmentAI.class, "piracyCheck", "pC");
		
		x.aliasAttribute(DistressCallNormalAssignmentAI.class, "system", "s");
		x.aliasAttribute(DistressCallNormalAssignmentAI.class, "jumpPoint", "jP");
		x.aliasAttribute(DistressCallNormalAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(DistressCallNormalAssignmentAI.class, "dur", "dur");
		x.aliasAttribute(DistressCallNormalAssignmentAI.class, "contactedPlayer", "cP");
		
		x.aliasAttribute(DistressCallPirateAmbushAssignmentAI.class, "system", "s");
		x.aliasAttribute(DistressCallPirateAmbushAssignmentAI.class, "jumpPoint", "jP");
		x.aliasAttribute(DistressCallPirateAmbushAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(DistressCallPirateAmbushAssignmentAI.class, "dur", "dur");
		
		x.aliasAttribute(DistressCallPirateAmbushTrapAssignmentAI.class, "system", "s");
		x.aliasAttribute(DistressCallPirateAmbushTrapAssignmentAI.class, "jumpPoint", "jP");
		x.aliasAttribute(DistressCallPirateAmbushTrapAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(DistressCallPirateAmbushTrapAssignmentAI.class, "dur", "dur");
		
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "system", "s");
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "dur", "dur");
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "contactedPlayer", "cP");
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "inner", "i");
		x.aliasAttribute(DistressCallResponsePirateAssignmentAI.class, "outer", "o");
		
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "system", "s");
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "dur", "dur");
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "contactedPlayer", "cP");
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "inner", "i");
		x.aliasAttribute(DistressCallResponseAssignmentAI.class, "outer", "o");
		
		x.aliasAttribute(TutorialLeashAssignmentAI.class, "system", "s");
		x.aliasAttribute(TutorialLeashAssignmentAI.class, "jumpPoint", "jP");
		x.aliasAttribute(TutorialLeashAssignmentAI.class, "elapsed", "e");
		x.aliasAttribute(TutorialLeashAssignmentAI.class, "dur", "dur");
		x.aliasAttribute(TutorialLeashAssignmentAI.class, "toGuard", "tG");
		
		
		x.alias("RtSeg", RouteSegment.class);
		x.aliasAttribute(RouteSegment.class, "id", "i");
		x.aliasAttribute(RouteSegment.class, "elapsed", "e");
		x.aliasAttribute(RouteSegment.class, "daysMax", "d");
		x.aliasAttribute(RouteSegment.class, "from", "f");
		x.aliasAttribute(RouteSegment.class, "to", "t");
		
//		x.alias("RPt", RoutePoint.class);
//		x.aliasAttribute(RoutePoint.class, "x", "x");
//		x.aliasAttribute(RoutePoint.class, "y", "y");
//		x.aliasAttribute(RoutePoint.class, "system", "s");
		
//		x.aliasAttribute(RouteSegment.class, "systemFrom", "sF");
//		x.aliasAttribute(RouteSegment.class, "systemTo", "sT");
//		x.aliasAttribute(RouteSegment.class, "custom", "c");
//		x.aliasAttribute(RouteSegment.class, "entityFrom", "eF");
//		x.aliasAttribute(RouteSegment.class, "entityTo", "eT");
		//x.aliasAttribute(RouteSegment.class, "vectorLocation", "vL");
		

		
		x.alias("TowCable", TowCable.class);
		x.alias("TowCableBuff", TowCableBuff.class);
		x.aliasAttribute(TowCableBuff.class, "buffId", "b");
		x.aliasAttribute(TowCableBuff.class, "frames", "f");
		
		x.alias("TowCable", TowCable.class);
		
		x.alias("BaseCampaignMission", BaseCampaignMission.class);
		x.aliasAttribute(BaseCampaignMission.class, "id", "id");
		x.aliasAttribute(BaseCampaignMission.class, "timestamp", "t");
		x.aliasAttribute(BaseCampaignMission.class, "acceptLocation", "aL");
		x.aliasAttribute(BaseCampaignMission.class, "event", "e");
		
		
		x.alias("BaseEventPlugin", BaseEventPlugin.class);
		x.aliasAttribute(BaseEventPlugin.class, "id", "id");
		x.aliasAttribute(BaseEventPlugin.class, "eventType", "eTp");
		x.aliasAttribute(BaseEventPlugin.class, "eventTarget", "eTg");
		x.aliasAttribute(BaseEventPlugin.class, "market", "mk");
		x.aliasAttribute(BaseEventPlugin.class, "entity", "en");
		x.aliasAttribute(BaseEventPlugin.class, "faction", "fa");
		x.aliasAttribute(BaseEventPlugin.class, "statModId", "sMI");
		x.aliasAttribute(BaseEventPlugin.class, "started", "st");
		x.aliasAttribute(BaseEventPlugin.class, "memory", "mem");
		x.aliasAttribute(BaseEventPlugin.class, "startProbability", "sP");
		

		x.alias("MissionCompletionRep", MissionCompletionRep.class);
		x.aliasAttribute(MissionCompletionRep.class, "successDelta", "sD");
		x.aliasAttribute(MissionCompletionRep.class, "successLimit", "sL");
		x.aliasAttribute(MissionCompletionRep.class, "failureDelta", "fD");
		x.aliasAttribute(MissionCompletionRep.class, "failureLimit", "fL");
		
		x.alias("NearbyEventsEvent", NearbyEventsEvent.class);
		x.aliasAttribute(NearbyEventsEvent.class, "derelictShipInterval", "dSI");
		x.aliasAttribute(NearbyEventsEvent.class, "distressCallInterval", "dCI");
		x.aliasAttribute(NearbyEventsEvent.class, "skipForDistressCalls", "sFDC");
		
		x.alias("CargoPodsEntityPlugin", CargoPodsEntityPlugin.class);
		x.aliasAttribute(CargoPodsEntityPlugin.class, "elapsed", "el");
		x.aliasAttribute(CargoPodsEntityPlugin.class, "maxDays", "mD");
		x.aliasAttribute(CargoPodsEntityPlugin.class, "extraDays", "eD");
		x.aliasAttribute(CargoPodsEntityPlugin.class, "neverExpire", "nE");
		
		
		x.alias("DistressCallAbility", DistressCallAbility.class);
		x.aliasAttribute(DistressCallAbility.class, "performed", "p");
		x.aliasAttribute(DistressCallAbility.class, "numTimesUsed", "nTU");
		x.aliasAttribute(DistressCallAbility.class, "lastUsed", "lU");
		x.aliasAttribute(DistressCallAbility.class, "uses", "u");
		
		x.alias("AbilityUseData", AbilityUseData.class);
		x.aliasAttribute(AbilityUseData.class, "timestamp", "t");
		x.aliasAttribute(AbilityUseData.class, "location", "l");

		x.alias("DistressCallOutcome", DistressCallOutcome.class);
		
		x.alias("DelayedActionScript", DelayedActionScript.class);
		x.aliasAttribute(DelayedActionScript.class, "daysLeft", "dL");
		x.aliasAttribute(DelayedActionScript.class, "done", "d");
		
		
		x.alias("GalatiaMarketScript", GalatiaMarketScript.class);
		x.aliasAttribute(GalatiaMarketScript.class, "market", "m");
		x.aliasAttribute(GalatiaMarketScript.class, "interval", "i");
		
		
		x.alias("CampaignTutorialStage", CampaignTutorialStage.class);
		
		x.alias("CampaignTutorialScript", CampaignTutorialScript.class);
		x.aliasAttribute(CampaignTutorialScript.class, "askedPlayerToSave", "aPTS");
		x.aliasAttribute(CampaignTutorialScript.class, "playerSaved", "pS");
		x.aliasAttribute(CampaignTutorialScript.class, "elapsed", "e");
		x.aliasAttribute(CampaignTutorialScript.class, "lastCheckDistToAncyra", "lCDTA");
		x.aliasAttribute(CampaignTutorialScript.class, "system", "s");
		x.aliasAttribute(CampaignTutorialScript.class, "ancyra", "a");
		x.aliasAttribute(CampaignTutorialScript.class, "derinkuyu", "d");
		x.aliasAttribute(CampaignTutorialScript.class, "stage", "st");
		x.aliasAttribute(CampaignTutorialScript.class, "orbitalResetDone", "oRD");
		x.aliasAttribute(CampaignTutorialScript.class, "debrisField", "dF");
		x.aliasAttribute(CampaignTutorialScript.class, "pirateFleet", "pF");
		x.aliasAttribute(CampaignTutorialScript.class, "detachment", "det");
		x.aliasAttribute(CampaignTutorialScript.class, "intel", "i");
		
		
		x.alias("TutorialMissionIntel", TutorialMissionIntel.class);
		x.alias("TutorialMissionStage", TutorialMissionStage.class);

		
		x.alias("RogueMinerMiscFleetManager", RogueMinerMiscFleetManager.class);
		x.alias("SaveNagScript", SaveNagScript.class);
		
		
		x.alias("ExSalv", ExtraSalvage.class);
		x.aliasAttribute(ExtraSalvage.class, "cargo", "c");
		
		x.alias("SpID", SpecialItemData.class);
		x.aliasAttribute(SpecialItemData.class, "id", "i");
		x.aliasAttribute(SpecialItemData.class, "data", "d");
		
		
		x.alias("BaseGenericPlugin", BaseGenericPlugin.class);
		x.alias("SalvageDefenderModificationPluginImpl", SalvageDefenderModificationPluginImpl.class);
		
		x.alias("CampaignPingSpec", CampaignPingSpec.class);
		x.aliasAttribute(CampaignPingSpec.class, "id", "id");
		x.aliasAttribute(CampaignPingSpec.class, "sounds", "s");
		x.aliasAttribute(CampaignPingSpec.class, "color", "c");
		x.aliasAttribute(CampaignPingSpec.class, "minRange", "mR");
		x.aliasAttribute(CampaignPingSpec.class, "range", "r");
		x.aliasAttribute(CampaignPingSpec.class, "duration", "d");
		x.aliasAttribute(CampaignPingSpec.class, "delay", "de");
		x.aliasAttribute(CampaignPingSpec.class, "width", "w");
		x.aliasAttribute(CampaignPingSpec.class, "alphaMult", "aM");
		x.aliasAttribute(CampaignPingSpec.class, "inFraction", "iF");
		x.aliasAttribute(CampaignPingSpec.class, "useFactionColor", "uFC");
		x.aliasAttribute(CampaignPingSpec.class, "invert", "i");
		x.aliasAttribute(CampaignPingSpec.class, "num", "n");
		
		
//		<c cl="com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetFleetAssignmentAI$EconomyRouteData" z="7570">
//		<cargoCap>3220.0</cargoCap>
//		<fuelCap>1685.0</fuelCap>
//		<personnelCap>485.0</personnelCap>
//		<size>6.0</size>
//		<smuggling>false</smuggling>
//		<from cl="Market" ref="435"></from>
//		<to cl="Market" ref="410"></to>
//		<cargoDeliver z="7571">
//		<com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetFleetAssignmentAI_-CargoQuantityData z="7572">
//		<cargo>organics</cargo>
//		<units>5</units>
//		</com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetFleetAssignmentAI_-CargoQuantityData>
		x.alias("ERDat", EconomyRouteData.class);
		x.aliasAttribute(EconomyRouteData.class, "cargoCap", "c");
		x.aliasAttribute(EconomyRouteData.class, "fuelCap", "f");
		x.aliasAttribute(EconomyRouteData.class, "personnelCap", "p");
		x.aliasAttribute(EconomyRouteData.class, "size", "s");
		x.aliasAttribute(EconomyRouteData.class, "smuggling", "m");
		x.aliasAttribute(EconomyRouteData.class, "from", "r");
		x.aliasAttribute(EconomyRouteData.class, "to", "t");
		x.aliasAttribute(EconomyRouteData.class, "cargoDeliver", "d");
		x.aliasAttribute(EconomyRouteData.class, "cargoReturn", "u");
		
		
		x.alias("CQD", CargoQuantityData.class);
		x.aliasAttribute(CargoQuantityData.class, "cargo", "c");
		x.aliasAttribute(CargoQuantityData.class, "units", "u");
		
		
		x.alias("CommRelayEP", CommRelayEntityPlugin.class);
		x.alias("NavBuoyEP", NavBuoyEntityPlugin.class);
		x.alias("SensorArrayEP", SensorArrayEntityPlugin.class);
		
		x.alias("PopulationAndInfrastructure", PopulationAndInfrastructure.class);
		x.aliasAttribute(BaseIndustry.class, "supply", "s");
		x.aliasAttribute(BaseIndustry.class, "demand", "d");
		x.aliasAttribute(BaseIndustry.class, "income", "i");
		x.aliasAttribute(BaseIndustry.class, "upkeep", "u");
		x.aliasAttribute(BaseIndustry.class, "market", "m");
		x.aliasAttribute(BaseIndustry.class, "id", "id");
		x.aliasAttribute(BaseIndustry.class, "buildProgress", "bP");
		x.aliasAttribute(BaseIndustry.class, "building", "b");
		x.aliasAttribute(BaseIndustry.class, "upgradeId", "uI");
		x.aliasAttribute(BaseIndustry.class, "aiCoreId", "aCI");
		x.aliasAttribute(BaseIndustry.class, "demandReduction", "dR");
		x.aliasAttribute(BaseIndustry.class, "supplyBonus", "sB");
		x.aliasAttribute(BaseIndustry.class, "wasDisrupted", "wD");
		
		
		x.alias("DFInfl", DefaultFleetInflater.class);
		x.alias("DFInflP", DefaultFleetInflaterParams.class);
		x.aliasAttribute(DefaultFleetInflaterParams.class, "seed", "s");
		x.aliasAttribute(DefaultFleetInflaterParams.class, "timestamp", "t");
		x.aliasAttribute(DefaultFleetInflaterParams.class, "persistent", "p");
		x.aliasAttribute(DefaultFleetInflaterParams.class, "quality", "q");
		x.aliasAttribute(DefaultFleetInflaterParams.class, "mode", "m");
		
		
		x.alias("MilRespScr", MilitaryResponseScript.class);
		x.aliasAttribute(MilitaryResponseScript.class, "tracker", "t");
		x.aliasAttribute(MilitaryResponseScript.class, "params", "p");
		x.aliasAttribute(MilitaryResponseScript.class, "elapsed", "e");
		
		
		x.alias("MilRespP", MilitaryResponseParams.class);
		x.aliasAttribute(MilitaryResponseParams.class, "type", "t");
		x.aliasAttribute(MilitaryResponseParams.class, "responseReason", "rR");
		x.aliasAttribute(MilitaryResponseParams.class, "faction", "f");
		x.aliasAttribute(MilitaryResponseParams.class, "actor", "a");
		x.aliasAttribute(MilitaryResponseParams.class, "target", "t");
		x.aliasAttribute(MilitaryResponseParams.class, "responseFraction", "rF");
		x.aliasAttribute(MilitaryResponseParams.class, "responseDuration", "rD");
		x.aliasAttribute(MilitaryResponseParams.class, "travelText", "tT");
		x.aliasAttribute(MilitaryResponseParams.class, "actionText", "aT");
		
		x.alias("BaseMarketConditionPlugin", BaseMarketConditionPlugin.class);
		x.aliasAttribute(BaseMarketConditionPlugin.class, "market", "m");
		x.aliasAttribute(BaseMarketConditionPlugin.class, "condition", "c");
		
		x.alias("ResourceDepositsMC", ResourceDepositsCondition.class);
		
		
		
		x.alias("LuddicPathCells", LuddicPathCells.class);
		x.aliasAttribute(LuddicPathCells.class, "intel", "i");
		
		x.alias("LuddicPathCellsIntel", LuddicPathCellsIntel.class);
		x.aliasAttribute(LuddicPathCellsIntel.class, "sleeper", "s");
		x.aliasAttribute(LuddicPathCellsIntel.class, "sleeperTimeout", "sT");
		x.aliasAttribute(LuddicPathCellsIntel.class, "market", "m");
		x.aliasAttribute(LuddicPathCellsIntel.class, "incidentTracker", "iT");
		x.aliasAttribute(LuddicPathCellsIntel.class, "random", "r");
		x.aliasAttribute(LuddicPathCellsIntel.class, "numIncidentAttempts", "nIA");
		x.aliasAttribute(LuddicPathCellsIntel.class, "incidentDelay", "iD");
		x.aliasAttribute(LuddicPathCellsIntel.class, "incidentType", "iTy");
		x.aliasAttribute(LuddicPathCellsIntel.class, "smuggler", "sm");
		x.aliasAttribute(LuddicPathCellsIntel.class, "prevIncident", "pI");
		x.aliasAttribute(LuddicPathCellsIntel.class, "sincePrevIncident", "sPI");
		x.aliasAttribute(LuddicPathCellsIntel.class, "prevIncidentData", "pID");
		x.aliasAttribute(LuddicPathCellsIntel.class, "inertiaTime", "iTi");
		
		
		
		x.alias("Spaceport", Spaceport.class);
		x.alias("Mining", Mining.class);
		x.alias("GroundDefenses", GroundDefenses.class);
		x.alias("Refining", Refining.class);
		x.alias("MilitaryBase", MilitaryBase.class);
		x.alias("Farming", Farming.class);
		x.alias("LightIndustry", LightIndustry.class);
		x.alias("FuelProduction", FuelProduction.class);
		x.alias("HeavyIndustry", HeavyIndustry.class);
		x.alias("PlanetaryShield", PlanetaryShield.class);
		x.alias("TechMining", TechMining.class);
		x.alias("TradeCenter", TradeCenter.class);
		x.alias("LionsGuardHQ", LionsGuardHQ.class);
		x.alias("Waystation", Waystation.class);
		x.alias("Cryosanctum", Cryosanctum.class);
		x.alias("Cryorevival", Cryorevival.class);
		
		x.alias("OrbitalStation", OrbitalStation.class);
		x.aliasAttribute(OrbitalStation.class, "stationFleet", "sF");
		x.aliasAttribute(OrbitalStation.class, "usingExistingStation", "uES");
		x.aliasAttribute(OrbitalStation.class, "stationEntity", "sE");
		
		
		x.alias("EconomyFleetAssignmentAI", EconomyFleetAssignmentAI.class);
		x.aliasAttribute(EconomyFleetAssignmentAI.class, "origFaction", "oF");
		x.aliasAttribute(EconomyFleetAssignmentAI.class, "factionChangeTracker", "fCT");
		
		x.alias("MercAssignmentAIV2", MercAssignmentAIV2.class);
		x.alias("PatrolAssignmentAIV4", PatrolAssignmentAIV4.class);
		x.alias("DisposableAggroAssignmentAI", DisposableAggroAssignmentAI.class);
		
		x.alias("DisposableFleetManager", DisposableFleetManager.class);
		x.alias("DisposablePirateFleetManager", DisposablePirateFleetManager.class);
		x.alias("DisposableLuddicPathFleetManager", DisposableLuddicPathFleetManager.class);
		
		
		x.alias("PirateBaseIntel", PirateBaseIntel.class);
		x.aliasAttribute(PirateBaseIntel.class, "system", "s");
		x.aliasAttribute(PirateBaseIntel.class, "market", "m");
		x.aliasAttribute(PirateBaseIntel.class, "entity", "e");
		x.aliasAttribute(PirateBaseIntel.class, "elapsedDays", "eD");
		x.aliasAttribute(PirateBaseIntel.class, "duration", "d");
		x.aliasAttribute(PirateBaseIntel.class, "bountyData", "bD");
		x.aliasAttribute(PirateBaseIntel.class, "tier", "t");
		x.aliasAttribute(PirateBaseIntel.class, "matchedStationToTier", "mSTT");
		x.aliasAttribute(PirateBaseIntel.class, "monthlyInterval", "mI");
		x.aliasAttribute(PirateBaseIntel.class, "raidTimeoutMonths", "rTM");
		
		x.alias("BaseBountyData", BaseBountyData.class);
		x.aliasAttribute(BaseBountyData.class, "bountyElapsedDays", "bED");
		x.aliasAttribute(BaseBountyData.class, "bountyDuration", "bD");
		x.aliasAttribute(BaseBountyData.class, "baseBounty", "bB");
		x.aliasAttribute(BaseBountyData.class, "repChange", "rC");
		x.aliasAttribute(BaseBountyData.class, "bountyFaction", "bF");
		
		
		x.alias("LuddicPathBaseIntel", LuddicPathBaseIntel.class);
		x.aliasAttribute(LuddicPathBaseIntel.class, "system", "s");
		x.aliasAttribute(LuddicPathBaseIntel.class, "market", "m");
		x.aliasAttribute(LuddicPathBaseIntel.class, "entity", "e");
		x.aliasAttribute(LuddicPathBaseIntel.class, "elapsedDays", "eD");
		x.aliasAttribute(LuddicPathBaseIntel.class, "duration", "d");
		x.aliasAttribute(LuddicPathBaseIntel.class, "bountyData", "bD");
		x.aliasAttribute(LuddicPathBaseIntel.class, "monthlyInterval", "mI");
		x.aliasAttribute(LuddicPathBaseIntel.class, "monthsNoBounty", "mNB");
		x.aliasAttribute(LuddicPathBaseIntel.class, "large", "l");
		x.aliasAttribute(LuddicPathBaseIntel.class, "random", "r");
		
		
		x.alias("PirateActivity", PirateActivity.class);
		x.aliasAttribute(PirateActivity.class, "intel", "i");
		
		x.alias("PirateActivityIntel", PirateActivityIntel.class);
		x.aliasAttribute(PirateActivityIntel.class, "system", "sy");
		x.aliasAttribute(PirateActivityIntel.class, "source", "so");
		
		
		x.alias("LocalResourcesSubmarketPlugin", LocalResourcesSubmarketPlugin.class);
		x.alias("CryosleeperEntityPlugin", CryosleeperEntityPlugin.class);
		
		x.alias("BaseIntelPlugin", BaseIntelPlugin.class);
		x.aliasAttribute(BaseIntelPlugin.class, "important", "ii");
		x.aliasAttribute(BaseIntelPlugin.class, "timestamp", "tt");
		x.aliasAttribute(BaseIntelPlugin.class, "neverClicked", "nC");
		x.aliasAttribute(BaseIntelPlugin.class, "ended", "ended");
		x.aliasAttribute(BaseIntelPlugin.class, "ending", "ending");
		x.aliasAttribute(BaseIntelPlugin.class, "endingTimeRemaining", "eTR");
		x.aliasAttribute(BaseIntelPlugin.class, "postingLocation", "pLoc");
		x.aliasAttribute(BaseIntelPlugin.class, "postingRangeLY", "pRange");
		
		x.alias("BaseMissionIntel", BaseMissionIntel.class);
		x.aliasAttribute(BaseMissionIntel.class, "randomCancel", "rC");
		x.aliasAttribute(BaseMissionIntel.class, "randomCancelProb", "rCP");
		x.aliasAttribute(BaseMissionIntel.class, "missionResult", "mR");
		x.aliasAttribute(BaseMissionIntel.class, "missionState", "mS");
		x.aliasAttribute(BaseMissionIntel.class, "duration", "dur");
		x.aliasAttribute(BaseMissionIntel.class, "elapsedDays", "eD");
		
		x.alias("MissionResult", MissionResult.class);
		
		
		
		x.alias("PersonBountyIntel", PersonBountyIntel.class);
		x.alias("SystemBountyIntel", SystemBountyIntel.class);
		x.alias("TradeFleetDepartureIntel", TradeFleetDepartureIntel.class);
		x.alias("FactionHostilityIntel", FactionHostilityIntel.class);
		x.alias("ProcurementMissionIntel", ProcurementMissionIntel.class);
		x.alias("SurveyPlanetMissionIntel", SurveyPlanetMissionIntel.class);
		x.alias("AnalyzeEntityMissionIntel", AnalyzeEntityMissionIntel.class);
		x.alias("ProcurementMissionCreator", ProcurementMissionCreator.class);
		x.alias("AnalyzeEntityIntelCreator", AnalyzeEntityIntelCreator.class);
		x.alias("SurveyPlanetIntelCreator", SurveyPlanetIntelCreator.class);
		
		
		x.alias("DeliveryMissionIntel", DeliveryMissionIntel.class);
		x.alias("CoreDiscoverEntityPlugin", CoreDiscoverEntityPlugin.class);
		x.alias("CoreBuildObjectiveTypePicker", CoreBuildObjectiveTypePicker.class);
		
		x.alias("MonthlyReport", MonthlyReport.class);
		x.alias("FDNode", FDNode.class);
		x.aliasAttribute(FDNode.class, "children", "c");
		x.aliasAttribute(FDNode.class, "parent", "p");
		x.aliasAttribute(FDNode.class, "name", "n");
		x.aliasAttribute(FDNode.class, "icon", "i");
		x.aliasAttribute(FDNode.class, "income", "in");
		x.aliasAttribute(FDNode.class, "upkeep", "up");
		x.aliasAttribute(FDNode.class, "totalIncome", "tI");
		x.aliasAttribute(FDNode.class, "totalUpkeep", "tU");
		x.aliasAttribute(FDNode.class, "custom", "c1");
		x.aliasAttribute(FDNode.class, "custom2", "c2");
		x.aliasAttribute(FDNode.class, "mapEntity", "mE");
		x.aliasAttribute(FDNode.class, "tooltipCreator", "tC");
		x.aliasAttribute(FDNode.class, "tooltipParam", "tP");
		
		x.alias("MonthlyReportNodeTooltipCreator", MonthlyReportNodeTooltipCreator.class);
		x.alias("GalatianAcademyStipend", GalatianAcademyStipend.class);
		x.alias("WarSimScript", WarSimScript.class);
		x.alias("PersonBountyManager", PersonBountyManager.class);
		x.alias("SystemBountyManager", SystemBountyManager.class);
		x.alias("PirateBaseManager", PirateBaseManager.class);
		x.alias("PlayerRelatedPirateBaseManager", PlayerRelatedPirateBaseManager.class);
		x.alias("LuddicPathBaseManager", LuddicPathBaseManager.class);
		x.alias("HegemonyInspectionManager", HegemonyInspectionManager.class);
		x.alias("DecivTracker", DecivTracker.class);
		x.alias("MarketDecivData", MarketDecivData.class);
		x.aliasAttribute(MarketDecivData.class, "market", "m");
		x.aliasAttribute(MarketDecivData.class, "stabilityHistory", "sH");
		
		
		x.alias("FactionHostilityManager", FactionHostilityManager.class);
		x.alias("FactionHostilityIntel", FactionHostilityIntel.class);
		x.alias("GenericMissionManager", GenericMissionManager.class);
		
		
		
		
		x.alias("PortsideBarData", PortsideBarData.class);
		x.alias("BarEventManager", BarEventManager.class);
		
		x.alias("PirateBaseRumorBarEvent", PirateBaseRumorBarEvent.class);
		
		x.alias("LuddicFarmerBarEvent", LuddicFarmerBarEvent.class);
		x.alias("LuddicFarmerBarEventCreator", LuddicFarmerBarEventCreator.class);
		
		x.alias("TriTachLoanBarEvent", TriTachLoanBarEvent.class);
		x.alias("TriTachLoanBarEventCreator", TriTachLoanBarEventCreator.class);
		
		x.alias("MercsOnTheRunBarEvent", MercsOnTheRunBarEvent.class);
		x.alias("MercsOnTheRunBarEventCreator", MercsOnTheRunBarEventCreator.class);
		
		x.alias("LuddicCraftBarEvent", LuddicCraftBarEvent.class);
		x.alias("LuddicCraftBarEventCreator", LuddicCraftBarEventCreator.class);
		
		x.alias("ScientistAICoreBarEvent", ScientistAICoreBarEvent.class);
		x.alias("ScientistAICoreBarEventCreator", ScientistAICoreBarEventCreator.class);
		
		x.alias("QuartermasterCargoSwapBarEvent", QuartermasterCargoSwapBarEvent.class);
		x.alias("QuartermasterCargoSwapBarEventCreator", QuartermasterCargoSwapBarEventCreator.class);
		
		x.alias("CorruptPLClerkSuppliesBarEvent", CorruptPLClerkSuppliesBarEvent.class);
		x.alias("CorruptPLClerkSuppliesBarEventCreator", CorruptPLClerkSuppliesBarEventCreator.class);
		
		x.alias("DiktatLobsterBarEvent", DiktatLobsterBarEvent.class);
		x.alias("DiktatLobsterBarEventCreator", DiktatLobsterBarEventCreator.class);
		
		x.alias("DeliveryBarEvent", DeliveryBarEvent.class);
		x.alias("DeliveryBarEventCreator", DeliveryBarEventCreator.class);
		
		x.alias("TriTachMajorLoanBarEvent", TriTachMajorLoanBarEvent.class);
		x.alias("TriTachMajorLoanBarEventCreator", TriTachMajorLoanBarEventCreator.class);
		
		x.alias("TriTachLoanIncentiveScript", TriTachLoanIncentiveScript.class);
		x.alias("DeliveryFailureConsequences", DeliveryFailureConsequences.class);
		
		
		x.alias("EconomyFleetRouteManager", EconomyFleetRouteManager.class);
		x.alias("ShippingDisruption", ShippingDisruption.class);
		
		
		x.alias("DistressCallIntel", DistressCallIntel.class);
		x.alias("HyperStormBoost", HyperStormBoost.class);
		x.alias("AsteroidImpact", AsteroidImpact.class);
		
		
	}

//	// problem: adding a new field could break saves
//	public static void autoAlias(XStream x, Class c, String name, int level) {
//		x.alias(name, c);
//
//		char curr = 'a';
//		for (Field f : c.getFields()) {
//			if (Modifier.isTransient(f.getModifiers())) continue;
//			String alias = "" + curr;
//			if (level > 0) alias += level;
//			x.alias("", c);
//			curr++;
//		}
//	}
	
	

	
	
	@Override
	public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
		if (ship.isFighter()) return null;
		
		Set<String> derelicts = new HashSet<String>();
		derelicts.add("warden");
		derelicts.add("defender");
		derelicts.add("picket");
		derelicts.add("sentry");
		derelicts.add("berserker");
		derelicts.add("bastillion");
		
		String hullId = ship.getHullSpec().getHullId();
		if (!derelicts.contains(hullId) && !ship.getVariant().hasHullMod(HullMods.AUTOMATED)) return null;
		
		//HullSize size = ship.getHullSize();
		
		ShipAIConfig config = new ShipAIConfig();
		config.alwaysStrafeOffensively = true;
		config.backingOffWhileNotVentingAllowed = false;
		config.turnToFaceWithUndamagedArmor = false;
		config.burnDriveIgnoreEnemies = true;
		config.personalityOverride = Personalities.RECKLESS;
		
		return new PluginPick<ShipAIPlugin>(Global.getSettings().createDefaultShipAI(ship, config), PickPriority.CORE_SPECIFIC);
	}
	

	
	
}


	
	
	
	
	
	
	
	
	
	
	
