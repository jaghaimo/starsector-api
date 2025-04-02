package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.DCPPlugin;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.ShroudNegativeParticleFilter;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMemberOffsetModifier;
import com.fs.starfarer.api.util.Misc;

public class ShroudedMawShipCreator extends BaseDwellerShipCreator implements DCPPlugin {

	@Override
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		DwellerCombatPlugin plugin = super.createPlugin(ship);
		plugin.setPlugin(this);
		
		List<DwellerShipPart> parts = plugin.getParts();
		parts.clear();
		
		float scale = 2f;
		
		float xOff = 40f * scale;
		xOff = 0f;
		//WobblyPart part = new WobblyPart("shrouded_maw_base", 1f * scale, 1f, new Vector2f(50f * scale, 0), 0f);
		WobblyPart part = new WobblyPart("shrouded_maw_base", 1f * scale, 0.7f, new Vector2f(xOff, 0f), 0f);
		//part.alphaMult = 0.5f;
		parts.add(part);
		
		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
		
		//branching_structureA
		//teeth1 (glow)
		//teeth2
		//teeth3
		//
		
		xOff = 30f * scale;
		//part = new WobblyPart("shrouded_maw_glow", 1f * scale, 5, 5, 1f, new Vector2f(-50f * scale, 0), 0f);
		part = new WobblyPart("shrouded_maw_glow", 0.5f * scale, 5, 5, 0.5f, new Vector2f(xOff, 0f), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.alphaMult = 0.5f;
		parts.add(part);
		
		// eye spot cluster
		part = new WobblyPart("shrouded_spot_cluster", 1f * scale, 5, 5, 1f, new Vector2f(-25f * scale + xOff, 0f), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.alphaMult = 0.5f;
		parts.add(part);
		
		
		//part = new WobblyPart("shrouded_maw_tail1", 1f * scale, 5, 5, 0.5f, new Vector2f(-100f * scale, 0f), 0f);
		//part.alphaMult = 0.5f;
		//parts.add(part);
		
//		part = new WobblyPart("teeth1", 1f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		parts.add(part);
		
//		part = new WobblyPart("coronet_stalks", 0.5f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setShieldActivated();
//		parts.add(part);
		
		return plugin;
	}
	
	@Override
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.initBeforeShipCreation(hullSize, stats, id);
		
		//stats.getEnergyWeaponRangeBonus().modifyFlat(id, 500f);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, 100f);
		stats.getMissileWeaponRangeBonus().modifyFlat(id, 400f);
	}

	@Override
	protected DwellerShroud createShroud(ShipAPI ship) {
		DwellerShroud shroud = super.createShroud(ship);
		shroud.getShroudParams().negativeParticleFilter = new ShroudNegativeParticleFilter() {
			@Override
			public boolean isParticleOk(DwellerShroud shroud, Vector2f loc) {
				float facing = shroud.getAttachedTo().getFacing();
				Vector2f diff = Vector2f.sub(loc, shroud.getAttachedTo().getLocation(), new Vector2f());
				diff = Misc.rotateAroundOrigin(diff, -facing);
				return Math.abs(diff.x) < shroud.getParams().maxOffset * 0.75f;
			}
			
		};
		return shroud;
	}



	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
		params.maxOffset = 400f;
		params.initialMembers = 250;
		//params.initialMembers = 0;
		params.baseMembersToMaintain = params.initialMembers;
		params.numToRespawn = 5;
		params.numToFlash *= 3;
		
		params.spawnOffsetMult = 0.67f;
		params.spawnOffsetMultForInitialSpawn = params.spawnOffsetMult;
		
		params.baseSpriteSize *= 2f;
		
		//params.flashFrequency /= 2f;
		//params.flashFrequency = 17f;
		//params.numToFlash = 2;
		
		params.negativeParticleAreaMult = 0.9f;
		//params.negativeParticleSizeMult = 0.81f;
		params.negativeParticleSizeMult = 0.9f;
		//params.negativeParticleGenRate = 0f;
		
//		params.negativeParticleNumBase = 3;
//		params.negativeParticleNumOverloaded = 2;
		
		params.overloadGlowSizeMult *= 2f;
		params.overloadArcThickness *= 2f;
		params.overloadArcCoreThickness *= 2f;
		
//		params.maxSpeed += 500f;
//		params.springStretchMult = 30f;
//		params.baseFriction *= 10f;
//		params.frictionRange *= 1f;
		
		params.offsetModifier = new SwarmMemberOffsetModifier() {
			@Override
			public void modifyOffset(SwarmMember p) {
				p.offset.x *= 0.75f;
			}
		};
	}

	@Override
	public void advance(DwellerCombatPlugin plugin, float amount) {
		CombatEntityAPI attachedTo = plugin.getAttachedTo();
		if (attachedTo instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) attachedTo;
			ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
		}
	}
	
}









