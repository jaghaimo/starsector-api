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
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;

public class CoordinatedManeuversScript extends BaseEveryFrameCombatPlugin {
	public static final Object KEY_STATUS = new Object();
	
	public static final float BASE_MAXIMUM = 20;
	public static final float PER_BUOY = 5;
	
	public static final String BONUS_ID = "coord_maneuvers_bonus";
	
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
		
		// nothing to do with Coordinated Maneuvers, just a convenient place to add this
		updateForceConcentration(engine.getFleetManager(0));
		updateForceConcentration(engine.getFleetManager(1));
		
		updateDPFromSupportDoctrine();
	}
	
	protected ShipAPI undoDPMod = null;
	protected void updateDPFromSupportDoctrine() {
		// making sure that while transferring command away from something, the previous player ship
		// does NOT receive the DP reduction until transferring command is finished
		
		ShipAPI from = engine.getShipPlayerIsTransferringCommandFrom();
		String id = SupportDoctrine.SUPPORT_DOCTRINE_DP_REDUCTION_ID + "_reverse";
		if (from != null) {
			StatMod bonus = from.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).getFlatBonus(SupportDoctrine.SUPPORT_DOCTRINE_DP_REDUCTION_ID);
			if (bonus != null && bonus.value != 0) {
				undoDPMod = from;
				from.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -bonus.value);
				if (from.getFleetMember() != null) {
					from.getFleetMember().getStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -bonus.value);
				}
			}
		} else if (undoDPMod != null) {
			undoDPMod.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(id);
			if (undoDPMod.getFleetMember() != null) {
				undoDPMod.getFleetMember().getStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(id);
			}
			undoDPMod = null;
		}
		
	}


	private void updateForSide(CombatFleetManagerAPI manager) {

//		PersonAPI commander = manager.getFleetCommander();
//		if (commander == null) {
//			cleanUpIfNeeded(manager);
//			return;
//		}
//		float max = BASE_MAXIMUM + commander.getStats().getDynamic().getValue(Stats.COORDINATED_MANEUVERS_MAX, 0f);
		
		float max = 0f;
		for (PersonAPI commander : manager.getAllFleetCommanders()) {
			max = Math.max(max, BASE_MAXIMUM + commander.getStats().getDynamic().getValue(Stats.COORDINATED_MANEUVERS_MAX, 0f));
		}
		
		if (max <= 0f) {
			cleanUpIfNeeded(manager);
			return;
		}
		
		boolean buoysOnly = true;
		float total = 0f;
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.isStationModule()) continue;
			
			float curr = member.getShip().getMutableStats().getDynamic().getValue(Stats.COORDINATED_MANEUVERS_FLAT, 0f);
			total += curr;
		}
		
		if (total > 0) buoysOnly = false;
		
		int numBuoys = 0;
		for (BattleObjectiveAPI obj : engine.getObjectives()) {
			if (obj.getOwner() == manager.getOwner() && BattleObjectives.NAV_BUOY.equals(obj.getType())) {
				total += PER_BUOY;
				numBuoys++;
			}
		}

		
		if (total <= 0f) {
			cleanUpIfNeeded(manager);
			return;
		}
		
		//if (total > max) total = max;
		
		boolean includeSelf = false;
		includeSelf = true;
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.getShip() == null) continue;
			
			float curr = member.getShip().getMutableStats().getDynamic().getValue(Stats.COORDINATED_MANEUVERS_FLAT, 0f);
			if (includeSelf) curr = 0f;
			
			float bonus = Math.min(max, Math.max(0f, total - curr));
			member.getShip().getMutableStats().getMaxSpeed().modifyPercent(BONUS_ID, bonus);
		}
		
		needsCleanup.add(manager);
		
		
		if (manager.getOwner() == engine.getPlayerShip().getOwner()) {
			//if (engine.getPlayerShip().isShuttlePod()) return;
			
			float curr = engine.getPlayerShip().getMutableStats().getDynamic().getValue(Stats.COORDINATED_MANEUVERS_FLAT, 0f);
			if (includeSelf) curr = 0f;

			float bonus = Math.min(max, Math.max(0f, total - curr));
			
			String title = "Coordinated Maneuvers:" + " " + (int) Math.min(max, total) + "%";
			//String data = "+" + (int)bonus + "% top speed (ship: " + (int) curr + "%)";
			String data = "+" + (int)bonus + "% top speed";
			if (buoysOnly) {
				title = "Nav Buoy";
				if (numBuoys > 1) {
					title += "s";
					title += " (" + numBuoys + ")";
				}
				data = "+" + (int)bonus + "% top speed";
			}
			String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_coordinated_maneuvers");
			engine.maintainStatusForPlayerShip(KEY_STATUS, icon,
						title, 
						data, false);
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
	
	
	protected void updateForceConcentration(CombatFleetManagerAPI manager) {
		if (true) return;
		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
		for (DeployedFleetMemberAPI member : deployed) {
			if (member.isFighterWing()) continue;
			if (member.isStationModule()) continue;
			if (member.getMember() == null) continue;
			
			
			PersonAPI fc = member.getMember().getFleetCommander();
			if (fc == null) fc = member.getMember().getFleetCommanderForStats();
			ShipAPI ship = member.getShip();
			
			if (ship == null) continue;
			if (fc == null) continue;
			
			//boolean hasFC = fc.getStats().getDynamic().getMod(Stats.HAS_FORCE_CONCENTRATION_BONUS_MOD).computeEffective(0f) > 0f;
			boolean hasFC = false;
			
			String id = "fc_zf_bonus";
			if (hasFC) {
				boolean hasZF = ship.isEngineBoostActive();
				if (ship.areAnyEnemiesInRange()) {
					ship.getMutableStats().getZeroFluxSpeedBoost().modifyFlat(id, ForceConcentration.ZERO_FLUX_SPEED_BONUS_SMALL);
				} else {
					ship.getMutableStats().getZeroFluxSpeedBoost().modifyFlat(id, ForceConcentration.ZERO_FLUX_SPEED_BONUS);
				}
				
				boolean applyAccelAndTurnModifiers = !ship.areAnyEnemiesInRange() && hasZF;
				if (applyAccelAndTurnModifiers) {
					ship.getMutableStats().getAcceleration().modifyFlat(id, ForceConcentration.ZERO_FLUX_ACCEL_BONUS);
					ship.getMutableStats().getDeceleration().modifyFlat(id, ForceConcentration.ZERO_FLUX_ACCEL_BONUS);
					ship.getMutableStats().getMaxTurnRate().modifyFlat(id, ForceConcentration.ZERO_FLUX_TURN_BONUS);
					ship.getMutableStats().getTurnAcceleration().modifyFlat(id, ForceConcentration.ZERO_FLUX_TURN_ACCEL_BONUS);
				} else {
					ship.getMutableStats().getAcceleration().unmodifyFlat(id);
					ship.getMutableStats().getDeceleration().unmodifyFlat(id);
					ship.getMutableStats().getMaxTurnRate().unmodifyFlat(id);
					ship.getMutableStats().getTurnAcceleration().unmodifyFlat(id);
				}
			}
		}
	}
	
	
//	public static PersonAPI getCommander(CombatFleetManagerAPI manager) {
//		List<DeployedFleetMemberAPI> deployed = manager.getDeployedCopyDFM();
//		if (deployed.isEmpty()) return null;
//		
//		PersonAPI defaultCommander = manager.getDefaultCommander();
//		for (DeployedFleetMemberAPI member : deployed) {
//			if (member.isFighterWing()) continue;
//			FleetMemberAPI m = member.getMember();
//			PersonAPI commander = m.getFleetCommanderForStats();
//			if (commander == null && m.getFleetData() != null) {
//				commander = m.getFleetData().getCommander();
//			}
//			if (commander == null) {
//				commander = defaultCommander;
//			}
//			return commander;
//		}
//		return null;
//	}
	

	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}

}
