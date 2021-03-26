package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BattleAPI.BattleSide;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.GroundRaidTargetPickerDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener.RaidResultData;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.graid.DisruptIndustryRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 */
public class MarketCMD extends BaseCommandPlugin {

	public static enum RaidType {
		CUSTOM_ONLY,
		VALUABLE,
		DISRUPT,
	}
	
	public static enum BombardType {
		TACTICAL,
		SATURATION,
	}
	
	public static enum RaidDangerLevel {
		NONE("None", "None", Misc.getPositiveHighlightColor(), 0f, 60f, 1),
		MINIMAL("Minimal", "Minimal", Misc.getPositiveHighlightColor(), 0.02f, 50f, 1),
		LOW("Low", "Light", Misc.getPositiveHighlightColor(), 0.05f, 40f, 2),
		MEDIUM("Medium", "Moderate", Misc.getHighlightColor(), 0.1f, 30f, 3),
		HIGH("High", "Heavy", Misc.getNegativeHighlightColor(), 0.2f, 20f, 5),
		EXTREME("Extreme", "Extreme", Misc.getNegativeHighlightColor(), 0.4f, 10f, 7);
		
		private static RaidDangerLevel [] vals = values();
		
		public String name;
		public String lossesName;
		public Color color;
		public float marineLossesMult;
		public int marineTokens;
		public float disruptionDays;
		private RaidDangerLevel(String name, String lossesName, Color color, float marineLossesMult, float disruptionDays, int marineTokens) {
			this.name = name;
			this.lossesName = lossesName;
			this.color = color;
			this.marineLossesMult = marineLossesMult;
			this.disruptionDays = disruptionDays;
			this.marineTokens = marineTokens;
		}
		
		public RaidDangerLevel next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public RaidDangerLevel prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}
	}
	
	public static class TempData {
		//public boolean canSurpriseRaid;
		//public boolean isSurpriseRaid;
		public boolean canRaid;
		public boolean canBombard;
		
		public int bombardCost;
		
		public int marinesLost;
		
		//public boolean canFail = false;
		//public float failProb = 0f;
		
		public float raidMult;
		
		public float attackerStr;
		public float defenderStr;
		
		public boolean nonMarket = false;
		
		public RaidType raidType = null;
		public BombardType bombardType = null;
		public CargoAPI raidLoot;
		public int xpGained;
		public Industry target = null;
		public List<FactionAPI> willBecomeHostile = new ArrayList<FactionAPI>();
		public List<Industry> bombardmentTargets = new ArrayList<Industry>();
		public List<GroundRaidObjectivePlugin> objectives = new ArrayList<GroundRaidObjectivePlugin>();
		public String contText;
		public String raidGoBackTrigger;
		public String raidContinueTrigger;
	}
	
	public static int HOSTILE_ACTIONS_TIMEOUT_DAYS = 60;
	public static int TACTICAL_BOMBARD_TIMEOUT_DAYS = 120;
	public static int SATURATION_BOMBARD_TIMEOUT_DAYS = 365;
	
	public static int MIN_MARINE_TOKENS = 1;
	public static float RE_PER_MARINE_TOKEN = 0.1f;
	public static int MAX_MARINE_TOKENS = 10;
	public static float LOSS_REDUCTION_PER_RESERVE_TOKEN = 0.05f;
	public static float LOSS_INCREASE_PER_RAID = 0.5f;
	public static float MAX_MARINE_LOSSES = 0.8f;

	public static float MIN_RE_TO_REDUCE_MARINE_LOSSES = 0.5f;
	public static float MAX_MARINE_LOSS_REDUCTION_MULT = 0.05f;
	
	// for causing deficit; higher value means less units need to be raided to cause same deficit	
	public static float ECON_IMPACT_MULT = 1f;
	
	public static float QUANTITY_MULT_NORMAL = 1f; 
	public static float QUANTITY_MULT_EXCESS = 2f; 
	public static float QUANTITY_MULT_DEFICIT = -0.5f; 
	public static float QUANTITY_MULT_OVERALL = 0.1f; 
	
	
	public static String ENGAGE = "mktEngage";
	
	public static String RAID = "mktRaid";
	public static String RAID_NON_MARKET = "mktRaidNonMarket";
	//public static String RAID_SURPRISE = "mktRaidSurprise";
	//public static String RAID_RARE = "mktRaidRare";
	public static String RAID_VALUABLE = "mktRaidValuable";
	public static String RAID_DISRUPT = "mktRaidDisrupt";
	public static String RAID_GO_BACK = "mktRaidGoBack";
	public static String RAID_CONFIRM_CONTINUE = "mktRaidConfirmContinue";
	
	public static String RAID_CONFIRM = "mktRaidConfirm";
	public static String RAID_CONFIRM_STORY = "mktRaidConfirmStory";
	public static String RAID_NEVER_MIND = "mktRaidNeverMind";
	public static String RAID_RESULT = "mktRaidResult";
	
	public static String INVADE = "mktInvade";
	public static String GO_BACK = "mktGoBack";
	
	public static String BOMBARD = "mktBombard";
	public static String BOMBARD_TACTICAL = "mktBombardTactical";
	public static String BOMBARD_SATURATION = "mktBombardSaturation";
	public static String BOMBARD_CONFIRM = "mktBombardConfirm";
	public static String BOMBARD_NEVERMIND = "mktBombardNeverMind";
	public static String BOMBARD_RESULT = "mktBombardResult";
	
	public static String DEBT_RESULT_CONTINUE = "marketCmd_checkDebtContinue";
	
	
	
	
	public static float DISRUPTION_THRESHOLD = 0.25f;
	public static float VALUABLES_THRESHOLD = 0.05f;
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected MarketAPI market;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;

	protected TempData temp = new TempData();
	
	public MarketCMD() {
	}
	
	protected void clearTemp() {
		if (temp != null) {
			//temp.isSurpriseRaid = false;
			temp.raidType = null;
			temp.bombardType = null;
			temp.raidLoot = null;
			temp.target = null;
			temp.willBecomeHostile.clear();
			temp.bombardmentTargets.clear();
			temp.objectives.clear();
			temp.contText = null;
			temp.raidGoBackTrigger = null;
			temp.raidContinueTrigger = null;
			//temp.canFail = false;
			//temp.failProb = 0f;
		}
	}
	
	public MarketCMD(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		faction = entity.getFaction();
		
		market = entity.getMarket();
		
		//DebugFlags.MARKET_HOSTILITIES_DEBUG = false;
		//market.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET, true, 0.1f);
		
		String key = "$MarketCMD_temp";
		MemoryAPI mem = null;
		if (market != null) {
			mem = market.getMemoryWithoutUpdate();
		} else {
			mem = entity.getMemoryWithoutUpdate();
		}
		if (mem.contains(key)) {
			temp = (TempData) mem.get(key);
		} else {
			mem.set(key, temp, 0f);
		}
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		if (command.equals("showDefenses")) {
			clearTemp();
			//new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
			showDefenses(true);
		} else if (command.equals("goBackToDefenses")) {
			if (temp.nonMarket) {
				String trigger = temp.raidGoBackTrigger;
				if (trigger == null || trigger.isEmpty()) trigger = "PopulateOptions";
				clearTemp();
				FireAll.fire(null, dialog, memoryMap, trigger);
				return true;
			}
			clearTemp();
			//new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
			showDefenses(true);
			//dialog.getVisualPanel().finishFadeFast();
		} else if (command.equals("engage")) {
			engage();
		} else if (command.equals("raidMenu")) {
//			boolean surprise = "mktRaidSurprise".equals(memory.get("$option"));
//			temp.isSurpriseRaid = surprise;
			raidMenu();
//		} else if (command.equals("raidRare")) {
//			raidRare();
		} else if (command.equals("raidNonMarket")) {
			raidNonMarket();
		} else if (command.equals("raidValuable")) {
			raidValuable();
		} else if (command.equals("raidDisrupt")) {
			raidDisrupt();
		} else if (command.equals("raidConfirm")) {
			raidConfirm(false);
		} else if (command.equals("raidConfirmContinue")) {
			raidConfirmContinue();
		} else if (command.equals("raidNeverMind")) {
			raidNeverMind();
		} else if (command.equals("addContinueToRaidResultOption")) {
			addContinueOption(temp.contText);
		} else if (command.equals("raidResult")) {
			raidResult();
		} else if (command.equals("bombardMenu")) {
			bombardMenu();
		} else if (command.equals("bombardTactical")) {
			bombardTactical();
		} else if (command.equals("bombardSaturation")) {
			bombardSaturation();
		} else if (command.equals("bombardConfirm")) {
			bombardConfirm();
		} else if (command.equals("bombardNeverMind")) {
			bombardNeverMind();
		} else if (command.equals("bombardResult")) {
			bombardResult();
		} else if (command.equals("checkDebtEffect")) {
			return checkDebtEffect();
		} else if (command.equals("applyDebtEffect")) {
			applyDebtEffect();
		} else if (command.equals("checkMercsLeaving")) {
			return checkMercsLeaving();
		} else if (command.equals("convinceMercToStay")) {
			convinceMercToStay();
		} else if (command.equals("mercLeaves")) {
			mercLeaves();
		}
		
		return true;
	}

	protected void showDefenses(boolean withText) {
		CampaignFleetAPI primary = getInteractionTargetForFIDPI();
		CampaignFleetAPI station = getStationFleet();
		
		boolean hasNonStation = false;
		boolean hasOtherButInsignificant = true;
		boolean hasStation = station != null;
		boolean otherWantsToFight = false;
		BattleAPI b = null;
		FleetEncounterContext context = null;
		FleetInteractionDialogPluginImpl plugin = null;
		
		boolean ongoingBattle = false;
		
		boolean playerOnDefenderSide = false;
		boolean playerCanNotJoin = false;

		String stationType = "station";
		if (station != null) {
			FleetMemberAPI flagship = station.getFlagship();
			if (flagship != null && flagship.getVariant() != null) {
				String name = flagship.getVariant().getDesignation().toLowerCase();
				stationType = name;
			}
		}
		
		StationState state = getStationState();
		
		if (market != null) {
			Global.getSector().getEconomy().tripleStep();
		}
		
		if (primary == null) {
			if (state == StationState.NONE) {
				text.addPara("The colony has no orbital station or nearby fleets to defend it.");
			} else {
				printStationState();
				text.addPara("There are no nearby fleets to defend the colony.");
			}
		} else {
			ongoingBattle = primary.getBattle() != null;

			CampaignFleetAPI pluginFleet = primary;
			if (ongoingBattle) {
				BattleSide playerSide = primary.getBattle().pickSide(playerFleet);
				CampaignFleetAPI other = primary.getBattle().getPrimary(primary.getBattle().getOtherSide(playerSide));
				if (other != null) {
					pluginFleet = other;
				}
			}
			
			FIDConfig params = new FIDConfig();
			params.justShowFleets = true;
			params.showPullInText = withText;
			plugin = new FleetInteractionDialogPluginImpl(params);
			//dialog.setInteractionTarget(primary);
			dialog.setInteractionTarget(pluginFleet);
			plugin.init(dialog);
//			if (ongoingBattle) {
//				plugin.setPlayerFleet(primary.getBattle().getPlayerCombined());
//			}
			dialog.setInteractionTarget(entity);
			
			
			context = (FleetEncounterContext)plugin.getContext();
			b = context.getBattle();
			
			BattleSide playerSide = b.pickSide(playerFleet);
			if (playerSide != BattleSide.NO_JOIN) {
				if (b.getOtherSideCombined(playerSide).isEmpty()) {
					playerSide = BattleSide.NO_JOIN;
				}
			}
			playerCanNotJoin = playerSide == BattleSide.NO_JOIN;
			if (!playerCanNotJoin) {
				playerOnDefenderSide = b.getSide(playerSide) == b.getSideFor(primary);
			}
			if (!ongoingBattle) {
				playerOnDefenderSide = false;
			}

			boolean otherHasStation = false;
			if (playerSide != BattleSide.NO_JOIN) {
				//for (CampaignFleetAPI fleet : b.getNonPlayerSide()) {
				if (station != null) {
					for (CampaignFleetAPI fleet : b.getSideFor(station)) {
						if (!fleet.isStationMode()) {
							hasNonStation = true;
							hasOtherButInsignificant &= Misc.isInsignificant(fleet);
						}
					}
				} else {
					if (b.getNonPlayerSide() != null) {
						for (CampaignFleetAPI fleet : b.getNonPlayerSide()) {
							if (!fleet.isStationMode()) {
								hasNonStation = true;
								hasOtherButInsignificant &= Misc.isInsignificant(fleet);
							}
						}
					} else {
						hasNonStation = true;
					}
				}
				
				for (CampaignFleetAPI fleet : b.getOtherSide(playerSide)) {
					if (!fleet.isStationMode()) {
						//hasNonStation = true;
					} else {
						otherHasStation = true;
					}
				}
			}
			
			if (!hasNonStation) hasOtherButInsignificant = false;
			
			//otherWantsToFight = hasStation || plugin.otherFleetWantsToFight(true);
			
			// inaccurate because it doesn't include the station in the "wants to fight" calculation, but, this is tricky
			// and I don't want to break it right now
			otherWantsToFight = otherHasStation || plugin.otherFleetWantsToFight(true);
			
			if (withText) {
				if (hasStation) {
					String name = "An orbital station";
					if (station != null) {
						FleetMemberAPI flagship = station.getFlagship();
						if (flagship != null) {
							name = flagship.getVariant().getDesignation().toLowerCase();
							stationType = name;
							name = Misc.ucFirst(station.getFaction().getPersonNamePrefixAOrAn()) + " " + 
									station.getFaction().getPersonNamePrefix() + " " + name;
						}
					}
					text.addPara(name + " dominates the orbit and prevents any " +
								 "hostile action, aside from a quick raid, unless it is dealt with.");
					
					
					if (hasNonStation) {
						if (ongoingBattle) {
							text.addPara("There are defending ships present, but they are currently involved in a battle, "
									+ "and you could take advantage of the distraction to launch a raid.");
						} else {
							if (hasOtherButInsignificant) {
								text.addPara("Defending ships are present, but not in sufficient strength " +
										 "to want to give battle or prevent any hostile action you might take.");
							} else {
								text.addPara("The defending ships present are, with the support of the station, sufficient to prevent " +
									 "raiding as well.");
							}
						}
					}
				} else if (hasNonStation && otherWantsToFight) {
					printStationState();
					text.addPara("Defending ships are present in sufficient strength to prevent any hostile action " +
					"until they are dealt with.");
				} else if (hasNonStation && !otherWantsToFight) {
					printStationState();
					text.addPara("Defending ships are present, but not in sufficient strength " +
								 "to want to give battle or prevent any hostile action you might take.");
				}
				
				plugin.printOngoingBattleInfo();
			}
		}

		if (!hasNonStation) hasOtherButInsignificant = false;
		
		options.clearOptions();
		
		String engageText = "Engage the defenders";
		
		if (playerCanNotJoin) {
			engageText = "Engage the defenders";
		} else if (playerOnDefenderSide) {
			if (hasStation && hasNonStation) {
				engageText = "Aid the " + stationType + " and its defenders";
			} else if (hasStation) {
				engageText = "Aid the " + stationType + "";
			} else {
				engageText = "Aid the defenders";
			}
		} else {
			if (ongoingBattle) {
				engageText = "Aid the attacking forces";
			} else {
				if (hasStation && hasNonStation) {
					engageText = "Engage the " + stationType + " and its defenders";
				} else if (hasStation) {
					engageText = "Engage the " + stationType + "";
				} else {
					engageText = "Engage the defenders";
				}
			}
		}
		
		
		options.addOption(engageText, ENGAGE);
		
		
		temp.canRaid = ongoingBattle || hasOtherButInsignificant || (hasNonStation && !otherWantsToFight) || !hasNonStation;
		temp.canBombard = (hasOtherButInsignificant || (hasNonStation && !otherWantsToFight) || !hasNonStation) && !hasStation;
		//temp.canSurpriseRaid = Misc.getDaysSinceLastRaided(market) < SURPRISE_RAID_TIMEOUT;
		
		boolean couldRaidIfNotDebug = temp.canRaid;
		if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
			if (!temp.canRaid || !temp.canBombard) {
				text.addPara("(DEBUG mode: can raid and bombard anyway)");
			}
			temp.canRaid = true;
			temp.canBombard = true;
			//temp.canSurpriseRaid = true;
		}
			
//		options.addOption("Launch a raid against the colony", RAID);
//		options.addOption("Consider an orbital bombardment", BOMBARD);
//		options.addOption("Launch a surprise raid against " + market.getName(), RAID_SURPRISE);
		options.addOption("Launch a raid against " + market.getName() + "", RAID);
		//dialog.setOptionColor(RAID_SURPRISE, Misc.getStoryOptionColor());
		options.addOption("Consider an orbital bombardment of " + market.getName() + "", BOMBARD);
		
		if (!temp.canRaid) {
			options.setEnabled(RAID, false);
			options.setTooltip(RAID, "The presence of enemy fleets that are willing to offer battle makes a raid impossible.");
		} 
		
//		if (!temp.canSurpriseRaid) {
////			float surpriseRaidDays = (int) (SURPRISE_RAID_TIMEOUT - Misc.getDaysSinceLastRaided(market));
////			if (surpriseRaidDays > 0) {
////				surpriseRaidDays = (int) Math.round(surpriseRaidDays);
////				if (surpriseRaidDays < 1) surpriseRaidDays = 1;
////				String days = "days";
////				if (surpriseRaidDays == 1) {
////					days = "day";
////				}
////				//text.addPara("Your ground forces commander estimates that");
////			}
//			options.setEnabled(RAID_SURPRISE, false);
//			options.setTooltip(RAID_SURPRISE, "This colony was raided within the last cycle and its ground defenses are on high alert, making a surprise raid impossible.");
//		}
		
		if (!temp.canBombard) {
			options.setEnabled(BOMBARD, false);
			options.setTooltip(BOMBARD, "All defenses must be defeated to make a bombardment possible.");
		}
		
		
		//DEBUG = false;
		if (temp.canRaid && getRaidCooldown() > 0) {// && couldRaidIfNotDebug) {
			if (!DebugFlags.MARKET_HOSTILITIES_DEBUG) {
				options.setEnabled(RAID, false);
				text.addPara("Your forces will be able to organize another raid within a day or so.");
				temp.canRaid = false;
			} else {
				text.addPara("Your forces will be able to organize another raid within a day or so.");
				text.addPara("(DEBUG mode: can do it anyway)");
			}
			//options.setTooltip(RAID, "Need more time to organize another raid.");
		}
		
		//options.addOption("Launch a raid of the colony", RAID);
		
		
		if (context != null && otherWantsToFight && !playerCanNotJoin) {
			boolean knows = context.getBattle() != null && context.getBattle().getNonPlayerSide() != null &&
							context.getBattle().knowsWhoPlayerIs(context.getBattle().getNonPlayerSide());
			boolean lowImpact = context.isLowRepImpact();
			FactionAPI nonHostile = plugin.getNonHostileOtherFaction();
			//if (!playerFleet.getFaction().isHostileTo(otherFleet.getFaction()) && knows && !context.isEngagedInHostilities()) {
			if (nonHostile != null && knows && !lowImpact && !context.isEngagedInHostilities()) {
				options.addOptionConfirmation(ENGAGE,
						"The " + nonHostile.getDisplayNameLong() + 
						" " + nonHostile.getDisplayNameIsOrAre() + 
						" not currently hostile, and you have been positively identified. " +
						"Are you sure you want to engage in open hostilities?", "Yes", "Never mind");
			}
		} else if (context == null || playerCanNotJoin || !otherWantsToFight) {
			options.setEnabled(ENGAGE, false);
			if (!otherWantsToFight) {
				if (ongoingBattle && playerOnDefenderSide && !otherWantsToFight) {
					options.setTooltip(ENGAGE, "The attackers are in disarray and not currently attempting to engage the station.");
				} else {
					if (playerCanNotJoin) {
						options.setTooltip(ENGAGE, "You're unable to join this battle.");
					} else if (primary == null) {
						options.setTooltip(ENGAGE, "There are no defenders to engage.");
					} else {
						options.setTooltip(ENGAGE, "The defenders are refusing to give battle to defend the colony.");
					}
				}
			}
		}
		
		options.addOption("Go back", GO_BACK);
		options.setShortcut(GO_BACK, Keyboard.KEY_ESCAPE, false, false, false, true);
		
		
		if (plugin != null) {
			plugin.cleanUpBattle();
		}
		
	}
	
	public static float getRaidStr(CampaignFleetAPI fleet) {
		float attackerStr = fleet.getCargo().getMaxPersonnel() * 0.25f;
		float support = Misc.getFleetwideTotalMod(fleet, Stats.FLEET_GROUND_SUPPORT, 0f);
		attackerStr += Math.min(support, attackerStr);
		
		StatBonus stat = fleet.getStats().getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD);
		attackerStr = stat.computeEffective(attackerStr);
		
		return attackerStr;
	}
	public static float getDefenderStr(MarketAPI market) {
		StatBonus stat = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
		float defenderStr = (int) Math.round(stat.computeEffective(0f));
		float added = getDefenderIncreaseValue(market);
		defenderStr += added;
		return defenderStr;
	}
	
	public static float getRaidEffectiveness(MarketAPI market, CampaignFleetAPI fleet) {
		return getRaidEffectiveness(market, getRaidStr(fleet));
	}
	public static float getRaidEffectiveness(MarketAPI market, float attackerStr) {
		float defenderStr = getDefenderStr(market);
		return attackerStr / Math.max(1f, (attackerStr + defenderStr));
	}

	public static int getMarinesFor(MarketAPI market, int tokens) {
		float defenderStr = getDefenderStr(market);
		return getMarinesFor((int)defenderStr, tokens);
	}
	public static int getMarinesFor(int defenderStrength, int tokens) {
//		mult = as / (as + ds);
//		tokens = mult / re_per
//
//		t * re_per = as / (as + ds)
//		t * re_per * (as + ds) = as;
//		t * re_per * as + t * re_per * ds = as;
//		t * re_per * ds = as * (1 - t * re_per)
//		as = t * re_per * ds / (1 - t * re_per)
		
		
		int marines = (int) Math.round((float) tokens * RE_PER_MARINE_TOKEN * 
							(float) defenderStrength / (1f - (float) tokens * RE_PER_MARINE_TOKEN));
		
		return marines;
	}
	public static int getDisruptDaysPerToken(MarketAPI market, Industry industry) {
		DisruptIndustryRaidObjectivePluginImpl obj = new DisruptIndustryRaidObjectivePluginImpl(market, industry);
		return (int) Math.round(obj.getBaseDisruptDuration(1));
	}
	
	
	protected void raidNonMarket() {
		float width = 350;
		float opad = 10f;
		float small = 5f;
		
		Color h = Misc.getHighlightColor();
		
		temp.nonMarket = true;
		
		float difficulty = memory.getFloat("$raidDifficulty");
		temp.raidGoBackTrigger = memory.getString("$raidGoBackTrigger");
		temp.raidContinueTrigger = memory.getString("$raidContinueTrigger");
		
		dialog.getVisualPanel().showImagePortion("illustrations", "raid_prepare", 640, 400, 0, 0, 480, 300);
		
		float marines = playerFleet.getCargo().getMarines();
		float support = Misc.getFleetwideTotalMod(playerFleet, Stats.FLEET_GROUND_SUPPORT, 0f);
		if (support > marines) support = marines;
		
		StatBonus attackerBase = new StatBonus(); 
		StatBonus defenderBase = new StatBonus();
		
		//defenderBase.modifyFlatAlways("base", baseDef, "Base value for a size " + market.getSize() + " colony");
		
		attackerBase.modifyFlatAlways("core_marines", marines, "Marines on board");
		attackerBase.modifyFlatAlways("core_support", support, "Fleet capability for ground support");
		
		StatBonus attacker = playerFleet.getStats().getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD);
		StatBonus defender = new StatBonus();
		if (market != null && difficulty <= 0) defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
		
		defender.modifyFlat("difficulty", difficulty, "Expected resistance");
		
		String surpriseKey = "core_surprise";
//		if (temp.isSurpriseRaid) {
//			//defender.modifyMult(surpriseKey, 0.1f, "Surprise raid");
//			attacker.modifyMult(surpriseKey, SURPRISE_RAID_STRENGTH_MULT, "Surprise raid");
//		}
		
		String increasedDefensesKey = "core_addedDefStr";
		float added = 0;
		if (market != null) added = getDefenderIncreaseValue(market);
		if (added > 0) {
			defender.modifyFlat(increasedDefensesKey, added, "Increased defender preparedness");
		}
		
		float attackerStr = (int) Math.round(attacker.computeEffective(attackerBase.computeEffective(0f)));
		float defenderStr = (int) Math.round(defender.computeEffective(defenderBase.computeEffective(0f)));
		
		temp.attackerStr = attackerStr;
		temp.defenderStr = defenderStr;
		
		TooltipMakerAPI info = text.beginTooltip();
		
		info.setParaSmallInsignia();
		
		String has = faction.getDisplayNameHasOrHave();
		String is = faction.getDisplayNameIsOrAre();
		boolean hostile = faction.isHostileTo(Factions.PLAYER);
		boolean tOn = playerFleet.isTransponderOn();
		float initPad = 0f;
		if (!hostile && !faction.isNeutralFaction()) {
			if (tOn) {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
						" not currently hostile. Your fleet's transponder is on, and carrying out a raid " +
						"will result in open hostilities.",
						initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			} else {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
						" not currently hostile. Your fleet's transponder is off, and carrying out a raid " +
						"will only result in a minor penalty to your standing.",
						initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			}
			initPad = opad;
		}
		
		float sep = small;
		sep = 3f;
		info.addPara("Raid strength: %s", initPad, h, "" + (int)attackerStr);
		info.addStatModGrid(width, 50, opad, small, attackerBase, true, statPrinter(false));
		if (!attacker.isUnmodified()) {
			info.addStatModGrid(width, 50, opad, sep, attacker, true, statPrinter(true));
		}
		
		
		info.addPara("Operation difficulty: %s", opad, h, "" + (int)defenderStr);
		//info.addStatModGrid(width, 50, opad, small, defenderBase, true, statPrinter());
		//if (!defender.isUnmodified()) {
		info.addStatModGrid(width, 50, opad, small, defender, true, statPrinter(true));
		//}
		
		defender.unmodifyFlat(increasedDefensesKey);
		defender.unmodifyMult(surpriseKey);
		attacker.unmodifyMult(surpriseKey);
		
		text.addTooltip();
		
		boolean hasForces = true;
		temp.raidMult = attackerStr / Math.max(1f, (attackerStr + defenderStr));
		temp.raidMult = Math.round(temp.raidMult * 100f) / 100f;
		
		{
			Color eColor = h;
			if (temp.raidMult < DISRUPTION_THRESHOLD && temp.raidMult < VALUABLES_THRESHOLD) {
				eColor = Misc.getNegativeHighlightColor();
			}
			text.addPara("Projected raid effectiveness: %s",
					eColor,
					"" + (int)(temp.raidMult * 100f) + "%");
			//"" + (int)Math.round(temp.raidMult * 100f) + "%");
			if (temp.raidMult < VALUABLES_THRESHOLD) {
				text.addPara("You do not have the forces to carry out an effective raid.");
				hasForces = false;
			}
		}
		
		options.clearOptions();
		
		options.addOption("Designate raid objectives", RAID_VALUABLE);
		
		if (!hasForces) {
			options.setEnabled(RAID_VALUABLE, false);
		}
		
		options.addOption("Go back", RAID_GO_BACK);
		options.setShortcut(RAID_GO_BACK, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	
//	protected void raidRare() {
//		
//	}
	
	protected void raidMenu() {
		float width = 350;
		float opad = 10f;
		float small = 5f;
		
		Color h = Misc.getHighlightColor();
		
		temp.nonMarket = false;
		
//		dialog.getVisualPanel().showPlanetInfo(market.getPrimaryEntity());
//		dialog.getVisualPanel().finishFadeFast();
		dialog.getVisualPanel().showImagePortion("illustrations", "raid_prepare", 640, 400, 0, 0, 480, 300);

		float marines = playerFleet.getCargo().getMarines();
		float support = Misc.getFleetwideTotalMod(playerFleet, Stats.FLEET_GROUND_SUPPORT, 0f);
		if (support > marines) support = marines;
		
		StatBonus attackerBase = new StatBonus(); 
		StatBonus defenderBase = new StatBonus(); 
		
		//defenderBase.modifyFlatAlways("base", baseDef, "Base value for a size " + market.getSize() + " colony");
		
		attackerBase.modifyFlatAlways("core_marines", marines, "Marines on board");
		attackerBase.modifyFlatAlways("core_support", support, "Fleet capability for ground support");
		
		StatBonus attacker = playerFleet.getStats().getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD);
		StatBonus defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
		
		String surpriseKey = "core_surprise";
//		if (temp.isSurpriseRaid) {
//			//defender.modifyMult(surpriseKey, 0.1f, "Surprise raid");
//			attacker.modifyMult(surpriseKey, SURPRISE_RAID_STRENGTH_MULT, "Surprise raid");
//		}
		
		String increasedDefensesKey = "core_addedDefStr";
		float added = getDefenderIncreaseValue(market);
		if (added > 0) {
			defender.modifyFlat(increasedDefensesKey, added, "Increased defender preparedness");
		}
		
		float attackerStr = (int) Math.round(attacker.computeEffective(attackerBase.computeEffective(0f)));
		float defenderStr = (int) Math.round(defender.computeEffective(defenderBase.computeEffective(0f)));
		
		temp.attackerStr = attackerStr;
		temp.defenderStr = defenderStr;
		
		TooltipMakerAPI info = text.beginTooltip();
		
		info.setParaSmallInsignia();
		
		String has = faction.getDisplayNameHasOrHave();
		String is = faction.getDisplayNameIsOrAre();
		boolean hostile = faction.isHostileTo(Factions.PLAYER);
		boolean tOn = playerFleet.isTransponderOn();
		float initPad = 0f;
		if (!hostile) {
			if (tOn) {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
						" not currently hostile. Your fleet's transponder is on, and carrying out a raid " +
						"will result in open hostilities.",
						initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			} else {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
						" not currently hostile. Your fleet's transponder is off, and carrying out a raid " +
						"will only result in a minor penalty to your standing.",
						initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			}
			initPad = opad;
		}
		
		float sep = small;
		sep = 3f;
		info.addPara("Raid strength: %s", initPad, h, "" + (int)attackerStr);
		info.addStatModGrid(width, 50, opad, small, attackerBase, true, statPrinter(false));
		if (!attacker.isUnmodified()) {
			info.addStatModGrid(width, 50, opad, sep, attacker, true, statPrinter(true));
		}
		
		
		info.addPara("Ground defense strength: %s", opad, h, "" + (int)defenderStr);
		//info.addStatModGrid(width, 50, opad, small, defenderBase, true, statPrinter());
		//if (!defender.isUnmodified()) {
			info.addStatModGrid(width, 50, opad, small, defender, true, statPrinter(true));
		//}
			
		defender.unmodifyFlat(increasedDefensesKey);
		defender.unmodifyMult(surpriseKey);
		attacker.unmodifyMult(surpriseKey);
		
		text.addTooltip();

		boolean hasForces = true;
		boolean canDisrupt = true;
		temp.raidMult = attackerStr / Math.max(1f, (attackerStr + defenderStr));
		temp.raidMult = Math.round(temp.raidMult * 100f) / 100f;
		//temp.raidMult = 1f;
		
		
		
		
		{
			//temp.failProb = 0f;
			Color eColor = h;
			if (temp.raidMult < DISRUPTION_THRESHOLD && temp.raidMult < VALUABLES_THRESHOLD) {
				eColor = Misc.getNegativeHighlightColor();
			}
			if (temp.raidMult < DISRUPTION_THRESHOLD) {
				//eColor = Misc.getNegativeHighlightColor();
				canDisrupt = false;
				//temp.canFail = true;
			} else if (temp.raidMult >= 0.7f) {
				//eColor = Misc.getPositiveHighlightColor();
			}
//			text.addPara("Projected raid effectiveness: %s. " +
//					"This will determine the outcome of the raid, " +
//					"as well as the casualties suffered by your forces, if any.",
//					eColor,
//					"" + (int)Math.round(temp.raidMult * 100f) + "%");
			text.addPara("Projected raid effectiveness: %s",
					eColor,
					"" + (int)(temp.raidMult * 100f) + "%");
					//"" + (int)Math.round(temp.raidMult * 100f) + "%");
			if (!canDisrupt) {
				text.addPara("The ground defenses are too strong for your forces to be able to cause long-term disruption.");
			}
			if (temp.raidMult < VALUABLES_THRESHOLD) {
				text.addPara("You do not have the forces to carry out an effective raid to acquire valuables or achieve other objectives.");
				hasForces = false;
			}
//			if (canDisrupt) {
//			} else {
//				text.addPara("Projected raid effectiveness: %s. " +
//						"This will determine the outcome of the raid, " +
//						"as well as the casualties suffered by your forces, if any.",
//						eColor,
//						"" + (int)Math.round(temp.raidMult * 100f) + "%");
//			}
		}
		
		if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
			canDisrupt = true;
		}
		
		options.clearOptions();
		
		//options.addOption("Try to acquire rare items, such as blueprints", RAID_RARE);
		//options.addOption("Try to acquire valuables, such as commodities, blueprints, and other items", RAID_VALUABLE);
		options.addOption("Try to acquire valuables, such as commodities or blueprints, or achieve other objectives", RAID_VALUABLE);
		options.addOption("Disrupt the operations of a specific industry or facility", RAID_DISRUPT);
		
		if (!hasForces) {
			options.setEnabled(RAID_VALUABLE, false);
			//options.setEnabled(RAID_RARE, false);
		}
		
		if (!hasForces || !canDisrupt) {
			options.setEnabled(RAID_DISRUPT, false);
			if (!canDisrupt) {
				String pct = "" + (int)Math.round(DISRUPTION_THRESHOLD * 100f) + "%";
				options.setTooltip(RAID_DISRUPT, "Requires at least " + pct + " raid effectiveness.");
				options.setTooltipHighlights(RAID_DISRUPT, pct);
				options.setTooltipHighlightColors(RAID_DISRUPT, h);
			}
		}
	
		options.addOption("Go back", RAID_GO_BACK);
		options.setShortcut(RAID_GO_BACK, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
//	protected void raidRare() {
//		
//	}
	
	protected void raidValuable() {
		temp.raidType = RaidType.VALUABLE;
		
		List<GroundRaidObjectivePlugin> obj = new ArrayList<GroundRaidObjectivePlugin>();
		
		// See: StandardGroundRaidObjectivesCreator; it creates the standard objectives with priority 0 below
		final RaidType useType = !temp.nonMarket ? temp.raidType : RaidType.CUSTOM_ONLY;
		//if (temp.nonMarket) useType = RaidType.CUSTOM_ONLY;
		for (int i = 0; i < 10; i++) {
			ListenerUtil.modifyRaidObjectives(market, entity, obj, useType, getNumMarineTokens(), i);
		}
		
		if (obj.isEmpty()) {
			text.addPara("After careful consideration, there do not appear to be any targets " +
						 "likely to yield anything of value.");
			addNeverMindOption();
			return;
		}
		
		
		dialog.showGroundRaidTargetPicker("Select raid objectives", "Select", market, obj, 
				new GroundRaidTargetPickerDelegate() {
			public void pickedGroundRaidTargets(List<GroundRaidObjectivePlugin> data) {
				float value = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					value += curr.getProjectedCreditsValue();
				}
				Color h = Misc.getHighlightColor();
				List<String> names = new ArrayList<String>();
				for (GroundRaidObjectivePlugin curr : data) {
					names.add(curr.getNameOverride() != null ? curr.getNameOverride() : curr.getName());
				}
				String list = Misc.getAndJoined(names);
				String item = "objective";
				if (names.size() > 1) {
					item = "objectives";
				}
				
				String isOrAre = "are";
				String marinesStr = "marines";
				if (playerCargo.getMarines() == 1) {
					isOrAre = "is";
					marinesStr = "marine";
				}
				//float losses = getProjectedMarineLossesFloat();
				LabelAPI label = text.addPara("Your marine commander submits a plan for your approval. Losses during this " +
						"operation are projected to be %s. There " + isOrAre + " a total of %s " +
						marinesStr + " in your fleet.", 
						getMarineLossesColor(data), getProjectedMarineLosses(data).toLowerCase(), 
						Misc.getWithDGS(playerCargo.getMarines()));
				label.setHighlightColors(getMarineLossesColor(data), Misc.getHighlightColor());
				text.addPara(Misc.ucFirst(item) + " targeted: " + list + ".", h,
						names.toArray(new String[0]));
				if (value > 0) {
					text.addPara("The estimated value of the items obtained is projected to be around %s.",
							h, Misc.getDGSCredits(value));
				}

//				text.addPara("The marines are ready to go, awaiting your final confirmation. There are a total of %s " +
//						"marines in your fleet.", Misc.getHighlightColor(), Misc.getWithDGS(playerCargo.getMarines()));
				text.addPara("The marines are ready to go, awaiting your final confirmation.");
				temp.objectives = data;
				addConfirmOptions();
			}
			
			public boolean isDisruptIndustryMode() {
				return false;
			}
			
			public boolean isCustomOnlyMode() {
				return useType == RaidType.CUSTOM_ONLY;
			}
			
			public void cancelledGroundRaidTargetPicking() {
				
			}
			
			public int getCargoSpaceNeeded(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getCargoSpaceNeeded();
				}
				return (int) total;
			}

			public int getFuelSpaceNeeded(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getFuelSpaceNeeded();
				}
				return (int) total;
			}

			public int getProjectedCreditsValue(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getProjectedCreditsValue();
				}
				return (int) total;
			}
			
			public int getNumMarineTokens() {
				return MarketCMD.this.getNumMarineTokens();
			}
			
			public MutableStat getMarineLossesStat(List<GroundRaidObjectivePlugin> data) {
				return MarketCMD.this.getMarineLossesStat(data);
			}
			
			public String getProjectedMarineLosses(List<GroundRaidObjectivePlugin> data) {
				//return "" + (int) Math.round(getProjectedMarineLossesFloat());
				float marines = playerFleet.getCargo().getMarines();
				float losses = getAverageMarineLosses(data);
				
				float f = losses / Math.max(1f, marines);
				
				for (RaidDangerLevel level : RaidDangerLevel.values()) {
					float test = level.marineLossesMult + (level.next().marineLossesMult - level.marineLossesMult) * 0.5f;
					if (level == RaidDangerLevel.NONE) test = RaidDangerLevel.NONE.marineLossesMult;
					if (test >= f) {
						return level.lossesName;
					}
				}
				return RaidDangerLevel.EXTREME.lossesName;
			}
			
			public float getAverageMarineLosses(List<GroundRaidObjectivePlugin> data) {
				return MarketCMD.this.getAverageMarineLosses(data);
			}
		
			public Color getMarineLossesColor(List<GroundRaidObjectivePlugin> data) {
				float marines = playerFleet.getCargo().getMarines();
				float losses = getAverageMarineLosses(data);
				
					
				float f = losses / Math.max(1f, marines);
				if (f <= 0 && data.isEmpty())  return Misc.getGrayColor();
				
				for (RaidDangerLevel level : RaidDangerLevel.values()) {
					float test = level.marineLossesMult + (level.next().marineLossesMult - level.marineLossesMult) * 0.5f;
					if (test >= f) {
						return level.color;
					}
				}
				return RaidDangerLevel.EXTREME.color;
			}
			public String getRaidEffectiveness() {
				return "" + (int)(temp.raidMult * 100f) + "%";
			}
		});
	}
	
	protected void addBombardConfirmOptions() {
		options.clearOptions();
		options.addOption("Launch bombardment", BOMBARD_CONFIRM);
		options.addOption("Never mind", BOMBARD_NEVERMIND);
		options.setShortcut(BOMBARD_NEVERMIND, Keyboard.KEY_ESCAPE, false, false, false, true);
		
		List<FactionAPI> nonHostile = new ArrayList<FactionAPI>();
		for (FactionAPI faction : temp.willBecomeHostile) {
			boolean hostile = faction.isHostileTo(Factions.PLAYER);
			if (!hostile) {
				nonHostile.add(faction);
			}
		}
		
		if (nonHostile.size() == 1) {
			FactionAPI faction = nonHostile.get(0);
			options.addOptionConfirmation(BOMBARD_CONFIRM,
					"The " + faction.getDisplayNameLong() + 
					" " + faction.getDisplayNameIsOrAre() + 
					" not currently hostile, and will become hostile if you carry out the bombardment. " +
					"Are you sure?", "Yes", "Never mind");
		} else if (nonHostile.size() > 1) {
			options.addOptionConfirmation(BOMBARD_CONFIRM,
					"Multiple factions that are not currently hostile " +
					"will become hostile if you carry out the bombardment. " +
					"Are you sure?", "Yes", "Never mind");
		}
	}
	
	protected void raidDisrupt() {
		temp.raidType = RaidType.DISRUPT;
		
		// See: StandardGroundRaidObjectivesCreator; it creates the standard objectives with priority 0 below
		List<GroundRaidObjectivePlugin> obj = new ArrayList<GroundRaidObjectivePlugin>();
		for (int i = 0; i < 10; i++) {
			ListenerUtil.modifyRaidObjectives(market, entity, obj, temp.raidType, getNumMarineTokens(), i);
		}
		
		if (obj.isEmpty()) {
			text.addPara("There are no industries or facilities present that could be disrupted by a raid.");
			addNeverMindOption();
			return;
		}
		
		
		dialog.showGroundRaidTargetPicker("Select raid objectives", "Select", market, obj, 
				new GroundRaidTargetPickerDelegate() {
			public void pickedGroundRaidTargets(List<GroundRaidObjectivePlugin> data) {
				float value = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					value += curr.getProjectedCreditsValue();
				}
				Color h = Misc.getHighlightColor();
				List<String> names = new ArrayList<String>();
				for (GroundRaidObjectivePlugin curr : data) {
					names.add(curr.getNameOverride() != null ? curr.getNameOverride() : curr.getName());
				}
				String list = Misc.getAndJoined(names);
				String item = "objective";
				if (names.size() > 1) {
					item = "objectives";
				}
				
				//float losses = getProjectedMarineLossesFloat();

				text.addPara("Your marine commander submits a plan for your approval. Losses during this " +
						"operation are projected to be %s.", 
						getMarineLossesColor(data), getProjectedMarineLosses(data).toLowerCase());
				text.addPara(Misc.ucFirst(item) + " targeted: " + list + ".", h,
						names.toArray(new String[0]));
				
				if (value > 0) {
					text.addPara("The estimated value of the items obtained is projected to be around %s.",
							h, Misc.getDGSCredits(value));
				}

				text.addPara("The marines are ready to go, awaiting your final confirmation. There are a total of %s " +
						"marines in your fleet.", Misc.getHighlightColor(), Misc.getWithDGS(playerCargo.getMarines()));
				temp.objectives = data;
				addConfirmOptions();
			}
			
			public boolean isDisruptIndustryMode() {
				return true;
			}
			
			public void cancelledGroundRaidTargetPicking() {
				
			}
			
			public int getCargoSpaceNeeded(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getCargoSpaceNeeded();
				}
				return (int) total;
			}

			public int getFuelSpaceNeeded(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getFuelSpaceNeeded();
				}
				return (int) total;
			}

			public int getProjectedCreditsValue(List<GroundRaidObjectivePlugin> data) {
				float total = 0;
				for (GroundRaidObjectivePlugin curr : data) {
					total += curr.getProjectedCreditsValue();
				}
				return (int) total;
			}
			
			public int getNumMarineTokens() {
				return MarketCMD.this.getNumMarineTokens();
			}
			
			public MutableStat getMarineLossesStat(List<GroundRaidObjectivePlugin> data) {
				return MarketCMD.this.getMarineLossesStat(data);
			}
			
			public String getProjectedMarineLosses(List<GroundRaidObjectivePlugin> data) {
				//return "" + (int) Math.round(getProjectedMarineLossesFloat());
				float marines = playerFleet.getCargo().getMarines();
				float losses = getAverageMarineLosses(data);
				
				float f = losses / Math.max(1f, marines);
				
				for (RaidDangerLevel level : RaidDangerLevel.values()) {
					float test = level.marineLossesMult + (level.next().marineLossesMult - level.marineLossesMult) * 0.5f;
					if (level == RaidDangerLevel.NONE) test = RaidDangerLevel.NONE.marineLossesMult;
					if (test >= f) {
						return level.lossesName;
					}
				}
				return RaidDangerLevel.EXTREME.lossesName;
			}
			
			public float getAverageMarineLosses(List<GroundRaidObjectivePlugin> data) {
				return MarketCMD.this.getAverageMarineLosses(data);
			}
		
			public Color getMarineLossesColor(List<GroundRaidObjectivePlugin> data) {
				float marines = playerFleet.getCargo().getMarines();
				float losses = getAverageMarineLosses(data);
				
					
				float f = losses / Math.max(1f, marines);
				if (f <= 0)  return Misc.getGrayColor();
				
				for (RaidDangerLevel level : RaidDangerLevel.values()) {
					float test = level.marineLossesMult + (level.next().marineLossesMult - level.marineLossesMult) * 0.5f;
					if (test >= f) {
						return level.color;
					}
				}
				return RaidDangerLevel.EXTREME.color;
			}
			public String getRaidEffectiveness() {
				return "" + (int)(temp.raidMult * 100f) + "%";
			}

			public boolean isCustomOnlyMode() {
				// TODO Auto-generated method stub
				return false;
			}
		});		
		
		
//		dialog.showIndustryPicker("Select raid target", "Select", market, targets, new IndustryPickerListener() {
//			public void pickedIndustry(Industry industry) {
//				raidDisruptIndustryPicked(industry);
//			}
//			public void cancelledIndustryPicking() {
//				
//			}
//		});
	}
	
	protected float computeBaseDisruptDuration(Industry ind) {
		//float dur = getNumMarineTokens() * Global.getSettings().getFloat("raidDisruptDurationPerMarineToken") - ind.getDisruptedDays();
		float dur = getNumMarineTokens() * ind.getSpec().getDisruptDanger().disruptionDays - ind.getDisruptedDays();
		return (int) dur;
	}
	
	public static int getBombardDestroyThreshold() {
		return Global.getSettings().getInt("bombardSaturationDestroySize");
		
	}
	public static int getBombardDisruptDuration() {
		float dur = Global.getSettings().getFloat("bombardDisruptDuration");
		return (int) dur;
	}
	
	protected void raidDisruptIndustryPicked(Industry target) {
		temp.target = target;
		text.addParagraph("Target: " + target.getCurrentName(), Global.getSettings().getColor("buttonText"));
		
		float dur = computeBaseDisruptDuration(target);
		
		Color h = Misc.getHighlightColor();
		
		float already = target.getDisruptedDays();
		if (already > 0) {
			text.addPara(target.getNameForModifier() + " operations are already disrupted, and a raid will have " +
					"reduced effect.");
		}
		
		text.addPara("Your ground forces commander estimates that given the relative force strengths, " +
				" the raid should disrupt all " + target.getCurrentName() + " operations for at least %s days.",
				h, "" + (int) Misc.getRounded(dur));
		
		text.addPara("Your forces are ready to go, awaiting your final confirmation.");
		
		options.clearOptions();
		
		addConfirmOptions();
	}
	
	
	protected void addNeverMindOption() {
		options.clearOptions();
		options.addOption("Never mind", RAID_NEVER_MIND);
		options.setShortcut(RAID_NEVER_MIND, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	protected void addBombardNeverMindOption() {
		options.clearOptions();
		options.addOption("Never mind", BOMBARD_NEVERMIND);
		options.setShortcut(BOMBARD_NEVERMIND, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	protected void addContinueOption() {
		addContinueOption(null);
	}
	protected void addContinueOption(String text) {
		if (text == null) text = "Continue";
		options.clearOptions();
		options.addOption(text, RAID_RESULT);
	}
	
	
	public static final String DEFENDER_INCREASE_KEY = "$core_defenderIncrease";
	public static float getDefenderIncreaseRaw(MarketAPI market) {
		if (market == null) return 0f;
		float e = market.getMemoryWithoutUpdate().getExpire(DEFENDER_INCREASE_KEY);
		if (e < 0) e = 0;
		return e;
	}
	
	public static void applyDefenderIncreaseFromRaid(MarketAPI market) {
		float e = market.getMemoryWithoutUpdate().getExpire(DEFENDER_INCREASE_KEY);
		if(e < 0) e = 0;
		e += getRaidDefenderIncreasePerRaid();
		float max = getRaidDefenderIncreaseMax();
		if (e > max) e = max;
		
		market.getMemoryWithoutUpdate().set(DEFENDER_INCREASE_KEY, true);
		market.getMemoryWithoutUpdate().expire(DEFENDER_INCREASE_KEY, e);
	}
	
	public static float getDefenderIncreaseValue(MarketAPI market) {
		float e = getDefenderIncreaseRaw(market);
		float f = getRaidDefenderIncreaseFraction();
		float min = getRaidDefenderIncreaseMin();
		
		float base = PopulationAndInfrastructure.getBaseGroundDefenses(market.getSize());
		float incr = Math.max(base * f, min);
		
		float per = getRaidDefenderIncreasePerRaid();
		
		return (int)(incr * e / per);
	}
	
	protected static float getRaidDefenderIncreasePerRaid() {
		return Global.getSettings().getFloat("raidDefenderIncreasePerRaid");
	}
	protected static float getRaidDefenderIncreaseMax() {
		return Global.getSettings().getFloat("raidDefenderIncreaseMax");
	}
	protected static float getRaidDefenderIncreaseFraction() {
		return Global.getSettings().getFloat("raidDefenderIncreaseFraction");
	}
	protected static float getRaidDefenderIncreaseMin() {
		return Global.getSettings().getFloat("raidDefenderIncreaseMin");
	}
	
	
	protected float getRaidCooldownMax() {
		return Global.getSettings().getFloat("raidCooldownDays");
	}
	
	protected void setRaidCooldown(float cooldown) {
		String key = "$raid_cooldown";
		Global.getSector().getMemoryWithoutUpdate().set(key, true, cooldown);
	}
	
	protected float getRaidCooldown() {
		String key = "$raid_cooldown";
		return Global.getSector().getMemoryWithoutUpdate().getExpire(key);
	}
	
	protected Random getRandom() {
		String key = "$raid_random";
		MemoryAPI mem = null;
		SectorEntityToken entity = null;
		if (market != null) {
			mem = market.getMemoryWithoutUpdate();
			entity = market.getPrimaryEntity();
		} else {
			entity = this.entity;
			mem = entity.getMemoryWithoutUpdate();
		}
		Random random = null;
		if (mem.contains(key)) {
			random = (Random) mem.get(key);
		} else {
			if (entity != null) {
				long seed = Misc.getSalvageSeed(entity);
				seed *= (Global.getSector().getClock().getMonth() + 10);
				random = new Random(seed);
			} else {
				random = new Random();
			}
		}
		mem.set(key, random, 30f);
		
		return random;
	}
	
	
	public int getNumMarineTokens() {
		//if (true) return MAX_MARINE_TOKENS;
		int num = (int) Math.round(temp.raidMult / RE_PER_MARINE_TOKEN); 
		if (num < MIN_MARINE_TOKENS) num = MIN_MARINE_TOKENS;
		if (num > MAX_MARINE_TOKENS) num = MAX_MARINE_TOKENS;
		return num;
	}
	
	protected MutableStat getMarineLossesStat(List<GroundRaidObjectivePlugin> data) {
		MutableStat stat = new MutableStat(1f);
		
		float total = 0f;
		float assignedTokens = 0f;
		for (GroundRaidObjectivePlugin curr : data) {
			RaidDangerLevel danger = curr.getDangerLevel();
			total += danger.marineLossesMult * (float) curr.getMarinesAssigned();
			assignedTokens += curr.getMarinesAssigned();
		}
		
		float danger = total / Math.max(1f, assignedTokens);
		
		float hazard = 1f;
		if (market != null) hazard = market.getHazardValue();
		
		float reMult = 1f;
		if (temp.raidMult > MIN_RE_TO_REDUCE_MARINE_LOSSES) {
			float extra = (temp.raidMult - MIN_RE_TO_REDUCE_MARINE_LOSSES) / (1f - MIN_RE_TO_REDUCE_MARINE_LOSSES);
			extra = MAX_MARINE_LOSS_REDUCTION_MULT + (1f - MAX_MARINE_LOSS_REDUCTION_MULT) * (1f - extra);
			reMult = extra;
		} else if (temp.raidMult < RE_PER_MARINE_TOKEN) {
			float extra = 1f + (RE_PER_MARINE_TOKEN - temp.raidMult) / RE_PER_MARINE_TOKEN;
			reMult = extra;
		}

		float reservesMult = 1f;
		float maxTokens = getNumMarineTokens();
		if (maxTokens > assignedTokens) {
			reservesMult = 1f - (maxTokens - assignedTokens) * LOSS_REDUCTION_PER_RESERVE_TOKEN;
			reservesMult = Math.max(0.5f, reservesMult);
		}
		
		float e = getDefenderIncreaseRaw(market);
		float per = getRaidDefenderIncreasePerRaid();
		float prep = e / per * LOSS_INCREASE_PER_RAID;
		
		stat.modifyMultAlways("danger", danger, "Danger level of objectives");
		stat.modifyMult("hazard", hazard, "Colony hazard rating");
		if (reMult < 1f) {
			stat.modifyMultAlways("reMult", reMult, "High raid effectiveness");
		} else if (reMult > 1f) {
			stat.modifyMultAlways("reMult", reMult, "Low raid effectiveness");
		}
		
		if (reservesMult < 1f && assignedTokens > 0) {
			stat.modifyMultAlways("reservesMult", reservesMult, "Forces held in reserve");
		}
//		else if (reservesMult >= 1f && assignedTokens > 0) {
//			stat.modifyMultAlways("reservesMult", 1f, "No forces held in reserve");
//		}
		
		stat.modifyMult("prep", 1f + prep, "Increased defender preparedness");
		
		stat.applyMods(playerFleet.getStats().getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT));
		
		ListenerUtil.modifyMarineLossesStatPreRaid(market, data, stat);
		
		return stat;
	}
	
	

	protected float getAverageMarineLosses(List<GroundRaidObjectivePlugin> data) {
		MutableStat stat = getMarineLossesStat(data);
		float mult = stat.getModifiedValue();
		if (mult > MAX_MARINE_LOSSES) {
			mult = MAX_MARINE_LOSSES;
		}
		
		float marines = playerFleet.getCargo().getMarines();
		return marines * mult;
	}
	
	protected void addMilitaryResponse() {
		if (market == null) return;
		
		if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) {
			MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
					"player_ground_raid_" + market.getId(), 
					market.getFaction(),
					market.getPrimaryEntity(),
					0.75f,
					30f);
			market.getContainingLocation().addScript(new MilitaryResponseScript(params));
		}
		List<CampaignFleetAPI> fleets = market.getContainingLocation().getFleets();
		for (CampaignFleetAPI other : fleets) {
			if (other.getFaction() == market.getFaction()) {
				MemoryAPI mem = other.getMemoryWithoutUpdate();
				Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "raidAlarm", true, 1f);
			}
		}
	}
	
	protected void raidConfirm(boolean secret) {
		if (temp.raidType == null) {
			raidNeverMind();
			return;
		}
		
//		if (temp.raidType == RaidType.VALUABLE) {
//			dialog.getVisualPanel().showImagePortion("illustrations", "raid_valuables_result", 640, 400, 0, 0, 480, 300);
//		} else if (temp.raidType == RaidType.DISRUPT) {
//			dialog.getVisualPanel().showImagePortion("illustrations", "raid_disrupt_result", 640, 400, 0, 0, 480, 300);
//		}
		
		Random random = getRandom();
		//random = new Random();
		
		if (!DebugFlags.MARKET_HOSTILITIES_DEBUG) {
			Misc.increaseMarketHostileTimeout(market, HOSTILE_ACTIONS_TIMEOUT_DAYS);
		}
		
		addMilitaryResponse();
		
		
		if (market != null) {
			applyDefenderIncreaseFromRaid(market);
		}
		
		setRaidCooldown(getRaidCooldownMax());

		//RecentUnrest.get(market).add(3, Misc.ucFirst(reason));
		int stabilityPenalty = 0;
		if (!temp.nonMarket) {
			String reason = "Recently raided";
			if (Misc.isPlayerFactionSetUp()) {
				reason = playerFaction.getDisplayName() + " raid";
			}
			stabilityPenalty = applyRaidStabiltyPenalty(market, reason, temp.raidMult);
			Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
								   Factions.PLAYER, true, 30f);
			Misc.setRaidedTimestamp(market);
		}
		
		int marines = playerFleet.getCargo().getMarines();
		float probOfLosses = 1f;
		
		int losses = 0;
		if (random.nextFloat() < probOfLosses) {
			float averageLosses = getAverageMarineLosses(temp.objectives);
			float variance = averageLosses / 4f;
			
			//float randomizedLosses = averageLosses - variance + variance * 2f * random.nextFloat();
			float randomizedLosses = StarSystemGenerator.getNormalRandom(
							random, averageLosses - variance, averageLosses + variance);
			if (randomizedLosses < 1f) {
				randomizedLosses = random.nextFloat() < randomizedLosses ? 1f : 0f;
			}
			randomizedLosses = Math.round(randomizedLosses);
			losses = (int) randomizedLosses;
			
			if (losses < 0) losses = 0;
			if (losses > marines) losses = marines;
		}
		
		//losses = random.nextInt(marines / 2);
		
		if (losses <= 0) {
			text.addPara("Your forces have not suffered any casualties.");
			temp.marinesLost = 0;
		} else {
			text.addPara("You forces have suffered casualties during the raid.", Misc.getHighlightColor(), "" + losses);
			playerFleet.getCargo().removeMarines(losses);
			temp.marinesLost = losses;
			AddRemoveCommodity.addCommodityLossText(Commodities.MARINES, losses, text);
		}
		
		
		if (!secret) {
			boolean tOn = playerFleet.isTransponderOn();
			boolean hostile = faction.isHostileTo(Factions.PLAYER);
			CustomRepImpact impact = new CustomRepImpact();
			if (market != null) {
				impact.delta = market.getSize() * -0.01f * 1f;
			} else {
				impact.delta = -0.01f;
			}
			if (!hostile && tOn) {
				impact.ensureAtBest = RepLevel.HOSTILE;
			}
			if (impact.delta != 0 && !faction.isNeutralFaction()) {
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, 
							impact, null, text, true, true),
							faction.getId());
			}
		}
		
		if (stabilityPenalty > 0) {
			text.addPara("Stability of " + market.getName() + " reduced by %s.",
					Misc.getHighlightColor(), "" + stabilityPenalty);
		}
		
//		if (!temp.nonMarket) {
//			if (temp.raidType == RaidType.VALUABLE || true) {
//				text.addPara("The raid was successful in achieving its objectives.");
//			}
//		}
		
		CargoAPI result = performRaid(random, temp.raidMult);
		
		if (market != null) market.reapplyIndustries();
		
		result.sort();
		result.updateSpaceUsed();
		
		temp.raidLoot = result;
		
//		int raidCredits = (int)result.getCredits().get();
//		if (raidCredits < 0) raidCredits = 0;
//		
//		//result.clear();
//		if (raidCredits > 0) {
//			AddRemoveCommodity.addCreditsGainText(raidCredits, text);
//			playerFleet.getCargo().getCredits().add(raidCredits);
//		}
		
		if (temp.xpGained > 0) {
			Global.getSector().getPlayerStats().addXP(temp.xpGained, dialog.getTextPanel());
		}
		if (temp.raidType == RaidType.VALUABLE) {
			if (result.getTotalCrew() + result.getSpaceUsed() + result.getFuel() < 10) {
				dialog.getVisualPanel().showImagePortion("illustrations", "raid_covert_result", 640, 400, 0, 0, 480, 300);
			} else {
				dialog.getVisualPanel().showImagePortion("illustrations", "raid_valuables_result", 640, 400, 0, 0, 480, 300);
			}
		} else if (temp.raidType == RaidType.DISRUPT) {
			dialog.getVisualPanel().showImagePortion("illustrations", "raid_disrupt_result", 640, 400, 0, 0, 480, 300);
		}
		
		boolean withContinue = false;
		
		for (GroundRaidObjectivePlugin curr : temp.objectives) {
			if (curr.withContinueBeforeResult()) {
				withContinue = true;
				break;
			}
		}
		
//		if (market.getMemoryWithoutUpdate().getBoolean("$raid_showContinueBeforeResult"))
//		withContinue = true;
		
		if (withContinue) {
			options.clearOptions();
			options.addOption("Continue", RAID_CONFIRM_CONTINUE);
		} else {
			raidConfirmContinue();
		}
	}
	
	public void raidConfirmContinue() {
		//Random random = getRandom();
		String contText = null;
		if (temp.raidType == RaidType.VALUABLE || true) {
			if (!temp.nonMarket) {
				if (temp.raidType == RaidType.VALUABLE || true) {
					//text.addPara("The raid was successful in achieving its objectives.");
				}
			}
			
//			CargoAPI result = performRaid(random, temp.raidMult);
//			
//			if (market != null) market.reapplyIndustries();
//			
//			result.sort();
//			
//			temp.raidLoot = result;
			
			int raidCredits = (int)temp.raidLoot.getCredits().get();
			if (raidCredits < 0) raidCredits = 0;
			
			//result.clear();
			if (raidCredits > 0) {
				AddRemoveCommodity.addCreditsGainText(raidCredits, text);
				playerFleet.getCargo().getCredits().add(raidCredits);
			}
			
//			if (temp.xpGained > 0) {
//				Global.getSector().getPlayerStats().addXP(temp.xpGained, dialog.getTextPanel());
//			}
			
			if (!temp.raidLoot.isEmpty()) {
				contText = "Pick through the spoils";
			}
			temp.contText = contText;
			
			float assignedTokens = 0f;
			List<Industry> disrupted = new ArrayList<Industry>();
			for (GroundRaidObjectivePlugin curr : temp.objectives) {
				assignedTokens += curr.getMarinesAssigned();
				if (curr instanceof DisruptIndustryRaidObjectivePluginImpl && curr.getSource() != null) {
					disrupted.add(curr.getSource());
				}
			}
			
			RaidResultData data = new RaidResultData();
			data.market = market;
			data.entity = entity;
			data.objectives = temp.objectives;
			data.type = temp.raidType;
			data.raidEffectiveness = temp.raidMult;
			data.xpGained = temp.xpGained;
			data.marinesTokensInReserve = (int) Math.round(getNumMarineTokens() - assignedTokens);
			data.marinesTokens = getNumMarineTokens();
			data.marinesLost = temp.marinesLost;
			
			ListenerUtil.reportRaidObjectivesAchieved(data, dialog, memoryMap);
			
			if (temp.raidType == RaidType.VALUABLE) {
				ListenerUtil.reportRaidForValuablesFinishedBeforeCargoShown(dialog, market, temp, temp.raidLoot);
			} else if (temp.raidType == RaidType.DISRUPT) {
				for (Industry curr : disrupted) {
					ListenerUtil.reportRaidToDisruptFinished(dialog, market, temp, curr);
				}
			}
			
		}
		
		Global.getSoundPlayer().playUISound("ui_raid_finished", 1f, 1f);
		
		FireBest.fire(null, dialog, memoryMap, "PostGroundRaid");
	}
	
	protected CargoAPI performRaid(Random random, float raidEffectiveness) {
		CargoAPI result = Global.getFactory().createCargo(true);
		
		float leftoverRE = (int)Math.round(raidEffectiveness * 100f) % (int)Math.round(RE_PER_MARINE_TOKEN * 100f);
		leftoverRE /= 100f;
		if (raidEffectiveness < RE_PER_MARINE_TOKEN) {
			//leftoverRE = leftoverRE - RE_PER_MARINE_TOKEN;
			leftoverRE = 0f;
		}
		
		long baseSeed = random.nextLong();
		
		int xp = 0;
		for (GroundRaidObjectivePlugin plugin : temp.objectives) {
			float lootMult = 1f + leftoverRE / Math.max(RE_PER_MARINE_TOKEN, raidEffectiveness);
			
			Random curr = new Random(Misc.seedUniquifier() ^ (baseSeed * plugin.getClass().getName().hashCode()));
			xp += plugin.performRaid(result, curr, lootMult, dialog.getTextPanel());
		}

		temp.xpGained = xp;
		
		return result;
	}
	
	
	protected void raidNeverMind() {
		if (temp.nonMarket) {
			raidNonMarket();
		} else {
			raidMenu();
		}
	}
	
	
	protected void raidShowLoot() {
		dialog.getVisualPanel().showLoot("Spoils", temp.raidLoot, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
				//dialog.dismiss();
				finishedRaidOrBombard();
			}
		});
	}
	
	
	protected void printStationState() {
		StationState state = getStationState();
		if (state == StationState.REPAIRS || state == StationState.UNDER_CONSTRUCTION) {
			CampaignFleetAPI fleet = Misc.getStationBaseFleet(market);
			String name = "orbital station";
			if (fleet != null) {
				FleetMemberAPI flagship = fleet.getFlagship();
				if (flagship != null) {
					name = flagship.getVariant().getDesignation().toLowerCase();
				}
			}
			if (state == StationState.REPAIRS) {
				text.addPara("The " + name + " has suffered extensive damage and is not currently combat-capable.");
			} else {
				text.addPara("The " + name + " is under construction and is not currently combat-capable.");
			}
		}
	}

	
	protected void engage() {
		final SectorEntityToken entity = dialog.getInteractionTarget();
		final MemoryAPI memory = getEntityMemory(memoryMap);

		final CampaignFleetAPI primary = getInteractionTargetForFIDPI();
		
		dialog.setInteractionTarget(primary);
		
		final FIDConfig config = new FIDConfig();
		config.leaveAlwaysAvailable = true;
		config.showCommLinkOption = false;
		config.showEngageText = false;
		config.showFleetAttitude = false;
		config.showTransponderStatus = false;
		//config.showWarningDialogWhenNotHostile = false;
		config.alwaysAttackVsAttack = true;
		config.impactsAllyReputation = true;
//		config.impactsEnemyReputation = false;
//		config.pullInAllies = false;
//		config.pullInEnemies = false;
//		config.lootCredits = false;
		
//		config.firstTimeEngageOptionText = "Engage the automated defenses";
//		config.afterFirstTimeEngageOptionText = "Re-engage the automated defenses";
		config.noSalvageLeaveOptionText = "Continue";
		
		config.dismissOnLeave = false;
		config.printXPToDialog = true;
		
		config.straightToEngage = true;
		
		CampaignFleetAPI station = getStationFleet();
		config.playerAttackingStation = station != null;
		
		final FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(config);
		
		final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		config.delegate = new BaseFIDDelegate() {
			@Override
			public void notifyLeave(InteractionDialogAPI dialog) {
				if (primary.isStationMode()) {
					primary.getMemoryWithoutUpdate().clear();
					primary.clearAssignments();
					//primary.deflate();
				}
				
				dialog.setPlugin(originalPlugin);
				dialog.setInteractionTarget(entity);
				
				boolean quickExit = entity.hasTag(Tags.NON_CLICKABLE);
				
				if (!Global.getSector().getPlayerFleet().isValidPlayerFleet() || quickExit) {
					dialog.getOptionPanel().clearOptions();
					dialog.getOptionPanel().addOption("Leave", "marketLeave");
					dialog.getOptionPanel().setShortcut("marketLeave", Keyboard.KEY_ESCAPE, false, false, false, true);
	
					dialog.showTextPanel();
					dialog.setPromptText("You decide to...");
					dialog.getVisualPanel().finishFadeFast();
					text.updateSize();
					
//					dialog.hideVisualPanel();
//					dialog.getVisualPanel().finishFadeFast();
//					dialog.hideTextPanel();
//					dialog.dismiss();
					return;
				}
				
				if (plugin.getContext() instanceof FleetEncounterContext) {
					FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
					if (context.didPlayerWinMostRecentBattleOfEncounter()) {
						// may need to do something here re: station being defeated & timed out
						//FireBest.fire(null, dialog, memoryMap, "BeatDefendersContinue");
					} else {
						//dialog.dismiss();
					}
					
					if (context.isEngagedInHostilities()) {
						dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "NONE", 0);
					}
					
					showDefenses(context.isEngagedInHostilities());
				} else {
					showDefenses(false);
				}
				dialog.getVisualPanel().finishFadeFast();
				
				//dialog.dismiss();
			}
			@Override
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				//bcc.aiRetreatAllowed = false;
				bcc.objectivesAllowed = false;
			}
			@Override
			public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
			}
			
		};
		
		dialog.setPlugin(plugin);
		plugin.init(dialog);
		
	}

	protected CampaignFleetAPI getStationFleet() {
		CampaignFleetAPI station = Misc.getStationFleet(market);
		if (station == null) return null;
		
		if (station.getFleetData().getMembersListCopy().isEmpty()) return null;
		
		return station;
	}
	
	protected CampaignFleetAPI getInteractionTargetForFIDPI() {
		CampaignFleetAPI primary = getStationFleet();
		if (primary == null) {
			CampaignFleetAPI best = null;
			float minDist = Float.MAX_VALUE;
			for (CampaignFleetAPI fleet : Misc.getNearbyFleets(entity, 2000)) {
				if (fleet.getBattle() != null) continue;
				
				if (fleet.getFaction() != market.getFaction()) continue;
				if (fleet.getFleetData().getNumMembers() <= 0) continue;
				
				float dist = Misc.getDistance(entity.getLocation(), fleet.getLocation());
				dist -= entity.getRadius();
				dist -= fleet.getRadius();
				
				if (dist < Misc.getBattleJoinRange() ) {
					if (dist < minDist) {
						best = fleet;
						minDist = dist;
					}
				}
			}
			primary = best;
		} else {
			//primary.setLocation(entity.getLocation().x, entity.getLocation().y);
		}
		return primary;
	}
	
	public static enum StationState {
		NONE,
		OPERATIONAL,
		UNDER_CONSTRUCTION,
		REPAIRS
	}
	
	protected StationState getStationState() {
		CampaignFleetAPI fleet = Misc.getStationFleet(market);
		boolean destroyed = false;
		if (fleet == null) {
			fleet = Misc.getStationBaseFleet(market);
			if (fleet != null) {
				destroyed = true;
			}
		}
		
		if (fleet == null) return StationState.NONE;
		
		MarketAPI market = Misc.getStationMarket(fleet);
		if (market != null) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_STATION)) {
					if (ind.isBuilding() && !ind.isDisrupted() && !ind.isUpgrading()) {
						return StationState.UNDER_CONSTRUCTION;
					}
				}
			}
		}
		
		if (destroyed) return StationState.REPAIRS;
		
		return StationState.OPERATIONAL;
	}
	
	
	public static int applyRaidStabiltyPenalty(MarketAPI target, String desc, float re) {
		int penalty = 0;
		if (re >= 0.75f) penalty = 3;
		else if (re >= 0.5f) penalty = 2;
		else if (re >= 0.25f) penalty = 1;
		if (penalty > 0) {
			RecentUnrest.get(target).add(penalty, desc);
		}
		return penalty;
	}
	
	public static int applyRaidStabiltyPenalty(MarketAPI target, String desc, float re, float maxPenalty) {
		int penalty = Math.round((0.5f + maxPenalty) * re);
		if (penalty > 0) {
			RecentUnrest.get(target).add(penalty, desc);
		}
		return penalty;
	}
	
	
	public static StatModValueGetter statPrinter(final boolean withNegative) {
		return new StatModValueGetter() {
			public String getPercentValue(StatMod mod) {
				String prefix = mod.getValue() > 0 ? "+" : "";
				return prefix + (int)(mod.getValue()) + "%";
			}
			public String getMultValue(StatMod mod) {
				return Strings.X + "" + Misc.getRoundedValue(mod.getValue());
			}
			public String getFlatValue(StatMod mod) {
				String prefix = mod.getValue() > 0 ? "+" : "";
				return prefix + (int)(mod.getValue()) + "";
			}
			public Color getModColor(StatMod mod) {
				if (withNegative && mod.getValue() < 1f) return Misc.getNegativeHighlightColor();
				return null;
			}
		};
	}
	
	
	public static int getBombardmentCost(MarketAPI market, CampaignFleetAPI fleet) {
		float str = getDefenderStr(market);
		int result = (int) (str * Global.getSettings().getFloat("bombardFuelFraction"));
		if (result < 2) result = 2;
		if (fleet != null) {
			float bomardBonus = Misc.getFleetwideTotalMod(fleet, Stats.FLEET_BOMBARD_COST_REDUCTION, 0f);
			result -= bomardBonus;
			if (result < 0) result = 0;
		}
		return result;
	}
	
	public static int getTacticalBombardmentStabilityPenalty() {
		return (int) Global.getSettings().getFloat("bombardTacticalStability");
	}
	public static int getSaturationBombardmentStabilityPenalty() {
		return (int) Global.getSettings().getFloat("bombardSaturationStability");
	}
	

	protected void bombardMenu() {
		float width = 350;
		float opad = 10f;
		float small = 5f;
		
		Color h = Misc.getHighlightColor();
		Color b = Misc.getNegativeHighlightColor();
		
		dialog.getVisualPanel().showImagePortion("illustrations", "bombard_prepare", 640, 400, 0, 0, 480, 300);

		StatBonus defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
		
		float bomardBonus = Misc.getFleetwideTotalMod(playerFleet, Stats.FLEET_BOMBARD_COST_REDUCTION, 0f);
		String increasedBombardKey = "core_addedBombard";
		StatBonus bombardBonusStat = new StatBonus();
		if (bomardBonus > 0) {
			bombardBonusStat.modifyFlat(increasedBombardKey, -bomardBonus, "Specialized fleet bombardment capability");
		}
		
		float defenderStr = (int) Math.round(defender.computeEffective(0f));
		defenderStr -= bomardBonus;
		if (defenderStr < 0) defenderStr = 0;
		
		temp.defenderStr = defenderStr;
		
		TooltipMakerAPI info = text.beginTooltip();
		
		info.setParaSmallInsignia();
		
		String has = faction.getDisplayNameHasOrHave();
		String is = faction.getDisplayNameIsOrAre();
		boolean hostile = faction.isHostileTo(Factions.PLAYER);
		boolean tOn = playerFleet.isTransponderOn();
		float initPad = 0f;
		if (!hostile) {
			info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
					" not currently hostile. A bombardment is a major enough hostile action that it can't be concealed, " +
					"regardless of transponder status.",
					initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			initPad = opad;
		}

		info.addPara("Starship fuel can be easily destabilized, unlocking the destructive " +
				"potential of the antimatter it contains. Ground defenses can counter " +
				"a bombardment, though in practice it only means that more fuel is required to achieve " +
				"the same result.", initPad);
				
		
		if (bomardBonus > 0) {
			info.addPara("Effective ground defense strength: %s", opad, h, "" + (int)defenderStr);
		} else {
			info.addPara("Ground defense strength: %s", opad, h, "" + (int)defenderStr);
		}
		info.addStatModGrid(width, 50, opad, small, defender, true, statPrinter(true));
		if (!bombardBonusStat.isUnmodified()) {
			info.addStatModGrid(width, 50, opad, 3f, bombardBonusStat, true, statPrinter(false));
		}
		
		text.addTooltip();

//		text.addPara("A tactical bombardment will only hit military targets and costs less fuel. A saturation " +
//				"bombardment will devastate the whole colony, and only costs marginally more fuel, as the non-military " +
//				"targets don't have nearly the same degree of hardening.");
		
		temp.bombardCost = getBombardmentCost(market, playerFleet);
		
		int fuel = (int) playerFleet.getCargo().getFuel();
		boolean canBombard = fuel >= temp.bombardCost;
		
		LabelAPI label = text.addPara("A bombardment requires %s fuel. " +
									  "You have %s fuel.",
				h, "" + temp.bombardCost, "" + fuel);
		label.setHighlight("" + temp.bombardCost, "" + fuel);
		label.setHighlightColors(canBombard ? h : b, h);

		options.clearOptions();
		
		options.addOption("Prepare a tactical bombardment", BOMBARD_TACTICAL);
		options.addOption("Prepare a saturation bombardment", BOMBARD_SATURATION);
		
		if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
			canBombard = true;
		}
		if (!canBombard) {
			options.setEnabled(BOMBARD_TACTICAL, false);
			options.setTooltip(BOMBARD_TACTICAL, "Not enough fuel.");
			options.setEnabled(BOMBARD_SATURATION, false);
			options.setTooltip(BOMBARD_SATURATION, "Not enough fuel.");
		}

		options.addOption("Go back", RAID_GO_BACK);
		options.setShortcut(RAID_GO_BACK, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	
	protected void addConfirmOptions() {
		options.clearOptions();
//		if (temp.isSurpriseRaid) {
//			options.addOption("Launch surprise raid", RAID_CONFIRM);
//		} else {
			//options.addOption("Launch full-scale raid", RAID_CONFIRM);
			options.addOption("Launch raid", RAID_CONFIRM);
//		}
		
		boolean tOn = playerFleet.isTransponderOn();
			
		//if (!temp.nonMarket) {
		if (market != null && !market.isPlanetConditionMarketOnly()) {
			options.addOption("Make special efforts to keep your preparations secret, then proceed", RAID_CONFIRM_STORY);
			String req = "";
			if (tOn) {
				req = "\n\nRequires transponder to be turned off";
				options.setEnabled(RAID_CONFIRM_STORY, false);
			}
			options.setTooltip(RAID_CONFIRM_STORY, "Suffer no penalty to your standing with " + market.getFaction().getDisplayNameWithArticle() + ". " +
					"Will not help if forced to turn your transponder on by patrols arriving to investigate the raid." + req);
			options.setTooltipHighlightColors(RAID_CONFIRM_STORY, market.getFaction().getBaseUIColor(), Misc.getNegativeHighlightColor());
			options.setTooltipHighlights(RAID_CONFIRM_STORY, market.getFaction().getDisplayNameWithArticleWithoutArticle(), req.isEmpty() ? req : req.substring(2));
			StoryOptionParams params = new StoryOptionParams(RAID_CONFIRM_STORY, 1, "noRepPenaltyRaid", Sounds.STORY_POINT_SPEND_LEADERSHIP,
					"Secretly raided " + market.getName() + "");
			SetStoryOption.set(dialog, params, 
				new BaseOptionStoryPointActionDelegate(dialog, params) {
					@Override
					public void confirm() {
						super.confirm();
						raidConfirm(true);
					}
			});
		}
			
			
		options.addOption("Never mind", RAID_NEVER_MIND);
		options.setShortcut(RAID_NEVER_MIND, Keyboard.KEY_ESCAPE, false, false, false, true);
		
		boolean hostile = faction.isHostileTo(Factions.PLAYER);
		if (tOn && !hostile && !faction.isNeutralFaction()) {
			options.addOptionConfirmation(RAID_CONFIRM,
					"The " + faction.getDisplayNameLong() + 
					" " + faction.getDisplayNameIsOrAre() + 
					" not currently hostile, and you have been positively identified. " +
					"Are you sure you want to engage in open hostilities?", "Yes", "Never mind");
		}
	}
	
	public static List<Industry> getTacticalBombardmentTargets(MarketAPI market) {
		int dur = getBombardDisruptDuration();
		List<Industry> targets = new ArrayList<Industry>();
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_TACTICAL_BOMBARDMENT)) {
				if (ind.getDisruptedDays() >= dur * 0.8f) continue;
				targets.add(ind);
			}
		}
		return targets;
	}
	
	protected void bombardTactical() {
		
		temp.bombardType = BombardType.TACTICAL; 
		
		boolean hostile = faction.isHostileTo(Factions.PLAYER);
		temp.willBecomeHostile.clear();
		temp.willBecomeHostile.add(faction);	
		
		float opad = 10f;
		float small = 5f;
		
		Color h = Misc.getHighlightColor();
		Color b = Misc.getNegativeHighlightColor();
		
		
		int dur = getBombardDisruptDuration();
		
		List<Industry> targets = getTacticalBombardmentTargets(market);
		temp.bombardmentTargets.clear();
		temp.bombardmentTargets.addAll(targets);
		
		if (targets.isEmpty()) {
			text.addPara(market.getName() + " does not have any undisrupted military targets that would be affected by a tactical bombardment.");
			addBombardNeverMindOption();
			return;	
		}
		
		
		int fuel = (int) playerFleet.getCargo().getFuel();
		text.addPara("A tactical bombardment will destabilize the colony, and will also disrupt the " +
				"following military targets for approximately %s days:",
					 h, "" + dur);
		
		TooltipMakerAPI info = text.beginTooltip();
		
		info.setParaSmallInsignia();
		info.setParaFontDefault();
		
		info.setBulletedListMode(BaseIntelPlugin.INDENT);
		float initPad = 0f;
		for (Industry ind : targets) {
			//info.addPara(ind.getCurrentName(), faction.getBaseUIColor(), initPad);
			info.addPara(ind.getCurrentName(), initPad);
			initPad = 3f;
		}
		info.setBulletedListMode(null);
		
		text.addTooltip();
		
		text.addPara("The bombardment requires %s fuel. " +
					 "You have %s fuel.",
					 h, "" + temp.bombardCost, "" + fuel);
		
		addBombardConfirmOptions();
	}
	
	protected void bombardSaturation() {
		
		temp.bombardType = BombardType.SATURATION;

		temp.willBecomeHostile.clear();
		temp.willBecomeHostile.add(faction);
		
		List<FactionAPI> nonHostile = new ArrayList<FactionAPI>();
		nonHostile.add(faction);
		for (FactionAPI faction : Global.getSector().getAllFactions()) {
			if (temp.willBecomeHostile.contains(faction)) continue;
			if (faction.getCustomBoolean(Factions.CUSTOM_CARES_ABOUT_ATROCITIES)) {
				boolean hostile = faction.isHostileTo(Factions.PLAYER);
				temp.willBecomeHostile.add(faction);
				if (!hostile) {
					nonHostile.add(faction);
				}
			}
			
		}
		
		float opad = 10f;
		float small = 5f;
		
		Color h = Misc.getHighlightColor();
		Color b = Misc.getNegativeHighlightColor();
		
		
		int dur = getBombardDisruptDuration();
		
		List<Industry> targets = new ArrayList<Industry>();
		for (Industry ind : market.getIndustries()) {
			if (!ind.getSpec().hasTag(Industries.TAG_NO_SATURATION_BOMBARDMENT)) {
				if (ind.getDisruptedDays() >= dur * 0.8f) continue;
				targets.add(ind);
			}
		}
		temp.bombardmentTargets.clear();
		temp.bombardmentTargets.addAll(targets);
		
		boolean destroy = market.getSize() <= getBombardDestroyThreshold();
		if (Misc.isStoryCritical(market)) destroy = false;
		
		int fuel = (int) playerFleet.getCargo().getFuel();
		if (destroy) {
			text.addPara("A saturation bombardment of a colony this size will destroy it utterly.");
		} else {
			text.addPara("A saturation bombardment will destabilize the colony, reduce its population, " +
					"and disrupt all operations for a long time.");
		}
		
		
//		TooltipMakerAPI info = text.beginTooltip();
//		info.setParaFontDefault();
//		
//		info.setBulletedListMode(BaseIntelPlugin.INDENT);
//		float initPad = 0f;
//		for (Industry ind : targets) {
//			//info.addPara(ind.getCurrentName(), faction.getBaseUIColor(), initPad);
//			info.addPara(ind.getCurrentName(), initPad);
//			initPad = 3f;
//		}
//		info.setBulletedListMode(null);
//		
//		text.addTooltip();
		

		if (nonHostile.isEmpty()) {
			text.addPara("An atrocity of this scale can not be hidden, but any factions that would " +
					"be dismayed by such actions are already hostile to you.");
		} else {
			text.addPara("An atrocity of this scale can not be hidden, " +
						 "and will make the following factions hostile:");
		}
		
		if (!nonHostile.isEmpty()) {
			TooltipMakerAPI info = text.beginTooltip();
			info.setParaFontDefault();
			
			info.setBulletedListMode(BaseIntelPlugin.INDENT);
			float initPad = 0f;
			for (FactionAPI fac : nonHostile) {
				info.addPara(Misc.ucFirst(fac.getDisplayName()), fac.getBaseUIColor(), initPad);
				initPad = 3f;
			}
			info.setBulletedListMode(null);
			
			text.addTooltip();
		}
		
		text.addPara("The bombardment requires %s fuel. " +
					 "You have %s fuel.",
					 h, "" + temp.bombardCost, "" + fuel);
		
		addBombardConfirmOptions();
	}
	
	protected void bombardConfirm() {
		
		if (temp.bombardType == null) {
			bombardNeverMind();
			return;
		}
		
		if (temp.bombardType == BombardType.TACTICAL) {
			dialog.getVisualPanel().showImagePortion("illustrations", "bombard_tactical_result", 640, 400, 0, 0, 480, 300);
		} else {
			dialog.getVisualPanel().showImagePortion("illustrations", "bombard_saturation_result", 640, 400, 0, 0, 480, 300);
		}
		
		Random random = getRandom();
		
		if (!DebugFlags.MARKET_HOSTILITIES_DEBUG) {
			float timeout = TACTICAL_BOMBARD_TIMEOUT_DAYS;
			if (temp.bombardType == BombardType.SATURATION) {
				timeout = SATURATION_BOMBARD_TIMEOUT_DAYS;
			}
			Misc.increaseMarketHostileTimeout(market, timeout);
			
			timeout *= 0.7f;
			
			for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(market.getContainingLocation())) {
				if (curr == market) continue;
				boolean cares = curr.getFaction().getCustomBoolean(Factions.CUSTOM_CARES_ABOUT_ATROCITIES);
				cares &= temp.bombardType == BombardType.SATURATION;
				
				if (curr.getFaction().isNeutralFaction()) continue;
				if (curr.getFaction().isPlayerFaction()) continue;
				if (curr.getFaction().isHostileTo(market.getFaction()) && !cares) continue;
				
				Misc.increaseMarketHostileTimeout(curr, timeout);
			}
		}
		
		addMilitaryResponse();
		
		playerFleet.getCargo().removeFuel(temp.bombardCost);
		AddRemoveCommodity.addCommodityLossText(Commodities.FUEL, temp.bombardCost, text);
	
		for (FactionAPI curr : temp.willBecomeHostile) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = market.getSize() * -0.01f * 1f;
			impact.ensureAtBest = RepLevel.HOSTILE;
			if (temp.bombardType == BombardType.SATURATION) {
				if (curr == faction) {
					impact.ensureAtBest = RepLevel.VENGEFUL;
				}
				impact.delta = market.getSize() * -0.01f * 1f;
			}
			Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
					impact, null, text, true, true),
					curr.getId());
		}
	
		if (temp.bombardType == BombardType.SATURATION) {
			int atrocities = (int) Global.getSector().getCharacterData().getMemoryWithoutUpdate().getFloat(MemFlags.PLAYER_ATROCITIES);
			atrocities++;
			Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.PLAYER_ATROCITIES, atrocities);
		}
		
		
		int stabilityPenalty = getTacticalBombardmentStabilityPenalty();
		if (temp.bombardType == BombardType.SATURATION) {
			stabilityPenalty = getSaturationBombardmentStabilityPenalty();
		}
		boolean destroy = temp.bombardType == BombardType.SATURATION && market.getSize() <= getBombardDestroyThreshold();
		if (Misc.isStoryCritical(market)) destroy = false;
		
		if (stabilityPenalty > 0 && !destroy) {
			String reason = "Recently bombarded";
			if (Misc.isPlayerFactionSetUp()) {
				reason = playerFaction.getDisplayName() + " bombardment";
			}
			RecentUnrest.get(market).add(stabilityPenalty, reason);
			text.addPara("Stability of " + market.getName() + " reduced by %s.",
					Misc.getHighlightColor(), "" + stabilityPenalty);
		}
		
		if (market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.POLLUTION)) {
			market.addCondition(Conditions.POLLUTION);
		}
		
		if (!destroy) {
			for (Industry curr : temp.bombardmentTargets) {
				int dur = getBombardDisruptDuration();
				dur *= StarSystemGenerator.getNormalRandom(random, 1f, 1.25f);
				curr.setDisrupted(dur);
			}
		}
		
		
		
		if (temp.bombardType == BombardType.TACTICAL) {
			text.addPara("Military operations disrupted.");
			
			ListenerUtil.reportTacticalBombardmentFinished(dialog, market, temp);
		} else if (temp.bombardType == BombardType.SATURATION) {
			if (destroy) {
				DecivTracker.decivilize(market, true);
				text.addPara(market.getName() + " destroyed.");
			} else {
				int prevSize = market.getSize();
				CoreImmigrationPluginImpl.reduceMarketSize(market);
				if (prevSize == market.getSize()) {
					text.addPara("All operations disrupted.");
				} else {
					text.addPara("All operations disrupted. Colony size reduced to %s.", 
							Misc.getHighlightColor()
							, "" + market.getSize());
				}
				
			}
			ListenerUtil.reportSaturationBombardmentFinished(dialog, market, temp);
		}
		
		if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
			if (dialog.getInteractionTarget() != null &&
					dialog.getInteractionTarget().getMarket() != null) {
				Global.getSector().setPaused(false);
				dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
				Global.getSector().setPaused(true);
			}
			((RuleBasedDialog) dialog.getPlugin()).updateMemory();
		}
		
		Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED, 
	   			   			  Factions.PLAYER, true, 30f);

		if (destroy) {
			if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
				((RuleBasedDialog) dialog.getPlugin()).updateMemory();
//				market.getMemoryWithoutUpdate().unset("$tradeMode");
//				entity.getMemoryWithoutUpdate().unset("$tradeMode");
			}
		}
		
		addBombardVisual(market.getPrimaryEntity());
		
		addBombardContinueOption();
	}
	
	
	protected void bombardNeverMind() {
		bombardMenu();		
	}

	protected void raidResult() {
		if (temp.raidLoot != null) {
			if (temp.raidLoot.isEmpty()) {
//				clearTemp();
//				showDefenses(true);
				//dialog.dismiss();
				finishedRaidOrBombard();
			} else {
				raidShowLoot();
			}
			return;
		} else {
			//dialog.dismiss();
			finishedRaidOrBombard();
		}
	}
	
	protected void bombardResult() {
		//dialog.dismiss();
		finishedRaidOrBombard();
	}
	
	protected void finishedRaidOrBombard() {
		//showDefenses(true);
	
		new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
		
		//FireAll.fire(null, dialog, memoryMap, "MarketPostOpen");
		dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$menuState", "main", 0);
		if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains("$tradeMode")) {
			if (market.isPlanetConditionMarketOnly()) {
				dialog.getInteractionTarget().getMemoryWithoutUpdate().unset("$hasMarket");
			}
			dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "NONE", 0);
		} else {
			// station that's now abandoned
			dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "OPEN", 0);
		}
		
		if (temp.nonMarket) {
			String trigger = temp.raidContinueTrigger;
			if (trigger == null || trigger.isEmpty()) trigger = "OpenInteractionDialog";
			FireAll.fire(null, dialog, memoryMap, trigger);
		} else {
			FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
		}
		
		clearTemp();
	}
	
	protected void addBombardContinueOption() {
		addBombardContinueOption(null);
	}
	protected void addBombardContinueOption(String text) {
		if (text == null) text = "Continue";
		options.clearOptions();
		options.addOption(text, BOMBARD_RESULT);
	}
	
	
	protected boolean checkDebtEffect() {
		String key = "$debt_effectTimeout";
		if (Global.getSector().getMemoryWithoutUpdate().contains(key)) return false;
		
		//if (true) return true;
		
		// can't exactly melt away in that small an outpost, not that it's outright desertion
		// but it's also not a great place to leave the fleet
		if (market.isPlayerOwned() && market.getSize() <= 3) return false;
		
		MonthlyReport report = SharedData.getData().getPreviousReport();
		
		
		// require 2 months of debt in a row
		if (report.getPreviousDebt() <= 0 || report.getDebt() <= 0) return false;
		
		float debt = report.getDebt() + report.getDebt();
		float income = report.getRoot().totalIncome;
		if (income < 1) income = 1;
		
		float f = debt / income;
		if (f > 1) f = 1;
		if (f < 0) f = 0;
		// don't penalize minor shortfalls
		if (f < 0.1f) return false;
		
		// and don't reduce crew below a certain minimum
		int crew = playerFleet.getCargo().getCrew();
		int marines = playerFleet.getCargo().getMarines();
		if (crew <= 10 && marines <= 10) return false;
		
		return true;
	}
	
	protected void applyDebtEffect() {
		
		MonthlyReport report = SharedData.getData().getPreviousReport();
		float debt = report.getDebt() + report.getDebt();
		float income = report.getRoot().totalIncome;
		if (income < 1) income = 1;
		
		float f = debt / income;
		if (f > 1) f = 1;
		if (f < 0) f = 0;
		
		int crew = playerFleet.getCargo().getCrew();
		int marines = playerFleet.getCargo().getMarines();
		
		float maxLossFraction = 0.03f + Math.min(f + 0.05f, 0.2f) * (float) Math.random();
		float marineLossFraction = 0.03f + Math.min(f + 0.05f, 0.2f) * (float) Math.random();
		
		
		int crewLoss = (int) (crew * maxLossFraction);
		if (crewLoss < 2) crewLoss = 2;
		
		int marineLoss = (int) (marines * marineLossFraction);
		if (marineLoss < 2) marineLoss = 2;
		
		dialog.getVisualPanel().showImagePortion("illustrations", "crew_leaving", 640, 400, 0, 0, 480, 300);
		
		text.addPara("The lack of consistent pay over the last few months has caused discontent among your crew. " +
				"A number take this opportunity to leave your employment.");
		
		if (crewLoss < crew) {
			playerFleet.getCargo().removeCrew(crewLoss);
			AddRemoveCommodity.addCommodityLossText(Commodities.CREW, crewLoss, text);
		}
		if (marineLoss <= marines) {
			playerFleet.getCargo().removeMarines(marineLoss);
			AddRemoveCommodity.addCommodityLossText(Commodities.MARINES, marineLoss, text);
		}
		
		String key = "$debt_effectTimeout";
		Global.getSector().getMemoryWithoutUpdate().set(key, true, 30f + (float) Math.random() * 10f);
		
		options.clearOptions();
		options.addOption("Continue", DEBT_RESULT_CONTINUE);
	}

	
	public void doGenericRaid(FactionAPI faction, float attackerStr) {
		doGenericRaid(faction, attackerStr, 3f);
	}
	
	public void doGenericRaid(FactionAPI faction, float attackerStr, float maxPenalty) {
		// needed for pirate raids not to stack
		// not needed anymore, but doesn't hurt anything
		if (Misc.flagHasReason(market.getMemoryWithoutUpdate(), 
				MemFlags.RECENTLY_RAIDED, faction.getId())) {
			return;
		}
		
		float re = getRaidEffectiveness(market, attackerStr);
		if (maxPenalty == 3) {
			applyRaidStabiltyPenalty(market, Misc.ucFirst(faction.getPersonNamePrefix()) + " raid", re);
		} else {
			applyRaidStabiltyPenalty(market, Misc.ucFirst(faction.getPersonNamePrefix()) + " raid", re, maxPenalty);
		}
		//RecentUnrest.get(market).add(3, Misc.ucFirst(faction.getPersonNamePrefix()) + " raid");
		
		Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
							   faction.getId(), true, 30f);
		Misc.setRaidedTimestamp(market);
	}
	
	public boolean doIndustryRaid(FactionAPI faction, float attackerStr, Industry industry, float durMult) {
		temp.raidType = RaidType.DISRUPT;
		temp.target = industry;
		
		StatBonus defenderBase = new StatBonus(); 
		
		StatBonus defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
		String increasedDefensesKey = "core_addedDefStr";
		float added = getDefenderIncreaseValue(market);
		if (added > 0) {
			defender.modifyFlat(increasedDefensesKey, added, "Increased defender preparedness");
		}
		float defenderStr = (int) Math.round(defender.computeEffective(defenderBase.computeEffective(0f)));
		defender.unmodifyFlat(increasedDefensesKey);
		
		temp.attackerStr = attackerStr;
		temp.defenderStr = defenderStr;
		
		boolean hasForces = true;
		boolean canDisrupt = true;
		temp.raidMult = attackerStr / Math.max(1f, (attackerStr + defenderStr));
		temp.raidMult = Math.round(temp.raidMult * 100f) / 100f;
		
		if (temp.raidMult < VALUABLES_THRESHOLD) {
			hasForces = false;
		}
		if (temp.raidMult < DISRUPTION_THRESHOLD) {
			canDisrupt = false;
		}
		if (!canDisrupt) return false;
		
		
		Random random = getRandom();
		
		applyDefenderIncreaseFromRaid(market);
		
		String reason = faction.getDisplayName() + " raid";
		
		applyRaidStabiltyPenalty(market, reason, temp.raidMult);
		Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
							   faction.getId(), true, 30f);
		Misc.setRaidedTimestamp(market);
		
		if (temp.target != null) {
			float dur = computeBaseDisruptDuration(temp.target);
			dur *= StarSystemGenerator.getNormalRandom(random, 1f, 1.25f);
			dur *= durMult;
			if (dur < 2) dur = 2;
			float already = temp.target.getDisruptedDays();
			temp.target.setDisrupted(already + dur);
		}
		
		return true;
	}
	
	
	public void doBombardment(FactionAPI faction, BombardType type) {
		temp.bombardType = type;
		
		Random random = getRandom();
	
		int dur = getBombardDisruptDuration();
		
		int stabilityPenalty = getTacticalBombardmentStabilityPenalty();
		if (temp.bombardType == BombardType.SATURATION) {
			stabilityPenalty = getSaturationBombardmentStabilityPenalty();
			
			List<Industry> targets = new ArrayList<Industry>();
			for (Industry ind : market.getIndustries()) {
				if (!ind.getSpec().hasTag(Industries.TAG_NO_SATURATION_BOMBARDMENT)) {
					if (ind.getDisruptedDays() >= dur * 0.8f) continue;
					targets.add(ind);
				}
			}
			temp.bombardmentTargets.clear();
			temp.bombardmentTargets.addAll(targets);
		} else {
			List<Industry> targets = new ArrayList<Industry>();
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_TACTICAL_BOMBARDMENT)) {
					if (ind.getDisruptedDays() >= dur * 0.8f) continue;
					targets.add(ind);
				}
			}
			temp.bombardmentTargets.clear();
			temp.bombardmentTargets.addAll(targets);
		}
		
		
		if (stabilityPenalty > 0) {
			String reason = faction.getDisplayName() + " bombardment";
			RecentUnrest.get(market).add(stabilityPenalty, reason);
		}
		
		if (market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.POLLUTION)) {
			market.addCondition(Conditions.POLLUTION);
		}
		
		for (Industry curr : temp.bombardmentTargets) {
			dur = getBombardDisruptDuration();
			dur *= StarSystemGenerator.getNormalRandom(random, 1f, 1.25f);
			curr.setDisrupted(dur);
		}
		
		if (temp.bombardType == BombardType.TACTICAL) {
		} else if (temp.bombardType == BombardType.SATURATION) {
			boolean destroy = market.getSize() <= getBombardDestroyThreshold();
			if (Misc.isStoryCritical(market)) destroy = false;
			if (destroy) {
				DecivTracker.decivilize(market, true);
			} else {
				CoreImmigrationPluginImpl.reduceMarketSize(market);
			}
		}
		
		
		Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED, 
				   			   faction.getId(), true, 30f);
		
		addBombardVisual(market.getPrimaryEntity());
	}
	
	
	public static void addBombardVisual(SectorEntityToken target) {
		if (target != null && target.isInCurrentLocation()) {
			int num = (int) (target.getRadius() * target.getRadius() / 300f);
			num *= 2;
			if (num > 150) num = 150;
			if (num < 10) num = 10;
			target.addScript(new BombardmentAnimation(num, target));
		}
	}
	
	public static class BombardmentAnimation implements EveryFrameScript {
		public BombardmentAnimation(int num, SectorEntityToken target) {
			this.num = num;
			this.target = target;
		}
		int num = 0;
		SectorEntityToken target;
		int added = 0;
		float elapsed = 0;
		public boolean runWhilePaused() {
			return false;
		}
		public boolean isDone() {
			return added >= num;
		}
		public void advance(float amount) {
			elapsed += amount * (float) Math.random();
			if (elapsed < 0.03f) return;
			
			elapsed = 0f;
			
			int curr = (int) Math.round(Math.random() * 4);
			if (curr < 1) curr = 0;
			
			Color color = new Color(255, 165, 100, 255);
			
			Vector2f vel = new Vector2f();
			
			if (target.getOrbit() != null && 
					target.getCircularOrbitRadius() > 0 && 
					target.getCircularOrbitPeriod() > 0 && 
					target.getOrbitFocus() != null) {
				float circumference = 2f * (float) Math.PI * target.getCircularOrbitRadius();
				float speed = circumference / target.getCircularOrbitPeriod();
				
				float dir = Misc.getAngleInDegrees(target.getLocation(), target.getOrbitFocus().getLocation()) + 90f;
				vel = Misc.getUnitVectorAtDegreeAngle(dir);
				vel.scale(speed / Global.getSector().getClock().getSecondsPerDay());
			}
			
			for (int i = 0; i < curr; i++) {
				float glowSize = 50f + 50f * (float) Math.random();
				float angle = (float) Math.random() * 360f;
				float dist = (float) Math.sqrt(Math.random()) * target.getRadius();
				
				float factor = 0.5f + 0.5f * (1f - (float)Math.sqrt(dist / target.getRadius()));;
				glowSize *= factor;
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(dist);
				Vector2f.add(loc, target.getLocation(), loc);
				
				Color c2 = Misc.scaleColor(color, factor);
				//c2 = color;
				Misc.addHitGlow(target.getContainingLocation(), loc, vel, glowSize, c2);
				added++;
				
				if (i == 0) {
					dist = Misc.getDistance(loc, Global.getSector().getPlayerFleet().getLocation());
					if (dist < HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE) {
						float volumeMult = 1f - (dist / HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE);
						volumeMult = (float) Math.sqrt(volumeMult);
						volumeMult *= 0.1f * factor;
						if (volumeMult > 0) {
							Global.getSoundPlayer().playSound("mine_explosion", 1f, 1f * volumeMult, loc, Misc.ZERO);
						}
					}
				}
			}
		}
	}
	
	
	protected boolean checkMercsLeaving() {
		String key = "$mercs_leaveTimeout";
		if (Global.getSector().getMemoryWithoutUpdate().contains(key)) return false;
		
		if (market.isHidden()) return false;
		if (market.getSize() <= 3) return false;
		
		List<OfficerDataAPI> mercs = Misc.getMercs(playerFleet);
		if (mercs.isEmpty()) return false;
		
		MonthlyReport report = SharedData.getData().getPreviousReport();
		boolean debt = report.getDebt() > 0;
		
		float contractDur = Global.getSettings().getFloat("officerMercContractDur");
		
		for (OfficerDataAPI od : mercs) {
			float elapsed = Misc.getMercDaysSinceHired(od.getPerson());
			
			if (elapsed > contractDur || debt) {
				dialog.getInteractionTarget().setActivePerson(od.getPerson());
				//dialog.getVisualPanel().showPersonInfo(getPerson(), true);
				((RuleBasedInteractionDialogPluginImpl)dialog.getPlugin()).notifyActivePersonChanged();
				return true;
			}
		}
		
		return false;
	}
	
	protected void convinceMercToStay() {
		PersonAPI merc = dialog.getInteractionTarget().getActivePerson();
		dialog.getInteractionTarget().setActivePerson(null);
		
		if (merc != null) {
			Misc.setMercHiredNow(merc);
			
			String key = "$mercs_leaveTimeout";
			Global.getSector().getMemoryWithoutUpdate().set(key, true, 5f + (float) Math.random() * 5f);
		}
		
	}
	
	protected void mercLeaves() {
		PersonAPI merc = dialog.getInteractionTarget().getActivePerson();
		dialog.getInteractionTarget().setActivePerson(null);
		
		if (merc != null) {
			FleetMemberAPI member = playerFleet.getFleetData().getMemberWithCaptain(merc);
			if (member != null) {
				member.setCaptain(null);
			}
			playerFleet.getFleetData().removeOfficer(merc);
			
			AddRemoveCommodity.addOfficerLossText(merc, text);
			
			String key = "$mercs_leaveTimeout";
			Global.getSector().getMemoryWithoutUpdate().set(key, true, 5f + (float) Math.random() * 5f);
		}
	}

}




















