package com.fs.starfarer.api.impl.campaign.tutorial;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

public class TutorialLevelUpDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		CONT3,
		CONT4,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		//visual.showImagePortion("illustrations", "jump_point_hyper", 640, 400, 0, 0, 480, 300);
		visual.showFleetInfo("Your fleet", playerFleet, null, null);
	
		//dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		
		String control = Global.getSettings().getControlStringForEnumName("CORE_CHARACTER");
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("You've gained a level!");
			textPanel.addPara("Normally you gain %s character point with each level-up, but you " +
					"get an additional %s points at the start of your campaign.",
					Misc.getHighlightColor(), "1", "3");
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			textPanel.addParagraph("You can spend character points to increase aptitudes and skills. Each aptitude governs a set of skills, and " +
					"the maximum level of a skill is limited by the level of the governing aptitude.");
			
			String max = "" + (int) Global.getSettings().getLevelupPlugin().getMaxLevel();
			textPanel.addPara("The maximum level you can reach is %s. Once character points are spent, they can not be refunded.",
					Misc.getHighlightColor(), max);
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT3, null);
			break;
		case CONT2:
//			textPanel.addParagraph("Each aptitude has skills ");
//			options.clearOptions();
//			options.addOption("Continue", OptionId.CONT3, null);
			break;
		case CONT3:
			textPanel.addPara("Press %s to open the character tab and consider your options. You don't " +
					"have to actually spend the points now if you don't want to.",
					Misc.getHighlightColor(), control);
			
			options.clearOptions();
			options.addOption("Finish", OptionId.CONT4, null);
			break;
		case CONT4:
			Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
	}
	

	
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



