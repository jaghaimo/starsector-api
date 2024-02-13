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
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PirateHostileActivityFactor extends BaseHostileActivityFactor implements FGIEventListener {

//	public static class HARaidEventData {
//		public SectorEntityToken source;
//		public StarSystemAPI target;
//	}
	
	public static String RAID_KEY = "$PirateRaid_ref";
	public static String SMALL_RAID_KEY = "$SmallPirateRaid_ref";
	
	public static final String DEFEATED_LARGE_PIRATE_RAID = "$defeatedLargePirateRaid";
	
	public static boolean isDefeatedLargePirateRaid() {
		//if (true) return true;
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_LARGE_PIRATE_RAID);
	}
	public static void setDefeatedLargePirateRaid(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_LARGE_PIRATE_RAID, value);
	}
	
	
	public PirateHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (PiracyRespiteScript.get() != null) {
			return 0;
		}
		return super.getProgress(intel);
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

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
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

	@Override
	public Color getNameColorForThreatList() {
		return Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
	}

	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor();
	}
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		if (getProgress(intel) <= 0) {
			return 1;
		}
		return super.getMaxNumFleets(system);
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
		
		float f = 0f;
		f += getEffectMagnitude(system);
		
		if (f > 1f) f = 1f;
		
		int difficulty = 0;
		difficulty += (int) Math.round(f * 7f);
		
//		int size = 0;
//		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
//			size = Math.max(market.getSize(), size);
//		}
//		int minDiff = Math.max(0, size - 2);
		
		float mult = 1f;
		if (getProgress(intel) <= 0) {
			mult = 0.5f;
		}
		
		int minDiff = Math.round(intel.getMarketPresenceFactor(system) * 6f * mult);
		
		if (difficulty < minDiff) difficulty = minDiff;
		
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
		
		small = 8f;
//		info.addPara("There are rumors that a pirate raid targeting your colonies "
//				+ "may be organized in the near future.", opad);
//		
//		info.addPara(BaseIntelPlugin.BULLET + "If the raid is successful, the targeted colonies will suffer from reduced stability.", opad,
//				Misc.getNegativeHighlightColor(), "reduced stability");
//		
//		info.addPara(BaseIntelPlugin.BULLET + "If the raid is defeated, your colonies will gain "
//				+ "increased accessibility for several cycles.", 
//				0f, Misc.getPositiveHighlightColor(), "increased accessibility");

		info.addPara("There are rumors that a pirate raid targeting your colonies "
				+ "may be organized in the near future. If the raid is successful, the targeted colonies will "
				+ "suffer from reduced stability.", small,
				Misc.getNegativeHighlightColor(), "reduced stability");
		
		if (stage.id == Stage.HA_EVENT) {
			if (PiracyRespiteScript.DURATION < 0) {
				info.addPara("If the raid is defeated, your colonies will "
						+ "permanently gain increased accessibility.", 
						opad, Misc.getPositiveHighlightColor(), "increased accessibility");
			} else {
				info.addPara("If the raid is defeated, your colonies will gain "
						+ "increased accessibility for several cycles.", 
						opad, Misc.getPositiveHighlightColor(), "increased accessibility");
			}
		}
		
		//if (stage.id == Stage.MINOR_EVENT) {
			stage.addResetReq(info, false, "crisis", -1, -1, opad);
//		} else {
//			stage.beginResetReqList(info, true, "crisis", opad);
			// want to keep this less prominent, actually, so: just the above
//			info.addPara("An agreement is reached with Kanta, the pirate queen", 
//					0f, Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor(), "Luddic Path");
//			stage.endResetReqList(info, false, "crisis", -1, -1);
//		}
		
		
		
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
			if (stage.id == Stage.MINOR_EVENT) {
				return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					tooltip.addTitle("Pirate raid");
					tooltip.addPara("A pirate raid will be launched against one of your "
							+ "star systems.", opad);
				}
			};
			}
			return getDefaultEventTooltip("Pirate raid", intel, stage);
//			return new BaseFactorTooltip() {
//				@Override
//				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
//					float opad = 10f;
//					tooltip.addTitle("Pirate raid");
//					tooltip.addPara("A pirate raid will be launched against one of your "
//							+ "star systems.", opad);
////					tooltip.addPara("If the raid is defeated, your colonies will receive "
////							+ "increased accessibility for several cycles.", 
////							opad, Misc.getPositiveHighlightColor(), "increased accessibility");
//					stage.addResetReq(tooltip, true, "crisis", HostileActivityEventIntel.RESET_MIN, HostileActivityEventIntel.RESET_MAX, opad);
//				}
//			};
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (KantaCMD.playerHasProtection()) return 0f;
		
		if (PiracyRespiteScript.get() != null) return 0f;
		
		if (stage.id == Stage.HA_EVENT || stage.id == Stage.MINOR_EVENT) {
			StarSystemAPI target = findRaidTarget(intel, stage);
			MarketAPI source = findRaidSource(intel, stage, target);
			if (target != null && source != null) {
				return 10f;
			}
		}
		return 0f;
	}
	

//	public void resetEvent(HostileActivityEventIntel intel, EventStageData stage) {
//		super.resetEvent(intel, stage);
//	}
	
	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
//		if (true) return;
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
	
		stage.rollData = null;
		return startRaid(source, target, stage, getRandomizedStageRandom(5));
	}
	
	public StarSystemAPI findRaidTarget(HostileActivityEventIntel intel, EventStageData stage) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(getRandomizedStageRandom(3));
		
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			float mag = getEffectMagnitude(system);
			if (mag < 0.1f && stage.id != Stage.MINOR_EVENT) {
			//if (mag < 0.2f) {
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
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom());
		for (int i = 0; i < list.size() && i < 4; i++) {
			MarketAPI market = list.get(i);
			float dist = Misc.getDistance(target.getLocation(), market.getLocationInHyperspace());
			float w = 100000f / (dist * dist);
			picker.add(market, w);
		}
		
		return picker.pick();
	}
	
	public static void avertOrAbortRaid() {
		if (GenericRaidFGI.get(SMALL_RAID_KEY) != null) {
			GenericRaidFGI.get(SMALL_RAID_KEY).finish(false);
		}
		
		if (GenericRaidFGI.get(RAID_KEY) != null) {
			GenericRaidFGI.get(RAID_KEY).finish(false);
		}
		
		HostileActivityEventIntel intel = HostileActivityEventIntel.get();
		if (intel == null) return;
		
		HAERandomEventData data = intel.getRollDataForEvent();
		if (data != null && data.factor instanceof PirateHostileActivityFactor) {
			intel.resetHA_EVENT();
		}
	}

	
	public boolean startRaid(MarketAPI source, StarSystemAPI target, EventStageData stage, Random random) {
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 7f + random.nextFloat() * 14f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = target;
		for (MarketAPI market : Misc.getMarketsInLocation(target)) {
			if (market.getFaction().isHostileTo(source.getFaction()) || market.getFaction().isPlayerFaction()) {
				params.raidParams.allowedTargets.add(market);
			}
		}
		if (params.raidParams.allowedTargets.isEmpty()) return false;
		params.raidParams.allowNonHostileTargets = true;
		
		params.style = FleetStyle.STANDARD;
		
		if (stage.id == Stage.MINOR_EVENT) {
			params.fleetSizes.add(5);
			params.fleetSizes.add(3);
			params.memoryKey = SMALL_RAID_KEY;
		} else {
			params.memoryKey = RAID_KEY;
			
			float mag1 = getEffectMagnitude(target);
			if (mag1 > 1f) mag1 = 1f;
			float mag2 = intel.getMarketPresenceFactor(target);
			float totalDifficulty = (0.25f + mag1 * 0.25f + mag2 * 0.5f) * 100f;
			
			Random r = getRandomizedStageRandom(7);
			if (r.nextFloat() < 0.33f) {
				params.style = FleetStyle.QUANTITY;
			}
			
			while (totalDifficulty > 0) {
				float max = Math.min(10f, totalDifficulty * 0.5f);
				float min = Math.max(2, max - 2);
				if (max < min) max = min;
				
				int diff = Math.round(StarSystemGenerator.getNormalRandom(r, min, max));
				
				params.fleetSizes.add(diff);
				totalDifficulty -= diff;
			}
		}
		
		PirateBaseIntel base = PirateBaseIntel.getIntelFor(source);
		if (base != null) {
			if (Misc.isHiddenBase(source) && !base.isPlayerVisible()) {
				base.makeKnown();
				base.sendUpdateIfPlayerHasIntel(PirateBaseIntel.DISCOVERED_PARAM, false);
			}
		}
		
		GenericRaidFGI raid = new GenericRaidFGI(params);
		if (stage.id == Stage.HA_EVENT) { // don't want piracy respite from the minor raid
			raid.setListener(this);
		}
		Global.getSector().getIntelManager().addIntel(raid);
		
		return true;
	}
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setDefeatedLargePirateRaid(true);
		new PiracyRespiteScript();
	}
	
	
	
	public static void main(String[] args) {
		Random r = new Random();
		int [] counts = new int[11];
		for (int i = 0; i < 10000; i++) {
			int x = Math.round(getNormalRandom(r, 7, 10));
			counts[x]++;
		}
		for (int i = 0; i < counts.length; i++) {
			System.out.println(i + ":	" + counts[i]);
		}
	}
	
	public static float getNormalRandom(Random random, float min, float max) {
		double r = random.nextGaussian();
		r *= 0.2f;
		r += 0.5f;
		if (r < 0) r = 0;
		if (r > 1) r = 1;
		
		// 70% chance 0.3 < r < .7
		// 95% chance 0.1 < r < .7
		// 99% chance 0 < r < 1
		return min + (float) r * (max - min);
	}

}




