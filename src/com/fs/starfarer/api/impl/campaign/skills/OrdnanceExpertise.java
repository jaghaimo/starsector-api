package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class OrdnanceExpertise {
	
	public static float MAX_CR_BONUS = 15;
	public static float FLUX_PER_OP = 2;
	public static float CAP_PER_OP = 20;
	
	
	

//	public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {
//		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//			ship.addListener(new RangedSpecDamageDealtMod());
//		}
//
//		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
//			ship.removeListenerOfClass(RangedSpecDamageDealtMod.class);
//		}
//		
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			
//		}
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {}
//		
//		public String getEffectDescription(float level) {
//			return null;
//		}
//		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
//											TooltipMakerAPI info, float width) {
//			init(stats, skill);
//			
//			
//			
//			if (CRITS) {
//				info.addPara("Ballistic and energy weapons have a chance to deal %s damage at long range",
//						0f, hc, hc, "+" + (int) CRIT_DAMAGE_BONUS_PERCENT + "%");
//				info.addPara(indent + "%s chance at %s range and below, " +
//						   "%s chance at %s range and above",
//						0f, tc, hc, 
//						"0%",
//						"" + (int) MIN_RANGE,
//						"" + (int) MAX_CHANCE_PERCENT + "%",
//						"" + (int) MAX_RANGE
//						);
//			} else {
//				info.addPara("Ballistic and energy weapons deal up to %s damage at long range",
//						0f, hc, hc, "+" + (int) MAX_CHANCE_PERCENT + "%");
//				info.addPara(indent + "%s at %s range and below, " +
//						   "%s at %s range and above",
//						0f, tc, hc, 
//						"0%",
//						"" + (int) MIN_RANGE,
//						"" + (int) MAX_CHANCE_PERCENT + "%",
//						"" + (int) MAX_RANGE
//						);
//			}
//			//info.addPara(indent + "Beam weapons have their damage increased by the chance percentage instead", tc, 0f);
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//	}
	
	
	public static class Level1 implements ShipSkillEffect {
		
//		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//			System.out.println("ewfwefwe");
//		}
//		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
//		}
			
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (stats.getVariant() != null) {
				MutableCharacterStatsAPI cStats = BaseSkillEffectDescription.getCommanderStats(stats);
				float flux = FLUX_PER_OP * stats.getVariant().computeWeaponOPCost(cStats);
				stats.getFluxDissipation().modifyFlat(id, flux);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxDissipation().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(FLUX_PER_OP) + " flux dissipation per ordnance point spent on weapons";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (stats.getVariant() != null) {
				MutableCharacterStatsAPI cStats = BaseSkillEffectDescription.getCommanderStats(stats);
				float flux = CAP_PER_OP * stats.getVariant().computeWeaponOPCost(cStats);
				stats.getFluxCapacity().modifyFlat(id, flux);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxCapacity().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(CAP_PER_OP) + " flux capacity per ordnance point spent on weapons";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level2 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Ordnance Expertise skill");
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MAX_CR_BONUS) + "% maximum combat readiness";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}











