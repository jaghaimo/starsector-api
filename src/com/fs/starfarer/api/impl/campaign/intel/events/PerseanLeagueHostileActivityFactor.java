package com.fs.starfarer.api.impl.campaign.intel.events;

import java.util.Random;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.PerseanLeagueBlockade;
import com.fs.starfarer.api.impl.campaign.intel.group.PerseanLeaguePunitiveExpedition;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class PerseanLeagueHostileActivityFactor extends BaseHostileActivityFactor implements
						FGIEventListener, ColonyPlayerHostileActListener {

	//public static final String DEFEATED_BLOCKADE = "$defeatedLeagueBlockade";
	public static final String HASSLE_REASON = "leagueEnforcer";
	
//	public static float INDEPENDENT_REP_FOR_DEFEATING = 0.5f;
//	public static float HEGEMONY_REP_FOR_DEFEATING = 0.3f;
	
	public PerseanLeagueHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!checkFactionExists(Factions.PERSEAN, true)) {
			return 0;
		}
		return super.getProgress(intel);
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Persean League";
	}
	
	public String getNameForThreatList(boolean first) {
		return "Persean League";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("A large independent polity is a tantalizing prize for the Persean League, and "
						+ "they will go quite far in exerting pressure for you to join them. League "
						+ "\"enforcer\" fleets prowl your systems, ostensibly to protect the League's "
						+ "interests in \"unclaimed territory\".", 0f);
				
//				tooltip.addPara("Going to Kazeron and negotiating to join the League is likely to get "
//						+ "this harassment to stop. A saturation bombardment of a League world would make your "
//						+ "joining the league politically impossible, but of course has other ramifications. If "
//						+ "left unchecked, the conflict will eventually come to a head and is likely to "
//						+ "be resolved one way or another.", opad, Misc.getHighlightColor(),
//						"join the League", "saturation bombardment");
			}
		};
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0;
	}


	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor();
	}
	
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("perseanLeagueMaxFleets");
	}
	
	
	@Override
	public float getSpawnInHyperProbability(StarSystemAPI system) {
		return 0f;
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
		// minimum is 0.66f for this factor due to it requiring some market presence
		float f = intel.getMarketPresenceFactor(system);
		
		int difficulty = 0 + (int) Math.max(1f, Math.round(f * 4f));
		difficulty += random.nextInt(6);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.PERSEAN;
		
		m.createStandardFleet(difficulty, factionId, loc);
		m.triggerSetFleetType(FleetTypes.LEAGUE_ENFORCER);
		m.triggerSetPatrol();
		m.triggerSetFleetHasslePlayer(HASSLE_REASON);
		m.triggerSetFleetFlag("$leagueEnforcer");
		
		m.triggerFleetAllowLongPursuit();
		m.triggerMakeLowRepImpact();
		
		//m.triggerMakeHostile();
		//m.triggerMakeHostileWhileTransponderOff();
		
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null) {
			fleet.addScript(new NPCHassler(fleet, system));
		}

		return fleet;
	}
	

	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		//info.addPara("Rumors of Persean League blockade", tc, initPad);
		Color c = Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor();
		info.addPara("Impending Persean League blockade", initPad, tc, c, "Persean League");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Persean League blockade averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		info.addPara("You've received intel that the Persean League is planning a lengthy blockade of one of your systems. "
				+ "Colonies in that system will suffer a major accessibility penalty for as long as the blockade lasts.",
				small, Misc.getNegativeHighlightColor(), "major accessibility penalty");
		
//		LabelAPI label = info.addPara("If the blockading force is defeated, your standing with the Hegemony "
//				+ "and the independents will increase substantially, and the Persean League will likely abandon "
//				+ "further efforts to strong-arm you and be more open to negotiation.", 
//				opad);
//		label.setHighlight("Hegemony", "independents", "increase substantially", "Persean League");
//		label.setHighlightColors(Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor(),
//				Global.getSector().getFaction(Factions.INDEPENDENT).getBaseUIColor(),
//				Misc.getPositiveHighlightColor(),
//				Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor());
		
		LabelAPI label = info.addPara("If the blockading force is defeated, your colonies will be viewed as "
				+ "a more stable trading partner, resulting in increased accessibility, "
				+ "and the Persean League will likely abandon "
				+ "further efforts to strong-arm you and be more open to negotiation.", 
				opad);
		label.setHighlight("increased accessibility", "Persean League");
		label.setHighlightColors(Misc.getPositiveHighlightColor(),
								 Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor());		
		
		Color c = Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor();
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("You go to %s and make an agreement about joining the League", 0f, c, "Kazeron");
		info.addPara("%s is tactically bombarded", 0f, c, "Kazeron");
		//info.addPara("Performing a saturation bombardment of a %s world", 0f, c, "Persean League");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.PERSEAN).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Persean League blockade", intel, stage);
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			if (wasPLEverSatBombardedByPlayer() || getKazeron(true) == null) {
				return 0f;
			}
			
			if (PerseanLeagueBlockade.get() != null) {
				return 0f;
			}
			if (PerseanLeaguePunitiveExpedition.get() != null) {
				return 0f;
			}
			
			StarSystemAPI target = findBlockadeTarget(intel, stage);
			MarketAPI source = getBlockadeSource(intel, stage, target);
			if (target != null && source != null) {
				return 10f;
			}
		}
		return 0;
	}
	

	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = new HAERandomEventData(this, stage);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		StarSystemAPI target = findBlockadeTarget(intel, stage);
		MarketAPI source = getBlockadeSource(intel, stage, target);
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startBlockade(source, target, stage, getRandomizedStageRandom(3));
	}
	
	
	public static StarSystemAPI findBlockadeTarget(HostileActivityEventIntel intel, EventStageData stage) {
		float max = 0f;
		StarSystemAPI best = null;
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			float w = intel.getMarketPresenceFactor(system);
			if (w > max) {
				max = w;
				best = system;
			}
		}
		return best;
	}
	
	public MarketAPI getBlockadeSource(HostileActivityEventIntel intel, EventStageData stage, final StarSystemAPI target) {
		return getKazeron(true);
	}
	
	public static MarketAPI getKazeron(boolean requireMilitaryBase) {
		MarketAPI kazeron = Global.getSector().getEconomy().getMarket("kazeron");
		if (kazeron == null || kazeron.hasCondition(Conditions.DECIVILIZED) || 
				!kazeron.getFactionId().equals(Factions.PERSEAN)) {
			return null;
		}
		if (requireMilitaryBase) {
			Industry b = kazeron.getIndustry(Industries.MILITARYBASE);
			if (b == null) b = kazeron.getIndustry(Industries.HIGHCOMMAND);
			if (b == null || b.isDisrupted() || !b.isFunctional()) {
				return null;
			}
		}
		return kazeron;
	}
	
	public static boolean wasPLEverSatBombardedByPlayer() {
		FactionAPI faction = Global.getSector().getFaction(Factions.PERSEAN);
		if (faction != null) {
			return faction.getMemoryWithoutUpdate().getInt(MemFlags.FACTION_SATURATION_BOMBARED_BY_PLAYER) > 0;
		}
		return false;
	}
	
	
	public boolean startBlockade(MarketAPI source, StarSystemAPI target, EventStageData stage, Random random) {
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 7f + random.nextFloat() * 14f;
		params.payloadDays = 365f;
		
		params.makeFleetsHostile = false;
		
		FGBlockadeParams bParams = new FGBlockadeParams();
		bParams.where = target;
		bParams.targetFaction = Factions.PLAYER;
		
		
		params.style = FleetStyle.STANDARD;
		
		
		// standard Kazeron fleet size multiplier with no shortages/issues is a bit over 200%
		float fleetSizeMult = source.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
		
		float f = intel.getMarketPresenceFactor(target);
		
		float totalDifficulty = fleetSizeMult * 50f * (0.5f + 0.5f * f);
		if (totalDifficulty < 30) {
			return false;
		}
		if (totalDifficulty > 100) {
			totalDifficulty = 100;
		}
		
		
		totalDifficulty -= 10;
		totalDifficulty -= 5;
		totalDifficulty -= 4;
		totalDifficulty -= 1;
		totalDifficulty -= 1;
		params.fleetSizes.add(10); // first size 10 pick becomes the Grand Armada
		params.fleetSizes.add(5);
		params.fleetSizes.add(4);
		params.fleetSizes.add(1); // supply fleets #1
		params.fleetSizes.add(1); // supply fleets #2
		
		Random r = getRandomizedStageRandom(7);
		
		// mostly maxed-out fleets, some smaller ones
		while (totalDifficulty > 0) {
			float max = 5f;
			float min = 3f;
			
			if (r.nextFloat() > 0.3f) {
				min = (int) Math.min(totalDifficulty, 10f);
				max = (int) Math.min(totalDifficulty, 10f);
			}
			
			int diff = Math.round(StarSystemGenerator.getNormalRandom(r, min, max));
			
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		PerseanLeagueBlockade blockade = new PerseanLeagueBlockade(params, bParams);
		blockade.setListener(this);
		Global.getSector().getIntelManager().addIntel(blockade);
		
		return true;
	}
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		PerseanLeagueMembership.setDefeatedBlockade(true);
		new EstablishedPolityScript();
//		Misc.adjustRep(Factions.HEGEMONY, HEGEMONY_REP_FOR_DEFEATING, null);
//		Misc.adjustRep(Factions.INDEPENDENT, INDEPENDENT_REP_FOR_DEFEATING, null);
	}
	
	
	
	@Override
	public void notifyFactorRemoved() {
		Global.getSector().getListenerManager().removeListener(this);
	}

	public void notifyEventEnding() {
		notifyFactorRemoved();
	}

	
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market,
			TempData actionData, CargoAPI cargo) {
		
	}

	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
		
	}

	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		MarketAPI kazeron = getKazeron(false);
		if (market != null && market == kazeron) {
			EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
			if (stage != null) {
				boolean thisEvent = stage.rollData instanceof HAERandomEventData && 
						((HAERandomEventData)stage.rollData).factor == this;
				// no points if blockade is the event since it'll lead to a reset anyway
				if (!thisEvent) {
					int points = Global.getSettings().getInt("HA_tacBombardKazeron");
					if (points > 0) {
						intel.addFactor(new HAKazeronTacBombardmentFactor(-points));
					}
				}
			}
//			intel.resetHA_EVENTIfFromFactor(this);
//			EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
//			if (stage != null && stage.rollData instanceof HAERandomEventData && 
//					((HAERandomEventData)stage.rollData).factor == this) {
//				intel.resetHA_EVENT();
//			}
		}
	}

	public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			MarketAPI kazeron = getKazeron(true);
			
			if (kazeron == null || wasPLEverSatBombardedByPlayer()) {
				intel.resetHA_EVENT();
			}			
		}
	}
	
	
}




