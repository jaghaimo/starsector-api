package com.fs.starfarer.api.impl.campaign.intel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.SurveyPlanetListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SurveyPlanetMissionIntel extends BaseMissionIntel implements SurveyPlanetListener {
	public static Logger log = Global.getLogger(SurveyPlanetMissionIntel.class);
	
	protected int reward;
	protected FactionAPI faction;
	protected MarketAPI market;
	
	protected PlanetAPI planet;


	
	public SurveyPlanetMissionIntel(PlanetAPI planet) {
		this.planet = planet;
		
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
		
		reward = (int) Misc.getDistance(new Vector2f(), planet.getLocationInHyperspace());
		//reward *= 1.25f;
		reward = 20000 + (reward / 10000) * 10000;
		if (reward < 10000) reward = 10000;
		
		
		log.info("Created SurveyPlanetMissionIntel: " + planet.getName() + ", faction: " + faction.getDisplayName());

		
		initRandomCancel();
		setPostingLocation(market.getPrimaryEntity());
		
		Global.getSector().getIntelManager().queueIntel(this);
		
	}


	public PlanetAPI getPlanet() {
		return planet;
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
		planet.getMemoryWithoutUpdate().set("$spm_target", true, getDuration());
		planet.getMemoryWithoutUpdate().set("$spm_eventRef", this, getDuration());
		Misc.setFlagWithReason(planet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
						       "spm", true, getDuration());	
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	
	@Override
	public void endMission() {
		planet.getMemoryWithoutUpdate().unset("$spm_target");
		planet.getMemoryWithoutUpdate().unset("$spm_eventRef");
		Misc.setFlagWithReason(planet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
							  "spm", false, 0f);
		Global.getSector().getListenerManager().removeListener(this);
		
		endAfterDelay();
	}

	@Override
	public void advanceMission(float amount) {
		if (planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
			reportPlayerSurveyedPlanet(planet);
		}
	}

	@Override
	public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
//		if (action.equals("finishedSurvey")) {
//			AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
//			cargo.getCredits().add(reward);
//			
//			MissionCompletionRep rep = new MissionCompletionRep(RepRewards.HIGH, RepLevel.WELCOMING,
//					 -RepRewards.TINY, RepLevel.INHOSPITABLE);
//
//			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
//					new RepActionEnvelope(RepActions.MISSION_SUCCESS, rep,
//										  null, dialog.getTextPanel(), true, false), 
//										  faction.getId());
//			setMissionResult(new MissionResult(reward, result));
//			setMissionState(MissionState.COMPLETED);
//			endMission();
//		}
		
		return true;
	}
	
	public void reportPlayerSurveyedPlanet(PlanetAPI planet) {
		if (planet != this.planet) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		cargo.getCredits().add(reward);
		
		MissionCompletionRep rep = new MissionCompletionRep(RepRewards.HIGH, RepLevel.WELCOMING,
				 -RepRewards.TINY, RepLevel.INHOSPITABLE);

		ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.MISSION_SUCCESS, rep,
									  null, null, true, false), 
									  faction.getId());
		setMissionResult(new MissionResult(reward, result));
		setMissionState(MissionState.COMPLETED);
		endMission();
		sendUpdateIfPlayerHasIntel(missionResult, false);
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
				float betweenPad = 0f;
				if (mode != ListInfoMode.IN_DESC) {
					info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
												 faction.getBaseUIColor(),
												 faction.getDisplayName());
					initPad = betweenPad;
				} else {
					betweenPad = 0f;
				}
				
				info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(reward));
				addDays(info, "to complete", duration - elapsedDays, tc, betweenPad);
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
		//return "Survey";
		return super.getSortString();
	}
	
	public String getName() {
		//String name = planet.getName();
		String name = planet.getTypeNameWithWorld();
		return "Survey " + name + getPostfixForState();
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
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(faction.getLogo(), width, 128, opad);

		String name = planet.getName();
		
		String authorities = "authorities";
		if (!faction.getId().equals(market.getFactionId())) {
			authorities = "concerns";
		}
		
		info.addPara("%s " + authorities + " " + market.getOnOrAt() + " " + market.getName() + 
				" have posted a reward for completing a full survey of " + name + ", " + 
				planet.getSpec().getAOrAn() + " " + planet.getTypeNameWithWorld().toLowerCase() + ".",
				opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
		
		
		if (isPosted() || isAccepted()) {
			addBulletPoints(info, ListInfoMode.IN_DESC);
			
			info.showFullSurveyReqs(planet, true, opad);
			
//			SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
//			plugin.init(Global.getSector().getPlayerFleet(), planet);
//
//			
//			Map<String, Integer> required = plugin.getRequired();
//			Map<String, Integer> consumed = plugin.getConsumed();
//
//			StatBonus stat = new StatBonus();
//			int id = 0;
//			for (String key : required.keySet()) {
//				CommoditySpecAPI com = Global.getSettings().getCommoditySpec(key);
//				int qty = required.get(key);
//				
//				stat.modifyFlat("" + id++, qty, Misc.ucFirst(com.getLowerCaseName()));
//			}
//			for (String key : consumed.keySet()) {
//				CommoditySpecAPI com = Global.getSettings().getCommoditySpec(key);
//				int qty = consumed.get(key);
//				
//				stat.modifyFlat("" + id++, qty, Misc.ucFirst(com.getLowerCaseName()));
//			}
//			
//			info.addPara("The following resources are required for your fleet to run a full survey of " + name + ":", opad);
//			info.setLowGridRowHeight();
//			info.addStatModGrid(200, 50f, opad, opad, stat, new StatModValueGetter() {
//				public String getPercentValue(StatMod mod) { return null; }
//				public String getMultValue(StatMod mod) { return null; }
//				public Color getModColor(StatMod mod) { return null; }
//				public String getFlatValue(StatMod mod) {
//					return "" + (int)mod.value;
//				}
//			});
			
			addGenericMissionState(info);
			
			addAcceptOrAbandonButton(info, width, "Accept", "Abandon");
		} else {
			addGenericMissionState(info);
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}

	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("campaignMissions", "survey_planet");
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
		return planet;
	}


	
}
