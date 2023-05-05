package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AnalyzeEntityMissionIntel extends BaseMissionIntel {
	public static Logger log = Global.getLogger(AnalyzeEntityMissionIntel.class);
	
	protected int reward;
	protected FactionAPI faction;
	protected MarketAPI market;
	
	protected SectorEntityToken entity;

	
	public AnalyzeEntityMissionIntel(SectorEntityToken entity) {
		this.entity = entity;
		
		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getFaction().isPlayerFaction()) continue;
			marketPicker.add(market, market.getSize());
		}
		
		market = marketPicker.pick();
		if (market == null) {
			endImmediately();
			return;
		}
		
		faction = market.getFaction();
		if (!market.getFaction().isHostileTo(Factions.INDEPENDENT) && (float) Math.random() > 0.67f) {
			faction = Global.getSector().getFaction(Factions.INDEPENDENT);
		}
		
		setDuration(120f);
		
		reward = (int) Misc.getDistance(new Vector2f(), entity.getLocationInHyperspace());
		//reward *= 1.25f;
		reward = 20000 + (reward / 10000) * 10000;
		if (reward < 10000) reward = 10000;
		
		
		log.info("Created AnalyzeEntityMissionIntel: " + entity.getName() + ", faction: " + faction.getDisplayName());

		
		initRandomCancel();
		setPostingLocation(market.getPrimaryEntity());
		
		Global.getSector().getIntelManager().queueIntel(this);
		
	}
	
	@Override
	public void notifyPlayerAboutToOpenIntelScreen() {
		if (isPosted() && (!entity.isAlive() || entity.hasTag(Tags.NON_CLICKABLE))) {
			cancel();
		}
	}



	public SectorEntityToken getEntity() {
		return entity;
	}

	@Override
	protected MissionResult createAbandonedResult(boolean withPenalty) {
		if (withPenalty) {
			MissionCompletionRep rep = new MissionCompletionRep(RepRewards.HIGH, RepLevel.WELCOMING,
					 -RepRewards.TINY, RepLevel.INHOSPITABLE);
			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.MISSION_FAILURE, rep,
									  null, null, true, false),
									  faction.getId());
			return new MissionResult(0, result);
		}
		return new MissionResult();
	}

	@Override
	protected MissionResult createTimeRanOutFailedResult() {
		return createAbandonedResult(true);
	}

	@Override
	public void missionAccepted() {
		entity.getMemoryWithoutUpdate().set("$aem_target", true, getDuration());
		entity.getMemoryWithoutUpdate().set("$aem_eventRef", this, getDuration());
		Misc.setFlagWithReason(entity.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
						       "aem", true, getDuration());	
	}
	
	
	@Override
	public void endMission() {
		entity.getMemoryWithoutUpdate().unset("$aem_target");
		entity.getMemoryWithoutUpdate().unset("$aem_eventRef");
		Misc.setFlagWithReason(entity.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
							  "aem", false, 0f);
		endAfterDelay();
	}

	@Override
	public void advanceMission(float amount) {
	}

	@Override
	public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (action.equals("runPackage")) {
			AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
			cargo.getCredits().add(reward);
			
			MissionCompletionRep rep = new MissionCompletionRep(RepRewards.HIGH, RepLevel.WELCOMING,
					 -RepRewards.TINY, RepLevel.INHOSPITABLE);

			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_SUCCESS, rep,
										  null, dialog.getTextPanel(), true, false), 
										  faction.getId());
			setMissionResult(new MissionResult(reward, result));
			setMissionState(MissionState.COMPLETED);
			endMission();
		}
		
		return true;
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
		
		if (isUpdate) {
			// 3 possible updates: de-posted/expired, failed, completed
			if (isFailed() || isCancelled()) {
				return;
			} else if (isCompleted()) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
				}
				CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, faction, null, 
														  null, null, info, tc, isUpdate, 0f);
			}
		} else {
			// either in small description, or in tooltip/intel list
			if (missionResult != null) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
					initPad = 0f;
				}
				
				if (missionResult.rep1 != null) {
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
				
				info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(reward));
				addDays(info, "to complete", duration - elapsedDays, tc, 0f);
			}
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "Analyze";
	}
	
	public String getName() {
		String name = "";
//		if (entity.getCustomEntitySpec() != null) {
//			name = entity.getCustomEntitySpec().getNameInText();
//		} else {
			name = entity.getName(); // we want caps on every word since this is a title, so no getNameInText()
//		}
			
		return "Analyze " + name + getPostfixForState();
	}
	

	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		//Color c = getTitleColor(mode);
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(faction.getLogo(), width, 128, opad);

		String name = "";
		String shortName = "";
		String isOrAre = "is";
		String aOrAn = "a";
		if (entity.getCustomEntitySpec() != null) {
			name = entity.getCustomEntitySpec().getNameInText();
			shortName = entity.getCustomEntitySpec().getShortName();
			isOrAre = entity.getCustomEntitySpec().getIsOrAre();
			aOrAn = entity.getCustomEntitySpec().getAOrAn();
		} else {
			name = entity.getName();
			shortName = entity.getName();
		}
		
		String authorities = "authorities";
		if (!faction.getId().equals(market.getFactionId())) {
			authorities = "concerns";
		}
		info.addPara("%s " + authorities + " " + market.getOnOrAt() + " " + market.getName() + 
				" have posted a reward for running a custom sensor package on " + aOrAn + " " + name + ".",
				opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
		
		String loc = BreadcrumbSpecial.getLocatedString(entity, true);
		info.addPara("The " + shortName + " " + isOrAre + " " + loc + ".", opad);
		
		if (isPosted() || isAccepted()) {
			addBulletPoints(info, ListInfoMode.IN_DESC);
			
			addGenericMissionState(info);
			
			addAcceptOrAbandonButton(info, width, "Accept", "Abandon");
		} else {
			addGenericMissionState(info);
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}

	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("campaignMissions", "analyze_entity");
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(Tags.INTEL_MISSIONS);
		tags.add(faction.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (entity != null && entity.isDiscoverable() && entity.getStarSystem() != null) {
			return entity.getStarSystem().getCenter();
		}
		return entity;
	}

	
	
}
