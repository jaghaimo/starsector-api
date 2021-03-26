package com.fs.starfarer.api.impl.campaign.plog;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction.ShipSaleInfo;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.plugins.SurveyPlugin;

public class PlaythroughLog implements EconomyTickListener,
									   ColonyInteractionListener,
									   //EconomyUpdateListener,
									   PlayerColonizationListener,
									   ColonyPlayerHostileActListener {

	public static final String KEY = "$core_playthroughLog";
	public static PlaythroughLog getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test == null) {
			test = new PlaythroughLog();
			Global.getSector().getMemoryWithoutUpdate().set(KEY, test);
			Global.getSector().getListenerManager().addListener(test);
		}
		return (PlaythroughLog) test; 
	}
	
	public static class PLIntelUIData {
		public LinkedHashSet<String> selectedGraphs = new LinkedHashSet<String>();
	}
	
	protected List<PLEntry> entries = new ArrayList<PLEntry>();
	protected Map<String, PLStat> stats = new LinkedHashMap<String, PLStat>();
	protected PLIntelUIData uiData = new PLIntelUIData();

	transient protected List<PLSnapshot> data = new ArrayList<PLSnapshot>();
	protected String saved = "";
	
	public PlaythroughLog() {
		//Global.getSector().getEconomy().addUpdateListener(this);
		initStats();
	}
	
//	// called from the UI when the player visits a colony etc
//	// take some samples here so that we have a better chance of catching changes in credits/fleet size/etc.
	public void reportPlayerClosedMarket(MarketAPI market) {
		reportEconomyTick(-1);
	}

	public void reportPlayerOpenedMarket(MarketAPI market) {
		reportEconomyTick(-1);
	}

	public void reportEconomyTick(int iterIndex) {
		if (Global.getSector().isInNewGameAdvance() || Global.getSector().getPlayerFleet() == null) return;
		
//		if (data.isEmpty()) {
//			PLSnapshot snapshot = new PLSnapshot();
//			data.add(snapshot);
//		}
		
		for (String key : stats.keySet()) {
			PLStat stat = stats.get(key);
			stat.accrueValue();
		}
	}
	
	public void reportEconomyMonthEnd() {
		if (Global.getSector().isInNewGameAdvance() || Global.getSector().getPlayerFleet() == null) return;
		
		takeSnapshot(false);
	}
	
	protected HullSize biggestBought = HullSize.FIGHTER;
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		if (biggestBought == null) biggestBought = HullSize.FIGHTER;
		for (ShipSaleInfo info : transaction.getShipsBought()) {
			HullSize size = info.getMember().getHullSpec().getHullSize();
			if (size.ordinal() > biggestBought.ordinal()) {
				biggestBought = size;
				addEntry("Bought " + info.getMember().getVariant().getHullSpec().getNameWithDesignationWithDashClass());
			}
		}
	}
	
	public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		addEntry("Saturation-bombarded " + market.getName() + " (" + 
				"size " + (market.getSize() + 1) + " " + market.getFaction().getEntityNamePrefix() + " colony)");
	}
	
	public void reportPlayerAbandonedColony(MarketAPI colony) {
		String extra = "";
		if (colony.getPlanetEntity() != null) { 
			SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
			String cid = plugin.getSurveyDataType(colony.getPlanetEntity());
			CommoditySpecAPI pClass = Global.getSettings().getCommoditySpec(cid);
			extra = " (" +  pClass.getName() + " " + colony.getPlanetEntity().getTypeNameWithLowerCaseWorld() + ")";
		}
		addEntry("Abdandoned size " + colony.getSize() + " colony " + colony.getOnOrAt() + " " + colony.getName() + extra);
	}

	public void reportPlayerColonizedPlanet(PlanetAPI planet) {
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		String cid = plugin.getSurveyDataType(planet);
		CommoditySpecAPI pClass = Global.getSettings().getCommoditySpec(cid);
		addEntry("Established colony on " + planet.getName() + " (" + 
				 pClass.getName().replaceAll(" Survey Data", "") + " " + planet.getTypeNameWithLowerCaseWorld() + ")");
	}
	
	
	public void takeSnapshot(boolean debug) {
		PLSnapshot snapshot = new PLSnapshot();
		
		for (String key : stats.keySet()) {
			PLStat stat = stats.get(key);
			long value = stat.getValueForAllAccrued();
			
			if (debug) {
				value += (int)((float) Math.random() * 500);
				value -= (int)((float) Math.random() * 500);
				if (value < 0) value = 0;
			}
			
			snapshot.getData().put(key, value);
		}
		
		// have to add it here otherwise getPrevValue() uses this snapshot not the actual previous one
		data.add(snapshot);
	}

	
	
	protected Object readResolve() throws DataFormatException, UnsupportedEncodingException {
		if (stats == null) {
			stats = new LinkedHashMap<String, PLStat>();
			initStats();
		}
		if (data == null) {
			data = new ArrayList<PLSnapshot>();
		}
		
		
		byte [] input = BaseTiledTerrain.toByteArray(saved);
		
		Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		
		StringBuilder result = new StringBuilder(); 
		byte [] temp = new byte[100];
		while (!decompressor.finished()) {
			int read = decompressor.inflate(temp);
			// this should be OK since the data is base64 encoded so should be ascii not utf8 i.e. no multi-byte chars
			result.append(new String(temp, 0, read, "UTF-8"));
		}

		decompressor.end();
		
		saved = result.toString();
		
		data.clear();
		if (saved == null) saved = "";
		String [] parts = saved.split("\n");
		for (String p : parts) {
			if (p.isEmpty()) continue;
			PLSnapshot next = new PLSnapshot(p);
			data.add(next);
		}
		return this;
	}
	
	protected Object writeReplace() throws UnsupportedEncodingException {
		saved = "";
		for (PLSnapshot s : data) {
			saved += s.getString() + "\n";
		}
		if (!saved.isEmpty()) {
			saved = saved.substring(0, saved.length() - 1);
		}

		Deflater compressor = new Deflater();
		compressor.setInput(saved.getBytes("UTF-8"));
		compressor.finish();

		StringBuilder result = new StringBuilder();
		byte [] temp = new byte[100];
		while (!compressor.finished()) {
			int read = compressor.deflate(temp);
			result.append(BaseTiledTerrain.toHexString(Arrays.copyOf(temp, read)));
		}
		compressor.end();
		
		saved = result.toString();
		
		return this;
	}
	
	
	protected void initStats() {
		addStat(new PLStatLevel());
		addStat(new PLStatFleet());
		addStat(new PLStatCredits());
		addStat(new PLStatSupplies());
		addStat(new PLStatFuel());
		addStat(new PLStatCargo());
		addStat(new PLStatCrew());
		addStat(new PLStatMarines());
		addStat(new PLStatColonies());
	}
	
	public CampaignClockAPI getDateForIndex(int index) {
		if (index < 0 || index >= data.size()) {
			return Global.getSector().getClock();
		}
		PLSnapshot s = data.get(index);
		CampaignClockAPI clock = Global.getSector().getClock().createClock(s.getTimestamp());
		return clock;
	}
	
	public void addStat(PLStat stat) {
		stats.put(stat.getId(), stat);
	}

	public Map<String, PLStat> getStats() {
		return stats;
	}
	
	public long getPrevValue(String key) {
		if (data.isEmpty()) return 0;
		Long val = data.get(data.size() - 1).getData().get(key);
		if (val == null) return 0;
		return val;
	}
	
	public List<PLSnapshot> getData() {
		return data;
	}

	public List<PLEntry> getEntries() {
		return entries;
	}
	
	public void addEntry(PLEntry entry) {
		entries.add(entry);
	}
	public void addEntry(String text) {
		entries.add(new PLTextEntry(text));
	}
	public void addEntry(String text, boolean story) {
		entries.add(new PLTextEntry(text, story));
	}

	public PLIntelUIData getUIData() {
		if (uiData == null) {
			uiData = new PLIntelUIData();
		}
		return uiData;
	}

	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, CargoAPI cargo) {
	}

	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
	}

	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
	}

	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
	}
}


