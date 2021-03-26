package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShardSpawner extends BaseHullMod {

	public static Color JITTER_COLOR = new Color(100,100,255,50);
	public static String DATA_KEY = "core_shard_spawner_data_key";
	
	public static float SPAWN_TIME = 4f;
	
	public static enum ShardType {
		GENERAL,
		ANTI_ARMOR,
		ANTI_SHIELD,
		POINT_DEFENSE,
		MISSILE,
	}
	
	public static class ShardTypeVariants {
		public Map<ShardType, WeightedRandomPicker<String>> variants = new HashMap<ShardType, WeightedRandomPicker<String>>();
		public ShardTypeVariants() {
		}
		public WeightedRandomPicker<String> get(ShardType type) {
			WeightedRandomPicker<String> result = variants.get(type);
			if (result == null) {
				result = new WeightedRandomPicker<String>();
				variants.put(type, result);
			}
			return result;
		}
	}
	
	public static Map<HullSize, ShardTypeVariants> variantData = new HashMap<HullSize, ShardTypeVariants>();
	static {
		ShardTypeVariants fighters = new ShardTypeVariants();
		variantData.put(HullSize.FIGHTER, fighters);
		fighters.get(ShardType.GENERAL).add("aspect_attack_wing", 10f);
		fighters.get(ShardType.GENERAL).add("aspect_missile_wing", 1f);
		
		fighters.get(ShardType.MISSILE).add("aspect_missile_wing", 10f);
		
		fighters.get(ShardType.ANTI_ARMOR).add("aspect_attack_wing", 10f);
		
		fighters.get(ShardType.ANTI_SHIELD).add("aspect_shieldbreaker_wing", 10f);
		
		fighters.get(ShardType.POINT_DEFENSE).add("aspect_shock_wing", 10f);
		
		
		ShardTypeVariants small = new ShardTypeVariants();
		variantData.put(HullSize.FRIGATE, small);
		
		small.get(ShardType.GENERAL).add("shard_left_Attack", 10f);
		small.get(ShardType.GENERAL).add("shard_left_Attack2", 10f);
		small.get(ShardType.GENERAL).add("shard_right_Attack", 10f);
		small.get(ShardType.GENERAL).add("aspect_attack_wing", 10f);
		small.get(ShardType.GENERAL).add("aspect_missile_wing", 1f);
		
		small.get(ShardType.ANTI_ARMOR).add("shard_left_Armorbreaker", 10f);
		
		small.get(ShardType.ANTI_SHIELD).add("shard_left_Shieldbreaker", 10f);
		small.get(ShardType.ANTI_SHIELD).add("shard_right_Shieldbreaker", 10f);
		//small.get(ShardType.ANTI_SHIELD).add("aspect_shieldbreaker_wing", 10f);
		
		small.get(ShardType.POINT_DEFENSE).add("shard_left_Defense", 10f);
		small.get(ShardType.POINT_DEFENSE).add("shard_right_Shock", 10f);
		//small.get(ShardType.POINT_DEFENSE).add("aspect_shock_wing", 10f);
		
		small.get(ShardType.MISSILE).add("shard_left_Missile", 10f);
		small.get(ShardType.MISSILE).add("shard_right_Missile", 10f);
		//small.get(ShardType.MISSILE).add("aspect_missile_wing", 10f);
		
		
		ShardTypeVariants medium = new ShardTypeVariants();
		variantData.put(HullSize.DESTROYER, medium);
		
		medium.get(ShardType.GENERAL).add("facet_Attack");
		medium.get(ShardType.GENERAL).add("facet_Attack2");
		
		medium.get(ShardType.ANTI_ARMOR).add("facet_Armorbreaker");
		
		medium.get(ShardType.ANTI_SHIELD).add("facet_Shieldbreaker");
		
		medium.get(ShardType.POINT_DEFENSE).add("facet_Defense");
		
		medium.get(ShardType.MISSILE).add("facet_Missile");
		
		ShardTypeVariants large = new ShardTypeVariants();
		variantData.put(HullSize.CRUISER, large);
		
		large.get(ShardType.GENERAL).add("tesseract_Attack");
		large.get(ShardType.GENERAL).add("tesseract_Attack2");
		large.get(ShardType.GENERAL).add("tesseract_Strike");
		large.get(ShardType.GENERAL).add("tesseract_Disruptor");
		
		large.get(ShardType.ANTI_ARMOR).add("tesseract_Disruptor");
		large.get(ShardType.ANTI_ARMOR).add("tesseract_Strike");
		
		large.get(ShardType.ANTI_SHIELD).add("tesseract_Shieldbreaker");
		
		large.get(ShardType.POINT_DEFENSE).add("tesseract_Defense");
		
		large.get(ShardType.MISSILE).add("tesseract_Strike");
	}
	
	public static class ShardSpawnerData {
		boolean done = false;
		float delay = 2f + (float) Math.random() * 1f;
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBreakProb().modifyMult(id, 0f);
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.setCombatNotOverForAtLeast(SPAWN_TIME + 1f);
		
		if (!ship.isHulk() || !engine.isEntityInPlay(ship)) return;
		
		String key = DATA_KEY + "_" + ship.getId();
		ShardSpawnerData data = (ShardSpawnerData) engine.getCustomData().get(key);
		if (data == null) {
			data = new ShardSpawnerData();
			engine.getCustomData().put(key, data);
		}
		
		if (data.done) return;
		
		
		ship.setHitpoints(ship.getMaxHitpoints());
		ship.getMutableStats().getHullDamageTakenMult().modifyMult("ShardSpawnerInvuln", 0f);
		data.delay -= amount;
		if (data.delay > 0) return;
		
		//ship.setCollisionClass(CollisionClass.NONE);
		float dur = SPAWN_TIME;
		float extraDur = 0f;

		
		float splitWeight = 0f;
		
		float probNothingAtAll = 0f;
		//float forceFighterProb = 0f;
		float cruiserProb = 0f;
		float cruiserProbMult = 0f;
		float maxCruisers = 0f;
		float destroyerProb = 0f;
		float destroyerProbMult = 0f;
		float maxDestroyers = 0f;
		float frigateProb = 0f;
		float frigateProbMult = 0f;
		float maxFrigates = 0f;
		
		if (ship.isCapital()) {
			splitWeight = 12f;
			
			cruiserProb = 1f;
			cruiserProbMult = 0.5f;
			maxCruisers = 1f;
			destroyerProb = 1f;
			destroyerProbMult = 1f;
			maxDestroyers = 2f;
			frigateProb = 1f;
			frigateProbMult = 1f;
			maxFrigates = 3f;
		} else if (ship.isCruiser()) {
//			splitWeight = 7f;
//			
//			destroyerProb = 1f;
//			destroyerProbMult = 0.5f;
//			maxDestroyers = 1f;
//			frigateProb = 1f;
//			frigateProbMult = 0.67f;
//			maxFrigates = 3f;
			
			splitWeight = 7f;
			
			destroyerProb = 1f;
			destroyerProbMult = 0.5f;
			maxDestroyers = 1f;
			frigateProb = 1f;
			frigateProbMult = 1f;
			maxFrigates = 2f;
		} else if (ship.isDestroyer()) {
//			splitWeight = 4f;
//			
//			frigateProb = 1f;
//			frigateProbMult = 0.75f;
//			maxFrigates = 3f;
			
			splitWeight = 4f;
			
			frigateProb = 1f;
			frigateProbMult = 1f;
			maxFrigates = 2f;
		} else if (ship.isFrigate()) {
			splitWeight = 2f;
		}
		WeightedRandomPicker<ShardType> typePicker = getTypePickerBasedOnLocalConditions(ship);
		
//		splitWeight = 8f;
//		destroyerProb = 0.8f;
//		maxDestroyers = 2f;
//		//maxDestroyers = 0f;
//		
//		splitWeight = 1f;
		
		if ((float) Math.random() < probNothingAtAll) {
			splitWeight = 0f;
		}

		WeightedRandomPicker<Float> spawnAngles = new WeightedRandomPicker<Float>();
		int spawnAnglesIter = 0;
		float angleOffset = (float) Math.random() * 360f;
		
		float addedWeight = 0f;
		float fighters = 0f;
		float frigates = 0f;
		float destroyers = 0f;
		float cruisers = 0f;
		List<ShardFadeInPlugin> shards = new ArrayList<ShardFadeInPlugin>();
		while (addedWeight < splitWeight) {
			ShardType type = typePicker.pick();
			
			float rem = splitWeight - addedWeight;
			boolean cruiser = (float) Math.random() < cruiserProb && cruisers < maxCruisers && rem >= 3.5f;
			boolean destroyer = (float) Math.random() < destroyerProb && destroyers < maxDestroyers && rem >= 1.5f;
			boolean frigate = (float) Math.random() < frigateProb && frigates < maxFrigates;
			//boolean fighter = (float) Math.random() < forceFighterProb && fighters < maxFromFightersOnlyCategory;
			String variant = null;
			float weight = 1f;
			
			if (cruiser) {
				ShardTypeVariants variants = variantData.get(HullSize.CRUISER);
				WeightedRandomPicker<String> variantPicker = variants.get(type);
				variant = variantPicker.pick();
				if (variant != null) {
					weight = 4f;
					cruisers++;
					cruiserProb *= cruiserProbMult;
				}
			}
			
			if (destroyer && variant == null) {
				ShardTypeVariants variants = variantData.get(HullSize.DESTROYER);
				WeightedRandomPicker<String> variantPicker = variants.get(type);
				variant = variantPicker.pick();
				if (variant != null) {
					weight = 2f;
					destroyers++;
					destroyerProb *= destroyerProbMult;
				}
			} 
			
			if (frigate && variant == null) {
				ShardTypeVariants variants = variantData.get(HullSize.FRIGATE);
				WeightedRandomPicker<String> variantPicker = variants.get(type);
				variant = variantPicker.pick();
				if (variant != null) {
					weight = 1f;
					frigates++;
					frigateProb *= frigateProbMult;
				}
			}
			
			if (variant == null) {
				ShardTypeVariants variants = variantData.get(HullSize.FIGHTER);
				WeightedRandomPicker<String> variantPicker = variants.get(type);
				variant = variantPicker.pick();
				if (variant != null) {
					fighters++;
				}
			}
			
			//variant = "aspect_shock_wing";
			//variant = "aspect_shieldbreaker_wing";
			//variant = "aspect_missile_wing";
			if (variant != null) {
				if (spawnAngles == null || spawnAngles.isEmpty()) {
					spawnAngles = getSpawnAngles(spawnAnglesIter++);
				}
				float angle = spawnAngles.pickAndRemove() + angleOffset;
				ShardFadeInPlugin shard = createShipFadeInPlugin(variant, ship, extraDur, dur, angle);
				shards.add(shard);
				Global.getCombatEngine().addPlugin(shard);
				addedWeight += weight;
				
//				float delay = 0.5f + (float) Math.random() * 0.5f;
//				extraDur += delay;
			} else {
				addedWeight += 0.1f; // if we didn't manage to add anything, eventually break out of the loop
			}
		}
		
		Global.getCombatEngine().addPlugin(createShipFadeOutPlugin(ship, dur + extraDur * 0.5f, shards));
		
//		Global.getCombatEngine().addPlugin(createShipFadeInPlugin("shard_left_Attack", ship, 0f, dur));
//		Global.getCombatEngine().addPlugin(createShipFadeInPlugin("shard_right_Attack", ship, 0f, dur));
		data.done = true;
	}
	
	public WeightedRandomPicker<Float> getSpawnAngles(int iter) {
		WeightedRandomPicker<Float> picker = new WeightedRandomPicker<Float>();
		float start = 0f;
		float incr = 60f;
		if (iter == 1) {
			start = 30f;
		} else if (iter == 2) {
			start = 15f;
			incr = 30f;
		} else {
			incr = 10f;
		}
		for (float i = start; i < 360f + start; i += incr) {
			picker.add(i);
		}
		return picker;
	}
	
	
	
	public WeightedRandomPicker<ShardType> getTypePickerBasedOnLocalConditions(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float checkRadius = 5000;
		Iterator<Object> iter = engine.getAiGridShips().getCheckIterator(ship.getLocation(), checkRadius * 2f, checkRadius * 2f);

		float weightFighters = 0f;
		float weightGoodShields = 0f;
		float weightGoodArmor = 0f;
		float weightVulnerable = 0f;
		float weightCarriers = 0f;
		
		float weightEnemies = 0f;
		float weightFriends = 0f;
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof ShipAPI) {
				ShipAPI other = (ShipAPI) o;
				if (other.getOwner() == Misc.OWNER_NEUTRAL) continue;
				
				boolean enemy = ship.getOwner() != other.getOwner();
				if (enemy) {
					if (other.isFighter() || other.isDrone()) {
						weightFighters += 0.25f;
						weightEnemies += 0.25f;
					} else {
						float w = Misc.getShipWeight(other);
						weightEnemies += w;
						
						if (hasGoodShields(other)) {
							weightGoodShields += w;
						}
						if (hasGoodArmor(other)) {
							weightGoodArmor += w;
						}
						if (isVulnerableToMissileBarrage(ship, other)) {
							weightVulnerable += w;
						}
						if (other.getVariant().isCarrier()) {
							weightCarriers += w;
						}
					}
				} else {
					if (other.isFighter() || other.isDrone()) {
						weightFriends += 0.25f;
					} else {
						float w = Misc.getShipWeight(other);
						weightFriends += w;
					}
				}
			}
		}
		
		WeightedRandomPicker<ShardType> picker = new WeightedRandomPicker<ShardType>();

		float total = weightFighters + weightGoodShields + weightGoodArmor + weightVulnerable + weightCarriers;
		if (total <= 1f) total = 1f;
		
		float antiFighter = (weightFighters + weightCarriers) / total;
		float antiShield = weightGoodShields / total;
		float antiArmor = weightGoodArmor / total;
		float missile = weightVulnerable / total;

		float friends = weightFriends / Math.max(1f, weightEnemies + weightFriends);
		
		picker.add(ShardType.GENERAL, 0.0f + (1f - friends) * 0.4f);
//		picker.add(ShardType.GENERAL, 0.2f);
//		if (friends < 0.3f) {
//			picker.add(ShardType.GENERAL, Math.min(0.25f, (1f - friends) * 0.25f));
//		}
		
		float unlikelyWeight = 0f;
		float unlikelyThreshold = 0.2f;
		
		if (antiFighter < unlikelyThreshold) antiFighter = unlikelyWeight;
		picker.add(ShardType.POINT_DEFENSE, antiFighter);
		
		if (antiShield < unlikelyThreshold) antiShield = unlikelyWeight;
		picker.add(ShardType.ANTI_SHIELD, antiShield);
		
		if (antiArmor < unlikelyThreshold) antiArmor = unlikelyWeight;
		picker.add(ShardType.ANTI_ARMOR, antiArmor);
		
		if (missile < unlikelyThreshold) missile = unlikelyWeight;
		picker.add(ShardType.MISSILE, missile);
		
		return picker;
	}
	
	public boolean isVulnerableToMissileBarrage(ShipAPI from, ShipAPI other) {
		float incap = Misc.getIncapacitatedTime(other);
		
		float dist = Misc.getDistance(from.getLocation(), other.getLocation());
		if (dist > 2000) return false;
		
		float assumedMissileSpeed = 500;
		float eta = dist / assumedMissileSpeed;
		eta += SPAWN_TIME;
		eta += 2f;
		
		return incap >= eta || (other.getFluxLevel() >= 0.95f && other.getFluxTracker().getTimeToVent() >= eta);
	}
	
	public boolean hasGoodArmor(ShipAPI other) {
		float requiredArmor = 1240;

		if (other.getArmorGrid().getArmorRating() < requiredArmor) return false;
		
		float armor = other.getAverageArmorInSlice(other.getFacing(), 120f);
		return armor >= requiredArmor * 0.8f;
		
	}
	
	public boolean hasGoodShields(ShipAPI other) {
		ShieldAPI shield = other.getShield();
		if (shield == null) return false;
		if (shield.getType() == ShieldType.NONE) return false;
		if (shield.getType() == ShieldType.PHASE) return false;
		
		float requiredCapacity = 10000000f;
		switch (other.getHullSize()) {
		case CAPITAL_SHIP:
			requiredCapacity = 25000;
			if (shield.getType() == ShieldType.FRONT && shield.getArc() < 250) {
				requiredCapacity = 1000000;
			}
			break;
		case CRUISER:
			requiredCapacity = 12500;
			if (shield.getType() == ShieldType.FRONT && shield.getArc() < 250) {
				requiredCapacity = 1000000;
			}
			break;
		case DESTROYER:
			requiredCapacity = 8000;
			break;
		case FRIGATE:
			requiredCapacity = 4000;
			break;
		}
		
		float e = other.getShield().getFluxPerPointOfDamage() *
				  other.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
		float capacity = other.getMaxFlux();
		capacity /= Math.max(0.1f, e);
		
		return capacity >= requiredCapacity && e <= 1f;
	}
	

	
	protected EveryFrameCombatPlugin createShipFadeOutPlugin(final ShipAPI ship, final float fadeOutTime,
											final List<ShardFadeInPlugin> shards) {
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
			
			protected void pushShipsAway(float amount) {
				Vector2f com = new Vector2f();
				float count = 0f;
				for (ShardFadeInPlugin shard : shards) {
					ShipAPI ship = shard.ships[0];
					if (ship.isFighter()) continue;
					Vector2f.add(com, ship.getLocation(), com);
					count++;
				}
				com.scale(1f / Math.max(1f, count));
				
				Vector2f comForFighters = new Vector2f();
				count = 0f;
				for (ShardFadeInPlugin shard : shards) {
					ShipAPI ship = shard.ships[0];
					if (!ship.isFighter()) continue;
					Vector2f.add(comForFighters, ship.getLocation(), comForFighters);
					count++;
				}
				comForFighters.scale(1f / Math.max(1f, count));
				
				float progress = elapsed / fadeOutTime;
				if (progress > 1f) progress = 1f;
				for (ShardFadeInPlugin shard : shards) {
					ShipAPI ship = shard.ships[0];
					Vector2f currCom = com;
					if (ship.isFighter()) currCom = comForFighters;
					
					Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(currCom, ship.getLocation()));
					float speed = ship.getCollisionRadius() * 0.5f;
					dir.scale(amount * speed * progress);
					Vector2f.add(ship.getLocation(), dir, ship.getLocation());
				}
			}
			
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
				
				elapsed += amount;
				
				
				float progress = elapsed / fadeOutTime;
				if (progress > 1f) progress = 1f;
				ship.setAlphaMult(1f - progress);
				
				//if (progress < 0.5f) {
					pushShipsAway(amount);
				//}
					
				if (progress > 0.5f) {
					ship.setCollisionClass(CollisionClass.NONE);
				}
				
				float jitterLevel = progress;
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}
				
				float jitterRange = progress;
				//jitterRange = (float) Math.sqrt(jitterRange);
				float maxRangeBonus = 100f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = JITTER_COLOR;
				int alpha = c.getAlpha();
				alpha += 100f * progress;
				if (alpha > 255) alpha = 255;
				c = Misc.setAlpha(c, alpha);
				
				ship.setJitter(this, c, jitterLevel, 35, 0f, jitterRangeBonus);
				
				interval.advance(amount);
				if (interval.intervalElapsed() && elapsed < fadeOutTime * 0.75f) {
					CombatEngineAPI engine = Global.getCombatEngine();
					c = RiftLanceEffect.getColorForDarkening(RiftCascadeEffect.STANDARD_RIFT_COLOR);
					float baseDuration = 2f;
					Vector2f vel = new Vector2f(ship.getVelocity());
					float size = ship.getCollisionRadius() * 0.35f;
					for (int i = 0; i < 3; i++) {
						Vector2f point = new Vector2f(ship.getLocation());
						point = Misc.getPointWithinRadiusUniform(point, ship.getCollisionRadius() * 0.5f, Misc.random);
						float dur = baseDuration + baseDuration * (float) Math.random();
						float nSize = size;
						Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
						Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
						v.scale(nSize + nSize * (float) Math.random() * 0.5f);
						v.scale(0.2f);
						Vector2f.add(vel, v, v);
						
						float maxSpeed = nSize * 1.5f * 0.2f; 
						float minSpeed = nSize * 1f * 0.2f; 
						float overMin = v.length() - minSpeed;
						if (overMin > 0) {
							float durMult = 1f - overMin / (maxSpeed - minSpeed);
							if (durMult < 0.1f) durMult = 0.1f;
							dur *= 0.5f + 0.5f * durMult;
						}
						engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
														0.5f / dur, 0f, dur, c);
					}
				}
				
				if (elapsed > fadeOutTime) {
					ship.setHitpoints(0f);
					Global.getCombatEngine().removeEntity(ship);
					ship.setAlphaMult(0f);
					Global.getCombatEngine().removePlugin(this);
				}
			}
		};
	}
	
	
	protected ShardFadeInPlugin createShipFadeInPlugin(final String variantId, final ShipAPI source, 
															final float delay, final float fadeInTime, final float angle) {
	
		return new ShardFadeInPlugin(variantId, source, delay, fadeInTime, angle);
	}
	
	public static class ShardFadeInPlugin extends BaseEveryFrameCombatPlugin {
		float elapsed = 0f;
		ShipAPI [] ships = null;
		CollisionClass collisionClass;
		
		String variantId;
		ShipAPI source;
		float delay;
		float fadeInTime;
		float angle;
		
		public ShardFadeInPlugin(String variantId, ShipAPI source, float delay, float fadeInTime, float angle) {
			this.variantId = variantId;
			this.source = source;
			this.delay = delay;
			this.fadeInTime = fadeInTime;
			this.angle = angle;
			
		}
			
		
		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (Global.getCombatEngine().isPaused()) return;
		
			elapsed += amount;
			if (elapsed < delay) return;
			
			CombatEngineAPI engine = Global.getCombatEngine();
			
			if (ships == null) {
				float facing = source.getFacing() + 15f * ((float) Math.random() - 0.5f);
//					Vector2f loc = new Vector2f();
//					loc = Misc.getPointWithinRadius(loc, source.getCollisionRadius() * 0.25f);
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(source.getCollisionRadius() * 0.1f);
				Vector2f.add(loc, source.getLocation(), loc);
				CombatFleetManagerAPI fleetManager = engine.getFleetManager(source.getOriginalOwner());
				boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
				fleetManager.setSuppressDeploymentMessages(true);
				if (variantId.endsWith("_wing")) {
					FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(variantId);
					ships = new ShipAPI[spec.getNumFighters()];
					PersonAPI captain = Global.getSettings().createPerson();
					captain.setPersonality(Personalities.RECKLESS); // doesn't matter for fighters
					captain.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
					captain.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
					captain.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
					ShipAPI leader = engine.getFleetManager(source.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, captain);
					for (int i = 0; i < ships.length; i++) {
						ships[i] = leader.getWing().getWingMembers().get(i);
						ships[i].getLocation().set(loc);
					}
					collisionClass = ships[0].getCollisionClass();
				} else {
					ships = new ShipAPI[1];
					ships[0] = engine.getFleetManager(source.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, source.getOriginalCaptain());
				}
				for (int i = 0; i < ships.length; i++) {
					ships[i].cloneVariant();
					ships[i].getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
				}
				fleetManager.setSuppressDeploymentMessages(wasSuppressed);
				collisionClass = ships[0].getCollisionClass();
				
				DeployedFleetMemberAPI sourceMember = fleetManager.getDeployedFleetMemberFromAllEverDeployed(source);
				DeployedFleetMemberAPI deployed = fleetManager.getDeployedFleetMemberFromAllEverDeployed(ships[0]);
				if (sourceMember != null && deployed != null) {
					Map<DeployedFleetMemberAPI, DeployedFleetMemberAPI> map = fleetManager.getShardToOriginalShipMap();
					while (map.containsKey(sourceMember)) {
						sourceMember = map.get(sourceMember);
					}
					if (sourceMember != null) {
						map.put(deployed, sourceMember);
					}
				}
			}
			
			
			
			float progress = (elapsed - delay) / fadeInTime;
			if (progress > 1f) progress = 1f;
			
			for (int i = 0; i < ships.length; i++) {
				ShipAPI ship = ships[i];
				ship.setAlphaMult(progress);
				
				if (progress < 0.5f) {
					ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
					ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
					ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
					ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
					ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
				}
				
				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
				ship.blockCommandForOneFrame(ShipCommand.FIRE);
				ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
				ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
				ship.setHoldFireOneFrame(true);
				ship.setHoldFire(true);
				
				
				ship.setCollisionClass(CollisionClass.NONE);
				ship.getMutableStats().getHullDamageTakenMult().modifyMult("ShardSpawnerInvuln", 0f);
				if (progress < 0.5f) {
					ship.getVelocity().set(source.getVelocity());
				} else if (progress > 0.75f){
					ship.setCollisionClass(collisionClass);
					ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("ShardSpawnerInvuln");
				}
				
//					Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(source.getLocation(), ship.getLocation()));
//					dir.scale(amount * 50f * progress);
//					Vector2f.add(ship.getLocation(), dir, ship.getLocation());
				
				
				float jitterLevel = progress;
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}
				
				float jitterRange = 1f - progress;
				float maxRangeBonus = 50f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = JITTER_COLOR;
				
				ship.setJitter(this, c, jitterLevel, 25, 0f, jitterRangeBonus);
			}
			
			if (elapsed > fadeInTime) {
				for (int i = 0; i < ships.length; i++) {
					ShipAPI ship = ships[i];
					ship.setAlphaMult(1f);
					ship.setHoldFire(false);
					ship.setCollisionClass(collisionClass);
					ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("ShardSpawnerInvuln");
				}
				engine.removePlugin(this);
			}
		}
	}
	
}









