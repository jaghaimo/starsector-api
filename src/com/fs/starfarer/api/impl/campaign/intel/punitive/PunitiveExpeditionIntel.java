package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.awt.Color;
import java.util.Random;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExData;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExGoal;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExReason;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExType;
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

public class PunitiveExpeditionIntel extends RaidIntel implements RaidDelegate {

	public static final String BUTTON_AVERT = "BUTTON_CHANGE_ORDERS";
	public static float REP_PENALTY = 0.05f;
	
	public static enum PunExOutcome {
		TASK_FORCE_DEFEATED,
		COLONY_NO_LONGER_EXISTS,
		SUCCESS,
		BOMBARD_FAIL,
		RAID_FAIL,
		AVERTED,
	}
	
	public static final Object ENTERED_SYSTEM_UPDATE = new Object();
	public static final Object OUTCOME_UPDATE = new Object();
	
	protected PEActionStage action;
	protected PunExGoal goal;
	protected MarketAPI target;
	protected MarketAPI from;
	protected PunExOutcome outcome;
	
	protected Random random = new Random();

	protected PunExReason bestReason;
	protected Industry targetIndustry;
	protected FactionAPI targetFaction;
	
	public PunitiveExpeditionIntel(FactionAPI faction, MarketAPI from, MarketAPI target, 
								   float expeditionFP, float organizeDuration,
								   PunExGoal goal, Industry targetIndustry, PunExReason bestReason) {
		super(target.getStarSystem(), faction, null);
		this.goal = goal;
		this.targetIndustry = targetIndustry;
		this.bestReason = bestReason;
		this.delegate = this;
		this.from = from;
		this.target = target;
		targetFaction = target.getFaction();
		
		SectorEntityToken gather = from.getPrimaryEntity();
		SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getPrimaryEntity());
		
		if (gather == null || raidJump == null) {
			endImmediately();
			return;
		}
		
		
		float orgDur = organizeDuration;
		if (DebugFlags.PUNITIVE_EXPEDITION_DEBUG || DebugFlags.FAST_RAIDS) orgDur = 0.5f;
		
		addStage(new PEOrganizeStage(this, from, orgDur));
		
		float successMult = 0.5f;
		PEAssembleStage assemble = new PEAssembleStage(this, gather);
		assemble.addSource(from);
		assemble.setSpawnFP(expeditionFP);
		assemble.setAbortFP(expeditionFP * successMult);
		addStage(assemble);
		
		
		PETravelStage travel = new PETravelStage(this, gather, raidJump, false);
		travel.setAbortFP(expeditionFP * successMult);
		addStage(travel);
		
		action = new PEActionStage(this, target);
		action.setAbortFP(expeditionFP * successMult);
		addStage(action);
		
		addStage(new PEReturnStage(this));
		
		setImportant(true);
		
		//applyRepPenalty();
		Global.getSector().getIntelManager().addIntel(this);
		
		//repResult = null;
	}
	
	protected transient ReputationAdjustmentResult repResult = null;
	public void applyRepPenalty() {
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = -REP_PENALTY;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, null, false, false),
						getFaction().getId());
	}
	
	
	public Random getRandom() {
		return random;
	}

	public MarketAPI getTarget() {
		return target;
	}
	
	public FactionAPI getTargetFaction() {
		return targetFaction;
	}

	public MarketAPI getFrom() {
		return from;
	}

	public RouteFleetAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		RaidAssignmentAI raidAI = new RaidAssignmentAI(fleet, route, action);
		//raidAI.setDelegate(action);
		return raidAI;
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

	public void sendOutcomeUpdate() {
		sendUpdateIfPlayerHasIntel(OUTCOME_UPDATE, false);
	}
	
	public void sendEnteredSystemUpdate() {
		//applyRepPenalty();
		sendUpdateIfPlayerHasIntel(ENTERED_SYSTEM_UPDATE, false);
		//repResult = null;
	}
	
	@Override
	public String getName() {
		String base = Misc.ucFirst(faction.getPersonNamePrefix()) + " Expedition";
		if (isEnding()) {
			if (outcome == PunExOutcome.AVERTED) {
				return base + " - Averted";
			}
			if (isSendingUpdate() && isFailed()) {
				return base + " - Failed";
			}
			if (isSucceeded() || outcome == PunExOutcome.SUCCESS) {
				return base + " - Successful";
			}
			if (outcome == PunExOutcome.RAID_FAIL || 
					outcome == PunExOutcome.BOMBARD_FAIL ||
					outcome == PunExOutcome.COLONY_NO_LONGER_EXISTS ||
					outcome == PunExOutcome.TASK_FORCE_DEFEATED) {
				return base + " - Failed";
			}
		}
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
		
		if (getListInfoParam() == OUTCOME_UPDATE) {
//			if (isSucceeded()) {
//				info.addPara("Succeeded", initPad, tc);
//			} else {
//				
//			}
//			return;
		}
		
		if (getListInfoParam() == ENTERED_SYSTEM_UPDATE) {
			FactionAPI other = target.getFaction();
			info.addPara("Target: %s", initPad, tc,
					     other.getBaseUIColor(), target.getName());
			initPad = 0f;
			info.addPara("Arrived in-system", tc, initPad);
//			info.addPara("" + faction.getDisplayName() + " forces arrive in-system", initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayName());
			if (repResult != null) {
				initPad = 0f;
				CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
					  				null, null, info, tc, isUpdate, initPad);
			}
			return;
		}
		

		FactionAPI other = targetFaction;
		if (outcome != null) {
			if (outcome == PunExOutcome.TASK_FORCE_DEFEATED) {
				info.addPara("Expeditionary force defeated", tc, initPad);
			} else if (outcome == PunExOutcome.COLONY_NO_LONGER_EXISTS) {
				info.addPara("Expedition aborted", tc, initPad);
			} else if (outcome == PunExOutcome.AVERTED) {
				info.addPara("Expedition planning disrupted", initPad, tc, other.getBaseUIColor(), target.getName());
			} else if (outcome == PunExOutcome.BOMBARD_FAIL) {
				info.addPara("Bombardment of %s failed", initPad, tc, other.getBaseUIColor(), target.getName());
			} else if (outcome == PunExOutcome.RAID_FAIL) {
				info.addPara("Raid of %s failed", initPad, tc, other.getBaseUIColor(), target.getName());
			} else if (outcome == PunExOutcome.SUCCESS) {
				if (goal == PunExGoal.BOMBARD) {
					if (!target.isInEconomy()) {
						info.addPara("%s destroyed by bombardment", initPad, tc, other.getBaseUIColor(), target.getName());
					} else {
						info.addPara("Bombardment of %s successful", initPad, tc, other.getBaseUIColor(), target.getName());
					}
				} else if (targetIndustry != null && targetIndustry.getDisruptedDays() >= 2) {
					info.addPara(targetIndustry.getCurrentName() + " disrupted for %s days",
							initPad, tc, h, "" + (int)Math.round(targetIndustry.getDisruptedDays()));
				}
			}
			
			if (repResult != null) {
				initPad = 0f;
				CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
					  				null, null, info, tc, isUpdate, initPad);
			}
			
			return;
		}
		
		info.addPara("Target: %s", initPad, tc,
			     other.getBaseUIColor(), target.getName());
		initPad = 0f;
		
		if (goal == PunExGoal.BOMBARD) {
			String goalStr = "saturation bombardment";
			info.addPara("Goal: %s", initPad, tc, Misc.getNegativeHighlightColor(), goalStr);
		}
		
		float eta = getETA();
		if (eta > 1 && !isEnding()) {
			String days = getDaysString(eta);
			info.addPara("Estimated %s " + days + " until arrival", 
					initPad, tc, h, "" + (int)Math.round(eta));
			initPad = 0f;
		} else if (!isEnding() && action.getElapsed() > 0) {
			info.addPara("Currently in-system", tc, initPad);
			initPad = 0f;
		}
		
		if (repResult != null) {
			initPad = 0f;
			CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null, 
				  				null, null, info, tc, isUpdate, initPad);
		}
		
		unindent(info);
	}
	
	public PEActionStage getActionStage() {
		for (RaidStage stage : stages) {
			if (stage instanceof PEActionStage) {
				return (PEActionStage) stage;
			}
		}
		return null;
		//return (PEActionStage) stages.get(2);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		super.createIntelInfo(info, mode);
	}

	
	public void addInitialDescSection(TooltipMakerAPI info, float initPad) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		FactionAPI faction = getFaction();
		String is = faction.getDisplayNameIsOrAre();
		
		String goalDesc = "";
		String goalHL = "";
		Color goalColor = Misc.getTextColor();
		switch (goal) {
		case RAID_PRODUCTION:
			goalDesc = "disrupting the colony's " + targetIndustry.getCurrentName();
			break;
		case RAID_SPACEPORT:
			goalDesc = "raiding the colony's " + targetIndustry.getCurrentName() + " to disrupt its operations";
			break;
		case BOMBARD:
			goalDesc = "a saturation bombardment of the colony";
			goalHL = "saturation bombardment of the colony";
			goalColor = Misc.getNegativeHighlightColor();
			break;
		}
		
		String strDesc = getRaidStrDesc();
		int numFleets = (int) getOrigNumFleets();
		String fleets = "fleets";
		if (numFleets == 1) fleets = "fleet";
		
		if (outcome == null) {
			LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
					" targeting %s with a " + strDesc + " expeditionary force, projected to be comprised of " + 
					numFleets + " " + fleets + ". " +
					"Its likely goal is " + goalDesc + ".",
					initPad, faction.getBaseUIColor(), target.getName());
			label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), target.getName(), strDesc, "" + numFleets, goalHL);
			label.setHighlightColors(faction.getBaseUIColor(), targetFaction.getBaseUIColor(), h, h, goalColor);	
		} else {
			LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
					" targeting %s with an expeditionary force. " +
					"Its likely goal is " + goalDesc + ".",
					initPad, faction.getBaseUIColor(), target.getName());
			label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), target.getName(), goalHL);
			label.setHighlightColors(faction.getBaseUIColor(), targetFaction.getBaseUIColor(), goalColor);
		}
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
		
		addInitialDescSection(info, opad);
		
		if (bestReason.type == PunExType.ANTI_COMPETITION && bestReason.commodityId != null) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(bestReason.commodityId);
			info.addPara("The primary reason for the expedition is the colony's market share " +
					"in the exports of " + spec.getName() + ".", opad);
		} else if (bestReason.type == PunExType.ANTI_FREE_PORT) {
			info.addPara("The primary reason for the expedition is the colony's \"free port\" " +
					"status, and the concomitant export of illegal goods alongside it being a haven for " +
					"various undesirables.", opad);
		} else if (bestReason.type == PunExType.TERRITORIAL) {
			info.addPara("The primary reason for the expedition is the colony being established in " +
						 "space claimed by " + faction.getDisplayNameWithArticle() + ".", opad);
		}
		
		if (outcome == null) {
			addStandardStrengthComparisons(info, target, targetFaction, goal != PunExGoal.BOMBARD, goal == PunExGoal.BOMBARD,
										   "expedition", "expedition's");
		}
		
		info.addSectionHeading("Status", 
				   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
		
		for (RaidStage stage : stages) {
			stage.showStageInfo(info);
			if (getStageIndex(stage) == failStage) break;
		}
		
		if (getCurrentStage() == 0 && !isFailed()) {
			FactionAPI pf = Global.getSector().getPlayerFaction();
			ButtonAPI button = info.addButton("Avert", BUTTON_AVERT, 
				  	pf.getBaseUIColor(), pf.getDarkUIColor(),
				  (int)(width), 20f, opad * 2f);
			button.setShortcut(Keyboard.KEY_T, true);
		}
		
	
		if (!from.getFaction().isHostileTo(targetFaction) && !isFailed()) {
//			LabelAPI label = info.addPara("This operation is being carried " +
//					"without an open declaration of war. Fighting the " +
//					"expeditionary force will not result in " + faction.getDisplayNameWithArticle() + 
//					" immediately becoming hostile, unless the relationship is already strained.", Misc.getGrayColor(), 
//					opad);
//			LabelAPI label = info.addPara("This operation is being carried " +
//					"without an open declaration of war. Fighting the " +
//					"expeditionary force will not result in reputation changes with " + faction.getDisplayNameWithArticle() + 
//					".", Misc.getGrayColor(), 
//					opad);
			LabelAPI label = info.addPara("This operation is being carried " +
					"without an open declaration of war. Defeating the " +
					"expeditionary force will only result in a small reputation reduction with " + faction.getDisplayNameWithArticle() + 
					".", Misc.getGrayColor(), 
					opad);
			label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle());
			label.setHighlightColors(faction.getBaseUIColor());
		}
	}
	
	

	@Override
	public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant, boolean sendIfHidden) {
		
		if (listInfoParam == UPDATE_RETURNING) {
			// we're using sendOutcomeUpdate() to send an end-of-event update instead
			return;
		}
		
		if (listInfoParam == UPDATE_FAILED) {
			applyRepPenalty();
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
				outcome = PunExOutcome.COLONY_NO_LONGER_EXISTS;
			} else {
				outcome = PunExOutcome.TASK_FORCE_DEFEATED;
			}
		}
		
		PunExData data = PunitiveExpeditionManager.getInstance().getDataFor(faction);
		if (data != null) {
			if (outcome == PunExOutcome.SUCCESS) {
				data.numSuccesses++;
			}
		}
	}
	
	
	@Override
	public String getIcon() {
		return faction.getCrest();
	}

	public PunExGoal getGoal() {
		return goal;
	}

	public Industry getTargetIndustry() {
		return targetIndustry;
	}

	public PunExOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(PunExOutcome outcome) {
		this.outcome = outcome;
	}
	
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		MarketAPI market = route.getMarket();
		CampaignFleetAPI fleet = createFleet(market.getFactionId(), route, market, null, random);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		//fleet.addEventListener(this);
		
		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().x);
		
		fleet.addScript(createAssignmentAI(fleet, route));
		
		return fleet;
	}
	
	public CampaignFleetAPI createFleet(String factionId, RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
		if (random == null) random = new Random();
		
		OptionalFleetData extra = route.getExtra();

		float combat = extra.fp;
		float tanker = extra.fp * (0.1f + random.nextFloat() * 0.05f);
		float transport = extra.fp * (0.1f + random.nextFloat() * 0.05f);
		float freighter = 0f;
		
		if (goal == PunExGoal.BOMBARD) {
			tanker += transport;
		} else {
			transport += tanker / 2f;
			tanker *= 0.5f;
		}
		
		combat -= tanker;
		combat -= transport;
		
		
		FleetParamsV3 params = new FleetParamsV3(
				market, 
				locInHyper,
				factionId,
				route == null ? null : route.getQualityOverride(),
				extra.fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod, won't get used since routes mostly have quality override set
				);
		//params.ignoreMarketFleetSizeMult = true; // already accounted for in extra.fp
		
		if (route != null) {
			params.timestamp = route.getTimestamp();
		}
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_WAR_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
		
		if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		}
		
		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		
		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);
		
		Misc.makeNoRepImpact(fleet, "punex");
		Misc.makeHostile(fleet);
		
		return fleet;
	}
	
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_AVERT) {
			ui.showDialog(null, new PEAvertInteractionDialogPluginImpl(this, ui));
		}
	}

	public PunExReason getBestReason() {
		return bestReason;
	}

	public boolean isTerritorial() {
		return bestReason != null && bestReason.type == PunExType.TERRITORIAL;
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (target != null && target.isInEconomy() && target.getPrimaryEntity() != null) {
			return target.getPrimaryEntity();
		}
		return super.getMapLocation(map);
	}
	
//	@Override
//	public List<ArrowData> getArrowData(SectorMapAPI map) {
//		
//	}
}






