package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.impl.campaign.intel.events.PirateBasePirateActivityCause2;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetFilter;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For hostile activity ("HA") related tasks.
 * 
 *	HA_CMD <action> <parameters>
 */
public class HA_CMD extends BaseCommandPlugin {

	public static final String PATHER_AGREEMENT = "$patherAgreement";
	public static final String PATHER_AGREEMENT_PERMANENT = "$patherAgreementPermanent";
	
	public static void setPatherAgreement(boolean agreement, float duration) {
		if (!agreement) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(PATHER_AGREEMENT);
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(PATHER_AGREEMENT_PERMANENT);
			return;
		}
		if (duration <= 0) {
			Global.getSector().getPlayerMemoryWithoutUpdate().set(PATHER_AGREEMENT_PERMANENT, true);
		}
		Global.getSector().getPlayerMemoryWithoutUpdate().set(PATHER_AGREEMENT, true, duration);
	}
	public static boolean playerHasPatherAgreement() {
		//if (true) return true;
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT);
	}
	public static boolean playerPatherAgreementIsPermanent() {
		//if (true) return true;
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT_PERMANENT);
	}
	
	public static float getPlayerPatherAgreementDays() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getExpire(PATHER_AGREEMENT);
	}

	
	public static int computePirateProtectionPaymentPerMonth(PirateBaseIntel intel) {
		int perTick = computePirateProtectionPaymentPerTick(intel);
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		return (int) (perTick * numIter);
	}
	
	public static int computePirateProtectionPaymentPerTick(PirateBaseIntel intel) {
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;
		
		int payment = 0;
		float feeFraction = Global.getSettings().getFloat("pirateProtectionPaymentFraction");
		
		for (MarketAPI market : PirateBasePirateActivityCause2.getColoniesAffectedBy(intel)) {
			//if (market.isHidden()) continue;
			//if (!Factions.DIKTAT.equals(market.getFaction().getId()) && !market.isPlayerOwned()) continue;
			if (!market.isPlayerOwned()) continue;
			
			payment += (int) (market.getGrossIncome() * f) * feeFraction;
		}
		
		return payment;
	}
	
	public static class StationKingScript implements EveryFrameScript, TooltipCreator, 
													 EconomyTickListener, 
													 FleetEventListener,
													 CurrentLocationChangedListener,
													 ColonyPlayerHostileActListener {

		protected boolean done = false;
		protected PirateBaseIntel intel;
		protected Random random = new Random();
		//protected IntervalUtil interval;
		
		protected float untilMercCheck = 0f;
		protected float mercProbMult = 1f;
		protected transient boolean spawnMerc = false;
		
		public StationKingScript(PirateBaseIntel intel) {
			this.intel = intel;
			Global.getSector().getListenerManager().addListener(this);
			//interval = new IntervalUtil(0.01f, maxInterval)
			resetMercCheckDelay();
		}
		
		protected Object readResolve() {
			if (random == null) {
				random = new Random();
			}
			return this;
		}
		
		protected void resetMercCheckDelay() {
			untilMercCheck = Global.getSettings().getFloatFromArray("pirateProtectionMercSpawnInterval", 0) +
					(Global.getSettings().getFloatFromArray("pirateProtectionMercSpawnInterval", 1) - 
					Global.getSettings().getFloatFromArray("pirateProtectionMercSpawnInterval", 0)) * Misc.random.nextFloat();			
		}
		
		protected void setDone() {
			done = true;
			Global.getSector().getListenerManager().removeListener(this);
		}
		
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}

		public void reportEconomyTick(int iterIndex) {
			MonthlyReport report = SharedData.getData().getCurrentReport();
			FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
			
			int payment = computePirateProtectionPaymentPerTick(intel);
			if (payment <= 0) return;
			
			FDNode paymentNode = report.getNode(marketsNode, "pirate_payment"); 
			paymentNode.name = "Protection payment";
			//paymentNode.custom = MonthlyReport.EXPORTS;
			//paymentNode.mapEntity = market.getPrimaryEntity();
			paymentNode.upkeep += payment;
			paymentNode.tooltipCreator = this;
			paymentNode.mapEntity = intel.getEntity();
			
			paymentNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
			//paymentNode.icon = intel.getIcon();
			//}			
		}

		public void reportEconomyMonthEnd() {
			
		}
		
		public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
			float opad = 10f;
			tooltip.addSpacer(-10f);
			tooltip.addPara("Protection payment to the " + intel.getBaseCommander().getPost() + " of " +  
							intel.getEntity().getName() + ". The pirate base is located in the " + 
							intel.getSystem().getNameWithLowercaseTypeShort() + ".", opad);
		}

		public float getTooltipWidth(Object tooltipParam) {
			return 450;
		}

		public boolean isTooltipExpandable(Object tooltipParam) {
			return false;
		}
		
		public void advance(float amount) {
			if (isDone()) return;

			if (intel.isEnded() || intel.isEnding()) {
				if (intel.playerHasDealWithBaseCommander()) {
					// the "station destroyed by mercs (or whoever)" case
					intel.setPlayerHasDealWithBaseCommander(false);
				}
				setDone();
				return;
			}
			
			
			if (intel.playerHasDealWithBaseCommander() && intel.getSystem().isCurrentLocation()) {
				CampaignFleetAPI station = Misc.getStationFleet(intel.getEntity());
				List<CampaignFleetAPI> fleets = new ArrayList<CampaignFleetAPI>(intel.getSystem().getFleets());
				fleets.add(station);
				for (CampaignFleetAPI fleet : fleets) {
					MarketAPI source = Misc.getSourceMarket(fleet);
					if (source == intel.getMarket() || fleet == station) {// &&
							//fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE)) {
						Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(),
								MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, "psk_deal", true, 0.1f);
						fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_PLAYER_BATTLE_JOIN_TOFF, true, 0.1f);
					}
				}
			}
			
			float days = Global.getSector().getClock().convertToDays(amount);
			untilMercCheck -= days;
			if (untilMercCheck < 0) untilMercCheck = 0;
			
			if (spawnMerc) {
				spawnMerc();
				spawnMerc = false;
			}
		}

		public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		}

		public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
			if (!battle.isPlayerInvolved()) return;
			
			CampaignFleetAPI station = Misc.getStationFleet(intel.getEntity());
			for (CampaignFleetAPI curr : battle.getNonPlayerSideSnapshot()) {
				if (!curr.knowsWhoPlayerIs() && curr != station) continue;
				
				MarketAPI source = Misc.getSourceMarket(curr);
				
				if (source == intel.getMarket() || curr == station) {
					endDeal(false, null);
					return;
				}
			}
		}
		
		public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, CargoAPI cargo) {
			if (market == intel.getMarket() && !actionData.secret && !cargo.isEmpty()) {
				endDeal(false, dialog);
			}
		}

		public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
			if (market == intel.getMarket() && !actionData.secret) {
				endDeal(false, dialog);
			}
		}

		public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
			if (market == intel.getMarket() && !actionData.secret) {
				endDeal(false, dialog);
			}
		}

		public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
			if (market == intel.getMarket() && !actionData.secret) {
				endDeal(false, dialog);
			}
		}
		
		public void endDeal(boolean amicable, InteractionDialogAPI dialog) {
			if (!intel.playerHasDealWithBaseCommander()) return;
			
			TextPanelAPI textPanel = dialog == null ? null : dialog.getTextPanel();
			
			intel.setPlayerHasDealWithBaseCommander(false);
			
			if (amicable) {
				intel.sendUpdateIfPlayerHasIntel(PirateBaseIntel.DEAL_CANCELLED_PARAM, textPanel);
			} else {
				intel.sendUpdateIfPlayerHasIntel(PirateBaseIntel.DEAL_BROKEN_PARAM, textPanel);
			}
			
			if (!amicable) {
				Misc.incrUntrustwortyCount();
			}
			
			if (amicable) {
				Misc.adjustRep(intel.getBaseCommander(), -(RepRewards.HIGH + 0.01f), textPanel);
				Misc.adjustRep(intel.getBaseCommander().getFaction().getId(), -(RepRewards.MEDIUM + 0.01f), textPanel);
			} else {
				Misc.adjustRep(intel.getBaseCommander(), -(RepRewards.HIGH * 2f), textPanel);
				Misc.adjustRep(intel.getBaseCommander().getFaction().getId(), -(RepRewards.MEDIUM * 2f), textPanel);
			}
			
			
			CampaignFleetAPI station = Misc.getStationFleet(intel.getEntity());
			List<CampaignFleetAPI> fleets = new ArrayList<CampaignFleetAPI>(intel.getSystem().getFleets());
			fleets.add(station);
			for (CampaignFleetAPI fleet : fleets) {
				MarketAPI source = Misc.getSourceMarket(fleet);
				if (source == intel.getMarket() || fleet == station) {// &&
						//fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE)) {
					Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(),
							MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, "psk_deal", false, 0);
					fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_ALLOW_PLAYER_BATTLE_JOIN_TOFF);
				}
			}
			
			setDone();
		}

		public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
			if (curr == intel.getSystem()) {
				float prob = Global.getSettings().getFloat("pirateProtectionMercSpawnChance");
				prob *= mercProbMult;
				
				// uhcomment to spawn merc whenever the player enters the system with the base
//				untilMercCheck = 0f;
//				prob = 1f;
				
				if (untilMercCheck <= 0 && random.nextFloat() < prob) {
					mercProbMult *= Global.getSettings().getFloat("pirateProtectionMercSpawnChanceMult");
					spawnMerc = true;
					resetMercCheckDelay();
				}
			}
		}
		
		public void spawnMerc() {
			
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			
			JumpPointAPI jp = Misc.findNearestJumpPointTo(pf);
			float dist = Misc.getDistance(pf, jp);
			if (dist > 500f) {
				return;
			}
			
			float f = 10f;
			StarSystemAPI system = intel.getSystem();
			int difficulty = 0;
			
			
			PirateBaseTier tier = intel.getTier();
			switch (tier) {
			case TIER_1_1MODULE: difficulty = 3; break;
			case TIER_2_1MODULE: difficulty = 4; break;
			case TIER_3_2MODULE: difficulty = 5; break;
			case TIER_4_3MODULE: difficulty = 6; break;
			case TIER_5_3MODULE: difficulty = 7; break;
			}
			difficulty += random.nextInt(3);
			
			
			FleetCreatorMission m = new FleetCreatorMission(random);
			m.beginFleet();
			
			Vector2f hLoc = system.getLocation();
			m.createQualityFleet(difficulty, Factions.MERCENARY, hLoc);
			//m.triggerFleetAllowLongPursuit();
			m.triggerSetFleetFaction(Factions.INDEPENDENT);
			m.triggerMakeLowRepImpact();
			m.triggerFleetSetAllWeapons();
			m.triggerFleetMakeImportantPermanent(null);
			m.triggerSetFleetMemoryValue("$psk_merc", true);

			CampaignFleetAPI fleet = m.createFleet();
			if (fleet != null) {
				
				system.addEntity(fleet);
				
				Vector2f loc = new Vector2f(pf.getLocation());
				loc = Misc.getPointAtRadius(loc, 400f);
				fleet.setLocation(loc.x, loc.y);
				SectorEntityToken e = system.createToken(loc);
				fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, e, 2f + 3f * random.nextFloat(), "preparing to attack " + intel.getEntity().getName());
				//CampaignFleetAPI station = Misc.getStationFleet(intel.getEntity()); 
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, intel.getEntity(), 20f, "moving to attack " + intel.getEntity().getName());
				//Misc.giveStandardReturnToSourceAssignments(fleet, false);
				fleet.addScript(new MissionFleetAutoDespawn(null, fleet));
				
				fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, pf, 1f, null);
				
			}
			
		}
	}

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
		
		//MarketAPI market = dialog.getInteractionTarget().getMarket();
		StarSystemAPI system = null;
		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
		}
				
		if ("baseInvolved".equals(action)) {
			if (system == null) return false;
			PirateBaseIntel base = PirateBaseIntel.getIntelFor(dialog.getInteractionTarget());
			if (base == null) return false;
			
			return !PirateBasePirateActivityCause2.getColoniesAffectedBy(base).isEmpty();
		} else if ("addStationKingScript".equals(action)) {
			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			PirateBaseIntel base = PirateBaseIntel.getIntelFor(dialog.getInteractionTarget());
			if (base == null) return false;
			base.getSystem().addScript(new StationKingScript(base));
			base.sendUpdate(PirateBaseIntel.DEAL_MADE_PARAM, text);
			
			Misc.adjustRep(base.getBaseCommander(), RepRewards.HIGH, text);
			Misc.adjustRep(base.getBaseCommander().getFaction().getId(), RepRewards.MEDIUM, text);
		} else if ("endStationKingDeal".equals(action)) {
			PirateBaseIntel base = PirateBaseIntel.getIntelFor(dialog.getInteractionTarget());
			if (base == null) return false;
			for (EveryFrameScript curr : base.getSystem().getScripts()) {
				if (curr instanceof StationKingScript) {
					StationKingScript script = (StationKingScript) curr;
					script.endDeal(true, dialog);
					return true;
				}
			}
			return false;
		} else if ("playerColoniesHavePatherActvity".equals(action)) {
			for (MarketAPI market : Misc.getPlayerMarkets(false)) {
				float interest = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
				if (market.hasCondition(Conditions.PATHER_CELLS) && interest > 0) {
					return true;
				}
			}
//			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(HostileActivityIntel.class)) {
//				HostileActivityIntel curr = (HostileActivityIntel) intel;
//				HostileActivityPlugin a = curr.getActivityOfClass(LuddicPathHostileActivityPluginImpl.class);
//				if (a != null && a.getEffectMagnitude() > 0f) {
//					return true;
//				}
//			}
		} else if ("payMegaTithe".equals(action)) {
			int megaTithe = memory.getInt("$LP_megaTithe");
			int dur = memory.getInt("$LP_megaTitheDuration"); 
			cargo.getCredits().subtract(megaTithe);
			AddRemoveCommodity.addCreditsLossText(megaTithe, text);
			if (cargo.getCredits().get() <= 0) {
				cargo.getCredits().set(0);
			}
			setPatherAgreement(true, dur);
			
			final String factionId = Factions.LUDDIC_PATH;
			float range = 100000f;
			List<CampaignFleetAPI> fleets = Misc.findNearbyFleets(pf, range, new FleetFilter() {
				public boolean accept(CampaignFleetAPI curr) {
					return curr.getFaction().getId().equals(factionId);
				}
			});
			
			for (CampaignFleetAPI curr : fleets) {
				if (curr.getAI() != null) {
					curr.getAI().setActionTextOverride(null);
				}
				if (curr.getAI() instanceof ModularFleetAIAPI) {
					ModularFleetAIAPI mAI = (ModularFleetAIAPI) curr.getAI();
					mAI.getTacticalModule().forceTargetReEval();
				}
				Misc.giveStandardReturnToSourceAssignments(curr, true);
			}
			
			return true;
		} else if ("computeMegaTithe".equals(action)) {
			float credits = cargo.getCredits().get();
			
			float normalTithe = (int) Global.getSector().getPlayerFleet().getFleetPoints() * 200;
			
			int tithe = 0;
			
			float tithePerPointPerColonySize = Global.getSettings().getFloat("luddicPathTithePerPointOfInterestPerColonySize");
			int titheDuration = Global.getSettings().getInt("luddicPathTitheDurationDays");
			
			for (MarketAPI market : Misc.getPlayerMarkets(false)) {
				float interest = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
				interest += market.getSize();
				tithe += interest * market.getSize() * tithePerPointPerColonySize;
			}
			
			tithe += normalTithe;
			
			tithe = (int) Misc.getRounded(tithe);
			
			memoryMap.get(MemKeys.LOCAL).set("$LP_megaTithe", (int)tithe, 0);
			memoryMap.get(MemKeys.LOCAL).set("$LP_megaTitheDGS", Misc.getWithDGS(tithe), 0);
			memoryMap.get(MemKeys.LOCAL).set("$LP_megaTitheDuration", titheDuration, 0);
			
			return tithe > 0;
		}
		
		return false;
	}

	
	
}
