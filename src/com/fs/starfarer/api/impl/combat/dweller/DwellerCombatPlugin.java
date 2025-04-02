package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.ShipExplosionFlareVisual;
import com.fs.starfarer.api.impl.combat.ShipExplosionFlareVisual.ShipExplosionFlareParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.MutatingValueUtil;

public class DwellerCombatPlugin extends BaseCombatLayeredRenderingPlugin implements HullDamageAboutToBeTakenListener {
	
	public static Color STANDARD_PART_GLOW_COLOR = new Color(255, 0, 50, 255);
	
	public static String WEAPON_ACTIVATED = "weapon_activated";
	public static String SHIELD_ACTIVATED = "shield_activated";
	public static String SYSTEM_ACTIVATED = "system_activated";
	public static String FLUX_ACTIVATED = "flux_activated";
	
	public static interface DCPPlugin {
		public void advance(DwellerCombatPlugin plugin, float amount);
	}
	
	
	public static class WobblyPart extends BaseDwellerShipPart {
		public WarpingSpriteRendererUtilV2 renderer;
		public boolean negativeBlend = false;
		public boolean additiveBlend = false;
		public MutatingValueUtil spin;
		public float angle = 0f;
		
		public WobblyPart(String spriteKey, float scale, float warpMult, Vector2f offset, float facingOffset) {
			this(spriteKey, scale, 5, 5, warpMult, offset, facingOffset);
		}
		public WobblyPart(String spriteKey, float scale, int verticesWide, int verticesTall, 
						float warpMult, Vector2f offset, float facingOffset) {
			super(offset, facingOffset);
			
			SpriteAPI sprite = Global.getSettings().getSprite("dweller", spriteKey);
			
			float width = sprite.getWidth() * scale;
			float height = sprite.getHeight() * scale;
			float warpAmt = width * 0.04f * warpMult;
			
			sprite.setSize(width, height);
			sprite.setCenter(width/2f, height/2f);
			renderer = new WarpingSpriteRendererUtilV2(sprite, verticesWide, verticesTall, warpAmt, warpAmt * 1.4f, 1f);
			
			spin = new MutatingValueUtil(0, 0, 0);
		}
		
		public float getAngle() {
			return angle;
		}
		public void setAngle(float angle) {
			this.angle = angle;
		}
		public void setSpin(float min, float max, float rate) {
			spin = new MutatingValueUtil(min, max, rate);
		}
		
		public MutatingValueUtil getSpin() {
			return spin;
		}
		
		public void advance(float amount) {
			super.advance(amount);
			spin.advance(amount);
			this.angle += spin.getValue() * amount;
			renderer.advance(amount);
		}
		
		public void renderImpl(float x, float y, float alphaMult, float angle, CombatEngineLayers layer) {
			if (layer == CombatEngineLayers.BELOW_INDICATORS_LAYER) {
				if (negativeBlend) {
					GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
					renderer.getSprite().setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				} else {
					if (additiveBlend) {
						renderer.getSprite().setAdditiveBlend();
					} else {
						renderer.getSprite().setNormalBlend();
					}
				}
				
				renderer.getSprite().setAlphaMult(alphaMult);
				renderer.getSprite().setColor(color);
				renderer.getSprite().setAngle(angle + facingOffset + this.angle);
				renderer.renderAtCenter(x, y);
				
				if (negativeBlend) {
					GL14.glBlendEquation(GL14.GL_FUNC_ADD);
				}
			}
		}
	}
	
	
	public static DwellerCombatPlugin getDwellerPluginFor(CombatEntityAPI entity) {
		if (entity == null) return null;
		return getShipMap().get(entity);
	}
	
	public static String KEY_SHIP_MAP = "DwellerCombatPlugin_shipMap_key";
	
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<CombatEntityAPI, DwellerCombatPlugin> getShipMap() {
		LinkedHashMap<CombatEntityAPI, DwellerCombatPlugin> map = 
				(LinkedHashMap<CombatEntityAPI, DwellerCombatPlugin>) Global.getCombatEngine().getCustomData().get(KEY_SHIP_MAP);
		if (map == null) {
			map = new LinkedHashMap<>();
			Global.getCombatEngine().getCustomData().put(KEY_SHIP_MAP, map);
		}
		return map;
	}

//	@SuppressWarnings("unchecked")
//	public static ListMap<DwellerCombatPlugin> getStringToDwellerPluginMap(String key) {
//		ListMap<DwellerCombatPlugin> map = 
//				(ListMap<DwellerCombatPlugin>) Global.getCombatEngine().getCustomData().get(key);
//		if (map == null) {
//			map = new ListMap<>();
//			Global.getCombatEngine().getCustomData().put(key, map);
//		}
//		return map;
//	}
	
	protected CombatEntityAPI attachedTo;
	protected float elapsed = 0f;
	
	protected List<DwellerShipPart> parts = new ArrayList<>();
	
	protected boolean spawnedShipExplosionParticles = false;
	protected DCPPlugin plugin = null;
	
	public Object custom1;
	public Object custom2;
	public Object custom3;
	
	
	public DwellerCombatPlugin(CombatEntityAPI attachedTo) {
		CombatEntityAPI e = Global.getCombatEngine().addLayeredRenderingPlugin(this);
		e.getLocation().set(attachedTo.getLocation());
		
		this.attachedTo = attachedTo;
		
		if (attachedTo instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) attachedTo;
			ship.addListener(this);
		}
		
		getShipMap().put(attachedTo, this);
	}
	
	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}
	
	public float getRenderRadius() {
		float extra = 300f;
		return attachedTo.getCollisionRadius() + extra;
	}
	
	//protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER);
	//protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES);
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}
	

	protected float sinceTest = 10f;
	public void advance(float amount) {
		//if (true) return;
		
		
		if (Global.getCombatEngine().isPaused() || entity == null || isExpired()) return;
		
		
		entity.getLocation().set(attachedTo.getLocation());

		elapsed += amount;
		
//		sinceTest += amount;
//		if (Keyboard.isKeyDown(Keyboard.KEY_K) && sinceTest > 1f) {
//			spawnedShipExplosionParticles = false;
//			notifyAboutToTakeHullDamage(null, (ShipAPI) attachedTo, new Vector2f(), 1000000f);
//			sinceTest = 0f;
//		}
		
//		Vector2f aVel = attachedTo.getVelocity();
//		float aSpeed = aVel.length();
//		Vector2f facingDir = Misc.getUnitVectorAtDegreeAngle(attachedTo.getFacing());
//		Vector2f aLoc = new Vector2f(attachedTo.getLocation());
		
		if (isExpired()) {
		}
		
		if (attachedTo instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) attachedTo;
			if (ship.getShield() != null) {
				ship.setJitterShields(true);
				ship.setCircularJitter(true);
				Color color = new Color(255,0,50,255);
				ship.setJitter(this, color, 1f, 3, 0f);
				//ship.getShield().applyShieldEffects(null, null, 0f, 2f, 1f);
			}
		}
		
		if (shouldDespawn()) {
			for (DwellerShipPart part : parts) {
				part.fadeOut();
			}
		} else {
			if (attachedTo instanceof ShipAPI) {
				ShipAPI ship = (ShipAPI) attachedTo;
				
				fadeOut(WEAPON_ACTIVATED, SHIELD_ACTIVATED, FLUX_ACTIVATED, SYSTEM_ACTIVATED);
				
				boolean activeWeapons = false;
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative()) continue;
					if (w.isFiring()) {
						activeWeapons = true;
						break;
					}
				}
			
				if (activeWeapons) {
					fadeIn(WEAPON_ACTIVATED);
				}
				
				if (ship.getShield() != null && ship.getShield().isOn()) {
					fadeIn(SHIELD_ACTIVATED);
				}
				
				float systemLevel = 0f;
				if (ship.getSystem() != null) systemLevel = ship.getSystem().getEffectLevel();
				if (systemLevel > 0) {
					fadeIn(SYSTEM_ACTIVATED);
				}
				setBrightness(systemLevel, SYSTEM_ACTIVATED);
				
				float fluxLevel = ship.getFluxLevel();
				if (fluxLevel > 0f) {
					fadeIn(FLUX_ACTIVATED);
				}
				setBrightness(fluxLevel, FLUX_ACTIVATED);
			}
		}
		
//		float mult = 1f;
//		mult += ((ShipAPI) attachedTo).getSystem().getEffectLevel() * 20f;
		//if (((ShipAPI) attachedTo).getSystem().isActive()) mult = 10f;
		for (DwellerShipPart part : parts) {
			part.advance(amount);
		}
		
		if (plugin != null) {
			plugin.advance(this, amount);
		}
	}
	

	public boolean shouldDespawn() {
		//if (true) return false;
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
		boolean shouldDespawn = shouldDespawn();
		if (shouldDespawn) {
			boolean allFaded = true;
			for (DwellerShipPart part : parts) {
				if (!part.getFader().isFadedOut() && part.getAlphaMult() > 0) {
					allFaded = false;
					break;
				}
			}
			if (allFaded) {
				getShipMap().remove(attachedTo);
				return true;
			}
		}
		return false;
	}
	
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		//if (true) return;
		
		//Color color = Color.white;
		float alphaMult = viewport.getAlphaMult();
		if (alphaMult <= 0f) return;
		
		Vector2f aLoc = new Vector2f(attachedTo.getLocation());
		
		for (DwellerShipPart part : parts) {
			part.render(aLoc.x, aLoc.y, alphaMult, attachedTo.getFacing() - 90f, layer);
		}
	}


	public CombatEntityAPI getAttachedTo() {
		return attachedTo;
	}

	public List<DwellerShipPart> getParts() {
		return parts;
	}

	public DwellerShipPart getPart(String id) {
		for (DwellerShipPart curr : parts) {
			if (id.equals(curr.getId())) return curr;
		}
		return null;
	}
	
	public void fadeIn(String ... tags) {
		for (DwellerShipPart part : getParts(tags)) {
			part.fadeIn();
		}
	}
	public void fadeOut(String ... tags) {
		for (DwellerShipPart part : getParts(tags)) {
			part.fadeOut();
		}
	}
	public void setAlphaMult(float alphaMult, String ... tags) {
		for (DwellerShipPart part : getParts(tags)) {
			part.setAlphaMult(alphaMult);
		}
	}
	public void setBrightness(float b, String ... tags) {
		String key = "";
		for (String tag : tags) key += tag + "_";
		if (tags.length == 1) key = tags[0];
		
		for (DwellerShipPart part : getParts(tags)) {
			part.getBrightness().shift(key, b, 0.5f, 0.5f, 1f);
		}
	}
	
	public List<DwellerShipPart> getParts(String ... tags) {
		List<DwellerShipPart> result = new ArrayList<>();
		
		OUTER: for (DwellerShipPart curr : parts) {
			for (String tag : tags) {
				if (curr.hasTag(tag)) {
					result.add(curr);
					continue OUTER;
				}
			}
		}
		
		return result;
	}


	@Override
	public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
		float hull = ship.getHitpoints();
		if (damageAmount >= hull && !spawnedShipExplosionParticles && ship.getExplosionScale() > 0f) {
			int numSwirly = 11;
			int numDark = 11;
			//numDark = 0;
			float size = ship.getCollisionRadius() * 0.25f;
			if (size < 15) size = 15;
			if (size > 50) size = 50;
			//if (DwellerCombatStrategyAI.isMaw(ship)) {
			float durMult = 1f;
			float flashMult = 1f;
			if (ship.isCapital()) {
				size = 100;
				durMult = 2f;
				flashMult = 2f;
			}
//			size = 50;
//			size = 15;
			
			float baseSize = size;
			
			CombatEngineAPI engine = Global.getCombatEngine();
			
			float rampUp = 0f;
			
			Color color = DwellerShroud.SHROUD_COLOR;
			
			for (int i = 0; i < numSwirly; i++) {
				Vector2f loc = new Vector2f(ship.getLocation());
				//loc.x += 750f;
				float scatterMult = 0.5f;
				//loc = Misc.getPointWithinRadius(loc, baseSize * 1f * scatterMult);
				loc = Misc.getPointWithinRadius(loc, size * 1f * scatterMult);
				float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
				
				float dur = 0.5f + (float) Math.random() * 0.5f;
				dur *= durMult;
				
				size *= 1.25f;

				engine.addSwirlyNebulaParticle(loc, ship.getVelocity(), s, 3f, rampUp, 0f, dur, color, false);
			}
			
			size = baseSize;
			for (int i = 0; i < numDark; i++) {
				Vector2f loc = new Vector2f(ship.getLocation());
				//loc.x += 750f;
				float scatterMult = 0.5f;
				//loc = Misc.getPointWithinRadius(loc, baseSize * 1f * scatterMult);
				loc = Misc.getPointWithinRadius(loc, size * 1f * scatterMult);
				float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
				
				float dur = 0.5f + (float) Math.random() * 0.5f;
				dur *= durMult;
				
				//size *= 1.1f;
				size *= 1.25f;
				//s *= 0.5f;
				
				engine.addNegativeSwirlyNebulaParticle(loc, ship.getVelocity(), s, 3f, rampUp, 0f, dur, color);
			}
			
			
			Vector2f expVel = ship.getVelocity();
			Vector2f expLoc = ship.getShieldCenterEvenIfNoShield();
			expLoc = ship.getLocation();
			float explosionScale = 1f;
			if (ship.isCapital()) {
				explosionScale *= 1.7f;
			} else if (ship.isCruiser()) {
				explosionScale *= 1.5f;
			} else if (ship.isDestroyer()) {
				explosionScale *= 1.5f;
			}

			Color flashColor = new Color(255,50,100,255);
			float b = 1f;

			float glowSize = (float) Math.sqrt(ship.getCollisionRadius()) * 15f * 4f;
			if (ship.isFighter()) glowSize *= 0.5f;
			glowSize *= explosionScale;
			glowSize *= flashMult;

			ShipExplosionFlareParams sp = new ShipExplosionFlareParams();
			//sp.attachedTo = this;
			float er = (float) Math.sqrt(ship.getCollisionRadius()) * 15f * 4.0f;
			float mult = 475f/994f;
			er *= mult;
			sp.flareWidth = er * 4f * explosionScale;
			sp.flareHeight = er * 1.6f * explosionScale;
			sp.color = flashColor;
			sp.fadeIn = 0.1f;
			sp.fadeOut = 2f;
			
			CombatEntityAPI e = engine.addLayeredRenderingPlugin(new ShipExplosionFlareVisual(sp));
			e.getLocation().set(expLoc);
			e.getVelocity().set(expVel);

			engine.addHitParticle(expLoc, expVel, glowSize * 1f, b, 1.5f * durMult, flashColor);
			engine.addHitParticle(expLoc, expVel, glowSize * 0.5f, b, 1.5f * durMult, flashColor);
//			if (ship.isFrigate()) { // needs just a little extra oomph 
//				engine.addHitParticle(expLoc, expVel, glowSize * 0.5f, b, 1.5f, Color.white);
//			}			
			
			spawnedShipExplosionParticles = true;
		}
		return false;
	}

	public DCPPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(DCPPlugin plugin) {
		this.plugin = plugin;
	}
	
}







