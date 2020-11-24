package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.BattleAPI.BattleSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DisengageHarryAvailability;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.EngagementOutcome;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.PursueAvailability;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.Status;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.InitialBoardingResponse;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.PursuitOption;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatReadinessPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext.BoardingResult;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext.EngageBoardableOutcome;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class FleetInteractionDialogPluginImpl implements InteractionDialogPlugin, RuleBasedDialog {

	public static interface FIDConfigGen {
		FIDConfig createConfig();
	}
	
	public static class FIDConfig {
		public boolean showCommLinkOption = true;
		public boolean leaveAlwaysAvailable = false;
		public boolean showWarningDialogWhenNotHostile = true;
		public boolean showTransponderStatus = true;
		public boolean showFleetAttitude = true;
		public boolean showEngageText = true;
		public boolean alwaysAttackVsAttack = false;
		public boolean dismissOnLeave = true;
		public boolean lootCredits = true;
		
		public boolean impactsEnemyReputation = true;
		public boolean impactsAllyReputation = true;
		
		public boolean pullInAllies = true;
		public boolean pullInEnemies = true;
		public boolean pullInStations = true;
		
		public String noSalvageLeaveOptionText = null;
		public String firstTimeEngageOptionText = null;
		public String afterFirstTimeEngageOptionText = null;
		
		public FIDDelegate delegate = null;
		public boolean printXPToDialog = false;
		
		public boolean justShowFleets = false;
		public boolean showPullInText = true;
		
		public boolean straightToEngage = false;
		public boolean playerAttackingStation = false;
		public boolean playerDefendingStation = false;
		
		
		public Random salvageRandom = null;
	}
	
	public static interface FIDDelegate {
		public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage);
		public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc);
		public void notifyLeave(InteractionDialogAPI dialog);
	}
	
	public static class BaseFIDDelegate implements FIDDelegate {
		public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {}
		public void notifyLeave(InteractionDialogAPI dialog) {}
		public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {}
	}
	
	
	
	
	protected static enum VisualType {
		FLEET_INFO,
		OTHER,
	}
	
	
	public static enum OptionId {
		INIT,
		PRINT_ONGOING_BATTLE_INFO,
		OPEN_COMM,
		CUT_COMM,
		ENGAGE,
		ATTEMPT_TO_DISENGAGE,
		DISENGAGE,
		SCUTTLE,
		PURSUE,
		AUTORESOLVE_PURSUE,
		HARRY_PURSUE,
		LET_THEM_GO,
		LEAVE,
		CONTINUE_LEAVE,
		CONTINUE,
		GO_TO_MAIN,
		GO_TO_PRE_BATTLE,
		RECOVERY_SELECT,
		RECOVERY_CONTINUE,
		CONTINUE_LOOT,
		CONTINUE_INTO_BATTLE,
		
		CONTINUE_INTO_BOARDING,
		BOARDING_ACTION,
		SELECT_FLAGSHIP,
		CRASH_MOTHBALL,
		ENGAGE_BOARDABLE,
		ABORT_BOARDING_ACTION,
		HARD_DOCK,
		LAUNCH_ASSAULT_TEAMS,
		LET_IT_GO,
		
		SELECTOR_MARINES,
		SELECTOR_CREW,
		
		REINIT_CONTINUE,
		
		INITIATE_BATTLE,
		JOIN_ONGOING_BATTLE,
		CONTINUE_ONGOING_BATTLE,
		
		DEV_MODE_ESCAPE,
	}
	
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	protected CampaignFleetAPI otherFleet;
	
	protected FleetGoal playerGoal = FleetGoal.ATTACK;
	protected FleetGoal otherGoal = FleetGoal.ATTACK;

	protected VisualType currVisualType = VisualType.FLEET_INFO;
	
	protected FleetEncounterContext context = new FleetEncounterContext();
	
	protected static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	protected static final Color FRIEND_COLOR = Global.getSettings().getColor("textFriendColor");
	protected static final Color ENEMY_COLOR = Misc.getNegativeHighlightColor();
	
	protected RuleBasedInteractionDialogPluginImpl conversationDelegate;
	protected boolean ongoingBattle = false;
	protected boolean firstEngagement = true;
	protected boolean joinedBattle = false;
	
	public static boolean inConversation = false;
	public static boolean directToComms = false;
	
	protected FIDConfig config;
	
	public FleetInteractionDialogPluginImpl() {
		this(null);
	}
	public FleetInteractionDialogPluginImpl(FIDConfig params) {
		this.config = params;
		
		if (origFlagship == null) {
			origFlagship = Global.getSector().getPlayerFleet().getFlagship();
		}
		if (origCaptains.isEmpty()) {
			for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
				origCaptains.put(member, member.getCaptain());
			}
		}
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return conversationDelegate == null ? null : conversationDelegate.getMemoryMap();
	}
	
	private boolean skipAttitudeOnInit = false;
	public void reinit(boolean withContinueOnRuleFound) {
		RulesAPI rules = Global.getSector().getRules();
		RuleAPI rule = rules.getBestMatching(null, "BeginFleetEncounter", dialog, conversationDelegate.getMemoryMap());
		if (rule == null || !withContinueOnRuleFound) {
			reinitPostContinue();
		} else {
			options.clearOptions();
			options.addOption("Continue", OptionId.REINIT_CONTINUE, null);
			if (Global.getSettings().isDevMode()) {
				DevMenuOptions.addOptions(dialog);
			}
		}
	}
	
	public void reinitPostContinue() {
		//init(dialog);
		inConversation = false;
		directToComms = false;
		
		conversationDelegate.fireBest("BeginFleetEncounter");
		if (directToComms) {
			optionSelected(null, OptionId.OPEN_COMM);
		} else {
			//skipAttitudeOnInit = true;
			optionSelected(null, OptionId.INIT);
		}

	}
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
		if (this.config == null) {
			MemoryAPI memory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
//			if (memory.contains(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE)) {
//				this.config = (FIDConfig) memory.get(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE);
//			} else 
			if (memory.contains(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN)) {
				this.config = ((FIDConfigGen) memory.get(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN)).createConfig();
			} else {
				this.config = new FIDConfig();
			}
		}
		
		
		
//		boolean sampling = true;
//		while (sampling) {
		if (Global.getSettings().isDevMode()) {
			dialog.setOptionOnEscape("dev mode exit", OptionId.DEV_MODE_ESCAPE);
			dialog.setOptionOnConfirm("dev mode exit", OptionId.DEV_MODE_ESCAPE);
		}
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		otherFleet = (CampaignFleetAPI) (dialog.getInteractionTarget());

//		playerFleet.getFleetData().takeSnapshot();
//		otherFleet.getFleetData().takeSnapshot();
		
		if (context.getBattle() == null) {
			if (otherFleet.getBattle() == null || otherFleet.getBattle().isDone()) {
				ongoingBattle = false;
				BattleAPI battle = Global.getFactory().createBattle(playerFleet, otherFleet);
				context.setBattle(battle);
				pullInNearbyFleets();
			} else {
				ongoingBattle = true;
				context.setBattle(otherFleet.getBattle());
				if (context.getBattle().canJoin(playerFleet)) {
					//context.getBattle().join(playerFleet);
					pullInNearbyFleets();
				}
			}
		}
		
		for (CampaignFleetAPI fleet : context.getBattle().getBothSides()) {
			fleet.inflateIfNeeded();
		}
		context.getBattle().genCombined();
		
		visual.setVisualFade(0.25f, 0.25f);
		if (!config.straightToEngage) {
			if (ongoingBattle && !joinedBattle) {
				BattleAPI b = context.getBattle();
				String titleOne = b.getPrimary(b.getSideOne()).getNameWithFactionKeepCase();
				if (b.getSideOne().size() > 1) titleOne += ", with allies";
				String titleTwo = b.getPrimary(b.getSideTwo()).getNameWithFactionKeepCase();
				if (b.getSideTwo().size() > 1) titleTwo += ", with allies";
				visual.showPreBattleJoinInfo(null, playerFleet, Misc.ucFirst(titleOne), Misc.ucFirst(titleTwo), context);
			} else {
				//visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
				showFleetInfo();
			}
		}
		
		inConversation = false;
		directToComms = false;
		conversationDelegate = new RuleBasedInteractionDialogPluginImpl();
		conversationDelegate.setEmbeddedMode(true);
		conversationDelegate.init(dialog);
		
//		}
		
		if (!config.justShowFleets) {
	//		if (ongoingBattle) {
			conversationDelegate.getMemoryMap().get(MemKeys.LOCAL).set("$ongoingBattle", ongoingBattle, 0);
			if (!ongoingBattle) {
				conversationDelegate.fireBest("BeginFleetEncounter");
			}
	//		} else {
	//			conversationDelegate.fireBest("OngoingBattleEncounter");
	//		}
		
			if (directToComms) {
				optionSelected(null, OptionId.OPEN_COMM);
			} else {
				optionSelected(null, OptionId.INIT);
			}
			
			if (config.straightToEngage){
				if (ongoingBattle) {
					optionSelected(null, OptionId.JOIN_ONGOING_BATTLE);
				} else {
					optionSelected(null, OptionId.ENGAGE);
				}
			}
		} else {
//			if (config.showPullInText) {
//				optionSelected(null, OptionId.PRINT_ONGOING_BATTLE_INFO);
//			}
		}
	}
	
	public void printOngoingBattleInfo() {
		optionSelected(null, OptionId.PRINT_ONGOING_BATTLE_INFO);
	}
	
	
	protected List<CampaignFleetAPI> pulledIn = new ArrayList<CampaignFleetAPI>();
	protected void pullInNearbyFleets() {
		BattleAPI b = context.getBattle();
		if (!ongoingBattle) {
			b.join(Global.getSector().getPlayerFleet());
		}
		
		BattleSide playerSide = b.pickSide(Global.getSector().getPlayerFleet());
		
		boolean hostile = otherFleet.getAI() != null && otherFleet.getAI().isHostileTo(playerFleet);
		if (ongoingBattle) hostile = true;
		
		//canDecline = otherFleet.getAI() != null && other
		
//		boolean someJoined = false;
		CampaignFleetAPI actualPlayer = Global.getSector().getPlayerFleet();
		CampaignFleetAPI actualOther = (CampaignFleetAPI) (dialog.getInteractionTarget());
		
		//textPanel.addParagraph("Projecting nearby fleet movements:");
		//textPanel.addParagraph("You encounter a ");
		pulledIn.clear();
		
		if (config.pullInStations && !b.isStationInvolved()) {
			SectorEntityToken closestEntity = null;
			CampaignFleetAPI closest = null;
			Pair<SectorEntityToken, CampaignFleetAPI> p = Misc.getNearestStationInSupportRange(actualOther);
			if (p != null) {
				closestEntity = p.one;
				closest = p.two;
			}
			
			if (closest != null) {
				BattleSide joiningSide = b.pickSide(closest, true);
				boolean canJoin = joiningSide != BattleSide.NO_JOIN;
				if (!config.pullInAllies && joiningSide == playerSide) {
					canJoin = false;
				}
				if (!config.pullInEnemies && joiningSide != playerSide) {
					canJoin = false;
				}
				if (b == closest.getBattle()) {
					canJoin = false;
				}
				if (closest.getBattle() != null) {
					canJoin = false;
				}
				
				if (canJoin) {
					if (closestEntity != null) {
						closestEntity.getMarket().reapplyIndustries(); // need to pick up station CR value, in some cases
					}
					b.join(closest);
					pulledIn.add(closest);
					
					if (!config.straightToEngage && config.showPullInText) {
						if (b.getSide(playerSide) == b.getSideFor(closest)) {
							textPanel.addParagraph(
									Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting your forces.");//, FRIEND_COLOR);
						} else {
							if (hostile) {
								textPanel.addParagraph(Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting the enemy.");//, ENEMY_COLOR);
							} else {
								textPanel.addParagraph(Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting the opposing side.");
							}
						}
						textPanel.highlightFirstInLastPara(closest.getNameWithFactionKeepCase() + ":", closest.getFaction().getBaseUIColor());
					}
				}
			}
		}
		
		
		for (CampaignFleetAPI fleet : actualPlayer.getContainingLocation().getFleets()) {
			if (b == fleet.getBattle()) continue;
			if (fleet.getBattle() != null) continue;
			
			if (fleet.isStationMode()) continue;
			
			float dist = Misc.getDistance(actualOther.getLocation(), fleet.getLocation());
			dist -= actualOther.getRadius();
			dist -= fleet.getRadius();
//			if (dist < Misc.getBattleJoinRange()) {
//				System.out.println("Checking: " + fleet.getNameWithFaction());
//			}
			
			if (fleet.getFleetData().getNumMembers() <= 0) continue;
			
			float baseSensorRange = playerFleet.getBaseSensorRangeToDetect(fleet.getSensorProfile());
			boolean visible = fleet.isVisibleToPlayerFleet();
			VisibilityLevel level = fleet.getVisibilityLevelToPlayerFleet();
//			if (dist < Misc.getBattleJoinRange() && 
//					(dist < baseSensorRange || (visible && level != VisibilityLevel.SENSOR_CONTACT))) {
//				System.out.println("2380dfwef");
//			}
			float joinRange = Misc.getBattleJoinRange();
			if (fleet.getFaction().isPlayerFaction() && !fleet.isStationMode()) {
				joinRange += Global.getSettings().getFloat("battleJoinRangePlayerFactionBonus");
			}
			if (dist < joinRange && 
					(dist < baseSensorRange || (visible && level != VisibilityLevel.SENSOR_CONTACT)) && 
					((fleet.getAI() != null && fleet.getAI().wantsToJoin(b, true)) || fleet.isStationMode())) {
				
				BattleSide joiningSide = b.pickSide(fleet, true);
				if (!config.pullInAllies && joiningSide == playerSide) continue;
				if (!config.pullInEnemies && joiningSide != playerSide) continue;
				
				b.join(fleet);
				pulledIn.add(fleet);
				//if (b.isPlayerSide(b.getSideFor(fleet))) {
				if (!config.straightToEngage && config.showPullInText) {
					if (b.getSide(playerSide) == b.getSideFor(fleet)) {
						textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": supporting your forces.");//, FRIEND_COLOR);
					} else {
						if (hostile) {
							textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": joining the enemy.");//, ENEMY_COLOR);
						} else {
							textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": supporting the opposing side.");
						}
					}
					textPanel.highlightFirstInLastPara(fleet.getNameWithFactionKeepCase() + ":", fleet.getFaction().getBaseUIColor());
				}
//				someJoined = true;
			}
		}
		
		if (otherFleet != null) otherFleet.inflateIfNeeded();
		for (CampaignFleetAPI curr : pulledIn) {
			curr.inflateIfNeeded();
		}
		
//		if (!someJoined) {
//			addText("No nearby fleets will join the battle.");
//		}
		if (!ongoingBattle) {
			b.genCombined();
			b.takeSnapshots();
			playerFleet = b.getPlayerCombined();
			otherFleet = b.getNonPlayerCombined();
			if (!config.straightToEngage) {
				showFleetInfo();
			}
		}
		
	}
	
	
	protected EngagementResultAPI lastResult = null;
	public void backFromEngagement(EngagementResultAPI result) {

		// failsafe
		if (playerGoal == null && otherGoal == null) {
			EngagementResultForFleetAPI player = result.didPlayerWin() ? result.getWinnerResult() : result.getLoserResult();
			EngagementResultForFleetAPI other = result.didPlayerWin() ? result.getLoserResult() : result.getWinnerResult();
			if (player.getDeployed().isEmpty()) {
				playerGoal = FleetGoal.ATTACK;
				otherGoal = FleetGoal.ATTACK;				
			} else {
				playerGoal = FleetGoal.ATTACK;
				otherGoal = FleetGoal.ATTACK;
			}
			player.setGoal(playerGoal);
			other.setGoal(otherGoal);
		}
		
		if (!ongoingBattle) {
			if (!otherFleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS)) {
				otherFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true, 0);
			}
		}
		
		result.setBattle(context.getBattle());
		
		context.processEngagementResults(result);
		lastResult = result;
		
		boolean startedWithAllies = false;
		if (context.getBattle() != null) {
			startedWithAllies = context.getBattle().getPlayerSideSnapshot().size() > 1;
		}
		if (!Global.getSector().getPlayerFleet().isValidPlayerFleet() &&
				startedWithAllies && context.getBattle().getPlayerSide().size() > 1) {
				//!context.getBattle().getPlayerCombined().getFleetData().getMembersListCopy().isEmpty()) {
			showFleetInfo();
			addText(getString("battleFleetLost"));
			addText(getString("finalOutcomeNoShipsLeft"));
			options.clearOptions();
			options.addOption("Leave", OptionId.LEAVE, null);
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			return;
		}
		
		restoreOrigCaptains();
		if (origFlagship != null) {
			if (selectedFlagship != null) {
				PersonAPI captain = origFlagship.getCaptain();
				if (captain != null && !captain.isPlayer()) {
					selectedFlagship.setCaptain(captain);
				}
			}
			Global.getSector().getPlayerFleet().getFleetData().setFlagship(origFlagship);
//			origFlagship = null;
//			selectedFlagship = null;
			
		}
		
		if (context.getLastEngagementOutcome() == null) {
			return; // failsafe
		}

		boolean totalDefeat = !playerFleet.isValidPlayerFleet();
		boolean mutualDestruction = context.getLastEngagementOutcome() == EngagementOutcome.MUTUAL_DESTRUCTION;
		
		DataForEncounterSide playerSide = context.getDataFor(playerFleet);
		CrewCompositionAPI crewLosses = playerSide.getCrewLossesDuringLastEngagement();
		if ((int)crewLosses.getCrewInt() + (int)crewLosses.getMarines() > 0 && !totalDefeat && !mutualDestruction) {
			addText(getString("casualtyReport"));
			
			DataForEncounterSide data = context.getDataFor(playerFleet);
			int crewLost = (int) (data.getCrewLossesDuringLastEngagement().getCrewInt());
			int marinesLost = (int) (data.getCrewLossesDuringLastEngagement().getMarines());
			String crewLostStr = getApproximate(crewLost);
			if (crewLostStr.equals("no")) crewLostStr = "";
			if (crewLostStr.indexOf(" ") >= 0) {
				crewLostStr = crewLostStr.substring(crewLostStr.indexOf(" ") + 1);
			}
			String marinesLostStr = getApproximate(marinesLost);
			if (marinesLostStr.equals("no")) marinesLostStr = "";
			if (marinesLostStr.indexOf(" ") >= 0) {
				marinesLostStr = marinesLostStr.substring(marinesLostStr.indexOf(" ") + 1);
			}
			textPanel.highlightInLastPara(HIGHLIGHT_COLOR, crewLostStr, marinesLostStr);
		}
		
		boolean showFleetInfo = false;
		
		switch (context.getLastEngagementOutcome()) {
		case PURSUIT_PLAYER_OUT_FIRST_WIN:
			addText(getString("playerOutFirstPursuitWin"));
			showFleetInfo = true;
			break;
		case PURSUIT_PLAYER_OUT_FIRST_LOSS:
			addText(getString("playerOutFirstPursuitLoss"));
			showFleetInfo = true;
			break;
		case BATTLE_PLAYER_OUT_FIRST_WIN:
			addText(getString("playerOutFirstEngageWin"));
			showFleetInfo = true;
			break;
		case BATTLE_PLAYER_OUT_FIRST_LOSS:
			addText(getString("playerOutFirstEngageLoss"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_OUT_FIRST_WIN:
			addText(getString("playerOutFirstEscapeWin"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_OUT_FIRST_LOSS:
			addText(getString("playerOutFirstEscapeLoss"));
			showFleetInfo = true;
			break;
		case BATTLE_ENEMY_WIN:
			addText(getString("battleDefeat"));
			showFleetInfo = true;
			//enemyHasPostCombatOptions = true;
			break;
		case BATTLE_ENEMY_WIN_TOTAL:
			addText(getString("battleTotalDefeat"));
			showFleetInfo = true;
			break;
		case BATTLE_PLAYER_WIN:
			addText(getString("battleVictory"));
			showFleetInfo = true;
			break;
		case BATTLE_PLAYER_WIN_TOTAL:
			addText(getString("battleTotalVictory"));
			showFleetInfo = true;
			break;
		case ESCAPE_ENEMY_LOSS_TOTAL:
			addText(getString("pursuitTotalVictory"));
			showFleetInfo = true;
			break;
		case ESCAPE_ENEMY_SUCCESS:
			if (result.getLoserResult().getDisabled().isEmpty() && result.getLoserResult().getDestroyed().isEmpty()) {
				addText(getString("pursuitVictoryNoLosses"));
			} else {
				addText(getString("pursuitVictoryLosses"));
			}
			showFleetInfo = true;
			break;
		case ESCAPE_ENEMY_WIN:
			addText(getString("pursuitDefeat"));
			showFleetInfo = true;
			break;
		case ESCAPE_ENEMY_WIN_TOTAL:
			addText(getString("pursuitTotalDefeat"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_LOSS_TOTAL:
			addText(getString("escapeTotalDefeat"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_SUCCESS:
			addText(getString("escapeDefeat"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_WIN:
			addText(getString("escapeVictory"));
			showFleetInfo = true;
			break;
		case ESCAPE_PLAYER_WIN_TOTAL:
			addText(getString("escapeTotalVictory"));
			showFleetInfo = true;
			break;
		case MUTUAL_DESTRUCTION:
			addText(getString("engagementMutualDestruction"));
			// bit of a hack. this'll make it so that the player's ships have a chance to be repaired
			// in the event of mutual destruction by adding them to the enemy fleet side's "disabled enemy ships" list.
			// it'll work by using the existing vs-player boarding path
			if (mutualDestruction) {
				DataForEncounterSide otherData = context.getDataFor(otherFleet);
				for (FleetMemberAPI member : result.getLoserResult().getDisabled()) {
					otherData.addEnemy(member, Status.DISABLED);
				}
			}
		}
		
		EngagementOutcome last = context.getLastEngagementOutcome();
		if (last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS ||
				last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN) {
			float recoveryFraction = context.performPostEngagementRecoveryBoth(result);
			if (recoveryFraction > 0) {
				addText(getString("bothRecovery"));
			}
		} else {
			float recoveryFraction = context.performPostVictoryRecovery(result);
			if (recoveryFraction > 0) {
				if (context.didPlayerWinLastEngagement()) {
					addText(getString("playerRecovery"));
				} else {
					addText(getString("enemyRecovery"));
				}
			}
		}
		
		if (showFleetInfo) {
			//visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
			showFleetInfo();
		}
		
		addPostBattleAttitudeText();
		
		
		if (config.straightToEngage) {
			//optionSelected(null, OptionId.LEAVE);
			goToEncounterEndPath();
		} else {
			if (ongoingBattle) {
				options.clearOptions();
				updateEngagementChoice(true);
			} else {
				updateMainState(true);
			}
		}
		
		if (isFightingOver()) {
			if (context.isEngagedInHostilities()) {
				context.getDataFor(playerFleet).setDisengaged(!context.didPlayerWinEncounter());
				context.getDataFor(otherFleet).setDisengaged(context.didPlayerWinEncounter());
			}
		}
	}
	
	protected void addPostBattleAttitudeText() {
		if (!config.showFleetAttitude) return;
		
		if (!ongoingBattle) {
			if (!context.wasLastEngagementEscape()) {
				if (context.didPlayerWinLastEngagement()) {
					addText(getString("cleanDisengageOpportunity"), getString("highlightCleanDisengage"), Misc.getPositiveHighlightColor());
				} else if (didEnoughToDisengage(playerFleet)) {
					addText(getString("playerDisruptedEnemy"), getString("highlghtDisruptedEnemy"), Misc.getPositiveHighlightColor());
				}
			}
		}
		if (!isFightingOver()) {
			String side = "";
			if (context.getBattle() != null && context.getBattle().getNonPlayerSide().size() > 1) {
				side = "Side";
			}
			if (otherFleetWantsToFight()) {
				addText(getString("postBattleAggressive" + side));
			} else if (otherFleetWantsToDisengage()) {
				if (!otherCanDisengage()) {
					addText(getString("postBattleAggressive" + side));
				} else {
					addText(getString("postBattleDisengage" + side));
				}
			} else {
				if (otherFleetHoldingVsStrongerEnemy()) {
					addText(getString("postBattleHoldVsStrongerEnemy" + side));
				} else {
					addText(getString("postBattleNeutral" + side));
				}
			}
		}
	}
	
	public List<FleetMemberAPI> getPursuitCapablePlayerShips() {
		List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.isAlly()) continue;
			if (member.isCivilian()) continue;
			members.add(member);
		}
		return members;
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		// might not be a string if it's the dev-mode "escape to leave" option
		if (inConversation && optionData instanceof String) {
			conversationDelegate.optionSelected(text, optionData);
			if (!inConversation) {
				//optionSelected(null, OptionId.CUT_COMM);
				//optionSelected(null, OptionId.INIT);
			}
			return;
		}
		
		if (optionData == DumpMemory.OPTION_ID) {
			//new DumpMemory().execute(null, dialog, null, conversationDelegate.getMemoryMap());
			new DumpMemory().execute(null, dialog, null, getMemoryMap());
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		if (optionData instanceof String) {
			//??? failsafe
			optionSelected(null, OptionId.CUT_COMM);
			return;
		}
		
		OptionId option = (OptionId) optionData;
		
//		if (option == OptionId.OPEN_COMM) {
//			textPanel.clear();
//		}
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		switch (option) {
		case PRINT_ONGOING_BATTLE_INFO:
			if (ongoingBattle) {
				if (!config.straightToEngage) addText(getString("ongoingBattleEncounter"));
				BattleAPI b = context.getBattle();
				b.genCombined();
				
				BattleSide side = b.pickSide(playerFleet);
				BattleSide sideAssumingTransponderOn = b.pickSide(playerFleet, false);
				
				if (!config.straightToEngage) {
					if (side == sideAssumingTransponderOn && side == BattleSide.NO_JOIN) {
						addText(getString("ongoingBattleNoJoin"));
					} else if (side != sideAssumingTransponderOn && side == BattleSide.NO_JOIN) {
						addText(getString("ongoingBattleNoJoinTransponder"));
					} else {
						addText(getString("ongoingBattleShareIFF"));
					}
				}
			}
			break;
		case INIT:
			if (ongoingBattle) {
				if (!config.straightToEngage) addText(getString("ongoingBattleEncounter"));
				BattleAPI b = context.getBattle();
				b.genCombined();
				
				BattleSide side = b.pickSide(playerFleet);
				BattleSide sideAssumingTransponderOn = b.pickSide(playerFleet, false);
				
				if (!config.straightToEngage) {
					if (side == sideAssumingTransponderOn && side == BattleSide.NO_JOIN) {
						addText(getString("ongoingBattleNoJoin"));
					} else if (side != sideAssumingTransponderOn && side == BattleSide.NO_JOIN) {
						addText(getString("ongoingBattleNoJoinTransponder"));
					} else {
						addText(getString("ongoingBattleShareIFF"));
					}
				}
//				if (context.getBattle().canJoin(playerFleet)) {
//					BattleSide playerSide = b.pickSide(playerFleet);
//					CampaignFleetAPI prePlayerAllies = b.getCombined(playerSide);
//					CampaignFleetAPI enemies = b.getOtherSideCombined(playerSide);
//				}
			} else {
				boolean hostile = otherFleet.getAI() != null && otherFleet.getAI().isHostileTo(playerFleet);
				hostile |= context.isEngagedInHostilities();
				if (!skipAttitudeOnInit) {
					String side = "";
					if (context.getBattle() != null && context.getBattle().getNonPlayerSide().size() > 1) {
						side = "Side";
					}
					if (config.showFleetAttitude) {
						boolean hasStation = false;
						boolean allStation = true;
						for (CampaignFleetAPI curr : context.getBattle().getSideFor(otherFleet)) {
							allStation &= curr.isStationMode();
							hasStation |= curr.isStationMode();
						}
						if (otherFleetWantsToFight() && !canDisengage() && hasStation && !allStation) {
							addText(getString("initialWithStationVsLargeFleet"));
						} else if (otherFleetWantsToFight()) {
							addText(getString("initialAggressive" + side));
						} else if (otherFleetWantsToDisengage()) {
							if (!otherCanDisengage()) {
								if (hostile) {
									addText(getString("initialNeutral" + side));
								} else {
									addText(getString("initialNeutral" + side));
								}
							} else {
								if (hostile) {
									addText(getString("initialDisengage" + side));
								} else {
									addText(getString("initialCareful" + side));
								}
							}
						} else {
							if (otherFleetHoldingVsStrongerEnemy()) {
								addText(getString("initialHoldVsStrongerEnemy" + side));
							} else {
								addText(getString("initialNeutral" + side));
							}
						}
					}
				}
				if (!shownKnownStatus && config.showTransponderStatus) {
					shownKnownStatus = true;
					String side = "";
					if (context.getBattle() != null && context.getBattle().getNonPlayerSide().size() > 1) {
						side = "Side";
					}
					if (!otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SKIP_TRANSPONDER_STATUS_INFO)) {
						//boolean knows = otherFleet.knowsWhoPlayerIs();
						boolean knows = context.getBattle() != null && context.getBattle().getNonPlayerSide() != null &&
								context.getBattle().knowsWhoPlayerIs(context.getBattle().getNonPlayerSide());
						if (!knows) {
							addText(getString("initialDoesntKnow" + side));
						} else {
							CampaignFleetAPI actualPlayer = Global.getSector().getPlayerFleet();
							if (actualPlayer.isTransponderOn()) {
								addText(getString("initialKnows" + side));
							} else {
								addText(getString("initialKnowsTOff" + side));
							}
						}
					}
				}
				//textPanel.highlightFirstInLastPara("neutral posture", HIGHLIGHT_COLOR);
			}
			updateMainState(true);
			break;
		case REINIT_CONTINUE:
			reinitPostContinue();
			break;
		case INITIATE_BATTLE:
			{
//			BattleAPI b = context.getBattle();
//			b.join(Global.getSector().getPlayerFleet());
//			
//			boolean someJoined = false;
//			CampaignFleetAPI actualPlayer = Global.getSector().getPlayerFleet();
//			for (CampaignFleetAPI fleet : actualPlayer.getContainingLocation().getFleets()) {
//				if (b == fleet.getBattle()) continue;
//				
//				float dist = Misc.getDistance(actualPlayer.getLocation(), fleet.getLocation());
//				dist -= actualPlayer.getRadius();
//				dist -= fleet.getRadius();
//				if (dist < 200 && fleet.getAI() != null && fleet.getAI().wantsToJoin(b)) {
//					b.join(fleet);
//					addText(Misc.ucFirst(fleet.getNameWithFaction()) + " will join the battle.");
//					textPanel.highlightFirstInLastPara(fleet.getNameWithFaction(), fleet.getFaction().getBaseUIColor());
//					someJoined = true;
//				}
//			}
//			if (!someJoined) {
//				addText("No nearby fleets will join the battle.");
//			}
//			
//			b.genCombined();
//			
//			showFleetInfo();
//			
//			playerFleet = b.getPlayerCombined();
//			otherFleet = b.getNonPlayerCombined();
			
			//updateEngagementChoice(true);
			updateMainState(true);
			}
			break;
		case JOIN_ONGOING_BATTLE:
			if (context.getBattle().canJoin(playerFleet)) {
				BattleAPI b = context.getBattle();
				for (CampaignFleetAPI fleet : b.getBothSides()) {
					fleet.inflateIfNeeded();
				}
				b.genCombined();
				
				BattleSide playerSide = b.pickSide(playerFleet);
				CampaignFleetAPI prePlayerAllies = b.getCombined(playerSide);
				CampaignFleetAPI enemies = b.getOtherSideCombined(playerSide);
				
				boolean alliedWantsToFightBefore = fleetWantsToFight(prePlayerAllies, enemies);
				boolean alliedWantsToDisengageBefore = fleetWantsToDisengage(prePlayerAllies, enemies) && fleetCanDisengage(prePlayerAllies);
				boolean alliedHoldingBefore = fleetHoldingVsStrongerEnemy(prePlayerAllies, enemies);
				boolean otherWantsToFightBefore = fleetWantsToFight(enemies, prePlayerAllies);
				boolean otherWantsToDisengageBefore = fleetWantsToDisengage(enemies, prePlayerAllies) && fleetCanDisengage(enemies);
				boolean otherHoldingBefore = fleetHoldingVsStrongerEnemy(enemies, prePlayerAllies);
				
				//System.out.println("Ships before: " + prePlayerAllies.getFleetData().getMembersListCopy().size());
				
				b.join(playerFleet);
				b.genCombined();
				
				showFleetInfo();
				joinedBattle = true;
				
				playerFleet = b.getPlayerCombined();
				otherFleet = b.getNonPlayerCombined();
				
				//System.out.println("Ships after: " + playerFleet.getFleetData().getMembersListCopy().size());
				boolean alliedWantsToFight = alliedFleetWantsToFight();
				boolean alliedWantsToDisengage = alliedFleetWantsToDisengage() && alliedCanDisengage();
				boolean alliedHolding = alliedFleetHoldingVsStrongerEnemy();
				
				boolean otherWantsToFight = otherFleetWantsToFight();
				boolean otherWantsToDisengage = otherFleetWantsToDisengage() && otherCanDisengage();
				boolean otherHolding = otherFleetHoldingVsStrongerEnemy();
				
				b.takeSnapshots();
				
				options.clearOptions();
				updateEngagementChoice(true);
				if (!allyEngagementChoiceNoBattle) {
					updatePreCombat();
				}
			} else {
				addText("Failed to join battle; shouldn't happen.");
				updateMainState(true);
			}
			break;
		case CONTINUE_ONGOING_BATTLE:
			updatePreCombat();
			break;
		case ENGAGE:
			//visual.showImagePortion("illustrations", "hound_hangar", 350, 75, 800, 800, 0, 0, 400, 400);
			if (otherFleetWantsToDisengage() && otherCanDisengage()) {
				playerGoal = FleetGoal.ATTACK;
				otherGoal = FleetGoal.ESCAPE;
				if (config.showEngageText) {
					addText(getString("engagePursuit"));
				}
			} else {
				playerGoal = FleetGoal.ATTACK;
				otherGoal = FleetGoal.ATTACK;
				if (config.showEngageText) {
					addText(getString("engageMutual"));
				}
			}
			updatePreCombat();
			break;
		case CONTINUE_INTO_BATTLE:
//			if (context.getBattle() == null) {
//				if (otherFleet.getBattle() != null) {
//					context.setBattle(otherFleet.getBattle());
//				} else {
//					BattleAPI battle = Global.getFactory().createBattle(playerFleet, otherFleet);
//					context.setBattle(battle);
//				}
//			}
				
			BattleCreationContext bcc;
			if (config.alwaysAttackVsAttack){
				playerGoal = FleetGoal.ATTACK;
				otherGoal = FleetGoal.ATTACK;
			}
			
			if (context.getBattle() != null) {
				BattleAPI b = context.getBattle();
				
				if (b.isStationInvolved()) {
					boolean regen = false;
					if (b.isStationInvolvedOnPlayerSide()) {
						if (otherGoal == FleetGoal.ESCAPE) {
							regen = true;
						}
					} else {
						if (playerGoal == FleetGoal.ESCAPE) {
							regen = true;
						}
					}
					
					if (regen) {
						b.genCombined(false);
					}
				}
				
				CampaignFleetAPI combinedPlayer = b.getPlayerCombined();
				CampaignFleetAPI combinedEnemy = b.getNonPlayerCombined();
				
//				playerGoal = null;
//				otherGoal = null;
				
				bcc = new BattleCreationContext(combinedPlayer, playerGoal, combinedEnemy, otherGoal);
				bcc.setPlayerCommandPoints((int) Global.getSector().getPlayerFleet().getCommanderStats().getCommandPoints().getModifiedValue());
				
				if (b.isStationInvolved() && playerGoal != FleetGoal.ESCAPE && otherGoal != FleetGoal.ESCAPE) {
					bcc.objectivesAllowed = false;
				}
				if (config.delegate != null) {
					config.delegate.battleContextCreated(dialog, bcc);
				}
				
				if (firstEngagement) {
					if (playerGoal != FleetGoal.ESCAPE && ongoingBattle) {
						bcc.setInitialStepSize(1.5f);
						bcc.setInitialNumSteps(10 + (float) Math.random() * 30);
					}
					firstEngagement = false;
				} else {
					if (playerGoal != FleetGoal.ESCAPE && ongoingBattle) {
						bcc.setInitialStepSize(1.5f);
						bcc.setInitialNumSteps(5 + (float) Math.random() * 5);
					}
				}
			} else {
				bcc = new BattleCreationContext(playerFleet, playerGoal, otherFleet, otherGoal);
				bcc.setPlayerCommandPoints((int) Global.getSector().getPlayerFleet().getCommanderStats().getCommandPoints().getModifiedValue());
				if (config.delegate != null) {
					config.delegate.battleContextCreated(dialog, bcc);
				}
			}
			
			if (playerGoal == FleetGoal.ESCAPE) {
				//DataForEncounterSide data = context.getDataFor(otherFleet);
				CampaignFleetAIAPI ai = playerFleet.getAI();
				if (ai != null) {
					ai.performCrashMothballingPriorToEscape(context, otherFleet);
				}
			} else if (otherGoal == FleetGoal.ESCAPE) {
				//DataForEncounterSide data = context.getDataFor(playerFleet);
				CampaignFleetAIAPI ai = otherFleet.getAI();
				if (ai != null) {
					ai.performCrashMothballingPriorToEscape(context, playerFleet);
				}
			}
			
			visual.fadeVisualOut();
			dialog.startBattle(bcc);
			break;
		case DISENGAGE:
//			CampaignFleetAIAPI ai = otherFleet.getAI();
//			PursuitOption po = otherFleet.getAI().pickPursuitOption(context, playerFleet);
			PursuitOption po = pickPursuitOption(otherFleet, playerFleet, context);
			if (otherFleetHoldingVsStrongerEnemy() || !otherFleetWantsToFight() || canDisengageCleanly(playerFleet)) {
				po = PursuitOption.LET_THEM_GO;
			}
			
			context.applyPursuitOption(otherFleet, playerFleet, po);
			context.getDataFor(playerFleet).setDisengaged(true);
			context.getDataFor(otherFleet).setDisengaged(false);
			switch (po) {
			case PURSUE:
				// shouldn't happen here, or we'd be in ATTEMPT_TO_DISENGAGE
			case HARRY:
				context.applyPursuitOption(otherFleet, playerFleet, PursuitOption.HARRY);
				addText(getString("enemyHarass"));
				context.setEngagedInHostilities(true); // this was commented out, why?
				context.setOtherFleetHarriedPlayer(true);
				context.getDataFor(playerFleet).setDisengaged(true);
				context.getDataFor(otherFleet).setDisengaged(false);
				break;
			case LET_THEM_GO:
				if (canDisengageCleanly(playerFleet)) {
					context.setEngagedInHostilities(true); // so that other fleets stand down and don't insta-pursue
					addText(getString("enemyUnableToPursue"));
				} else {
					addText(getString("enemyDecidesNotToPursue"));
				}
				break;
			}
			updateMainState(true);
			break;
		case ATTEMPT_TO_DISENGAGE:
			boolean letGo = true;
			if (otherFleetWantsToFight()) {
				//PursuitOption pursuitOption = otherFleet.getAI().pickPursuitOption(context, playerFleet);
				PursuitOption pursuitOption = pickPursuitOption(otherFleet, playerFleet, context);
				if (pursuitOption == PursuitOption.PURSUE) {
					playerGoal = FleetGoal.ESCAPE;
					otherGoal = FleetGoal.ATTACK;
					addText(getString("enemyPursuit"));
					letGo = false;
					updatePreCombat();
				} else if (pursuitOption == PursuitOption.HARRY) {
					context.applyPursuitOption(otherFleet, playerFleet, PursuitOption.HARRY);
					addText(getString("enemyHarass"));
					context.setEngagedInHostilities(true);
					//context.getDataFor(playerFleet).setDisengaged(!context.isEngagedInHostilities());
					context.getDataFor(playerFleet).setDisengaged(true);
					context.getDataFor(otherFleet).setDisengaged(false);
					updateMainState(true);
					letGo = false;
				} else {
					letGo = true;
				}
			}
			if (letGo) {
				//PursueAvailability pa = context.getPursuitAvailability(otherFleet, playerFleet);
				PursueAvailability pa = getPursuitAvailability(otherFleet);
				DisengageHarryAvailability dha = context.getDisengageHarryAvailability(otherFleet, playerFleet);
				if (dha == DisengageHarryAvailability.AVAILABLE || pa == PursueAvailability.AVAILABLE) {
					addText(getString("enemyDecidesNotToPursue"));
				} else {
					addText(getString("enemyUnableToPursue"));
				}
				context.getDataFor(playerFleet).setDisengaged(true);
				context.getDataFor(otherFleet).setDisengaged(!context.isEngagedInHostilities());
				updateMainState(true);
			}
			
//			String name = "Corvus III";
//			SectorEntityToken planet = Global.getSector().getStarSystem("Corvus").getEntityByName(name);
//			//planet = Global.getSector().getStarSystem("Corvus").getStar();
//			if (planet != null) {
//				addText("Incoming visual feed from " + name + ".");
//				visual.showPlanetInfo(planet);
//			} else {
//				addText("Planet " + name + " not found in the Corvus system.");
//			}
//			dialog.showTextPanel();
			//dialog.hideTextPanel();
			//dialog.setXOffset(-200);
			break;
		case OPEN_COMM:
			CampaignFleetAPI actualOther = (CampaignFleetAPI) (dialog.getInteractionTarget());
			dialog.showTextPanel();
			dialog.flickerStatic(0.1f, 0.1f);
			
			inConversation = true;
			conversationDelegate = new RuleBasedInteractionDialogPluginImpl();
			conversationDelegate.setEmbeddedMode(true);
			conversationDelegate.init(dialog);
			
			dialog.getInteractionTarget().setActivePerson(actualOther.getCommander());
			conversationDelegate.notifyActivePersonChanged();
			
			boolean otherWantsToRun = otherFleetWantsToDisengage() && otherCanDisengage();
			MemoryAPI mem = conversationDelegate.getMemoryMap().get(MemKeys.LOCAL);
			if (otherWantsToRun) {
				mem.unset("$weakerThanPlayerButHolding");
			}
			
			if (!conversationDelegate.fireBest("OpenCommLink")) {
				addText("You try to establish a comm link, but only get static.");
				dialog.getInteractionTarget().setActivePerson(null);
				conversationDelegate.notifyActivePersonChanged();
				inConversation = false;
			}
			if (inConversation) {
				visual.showPersonInfo(actualOther.getCommander());
			}
			break;
		case CUT_COMM:
			dialog.showTextPanel();
			dialog.flickerStatic(0.1f, 0.1f);
			
//			addText(getString("cutComm"));
//			visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
//			updateMainState();
			
			inConversation = false;
//			addText(getString("cutComm"));
			//visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
			showFleetInfo();
			optionSelected(null, OptionId.INIT);
			
			break;
		case PURSUE:
			playerGoal = FleetGoal.ATTACK;
			otherGoal = FleetGoal.ESCAPE;
			addText(getString("pursue"));
			updatePreCombat();
			break;
		case AUTORESOLVE_PURSUE:
			List<FleetMemberAPI> members = getPursuitCapablePlayerShips();
//			List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
//			for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
//				if (member.isAlly()) continue;
//				if (member.isCivilian()) continue;
//				members.add(member);
//			}
			dialog.showFleetMemberPickerDialog("Select craft to send in pursuit", "Ok", "Cancel", 
					3, 7, 58f, false, true, members,
			new FleetMemberPickerListener() {
				public void pickedFleetMembers(List<FleetMemberAPI> members) {
					if (members != null && !members.isEmpty()) {
						BattleAutoresolverPluginImpl resolver = new BattleAutoresolverPluginImpl(context.getBattle());
						resolver.resolvePlayerPursuit(context, members);
						if (resolver.getResult() != null) {
							addText(getString("pursuitAutoresolve"));
							if (context.getBattle() != null) {
								CampaignFleetAPI player = Global.getSector().getPlayerFleet();
								CampaignFleetAPI ally = null;
								float alliedFP = 0;
								for (CampaignFleetAPI curr : context.getBattle().getPlayerSide()) {
									if (!curr.isPlayerFleet() && !curr.getFleetData().getMembersListCopy().isEmpty()) {
										if (ally == null) ally = curr;
										alliedFP += ally.getFleetPoints();
									}
								}
								float playerFP = 0f;
								for (FleetMemberAPI member : members) {
									playerFP += member.getFleetPointCost();
								}
								float damage = 0f;
								for (FleetMemberAPI member : resolver.getResult().getLoserResult().getDisabled()) {
									damage += member.getFleetPointCost();
								}
								for (FleetMemberAPI member : resolver.getResult().getLoserResult().getDestroyed()) {
									damage += member.getFleetPointCost();
								}
								float total = playerFP + alliedFP;
								if (total < 1) total = 1;
								context.setPlayerFPHullDamageToEnemies(context.getPlayerFPHullDamageToEnemies() + damage * playerFP / total);
								if (ally != null && alliedFP > 0) {
									context.setAllyFPHullDamageToEnemies(context.getAllyFPHullDamageToEnemies() + damage * alliedFP / total);
								}
							}
							backFromEngagement(resolver.getResult());
						}
					}
				}
				public void cancelledFleetMemberPicking() {
					
				}
			});
			break;
		case CRASH_MOTHBALL:
			List<FleetMemberAPI> choices = getCrashMothballable(playerFleet.getFleetData().getCombatReadyMembersListCopy());
			dialog.showFleetMemberPickerDialog("Select craft to crash-mothball", "Ok", "Cancel", 
					3, 7, 58f, false, true, choices,
			new FleetMemberPickerListener() {
				public void pickedFleetMembers(List<FleetMemberAPI> members) {
					for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
						member.getRepairTracker().setCrashMothballed(false);
					}
					if (members != null && !members.isEmpty()) {
						for (FleetMemberAPI member : members) {
							member.getRepairTracker().setCrashMothballed(true);
						}
						
						crashMothballList = createShipNameListString(members);
						if (members.size() == 1) {
							addText(getString("crashMothballSelectedOneShip"));
						} else {
							addText(getString("crashMothballSelectedMultiple"));
						}
					}
				}
				public void cancelledFleetMemberPicking() {
					
				}
			});
			break;
		case SCUTTLE:
			break;
		case GO_TO_PRE_BATTLE:
			updateEngagementChoice(false);
			break;
		case GO_TO_MAIN:
			if (config.straightToEngage) {
				optionSelected(null, OptionId.LEAVE);
				break;
			}
			List<CampaignFleetAPI> playerSide = context.getBattle().getPlayerSide();
			List<CampaignFleetAPI> otherSide = context.getBattle().getNonPlayerSide();
			//context.getBattle().leave(playerFleet);
			if (joinedBattle) {
				//context.getBattle().leave(otherFleet);
				joinedBattle = false;
			}
			if (ongoingBattle) {
				playerFleet = Global.getSector().getPlayerFleet();
				otherFleet = (CampaignFleetAPI) (dialog.getInteractionTarget());
				context.getBattle().leave(playerFleet, context.isEngagedInHostilities() || context.isOtherFleetHarriedPlayer());
				BattleAPI b = context.getBattle();
				String titleOne = b.getPrimary(b.getSideOne()).getNameWithFactionKeepCase();
				if (b.getSideOne().size() > 1) titleOne += ", with allies";
				String titleTwo = b.getPrimary(b.getSideTwo()).getNameWithFactionKeepCase();
				if (b.getSideTwo().size() > 1) titleTwo += ", with allies";
				visual.showPreBattleJoinInfo(null, playerFleet, Misc.ucFirst(titleOne), Misc.ucFirst(titleTwo), context);
			} else {
//				context.getBattle().uncombine();
//				if (playerSide != null) {
//					for (CampaignFleetAPI curr : new ArrayList<CampaignFleetAPI>(playerSide)) {
//						if (curr != playerFleet) {
//							context.getBattle().leave(curr);
//						}
//					}
//				}
//				if (otherSide != null) {
//					for (CampaignFleetAPI curr : new ArrayList<CampaignFleetAPI>(otherSide)) {
//						if (curr != otherFleet) {
//							context.getBattle().leave(curr);
//						}
//					}
//				}
//				showFleetInfo();
			}
			updateMainState(false);
			break;
		case CONTINUE:
			visual.showCustomPanel(810, 400, new ExampleCustomUIPanel());
			dialog.hideTextPanel();
			break;
		case DEV_MODE_ESCAPE:
			context.applyAfterBattleEffectsIfThereWasABattle();
			
			BattleAPI b = context.getBattle();
			if (b.isPlayerInvolved()) {
				cleanUpBattle();
			}
		case LEAVE:
		case CONTINUE_LEAVE:
			if (option != OptionId.CONTINUE_LEAVE) {
				if (context.adjustPlayerReputation(dialog, getString("friendlyFireRepLoss"),
												   config.impactsAllyReputation, config.impactsEnemyReputation)) {
					options.clearOptions();
					options.addOption("Continue", OptionId.CONTINUE_LEAVE, null);
					if (!config.straightToEngage) {
						options.setShortcut(OptionId.CONTINUE_LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
					}
					break;
				}
			}
			if (isFightingOver()) {
				if (!context.hasWinnerAndLoser()) {
					if (context.getDataFor(playerFleet).isWonLastEngagement()) {
						context.getDataFor(playerFleet).setDisengaged(false);
						context.getDataFor(otherFleet).setDisengaged(true);
					} else {
						context.getDataFor(playerFleet).setDisengaged(true);
						context.getDataFor(otherFleet).setDisengaged(false);
					}
				}
			} else {
				if (context.isEngagedInHostilities()) {
					context.getDataFor(playerFleet).setDisengaged(true);
					context.getDataFor(otherFleet).setDisengaged(false);
				} else {
					context.getDataFor(playerFleet).setDisengaged(true);
					context.getDataFor(otherFleet).setDisengaged(true);
				}
			}
			
			if (config.printXPToDialog) {
				context.setTextPanelForXPGain(textPanel);
				textPanel.setFontSmallInsignia();
			}
			context.applyAfterBattleEffectsIfThereWasABattle();
			context.setTextPanelForXPGain(null);
			textPanel.setFontInsignia();
			
//			if (config.dismissOnLeave) {
//				Global.getSector().getCampaignUI().addMessage("Game paused");
//			}
			
			cleanUpBattle();
//			context.getBattle().leave(Global.getSector().getPlayerFleet());
//			if (!ongoingBattle) {
//				context.getBattle().finish();
//			}
			
			if (config.dismissOnLeave) {
				dialog.dismiss();
			} else {
				//options.clearOptions();
				dialog.setOptionOnEscape("", null);
				dialog.setOptionOnConfirm("", null);
			}
			if (config.delegate != null) {
				config.delegate.notifyLeave(dialog);
			}
			break;
		case HARRY_PURSUE:
			addText(getString("playerHarass"));
			context.applyPursuitOption(playerFleet, otherFleet, PursuitOption.HARRY);
			context.setEngagedInHostilities(true);
			context.getDataFor(playerFleet).setDisengaged(false);
			context.getDataFor(otherFleet).setDisengaged(true);
			context.setEngagedInHostilities(true);
			goToEncounterEndPath();
			break;			
		case LET_THEM_GO:
			addText(getString("playerLetGo"));
			//context.getDataFor(playerFleet).setDisengaged(!context.isEngagedInHostilities());
			context.getDataFor(playerFleet).setDisengaged(false);
			context.getDataFor(otherFleet).setDisengaged(true);
			goToEncounterEndPath();
			break;
		case RECOVERY_CONTINUE:
			goToEncounterEndPath();
			break;
		case RECOVERY_SELECT:
			if (!recoverableShips.isEmpty()) {
				dialog.showFleetMemberRecoveryDialog("Select ships to recover", recoverableShips,
				new FleetMemberPickerListener() {
					public void pickedFleetMembers(List<FleetMemberAPI> members) {
						if (members != null && !members.isEmpty()) {
							recoveredShips.clear();
							recoveredShips.addAll(members);
							FleetEncounterContext.recoverShips(members, context, playerFleet, otherFleet);
							showFleetInfo();
							winningPath();
						}
					}
					public void cancelledFleetMemberPicking() {
					}
				});
			}
			break;
		case CONTINUE_LOOT:
			visual.setVisualFade(0, 0);
			dialog.hideTextPanel();
			
			Global.getSector().reportEncounterLootGenerated(context, context.getLoot());
			
			visual.showLoot("Salvaged", context.getLoot(), true, new CoreInteractionListener() {
				public void coreUIDismissed() {
					if (config.printXPToDialog) {
						context.setTextPanelForXPGain(textPanel);
						textPanel.setFontSmallInsignia();
					}
					context.applyAfterBattleEffectsIfThereWasABattle();
					context.setTextPanelForXPGain(null);
					textPanel.setFontInsignia();
//					context.getBattle().uncombine();
//					context.getBattle().leave(Global.getSector().getPlayerFleet());
					cleanUpBattle();
					
					if (config.dismissOnLeave) {
						dialog.dismiss();
						dialog.hideTextPanel();
					} else {
						dialog.showTextPanel();
						//options.clearOptions();
						dialog.setOptionOnEscape("", null);
						dialog.setOptionOnConfirm("", null);
					}
					if (config.delegate != null) {
						config.delegate.notifyLeave(dialog);
					}
				}
			});
			options.clearOptions();
			dialog.setPromptText("");
			//options.addOption("Leave", OptionId.LEAVE, null);
			break;
		case CONTINUE_INTO_BOARDING:
			goToEncounterEndPath();
			break;
		case BOARDING_ACTION:
			boardingPhase++;
			CampaignFleetAPI sourceFleet = context.getBattle().getSourceFleet(toBoard);
			boardingResult = context.boardShip(toBoard, Global.getSector().getPlayerFleet(), sourceFleet);
			//boardingResult = context.boardShip(toBoard, Global.getSector().getPlayerFleet(), otherFleet);
			goToEncounterEndPath();
			break;
		case SELECT_FLAGSHIP:
			members = new ArrayList<FleetMemberAPI>();
			for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
				if (member.isFighterWing()) continue;
				if (member.isAlly()) continue;
				members.add(member);
			}
			if (!members.isEmpty()) {
				dialog.showFleetMemberPickerDialog("Select flagship for this engagement", "Ok", "Cancel", 
						3, 7, 58f, false, false, members,
				new FleetMemberPickerListener() {
					public void pickedFleetMembers(List<FleetMemberAPI> members) {
						if (members != null && !members.isEmpty()) {
//							if (origFlagship == null) {
//								origFlagship = Global.getSector().getPlayerFleet().getFlagship();
//								if (origCaptains.isEmpty()) {
//									//origCaptains.clear();
//									for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
//										origCaptains.put(member, member.getCaptain());
//									}
//								}
//							}
							restoreOrigCaptains();
							
							selectedFlagship = members.get(0);
							PersonAPI captain = selectedFlagship.getCaptain();
							Global.getSector().getPlayerFleet().getFleetData().setFlagship(selectedFlagship);
							if (origFlagship != null && captain != null && !captain.isPlayer()) {
								origFlagship.setCaptain(captain);
							}
							addText(getString("selectedFlagship"));
						}
					}
					public void cancelledFleetMemberPicking() {
						
					}
				});
			}
			break;
		case ENGAGE_BOARDABLE:
			EngageBoardableOutcome outcome = context.engageBoardableShip(toBoard, otherFleet, playerFleet);
			switch (outcome) {
			case DESTROYED:
				addText(getString("engageBoardableDestroyed"));
				break;
			case DISABLED:
				addText(getString("engageBoardableDisabled"));
				break;
			case ESCAPED:
				addText(getString("engageBoardableEscaped"));
				break;
			}
			toBoard = null;
			goToEncounterEndPath();
			break;
		case LET_IT_GO:
			context.letBoardableGo(toBoard, otherFleet, playerFleet);
			addText(getString("letBoardableGo"));
			toBoard = null;
			goToEncounterEndPath();
			break;
//		case ABORT_BOARDING_ACTION:
//			context.letBoardableGo(toBoard, otherFleet, playerFleet);
//			addText(getString("letBoardableGo"));
//			toBoard = null;
//			goToEncounterEndPath();
//			break;
//		case HARD_DOCK:
//			initBoardingParty();
//			if (boardingParty != null) {
//				boardingAttackType = BoardingAttackType.SHIP_TO_SHIP;
//				boardingResult = context.boardShip(toBoard, boardingParty, boardingAttackType, boardingTaskForce, Global.getSector().getPlayerFleet(), otherFleet);
//				goToEncounterEndPath();
//			}
//			break;
//		case LAUNCH_ASSAULT_TEAMS:
//			initBoardingParty();
//			if (boardingParty != null) {
//				boardingAttackType = BoardingAttackType.LAUNCH_FROM_DISTANCE;
//				boardingResult = context.boardShip(toBoard, boardingParty, boardingAttackType, boardingTaskForce, Global.getSector().getPlayerFleet(), otherFleet);
//				goToEncounterEndPath();
//			}
//			break;
		}
	}

	protected void rememberWasBeaten() {
		if (context.getBattle() == null) return;
		
		for (CampaignFleetAPI other : context.getBattle().getNonPlayerSide()) {
			MemoryAPI mem = other.getMemoryWithoutUpdate();
			if (!mem.getBoolean(MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER)) {
				mem.set(MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER, true, 0.5f);
			}
		}
	}
	
	protected void restoreOrigCaptains() {
		if (origCaptains != null) {
			for (FleetMemberAPI member : origCaptains.keySet()) {
				PersonAPI captain = origCaptains.get(member);
				if (captain != null) {
					member.setCaptain(captain);
				}
			}
		}
	}

	protected boolean cleanedUp = false;
	public void cleanUpBattle() {
		if (cleanedUp) return;
		cleanedUp = true;
		
		BattleAPI b = context.getBattle();
		
		DataForEncounterSide enemyData = context.getDataFor(b.getNonPlayerCombined());
		DataForEncounterSide playerData = context.getDataFor(b.getPlayerCombined());
		if (enemyData != null && playerData != null && enemyData.disengaged() && !playerData.disengaged()) {
			rememberWasBeaten();
		}
		
		
		b.leave(Global.getSector().getPlayerFleet(), context.isEngagedInHostilities() || context.isOtherFleetHarriedPlayer());
		
		BattleSide playerSide = b.getPlayerSide() == b.getSideOne() ? BattleSide.ONE : BattleSide.TWO;
		BattleSide otherSide = b.getPlayerSide() == b.getSideOne() ? BattleSide.TWO : BattleSide.ONE;
		
		BattleSide winner = context.didPlayerWinEncounter() ? playerSide : otherSide;
		if (!context.isEngagedInHostilities() && !context.isOtherFleetHarriedPlayer()) winner = BattleSide.NO_JOIN;
		
		if (!ongoingBattle) {
			b.finish(winner, context.isEngagedInHostilities() || context.isOtherFleetHarriedPlayer());
			Global.getSector().getPlayerFleet().getFleetData().setSyncNeeded();
			Global.getSector().getPlayerFleet().getFleetData().syncIfNeeded();
		} else if (ongoingBattle) {
			EngagementOutcome last = context.getLastEngagementOutcome();
			if (last == EngagementOutcome.ESCAPE_ENEMY_SUCCESS || last == EngagementOutcome.ESCAPE_PLAYER_SUCCESS || harryEndedBattle || context.isOtherFleetHarriedPlayer() || allyEngagementChoiceNoBattle) {
				b.finish(winner, true);
			} else {
				for (CampaignFleetAPI curr : pulledIn) {
					b.leave(curr, context.isEngagedInHostilities() || context.isOtherFleetHarriedPlayer());
				}
			}
		}
		if (context.isEngagedInHostilities()) {
			b.applyVisibilityMod(Global.getSector().getPlayerFleet());
		}
		
	}
	
	
	protected boolean okToLeave = false;
	protected boolean didRepairs = false;
	protected boolean didBoardingCheck = false;
	protected boolean didRecoveryCheck = false;
	protected boolean pickedMemberToBoard = false;
	protected FleetMemberAPI toBoard = null;
	protected String repairedShipList = null;
	//protected String boardingTaskForceList = null;
	//protected List<FleetMemberAPI> boardingTaskForce = null;
	protected int boardingPhase = 0;
	protected float boardingPercentSuccess = 0;
	
	protected String crashMothballList = null;
	protected CrewCompositionAPI maxBoardingParty = null;
	protected CrewCompositionAPI boardingParty = null;
	//protected BoardingAttackType boardingAttackType = null;
	protected BoardingResult boardingResult = null;
	protected FleetMemberAPI selectedFlagship = null;
	protected FleetMemberAPI origFlagship = null;
	protected Map<FleetMemberAPI, PersonAPI> origCaptains = new HashMap<FleetMemberAPI, PersonAPI>();
	
	protected InitialBoardingResponse aiBoardingResponse = null;
	
	protected boolean shownKnownStatus = false;
	
	protected void goToEncounterEndPath() {
		if (context.didPlayerWinEncounter() ||
				(config.straightToEngage && 
						context.getLastEngagementOutcome() == EngagementOutcome.BATTLE_PLAYER_WIN)) {
			winningPath();
		} else {
			losingPath();
		}
	}
	
	protected void losingPath() {
		options.clearOptions();
		
		context.getDataFor(playerFleet).setDisengaged(true);
		
		if (!recoveredCrew) {
			recoveredCrew = true;
			context.recoverCrew(otherFleet);
		}
		
		boolean playerHasReadyShips = !playerFleet.getFleetData().getCombatReadyMembersListCopy().isEmpty();
		boolean otherHasReadyShips = !otherFleet.getFleetData().getCombatReadyMembersListCopy().isEmpty();
		boolean totalDefeat = !playerFleet.isValidPlayerFleet();
		boolean mutualDestruction = context.getLastEngagementOutcome() == EngagementOutcome.MUTUAL_DESTRUCTION;
//		if (!didBoardingCheck) {
//			didBoardingCheck = true;
//			toBoard = context.pickShipToBoard(otherFleet, playerFleet);
//			if (toBoard != null) {
//				pickedMemberToBoard = true;
//				options.addOption("Continue", OptionId.CONTINUE_INTO_BOARDING, null);
//				return;
//			}
//		}
		
		if (toBoard != null && aiBoardingResponse == null) {
			visual.showFleetMemberInfo(toBoard);
			
			if (mutualDestruction) {
				addText(getString("mutualDestructionRepairs"));
				aiBoardingResponse = InitialBoardingResponse.LET_IT_GO;
			} else {
				if (totalDefeat) {
					addText(getString("lastFriendlyShipRepairs"));
				} else {
					addText(getString("friendlyShipBoardable"));
				}
				aiBoardingResponse = otherFleet.getAI().pickBoardingResponse(context, toBoard, playerFleet);
			}
			
			if (!otherHasReadyShips) {
				aiBoardingResponse = InitialBoardingResponse.LET_IT_GO;
			}
			
			options.addOption("Continue", OptionId.CONTINUE_INTO_BOARDING, null);
			return;
		}
		
		if (toBoard != null && aiBoardingResponse != null) {
			switch (aiBoardingResponse) {
			case BOARD:
				break;
			case ENGAGE:
				EngageBoardableOutcome outcome = context.engageBoardableShip(toBoard, playerFleet, otherFleet);
				switch (outcome) {
				case DESTROYED:
					if (totalDefeat) {
						addText(getString("lastFriendlyBoardableDestroyed"));
					} else {
						addText(getString("engageFriendlyBoardableDestroyed"));
					}
					break;
				case DISABLED:
					if (totalDefeat) {
						addText(getString("lastFriendlyBoardableDisabled"));
					} else {
						addText(getString("engageFriendlyBoardableDisabled"));
					}
					break;
				case ESCAPED:
					if (totalDefeat) {
						addText(getString("lastFriendlyBoardableEscaped"));
					} else {
						addText(getString("engageFriendlyBoardableEscaped"));
					}
					break;
				}
				break;
			case LET_IT_GO:
				context.letBoardableGo(toBoard, playerFleet, otherFleet);
				if (!mutualDestruction) {
					if (totalDefeat) {
						addText(getString("engageFriendlyBoardableLetGo"));
					} else {
						addText(getString("lastFriendlyBoardableLetGo"));
					}
				}
				break;
			}
		}
		
		totalDefeat = !playerFleet.isValidPlayerFleet();
		if (totalDefeat) {
			addText(getString("finalOutcomeNoShipsLeft"));
		}
		
		if (pickedMemberToBoard) {
			//visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
			showFleetInfo();
		}
		
		if (config.salvageRandom != null) {
			context.setSalvageRandom(config.salvageRandom);
		}
		context.generateLoot(null, config.lootCredits);
		context.autoLoot();
		//context.repairShips();
		String leave = "Leave";
		if (config.straightToEngage) {
			leave = "Continue";
		}
		options.addOption(leave, OptionId.LEAVE, null);
		if (!config.straightToEngage) {
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
		} else {
			optionSelected(null, OptionId.LEAVE);
		}
	}
	
	protected boolean recoveredCrew = false;
	protected boolean lootedCredits = false;
	protected String creditsLooted = null;
	protected void winningPath() {
		options.clearOptions();
		DataForEncounterSide playerData = context.getDataFor(playerFleet);
		context.getDataFor(otherFleet).setDisengaged(true);
		
		if (!recoveredCrew) {
			recoveredCrew = true;
			if ((int)playerData.getRecoverableCrewLosses().getCrewInt() + (int)playerData.getRecoverableCrewLosses().getMarines() > 0) {
				addText(getString("recoveryReport"));
				DataForEncounterSide data = context.getDataFor(playerFleet);
				int crewRecovered = (int) data.getRecoverableCrewLosses().getCrew();
				int marinesRecovered = (int) data.getRecoverableCrewLosses().getMarines();
				String crewRecStr = "" + crewRecovered;
				if (crewRecovered <= 0) {
					crewRecStr = "";
				}
				String marinesRecStr = "" + marinesRecovered;
				if (marinesRecovered <= 0) {
					marinesRecStr = "";
				}
				//if (crewRecStr != null && marinesRecStr != null) {
					textPanel.highlightInLastPara(HIGHLIGHT_COLOR, crewRecStr, marinesRecStr);
				//} else if (crewRecStr != null) {
					//textPanel.highlightInLastPara(HIGHLIGHT_COLOR, crewRecStr);
				//} else if (marinesRecStr != null) {
					//textPanel.highlightInLastPara(HIGHLIGHT_COLOR, marinesRecStr);
				//}
				
				context.recoverCrew(playerFleet);
			}
		}
		
		CampaignFleetAPI actualPlayer = Global.getSector().getPlayerFleet();
		
		boolean playerHasPersonnel = actualPlayer.getCargo().getMarines() > 0;
		boolean playerHasReadyShips = !actualPlayer.getFleetData().getCombatReadyMembersListCopy().isEmpty();

		
		if (!didRecoveryCheck) {
			didRecoveryCheck = true;
			recoverableShips = context.getRecoverableShips(context.getBattle(), playerFleet, otherFleet);
			
			if (recoverableShips != null && !recoverableShips.isEmpty()) {
				int crew = actualPlayer.getCargo().getCrew();
				int needed = (int)actualPlayer.getFleetData().getMinCrew();
				
				int extra = crew - needed;
				
				int num = recoverableShips.size();
				String numString = "several ships disabled or destroyed";
				if (num == 1) numString = "a ship disabled";
				String pre = "The salvage chief reports that " + numString + " during the battle " +
							 "can be restored to basic functionalty. Recovering ships instead of breaking " +
							 "them for salvage will greatly reduce the salvage gained from these ships.";
				textPanel.addParagraph(pre);
//				if (extra > 0) {
//					textPanel.addPara(pre + 
//							"You have %s extra crew available, beyond what's " +
//							"already required to operate your current ships.", Misc.getHighlightColor(), "" + extra);
//				} else {
//					textPanel.addParagraph(pre + 
//							"You have no extra crew available for any recovered vessels, beyond what's " +
//							"already required to operate your current ships.");
//				}
				options.addOption("Consider ship recovery", OptionId.RECOVERY_SELECT, null);
				options.addOption("Continue", OptionId.RECOVERY_CONTINUE, null);
				
				return;
			}
		}
		
		
		
		context.adjustPlayerReputation(dialog, getString("friendlyFireRepLoss"),
									   config.impactsAllyReputation, config.impactsEnemyReputation);
		
//		"noSalvageReport":"There's no salvage to be had.",
//		"noSalvageReportPlayerDidNothing":"Your $fleetOrShip does not participate in salvage operations due to its limited contributions throughout the encounter.",
//		"salvageReportPlayer":"Your $fleetOrShip is able to participate in salvage operations due to its contributions throughout the encounter.",
		boolean validFleet = playerFleet.isValidPlayerFleet();
		BattleAPI battle = context.getBattle();
		boolean hasAllies = false;
		boolean startedWithAllies = false;
		if (battle != null) {
			hasAllies = context.getBattle().getPlayerSide().size() <= 1;
			startedWithAllies = context.getBattle().getPlayerSideSnapshot().size() > 1;
		}
		
		if (!lootedCredits) {
			if (config.salvageRandom != null) {
				context.setSalvageRandom(config.salvageRandom);
			}
			
			context.generateLoot(recoveredShips, config.lootCredits);
			if (config.delegate != null) {
				config.delegate.postPlayerSalvageGeneration(dialog, context, context.getLoot());
			}
			lootedCredits = true;
			
			float credits = context.getCreditsLooted();
			if (context.isEngagedInHostilities() && context.getLastEngagementOutcome() != null) {
				if (validFleet) {
					if (credits <= 0 && context.getLoot().isEmpty()) {
						if (startedWithAllies) {
							addText(getString("noSalvageReportPlayerDidNothing"));
						} else {
							addText(getString("noSalvageReport"));
						}
					} else {
						if (startedWithAllies) {
							addText(getString("salvageReportPlayer"));
						}
					}
				}
			}
			
			//creditsLooted = "" + (int) credits;
			creditsLooted = Misc.getWithDGS((int)credits);
			if (credits > 0 && validFleet) {
				addText(getString("creditsLootedReport"));
				textPanel.highlightLastInLastPara(creditsLooted, HIGHLIGHT_COLOR);
				Global.getSector().getPlayerFleet().getCargo().getCredits().add(credits);
			}
		}
		
		if (!context.getLoot().isEmpty() && validFleet) {
			options.addOption("Pick through the wreckage", OptionId.CONTINUE_LOOT, null);
		} else {
			if (!validFleet) {
				addText(getString("finalOutcomeNoShipsLeft"));
			}
			String leave = "Leave";
			boolean withEscape = true;
			if (config.noSalvageLeaveOptionText != null && validFleet && context.getLoot().isEmpty()) {
				leave = config.noSalvageLeaveOptionText;
				withEscape = false;
			}
			options.addOption(leave, OptionId.LEAVE, null);
			if (withEscape) {
				options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			}
		}
	}
	
	protected List<FleetMemberAPI> getCrashMothballable(List<FleetMemberAPI> all) {
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
		CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();
		for (FleetMemberAPI member : all) {
			if (member.isAlly()) continue;
			if (!member.isFighterWing() && member.getRepairTracker().getCR() < crPlugin.getMalfunctionThreshold(member.getStats())) {
				result.add(member);
			}
		}
		return result;
	}

	
	protected OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {
		
		if (inConversation) {
			conversationDelegate.optionMousedOver(optionText, optionData);
			return;
		}
		
		if (optionData instanceof String) return;
		
		if (optionData == null) {
			if (currVisualType != VisualType.FLEET_INFO) {
				showFleetInfo();
				currVisualType = VisualType.FLEET_INFO;
			}
			lastOptionMousedOver = null;
			return;
		}
		OptionId option = (OptionId) optionData;
		if (option == lastOptionMousedOver) return;
		lastOptionMousedOver = option;
	}
	
	protected void showFleetInfo() {
		BattleAPI b = context.getBattle();
		if (b != null && b.isPlayerInvolved()) {
			String titleOne = "Your forces";
			if (b.isPlayerInvolved() && b.getPlayerSide().size() > 1) {
				titleOne += ", with allies";
			}
			if (!Global.getSector().getPlayerFleet().isValidPlayerFleet() && b.getPlayerSide().size() > 1) {
				titleOne = "Allied forces";
			}
			String titleTwo = null;
			if (b.getPrimary(b.getNonPlayerSide()) != null) {
				titleTwo = b.getPrimary(b.getNonPlayerSide()).getNameWithFactionKeepCase();
			}
			if (b.getNonPlayerSide().size() > 1) titleTwo += ", with allies";
			visual.showFleetInfo(titleOne, b.getPlayerCombined(), Misc.ucFirst(titleTwo), b.getNonPlayerCombined(), context);
		} else {
			visual.showFleetInfo((String)null, playerFleet, (String)null, otherFleet, context);
		}
	}
	
	public void advance(float amount) {
		
	}
	
	protected void addText(String text) {
		textPanel.addParagraph(text);
	}
	protected void addText(String text, Color color) {
		textPanel.addParagraph(text, color);
	}
	protected void addText(String text, String hl, Color hlColor) {
		LabelAPI label = textPanel.addParagraph(text);
		label.setHighlight(hl);
		label.setHighlightColor(hlColor);
	}
	
	protected void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	protected void updateDialogState() {
		options.clearOptions();
		options.addOption("Cut the comm link", OptionId.CUT_COMM, null);
	}
	
	protected void updatePreCombat() {
		options.clearOptions();
		
		//playerFleet.updateCounts();
		//int nonFighters = playerFleet.getFleetData().getMembersListCopy().size() - playerFleet.getNumFighters();
		boolean canTransfer = false;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.isFighterWing() || member.isAlly()) continue;
			if (member.isFlagship()) continue;
			canTransfer = true;
			break;
		}
		if (playerGoal == FleetGoal.ATTACK && otherGoal == FleetGoal.ESCAPE) {
			String tooltipText = getString("tooltipPursueAutoresolve");
			options.addOption("Order your second-in-command to handle it", OptionId.AUTORESOLVE_PURSUE, tooltipText);
			options.addOption("Transfer command for this engagement", OptionId.SELECT_FLAGSHIP, getString("tooltipSelectFlagship"));
			//if (nonFighters <= 1) {
			if (!canTransfer) {
				options.setEnabled(OptionId.SELECT_FLAGSHIP, false);
			}
			options.addOption("Take command of the action", OptionId.CONTINUE_INTO_BATTLE, null);
		} else {
			options.addOption("Transfer command for this engagement", OptionId.SELECT_FLAGSHIP, getString("tooltipSelectFlagship"));
			//if (nonFighters <= 1) {
			if (!canTransfer) {
				options.setEnabled(OptionId.SELECT_FLAGSHIP, false);
			}
			if (playerGoal == FleetGoal.ESCAPE) {
				List<FleetMemberAPI> choices = getCrashMothballable(playerFleet.getFleetData().getCombatReadyMembersListCopy());
				
				options.addOption("Crash-mothball some of your ships to prevent malfunctions", OptionId.CRASH_MOTHBALL, null);
				if (choices.isEmpty()) {
					options.setEnabled(OptionId.CRASH_MOTHBALL, false);
					options.setTooltip(OptionId.CRASH_MOTHBALL, getString("tooltipCrashMothballUnavailable"));
				} else {
					options.setTooltip(OptionId.CRASH_MOTHBALL, getString("tooltipCrashMothball"));
				}
			}
			if (config.straightToEngage) {
				options.addOption("Continue into battle", OptionId.CONTINUE_INTO_BATTLE, null);
			} else {
				options.addOption("Continue", OptionId.CONTINUE_INTO_BATTLE, null);
			}
		}
		
		boolean canGoBack = ongoingBattle || otherGoal == FleetGoal.ESCAPE || Global.getSettings().isDevMode();
		if (canGoBack) {
			options.addOption("Go back", OptionId.GO_TO_MAIN, null);
			options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
		}
//		if (ongoingBattle) {
//			options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//			options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
//		}
		if (Global.getSettings().isDevMode()) {
//			if (ongoingBattle) {
////				options.addOption("Go back", OptionId.GO_TO_MAIN, null);
////				options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
//			} else {
//				options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//				options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
//			}
			DevMenuOptions.addOptions(dialog);
		}
	}
	
	protected String createShipNameListString(List<FleetMemberAPI> members) {
		String str = "";
		int fighters = 0;
		int ships = 0;
		for (FleetMemberAPI member : members) {
			boolean last = members.indexOf(member) == members.size() - 1;
			boolean secondToLast = members.indexOf(member) == members.size() - 2;
			boolean fighter = member.isFighterWing();
			if (fighter) {
				fighters++;
			} else {
				ships++;
				if (last && fighters == 0 && ships > 1) {
					if (members.size() > 2) {
						str += ", and the " + member.getShipName();
					} else {
						str += " and the " + member.getShipName();
					}
				} else {
					str += "the " + member.getShipName();
				}
			}
			if (!last && !secondToLast && !fighter) {
				str += ", ";
			} 
			
			if (last && fighters > 0) {
				if (fighters == 1) {
					if (ships == 0) {
						str += "a fighter wing";
					} else {
						if (ships > 1) {
							str += ", and a fighter wing";
						} else {
							str += " and a fighter wing";
						}
					}
				} else {
					if (ships == 0) {
						str += "several fighter wings";
					} else {
						if (ships > 1) {
							str += ", and several fighter wings";
						} else {
							str += " and several fighter wings";
						}
					}
				}
			}
		}
		return str;
	}
	
	protected void updateMainState(boolean withText) {
		options.clearOptions();
		
		if (isFightingOver()) {
			goToEncounterEndPath();
			return;
		}
		
		if (ongoingBattle) {
			BattleAPI battle = context.getBattle();
			boolean playerHasReadyShips = false;
			for (FleetMemberAPI member : playerFleet.getFleetData().getCombatReadyMembersListCopy()) {
				if (!member.isAlly()) {
					playerHasReadyShips = true;
				}
			}
			if (!joinedBattle && battle.canJoin(playerFleet)) {
				options.addOption("Join the battle", OptionId.JOIN_ONGOING_BATTLE, null);
				if (!playerHasReadyShips) {
					options.setEnabled(OptionId.JOIN_ONGOING_BATTLE, false);
				}
			}
			
			options.addOption("Leave", OptionId.LEAVE, null);
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			if (Global.getSettings().isDevMode()) {
				DevMenuOptions.addOptions(dialog);
			}
		} else {
			if (config.showCommLinkOption) {
				if (otherFleet.getMemoryWithoutUpdate().is("$hailing", true)) {
					options.addOption("Accept the comm request", OptionId.OPEN_COMM, Misc.getStoryOptionColor(), null);
					otherFleet.getMemoryWithoutUpdate().unset("$hailing");
				} else {
					options.addOption("Open a comm link", OptionId.OPEN_COMM, null);
				}
			}
		
			boolean smuggler = otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
			if (otherFleet.getFaction().isPlayerFaction() && !smuggler) {
				options.addOption("Leave", OptionId.LEAVE, null);
				options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			} else {
				updateEngagementChoice(withText);
			}
		}
		
	}
	
	
	protected boolean allyEngagementChoiceNoBattle = false;
	protected boolean harryEndedBattle = false;
	private List<FleetMemberAPI> recoverableShips;
	private List<FleetMemberAPI> recoveredShips = new ArrayList<FleetMemberAPI>();
	protected void updateEngagementChoice(boolean withText) {
		allyEngagementChoiceNoBattle = false;
		//options.clearOptions();
		if (isFightingOver()) {
			goToEncounterEndPath();
			return;
		}
		//options.clearOptions();
		
		BattleAPI b = context.getBattle();

		if (ongoingBattle && b.getPlayerSide() != null && b.getPlayerSide().size() <= 1) {
		//if (ongoingBattle && b.getPlayerSide() != null && b.isPlayerPrimary()) {
			ongoingBattle = false;
			if (config.showCommLinkOption) {
				options.addOption("Open a comm link", OptionId.OPEN_COMM, null);
			}
		}
	
		playerGoal = null;
		otherGoal = null;
		
		boolean alliedWantsToFight = alliedFleetWantsToFight();
		boolean alliedWantsToRun = alliedFleetWantsToDisengage() && alliedCanDisengage();
		boolean alliedHolding = alliedFleetHoldingVsStrongerEnemy();
		boolean otherWantsToFight = otherFleetWantsToFight();
		boolean otherWantsToRun = otherFleetWantsToDisengage() && otherCanDisengage();
		otherWantsToRun = otherFleetWantsToDisengage() && otherCanDisengage();
		boolean otherHolding = otherFleetHoldingVsStrongerEnemy();
		
		//boolean otherWantsToRun = otherFleetWantsToDisengage() && otherCanDisengage();
		boolean playerHasReadyShips = false;
		boolean allyHasReadyShips = false;
		for (FleetMemberAPI member : playerFleet.getFleetData().getCombatReadyMembersListCopy()) {
			if (member.isAlly() && !member.isStation()) {
				allyHasReadyShips = true;
			} else {
				playerHasReadyShips = true;
			}
		}
		
		if (otherWantsToRun && canDisengageCleanly(otherFleet)) {
//			if (didEnoughToDisengage(otherFleet)) {
//				if (context.getBattle().getPlayerSide().size() > 1) {
//					if (withText) addText(getString("enemyDisruptedPlayerSide"), Misc.getNegativeHighlightColor());
//				} else {
//					if (withText) addText(getString("enemyDisruptedPlayer"), Misc.getNegativeHighlightColor());
//				}
//			} else {
				if (context.getBattle().getPlayerSide().size() > 1) {
					if (withText) addText(getString("enemyCleanDisengageSide"));
				} else {
					if (withText) addText(getString("enemyCleanDisengage"));
				}
//			}
			goToEncounterEndPath();
		} else if (otherWantsToRun) {
			String pursueTooltip = "tooltipPursue";
			String harassTooltip = "tooltipHarassRetreat";
			String letThemGoTooltip = "tooltipLetThemGo";
			if (!context.isEngagedInHostilities()) {
				letThemGoTooltip = "tooltipLetThemGoNoPenalty";
			}
			
			boolean canPursue = false;
			boolean canHasass = false;
			//PursueAvailability pa = context.getPursuitAvailability(playerFleet, otherFleet);
			PursueAvailability pa = getPursuitAvailability(playerFleet);
			//List<FleetMemberAPI> members = getPursuitCapablePlayerShips();
			//if (members.isEmpty()) pa = PursueAvailability.NO_READY_SHIPS;
			
			DisengageHarryAvailability dha = context.getDisengageHarryAvailability(playerFleet, otherFleet);
			
			switch (pa) {
			case AVAILABLE:
				canPursue = true;
				break;
			case LOST_LAST_ENGAGEMENT:
				pursueTooltip = "tooltipPursueLostLast";
				break;
			case NO_READY_SHIPS:
				pursueTooltip = "tooltipNoReadyShips";
				break;
			case TOOK_SERIOUS_LOSSES:
				if (context.getBattle().getPlayerSide().size() > 1) {
					if (withText) addText(getString("enemyDisruptedPlayerSide"), getString("highlightDisruptedPlayer"), Misc.getNegativeHighlightColor());
				} else {
					if (withText) addText(getString("enemyDisruptedPlayer"), getString("highlightDisruptedPlayer"), Misc.getNegativeHighlightColor());
				}
				pursueTooltip = "tooltipPursueSeriousLosses";
				break;
			case TOO_SLOW:
				pursueTooltip = "tooltipPursueTooSlow";
				break;
			}
			
			switch (dha) {
			case AVAILABLE:
				canHasass = true;
				break;
			case NO_READY_SHIPS:
				harassTooltip = "tooltipNoReadyShips";
				break;
			}
			
			if (ongoingBattle) {
				boolean station = false;
				if (playerFleet != null) {
					for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
						if (member.isStation()) {
							station = true;
							break;
						}
					}
				}
				
				//boolean letGo = (!canPursue && !canHasass) || !allyHasReadyShips || station;
				boolean letGo = (!canPursue && !canHasass) || !allyHasReadyShips;// || station;
				//if (!letGo) {
					//PursuitOption po = playerFleet.getAI().pickPursuitOption(context, otherFleet);
					PursuitOption po = pickPursuitOption(playerFleet, otherFleet, context);
					po = PursuitOption.PURSUE;
					if (alliedWantsToRun || alliedHolding || !alliedWantsToFight || letGo) {
						po = PursuitOption.LET_THEM_GO;
					}
					if (!canPursue && canHasass) {
						po = PursuitOption.HARRY;
					}
					//po = PursuitOption.LET_THEM_GO;
					switch (po) {
					case PURSUE:
						if (withText) addText(getString("ongoingBattlePursue"));
						playerGoal = FleetGoal.ATTACK;
						otherGoal = FleetGoal.ESCAPE;
						options.addOption("Join the pursuit", OptionId.CONTINUE_ONGOING_BATTLE, getString(pursueTooltip));
						if (!canPursue || !playerHasReadyShips) {
							options.setEnabled(OptionId.CONTINUE_ONGOING_BATTLE, false);
						}
						break;
					case HARRY:
						// CR loss from harrying
						context.applyPursuitOption(playerFleet, otherFleet, po);
						
						if (withText) addText(getString("ongoingBattleHarass"));
						context.setEngagedInHostilities(true);
						context.getDataFor(playerFleet).setDisengaged(false);
						context.getDataFor(otherFleet).setDisengaged(true);
						allyEngagementChoiceNoBattle = true;
						harryEndedBattle = true;
						//rememberWasBeaten();
						break;
					case LET_THEM_GO:
						letGo = true;
						if (context.isEngagedInHostilities()) {
							context.getDataFor(playerFleet).setDisengaged(false);
							context.getDataFor(otherFleet).setDisengaged(true);
						}
						allyEngagementChoiceNoBattle = true;
						//rememberWasBeaten();
						break;
					}
				//}
				if (letGo) {
					if (withText) addText(getString("ongoingBattleLetGo"));
					allyEngagementChoiceNoBattle = true;
				}
				
				if (context.isEngagedInHostilities() && context.isBattleOver()) {
					goToEncounterEndPath();
				} else {
					options.addOption("Leave", OptionId.LEAVE, null);
					options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
				}
			} else {
				CampaignFleetAIAPI ai = otherFleet.getAI();
				boolean hostile = false;
				if (ai != null) {
					hostile = ai.isHostileTo(playerFleet) || context.isEngagedInHostilities();
				}
				
				options.addOption("Pursue them", OptionId.PURSUE, getString(pursueTooltip));
				options.addOption("Harry their retreat", OptionId.HARRY_PURSUE, getString(harassTooltip));
				boolean knows = context.getBattle() != null && context.getBattle().getNonPlayerSide() != null &&
								context.getBattle().knowsWhoPlayerIs(context.getBattle().getNonPlayerSide());
				boolean lowImpact = context.isLowRepImpact();
				FactionAPI nonHostile = getNonHostileOtherFaction();
				//if (!playerFleet.getFaction().isHostileTo(otherFleet.getFaction()) && knows && !context.isEngagedInHostilities()) {
				if (nonHostile != null && knows && !lowImpact && !context.isEngagedInHostilities() &&
						config.showWarningDialogWhenNotHostile) {
					options.addOptionConfirmation(OptionId.HARRY_PURSUE, "The " + nonHostile.getDisplayNameLong() + " " + nonHostile.getDisplayNameIsOrAre() + " not currently hostile, and you have been positively identified. Are you sure you want to engage in hostilities with one of their fleets?", "Yes", "Never mind");
					options.addOptionConfirmation(OptionId.PURSUE, "The " + nonHostile.getDisplayNameLong() + " " + nonHostile.getDisplayNameIsOrAre() + " not currently hostile, and you have been positively identified. Are you sure you want to engage in hostilities with one of their fleets?", "Yes", "Never mind");
				}
				if (hostile) {
					options.addOption("Let them go", OptionId.LET_THEM_GO, getString(letThemGoTooltip));
				} else {
					options.addOption("Leave", OptionId.LEAVE, null);
					options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
//					options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//					options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
				}
				
				if (!canPursue || !playerHasReadyShips) {
					options.setEnabled(OptionId.PURSUE, false);
				}
				if (!canHasass || !playerHasReadyShips) {
					options.setEnabled(OptionId.HARRY_PURSUE, false);
				}
			}
		} else {
			if (ongoingBattle) {
				if (alliedWantsToRun) {
					if (withText && !config.straightToEngage) addText(getString("ongoingBattleDisengage"));
					playerGoal = FleetGoal.ESCAPE;
					otherGoal = FleetGoal.ATTACK;
					options.addOption("Join the disengage attempt", OptionId.CONTINUE_ONGOING_BATTLE, null);
				} else {
					boolean station = false;
					if (playerFleet != null) {
						for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
							if (member.isStation()) {
								station = true;
								break;
							}
						}
					}
					
					if (withText && !config.straightToEngage) {
						if (station) {
							addText(getString("ongoingBattleStation"));
						} else {
							addText(getString("ongoingBattleEngage"));
						}
					}
					playerGoal = FleetGoal.ATTACK;
					otherGoal = FleetGoal.ATTACK;
					
					if (playerHasReadyShips) {
						options.addOption("Join the engagement", OptionId.CONTINUE_ONGOING_BATTLE, null);
					} else {
						options.addOption("Join the engagement", OptionId.CONTINUE_ONGOING_BATTLE, getString("tooltipNoReadyShips"));
						options.setEnabled(OptionId.CONTINUE_ONGOING_BATTLE, false);
					}
					
					options.addOption("Leave", OptionId.LEAVE, null);
					options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
				}
			} else {
				String engageText = "Move in to engage";
				if (config.firstTimeEngageOptionText != null && !context.isEngagedInHostilities()) {
					engageText = config.firstTimeEngageOptionText;
				}
				if (config.afterFirstTimeEngageOptionText != null && context.isEngagedInHostilities()) {
					engageText = config.afterFirstTimeEngageOptionText;
				}
				if (playerHasReadyShips) {
					options.addOption(engageText, OptionId.ENGAGE, getString("tooltipEngage"));
					boolean knows = context.getBattle() != null && context.getBattle().getNonPlayerSide() != null &&
									context.getBattle().knowsWhoPlayerIs(context.getBattle().getNonPlayerSide());
					boolean lowImpact = context.isLowRepImpact();
					FactionAPI nonHostile = getNonHostileOtherFaction();
					//if (!playerFleet.getFaction().isHostileTo(otherFleet.getFaction()) && knows && !context.isEngagedInHostilities()) {
					if (nonHostile != null && knows && !lowImpact && !context.isEngagedInHostilities() &&
							config.showWarningDialogWhenNotHostile) {
						options.addOptionConfirmation(OptionId.ENGAGE, "The " + nonHostile.getDisplayNameLong() + " " + nonHostile.getDisplayNameIsOrAre() + " not currently hostile, and you have been positively identified. Are you sure you want to attack one of their fleets?", "Yes", "Never mind");
					}
					
				} else {
					options.addOption(engageText, OptionId.ENGAGE, getString("tooltipNoReadyShips"));
					options.setEnabled(OptionId.ENGAGE, false);
				}
				CampaignFleetAIAPI ai = otherFleet.getAI();
				boolean hostile = false;
				if (ai != null) {
					hostile = ai.isHostileTo(playerFleet) || context.isEngagedInHostilities();
				}
				if (!config.leaveAlwaysAvailable &&
						(otherFleetWantsToFight() || (hostile && !otherFleetWantsToDisengage()))) {
					if (canDisengageCleanly(playerFleet)) {
						options.addOption("Disengage", OptionId.DISENGAGE, getString("tooltipCleanDisengage"));
					} else if (canDisengageWithoutPursuit(playerFleet) && !(!otherFleetWantsToFight() && !otherFleetWantsToDisengage())) {
						options.addOption("Disengage", OptionId.DISENGAGE, getString("tooltipHarrassableDisengage"));
					} else {
						if (otherFleetHoldingVsStrongerEnemy() || (!otherFleetWantsToFight() && !otherFleetWantsToDisengage())) {
							options.addOption("Leave", OptionId.LEAVE, null);
							options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
//							options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//							options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
						} else {
							if (canDisengage() || !playerHasReadyShips) {
								options.addOption("Attempt to disengage", OptionId.ATTEMPT_TO_DISENGAGE, getString("tootipAttemptToDisengage"));
							} else {

								boolean hasStation = false;
								boolean allStation = true;
								for (CampaignFleetAPI curr : context.getBattle().getSideFor(otherFleet)) {
									allStation &= curr.isStationMode();
									hasStation |= curr.isStationMode();
								}
								
								if (hasStation) {
									if (allStation) {
										options.addOption("Disengage", OptionId.DISENGAGE, getString("tooltipCleanDisengage"));
									} else {
										options.addOption("Disengage", OptionId.DISENGAGE, getString("tooltipHarrassableDisengage"));
									}
								} else {
									if (withText) {
										//addText(getString("playerTooLargeToDisengage"));
										addText(getString("playerTooLargeToDisengage"), getString("highlightTooLarge"), Misc.getNegativeHighlightColor());
										addText(getString("playerTooLargeCanFightToDisengage"), getString("highlightCanFight"), Misc.getHighlightColor());
									}
								}
							}
						}
					}
				} else {
					options.addOption("Leave", OptionId.LEAVE, null);
					options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
//					options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//					options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
				}
			}
		}
		
		if (playerOutBeforeAllies()) {
			if (!options.hasOption(OptionId.LEAVE) && 
					!options.hasOption(OptionId.LET_THEM_GO) &&
					!options.hasOption(OptionId.DISENGAGE)) {
				options.addOption("Leave", OptionId.LEAVE, null);
				options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			}
		}
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
		
		// if it's an ongoing battle, this will all get cleared out by a subsequent call to updatePreCombat()
//		if (!options.hasOption(OptionId.GO_TO_MAIN)) {
//			options.addOption("Go back", OptionId.GO_TO_MAIN, null);
//			options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
//		}
		
//		if (Global.getSettings().isDevMode()) {
//			DevMenuOptions.addOptions(dialog);
//		}
	}
	
	protected PursuitOption pickPursuitOption(CampaignFleetAPI fleet, CampaignFleetAPI other, FleetEncounterContext context) {
		if (fleet.getAI() == null) return PursuitOption.LET_THEM_GO;
		
		if (context.getBattle() != null) {
			boolean allStation = true;
			for (CampaignFleetAPI curr : context.getBattle().getSideFor(fleet)) {
//				if (curr.isStationMode()) {
//					return PursuitOption.HARRY;
//				}
				allStation &= curr.isStationMode();
			}
			if (allStation) {
				return PursuitOption.LET_THEM_GO;
			}
		}
		
		return fleet.getAI().pickPursuitOption(context, other);
	}
	
	public FactionAPI getNonHostileOtherFaction() {
		if (context.getBattle() == null) return null;
		
		FactionAPI player = Global.getSector().getPlayerFaction();
		int max = -1;
		CampaignFleetAPI result = null;
		
		//BattleSide playerSide = context.getBattle().pickSide(Global.getSector().getPlayerFleet());
		
		//List<CampaignFleetAPI> otherSide = context.getBattle().getSideFor(otherFleet);
		List<CampaignFleetAPI> otherSide = context.getBattle().getNonPlayerSide();
		
		if (otherSide != null) {
			for (CampaignFleetAPI other : otherSide) {
				if (!player.isHostileTo(other.getFaction()) && other.getFleetPoints() > max) {
					result = other;
					max = other.getFleetPoints();
				}
			}
		}
		return result == null ? null : result.getFaction();
	}
	
	protected boolean playerOutBeforeAllies() {
		EngagementOutcome last = context.getLastEngagementOutcome();
		if (last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS ||
				last == EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_LOSS ||
				last == EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_LOSS
			) {
			return true;
		}
		return false;
	}
	
	public static boolean canDisengage() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		float total = 0f;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.canBeDeployedForCombat()) {
				total += member.getDeploymentPointsCost();
			}
		}
		return total <= getDisengageSize();
	}
	
	protected boolean otherCanDisengage() {
		return fleetCanDisengage(otherFleet);
	}
	
	protected boolean alliedCanDisengage() {
		return fleetCanDisengage(playerFleet);
	}
	
	protected boolean fleetCanDisengage(CampaignFleetAPI fleet) {
		float total = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.canBeDeployedForCombat()) {
				total += member.getDeploymentPointsCost();
			}
		}
		return total <= getDisengageSize();
	}
	
	public static float getDisengageSize() {
		float abs = Global.getSettings().getFloat("maxDisengageSize");
		float fraction = Global.getSettings().getFloat("maxDisengageFraction") * Global.getSettings().getBattleSize();
		return Math.min(abs, fraction);
	}
	
	protected boolean didEnoughToDisengage(CampaignFleetAPI fleet) {
		DataForEncounterSide data = context.getDataFor(fleet);
		return data.isDidEnoughToDisengage();
	}
	
	protected boolean canDisengageCleanly(CampaignFleetAPI fleet) {
		//if (wasEnemyDisrupted(fleet)) return true;
		DataForEncounterSide data = context.getDataFor(fleet);
		if (data.isWonLastEngagement()) return true;
		
		
		if (fleet == playerFleet) {
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				if (member.isStation()) return true;
			}
		}
		
		EngagementOutcome last = context.getLastEngagementOutcome();
		if (fleet == playerFleet && !ongoingBattle &&
				(last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS ||
				last == EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_LOSS ||
				last == EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN ||
				last == EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_LOSS)) {
			return true;
		}
		
		return false;
	}
	protected boolean canDisengageWithoutPursuit(CampaignFleetAPI fleet) {
		CampaignFleetAPI other = playerFleet;
		if (other == fleet) other = otherFleet;
		PursueAvailability pa = getPursuitAvailability(other);
		return pa != PursueAvailability.AVAILABLE;
	}
	
	protected PursueAvailability getPursuitAvailability(CampaignFleetAPI fleet) {
		CampaignFleetAPI other = playerFleet;
		if (other == fleet) other = otherFleet;
		PursueAvailability pa = context.getPursuitAvailability(fleet, other);
		if (pa == PursueAvailability.TOO_SLOW && fleet == playerFleet) {
			pa = PursueAvailability.AVAILABLE;
		}
		return pa;
	}
	
	protected String getString(String id) {
		String str = Global.getSettings().getString("fleetInteractionDialog", id);
		
		String faction = otherFleet.getFaction().getEntityNamePrefix();
		if (faction == null || faction.isEmpty()) {
			faction = otherFleet.getFaction().getDisplayName();
		}
		String fleetName = otherFleet.getName();
		String firstName = otherFleet.getCommander().getName().getFirst();
		String lastName = otherFleet.getCommander().getName().getLast();
		String fleetOrShip = "fleet";
		if (otherFleet.getFleetData().getMembersListCopy().size() == 1) {
			fleetOrShip = "ship";
			if (otherFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				fleetOrShip = "fighter wing";
			}
		}
		String playerFleetOrShip = "fleet";
		if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
			playerFleetOrShip = "ship";
			if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				playerFleetOrShip = "fighter wing";
			}
		}
		
		DataForEncounterSide data = context.getDataFor(playerFleet);
		if (data != null) {
			int crewLost = (int) (data.getCrewLossesDuringLastEngagement().getCrewInt());
			String crewLostStr = getApproximate(crewLost);
			
			int marinesLost = (int) (data.getCrewLossesDuringLastEngagement().getMarines());
			String marinesLostStr = getApproximate(marinesLost);
			
			int crewRecovered = (int) data.getRecoverableCrewLosses().getCrewInt();
			int marinesRecovered = (int) data.getRecoverableCrewLosses().getMarines();
		
			String crewRecStr = "" + crewRecovered;
			if (crewRecovered <= 0) {
				crewRecStr = "no";
			}
			String marinesRecStr = "" + marinesRecovered;
			if (marinesRecovered <= 0) {
				marinesRecStr = "no";
			}
			
			str = str.replaceAll("\\$crewLost", crewLostStr);
			str = str.replaceAll("\\$marinesLost", marinesLostStr);
			str = str.replaceAll("\\$crewLost", crewLostStr);
			str = str.replaceAll("\\$crewRecovered", crewRecStr);
			str = str.replaceAll("\\$marinesRecovered", marinesRecStr);
		}
		
		if (toBoard != null) {
			int numLifeSigns = (int) (toBoard.getCrewComposition().getCrew() + toBoard.getCrewComposition().getMarines());
			str = str.replaceAll("\\$numLifeSigns", getApproximate(numLifeSigns));
			
			str = str.replaceAll("\\$boardableShipName", toBoard.getShipName());
		}
		
		str = str.replaceAll("\\$faction", faction);
		str = str.replaceAll("\\$fleetName", fleetName);
		str = str.replaceAll("\\$firstName", firstName);
		str = str.replaceAll("\\$lastName", lastName);
		str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
		str = str.replaceAll("\\$playerFleetOrShip", playerFleetOrShip);
		
		if (selectedFlagship != null) {
			str = str.replaceAll("\\$flagship", "the " + selectedFlagship.getShipName());
		}
		
		str = str.replaceAll("\\$creditsLooted", creditsLooted);
		
		if (crashMothballList != null) {
			str = str.replaceAll("\\$crashMothballList", crashMothballList);
		}
		
		if (repairedShipList != null) {
			str = str.replaceAll("\\$repairedShipList", repairedShipList);
		}
		
		int marines = Global.getSector().getPlayerFleet().getCargo().getMarines();
		str = str.replaceAll("\\$marines", "" + marines);
		
		str = str.replaceAll("\\$boardingSuccessChance", "" + (int) boardingPercentSuccess + "%");
		
		if (boardingResult != null) {
			str = str.replaceAll("\\$boardingCrewLost", getIntOrNo(boardingResult.getAttackerLosses().getCrew()));
			str = str.replaceAll("\\$boardingMarinesLost", getIntOrNo(boardingResult.getAttackerLosses().getMarines()));
			str = str.replaceAll("\\$boardingEnemyCrewLost", getIntOrNo(boardingResult.getDefenderLosses().getCrew()));
			str = str.replaceAll("\\$boardingEnemyMarinesLost", getIntOrNo(boardingResult.getDefenderLosses().getMarines()));
		}
		
//		# $alliedFactionAndTheirAllies "Hegemony forces and their allies"
//		# $enemyFactionAndTheirAllies "Hegemony forces and their allies"
//		# $yourForcesWereOrYourSideWas
		BattleAPI b = context.getBattle();
		if (b != null) {
			BattleSide playerSide = b.pickSide(Global.getSector().getPlayerFleet());
			CampaignFleetAPI sideOnePrimary = b.getPrimary(b.getSideOne());
			CampaignFleetAPI sideTwoPrimary = b.getPrimary(b.getSideTwo());
			if (playerSide != BattleSide.NO_JOIN) {
				sideOnePrimary = b.getPrimary(b.getSide(playerSide));
				sideTwoPrimary = b.getPrimary(b.getOtherSide(playerSide));
			}
			
			if (sideOnePrimary != null) {
				String strOne = sideOnePrimary.getFaction().getEntityNamePrefix() + " forces";
				if (strOne.startsWith(" ")) {
					strOne = sideOnePrimary.getFaction().getDisplayName() + " forces";
				}
//				if (b.isStationInvolved(b.getSideFor(sideOnePrimary))) {
//					strOne = strOne.replaceFirst(" forces", " station");
//				}
				for (CampaignFleetAPI fleet : b.getSideFor(sideOnePrimary)) {
					if (fleet.getFaction() != sideOnePrimary.getFaction()) {
						if (fleet.isPlayerFleet()) continue;
						strOne += " and their allies";
						break;
					}
				}
				str = str.replaceAll("\\$alliedFactionAndTheirAllies", strOne);
			}
			if (sideTwoPrimary != null) {
				String strTwo = sideTwoPrimary.getFaction().getEntityNamePrefix() + " forces";
				if (strTwo.startsWith(" ")) {
					strTwo = sideTwoPrimary.getFaction().getDisplayName() + " forces";
				}
//				if (b.isStationInvolved(b.getSideFor(sideTwoPrimary))) {
//					strTwo = strTwo.replaceFirst(" forces", " station");
//				}
				for (CampaignFleetAPI fleet : b.getSideFor(sideTwoPrimary)) {
					if (fleet.getFaction() != sideTwoPrimary.getFaction()) {
						if (fleet.isPlayerFleet()) continue;
						strTwo += " and their allies";
						break;
					}
				}
				str = str.replaceAll("\\$enemyFactionAndTheirAllies", strTwo);
			}
			
			//$yourForcesWereOrYourSideWas
			String yourForcesWere = "Your forces were";
			if (b.getPlayerSide() != null && b.getPlayerSide().size() > 1) {
				yourForcesWere = "Your side was";
			}
			str = str.replaceAll("\\$yourForcesWereOrYourSideWas", yourForcesWere);
		}
		

//		float recoveryFraction = context.getStandDownRecoveryFraction();
//		str = str.replaceAll("\\$standDownRecovery", "" + (int) (recoveryFraction * 100f));
		
		return str;
	}
	
	protected String getIntOrNo(float value) {
		if (value < 1) {
			return "no";
		}
		return "" + (int) value;
	}
	
	protected String getApproximate(float value) {
		int v = (int) value;
		String str = "multiple";
		if (v <= 0) {
			str = "no";
		} else if (v < 10) {
			str = "" + v;
		} else if (v < 100) {
			v = (int) Math.round((float) v/10f) * 10;
			str = "approximately " + v;
		} else if (v < 1000) {
			v = (int) Math.round((float) v/10f) * 10;
			str = "approximately " + v;
		} else {
			v = (int) Math.round((float) v/100f) * 100;
			str = "" + v;
		}
		return str;
	}
	
	protected String getApproximateNumOnly(float value) {
		int v = (int) value;
		String str = "";
		if (v <= 0) {
			str = "asdasd";
		} else if (v < 10) {
			str = "" + v;
		} else if (v < 100) {
			v = (int) Math.round((float) v/10f) * 10;
			str = "" + v;
		} else if (v < 1000) {
			v = (int) Math.round((float) v/10f) * 10;
			str = "" + v;
		} else {
			v = (int) Math.round((float) v/100f) * 100;
			str = "" + v;
		}
		return str;
	}
	
	
	protected boolean isFightingOver() {
		return context.isBattleOver() || 
				(context.getDataFor(otherFleet).disengaged() && context.getDataFor(playerFleet).disengaged());
//		return context.getDataFor(playerFleet).getLastGoal() == FleetGoal.ESCAPE ||
//			   context.getDataFor(otherFleet).getLastGoal() == FleetGoal.ESCAPE;
		//return context.getWinnerData().getLastGoal() == FleetGoal.ESCAPE || context.getLoserData().getLastGoal() == FleetGoal.ESCAPE;
	}
	
	public boolean alliedFleetWantsToFight() {
		return fleetWantsToFight(playerFleet, otherFleet);
	}
	public boolean otherFleetWantsToFight() {
		return fleetWantsToFight(otherFleet, playerFleet);
	}
	public boolean otherFleetWantsToFight(boolean assumeHostile) {
		return fleetWantsToFight(otherFleet, playerFleet, assumeHostile);
	}
	protected boolean fleetWantsToFight(CampaignFleetAPI fleet, CampaignFleetAPI other) {
		return fleetWantsToFight(fleet, other, false);
	}
	protected boolean fleetWantsToFight(CampaignFleetAPI fleet, CampaignFleetAPI other, boolean assumeHostile) {
		if (config.alwaysAttackVsAttack) return true;
		
		boolean hasNonCivReserves = false;
		for (FleetMemberAPI member : context.getDataFor(fleet).getInReserveDuringLastEngagement()) {
			if (!member.isCivilian()) {
				hasNonCivReserves = true;
				break;
			}
		}
		if (context.isEngagedInHostilities() &&
				!context.getDataFor(fleet).isWonLastEngagement() &&
				!hasNonCivReserves) {
			return false;
		}
		
		CampaignFleetAIAPI ai = fleet.getAI();
		if (ai == null) return false;
		EncounterOption option = ai.pickEncounterOption(context, other);
		
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE)) return false;
		
		return (ai.isHostileTo(other) || context.isEngagedInHostilities() || assumeHostile || 
				fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE) //||
				// "aggressive" just means "always engage IF already hostile"
				//fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE)
				) &&
				(option == EncounterOption.ENGAGE || (option == EncounterOption.HOLD && ongoingBattle));
	}
	
	
	
	protected boolean alliedFleetHoldingVsStrongerEnemy() {
		return fleetHoldingVsStrongerEnemy(playerFleet, otherFleet);
	}
	protected boolean otherFleetHoldingVsStrongerEnemy() {
		return fleetHoldingVsStrongerEnemy(otherFleet, playerFleet);
	}
	protected boolean fleetHoldingVsStrongerEnemy(CampaignFleetAPI fleet, CampaignFleetAPI other) {
		CampaignFleetAIAPI ai = fleet.getAI();
		if (ai == null) return false;
		boolean hostile = ai.isHostileTo(other) || (other.getAI() != null && other.getAI().isHostileTo(fleet)) || context.isEngagedInHostilities();
		if (!hostile) return false;
		
		if (ai.pickEncounterOption(context, other) == EncounterOption.HOLD_VS_STRONGER) return true;
		
		return fleetWantsToDisengage(fleet, other) && !fleetCanDisengage(fleet);
	}
	
	protected boolean alliedFleetWantsToDisengage() {
		return fleetWantsToDisengage(playerFleet, otherFleet);
	}
	
	protected boolean otherFleetWantsToDisengage() {
		return fleetWantsToDisengage(otherFleet, playerFleet);
	}
	protected boolean fleetWantsToDisengage(CampaignFleetAPI fleet, CampaignFleetAPI other) {
		if (config.alwaysAttackVsAttack) return false;
		
		boolean hasNonCivReserves = false;
		for (FleetMemberAPI member : context.getDataFor(fleet).getInReserveDuringLastEngagement()) {
			if (!member.isCivilian()) {
				hasNonCivReserves = true;
				break;
			}
		}
		if (context.isEngagedInHostilities() &&
				!context.getDataFor(fleet).isWonLastEngagement() &&
				!hasNonCivReserves) {
			return true;
		}
		
		CampaignFleetAIAPI ai = fleet.getAI();
		if (ai == null) return false;
		return ai.pickEncounterOption(context, other) == EncounterOption.DISENGAGE;
	}

	public Object getContext() {
		return context;
	}

	public void updateMemory() {
		if (conversationDelegate != null) {
			conversationDelegate.updateMemory();
		}
	}
	
	public void notifyActivePersonChanged() {
		if (conversationDelegate != null) {
			conversationDelegate.notifyActivePersonChanged();
		}
	}

	public void setActiveMission(CampaignEventPlugin mission) {
		if (mission == null) {
			conversationDelegate.getMemoryMap().remove(MemKeys.MISSION);
		} else {
			MemoryAPI memory = mission.getMemory();
			if (memory != null) {
				conversationDelegate.getMemoryMap().put(MemKeys.MISSION, memory);
			} else {
				conversationDelegate.getMemoryMap().remove(MemKeys.MISSION);
			}
		}
	}
}



