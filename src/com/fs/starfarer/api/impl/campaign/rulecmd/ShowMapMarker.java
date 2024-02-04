package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowIntelMarker <entity or market id> <optional title> <optional text>
 */
public class ShowMapMarker extends BaseCommandPlugin {

	public ShowMapMarker() {
		
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String id = params.get(0).getString(memoryMap);
		
		SectorEntityToken entity = Global.getSector().getEntityById(id);
		if (entity == null) {
			MarketAPI market = Global.getSector().getEconomy().getMarket(id);
			if (market != null) {
				entity = market.getPrimaryEntity();
			}
		}
		
		if (entity == null) return false;
		
		String title = entity.getName();
		if (params.size() >= 2) {
			title = params.get(1).getString(memoryMap);
		}
		String text = null;
		if (params.size() >= 3) {
			text = params.get(2).getString(memoryMap);
		}
		
		String icon = Global.getSettings().getSpriteName("intel", "discovered_entity");
		
		Set<String> tags = new LinkedHashSet<String>();
		tags.add(Tags.INTEL_NEW);
		
		dialog.getVisualPanel().showMapMarker(entity, title, entity.getFaction().getBaseUIColor(), 
											true, icon, text, tags);
		
		return true;
	}
	

}


