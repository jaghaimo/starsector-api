package com.fs.starfarer.api.impl.campaign.econ;

import java.util.Map;

/**
 * Unused.
 */
public class Smuggling extends BaseMarketConditionPlugin {
	
	public Smuggling() {
	}

	public void apply(String id) {
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}
	
	@Override
	public void setParam(Object param) {
	}
	
	public Map<String, String> getTokenReplacements() {
		Map<String, String> tokens = super.getTokenReplacements();
		
		return tokens;
	}

	@Override
	public String[] getHighlights() {
		return null;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	
	
}
