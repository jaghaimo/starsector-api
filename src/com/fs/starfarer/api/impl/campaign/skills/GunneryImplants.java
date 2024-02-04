package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class GunneryImplants {
	
	public static float RECOIL_BONUS = 25f;
	public static float TARGET_LEADING_BONUS = 100f;
	public static float RANGE_BONUS = 15f;
	public static float RANGE_BONUS_ELITE = 5f;
	
	public static float EW_FRIGATES = 4f;
	public static float EW_DESTROYERS = 2f;
	public static float EW_OTHER = 1f;
	
	public static class Level1A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float bonus = 0f;
			if (hullSize == HullSize.FRIGATE) bonus = EW_FRIGATES;
			if (hullSize == HullSize.DESTROYER) bonus = EW_DESTROYERS;
			if (hullSize == HullSize.CRUISER || hullSize == HullSize.CAPITAL_SHIP) bonus = EW_OTHER;
			if (bonus > 0f) {
				stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, bonus);
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
			//return "+1-4" + "% to ECM rating of ships, depending on ship size";
			return "+" + (int)EW_FRIGATES + "% to ECM rating* of fleet when piloting a frigate, " +
				   "+" + (int) EW_DESTROYERS + "% when piloting a destroyer, " +
				   "+" + (int) EW_OTHER + "% for larger hulls";
//			"Destroyers: grants " + (int)EW_DESTROYERS + "% to ECM rating of fleet";
//			return "Frigates: grants " + (int)EW_FRIGATES + "% to ECM rating of fleet\n"+
//				   "Destroyers: grants " + (int)EW_DESTROYERS + "% to ECM rating of fleet";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

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
			return "+" + (int)(RANGE_BONUS) + "% ballistic and energy weapon range";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_ELITE);
			stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_ELITE);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponRangeBonus().unmodify(id);
			stats.getEnergyWeaponRangeBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(RANGE_BONUS_ELITE) + "% ballistic and energy weapon range";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
