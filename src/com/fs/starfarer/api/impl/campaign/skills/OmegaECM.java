package com.fs.starfarer.api.impl.campaign.skills;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class OmegaECM {
	
	public static Map<HullSize, Float> BONUS = new HashMap<ShipAPI.HullSize, Float>();
	static {
		BONUS.put(HullSize.FRIGATE, 5f);
		BONUS.put(HullSize.DESTROYER, 10f);
		BONUS.put(HullSize.CRUISER, 20f);
		BONUS.put(HullSize.CAPITAL_SHIP, 40f);
	}

	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			Float bonus = BONUS.get(hullSize);
			if (bonus != null) {
				stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, bonus);
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
			int min = (int)Math.round(BONUS.get(HullSize.FRIGATE));
			int max = (int)Math.round(BONUS.get(HullSize.CAPITAL_SHIP));
			return "+" + min + "-" + max + "% to ECM rating of ships, depending on ship size";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
