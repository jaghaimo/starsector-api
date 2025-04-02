package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.DCPPlugin;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMemberOffsetModifier;
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI;
import com.fs.starfarer.api.util.ListMap;

public class ShroudedMaelstromShipCreator extends BaseDwellerShipCreator {

	public static float FLUX_COST_MULT = 1f;
	public static float RANGE_BONUS = 200f;
	
//	public static float RL_DELAY = 0.33f;
//	public static float IE_DELAY = 0.25f;
	
	@Override
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.initBeforeShipCreation(hullSize, stats, id);
		
//		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
//		stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
//		stats.getMissileWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
		
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
	}

	
	@Override
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		DwellerCombatPlugin plugin = super.createPlugin(ship);
		plugin.setPlugin(new DCPPlugin() {
			ListMap<WeaponAPI> weapons = new ListMap<>();
			int [] currIndex = new int[10];
			float [] currElapsed = new float [10];
			@Override
			public void advance(DwellerCombatPlugin plugin, float amount) {
				CombatEntityAPI attachedTo = plugin.getAttachedTo();
				if (attachedTo instanceof ShipAPI) {
					ShipAPI ship = (ShipAPI) attachedTo;

					if (weapons.isEmpty()) {
						for (WeaponAPI w : ship.getAllWeapons()) {
							if (w.isDecorative()) continue;
							weapons.add(w.getId(), w);
						}
					}
					
					int i = 0;
					for (List<WeaponAPI> list : weapons.values()) {
						
						WeaponAPI w = list.get(0);
						float delay = ((ProjectileWeaponSpecAPI)w.getSpec()).getRefireDelay() / (float) list.size();
						
						currElapsed[i] += amount;
						while (currElapsed[i] > delay) {
							currElapsed[i] -= delay;
							currIndex[i]++;
							currIndex[i] %= list.size();
						}

						int index = 0;
						for (WeaponAPI curr : list) {	
							if (index != currIndex[i]) {
								curr.setForceDisabled(true);
							} else {
								curr.setForceDisabled(false);
							}
							index++;
						}
						
						i++;
					}
				}
			}
		});
		
		
		List<DwellerShipPart> parts = plugin.getParts();
		
		float scale = 1f;
		scale = 1.33f;
		
		//scale = 1.5f;
		
		float spinMult = 1f;
		spinMult = 0.5f;
		spinMult = 0.125f;
		
		
		
		
		/*part = new WobblyPart("shrouded_maelstrom_base", 2f * scale, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(360f * 2f * spinMult, 360f * 2f * spinMult, 360f * spinMult);
		part.alphaMult = 0.5f;
		parts.add(part);
		*/
		
		
		WobblyPart  part = new WobblyPart("shrouded_vortex_base2", 3f * scale, 3, 3, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(270f * 2f * spinMult, 360f * 2f * spinMult, 270f * spinMult);
		part.alphaMult = 0.5f;
		parts.add(part);
		
		part = new WobblyPart("shrouded_vortex_base2", 2f * scale, 3, 3, 1f, new Vector2f(0, 0), 180f);
		part.setSpin(270f * 2f * spinMult, 360f * 2f * spinMult, 270f * spinMult);
		part.alphaMult = 0.4f;
		parts.add(part);
		
		//WobblyPart part = new WobblyPart("shrouded_vortex_base", 3f * scale, 1f, new Vector2f(0, 0), 0f);

		
		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
		// eye spot clusters
		part = new WobblyPart("shrouded_eye_cluster1", 1.2f * scale, 5, 5, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(50f * 2f * spinMult, 90f * 2f * spinMult, 320f * spinMult);
		part.color = glow;
		part.additiveBlend = true;
		parts.add(part);
		
		part = new WobblyPart("shrouded_eye_cluster2", 1.2f * scale, 5, 5, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(60f * 2f * spinMult, 100f * 2f * spinMult, 320f * spinMult);
		part.color = glow;
		part.additiveBlend = true;
		parts.add(part);
		
		part = new WobblyPart("shrouded_eye_cluster3", 1.2f * scale, 5, 5, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(70f * 2f * spinMult, 110f * 2f * spinMult, 320f * spinMult);
		part.color = glow;
		part.additiveBlend = true;
		parts.add(part);
		
		
		part = new WobblyPart("shrouded_maelstrom_base", 1.8f * scale, 1f, new Vector2f(0, 0), 0f);
		part.setSpin(300f * 2f * spinMult, 340f * 2f * spinMult, 320f * spinMult);
		parts.add(part);
		
		

		
		
//		WobblyPart part = new WobblyPart("shrouded_maelstrom", 0.3f * scale, 1f, new Vector2f(0, 0), 0f);
//		parts.add(part);
//		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
//		part = new WobblyPart("clusterA", 1f * scale, 3, 3, 2f, new Vector2f(70f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setWeaponActivated();
//		parts.add(part);
//		
//		part = new WobblyPart("clusterB", 1f * scale, 3, 3, 2f, new Vector2f(-10f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setFluxActivated();
//		parts.add(part);
//		
//		part = new WobblyPart("coronet_stalks", 0.5f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setShieldActivated();
//		parts.add(part);
		
		return plugin;
	}

	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
//		numMembers = 100;
//		radius = 150f;
		
		params.maxOffset = 200f;
		params.initialMembers = 200;
		params.baseMembersToMaintain = params.initialMembers;
		
		params.numToRespawn = 2;
		params.numToFlash = 3;
		
		params.maxOffset = 250f;
		params.numToFlash = 4;
		params.baseSpriteSize *= 1.33f;
		
		params.spawnOffsetMult = 0.67f;
		params.spawnOffsetMultForInitialSpawn = params.spawnOffsetMult;
		
		
		params.offsetModifier = new SwarmMemberOffsetModifier() {
			@Override
			public void modifyOffset(SwarmMember p) {
				p.offset.x *= 0.85f;
				p.offset.y *= 1.25f;
			}
		};
		
	}
	
	
}









