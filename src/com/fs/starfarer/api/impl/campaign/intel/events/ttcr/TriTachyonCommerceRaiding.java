package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class TriTachyonCommerceRaiding extends BaseEventIntel {
	
	public static int PROGRESS_MAX = 300;
	public static int PROGRESS_1 = 150;
	
	public static String KEY = "$ttcr_ref";
	
	public static enum Stage {
		START,
		SEND_MERC,
		SUCCESS,
	}
	

	public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
		if (get() == null) {
			new TriTachyonCommerceRaiding(null, false);
		}
		if (get() != null) {
			get().addFactor(factor, dialog);
		}
	}
	
	public static TriTachyonCommerceRaiding get() {
		return (TriTachyonCommerceRaiding) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	public TriTachyonCommerceRaiding(TextPanelAPI text, boolean withIntelNotification) {
		super();
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setup();
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
	}
	
	protected void setup() {
		factors.clear();
		stages.clear();
		
		setMaxProgress(PROGRESS_MAX);
		
		addStage(Stage.START, 0);
		addStage(Stage.SEND_MERC, PROGRESS_1, true, StageIconSize.MEDIUM);
		addStage(Stage.SUCCESS, PROGRESS_MAX, true, StageIconSize.LARGE);
		
		// not actualy repeatable since no way to reduce progress
		// but this will keep the icon and the stage description showing
		//getDataFor(Stage.SEND_MERC).isRepeatable = false;
		
		addFactor(new TTCRCommerceRaidersDestroyedFactorHint());
		addFactor(new TTCRTradeFleetsDestroyedFactorHint());
		addFactor(new TTCRIndustryDisruptedFactorHint());
		
	}
	
	protected Object readResolve() {
		return this;
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, 
			   						Color tc, float initPad) {
		
		if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
			return;
		}
		
		Color h = Misc.getHighlightColor();
		if (isUpdate && getListInfoParam() instanceof EventStageData) {
			EventStageData esd = (EventStageData) getListInfoParam();
			if (esd.id == Stage.SEND_MERC) {
				info.addPara("Several bounty hunters were recently hired to eliminate you", tc, initPad);
			}
			if (esd.id == Stage.SUCCESS) {
				info.addPara("You've convincied Tri-Tachyon to stop attacking your interests", tc, initPad);
			}
			return;
		}
		
	}
	
	@Override
	public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
		float opad = 10f;
		float small = 0f;
		Color h = Misc.getHighlightColor();
		
		//setProgress(0);
		//setProgress(199);
		//setProgress(600);
		//setProgress(899);
		//setProgress(1000);
		//setProgress(499);
		//setProgress(600);
		
		EventStageData stage = getDataFor(stageId);
		if (stage == null) return;
		
		if (isStageActive(stageId)) {
			addStageDesc(info, stageId, small, false);
		}
	}
	
	public FactionAPI getFaction() {
		return Global.getSector().getFaction(Factions.TRITACHYON);
	}
	
	
	public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		FactionAPI faction = getFaction();
		Color tt = faction.getBaseUIColor();
		
		if (stageId == Stage.START) {
			LabelAPI label = info.addPara("The %s needs to be convinced that continued investment in "
					+ "acting against your interests is unprofitable in the long term. If you are "
					+ "able to send this message without becoming openly hostile - or smooth any ruffled feathers "
					+ "afterwards - this may even "
					+ "mark you as someone whose understanding of business realities makes them worth working with.",
					initPad, tt, faction.getDisplayNameLong());
			label.setHighlight(faction.getDisplayNameLong(), "worth working with");
			label.setHighlightColors(tt,
					Misc.getPositiveHighlightColor());
		} else if (stageId == Stage.SUCCESS) {
			info.addPara("You've convinced the %s to stop attacking your interests.", initPad,
					tt, faction.getDisplayNameLong());
		} else if (stageId == Stage.SEND_MERC) {
			info.addPara("Several bounty hunters were recently hired to eliminate you. You are likely "
					+ "to encounter them in the coming months. Resolving the matter with Tri-Tachyon before then "
					+ "is unlikely to make a difference in this; a contract is a contract.", initPad);
		}
	}
	
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		
		if (esd != null && EnumSet.of(Stage.SEND_MERC, Stage.SUCCESS).contains(esd.id)) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					
					if (esd.id == Stage.SEND_MERC) {
						tooltip.addTitle("Bounty posted");
					} else if (esd.id == Stage.SUCCESS) {
						tooltip.addTitle("Success!");
					}

					addStageDesc(tooltip, esd.id, opad, true);
					
					esd.addProgressReq(tooltip, opad);
				}
			};
		}
		
		return null;
	}



	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("events", "triTachyonCR_START");
	}

	protected String getStageIconImpl(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return null;
		
		return Global.getSettings().getSpriteName("events", "triTachyonCR_" + ((Stage)esd.id).name());
	}
	
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_COLONIES);
		tags.add(Factions.TRITACHYON);
		return tags;
	}

	@Override
	public Color getBarColor() {
		Color color = getFaction().getBaseUIColor();
		color = Misc.interpolateColor(color, Color.black, 0.25f);
		return color;
	}
	
	@Override
	public Color getBarProgressIndicatorColor() {
		return super.getBarProgressIndicatorColor();
	}

	@Override
	protected int getStageImportance(Object stageId) {
		return super.getStageImportance(stageId);
	}


	@Override
	protected String getName() {
		return "Tri-Tachyon Commerce Raiding";
	}
	


	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		//setProgress(500);
		//float days = Global.getSector().getClock().convertToDays(amount);
	}
	
	
	@Override
	protected void notifyStageReached(EventStageData stage) {
		//applyFleetEffects();
		
		if (stage.id == Stage.SEND_MERC) {
			sendBountyHunters();
		}

		if (stage.id == Stage.SUCCESS) {
			TriTachyonHostileActivityFactor.setPlayerCounterRaidedTriTach();
			endAfterDelay();
		}
	}

	public boolean withMonthlyFactors() {
		return false;
	}
	
	protected String getSoundForStageReachedUpdate(Object stageId) {
//		if (stageId == Stage.SEND_MERC) {
//			return "ui_learned_ability";
//		}
		return super.getSoundForStageReachedUpdate(stageId);
	}

	@Override
	protected String getSoundForOneTimeFactorUpdate(EventFactor factor) {
		return null;
	}
	
	protected void sendBountyHunters() {
		// wolfpack
		{
			Random r = Misc.getRandom(random.nextLong(), 7);
			DelayedFleetEncounter e = new DelayedFleetEncounter(r, "TTCRBountyHunterWolfpack");
			e.setDelayVeryShort();
			//e.setDelayNone();
			e.setDoNotAbortWhenPlayerFleetTooStrong(); // small ships, few FP, but a strong fleet
			e.setLocationOuterSector(true, Factions.INDEPENDENT);
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, new Vector2f());
			e.triggerSetFleetMaxShipSize(1);
			e.triggerSetFleetDoctrineOther(5, 4);
			
			
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(r);
			picker.add("tempest_Attack", 10);
			picker.add("tempest_Attack", 10);
			picker.add("tempest_Attack", 10);
			picker.add("scarab_Experimental", 10);
			picker.add("scarab_Experimental", 10);
			picker.add("scarab_Experimental", 10);
			picker.add("hyperion_Attack", 10);
			picker.add("hyperion_Strike", 10);
			picker.add("omen_PD", 5);
			picker.add("omen_PD", 5);
			picker.add("monitor_Escort", 5);
			picker.add("monitor_Escort", 5);
			picker.add("shade_Assault", 5);
			picker.add("shade_Assault", 5);
			picker.add("afflictor_Strike", 5);
			picker.add("afflictor_Strike", 5);
			
//			for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
//				if (spec.hasTag("merc") && spec.hasTag("wolfpack")) {
//					List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(spec.getHullId());
//					for (String variantId : variants) {
//						ShipVariantAPI v = Global.getSettings().getVariant(variantId);
//						if (v.isGoalVariant() && v.isStockVariant()) {
//							picker.add(variantId);
//						}
//					}
//				}
//			}
			
			int add = 9;
			while (!picker.isEmpty() && add > 0) {
				e.triggerAddShips(picker.pickAndRemove());
				add--;
			}
			
			
		
			e.triggerSetFleetMaxNumShips(14);
			e.triggerSetFleetDoctrineComp(5, 0, 0);
		
			e.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			e.triggerFleetAddCommanderSkill(Skills.WOLFPACK_TACTICS, 1);
			e.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
			e.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
			e.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.UNUSUALLY_HIGH);
			
			e.triggerFleetMakeFaster(true, 0, true);
			
			e.triggerSetFleetFaction(Factions.INDEPENDENT);
			e.triggerMakeNoRepImpact();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerMakeFleetIgnoreOtherFleets();
			e.triggerSetFleetGenericHailPermanent("TTCRBountyHunterHail");
			e.triggerSetFleetFlagPermanent("$ttcr_wolfpack");
			e.endCreate();
		}
		
		// phase
		{
			Random r = Misc.getRandom(random.nextLong(), 3);
			DelayedFleetEncounter e = new DelayedFleetEncounter(r, "TTCRBountyHunterPhase");
			e.setDelayVeryShort();
			//e.setDelayNone();
			e.setLocationInnerSector(true, Factions.INDEPENDENT);
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, new Vector2f());
			
			e.triggerSetFleetDoctrineComp(0, 0, 5);
		
			e.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			e.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
			e.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
			e.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
			e.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			
			e.triggerFleetMakeFaster(true, 0, true);
			
			e.triggerSetFleetFaction(Factions.INDEPENDENT);
			e.triggerMakeNoRepImpact();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerMakeFleetIgnoreOtherFleets();
			e.triggerSetFleetGenericHailPermanent("TTCRBountyHunterHail");
			e.triggerSetFleetFlagPermanent("$ttcr_phase");
			e.endCreate();
		}
		
		
		// derelict
		{
			Random r = Misc.getRandom(random.nextLong(), 11);
			DelayedFleetEncounter e = new DelayedFleetEncounter(r, "TTCRBountyHunterDerelict");
			e.setDelayVeryShort();
			//e.setDelayNone();
			//e.setLocationCoreOnly(true, market.getFactionId());
			e.setLocationCoreOnly(true, Factions.INDEPENDENT);
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.VERY_LOW, Factions.LUDDIC_CHURCH, FleetTypes.MERC_BOUNTY_HUNTER, new Vector2f());
			
			//e.triggerSetFleetDoctrineComp(4, 2, 1);
			e.triggerSetFleetDoctrineOther(5, 5);
		
			e.triggerFleetAddCommanderSkill(Skills.DERELICT_CONTINGENT, 1);
			e.triggerFleetAddCommanderSkill(Skills.SUPPORT_DOCTRINE, 1);
			e.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.LOWER);
			
			e.triggerFleetMakeFaster(true, 2, true);
			
			e.triggerSetFleetFaction(Factions.INDEPENDENT);
			e.triggerMakeNoRepImpact();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerMakeFleetIgnoreOtherFleets();
			e.triggerSetFleetGenericHailPermanent("TTCRBountyHunterHail");
			e.triggerSetFleetFlagPermanent("$ttcr_derelict");
			e.endCreate();
		}
		
	}
	
}








