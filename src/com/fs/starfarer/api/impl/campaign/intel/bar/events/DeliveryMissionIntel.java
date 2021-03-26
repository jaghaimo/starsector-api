package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class DeliveryMissionIntel extends BaseMissionIntel {
	public static float PROB_PIRATE_ENCOUNTER = 0.5f;
	
	public static float PROB_CONSEQUENCES = 0.75f;
	public static float PROB_CONSEQUENCES_ESCROW = 0.25f;
	
	public static Logger log = Global.getLogger(DeliveryMissionIntel.class);
	
	protected DeliveryBarEvent event;
	
	public DeliveryMissionIntel(DeliveryBarEvent event, InteractionDialogAPI dialog) {
		this.event = event;
		
		setDuration(event.getDuration());
		
		Global.getSector().getIntelManager().addIntel(this, false, dialog == null ? null : dialog.getTextPanel());
		
		setImportant(true);
		setMissionState(MissionState.ACCEPTED);
		missionAccepted();
		Global.getSector().addScript(this);

		// doesn't quite connect up as far as easily making the mission fail etc
//		if ((float) Math.random() < PROB_PIRATE_ENCOUNTER) {
//			Random random = new Random();
//			String id = "dmi_" + Misc.genUID();
//			
//			float reward = event.getReward();
//			FleetSize size = FleetSize.LARGE;
//			String type = FleetTypes.PATROL_LARGE;
//			String repLoss = DelayedFleetEncounter.TRIGGER_REP_LOSS_HIGH;
//			if (reward <= 20000) {
//				size = FleetSize.SMALL;
//				type = FleetTypes.PATROL_SMALL;
//				repLoss = DelayedFleetEncounter.TRIGGER_REP_LOSS_MINOR;
//			} else if (reward <= 50000) {
//				size = FleetSize.MEDIUM;
//				type = FleetTypes.PATROL_MEDIUM;
//				repLoss = DelayedFleetEncounter.TRIGGER_REP_LOSS_MEDIUM;
//			}
//			
//			
//			String comName = getCommodity().getCommodity().getLowerCaseName();
//			
//			DelayedFleetEncounter e = new DelayedFleetEncounter(random, id);
//			e.setDelayNone();
//			e.setLocationInnerSector(false, null);
//			e.beginCreate();
//			e.triggerCreateFleet(size, FleetQuality.DEFAULT, Factions.PIRATES, type, new Vector2f());
//			e.setFleetWantsThing(Factions.PIRATES, 
//					"the " + comName + " shipment", "it",
//					"the " + comName + " shipment you're running for " + event.getPerson().getNameString(),
//					0,
//					true, ComplicationRepImpact.FULL,
//					repLoss, event.getPerson());
//			e.triggerSetFleetMemoryValue("$dmi_commodity", event.getCommodityId());
//			e.triggerSetFleetMemoryValue("$dmi_quantity", event.getQuantity());
//			e.triggerSetStandardAggroInterceptFlags();
//			e.triggerMakeLowRepImpact();
//			e.endCreate();
//		}
	}
	
	public void missionAccepted() {
		Misc.makeImportant(event.getDestination().getPrimaryEntity(), "deliveryEvent");
	}
	
	public DeliveryBarEvent getEvent() {
		return event;
	}

	@Override
	public void advanceMission(float amount) {
		if (!event.getDestination().isInEconomy()) {
			setMissionResult(new MissionResult(0, null, null));
			setMissionState(MissionState.FAILED);
			endMission();
		}
	}

	
	public void performDelivery(InteractionDialogAPI dialog) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		cargo.removeItems(CargoItemType.RESOURCES, event.getCommodityId(), event.getQuantity());
		cargo.getCredits().add(event.getReward() + event.getEscrow());
		applyTradeValueImpact(event.getReward());
		AddRemoveCommodity.addCommodityLossText(event.getCommodityId(), event.getQuantity(), dialog.getTextPanel());
		if (event.getEscrow() > 0) {
			AddRemoveCommodity.addCreditsGainText(event.getEscrow(), dialog.getTextPanel());
		}
		AddRemoveCommodity.addCreditsGainText(event.getReward(), dialog.getTextPanel());
		
		float repAmount = 0.01f * event.getReward() / 10000f;
		if (repAmount < 0.01f) repAmount = 0.01f;
		if (repAmount > 0.05f) repAmount = 0.05f;
		
		MissionCompletionRep completionRep = new MissionCompletionRep(repAmount, RepLevel.COOPERATIVE, -repAmount, RepLevel.INHOSPITABLE);
		
		ReputationAdjustmentResult rep = null;
		rep = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.MISSION_SUCCESS, completionRep,
						null, dialog.getTextPanel(), true, true), 
						getFactionForUIColors().getId());
		
		setMissionResult(new MissionResult(event.getReward() + event.getEscrow(), rep, null));
		setMissionState(MissionState.COMPLETED);
		endMission();
		
		ContactIntel.addPotentialContact(event.getPerson(), event.getMarket(), dialog.getTextPanel());
	}
	
	public boolean hasEnough() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		return cargo.getCommodityQuantity(event.getCommodityId()) >= event.getQuantity();
	}

	
	protected void applyTradeValueImpact(float totalReward) {
		FactionAPI faction = getFactionForUIColors();
		MarketAPI market = event.getDestination();
		
		boolean illegal = market.isIllegal(event.getCommodityId());
		
		SubmarketAPI submarket = null;
		for (SubmarketAPI curr : market.getSubmarketsCopy()) {
			if (!curr.getPlugin().isParticipatesInEconomy()) continue;
			
			if (illegal && curr.getPlugin().isBlackMarket()) {
				submarket = curr;
				break;
			}
			if (!illegal && curr.getPlugin().isOpenMarket()) {
				submarket = curr;
				break;
			}
		}
		
		if (submarket == null) return;
		
		PlayerTradeDataForSubmarket tradeData = SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, event.getCommodityId(), null);
		stack.setSize(event.getQuantity());
		tradeData.addToTrackedPlayerSold(stack, totalReward);
		
		Misc.affectAvailabilityWithinReason(getCommodity(), event.getQuantity());
	}
	
	
	public void endMission() {
		if (event.getMarket() == null) {
			endAfterDelay();
			return; // to fix crash for saves that are already in a bad state
		}
		
		Misc.makeUnimportant(event.getDestination().getPrimaryEntity(), "deliveryEvent");
		
		if (!event.getMarket().isPlayerOwned()) {
			if (isFailed() || isAbandoned()) {
				Global.getSector().getMemoryWithoutUpdate().set(
						DeliveryBarEvent.KEY_FAILED_RECENTLY, true, 
						DeliveryBarEvent.FAILED_RECENTLY_DURATION * (0.75f + (float) Math.random() * 0.5f));
				
				float p = PROB_CONSEQUENCES;
				if (event.getEscrow() > 0) {
					p = PROB_CONSEQUENCES_ESCROW;
				}
				if ((float) Math.random() < p || DebugFlags.BAR_DEBUG) {
					Global.getSector().addScript(new DeliveryFailureConsequences(this));
				}
			}
		}
		endAfterDelay();
	}
	
	public boolean runWhilePaused() {
		return false;
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		FactionAPI faction = getFactionForUIColors();
		if (isUpdate) {
			// 3 possible updates: de-posted/expired, failed, completed
			if (isFailed() || isCancelled()) {
				return;
			} else if (isCompleted() && missionResult != null) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
				}
				
				if (missionResult.rep1 != null && missionResult.rep1.delta != 0) {
					CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, faction, null, 
														  null, null, info, tc, isUpdate, 0f);
				}
			}
		} else {
			// either in small description, or in tooltip/intel list
			if (missionResult != null) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
					initPad = 0f;
				}
				
				if (missionResult.rep1 != null && missionResult.rep1.delta != 0) {
					CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, faction, null, 
													  null, null, info, tc, isUpdate, initPad);
					initPad = 0f;
				}
			} else {
				if (mode != ListInfoMode.IN_DESC) {
					info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
												 faction.getBaseUIColor(),
												 faction.getDisplayName());
					initPad = 0f;
				}
				
				LabelAPI label = info.addPara("%s units to " + event.getDestination().getName(), 
							 initPad, tc, h, "" + (int) event.getQuantity());
				label.setHighlight("" + event.getQuantity(), event.getDestination().getName());
				label.setHighlightColors(h, event.getDestination().getFaction().getBaseUIColor());
				info.addPara("%s reward", 0f, tc, h, Misc.getDGSCredits(event.getReward()));
				if (event.getEscrow() > 0) {
					info.addPara("%s held in escrow", 0f, tc, h, Misc.getDGSCredits(event.getEscrow()));
				}
				addDays(info, "to complete", duration - elapsedDays, tc, 0f);
			}
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "Delivery";
	}
	
	public String getName() {
		if (isAccepted() || isPosted()) {
			return "Delivery - " + getCommodity().getCommodity().getName();
		}
		
		return "Delivery " + getPostfixForState();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return event.getFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	protected CommodityOnMarketAPI getCommodity() {
		return event.getDestination().getCommodityData(event.getCommodityId());
	}
	

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		FactionAPI faction = getFactionForUIColors();
		
		boolean illegal = event.getDestination().isIllegal(event.getCommodityId());
		
		//info.addImage(commodity.getCommodity().getIconName(), width, 80, opad);
		
		CommodityOnMarketAPI com = getCommodity();
		MarketAPI market = event.getDestination();
		
		info.addImages(width, 80, opad, opad * 2f,
					   com.getCommodity().getIconName(),
					   faction.getCrest(),
					   market.getFaction().getCrest());

		
		String post = "";
		if (Factions.PIRATES.equals(faction.getId())) {
			post = "-affiliated";
		}
		
		//String desc = event.getPersonDesc();
		String start = "You've";
		if (!isPosted() && !isAccepted()) start = "You had";
		
		if (event.getMarket() == null) {
			return;
		}
		LabelAPI label = info.addPara(start + " accepted " + faction.getPersonNamePrefixAOrAn() + " " + 
				faction.getPersonNamePrefix() + post + " contract to deliver a quantity of " +
				com.getCommodity().getLowerCaseName() + 
				" from " + event.getMarket().getName() + " to " + market.getName() + ", " +
						"which is under " + market.getFaction().getPersonNamePrefix() + " control.", opad,
				faction.getBaseUIColor(), faction.getPersonNamePrefix() + post);
		
		label.setHighlight(faction.getPersonNamePrefix() + post, market.getFaction().getPersonNamePrefix());
		label.setHighlightColors(faction.getBaseUIColor(), market.getFaction().getBaseUIColor());
		
		if (isPosted() || isAccepted()) {
			addBulletPoints(info, ListInfoMode.IN_DESC);
			
			info.addPara("To make the delivery, either dock at " + market.getName() + " openly or approach it without " +
						 "attracting the attention of nearby patrols.", opad);
			
//			if (illegal) {
//				info.addPara("The legality of the delivery is at best questionable and " +
//						"it must be made with the transponder turned off.", opad);
//			} else {
//				info.addPara("The contract is above-board and the delivery may be made openly.", opad);
//			}
			
			//addGenericMissionState(info);
			addAcceptOrAbandonButton(info, width);
		} else {
			if (isFailed() && !market.isInEconomy()) {
				info.addPara("You have failed this contract because " + market.getName() + 
						     " no longer exists as a functional polity.", opad);	
			} else {
				addGenericMissionState(info);
			}
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}

	}
	
	public String getIcon() {
		return getCommodity().getCommodity().getIconName();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_TRADE);
		tags.add(getFactionForUIColors().getId());
		return tags;
	}
	

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return event.getDestination().getPrimaryEntity();
	}
	


	@Override
	protected String getMissionTypeNoun() {
		return "contract";
	}
	

	@Override
	protected MissionResult createAbandonedResult(boolean withPenalty) {
		if (withPenalty) {
			float repAmount = 0.01f * event.getReward() / 10000f;
			if (repAmount < 0.01f) repAmount = 0.01f;
			if (repAmount > 0.05f) repAmount = 0.05f;
			
			MissionCompletionRep completionRep = new MissionCompletionRep(repAmount, RepLevel.WELCOMING, -repAmount, RepLevel.INHOSPITABLE);
			
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_FAILURE, completionRep,
										  null, null, true, false),
										  getFactionForUIColors().getId());
			
			return new MissionResult(0, rep, null);
		}
		return new MissionResult();
	}

	@Override
	public boolean canAbandonWithoutPenalty() {
		return false;
	}

	@Override
	protected MissionResult createTimeRanOutFailedResult() {
		return createAbandonedResult(true);
	}
	
	public MarketAPI getDestination() {
		return event.getDestination();
	}
	
}


