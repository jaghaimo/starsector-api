package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidAssembleStage2;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.intel.raid.ReturnStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.TravelStage;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PirateHostileActivityFactor extends BaseHostileActivityFactor {

//	public static class HARaidEventData {
//		public SectorEntityToken source;
//		public StarSystemAPI target;
//	}
	
	public PirateHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Pirate activity";
	}
	
	public String getNameForThreatList(boolean first) {
		if (first) return "Pirates";
		return "Pirates";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Piracy follows interstellar civilization almost without exception.", 0f);
				if (KantaCMD.playerHasProtection()) {
					tooltip.addPara("However, you have %s, which is enough dissuade most pirates from attacking your interests.",
							opad, Misc.getPositiveHighlightColor(), "Kanta's protection");
				} else {
					if (KantaCMD.playerEverHadProtection()) {
						tooltip.addPara("You've %s, and it's not the sort of thing you can do over.",
								opad, Misc.getNegativeHighlightColor(), "lost Kanta's protection");
					} else {
						tooltip.addPara("Having %s, however, should be enough dissuade most pirates from attacking your interests.",
								opad, Misc.getHighlightColor(), "Kanta's protection");
					}
				}
			}
		};
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0 || KantaCMD.playerHasProtection();
	}


	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
	}
	
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
		
		float f = 0f;
		f += getEffectMagnitude(system);
		
		if (f > 1f) f = 1f;
		
		int difficulty = 0;
		difficulty += (int) Math.round(f * 7f);
		
		difficulty += random.nextInt(4);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.PIRATES;
		
		m.createStandardFleet(difficulty, factionId, loc);
		m.triggerSetPirateFleet();
		m.triggerMakeLowRepImpact();
		//m.triggerFleetAllowLongPursuit();
		
		CampaignFleetAPI fleet = m.createFleet();

		return fleet;
	}
	

	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Rumors of pirate raid", tc, initPad);
//		Color c = Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
//		info.addPara("Rumors of pirate raid", initPad, tc, c, "pirate raid");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Pirate raid averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 5f;
		info.addPara("There are rumors that a pirate raid targeting your colonies "
				+ "may be organized at some point in the future.", small);
		stage.addResetReq(info, false, opad);
		
		addBorder(info, Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor());
		
		
//		Color c = Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
//		UIComponentAPI rect = info.createRect(c, 2f);
//		info.addCustomDoNotSetPosition(rect).getPosition().inTL(-small, 0).setSize(
//				info.getWidthSoFar() + small * 2f, Math.max(64f, info.getHeightSoFar() + small));
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.PIRATES).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT || stage.id == Stage.MINOR_EVENT) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					tooltip.addTitle("Pirate raid");
					tooltip.addPara("A pirate raid will be launched against one of your "
							+ "star systems.", opad);
					stage.addResetReq(tooltip, true, opad);
				}
			};
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (KantaCMD.playerHasProtection()) return 0f;
		
		if (stage.id == Stage.HA_EVENT || stage.id == Stage.MINOR_EVENT) {
			StarSystemAPI target = findRaidTarget(intel, stage);
			MarketAPI source = findRaidSource(intel, stage, target);
			if (target != null && source != null) {
				return 10f;
			}
		}
		return 0;
	}
	

	public void resetEvent(HostileActivityEventIntel intel, EventStageData stage) {
		super.resetEvent(intel, stage);
	}
	
	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = new HAERandomEventData(this, stage);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		StarSystemAPI target = findRaidTarget(intel, stage);
		MarketAPI source = findRaidSource(intel, stage, target);
		if (source == null || target == null) {
			return false;
		}
	
		float mag = getEffectMagnitude(target);
		
		float raidFP = 500f * (0.5f + Math.min(1f, mag) * 0.5f);
		raidFP *= 0.85f + 0.3f * intel.getRandom().nextFloat();
		
		if (stage.id == Stage.MINOR_EVENT) {
			raidFP = 120 + 30f * intel.getRandom().nextFloat();
		}
		
		stage.rollData = null;
		return startRaid(source, target, raidFP);
	}
	
	public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
		
	}
	
	public StarSystemAPI findRaidTarget(HostileActivityEventIntel intel, EventStageData stage) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(intel.getRandom());
		
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			float mag = getEffectMagnitude(system);
			if (mag < 0.2f) {
				continue;
			}
			picker.add(system, mag * mag);
		}
		
		return picker.pick();
	}
	
	public MarketAPI findRaidSource(HostileActivityEventIntel intel, EventStageData stage, final StarSystemAPI target) {
		if (target == null) return null;
		
		List<MarketAPI> list = new ArrayList<MarketAPI>();
		float maxDist = Global.getSettings().getFloat("sectorWidth") * 0.5f;
		
		for (IntelInfoPlugin curr : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
			PirateBaseIntel base = (PirateBaseIntel) curr;
			if (base.playerHasDealWithBaseCommander()) continue;
			
			float dist = Misc.getDistance(target.getLocation(), base.getMarket().getLocationInHyperspace());
			if (dist > maxDist) continue;
			
			list.add(base.getMarket());
		}
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (Factions.PIRATES.equals(market.getFaction().getId())) {
				for (MarketAPI other : Misc.getMarketsInLocation(market.getContainingLocation())) {
					if (other == market) continue;
					if (!other.getFaction().isHostileTo(market.getFaction())) continue;
					if (other.getSize() <= market.getSize() - 2) continue;
					
					float dist = Misc.getDistance(market.getPrimaryEntity().getLocation(), other.getPrimaryEntity().getLocation());
					if (dist < 8000) continue;
					
					list.add(market);
				}
			}
		}
		
		Collections.sort(list, new Comparator<MarketAPI>() {
			public int compare(MarketAPI m1, MarketAPI m2) {
				float d1 = Misc.getDistance(target.getLocation(), m1.getLocationInHyperspace());
				float d2 = Misc.getDistance(target.getLocation(), m2.getLocationInHyperspace());
				return (int) Math.signum(d1 - d2);
			}
		});
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(intel.getRandom());
		for (int i = 0; i < list.size() && i < 4; i++) {
			MarketAPI market = list.get(i);
			float dist = Misc.getDistance(target.getLocation(), market.getLocationInHyperspace());
			float w = 100000f / (dist * dist);
			picker.add(market, w);
		}
		
		return picker.pick();
	}
	

	
	public boolean startRaid(MarketAPI source, StarSystemAPI target, float raidFP) {
		boolean hasTargets = false;
		for (MarketAPI curr : Misc.getMarketsInLocation(target)) {
			if (curr.getFaction().isHostileTo(source.getFaction())) {
				hasTargets = true;
				break;
			}
		}
		
		if (!hasTargets) return false;
		
		FactionAPI faction = source.getFaction();
		
		RaidIntel raid = new RaidIntel(target, faction, this);
		
		float successMult = 0.5f;
		
		JumpPointAPI gather = null;
		List<JumpPointAPI> points = source.getContainingLocation().getEntities(JumpPointAPI.class);
		float min = Float.MAX_VALUE;
		for (JumpPointAPI curr : points) {
			float dist = Misc.getDistance(source.getPrimaryEntity().getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				gather = curr;
			}
		}
		
		SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(faction, target.getCenter());
		if (gather == null || raidJump == null) return false;
		
		PirateRaidAssembleStage2 assemble = new PirateRaidAssembleStage2(raid, gather);
		assemble.addSource(source);
		assemble.setSpawnFP(raidFP);
		assemble.setAbortFP(raidFP * successMult);
		raid.addStage(assemble);
		
		TravelStage travel = new TravelStage(raid, gather, raidJump, false);
		travel.setAbortFP(raidFP * successMult);
		raid.addStage(travel);
		
		PirateRaidActionStage action = new PirateRaidActionStage(raid, target);
		action.setAbortFP(raidFP * successMult);
		raid.addStage(action);
		
		raid.addStage(new ReturnStage(raid));
		
		Global.getSector().getIntelManager().addIntel(raid);
		
		return true;
	}
	
}




