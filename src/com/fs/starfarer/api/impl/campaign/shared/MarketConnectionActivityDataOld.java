package com.fs.starfarer.api.impl.campaign.shared;


public class MarketConnectionActivityDataOld {

//	public static Logger log = Global.getLogger(MarketConnectionActivityData.class);
//	
//	private float econInterval = Global.getSettings().getFloat("economyIntervalnGameDays");
//	private IntervalUtil timer = new IntervalUtil(0.25f, .75f);
//	
//	private RollingAverageTracker tradePointsSent;
//	private RollingAverageTracker tradePointsLost;
//	private RollingAverageTracker smugglingPointsSent;
//	private RollingAverageTracker smugglingPointsLost;
//
//	private final String connId;
//	
//	//private MarketConnectionAPI conn;
//
//	public MarketConnectionActivityData(String connectionId) {
//		this.connId = connectionId;
//		float min = econInterval - Math.min(econInterval * 0.5f, 2f);
//		float max = econInterval + Math.min(econInterval * 0.5f, 2f);
//		tradePointsSent = new RollingAverageTracker(min, max, StarSystemActivityTracker.ROLLING_AVG_FACTOR);
//		tradePointsLost = new RollingAverageTracker(min, max, StarSystemActivityTracker.ROLLING_AVG_FACTOR);
//		smugglingPointsSent = new RollingAverageTracker(min, max, StarSystemActivityTracker.ROLLING_AVG_FACTOR);
//		smugglingPointsLost = new RollingAverageTracker(min, max, StarSystemActivityTracker.ROLLING_AVG_FACTOR);
//		
//		timer = new IntervalUtil(min, max);
//	}
//	
//	public void advance(float days) {
//		timer.advance(days);
//		
//		float e = timer.getElapsed() / timer.getIntervalDuration();
//		tradePointsSent.setElaspedFractionOverride(e);
//		tradePointsLost.setElaspedFractionOverride(e);
//		smugglingPointsSent.setElaspedFractionOverride(e);
//		smugglingPointsLost.setElaspedFractionOverride(e);
//		
//		if (timer.intervalElapsed()) {
//			tradePointsSent.updateAverage();
//			tradePointsLost.updateAverage();
//			smugglingPointsSent.updateAverage();
//			smugglingPointsLost.updateAverage();
//			
//			log.info(String.format("Updating connection: [" + connId + "]: trade (s: %d, l: %d), smuggling: (s: %d, l: %d)",
//					(int) tradePointsSent.getAverage(), (int) tradePointsLost.getAverage(),
//					(int) smugglingPointsSent.getAverage(), (int) smugglingPointsLost.getAverage()));
//		}
//		
//	}
//
//
//	public RollingAverageTracker getTradePointsSent() {
//		return tradePointsSent;
//	}
//
//
//	public RollingAverageTracker getTradePointsLost() {
//		return tradePointsLost;
//	}
//
//
//	public RollingAverageTracker getSmugglingPointsSent() {
//		return smugglingPointsSent;
//	}
//
//
//	public RollingAverageTracker getSmugglingPointsLost() {
//		return smugglingPointsLost;
//	}
//
//	public MarketConnectionAPI getConnection() {
//		return Global.getSector().getEconomy().getConnection(connId);
//	}
//	
////	public void reportTradeFleetLost(TradeConnectionData connection, 
////			 CampaignFleetAPI fleet,
////			 FleetDespawnReason reason, 
////			 Object param) {
////
////	}
////
////	public void reportTradeFleetAttacked(TradeConnectionData connection,
////					 CampaignFleetAPI fleet,
////					 CampaignFleetAPI enemy,
////					 boolean enemyWon) {
////	
	
	
}










