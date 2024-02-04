package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SupportDoctrine {

	public static float COMMAND_POINT_REGEN_PERCENT = 100f;
	
	public static String SUPPORT_DOCTRINE_DP_REDUCTION_ID = "support_doctrine_dp_reduction";
	public static float DP_REDUCTION = 0.2f;
	public static float DP_REDUCTION_MAX = 10f;
	
	public static boolean isNoOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
//			if (ship == Global.getCombatEngine().getShipPlayerIsTransferringCommandFrom()) {
//				return false; // player is transferring command, no bonus until the shuttle is done flying
//				// issue: won't get called again when transfer finishes
//			}
			return ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return true;
			return member.getCaptain().isDefault();
		}
	}
	
	public static boolean isOriginalNoOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
//			if (ship == Global.getCombatEngine().getShipPlayerIsTransferringCommandFrom()) {
//				return false; // player is transferring command, no bonus until the shuttle is done flying
//			}
			return ship.getOriginalCaptain() != null && ship.getOriginalCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return true;
			return member.getCaptain().isDefault();
		}
	}
	
	
	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isNoOfficer(stats)) {
				new Helmsmanship.Level1().apply(stats, hullSize, id, level);
				new Helmsmanship.Level2().apply(stats, hullSize, id, level);
				
				new DamageControl.Level2().apply(stats, hullSize, id, level);
				new DamageControl.Level3().apply(stats, hullSize, id, level);
				new DamageControl.Level4().apply(stats, hullSize, id, level);
				
				new CombatEndurance.Level1().apply(stats, hullSize, id, level);
				new CombatEndurance.Level2().apply(stats, hullSize, id, level);
				new CombatEndurance.Level3().apply(stats, hullSize, id, level);
				
				new OrdnanceExpertise.Level1().apply(stats, hullSize, id, level);
			} 
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			new Helmsmanship.Level1().unapply(stats, hullSize, id);
			new Helmsmanship.Level2().unapply(stats, hullSize, id);
			
			new DamageControl.Level2().unapply(stats, hullSize, id);
			new DamageControl.Level3().unapply(stats, hullSize, id);
			new DamageControl.Level4().unapply(stats, hullSize, id);
			
			new CombatEndurance.Level1().unapply(stats, hullSize, id);
			new CombatEndurance.Level2().unapply(stats, hullSize, id);
			new CombatEndurance.Level3().unapply(stats, hullSize, id);
			
			new OrdnanceExpertise.Level1().unapply(stats, hullSize, id);
		}
		
		public String getEffectDescription(float level) {
			return "Gain non-elite Helmsmanship, Damage Control, Combat Endurance, and Ordnance Expertise";
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
			if (isNoOfficer(stats)) {
				float baseCost = stats.getSuppliesToRecover().getBaseValue();
				float reduction = Math.min(DP_REDUCTION_MAX, baseCost * DP_REDUCTION);
				
				if (stats.getFleetMember() == null || stats.getFleetMember().getVariant() == null || 
						(!stats.getFleetMember().getVariant().hasHullMod(HullMods.NEURAL_INTERFACE) &&
						 !stats.getFleetMember().getVariant().hasHullMod(HullMods.NEURAL_INTEGRATOR))
								) {
					stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(SUPPORT_DOCTRINE_DP_REDUCTION_ID, -reduction);
				}
			} 
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(SUPPORT_DOCTRINE_DP_REDUCTION_ID);
		}
		
		public String getEffectDescription(float level) {
			String max = "" + (int) DP_REDUCTION_MAX;
			String percent = "" + (int)Math.round(DP_REDUCTION * 100f) + "%";
			return "Deployment point cost reduced by " + percent + " or " + max + " points, whichever is less";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
	public static class Level3 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addSpacer(opad);
			info.addPara("%s faster command point recovery unless command was transferred to a ship originally without an officer", 0f, hc, hc,
					"" + (int) COMMAND_POINT_REGEN_PERCENT + "%");
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (!isOriginalNoOfficer(stats)) {
				stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat(id, COMMAND_POINT_REGEN_PERCENT * 0.01f);
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).unmodify(id);
		}
	}
	
}





