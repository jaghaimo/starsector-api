package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.PatherCellListener;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LuddicPathHostileActivityFactor extends BaseHostileActivityFactor implements PatherCellListener {

	public static class HAPatherCellsEventData {
		public LuddicPathCellsIntel cells;
		public MarketAPI market;
		public Industry target;
		public HAPatherCellsEventData(LuddicPathCellsIntel cells, MarketAPI market) {
			this.cells = cells;
			this.market = market;
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

	public TooltipCreator getMainRowTooltip() {
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
				tooltip.addPara("You've tithed a signficant amount to the Pathers, and their fleets and "
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
		return getProgress(intel) > 0 || HA_CMD.playerHasPatherAgreement();
	}



	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("luddicPathMaxFleets");
	}

	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
		
		float f = 0f;
		f += getEffectMagnitudeAdjustedBySuppression(system);
		
		if (f > 1f) f = 1f;
		
		float p = Global.getSettings().getFloat("luddicPathSmallFleetProb");
		boolean small = random.nextFloat() < p;
		
		int difficulty = 0;
		
		if (small) {
			difficulty = 1 + random.nextInt(2);
		} else {
			difficulty = 3;
			difficulty += (int) Math.round(f * 5f);
			difficulty += random.nextInt(4);
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
	

	public void notifyEventEnding() {
		Global.getSector().getListenerManager().removeListener(this);
	}
	
	public HAPatherCellsEventData getPatherCellData(EventStageData stage) {
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
		
		if (data != null && data.target != null) {
			LabelAPI label = info.addPara("%s on %s disrupted for %s days by %s sabotage",
					initPad, tc, c,
					data.target.getNameForModifier(), data.market.getName(), 
					"" + (int)data.target.getDisruptedDays(), "Luddic Path");
			label.setHighlight(data.target.getNameForModifier(), data.market.getName(), 
								"" + (int)data.target.getDisruptedDays(), "Luddic Path");
			label.setHighlightColors(tc, Misc.getBasePlayerColor(), Misc.getHighlightColor(), c);
			return;
		}
		
		LabelAPI label = info.addPara("Signs of impending %s attack targeting %s",
								initPad, tc, c, "Luddic Path", data.market.getName());
		label.setHighlight("Luddic Path", data.market.getName());
		//label.setHighlightColors(c, Misc.getBasePlayerColor());
		label.setHighlightColors(Misc.getTextColor(), Misc.getBasePlayerColor());
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
		
		small = 5f;
		
		LabelAPI label = info.addPara("Recent intelligence indicates that a Luddic Path cell is preparing "
				+ "an act of industrial sabotage targeting %s.",
				small, Misc.getBasePlayerColor(), data.market.getName());
		label.setHighlight("Luddic Path", data.market.getName());
		label.setHighlightColors(c, Misc.getBasePlayerColor());
		stage.beginResetReqList(info, true, opad);
		
//		LabelAPI label = info.addPara("The Luddic Path cells on %s are disrupted", 
//				0f, Misc.getBasePlayerColor(), data.market.getName());
//		label.setHighlight("Luddic Path", data.market.getName());
//		label.setHighlightColors(c, Misc.getBasePlayerColor());
		info.addPara("The Luddic Path cells on %s are disrupted", 
					0f, Misc.getTextColor(), data.market.getName());
		stage.endResetReqList(info, false);
		//stage.addResetReq(info, false, opad);
		
		addBorder(info, Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor());
		
//		info.addSpacer(small);
//		c = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
//		UIComponentAPI rect = info.createRect(c, 2f);
//		float extra = 0f;
//		extra = 64f + 14f;
//		info.addCustomDoNotSetPosition(rect).getPosition().inTL(-small - extra, 0).setSize(
//				info.getWidthSoFar() + small * 2f + extra, Math.max(64f, info.getHeightSoFar() + 3f));
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.LUDDIC_PATH).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI info, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					info.addTitle("Luddic Path attack");
					HAPatherCellsEventData data = getPatherCellData(stage);
					if (data == null) return;
					
					info.addPara("A Luddic Path cell will perform "
							+ "an act of terror or industrial sabotage targeting %s.",
							opad, Misc.getBasePlayerColor(), data.market.getName());
					
					stage.beginResetReqList(info, true, opad);
					Color c = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
					LabelAPI label = info.addPara("The Luddic Path cells on %s are disrupted", 
							0f, Misc.getBasePlayerColor(), data.market.getName());
					label.setHighlight("Luddic Path", data.market.getName());
					label.setHighlightColors(c, Misc.getBasePlayerColor());
					stage.endResetReqList(info, true);
				}
			};
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
			picker.add(market, w);
		}
		return picker.pick();
	}

	
	public void resetEvent(HostileActivityEventIntel intel, EventStageData stage) {
		super.resetEvent(intel, stage);
//		HAERandomEventData data = (HAERandomEventData) stage.rollData;
//		intel.sendUpdateIfPlayerHasIntel(data, false);
//		stage.rollData = null;
	}
	
	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		MarketAPI market = pickTargetMarket();
		LuddicPathCellsIntel cells = LuddicPathCellsIntel.getCellsForMarket(market);
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

		WeightedRandomPicker<Industry> picker = new WeightedRandomPicker<Industry>(intel.getRandom());
		for (Industry ind : data.market.getIndustries()) {
			if (!ind.canBeDisrupted()) continue;
			float mult = 1f;
			if (ind.isStructure()) mult = 0.1f;
			picker.add(ind, ind.getPatherInterest() * mult);
		}
		Industry target = picker.pick();
		if (target == null) {
			return false;
		}
		
		data.target = target;
		
		float disruptionDur = LuddicPathCellsIntel.MIN_SABOTAGE + 
				intel.getRandom().nextFloat() * (LuddicPathCellsIntel.MAX_SABOTAGE - LuddicPathCellsIntel.MIN_SABOTAGE);
		target.setDisrupted(disruptionDur, true);
		
		intel.sendUpdateIfPlayerHasIntel(stage.rollData, false);
		
		return true;
	}

	public void reportCellsDisrupted(LuddicPathCellsIntel cell) {
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel == null) return;
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		HAPatherCellsEventData data = getPatherCellData(stage);
		if (data != null && data.cells == cell && stage.rollData != null) {
			intel.resetHA_EVENT();
		}
	}
	
}




