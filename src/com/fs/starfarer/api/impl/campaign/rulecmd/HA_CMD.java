package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
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
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.GensHannanMachinations;
import com.fs.starfarer.api.impl.campaign.intel.LuddicChurchImmigrationDeal;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership.AgreementEndingType;
import com.fs.starfarer.api.impl.campaign.intel.SindrianDiktatFuelDeal;
import com.fs.starfarer.api.impl.campaign.intel.TriTachyonDeal;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HALuddicPathDealFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HAPirateKingDealFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HegemonyHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchStandardActivityCause;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicPathHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.PerseanLeagueHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.PirateBasePirateActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.SindrianDiktatHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.SindrianDiktatStandardActivityCause;
import com.fs.starfarer.api.impl.campaign.intel.events.StandardPerseanLeagueActivityCause;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TriTachyonCommerceRaiding;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.KnightsOfLuddTakeoverExpedition;
import com.fs.starfarer.api.impl.campaign.intel.group.PerseanLeagueBlockade;
import com.fs.starfarer.api.impl.campaign.intel.group.PerseanLeaguePunitiveExpedition;
import com.fs.starfarer.api.impl.campaign.intel.group.SindrianDiktatPunitiveExpedition;
import com.fs.starfarer.api.impl.campaign.intel.group.TTMercenaryAttack;
import com.fs.starfarer.api.impl.campaign.intel.group.TTMercenaryReversedAttack;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
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
		// second part is to make 0.96a-RC8 saves work now that the correct variable is being set
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT) ||
				Global.getSector().getMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT);
	}
	public static boolean playerPatherAgreementIsPermanent() {
		//if (true) return true;
		// second part is to make 0.96a-RC8 saves work now that the correct variable is being set
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT_PERMANENT) || 
				Global.getSector().getMemoryWithoutUpdate().getBoolean(PATHER_AGREEMENT_PERMANENT);
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
			if (station == null) return;
			if (intel.getEntity() == null) return;
			LocationAPI cLoc = null;
			if (fleet != null) cLoc = fleet.getContainingLocation();
			if (cLoc == null && primaryWinner != null) cLoc = primaryWinner.getContainingLocation();
			if (intel.getEntity().getContainingLocation() != cLoc) {
				return;
			}
			
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

	protected SectorEntityToken other;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		other = dialog.getInteractionTarget();
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
			
			return baseInvolved(system, base);
		} else if ("addStationKingScript".equals(action)) {
			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			PirateBaseIntel base = PirateBaseIntel.getIntelFor(dialog.getInteractionTarget());
			if (base == null) return false;
			base.getSystem().addScript(new StationKingScript(base));
			
			// feels extraneous given the below also sending an update
			//base.sendUpdate(PirateBaseIntel.DEAL_MADE_PARAM, text);
			
			HostileActivityEventIntel ha = HostileActivityEventIntel.get();
			if (ha != null) {
				int tier = base.getTier().ordinal();
				if (tier < 0) tier = 0;
				if (tier > 4) tier = 4;
				int points = -1 * Global.getSettings().getIntFromArray("HA_pirateBase", tier);
				HAPirateKingDealFactor factor = new HAPirateKingDealFactor(points);
				ha.addFactor(factor, dialog);
			}
			
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
			LuddicPathHostileActivityFactor.avertOrAbortAttack();
			
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
			
			HostileActivityEventIntel ha = HostileActivityEventIntel.get();
			if (ha != null) {
				int points = -1 * Global.getSettings().getInt("HA_megaTithe");
				HALuddicPathDealFactor factor = new HALuddicPathDealFactor(points);
				ha.addFactor(factor, dialog);
			}
			
			return true;
		} else if ("gavePKToPather".equals(action)) {
			HostileActivityEventIntel ha = HostileActivityEventIntel.get();
			if (ha != null) {
				int points = -1 * Global.getSettings().getInt("HA_givePK");
				HALuddicPathDealFactor factor = new HALuddicPathDealFactor(points);
				ha.addFactor(factor, dialog);
			}
			LuddicPathHostileActivityFactor.avertOrAbortAttack();
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
		} else if ("kazeronInstallNanoforge".equals(action)) {
			MarketAPI kazeron = PerseanLeagueHostileActivityFactor.getKazeron(false);
			if (kazeron != null) {
				SpecialItemData data = new SpecialItemData(Items.PRISTINE_NANOFORGE, null);
				for (Industry ind : kazeron.getIndustries()) {
					if (ind.wantsToUseSpecialItem(data)) {
						ind.setSpecialItem(data);
						break;
					}
				}
			}
		} else if ("kazeronNanoforgeMissing".equals(action)) {
			MarketAPI kazeron = PerseanLeagueHostileActivityFactor.getKazeron(false);
			if (kazeron == null) return true;
			for (Industry ind : kazeron.getIndustries()) {
				if (ind.getSpecialItem() == null) continue;
				if (ind.getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE)) {
					return false;
				}
			}
			return true;
		} else if ("stopPayingHouseHannan".equals(action)) {
			PerseanLeagueMembership.stopPayingHouseHannan(params.get(1).getBoolean(memoryMap), dialog);
//			PerseanLeagueMembership.setPayingHouseHannan(false);
//			Misc.incrUntrustwortyCount();
		} else if ("leaveLeague".equals(action)) {
			PerseanLeagueMembership m = PerseanLeagueMembership.get();
			if (m != null) {
				// do this here so that the intel is shown; would also happen automatically
				// when the membership is ended
				PerseanLeagueMembership.stopPayingHouseHannan(false, dialog);
				
				m.endMembership(AgreementEndingType.ENDED, dialog);
				//m.sendUpdate(new Object(), dialog.getTextPanel());
			}
		} else if ("printTriTachDealRepReq".equals(action)) {
			FactionAPI triTach = Global.getSector().getFaction(Factions.TRITACHYON);
			CoreReputationPlugin.addRequiredStanding(triTach, RepLevel.INHOSPITABLE, null, dialog.getTextPanel(), null, null, 0f, true);
			CoreReputationPlugin.addCurrentStanding(triTach, null, dialog.getTextPanel(), null, null, 0f);
		} else if ("isTTCRInProgress".equals(action)) {
			return TriTachyonCommerceRaiding.get() != null;
		} else if ("isTTRaidingPlayerCommerce".equals(action)) {
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			if (intel != null) {
				EventFactor factor = intel.getFactorOfClass(TriTachyonHostileActivityFactor.class);
				if (factor instanceof TriTachyonHostileActivityFactor) {
					TriTachyonHostileActivityFactor ttFactor = (TriTachyonHostileActivityFactor) factor;
					return ttFactor.getProgress(intel) > 0;
				}
			}
		} else if ("makeTriTachDeal".equals(action)) {
			if (TriTachyonDeal.get() == null) {
				new TriTachyonDeal(dialog);
			}
		} else if ("breakTriTachDeal".equals(action)) {
			if (TriTachyonDeal.get() != null) {
				TriTachyonDeal.get().endAgreement(com.fs.starfarer.api.impl.campaign.intel.TriTachyonDeal.AgreementEndingType.BROKEN, dialog);
			}
		} else if ("printLCDealRepReq".equals(action)) {
			FactionAPI luddicChurch = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
			CoreReputationPlugin.addRequiredStanding(luddicChurch, RepLevel.INHOSPITABLE, null, dialog.getTextPanel(), null, null, 0f, true);
			CoreReputationPlugin.addCurrentStanding(luddicChurch, null, dialog.getTextPanel(), null, null, 0f);
		} else if ("breakLCDeal".equals(action)) {
			if (LuddicChurchImmigrationDeal.get() != null) {
				LuddicChurchImmigrationDeal.get().endAgreement(com.fs.starfarer.api.impl.campaign.intel.LuddicChurchImmigrationDeal.AgreementEndingType.BROKEN, dialog);
			}
		} else if ("KOLTakeoverInProgress".equals(action)) {
			return isKOLTakeoverInProgress();
		} else if ("makeLuddicChurchDeal".equals(action)) {
			if (LuddicChurchImmigrationDeal.get() == null) {
				new LuddicChurchImmigrationDeal(dialog);
			}
		} else if ("joinLeague".equals(action)) {
			new PerseanLeagueMembership(dialog);
			
			avertOrEndPLBlockadeAsNecessary();
			HegemonyHostileActivityFactor.avertInspectionIfNotInProgress();
		} else if ("canJoinLeague".equals(action)) {
			return canPlayerJoinTheLeague();	
		} else if ("isPLExpeditionInProgress".equals(action)) {
			return isPLExpeditionInProgress();
		} else if ("isPLProtectingPlayerSpace".equals(action)) {
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			if (intel != null) {
				EventFactor factor = intel.getFactorOfClass(PerseanLeagueHostileActivityFactor.class);
				if (factor instanceof PerseanLeagueHostileActivityFactor) {
					PerseanLeagueHostileActivityFactor plFactor = (PerseanLeagueHostileActivityFactor) factor;
					return plFactor.getProgress(intel) > 0;
				}
			}			
		} else if ("canSendPLPunitiveExpedition".equals(action)) {
			return canSendPerseanLeaguePunitiveExpedition();
		} else if ("updateLeagueData".equals(action)) {
			float duesF = Global.getSettings().getFloat("perseanLeagueFeeFraction");
			float hannanF = Global.getSettings().getFloat("houseHannanFeeFraction");
			
			memoryMap.get(MemKeys.LOCAL).set("$plDuesPercent", (int)Math.round(duesF * 100f) + "%", 0);
			memoryMap.get(MemKeys.LOCAL).set("$hannanBribePercent", (int)Math.round(hannanF * 100f) + "%", 0);
			return canPlayerJoinTheLeague();	
			
		} else if ("canRemakeDealWithHouseHannan".equals(action)) {
			return GensHannanMachinations.canRemakeDealWithHouseHannan();
		} else if ("endGHMachinations".equals(action)) {
			GensHannanMachinations m = GensHannanMachinations.get();
			if (m != null) {
				m.endMachinations(text);
			}
		} else if ("doKOLTTakeover".equals(action)) {
			KnightsOfLuddTakeoverExpedition takeover = KnightsOfLuddTakeoverExpedition.get();
			if (takeover != null) {
				takeover.performTakeover(true);
			}
		} else if ("updateKOLTArmadaData".equals(action)) {
			KnightsOfLuddTakeoverExpedition takeover = KnightsOfLuddTakeoverExpedition.get();
			if (takeover != null) {
				MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
				mem.set("$KOLT_target", takeover.getBlockadeParams().specificMarket.getName());
				return true;
			}
		} else if ("updateTTMAData".equals(action)) {
			MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
			
//			int bribe = Global.getSettings().getInt("triTachyonMercBribe");
//			int bribeSmall = bribe / 2;
//			mem.set("$bribeSmall", Misc.getWithDGS(bribeSmall));
//			mem.set("$bribe", Misc.getWithDGS(bribe));
			
			TTMercenaryAttack attack = TTMercenaryAttack.get();
			StarSystemAPI target = TriTachyonHostileActivityFactor.getPrimaryTriTachyonSystem();
			boolean reversible = attack != null && !attack.isSpawning() && !attack.isFailed() &&
					!attack.isSucceeded() && !attack.isAborted() && !attack.isEnding() && !attack.isEnded() &&
					target != null;
			
			mem.set("$attackReversible", reversible);
			if (target != null) {
				mem.set("$triTachSystem", target.getNameWithLowercaseTypeShort());
			}
			return true;
			
		} else if ("retargetTTMA".equals(action)) {
			TTMercenaryReversedAttack.sendReversedAttack(dialog);
		} else if ("computeSacredProtectorsData".equals(action)) {
			MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
			int supplies = (int) Global.getSector().getPlayerFleet().getFleetPoints() * 1;
			mem.set("$SP_supplies", (int)supplies, 0);
			return true;
		} else if ("sacredProtectorsCheckCargoPods".equals(action)) {
			MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
			int supplies = mem.getInt("$SP_supplies");
			return sacredProtectorsCheckCargoPods(supplies);
		} else if ("knightsHasslingPlayerColonies".equals(action)) {
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			if (intel != null) {
				HostileActivityCause2 cause = intel.getActivityCause(LuddicChurchHostileActivityFactor.class, LuddicChurchStandardActivityCause.class);
				if (cause instanceof LuddicChurchStandardActivityCause) {
					LuddicChurchStandardActivityCause lcCause = (LuddicChurchStandardActivityCause) cause;
					return lcCause.getProgress() > 0;
				}
			}
		} else if ("printSDDealRepReq".equals(action)) {
			FactionAPI diktat = Global.getSector().getFaction(Factions.DERELICT);
			CoreReputationPlugin.addRequiredStanding(diktat, RepLevel.INHOSPITABLE, null, dialog.getTextPanel(), null, null, 0f, true);
			CoreReputationPlugin.addCurrentStanding(diktat, null, dialog.getTextPanel(), null, null, 0f);
		} else if ("breakSDDeal".equals(action)) {
			if (SindrianDiktatFuelDeal.get() != null) {
				SindrianDiktatFuelDeal.get().endAgreement(com.fs.starfarer.api.impl.campaign.intel.SindrianDiktatFuelDeal.AgreementEndingType.BROKEN, dialog);
			}
		} else if ("makeDiktatDeal".equals(action)) {
			if (SindrianDiktatFuelDeal.get() == null) {
				new SindrianDiktatFuelDeal(dialog);
			}
		} else if ("diktatConcernedByFuelProd".equals(action)) {
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			if (intel != null) {
				HostileActivityCause2 cause = intel.getActivityCause(SindrianDiktatHostileActivityFactor.class, SindrianDiktatStandardActivityCause.class);
				if (cause instanceof SindrianDiktatStandardActivityCause) {
					SindrianDiktatStandardActivityCause lcCause = (SindrianDiktatStandardActivityCause) cause;
					return lcCause.getProgress() > 0;
				}
			}
		}
		
//		else if ("printLeagueRequirements".equals(action)) {
//			
//		}
		
		return false;
	}
	
	public boolean sacredProtectorsCheckCargoPods(int remove) {
		float maxPodsDist = 1500f;
		for (SectorEntityToken entity : other.getContainingLocation().getAllEntities()) {
			if (Entities.CARGO_PODS.equals(entity.getCustomEntityType())) {
				VisibilityLevel vLevel = entity.getVisibilityLevelTo(other);
				if (entity.getCustomPlugin() instanceof CargoPodsEntityPlugin) {
					float dist = Misc.getDistance(other, entity);
					if (dist > maxPodsDist) continue;
					
					if (vLevel == VisibilityLevel.COMPOSITION_DETAILS ||
							vLevel == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
						CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin) entity.getCustomPlugin();
						if (plugin.getElapsed() <= 1f && entity.getCargo() != null) {
							float supplies = entity.getCargo().getSupplies();
							if (supplies >= remove) {
								entity.getCargo().removeSupplies(remove * 2);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	
	public static boolean isKOLTakeoverInProgress() {
		return KnightsOfLuddTakeoverExpedition.get() != null && 
				!KnightsOfLuddTakeoverExpedition.get().isSucceeded() && 
				!KnightsOfLuddTakeoverExpedition.get().isAborted() && 
				!KnightsOfLuddTakeoverExpedition.get().isFailed();
	}
	
	public static boolean isPLExpeditionInProgress() {
		return PerseanLeaguePunitiveExpedition.get() != null && 
				!PerseanLeaguePunitiveExpedition.get().isSucceeded() && 
				!PerseanLeaguePunitiveExpedition.get().isAborted() && 
				!PerseanLeaguePunitiveExpedition.get().isFailed();
	}
	
	public static void avertOrEndKOLTakeoverAsNecessary() {
		KnightsOfLuddTakeoverExpedition takeover = KnightsOfLuddTakeoverExpedition.get();
		if (takeover != null) {
			takeover.finish(false);
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel != null) {
			HAERandomEventData data = intel.getRollDataForEvent();
			if (data != null && data.factor instanceof LuddicChurchHostileActivityFactor) {
				intel.resetHA_EVENT();
			}
		}
	}
	
	public static void avertOrEndDiktatAttackAsNecessary() {
		SindrianDiktatPunitiveExpedition attack = SindrianDiktatPunitiveExpedition.get();
		if (attack != null) {
			attack.finish(false);
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel != null) {
			HAERandomEventData data = intel.getRollDataForEvent();
			if (data != null && data.factor instanceof SindrianDiktatHostileActivityFactor) {
				intel.resetHA_EVENT();
			}
		}
	}
	
	
	public static void avertOrEndPLBlockadeAsNecessary() {
		PerseanLeagueBlockade blockade = PerseanLeagueBlockade.get();
		if (blockade != null) {
			blockade.finish(false);
		}

		PerseanLeaguePunitiveExpedition expedition = PerseanLeaguePunitiveExpedition.get();
		if (expedition != null) {
			expedition.finish(false);
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel != null) {
			HAERandomEventData data = intel.getRollDataForEvent();
			if (data != null && data.factor instanceof PerseanLeagueHostileActivityFactor) {
				intel.resetHA_EVENT();
			}
		}
	}
	
	public static boolean canPlayerJoinTheLeague() {
		if (isPLExpeditionInProgress()) return false;
		
		if (PerseanLeagueMembership.isLeagueMember()) return false;
		if (PerseanLeagueMembership.isLeftLeagueWhenGoodDeal()) return false;
		
		if (PerseanLeagueHostileActivityFactor.wasPLEverSatBombardedByPlayer()) return false;
		if (PerseanLeagueHostileActivityFactor.getKazeron(false) == null) return false;
		
		if (PerseanLeagueMembership.getNumTimesLeftLeague() >= PerseanLeagueMembership.TIMES_LEFT_LEAGUE_FOR_NO_REJOIN) {
			return false;
		}
		
		int large = 0;
		int count = 0;
		int medium = 0;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			int size = market.getSize();
			if (size >= StandardPerseanLeagueActivityCause.LARGE_COLONY) {
				large++;
			}
			if (size >= StandardPerseanLeagueActivityCause.MEDIUM_COLONY) {
				medium++;
			}
			count++;
		}
		if (large > 0 || (medium > 0 && count >= StandardPerseanLeagueActivityCause.COUNT_IF_MEDIUM)) {
			return true;
		}
		return false; 
	}

	public static boolean baseInvolved(StarSystemAPI system, PirateBaseIntel base) {
		if (system == null) return false;
		if (base == null) return false;
		
		return !PirateBasePirateActivityCause2.getColoniesAffectedBy(base).isEmpty();
	}
	

	public static class PLPunExData {
		public HostileActivityEventIntel intel;
		public StarSystemAPI target;
		public MarketAPI kazeron;
	}
	
	public static boolean canSendPerseanLeaguePunitiveExpedition() {
		return computePerseanLeaguePunitiveExpeditionData() != null;
	}
	public static PLPunExData computePerseanLeaguePunitiveExpeditionData() {
		if (PerseanLeagueMembership.isDefeatedBlockadeOrPunEx()) {
			return null;
		}
		if (isPLExpeditionInProgress()) {
			return null;
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel == null) return null;
		
		StarSystemAPI target = PerseanLeagueHostileActivityFactor.findBlockadeTarget(intel, null);
		if (target == null) return null;
		
		MarketAPI kazeron = PerseanLeagueHostileActivityFactor.getKazeron(true);
		if (kazeron == null) return null;
		
		PLPunExData data = new PLPunExData();
		data.intel = intel;
		data.target = target;
		data.kazeron = kazeron;
		return data;
	}
	
	public static void sendPerseanLeaguePunitiveExpedition(InteractionDialogAPI dialog) {
		
		PLPunExData data = computePerseanLeaguePunitiveExpeditionData();
		if (data == null) {
			return;
		}
		
		avertOrEndPLBlockadeAsNecessary();
		
		GenericRaidParams params = new GenericRaidParams(new Random(), true);
		params.factionId = data.kazeron.getFactionId();
		params.source = data.kazeron;
		
		Random random = new Random();
		
		params.prepDays = 14f + random.nextFloat() * 14f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = data.target;
		
		Set<String> disrupt = new LinkedHashSet<String>();
		for (MarketAPI market : Misc.getMarketsInLocation(data.target, Factions.PLAYER)) {
			params.raidParams.allowedTargets.add(market);
			params.raidParams.allowNonHostileTargets = true;
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				disrupt.add(ind.getId());
				
			}
		}
		
		// this is set in the custom fleet creation in PerseanLeaguePunitiveExpedition
		//params.makeFleetsHostile = false;
		
		params.raidParams.disrupt.addAll(disrupt);
		params.raidParams.raidsPerColony = Math.min(disrupt.size(), 4);
		if (disrupt.isEmpty()) {
			params.raidParams.raidsPerColony = 2;
		}
		
		if (params.raidParams.allowedTargets.isEmpty()) {
			return;
		}
		
		params.style = FleetStyle.STANDARD;
		
		
		float fleetSizeMult = data.kazeron.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
		
		float f = data.intel.getMarketPresenceFactor(data.target);
		
		float totalDifficulty = fleetSizeMult * 50f * (0.5f + 0.5f * f);
		if (totalDifficulty < 30) {
			return;
		}
		if (totalDifficulty > 100) {
			totalDifficulty = 100;
		}
		
		Random r = data.intel.getRandom();
		
		// mostly maxed-out fleets, some smaller ones
		while (totalDifficulty > 0) {
			float max = 6f;
			float min = 3f;
			
			if (r.nextFloat() > 0.3f) {
				min = (int) Math.min(totalDifficulty, 8f);
				max = (int) Math.min(totalDifficulty, 10f);
			}
			
			int diff = Math.round(StarSystemGenerator.getNormalRandom(r, min, max));
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		PerseanLeaguePunitiveExpedition punex = new PerseanLeaguePunitiveExpedition(params);
		punex.setPreFleetDeploymentDelay(30f + random.nextFloat() * 60f);
		//punex.setPreFleetDeploymentDelay(1f);
		TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
		Global.getSector().getIntelManager().addIntel(punex, false, text);		
		
	}
	
	
	
}





