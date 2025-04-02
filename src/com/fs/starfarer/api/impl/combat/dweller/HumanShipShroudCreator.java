package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.ShroudNegativeParticleFilter;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

public class HumanShipShroudCreator extends BaseDwellerShipCreator {
		@Override
		public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		}

		@Override
		public void initAfterShipCreation(ShipAPI ship, String id) {
		}

		@Override
		public void initAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
		}
		
		@Override
		public void initInCombat(ShipAPI ship) {
			DwellerShroud shroud = createShroud(ship);
			//setOverloadColorAndText(ship, shroud);
			Color color = Misc.setAlpha(Misc.setBrightness(shroud.getParams().flashFringeColor, 255), 255);
			ship.getFluxTracker().setOverloadColor(color);
		}
		
		@Override
		protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
			//params.maxOffset = ship.getCollisionRadius();
			float maxOffset = 100f;
			float scale = 1f;
			float overloadGlowScale = 1f;
			switch (ship.getHullSize()) {
			case CAPITAL_SHIP:
				maxOffset = 150f;
				scale = 1.75f;
				overloadGlowScale = 0.75f;
				break;
			case CRUISER:
				maxOffset = 120f;
				scale = 1.5f;
				overloadGlowScale = 0.75f;
				break;
			case DESTROYER:
				maxOffset = 100f;
				scale = 1.25f;
				overloadGlowScale = 0.5f;
				break;
			case FRIGATE:
			case FIGHTER:
				overloadGlowScale = 0.5f;
				maxOffset = 75f;
				break;
			}
			//params.maxOffset = ship.getCollisionRadius();
			params.maxOffset = maxOffset * 0.5f;
			params.initialMembers = 0;
			params.baseMembersToMaintain = params.initialMembers;
			
			params.spawnOffsetMult = 0.75f;
			
			int num = (int) (ship.getCollisionRadius() * ship.getCollisionRadius() / 800);
			if (num < 15) num = 15;
			if (ship.isDestroyer() && num < 22) num = 22;
			if (num > 150) num = 150;
			params.baseMembersToMaintain = num;
			params.initialMembers = num;
//			params.flashProbability = 0f;
//			params.flashFrequency = 0f;
//			params.alphaMult = 0.25f;
//			params.baseSpriteSize = 128f * 1.5f * 0.67f * 1.5f;
			
			//params.alphaMult = 0.25f;
			
			float numShroudMods = 0;
			for (String modId : ship.getVariant().getHullMods()) {
				HullModSpecAPI spec = Global.getSettings().getHullModSpec(modId);
				if (spec.hasTag(Tags.SHROUDED)) numShroudMods++;
			}
			
			params.alphaMult = 0.25f + (numShroudMods - 1f) * 0.1f;
			if (params.alphaMult > 0.75f) params.alphaMult = 0.75f;
			if (params.alphaMult < 0.25f) params.alphaMult = 0.25f;
			
			params.baseSpriteSize *= scale;
			
			//params.negativeParticleAlphaIntOverride = 100;
			params.negativeParticleSpeedCap = ship.getMaxSpeedWithoutBoost() + 100f;
			params.negativeParticleColorOverride = RiftLanceEffect.getColorForDarkening(ship.getSpriteAPI().getAverageColor());
			//params.negativeParticleSizeMult = 1.5f;
			params.negativeParticleSizeMult = scale;
			
			params.negativeParticleAreaMult = ship.getCollisionRadius() / params.maxOffset; 
			
//			params.negativeParticleAlphaIntOverride = 50;
//			params.negativeParticleSizeMult = 0.5f;
//			params.negativeParticleNumBase *= 3;
			
			params.overloadGlowSizeMult *= overloadGlowScale;
			params.overloadArcOffsetMult = params.negativeParticleAreaMult * 0.8f;
			
//			params.overloadArcThickness *= 2f;
//			params.overloadArcCoreThickness *= 2f;

			params.generateOffsetAroundAttachedEntityOval = true;
//			params.offsetModifier = new SwarmMemberOffsetModifier() {
//				@Override
//				public void modifyOffset(SwarmMember p) {
//					p.offset.x *= 0.75f;
//				}
//			};
			
			params.negativeParticleFilter = new ShroudNegativeParticleFilter() {
				@Override
				public boolean isParticleOk(DwellerShroud shroud, Vector2f loc) {
					if (shroud.getAttachedTo() instanceof ShipAPI) {
						ShipAPI ship = (ShipAPI) shroud.getAttachedTo();
						float targetingRadius = Misc.getTargetingRadius(loc, ship, false);
						float dist = Misc.getDistance(ship.getLocation(), loc);
						float pad = Math.max(50f, targetingRadius * 0.2f);
						pad = params.maxOffset;
						return dist < targetingRadius + pad && dist > targetingRadius * 0.75f;
					}
					return true;
				}
				
			};			
		}
	}