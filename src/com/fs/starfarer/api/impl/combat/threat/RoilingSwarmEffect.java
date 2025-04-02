package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RoilingSwarmEffect extends BaseCombatLayeredRenderingPlugin {

	public static interface SwarmMemberOffsetModifier {
		void modifyOffset(SwarmMember p);
	}
	
	public static class RoilingSwarmParams {
		public String spriteCat = "misc";
		public String spriteKey = "threat_swarm_pieces";
		public String despawnSound = "threat_swarm_destroyed";
		
		/**
		 * Set to non-null to exchange members with nearby swarms of the same class.
		 * Swarms should have the same sprite sheet, and the same glow color.
		 */
		public String memberExchangeClass = null;
		public float memberExchangeRange = 500f;
		public int minMembersToExchange = 1;
		public int maxMembersToExchange = 3;
		public float memberExchangeRate = 0.1f;
		
		public String flockingClass = null;
		
		public float baseSpriteSize = 20f;
		
		public float baseDur = 100000f;
		public float durRange = 0f;
		public float despawnDist = 0f;

		public float baseScale = 0.5f;
		public float scaleRange = 0.5f;
		public float baseFriction = 100f;
		public float frictionRange = 500f;
		public float baseSpringConstant = 50f;
		public float springConstantNegativeRange = 20f;
		public float baseSpringFreeLength = 20f;
		public float springFreeLengthRange = 20f;
		
		public float offsetRotationDegreesPerSecond = 0f;
		
		public float lateralFrictionFactor = 20f;
		public float lateralFrictionTurnRateFactor = 0;
		//public float lateralFrictionFactor = 0.5f;
		//public float lateralFrictionTurnRateFactor = 0.2f;
		
		public float minSpeedForFriction = 25f;
		
		public float flashRateMult = 1f;
		public float flashRadius = 100f;
		public float flashCoreRadiusMult = 1f;
		public float flashFrequency = 1f;
		public int numToFlash = 1;
		public int numToRespawn = 1;
		public float preFlashDelay = 0f;
		public float flashProbability = 0f;
		public boolean renderFlashOnSameLayer = false;
		public Color flashFringeColor = new Color(255,0,0,255);
		public Color flashCoreColor = Color.white;
		
		public float alphaMult = 1f;
		public float alphaMultBase = 1f;
		public float alphaMultFlash = 1f;
		public Color color = Color.white;
		
		public float minFadeoutTime = 1f;
		public float maxFadeoutTime = 1f;
		public float minDespawnTime = 2f;
		public float maxDespawnTime = 1f;
		
		public boolean autoscale = false;
		
		/**
		 * The amount of stretch is multiplied by this and then sqrt'ed.
		 */
		public float springStretchMult = 10f;
		
		public float swarmLeadsByFractionOfVelocity = 0.03f;
		
		public float outspeedAttachedEntityBy = 100f;
		
		public float visibleRange = 500f;
		public float maxTurnRate = 60f;
		public float spawnOffsetMult = 0f;
		public float spawnOffsetMultForInitialSpawn = -1f;
		public float maxSpeed = 500f;
		public float minOffset = 0f;
		public float maxOffset = 20f;
		public boolean generateOffsetAroundAttachedEntityOval = false;
		
		public SwarmMemberOffsetModifier offsetModifier = null;
		
		public boolean withInitialMembers = true;
		public boolean withRespawn = true;
		public int initialMembers = 50;
		public int baseMembersToMaintain = 50;
		public boolean removeMembersAboveMaintainLevel = true;
		public int maxNumMembersToAlwaysRemoveAbove = -1;
		public float memberRespawnRate = 1f;
		public float offsetRerollFractionOnMemberRespawn = 0f;
		
		public Set<String> tags = new LinkedHashSet<>();
		
		public boolean keepProxBasedScaleForAllMembers = false;
		
	}
	
	
	public static class SwarmMember {
		public SpriteAPI sprite;
		
		public Vector2f offset = new Vector2f();
		public Vector2f loc = new Vector2f();
		public Vector2f vel = new Vector2f();
		
		public float scale = 1f;
		public float turnRate = 1f;
		public float angle = 1f;
		public float recentlyPicked = 0f;
		
		public float dur;
		public FaderUtil fader;
		public FaderUtil flash;
		public FaderUtil flashNext;
		
		public FaderUtil scaler;
		public float minScale;
		public boolean keepScale = false;
		
		public SwarmMember(Vector2f startingLoc, RoilingSwarmParams params, CombatEntityAPI attachedTo) {
			//sprite = Global.getSettings().getSprite("misc", "nebula_particles");
			//fx_particles2 - swirly
			// nebula_particles2 - smooth, but 2x2
			// dust_particles - smooth
			
			sprite = Global.getSettings().getSprite(params.spriteCat, params.spriteKey);
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			
			//sprite.setAdditiveBlend();
			sprite.setNormalBlend();
			
			angle = (float) Math.random() * 360f;
			
			
			rollOffset(params, attachedTo);
			
			Vector2f spawnOffset = new Vector2f(offset);
			spawnOffset.scale(params.spawnOffsetMult);
			if (params.spawnOffsetMult != 0) {
				spawnOffset = Misc.rotateAroundOrigin(spawnOffset, attachedTo.getFacing());
			}
			
			Vector2f.add(startingLoc, spawnOffset, loc);
			
			vel = Misc.getPointWithinRadius(new Vector2f(), params.maxSpeed * 0.25f);
			
//			params.maxSpeed = 1200;
//			vel = new Vector2f(0, -1400f);
//			Vector2f.add(startingLoc, new Vector2f(0, 500f), loc);
			
			dur = params.baseDur + (float) Math.random() * params.durRange;
			scale = 1f;
			
			turnRate = Math.signum((float) Math.random() - 0.5f) * params.maxTurnRate * (float) Math.random();
			
			fader = new FaderUtil(0f, 0.5f + (float) Math.random() * 0.5f,
							params.minFadeoutTime + (params.maxFadeoutTime - params.minFadeoutTime) * (float) Math.random());
			fader.fadeIn();
			
			scaler = new FaderUtil(0f, 0.5f + (float) Math.random() * 0.5f, 0.5f + (float) Math.random() * 0.5f);
			scaler.setBounce(true, true);
			scaler.fadeIn();
			
		}
		
		public void rollOffset(RoilingSwarmParams params, CombatEntityAPI attachedTo) {
			if (params.generateOffsetAroundAttachedEntityOval && attachedTo instanceof ShipAPI) {
				ShipAPI ship = (ShipAPI) attachedTo;
				
				float angle = (float) Math.random() * 360f;
				Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
				Vector2f from = new Vector2f(dir);
				from.scale(ship.getCollisionRadius() + 1000f);
				Vector2f.add(from, ship.getLocation(), from);
				
				float min = Misc.getTargetingRadius(from, ship, false);
				float max = min + params.maxOffset;
				min += params.minOffset;
				
				float f = min/(Math.max(1f, max));
				f = Math.max(0.1f, f * 0.75f);
				
				// there's definitely a smarter way than this to get a uniform distribution -am
				float r = -1f;
				for (int i = 0; i < 10; i++) {
					float test = (float) Math.sqrt(Math.random());
					if (test >= f) {
						r = test;
						break;
					}
				}
				if (r < 0f) {
					r = f + (1f - f) * (float) Math.random();
				}
				
				//dir.scale(min + (max - min) * r);
				//r = f;
				dir.scale(max * r);
				offset = dir;
				offset = Misc.rotateAroundOrigin(offset, -attachedTo.getFacing());
//				if (params.spawnOffsetMultForInitialSpawn != params.spawnOffsetMult) {
//					offset = Misc.rotateAroundOrigin(offset, (float) Math.random() * 360f);
//					
//				}
				//offset = new Vector2f(200f, 0f);
			} else {
				offset = Misc.getPointWithinRadiusUniform(new Vector2f(), params.minOffset, params.maxOffset, Misc.random);
			}
			
			if (params.offsetModifier != null) {
				params.offsetModifier.modifyOffset(this);
			}
		}
		
		public void advance(float amount, RoilingSwarmParams params) {
			loc.x += vel.x * amount;
			loc.y += vel.y * amount;
			
			angle += turnRate * amount;
			
			dur -= amount;
			if (dur <= 0) fader.fadeOut();
			
			recentlyPicked -= amount;
			if (recentlyPicked < 0) recentlyPicked = 0f;

			fader.advance(amount);
			if (flash != null) {
				flash.advance(amount * params.flashRateMult);
				if (flash.isFadedOut()) {
					flash = null;
				}
			}
			if (flash == null && flashNext != null) {
				flash = flashNext;
				flashNext = null;
			}

			if (params.autoscale && !keepScale) {
				scaler.advance(amount * 0.5f);
				scale = minScale + (1f - minScale) * scaler.getBrightness() * scaler.getBrightness();
			}
		}
		
		public void flash() {
			if (flash == null) {
				flash = new FaderUtil(0f, 0.25f, 1f);
				flash.setBounceDown(true);
				flash.fadeIn();
			}
		}
		
		public void flashNext() {
			flashNext = new FaderUtil(0f, 0.25f, 1f);
			flashNext.setBounceDown(true);
			flashNext.fadeIn();
		}
		
		public void setRecentlyPicked(float pickDuration) {
			recentlyPicked = Math.max(recentlyPicked, pickDuration);
		}
	}
	
	public static RoilingSwarmEffect getSwarmFor(CombatEntityAPI entity) {
		if (entity == null) return null;
		return getShipMap().get(entity);
	}
	
	public static String KEY_SHIP_MAP = "RoilingSwarmEffect_shipMap_key";
	public static String KEY_FLOCKING_MAP = "RoilingSwarmEffect_flockingMap_key";
	public static String KEY_EXCHANGE_MAP = "RoilingSwarmEffect_exchangeMap_key";
	
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<CombatEntityAPI, RoilingSwarmEffect> getShipMap() {
		LinkedHashMap<CombatEntityAPI, RoilingSwarmEffect> map = 
				(LinkedHashMap<CombatEntityAPI, RoilingSwarmEffect>) Global.getCombatEngine().getCustomData().get(KEY_SHIP_MAP);
		if (map == null) {
			map = new LinkedHashMap<>();
			Global.getCombatEngine().getCustomData().put(KEY_SHIP_MAP, map);
		}
		return map;
	}
	public static ListMap<RoilingSwarmEffect> getFlockingMap() {
		return getStringToSwarmMap(KEY_FLOCKING_MAP);
	}
	public static ListMap<RoilingSwarmEffect> getExchangeMap() {
		return getStringToSwarmMap(KEY_EXCHANGE_MAP);
	}
	@SuppressWarnings("unchecked")
	public static ListMap<RoilingSwarmEffect> getStringToSwarmMap(String key) {
		ListMap<RoilingSwarmEffect> map = 
				(ListMap<RoilingSwarmEffect>) Global.getCombatEngine().getCustomData().get(key);
		if (map == null) {
			map = new ListMap<>();
			Global.getCombatEngine().getCustomData().put(key, map);
		}
		return map;
	}
	
	protected RoilingSwarmParams params;
	protected List<SwarmMember> members = new ArrayList<SwarmMember>();
	
	protected CombatEntityAPI attachedTo;
	protected float elapsed = 0f;
	protected IntervalUtil flashChecker;
	protected IntervalUtil respawnChecker;
	protected IntervalUtil transferChecker;
	protected boolean spawnedInitial = false;
	protected boolean despawning = false;
	protected boolean forceDespawn = false;
	protected float sinceExchange = 0f;
	protected float maxDistFromCenterToFragment = 0f;
	
	public Object custom1;
	public Object custom2;
	public Object custom3;
	
	
	public RoilingSwarmEffect(CombatEntityAPI attachedTo) {
		this(attachedTo, new RoilingSwarmParams());
	}
	
	public RoilingSwarmEffect(CombatEntityAPI attachedTo, RoilingSwarmParams params) {
		//System.out.println("Creating swarm for " + attachedTo);
		CombatEntityAPI e = Global.getCombatEngine().addLayeredRenderingPlugin(this);
		e.getLocation().set(attachedTo.getLocation());
		
		this.attachedTo = attachedTo;
		this.params = params;
		
		// these values kinda work, too - a bit tigher
		//params.maxOffset = 20;
		//params.baseSpringFreeLength = 10;
		
		//params.initialMembers = 1000;
		//params.frictionRange = 0f;
//		params.lateralFrictionFactor = 1f;
//		params.baseSpringFreeLength = 0f;
//		params.springFreeLengthRange = 40f;
		
//		params.frictionRange = 100f;
//		params.baseFriction = 50f;
//		params.lateralFrictionFactor = 1f;
		
//		params.memberExchangeClass = "attack_swarm";
		
//		params.baseDur = 5f;
//		params.durRange = 10f;
//		params.memberRespawnRate = 10f;
		
		flashChecker = new IntervalUtil(0.5f, 1.5f);
		respawnChecker = new IntervalUtil(0.5f, 1.5f);
		transferChecker = new IntervalUtil(0.2f, 1.8f);
		
		getShipMap().put(attachedTo, this);
		if (params.flockingClass != null) {
			getFlockingMap().add(params.flockingClass, this);
		}
		if (params.memberExchangeClass != null) {
			getExchangeMap().add(params.memberExchangeClass, this);
		}
	}
	
	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}
	
	public float getRenderRadius() {
		float extra = 0f;
		if (sinceExchange < 3f) {
			extra = 500f - sinceExchange * 100f;
		}
		extra = Math.max(extra, maxDistFromCenterToFragment);
		return params.visibleRange + extra;
	}
	
	
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.FIGHTERS_LAYER,
												CombatEngineLayers.ABOVE_PARTICLES_LOWER);
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}

	public SwarmMember addMember() {
		SwarmMember sm = new SwarmMember(attachedTo.getLocation(), params, attachedTo);
		addMember(sm);
		return sm;
	}
	public void addMember(SwarmMember sm) {
		members.add(sm);
	}
	public void removeMember(SwarmMember sm) {
		members.remove(sm);
	}
	public void addMembers(int num) {
		for (int i = 0; i < num; i++) {
			addMember();
		}
	}
	public void transferMembersTo(RoilingSwarmEffect other, float fraction) {
		int num = (int) (members.size() * fraction);
		transferMembersTo(other, num);
	}
	public void transferMembersTo(RoilingSwarmEffect other, int num) {
		transferMembersTo(other, num, null, 0f);
	}
	public void transferMembersTo(RoilingSwarmEffect other, int num, Vector2f point, float maxRangeFromPoint) {
		if (num <= 0) return;
		WeightedRandomPicker<SwarmMember> picker = getPicker(true, true);
		if (point != null) {
			picker = getPicker(true, true, point, maxRangeFromPoint);
		}
		for (int i = 0; i < num; i++) {
			SwarmMember pick = picker.pickAndRemove();
			if (pick == null) break;
			
			removeMember(pick);
			other.addMember(pick);
			pick.rollOffset(other.params, other.attachedTo);
		}
	}
	
	public void despawnMembers(int num) {
		despawnMembers(num, true);	
	}
	public void despawnMembers(int num, boolean allowFirst) {
		WeightedRandomPicker<SwarmMember> picker = getPicker(false, false);
		if (!allowFirst && !members.isEmpty()) {
			picker.remove(members.get(0));
		}
		for (int i = 0; i < num; i++) {
			SwarmMember pick = picker.pickAndRemove();
			if (pick == null) break;
			pick.fader.fadeOut();
		}
	}
	
	public SwarmMember pick(float pickDuration) {
		SwarmMember pick = getPicker(true, true).pick();
		if (pick != null) {
			pick.setRecentlyPicked(pickDuration);
		}
		return pick;
	}
	
	public WeightedRandomPicker<SwarmMember> getPicker(boolean preferNonFlashing, boolean preferNonPicked, 
														Vector2f towards) {
		WeightedRandomPicker<SwarmMember> picker = new WeightedRandomPicker<>();
		float angle = Misc.getAngleInDegrees(attachedTo.getLocation(), towards);
		for (SwarmMember p : members) {
			if (p.fader.isFadingOut() || p.fader.isFadedOut()) continue;
			float w = 1000f;
			if (preferNonFlashing && p.flash != null) w *= 0.001f;
			if (preferNonPicked && p.recentlyPicked > 0) w *= 0.001f;
			
			float curr = Misc.getAngleInDegrees(attachedTo.getLocation(), p.loc);
			float diff = Misc.getAngleDiff(angle, curr);
			if (diff > 90f) {
				float f = Misc.normalizeAngle(diff - 90f) / 90f;
				if (f > 0.9999f) f = 0.9999f;
				w *= 1f - f;
				w *= 0.05f;
			}
			
			picker.add(p, w);
		}
		return picker;
	}
	public WeightedRandomPicker<SwarmMember> getPicker(boolean preferNonFlashing, boolean preferNonPicked, 
												Vector2f point, float preferMaxRangeFromPoint) {
		WeightedRandomPicker<SwarmMember> picker = new WeightedRandomPicker<>();
		for (SwarmMember p : members) {
			if (p.fader.isFadingOut() || p.fader.isFadedOut()) continue;
			float w = 1000f;
			if (preferNonFlashing && p.flash != null) w *= 0.001f;
			if (preferNonPicked && p.recentlyPicked > 0) w *= 0.001f;
			
			float dist = Misc.getDistance(point, p.loc);
			if (dist > preferMaxRangeFromPoint) {
				float f = (dist - preferMaxRangeFromPoint) / Math.max(1f, preferMaxRangeFromPoint);
				if (f > 0.9999f) f = 0.9999f;
				w *= 1f - f;
			} else {
				w *= 0.25f + 0.75f * (1f - dist / Math.max(1f, preferMaxRangeFromPoint));
			}
			
			picker.add(p, w);
		}
		return picker;
	}
	public WeightedRandomPicker<SwarmMember> getPicker(boolean preferNonFlashing, boolean preferNonPicked) {
		WeightedRandomPicker<SwarmMember> picker = new WeightedRandomPicker<>();
		for (SwarmMember p : members) {
			if (p.fader.isFadingOut() || p.fader.isFadedOut()) continue;
			float w = 1000f;
			if (preferNonFlashing && p.flash != null) w *= 0.001f;
			if (preferNonPicked && p.recentlyPicked > 0) w *= 0.001f;
			picker.add(p, w);
		}
		return picker;
	}
	
	public int getNumActiveMembers() {
		return getPicker(false, false).getItems().size();
	}
	
	public float getGlowForMember(SwarmMember p) {
		float glow = 0f;
		if (p.flash != null) {
			glow = p.flash.getBrightness();
			glow *= glow;
		}
		return glow;
	}
	
	public int getNumMembersToMaintain() {
		return params.baseMembersToMaintain;
	}
	
	public void advance(float amount) {
		//if (true) return;
		
		if (Global.getCombatEngine().isPaused() || entity == null || isExpired()) return;
		
		if (!spawnedInitial && params.withInitialMembers) {
			float origSpawnOffsetMult = params.spawnOffsetMult;
			if (params.spawnOffsetMultForInitialSpawn >= 0) {
				params.spawnOffsetMult = params.spawnOffsetMultForInitialSpawn;
			}
			addMembers(params.initialMembers - getNumActiveMembers());
			params.spawnOffsetMult = origSpawnOffsetMult;
			spawnedInitial = true;
		}
		
//		attachedTo.setCollisionClass(CollisionClass.SHIP);
//		((ShipAPI)attachedTo).getMutableStats().getHullDamageTakenMult().modifyMult("efwefwefwe", 0f);
		
		//System.out.println("Swarm members: " + members.size());
		
		entity.getLocation().set(attachedTo.getLocation());

		elapsed += amount;
		
		Vector2f aVel = attachedTo.getVelocity();
		float aSpeed = aVel.length();
		float leadAmount = aSpeed * params.swarmLeadsByFractionOfVelocity;
		
		Vector2f facingDir = Misc.getUnitVectorAtDegreeAngle(attachedTo.getFacing());
		if (attachedTo.getVelocity().length() > 1f) {
			facingDir = Misc.normalise(new Vector2f(attachedTo.getVelocity()));
		}
		
		Vector2f aLoc = new Vector2f(attachedTo.getLocation());
//		if (params.generateOffsetAroundAttachedEntityOval && attachedTo instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) attachedTo;
//			aLoc = new Vector2f(ship.getShieldCenterEvenIfNoShield());
//		}
		
		List<SwarmMember> remove = new ArrayList<>();

		float maxSpeed = params.maxSpeed;
		if (params.outspeedAttachedEntityBy != 0) {
			float minMaxSpeed = attachedTo.getVelocity().length() + params.outspeedAttachedEntityBy;
			if (minMaxSpeed > maxSpeed) maxSpeed = minMaxSpeed;
		}
		
		// springs! (sort of, sqrt instead of linear) and friction
		boolean despawnAll = shouldDespawnAll();
		
		float maxOffsetForProx = params.maxOffset;
		if (params.generateOffsetAroundAttachedEntityOval) {
			maxOffsetForProx += attachedTo.getCollisionRadius() * 0.75f;
		}
		
		
//		int flashing = 0;
//		for (SwarmMember p : members) {
//			if (p.flash != null) flashing++;
//		}
//		System.out.println("Flashing: " + flashing + " / " + members.size());
		
		float maxDistSq = 0f;
		maxDistFromCenterToFragment = 0f;
		for (SwarmMember p : members) {
			float distSq = (aLoc.x - p.loc.x) * (aLoc.x - p.loc.x) + (aLoc.y - p.loc.y) * (aLoc.y - p.loc.y);
			maxDistSq = Math.max(maxDistSq, distSq);
			if (params.despawnDist > 0 && params.despawnDist * params.despawnDist < distSq) {
				p.fader.fadeOut();
			}
					
			if (!despawnAll) {
				Vector2f offset = new Vector2f(p.offset);
				//offset.y *= p.offsetDrift;
				//offset.y = p.offsetDrift * params.maxOffset;
				//offset = Misc.rotateAroundOrigin(offset, attachedTo.getFacing() + elapsed * 5f);
				
				float prox = offset.length() / maxOffsetForProx;
				prox = 1f - prox;
				
				
				offset = Misc.rotateAroundOrigin(offset, attachedTo.getFacing() + elapsed * params.offsetRotationDegreesPerSecond);
				//offset = Misc.rotateAroundOrigin(offset, attachedTo.getFacing());
				offset.x += facingDir.x * leadAmount;
				offset.y += facingDir.y * leadAmount;
				
				if (!params.keepProxBasedScaleForAllMembers) {
					p.scale = params.baseScale + (1f - prox) * params.scaleRange;
					if (p.scale > 1f) p.scale = 1f;
				}
				
				Vector2f dest = new Vector2f(aLoc);
				Vector2f.add(dest, offset, dest);
				float dist = Misc.getDistance(p.loc, dest);
				
				Vector2f dirToDest = Misc.getUnitVector(p.loc, dest);
				Vector2f perp = new Vector2f(-dirToDest.y, dirToDest.x);
				
				float friction = params.baseFriction + params.frictionRange * prox;
				
				float k = params.baseSpringConstant - params.springConstantNegativeRange * prox;
				float freeLength = params.baseSpringFreeLength + params.springFreeLengthRange * prox;
				
	//			if (proj == Global.getCombatEngine().getPlayerShip()) {
	//				System.out.println("32ferfwefw");
	//			}
				
				float stretch = dist - freeLength;
	
				stretch = (float) (Math.sqrt(Math.abs(stretch * params.springStretchMult)) * Math.signum(stretch));
				
				float forceMag = k * Math.abs(stretch);
				if (stretch < 0) forceMag = 0; // one-way spring, only pulls 
				
				float forceMagReduction = Math.min(Math.abs(forceMag), friction);
				forceMag -= forceMagReduction;
				friction -= forceMagReduction;
				
				
				Vector2f force = new Vector2f(dirToDest);
				force.scale(forceMag * Math.signum(stretch));
				
				Vector2f acc = new Vector2f(force);
				acc.scale(amount);
				Vector2f.add(p.vel, acc, p.vel);
				
				// leftover friction - apply against current velocity
				if (friction > 0) {
					float relSpeed = Vector2f.sub(aVel, p.vel, new Vector2f()).length();
					if (relSpeed > params.minSpeedForFriction) {
						Vector2f frictionDec = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p.vel));
						frictionDec.negate();
						frictionDec.scale(Math.min(friction, p.vel.length()) * amount); 
						Vector2f.add(p.vel, frictionDec, p.vel);
					}
				}
				
				// lateral friction to damp out any orbiting behavior fast
				float lateralSpeed = Math.abs(Vector2f.dot(p.vel, perp));
				if (lateralSpeed > 0) {// && lateralSpeed > params.minSpeedForFriction) {
					Vector2f frictionDec = new Vector2f(perp);
					if (Vector2f.dot(frictionDec, p.vel) > 0) {
						frictionDec.negate();
					}
					float lateralFactor = params.lateralFrictionFactor;
					lateralFactor += Math.min(Math.abs(attachedTo.getAngularVelocity()), 100f) * params.lateralFrictionTurnRateFactor;
					float lateralFriction = lateralSpeed * lateralFactor;
					frictionDec.scale(Math.min(lateralFriction, p.vel.length()) * amount); 
					Vector2f.add(p.vel, frictionDec, p.vel);
				}
				
			
				
				float speed = p.vel.length();
				if (speed > maxSpeed) {
					p.vel.scale(maxSpeed / speed);
				}
				
			}
			
			p.advance(amount, params);
			//p.loc.set(dest);
			
			if (despawnAll) {
				if (!p.fader.isFadingOut() && !p.fader.isFadedOut()) {
					//p.fader.setDurationOut(2f + (float) Math.random() * 1f);
					p.fader.setDurationOut(params.minDespawnTime + 
									(params.maxDespawnTime - params.minDespawnTime) * (float) Math.random());
					p.fader.fadeOut();
				}
			}
			
			
			if (p.fader.isFadedOut()) {
				remove.add(p);
			}
		}
		
		maxDistFromCenterToFragment = (float) Math.sqrt(maxDistSq);
		
		members.removeAll(remove);
		
		if (despawnAll) {
			if (!despawning) {
				if (params.despawnSound != null) {
					Global.getSoundPlayer().playSound(params.despawnSound, 1f, 1f, entity.getLocation(), aVel);
					despawning = true;
				}
			}
		}
		
		if (isExpired()) {
//			getShipMap().remove(attachedTo);
//			getFlockingMap().remove(params.flockingClass, this);
//			getExchangeMap().remove(params.memberExchangeClass, this);
		} else if (!despawnAll && !despawning){
			exchangeWithNearbySwarms(amount);
		}
		
		
		if (!despawnAll) {
			respawnChecker.advance(amount * params.memberRespawnRate);
			if (respawnChecker.intervalElapsed() && params.withRespawn) {
				int num = getNumMembersToMaintain();
				if (members.size() < num) {
					int add = Math.min(params.numToRespawn, num - members.size());
					addMembers(add);
					
					if (params.offsetRerollFractionOnMemberRespawn > 0f) {
						int reroll = Math.round(params.offsetRerollFractionOnMemberRespawn * members.size());
						if (reroll < 1) reroll = 1;
						WeightedRandomPicker<SwarmMember> picker = getPicker(true, false);
						for (int i = 0; i < reroll; i++) {
							SwarmMember pick = picker.pickAndRemove();
							if (pick == null) break;
							pick.rollOffset(params, attachedTo);
						}
					}
					
				} else if (members.size() > num && params.removeMembersAboveMaintainLevel) {
					despawnMembers(1);
				} else if (params.maxNumMembersToAlwaysRemoveAbove >= 0 &&
						members.size() > params.maxNumMembersToAlwaysRemoveAbove) {
					int extra = members.size() - params.maxNumMembersToAlwaysRemoveAbove;
					int numRemove = (int) Math.min(extra * 0.1f, 5f);
					if (numRemove < 1) numRemove = 1;
					despawnMembers(numRemove);
				}
			}
			
			
			flashChecker.advance(amount * params.flashFrequency);
			params.preFlashDelay -= amount;
			if (params.preFlashDelay < 0) params.preFlashDelay = 0;
			if (flashChecker.intervalElapsed() && params.preFlashDelay <= 0) {
				if (params.flashProbability > 0) {
					WeightedRandomPicker<SwarmMember> notFlashing = new WeightedRandomPicker<>();
					for (SwarmMember p : members) {
						if (p.flash == null) {
							notFlashing.add(p);
						}
					}
					for (int i = 0; i < params.numToFlash; i++) {
						if ((float) Math.random() < params.flashProbability) {
							SwarmMember pick = notFlashing.pickAndRemove();
							if (pick != null) pick.flash();
						}
					}
				}
			}
		}
		
		sinceExchange += amount;
//		if (proj.didDamage()) {
//			if (!resetTrailSpeed) {
//				for (ParticleData p : particles) {
//					Vector2f.add(p.vel, projVel, p.vel);
//				}
//				projVel.scale(0f);
//				resetTrailSpeed = true;
//			}
//			for (ParticleData p : particles) {
//				float dist = p.offset.length();
//				p.vel.scale(Math.min(1f, dist / 100f));
//			}
//		}
	}
	
	public void exchangeWithNearbySwarms(float amount) {
		if (params.memberExchangeClass == null || params.memberExchangeRange <= 0) return;
		
		transferChecker.advance(amount * params.memberExchangeRate);
		if (!transferChecker.intervalElapsed()) return;
		
		
		WeightedRandomPicker<RoilingSwarmEffect> swarmPicker = new WeightedRandomPicker<>();
		
		for (RoilingSwarmEffect other : getExchangeMap().getList(params.memberExchangeClass)) {
			if (other == this || other.getEntity() == null || other.despawning) continue;
			if (other.attachedTo == null || attachedTo == null) continue;
			if (other.attachedTo.getOwner() != attachedTo.getOwner()) continue;
			
			if (other.params.memberExchangeClass == null ||
					!other.params.memberExchangeClass.equals(params.memberExchangeClass)) {
				continue;
			}
			float dist = Misc.getDistance(entity.getLocation(), other.getEntity().getLocation());
			if (dist > params.memberExchangeRange) continue;
			
			swarmPicker.add(other);
		}
			
		RoilingSwarmEffect other = swarmPicker.pick();
		if (other == null) return;
			
		int num = params.minMembersToExchange + 
				Misc.random.nextInt(params.maxMembersToExchange - params.minMembersToExchange + 1);
		
		WeightedRandomPicker<SwarmMember> picker = getPicker(true, true);
		WeightedRandomPicker<SwarmMember> pickerOther = other.getPicker(true, true);
		
		num = Math.min(num, picker.getItems().size());
		num = Math.min(num, pickerOther.getItems().size());
		
		for (int i = 0; i < num; i++) {
			SwarmMember pick = picker.pickAndRemove();
			SwarmMember otherPick = pickerOther.pickAndRemove();
			if (pick == null || otherPick == null) break;
			
			removeMember(pick);
			other.addMember(pick);
			pick.rollOffset(other.params, other.attachedTo);
			
			other.removeMember(otherPick);
			addMember(otherPick);
			otherPick.rollOffset(params, attachedTo);
			
			sinceExchange = 0f;
		}
		
	}
	

	public boolean shouldDespawnAll() {
		if (forceDespawn) return true;
		
//		if ((float) Math.random() > 0.9995f && !params.generateOffsetAroundAttachedEntityOval) {
//			forceDespawn = true;
//			return true;
//		}
		
		if (attachedTo instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) attachedTo;
			return !Global.getCombatEngine().isShipAlive(ship);
		}
		if (attachedTo instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) attachedTo;
			return !Global.getCombatEngine().isMissileAlive(missile);
		}
		
		return attachedTo.isExpired() || !Global.getCombatEngine().isEntityInPlay(attachedTo);
	}

	public boolean isExpired() {
		boolean expired = shouldDespawnAll() && members.isEmpty();
		if (expired) {
			//getFlockingMap().getList(FragmentSwarmHullmod.STANDARD_SWARM_FLOCKING_CLASS).get(0)
			getShipMap().remove(attachedTo);
			getFlockingMap().remove(params.flockingClass, this);
			getExchangeMap().remove(params.memberExchangeClass, this);
		}
		return expired;
	}
	
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		//if (true) return;
		
		//Color color = Color.white;
		Color color = params.color;
		float alphaMult = viewport.getAlphaMult();
		if (alphaMult <= 0f) return;
		
		//alphaMult = 0.1f;
		alphaMult *= params.alphaMult;
		
		if (layer == CombatEngineLayers.FIGHTERS_LAYER) {
//			float zoom = viewport.getViewMult();
//			//System.out.println("Zoom: " + zoom);
//			if (zoom >= 3f) {
//				GL11.glDisable(GL11.GL_TEXTURE_2D);
//				if (!members.isEmpty()) {
//					Color c = members.get(0).sprite.getAverageBrightColor();
//					c = Misc.interpolateColor(c, members.get(0).sprite.getAverageColor(), 0.9f);
//					//Misc.setColor(c, alphaMult);
//					GL11.glEnable(GL11.GL_POINT_SMOOTH);
//					GL11.glPointSize(params.baseSpriteSize / zoom * 0.5f);
//					GL11.glBegin(GL11.GL_POINTS);
//					for (SwarmMember p : members) {
////						float size = params.baseSpriteSize;
////						size *= p.scale * p.fader.getBrightness();
//						
//						float b = p.fader.getBrightness();
//						Misc.setColor(c, alphaMult * b);
//						GL11.glVertex2f(p.loc.x, p.loc.y);
//					}
//					GL11.glEnd();
//				}
//			} else {
				if (!members.isEmpty()) {
					members.get(0).sprite.bindTexture();
				}
				for (SwarmMember p : members) {
					float size = params.baseSpriteSize;
					size *= p.scale * p.fader.getBrightness();
					
					float b = p.fader.getBrightness();
					//b *= 0.67f;
					//b *= 0.5f;
					//b *= 0.1f;
					
					p.sprite.setAngle(p.angle);
					p.sprite.setSize(size, size);
					p.sprite.setAlphaMult(alphaMult * b * params.alphaMultBase);
					p.sprite.setColor(color);
					p.sprite.renderAtCenterNoBind(p.loc.x, p.loc.y);
					
					float glow = getGlowForMember(p);
					if (glow > 0 && params.flashCoreRadiusMult <= 0f) {
						p.sprite.setAlphaMult(alphaMult * b * glow * params.alphaMultFlash);
						p.sprite.setColor(params.flashCoreColor);
						p.sprite.setAdditiveBlend();
						//p.sprite.setNormalBlend();
						p.sprite.renderAtCenter(p.loc.x, p.loc.y);
						p.sprite.setNormalBlend();
					}
				}
//			}
		}
		
		if ((layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER && !params.renderFlashOnSameLayer) || 
				(layer == CombatEngineLayers.FIGHTERS_LAYER && params.renderFlashOnSameLayer)) {
			SpriteAPI glowSprite = Global.getSettings().getSprite("misc", "threat_swarm_glow");
			glowSprite.setAdditiveBlend();
			for (SwarmMember p : members) {
				float glow = getGlowForMember(p);
				if (glow > 0f) {
					float size = params.flashRadius * (0.5f + 0.5f * glow) * 2f;
					size *= p.scale * p.fader.getBrightness();
					
//					float f = p.offset.length() / 150f;
//					if (f > 1f) f = 1f;
//					//f = 1 - Math.min(f * 2f, 1f);
//					f = 1 - f * 0.5f;
//					//f = 0f;
//					Color color2 = Misc.interpolateColor(params.flashFringeColor, Misc.setBrightness(
//									new Color(7, 163, 169), 255), f);
					
					float b = p.fader.getBrightness();
					if (b > 0 && size > 0) {
						glowSprite.setSize(size, size);
						glowSprite.setAlphaMult(alphaMult * b * glow * 0.5f * params.alphaMultFlash);
						glowSprite.setColor(params.flashFringeColor);
						glowSprite.renderAtCenter(p.loc.x, p.loc.y);
					}
					
					float memberSize = params.baseSpriteSize;
					memberSize *= p.scale;
					memberSize *= 2f;
					memberSize *= params.flashCoreRadiusMult;
					if (b > 0 && memberSize > 0) {
						glowSprite.setSize(memberSize, memberSize);
						glowSprite.setAlphaMult(alphaMult * b * p.fader.getBrightness() * glow * 0.5f * params.alphaMultFlash);
						glowSprite.setColor(params.flashCoreColor);
						glowSprite.renderAtCenter(p.loc.x, p.loc.y);
					}
				}
			}
		}
	}

	public RoilingSwarmParams getParams() {
		return params;
	}
	public List<SwarmMember> getMembers() {
		return members;
	}
	public CombatEntityAPI getAttachedTo() {
		return attachedTo;
	}
	public boolean isDespawning() {
		return despawning;
	}
	public boolean isForceDespawn() {
		return forceDespawn;
	}
	public void setForceDespawn(boolean forceDespawn) {
		this.forceDespawn = forceDespawn;
	}
	
	
	
}




