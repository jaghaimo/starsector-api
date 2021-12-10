package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseMarketConditionPlugin implements MarketConditionPlugin {

	protected MarketAPI market;
	protected MarketConditionAPI condition;
	
	public void init(MarketAPI market, MarketConditionAPI condition) {
		this.market = market;
		this.condition = condition;
	}
	
	public void apply(String id) {
	}

	public void unapply(String id) {
	}
	
	
	public void advance(float amount) {
		
	}
	
	public String getModId() {
		return condition.getIdForPluginModifications();
	}
	
	
	public static float getLowStabilityBonusMult(MarketAPI market) {
		float s = market.getStabilityValue();
		//if (true) return 1f + (10f - s) * 0.2f;
		if (true) return 1f + (10f - s) * 0.1f;
		
		switch ((int)market.getStabilityValue()) {
		case 0:
			return 3f;
		case 1:
			return 2.5f;
		case 2:
			return 2f;
		case 3:
			return 1.5f;
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return 1f;
		default:
			return 1f;
		}
	}
	
	public static float getLowStabilityPenaltyMult(MarketAPI market) {
		float s = market.getStabilityValue();
		//if (true) return 0.1f + s * 0.09f;
		if (true) return 0.5f + s * 0.05f;
		
		switch ((int)market.getStabilityValue()) {
		case 0:
			return 0.1f;
		case 1:
			return 0.25f;
		case 2:
			return 0.5f;
		case 3:
			return .75f;
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return 1f;
		default:
			return 1f;
		}
	}
	
	public static float getHighStabilityBonusMult(MarketAPI market) {
		float s = market.getStabilityValue();
		//if (true) return 1f + s * 0.2f;
		if (true) return 1f + s * 0.1f;
		
		switch ((int)market.getStabilityValue()) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			return 1f;
		case 7:
			return 1.5f;
		case 8:
			return 2f;
		case 9:
			return 2.5f;
		case 10:
			return 3f;
		default:
			return 1f;
		}
	}
	
	public static float getHighStabilityPenaltyMult(MarketAPI market) {
		float s = market.getStabilityValue();
		//if (true) return 0.1f + (10f - s) * 0.09f;
		if (true) return 0.5f + (10f - s) * 0.05f;
		
		switch ((int)market.getStabilityValue()) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			return 1f;
		case 7:
			return .75f;
		case 8:
			return 0.5f;
		case 9:
			return 0.25f;
		case 10:
			return 0.1f;
		default:
			return 1f;
		}		
	}
	
	public static void main(String[] args) {
	}


	public List<String> getRelatedCommodities() {
		return null;
	}

	public void setParam(Object param) {
		
	}

	public Map<String, String> getTokenReplacements() {
		HashMap<String, String> tokens = new LinkedHashMap<String, String>();
		
		tokens.put("$playerName", Global.getSector().getCharacterData().getName());
		if (market != null) {
			tokens.put("$marketFaction", market.getFaction().getDisplayName());
			tokens.put("$TheMarketFaction", Misc.ucFirst(market.getFaction().getDisplayNameWithArticle()));
			tokens.put("$theMarketFaction", market.getFaction().getDisplayNameWithArticle());
			
			if (market.getPrimaryEntity().getLocation() instanceof StarSystemAPI) {
				//tokens.put("$marketSystem", ((StarSystemAPI)eventTarget.getLocation()).getBaseName() + " star system");
				tokens.put("$marketSystem", ((StarSystemAPI)market.getPrimaryEntity().getLocation()).getBaseName());
			} else {
				tokens.put("$marketSystem", "hyperspace");
			}
			tokens.put("$market", market.getName());
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet != null) {
			String fleetOrShip = "fleet";
			if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
				fleetOrShip = "ship";
				if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
					fleetOrShip = "fighter wing";
				}
			}
			tokens.put("$playerShipOrFleet", fleetOrShip);
		}
		
		return tokens;
	}

	public String[] getHighlights() {
		return null;
	}
	
	public Color[] getHighlightColors() {
		String [] highlights = getHighlights();
		if (highlights != null) {
			Color c = Global.getSettings().getColor("buttonShortcut");
			Color [] colors = new Color[highlights.length];
			Arrays.fill(colors, c);
			return colors;
		}
		return null;
	}
	
	
	public void addTokensToList(List<String> list, String ... keys) {
		Map<String, String> tokens = getTokenReplacements();
		for (String key : keys) {
			if (tokens.containsKey(key)) {
				list.add(tokens.get(key));
			}
		}
	}

	public boolean isTransient() {
		return true;
	}

	public boolean showIcon() {
		return true;
	}
	
	
	public boolean hasCustomTooltip() {
		return true;
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float opad = 10f;
		
		Color color = market.getTextColorForFactionOrPlanet();
		tooltip.addTitle(condition.getName(), color);
		
		String text = condition.getSpec().getDesc();
		Map<String, String> tokens = getTokenReplacements();
		if (tokens != null) {
			for (String token : tokens.keySet()) {
				String value = tokens.get(token);
				text = text.replaceAll("(?s)\\" + token, value);
			}
		}
		
		if (!text.isEmpty()) {
			LabelAPI body = tooltip.addPara(text, opad);
			if (getHighlights() != null) {
				if (getHighlightColors() != null) {
					body.setHighlightColors(getHighlightColors());
				} else {
					body.setHighlightColor(Misc.getHighlightColor());
				}
				body.setHighlight(getHighlights());
			}
		}
		
		createTooltipAfterDescription(tooltip, expanded);
	}
	
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	
	public boolean isTooltipExpandable() {
		return false;
	}
	
	public float getTooltipWidth() {
		return 500f;
	}

	public boolean isPlanetary() {
		if (condition == null) return true;
		
		return condition.getSpec().isPlanetary();
	}

	public boolean runWhilePaused() {
		return false;
	}

	public String getIconName() {
		return condition.getSpec().getIcon();
	}

	public String getName() {
		return condition.getSpec().getName();
	}
	
	

}




