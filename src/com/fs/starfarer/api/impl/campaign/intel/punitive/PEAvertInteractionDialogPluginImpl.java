package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionIntel.PunExOutcome;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExData;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PEAvertInteractionDialogPluginImpl implements InteractionDialogPlugin {

	private static enum OptionId {
		INIT,
		USE_CONNECTIONS,
		BRIBE,
		LEAVE,
		
		CONFIRM,
		CANCEL,
	}
	
	//public static float BRIBE_BASE = 0;
	public static int BRIBE_MULT = 10000;
	public static int BRIBE_MAX = 100000;
	
	public static RepLevel MIN_REP = RepLevel.WELCOMING;
	public static float REP_COST  = 0.2f;
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;

	protected PunitiveExpeditionIntel intel;
	protected IntelUIAPI ui;
	
	public PEAvertInteractionDialogPluginImpl(PunitiveExpeditionIntel intel, IntelUIAPI ui) {
		this.intel = intel;
		this.ui = ui;
	}

	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		visual.setVisualFade(0.25f, 0.25f);
		//visual.showImagePortion("illustrations", "quartermaster", 640, 400, 0, 0, 480, 300);
		visual.showPlanetInfo(intel.getTarget().getPrimaryEntity());
	
		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	protected int computeBribeAmount() {
		PunExData data = PunitiveExpeditionManager.getInstance().getDataFor(intel.getFaction());
		int numAttempts = 1;
		if (data != null) numAttempts = data.numAttempts;
		
		int bribe = (int) (Math.pow(2, numAttempts) * BRIBE_MULT);
		if (bribe > BRIBE_MAX) bribe = BRIBE_MAX;
		return bribe;
//		int bribe = (int) Math.round(BRIBE_BASE + data.threshold * BRIBE_MULT);
//		return bribe;
	}
	
	protected void printOptionDesc(OptionId option) {
		Color tc = Misc.getTextColor();
		FactionAPI faction = intel.getFaction();
		
		switch (option) {
		case BRIBE:
			int bribe = computeBribeAmount();
			textPanel.addPara("Sufficient funding allocated to proper official and unofficial actors should " +
					"ensure that the expedition does not go beyond the planning stages.");
			
			int credits = (int) playerFleet.getCargo().getCredits().get();
			Color costColor = Misc.getHighlightColor();
			if (bribe > credits) costColor = Misc.getNegativeHighlightColor();
			
			textPanel.addPara("A total of %s should be enough to get the job done, and will also " +
					"ensure that your standing with " + faction.getDisplayNameWithArticle() + 
					" does not suffer.",
					costColor,
					Misc.getDGSCredits(bribe));
			
			textPanel.addPara("You have %s available.", Misc.getHighlightColor(),
					Misc.getDGSCredits(credits));
			
			break;
		case USE_CONNECTIONS:
			boolean canUseConnections = faction.isAtWorst(Factions.PLAYER, MIN_REP);
			if (canUseConnections) {
				textPanel.addPara("You can use your connections to pull a few strings and ensure the operation " +
						"never gets beyond the planning stages.");
			} else {
				textPanel.addPara("You do not have sufficient connections with " + faction.getPersonNamePrefix() + 
						" officials to stall out this kind of an operation.");
				CoreReputationPlugin.addRequiredStanding(faction, MIN_REP, null, textPanel, null, tc, 0, true);
			}
			
			CoreReputationPlugin.addCurrentStanding(faction, null, textPanel, null, tc, 0f);
			
			if (canUseConnections) {
				textPanel.addPara("Calling in these favors will reduce your " +
							 "standing with " + faction.getDisplayNameWithArticle() + " by %s points.", 
							 Misc.getHighlightColor(), "" + (int) Math.round(REP_COST * 100f));
			}
			
			break;
		}
	}
	
	protected void addChoiceOptions() {
		options.clearOptions();

		options.addOption("Allocate sufficient funds for bribes and other means of disrupting the planning", OptionId.BRIBE, null);
		options.addOption("Use your connections to disrupt the planning", OptionId.USE_CONNECTIONS, null);
		
		dialog.setOptionColor(OptionId.BRIBE, Misc.getStoryOptionColor());
		
		options.addOption("Dismiss", OptionId.LEAVE, null);
		options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	protected void addDismissOption() {
		options.clearOptions();
		options.addOption("Dismiss", OptionId.LEAVE, null);
		options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	protected OptionId beingConfirmed = null;
	protected void addConfirmOptions() {
		if (beingConfirmed == null) return;
		
		options.clearOptions();
		
		printOptionDesc(beingConfirmed);
		
		options.addOption("Take the necessary actions", OptionId.CONFIRM, null);
		options.addOption("Never mind", OptionId.CANCEL, null);
		options.setShortcut(OptionId.CANCEL, Keyboard.KEY_ESCAPE, false, false, false, true);

		if (beingConfirmed == OptionId.BRIBE) {

			SetStoryOption.set(dialog, 1, OptionId.CONFIRM, "bribePunitiveExpedition", Sounds.STORY_POINT_SPEND_INDUSTRY,
					"Issued bribe to avert " + intel.getFaction().getDisplayName() + " punitive expedition");
			
			int bribe = computeBribeAmount();
			if (bribe > playerFleet.getCargo().getCredits().get()) {
				options.setEnabled(OptionId.CONFIRM, false);
				options.setTooltip(OptionId.CONFIRM, "Not enough credits.");
			}
		} else if (beingConfirmed == OptionId.USE_CONNECTIONS) {
			FactionAPI faction = intel.getFaction();
			boolean canUseConnections = faction.isAtWorst(Factions.PLAYER, MIN_REP);
			if (!canUseConnections) {
				options.setEnabled(OptionId.CONFIRM, false);
				options.setTooltip(OptionId.CONFIRM, "Standing not high enough.");
			}
		}
	}
	
	
	public void printInit() {
		TooltipMakerAPI info = textPanel.beginTooltip();
		info.setParaSmallInsignia();
		intel.addInitialDescSection(info, 0);
		textPanel.addTooltip();
		
		textPanel.addPara("The operation is still in the planning stages, " +
				"and you have several options at your disposal to ensure it never gets off the ground.");
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
		}
		
		switch (option) {
		case INIT:
			printInit();
			addChoiceOptions();
			break;
		case BRIBE:
			beingConfirmed = OptionId.BRIBE;
			addConfirmOptions();
			break;
		case USE_CONNECTIONS:
			beingConfirmed = OptionId.USE_CONNECTIONS;
			addConfirmOptions();
			break;
		case CONFIRM:
			if (beingConfirmed == OptionId.BRIBE) {
				int bribe = computeBribeAmount();
				AddRemoveCommodity.addCreditsLossText(bribe, textPanel);
				playerFleet.getCargo().getCredits().subtract(bribe);
			} else if (beingConfirmed == OptionId.USE_CONNECTIONS) {
				CustomRepImpact impact = new CustomRepImpact();
				impact.delta = -REP_COST;
				ReputationAdjustmentResult repResult = Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, 
								impact, null, textPanel, false, true),
								intel.getFaction().getId());
			}
			intel.getOrganizeStage().abort();
			intel.setOutcome(PunExOutcome.AVERTED);
			intel.forceFail(false);
			intel.sendUpdate(PunitiveExpeditionIntel.OUTCOME_UPDATE, textPanel);
			addDismissOption();
			break;
		case CANCEL:
			addChoiceOptions();
			break;
		case LEAVE:
			leave();
			break;
		}
	}
	
	protected void leave() {
		dialog.dismiss();
		ui.updateUIForItem(intel);	
	}
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



