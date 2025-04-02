package com.fs.starfarer.api.impl.campaign.econ;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue.ConstructionQueueItem;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicMajority extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

	public static float STABILITY = 1f;
	public static float IMMIGRATION_BASE = 5f;
	
	public static float PRODUCTION_BASE_RURAL = 1f;
	public static Map<String, Integer> PRODUCTION_OVERRIDES = new LinkedHashMap<String, Integer>();
	// makes explaining the effect more complicated; don't do it
	static {
		//PRODUCTION_OVERRIDES.put(Industries.FARMING, 2);
	}
	
	public static int BONUS_MULT_DEFEATED_EXPEDITION = 2;

	
	@Deprecated
	public static String [] luddicFactions = new String [] {
		"knights_of_ludd",
		"luddic_church",
		"luddic_path",
	};
	
	
	public void apply(String id) {
		if (!matchesBonusConditions(market)) {
			unapply(id);
			return;
		}
		
		market.addTransientImmigrationModifier(this);
		
		int stability = (int) Math.round(STABILITY * getEffectMult());
		if (stability != 0) {
			market.getStability().modifyFlat(id, stability, "Luddic majority");
		}
		
		float mult = getEffectMult();
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_RURAL) || PRODUCTION_OVERRIDES.containsKey(ind.getId())) {
				int production = (int) Math.round(PRODUCTION_BASE_RURAL * mult);
				if (PRODUCTION_OVERRIDES.containsKey(ind.getId())) {
					production = (int) Math.round(PRODUCTION_OVERRIDES.get(ind.getId()) * mult);
				}
				if (production != 0) {
					ind.getSupplyBonusFromOther().modifyFlat(id, production, "Luddic majority");
				}
			}
		}
	}

	public void unapply(String id) {
		market.removeTransientImmigrationModifier(this);
		
		market.getStability().unmodify(id);
		
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_RURAL) || PRODUCTION_OVERRIDES.containsKey(ind.getId())) {
				ind.getSupplyBonusFromOther().unmodifyFlat(id);
			}
		}
	}
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float bonus = getImmigrationBonus(true);
		if (bonus > 0) {
			incoming.add(Factions.LUDDIC_CHURCH, bonus);
			incoming.getWeight().modifyFlat(getModId(), bonus, "Luddic immigration (Luddic majority)");
		}
	}
	
	public float getImmigrationBonus(boolean withEffectMult) {
		float bonus = IMMIGRATION_BASE * market.getSize();
		if (withEffectMult) bonus *= getEffectMult();
		return bonus;
	}
	
	public float getEffectMult() {
		if (market.isPlayerOwned() && 
				LuddicChurchHostileActivityFactor.isDefeatedExpedition()) {
			return BONUS_MULT_DEFEATED_EXPEDITION;
		}
		return 1f;
		
	}
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		String name = market.getName();
		float opad = 10f;
		
		tooltip.addPara("A majority of the population of " + name + " are Luddic faithful. "
				+ "This may result in a substantial boost "
				+ "to stability and productivity.", opad);
		
		tooltip.addPara("For colonies outside the core, it may also result in increased population growth, "
				+ "from Luddic immigrants seeking to escape the sometimes oppressive influence of the Luddic Church.", opad);

		tooltip.addPara("%s stability", 
				opad, Misc.getHighlightColor(),
				"+" + (int)STABILITY);
		
		tooltip.addPara("%s production for Farming, Light Industry, and similar", 
				opad, Misc.getHighlightColor(),
				"+" + (int)PRODUCTION_BASE_RURAL);

		if (market.isPlayerOwned()) {
			tooltip.addPara("%s population growth",
					opad, Misc.getHighlightColor(), 
					"+" + (int) getImmigrationBonus(false));
		}

		if (!Global.CODEX_TOOLTIP_MODE) {
			addConditions(tooltip, market, opad);
		}
	}
	
	public static void addConditions(TooltipMakerAPI tooltip, MarketAPI market, float opad) {
		boolean madeDeal = LuddicChurchHostileActivityFactor.isMadeDeal() && market.isPlayerOwned();
		boolean freePort = market.isFreePort();
		freePort = false;
		boolean habitable = market.hasCondition(Conditions.HABITABLE);
		
		boolean hasRural = false;
		boolean hasIndustrial = false;
		boolean hasMilitary = false;
		String heavy = null;
		String military = null;
		String rural = null;
		
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_INDUSTRIAL)) {
				if (heavy == null) heavy = ind.getCurrentName();
				hasIndustrial = true;
			}
			if (ind.getSpec().hasTag(Industries.TAG_MILITARY) || ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
				if (military == null) military = ind.getCurrentName();
				hasMilitary = true;
			}
			
			if (ind.getSpec().hasTag(Industries.TAG_RURAL)) {
				if (rural == null) rural = ind.getCurrentName();
				hasRural = true;
			}
		}
		
		if (market.getConstructionQueue() != null) {
			for (ConstructionQueueItem item : market.getConstructionQueue().getItems()) {
				IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(item.id);
				if (spec != null) {
					if (spec.hasTag(Industries.TAG_INDUSTRIAL)) {
						if (heavy == null) heavy = spec.getName();
						hasIndustrial = true;
					}
					if (spec.hasTag(Industries.TAG_MILITARY) || spec.hasTag(Industries.TAG_COMMAND)) {
						if (military == null) military = spec.getName();
						hasMilitary = true;
					}
					
					if (spec.hasTag(Industries.TAG_RURAL)) {
						if (rural == null) rural = spec.getName();
						hasRural = true;
					}
				}
				break;
			}
		}
		
		boolean matches = matchesBonusConditions(market);
		
		if (!matches) {
			if (market.isPlayerOwned()) {
				tooltip.addPara("The following factors result in these bonuses being negated, and, "
						+ "unless addressed, will result in the \"Luddic Majority\" condition "
						+ "being removed if the colony increases in size:", opad,
						Misc.getNegativeHighlightColor(), "negated", "removed");
			} else {
				tooltip.addPara("The following factors result in these bonuses being negated:", opad,
						Misc.getNegativeHighlightColor(), "negated", "removed");
			}
			//opad = 5f;
			tooltip.setBulletedListMode("    - ");
			if (market.getAdmin() != null && market.getAdmin().getId().equals(People.DARDAN_KATO)) {
				tooltip.addPara("Dardan Kato's \"policies\"", opad);
				opad = 0f;
			}
			if (madeDeal) {
				tooltip.addPara("Deal made with Luddic Church to curtail immigration", opad);
				opad = 0f;
			}
			if (freePort) {
				tooltip.addPara("The colony is a free port", opad);
				opad = 0f;
			}
			if (!habitable) {
				tooltip.addPara("The colony is not habitable", opad);
				opad = 0f;
			}
			if (!hasRural) {
				tooltip.addPara("The colony has no suitable employment for the faithful, such as farming or light industry", opad);
				opad = 0f;
			}
			if (hasIndustrial) {
				tooltip.addPara("The colony has heavy industrial facilities (" + heavy + ")", opad);
				opad = 0f;
			}
			if (hasMilitary) {
				tooltip.addPara("The colony has military facilities (" + military + ")", opad);
				opad = 0f;
			}
			tooltip.setBulletedListMode(null);
		} else {
			if (market.isPlayerOwned() && LuddicChurchHostileActivityFactor.isDefeatedExpedition()) {
				tooltip.addPara("The bonus is doubled due to the faithful " + market.getOnOrAt() + " " + 
							market.getName() + " feeling securely out from under the direct "
						+ "influence of the Luddic Church.", opad, Misc.getPositiveHighlightColor(), "doubled");
			}
		}
		
		tooltip.setBulletedListMode("    - ");
		tooltip.setBulletedListMode(null);
	}
	

	public static boolean matchesBonusConditions(MarketAPI market) {
		if (market.isPlayerOwned() && LuddicChurchHostileActivityFactor.isMadeDeal()) return false;
		
		// feels a bit too restrictive
		//if (market.isFreePort()) return false;
		
		if (!market.hasCondition(Conditions.HABITABLE)) return false;
		
		if (market.getAdmin() != null && market.getAdmin().getId().equals(People.DARDAN_KATO)) {
			// he's that dumb about it
			return false;
		}
		
		boolean hasRural = false;
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_INDUSTRIAL)) return false;
			if (ind.getSpec().hasTag(Industries.TAG_MILITARY)) return false;
			if (ind.getSpec().hasTag(Industries.TAG_COMMAND)) return false;
			
			hasRural |= ind.getSpec().hasTag(Industries.TAG_RURAL);
		}
		
		if (market.getConstructionQueue() != null) {
			for (ConstructionQueueItem item : market.getConstructionQueue().getItems()) {
				IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(item.id);
				if (spec != null) {
					if (spec.hasTag(Industries.TAG_INDUSTRIAL)) return false;
					if (spec.hasTag(Industries.TAG_MILITARY) || spec.hasTag(Industries.TAG_COMMAND)) return false;
				}
				break;
			}
		}
		
		return hasRural;
	}

	public String getIconName() {
		if (!matchesBonusConditions(market)) {// && market.isPlayerOwned()) {
			return Global.getSettings().getSpriteName("events", "luddic_majority_unhappy");
		}
		return super.getIconName();
	}
}








