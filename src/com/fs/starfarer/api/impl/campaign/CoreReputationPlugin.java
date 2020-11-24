package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessageParaAPI;
import com.fs.starfarer.api.campaign.comm.MessageSectionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.RelationshipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CoreReputationPlugin implements ReputationActionResponsePlugin {
	
	public static class MissionCompletionRep {
		public float successDelta;
		public RepLevel successLimit;
		public float failureDelta;
		public RepLevel failureLimit;
		public MissionCompletionRep(float successDelta, RepLevel successLimit,
								  	float failureDelta, RepLevel failureLimit) {
			this.successDelta = successDelta;
			this.successLimit = successLimit;
			this.failureDelta = failureDelta;
			this.failureLimit = failureLimit;
		}
		
	}
	
	public static class RepActionEnvelope {
		public RepActions action;
		public Object param = null;
		public CommMessageAPI message = null;
		public TextPanelAPI textPanel = null;
		public boolean addMessageOnNoChange = true;
		public boolean withMessage = true;
		public String reason;
		public RepActionEnvelope(RepActions action, Object param) {
			this.action = action;
			this.param = param;
		}
		public RepActionEnvelope(RepActions action) {
			this.action = action;
		}
		public RepActionEnvelope(RepActions action, Object param, CommMessageAPI message, boolean addMessageOnNoChange) {
			this.action = action;
			this.param = param;
			this.message = message;
			this.addMessageOnNoChange = addMessageOnNoChange;
		}
		public RepActionEnvelope(RepActions action, Object param, CommMessageAPI message, TextPanelAPI textPanel, boolean addMessageOnNoChange) {
			this(action, param, message, textPanel, addMessageOnNoChange, true);
		}
		public RepActionEnvelope(RepActions action, Object param, CommMessageAPI message, TextPanelAPI textPanel, boolean addMessageOnNoChange, boolean withMessage) {
			this(action, param, message, textPanel, addMessageOnNoChange, withMessage, null);
		}
		public RepActionEnvelope(RepActions action, Object param, CommMessageAPI message, TextPanelAPI textPanel, boolean addMessageOnNoChange, boolean withMessage, String reason) {
			this.action = action;
			this.param = param;
			this.message = message;
			this.addMessageOnNoChange = addMessageOnNoChange;
			this.textPanel = textPanel;
			this.withMessage = withMessage;
			this.reason = reason;
		}
		public RepActionEnvelope(RepActions action, Object param, TextPanelAPI textPanel) {
			this.action = action;
			this.param = param;
			this.textPanel = textPanel;
		}
	}
	
	public static class CustomRepImpact {
		public float delta = 0;
		public RepLevel limit = null;
		public RepLevel ensureAtBest = null;
		public RepLevel ensureAtWorst = null;
		public RepLevel requireAtBest = null;
		public RepLevel requireAtWorst = null;
	}
	
	public static enum RepActions {
		COMBAT_NO_DAMAGE_ESCAPE, // now unused
		
		COMBAT_NORMAL,
		COMBAT_AGGRESSIVE,
		
		COMBAT_NORMAL_TOFF,
		COMBAT_AGGRESSIVE_TOFF,
		
		COMBAT_HELP_MINOR,
		COMBAT_HELP_MAJOR,
		COMBAT_HELP_CRITICAL,
		
		COMBAT_FRIENDLY_FIRE,
		
		FOOD_SHORTAGE_PLAYER_ENDED_FAST,
		FOOD_SHORTAGE_PLAYER_ENDED_NORMAL,
		
		SYSTEM_BOUNTY_REWARD,
		PERSON_BOUNTY_REWARD,
		COMBAT_WITH_ENEMY,
		
		TRADE_EFFECT,
		SMUGGLING_EFFECT,
		TRADE_WITH_ENEMY,
		
		OTHER_FACTION_GOOD_REP_INVESTIGATION_MINOR,
		OTHER_FACTION_GOOD_REP_INVESTIGATION_MAJOR,
		OTHER_FACTION_GOOD_REP_INVESTIGATION_CRITICAL,
		
		SMUGGLING_INVESTIGATION_GUILTY,
		COMM_SNIFFER_INVESTIGATION_GUILTY,
		FOOD_INVESTIGATION_GUILTY,
		FOOD_INVESTIGATION_GUILTY_MAJOR,
		
		CAUGHT_INSTALLING_SNIFFER,
		
		CUSTOMS_NOTICED_EVADING,
		CUSTOMS_CAUGHT_SMUGGLING,
		CUSTOMS_REFUSED_TOLL,
		CUSTOMS_REFUSED_FINE,
		CUSTOMS_COULD_NOT_AFFORD,
		CUSTOMS_PAID,
		
		REP_DECAY_POSITIVE,
		//REP_DECAY_NEGATIVE,
		
		TRANSPONDER_OFF,
		TRANSPONDER_OFF_REFUSE,
		
		CARGO_SCAN_REFUSE,
		
		MISSION_SUCCESS,
		MISSION_FAILURE,
		
		MAKE_SUSPICOUS_AT_WORST,
		
		MAKE_HOSTILE_AT_BEST,
		COMMISSION_ACCEPT,
		COMMISSION_BOUNTY_REWARD,
		
		COMMISSION_NEUTRAL_BATTLE_PENALTY,
		COMMISSION_PENALTY_HOSTILE_TO_NON_ENEMY,
		
		SHRINE_OFFERING,
		
		INTERDICTED,
		
		CUSTOM,
//		MISSION_COMPLETED_MINOR,
//		MISSION_COMPLETED_MEDIUM,
//		MISSION_COMPLETED_IMPORTANT,
//		
//		MISSION_FAILED_MINOR,
//		MISSION_FAILED_MEDIUM,
//		MISSION_FAILED_IMPORTANT,
		
		
	}
	
	public static class RepRewards {
		public static final float TINY = 0.01f;
		public static final float SMALL = 0.02f;
		public static final float MEDIUM = 0.03f;
		public static final float HIGH = 0.05f;
		public static final float VERY_HIGH = 0.07f;
		public static final float EXTREME = 0.1f;
	}
	
	


	public ReputationAdjustmentResult handlePlayerReputationAction(Object action, final String factionId) {
		//final FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		final FactionAPI faction = Global.getSector().getFaction(factionId);
		return handlePlayerReputationActionInner(action, factionId, null, faction.getRelToPlayer());
	}


	public ReputationAdjustmentResult handlePlayerReputationAction(Object action, PersonAPI person) {
		return handlePlayerReputationActionInner(action, person.getFaction().getId(), person, person.getRelToPlayer());
	}

	
	public ReputationAdjustmentResult handlePlayerReputationActionInner(Object actionObject,
																	String factionId,
																	PersonAPI person,
																	RelationshipAPI delegate) {
		if (!(actionObject instanceof RepActionEnvelope) && !(actionObject instanceof RepActions)) {
			return new ReputationAdjustmentResult(0);
		}
		
//		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		FactionAPI faction = Global.getSector().getFaction(factionId);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		RepActions action = null;
		String reason = null;
		Object param = null;
		CommMessageAPI message = null;
		TextPanelAPI panel = null;
		boolean withMessage = true;
		boolean addMessageOnNoChange = true;
		if (actionObject instanceof RepActions) {
			action = (RepActions) actionObject;
			param = null;
		} else if (actionObject instanceof RepActionEnvelope) {
			RepActionEnvelope envelope = (RepActionEnvelope) actionObject;
			action = envelope.action;
			param = envelope.param;
			message = envelope.message;
			panel = envelope.textPanel;
			addMessageOnNoChange = envelope.addMessageOnNoChange;
			withMessage = envelope.withMessage;
			reason = envelope.reason;
		}
		
		float delta = 0;
		RepLevel limit = null;
		RepLevel ensureAtBest = null;
		RepLevel ensureAtWorst = null;
		RepLevel requireAtBest = null;
		RepLevel requireAtWorst = null;
		//RepLevel curr = player.getRelationshipLevel(faction);
		RepLevel curr = delegate.getLevel();
		
		if (action == RepActions.CUSTOM) {
			if (!(param instanceof CustomRepImpact)) {
				throw new RuntimeException("For RepActions.CUSTOM, use CustomRepImpact as a param in a RepActionEnvelope");
			}
			
			CustomRepImpact impact = (CustomRepImpact) param;
			
			delta = impact.delta;
			limit = impact.limit;
			ensureAtBest = impact.ensureAtBest;
			ensureAtWorst = impact.ensureAtWorst;
			requireAtBest = impact.requireAtBest;
			requireAtWorst = impact.requireAtWorst;
		}
		
		switch (action) {
		case COMBAT_HELP_MINOR:
			delta = RepRewards.TINY;
			limit = RepLevel.WELCOMING;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
		case COMBAT_HELP_MAJOR:
			delta = RepRewards.MEDIUM;
			limit = RepLevel.FRIENDLY;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
		case COMBAT_HELP_CRITICAL:
			delta = RepRewards.HIGH;
			limit = RepLevel.COOPERATIVE;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
//		case COMBAT_NO_DAMAGE_ESCAPE:
//			if (delegate.isAtWorst(RepLevel.FAVORABLE)) {
//				delta = -RepRewards.HIGH;
//			} else {
//				delta = -RepRewards.SMALL;
//			}
//			limit = RepLevel.HOSTILE;
//			break;
		case COMBAT_FRIENDLY_FIRE:
			float fpHull = (float) Math.ceil((Float) param);
			if (fpHull >= 20f) {
				fpHull = 0f;
				ensureAtBest = RepLevel.HOSTILE;
			}
			delta = -1f * fpHull * 0.01f;
			break;
		case COMBAT_NORMAL:
			delta = -RepRewards.MEDIUM;
			limit = RepLevel.VENGEFUL;
			ensureAtBest = RepLevel.HOSTILE;
			break;
		case COMBAT_AGGRESSIVE:
			delta = -RepRewards.HIGH;
			limit = RepLevel.VENGEFUL;
			ensureAtBest = RepLevel.HOSTILE;
			break;
		case COMBAT_NORMAL_TOFF:
			delta = -RepRewards.MEDIUM;
			limit = RepLevel.VENGEFUL;
			break;
		case COMBAT_AGGRESSIVE_TOFF:
			delta = -RepRewards.HIGH;
			limit = RepLevel.VENGEFUL;
			break;			
		case FOOD_SHORTAGE_PLAYER_ENDED_FAST:
			delta = getRepRewardForFoodShortage(action, (MarketAPI) param);
			limit = RepLevel.FRIENDLY;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
		case FOOD_SHORTAGE_PLAYER_ENDED_NORMAL:
			delta = getRepRewardForFoodShortage(action, (MarketAPI) param);
			limit = RepLevel.WELCOMING;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
		case SYSTEM_BOUNTY_REWARD:
			float fp = (Float) param;
			if (fp < 10) {
				delta = RepRewards.SMALL;
				limit = RepLevel.WELCOMING;
			} else if (fp < 30) {
				delta = RepRewards.MEDIUM;
				limit = RepLevel.FRIENDLY;
			} else {
				delta = RepRewards.HIGH;
				limit = RepLevel.COOPERATIVE;
			}
			requireAtWorst = RepLevel.VENGEFUL;
			break;
		case PERSON_BOUNTY_REWARD:
			delta = RepRewards.MEDIUM;
			limit = RepLevel.COOPERATIVE;
			requireAtWorst = RepLevel.VENGEFUL;
			break;
		case COMBAT_WITH_ENEMY:
			fp = (Float) param;
			if (fp < 10) {
				delta = RepRewards.TINY;
				limit = RepLevel.WELCOMING;
			} else if (fp < 30) {
				delta = RepRewards.SMALL;
				limit = RepLevel.FRIENDLY;
			} else {
				delta = RepRewards.MEDIUM;
				limit = RepLevel.COOPERATIVE;
			}
			requireAtWorst = RepLevel.VENGEFUL;
			break;
		case TRADE_EFFECT:
			delta = (Float) param * 0.01f;
			limit = RepLevel.WELCOMING;
			//if (faction.getId().equals(Factions.PIRATES)) {
//			if (faction.getCustom().optBoolean(Factions.CUSTOM_WILL_TRADE_WHEN_HOSTILE)) {
//				requireAtWorst = RepLevel.HOSTILE;
//			} else {
				requireAtWorst = RepLevel.INHOSPITABLE;
//			}
			break;
		case SMUGGLING_EFFECT:
			delta = -(Float) param * 0.01f;
			limit = RepLevel.INHOSPITABLE;
			break;
		case TRADE_WITH_ENEMY:
			delta = -(Float) param * 0.01f;
			limit = RepLevel.HOSTILE;
			break;
		case SMUGGLING_INVESTIGATION_GUILTY:
			delta = -RepRewards.HIGH;
			limit = RepLevel.HOSTILE;
			ensureAtBest = RepLevel.INHOSPITABLE;
			break;
		case OTHER_FACTION_GOOD_REP_INVESTIGATION_MINOR:
			delta = -RepRewards.HIGH;
			limit = RepLevel.HOSTILE;
			break;
		case OTHER_FACTION_GOOD_REP_INVESTIGATION_MAJOR:
			delta = -RepRewards.HIGH;
			limit = RepLevel.HOSTILE;
			ensureAtBest = RepLevel.INHOSPITABLE;
			break;
		case OTHER_FACTION_GOOD_REP_INVESTIGATION_CRITICAL:
			delta = -RepRewards.HIGH;
			limit = RepLevel.HOSTILE;
			ensureAtBest = RepLevel.HOSTILE;
			break;
		case CAUGHT_INSTALLING_SNIFFER:
			delta = -RepRewards.TINY;
			limit = RepLevel.INHOSPITABLE;
			break;
		case COMM_SNIFFER_INVESTIGATION_GUILTY:
			delta = -RepRewards.SMALL;
			limit = RepLevel.INHOSPITABLE;
			break;
		case FOOD_INVESTIGATION_GUILTY:
			delta = -RepRewards.HIGH;
			limit = RepLevel.INHOSPITABLE;
			break;
		case FOOD_INVESTIGATION_GUILTY_MAJOR:
			delta = -RepRewards.HIGH;
			limit = RepLevel.HOSTILE;
			ensureAtBest = RepLevel.SUSPICIOUS;
			break;
		case CUSTOMS_NOTICED_EVADING:
			delta = -RepRewards.MEDIUM;
			limit = RepLevel.HOSTILE;
			break;
		case CUSTOMS_CAUGHT_SMUGGLING:
			delta = -(Float) param * 0.01f;
			limit = RepLevel.HOSTILE;
			break;
		case CUSTOMS_REFUSED_TOLL:
			delta = -RepRewards.MEDIUM;
			limit = RepLevel.HOSTILE;
			break;
		case CUSTOMS_REFUSED_FINE:
			delta = -RepRewards.VERY_HIGH;
			limit = RepLevel.HOSTILE;
			break;
		case CUSTOMS_COULD_NOT_AFFORD:
			delta = -RepRewards.SMALL;
			limit = RepLevel.INHOSPITABLE;
			break;
		case CUSTOMS_PAID:
			delta = RepRewards.TINY;
			limit = RepLevel.WELCOMING;
			break;
		case REP_DECAY_POSITIVE:
			delta = .15f;
			limit = RepLevel.NEUTRAL;
			requireAtWorst = RepLevel.INHOSPITABLE;
			break;
		case MISSION_SUCCESS:
			MissionCompletionRep rep = (MissionCompletionRep) param;
			delta = rep.successDelta;
			limit = rep.successLimit;
			break;
		case MISSION_FAILURE:
			rep = (MissionCompletionRep) param;
			delta = rep.failureDelta;
			limit = rep.failureLimit;
			break;
		case TRANSPONDER_OFF:
			if (person != null) {
				delta = -RepRewards.MEDIUM;
			} else {
				delta = -RepRewards.SMALL;
			}
			limit = RepLevel.HOSTILE;
			break;
		case TRANSPONDER_OFF_REFUSE:
		case CARGO_SCAN_REFUSE:
			if (person != null) {
				delta = -RepRewards.HIGH;
			} else {
				delta = -RepRewards.MEDIUM;
			}
			limit = RepLevel.HOSTILE;
			break;
		case COMMISSION_ACCEPT:
			delta = RepRewards.HIGH;
			break;
		case SHRINE_OFFERING:
			delta = RepRewards.MEDIUM;
			limit = RepLevel.FRIENDLY;
			break;
		case INTERDICTED:
			delta = -RepRewards.TINY;
			limit = RepLevel.INHOSPITABLE;
			break;
		case MAKE_HOSTILE_AT_BEST:
			delta = 0;
			ensureAtBest = RepLevel.HOSTILE;
			break;
		case MAKE_SUSPICOUS_AT_WORST:
			delta = 0;
			ensureAtWorst = RepLevel.SUSPICIOUS;
			break;
		case COMMISSION_BOUNTY_REWARD:
			fp = (Float) param;
			if (fp < 20) {
				delta = RepRewards.TINY;
				limit = RepLevel.FRIENDLY;
			} else if (fp < 50) {
				delta = RepRewards.SMALL;
				limit = RepLevel.COOPERATIVE;
			} else {
				delta = RepRewards.MEDIUM;
				limit = RepLevel.COOPERATIVE;
			}
			break;
		case COMMISSION_PENALTY_HOSTILE_TO_NON_ENEMY:
			//delta = -RepRewards.MEDIUM;
			fp = (Float) param;
			if (fp < 20) {
				delta = -RepRewards.TINY;
			} else if (fp < 50) {
				delta = -RepRewards.SMALL;
			} else {
				delta = -RepRewards.MEDIUM;
			}
			limit = RepLevel.INHOSPITABLE;
			break;
		}

		if (delta < 0 && delta > -0.01f) delta = -0.01f;
		if (delta > 0 && delta < 0.01f) delta = 0.01f;
		delta = Math.round(delta * 100f) / 100f;
		
		if (delegate.getTarget() == Global.getSector().getPlayerFaction()) {
			delta = 0;
		}
		if (delegate.getTarget() == Global.getSector().getPlayerPerson()) {
			delta = 0;
		}
		
		float deltaSign = Math.signum(delta);
		//float before = player.getRelationship(faction.getId());
		float before = delegate.getRel();
		
		if (ensureAtBest != null) {
			delegate.ensureAtBest(ensureAtBest);
		}
		if (ensureAtWorst != null) {
			delegate.ensureAtWorst(ensureAtWorst);
		}
		
		if ((requireAtBest == null || curr.isAtBest(requireAtBest)) &&
				(requireAtWorst == null || curr.isAtWorst(requireAtWorst))) {
			delegate.adjustRelationship(delta, limit);
		}
		
		//float after = player.getRelationship(faction.getId());
		float after = delegate.getRel();
		delta = after - before;
		
		//if (delta != 0) {
		if (withMessage) {
			if (Math.abs(delta) >= 0.005f) {
				addAdjustmentMessage(delta, faction, person, message, panel, null, null, true, 0f, reason);
			} else if (deltaSign != 0 && addMessageOnNoChange) {
				addNoChangeMessage(deltaSign, faction, person, message, panel, null, null, true, 0f, reason);
			}
		}
		
		if (delta != 0) {
			if (person == null) {
				Global.getSector().reportPlayerReputationChange(faction.getId(), delta);
			} else {
				Global.getSector().reportPlayerReputationChange(person, delta);
			}
		}
		
		return new ReputationAdjustmentResult(delta);
	}
	
	
	private float getRepRewardForFoodShortage(RepActions action, MarketAPI market) {
		float mult = (float) market.getSize() / 10f + 0.5f;
		float delta = 0;
		switch (action) {
		case FOOD_SHORTAGE_PLAYER_ENDED_NORMAL:
			delta = RepRewards.MEDIUM;
			break;
		case FOOD_SHORTAGE_PLAYER_ENDED_FAST:
			delta = RepRewards.HIGH;
			break;
		}
		return delta * mult;
	}
	
	
	
	public static void addDeltaMessage(float delta, FactionAPI faction, PersonAPI person, TextPanelAPI panel,
									   TooltipMakerAPI info, Color tc, boolean withCurrent, float pad) {
		if (delta == 0) {
			addNoChangeMessage(delta, faction, person, null, panel, info, tc, withCurrent, pad);
		} else {
			addAdjustmentMessage(delta, faction, person, null, panel, info, tc, withCurrent, pad);
		}
		
	}
	public static void addNoChangeMessage(float deltaSign, FactionAPI faction, PersonAPI person, CommMessageAPI message, TextPanelAPI panel,
			TooltipMakerAPI info, Color tc, boolean withCurrent, float pad) {
		addNoChangeMessage(deltaSign, faction, person, message, panel, info, tc, withCurrent, pad, null);
	}
	public static void addNoChangeMessage(float deltaSign, FactionAPI faction, PersonAPI person, CommMessageAPI message, TextPanelAPI panel,
										  TooltipMakerAPI info, Color tc, boolean withCurrent, float pad, String reason) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
//		String standing = player.getRelationshipLevel(faction).getDisplayName();
//		standing = standing.toLowerCase();
		RepLevel level = player.getRelationshipLevel(faction.getId());
		int repInt = (int) Math.ceil((Math.round(player.getRelationship(faction.getId()) * 100f)));
		
		Color textColor = Global.getSettings().getColor("standardTextColor");
		Color factionColor = faction.getBaseUIColor();
		Color relColor = faction.getRelColor(player.getId());
		String targetName = faction.getDisplayNameWithArticle();
		String targetNameHighlight = faction.getDisplayName();
		if (Factions.INDEPENDENT.equals(faction.getId())) {
			targetName = faction.getDisplayNameLongWithArticle();
			targetNameHighlight = faction.getDisplayNameLong();
		}
		
		if (person != null) {
			targetName = person.getName().getFullName();
			relColor = person.getRelToPlayer().getRelColor();
			repInt = (int) Math.ceil((Math.round(person.getRelToPlayer().getRel() * 100f)));
			level = person.getRelToPlayer().getLevel();
		}
		
		String standing = "" + repInt + "/100" + " (" + level.getDisplayName().toLowerCase() + ")";
		
		
		

		String text = "Your relationship with " + targetName + 
					", currently at " + standing + ", is already well-established and is not affected by your recent actions";
		
		if (panel == null && info == null) {
//			Global.getSector().getCampaignUI().addMessage(text, textColor, 
//					standing, 
//					relColor);
			
			MessageIntel intel = new MessageIntel();
			if (person == null) {
				intel.addLine("Relationship with " + targetName + " not affected", null, 
							 new String[] {targetNameHighlight}, 
							 			   factionColor); 
				intel.addLine(BaseIntelPlugin.BULLET + "Currently at %s", null, new String[]{standing}, relColor);
				if (reason != null) intel.addLine(BaseIntelPlugin.BULLET + reason);
				intel.setIcon(faction.getCrest());
			} else {
				intel.addLine("Relationship with " + targetName + " not affected"); 
				intel.addLine(BaseIntelPlugin.BULLET + "Currently at %s", null, new String[]{standing}, relColor);
				if (reason != null) intel.addLine(BaseIntelPlugin.BULLET + reason);
				intel.setIcon(person.getPortraitSprite());
			}
			//intel.setIcon(Global.getSettings().getSpriteName("intel_categories", "reputation"));
			Global.getSector().getCampaignUI().addMessage(intel);
		}
		
		if (panel != null) {
			if (panel.getDialog() != null) {
				SectorEntityToken target = panel.getDialog().getInteractionTarget();
				if (target != null) {
					if (target.getActivePerson() != null) {
						target.getActivePerson().getMemory();
					}
					target.getMemory();
				}
			}
		}
		
		if (text != null) {
			if (message != null) {
				text = text.replaceAll(" by your recent actions", " by this.");
				appendToSection2(text, message);
				appendToSectionHighlights(message.getSection2(), 
						new String [] {standing}, 
						new Color [] {relColor});
				
				message.addTag(Tags.REPORT_REP);
				if (person == null) {
					message.getCustomMap().put(CommMessageAPI.MESSAGE_FACTION_ID_KEY, faction.getId());
				} else {
					message.getCustomMap().put(CommMessageAPI.MESSAGE_PERSON_ID_KEY, person.getId());
				}
				//message.setSmallIcon(faction.getLogo());
			} 
			
			if (panel != null) {
				text = text.replaceAll(" by your recent actions", "");
				printToTextPanel(panel, text, 
						new String [] {standing}, 
						new Color [] {relColor});
			}
		}
		
		if (info != null) {
			if (person == null) {
				LabelAPI label = info.addPara("Relationship with " + targetName + " not affected", tc, pad);
				label.setHighlight(targetNameHighlight); 
				label.setHighlightColors(factionColor); 
				if (withCurrent) info.addPara("Currently at %s", 0f, tc, relColor, standing);
			} else {
				LabelAPI label = info.addPara("Relationship with " + targetName + " not affected", tc, pad);
				if (withCurrent) info.addPara("Currently at %s", 0f, tc, relColor, standing);
			}
		}
	}
	
	
	public static void addCurrentStanding(FactionAPI faction, PersonAPI person, TextPanelAPI panel,
			TooltipMakerAPI info, Color tc, float pad) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		RepLevel level = player.getRelationshipLevel(faction.getId());
		int repInt = (int) Math.ceil((Math.round(player.getRelationship(faction.getId()) * 100f)));

		Color factionColor = faction.getBaseUIColor();
		Color relColor = faction.getRelColor(player.getId());
		String targetName = faction.getDisplayNameWithArticle();
		String targetNameHighlight = faction.getDisplayName();
		if (Factions.INDEPENDENT.equals(faction.getId())) {
			targetName = faction.getDisplayNameLongWithArticle();
			targetNameHighlight = faction.getDisplayNameLong();
		}

		if (person != null) {
			targetName = person.getName().getFullName();
			relColor = person.getRelToPlayer().getRelColor();
			repInt = (int) Math.ceil((Math.round(person.getRelToPlayer().getRel() * 100f)));
			level = person.getRelToPlayer().getLevel();
		}

		String standing = "" + repInt + "/100" + " (" + level.getDisplayName().toLowerCase() + ")";

		String text = "Current standing with " + targetName + 
					  " is " + standing + "";

		if (panel != null) {
			printToTextPanel(panel, text, 
					new String [] {targetNameHighlight, standing}, 
					new Color [] {factionColor, relColor});
		}

		if (info != null) {
			String end = "";
			if (pad > 0) {
				end = ".";
			}
			if (person == null) {
				LabelAPI label = info.addPara("Current standing with " + targetName + 
						" is %s" + end, pad, tc, relColor, standing);
				label.setHighlight(targetNameHighlight, standing); 
				label.setHighlightColors(factionColor, relColor);
			} else {
				LabelAPI label = info.addPara("Current standing with " + targetName + 
						" is %s" + end, pad, tc, relColor, standing);
				label.setHighlight(targetNameHighlight, standing); 
				label.setHighlightColors(factionColor, relColor);
			}
		}
	}
	
	public static void addRequiredStanding(FactionAPI faction, RepLevel req, PersonAPI person, TextPanelAPI panel,
			TooltipMakerAPI info, Color tc, float pad, boolean orBetter) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		RepLevel level = req;

		float rel = level.getMin();
		
		PersonAPI fake = Global.getFactory().createPerson();
		fake.getRelToPlayer().setRel(rel);

		int repInt = (int) Math.ceil((Math.round(rel * 100f)));
		Color relColor = fake.getRelToPlayer().getRelColor(level);
		
		
		Color factionColor = faction.getBaseUIColor();
		String targetName = faction.getDisplayNameWithArticle();
		String targetNameHighlight = faction.getDisplayName();
		if (Factions.INDEPENDENT.equals(faction.getId())) {
			targetName = faction.getDisplayNameLongWithArticle();
			targetNameHighlight = faction.getDisplayNameLong();
		}

		if (person != null) {
			targetName = person.getName().getFullName();
			relColor = person.getRelToPlayer().getRelColor();
		}
		

		String standing = "" + repInt + "/100" + " (" + level.getDisplayName().toLowerCase() + ")";

		String mid = "or better";
		if (!orBetter) {
			mid = "or worse";
		}
		String text = "Requires " + standing + " " + mid + " standing with " + targetName;
		
		if (panel != null) {
			printToTextPanel(panel, text, 
					new String [] {standing, targetNameHighlight}, 
					new Color [] {relColor, factionColor});
		}

		if (info != null) {
			String end = "";
			if (pad > 0) {
				end = ".";
			}
			if (person == null) {
				LabelAPI label = info.addPara("Requires %s " + mid + " standing with " + targetName + end, 
						 					  pad, tc, relColor, standing);
				label.setHighlight(standing, targetNameHighlight); 
				label.setHighlightColors(relColor, factionColor);
			} else {
				LabelAPI label = info.addPara("Requires %s " + mid + " standing with " + targetName + end, 
						 					  pad, tc, relColor, standing);
				label.setHighlight(standing, targetNameHighlight); 
				label.setHighlightColors(relColor, factionColor);
			}
		}
	}
	
	
	public static void addAdjustmentMessage(float delta, FactionAPI faction, PersonAPI person, 
			CommMessageAPI message, TextPanelAPI panel, 
			TooltipMakerAPI info, Color tc, boolean withCurrent, float pad) {
		addAdjustmentMessage(delta, faction, person, message, panel, info, tc, withCurrent, pad, null);
	}
	public static void addAdjustmentMessage(float delta, FactionAPI faction, PersonAPI person, 
							CommMessageAPI message, TextPanelAPI panel, 
							TooltipMakerAPI info, Color tc, boolean withCurrent, float pad,
							String reason) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		
		//faction may be null here if it's w/ person
		
		RepLevel level = null;
		int deltaInt = (int) Math.round((Math.abs(delta) * 100f));
		String targetName = null;
		String targetNameHighlight = null;
		Color relColor = null;
		
		int repInt = 0;
		if (faction != null) {
			level = player.getRelationshipLevel(faction.getId());
			repInt = (int) Math.ceil((Math.round(player.getRelationship(faction.getId()) * 100f)));
			targetName = faction.getDisplayNameWithArticle();
			targetNameHighlight = faction.getDisplayName();
			if (Factions.INDEPENDENT.equals(faction.getId())) {
				targetName = faction.getDisplayNameLongWithArticle();
				targetNameHighlight  = faction.getDisplayNameLong();
			}
			relColor = faction.getRelColor(player.getId());
		}
		
		if (person != null) {
			targetName = person.getName().getFullName();
			relColor = person.getRelToPlayer().getRelColor();
			repInt = (int) Math.ceil((Math.round(person.getRelToPlayer().getRel() * 100f)));
			level = person.getRelToPlayer().getLevel();
		}
		
		String standing = "" + repInt + "/100" + " (" + level.getDisplayName().toLowerCase() + ")";
		String text = null;
		
		
		Color textColor = Global.getSettings().getColor("standardTextColor");
		//Color textColor = Global.getSettings().getColor("textGrayColor");
		Color factionColor = null;
		if (faction != null) factionColor = faction.getBaseUIColor();
		
		Color deltaColor = Global.getSettings().getColor("textFriendColor");
		String deltaString = "improved by " + deltaInt;
		if (delta < 0) {
			deltaColor = Misc.getNegativeHighlightColor();
			deltaString = "reduced by " + deltaInt;
		} else if (delta == 0) {
			deltaString = "not affected";
			deltaColor = tc;
		}
		
		text = "Relationship with " +  targetName + " " + deltaString + ", currently at " + standing + "";
		
		if (panel == null && info == null) {
//			Global.getSector().getCampaignUI().addMessage(text, textColor, 
//					deltaString, standing, 
//					deltaColor, relColor);
			
			MessageIntel intel = new MessageIntel();
			if (person == null) {
				intel.addLine("Relationship with " + targetName + " " + deltaString, null, 
							 new String[] {targetNameHighlight, deltaString}, 
							 factionColor, deltaColor); 
				intel.addLine(BaseIntelPlugin.BULLET + "Currently at %s", null, new String[]{standing}, relColor);
				if (reason != null) intel.addLine(BaseIntelPlugin.BULLET + reason);
				if (faction != null) intel.setIcon(faction.getCrest());
			} else {
				intel.addLine("Relationship with " + targetName + " " + deltaString, null, 
						 new String[] {deltaString}, 
						 			   deltaColor); 
				intel.addLine(BaseIntelPlugin.BULLET + "Currently at %s", null, new String[]{standing}, relColor);
				if (reason != null) intel.addLine(BaseIntelPlugin.BULLET + reason);
				intel.setIcon(person.getPortraitSprite());
			}
			//intel.setIcon(Global.getSettings().getSpriteName("intel_categories", "reputation"));
			Global.getSector().getCampaignUI().addMessage(intel);
		}
		
		if (text != null) {
			
			if (message != null) {
				text += ".";
				appendToSection2(text, message);
				
				appendToSectionHighlights(message.getSection2(), 
						new String [] {deltaString, standing}, 
						new Color [] {deltaColor, relColor});
				
				message.addTag(Tags.REPORT_REP);
				if (person == null) {
					message.getCustomMap().put(CommMessageAPI.MESSAGE_FACTION_ID_KEY, faction.getId());
				} else {
					message.getCustomMap().put(CommMessageAPI.MESSAGE_PERSON_ID_KEY, person.getId());
				}
				//message.setSmallIcon(faction.getLogo());
			}
			
			if (panel != null) {
				printToTextPanel(panel, text, 
						new String [] {deltaString, standing},
						new Color [] {deltaColor, relColor});
			}
		}
		
		if (info != null) {
			if (person == null) {
//				LabelAPI label = info.addPara("Relationship with " + targetName + " " + deltaString + 
//						", currently " + standing, tc, pad);
//				label.setHighlight(targetNameHighlight, deltaString, standing); 
//				label.setHighlightColors(factionColor, deltaColor, relColor);
//				if (delta > 0) {
//					deltaString = "+" + deltaInt;
//				} else if (delta < 0) {
//					deltaString = "-" + deltaInt;
//				} else {
//					deltaString = "" + deltaInt;
//				}
//				LabelAPI label = info.addPara(faction.getDisplayName() + " reputation " + deltaString, tc, pad);
//				label.setHighlight(targetNameHighlight, deltaString); 
//				label.setHighlightColors(factionColor, deltaColor);
				
				LabelAPI label = info.addPara("Relationship with " + targetName + " " + deltaString, tc, pad);
				label.setHighlight(targetNameHighlight, deltaString); 
				label.setHighlightColors(factionColor, deltaColor);
				//info.setBulletedListMode(BaseIntelPlugin.INDENT);
				if (withCurrent) info.addPara("Currently at %s", 0f, tc, relColor, standing);
				//info.setBulletedListMode(BaseIntelPlugin.BULLET);
			} else {
				LabelAPI label = info.addPara("Relationship with " + targetName + " " + deltaString, tc, pad);
				label.setHighlight(deltaString); 
				label.setHighlightColors(deltaColor); 
				if (withCurrent) info.addPara("Currently at %s", 0f, tc, relColor, standing);
			}
		}
	}
	
	private static void printToTextPanel(TextPanelAPI panel, String text, String [] highlights, Color [] colors) {
		Color textColor = Global.getSettings().getColor("textGrayColor");
		panel.setFontSmallInsignia();
		panel.addParagraph(text, textColor);
		panel.highlightInLastPara(highlights);
		panel.setHighlightColorsInLastPara(colors);
		panel.setFontInsignia();
	}
	
	private static void appendToSectionHighlights(MessageSectionAPI section, String [] strings, Color [] colors) {
		String [] currStrings = section.getHighlights();
		Color [] currColors = section.getHighlightColors();
		if (currStrings == null) {
			section.setHighlights(strings);
			section.setHighlightColors(colors);
		} else {
			List<String> stringList = new ArrayList<String>();
			for (String s : currStrings) stringList.add(s);
			for (String s : strings) stringList.add(s);
			
			List<Color> colorList = new ArrayList<Color>();
			for (Color s : currColors) colorList.add(s);
			for (Color s : colors) colorList.add(s);
			
			section.setHighlights(stringList.toArray(new String [0]));
			section.setHighlightColors(colorList.toArray(new Color [0]));
		}
	}
	
	private static void appendToSection2(String text, CommMessageAPI message) {
		List<MessageParaAPI> body = message.getSection2().getBody();
		if (body.isEmpty()) {
			message.getSection2().addPara(text);
		} else {
			MessageParaAPI lastPara = body.get(body.size() - 1);
			String str = lastPara.getBody();
			str = str + "\n\n" + text;
			lastPara.setBody(str);
		}
	}

}




