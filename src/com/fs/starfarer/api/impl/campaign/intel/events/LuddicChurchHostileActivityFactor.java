package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.ColonySizeChangeListener;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.econ.LuddicMajority;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.KnightsOfLuddTakeoverExpedition;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LuddicChurchHostileActivityFactor extends BaseHostileActivityFactor implements
						FGIEventListener, ColonyPlayerHostileActListener, ColonySizeChangeListener {

	public static final String HASSLE_REASON = "sacredProtectors";
	
	// in $player memory
	public static final String DEFEATED_LUDDIC_CHURCH_EXPEDITION = "$defeatedLuddicChurchExpedition";
	public static final String MADE_IMMIGRATION_DEAL_WITH_LUDDIC_CHURCH = "$madeImmigrationDealWithLuddicChurch";
	public static final String BROKE_LUDDIC_CHURCH_DEAL = "$brokeLuddicChurchDeal";
	
	public static boolean isDefeatedExpedition() {
		//if (true) return true;
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_LUDDIC_CHURCH_EXPEDITION);
	}
	public static void setDefeatedExpedition(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_LUDDIC_CHURCH_EXPEDITION, value);
	}
	
	public static boolean isMadeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(MADE_IMMIGRATION_DEAL_WITH_LUDDIC_CHURCH);
	}
	public static void setMadeDeal(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(MADE_IMMIGRATION_DEAL_WITH_LUDDIC_CHURCH, value);
	}
	
	
	public static boolean brokeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(BROKE_LUDDIC_CHURCH_DEAL);
	}
	public static void setBrokeDeal(boolean broke) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(BROKE_LUDDIC_CHURCH_DEAL, broke);
	}
	
	
	
	
	
	
	
	public LuddicChurchHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!checkFactionExists(Factions.LUDDIC_CHURCH, true)) {
			return 0;
		}
		return super.getProgress(intel);
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Luddic Church";
	}
	
	public String getNameForThreatList(boolean first) {
		return "Knights of Ludd";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("A large community of the faithful -"
						+ " not under their direct control, and with potential to emerge as a major cultural center - "
						+ "is a growing source of concern to the Church. "
						+ "\"Protector\" fleets operated by the Knights of Ludd can be found your systems, "
						+ "ostensibly there to protect the "
						+ "interests of the local Luddic population.", 0f);
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
		return Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
	}
	
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("luddicChurchMaxFleets");
	}
	
	
	@Override
	public float getSpawnInHyperProbability(StarSystemAPI system) {
		return 0f;
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
		//float f = intel.getMarketPresenceFactor(system);
		
		int maxSize = 0;
		for (MarketAPI curr : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			maxSize = Math.max(curr.getSize(), maxSize);
		}
		
		//int difficulty = 0 + (int) Math.max(1f, Math.round(f * 6f));
		int difficulty = maxSize + 1;
		difficulty += random.nextInt(4);
		if (difficulty > 10) difficulty = 10;
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.LUDDIC_CHURCH;
		
		m.createStandardFleet(difficulty, factionId, loc);
		m.triggerSetFleetQuality(FleetQuality.HIGHER);
		m.triggerSetFleetType(FleetTypes.SACRED_PROTECTORS);
		m.triggerSetPatrol();
		m.triggerSetFleetHasslePlayer(HASSLE_REASON);
		m.triggerSetFleetFlag("$sacredProtectors");
		
		m.triggerFleetAllowLongPursuit();
		m.triggerMakeLowRepImpact();
		
		//m.triggerMakeHostile();
		//m.triggerMakeHostileWhileTransponderOff();
		
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null) {
			fleet.setName("Knights of Ludd " + fleet.getName());
			fleet.setNoFactionInName(true);
			NPCHassler hassle = new NPCHassler(fleet, system);
			hassle.getParams().crDamageMult = 0f; 
			fleet.addScript(hassle);
		}

		return fleet;
	}
	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color c = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
		info.addPara("Impending Luddic Church takeover operation", initPad, tc, c, "Luddic Church");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Luddic Church takeover averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		info.addPara("You've received intel that the Knights of Ludd, under the aegis of the Luddic Church, "
				+ "are planning an operation to take over one of your colonies that has a substantial "
				+ "population of the Luddic faithful.",
				small, Misc.getNegativeHighlightColor(), "take over one of your colonies");
		
		LabelAPI label = info.addPara("If the expedition is defeated, the Luddic faithful leaving the Church worlds "
				+ "will feel more secure, resulting in increased immigration, stability, and productivity, "
				+ "and the Luddic Church will likely abandon further efforts of this sort.", 
				opad);
		label.setHighlight("increased immigration, stability, and productivity", "Luddic Church");
		label.setHighlightColors(Misc.getPositiveHighlightColor(),
				Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor());
		
		Color c = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("You make an agreement with the Church", 0f);
		info.addPara("%s is tactically bombarded", 0f, c, "Hesperus");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Luddic Church expedition", intel, stage);
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			if (isDefeatedExpedition() || getHesperus(true) == null) {
				return 0f;
			}
			
			if (KnightsOfLuddTakeoverExpedition.get() != null) {
				return 0f;
			}
			
			MarketAPI target = findExpeditionTarget(intel, stage);
			MarketAPI source = getExpeditionSource(intel, stage, target);
			if (target != null && source != null) {
				return 20f;
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
		MarketAPI target = findExpeditionTarget(intel, stage);
		MarketAPI source = getExpeditionSource(intel, stage, target);
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startExpedition(source, target, stage, getRandomizedStageRandom(3));
	}
	
	
	public MarketAPI findExpeditionTarget(HostileActivityEventIntel intel, EventStageData stage) {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom());
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			if (market.getStarSystem() == null) continue;
			if (market.hasCondition(Conditions.LUDDIC_MAJORITY)) {
				float size = market.getSize();
				float w = Math.max(size - 3f, 1f);
				w = w * w * w;
				picker.add(market, w);
			}
		}
		return picker.pick();
	}
	
	public MarketAPI getExpeditionSource(HostileActivityEventIntel intel, EventStageData stage, final MarketAPI target) {
		return getHesperus(true);
	}
	
	public static MarketAPI getHesperus(boolean requireMilitaryBase) {
		MarketAPI kazeron = Global.getSector().getEconomy().getMarket("hesperus");
		if (kazeron == null || kazeron.hasCondition(Conditions.DECIVILIZED)) {
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
	
	
	public boolean startExpedition(MarketAPI source, MarketAPI target, EventStageData stage, Random random) {

		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 7f + random.nextFloat() * 14f;
		params.payloadDays = 180f;
		
		params.makeFleetsHostile = false;
		
		FGBlockadeParams bParams = new FGBlockadeParams();
		bParams.where = target.getStarSystem();
		bParams.targetFaction = Factions.PLAYER;
		bParams.specificMarket = target;
		
		params.noun = "takeover";
		params.forcesNoun = "Luddic forces";
		
		params.style = FleetStyle.STANDARD;
		
		
		params.fleetSizes.add(10); // first size 10 pick becomes the Armada
		
		// and a few smaller picket forces
		params.fleetSizes.add(4);
		params.fleetSizes.add(4);
		params.fleetSizes.add(3);
		params.fleetSizes.add(3);

		
		KnightsOfLuddTakeoverExpedition blockade = new KnightsOfLuddTakeoverExpedition(params, bParams);
		blockade.setListener(this);
		Global.getSector().getIntelManager().addIntel(blockade);
		
		return true;
	}
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setDefeatedExpedition(true);
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
		MarketAPI hesperus = getHesperus(false);
		if (market != null && market == hesperus) {
			EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
			if (stage != null) {
				boolean thisEvent = stage.rollData instanceof HAERandomEventData && 
						((HAERandomEventData)stage.rollData).factor == this;
				// no points if takeover expedition is the event since it'll lead to a reset anyway
				if (!thisEvent) {
					int points = Global.getSettings().getInt("HA_tacBombardHesperus");
					if (points > 0) {
						intel.addFactor(new HAHersperusTacBombardmentFactor(-points));
					}
				}
			}

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
			MarketAPI hesperus = getHesperus(true);
			
			if (hesperus == null) {
				intel.resetHA_EVENT();
			}			
		}
	}
	public void reportColonySizeChanged(MarketAPI market, int prevSize) {
		if (!market.isPlayerOwned()) return;
		
		boolean matches = LuddicMajority.matchesBonusConditions(market);
		
		if (market.hasCondition(Conditions.LUDDIC_MAJORITY) && !matches) {
			market.removeCondition(Conditions.LUDDIC_MAJORITY);
		} else if (!market.hasCondition(Conditions.LUDDIC_MAJORITY) && matches) {
			market.addCondition(Conditions.LUDDIC_MAJORITY);
		}
		
	}
	
	
	
}




