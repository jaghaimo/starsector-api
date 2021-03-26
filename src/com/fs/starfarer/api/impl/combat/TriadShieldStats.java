package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;

public class TriadShieldStats extends BaseShipSystemScript implements DamageTakenModifier {

	public static Color JITTER_COLOR = new Color(100,50,255,75);
	public static Color JITTER_UNDER_COLOR = new Color(100,50,255,155);
	
	public static class TriadShieldVisuals extends CombatEntityPluginWithParticles {
		public ShipAPI ship;
		public TriadShieldStats script;
		
		public TriadShieldVisuals(ShipAPI ship, TriadShieldStats script) {
			this.ship = ship;
			this.script = script;
		}
		
	
		@Override
		public EnumSet<CombatEngineLayers> getActiveLayers() {
			//return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
			return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
		}
		@Override
		public boolean isExpired() {
			return false;
		}
		@Override
		public float getRenderRadius() {
			return ship.getCollisionRadius() + 100f;
		}
		
		@Override
		public void advance(float amount) {
			super.advance(amount);
			
			entity.getLocation().set(ship.getLocation());
			if (Global.getCombatEngine().isPaused()) return;
		}

		@Override
		public void render(CombatEngineLayers layer, ViewportAPI viewport) {
			super.render(layer, viewport);
			
			float alphaMult = viewport.getAlphaMult();
			
			ShipSystemAPI system = ship.getPhaseCloak();
			if (system == null) system = ship.getSystem();
			alphaMult *= system.getEffectLevel();
			if (alphaMult <= 0f) return;
			
			GL11.glPushMatrix();
			GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0);
			GL11.glRotatef(ship.getFacing(), 0, 0, 1);
			
			GL11.glPopMatrix();
			
		}
	}
	
	protected TriadShieldVisuals visuals = null;
	
	public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
		return null;
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		if (visuals == null) {
			visuals = new TriadShieldVisuals(ship, this);
			Global.getCombatEngine().addLayeredRenderingPlugin(visuals);
			ship.addListener(this);
		}
		
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		
		if (state == State.COOLDOWN || state == State.IDLE) {
			unapply(stats, id);
			return;
		}
		
		ShipSystemAPI system = ship.getPhaseCloak();
		if (system == null) system = ship.getSystem();
		
		
		
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float jitterRangeBonus = jitterLevel * 0f;
		if (state == State.IN || state == State.ACTIVE) {
			
		} else if (state == State.OUT) {

		}
		
		float minJitter = 4;
		JITTER_COLOR = new Color(100,100,255,75);
		JITTER_UNDER_COLOR = new Color(100,100,255,155);
		
		//JITTER_COLOR = new Color(100,50,255,125);
//		JITTER_UNDER_COLOR = new Color(100,100,255,100);
//		JITTER_UNDER_COLOR = new Color(50,50,125,100);
//		JITTER_COLOR = new Color(100,255,50,125);
//		JITTER_UNDER_COLOR = new Color(255,100,100,100);
		if (jitterLevel > 0) {
			ship.setCircularJitter(true);
			ship.setJitter(this, JITTER_COLOR, jitterLevel, 1, 0f, 7f + jitterRangeBonus);
			ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, minJitter, minJitter + jitterRangeBonus);
		}
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

}
