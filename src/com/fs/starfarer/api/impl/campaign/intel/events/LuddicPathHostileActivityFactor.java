package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.listeners.PatherCellListener;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LuddicPathHostileActivityFactor extends BaseHostileActivityFactor implements PatherCellListener, FGIEventListener {

	public static String DEFEATED_PATHER_EXPEDITION = "$defeatedPatherExpedition";
	
	public static String ATTACK_KEY = "$PatherAttack_ref";

	public static boolean isPlayerDefeatedPatherExpedition() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_PATHER_EXPEDITION);
	}
	public static void setPlayerDefeatedPatherExpedition() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_PATHER_EXPEDITION, true);
	}
	
	
	public static class HAPatherCellsEventData {
		public LuddicPathCellsIntel cells;
		public MarketAPI market;
		public float interest;
		public HAPatherCellsEventData(LuddicPathCellsIntel cells, MarketAPI market) {
			this.cells = cells;
			this.market = market;
			interest = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
		}
	}
	
	public LuddicPathHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Luddic Path";
	}
	
	public String getNameForThreatList(boolean first) {
		if (first) return "Luddic Path";
		return "Luddic Path";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
	}
	
	@Override
	public Color getNameColorForThreatList() {
		return Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				
				tooltip.addPara("Advanced technology and artificial intelligence are anathema to the Luddic Path.", 0f);
				tooltip.addPara("Most of the Pather fleets are small, engaging in reconnaissance and demanding tithes "
						+ "from unwary travellers, but occasionaly a larger raiding force will make an appearance.", opad);
				
				addAgreementStatus(tooltip, opad);
			}
		};
	}
	
	public static void addAgreementStatus(TooltipMakerAPI tooltip, float initPad) {
		float opad = 10f;
		Color p = Misc.getPositiveHighlightColor();
		Color h = Misc.getHighlightColor();
		if (HA_CMD.playerHasPatherAgreement()) {
			if (!HA_CMD.playerPatherAgreementIsPermanent()) {
				float days = HA_CMD.getPlayerPatherAgreementDays();
				if (days < 1 && days > 0) days = 1;
				days = Math.round(days);
				String dStr = "days";
				if ((int)days == 1) dStr = "day";
				tooltip.addPara("You've %s a signficant amount to the Pathers, and their fleets and "
						+ "ground-based cells should leave your colonies alone for another %s " + dStr + ".",
						initPad, new Color[] {p, h}, "tithed", "" + (int)days);
			} else {
				tooltip.addPara("You've reached an understanding with the Pathers, and their fleets and "
						+ "ground-based cells should leave your colonies alone in the future, "
						+ "barring unexpected events.",
						initPad, p, "understanding");
			}
		} else {
			tooltip.addPara("It's possible that you might reach some kind of understanding with the Pathers, "
					+ "provided you find the right people to talk to.", initPad,
					h, "find the right people");
			
//			LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(market);
//			tooltip.addPara("If your colony has active Pather cells, destroying the base that's supplying them "
//					+ "will also reduce the level of Pather fleet actvity.", opad, h,
//					"destroying the base");
					
		}
	}

	public boolean shouldShow(BaseEventIntel intel) {
		//return getProgress(intel) > 0 || HA_CMD.playerHasPatherAgreement();
		return getProgress(intel) > 0 || (HA_CMD.playerHasPatherAgreement() && !HA_CMD.playerPatherAgreementIsPermanent());
	}



	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("luddicPathMaxFleets");
	}

	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
		
		float f = 0f;
		f += getEffectMagnitude(system);
		
		if (f > 1f) f = 1f;
		
		float p = Global.getSettings().getFloat("luddicPathSmallFleetProb");
		boolean small = random.nextFloat() < p;
		
		int difficulty = 0;
		
		if (small) {
			difficulty = 1 + random.nextInt(2);
		} else {
			difficulty = 3;
			difficulty += (int) Math.round(f * 5f);
			difficulty += random.nextInt(6);
		}
		
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.LUDDIC_PATH;
		
		if (small) {
			m.createStandardFleet(difficulty, factionId, loc);
		} else {
			m.createStandardFleet(difficulty, factionId, loc);
		}
		
		m.triggerSetPirateFleet();
		m.triggerMakeLowRepImpact();

		if (!small) {
			//m.triggerFleetPatherNoDefaultTithe();
			m.triggerFleetAllowLongPursuit();
		}
		
		CampaignFleetAPI fleet = m.createFleet();
		
		return fleet;
	}
	

	@Override
	public void notifyFactorRemoved() {
		Global.getSector().getListenerManager().removeListener(this);
	}

	public void notifyEventEnding() {
		notifyFactorRemoved();
	}
	
	public static HAPatherCellsEventData getPatherCellData(EventStageData stage) {
		if (stage == null) return null;
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			if (data.custom instanceof HAPatherCellsEventData) {
				HAPatherCellsEventData attackData = (HAPatherCellsEventData) data.custom;
				return attackData;
			}
		}
		return null;
	}
	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {

		HAPatherCellsEventData data = getPatherCellData(stage);
		Color c = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
		
		if (data == null) return;
		
		LabelAPI label = info.addPara("Signs of a Luddic Path attack targeting %s",
										initPad, tc, tc, data.market.getName());
		label.setHighlight("Luddic Path", data.market.getName());
		label.setHighlightColors(c, Misc.getBasePlayerColor());
	}

	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Luddic Path attack averted", tc, initPad);
	}

	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		HAPatherCellsEventData data = getPatherCellData(stage);
		if (data == null) return;
		
		Color c = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
		
		float small = 0f;
		float opad = 10f;
		
		small = 8f;
		
		LabelAPI label = info.addPara("There are signs of an impending Luddic Path attack targeting %s."
				+ " If the attack is successful, the colony will suffer a catastrophic saturation bombardment.",
								small, c, data.market.getName());
		label.setHighlight(data.market.getName(), "catastrophic saturation bombardment");
		label.setHighlightColors(Misc.getBasePlayerColor(), Misc.getNegativeHighlightColor());
		
		
		info.addPara("This attack represents a significant resource investment by the Pathers. "
				+ "If it is defeated, Luddic Path cells Sector-wide will be disrupted.", 
				opad, Misc.getPositiveHighlightColor(), "disrupted");
		
		
		stage.beginResetReqList(info, true, "crisis", opad);
		label = info.addPara("The Luddic Path cells on %s are disrupted", 
				0f, Misc.getBasePlayerColor(), data.market.getName());
		label.setHighlight("Luddic Path", data.market.getName());
		label.setHighlightColors(c, Misc.getBasePlayerColor());
		
		info.addPara("An agreement is reached with the Luddic Path", 
				0f, Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor(), "Luddic Path");
		stage.endResetReqList(info, false, "crisis", -1, -1); 
		
		addBorder(info, Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.LUDDIC_PATH).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Luddic Path attack", intel, stage);
//			return new BaseFactorTooltip() {
//				@Override
//				public void createTooltip(TooltipMakerAPI info, boolean expanded, Object tooltipParam) {
//					float opad = 10f;
//					info.addTitle("Luddic Path attack");
//					HAPatherCellsEventData data = getPatherCellData(stage);
//					if (data == null) return;
//					
//					info.addPara("A Luddic Path task force will target %s and attempt an obital bombardment.",
//							opad, Misc.getBasePlayerColor(), data.market.getName());
//					
//					stage.beginResetReqList(info, true, "crisis", opad);
//					Color c = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
//					LabelAPI label = info.addPara("The Luddic Path cells on %s are disrupted", 
//							0f, Misc.getBasePlayerColor(), data.market.getName());
//					label.setHighlight("Luddic Path", data.market.getName());
//					label.setHighlightColors(c, Misc.getBasePlayerColor());
//					stage.endResetReqList(info, true, "crisis", 
//							HostileActivityEventIntel.RESET_MIN, HostileActivityEventIntel.RESET_MAX);
//				}
//			};
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (HA_CMD.playerHasPatherAgreement()) return 0f;
		
		if (stage.id == Stage.HA_EVENT) {
			if (pickTargetMarket() != null) {
				return 10f;
			}
		}
		return 0;
	}
	
	public MarketAPI pickTargetMarket() {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom());
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			// to put a damper on shenanigans with establishing and abandoning a colony 
			// with an Alpha Core admin to bait an attack
			if (market.getDaysInExistence() < 180f && !Global.getSettings().isDevMode()) continue;
			
			LuddicPathCellsIntel cells = LuddicPathCellsIntel.getCellsForMarket(market);
			if (cells == null || cells.isSleeper()) continue;
			
			float w = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
			picker.add(market, w * w);
		}
		return picker.pick();
	}

	
//	public void resetEvent(HostileActivityEventIntel intel, EventStageData stage) {
//		super.resetEvent(intel, stage);
////		HAERandomEventData data = (HAERandomEventData) stage.rollData;
////		intel.sendUpdateIfPlayerHasIntel(data, false);
////		stage.rollData = null;
//	}
	
	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
//		if (true) return;
		
		MarketAPI market = pickTargetMarket();
		LuddicPathCellsIntel cells = LuddicPathCellsIntel.getCellsForMarket(market);
		if (market == null || cells == null) return;
		
		HAERandomEventData data = new HAERandomEventData(this, stage);
		data.custom = new HAPatherCellsEventData(cells, market);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		//if (true) return false;
		
		HAPatherCellsEventData data = getPatherCellData(stage);
		if (data == null || data.market == null || data.cells == null || data.cells.isSleeper()) {
			return false;
		}
		
		if (!data.market.isInEconomy()) return false;
		
		LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(data.market);
		if (base == null) return false;
		
		StarSystemAPI system = data.market.getStarSystem();
		if (system == null) return false;
		
		return startRaid(base.getMarket(), data.market, data.interest, system, stage, getRandomizedStageRandom(3));
	}

	public void reportCellsDisrupted(LuddicPathCellsIntel cell) {
//		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
//		if (intel == null) return;
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		HAPatherCellsEventData data = getPatherCellData(stage);
		if (data != null && data.cells == cell && stage.rollData != null) {
			intel.resetHA_EVENT();
		}
	}
	
	public static void avertOrAbortAttack() {
		if (GenericRaidFGI.get(ATTACK_KEY) != null) {
			GenericRaidFGI.get(ATTACK_KEY).finish(false);
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel == null) return;
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		HAPatherCellsEventData data = getPatherCellData(stage);
		if (data != null && stage.rollData != null) {
			intel.resetHA_EVENT();
		}
	}
	
	public boolean startRaid(MarketAPI source, MarketAPI target, float interest, StarSystemAPI system, EventStageData stage, Random random) {
		
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 14f + random.nextFloat() * 14f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = system;
		params.raidParams.allowedTargets.add(target);
		params.raidParams.allowNonHostileTargets = true;
		params.raidParams.setBombardment(BombardType.SATURATION);
		
		params.style = FleetStyle.STANDARD;
		
		
		float w = interest;
		w += Math.max(0f, (target.getSize() - 2)) * 10f;
		if (w < 0f) w = 0f;
		if (w > 50f) w = 50f;
		
		float f = w / 50f;
		float totalDifficulty = (0.25f + f * 0.75f) * 40f;
		
		Random r = getRandomizedStageRandom(7);
		if (r.nextFloat() < 0.33f) {
			params.style = FleetStyle.QUANTITY;
		}
		
		while (totalDifficulty > 0) {
//			float max = Math.min(10f, totalDifficulty * 0.5f);
//			float min = Math.max(2, max - 2);
//			if (max < min) max = min;
//			
//			int diff = Math.round(StarSystemGenerator.getNormalRandom(r, min, max));
			int diff = (int) Math.min(10f, totalDifficulty);
			if (diff < 2) diff = 2;
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		
		LuddicPathBaseIntel base = LuddicPathBaseIntel.getIntelFor(source);
		if (base != null) {
			if (Misc.isHiddenBase(source) && !base.isPlayerVisible()) {
				base.makeKnown();
				base.sendUpdateIfPlayerHasIntel(LuddicPathBaseIntel.DISCOVERED_PARAM, false);
			}
		}
		
		//PatherAttack raid = new PatherAttack(params);
		params.memoryKey = ATTACK_KEY;
		GenericRaidFGI raid = new GenericRaidFGI(params);
		raid.setListener(this);
		Global.getSector().getIntelManager().addIntel(raid);
		
		return true;
	}
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setPlayerDefeatedPatherExpedition();
		
		MessageIntel msg = new MessageIntel();
		msg.addLine("Luddic Path cells disrupted", Misc.getBasePlayerColor());
		msg.setIcon(Global.getSettings().getSpriteName("intel", "sleeper_cells"));
		msg.setSound(Sounds.REP_GAIN);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
		
		List<IntelInfoPlugin> cells = Global.getSector().getIntelManager().getIntel(LuddicPathCellsIntel.class);
		for (IntelInfoPlugin curr : cells) {
			LuddicPathCellsIntel cell = (LuddicPathCellsIntel) curr;
			//if (cell.getMarket().isPlayerOwned()) {
				cell.makeSleeper(Global.getSettings().getFloat("patherCellDisruptionDuration"));
				//cell.sendUpdateIfPlayerHasIntel(LuddicPathCellsIntel.UPDATE_DISRUPTED, false);
				ListenerUtil.reportCellDisrupted(cell);
			//}
		}
		
	}
	
}




