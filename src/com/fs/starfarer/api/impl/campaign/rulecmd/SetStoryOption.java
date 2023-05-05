package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseStoryPointActionDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI.OptionTooltipCreator;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetStoryColor <option id> <story points> <bonus XP fraction key> <optional: story point spent sound id> <optional: playthrough log text>
 */
public class SetStoryOption extends BaseCommandPlugin {

//	public static boolean set(InteractionDialogAPI dialog, int points, 
//							String optionId, String bonusXPKey, String soundId) {
//		return set(null, dialog, null, optionId + " " + points + " " + bonusXPKey + " " + soundId);
//		
//	}
	
	public static class StoryOptionParams {
		public Object optionId;
		public int numPoints;
		public String bonusXPID;
		public String soundId;
		public String logText;
		public StoryOptionParams(Object optionId, int numPoints, String bonusXPID, String soundId, String logText) {
			this.optionId = optionId;
			this.numPoints = numPoints;
			this.bonusXPID = bonusXPID;
			this.soundId = soundId;
			this.logText = logText;
		}
		
	}
	
	public static class BaseOptionStoryPointActionDelegate extends BaseStoryPointActionDelegate {
		protected Object optionId;
		protected float bonusXPFraction;
		protected InteractionDialogAPI dialog;
		protected int numPoints;
		protected String soundId;
		protected String logText;
		
		public BaseOptionStoryPointActionDelegate(InteractionDialogAPI dialog, StoryOptionParams params) {
			this.optionId = params.optionId;
			this.bonusXPFraction = Global.getSettings().getBonusXP(params.bonusXPID);
			this.dialog = dialog;
			this.numPoints = params.numPoints;
			this.soundId = params.soundId;
			this.logText = params.logText;
		}
		@Override
		public void preConfirm() {
			if (dialog != null) dialog.addOptionSelectedText(optionId, true);
		}
		@Override
		public void confirm() {
		}
		@Override
		public void createDescription(TooltipMakerAPI info) {
			//info.setParaSmallInsignia();
			info.setParaInsigniaLarge();
			
			addActionCostSection(info);
			
			info.addSpacer(20f);
		}
		
		protected void addActionCostSection(TooltipMakerAPI info) {
			
			int sp = Global.getSector().getPlayerStats().getStoryPoints();
			float pad = -10f;
			float opad = 10f;
			int percent = Math.round(bonusXPFraction * 100f);
			
			if (numPoints == 1) {
				if (percent <= 0) {
					info.addPara("This action requires a %s and does not grant any bonus experience.", 
							pad, Misc.getStoryOptionColor(), "" + Misc.STORY + " point");
				} else {
					info.addPara("This action requires a %s and grants %s bonus experience.", 
							pad, Misc.getStoryOptionColor(), "" + Misc.STORY + " point", "" + percent + "%");
				}
			} else {
				if (percent <= 0) {
					info.addPara("This action requires %s and does not grant any bonus experience.", 
							pad, Misc.getStoryOptionColor(), "" + numPoints + " " + Misc.STORY + " points");
				} else {
					info.addPara("This action requires %s and grants %s bonus experience for each point.", 
							pad, Misc.getStoryOptionColor(), "" + numPoints + " " + Misc.STORY + " points", "" + percent + "%");
				}
			}
			
			if (sp <= 0) {
				info.addPara("No " + Misc.STORY + " points available", Misc.getNegativeHighlightColor(), opad);
			} else if (sp < numPoints) {
				String points = "points";
				if (sp == 1) points = "point";
				info.addPara("Only %s " + Misc.STORY + " " + points + " available", opad, Misc.getNegativeHighlightColor(), 
						Misc.getNegativeHighlightColor(), "" + sp);
			}
			//info.addSpacer(opad);
		}
		
		@Override
		public float getBonusXPFraction() {
			return bonusXPFraction;
		}
		@Override
		public String getConfirmSoundId() {
			String soundId = this.soundId;
			if ("leadership".equals(soundId)) soundId = "ui_char_spent_story_point_leadership";
			if ("combat".equals(soundId)) soundId = "ui_char_spent_story_point_combat";
			if ("industry".equals(soundId)) soundId = "ui_char_spent_story_point_industry";
			if ("technology".equals(soundId)) soundId = "ui_char_spent_story_point_technology";
			if ("general".equals(soundId)) soundId = "ui_char_spent_story_point_combat";
			if ("generic".equals(soundId)) soundId = "ui_char_spent_story_point_combat";
			return soundId;
		}
		@Override
		public int getRequiredStoryPoints() {
			return numPoints;
		}
		//@Override
		public String getLogText() {
			return logText;
		}
		@Override
		public TextPanelAPI getTextPanel() {
			if (dialog == null) return null;
			return dialog.getTextPanel();
		}
		@Override
		public String getTitle() {
			return null;
			//return "Confirm use of story points";
		}
		@Override
		public boolean withSPInfo() {
			return false;
		}
	}
	
	public static boolean set(String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String params) {
		return new SetStoryOption().execute(ruleId, dialog, Misc.tokenize(params), memoryMap);
	}
	
	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		//SetStoryOption gaData_getAvoidCombat 1 gaData_avoidCombat general "Created a distraction"
		//SetStoryOption gaData_getAvoidCombat general "Created a distraction"

		String optionId = "";
		String bonusXPID = "";
		String soundId = "";
		String logText = null;
		int numPoints = 1;
		if (params.size() == 3) {
			optionId = params.get(0).string;
			numPoints = 1;
			bonusXPID = optionId;
			soundId = params.get(1).string;
			logText = params.get(2).getStringWithTokenReplacement(params.get(2).getString(memoryMap), dialog, memoryMap);
		} else {
			optionId = params.get(0).string;
			numPoints = (int) params.get(1).getFloat(memoryMap);
			bonusXPID = params.get(2).getString(memoryMap);
			//final float bonusXPFraction = params.get(2).getFloat(memoryMap);
			
			soundId = params.size() >= 4 ? params.get(3).string : null;
	//		if ("leadership".equals(soundId)) soundId = "ui_char_spent_story_point_leadership";
	//		if ("combat".equals(soundId)) soundId = "ui_char_spent_story_point_combat";
	//		if ("industry".equals(soundId)) soundId = "ui_char_spent_story_point_industry";
	//		if ("technology".equals(soundId)) soundId = "ui_char_spent_story_point_technology";
	//		if ("general".equals(soundId)) soundId = "ui_char_spent_story_point_combat";
			
			if (params.size() >= 5) {
				//logText = params.get(4).getString(memoryMap);
				logText = params.get(4).getStringWithTokenReplacement(params.get(4).getString(memoryMap), dialog, memoryMap);
			}
		}
		
		return set(dialog, numPoints, optionId, bonusXPID, soundId, logText);
	}
	
		
	public static boolean set(final InteractionDialogAPI dialog, final int numPoints, 
			final Object optionId, String bonusXPID, final String soundId, String logText) {
		//final float bonusXPFraction = Global.getSettings().getBonusXP(bonusXPID);
		StoryOptionParams params = new StoryOptionParams(optionId, numPoints, bonusXPID, soundId, logText);
		return set(dialog, params, new BaseOptionStoryPointActionDelegate(dialog, params));
	}
	public static boolean set(final InteractionDialogAPI dialog, final StoryOptionParams params,
				StoryPointActionDelegate delegate) {
				
		final float bonusXPFraction = Global.getSettings().getBonusXP(params.bonusXPID);
		dialog.makeStoryOption(params.optionId, params.numPoints, bonusXPFraction, params.soundId);
		
		if (params.numPoints > Global.getSector().getPlayerStats().getStoryPoints()) {
			dialog.getOptionPanel().setEnabled(params.optionId, false);
		}
		
		dialog.getOptionPanel().addOptionTooltipAppender(params.optionId, new OptionTooltipCreator() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
				float opad = 10f;
				float initPad = 0f;
				if (hadOtherText) initPad = opad;
				tooltip.addStoryPointUseInfo(initPad, params.numPoints, bonusXPFraction, true);
				int sp = Global.getSector().getPlayerStats().getStoryPoints();
				String points = "points";
				if (sp == 1) points = "point";
				tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad, 
						Misc.getStoryOptionColor(), "" + sp);
			}
		});
		
		dialog.getOptionPanel().addOptionConfirmation(params.optionId, delegate);
		
		dialog.getOptionPanel().setStoryOptionParams(params.optionId, params, delegate);
		
		return true;
	}

}
