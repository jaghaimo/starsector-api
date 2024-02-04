package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Commission;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class FactionCommissionIntel extends BaseMissionIntel implements EveryFrameScript, 
											FleetEventListener, EconomyTickListener {
	public static Logger log = Global.getLogger(FactionCommissionIntel.class);
	
	public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";
	
	public static class CommissionBountyResult extends MissionResult {
		public float fraction;
		public CommissionBountyResult(int payment, float fraction, ReputationAdjustmentResult rep) {
			super(payment, rep);
			this.fraction = fraction;
		}
	}
	
	public static class RepChangeData {
		public FactionAPI faction;
		public float delta;
	}
	
	
	protected float baseBounty = 0;
	protected FactionAPI faction = null;
	//protected FactionCommissionPlugin plugin = null;
	
	protected CommissionBountyResult latestResult;
	
	protected LinkedHashMap<String, RepChangeData> repChanges = new LinkedHashMap<String, RepChangeData>();
	
	public FactionCommissionIntel(FactionAPI faction) {
		this.faction = faction;
		baseBounty = Global.getSettings().getFloat("factionCommissionBounty");
	}
	
	protected Object readResolve() {
		baseBounty = Global.getSettings().getFloat("factionCommissionBounty");
		return this;
	}
	
	@Override
	public void advanceMission(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);

		RepLevel level = faction.getRelToPlayer().getLevel();
		if (!level.isAtWorst(RepLevel.NEUTRAL)) {
			setMissionResult(new MissionResult(-1, null));
			setMissionState(MissionState.COMPLETED);
			endMission();
			sendUpdateIfPlayerHasIntel(missionResult, false);
		} else {
			makeRepChanges(null);
		}
	}
	
	
	@Override
	public void missionAccepted() {
		log.info(String.format("Accepted commission with [%s]", faction.getDisplayName(), (int) baseBounty));
		
		setImportant(true);
		setMissionState(MissionState.ACCEPTED);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().addScript(this);
		
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.FCM_FACTION, faction.getId());
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.FCM_EVENT, this);
	}
	
	@Override
	public void endMission() {
		endMission(null);
	}
	public void endMission(InteractionDialogAPI dialog) {
		log.info(String.format("Ending commission with [%s]", faction.getDisplayName()));
		Global.getSector().getListenerManager().removeListener(this);
		Global.getSector().removeScript(this);
		
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(MemFlags.FCM_FACTION);
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(MemFlags.FCM_EVENT);
		
		undoAllRepChanges(dialog);
		
		//endAfterDelay();
		endImmediately();
		
		ListenerUtil.reportCommissionEnded(this);
	}
	
	public void makeRepChanges(InteractionDialogAPI dialog) {	
		FactionAPI player = Global.getSector().getPlayerFaction();
		for (FactionAPI other : getRelevantFactions()) {
			RepChangeData change = repChanges.get(other.getId());

			boolean madeHostile = change != null;
			boolean factionHostile = faction.isHostileTo(other);
			boolean playerHostile = player.isHostileTo(other); 
			
			if (factionHostile && !playerHostile && !madeHostile) {
				makeHostile(other, dialog);
			}
			
			if (!factionHostile && madeHostile) {
				undoRepChange(other, dialog);
			}
		}
	}
	
	public void makeHostile(FactionAPI other, InteractionDialogAPI dialog) {
		ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.MAKE_HOSTILE_AT_BEST, 
				null, null, dialog != null ? dialog.getTextPanel() : null, false, true), 
				other.getId());
		
		RepChangeData data = new RepChangeData();
		data.faction = other;
		data.delta = rep.delta;
		repChanges.put(other.getId(), data);
	}
	
	public void undoRepChange(FactionAPI other, InteractionDialogAPI dialog) {
		String id = other.getId();
		RepChangeData change = repChanges.get(id);
		
		if (change == null) return;
		
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = -change.delta;
		impact.delta = Math.max(0f, impact.delta - Global.getSettings().getFloat("factionCommissionRestoredRelationshipPenalty"));
		if (impact.delta > 0) {
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, 
							impact, null, dialog != null ? dialog.getTextPanel() : null, false, true), 
							id);
		}
		repChanges.remove(id);
	}
	
	public void undoAllRepChanges(InteractionDialogAPI dialog) {
		for (RepChangeData data : new ArrayList<RepChangeData>(repChanges.values())) {
			undoRepChange(data.faction, dialog);
		}
	}
	
	
	public List<FactionAPI> getRelevantFactions() {
		Set<FactionAPI> factions = new LinkedHashSet<FactionAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			FactionAPI curr = market.getFaction();
			if (factions.contains(curr)) continue;
			
			if (curr.isShowInIntelTab()) {
				factions.add(curr);
			}
		}
	
		return new ArrayList<FactionAPI>(factions);
	}
	public List<FactionAPI> getHostileFactions() {
		FactionAPI player = Global.getSector().getPlayerFaction();
		List<FactionAPI> hostile = new ArrayList<FactionAPI>();
		for (FactionAPI other : getRelevantFactions()) {
			if (this.faction.isHostileTo(other)) {
				hostile.add(other);
			}
		}
		return hostile;
	}
	

	public float computeStipend() {
		float level = Global.getSector().getPlayerPerson().getStats().getLevel();
		
		return Global.getSettings().getFloat("factionCommissionStipendBase") +
			   Global.getSettings().getFloat("factionCommissionStipendPerLevel") * level;
	}
	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;
		
		if (!battle.isPlayerInvolved()) return;
		
		int payment = 0;
		float fpDestroyed = 0;
		for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			if (!faction.isHostileTo(otherFleet.getFaction())) continue;
			
			float bounty = 0;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
				float mult = Misc.getSizeNum(loss.getHullSpec().getHullSize());
				bounty += mult * baseBounty;
				fpDestroyed += loss.getFleetPointCost();
			}
			
			payment += (int) (bounty * battle.getPlayerInvolvementFraction());
		}
	
		if (payment > 0) {
			Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
			
			float repFP = (int)(fpDestroyed * battle.getPlayerInvolvementFraction());
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.COMMISSION_BOUNTY_REWARD, new Float(repFP), null, null, true, false), 
							faction.getId());
			latestResult = new CommissionBountyResult(payment, battle.getPlayerInvolvementFraction(), rep);
			sendUpdateIfPlayerHasIntel(latestResult, false);
		}
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
		
		if (getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
			return;
		}
		
		if (missionResult != null && missionResult.payment < 0) {
//			info.addPara("Annulled by " + faction.getDisplayNameWithArticle(), initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		} else if (isUpdate && latestResult != null) {
			info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h, 
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			CoreReputationPlugin.addAdjustmentMessage(latestResult.rep1.delta, faction, null, 
													  null, null, info, tc, isUpdate, 0f);
		} else if (mode == ListInfoMode.IN_DESC) {
			info.addPara("%s base bounty per hostile frigate", initPad, tc, h, Misc.getDGSCredits(baseBounty));
			info.addPara("%s monthly stipend", 0f, tc, h, Misc.getDGSCredits(computeStipend()));
		} else {
//			info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayName());
//			initPad = 0f;
			info.addPara("%s base reward per frigate", initPad, tc, h, Misc.getDGSCredits(baseBounty));
			info.addPara("%s monthly stipend", 0f, tc, h, Misc.getDGSCredits(computeStipend()));
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
		return "Commission";
	}
	
	public String getName() {
		String prefix = Misc.ucFirst(faction.getPersonNamePrefix()) + " Commission";
//		if (plugin != null) {
//			String override = plugin.getNameOverride();
//			if (override != null) {
//				prefix = override;
//			}
//		}
		if (isEnding()) {
			if (missionResult != null && missionResult.payment < 0) {
				if (isSendingUpdate()) {
					return prefix + " - Annulled";
				}
				return prefix + " - Annulled";
				//return prefix + " (Annulled)";
			}
			//return prefix + " (Resigned)";
			return prefix + " - Resigned";
		}
		if (isSendingUpdate() && getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
			//return prefix + " Accepted";
			return prefix + " - Accepted";
		}
		return prefix;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}
	
	public FactionAPI getFaction() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		createSmallDescription(info, width, height, false);
	}
	public void createSmallDescription(TooltipMakerAPI info, float width, float height, 
									   boolean forMarketConditionTooltip) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(faction.getLogo(), width, 128, opad);
		
		
		if (isEnding()) {
			if (missionResult != null && missionResult.payment < 0) {
				info.addPara("Your commission was annulled by " + faction.getDisplayNameWithArticle() + 
						" due to your standing falling too low.",
						 opad, faction.getBaseUIColor(),
						 faction.getDisplayNameWithArticleWithoutArticle());
				
				CoreReputationPlugin.addRequiredStanding(faction, Commission.COMMISSION_REQ, null, null, info, tc, opad, true);
				CoreReputationPlugin.addCurrentStanding(faction, null, null, info, tc, opad);
			} else {
				info.addPara("You've resigned your commission with " + faction.getDisplayNameWithArticle() + 
						".",
						 opad, faction.getBaseUIColor(),
						 faction.getDisplayNameWithArticleWithoutArticle());
			}
		} else {
			info.addPara("You've accepted a %s commission.",
					opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
			
			info.addPara("The combat bounty payment depends on the number and size of ships destroyed.", opad);
		}
		
		if (latestResult != null) {
			//Color color = faction.getBaseUIColor();
			//Color dark = faction.getDarkUIColor();
			//info.addSectionHeading("Most Recent Reward", color, dark, Alignment.MID, opad);
			info.addPara("Most recent bounty:", opad);
			bullet(info);
			info.addPara("%s received", opad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h, 
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			CoreReputationPlugin.addAdjustmentMessage(latestResult.rep1.delta, faction, null, 
													  null, null, info, tc, false, 0f);
			unindent(info);
		}

		if (!isEnding() && !isEnded()) {
			boolean plMember = PerseanLeagueMembership.isLeagueMember();
			if (!plMember) {
				addAbandonButton(info, width, "Resign commission");
			} else {
				info.addPara("You can not resign your commission while polities under your "
						+ "control are members of the League.", opad);
			}
		}
	}
	
	public String getIcon() {
		return faction.getCrest();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.remove(Tags.INTEL_ACCEPTED);
		tags.remove(Tags.INTEL_MISSIONS);
		//tags.add(Tags.INTEL_COMMISSION);
		tags.add(Tags.INTEL_AGREEMENTS);
		tags.add(faction.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return null;
	}

	@Override
	protected MissionResult createAbandonedResult(boolean withPenalty) {
		return createResignedCommissionResult(true, false, null);
	}
	
	public MissionResult createResignedCommissionResult(boolean withPenalty, boolean inPerson, InteractionDialogAPI dialog) {
		if (withPenalty) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = -1f * Global.getSettings().getFloat("factionCommissionResignPenalty");
			if (inPerson) {
				impact.delta = -1f * Global.getSettings().getFloat("factionCommissionResignPenaltyInPerson");
			}
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, 
							impact, null, dialog != null ? dialog.getTextPanel() : null, false, true), 
							faction.getId());
			return new MissionResult();
		}
		return new MissionResult();
	}

	@Override
	protected MissionResult createTimeRanOutFailedResult() {
		return new MissionResult();
	}

	@Override
	protected String getMissionTypeNoun() {
		return "commission";
	}

	@Override
	protected float getNoPenaltyAbandonDays() {
		return 0f;
	}



	public void reportEconomyMonthEnd() {
//		if (plugin != null) {
//			plugin.reportEconomyMonthEnd();
//		}
	}

	public void reportEconomyTick(int iterIndex) {
		
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;
		
		//CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		MonthlyReport report = SharedData.getData().getCurrentReport();
		
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		fleetNode.name = "Fleet";
		fleetNode.custom = MonthlyReport.FLEET;
		fleetNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		float stipend = computeStipend();
		FDNode stipendNode = report.getNode(fleetNode, "node_id_stipend_" + faction.getId());
		stipendNode.income += stipend * f;
		
		if (stipendNode.name == null) {
			stipendNode.name = faction.getDisplayName() + " Commission";
			stipendNode.icon = faction.getCrest();
			stipendNode.tooltipCreator = new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 450;
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					tooltip.addPara("Your monthly stipend for holding a " + faction.getDisplayName() + " commission", 0f);
				}
			};
		}
		
//		if (plugin != null) {
//			plugin.reportEconomyTick(iterIndex);
//		}
	}

//	public FactionCommissionPlugin getPlugin() {
//		return plugin;
//	}
//
//	public void setPlugin(FactionCommissionPlugin plugin) {
//		this.plugin = plugin;
//	}
	
	
}






