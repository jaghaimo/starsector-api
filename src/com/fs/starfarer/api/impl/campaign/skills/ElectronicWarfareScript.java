package com.fs.starfarer.api.impl.campaign.skills;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;

public class ElectronicWarfareScript extends BaseEveryFrameCombatPlugin {
	public static Object KEY_STATUS = new Object();
	public static Object KEY_STATUS_ENEMY_RATING = new Object();
	public static Object KEY_STATUS2 = new Object();
	
	public static float BASE_MAXIMUM = 10;
	
	public static float PER_JAMMER = 5;
	
	public static String PENALTY_ID = "electronic_warfare_penalty";
	
	private CombatEngineAPI engine;
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
	
//	public ElectronicWarfareScript() {
//		System.out.println("WEFWEGWGWEGF124");
//	}
	
	private ShipAPI prevPlayerShip = null;
	private int skipFrames = 0;
	private Set<CombatFleetManagerAPI> needsCleanup = new HashSet<CombatFleetManagerAPI>();
	public void advance(float amount, List<InputEventAPI> events) {
		if (engine == null) return;
		if (engine.isPaused()) return;
		
		
		// if the player changed flagships:
		// skip a few frames to make sure the status ends up on top of the status list
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip != prevPlayerShip) {
			prevPlayerShip = playerShip;
			skipFrames = 20;
		}
		
		if (skipFrames > 0) {
			skipFrames--;
			return;
		}
		
		
		int [] player = getTotalAndMaximum(engine.getFleetManager(0));
		int [] enemy = getTotalAndMaximum(engine.getFleetManager(1));
		
		
		if (player == null || enemy == null) {
			cleanUpIfNeeded(engine.getFleetManager(0));
			cleanUpIfNeeded(engine.getFleetManager(1));
			return;
		}
		
		if (engine.getFleetManager(0) == null || engine.getFleetManager(1) == null) {
			cleanUpIfNeeded(engine.getFleetManager(0));
			cleanUpIfNeeded(engine.getFleetManager(1));
			return;
		}
		
		//player[0] = 20;
		
		float pTotal = player[0];
		float pMax = player[1];
		
		float eTotal = enemy[0];
		float eMax = enemy[1];

//		float origEMax = eMax;
//		if (player[2] > 0) {
//			float pExcess = pTotal - pMax * 2f;
//			if (pExcess > 0) {
//				eMax -= pExcess * 0.5f;
//				if (eMax < 0) eMax = 0;
//			}
//		}
//		
//		if (enemy[2] > 0) {
//			float eExcess = eTotal - origEMax * 2f;
//			if (eExcess > 0) {
//				pMax -= eExcess * 0.5f;
//				if (pMax < 0) pMax = 0;
//			}
//		}
		
		if (pTotal <= 0) cleanUpIfNeeded(engine.getFleetManager(1));
		if (eTotal <= 0) cleanUpIfNeeded(engine.getFleetManager(0));

		
		int totalPenalty = (int) Math.round(Math.min(BASE_MAXIMUM, pTotal + eTotal));
		if (totalPenalty <= 0f) return;

		float ecmRatingToPenaltyMult = 1f;
		
		float playerPenalty = (int) Math.min(eTotal * ecmRatingToPenaltyMult, eMax);
		if (pTotal > 0 && playerPenalty > 0) {
			float pMult = eTotal / (eTotal + pTotal);
			playerPenalty *= pMult;
		}
		
		float enemyPenalty = (int) Math.min(pTotal * ecmRatingToPenaltyMult, pMax);
		if (eTotal > 0 && enemyPenalty > 0) {
			float eMult = pTotal / (eTotal + pTotal);
			enemyPenalty *= eMult;
		}
		
		playerPenalty = Math.round(playerPenalty);
		enemyPenalty = Math.round(enemyPenalty);
		

		String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_electronic_warfare");
		
		if (playerPenalty > 0 || eTotal > 0) {
			applyPenalty(engine.getFleetManager(0), playerPenalty, eMax);
			
			String sMax = "";
			if (eMax <= playerPenalty) sMax = " (max)";
			String title = "Enemy ECM rating:" + " " + (int) eTotal + "%";
			String data = "-" + (int)playerPenalty + "% weapon range" + sMax;
			if (eMax <= 0) {
				data = "fully neutralized";
			}
			engine.maintainStatusForPlayerShip(KEY_STATUS_ENEMY_RATING, icon, title, data, eMax > 0);
		}
		
		if (enemyPenalty > 0 || pTotal > 0) {
			applyPenalty(engine.getFleetManager(1), enemyPenalty, pMax);
			
			String sMax = "";
			if (pMax <= enemyPenalty) sMax = " (max)";
			String title = "ECM rating:" + " " + (int) pTotal + "%";
			String data = "-" + (int)enemyPenalty + "% enemy weapon range" + sMax;
			if (pMax <= 0) {
				data = "fully neutralized";
			}
			engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, false);			
		}
		
		if (playerPenalty > 0 && eMax > 0 && engine.getPlayerShip() != null) {
			int eccm = 100 - (int) Math.round(engine.getPlayerShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MOD, 100f));
			if (eccm > 100) eccm = 100;
			if (eccm < 0) eccm = 0;
			//eccm = -eccm;
			if (eccm != 0) {
				//engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, "On-board ECCM", "up to " + eccm + "% ecm neutralized", false);
				engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, "On-board ECCM", "" + eccm + "% enemy ecm neutralized", false);
			}
		}
		
/*		
		//KEY_STATUS_ENEMY_RATING
		float diff = pTotal - eTotal;
		
		if (diff == 0) {
			cleanUpIfNeeded(engine.getFleetManager(0));
			cleanUpIfNeeded(engine.getFleetManager(1));
			return;
		}
		
		
		CombatFleetManagerAPI winner = engine.getFleetManager(0);
		CombatFleetManagerAPI loser = engine.getFleetManager(1);
		
		float max = pMax;
		if (diff < 0) {
			CombatFleetManagerAPI temp = winner;
			winner = loser;
			loser = temp;
			max = eMax;
		}
		
		float penalty = Math.min(Math.abs(diff), max);
		
		cleanUpIfNeeded(winner);
		applyPenalty(loser, penalty, max);
		
		
		boolean playerWon = winner.getOwner() == engine.getPlayerShip().getOwner(); 
					
		String title = "ECM rating:" + " " + (int) pTotal + "% vs " + (int)eTotal + "%";
		String data = "-" + (int)penalty + "% weapon range";
		if (playerWon) {
			data = "-" + (int)penalty + "% enemy weapon range";
		}
		
		String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_electronic_warfare");
		
		if (engine.getPlayerShip() != null && !playerWon) {
			int eccm = 100 - (int) Math.round(engine.getPlayerShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MOD, 100f));
			if (eccm > 100) eccm = 100;
			if (eccm < 0) eccm = 0;
			//eccm = -eccm;
			if (eccm != 0) {
				//engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, "On-board ECCM", "up to " + eccm + "% ecm neutralized", false);
				engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, "On-board ECCM", "" + eccm + "% ecm neutralized", false);
			}
		}
		
		engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, !playerWon);
		*/
	}
	
	private void applyPenalty(CombatFleetManagerAPI manager, float penalty, float maxPenalty) {
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.getShip() == null) continue;
			
			float currPenalty = penalty * member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MULT);
			currPenalty = member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MOD, currPenalty);
			if (currPenalty < 0) currPenalty = 0;
			
			float maxMod = penalty * member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MAX_FOR_SHIP_MOD, 0);
			float currMax = maxPenalty + maxMod;
			if (currPenalty > currMax) {
				currPenalty = currMax;
			}
			
			member.getShip().getMutableStats().getBallisticWeaponRangeBonus().modifyMult(PENALTY_ID, 1f - currPenalty/100f);
			member.getShip().getMutableStats().getEnergyWeaponRangeBonus().modifyMult(PENALTY_ID, 1f - currPenalty/100f);
			member.getShip().getMutableStats().getMissileWeaponRangeBonus().modifyMult(PENALTY_ID, 1f - currPenalty/100f);
		}
		
		needsCleanup.add(manager);
	}
	
	protected void cleanUpIfNeeded(CombatFleetManagerAPI manager) {
		if (needsCleanup.contains(manager)) {
			needsCleanup.remove(manager);
			List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
			for (DeployedFleetMemberAPI member : deployed) {
				if (member.isFighterWing()) continue;
				if (member.getShip() == null) continue;
				
				member.getShip().getMutableStats().getBallisticWeaponRangeBonus().unmodify(PENALTY_ID);
				member.getShip().getMutableStats().getEnergyWeaponRangeBonus().unmodify(PENALTY_ID);
				member.getShip().getMutableStats().getMissileWeaponRangeBonus().unmodify(PENALTY_ID);
			}
		}
	}
	
	private int [] getTotalAndMaximum(CombatFleetManagerAPI manager) {
//		PersonAPI commander = manager.getFleetCommander();
//		if (commander == null) {
//			return null;
//		}
//		float max = BASE_MAXIMUM + commander.getStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_MAX, 0f);
		
		float max = 0f;
		for (PersonAPI commander : manager.getAllFleetCommanders()) {
			max = Math.max(max, BASE_MAXIMUM + commander.getStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_MAX, 0f));
		}
		
		
		float total = 0f;
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		float canCounter = 0f;
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.isStationModule()) continue;
			float curr = member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT, 0f);
			total += curr;
			
			canCounter += member.getShip().getMutableStats().getDynamic().getValue(Stats.SHIP_BELONGS_TO_FLEET_THAT_CAN_COUNTER_EW, 0f);
		}
		
		for (BattleObjectiveAPI obj : engine.getObjectives()) {
			if (obj.getOwner() == manager.getOwner() && BattleObjectives.SENSOR_JAMMER.equals(obj.getType())) {
				total += PER_JAMMER;
			}
		}

		int counter = 0;
		if (canCounter > 0) counter = 1;

		return new int [] {(int) total, (int) max, counter};
	}

	
	
	
	
	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}

}
