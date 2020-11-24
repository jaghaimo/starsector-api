package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class FleetDesc extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		FactionAPI faction = fleet.getFaction();
		TextPanelAPI text = dialog.getTextPanel();
		
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		
		boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
		boolean trader = mem.getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
		
		Color hl = Misc.getHighlightColor();
		Color red = Misc.getNegativeHighlightColor();
		
		RouteData route = RouteManager.getInstance().getRoute(EconomyFleetRouteManager.SOURCE_ID, fleet);
		if ((trader || smuggler) && route != null) {
			EconomyRouteData data = (EconomyRouteData) route.getCustom();
			
			Integer id = route.getCurrentSegmentId();
			
			String from = data.from.getName();
			String to = data.to.getName();
			//int index = route.getCurrentIndex();
			if (id >= EconomyFleetRouteManager.ROUTE_DST_LOAD) {
				from = data.to.getName();
				to = data.from.getName();
			}
			String cargo = "";

			if (id <= EconomyFleetRouteManager.ROUTE_DST_UNLOAD) {
				cargo = EconomyRouteData.getCargoList(data.cargoDeliver);
				if (!cargo.isEmpty() && id == EconomyFleetRouteManager.ROUTE_SRC_LOAD) cargo += " (being loaded)";
			} else {
				cargo = EconomyRouteData.getCargoList(data.cargoReturn);
				if (!cargo.isEmpty() && id == EconomyFleetRouteManager.ROUTE_DST_LOAD) cargo += " (being loaded)";
			}
			
			if (trader || smuggler) {
				LabelAPI label = text.addParagraph("You encounter " + fleet.getFaction().getPersonNamePrefixAOrAn() + " " + 
						fleet.getFaction().getPersonNamePrefix() + " "+ fleet.getName().toLowerCase() + ".");
				label.highlightFirst(fleet.getFaction().getPersonNamePrefix());
				label.setHighlightColor(fleet.getFaction().getBaseUIColor());
				
				text.setFontSmallInsignia();
				
				text.addParagraph("--------------------------------------------------------------------------------------------------------------");
				if (data.from.isHidden()) {
					text.addPara("Port of origin: %s", hl, "Unknown");
				} else {
					text.addPara("Port of origin: %s", hl, data.from.getName());
				}
				text.addPara("Current destination: %s", hl, to);
				if (from.equals(data.to.getName())) {
					text.addPara("Returning from: %s", hl, data.to.getName());
				}
				if (cargo.isEmpty()) {
					text.addPara("No cargo");
				} else {
					text.addPara("Cargo: %s", hl, cargo);
					//text.addPara(cargo);
				}
				
				text.addParagraph("--------------------------------------------------------------------------------------------------------------");
				text.setFontInsignia();
				
				int penalty = Math.round(ShippingDisruption.getPenaltyForShippingLost(data.from.getSize(), data.size) * 100f);
				

				if (!fleet.getFaction().isPlayerFaction()) {
					if (data.from.isHidden()) {
						text.addPara("If this fleet does not reach its destination safely, it will cause a shortage " +
								"of the commodities it carries. " +
								"In addition, its loss would reduce the accessibility of its port of origin by %s for up to three months.",
								Misc.getTextColor(), Misc.getHighlightColor(), "" + penalty + "%");
					} else {
						text.addPara("If this fleet does not reach its destination safely, it will cause a shortage " +
								"of the commodities it carries. " +
								"In addition, its loss would reduce the accessbility of " + data.from.getName() + " by %s for up to three months.",
								Misc.getTextColor(), Misc.getHighlightColor(), "" + penalty + "%");
					}
				}
			}
		}
		
		if (!fleet.getFaction().isPlayerFaction()) {
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_REP_IMPACT)) {
				text.addPara("This fleet is operating without official sanction from the faction it nominally belongs to. " +
						"Engaging it in battle will not cause any changes to your reputation.",
						Misc.getHighlightColor(), "will not cause any changes to your reputation");
			} else if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT) && fleet.knowsWhoPlayerIs()) {
				text.addPara("This fleet is either operating in a legal gray area or its behavior " +
						"falls outside accepted norms. Engaging it in battle will not cause immediate hostilities " +
						"with the faction it nominally belongs to, though it will slightly strain the relationship.",
						Misc.getHighlightColor(), "will not cause immediate hostilities");
			}
		}
		
		//text.addParagraph("wfwef ew ew");
		
		return true;
	}

}















