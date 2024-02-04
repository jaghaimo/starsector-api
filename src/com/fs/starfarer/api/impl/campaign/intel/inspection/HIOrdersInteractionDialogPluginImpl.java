package com.fs.starfarer.api.impl.campaign.intel.inspection;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.events.HegemonyHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel.AntiInspectionOrders;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.util.Misc;

public class HIOrdersInteractionDialogPluginImpl implements InteractionDialogPlugin {

	//public static float BRIBE_BASE = 0;
	public static int BRIBE_MULT = 200000;
	public static int BRIBE_MAX = 500000;
	
	private static enum OptionId {
		INIT,
		COMPLY,
		BRIBE,
		RESIST,
		LEAVE,
		
		CONFIRM,
		CANCEL,
		
		HIDE, // too similar to "bribe"; not adding
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;

	protected HegemonyInspectionIntel intel;
	protected IntelUIAPI ui;
	
	public HIOrdersInteractionDialogPluginImpl(HegemonyInspectionIntel intel, IntelUIAPI ui) {
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
//		float threshold = HegemonyInspectionManager.getInstance().getThreshold();
//		int bribe = (int) Math.round(BRIBE_BASE + threshold * BRIBE_MULT);
//		return bribe;
		
		//int bribe = (int) (Math.pow(1.5f, HegemonyInspectionManager.getInstance().getNumAttempts()) * BRIBE_MULT);
		int bribe = (int) (Math.pow(1.5f, HegemonyHostileActivityFactor.getInspectionAttempts()) * BRIBE_MULT);
		if (bribe > BRIBE_MAX) bribe = BRIBE_MAX;
		return bribe;
		
//		int bribe = 20000;
//		for (String id : intel.getExpectedCores()) {
//			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(id);
//			bribe += spec.getBasePrice() * 2;
//		}
//		return bribe;
	}
	
	protected void printOptionDesc(AntiInspectionOrders orders, boolean inConfirm) {
		switch (orders) {
		case BRIBE:
			int bribe = computeBribeAmount();
			textPanel.addPara("Sufficient funding allocated to proper official and unofficial actors should " +
					"ensure that the inspection reaches a satisfactory outcome.");

			if (inConfirm) {
				textPanel.addPara("Once this order is given and the funds and agents dispatched, it can not be " +
								  "rescinded.");
				
				int credits = (int) playerFleet.getCargo().getCredits().get();
				Color costColor = Misc.getHighlightColor();
				if (bribe > credits) costColor = Misc.getNegativeHighlightColor();
				
//				textPanel.addPara("A total of %s should be enough to get the job done. " +
//					"It's more expensive than the cores involved, " +
//					"but guarantees that your standing with the Hegemony will not suffer.", costColor,
//							  	Misc.getDGSCredits(bribe));
				textPanel.addPara("A total of %s should be enough to get the job done. " +
						"and guarantees that your standing with the Hegemony will not suffer.", costColor,
						Misc.getDGSCredits(bribe));
				
				textPanel.addPara("You have %s available.", Misc.getHighlightColor(),
						Misc.getDGSCredits(credits));
			} else {
				textPanel.addPara("You've allocated %s to the task and have otherwise committed to this course of action.",
						 		  Misc.getHighlightColor(), Misc.getDGSCredits(bribe));
//				textPanel.addPara("You've allocated %s to the task. Giving different orders will allow you to recover these funds.", Misc.getHighlightColor(),
//						Misc.getDGSCredits(bribe));
			}
			break;
		case COMPLY:
			textPanel.addPara("The local authorities will comply with the inspection. This will result " +
					"in all AI cores being found and confiscated, and will cause your standing with the Hegemony " +
					"to fall based on the nuber of AI cores found.");
			textPanel.addPara("If AI cores currently in use are removed or moved off-planet, this activity will " +
					"surely leave traces for inspectors to find, inspiring them to much greater zeal.");
			break;
		case RESIST:
			textPanel.addPara("All space and ground forces available will resist the inspection.");
			textPanel.addPara("If the inspection reaches the surface, " +
							 "the ground defense strength will determine whether " +
							 "they're able to confiscate any AI cores.");
			textPanel.addPara("The Hegemony will become aware of this - and hostile - when the inspection task force " +
							  "enters the star system."); 
			break;
		}
	}
	
	protected void addChoiceOptions() {
		options.clearOptions();

		AntiInspectionOrders curr = intel.getOrders();
		if (curr != AntiInspectionOrders.BRIBE) {
			options.addOption("Order the local authorities to comply with the inspection", OptionId.COMPLY, null);
			options.addOption("Allocate sufficient funds to bribe or otherwise handle the inspectors", OptionId.BRIBE, null);
			options.addOption("Order your local forces to resist the inspection", OptionId.RESIST, null);
	
			dialog.setOptionColor(OptionId.BRIBE, Misc.getStoryOptionColor());
		}
		
		options.addOption("Dismiss", OptionId.LEAVE, null);
		options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
		
		if (curr == AntiInspectionOrders.COMPLY) {
			options.setEnabled(OptionId.COMPLY, false);
		}
		if (curr == AntiInspectionOrders.BRIBE) {
			options.setEnabled(OptionId.BRIBE, false);
		}
		if (curr == AntiInspectionOrders.RESIST) {
			options.setEnabled(OptionId.RESIST, false);
		}
	}
	
	protected AntiInspectionOrders beingConfirmed = null;
	protected void addConfirmOptions() {
		if (beingConfirmed == null) return;
		
		options.clearOptions();
		
		printOptionDesc(beingConfirmed, true);
		
		options.addOption("Confirm your orders", OptionId.CONFIRM, null);
		options.addOption("Never mind", OptionId.CANCEL, null);
		options.setShortcut(OptionId.CANCEL, Keyboard.KEY_ESCAPE, false, false, false, true);

		if (beingConfirmed == AntiInspectionOrders.BRIBE) {
			int bribe = computeBribeAmount();
			if (bribe > playerFleet.getCargo().getCredits().get()) {
				options.setEnabled(OptionId.CONFIRM, false);
				options.setTooltip(OptionId.CONFIRM, "Not enough credits.");
			}
			
			SetStoryOption.set(dialog, 1, OptionId.CONFIRM, "bribeAICoreInspection", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
					"Issued bribe to prevent " + intel.getFaction().getDisplayName() + " AI core inspection");
//			StoryOptionParams params = new StoryOptionParams(OptionId.BRIBE, 1, "bribeAICoreInspection", Sounds.STORY_POINT_SPEND_TECHNOLOGY);
//			SetStoryOption.set(dialog, params, new BaseOptionStoryPointActionDelegate(dialog, params) {
//				@Override
//				public void createDescription(TooltipMakerAPI info) {
//					float opad = 10f;
//					info.setParaInsigniaLarge();
//					info.addPara("Virtually guarantees that the inspection will not find any AI cores.",
//							-opad);
//					info.addSpacer(opad * 2f);
//					addActionCostSection(info);
//				}
//			});
		}
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
			printOptionDesc(intel.getOrders(), false);
			addChoiceOptions();
			break;
		case COMPLY:
			beingConfirmed = AntiInspectionOrders.COMPLY;
			addConfirmOptions();
			break;
		case BRIBE:
			beingConfirmed = AntiInspectionOrders.BRIBE;
			addConfirmOptions();
			break;
		case RESIST:
			beingConfirmed = AntiInspectionOrders.RESIST;
			addConfirmOptions();
			break;
		case CONFIRM:
			int invested = intel.getInvestedCredits();
			if (invested > 0) {
				AddRemoveCommodity.addCreditsGainText(invested, textPanel);
				playerFleet.getCargo().getCredits().add(invested);
				intel.setInvestedCredits(0);
			}
			
			intel.setOrders(beingConfirmed);
			if (beingConfirmed == AntiInspectionOrders.BRIBE) {
				int bribe = computeBribeAmount();
				intel.setInvestedCredits(bribe);
				AddRemoveCommodity.addCreditsLossText(bribe, textPanel);
				playerFleet.getCargo().getCredits().subtract(bribe);
			} else {
				
			}
			
			addChoiceOptions();
			//leave();
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
		if (ui != null) ui.updateUIForItem(intel);	
	}
	
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



