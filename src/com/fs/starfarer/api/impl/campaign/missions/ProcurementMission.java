package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ProcurementMission extends HubMissionWithBarEvent {

	public static float PROB_COMPLICATIONS = 0.5f;
	
	public static float MISSION_DAYS = 60f;
	
	public static float MIN_BASE_VALUE = 10000;
	public static float MAX_BASE_VALUE = 100000;
	public static float BASE_PRICE_MULT = 1.5f;
	
	public static float PROB_REMOTE = 0.5f;
	
	public static float PROB_BAR_UNDERWORLD = 0.25f;
	public static float PROB_ILLEGAL_IF_UNDERWORLD = 0.5f;
	public static float ILLEGAL_QUANTITY_MULT = 0.5f;
	
	
	public static enum Stage {
		TALK_TO_PERSON,
		COMPLETED,
		FAILED,
	}
	public static enum Variation {
		LOCAL,
		REMOTE,
	}
	
	protected String commodityId;
	protected int quantity;
	protected int pricePerUnit;
	
	protected Variation variation;
	protected MarketAPI deliveryMarket;
	protected PersonAPI deliveryContact;
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		if (barEvent) {
			if (rollProbability(PROB_BAR_UNDERWORLD)) {
				setGiverRank(Ranks.CITIZEN);
				setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
							 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
				setGiverImportance(pickImportance());
				setGiverTags(Tags.CONTACT_UNDERWORLD);
				setGiverFaction(Factions.PIRATES);
			} else {
				setGiverRank(Ranks.CITIZEN);
				String post = pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
				 		 			  Ranks.POST_MERCHANT, Ranks.POST_INVESTOR, 
				 		 			  Ranks.POST_EXECUTIVE, Ranks.POST_SENIOR_EXECUTIVE, 
				 		 			  Ranks.POST_PORTMASTER);
				setGiverPost(post);
				if (post.equals(Ranks.POST_SENIOR_EXECUTIVE)) {
					setGiverImportance(pickHighImportance());
				} else {
					setGiverImportance(pickImportance());
				}
				setGiverTags(Tags.CONTACT_TRADE);
			}
			findOrCreateGiver(createdAt, false, false);
		}
		
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		
		if (!setPersonMissionRef(person, "$proCom_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		PersonImportance importance = person.getImportance();
		boolean canOfferRemote = importance.ordinal() >= PersonImportance.MEDIUM.ordinal();
		boolean preferExpensive = getQuality() >= PersonImportance.HIGH.getValue();
		variation = Variation.LOCAL;
		if (canOfferRemote && rollProbability(PROB_REMOTE)) {
			variation = Variation.REMOTE;
		}
		if (CheapCommodityMission.SAME_CONTACT_DEBUG) {
			variation = Variation.REMOTE;
		}

		CommodityOnMarketAPI com = null;
		if (variation == Variation.LOCAL) {
			requireMarketIs(market);
			requireCommodityIsNotPersonnel();
			if (person.hasTag(Tags.CONTACT_UNDERWORLD) && rollProbability(PROB_ILLEGAL_IF_UNDERWORLD)) {
				preferCommodityIllegal();
			} else {
				requireCommodityLegal();
				requireCommodityDemandAtLeast(1);
			}
			requireCommoditySurplusAtMost(0);
			requireCommodityDeficitAtLeast(1);
			if (preferExpensive) {
				preferCommodityTags(ReqMode.ALL, Commodities.TAG_EXPENSIVE);
			}
			com = pickCommodity();
		} 
		
		if (com == null && canOfferRemote) {
			variation = Variation.REMOTE;
		}
		
		if (variation == Variation.REMOTE) {
			if (CheapCommodityMission.SAME_CONTACT_DEBUG) {
				requireMarketIs("jangala");
			} else {
				requireMarketIsNot(market);
			}
			requireMarketFaction(market.getFactionId());
			requireMarketNotHidden();
			requireCommodityIsNotPersonnel();
			if (person.hasTag(Tags.CONTACT_UNDERWORLD) && rollProbability(PROB_ILLEGAL_IF_UNDERWORLD)) {
				preferCommodityIllegal();
			} else {
				requireCommodityLegal();
				requireCommodityDemandAtLeast(1);
			}
			requireCommoditySurplusAtMost(0);
			requireCommodityDeficitAtLeast(1);
			if (preferExpensive) {
				preferCommodityTags(ReqMode.ALL, Commodities.TAG_EXPENSIVE);
			}
			com = pickCommodity();
		}
		
		if (com == null) return false;
		
		deliveryMarket = com.getMarket();
		
		commodityId = com.getId();
		
		float value = MIN_BASE_VALUE + (MAX_BASE_VALUE - MIN_BASE_VALUE) * getQuality();
		quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
		if (com.isIllegal()) {
			quantity *= ILLEGAL_QUANTITY_MULT;
		}
		
		if (quantity < 10) quantity = 10;
		pricePerUnit = (int) (com.getMarket().getSupplyPrice(com.getId(), quantity, true) / (float) quantity * 
							  BASE_PRICE_MULT / getRewardMult());
		pricePerUnit = getRoundNumber(pricePerUnit);
		if (pricePerUnit < 2) pricePerUnit = 2;
		
		
		if (variation == Variation.REMOTE) {
			if (com.isIllegal()) {
				deliveryContact = findOrCreateCriminal(deliveryMarket, true);
			} else {
				deliveryContact = findOrCreateTrader(deliveryMarket.getFactionId(), deliveryMarket, true);
			}
		} else {
			deliveryContact = person;
		}
		ensurePersonIsInCommDirectory(deliveryMarket, deliveryContact);
		//setPersonIsPotentialContactOnSuccess(deliveryContact);
		
		if (deliveryContact == null ||
				(variation == Variation.REMOTE && !setPersonMissionRef(deliveryContact, "$proCom_ref"))) {
			return false;
		}
		setPersonDoGenericPortAuthorityCheck(deliveryContact);
		makeImportant(deliveryContact, "$proCom_needsCommodity", Stage.TALK_TO_PERSON);
		
		setStartingStage(Stage.TALK_TO_PERSON);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, deliveryContact, "$proCom_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		
		if (getQuality() < 0.5f) {
			setRepFactionChangesVeryLow();
		} else {
			setRepFactionChangesLow();
		}
		setRepPersonChangesMedium();
		
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$proCom_barEvent", isBarEvent());
		
		set("$proCom_commodityId", commodityId);
		set("$proCom_underworld", getPerson().hasTag(Tags.CONTACT_UNDERWORLD));
		set("$proCom_playerHasEnough", playerHasEnough(commodityId, quantity));
		set("$proCom_commodityName", getSpec().getLowerCaseName());
		set("$proCom_quantity", Misc.getWithDGS(quantity));
		set("$proCom_pricePerUnit", Misc.getDGSCredits(pricePerUnit));
		set("$proCom_totalPrice", Misc.getDGSCredits(pricePerUnit * quantity));
		set("$proCom_variation", variation);
		set("$proCom_manOrWoman", getPerson().getManOrWoman());
		//set("$proCom_heOrShe", getPerson().getHeOrShe());
		//set("$proCom_HeOrShe", getPerson().getHeOrShe().substring(0, 1).toUpperCase() + getPerson().getHeOrShe().substring(1));
		
		
		if (variation == Variation.REMOTE) {
			set("$proCom_personName", deliveryContact.getNameString());
			set("$proCom_personPost", deliveryContact.getPost().toLowerCase());
			set("$proCom_PersonPost", Misc.ucFirst(deliveryContact.getPost()));
			set("$proCom_marketName", deliveryMarket.getName());
			set("$proCom_marketOnOrAt", deliveryMarket.getOnOrAt());
			set("$proCom_dist", getDistanceLY(deliveryMarket));
		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_PERSON) {
			TooltipMakerAPI text = info.beginImageWithText(deliveryContact.getPortraitSprite(), 48f);
			text.addPara("Deliver %s units of " + getSpec().getLowerCaseName() + " to " + deliveryContact.getNameString() + " " +
					deliveryMarket.getOnOrAt() + " " + deliveryMarket.getName() + ". You will be paid %s per unit, or " + 
					"%s total.", 0f, h,
					Misc.getWithDGS(quantity),
					Misc.getDGSCredits(pricePerUnit), 
					Misc.getDGSCredits(pricePerUnit * quantity));
			info.addImageWithText(opad);
			if (playerHasEnough(commodityId, quantity)) {
				info.addPara("You have enough " + getSpec().getLowerCaseName() + " in your cargo holds to complete " +
						"the delivery.", opad);
			} else {
				info.addPara("You do not have enough " + getSpec().getLowerCaseName() + " in your cargo holds to complete " +
						"the delivery.", opad);
			}
		}
	}

//	need to mention that need to acquire the item
//	check whether player has enough and chance desc/bullet point depending?
	
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_PERSON) {
			if (playerHasEnough(commodityId, quantity)) {
				info.addPara("Go to " + deliveryMarket.getName() + " and contact " + deliveryContact.getNameString() + " to arrange delivery", tc, pad);
			} else {
				String name = getSpec().getLowerCaseName();
				info.addPara("Acquire %s units of " + name, pad, tc, h, "" + (int) quantity);				
			}
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return getSpec().getName() + " Procurement";
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.accept(dialog, memoryMap);
		
		if (variation == Variation.REMOTE && rollProbability(PROB_COMPLICATIONS)) {
			DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
			e.setDelay(10f);
			e.setLocationCoreOnly(true, Factions.PIRATES);
			e.setEncounterInHyper();
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, new Vector2f());
			e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
			e.triggerSetStandardAggroPirateFlags();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerSetFleetMemoryValue("$proCom_commodityName", getSpec().getLowerCaseName());
			e.triggerSetFleetGenericHailPermanent("PROCOMPirateHail");
			e.endCreate();
		}
	}
	
	
	
	protected transient CommoditySpecAPI spec;
	protected CommoditySpecAPI getSpec() {
		if (spec == null) {
			spec = Global.getSettings().getCommoditySpec(commodityId);
		}
		return spec;
	}
}

