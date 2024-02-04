package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HullRestoration {
	
	public static float RECOVERY_PROB = 2f;
	public static float CR_PER_SMOD = 5;
	
	public static float CR_MAX_BONUS = 15;
	public static float CR_MINUS_PER_DMOD = 5;
	
	public static float DMOD_AVOID_MAX = 0.9f;
	public static float DMOD_AVOID_MIN = 0.75f;
	
	public static float DMOD_AVOID_MIN_DP = 5f;
	
	public static float DP_REDUCTION = 0.1f;
	public static float DP_REDUCTION_MAX = 5f;
	
	
	/**
	 * Lowest probability to avoid d-mods at this DP value and higher.
	 */
	public static float DMOD_AVOID_MAX_DP = 60f;
	
	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).modifyFlat(id, RECOVERY_PROB);	
		}
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).unmodify(id);	
		}
		public String getEffectDescription(float level) {
			return "All of your ships are almost always recoverable if lost in combat";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float dp = DMOD_AVOID_MIN_DP;
			if (stats.getFleetMember() != null) {
				dp = stats.getFleetMember().getDeploymentPointsCost();
			}
			float mult = 1f - (dp - DMOD_AVOID_MIN_DP) / (DMOD_AVOID_MAX_DP - DMOD_AVOID_MIN_DP);
			if (mult > 1f) mult = 1f;
			if (mult < 0f) mult = 0f;
			
			float probAvoid = DMOD_AVOID_MIN + (DMOD_AVOID_MAX - DMOD_AVOID_MIN) * mult;
			
			stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, 1f - probAvoid);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			String lowDP = "" + (int) DMOD_AVOID_MIN_DP;
			String highDP = "" + (int) DMOD_AVOID_MAX_DP;
			String lowChance = "" + (int) Math.round(DMOD_AVOID_MIN * 100f) + "%";
			String highChance = "" + (int) Math.round(DMOD_AVOID_MAX * 100f) + "%";
			return "Ships lost in combat have a " + lowChance + " (if " + highDP + " deployment points or higher) to " + highChance + " (" + lowDP + " DP or lower) chance to avoid d-mods";
			//"Ships lost in combat have a 90% (if 5 deployment points or lower) to 75% (50 DP or higher) chance to avoid d-mods
			//return "+" + (int)(CR_PER_SMOD) + "% maximum combat readiness per s-mod built into the hull";
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
			float num = 0f;
			if (stats.getVariant() != null) {
				num = stats.getVariant().getSMods().size();
			}
			stats.getMaxCombatReadiness().modifyFlat(id, num * CR_PER_SMOD * 0.01f, "Hull Restoration skill");
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(CR_PER_SMOD) + "% maximum combat readiness per s-mod built into the hull";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3B implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			FleetMemberAPI member = stats.getFleetMember();
			
			
			float dmods = 0;
			if (member != null) {
				dmods = DModManager.getNumDMods(member.getVariant());
			} else if (stats.getVariant() != null) {
				dmods = DModManager.getNumDMods(stats.getVariant());
			}
			
			float bonus = CR_MAX_BONUS - dmods * CR_MINUS_PER_DMOD;
			bonus = Math.round(bonus);
			if (bonus < 0) bonus = 0;
			
			stats.getMaxCombatReadiness().modifyFlat(id, bonus * 0.01f, "Hull Restoration skill");
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(CR_MAX_BONUS) + "% maximum combat readiness for all ships, minus " + (int)CR_MINUS_PER_DMOD + "% per d-mod (minimum of 0%)";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level4A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
		}
		
		public String getEffectDescription(float level) {
			if (FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL == 1) {
				return "Chance to remove one d-mod per month from a randomly selected ship in your fleet; faster for low-DP ships";
			} else if (FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL == 2) {
				return "Chance to remove a d-mod from a randomly selected ship in your fleet every two months";
			} else {
				return "Chance to remove a d-mod from a randomly selected ship in your fleet every " +
							FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL + " months";
			}
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level4B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
		}
		
		public String getEffectDescription(float level) {
			return "Chance to quickly remove one d-mod from newly acquired ships; higher for ships with more d-mods";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level5 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "pristine and near-pristine ships (at most one d-mod)");
			info.addSpacer(opad);

			String max = "" + (int) DP_REDUCTION_MAX;
			String percent = "" + (int)Math.round(DP_REDUCTION * 100f) + "%";
			info.addPara("Deployment point cost reduced by %s or %s points, whichever is less",
					0f, Misc.getHighlightColor(), Misc.getHighlightColor(), percent, max);			
			
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			FleetMemberAPI member = stats.getFleetMember();
			float dmods = 0;
			if (member != null) {
				dmods = DModManager.getNumDMods(member.getVariant());
			}
			
			if (dmods > 1) return;
			
			float baseCost = stats.getSuppliesToRecover().getBaseValue();
			float reduction = Math.min(DP_REDUCTION_MAX, baseCost * DP_REDUCTION);
			
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -reduction);
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(id);
		}
	}
}
