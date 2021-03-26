package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GenericSpecialItemPlugin extends BaseSpecialItemPlugin {
	
	protected void addInstalledInSection(TooltipMakerAPI tooltip, float pad) {
		String list = "";
		String [] params = spec.getParams().split(",");
		String [] array = new String[params.length];
		int i = 0;
		for (String curr : params) {
			curr = curr.trim();
			IndustrySpecAPI ind = Global.getSettings().getIndustrySpec(curr);
			if (ind == null) continue;
			list += ind.getName() + ", ";
			array[i] = ind.getName();
			i++;
		}
		if (!list.isEmpty()) {
			list = list.substring(0, list.length() - 2);
			tooltip.addPara(list, pad, 
					Misc.getGrayColor(), Misc.getBasePlayerColor(), array);
					//Misc.getGrayColor(), Misc.getHighlightColor(), array);
					//Misc.getGrayColor(), Misc.getTextColor(), array);
		}
	}
	
	public static void addReqsSection(Industry industry, InstallableItemEffect effect, TooltipMakerAPI tooltip, boolean withRequiresText, float pad) {
		List<String> reqs = effect.getRequirements(industry);
		List<String> unmet = effect.getUnmetRequirements(industry);
		
		if (reqs == null) reqs = new ArrayList<String>();
		if (unmet == null) unmet = new ArrayList<String>();
		
		Color [] hl = new Color[reqs.size()];
		
		int i = 0;
		String list = "";
		for (String curr : reqs) {
			list += curr + ", ";
			
			if (unmet.contains(curr)) {
				hl[i] = Misc.getNegativeHighlightColor();
			} else {
				hl[i] = Misc.getBasePlayerColor();
				//hl[i] = Misc.getHighlightColor();
				//hl[i] = Misc.getTextColor();
			}
			i++;
		}
		if (!list.isEmpty()) {
			list = list.substring(0, list.length() - 2);
			list = Misc.ucFirst(list);
			reqs.set(0, Misc.ucFirst(reqs.get(0)));
			
			float bulletWidth = 70f;
			if (withRequiresText) {
				tooltip.setBulletWidth(bulletWidth);
				tooltip.setBulletColor(Misc.getGrayColor());
				tooltip.setBulletedListMode("Requires:");
			}
			
			LabelAPI label = tooltip.addPara(list, Misc.getGrayColor(), pad);
			label.setHighlightColors(hl);
			label.setHighlight(reqs.toArray(new String[0]));
			
			if (withRequiresText) {
				tooltip.setBulletedListMode(null);
			}
		}
		
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		//super.createTooltip(tooltip, expanded, transferHandler, stackSource, false);
		
		float pad = 0f;
		float opad = 10f;
		
		tooltip.addTitle(getName());
		
		LabelAPI design = Misc.addDesignTypePara(tooltip, getDesignType(), opad);
		
		float bulletWidth = 70f;
		if (design != null) {
			bulletWidth = design.computeTextWidth("Design type: ");
		}
		
		InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(getId());
		if (effect != null) {
			tooltip.setBulletWidth(bulletWidth);
			tooltip.setBulletColor(Misc.getGrayColor());
			
			tooltip.setBulletedListMode("Installed in:");
			addInstalledInSection(tooltip, opad);
			tooltip.setBulletedListMode("Requires:");
			addReqsSection(null, effect, tooltip, false, pad);
			
			tooltip.setBulletedListMode(null);
			
			if (!spec.getDesc().isEmpty()) {
				Color c = Misc.getTextColor();
				//if (useGray) c = Misc.getGrayColor();
				tooltip.addPara(spec.getDesc(), c, opad);
			}
			effect.addItemDescription(null, tooltip, new SpecialItemData(getId(), null), InstallableItemDescriptionMode.CARGO_TOOLTIP);
		} else {
			if (!spec.getDesc().isEmpty()) {
				Color c = Misc.getTextColor();
				tooltip.addPara(spec.getDesc(), c, opad);
			}
		}
			

		addCostLabel(tooltip, opad, transferHandler, stackSource);
	}
	
}




