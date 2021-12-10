package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DerelictContingent {
	
	public static float MAX_DMODS = 5;
	//public static float MINUS_CR_PER_DMOD = 3f;
	public static float MINUS_CR_PER_DMOD = 0f;
	public static float MINUS_DP_PERCENT_PER_DMOD = 6f;

	
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			return "*Maximum effect reached " +
					"at " + (int) MAX_DMODS + " d-mods."
					;
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			return new String [] {"" + (int) MAX_DMODS};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			FleetMemberAPI member = stats.getFleetMember();
			float dmods = 0;
			if (member != null) {
				dmods = DModManager.getNumDMods(member.getVariant());
				if (dmods > MAX_DMODS) dmods = MAX_DMODS;
			}
			
			if (dmods > 0) {
				float depBonus = dmods * MINUS_DP_PERCENT_PER_DMOD;
				//stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, -depBonus);
				stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 1f - (depBonus/100f));
			
				stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).modifyFlat(id, 1f);
				if (MINUS_CR_PER_DMOD > 0) {
					float crPenalty = MINUS_CR_PER_DMOD * dmods;
					stats.getMaxCombatReadiness().modifyFlat(id, -crPenalty * 0.01f, "Derelict Operations skill");
				}
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodify(id);
			stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).unmodify(id);
			if (MINUS_CR_PER_DMOD > 0) {
				stats.getMaxCombatReadiness().unmodify(id);
			}
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara("Deployment point cost of ships reduced by %s per d-mod*", 0f, 
						hc, hc, "" + (int)MINUS_DP_PERCENT_PER_DMOD + "%");
			if (MINUS_CR_PER_DMOD > 0) {
				info.addPara("(D) hull deployment cost reduction also applies to maintenance cost,"
								+ " but maximum CR is reduced by %s per d-mod*", 0f,
								hc, hc, "" + (int)MINUS_CR_PER_DMOD + "%");
			} else {
				info.addPara("(D) hull deployment cost reduction also applies to maintenance cost", hc, 0f);
			}

		}
	}

}
