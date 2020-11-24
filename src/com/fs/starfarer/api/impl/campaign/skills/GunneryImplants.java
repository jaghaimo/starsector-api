package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class GunneryImplants {
	
	public static final float RECOIL_BONUS = 50f;
	public static final float TARGET_LEADING_BONUS = 100f;
	public static final float RANGE_BONUS = 15f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
			stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
			// slower recoil recovery, also, to match the reduced recoil-per-shot
			// overall effect is same as without skill but halved in every respect
			stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxRecoilMult().unmodify(id);
			stats.getRecoilPerShotMult().unmodify(id);
			stats.getRecoilDecayMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(RECOIL_BONUS) + "% weapon recoil";
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
			stats.getAutofireAimAccuracy().modifyFlat(id, TARGET_LEADING_BONUS * 0.01f);
			//stats.getCargoMod().modifyFlat(id, 100 * level);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getAutofireAimAccuracy().unmodify(id);
			//stats.getCargoMod().unmodify();
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(TARGET_LEADING_BONUS) + "% target leading accuracy for autofiring weapons";
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
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
			stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponRangeBonus().unmodify(id);
			stats.getEnergyWeaponRangeBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(RANGE_BONUS) + "% ballistic & energy weapon range";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
