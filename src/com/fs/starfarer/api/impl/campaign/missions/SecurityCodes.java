package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.MarineLossesStatModifier;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SecurityCodes extends HubMissionWithBarEvent implements ColonyPlayerHostileActListener,
															    	 MarineLossesStatModifier {

	public static enum Stage {
		ACTIVE,
		COMPLETED,
	}
	
	public static float MARINE_LOSSES_MULT = 0.05f;
	
	public static float PROB_COMPLICATIONS = 0.5f;
	public static float PROB_PATROL_ENCOUNTER_AFTER = 0.5f;
	
	public static float MIN_DAYS = 30f;
	public static float MAX_DAYS = 90f;
	
	public static float BASE_PRICE = 30000;
	
	protected int price;
	protected FactionAPI faction;
	protected int days;
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_AGENT, Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
						 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
			setGiverImportance(pickImportance());
			setGiverFaction(Factions.PIRATES);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$seco_ref")) {
			return false;
		}
		
		requireMarketFactionNotPlayer();
		requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.CUSTOM_DECENTRALIZED);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		
		MarketAPI market = pickMarket();
		if (market == null) return false;
		
		faction = market.getFaction();
		if (faction == null) return false;
		if (!setFactionMissionRef(faction, "$seco_ref")) {
			return false;
		}
		
		price = getRoundNumber((BASE_PRICE * (getQuality() + 0.9f + 0.2f * genRandom.nextFloat())) / getRewardMult());
		
		setStartingStage(Stage.ACTIVE);
		setSuccessStage(Stage.COMPLETED);
		
		setNoAbandon();
		setRepPersonChangesVeryLow();
		setRepFactionChangesTiny();
		
		days = (int)Math.round(MIN_DAYS + (MAX_DAYS - MIN_DAYS) * getQuality());
		setStageOnMemoryFlag(Stage.COMPLETED, faction, "$seco_completed");
		setTimeLimit(Stage.COMPLETED, days, null);
		
		if (rollProbability(PROB_COMPLICATIONS)) {
			triggerComplicationBegin(Stage.ACTIVE, ComplicationSpawn.EXITING_SYSTEM,
					createdAt.getStarSystem(), Factions.PIRATES,
					"the security codes", "they", "the " + faction.getPersonNamePrefix() + " security codes",
					0,
					true, ComplicationRepImpact.NONE, null);
			triggerComplicationEnd(true);
		}
		
		//addTempIntel();
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$seco_barEvent", isBarEvent());
		set("$seco_price", Misc.getWithDGS(price));
		set("$seco_manOrWoman", getPerson().getManOrWoman());
		set("$seco_hisOrHer", getPerson().getHisOrHer());
		set("$seco_heOrShe", getPerson().getHeOrShe());
		set("$seco_days", days);
		set("$seco_faction", faction.getPersonNamePrefix());
		set("$seco_factionColor", faction.getBaseUIColor());
	}
	
	@Override
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.ACTIVE) {
			FactionAPI f = faction;
			String percent = "" + (int) Math.round((1f - MARINE_LOSSES_MULT) * 100f) + "%";
			LabelAPI label = info.addPara("You've acquired ground-forces security codes for %s." + 
					" Having these allows for much better raid planning " +
							"and will reduce marine casualties by %s.",
					opad, f.getBaseUIColor(),
					f.getDisplayNameWithArticle(), percent);
			label.setHighlight(f.getDisplayNameWithArticleWithoutArticle(), percent);
			label.setHighlightColors(f.getBaseUIColor(), h);
			
			info.addPara("The codes are only good for one operation, and, if not used, " +
						 "will also expire after a time.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.ACTIVE) {
//			info.addPara("Have security codes for " + faction.getDisplayName(), tc, pad);
//			return true;
		}
		return false;
	}	

	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		Global.getSector().getListenerManager().addListener(this);
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		Global.getSector().getListenerManager().removeListener(this);
	}
	
	@Override
	public String getBaseName() {
		return "Security Codes - " + faction.getDisplayName();
	}
	
	public String getPostfixForState() {
		if (isEnding()) {
			return " (Expired)";	
		}
		return "";
	}
	
	@Override
	protected String getMissionTypeNoun() {
		return "Information";
	}
	
	@Override
	protected String getToCompleteText() {
		return "remaining";
	}
	
	
	protected void checkCodesUsed(MarketAPI market) {
		if (market.getFaction() == faction) {
			if (!isEnded() || isEnding()) {
				if (rollProbability(PROB_PATROL_ENCOUNTER_AFTER)) {
					DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
					e.setDelayShort();
					e.setLocationCoreOnly(true, faction.getId());
					e.beginCreate();
					e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, faction.getId(), FleetTypes.PATROL_LARGE, new Vector2f());
					e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
					e.triggerSetPatrol();
					e.triggerSetStandardAggroInterceptFlags();
					e.triggerSetFleetMemoryValue("$seco_marketName", market.getName());
					e.triggerSetFleetGenericHailPermanent("SECOPatrolHail");
					e.endCreate();
				}
			}
			Global.getSector().getListenerManager().removeListener(this);
			endAfterDelay();
		}
	}
	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog,
			MarketAPI market, TempData actionData, Industry industry) {
		checkCodesUsed(market);
	}

	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, 
			MarketAPI market, TempData actionData,
			CargoAPI cargo) {
		checkCodesUsed(market);
	}

	public void reportSaturationBombardmentFinished(
			InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
	}

	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog,
			MarketAPI market, TempData actionData) {
	}

	public void modifyMarineLossesStatPreRaid(MarketAPI market, List<GroundRaidObjectivePlugin> objectives, MutableStat stat) {
		if (market.getFaction() == faction) {
			stat.modifyMult("seco_mult", MARINE_LOSSES_MULT, "Security codes");
		}
	}
}





