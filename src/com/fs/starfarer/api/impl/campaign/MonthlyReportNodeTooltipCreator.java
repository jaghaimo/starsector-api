package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class MonthlyReportNodeTooltipCreator implements TooltipCreator {

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		FDNode node = (FDNode) tooltipParam;

		float pad = 3f;
		float opad = 10f;
		
		//tooltip.addTitle(node.name);
		
		int crewSalary = Global.getSettings().getInt("crewSalary");
		int marineSalary = Global.getSettings().getInt("marineSalary");
		int officerBase = Global.getSettings().getInt("officerSalaryBase");
		int officerPerLevel = Global.getSettings().getInt("officerSalaryPerLevel");
		float storageFreeFraction = Global.getSettings().getFloat("storageFreeFraction");
		
		Color h = Misc.getHighlightColor();
		
		FactionAPI faction = Global.getSector().getPlayerFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		Color grid = faction.getGridUIColor();
		Color bright = faction.getBrightUIColor();
		
		if (MonthlyReport.FLEET.equals(node.custom)) {
			tooltip.addPara("Fleet-related expenses.", 0);
		} else if (MonthlyReport.OUTPOSTS.equals(node.custom)) {
			tooltip.addPara("Colony-related income and expenses.", 0);
		} else if (MonthlyReport.PRODUCTION_WEAPONS.equals(node.custom)) {
			tooltip.addPara("Weapons and fighter LPCs installed on produced ships.", 0);
		} else if (MonthlyReport.PRODUCTION.equals(node.custom)) {
			float currPad = 0f;
			if (node.custom2 instanceof CargoAPI) {
				CargoAPI cargo = (CargoAPI) node.custom2;
				if (!cargo.isEmpty()) {
					tooltip.addSectionHeading("Equipment", color, dark, Alignment.MID, currPad);
					tooltip.showCargo(cargo, 10, true, opad);
					currPad = opad;
				}
				List<FleetMemberAPI> ships = new ArrayList<FleetMemberAPI>();
				ships.addAll(cargo.getMothballedShips().getMembersListCopy());
				if (!ships.isEmpty()) {
					tooltip.addSectionHeading("Ships", color, dark, Alignment.MID, currPad);
					tooltip.showShips(ships, 10, true, opad);
				}
			}
			
			FactionAPI pf = Global.getSector().getPlayerFaction();
			FactionProductionAPI prod = pf.getProduction();
			int accrued = prod.getAccruedProduction();
			if (accrued > 0) {
				tooltip.addPara("A total of %s worth of production effort has been put into projects that have not yet been " +
						"completed.", currPad, Misc.getHighlightColor(), "" + Misc.getDGSCredits(accrued));
			}
			
		} else if (MonthlyReport.STOCKPILING.equals(node.custom)) {
			if (node.custom2 instanceof CargoAPI) {
				tooltip.addPara("Expenses incurred due to the use of local stockpiles to counter shortages.", 0);
				CargoAPI cargo = (CargoAPI) node.custom2;
				if (!cargo.isEmpty()) {
					tooltip.addSectionHeading("Resources", color, dark, Alignment.MID, opad);
					tooltip.showCargo(cargo, 10, true, opad);
				}
				List<FleetMemberAPI> ships = new ArrayList<FleetMemberAPI>();
				ships.addAll(cargo.getMothballedShips().getMembersListCopy());
				if (!ships.isEmpty()) {
					tooltip.addSectionHeading("Ships", color, dark, Alignment.MID, opad);
					tooltip.showShips(ships, 10, true, opad);
				}
			}
		} else if (MonthlyReport.RESTOCKING.equals(node.custom)) {
			if (node.custom2 instanceof CargoAPI) {
				tooltip.addPara("Expenses incurred due to the need to restock local stockpiles to replace the resources drawn by your fleet.", 0);
				CargoAPI cargo = (CargoAPI) node.custom2;
				if (!cargo.isEmpty()) {
					tooltip.addSectionHeading("Resources", color, dark, Alignment.MID, opad);
					tooltip.showCargo(cargo, 10, true, opad);
				}
				List<FleetMemberAPI> ships = new ArrayList<FleetMemberAPI>();
				ships.addAll(cargo.getMothballedShips().getMembersListCopy());
				if (!ships.isEmpty()) {
					tooltip.addSectionHeading("Ships", color, dark, Alignment.MID, opad);
					tooltip.showShips(ships, 10, true, opad);
				}
			}
		} else if (MonthlyReport.OFFICERS.equals(node.custom)) {
			tooltip.addPara(
					"Each officer receives a base salary of %s credits per month, plus %s credits per officer level.", 0,
					h, Misc.getWithDGS(officerBase), Misc.getWithDGS(officerPerLevel));
		} else if (MonthlyReport.ADMIN.equals(node.custom)) {
			float f = Global.getSettings().getFloat("idleAdminSalaryMult");
			tooltip.addPara(
					"Each administrator receives a salary that depends on their skills. " +
					"When not assigned to govern a colony, their salary is reduced to %s.", 0, 
					Misc.getHighlightColor(), "" + (int)Math.round(f * 100f) + "%");
		} else if (MonthlyReport.CREW.equals(node.custom)) {
			tooltip.addPara("Each crew member receives a monthly salary of %s credits.", 0,
					h, Misc.getWithDGS(crewSalary));
		} else if (MonthlyReport.MARINES.equals(node.custom)) {
			tooltip.addPara("Each marine receives a monthly salary of %s credits.", 0,
					h, Misc.getWithDGS(marineSalary));
		} else if (MonthlyReport.LAST_MONTH_DEBT.equals(node.custom)) {
			tooltip.addPara("Unpaid debt carried over from last month.", 0);
		} else if (MonthlyReport.INDUSTRIES.equals(node.custom)) {
			tooltip.addPara("Upkeep and income from industries and structures located at the outpost or colony.", 0);
		} else if (MonthlyReport.INCENTIVES.equals(node.custom)) {
			tooltip.addPara("Total spent on hazard pay and related growth incentives at the colony during the previous month.", 0);
		} else if (MonthlyReport.EXPORTS.equals(node.custom)) {
			tooltip.addPara("Income from out-of-faction exports by this outpost or colony. " +
					"Smuggling and in-faction exports do not produce income.", 0);
		} else if (MonthlyReport.STORAGE.equals(node.custom)) {
//			tooltip.addPara("Fees and expenses incurred by storing crew or materiel at a location. These include " +
//					"things like rent (for populated planets) and life support/maintenance/etc for storage at an " +
//					"otherwise-abandoned location.", 0);
			tooltip.addPara("Fees and expenses incurred by storing crew or materiel at a location. Includes rent, security, and other such.", 0);
			
			String percent = "" + (int) (storageFreeFraction * 100f) + "%";
			tooltip.addPara("The monthly expenses are generally %s of the base value of what's in storage.", 10f, h,
							percent);
			
			tooltip.addPara("Storage at a colony under your control does not incur any fees.", 10f);
		} else if (node.custom instanceof CommodityOnMarketAPI) {
			float quantity = 0;
			if (node.custom2 instanceof Float) {
				quantity = (Float) node.custom2;
				if (quantity < 1) quantity = 1;
			}
			String units = "units";
			if (quantity <= 1) units = "unit";
			CommodityOnMarketAPI com = (CommodityOnMarketAPI) node.custom;
			tooltip.addPara("Approximately %s " + units + " of " + com.getCommodity().getName() + ".", 0f,
					h, Misc.getWithDGS(quantity));
		}
//		else if (MonthlyReport.STORAGE_CARGO.equals(node.custom)) {
//			MarketAPI market = (MarketAPI) node.custom2;
//			//FactionAPI faction = market.getFaction();
//			Misc.addStorageInfo(tooltip, color, dark, market, false, true);
//		} else if (MonthlyReport.STORAGE_SHIPS.equals(node.custom)) {
//			
//		}
		
	}

	
	
	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

}
