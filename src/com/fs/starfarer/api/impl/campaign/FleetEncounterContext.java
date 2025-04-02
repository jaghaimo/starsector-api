package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.campaign.CombatDamageData.DamageToFleetMember;
import com.fs.starfarer.api.campaign.CombatDamageData.DealtByFleetMember;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide.OfficerEngagementData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.PursuitOption;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.PromoteOfficerIntel;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FleetEncounterContext implements FleetEncounterContextPlugin {
	
	protected List<DataForEncounterSide> sideData = new ArrayList<DataForEncounterSide>();
	protected boolean engagedInHostilities = false;
	protected boolean engagedInActualBattle = false;
	protected boolean playerOnlyRetreated = true;
	protected boolean playerPursued = false;
	protected boolean playerDidSeriousDamage = false;
	protected BattleAPI battle;
	protected boolean otherFleetHarriedPlayer = false;
	protected boolean ongoingBattle = false;
	
	protected boolean isAutoresolve = false;
	
	
	protected CombatDamageData runningDamageTotal = null;
	
	protected Map<FleetMemberAPI, CampaignFleetAPI> origSourceForRecoveredShips = new LinkedHashMap<>();
	
	public FleetEncounterContext() {
		
	}
	public boolean isAutoresolve() {
		return isAutoresolve;
	}

	public void setAutoresolve(boolean isAutoresolve) {
		this.isAutoresolve = isAutoresolve;
	}


	public BattleAPI getBattle() {
		return battle;
	}

	public void setBattle(BattleAPI battle) {
		this.battle = battle;
	}

	public DataForEncounterSide getDataFor(CampaignFleetAPI participantOrCombined) {
		CampaignFleetAPI combined = battle.getCombinedFor(participantOrCombined);
		if (combined == null) {
			return new DataForEncounterSide(participantOrCombined);
		}
		
		for (DataForEncounterSide curr : sideData) {
			if (curr.getFleet() == combined) return curr;
		}
		DataForEncounterSide dfes = new DataForEncounterSide(combined);
		sideData.add(dfes);
		
		return dfes;
	}
	
	public DataForEncounterSide getWinnerData() {
		for (DataForEncounterSide curr : sideData) {
			if (!curr.disengaged()) {
				return curr;
			}
		}
		return null;
	}
	
	public DataForEncounterSide getLoserData() {
		for (DataForEncounterSide curr : sideData) {
			if (curr.disengaged()) {
				return curr;
			}
		}
		return null;
	}
	
	public boolean isEngagedInHostilities() {
		return engagedInHostilities;
	}
	
	public void setEngagedInHostilities(boolean engagedInHostilities) {
		this.engagedInHostilities = engagedInHostilities;
	}
	
	public void setOtherFleetHarriedPlayer(boolean otherFleetHarriedPlayer) {
		this.otherFleetHarriedPlayer = otherFleetHarriedPlayer;
	}
	
	public boolean isOtherFleetHarriedPlayer() {
		return otherFleetHarriedPlayer;
	}

	protected void updateDeployedMap(EngagementResultForFleetAPI result) {
		DataForEncounterSide data = getDataFor(result.getFleet());
		data.getMemberToDeployedMap().clear();
		
		List<DeployedFleetMemberAPI> deployed = result.getAllEverDeployedCopy();
		if (deployed != null && !deployed.isEmpty()) {
			for (DeployedFleetMemberAPI dfm : deployed) {
				if (dfm.getMember() != null) {
					FleetMemberAPI member = dfm.getMember();
					data.getMemberToDeployedMap().put(member, dfm);
					
					if (dfm != null && dfm.getShip() != null && dfm.getShip().getOriginalCaptain() != null &&
							!dfm.getShip().getOriginalCaptain().isDefault()) {
						data.getMembersWithOfficerOrPlayerAsOrigCaptain().add(member);
					}
				}
			}
		}
	}
	
	/**
	 * There may be members with no source fleet if they were added to combat
	 * using scripts.
	 * @param result
	 */
	protected void clearNoSourceMembers(EngagementResultForFleetAPI result) {
		Iterator<FleetMemberAPI> iter = result.getDeployed().iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (battle.getSourceFleet(member) == null) {
				iter.remove();
			}
		}
		iter = result.getReserves().iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (battle.getSourceFleet(member) == null) {
				iter.remove();
			}
		}
		iter = result.getDestroyed().iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (battle.getSourceFleet(member) == null) {
				iter.remove();
			}
		}
		iter = result.getDisabled().iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (battle.getSourceFleet(member) == null) {
				iter.remove();
			}
		}
		iter = result.getRetreated().iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (battle.getSourceFleet(member) == null) {
				iter.remove();
			}
		}
	}

	public boolean isEngagedInActualBattle() {
		return engagedInActualBattle;
	}

	public void setEngagedInActualBattle(boolean engagedInActualBattle) {
		this.engagedInActualBattle = engagedInActualBattle;
	}

	public void processEngagementResults(EngagementResultAPI result) {
		engagedInHostilities = true;
		engagedInActualBattle = true; // whether autoresolve or not, there was actual fighting
		
		// the fleets we get back here are combined fleets from the BattleAPI,
		// NOT the actual fleets involved, even if it's a 1 vs 1 battle.
		EngagementResultForFleetAPI winnerResult = result.getWinnerResult();
		EngagementResultForFleetAPI loserResult = result.getLoserResult();

		clearNoSourceMembers(winnerResult);
		clearNoSourceMembers(loserResult);
		
		// only happens for combat where player is involved
		CombatDamageData currDamageData = result.getLastCombatDamageData();
		if (currDamageData != null) {
			if (runningDamageTotal == null) {
				runningDamageTotal = currDamageData;
			} else {
				runningDamageTotal.add(currDamageData);
			}
			computeFPHullDamage();
		}
		
		//if (winnerResult.getFleet().isPlayerFleet() || loserResult.getFleet().isPlayerFleet()) {
		if (battle.isPlayerInvolved()) {
			Global.getSector().reportPlayerEngagement(result);
		}
		
		updateDeployedMap(winnerResult);
		updateDeployedMap(loserResult);

		//result.applyToFleets();
		applyResultToFleets(result);

		//if (winnerResult.getFleet().isPlayerFleet() && winnerResult.getGoal() != FleetGoal.ESCAPE) {
		if (battle.isPlayerSide(winnerResult) && winnerResult.getGoal() != FleetGoal.ESCAPE) {
			playerOnlyRetreated = false;
			if (loserResult.getGoal() == FleetGoal.ESCAPE) {
				playerPursued = true;
			}
		//} else if (loserResult.getFleet().isPlayerFleet() && loserResult.getGoal() != FleetGoal.ESCAPE) {
		} else if (battle.isPlayerSide(loserResult) && loserResult.getGoal() != FleetGoal.ESCAPE) {
			playerOnlyRetreated = false;
			if (winnerResult.getGoal() == FleetGoal.ESCAPE) {
				playerPursued = true;
			}
		}
		
		DataForEncounterSide winnerData = getDataFor(winnerResult.getFleet());
		DataForEncounterSide loserData = getDataFor(loserResult.getFleet());
		
		winnerData.setWonLastEngagement(true);
		winnerData.setEnemyCanCleanDisengage(winnerResult.enemyCanCleanDisengage());
		//winnerData.setFleetCanCleanDisengage(loserResult.enemyCanCleanDisengage());
		loserData.setWonLastEngagement(false);
		loserData.setEnemyCanCleanDisengage(loserResult.enemyCanCleanDisengage());
		//loserData.setFleetCanCleanDisengage(winnerResult.enemyCanCleanDisengage());
		
		winnerData.setDidEnoughToDisengage(true);
		float damageInFP = 0f;
		for (FleetMemberAPI member : winnerResult.getDisabled()) {
			damageInFP += member.getFleetPointCost();
		}
		for (FleetMemberAPI member : winnerResult.getDestroyed()) {
			damageInFP += member.getFleetPointCost();
		}
		for (FleetMemberAPI member : winnerResult.getRetreated()) {
			damageInFP += member.getFleetPointCost();
		}
		
//		float remaining = 0f;
//		for (FleetMemberAPI member : winnerResult.getFleet().getFleetData().getCombatReadyMembersListCopy()) {
//			remaining += member.getFleetPointCost();
//		}
//		loserData.setDidEnoughToDisengage(damageInFP >= remaining);
		loserData.setDidEnoughToDisengage(winnerResult.enemyCanCleanDisengage());
		
		
		winnerData.setLastGoal(winnerResult.getGoal());
		loserData.setLastGoal(loserResult.getGoal());
		
		winnerData.getDeployedInLastEngagement().clear();
		winnerData.getRetreatedFromLastEngagement().clear();
		winnerData.getInReserveDuringLastEngagement().clear();
		winnerData.getDisabledInLastEngagement().clear();
		winnerData.getDestroyedInLastEngagement().clear();
		winnerData.getDeployedInLastEngagement().addAll(winnerResult.getDeployed());
		winnerData.getRetreatedFromLastEngagement().addAll(winnerResult.getRetreated());
		winnerData.getInReserveDuringLastEngagement().addAll(winnerResult.getReserves());
		winnerData.getDisabledInLastEngagement().addAll(winnerResult.getDisabled());
		winnerData.getDestroyedInLastEngagement().addAll(winnerResult.getDestroyed());
		
		loserData.getDeployedInLastEngagement().clear();
		loserData.getRetreatedFromLastEngagement().clear();
		loserData.getInReserveDuringLastEngagement().clear();
		loserData.getDisabledInLastEngagement().clear();
		loserData.getDestroyedInLastEngagement().clear();
		loserData.getDeployedInLastEngagement().addAll(loserResult.getDeployed());
		loserData.getRetreatedFromLastEngagement().addAll(loserResult.getRetreated());
		loserData.getInReserveDuringLastEngagement().addAll(loserResult.getReserves());
		loserData.getDisabledInLastEngagement().addAll(loserResult.getDisabled());
		loserData.getDestroyedInLastEngagement().addAll(loserResult.getDestroyed());
		
		for (FleetMemberAPI member : loserResult.getDestroyed()) {
			loserData.addOwn(member, Status.DESTROYED);
		}
		
		for (FleetMemberAPI member : loserResult.getDisabled()) {
			loserData.addOwn(member, Status.DISABLED);
		}
		
		for (FleetMemberAPI member : winnerResult.getDestroyed()) {
			winnerData.addOwn(member, Status.DESTROYED);
		}
		
		for (FleetMemberAPI member : winnerResult.getDisabled()) {
			winnerData.addOwn(member, Status.DISABLED);
		}
		
		//if (winnerResult.getFleet().isPlayerFleet()) {
		if (result.getWinnerResult().getAllEverDeployedCopy() != null) {
			tallyOfficerTime(winnerData, winnerResult);
		}
		//} else if (loserResult.getFleet().isPlayerFleet()) {
		if (result.getLoserResult().getAllEverDeployedCopy() != null) {
			tallyOfficerTime(loserData, loserResult);
		}
		
		// important, so that in-combat Ship objects can be garbage collected.
		// Probably some combat engine references in there, too. 
		winnerResult.resetAllEverDeployed();
		getDataFor(winnerResult.getFleet()).getMemberToDeployedMap().clear();
		loserResult.resetAllEverDeployed();
		getDataFor(loserResult.getFleet()).getMemberToDeployedMap().clear();
		
		
		// moved from applyPostEngagementResult
		for (FleetMemberAPI member : winnerResult.getDestroyed()) {
			loserData.addEnemy(member, Status.DESTROYED);
		}
		for (FleetMemberAPI member : winnerResult.getDisabled()) {
			loserData.addEnemy(member, Status.DISABLED);
		}
		
		for (FleetMemberAPI member : loserResult.getDestroyed()) {
			winnerData.addEnemy(member, Status.DESTROYED);
		}
		for (FleetMemberAPI member : loserResult.getDisabled()) {
			winnerData.addEnemy(member, Status.DISABLED);
		}
		
		
		FleetGoal winnerGoal = winnerResult.getGoal();
		FleetGoal loserGoal = loserResult.getGoal();
		boolean totalWin = loserData.getFleet().getFleetData().getMembersListCopy().isEmpty();
		boolean playerOut = result.isPlayerOutBeforeEnd();
		
		if (playerOut) {
			FleetGoal playerGoal = null;
			FleetGoal otherGoal = null;
			if (battle.isPlayerSide(battle.getSideFor(winnerResult.getFleet()))) {
				playerGoal = winnerGoal;
				otherGoal = loserGoal;
			} else {
				playerGoal = loserGoal;
				otherGoal = winnerGoal;
			}
			if (playerGoal == FleetGoal.ATTACK) {
				if (otherGoal == FleetGoal.ATTACK) {
					if (winnerResult.isPlayer()) {
						lastOutcome = EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN;
					} else {
						lastOutcome = EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS;
					}
				} else {
					if (winnerResult.isPlayer()) {
						lastOutcome = EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_WIN;
					} else {
						lastOutcome = EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_LOSS;
					}
				}
			} else {
				if (winnerResult.isPlayer()) {
					lastOutcome = EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN;
				} else {
					lastOutcome = EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_LOSS;
				}
			}
		} else {
			if (totalWin && winnerData.getFleet().getFleetData().getMembersListCopy().isEmpty()) {
				lastOutcome = EngagementOutcome.MUTUAL_DESTRUCTION;
			} else {
				if (battle.isPlayerSide(battle.getSideFor(winnerResult.getFleet()))) {
					if (winnerGoal == FleetGoal.ATTACK && loserGoal == FleetGoal.ATTACK) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.BATTLE_PLAYER_WIN_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.BATTLE_PLAYER_WIN;
						}
					} else if (winnerGoal == FleetGoal.ESCAPE) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.ESCAPE_PLAYER_WIN_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.ESCAPE_PLAYER_WIN;
						}
					} else if (loserGoal == FleetGoal.ESCAPE) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.ESCAPE_ENEMY_LOSS_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.ESCAPE_ENEMY_SUCCESS;
						}
					}
				} else {
					if (winnerGoal == FleetGoal.ATTACK && loserGoal == FleetGoal.ATTACK) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.BATTLE_ENEMY_WIN_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.BATTLE_ENEMY_WIN;
						}
					} else if (winnerGoal == FleetGoal.ESCAPE) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.ESCAPE_ENEMY_WIN_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.ESCAPE_ENEMY_WIN;
						}
					} else if (loserGoal == FleetGoal.ESCAPE) {
						if (totalWin) {
							lastOutcome = EngagementOutcome.ESCAPE_PLAYER_LOSS_TOTAL;
						} else {
							lastOutcome = EngagementOutcome.ESCAPE_PLAYER_SUCCESS;
						}
					}
				}
			}
		}
		
		battle.uncombine();
		//battle.genCombinedDoNotRemoveEmpty();
		battle.genCombined();
	}
	
	protected void tallyOfficerTime(DataForEncounterSide data, EngagementResultForFleetAPI result) {
		float maxTime = 0f;
		for (DeployedFleetMemberAPI dfm : result.getAllEverDeployedCopy()) {
			float time = dfm.getShip().getFullTimeDeployed();
			
			if (time > maxTime) {
				maxTime = time;
			}
			
			time -= dfm.getShip().getTimeDeployedUnderPlayerControl();
			if (time <= 0) continue;
			
			PersonAPI person = dfm.getMember().getCaptain();
			CampaignFleetAPI source = battle.getSourceFleet(dfm.getMember());
			if (source == null) continue;
			
			//if (data.getFleet().getFleetData().getOfficerData(person) == null) {
			if (source.getFleetData().getOfficerData(person) == null) {
				OfficerEngagementData oed = data.getFleetMemberDeploymentData().get(dfm.getMember());
				if (oed == null) {
					oed = new OfficerEngagementData(source);
					oed.person = null;
					data.getFleetMemberDeploymentData().put(dfm.getMember(), oed);
				}
				time += dfm.getShip().getTimeDeployedUnderPlayerControl();
				oed.timeDeployed += time;
				continue; // not an officer
			}
			
			OfficerEngagementData oed = data.getOfficerData().get(person);
			if (oed == null) {
				oed = new OfficerEngagementData(source);
				oed.person = person;
				data.getOfficerData().put(person, oed);
			}
			oed.timeDeployed += time;
		}
		data.setMaxTimeDeployed(data.getMaxTimeDeployed() + maxTime);
	}
	
	public PursueAvailability getPursuitAvailability(CampaignFleetAPI fleet, CampaignFleetAPI otherFleet) {
		DataForEncounterSide otherData = getDataFor(otherFleet);
		
		if (otherData.isWonLastEngagement()) return PursueAvailability.LOST_LAST_ENGAGEMENT;
		if (canOutrunOtherFleet(otherFleet, fleet)) return PursueAvailability.TOO_SLOW;
		if (fleet.getFleetData().getCombatReadyMembersListCopy().isEmpty()) return PursueAvailability.NO_READY_SHIPS;
		if (otherData.isDidEnoughToDisengage()) return PursueAvailability.TOOK_SERIOUS_LOSSES;
		return PursueAvailability.AVAILABLE;
	}
	
	public DisengageHarryAvailability getDisengageHarryAvailability(CampaignFleetAPI fleet, CampaignFleetAPI otherFleet) {
		DataForEncounterSide otherData = getDataFor(otherFleet);
		if (otherData.isWonLastEngagement()) return DisengageHarryAvailability.LOST_LAST_ENGAGEMENT;
		if (fleet.getFleetData().getCombatReadyMembersListCopy().isEmpty()) return DisengageHarryAvailability.NO_READY_SHIPS;
		//if (otherData.isDidEnoughToDisengage()) return DisengageHarryAvailability.LOST_LAST_ENGAGEMENT
		return DisengageHarryAvailability.AVAILABLE;
	}
	
	public float getDeployCost(FleetMemberAPI member) {
		return member.getDeployCost();
	}
	
	public boolean isLowRepImpact() {
		boolean lowImpact = getBattle() != null && getBattle().getNonPlayerSide() != null && 
							getBattle().getPrimary(getBattle().getNonPlayerSide()) != null &&
							getBattle().getPrimary(getBattle().getNonPlayerSide()).getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT) == true;
		return lowImpact;
	}
	public boolean isNoRepImpact() {
		boolean noImpact = getBattle() != null && getBattle().getNonPlayerSide() != null && 
						   getBattle().getPrimary(getBattle().getNonPlayerSide()) != null &&
						   getBattle().getPrimary(getBattle().getNonPlayerSide()).getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_REP_IMPACT) == true;
		return noImpact;
	}
	
	protected boolean alreadyAdjustedRep = false;
	public boolean adjustPlayerReputation(InteractionDialogAPI dialog, String ffText) {
		return adjustPlayerReputation(dialog, ffText, true, true);
	}
	public boolean adjustPlayerReputation(InteractionDialogAPI dialog, String ffText, boolean okToAdjustAlly, boolean okToAdjustEnemy) {
		if (alreadyAdjustedRep) return false;
		
		if (battle != null && battle.isPlayerInvolved() && engagedInHostilities) {
			alreadyAdjustedRep = true;
			
			boolean printedAdjustmentText = false;
			
			boolean playerWon = didPlayerWinMostRecentBattleOfEncounter();
			List<CampaignFleetAPI> playerSide = battle.getPlayerSide();
			List<CampaignFleetAPI> enemySide = battle.getNonPlayerSide();
			
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			pf.setMoveDestination(pf.getLocation().x, pf.getLocation().y);
			
			// cases to cover: 1) player destroyed ships 2) player harried/harassed 3) player pursued
			// i.e. anything other than a non-destructive retreat, w/o an engagement
			boolean playerWasAggressive = playerDidSeriousDamage || !playerOnlyRetreated;
			RepActions action = null;
//			if (engagedInHostilities) {
//				action = RepActions.COMBAT_NO_DAMAGE_ESCAPE;
//			}
			boolean knowsWhoPlayerIs = battle.knowsWhoPlayerIs(enemySide);
			//boolean lowImpact = battle.getNonPlayerCombined().getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT) == true;
			boolean lowImpact = isLowRepImpact();
			if (lowImpact) {
				for (CampaignFleetAPI enemy : battle.getSnapshotFor(enemySide)) {
					Misc.makeLowRepImpact(enemy, "battleOnLowImpactSide");
				}
			}
			if (playerPursued && playerWon) {
				if (knowsWhoPlayerIs && !lowImpact) {
					action = RepActions.COMBAT_AGGRESSIVE;
				} else {
					action = RepActions.COMBAT_AGGRESSIVE_TOFF;
				}
			} else if (playerWasAggressive) {
				if (knowsWhoPlayerIs && !lowImpact) {
					action = RepActions.COMBAT_NORMAL;
				} else {
					action = RepActions.COMBAT_NORMAL_TOFF;
				}
			}
			
			if (isNoRepImpact()) {
				action = null;
			}
			
			if (!okToAdjustEnemy) action = null;
			
			Set<String> seen = new HashSet<String>();
			if (action != null) {
				// use snapshot: ensure loss of reputation with factions if their fleets were destroyed
				for (CampaignFleetAPI enemy : battle.getSnapshotFor(enemySide)) {
					String factionId = enemy.getFaction().getId();
					if (seen.contains(factionId)) continue;
					seen.add(factionId);
					Global.getSector().adjustPlayerReputation(new RepActionEnvelope(action, null, dialog.getTextPanel()), factionId);
					printedAdjustmentText = true;
				}
			}
			
			//if (playerWon) {
				action = RepActions.COMBAT_HELP_MINOR;
				float playerFP = 0;
				float allyFP = 0;
				float enemyFP = 0;
				for (CampaignFleetAPI fleet : battle.getSnapshotFor(playerSide)) {
					for (FleetMemberAPI member : fleet.getFleetData().getSnapshot()) {
						if (fleet.isPlayerFleet()) {
							playerFP += member.getFleetPointCost();
						} else {
							allyFP += member.getFleetPointCost();
						}
					}
				}
				for (CampaignFleetAPI fleet : battle.getSnapshotFor(enemySide)) {
					for (FleetMemberAPI member : fleet.getFleetData().getSnapshot()) {
						enemyFP += member.getFleetPointCost();
					}
				}
				if (allyFP > enemyFP || !playerWon) {
					action = RepActions.COMBAT_HELP_MINOR;
				} else if (allyFP < enemyFP * 0.5f) {
					action = RepActions.COMBAT_HELP_CRITICAL;
				} else {
					action = RepActions.COMBAT_HELP_MAJOR;
				}
				
//				if (playerFPHullDamageToEnemies <= 0) {
//					action = null;
//				} else if (playerFPHullDamageToEnemies < allyFPHullDamageToEnemies * 0.1f) {
//					action = RepActions.COMBAT_HELP_MINOR;
//				}
				float f = computePlayerContribFraction();
				if (f <= 0) {
					action = null;
				} else if (f < 0.1f) {
					action = RepActions.COMBAT_HELP_MINOR;
				}
				
				if (action != null) {
					float totalDam = allyFPHullDamageToEnemies + playerFPHullDamageToEnemies;
					if (totalDam < 10) {
						action = RepActions.COMBAT_HELP_MINOR;
					} else if (totalDam < 20 && action == RepActions.COMBAT_HELP_CRITICAL) {
						action = RepActions.COMBAT_HELP_MAJOR;
					}
				}
				
				if (battle.isPlayerInvolvedAtStart() && action != null) {
					//action = RepActions.COMBAT_HELP_MINOR;
					action = null;
				}
				
				if (!okToAdjustAlly) action = null;
//				if (leavingEarly) {
//					action = null;
//				}
				
				// rep increases
				seen.clear();
				for (CampaignFleetAPI ally : battle.getSnapshotFor(playerSide)) {
					if (ally.isPlayerFleet()) continue;
					
					String factionId = ally.getFaction().getId();
					if (seen.contains(factionId)) continue;
					seen.add(factionId);
					
					Float friendlyFPHull = playerFPHullDamageToAlliesByFaction.get(ally.getFaction());
					float threshold = 2f;
					if (action == RepActions.COMBAT_HELP_MAJOR) {
						threshold = 5f;
					} else if (action == RepActions.COMBAT_HELP_CRITICAL) {
						threshold = 10f;
					}
					if (friendlyFPHull != null && friendlyFPHull > threshold) {
						// can lose reputation with sides that didn't survive
						//Global.getSector().adjustPlayerReputation(new RepActionEnvelope(RepActions.COMBAT_FRIENDLY_FIRE, (friendlyFPHull - threshold), dialog.getTextPanel()), factionId);
					} else if (action != null && playerSide.contains(ally)) {
						// only gain reputation with factions whose fleets actually survived
						Global.getSector().adjustPlayerReputation(new RepActionEnvelope(action, null, dialog.getTextPanel()), factionId);
						printedAdjustmentText = true;
					}
				}
				
				
				// friendly fire rep decreases
				if (okToAdjustAlly) {
					boolean first = true;
					seen.clear();
					for (CampaignFleetAPI ally : battle.getSnapshotFor(playerSide)) {
						if (ally.isPlayerFleet()) continue;
						
						String factionId = ally.getFaction().getId();
						if (Factions.PLAYER.equals(factionId)) continue;
						if (seen.contains(factionId)) continue;
						seen.add(factionId);
						
						Float friendlyFPHull = playerFPHullDamageToAlliesByFaction.get(ally.getFaction());
						float threshold = 2f;
						if (action == RepActions.COMBAT_HELP_MAJOR) {
							threshold = 5f;
						} else if (action == RepActions.COMBAT_HELP_CRITICAL) {
							threshold = 10f;
						}
						if (friendlyFPHull != null && friendlyFPHull > threshold) {
							if (first && ffText != null) {
								first = false;
								dialog.getTextPanel().addParagraph(ffText);
							}
							// can lose reputation with sides that didn't survive
							Global.getSector().adjustPlayerReputation(new RepActionEnvelope(RepActions.COMBAT_FRIENDLY_FIRE, (friendlyFPHull - threshold), dialog.getTextPanel()), factionId);
							printedAdjustmentText = true;
						} else if (action != null && playerSide.contains(ally)) {
							// only gain reputation with factions whose fleets actually survived
							//Global.getSector().adjustPlayerReputation(new RepActionEnvelope(action, null, dialog.getTextPanel()), factionId);
						}
					}
				}
			//}
			return printedAdjustmentText;
		}
		
		return false;
	}
	
	
	public TextPanelAPI textPanelForXPGain = null;
	public TextPanelAPI getTextPanelForXPGain() {
		return textPanelForXPGain;
	}

	public void setTextPanelForXPGain(TextPanelAPI textPanelForXPGain) {
		this.textPanelForXPGain = textPanelForXPGain;
	}

	protected boolean noHarryBecauseOfStation = false;
	public boolean isNoHarryBecauseOfStation() {
		return noHarryBecauseOfStation;
	}
	public void setNoHarryBecauseOfStation(boolean noHarryBecauseOfStation) {
		this.noHarryBecauseOfStation = noHarryBecauseOfStation;
	}
	
	public void applyAfterBattleEffectsIfThereWasABattle() {
		if (!hasWinnerAndLoser() || !engagedInHostilities) {
			for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
				member.getStatus().resetAmmoState();
			}
			
			if (noHarryBecauseOfStation && battle != null) {
				List<CampaignFleetAPI> otherSide = battle.getNonPlayerSide();
				CampaignFleetAPI fleet = battle.getPrimary(otherSide);
				if (fleet.getAI() != null && 
						!fleet.getAI().isCurrentAssignment(FleetAssignment.STANDING_DOWN)) {
					fleet.getAI().addAssignmentAtStart(FleetAssignment.STANDING_DOWN, fleet, 0.5f + 0.5f * (float) Math.random(), null);
				}
			}
			
			//Global.getSector().setLastPlayerBattleTimestamp(Global.getSector().getClock().getTimestamp());
			Global.getSector().getPlayerFleet().setNoEngaging(3f);
			return;
		}
		
		gainXP();
		addPotentialOfficer();
		
//		CampaignFleetAPI winner = getWinnerData().getFleet();
//		CampaignFleetAPI loser = getLoserData().getFleet();
//		List<CampaignFleetAPI> winners = battle.getSideFor(getWinnerData().getFleet());
//		List<CampaignFleetAPI> losers = battle.getSideFor(getLoserData().getFleet());
		List<CampaignFleetAPI> winners = battle.getSnapshotSideFor(getWinnerData().getFleet());
		List<CampaignFleetAPI> losers = battle.getSnapshotSideFor(getLoserData().getFleet());
		if (winners == null || losers == null) return;
		
		for (CampaignFleetAPI loser : losers) {
			for (FleetMemberAPI member : loser.getFleetData().getMembersListCopy()) {
				member.getStatus().resetAmmoState();
			}
			
			loser.getVelocity().set(0, 0);
			if (loser.isPlayerFleet()) continue;
			if (loser.isPlayerFleet()) loser.setNoEngaging(3f);
			
		}
		for (CampaignFleetAPI winner : winners) {
			for (FleetMemberAPI member : winner.getFleetData().getMembersListCopy()) {
				member.getStatus().resetAmmoState();
			}
			
			winner.getVelocity().set(0, 0);
			if (winner.isPlayerFleet()) continue;
			if (winner.isPlayerFleet()) winner.setNoEngaging(3f);
			
		}
		
		if (battle.isPlayerSide(winners)) {
			for (CampaignFleetAPI fleet : battle.getPlayerSide()) {
				if (fleet.isPlayerFleet()) continue;
				
				Misc.forgetAboutTransponder(fleet);
			}
		}
		
		battle.setPlayerInvolvementFraction(computePlayerContribFraction());
		
		if (!isAutoresolve && engagedInActualBattle) {
			Global.getSector().reportBattleOccurred(battle.getPrimary(winners), battle);
			Global.getSector().reportBattleFinished(battle.getPrimary(winners), battle);
		}
		
		CampaignFleetAPI largestWinner = battle.getPrimary(winners);
		for (CampaignFleetAPI loser : losers) {
			if (loser.getFleetData().getMembersListCopy().isEmpty()) {
				//Global.getSector().reportFleetDewspawned(loser, FleetDespawnReason.DESTROYED_BY_FLEET, winner);
				loser.despawn(FleetDespawnReason.DESTROYED_BY_BATTLE, battle);
			}
		}
		
		for (CampaignFleetAPI winner : winners) {
			if (winner.getFleetData().getMembersListCopy().isEmpty()) {
				//Global.getSector().reportFleetDewspawned(loser, FleetDespawnReason.DESTROYED_BY_FLEET, winner);
				winner.despawn(FleetDespawnReason.DESTROYED_BY_BATTLE, battle);
			}
		}
		
		for (CampaignFleetAPI enemy : battle.getBothSides()) {
			if (enemy.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI mAI = (ModularFleetAIAPI) enemy.getAI();
				mAI.getTacticalModule().forceTargetReEval();
			}
		}

	}
	
	
	
	
	public float performPostVictoryRecovery(EngagementResultAPI result) {
		EngagementResultForFleetAPI winnerResult = result.getWinnerResult();
		EngagementResultForFleetAPI loserResult = result.getLoserResult();
		return performPostVictoryRecovery(winnerResult, loserResult);
	}
	public float performPostEngagementRecoveryBoth(EngagementResultAPI result) {
		EngagementResultForFleetAPI winnerResult = result.getWinnerResult();
		EngagementResultForFleetAPI loserResult = result.getLoserResult();
		float f = performPostVictoryRecovery(winnerResult, loserResult);
		f += performPostVictoryRecovery(loserResult, winnerResult);
		f /= 2f;
		return f;
	}
		
		
	public float performPostVictoryRecovery(EngagementResultForFleetAPI winnerResult, EngagementResultForFleetAPI loserResult) {
		DataForEncounterSide winnerData = getDataFor(winnerResult.getFleet());
		DataForEncounterSide loserData = getDataFor(loserResult.getFleet());
		
		//float totalFpUsed = 0f;
		float loserDepDestroyed = 0f;
		float loserDepLeft = 0f;
		
		for (FleetMemberAPI member : loserData.getRetreatedFromLastEngagement()) {
			loserDepLeft += member.getDeploymentPointsCost();
		}
		for (FleetMemberAPI member : loserData.getInReserveDuringLastEngagement()) {
			loserDepLeft += member.getDeploymentPointsCost();
		}
		
		for (FleetMemberAPI member : loserData.getDestroyedInLastEngagement()) {
			loserDepDestroyed += member.getDeploymentPointsCost();
		}
		for (FleetMemberAPI member : loserData.getDisabledInLastEngagement()) {
			loserDepDestroyed += member.getDeploymentPointsCost();
		}
		for (FleetMemberAPI member : loserData.getRetreatedFromLastEngagement()) {
			if (member.isFighterWing()) {
				DeployedFleetMemberAPI dfm = getDataFor(loserData.getFleet()).getMemberToDeployedMap().get(member);
				if (dfm != null && dfm.getMember() == member) {
					float deploymentCR = dfm.getShip().getWingCRAtDeployment();
					float finalCR = deploymentCR;
					//float finalCR = dfm.getShip().getRemainingWingCR();
					if (deploymentCR > finalCR) {
						float crPer = dfm.getMember().getStats().getCRPerDeploymentPercent().computeEffective(dfm.getMember().getVariant().getHullSpec().getCRToDeploy()) / 100f;
						float extraCraftLost = (deploymentCR - finalCR) / crPer;
						float wingSize = dfm.getMember().getNumFightersInWing();
						if (extraCraftLost >= 1) {
							loserDepDestroyed += Math.min(1f, extraCraftLost / wingSize) * member.getDeploymentPointsCost();
						}
					}
				}
			}
		}

		float totalRecovery = 0f;
		float count = 0f;
		for (FleetMemberAPI member : winnerData.getDeployedInLastEngagement()) {
			float dp = member.getDeploymentPointsCost();
			float recoveryFraction = Math.max(0, (dp * 1.25f - loserDepDestroyed)) / dp;
//			if (member.getFleetData() != null && member.getFleetData().getFleet() != null &&
//					member.getFleetData().getFleet().isPlayerFleet()) {
			if (loserDepDestroyed > loserDepLeft * 2f) {
				recoveryFraction = Math.max(0, (dp * 0.75f - loserDepDestroyed)) / dp; 
			}
			if (recoveryFraction > 1f) recoveryFraction = 1f;
			if (loserDepDestroyed <= 0) recoveryFraction = 1f;
			
			float deployCost = getDeployCost(member);
			if (preEngagementCRForWinner.containsKey(member)) {
				float prevCR = preEngagementCRForWinner.get(member);
				if (prevCR < deployCost) {
					prevCR = deployCost;
				}
			}
			
			float recoveryAmount = Math.round(deployCost * recoveryFraction * 100f) / 100f;
			
			totalRecovery += recoveryAmount;
			count++;
			
			if (recoveryAmount <= 0) continue;
			
			
			member.getRepairTracker().applyCREvent(recoveryAmount, "Post engagement recovery");
		}

		if (count <= 0) return 0;
		
		return Math.round(totalRecovery / count * 100f) / 100f;
	}
	
	
	
	public void applyPursuitOption(CampaignFleetAPI pursuingFleet, CampaignFleetAPI otherFleet, PursuitOption pursuitOption) {
		
		if (Misc.isPlayerOrCombinedPlayerPrimary(pursuingFleet) && pursuitOption != PursuitOption.LET_THEM_GO) {
			playerOnlyRetreated = false;
		}
		
		DataForEncounterSide pursuer = getDataFor(pursuingFleet);
		DataForEncounterSide other = getDataFor(otherFleet);
		
		if (pursuitOption == PursuitOption.HARRY) {
			for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
				float deployCost = getDeployCost(member);
				
				float harryCost = deployCost * 1f;
				member.getRepairTracker().applyCREvent(-harryCost, "harried while disengaging");
			}
		}
	}	
	
	
	
	protected EngagementOutcome lastOutcome = null;
	public EngagementOutcome getLastEngagementOutcome() {
		return lastOutcome;
	}
	
	public boolean isBattleOver() {
		if (hasWinnerAndLoser()) return true;
		
		return lastOutcome != null &&
			   lastOutcome != EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN &&
			   lastOutcome != EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS &&
			   lastOutcome != EngagementOutcome.BATTLE_ENEMY_WIN &&
			   lastOutcome != EngagementOutcome.BATTLE_PLAYER_WIN;
	}
	
	public boolean wasLastEngagementEscape() {
		return lastOutcome != null &&
//			   lastOutcome != EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN &&
//			   lastOutcome != EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_LOSS &
		   	   lastOutcome != EngagementOutcome.BATTLE_ENEMY_WIN &&
		       lastOutcome != EngagementOutcome.BATTLE_PLAYER_WIN;
	}
	
	public boolean didPlayerWinLastEngagement() {
		return lastOutcome == EngagementOutcome.BATTLE_PLAYER_WIN ||
		   	   lastOutcome == EngagementOutcome.BATTLE_PLAYER_WIN_TOTAL ||
		   	   lastOutcome == EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN ||
		   	   lastOutcome == EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_WIN ||
		   	   lastOutcome == EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN ||
		   	   lastOutcome == EngagementOutcome.ESCAPE_ENEMY_LOSS_TOTAL ||
		   	   lastOutcome == EngagementOutcome.ESCAPE_ENEMY_SUCCESS ||
		   	   lastOutcome == EngagementOutcome.ESCAPE_PLAYER_WIN ||
		   	   lastOutcome == EngagementOutcome.ESCAPE_PLAYER_WIN_TOTAL;
	}
	
	/**
	 * The difference from didPlayerWinEncounterOutright() is that the opposing fleet may
	 * still choose to re-engage.
	 * @return
	 */
	public boolean didPlayerWinMostRecentBattleOfEncounter() {
		if (getDataFor(Global.getSector().getPlayerFleet()).disengaged()) return false;

		return didPlayerWinEncounterOutright() || lastOutcome == EngagementOutcome.BATTLE_PLAYER_WIN;
	}
	
	/**
	 * Player won, and it's over - no more fighting is *possible* in this encounter.
	 * @return
	 */
	public boolean didPlayerWinEncounterOutright() {
		if (getDataFor(Global.getSector().getPlayerFleet()).disengaged()) return false;
		
		// non-fighting "win", i.e. harrying a weaker enemy
		//if (lastOutcome == null && getWinner() == Global.getSector().getPlayerFleet()) {
		//if (lastOutcome == null && battle.isPlayerSide(battle.getSideFor(getWinner()))) { 
		if ((lastOutcome == null || hasWinnerAndLoser()) && battle.isPlayerSide(battle.getSideFor(getWinner()))) { 
			return true;
		}
		
		return lastOutcome == EngagementOutcome.BATTLE_PLAYER_WIN_TOTAL ||
				//lastOutcome == EngagementOutcome.BATTLE_PLAYER_WIN ||
				lastOutcome == EngagementOutcome.ESCAPE_ENEMY_LOSS_TOTAL ||
				lastOutcome == EngagementOutcome.ESCAPE_ENEMY_SUCCESS ||
				lastOutcome == EngagementOutcome.ESCAPE_PLAYER_WIN ||
				lastOutcome == EngagementOutcome.ESCAPE_PLAYER_WIN_TOTAL;		
	}
	
	
	public int getCreditsLooted() {
		return creditsLooted;
	}
	
	public float getSalvageMult(Status status) {
		float mult = 1f;
		switch (status) {
		case DESTROYED:
			//mult = 0.5f;
			mult = 1f;
			break;
		case DISABLED:
			mult = 1f;
			break;
		case REPAIRED:
			mult = 1f;
			break;
		case CAPTURED:
			mult = 0.1f;
			break;
		}
		return mult;
	}
	
	public float getCargoLootMult(Status status) {
		float mult = 1f;
		switch (status) {
		case DESTROYED:
			mult = 1f;
			break;
		case DISABLED:
			mult = 1f;
			break;
		case REPAIRED:
			mult = 1f;
			break;
		case CAPTURED:
			mult = 1f;
			break;
		}
		return mult;
	}
	
//	public List<FleetMemberAPI> repairShips() {
//		DataForEncounterSide winner = getWinnerData();
//		return repairShips(winner);
//		
////		DataForEncounterSide loser = getLoserData();
////		repairShips(loser);
//	}
	
	public static enum EngageBoardableOutcome {
		ESCAPED,
		DISABLED,
		DESTROYED,
	}
	
	public EngageBoardableOutcome engageBoardableShip(FleetMemberAPI toBoard, 
								CampaignFleetAPI fleetItBelongsTo,
								CampaignFleetAPI attackingFleet) {
		float r = (float) Math.random();
//		if (r < ENGAGE_ESCAPE_CHANCE && !Misc.isPlayerOrCombinedContainingPlayer(attackingFleet)) {
//			// escaped
//			CampaignFleetAPI fleet = getBattle().getSourceFleet(toBoard);
//			letBoardableGo(toBoard, fleet, attackingFleet);
//			
//			return EngageBoardableOutcome.ESCAPED;
//		} else
		if (r < ENGAGE_ESCAPE_CHANCE + ENGAGE_DISABLE_CHANCE) {
			// disabled
			DataForEncounterSide attackerSide = getDataFor(attackingFleet);
			attackerSide.changeEnemy(toBoard, Status.DISABLED);
			toBoard.getStatus().disable();
			return EngageBoardableOutcome.DISABLED;
		} else {
			DataForEncounterSide attackerSide = getDataFor(attackingFleet);
			attackerSide.changeEnemy(toBoard, Status.DESTROYED);
			toBoard.getStatus().disable();
			return EngageBoardableOutcome.DESTROYED;
		}
	}
	
	
	public static enum BoardingAttackType {
		SHIP_TO_SHIP,
		LAUNCH_FROM_DISTANCE,
	}
	public static enum BoardingOutcome {
		SUCCESS,
		SELF_DESTRUCT,
		SUCCESS_TOO_DAMAGED,
		SHIP_ESCAPED,
		SHIP_ESCAPED_CLEAN,
	}
	
	public static class BoardingResult {
		private BoardingOutcome outcome;
		private CrewCompositionAPI attackerLosses = Global.getFactory().createCrewComposition();
		private CrewCompositionAPI defenderLosses = Global.getFactory().createCrewComposition();
		private FleetMemberAPI member;
		private List<FleetMemberAPI> lostInSelfDestruct = new ArrayList<FleetMemberAPI>();
		
		public BoardingOutcome getOutcome() {
			return outcome;
		}
		public List<FleetMemberAPI> getLostInSelfDestruct() {
			return lostInSelfDestruct;
		}
		public void setOutcome(BoardingOutcome outcome) {
			this.outcome = outcome;
		}
		public CrewCompositionAPI getAttackerLosses() {
			return attackerLosses;
		}
		public void setAttackerLosses(CrewCompositionAPI attackerLosses) {
			this.attackerLosses = attackerLosses;
		}
		public CrewCompositionAPI getDefenderLosses() {
			return defenderLosses;
		}
		public void setDefenderLosses(CrewCompositionAPI defenderLosses) {
			this.defenderLosses = defenderLosses;
		}
		public FleetMemberAPI getMember() {
			return member;
		}
		public void setMember(FleetMemberAPI member) {
			this.member = member;
		}
		
	}
	
	public static final float SELF_DESTRUCT_CHANCE = 0.25f;
	public static final float CIV_SELF_DESTRUCT_CHANCE = 0.05f;
	
	public static final float ENGAGE_ESCAPE_CHANCE = 0.25f;
	public static final float ENGAGE_DISABLE_CHANCE = 0.5f;
	public static final float ENGAGE_DESTROY_CHANCE = 0.25f;
	
	public static final float LAUNCH_CLEAN_ESCAPE_CHANCE = 0.5f;
	public static final float DOCK_SUCCESS_CHANCE = 0.5f;
	public static final float LAUNCH_SUCCESS_CHANCE = 0.25f;
	
	//public static final float DEFENDER_BONUS = 4f;
	//public static final float DEFENDER_VS_LAUNCH_BONUS = 3f;
	
	public BoardingResult boardShip(FleetMemberAPI member, CampaignFleetAPI attacker, CampaignFleetAPI defender) {
		
		
		DataForEncounterSide attackerSide = getDataFor(attacker);
		DataForEncounterSide defenderSide = getDataFor(defender);
		
		float attackerMarineMult = attacker.getCommanderStats().getMarineEffectivnessMult().getModifiedValue();
		float defenderMarineMult = defender.getCommanderStats().getMarineEffectivnessMult().getModifiedValue();
		
		float crewMult = 2f;
		float marineMult = 7f;
		
		
		float attackerStr = attacker.getCargo().getMarines() * marineMult;
		attackerStr *= attackerMarineMult;
		
		CrewCompositionAPI defenderCrew = member.getCrewComposition();
		float defenderStr = defenderCrew.getCrew() * crewMult + defenderCrew.getMarines() * marineMult;
		defenderStr *= defenderMarineMult;

		//defenderStr *= Global.getSettings().getFloat("boardingDifficulty");
		
		Random rand = new Random(1300000 * (member.getId().hashCode() + defender.getId().hashCode() + Global.getSector().getClock().getDay()));
		attackerStr *= 0.75f + 0.25f * rand.nextFloat();
		defenderStr *= 0.75f + 0.25f * rand.nextFloat();
		
		boolean attackerWin = attackerStr > defenderStr;
		boolean defenderWin = !attackerWin;
		
		BoardingResult result = new BoardingResult();
		result.setMember(member);
		
		
		BoardingOutcome outcome = BoardingOutcome.SUCCESS;
		if (defenderWin) {
			outcome = BoardingOutcome.SHIP_ESCAPED;
		}
		
		CrewCompositionAPI boardingParty = Global.getFactory().createCrewComposition();
		boardingParty.addMarines(attacker.getCargo().getMarines());
		
		result.setOutcome(outcome);
		switch (outcome) {
		case SHIP_ESCAPED:
			computeCrewLossFromBoarding(result, member, boardingParty, attackerStr, defenderStr);
			result.getAttackerLosses().removeFromCargo(attacker.getCargo());
			member.getCrewComposition().removeAll(result.getDefenderLosses());
			
			letBoardableGo(member, defender, attacker);
			break;
		case SUCCESS:
			computeCrewLossFromBoarding(result, member, boardingParty, attackerStr, defenderStr);
			result.getAttackerLosses().removeFromCargo(attacker.getCargo());
			member.getCrewComposition().removeAll(result.getDefenderLosses());
			
			//attackerSide.removeEnemyCasualty(member);
			attacker.getFleetData().addFleetMember(member);
			getBattle().getCombinedFor(attacker).getFleetData().addFleetMember(member);
			
			member.getRepairTracker().setMothballed(true);
			//defender.getFleetData().removeFleetMember(member);
			
			attackerSide.changeEnemy(member, Status.CAPTURED);
			defenderSide.changeOwn(member, Status.CAPTURED);
			
			attackerSide.getInReserveDuringLastEngagement().add(member);
			defenderSide.getDestroyedInLastEngagement().remove(member);
			defenderSide.getDisabledInLastEngagement().remove(member);
			
			member.setOwner(0);
			member.setCaptain(Global.getFactory().createPerson());
			break;
		}

		return result;
	}
	
	public float getBoardingSuccessPercent(FleetMemberAPI member, CampaignFleetAPI attacker, CampaignFleetAPI defender) {
		DataForEncounterSide attackerSide = getDataFor(attacker);
		DataForEncounterSide defenderSide = getDataFor(defender);
		float attackerMarineMult = attacker.getCommanderStats().getMarineEffectivnessMult().getModifiedValue();
		float defenderMarineMult = defender.getCommanderStats().getMarineEffectivnessMult().getModifiedValue();
		
		float crewMult = 2f;
		float marineMult = 7f;
		
		Random rand = new Random();
		
		float wins = 0;
		float losses = 0;
		
		for (int i = 0; i < 100; i++) {
			float attackerStr = attacker.getCargo().getMarines() * marineMult;
			attackerStr *= attackerMarineMult;
			
			CrewCompositionAPI defenderCrew = member.getCrewComposition();
			float defenderStr = defenderCrew.getCrew() * crewMult + defenderCrew.getMarines() * marineMult;
			defenderStr *= defenderMarineMult;
	
			//defenderStr *= Global.getSettings().getFloat("boardingDifficulty");
			
			attackerStr *= 0.75f + 0.25f * rand.nextFloat();
			defenderStr *= 0.75f + 0.25f * rand.nextFloat();
		
			boolean attackerWin = attackerStr > defenderStr;
			if (attackerWin) wins++;
			else losses++;
		}
		
		return wins;
	}
	
	
	protected void computeMissedLaunchLosses(BoardingResult result, CrewCompositionAPI boardingParty) {
		result.getAttackerLosses().addAll(boardingParty);
		result.getAttackerLosses().multiplyBy((float) Math.random() * 0.2f);
	}
	
	protected void computeCrewLossFromBoarding(BoardingResult result,
							FleetMemberAPI member, CrewCompositionAPI boardingParty,
							float attackerStr, float defenderStr) {
		
		if (attackerStr < 1) attackerStr = 1;
		if (defenderStr < 1) defenderStr = 1;
		float cap = 2f;
		float attackerExtraStr = 0f;
		if (attackerStr > defenderStr * cap) {
			attackerExtraStr = attackerStr - defenderStr * cap;  
			attackerStr = defenderStr * cap;
		}
		if (defenderStr > attackerStr * cap) {
			defenderStr = attackerStr * cap;
		}
		
		float attackerLosses = defenderStr / (attackerStr + defenderStr);
		float defenderLosses = attackerStr / (attackerStr + defenderStr);
		
		if (attackerStr > defenderStr) {
			result.getAttackerLosses().addAll(boardingParty);
			result.getAttackerLosses().multiplyBy(attackerLosses * attackerStr / (attackerExtraStr + attackerStr));
			result.getDefenderLosses().addAll(member.getCrewComposition());
			result.getDefenderLosses().multiplyBy(defenderLosses);
		} else {
			result.getAttackerLosses().addAll(boardingParty);
			result.getAttackerLosses().multiplyBy(attackerLosses);
			result.getDefenderLosses().addAll(member.getCrewComposition());
			result.getDefenderLosses().multiplyBy(defenderLosses);
		}
		//member.getCrewComposition().removeAll(result.getDefenderLosses());
	}
	
	
	protected void applyBoardingSelfDestruct(FleetMemberAPI member, 
			 CrewCompositionAPI boardingParty, BoardingAttackType attackType,
			 List<FleetMemberAPI> boardingTaskForce,
			 CampaignFleetAPI attacker, CampaignFleetAPI defender,
			 BoardingResult result) {
		
		DataForEncounterSide attackerSide = getDataFor(attacker);
		DataForEncounterSide defenderSide = getDataFor(defender);
		
		attackerSide.changeEnemy(member, Status.DESTROYED);
		defenderSide.changeOwn(member, Status.DESTROYED);

		
		CrewCompositionAPI total = Global.getFactory().createCrewComposition();
		
		if (attackType == BoardingAttackType.SHIP_TO_SHIP) {
			for (FleetMemberAPI fm : boardingTaskForce) {
				float damage = member.getStats().getFluxCapacity().getModifiedValue() * (1f + (float) Math.random() * 0.5f);
				float hull = fm.getStatus().getHullFraction();
				float hullDamageFactor = 0f;
				fm.getStatus().applyDamage(damage);
				if (fm.getStatus().getHullFraction() <= 0) {
					fm.getStatus().disable();
					attacker.getFleetData().removeFleetMember(fm);
					attackerSide.addOwn(fm, Status.DESTROYED);
					//total.addAll(fm.getCrewComposition());
					
					attackerSide.getRetreatedFromLastEngagement().remove(fm);
					attackerSide.getInReserveDuringLastEngagement().remove(fm);
					attackerSide.getDeployedInLastEngagement().remove(fm);
					attackerSide.getDestroyedInLastEngagement().add(fm);
					
					result.getLostInSelfDestruct().add(fm);
					
					hullDamageFactor = 1f;
				} else {
					float newHull = fm.getStatus().getHullFraction();
					float diff = hull - newHull;
					if (diff < 0) diff = 0;
					hullDamageFactor = diff;
				}
				CrewCompositionAPI temp = Global.getFactory().createCrewComposition();
				temp.addAll(fm.getCrewComposition());
				float lossFraction = computeLossFraction(fm, null, fm.getStatus().getHullFraction(), hullDamageFactor);
				temp.multiplyBy(lossFraction);
				total.addAll(temp);
			}
			//total.removeAll(boardingParty);
		}
		
		float lossFraction = computeLossFraction(null, null, 0f, 1f);
		total.setMarines(Math.round(Math.max(total.getMarines(), boardingParty.getMarines() * lossFraction)));
		total.setCrew(Math.round(Math.max(total.getCrew(), boardingParty.getCrew() * lossFraction)));
		
		//result.getAttackerLosses().addAll(boardingParty);
//		float lossFraction = computeLossFraction(boardingTaskForce.get(0), 0f, 1f);
//		total.multiplyBy(lossFraction);
		
		result.getAttackerLosses().addAll(total);
		result.getDefenderLosses().addAll(member.getCrewComposition());
	}
	
	public void letBoardableGo(FleetMemberAPI toBoard, CampaignFleetAPI fleetItBelongsTo, CampaignFleetAPI attackingFleet) {
		DataForEncounterSide attackerSide = getDataFor(attackingFleet);
		attackerSide.removeEnemyCasualty(toBoard);
		
		DataForEncounterSide defenderSide = getDataFor(fleetItBelongsTo);
		defenderSide.removeOwnCasualty(toBoard);
		
		
		defenderSide.getDestroyedInLastEngagement().remove(toBoard);
		defenderSide.getDisabledInLastEngagement().remove(toBoard);
		defenderSide.getRetreatedFromLastEngagement().add(toBoard);
		
		if (!fleetItBelongsTo.isValidPlayerFleet()) {
			fleetItBelongsTo.getCargo().removeCrew(fleetItBelongsTo.getCargo().getCrew());
			fleetItBelongsTo.getCargo().removeMarines(fleetItBelongsTo.getCargo().getMarines());
		}
		
		FleetDataAPI data = fleetItBelongsTo.getFleetData();
		data.addFleetMember(toBoard);
		
		getBattle().getCombinedFor(fleetItBelongsTo).getFleetData().addFleetMember(toBoard);
		
		toBoard.getCrewComposition().addToCargo(fleetItBelongsTo.getCargo());
	}
	
	public List<FleetMemberAPI> getStoryRecoverableShips() {
		return storyRecoverableShips;
	}

	protected List<FleetMemberAPI> recoverableShips = new ArrayList<FleetMemberAPI>();
	protected List<FleetMemberAPI> storyRecoverableShips = new ArrayList<FleetMemberAPI>();
	public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {
		
		storyRecoverableShips.clear();
		
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
//		int max = Global.getSettings().getMaxShipsInFleet() - 
//				  Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size();
//		if (Misc.isPlayerOrCombinedContainingPlayer(winningFleet) && max <= 0) {
//			return result;
//		}
		
		if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet)) {
			return result;
		}
		
		DataForEncounterSide winnerData = getDataFor(winningFleet);
		DataForEncounterSide loserData = getDataFor(otherFleet);
		
		float playerContribMult = computePlayerContribFraction();
		List<FleetMemberData> enemyCasualties = winnerData.getEnemyCasualties();
		List<FleetMemberData> ownCasualties = winnerData.getOwnCasualties();
		List<FleetMemberData> all = new ArrayList<FleetMemberData>();
		all.addAll(ownCasualties);
		Collections.sort(all, new Comparator<FleetMemberData>() {
			public int compare(FleetMemberData o1, FleetMemberData o2) {
				int result = o2.getMember().getVariant().getSMods().size() - o1.getMember().getVariant().getSMods().size();
				if (result == 0) {
					result = o2.getMember().getHullSpec().getHullSize().ordinal() - o1.getMember().getHullSpec().getHullSize().ordinal();
				}
				return result;
			}
		});
		
		
		//Random random = Misc.getRandom(battle.getSeed(), 11);
		Random random = Misc.getRandom(Global.getSector().getPlayerBattleSeed(), 11);
		//System.out.println("BATTLE SEED: " + Global.getSector().getPlayerBattleSeed());
		
		// since the number of recoverable ships is limited, prefer "better" ships
		WeightedRandomPicker<FleetMemberData> enemyPicker = new WeightedRandomPicker<FleetMemberData>(random);
		
		// doesn't matter how it's sorted, as long as it's consistent so that
		// the order it's insertied into the picker in is the same
		List<FleetMemberData> enemy = new ArrayList<FleetMemberData>(enemyCasualties);
		Collections.sort(enemy, new Comparator<FleetMemberData>() {
			public int compare(FleetMemberData o1, FleetMemberData o2) {
				int result = o2.getMember().getId().hashCode() - o1.getMember().getId().hashCode();
				return result;
			}
		});
		
		for (FleetMemberData curr : enemy) {
			float base = 10f;
			switch (curr.getMember().getHullSpec().getHullSize()) {
			case CAPITAL_SHIP: base = 40f; break;
			case CRUISER: base = 20f; break;
			case DESTROYER: base = 10f; break;
			case FRIGATE: base = 5f; break;
			}
			float w = curr.getMember().getUnmodifiedDeploymentPointsCost() / base;
			
			enemyPicker.add(curr, w);
		}
		List<FleetMemberData> sortedEnemy = new ArrayList<FleetMemberData>();
		while (!enemyPicker.isEmpty()) {
			sortedEnemy.add(enemyPicker.pickAndRemove());
		}
		
		
		all.addAll(sortedEnemy);
		
//		for (FleetMemberData curr : all) {
//			System.out.println(curr.getMember().getHullId());
//		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		int maxRecoverablePerType = 24;
		
		float probLessDModsOnNext = Global.getSettings().getFloat("baseProbLessDModsOnRecoverableEnemyShip");
		float lessDmodsOnNextMult = Global.getSettings().getFloat("lessDModsOnRecoverableEnemyShipMultNext");
		
		int count = 0;
		for (FleetMemberData data : all) {
//			if (data.getMember().getHullId().contains("legion")) {
//				System.out.println("wefwefwefe");
//			}
			//if (data.getMember().getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)) continue;
			if (Misc.isUnboardable(data.getMember())) continue;
			if (data.getStatus() != Status.DISABLED && data.getStatus() != Status.DESTROYED) continue;

			boolean own = ownCasualties.contains(data);
			if (own && data.getMember().isAlly()) continue;

//			if (data.getMember().getHullId().startsWith("vanguard_pirates")) {
//				System.out.println("wefwefwefe12341234");
//			}
			
			float mult = 1f;
			if (data.getStatus() == Status.DESTROYED) mult = 0.5f;
			if (!own) mult *= playerContribMult;
			
			
			boolean useOfficerRecovery = false;
			if (own) {
				useOfficerRecovery = winnerData.getMembersWithOfficerOrPlayerAsOrigCaptain().contains(data.getMember());
				if (useOfficerRecovery) {
					mult = 1f;
				}
			}
			
			boolean noRecovery = false;
			if (battle != null &&
				battle.getSourceFleet(data.getMember()) != null) {
				CampaignFleetAPI fleet = battle.getSourceFleet(data.getMember());
				if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY)) {
					noRecovery = true;
				}
			}
	
//			if (data.getMember().getHullId().startsWith("cerberus")) {
//				System.out.println("wefwefew");
//			}
			boolean normalRecovery = !noRecovery && 
						Misc.isShipRecoverable(data.getMember(), playerFleet, own, useOfficerRecovery, 1f * mult);
			boolean storyRecovery = !noRecovery && !normalRecovery;
		
			boolean alwaysRec = data.getMember().getVariant().hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE);

			float shipRecProb = data.getMember().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).computeEffective(0f);
			if (!own && !alwaysRec && (storyRecovery || normalRecovery) && shipRecProb < 1f) {
				float per = Global.getSettings().getFloat("probNonOwnNonRecoverablePerDMod");
				float perAlready = Global.getSettings().getFloat("probNonOwnNonRecoverablePerAlreadyRecoverable");
				float max = Global.getSettings().getFloat("probNonOwnNonRecoverableMax");
				int dmods = DModManager.getNumDMods(data.getMember().getVariant());
				
				float assumedAddedDmods = 3f;
				assumedAddedDmods -= Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.SHIP_DMOD_REDUCTION, 0) * 0.5f;
				assumedAddedDmods = Math.min(assumedAddedDmods, 5 - dmods);
				
				float recoveredSoFar = 0f;
				if (storyRecovery) recoveredSoFar = storyRecoverableShips.size();
				else recoveredSoFar = result.size(); 
				
				if (random.nextFloat() < Math.min(max, (dmods + assumedAddedDmods) * per) + recoveredSoFar * perAlready) {
					noRecovery = true;
				}
			}
			
			
			//if (true || Misc.isShipRecoverable(data.getMember(), playerFleet, own, useOfficerRecovery, battle.getSeed(), 1f * mult)) {
			if (!noRecovery && (normalRecovery || storyRecovery)) {
			//if (Misc.isShipRecoverable(data.getMember(), playerFleet, battle.getSeed(), 1f * mult)) {
				
				if (!own || !Misc.isUnremovable(data.getMember().getCaptain())) {
					String aiCoreId = null;
					if (own && data.getMember().getCaptain() != null && 
							data.getMember().getCaptain().isAICore()) {
						aiCoreId = data.getMember().getCaptain().getAICoreId();
					}
					
					// if it's an AI core on a player ship, then:
					// 1. It's integrated/unremovable, so, don't remove (we don't even end up here)
					// 2. Ship will be recovered and will still have it, or
					// 3. Ship will not be recovered, and it will get added to loot in lootWeapons()
					boolean keepCaptain = false;
					// don't do this - want to only show the AI core in recovery dialog when 
					// it's integrated and would be lost if not recovered
//					if (own && (data.getMember().getCaptain() == null || 
//							data.getMember().getCaptain().isAICore())) {
//						keepCaptain = true;
//					}
					if (!keepCaptain) {
						if (aiCoreId != null) {
							data.getMember().setCaptain(Global.getFactory().createPerson());
							data.getMember().getCaptain().getMemoryWithoutUpdate().set(
										"$aiCoreIdForRecovery", aiCoreId);
						} else if (!own && data.getMember().getCaptain() != null &&
								data.getMember().getCaptain().isAICore()) {
							aiCoreId = data.getMember().getCaptain().getAICoreId();
							data.getMember().setCaptain(Global.getFactory().createPerson());
							data.getMember().getCaptain().getMemoryWithoutUpdate().set(
									"$aiCoreIdForPossibleRecovery", aiCoreId);
							
						}
					}
				}
				
				ShipVariantAPI variant = data.getMember().getVariant();
				variant = variant.clone();
				variant.setSource(VariantSource.REFIT);
				
				// maybe this was necessary? commenting this out to for simulator to be able to unlock recoverable ship variants
				//variant.setOriginalVariant(null);
				
				//DModManager.setDHull(variant);
				data.getMember().setVariant(variant, false, true);
				
				boolean lessDmods = false;
				if (!own && data.getStatus() != Status.DESTROYED && random.nextFloat() < probLessDModsOnNext) {
					lessDmods = true;
					probLessDModsOnNext *= lessDmodsOnNextMult;
				}
				
				//Random dModRandom = new Random(1000000 * (data.getMember().getId().hashCode() + Global.getSector().getClock().getDay()));
				Random dModRandom = new Random(1000000 * data.getMember().getId().hashCode() + Global.getSector().getPlayerBattleSeed());
				dModRandom = Misc.getRandom(dModRandom.nextLong(), 5);
				if (lessDmods) {
					DModManager.reduceNextDmodsBy = 3;	
				}
				
				float probAvoidDmods = 
						data.getMember().getStats().getDynamic().getMod(
								Stats.DMOD_AVOID_PROB_MOD).computeEffective(0f);
				
				float probAcquireDmods = 
						data.getMember().getStats().getDynamic().getMod(
								Stats.DMOD_ACQUIRE_PROB_MOD).computeEffective(1f);
				
				if (dModRandom.nextFloat() >= probAvoidDmods && dModRandom.nextFloat() < probAcquireDmods) {
					DModManager.addDMods(data, own, Global.getSector().getPlayerFleet(), dModRandom);
					if (DModManager.getNumDMods(variant) > 0) {
						DModManager.setDHull(variant);
					}
				}
				
				float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
				float wingProb = Global.getSettings().getFloat("salvageWingProb");
				if (own) {
					weaponProb = Global.getSettings().getFloat("salvageOwnWeaponProb");
					wingProb = Global.getSettings().getFloat("salvageOwnWingProb");
					weaponProb = playerFleet.getStats().getDynamic().getValue(Stats.OWN_WEAPON_RECOVERY_MOD, weaponProb);
					wingProb = playerFleet.getStats().getDynamic().getValue(Stats.OWN_WING_RECOVERY_MOD, wingProb);
				}
				
				boolean retain = data.getMember().getHullSpec().hasTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY) ||
						data.getMember().getVariant().hasTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
				prepareShipForRecovery(data.getMember(), own, true, !own && !retain, weaponProb, wingProb, salvageRandom);
				
				if (normalRecovery) {
					if (result.size() < maxRecoverablePerType) {
						result.add(data.getMember());
					}
				} else if (storyRecovery) {
					if (storyRecoverableShips.size() < maxRecoverablePerType) {
						storyRecoverableShips.add(data.getMember());
					}
				}
				
//				count++;
//				if (count >= max) break;
			}
			
			
//			else {
//				data.getMember().getVariant().removeTag(Tags.SHIP_RECOVERABLE);
//			}
		}
		
		//System.out.println("Recoverable: " + result.size() + ", story: " + storyRecoverableShips.size());
		
		
		recoverableShips.clear();
		recoverableShips.addAll(result);
		return result;
	}
	

	
	
	public static void recoverShips(List<FleetMemberAPI> ships, FleetEncounterContext context, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {
		
		if (!Misc.isPlayerOrCombinedContainingPlayer(winningFleet)) {
			return;
		}
		
		DataForEncounterSide winnerData = null;
		DataForEncounterSide loserData = null;
		
		if (context != null) {
			winnerData = context.getDataFor(winningFleet);
			loserData = context.getDataFor(otherFleet);
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		for (FleetMemberAPI member : ships) {
			//CampaignFleetAPI sourceFleet = context.getBattle().getSourceFleet(member);
			//repairFleetMember(member, sourceFleet == playerFleet);
			
			if (member.getStatus().getNumStatuses() <= 1) {
				member.getStatus().repairDisabledABit();
			}
//			for (int i = 1; i < member.getStatus().getNumStatuses(); i++) {
//				if ((float) Math.random() > 0.33f) {
//					member.getStatus().setDetached(i, true);
//					member.getStatus().setHullFraction(i, 0f);
//				}
//			}
			
			float minHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f);
			float maxHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f);
			float minCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f);
			float maxCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f);
			
			minHull += member.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f);
			maxHull += member.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f);
			minCR += member.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f);
			maxCR += member.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f);
			
			float hull = (float) Math.random() * (maxHull - minHull) + minHull;
			if (hull < 0.01f) hull = 0.01f;
			if (hull > 1f) hull = 1f;
			member.getStatus().setHullFraction(hull);
			
			float cr = (float) Math.random() * (maxCR - minCR) + minCR;
			if (cr < 0 || member.isMothballed()) cr = 0;
			float max = member.getRepairTracker() == null ? 1f : member.getRepairTracker().getMaxCR();
			if (cr > max) cr = max;
			member.getRepairTracker().setCR(cr);
			
			if (winnerData != null) winnerData.getInReserveDuringLastEngagement().add(member);
			playerFleet.getFleetData().addFleetMember(member);
			if (context != null) {
				context.getBattle().getCombinedFor(playerFleet).getFleetData().addFleetMember(member);
				context.origSourceForRecoveredShips.put(member, context.getBattle().getSourceFleet(member));
				context.getBattle().getMemberSourceMap().put(member, playerFleet);
			}
			
			member.setFleetCommanderForStats(null, null);
			

			member.setOwner(0);
			
			if (!Misc.isUnremovable(member.getCaptain())) {
				member.setCaptain(Global.getFactory().createPerson());
				member.getCaptain().setFaction(Factions.PLAYER);
			}
			
			//member.getRepairTracker().setMothballed(true);
			
			if (winnerData != null) {
				winnerData.changeEnemy(member, Status.REPAIRED);
				winnerData.changeOwn(member, Status.REPAIRED);
				
				winnerData.getDestroyedInLastEngagement().remove(member);
				winnerData.getDisabledInLastEngagement().remove(member);
			}
			
			if (loserData != null) {
				loserData.changeEnemy(member, Status.REPAIRED);
				loserData.changeOwn(member, Status.REPAIRED);
				
				loserData.getDestroyedInLastEngagement().remove(member);
				loserData.getDisabledInLastEngagement().remove(member);
			}
		}

		return;
	}
	
	
	public static void prepareShipForRecovery(FleetMemberAPI member,
					boolean retainAllHullmods, boolean retainKnownHullmods, boolean clearSMods,
					float weaponRetainProb, float wingRetainProb, Random salvageRandom) {
		ShipVariantAPI variant = member.getVariant().clone();
		//variant.setOriginalVariant(null);
		if (retainAllHullmods) {
			// do nothing
		} else if (retainKnownHullmods) {
			for (String modId : new ArrayList<String>(variant.getHullMods())) {
				if (!Global.getSector().getPlayerFaction().knowsHullMod(modId)) {
					variant.removeMod(modId);
				}
			}
		} else {
			variant.clearHullMods();
			variant.setNumFluxCapacitors(0);
			variant.setNumFluxVents(0);
		}
		
		if (clearSMods && !variant.hasTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE)) {
			for (String id : new ArrayList<String>(variant.getSMods())) {
				variant.removePermaMod(id);
			}
		}
		
		variant.setSource(VariantSource.REFIT);
		member.setVariant(variant, false, false);
		List<String> remove = new ArrayList<String>();
		
//		if (!retainHullmods) {
//			variant.clearHullMods();
//			variant.setNumFluxCapacitors(0);
//			variant.setNumFluxVents(0);
//		}
		
		Random random = new Random();
		if (salvageRandom != null) random = salvageRandom;
		
		if (!member.isFighterWing()) {
			for (String slotId : variant.getNonBuiltInWeaponSlots()) {
				if (random.nextFloat() > weaponRetainProb) {
					remove.add(slotId);
				}
			}
			for (String slotId : remove) {
				variant.clearSlot(slotId);
			}
			
			int index = 0;
			for (String id : variant.getFittedWings()) {
				if (random.nextFloat() > wingRetainProb) {
					variant.setWingId(index, null); // won't clear out built-in wings
				}
				index++;
			}
		}
		
		for (String slotId : variant.getStationModules().keySet()) {
			prepareModuleForRecovery(member, slotId, 
					retainAllHullmods, retainKnownHullmods, clearSMods, weaponRetainProb, wingRetainProb, salvageRandom);
		}
		
		
		for (int i = 1; i < member.getStatus().getNumStatuses(); i++) {
			if (random.nextFloat() > 0.5f) {
				member.getStatus().setDetached(i, false);
				member.getStatus().setHullFraction(i, 0.1f + 0.1f * random.nextFloat());
			}
		}
	
		// get rid of any short-term modifiers, such as "0 repair rate during emergency burn"
		for (int i = 0; i < 10; i++) {
			member.getBuffManager().advance(1f);
		}
//		for (Buff buff : new ArrayList<Buff>(member.getBuffManager().getBuffs())) {
//			member.getBuffManager().removeBuff(buff.getId());
//		}
		
		variant.addTag(Tags.SHIP_RECOVERABLE);
	}
	
	public static void prepareModuleForRecovery(FleetMemberAPI member, String moduleSlotId,
			boolean retainAllHullmods, boolean retainKnownHullmods, boolean clearSMods,
			float weaponRetainProb, float wingRetainProb, Random salvageRandom) {
		
		ShipVariantAPI moduleCurrent = member.getVariant().getModuleVariant(moduleSlotId);
		if (moduleCurrent == null) return;	
		
		moduleCurrent = moduleCurrent.clone();
		moduleCurrent.setOriginalVariant(null);
		if (retainAllHullmods) {
			// do nothing
		} else if (retainKnownHullmods) {
			for (String modId : new ArrayList<String>(moduleCurrent.getHullMods())) {
				if (!Global.getSector().getPlayerFaction().knowsHullMod(modId)) {
					moduleCurrent.removeMod(modId);
				}
			}
		} else {
			moduleCurrent.clearHullMods();
			moduleCurrent.setNumFluxCapacitors(0);
			moduleCurrent.setNumFluxVents(0);
		}
		
		if (clearSMods && !moduleCurrent.hasTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE)) {
			for (String id : new ArrayList<String>(moduleCurrent.getSMods())) {
				moduleCurrent.removePermaMod(id);
			}
		}
		
		moduleCurrent.setSource(VariantSource.REFIT);
		member.getVariant().setModuleVariant(moduleSlotId, moduleCurrent);
		
		List<String> remove = new ArrayList<String>();

		Random random = Misc.random;
		if (salvageRandom != null) random = salvageRandom;

		for (String slotId : moduleCurrent.getNonBuiltInWeaponSlots()) {
			if (random.nextFloat() > weaponRetainProb) {
				remove.add(slotId);
			}
		}
		for (String slotId : remove) {
			moduleCurrent.clearSlot(slotId);
		}

		int index = 0;
		for (String id : moduleCurrent.getFittedWings()) {
			if (random.nextFloat() > wingRetainProb) {
				moduleCurrent.setWingId(index, null); // won't clear out built-in wings
			}
			index++;
		}
	}

	
	public void gainXP() {
		if (sideData.size() != 2) return;
		if (!battle.isPlayerInvolved()) return;

		DataForEncounterSide sideOne = sideData.get(0);
		DataForEncounterSide sideTwo = sideData.get(1);
		if (battle.isPlayerSide(battle.getSideFor(sideOne.getFleet()))) {
			gainXP(sideOne, sideTwo);
		} else if (battle.isPlayerSide(battle.getSideFor(sideTwo.getFleet()))) {
			gainXP(sideTwo, sideOne);
		}
	}
	
	protected void gainOfficerXP(DataForEncounterSide data, float xp) {
		float max = data.getMaxTimeDeployed();
		if (max < 1) max = 1;
		float num = data.getOfficerData().size();
		if (num < 1) num = 1;
		for (PersonAPI person : data.getOfficerData().keySet()) {
			OfficerEngagementData oed = data.getOfficerData().get(person);
			if (oed.sourceFleet == null || !oed.sourceFleet.isPlayerFleet()) continue;
			
			OfficerDataAPI od = oed.sourceFleet.getFleetData().getOfficerData(person);
			if (od == null) continue; // shouldn't happen, as this is checked earlier before it goes into the map
			
			float f = oed.timeDeployed / max;
			if (f < 0) f = 0;
			if (f > 1) f = 1;
			
			od.addXP((long)(f * xp / num), textPanelForXPGain);
		}
	}
	
	
	public float getPlayerFPHullDamageToEnemies() {
		return playerFPHullDamageToEnemies;
	}

	public void setPlayerFPHullDamageToEnemies(float playerFPHullDamageToEnemies) {
		this.playerFPHullDamageToEnemies = playerFPHullDamageToEnemies;
	}

	public float getAllyFPHullDamageToEnemies() {
		return allyFPHullDamageToEnemies;
	}

	public void setAllyFPHullDamageToEnemies(float allyFPHullDamageToEnemies) {
		this.allyFPHullDamageToEnemies = allyFPHullDamageToEnemies;
	}

	protected float playerFPHullDamageToEnemies = 0f;
	protected float allyFPHullDamageToEnemies = 0f;
	protected float playerFPHullDamageToAllies = 0f;
	protected Map<FactionAPI, Float> playerFPHullDamageToAlliesByFaction = new HashMap<FactionAPI, Float>();
	protected void computeFPHullDamage() {
		if (runningDamageTotal == null) return;
		
//		playerFPHullDamageToEnemies = 0f;
//		allyFPHullDamageToEnemies = 0f;
//		playerFPHullDamageToAllies = 0f;
		
		for (FleetMemberAPI member : runningDamageTotal.getDealt().keySet()) {
			if (member.getOwner() != 0) continue;
			
			
			DealtByFleetMember dealt = runningDamageTotal.getDealt().get(member);
			for (FleetMemberAPI target : dealt.getDamage().keySet()) {
				if (battle.getSourceFleet(target) == null) continue;
				
				DamageToFleetMember damage = dealt.getDamageTo(target);
				float maxHull = target.getStats().getHullBonus().computeEffective(target.getHullSpec().getHitpoints());
				if (maxHull <= 0) continue;
				if (target.isFighterWing()) {
					maxHull *= target.getNumFightersInWing();
				}
				
				float currDam = Math.min(damage.hullDamage, maxHull) / maxHull * (float) target.getFleetPointCost();
				if (target.getOwner() == 1) {
					CampaignFleetAPI fleet = battle != null ? battle.getSourceFleet(member) : null;
					boolean ally = member.isAlly();
					if (ally && fleet != null && 
							fleet.getFaction() != null && fleet.getFaction().isPlayerFaction()) {
						ally = false;
					}
					if (ally) {
						allyFPHullDamageToEnemies += currDam;
					} else {
						playerFPHullDamageToEnemies += currDam;
					}
				} else if (!member.isAlly() && target.isAlly() && !target.isFighterWing()) {
					playerFPHullDamageToAllies += currDam;
					CampaignFleetAPI fleet = battle != null ? battle.getSourceFleet(target) : null;
					if (fleet != null) {
						float curr = currDam;
						if (playerFPHullDamageToAlliesByFaction.containsKey(fleet.getFaction())) {
							curr += playerFPHullDamageToAlliesByFaction.get(fleet.getFaction());
						}
						playerFPHullDamageToAlliesByFaction.put(fleet.getFaction(), curr);
					}
				}
			}
		}
		
//		if (playerFPHullDamageToEnemies <= 0) {
//			System.out.println("HERE 12523423");
//		}
//		allyFPHullDamageToEnemies += playerFPHullDamageToEnemies;
//		playerFPHullDamageToEnemies = 0f;
		runningDamageTotal = null;
	}
	
	
	public float computePlayerContribFraction() {
		float total = playerFPHullDamageToEnemies + allyFPHullDamageToEnemies;
		if (total <= 0) {
			if (battle == null) return 1f;
			if (battle.isPlayerInvolved() && (battle.getPlayerSideSnapshot().size() <= 1 || battle.getPlayerSide().size() <= 1)) return 1f;
			return 0f;
		}
		
		boolean hasAllies = false;
		boolean startedWithAllies = false;
		if (battle != null) {
			hasAllies = battle.getPlayerSide().size() <= 1;
			startedWithAllies = battle.getPlayerSideSnapshot().size() > 1;
		}
		if (startedWithAllies) { // && hasAllies) {
			//return Math.min(0.9f, playerFPHullDamageToEnemies / total);
			return Math.min(1f, playerFPHullDamageToEnemies / total);
		} else {
			return 1f;
		}
	}
	
	protected float xpGained = 0;
	protected void gainXP(DataForEncounterSide side, DataForEncounterSide otherSide) {
		float bonusXP = 0f;
		float points = 0f;
		for (FleetMemberData data : side.getOwnCasualties()) {
			if (data.getStatus() == Status.DISABLED || 
					data.getStatus() == Status.DESTROYED) {
				float [] bonus = Misc.getBonusXPForScuttling(data.getMember());
				points += bonus[0];
				bonusXP += bonus[1] * bonus[0];
			}
		}
		if (bonusXP > 0 && points > 0) {
			points = 1;
			Global.getSector().getPlayerStats().setOnlyAddBonusXPDoNotSpendStoryPoints(true);
			Global.getSector().getPlayerStats().setBonusXPGainReason("from losing s-modded ships");
			Global.getSector().getPlayerStats().spendStoryPoints((int)Math.round(points), true, textPanelForXPGain, false, bonusXP, null);
			Global.getSector().getPlayerStats().setOnlyAddBonusXPDoNotSpendStoryPoints(false);
			Global.getSector().getPlayerStats().setBonusXPGainReason(null);
		}
		
		//CampaignFleetAPI fleet = side.getFleet();
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float fpTotal = 0;
		for (FleetMemberData data : otherSide.getOwnCasualties()) {
			float fp = data.getMember().getFleetPointCost();
			
//			String prefix = "xp_mult_";
//			for (String tag : data.getMember().getHullSpec().getTags()) {
//				if (tag.startsWith(prefix)) {
//					tag = tag.replaceFirst(prefix, "");
//					fp *= Float.parseFloat(tag);
//					break;
//				}
//			}
			
			fp *= 1f + data.getMember().getCaptain().getStats().getLevel() / 5f;
			fpTotal += fp;
		}
		
		float xp = (float) fpTotal * 250;
		xp *= 2f;
		
		float difficultyMult = Math.max(1f, difficulty);
		xp *= difficultyMult;
		
		xp *= computePlayerContribFraction();
		
		xp *= Global.getSettings().getFloat("xpGainMult");
		
		
		if (xp > 0) {
			//fleet.getCargo().gainCrewXP(xp);
			
			//if (side.getFleet().isPlayerFleet()) {
			//}
			// only gain XP if it's the player fleet anyway, no need to check this here
			gainOfficerXP(side, xp);
			
			fleet.getCommander().getStats().addXP((long) xp, textPanelForXPGain);
			fleet.getCommander().getStats().levelUpIfNeeded(textPanelForXPGain);
			
			xpGained = xp;
		}
	}
	
	public void addPotentialOfficer() {
		if (!isEngagedInHostilities()) return;
		if (xpGained <= 0) return;
		if (sideData.size() != 2) return;
		if (!battle.isPlayerInvolved()) return;

		DataForEncounterSide sideOne = sideData.get(0);
		DataForEncounterSide sideTwo = sideData.get(1);
		
		DataForEncounterSide player = sideOne;
		DataForEncounterSide enemy = sideTwo;
		if (battle.isPlayerSide(battle.getSideFor(sideTwo.getFleet()))) {
			player = sideTwo;
			enemy = sideOne;
		}
		
		float fpDestroyed = 0;
		for (FleetMemberData data : enemy.getOwnCasualties()) {
			float fp = data.getMember().getFleetPointCost();
			fp *= 1f + data.getMember().getCaptain().getStats().getLevel() / 5f;
			fpDestroyed += fp;
		}
		fpDestroyed *= computePlayerContribFraction();
		for (FleetMemberData data : player.getOwnCasualties()) {
			if (data.getMember().isAlly()) continue;
			float fp = data.getMember().getFleetPointCost();
			fp *= 1f + data.getMember().getCaptain().getStats().getLevel() / 5f;
			fpDestroyed += fp;
		}
		
		float fpInFleet = 0f;
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			float fp = member.getFleetPointCost();
			fp *= 1f + member.getCaptain().getStats().getLevel() / 5f;
			fpInFleet += fp;
		}
		
		
		float maxProb = Global.getSettings().getFloat("maxOfficerPromoteProb");
		float probMult = Global.getSettings().getFloat("officerPromoteProbMult");
		int max = Misc.getMaxOfficers(Global.getSector().getPlayerFleet());
		int curr = Misc.getNumNonMercOfficers(Global.getSector().getPlayerFleet());

		float prob = fpDestroyed / (Math.max(1f, fpInFleet));
		//prob /= 5f;
		prob *= probMult;
		
		if (curr >= max) prob *= 0.5f;
		if (prob > maxProb) prob = maxProb;
		
		Random random = Misc.random;
		if (salvageRandom != null) {
			random = salvageRandom;
		}
		
		if (TutorialMissionIntel.isTutorialInProgress()) {
			prob = 0f;
		}
		
		if (random.nextFloat() < prob) {
			PromoteOfficerIntel intel = new PromoteOfficerIntel(textPanelForXPGain);
			Global.getSector().getIntelManager().addIntel(intel, false, textPanelForXPGain);
		}
	}
	

	protected CargoAPI loot = Global.getFactory().createCargo(false);
	protected int creditsLooted = 0;
	public void generateLoot(List<FleetMemberAPI> recoveredShips, boolean withCredits) {
		creditsLooted = 0;
		loot.clear();
		//if (getWinner() == Global.getSector().getPlayerFleet()) {
		if (battle.isPlayerSide(battle.getSideFor(getWinner()))) {
			generatePlayerLoot(recoveredShips, withCredits);
		} else { //if (getLoser() == Global.getSector().getPlayerFleet()) {
			handleCargoLooting(recoveredShips, true);
		}
		loot.sort();
	}
	
	private Random salvageRandom = null;
	public Random getSalvageRandom() {
		return salvageRandom;
	}
	public void setSalvageRandom(Random salvageRandom) {
		this.salvageRandom = salvageRandom;
	}

	protected void generatePlayerLoot(List<FleetMemberAPI> recoveredShips, boolean withCredits) {
		//computeFPHullDamage();
		
		
		DataForEncounterSide winner = getWinnerData();
		DataForEncounterSide loser = getLoserData();

		if (winner == null || loser == null) return;
	
		float adjustedFPSalvage = 0;
		float playerContribMult = computePlayerContribFraction();
		
		Random origSalvageRandom = salvageRandom;
		long extraSeed = 1340234324325L;
		if (origSalvageRandom != null) extraSeed = origSalvageRandom.nextLong();
		
		for (FleetMemberData data : winner.getEnemyCasualties()) {
			if (data.getStatus() == Status.REPAIRED) {
				continue;
			}
			
			if (data.getMember() != null && data.getMember().getHullSpec().hasTag(Tags.NO_BATTLE_SALVAGE)) {
				continue;
			}
			
			if (origSalvageRandom != null) {
				String sig = data.getMember().getHullId();
				if (data.getMember().getVariant() != null) {
					for (WeaponGroupSpec spec : data.getMember().getVariant().getWeaponGroups()) {
						for (String slotId : spec.getSlots()) {
							String w = data.getMember().getVariant().getWeaponId(slotId);
							if (w != null) sig += w;
						}
					}
				}
				if (loser != null && loser.getFleet() != null && loser.getFleet().getFleetData() != null) {
					List<FleetMemberAPI> members = loser.getFleet().getFleetData().getMembersListCopy();
					if (members != null) {
						int index = members.indexOf(data.getMember());
						if (index >= 0) {
							sig += "" + index;
						}
					}
				}
				long seed = sig.hashCode() * 143234234234L * extraSeed;
				salvageRandom = new Random(seed);
				//System.out.println("Seed for " + data.getMember() + ": " + seed);
			}
			
			float mult = getSalvageMult(data.getStatus()) * playerContribMult;
			lootWeapons(data.getMember(), data.getMember().getVariant(), false, mult, false);
			lootHullMods(data.getMember(), data.getMember().getVariant(), mult);
			lootWings(data.getMember(), data.getMember().getVariant(), false, mult);
			adjustedFPSalvage += (float) data.getMember().getFleetPointCost() * mult;
		}
		
		for (FleetMemberData data : winner.getOwnCasualties()) {
			if (data.getMember().isAlly()) continue;
			
			if (data.getStatus() == Status.CAPTURED || data.getStatus() == Status.REPAIRED) {
				continue;
			}
			
			if (data.getMember() != null && data.getMember().getHullSpec().hasTag(Tags.NO_BATTLE_SALVAGE)) {
				continue;
			}
			
			// only care about salvageRandom for enemy casualties, not player
//			if (origSalvageRandom != null) {
//				salvageRandom = new Random(data.getMember().getId().hashCode() * 143234234234L * extraSeed);
//			}
			
			float mult = getSalvageMult(data.getStatus());
			lootWeapons(data.getMember(), data.getMember().getVariant(), true, mult, false);
			lootWings(data.getMember(), data.getMember().getVariant(), true, mult);
			
			adjustedFPSalvage += (float) data.getMember().getFleetPointCost() * mult;
		}
		
		if (recoveredShips != null) {
			for (FleetMemberAPI member : recoveredShips) {
				float mult = getSalvageMult(Status.CAPTURED);
				adjustedFPSalvage += (float) member.getFleetPointCost() * mult;
			}
		}
		
		salvageRandom = origSalvageRandom;
		
		// don't want salvageRandom to be influenced by the number of losses on either side
		Random resetSalvageRandomTo = null;
		Random forRandomDrops = null;
		Random forCargoDrops = null;

		Random random = Misc.random;
		if (salvageRandom != null) {
			random = salvageRandom;
			resetSalvageRandomTo = Misc.getRandom(random.nextLong(), 11);
			forRandomDrops = Misc.getRandom(random.nextLong(), 17);
			forCargoDrops = Misc.getRandom(random.nextLong(), 31);
		} else {
			if (getBattle() != null) {
				MemoryAPI memory = getBattle().getNonPlayerCombined().getMemoryWithoutUpdate();
				if (memory.contains(MemFlags.SALVAGE_SEED)) {
					random = new Random(memory.getLong(MemFlags.SALVAGE_SEED));
				}
			}
		}
		
		float minCreditsFraction = Global.getSettings().getFloat("salvageFractionCreditsMin");
		float maxCreditsFraction = Global.getSettings().getFloat("salvageFractionCreditsMax");
		
		float creditsFraction = minCreditsFraction + (maxCreditsFraction - minCreditsFraction) * random.nextFloat();
		creditsFraction *= playerContribMult;
		
		float maxSalvageValue = adjustedFPSalvage * Global.getSettings().getFloat("salvageValuePerFP");
		if (Misc.isEasy()) {
			 maxSalvageValue *= Global.getSettings().getFloat("easySalvageMult");
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		float valueMultFleet = playerFleet.getStats().getDynamic().getValue(Stats.BATTLE_SALVAGE_MULT_FLEET);
		float valueModShips = getSalvageValueModPlayerShips();
		
		
		maxSalvageValue *= valueMultFleet + valueModShips;
		
		creditsLooted = Math.round(maxSalvageValue * creditsFraction);
		if (!withCredits) creditsLooted = 0;
		maxSalvageValue -= creditsLooted;
		
		float salvageValue = 0f;
		WeightedRandomPicker<String> lootPicker = new WeightedRandomPicker<String>(random);
		lootPicker.add(Commodities.METALS, 20);
		lootPicker.add(Commodities.SUPPLIES, 10);
		lootPicker.add(Commodities.FUEL, 10);
		lootPicker.add(Commodities.HEAVY_MACHINERY, 1);
		
		while (salvageValue < maxSalvageValue) {
			String commodityId = lootPicker.pick();
			if (commodityId == null) break;
			
			CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(commodityId);
			float qty = 1f;
			salvageValue += spec.getBasePrice() * qty;
			loot.addCommodity(commodityId, qty);
		}

		
		float fuelMult = playerFleet.getStats().getDynamic().getValue(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET);
		float fuel = loot.getFuel();
		if (fuelMult > 1f) {
			loot.addFuel((int) Math.round(fuel * (fuelMult - 1f)));
		}
		
		if (getBattle().getSnapshotSideFor(loser.getFleet()) == null) return;
		
		List<DropData> dropRandom = new ArrayList<DropData>();
		List<DropData> dropValue = new ArrayList<DropData>();
		//for (CampaignFleetAPI other : getBattle().getSideFor(loser.getFleet())) {
		for (CampaignFleetAPI other : getBattle().getSnapshotSideFor(loser.getFleet())) {
			dropRandom.addAll(other.getDropRandom());
			dropValue.addAll(other.getDropValue());
			other.getDropRandom().clear();
			other.getDropValue().clear();
			
			CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(other);
			loot.addAll(extra);
			
			BaseSalvageSpecial.clearExtraSalvage(other);
			if (!extra.isEmpty()) {
				ListenerUtil.reportExtraSalvageShown(other);
			}
		}
		
		if (forRandomDrops != null) {
			random = forRandomDrops;
		}
		CargoAPI extra = SalvageEntity.generateSalvage(random, valueMultFleet + valueModShips, 1f, fuelMult, dropValue, dropRandom);
		for (CargoStackAPI stack : extra.getStacksCopy()) {
			loot.addFromStack(stack);
		}
		
		if (forCargoDrops != null) {
			salvageRandom = forCargoDrops;
		}
		handleCargoLooting(recoveredShips, false);
		
		if (resetSalvageRandomTo != null) {
			salvageRandom = resetSalvageRandomTo;
		}
	}
	
	public float getSalvageValueModPlayerShips() {
		return RepairGantry.getAdjustedGantryModifierForPostCombatSalvage(Global.getSector().getPlayerFleet());
//		float valueModShips = 0;
//		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
//			if (member.isMothballed()) continue;
//			float maxCurr = member.getStats().getDynamic().getValue(Stats.BATTLE_SALVAGE_VALUE_MULT_MOD, 0f);
//			OfficerEngagementData data = getWinnerData().getFleetMemberDeploymentData().get(member);
//			if (data == null) continue;
//			float memberDeployed = data.timeDeployed;
//			float maxDeployed = getWinnerData().getMaxTimeDeployed();
//			if (maxDeployed <= 0) continue;
//			maxCurr *= Math.min(1f, memberDeployed / maxDeployed);
//			
//			valueModShips += maxCurr;
//		}
//		return valueModShips;
	}
	
	protected static class LootableCargoStack {
		public CargoAPI source;
		public CargoStackAPI stack;
		public LootableCargoStack(CargoAPI source, CargoStackAPI stack) {
			this.source = source;
			this.stack = stack;
		}
	}
	
	protected static class LossFraction {
		public float maxCargo;
		public float maxFuel;
		public float lostCargo;
		public float lostFuel;
	}
	
	
	protected void handleCargoLooting(List<FleetMemberAPI> recoveredShips, boolean takingFromPlayer) {
		DataForEncounterSide winner = getWinnerData();
		DataForEncounterSide loser = getLoserData();

		if (winner == null || loser == null) return;
		
		loser.getFleet().getFleetData().updateCargoCapacities();
		CargoAPI loserCargo = (CargoAPI) loser.getFleet().getCargo();
		float maxCargo = loserCargo.getMaxCapacity();
		float maxFuel = loserCargo.getMaxFuel();
		
		Random random = Misc.random;
		if (salvageRandom != null) random = salvageRandom;
		
//		boolean playerLost = battle.isPlayerSide(battle.getSideFor(loser.getFleet()));
		
		Map<CargoAPI, LossFraction> fractions = new HashMap<CargoAPI, LossFraction>();
		
		float lostCargo = 0f;
		float lostFuel = 0f;

		float totalLoss = 0f;
		for (FleetMemberData data : winner.getEnemyCasualties()) {
//			if (data.getStatus() == Status.REPAIRED) {
//				continue;
//			}
//			if (playerLost && data.getMember().isAlly()) {
//				continue;
//			}
			
			CampaignFleetAPI source = battle.getSourceFleet(data.getMember());
			CampaignFleetAPI orig = origSourceForRecoveredShips.get(data.getMember());
			if (orig != null) source = orig;
			
			if (source != null) {
				CargoAPI c = source.getCargo();
				LossFraction loss = fractions.get(c);
				if (loss == null) {
					loss = new LossFraction();
					loss.maxCargo = c.getMaxCapacity();
					loss.maxFuel = c.getMaxFuel();
					fractions.put(c, loss);
				}
				
				loss.lostCargo += data.getMember().getCargoCapacity();
				loss.lostFuel += data.getMember().getFuelCapacity();
				
				loss.maxCargo += data.getMember().getCargoCapacity();
				loss.maxFuel += data.getMember().getFuelCapacity();
				
				totalLoss += loss.maxCargo + loss.maxFuel;
			} else {
				lostCargo += data.getMember().getCargoCapacity();
				lostFuel += data.getMember().getFuelCapacity();
				
				maxCargo += data.getMember().getCargoCapacity();
				maxFuel += data.getMember().getFuelCapacity();
				
				totalLoss += maxCargo + maxFuel;
			}
			
		}
		
		//if (lostCargo <= 0 && lostFuel <= 0) {
		if (totalLoss <= 0) {
			return;
		}
		
		if (maxCargo < 1) maxCargo = 1;
		if (maxFuel < 1) maxFuel = 1;
		
		float recoveryFraction = Global.getSettings().getFloat("salvageCargoFraction");
		
		if (battle.isPlayerSide(battle.getSideFor(winner.getFleet()))) {
			float playerContribMult = computePlayerContribFraction();
			recoveryFraction *= playerContribMult;
		}
		
		float cargoFractionLost = lostCargo / maxCargo;
		float fuelFractionLost = lostFuel / maxFuel;
		if (lostCargo > maxCargo) cargoFractionLost = 1f;
		if (lostFuel > maxFuel) fuelFractionLost = 1f;
		
		
		List<CampaignFleetAPI> losers = battle.getSnapshotSideFor(loser.getFleet());
		if (losers == null) return;
		
		List<LootableCargoStack> stacks = new ArrayList<LootableCargoStack>();
		for (CampaignFleetAPI curr : losers) {
			for (CargoStackAPI stack : curr.getCargo().getStacksCopy()) {
				stacks.add(new LootableCargoStack(curr.getCargo(), stack));
			}
		}
		
		for (LootableCargoStack stack : stacks) {
			if (stack.stack.isNull()) continue;
			if (stack.stack.isPersonnelStack()) continue;
			if (stack.stack.getSize() < 1) continue;
			
			float actualCargoFractionLost = cargoFractionLost;
			float actualFuelFractionLost = fuelFractionLost;
			LossFraction loss = fractions.get(stack.source);
			if (loss != null) {
				actualCargoFractionLost = loss.lostCargo / loss.maxCargo;
				actualFuelFractionLost = loss.lostFuel / loss.maxFuel;
				if (loss.lostCargo > loss.maxCargo) actualCargoFractionLost = 1f;
				if (loss.lostFuel > loss.maxFuel) actualFuelFractionLost = 1f;
			}
			
			
			if (takingFromPlayer) {
				if (stack.stack.isSpecialStack()) continue;
				if (stack.stack.isCommodityStack()) {
					CommoditySpecAPI spec = stack.stack.getResourceIfResource();
					if (spec != null && spec.hasTag(Commodities.TAG_NO_LOSS_FROM_COMBAT)){
						continue;
					}
				}
			}
			
			float numLost = 0;
			float numTaken = 0;
			if (stack.stack.isFuelStack()) {
				numLost = actualFuelFractionLost * stack.stack.getSize();
				numTaken = Math.round(numLost * (0.5f + random.nextFloat() * 0.5f));
			} else {
				numLost = actualCargoFractionLost * stack.stack.getSize();
				numTaken = Math.round(numLost * (0.5f + random.nextFloat() * 0.5f));
			}
			
			if (numLost < 1) {
				if (random.nextFloat() < numLost) {
					numLost = 1;
				} else {
					numLost = 0;
					numTaken = 0;
				}
			}
			
			if (numLost <= 0) continue;
			
			stack.stack.add(-numLost);
			if (numTaken * recoveryFraction >= 1) {
				loot.addItems(stack.stack.getType(), stack.stack.getData(), numTaken * recoveryFraction);
			}
		}
		
		for (CampaignFleetAPI fleet : battle.getSideFor(loser.getFleet())) {
			if (fleet.isPlayerFleet()) {
				fleet.getCargo().sort();
				break;
			}
		}
	}
	

	public CargoAPI getLoot() {
		return loot;
	}

	protected void lootHullMods(FleetMemberAPI member, ShipVariantAPI variant, float mult) {
		if (variant == null) return;
		if (member.isFighterWing()) return;
		Random random = Misc.random;
		if (salvageRandom != null) random = salvageRandom;
		
		float p = Global.getSettings().getFloat("salvageHullmodProb");
		float pItem = Global.getSettings().getFloat("salvageHullmodRequiredItemProb");
		
		for (String id : variant.getHullMods()) {
			if (!variant.getHullSpec().isBuiltInMod(id)) {
				if (random.nextFloat() < pItem && random.nextFloat() < mult) {
					HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
					CargoStackAPI item = spec.getEffect().getRequiredItem();
					if (item != null) {
						boolean addToLoot = true;
						if (item.getSpecialItemSpecIfSpecial() != null && item.getSpecialItemSpecIfSpecial().hasTag(Tags.NO_DROP)) {
							addToLoot = false;
						} else if (item.getResourceIfResource() != null && item.getResourceIfResource().hasTag(Tags.NO_DROP)) {
							addToLoot = false;
						} else if (item.getFighterWingSpecIfWing() != null && item.getFighterWingSpecIfWing().hasTag(Tags.NO_DROP)) {
							addToLoot = false;
						} else if (item.getWeaponSpecIfWeapon() != null && item.getWeaponSpecIfWeapon().hasTag(Tags.NO_DROP)) {
							addToLoot = false;
						}
						if (addToLoot) {
							loot.addItems(item.getType(), item.getData(), 1);
						}
					}
				}
			}
			
			//if (random.nextFloat() > mult) continue;
			if (random.nextFloat() < p && random.nextFloat() < mult) {
				HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
				boolean known = Global.getSector().getPlayerFaction().knowsHullMod(id);
				if (DebugFlags.ALLOW_KNOWN_HULLMOD_DROPS) known = false;
				if (known || spec.isHidden() || spec.isHiddenEverywhere()) continue;
				//if (spec.isAlwaysUnlocked()) continue;
				if (spec.hasTag(Tags.HULLMOD_NO_DROP)) continue;
				
				loot.addHullmods(id, 1);
			}
		}
		
		for (String slotId : variant.getModuleSlots()) {
			WeaponSlotAPI slot = variant.getSlot(slotId);
			if (slot.isStationModule()) {
				ShipVariantAPI module = variant.getModuleVariant(slotId);
				if (module == null) continue;
				lootHullMods(member, module, mult);
			}
		}
	}
	
	protected void lootWings(FleetMemberAPI member, ShipVariantAPI variant, boolean own, float mult) {
		if (variant == null) return;
		if (member.isFighterWing()) return;
		Random random = Misc.random;
		if (salvageRandom != null) random = salvageRandom;
		
		float p = Global.getSettings().getFloat("salvageWingProb");
		if (own) {
			p = Global.getSettings().getFloat("salvageOwnWingProb");
			p = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.OWN_WING_RECOVERY_MOD, p);
		} else {
			p = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.ENEMY_WING_RECOVERY_MOD, p);
		}
		
		boolean alreadyStripped = recoverableShips.contains(member);
		
		for (String id : variant.getNonBuiltInWings()) {
			if (!alreadyStripped) {
				if (random.nextFloat() > mult) continue;
				if (random.nextFloat() > p) continue;
			}
				
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
			if (spec.hasTag(Tags.WING_NO_DROP)) continue;
			loot.addItems(CargoItemType.FIGHTER_CHIP, id, 1);
		}
		
		for (String slotId : variant.getModuleSlots()) {
			WeaponSlotAPI slot = variant.getSlot(slotId);
			if (slot.isStationModule()) {
				ShipVariantAPI module = variant.getModuleVariant(slotId);
				if (module == null) continue;
				lootWings(member, module, own, mult);
			}
		}
	}
	
	protected void lootWeapons(FleetMemberAPI member, ShipVariantAPI variant, boolean own, float mult, boolean lootingModule) {
		if (variant == null) return;
		if (member.isFighterWing()) return;
		
//		if (own) {
//			System.out.println("238034wefwef");
//		}
		//isUnremovable(
		if (own && !lootingModule && member.getCaptain() != null &&
				member.getCaptain().getMemoryWithoutUpdate().contains("$aiCoreIdForRecovery") &&
				//member.getCaptain().isAICore() && 
				!Misc.isUnremovable(member.getCaptain())) {
			//loot.addItems(CargoItemType.RESOURCES, member.getCaptain().getAICoreId(), 1);
			loot.addItems(CargoItemType.RESOURCES,
						  member.getCaptain().getMemoryWithoutUpdate().getString("$aiCoreIdForRecovery"), 1);
		}
		
		if (own) {
			HullModItemManager.getInstance().giveBackAllItems(member, loot);
		}
		
		
		Random random = Misc.random;
		if (salvageRandom != null) random = salvageRandom;
		
		String coreIdOverride = null;
		if (member.getCaptain() != null && 
				member.getCaptain().getMemoryWithoutUpdate().contains("$aiCoreIdForPossibleRecovery")) {
			coreIdOverride = member.getCaptain().getMemoryWithoutUpdate().getString("$aiCoreIdForPossibleRecovery");
		}
		if (!own && !lootingModule && 
				(member.getCaptain().isAICore() || coreIdOverride != null) &&
				!variant.hasTag(Tags.VARIANT_DO_NOT_DROP_AI_CORE_FROM_CAPTAIN)) {
			String cid = member.getCaptain().getAICoreId();
			if (coreIdOverride != null) {
				cid = coreIdOverride;
			}
			if (cid != null) {
				CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(cid);
				if (!spec.hasTag(Tags.NO_DROP)) {
					float prob = Global.getSettings().getFloat("drop_prob_officer_" + cid);
					if (member.isStation()) {
						prob *= Global.getSettings().getFloat("drop_prob_mult_ai_core_station");
					} else if (member.isFrigate()) {
						prob *= Global.getSettings().getFloat("drop_prob_mult_ai_core_frigate");
					} else if (member.isDestroyer()) {
						prob *= Global.getSettings().getFloat("drop_prob_mult_ai_core_destroyer");
					} else if (member.isCruiser()) {
						prob *= Global.getSettings().getFloat("drop_prob_mult_ai_core_cruiser");
					} else if (member.isCapital()) {
						prob *= Global.getSettings().getFloat("drop_prob_mult_ai_core_capital");
					}
					if (prob > 0 && random.nextFloat() < prob) {
						loot.addItems(CargoItemType.RESOURCES, cid, 1);
					}
				}
			}
			
		}
		
		float p = Global.getSettings().getFloat("salvageWeaponProb");
		if (own) {
			p = Global.getSettings().getFloat("salvageOwnWeaponProb");
			p = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.OWN_WEAPON_RECOVERY_MOD, p);
		} else {
			p = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.ENEMY_WEAPON_RECOVERY_MOD, p);
		}
		boolean alreadyStripped = recoverableShips.contains(member);
		

		Set<String> remove = new HashSet<String>();
		
		// there's another failsafe for OMEGA specifically, see SalvageDefenderInteraction.postPlayerSalvageGeneration()
		if (variant.hasTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS)) {
			for (String slotId : variant.getNonBuiltInWeaponSlots()) {
				String weaponId = variant.getWeaponId(slotId);
				if (weaponId == null) continue;
				if (loot.getNumWeapons(weaponId) <= 0) {
					WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponId);
					if (spec.hasTag(Tags.NO_DROP)) continue;
					
					loot.addWeapons(weaponId, 1);
					remove.add(slotId);
				}
			}
		}
		
		for (String slotId : variant.getNonBuiltInWeaponSlots()) {
			if (remove.contains(slotId)) continue;
			//if ((float) Math.random() * mult > 0.75f) {
			if (!alreadyStripped) {
				if (random.nextFloat() > mult) continue;
				if (random.nextFloat() > p) continue;
			}
			
			String weaponId = variant.getWeaponId(slotId);
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponId);
			if (spec.hasTag(Tags.NO_DROP)) continue;
			
			loot.addItems(CargoAPI.CargoItemType.WEAPONS, weaponId, 1);
			remove.add(slotId);
		}
		
		
		for (String slotId : variant.getModuleSlots()) {
			WeaponSlotAPI slot = variant.getSlot(slotId);
			if (slot.isStationModule()) {
				ShipVariantAPI module = variant.getModuleVariant(slotId);
				if (module == null) continue;
				lootWeapons(member, module, own, mult, true);
			}
		}		
		// DO NOT DO THIS - no point in removing them here since the ship is scrapped
		// and would need to clone the variant to do this right
//		for (String slotId : remove) {
//			variant.clearSlot(slotId);
//		}
		//System.out.println("Cleared variant: " + variant.getHullVariantId());
	}
	
	public void autoLoot() {
		DataForEncounterSide winner = getWinnerData();
		DataForEncounterSide loser = getLoserData();
		if (winner == null || loser == null) return;
		
		List<CampaignFleetAPI> winners = battle.getSideFor(winner.getFleet());
		WeightedRandomPicker<CampaignFleetAPI> picker = new WeightedRandomPicker<CampaignFleetAPI>();
		for (CampaignFleetAPI curr : winners) {
			picker.add(curr, curr.getFleetPoints());
		}
		for (CargoStackAPI stack : loot.getStacksCopy()) {
			if (stack.isNull() || stack.isFuelStack()) continue;
			
			CampaignFleetAPI pick = picker.pick();
			if (pick == null) break;
			
			CargoAPI winnerCargo = pick.getCargo();
			float spaceLeft = winnerCargo.getSpaceLeft();
			if (spaceLeft <= 0) {
				picker.remove(pick);
				continue;
			}
			
			float spacePerUnit = stack.getCargoSpacePerUnit();
			float maxUnits = (int) (spaceLeft / spacePerUnit);
			if (maxUnits > stack.getSize()) maxUnits = stack.getSize();
			maxUnits = Math.round(maxUnits * (Math.random() * 0.5f + 0.5f));
			winnerCargo.addItems(stack.getType(), stack.getData(), maxUnits);
		}
		
		picker.clear();
		for (CampaignFleetAPI curr : winners) {
			picker.add(curr, curr.getFleetPoints());
		}
		for (CargoStackAPI stack : loot.getStacksCopy()) {
			if (stack.isNull() || !stack.isFuelStack()) continue;
			
			CampaignFleetAPI pick = picker.pick();
			if (pick == null) break;
			
			CargoAPI winnerCargo = pick.getCargo();
			float spaceLeft = winnerCargo.getMaxCapacity() - winnerCargo.getFuel();
			if (spaceLeft <= 0) {
				picker.remove(pick);
				continue;
			}
			
			float spacePerUnit = stack.getCargoSpacePerUnit();
			float maxUnits = (int) (spaceLeft / spacePerUnit);
			if (maxUnits > stack.getSize()) maxUnits = stack.getSize();
			maxUnits = Math.round(maxUnits * (Math.random() * 0.5f + 0.5f));
			winnerCargo.addItems(stack.getType(), stack.getData(), maxUnits);
		}
	}

	public boolean hasWinnerAndLoser() {
		return getWinner() != null && getLoser() != null;
	}
	
	public CampaignFleetAPI getWinner() {
		return getWinnerData() != null ? getWinnerData().getFleet() : null;
	}
	public CampaignFleetAPI getLoser() {
		return getLoserData() != null ? getLoserData().getFleet() : null;
	}
	
	public boolean canOutrunOtherFleet(CampaignFleetAPI fleet, CampaignFleetAPI other) {
		return fleet.getFleetData().getMinBurnLevel() >= other.getFleetData().getMaxBurnLevel() + 1f;
	}
	

	protected void applyResultToFleets(EngagementResultAPI result) {
//		applyCrewAndShipLosses(result);
//		fixFighters(result.getWinnerResult());
//		fixFighters(result.getLoserResult());
		applyShipLosses(result);	
		applyCrewLosses(result);
	}

	
	public void fixFighters(EngagementResultForFleetAPI result) {
		Set<CampaignFleetAPI> fleetsWithDecks = new HashSet<CampaignFleetAPI>();
		for (FleetMemberAPI curr : result.getReserves()) {
			if (battle.getSourceFleet(curr) == null) continue;
			if (curr.isMothballed()) continue;
			if (curr.getNumFlightDecks() > 0) {
				fleetsWithDecks.add(battle.getSourceFleet(curr));
			}
		}
		for (FleetMemberAPI curr : result.getDeployed()) {
			if (battle.getSourceFleet(curr) == null) continue;
			if (curr.isMothballed()) continue;
			if (curr.getNumFlightDecks() > 0) {
				fleetsWithDecks.add(battle.getSourceFleet(curr));
			}
		}
		for (FleetMemberAPI curr : result.getRetreated()) {
			if (battle.getSourceFleet(curr) == null) continue;
			if (curr.isMothballed()) continue;
			if (curr.getNumFlightDecks() > 0) {
				fleetsWithDecks.add(battle.getSourceFleet(curr));
			}
		}
		
		List<FleetMemberAPI> saved = new ArrayList<FleetMemberAPI>();
		for (FleetMemberAPI curr : result.getDestroyed()) {
			if (battle.getSourceFleet(curr) == null) continue;
			if (!fleetsWithDecks.contains(battle.getSourceFleet(curr))) continue;
			if (curr.isFighterWing()) {
				saved.add(curr);
			}
		}
		
		result.getDestroyed().removeAll(saved);
		result.getRetreated().addAll(saved);
		
		
		List<FleetMemberAPI> toRepair = new ArrayList<FleetMemberAPI>();
		toRepair.addAll(result.getDeployed());
		toRepair.addAll(result.getRetreated());
		for (FleetMemberAPI curr : toRepair) {
			if (battle.getSourceFleet(curr) == null) continue;
			if (curr.isFighterWing()) {
				if (fleetsWithDecks.contains(battle.getSourceFleet(curr))) {
					curr.getStatus().repairFully();
				} else {
					curr.getStatus().repairFullyNoNewFighters();
				}
			}
		}
		
	}
	
	protected void applyCrewLosses(EngagementResultAPI result) {
		EngagementResultForFleetAPI winner = result.getWinnerResult();
		EngagementResultForFleetAPI loser = result.getLoserResult();
		
		//boolean playerInvolved = winner.getFleet().isPlayerFleet() || loser.getFleet().isPlayerFleet();
		boolean playerInvolved = battle.isPlayerInvolved();
		calculateAndApplyCrewLosses(winner, playerInvolved);
		calculateAndApplyCrewLosses(loser, playerInvolved);
		
//		applyCrewLosses(winner);
//		applyCrewLosses(loser);
	}
	
	protected void applyShipLosses(EngagementResultAPI result) {
		EngagementResultForFleetAPI winner = result.getWinnerResult();
		EngagementResultForFleetAPI loser = result.getLoserResult();
		
		applyShipLosses(winner);
		applyShipLosses(loser);
		
		applyCREffect(winner);
		applyCREffect(loser);
	}
	
	protected Map<FleetMemberAPI, Float> preEngagementCRForWinner = new HashMap<FleetMemberAPI, Float>();
	protected void applyCREffect(EngagementResultForFleetAPI result) {
		boolean wonBattle = result.isWinner();
		if (wonBattle) {
			preEngagementCRForWinner.clear();
			for (FleetMemberAPI member : result.getFleet().getFleetData().getMembersListCopy()) {
				preEngagementCRForWinner.put(member, member.getRepairTracker().getCR());
			}
		}
		
		List<FleetMemberAPI> applyDeployCostTo = new ArrayList<FleetMemberAPI>(result.getDeployed());
		
		for (FleetMemberAPI member : result.getDisabled()) {
			// does not work, needs more things changed to work
			//float mult = member.getStats().getDynamic().getValue(Stats.CR_LOSS_WHEN_DISABLED_MULT);
			float mult = 1f;
			if (mult > 0) {
				member.getRepairTracker().applyCREvent(-1f * mult, "disabled in combat");
			}
			if (mult < 1) {
				applyDeployCostTo.add(member);
			}
		}
		for (FleetMemberAPI member : result.getDestroyed()) {
//			if (member.getHullId().equals("vanguard_pirates")) {
//				System.out.println("efwefwef");
//			}
			//float mult = member.getStats().getDynamic().getValue(Stats.CR_LOSS_WHEN_DISABLED_MULT);
			float mult = 1f;
			if (mult > 0) {
				member.getRepairTracker().applyCREvent(-1f * mult, "disabled in combat");
			}
			if (mult < 1) {
				applyDeployCostTo.add(member);
			}
		}
		
		for (FleetMemberAPI member : applyDeployCostTo) {
			float deployCost = getDeployCost(member);
			if (member.isFighterWing()) {
				member.getRepairTracker().applyCREvent(-deployCost, "wing deployed in combat");
			} else {
				member.getRepairTracker().applyCREvent(-deployCost, "deployed in combat");
			}
			
			applyExtendedCRLossIfNeeded(result, member);
		}
		
		//float retreatLossMult = StarfarerSettings.getCRLossMultForRetreatInLoss();
		float retreatLossMult = Global.getSettings().getFloat("crLossMultForRetreatInLoss");
		for (FleetMemberAPI member : result.getRetreated()) {
			float deployCost = getDeployCost(member);
			if (member.isFighterWing()) {
				member.getRepairTracker().applyCREvent(-deployCost, "wing deployed in combat");
			} else {
				member.getRepairTracker().applyCREvent(-deployCost, "deployed in combat");
			}
			
			applyExtendedCRLossIfNeeded(result, member);
			
			if (!wonBattle && result.getGoal() != FleetGoal.ESCAPE) {
				float retreatCost = deployCost * retreatLossMult;
				if (retreatCost > 0) {
					member.getRepairTracker().applyCREvent(-retreatCost, "retreated from lost engagement");
				}
			}
		}
		
//		// important, so that in-combat Ship objects can be garbage collected.
//		// Probably some combat engine references in there, too. 
//		// NOTE: moved this elsewhere in this class
//		result.resetAllEverDeployed();
//		getDataFor(result.getFleet()).getMemberToDeployedMap().clear();
	}
	
	
//	protected void saveAmmoState(FleetMemberAPI member, ShipAPI ship) {
//		if (ship == null) return;
//		
//		Map<String, Integer> ammo = member.getAmmoStateAtEndOfLastEngagement();
//		for (WeaponAPI w : ship.getAllWeapons()) {
//			if (w.usesAmmo() && w.getAmmoPerSecond() <= 0) {
//				ammo.put(w.getSlot().getId(), w.getAmmo());
//			}
//		}
//	}
	
	/**
	 * Only matters in non-auto-resolved battles.
	 * @param member
	 */
	protected void applyExtendedCRLossIfNeeded(EngagementResultForFleetAPI result, FleetMemberAPI member) {
		DeployedFleetMemberAPI dfm = getDataFor(result.getFleet()).getMemberToDeployedMap().get(member);
		if (dfm == null) return;
		
		if (battle != null && battle.getSourceFleet(member) == null) {
			return;
		}
		if (member.getFleetCommander() == null) {
			return;
		}
		
		if (dfm.getMember() == member && dfm.isFighterWing()) {
			//float finalCR = dfm.getShip().getRemainingWingCR();
			float cr = member.getRepairTracker().getBaseCR();
			float finalCR = cr;
			if (cr > finalCR) {
				member.getRepairTracker().applyCREvent(-(cr - finalCR), "deployed replacement chassis in combat");
			}
			return;
		}
		if (dfm.getMember() == member && !dfm.isFighterWing()) {
			float deployCost = getDeployCost(member);
			float endOfCombatCR = dfm.getShip().getCurrentCR() - deployCost;
			float cr = member.getRepairTracker().getCR();
			if (cr > endOfCombatCR) {
				member.getRepairTracker().applyCREvent(-(cr - endOfCombatCR), "extended deployment");
			}
			
			ShipAPI ship = dfm.getShip();
			if (dfm.getShip() != null && !dfm.isFighterWing()) {
				float wMult = Global.getSettings().getFloat("crLossMultForWeaponDisabled");
				float eMult = Global.getSettings().getFloat("crLossMultForFlameout");
				float mMult = Global.getSettings().getFloat("crLossMultForMissilesFired");
				float hMult = Global.getSettings().getFloat("crLossMultForHullDamage");
				
				float hullDamageFraction = ship.getHullLevelAtDeployment() - ship.getLowestHullLevelReached();
				float hullDamageCRLoss = hullDamageFraction * hMult;
				hullDamageCRLoss *= ship.getMutableStats().getDynamic().getValue(Stats.HULL_DAMAGE_CR_LOSS);
				if (hullDamageCRLoss > 0) {
					member.getRepairTracker().applyCREvent(-hullDamageCRLoss, "hull damage sustained");
				}
				
				member.getStatus().setHullFraction(ship.getLowestHullLevelReached());
				
				
				float instaRepairFraction = member.getStats().getDynamic().getValue(Stats.INSTA_REPAIR_FRACTION, 0f);
				if (instaRepairFraction > 0) {
					float hullDamage = member.getStatus().getHullDamageTaken();
					float armorDamage = member.getStatus().getArmorDamageTaken();
					
					member.getStatus().repairArmorAllCells(armorDamage * instaRepairFraction); 
					member.getStatus().repairHullFraction(hullDamage * instaRepairFraction); 
				}
				
				
				float totalDisabled = 0f;
				MutableCharacterStatsAPI stats = member.getFleetCommander().getStats();
				float maxOP = ship.getVariant().getHullSpec().getOrdnancePoints(stats);
				if (maxOP <= 1) maxOP = 1;
				
				for (WeaponAPI w : ship.getDisabledWeapons()) {
					totalDisabled += w.getSpec().getOrdnancePointCost(stats, ship.getVariant().getStatsForOpCosts()) * wMult;
				}
				if (ship.getNumFlameouts() > 0) {
					totalDisabled += maxOP * eMult;
				}
				
				float damageBasedCRLoss = Math.min(1f, totalDisabled / maxOP);
				if (damageBasedCRLoss > 0) {
					member.getRepairTracker().applyCREvent(-damageBasedCRLoss, "weapon and engine damage sustained");
				}
				
				float missileReloadOP = 0f;
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.getType() == WeaponType.MISSILE && w.usesAmmo()) {
						missileReloadOP += (1f - (float) w.getAmmo() / (float) w.getMaxAmmo()) * w.getSpec().getOrdnancePointCost(stats, ship.getVariant().getStatsForOpCosts()) * mMult;
					}
				}
				
				float missileReloadLoss = Math.min(1f, missileReloadOP / maxOP);
				if (missileReloadLoss > 0) {
					member.getRepairTracker().applyCREvent(-missileReloadLoss, "missile weapons used in combat");
				}
			}
			
			return;
		}
	}
	
	
	protected void applyShipLosses(EngagementResultForFleetAPI result) {
		for (FleetMemberAPI member : result.getDestroyed()) {
			if (battle.getSourceFleet(member) == null) continue;
			battle.getSourceFleet(member).removeFleetMemberWithDestructionFlash(member);
			result.getFleet().getFleetData().removeFleetMember(member);
		}
		for (FleetMemberAPI member : result.getDisabled()) {
			if (battle.getSourceFleet(member) == null) continue;
			battle.getSourceFleet(member).removeFleetMemberWithDestructionFlash(member);
			result.getFleet().getFleetData().removeFleetMember(member);
		}
	}
	
//	protected void applyCrewLosses(EngagementResultForFleetAPI result) {
//		CargoAPI cargo = result.getFleet().getCargo();
//		DataForEncounterSide data = getDataFor(result.getFleet());
//		CrewCompositionAPI crewLosses = data.getCrewLossesDuringLastEngagement();
//		
//		cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, CargoAPI.CrewXPLevel.GREEN.getId(), crewLosses.getGreen());
//		cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, CargoAPI.CrewXPLevel.REGULAR.getId(), crewLosses.getRegular());
//		cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, CargoAPI.CrewXPLevel.VETERAN.getId(), crewLosses.getVeteran());
//		cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, CargoAPI.CrewXPLevel.ELITE.getId(), crewLosses.getElite());
//		
//		cargo.removeMarines((int) crewLosses.getMarines());
//	}

	protected float computeLossFraction(FleetMemberAPI member, EngagementResultForFleetAPI result, float hullFraction, float hullDamage) {
		if (member == null && hullFraction == 0) {
			return (0.75f + (float) Math.random() * 0.25f);
		}
		
		//System.out.println("hullDamage: " + hullDamage);
		if (member.isFighterWing() && result != null) {
			//System.out.println("Fighter hullDamage: " + hullDamage);
			float extraLossMult = hullDamage;
			DeployedFleetMemberAPI dfm = getDataFor(result.getFleet()).getMemberToDeployedMap().get(member);
			if (dfm != null && dfm.getMember() == member) {
				//float finalCR = dfm.getShip().getRemainingWingCR();
				float cr = member.getRepairTracker().getCR();
				float finalCR = cr;
				if (cr > finalCR) {
					float crPer = dfm.getMember().getStats().getCRPerDeploymentPercent().computeEffective(dfm.getMember().getVariant().getHullSpec().getCRToDeploy()) / 100f;
					float extraCraftLost = (cr - finalCR) / crPer;
					float wingSize = dfm.getMember().getNumFightersInWing();
					if (extraCraftLost >= 1) {
						extraLossMult = hullDamage + extraCraftLost / wingSize;
					}
				}
			}
			return (0.25f + (float) Math.random() * 0.75f * (float) Math.random()) * member.getStats().getCrewLossMult().getModifiedValue() * extraLossMult;
		}
		
		
		float extraFromFighters = 0f;
		if (!member.isFighterWing() && result != null) {
			DeployedFleetMemberAPI dfm = getDataFor(result.getFleet()).getMemberToDeployedMap().get(member);
			if (dfm != null && dfm.getMember() == member) {
				float craftCrewLoss = 0;
				for (FighterLaunchBayAPI bay : dfm.getShip().getLaunchBaysCopy()) {
					if (bay.getWing() == null || bay.getWing().getLeader() == null) continue;
					float baseCrew = bay.getWing().getLeader().getHullSpec().getMinCrew();
					float perCraft = bay.getWing().getLeader().getMutableStats().getMinCrewMod().computeEffective(baseCrew);
					perCraft *= bay.getWing().getLeader().getMutableStats().getDynamic().getValue(Stats.FIGHTER_CREW_LOSS_MULT);
					craftCrewLoss += perCraft * bay.getNumLost();
				}
				
				float baseLossFraction = Global.getSettings().getFloat("fighterCrewLossBase");
				craftCrewLoss *= baseLossFraction;
				
				float memberCrew = member.getMinCrew();
				if (memberCrew > 0) {
					float threshold = memberCrew * 0.33f;
					
					float actualLost = 0f;
					float mult = 1f;
					do {
						float curr = Math.min(craftCrewLoss, threshold);
						craftCrewLoss -= curr;
						
						curr *= mult;
						actualLost += curr;
						mult /= 2f;
						
					} while (craftCrewLoss > 0);
					
					extraFromFighters = actualLost / memberCrew;
					extraFromFighters *= member.getStats().getDynamic().getValue(Stats.FIGHTER_CREW_LOSS_MULT);
				}
			}
		}
		
		if (hullFraction == 0) {
			return Math.min(1f, (0.75f + (float) Math.random() * 0.25f) * member.getStats().getCrewLossMult().getModifiedValue() + extraFromFighters); 
		}
		return Math.min(1f, hullDamage * hullDamage * (0.5f + (float) Math.random() * 0.5f) * member.getStats().getCrewLossMult().getModifiedValue() + extraFromFighters);
	}
	
	
	
	protected float computeRecoverableFraction(FleetMemberAPI member, EngagementResultForFleetAPI result, float hullFraction, float hullDamage) {
		float f = 1f - computeLossFraction(member, result, hullFraction, hullDamage);
		if (f < 0) f = 0;
		return f;
	}
	
	public void calculateAndApplyCrewLosses(EngagementResultForFleetAPI result, boolean playerInvolved) {
		boolean wonBattle = result.isWinner(); 
		
		DataForEncounterSide data = getDataFor(result.getFleet());
		CrewCompositionAPI recoverable = data.getRecoverableCrewLosses();
		//recoverable.removeAllCrew();
		
		List<FleetMemberAPI> all = new ArrayList<FleetMemberAPI>();
		all.addAll(result.getDisabled());
		all.addAll(result.getDeployed());
		all.addAll(result.getDestroyed());
		all.addAll(result.getRetreated());
		all.addAll(result.getReserves());
		
		for (FleetMemberAPI member : result.getReserves()) {
			member.getStatus().resetDamageTaken();
		}
		
		CrewCompositionAPI playerLosses = data.getCrewLossesDuringLastEngagement();
		playerLosses.removeAllCrew();

		CrewCompositionAPI crewLosses = Global.getFactory().createCrewComposition();
		crewLosses.removeAllCrew();
		
		CampaignFleetAPI playerFleet = null;
		//float maxExtraLoss = 0f;
		float playerCapacityLost = 0f;
		for (FleetMemberAPI member : all) {
			if (battle.getSourceFleet(member) == null) continue;
			boolean player = battle.getSourceFleet(member) != null && battle.getSourceFleet(member).isPlayerFleet();
			CrewCompositionAPI c = member.getCrewComposition();
			//float hull = member.getStatus().getHullFraction();
			float hullDamage = member.getStatus().getHullDamageTaken();
			float hullFraction = member.getStatus().getHullFraction();
			member.getStatus().resetDamageTaken();
			
			//if (hullDamage > 0 && !result.getFleet().isPlayerFleet() && playerInvolved) {
//			if (hullDamage > 0 && playerInvolved &&
//					!battle.getPlayerSide().contains(battle.getSourceFleet(member))) {
//				playerDidSeriousDamage = true;
//			}
//			if (lostBattle) {
//				System.out.println("HERE");
//			}
			
			float f1 = computeLossFraction(member, result, hullFraction, hullDamage);
			
			// ship is disabled or destroyed, lose all crew for now, but it may be recovered later
			if (result.getDisabled().contains(member) || result.getDestroyed().contains(member)) {
				if (playerInvolved &&
						!battle.getPlayerSide().contains(battle.getSourceFleet(member))) {
					playerDidSeriousDamage = true;
				}
				if (player) {
					if (f1 < 1) {
						recoverable.addCrew((1f - f1) * c.getCrew());
					}
					playerLosses.addCrew(c.getCrew() * 1f);
					playerCapacityLost += member.getMaxCrew();
				}

				crewLosses.addCrew(c.getCrew() * 1f);
				c.setCrew(0);
				//c.addCrew(-c.getCrew() * f1);
				// c should now be left with the appropriate crew composition (base minus losses) to use
				// as a starting point for boarding actions
				
			} else {
				// the ship is still ok, only lose the non-recoverable casualties
				// for fighters, which can lose more than their actual max crew 
				if (f1 > 1) {
					crewLosses.addCrew((f1 - 1) * c.getCrew());
				}
				
				float lost = c.getCrew() * f1;
				// both fighters and normal ships
				c.transfer(lost, crewLosses);
				
				if (player) {
					playerLosses.addCrew(lost);
					playerCapacityLost += member.getMaxCrew() * f1;
				}
			}
			
			if (battle.getSourceFleet(member).isPlayerFleet() && 
					(crewLosses.getCrew() > 0 || crewLosses.getMarines() > 0)) {
				playerFleet = battle.getSourceFleet(member);
			}
			
			CargoAPI cargo = battle.getSourceFleet(member).getCargo();
			cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, (int)crewLosses.getCrew());
			cargo.removeMarines((int) crewLosses.getMarines());
			crewLosses.clear();
		}
		
		// lose over-capacity crew
		if (playerFleet != null) {
			playerFleet.getFleetData().updateCargoCapacities();
			CargoAPI cargo = playerFleet.getCargo();
			float maxCrew = cargo.getMaxPersonnel();
			float totalCrew = cargo.getTotalCrew();
			float marines = cargo.getMarines();
			float recoverableTotal = recoverable.getCrew() + recoverable.getMarines();
			
			//recoverableTotal = 0f; // uncomment to let recoverable crew not be lost due to being over capacity
			
			float total = totalCrew + marines + recoverableTotal;
			if (maxCrew + playerCapacityLost > 0) {
				total *= playerCapacityLost / (maxCrew + playerCapacityLost);
			}
//			if (!wonBattle) {
//				total = totalCrew + marines;
//				recoverableTotal = 0f;
//			}
			//if (total > playerOvercapLosses) total = playerOvercapLosses;
			if (total > maxCrew) {
				//float toLose = Math.min(maxExtraLoss, total - maxCrew);
				float toLose = total - maxCrew;
				if (toLose > 0) {
					//recoverable.clear();
					recoverable.transfer(Math.min(recoverableTotal, toLose), null);
					toLose -= recoverableTotal;
					total -= recoverableTotal;
					
					if (toLose > 0 && total > 0) {
						float crew = cargo.getCrew(); // this is 99% the same as totalCrew, but leaving as is for now
	
						crewLosses.clear();
						crewLosses.addCrew((int)Math.ceil(crew / total * toLose));
						crewLosses.addMarines((int)Math.ceil(marines / total * toLose));
						
						playerLosses.addCrew(crewLosses.getCrew() * 1f);
						playerLosses.addMarines(crewLosses.getMarines() * 1f);
						
						cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, (int)crewLosses.getCrew());
						cargo.removeMarines((int) crewLosses.getMarines());
						crewLosses.clear();
					}
				}
			}
		}
		
	}

	public void recoverCrew(CampaignFleetAPI fleet) {
		if (battle.isPlayerSide(battle.getSideFor(fleet))) {
			DataForEncounterSide data = getDataFor(fleet);
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			CrewCompositionAPI rec = data.getRecoverableCrewLosses();
			
			cargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, rec.getCrew());
			
			cargo.addMarines((int) rec.getMarines());
		}
	}
	
	
	
	public float getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(float difficulty) {
		this.difficulty = difficulty;
	}
	
	public boolean isComputedDifficulty() {
		return computedDifficulty;
	}

	public void setComputedDifficulty(boolean computedDifficulty) {
		this.computedDifficulty = computedDifficulty;
	}

	public static float MAX_XP_MULT = 6f;
	
	protected float difficulty = 1f;
	protected boolean computedDifficulty = false;
	public float computeBattleDifficulty() {
		if (computedDifficulty) return difficulty;
		
		computedDifficulty = true;
		if (battle == null || !battle.isPlayerInvolved()) {
			difficulty = 1f;
			return difficulty;
		}
		
		float scorePlayer = 0f;
		float scoreEnemy = 0f;
		
		float officerBase = 30;
		float officerPerLevel = 15; 
		//float baseMult = 0.2f;
		float baseMult = 2f;
		float dModMult = 0.9f;
		
		for (FleetMemberAPI member : battle.getNonPlayerCombined().getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			float mult = baseMult;
			if (member.isStation()) mult *= 1f;
			else if (member.isCivilian()) mult *= 0.25f;
			if (member.getCaptain() != null && !member.getCaptain().isDefault()) {
				scoreEnemy += officerBase + officerPerLevel * Math.max(1f, member.getCaptain().getStats().getLevel());
			}
			int dMods = DModManager.getNumDMods(member.getVariant());
			for (int i = 0; i < dMods; i++) {
				mult *= dModMult;
			}
//			String prefix = "battle_difficulty_mult_";
//			for (String tag : member.getHullSpec().getTags()) {
//				if (tag.startsWith(prefix)) {
//					tag = tag.replaceFirst(prefix, "");
//					mult *= Float.parseFloat(tag);
//					break;
//				}
//			}
			//scoreEnemy += member.getUnmodifiedDeploymentPointsCost() * mult;
			scoreEnemy += member.getFleetPointCost() * mult;
		}
		scoreEnemy *= 0.6f;
		
		float maxPlayserShipScore = 0f;
		
		officerBase *= 0.5f;
		officerPerLevel *= 0.5f;
		Set<PersonAPI> seenOfficers = new HashSet<PersonAPI>();
		int unofficeredShips = 0;
		for (FleetMemberAPI member : battle.getPlayerCombined().getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			float mult = baseMult;
			if (member.isStation()) mult *= 1f;
			else if (member.isCivilian()) mult *= 0.25f;
			if (member.getCaptain() != null && !member.getCaptain().isDefault()) {
				scorePlayer += officerBase + officerPerLevel * Math.max(1f, member.getCaptain().getStats().getLevel());
				seenOfficers.add(member.getCaptain());
			} else if (!member.isCivilian()) {
				unofficeredShips++;
			}
			int dMods = DModManager.getNumDMods(member.getVariant());
			for (int i = 0; i < dMods; i++) {
				mult *= dModMult;
			}
			//float currShipBaseScore = member.getUnmodifiedDeploymentPointsCost() * mult;
			float currShipBaseScore = member.getFleetPointCost() * mult;
			scorePlayer += currShipBaseScore;
			if (battle.getSourceFleet(member) != null && battle.getSourceFleet(member).isPlayerFleet()) {
				maxPlayserShipScore = Math.max(maxPlayserShipScore, currShipBaseScore);
			}
		}
		
		// so that removing officers from ships prior to a fight doesn't increase the XP gained
		// otherwise would usually be optimal to do this prior to every fight for any officers 
		// on ships that aren't expected to be deployed
		for (OfficerDataAPI od : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
			if (seenOfficers.contains(od.getPerson())) continue;
			if (od.getPerson().isPlayer()) continue;
			if (unofficeredShips <= 0) break;
			unofficeredShips--;
			scorePlayer += officerBase + officerPerLevel * Math.max(1f, od.getPerson().getStats().getLevel());
		}
	
		scorePlayer = Math.max(scorePlayer, Math.min(scoreEnemy * 0.5f, maxPlayserShipScore * 6f));
		
		if (scorePlayer < 1) scorePlayer = 1;
		if (scoreEnemy < 1) scoreEnemy = 1;
		
		
//		difficulty = scoreEnemy / (scorePlayer + scoreEnemy);
//		if (difficulty > 1) difficulty = 1;
//		if (scorePlayer < scoreEnemy) {
//			difficulty *= MAX_XP_MULT;
//		}
		//difficulty = scoreEnemy / (1f * scorePlayer);
		difficulty = scoreEnemy / scorePlayer;
		if (difficulty < 0) difficulty = 0;
		if (difficulty > MAX_XP_MULT) difficulty = MAX_XP_MULT;
		return difficulty;
	}
}











