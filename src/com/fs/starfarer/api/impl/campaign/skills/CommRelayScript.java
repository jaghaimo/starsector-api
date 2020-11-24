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

public class CommRelayScript extends BaseEveryFrameCombatPlugin {
	public static final float RATE_BONUS_PER_COMM_RELAY = 25f;
	public static final Object KEY_STATUS = new Object();
	public static final String BONUS_ID = "comm_relay_script_bonus";
	
	
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
		
		updateForSide(engine.getFleetManager(0));
		updateForSide(engine.getFleetManager(1));
	}
	
	
	private void updateForSide(CombatFleetManagerAPI manager) {

		PersonAPI commander = manager.getFleetCommander();
		if (commander == null) {
			manager.getTaskManager(false).getCPRateModifier().unmodify(BONUS_ID);
			return;
		}
		
		
		float total = 0f;
		
		float modifier = commander.getStats().getDynamic().getValue(Stats.COMMAND_POINT_RATE_COMMANDER);
		boolean relaysOnly = modifier == 1f;
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			float curr = member.getShip().getMutableStats().getDynamic().getValue(Stats.COMMAND_POINT_RATE_FLAT, 0f);
			total += curr;
		}
		
		if (total > 0) relaysOnly = false;
		
		
		int numRelays = 0;
		for (BattleObjectiveAPI obj : engine.getObjectives()) {
			if (obj.getOwner() == manager.getOwner() && BattleObjectives.COMM_RELAY.equals(obj.getType())) {
				total += RATE_BONUS_PER_COMM_RELAY / 100f;
				numRelays++;
			}
		}
		
		
		manager.getTaskManager(false).getCPRateModifier().modifyFlat(BONUS_ID, total);
		
		
		modifier += manager.getTaskManager(false).getCPRateModifier().getModifiedValue();
		modifier -= 1f;
		
		if (manager.getOwner() == 0) {
			// use mult instead of modifier since mult includes skill bonus and modifier is only relays
			float withMult = manager.getTaskManager(false).getCPRateMult();
			String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_coordinated_maneuvers");
			String title = "command network";
			//String data = "+" + (int)(modifier * 100f) + "% cp recovery rate";
			
			String data = "+" + (int)Math.round((withMult - 1f) * 100f) + "% cp recovery rate";
			boolean debuff = false;
			if (withMult < 1f) {
				data = "" + (int)Math.round((withMult - 1f) * 100f) + "% cp recovery rate";
				debuff = true;
			}
			
//			if (relaysOnly) {
//				title = "comm relay";
//				if (numRelays > 1) {
//					title += "s";
//					title += " (" + numRelays + ")";
//				}
//			}
				
			engine.maintainStatusForPlayerShip(KEY_STATUS, icon,
						title, 
						data, debuff);
		}
		
	}
	
	protected void cleanUpIfNeeded(CombatFleetManagerAPI manager) {
		if (needsCleanup.contains(manager)) {
			needsCleanup.remove(manager);
			List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
			for (DeployedFleetMemberAPI member : deployed) {
				if (member.isFighterWing()) continue;
				if (member.getShip() == null) continue;
				member.getShip().getMutableStats().getMaxSpeed().unmodify(BONUS_ID);
			}
		}
	}
	

	

	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}


	public boolean hasInputPriority() {
		// TODO Auto-generated method stub
		return false;
	}

}
