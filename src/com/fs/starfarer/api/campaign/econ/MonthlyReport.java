package com.fs.starfarer.api.campaign.econ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.MonthlyReportNodeTooltipCreator;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class MonthlyReport {

	public static String CREW = "node_id_crew";
	public static String MARINES = "node_id_marines";
	public static String FLEET = "node_id_fleet";
	public static String OUTPOSTS = "node_id_outposts";
	public static String PRODUCTION = "node_id_prod";
	public static String PRODUCTION_WEAPONS = "node_id_prod_weapons";
	//public static String SURPLUS = "node_id_surplus";
	public static String OVERHEAD = "node_id_overhead";
	public static String STOCKPILING = "node_id_stockpiling";
	public static String RESTOCKING = "node_id_restocking";
	
	public static String INCENTIVES = "node_id_incentives";
	public static String INDUSTRIES = "node_id_industries";
	public static String EXPORTS = "node_id_exports";
	public static String STORAGE = "node_id_storage";
	public static String ADMIN = "node_id_admin";
	public static String STORAGE_CARGO = "node_id_storage_cargo";
	public static String STORAGE_SHIPS = "node_id_storage_ships";
	
	public static String LAST_MONTH_DEBT = "node_id_last_month_debt";
	//public static String IMPORTS = "node_id_imports";
	
	public static String OFFICERS = "node_id_officers";
	
	/**
	 * Financial data node for a monthly income/expenses report.
	 * 
	 * @author Alex Mosolov
	 *
	 * Copyright 2017 Fractal Softworks, LLC
	 */
	public static class FDNode {
		protected LinkedHashMap<String, FDNode> children;
		public FDNode parent;
		//public String sortString;
		public String name;
		public String icon;
		
		public float income;
		public float upkeep;
		public float totalIncome;
		public float totalUpkeep;
		
		public Object custom;
		public Object custom2;
		public SectorEntityToken mapEntity;
		
		public TooltipCreator tooltipCreator = null;
		public Object tooltipParam;

		public LinkedHashMap<String, FDNode> getChildren() {
			if (children == null) return new LinkedHashMap<String, FDNode>();
			return children;
		}
		
		public int getDepth() {
			FDNode curr = this;
			int count = 0;
			while (curr.parent != null) {
				curr = curr.parent;
				count++;
			}
			return count;
		}
	}
	
	private FDNode root = new FDNode();
	private long timestamp;
	private int debt = 0;
	private int previousDebt = 0;
	
	private MonthlyReportNodeTooltipCreator monthlyReportTooltip;
	public MonthlyReportNodeTooltipCreator getMonthlyReportTooltip() {
		if (monthlyReportTooltip == null) {
			monthlyReportTooltip = new MonthlyReportNodeTooltipCreator();
		}
		return monthlyReportTooltip;
	}
	
	public void computeTotals() {
		computeTotals(root);
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getPreviousDebt() {
		return previousDebt;
	}

	public void setPreviousDebt(int previousDebt) {
		this.previousDebt = previousDebt;
	}

	public int getDebt() {
		return debt;
	}

	public void setDebt(int debt) {
		this.debt = debt;
	}

	public FDNode getRoot() {
		return root;
	}

	protected void computeTotals(FDNode curr) {
		curr.totalIncome = curr.income;
		curr.totalUpkeep = curr.upkeep;
		
		for (FDNode child : curr.getChildren().values()) {
			computeTotals(child);
		}
		
		if (curr.parent != null) {
			curr.parent.totalIncome += curr.totalIncome;
			curr.parent.totalUpkeep += curr.totalUpkeep;
		}
	}
	
	public List<FDNode> getAllNodes() {
		List<FDNode> all = new ArrayList<FDNode>();
		getAllNodes(root, all);
		return all;
	}
	
	protected void getAllNodes(FDNode curr, List<FDNode> nodes) {
		nodes.add(curr);
		
		for (FDNode child : curr.getChildren().values()) {
			getAllNodes(child, nodes);
		}
	}
	
	public FDNode getNode(String ... path) {
		return getNode(root, new LinkedList<String>(Arrays.asList(path)));
	}
	
	public FDNode getNode(FDNode from, String ... path) {
		return getNode(from, new LinkedList<String>(Arrays.asList(path)));
	}
	public FDNode getNode(FDNode from, List<String> path) {
		if (path.isEmpty()) return from;
		
		String nextId = path.remove(0);
		FDNode next = from.children == null ? null : from.children.get(nextId);
		if (next == null) { 
			if (from.children == null) {
				from.children = new LinkedHashMap<String, FDNode>();
			}
			next = new FDNode();
			next.parent = from;
			from.children.put(nextId, next);
		}
		return getNode(next, path);
	}

	
	public FDNode getColoniesNode() {
		FDNode marketsNode = getNode(MonthlyReport.OUTPOSTS);
		if (marketsNode.name == null) {
			marketsNode.name = "Colonies";
			marketsNode.custom = MonthlyReport.OUTPOSTS;
			marketsNode.tooltipCreator = getMonthlyReportTooltip();
		}
		return marketsNode;
	}
	
	public FDNode getMarketNode(MarketAPI market) {
		FDNode marketsNode = getColoniesNode();
		
		FDNode mNode = getNode(marketsNode, market.getId());
		if (mNode.name == null) {
			mNode.name = market.getName() + " (" + market.getSize() + ")";
			mNode.custom = market;
		}
		return mNode;
	}
	
	public FDNode getCounterShortageNode(MarketAPI market) {
		FDNode mNode = getMarketNode(market);
		
		FDNode sNode = getNode(mNode, MonthlyReport.STOCKPILING);
		if (sNode.name == null) {
			sNode.name = "Stockpiles used to counter shortages";
			sNode.custom = MonthlyReport.STOCKPILING;
			sNode.custom2 = Global.getFactory().createCargo(true);
			((CargoAPI)sNode.custom2).initMothballedShips(Factions.PLAYER);
			sNode.tooltipCreator = getMonthlyReportTooltip();
		}
		return sNode;
	}
	
	public FDNode getRestockingNode(MarketAPI market) {
		//FDNode mNode = getMarketNode(market);
		
		FDNode mNode = getColoniesNode();
		
		FDNode sNode = getNode(mNode, MonthlyReport.RESTOCKING);
		if (sNode.name == null) {
			sNode.name = "Stockpiles drawn by your fleet";
			sNode.custom = MonthlyReport.RESTOCKING;
			sNode.custom2 = Global.getFactory().createCargo(true);
			((CargoAPI)sNode.custom2).initMothballedShips(Factions.PLAYER);
			sNode.tooltipCreator = getMonthlyReportTooltip();
		}
		return sNode;
	}
	
	public FDNode getDebtNode() {
		FDNode debtNode = getNode(MonthlyReport.LAST_MONTH_DEBT);
		if (debtNode.name == null) {
			debtNode.name = "Last month's debt";
			debtNode.custom = MonthlyReport.LAST_MONTH_DEBT;
			debtNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
			debtNode.tooltipCreator = getMonthlyReportTooltip();
		}
		return debtNode;
	}
	
	public static void main(String[] args) {
		MonthlyReport data = new MonthlyReport();
		
		data.getNode("test", "test1", "test2").income = 10;
		
		System.out.println(data);
	}
}





