package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;

public class PlayerActivityTracker {

	public static class FleetStatsSnapshot implements Cloneable {
		private float fleetPoints;
		private float cargoOnBoard;
		private float cargoCapacity;
		private float fuelOnBoard;
		private float fuelCapacity;
		private float numShips;
		private float fleetSizeNum;
		
		public void update() {
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			fleetPoints = pf.getFleetPoints();
			cargoOnBoard = pf.getCargo().getSpaceUsed();
			cargoCapacity = pf.getCargo().getMaxCapacity();
			fuelOnBoard = pf.getCargo().getFuel();
			fuelCapacity = pf.getCargo().getMaxFuel();
			numShips = pf.getFleetData().getMembersListCopy().size();
			fleetSizeNum = pf.getFleetSizeCount();
		}
		@Override
		public FleetStatsSnapshot clone() {
			try {
				return (FleetStatsSnapshot) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		
		public float getFleetPoints() {
			return fleetPoints;
		}
		public void setFleetPoints(float fleetPoints) {
			this.fleetPoints = fleetPoints;
		}
		public float getCargoOnBoard() {
			return cargoOnBoard;
		}
		public void setCargoOnBoard(float cargoOnBoard) {
			this.cargoOnBoard = cargoOnBoard;
		}
		public float getCargoCapacity() {
			return cargoCapacity;
		}
		public void setCargoCapacity(float cargoCapacity) {
			this.cargoCapacity = cargoCapacity;
		}
		public float getFuelOnBoard() {
			return fuelOnBoard;
		}
		public void setFuelOnBoard(float fuelOnBoard) {
			this.fuelOnBoard = fuelOnBoard;
		}
		public float getFuelCapacity() {
			return fuelCapacity;
		}
		public void setFuelCapacity(float fuelCapacity) {
			this.fuelCapacity = fuelCapacity;
		}
		public float getNumShips() {
			return numShips;
		}
		public void setNumShips(float numShips) {
			this.numShips = numShips;
		}
		public float getFleetSizeNum() {
			return fleetSizeNum;
		}
		public void setFleetSizeNum(float fleetSizeNum) {
			this.fleetSizeNum = fleetSizeNum;
		}
	}
	
	
	private Map<MarketAPI, Long> lastVisit = new HashMap<MarketAPI, Long>();
	private Map<SubmarketAPI, PlayerTradeDataForSubmarket> submarketTradeData = new LinkedHashMap<SubmarketAPI, PlayerTradeDataForSubmarket>();
	private PlayerTradeProfitabilityData profitabilityData = new PlayerTradeProfitabilityData();
	private ReputationChangeTracker repChangeTracker = new ReputationChangeTracker();
	private FleetStatsSnapshot playerFleetStats = new FleetStatsSnapshot();
	
	protected Object readResolve() {
		if (profitabilityData == null) {
			profitabilityData = new PlayerTradeProfitabilityData();
		}
		if (repChangeTracker == null) {
			repChangeTracker = new ReputationChangeTracker();
		}
		if (playerFleetStats == null) {
			playerFleetStats = new FleetStatsSnapshot();
		}
		return this;
	}
	
	public void advance(float days) {
		//submarketTradeData.clear();
		List<SubmarketAPI> remove = new ArrayList<SubmarketAPI>();
		for (PlayerTradeDataForSubmarket data : submarketTradeData.values()) {
			data.advance(days);
			
			MarketAPI market = Global.getSector().getEconomy().getMarket(data.getMarket().getId());
			if (market == null || market.getSubmarket(data.getSubmarket().getSpecId()) == null ||
					market.getSubmarket(data.getSubmarket().getSpecId()) != data.getSubmarket()) {
				remove.add(data.getSubmarket());
			}
		}
		
		for (SubmarketAPI sub : remove) {
			submarketTradeData.remove(sub);
		}
		
		profitabilityData.advance(days);
		
		repChangeTracker.advance(days);
		
		playerFleetStats.update();
	}
	
	public ReputationChangeTracker getRepChangeTracker() {
		return repChangeTracker;
	}


	public PlayerTradeProfitabilityData getProfitabilityData() {
		return profitabilityData;
	}

	public void updateLastVisit(MarketAPI market) {
		Long timestamp = Global.getSector().getClock().getTimestamp();
		lastVisit.put(market, timestamp);
	}
	
	public float getDaysSinceLastVisitTo(MarketAPI market) {
		Long timestamp = lastVisit.get(market);
		if (timestamp == null) return 10000f;
		return Global.getSector().getClock().getElapsedDaysSince(timestamp);
	}
	
	
	public PlayerTradeDataForSubmarket getPlayerTradeData(SubmarketAPI submarket) {
		PlayerTradeDataForSubmarket data = submarketTradeData.get(submarket);
		if (data == null) {
			data = new PlayerTradeDataForSubmarket(submarket);
			submarketTradeData.put(submarket, data);
		}
		return data;
	}
	

	public FleetStatsSnapshot getPlayerFleetStats() {
		return playerFleetStats;
	}
	
}
