package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.ColorShifterUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Hullmod that creates a fragment swarm around the ship. This swarm is required to power "fragment" weapons.
 * 
 * @author Alex
 *
 */
public class FragmentSwarmHullmod extends BaseHullMod {
	
	public static String STANDARD_SWARM_EXCHANGE_CLASS = "standard_swarm_exchange_class";
	public static String STANDARD_SWARM_FLOCKING_CLASS = "standard_swarm_flocking_class";
	public static String CONSTRUCTION_SWARM_FLOCKING_CLASS = "construction_swarm_flocking_class";
	public static String RECLAMATION_SWARM_FLOCKING_CLASS = "reclamation_swarm_flocking_class";
	public static String RECLAMATION_SWARM_EXCHANGE_CLASS = "reclamation_swarm_exchange_class";
	
	public static boolean SHOW_OVERLAY_ON_THREAT_SHIPS = false;
	
	public static float SMOD_CR_PENALTY = 0.2f;
	public static float SMOD_MAINTENANCE_PENALTY = 50f;
	
	public static Object STATUS_KEY1 = new Object();
	
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getMaxCombatReadiness().modifyFlat(id, -Math.round(SMOD_CR_PENALTY * 100f) * 0.01f, "Fragment swarm");
			stats.getSuppliesPerMonth().modifyPercent(id, SMOD_MAINTENANCE_PENALTY);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (SHOW_OVERLAY_ON_THREAT_SHIPS || !ship.getHullSpec().hasTag(Tags.THREAT)) {
			ship.setExtraOverlay(Global.getSettings().getSpriteName("misc", "fragment_swarm"));
			ship.setExtraOverlayMatchHullColor(false);
			ship.setExtraOverlayShadowOpacity(1f);
		}
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (amount <= 0f || ship == null) return;
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm == null) {
			swarm = createSwarmFor(ship);
		}
		
		if (ship.isFighter()) return;
		
		boolean playerShip = Global.getCurrentState() == GameState.COMBAT &&
				Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship;
		
		
		RoilingSwarmParams params = swarm.params;
		params.baseMembersToMaintain = (int) ship.getMutableStats().getDynamic().getValue(
										Stats.FRAGMENT_SWARM_SIZE_MOD, getBaseSwarmSize(ship.getHullSize()));
		params.memberRespawnRate = getBaseSwarmRespawnRateMult(ship.getHullSize()) * 
								ship.getMutableStats().getDynamic().getValue(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT);
		
//		if (ship.getHullSpec().getHullId().equals(ThreatHullmod.HIVE_UNIT)) {
//			params.baseMembersToMaintain = SwarmLauncherEffect.FRAGMENT_NUM.get(SwarmLauncherEffect.ATTACK_SWARM_WING);
//			params.baseMembersToMaintain *= 8;
//			params.memberRespawnRate = 15 * ship.getMutableStats().getDynamic().getValue(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT);
//		}
		
		params.maxNumMembersToAlwaysRemoveAbove = (int) (params.baseMembersToMaintain * 1.5f);
		params.initialMembers = params.baseMembersToMaintain;
		
		
		if (playerShip) {
			int active = swarm.getNumActiveMembers();
			
			int maxRequired = 0;
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.getEffectPlugin() instanceof FragmentWeapon) {
					FragmentWeapon fw = (FragmentWeapon) w.getEffectPlugin();
					maxRequired = Math.max(maxRequired, fw.getNumFragmentsToFire());
				}
			}
			
			boolean debuff = active < maxRequired;
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY1,
					Global.getSettings().getSpriteName("ui", "icon_tactical_fragment_swarm"),
					spec.getDisplayName(), 
					"FRAGMENTS: " + active,
					debuff);
		}
	}
	
	
	public static RoilingSwarmEffect createSwarmFor(ShipAPI ship) {
		RoilingSwarmEffect existing = RoilingSwarmEffect.getSwarmFor(ship);
		if (existing != null) return existing;
		
//		if (true) {
//			return SwarmLauncherEffect.createTestDwellerSwarmFor(ship);
//		}
		
		RoilingSwarmParams params = new RoilingSwarmParams();
		if (ship.isFighter()) {
			float radius = 20f;
			int numMembers = 50;
			
			String wingId = ship.getWing() == null ? null : ship.getWing().getWingId();
			if (SwarmLauncherEffect.SWARM_RADIUS.containsKey(wingId)) {
				radius = SwarmLauncherEffect.SWARM_RADIUS.get(wingId); 
			}
			if (SwarmLauncherEffect.FRAGMENT_NUM.containsKey(wingId)) {
				numMembers = SwarmLauncherEffect.FRAGMENT_NUM.get(wingId); 
			}
			
			params.memberExchangeClass = STANDARD_SWARM_EXCHANGE_CLASS;
			params.flockingClass = FragmentSwarmHullmod.STANDARD_SWARM_FLOCKING_CLASS;
			params.maxSpeed = ship.getMaxSpeedWithoutBoost() + 
						Math.max(ship.getMaxSpeedWithoutBoost() * 0.25f + 50f, 100f);
			
			params.flashRateMult = 0.25f;
			params.flashCoreRadiusMult = 0f;
			params.flashRadius = 120f;
			params.flashFringeColor = new Color(255,0,0,40);
			params.flashCoreColor = new Color(255,255,255,127);
			
			// if this is set to true and the swarm is glowing, missile-fragments pop over the glow and it looks bad
			//params.renderFlashOnSameLayer = true;
			
			params.maxOffset = radius;
			params.initialMembers = numMembers;
			params.baseMembersToMaintain = params.initialMembers;
		} else {
			params.memberExchangeClass = STANDARD_SWARM_EXCHANGE_CLASS;
			params.maxSpeed = ship.getMaxSpeedWithoutBoost() + 
						Math.max(ship.getMaxSpeedWithoutBoost() * 0.25f + 50f, 100f) +
						ship.getMutableStats().getZeroFluxSpeedBoost().getModifiedValue();
	
			params.flashRateMult = 0.25f;
			params.flashCoreRadiusMult = 0f;
			params.flashRadius = 120f;
			params.flashFringeColor = new Color(255,0,0,40);
			params.flashCoreColor = new Color(255,255,255,127);
			
			// if this is set to true and the swarm is glowing, missile-fragments pop over the glow and it looks bad
			//params.renderFlashOnSameLayer = true;
			
			params.minOffset = 0f;
			params.maxOffset = Math.min(100f, ship.getCollisionRadius() * 0.5f);
			params.generateOffsetAroundAttachedEntityOval = true;
			params.despawnSound = null; // ship explosion does the job instead
			params.spawnOffsetMult = 0.33f;
			params.spawnOffsetMultForInitialSpawn = 1f;
			
			params.baseMembersToMaintain = getBaseSwarmSize(ship.getHullSize());
			params.memberRespawnRate = getBaseSwarmRespawnRateMult(ship.getHullSize());
			params.maxNumMembersToAlwaysRemoveAbove = params.baseMembersToMaintain * 2;
			
			//params.offsetRerollFractionOnMemberRespawn = 0.05f;
			
			params.initialMembers = 0;
			params.initialMembers = params.baseMembersToMaintain;
			params.removeMembersAboveMaintainLevel = false;
		}
		
		List<WeaponAPI> glowWeapons = new ArrayList<>();
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.usesAmmo() && w.getSpec().hasTag(Tags.FRAGMENT_GLOW)) {
				glowWeapons.add(w);
			}
			if (w.getSpec().hasTag(Tags.OVERSEER_CHARGE) || 
					(ship.isFighter() && w.getSpec().hasTag(Tags.OVERSEER_CHARGE_FIGHTER))) {
				w.setAmmo(0);
			}
		}
		
//		if (ship.hasTag(Tags.FRAGMENT_SWARM_START_WITH_ZERO_FRAGMENTS)) {
//			params.initialMembers = 0;
//		}
		
		return new RoilingSwarmEffect(ship, params) {
			protected ColorShifterUtil glowColorShifter = new ColorShifterUtil(new Color(0, 0, 0, 0));
			protected boolean resetFlash = false;
			
			@Override
			public int getNumMembersToMaintain() {
				if (ship.isFighter()) {
					return (int)Math.round(((0.2f + 0.8f * ship.getHullLevel()) * super.getNumMembersToMaintain()));
				}
				return super.getNumMembersToMaintain();
			}

			@Override
			public void advance(float amount) {
				super.advance(amount);
				
				glowColorShifter.advance(amount);
				
				// this is actually QUITE performance-intensive on the rendering, at least doubles the cost per swarm
				// (comment was from when flashFrequency was *10 with a shorter flashRateMult; *2 is pretty ok -am
				if (VoltaicDischargeOnFireEffect.isSwarmPhaseMode(ship)) {
					params.flashFrequency = 4f;
					params.flashProbability = 1f;
					resetFlash = true;
				} else {
					if (!glowWeapons.isEmpty()) {
						float ammoFractionTotal = 0f;
						float totalOP = 0f;
						for (WeaponAPI w : glowWeapons) {
							float f = w.getAmmo() / Math.max(1f, w.getMaxAmmo());
							Color glowColor = w.getSpec().getGlowColor();
	//						if (f > 0) {
	//							glowColorShifter.shift(w, glowColor, 0.5f, 0.5f, 1f);
	//						}
							glowColorShifter.shift(w, glowColor, 0.5f, 0.5f, 1f);
							float weight = w.getSpec().getOrdnancePointCost(null);
							ammoFractionTotal += f * weight;
							totalOP += weight;
						}
						
						float ammoFraction = ammoFractionTotal / Math.max(1f, totalOP);
	 					params.flashFrequency = (1f + ammoFraction) * 2f;
	 					params.flashFrequency *= Math.max(1f, Math.min(2f, params.baseMembersToMaintain / 50f));
						params.flashProbability = 1f;
						if (ammoFraction <= 0f) {
							params.flashProbability = 0f;
						}
						//params.flashFringeColor = new Color(255,0,0,(int)(30f + 30f * ammoFraction));
						//float glowAlphaBase = 50f;
						float glowAlphaBase = 30f;
						if (ship.isFighter()) {
							glowAlphaBase = 18f;
						}
						
						float extraGlow = (totalOP - 10f) / 90f;
						if (extraGlow < 0) extraGlow = 0;
						if (extraGlow > 1f) extraGlow = 1f;
						
						int glowAlpha = (int)(glowAlphaBase + glowAlphaBase * (ammoFraction + extraGlow * 0.5f));
						if (glowAlpha > 255) glowAlpha = 255;
						//params.flashFringeColor = Misc.setAlpha(glowColorShifter.getCurr(), glowAlpha);
						params.flashFringeColor = Misc.setBrightness(glowColorShifter.getCurr(), 255);
						params.flashFringeColor = Misc.setAlpha(params.flashFringeColor, glowAlpha);
						
						resetFlash = true;
					} else {
						//if (ThreatSwarmAI.isAttackSwarm(ship)) {
						if (resetFlash) {
							params.flashProbability = 0f;
							resetFlash = false;
						}
					}
				}
				
//				int flashing = 0;
//				for (SwarmMember p : members) {
//					if (p.flash != null) {
//						flashing++;
//					}
//				}
//				System.out.println("Flashing: " + flashing + ", total: " + members.size());
			}
			
		};
	}
	
	
	
	
	public static int getBaseSwarmSize(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 100;
		case CRUISER: return 60;
		case DESTROYER: return 40;
		case FRIGATE: return 20;
		case FIGHTER: return 50; 
		case DEFAULT: return 20;
		default: return 20;
		}
	}
	
	public static float getBaseSwarmRespawnRateMult(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 5f;
		case CRUISER: return 3f;
		case DESTROYER: return 2f;
		case FRIGATE: return 1f;
		case FIGHTER: return 0f; 
		case DEFAULT: return 0f;
		default: return 0f;
		}
	}
	
	
	@Override
	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.FRAGMENT_FABRICATOR, null), null);
	}
	
	public static boolean hasShroudedHullmods(ShipAPI ship) {
		if (ship == null || ship.getVariant() == null) return false;
		for (String id : ship.getVariant().getHullMods()) {
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (spec != null && spec.hasTag(Tags.SHROUDED)) return true;
		}
		return false;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship != null && ship.getHullSpec().isPhase()) {
			return false;
		}
		if (hasShroudedHullmods(ship)) return false;
		
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.getHullSpec().isPhase()) {
			return "Can not be installed on a phase ship";
		}
		return "Incompatible with Shrouded hullmods";
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)getBaseSwarmSize(HullSize.FRIGATE);
		if (index == 1) return "" + (int)getBaseSwarmSize(HullSize.DESTROYER);
		if (index == 2) return "" + (int)getBaseSwarmSize(HullSize.CRUISER);
		if (index == 3) return "" + (int)getBaseSwarmSize(HullSize.CAPITAL_SHIP);
		
		if (index == 4) return "" + (int)getBaseSwarmRespawnRateMult(HullSize.FRIGATE);
		if (index == 5) return "" + (int)getBaseSwarmRespawnRateMult(HullSize.DESTROYER);
		if (index == 6) return "" + (int)getBaseSwarmRespawnRateMult(HullSize.CRUISER);
		if (index == 7) return "" + (int)getBaseSwarmRespawnRateMult(HullSize.CAPITAL_SHIP);

		return null;
	}
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(SMOD_CR_PENALTY * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(SMOD_MAINTENANCE_PENALTY) + "%";
		return null;
	}
	
	@Override
	public boolean isSModEffectAPenalty() {
		return true;
	}
}











