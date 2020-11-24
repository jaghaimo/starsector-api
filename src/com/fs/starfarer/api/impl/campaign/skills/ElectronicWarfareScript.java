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
	public static final Object KEY_STATUS = new Object();
	
	public static final float BASE_MAXIMUM = 10;
	public static final float PER_JAMMER = 5;
	
	public static final String PENALTY_ID = "electronic_warfare_penalty";
	
	private CombatEngineAPI engine;
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
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
		
		float pTotal = player[0];
		float pMax = player[1];
		
		float eTotal = enemy[0];
		float eMax = enemy[1];
		
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
		applyPenalty(loser, penalty);
		
		
		boolean playerWon = winner.getOwner() == engine.getPlayerShip().getOwner(); 
					
		String title = "ECM rating:" + " " + (int) pTotal + "% vs " + (int)eTotal + "%";
		String data = "-" + (int)penalty + "% weapon range";
		if (playerWon) {
			data = "-" + (int)penalty + "% enemy weapon range";
		}
		
		String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_electronic_warfare");
		engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, !playerWon);
	}
	
	private void applyPenalty(CombatFleetManagerAPI manager, float penalty) {
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.getShip() == null) continue;
			
			float currPenalty = penalty * member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MULT);
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
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.isStationModule()) continue;
			float curr = member.getShip().getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT, 0f);
			total += curr;
		}
		
		for (BattleObjectiveAPI obj : engine.getObjectives()) {
			if (obj.getOwner() == manager.getOwner() && BattleObjectives.SENSOR_JAMMER.equals(obj.getType())) {
				total += PER_JAMMER;
			}
		}


		return new int [] {(int) total, (int) max};
	}

	
	
	
	
	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}

}
