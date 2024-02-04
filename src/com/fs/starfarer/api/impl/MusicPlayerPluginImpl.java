package com.fs.starfarer.api.impl;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.MusicPlayerPluginWithVolumeControl;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class MusicPlayerPluginImpl implements MusicPlayerPluginWithVolumeControl {

	public static String KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY = "$playLocationMusicDuringEnc";
	public static String MUSIC_SET_MEM_KEY = "$musicSetId";
	
	
	public static String MUSIC_ENCOUNTER_MYSTERIOUS_AGGRO = "music_encounter_mysterious";
	public static String MUSIC_ENCOUNTER_MYSTERIOUS_NON_AGGRESSIVE = "music_encounter_mysterious_non_aggressive";
	public static String MUSIC_ENCOUNTER_NEUTRAL = "music_encounter_neutral";
	
	
	public static Object CAMPAIGN_SYSTEM = new Object();
	public static Object CAMPAIGN_HYPERSPACE = new Object();
	public static Object NO_MUSIC = new Object();
	public static Object COMBAT = new Object();
	public static Object TITLE = new Object();
	public static Object MARKET = new Object();
	public static Object ENCOUNTER = new Object();
	public static Object PLANET_SURVEY = new Object();
	
	public static Object CUSTOM = new Object();
	
	public static Map<String, String> stringTokens = new HashMap<String, String>();
	
	/**
	 * Goal here is to return tokens for which an == comparison works.
	 * @param str
	 * @return
	 */
	public static Object getToken(String str) {
		if (!stringTokens.containsKey(str)) {
			stringTokens.put(str, str);
		}
		return stringTokens.get(str);
	}
	
	public static String SYSTEM_MUSIC_PREFIX = "core_sys_music_";
	
	public Object getStateTokenForCampaignLocation() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet.getContainingLocation() instanceof StarSystemAPI) {
			/*
			 Just returning CAMPAIGN_SYSTEM and letting getMusicSetIdForCampaignStateToken()
			 return the musicSetId misses the case where there's a transition between
			 two star systems with different music, without hyperspace travel in between -
			 since there would be no state transition (due to it being the same CAMPAIGN_SYSTEM state), 
			 a restart of the music - and a change to a different music set - would not be triggered.
			 
			 Since the state ID includes the music set id, this means that moving between
			 two systems with the same $musicSetId will not trigger a state change and
			 the same music will just keep playing uninterrupted.
			 */
			StarSystemAPI system = (StarSystemAPI) playerFleet.getContainingLocation();
			String musicSetId = system.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
			if (musicSetId != null) {
				return getToken(SYSTEM_MUSIC_PREFIX + musicSetId);
			}
			return CAMPAIGN_SYSTEM;
		}
//		float depth = Misc.getAbyssalDepthOfPlayer();
//		if (depth > 0.5f) {
//			return NO_MUSIC;
//		}
		return CAMPAIGN_HYPERSPACE;
	}
	
	public String getMusicSetIdForCombat(CombatEngineAPI engine) {
		return "music_combat";
	}
	
	public String getMusicSetIdForTitle() {
		return "music_title";
	}
	
	
	public float getMusicSetVolumeForCampaignStateToken(Object token, Object param) {
		if (token == CAMPAIGN_HYPERSPACE) {
			//float depth = Misc.getAbyssalDepthOfPlayer();
			// need to check this way so that the hyperspace track doesn't fade back in
			// right after transitiong to an abyssal system (as the track fades out)
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			float depth = 0f;
			if (pf != null) depth = Misc.getAbyssalDepth(pf.getLocationInHyperspace());
			float vol = 1f - depth;
			if (vol > 1f) vol = 1f;
			if (vol < 0f) vol = 0f;
			return vol;
		}
		return 1f;
	}
	
	public String getMusicSetIdForCampaignStateToken(Object token, Object param) {
		if (token == MARKET) {
			return getMarketMusicSetId(param);
		}
		if (token == ENCOUNTER) {
			return getEncounterMusicSetId(param);
		}
		if (token == CAMPAIGN_SYSTEM || 
				(token instanceof String && ((String)token).startsWith(SYSTEM_MUSIC_PREFIX))) {
			return getStarSystemMusicSetId();
		}
		if (token == CAMPAIGN_HYPERSPACE) {
			return getHyperspaceMusicSetId();
		}
		if (token == PLANET_SURVEY) {
			return getPlanetSurveyMusicSetId(param);
		}
		if (token == NO_MUSIC) {
			return null;
		}
		return null;
	}
	
	/**
	 * @param param is a MarketAPI.
	 * @return
	 */
	protected String getPlanetSurveyMusicSetId(Object param) {
		return "music_survey_and_scavenge";
	}
	
	protected String getHyperspaceMusicSetId() {
		return "music_campaign_hyperspace";
	}
	
	protected String getStarSystemMusicSetId() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet.getContainingLocation() instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) playerFleet.getContainingLocation();
			String musicSetId = system.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
			if (musicSetId != null) return musicSetId;

			if (system.hasTag(Tags.THEME_CORE) ||
					!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
				return "music_campaign";
			}
		}
		
		return "music_campaign_non_core";
	}
	
	protected String getEncounterMusicSetId(Object param) {
		if (param instanceof SectorEntityToken) {
			SectorEntityToken token = (SectorEntityToken) param;
			
			String musicSetId = token.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
			if (musicSetId != null) return musicSetId;
			
			if (Entities.ABYSSAL_LIGHT.equals(token.getCustomEntityType())) {
				return MUSIC_ENCOUNTER_NEUTRAL;
			}
			if (Entities.CORONAL_TAP.equals(token.getCustomEntityType())) {
				return MUSIC_ENCOUNTER_MYSTERIOUS_AGGRO;
			}
			if (Entities.WRECK.equals(token.getCustomEntityType())) {
				return MUSIC_ENCOUNTER_NEUTRAL;
			}
			
			if (Entities.DEBRIS_FIELD_SHARED.equals(token.getCustomEntityType())) {
				return "music_survey_and_scavenge";
			}
			if (token.hasTag(Tags.GATE)) {
				return MUSIC_ENCOUNTER_NEUTRAL;
			}
			if (token.hasTag(Tags.SALVAGEABLE)) {
				if (token.getMemoryWithoutUpdate() != null && token.getMemoryWithoutUpdate().getBoolean("$hasDefenders")) {
					if (token.getMemoryWithoutUpdate().getBoolean("$limboMiningStation")) {
						return MUSIC_ENCOUNTER_MYSTERIOUS_AGGRO;
					}
					if (token.getMemoryWithoutUpdate().getBoolean("$limboWormholeCache")) {
						return MUSIC_ENCOUNTER_MYSTERIOUS_AGGRO;
					}
					return MUSIC_ENCOUNTER_NEUTRAL;
				}
				return "music_survey_and_scavenge";
			}
			if (token.hasTag(Tags.SALVAGE_MUSIC)) {
				return "music_survey_and_scavenge";
			}
			
			if (token.getFaction() != null) {
				FactionAPI faction = (FactionAPI) token.getFaction();
				String type = null;
				//MemoryAPI mem = token.getMemoryWithoutUpdate();
				boolean hostile = false;
				boolean knowsWhoPlayerIs = false;
				if (token instanceof CampaignFleetAPI) {
					CampaignFleetAPI fleet = (CampaignFleetAPI) token;
					if (fleet.getAI() instanceof ModularFleetAIAPI) {
						hostile = ((ModularFleetAIAPI) fleet.getAI()).isHostileTo(Global.getSector().getPlayerFleet());
					}
					knowsWhoPlayerIs = fleet.knowsWhoPlayerIs();
				}
				
				if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE) && knowsWhoPlayerIs && !hostile) {
					type = "encounter_friendly";
				} else if ((faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS) && knowsWhoPlayerIs) || hostile) {
					type = "encounter_hostile";
				} else {
					type = "encounter_neutral";
				}
				
				if (type != null) {
					musicSetId = faction.getMusicMap().get(type);
					if (musicSetId != null) {
						return musicSetId;
					}
				}
				
				musicSetId = null;
				if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE)) {
					musicSetId = "music_default_encounter_friendly";
				} else if (faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS)) {
					musicSetId = "music_default_encounter_hostile";
				} else {
					musicSetId = "music_default_encounter_neutral";
				}
				return musicSetId;
			}
		}
		return null;
	}
	

	protected String getMarketMusicSetId(Object param) {
		if (param instanceof MarketAPI) {
			MarketAPI market = (MarketAPI) param;
			
			String musicSetId = market.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
			if (musicSetId != null) return musicSetId;
			
			if (market.getPrimaryEntity() != null &&
					market.getPrimaryEntity().getMemoryWithoutUpdate().getBoolean("$abandonedStation")) {
				return getPlanetSurveyMusicSetId(param);
			}
			
			FactionAPI faction = market.getFaction();
			if (faction != null) {
				String type = null;
				if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE)) {
					type = "market_friendly";
				} else if (faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS)) {
					type = "market_hostile";
				} else {
					type = "market_neutral";
				}
				
				if (type != null) {
					musicSetId = faction.getMusicMap().get(type);
					if (musicSetId != null) {
						return musicSetId;
					}
				}
				
				musicSetId = null;
				if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE)) {
					musicSetId = "music_default_market_friendly";
				} else if (faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS)) {
					musicSetId = "music_default_market_hostile";
				} else {
					musicSetId = "music_default_market_neutral";
				}
				return musicSetId;
			}
		}
		return null;
	}
}








