package com.fs.starfarer.api.impl.combat.threat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;


public class ConstructionSwarmSystemAI implements ShipSystemAIScript {

	public static float REQUIRED_DP_FOR_NORMAL_USE = 35;
	
	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	//protected ConstructionSwarmSystemScript script;
	
	protected IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
	
	protected float keepUsingFor = 0f;
	protected float timeSpentAtHighFragmentLevel = 0f;
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		
		// can't actual use this here this way due to class load order dependency making the game crash on startup
		//script = (ConstructionSwarmSystemScript)system.getScript();
	}
	
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		
		keepUsingFor -= amount;
		
		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			
			
			ConstructionSwarmSystemScript script = (ConstructionSwarmSystemScript)system.getScript();
			if (!script.isUsable(system, ship)) {
				return;
			}
			
			CombatFleetManagerAPI manager = Global.getCombatEngine().getFleetManager(ship.getOriginalOwner());
			int dpLeft = 0;
			if (manager != null) {
				dpLeft = manager.getMaxStrength() - manager.getCurrStrength();
			}
			
			float cr = ship.getCurrentCR();
			float softFlux = ship.getFluxLevel();
			float hardFlux = ship.getHardFluxLevel();
			
			if (cr < 0.2f + hardFlux * 0.2f) {
				return;
			}

			RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
			if (swarm == null) return;

			int active = swarm.getNumActiveMembers();
			int max = swarm.getNumMembersToMaintain();

			if (active >= max * 0.9f) {
				timeSpentAtHighFragmentLevel += tracker.getIntervalDuration();
			} else {
				timeSpentAtHighFragmentLevel = 0f;
			}
			
		
			
			if ((active >= max || timeSpentAtHighFragmentLevel >= 10f) && dpLeft >= REQUIRED_DP_FOR_NORMAL_USE) {
				keepUsingFor = 3f + (float) Math.random() * 2f;
			}
			
			if (keepUsingFor <= 0f && (hardFlux > 0.5f || softFlux > 0.9f)) {
				keepUsingFor = 0.5f;
			}

			if (keepUsingFor > 0f) {
				ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
			}
		}
	}

}






















