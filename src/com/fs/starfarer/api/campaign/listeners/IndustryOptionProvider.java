package com.fs.starfarer.api.campaign.listeners;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public interface IndustryOptionProvider {

	public class IndustryOptionData {
		public String text;
		public Color color;
		public boolean enabled = true;
		public Object id;
		public Industry ind;
		public IndustryOptionProvider provider;
		
		
		public IndustryOptionData(String text, Object id, Industry ind, IndustryOptionProvider provider) {
			this.text = text;
			this.id = id;
			this.ind = ind;
			this.provider = provider;
			
			if (ind.getMarket() != null) {
				color = ind.getMarket().getFaction().getBaseUIColor();
			} else {
				color = Misc.getBasePlayerColor();
			}
		}
	}
	
	public List<IndustryOptionData> getIndustryOptions(Industry ind);
	public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width);
	
	public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui);
	public void addToIndustryTooltip(Industry ind, IndustryTooltipMode mode, TooltipMakerAPI tooltip, float width, boolean expanded);
}








