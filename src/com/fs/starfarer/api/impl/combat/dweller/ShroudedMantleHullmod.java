package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * For non-dweller ships with a sort-of shroud.
 */
public class ShroudedMantleHullmod extends HumanShipShroudedHullmod {
	
	public static float HEAL_MULT = 0.5f;
	
	public static float LUNGE_COOLDOWN = 10f;
	public static float LUNGE_DUR = 4f;
	public static float LUNGE_SPEED = 250f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.applyEffectsBeforeShipCreation(hullSize, stats, id);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getDynamic().getMod(AssayingRiftEffect.HUNGERING_RIFT_HEAL_MOD_HUMAN_SHIPS).modifyFlat(id, HEAL_MULT);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CREW_CASUALTIES + "%";
		return null;
	}
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(HEAL_MULT * 100f) + "%";
		return null;
	}	
	
	@Override
	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.SHROUDED_MANTLE, null), null);
	}

	
	public static String DATA_KEY = "core_ShroudedMantleHullmod_data_key";
	public static class ShroudedMantleHullmodData {
		IntervalUtil interval = new IntervalUtil(0.75f, 1.25f);
		float hullAtPrevLunge = 1f;
		float cooldown = 0f;
		boolean lunging = false;
		Vector2f lungeDest;
		float lungeElapsed = 0f;
		boolean fadedFlash = false;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		
		if (!ship.isAlive()) return;
		if (amount <= 0f) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = DATA_KEY + "_" + ship.getId();
		ShroudedMantleHullmodData data = (ShroudedMantleHullmodData) engine.getCustomData().get(key);
		if (data == null) {
			data = new ShroudedMantleHullmodData();
			engine.getCustomData().put(key, data);
		}
		
		boolean forceUse = false;
		
//		if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
//			forceUse = true;
//			data.cooldown = 0f;
//		}
		
		if (data.cooldown > 0) {
			data.cooldown -= amount;
			if (data.cooldown < 0) data.cooldown = 0;
			if (data.cooldown > 0) {
				return;
			}
		}

		if (ship.getFluxLevel() > 0.95f && ship.getHullLevel() > 0.25f && 
				ship.getShield() != null && ship.getShield().isOn()) {
			forceUse = true;
		}
		if (ship.getFluxTracker().isOverloaded()) {
			forceUse = true;
		}
		
		data.interval.advance(amount * 2f);
		if ((data.interval.intervalElapsed() || forceUse) && !data.lunging) {
			float hull = ship.getHullLevel();
			data.hullAtPrevLunge = Math.max(hull, data.hullAtPrevLunge);
			if (hull <= data.hullAtPrevLunge - ConvulsiveLungeSystemAI.HULL_LOSS_FOR_PULLBACK || forceUse) {
				data.lunging = true;
				data.lungeElapsed = 0f;
				data.lungeDest = null;
				data.fadedFlash = false;
				data.hullAtPrevLunge = hull;
			}
		}
		
		doLunge(data, ship, amount);
	}
	
	protected void doLunge(ShroudedMantleHullmodData data, ShipAPI ship, float amount) {
		if (!data.lunging) return;
		//if (ship.getFluxTracker().isOverloadedOrVenting()) return;
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		
		if (data.lungeElapsed < 1f || data.lungeDest == null) {
			if (data.lungeDest == null) {
				data.lungeDest = Misc.getUnitVectorAtDegreeAngle(Global.getSettings().getSafeMovementDir(ship));
				data.lungeDest.scale(ConvulsiveLungeSystemScript.PULL_DIST);
				Vector2f.add(data.lungeDest, ship.getLocation(), data.lungeDest);
			}
			
			if (data.lungeDest != null) {
				if (shroud != null) {
					Vector2f dir = Misc.getUnitVector(ship.getLocation(), data.lungeDest);
					float accel = ConvulsiveLungeSystemScript.PARTICLE_WINDUP_ACCEL * amount * 1f;
					if (!ship.isFrigate()) accel *= 2f;
					boolean affect = true;
					for (SwarmMember p : shroud.getMembers()) {
						if (affect) {
							p.vel.x += dir.x * accel;
							p.vel.y += dir.y * accel;
						}
						if (ship.isFrigate()) {
							affect = !affect;
						}
					}
				}
			}
		} else if (data.lungeDest != null) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), data.lungeDest));
			
			boolean slowdown = data.lungeElapsed > LUNGE_DUR - 1f;
			if (slowdown) {
				dir = Misc.normalise(new Vector2f(ship.getVelocity()));
				dir.negate();
			}
			
			if (!data.fadedFlash) {
				if (shroud != null) {
					for (SwarmMember p : shroud.getMembers()) {
						if (p.flash != null) {
							p.flash.fadeOut();
						}
					}
				}
				data.fadedFlash = true;
			}
			
			Vector2f loc = ship.getLocation();
			float dist = Misc.getDistance(loc, data.lungeDest);
			
			//Vector2f perp = new Vector2f(-dir.y, dir.x);
			
			float friction = ConvulsiveLungeSystemScript.FRICTION;
			float k = ConvulsiveLungeSystemScript.SPRING_CONSTANT;
			float freeLength = 0f;
			float stretch = dist - freeLength;

			float forceMag = k * Math.abs(stretch);
			
			float speedInDir = Vector2f.dot(dir, ship.getVelocity());
			if (speedInDir > LUNGE_SPEED) {
				float mult = 1f - Math.min(1f, (speedInDir - LUNGE_SPEED) / 100f);
				forceMag *= mult;
			}
			
			
			float forceMagReduction = Math.min(Math.abs(forceMag), friction);
			forceMag -= forceMagReduction;
			friction -= forceMagReduction;
			
			Vector2f force = new Vector2f(dir);
			if (slowdown) {
				forceMag = ship.getVelocity().length() * 2f;
				force.scale(forceMag);
			} else {
				force.scale(forceMag * Math.signum(stretch));
			}
			
			Vector2f acc = new Vector2f(force);
			acc.scale(amount);
			Vector2f.add(ship.getVelocity(), acc, ship.getVelocity());
		}
		
		ship.giveCommand(ShipCommand.DECELERATE, null, 0);
		
		data.lungeElapsed += amount;
		if (data.lungeElapsed > LUNGE_DUR) {
			data.lunging = false;
			data.cooldown = LUNGE_COOLDOWN;
		}
	}
	
}














