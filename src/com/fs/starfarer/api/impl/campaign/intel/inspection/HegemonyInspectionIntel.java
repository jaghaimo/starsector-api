package com.fs.starfarer.api.impl.campaign.intel.inspection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidAssignmentAI;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidDelegate;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HegemonyInspectionIntel extends RaidIntel implements RaidDelegate {

	//public static float DEFAULT_INSPECTION_GROUND_STRENGTH = 1000;
	
	public static interface InspectionEndedListener {
		public void notifyInspectionEnded(HegemonyInspectionOutcome outcome);
	}
	
	public static enum HegemonyInspectionOutcome {
		COLONY_NO_LONGER_EXISTS,
		TASK_FORCE_DESTROYED, // rep hit due to destruction, depends on transponder status and other factors
		CONFISCATE_CORES, // cores taken, minor rep hit
		FOUND_EVIDENCE_NO_CORES, // player removed cores - lots of disruption
		//CORES_HIDDEN, // player invested into hiding cores - costs credits, no rep hit
		BRIBED, // player paid a lot of bribe money, no rep hit
	}
	
	public static enum AntiInspectionOrders {
		COMPLY,
		HIDE,
		BRIBE,
		RESIST,
	}
	
	public static final String BUTTON_CHANGE_ORDERS = "BUTTON_CHANGE_ORDERS";
	
	public static final Object MADE_HOSTILE_UPDATE = new Object();
	public static final Object ENTERED_SYSTEM_UPDATE = new Object();
	public static final Object OUTCOME_UPDATE = new Object();
	
	
	protected HIActionStage action;
	
	protected AntiInspectionOrders orders = AntiInspectionOrders.COMPLY;
	protected int investedCredits = 0;
	protected MarketAPI target;
	protected FactionAPI targetFaction;
	protected MarketAPI from;
	
	protected List<String> expectedCores = new ArrayList<String>();
	protected boolean enteredSystem = false;
	protected HegemonyInspectionOutcome outcome;
	protected Random random = new Random();
	
	protected InspectionEndedListener listener;
	
	public HegemonyInspectionIntel(MarketAPI from, MarketAPI target, float inspectionFP) {
		super(target.getStarSystem(), from.getFaction(), null);
		this.delegate = this;
		this.from = from;
		this.target = target;
		targetFaction = target.getFaction();
		
		for (Industry curr : target.getIndustries()) {
			String id = curr.getAICoreId();
			if (id != null) {
				expectedCores.add(id);
			}
		}
		PersonAPI admin = target.getAdmin();
		if (admin.isAICore()) {
			expectedCores.add(admin.getAICoreId());
		}
		
		float orgDur = 20f + 10f * (float) Math.random();
		if (Global.getSettings().isDevMode()) {
			orgDur = 1f;
		}
		
		if (DebugFlags.HEGEMONY_INSPECTION_DEBUG || DebugFlags.FAST_RAIDS) orgDur = 0.5f;
		addStage(new HIOrganizeStage(this, from, orgDur));
		
		SectorEntityToken gather = from.getPrimaryEntity();
		SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getPrimaryEntity());
		
		if (gather == null || raidJump == null) {
			endImmediately();
			return;
		}
		
		float successMult = 0.5f;
		
		HIAssembleStage assemble = new HIAssembleStage(this, gather);
		assemble.addSource(from);
		assemble.setSpawnFP(inspectionFP);
		assemble.setAbortFP(inspectionFP * successMult);
		addStage(assemble);
		
		
		
		HITravelStage travel = new HITravelStage(this, gather, raidJump, false);
		travel.setAbortFP(inspectionFP * successMult);
		addStage(travel);
		
		action = new HIActionStage(this, target);
		action.setAbortFP(inspectionFP * successMult);
		addStage(action);
		
		addStage(new HIReturnStage(this));
		
		setImportant(true);
		
		Global.getSector().getIntelManager().addIntel(this);
	}
	
	public InspectionEndedListener getListener() {
		return listener;
	}


	public void setListener(InspectionEndedListener listener) {
		this.listener = listener;
	}



	public Random getRandom() {
		return random;
	}

	public MarketAPI getTarget() {
		return target;
	}

	public MarketAPI getFrom() {
		return from;
	}

	public RouteFleetAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		RaidAssignmentAI raidAI = new RaidAssignmentAI(fleet, route, action);
		//raidAI.setDelegate(action);
		return raidAI;
	}
	
	public AntiInspectionOrders getOrders() {
		return orders;
	}

	public void setOrders(AntiInspectionOrders orders) {
		this.orders = orders;
	}

	public List<String> getExpectedCores() {
		return expectedCores;
	}
	
	public int getInvestedCredits() {
		return investedCredits;
	}

	public void setInvestedCredits(int investedCredits) {
		this.investedCredits = investedCredits;
	}

	public boolean isEnteredSystem() {
		return enteredSystem;
	}

	public void setEnteredSystem(boolean enteredSystem) {
		this.enteredSystem = enteredSystem;
	}

	public HegemonyInspectionOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(HegemonyInspectionOutcome outcome) {
		this.outcome = outcome;
	}

	protected transient String targetOwner = null;
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		if (target != null && targetOwner == null) targetOwner = target.getFactionId();
		if (failStage < 0 && targetOwner != null && target != null && !targetOwner.equals(target.getFactionId())) {
			forceFail(false);
		}
	}




	protected transient ReputationAdjustmentResult repResult = null;
	public void makeHostileAndSendUpdate() {
		boolean hostile = getFaction().isHostileTo(Factions.PLAYER);
		if (!hostile) {
			repResult = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MAKE_HOSTILE_AT_BEST, 
					null, null, null, false, false), 
					Factions.HEGEMONY);
			sendUpdateIfPlayerHasIntel(MADE_HOSTILE_UPDATE, false);
		}
	}
	
	public void sendInSystemUpdate() {
		sendUpdateIfPlayerHasIntel(ENTERED_SYSTEM_UPDATE, false);
	}
	
	public void applyRepPenalty(float delta) {
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = delta;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, null, false, false),
						getFaction().getId());
	}
	
	public void sendOutcomeUpdate() {
		sendUpdateIfPlayerHasIntel(OUTCOME_UPDATE, false);
	}
	
	@Override
	public String getName() {
		String base = "Hegemony AI Inspection";
		if (outcome == HegemonyInspectionOutcome.TASK_FORCE_DESTROYED ||
				outcome == HegemonyInspectionOutcome.COLONY_NO_LONGER_EXISTS) return base + " - Failed";
		if (outcome != null) return base + " - Completed";
		return base;
	}

	
	@Override
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		//super.addBulletPoints(info, mode);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		boolean hostile = getFaction().isHostileTo(Factions.PLAYER);
		if (hostile) {
			orders = AntiInspectionOrders.RESIST;
		}
		
		if (getListInfoParam() == MADE_HOSTILE_UPDATE) {
			FactionAPI other = target.getFaction();
			info.addPara("Target: %s", initPad, tc,
					     other.getBaseUIColor(), target.getName());
			initPad = 0f;
			info.addPara("" + faction.getDisplayName() + " forces arrive in-system and encounter resistance", initPad, tc,
				 	 faction.getBaseUIColor(), faction.getDisplayName());
			initPad = 0f;
			CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
					  				null, null, info, tc, isUpdate, initPad);
			return;
		}
		
		if (getListInfoParam() == ENTERED_SYSTEM_UPDATE) {
			FactionAPI other = target.getFaction();
			info.addPara("Target: %s", initPad, tc,
					     other.getBaseUIColor(), target.getName());
			initPad = 0f;
			info.addPara("Arrived in-system", tc, initPad);
//			info.addPara("" + faction.getDisplayName() + " inspection arrives in-system", initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayName());
			return;
		}
		
		if (getListInfoParam() == OUTCOME_UPDATE) {
			int num = getActionStage().getCoresRemoved().size();
			if (num > 0) {
				String cores = "cores";
				if (num == 1) cores = "core";
				info.addPara("%s AI " + cores + " confiscated", initPad, tc, h, "" + num);
				initPad = 0f;
			}
			if (outcome == HegemonyInspectionOutcome.BRIBED) {
				info.addPara("No AI cores found", initPad, tc, h, "" + num);
			} else if (outcome == HegemonyInspectionOutcome.FOUND_EVIDENCE_NO_CORES) {
				FactionAPI other = target.getFaction();
				info.addPara("Operations at %s disrupted", initPad, tc,
						     other.getBaseUIColor(), target.getName());
				//info.addPara("Operations disrupted by inspection", initPad, h, "" + num);
			} else if (outcome == HegemonyInspectionOutcome.CONFISCATE_CORES) {
			}
			initPad = 0f;
			if (repResult != null) {
				CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
						null, null, info, tc, isUpdate, initPad);
			}
			return;
		}

//		if (getListInfoParam() == UPDATE_FAILED) {
//			FactionAPI other = target.getFaction();
//			info.addPara("Target: %s", initPad, tc,
//					     other.getBaseUIColor(), target.getName());
//			initPad = 0f;
//			info.addPara("Inspection failed", tc, initPad);
//			return;
//		}
		
		float eta = getETA();
		
		FactionAPI other = target.getFaction();
		info.addPara("Target: %s", initPad, tc,
				     other.getBaseUIColor(), target.getName());
		initPad = 0f;
		
		if (eta > 1 && outcome == null) {
			String days = getDaysString(eta);
			info.addPara("Estimated %s " + days + " until arrival", 
					initPad, tc, h, "" + (int)Math.round(eta));
			initPad = 0f;
			
			if (hostile || orders == AntiInspectionOrders.RESIST) {
				info.addPara("Defenders will resist", tc, initPad);
			} else if (orders == AntiInspectionOrders.COMPLY) {
				info.addPara("Defenders will comply", tc, initPad);
			} else if (orders == AntiInspectionOrders.BRIBE) {
				info.addPara("Funds allocated for bribe", tc, initPad);
			}
		} else if (outcome == null && action.getElapsed() > 0) {
			info.addPara("Inspection under way", tc, initPad);
			initPad = 0f;
		} else if (outcome != null) {
			int num = getActionStage().getCoresRemoved().size();
			if (num > 0) {
				String cores = "cores";
				if (num == 1) cores = "core";
				info.addPara("%s AI " + cores + " confiscated", initPad, tc, h, "" + num);
				initPad = 0f;
			} else if (outcome == HegemonyInspectionOutcome.TASK_FORCE_DESTROYED) {
				//info.addPara("Inspection failed", tc, initPad);
			}
//			info.addPara("Inspection under way", tc, initPad);
//			initPad = 0f;
		}
		
		unindent(info);
	}
	
	public HIActionStage getActionStage() {
		for (RaidStage stage : stages) {
			if (stage instanceof HIActionStage) {
				return (HIActionStage) stage;
			}
		}
		return null;
//		return (HIActionStage) stages.get(2);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		super.createIntelInfo(info, mode);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		//super.createSmallDescription(info, width, height);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);
		
		FactionAPI faction = getFaction();
		String has = faction.getDisplayNameHasOrHave();
		String is = faction.getDisplayNameIsOrAre();
		
		//AssembleStage as = getAssembleStage();
		//MarketAPI source = as.getSources().get(0);
		
		String strDesc = getRaidStrDesc();
		int numFleets = (int) getOrigNumFleets();
		String fleets = "fleets";
		if (numFleets == 1) fleets = "fleet";
		
		LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
				" targeting %s for an inspection due to the suspected use of AI cores there." +
				" The task force is projected to be " + strDesc + " and is likely comprised of " +
				"" + numFleets + " " + fleets + ".",
				opad, faction.getBaseUIColor(), target.getName());
		label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), target.getName(), strDesc, "" + numFleets);
		label.setHighlightColors(faction.getBaseUIColor(), target.getFaction().getBaseUIColor(), h, h);
		
		if (outcome == null) {
			addStandardStrengthComparisons(info, target, targetFaction, true, false, "inspection", "inspection's");
		}
		
		info.addSectionHeading("Status", 
				   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
		
		for (RaidStage stage : stages) {
			stage.showStageInfo(info);
			if (getStageIndex(stage) == failStage) break;
		}
		
		
		if (outcome == null) {
			FactionAPI pf = Global.getSector().getPlayerFaction();
			info.addSectionHeading("Your orders", 
					   pf.getBaseUIColor(), pf.getDarkUIColor(), Alignment.MID, opad);
			
			boolean hostile = getFaction().isHostileTo(Factions.PLAYER);
			if (hostile) {
				label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
						" hostile towards " + pf.getDisplayNameWithArticle() + ". Your forces will attempt to resist the inspection.",
						opad);
				label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), 
								   pf.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(faction.getBaseUIColor(), pf.getBaseUIColor());
			} else {
				switch (orders) {
				case COMPLY:
					info.addPara("The authorities at " + target.getName() + " will comply with the inspection. " +
							"It is certain to find any AI cores currently in use.", opad);
					break;
				case BRIBE:
					info.addPara("You've allocated enough funds to ensure the inspection " +
							     "will produce a satisfactory outcome all around.", opad);
					break;
	//			case HIDE:
	//				info.addPara("You've allocated funds to improve AI core concealment measures. It is certain that the inspection " +
	//						"can be defeated, but some amount of suspicion will remain.", opad);
	//				break;
				case RESIST:
					info.addPara("Your space and ground forces will attempt to resist the inspection.", opad);
					break;
				}
				
				if (!enteredSystem) {
					ButtonAPI button = info.addButton("Change orders", BUTTON_CHANGE_ORDERS, 
						  	pf.getBaseUIColor(), pf.getDarkUIColor(),
						  (int)(width), 20f, opad * 2f);
					button.setShortcut(Keyboard.KEY_T, true);
				} else {
					info.addPara("The inspection task force is in-system and there's no time to implement new orders.", opad);
				}
			}
		} else {
			//addBulletPoints(info, ListInfoMode.IN_DESC);
			bullet(info);
			if (repResult != null) {
				CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
						null, null, info, tc, false, opad);
			}
			unindent(info);
		}
	}
	
	

	@Override
	public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant, boolean sendIfHidden) {
		
		if (listInfoParam == UPDATE_RETURNING) {
			// we're using sendOutcomeUpdate() to send an end-of-event update instead
			return;
		}
		
		super.sendUpdateIfPlayerHasIntel(listInfoParam, onlyIfImportant, sendIfHidden);
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		//return super.getIntelTags(map);
		
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MILITARY);
		tags.add(Tags.INTEL_COLONIES);
		tags.add(getFaction().getId());
		return tags;
	}

	
	public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
		if (outcome == null && failStage >= 0) {
			if (!target.isInEconomy() || !target.isPlayerOwned()) {
				outcome = HegemonyInspectionOutcome.COLONY_NO_LONGER_EXISTS;
			} else {
				outcome = HegemonyInspectionOutcome.TASK_FORCE_DESTROYED;
			}
			//sendOutcomeUpdate(); // don't do this - base raid sends an UPDATE_FAILED so we're good already
		}
		if (listener != null && outcome != null) {
			listener.notifyInspectionEnded(outcome);
		}
	}
	
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_CHANGE_ORDERS) {
			ui.showDialog(null, new HIOrdersInteractionDialogPluginImpl(this, ui));
		}
	}

	
	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "hegemony_inspection");
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (target != null && target.isInEconomy() && target.getPrimaryEntity() != null) {
			return target.getPrimaryEntity();
		}
		return super.getMapLocation(map);
	}
}






