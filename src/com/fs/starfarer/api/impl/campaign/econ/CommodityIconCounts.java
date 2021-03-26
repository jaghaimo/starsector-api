package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;

public class CommodityIconCounts {

	protected CommodityOnMarketAPI com;
	
	public int available;
	public int production;
	public int demand;
	public int nonDemandExport;
	public int deficit;
	public int imports;
	public int demandMetWithLocal;
	public int demandMet;
	public int globalExport;
	public int inFactionOnlyExport;
	public int canNotExport;
	public int extra;

	public CommodityIconCounts(CommodityOnMarketAPI com) {
		this.com = com;
		
//		int shippingGlobal = CommodityMarketData.getShippingCapacity(com.getMarket(), false);
//		int shippingInFaction = CommodityMarketData.getShippingCapacity(com.getMarket(), true);
		int shippingGlobal = Global.getSettings().getShippingCapacity(com.getMarket(), false);
		int shippingInFaction = Global.getSettings().getShippingCapacity(com.getMarket(), true);
		
//		if (com.getId().equals("food") && com.getMarket().getId().equals("jangala")) {
//			System.out.println("efwefwef");
//		}
		
		available = com.getAvailable();
		production = com.getMaxSupply();
//		if (com.getCommodity().isPrimary()) {
//			List<CommodityOnMarket> withSameDemandClass = ((Market)com.getMarket()).getCommoditiesWithClass(com.getDemandClass());
//			for (CommodityOnMarket other : withSameDemandClass) {
//				if (com == other) continue;
//				production += other.getMaxSupply();
//			}
//		}
		production = Math.min(production, available);
			
		int export = 0;
		demand = com.getMaxDemand();
		export = (int) Math.min(production, shippingGlobal);
		
		extra = available - Math.max(export, demand);
		if (extra < 0) extra = 0;
		
		deficit = demand - available;
		demandMet = Math.min(available, demand);
		
		demandMetWithLocal = Math.min(available, production) - extra;
		//imports = available - production - extra;
		imports = available - production;
		
		
		nonDemandExport = 0;
		if (demandMetWithLocal > demand && demand > 0) {
			nonDemandExport = demandMetWithLocal - demand;
			demandMetWithLocal = demand;
		}
		
		globalExport = production;
		inFactionOnlyExport = 0;
		canNotExport = 0;
		

//		if (com.getMarket().getName().equals("Stral") && com.getId().equals(Commodities.SUPPLIES)) {
//			System.out.println("wefwefe");
//		}
		
		if (globalExport > shippingGlobal) {
			inFactionOnlyExport = globalExport - shippingGlobal;
			globalExport = shippingGlobal;
		}
		if (globalExport + inFactionOnlyExport > shippingInFaction) {
			canNotExport = globalExport + inFactionOnlyExport - shippingInFaction;
			inFactionOnlyExport -= canNotExport;
		}
		
		int aboveMax = Math.max(demandMetWithLocal, globalExport) + canNotExport + inFactionOnlyExport - (available - imports);
		if (aboveMax > 0) {
			inFactionOnlyExport -= aboveMax;
			if (inFactionOnlyExport < 0) {
				canNotExport += inFactionOnlyExport;
			}
		}
		
		
		if (inFactionOnlyExport < 0) inFactionOnlyExport = 0;
		if (canNotExport < 0) canNotExport = 0;
		if (nonDemandExport < 0) nonDemandExport = 0;
		if (imports < 0) imports = 0;
		if (deficit < 0) deficit = 0;
		if (demandMetWithLocal < 0) demandMetWithLocal = 0;
	}
	
}








