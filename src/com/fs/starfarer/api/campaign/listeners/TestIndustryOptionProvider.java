package com.fs.starfarer.api.campaign.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.impl.campaign.PlanetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TestIndustryOptionProvider extends BaseIndustryOptionProvider {

	public static Object INTERACTION_PLUGIN = new Object();
	public static Object INTERACTION_TRIGGER = new Object();
	public static Object CUSTOM_PLUGIN = new Object();
	public static Object STORY_ACTION = new Object();
	public static Object IMMEDIATE_ACTION = new Object();
	public static Object DISABLED_OPTION = new Object();
	
	@Override
	public List<IndustryOptionData> getIndustryOptions(Industry ind) {
		if (isUnsuitable(ind, false)) return null;
		
		
		if (ind.getMarket().getId().equals("jangala") && ind.getId().equals(Industries.POPULATION)) {
			
			List<IndustryOptionData> result = new ArrayList<IndustryOptionData>();
			
			IndustryOptionData opt = new IndustryOptionData("Interaction dialog with plugin", INTERACTION_PLUGIN, ind, this);
			opt.color = Color.GREEN;
			result.add(opt);
			
			opt = new IndustryOptionData("Interaction dialog with rule trigger", INTERACTION_TRIGGER, ind, this);
			opt.color = Color.MAGENTA;
			result.add(opt);
			
			opt = new IndustryOptionData("Custom dialog plugin", CUSTOM_PLUGIN, ind, this);
			opt.color = Color.ORANGE;
			result.add(opt);
			
			opt = new IndustryOptionData("Story point action dialog", STORY_ACTION, ind, this);
			opt.color = Misc.getStoryOptionColor();
			result.add(opt);
			
			opt = new IndustryOptionData("Take an immediate action", IMMEDIATE_ACTION, ind, this);
			opt.color = Color.RED;
			result.add(opt);
			
			opt = new IndustryOptionData("Disabled option", DISABLED_OPTION, ind, this);
			opt.enabled = false;
			result.add(opt);
			
			return result;
		}
		return null;
	}

	@Override
	public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
		if (opt.id == INTERACTION_PLUGIN) {
			tooltip.addPara("This option shows a standard interaction dialog with a custom plugin. "
					+ "In this case, the dialog used is the PlanetInteractionDialogPluginImpl,"
					+ " with Corvus - the star - as the target.", 0f);
		} else if (opt.id == INTERACTION_TRIGGER) {
			tooltip.addPara("This option shows a rule-driven interaction dialog. In this case, "
					+ "the dialog targets the Asharu Terraforming Platform and fires the "
					+ "OpenInteractionDialog trigger to start the interaction.", 0f);
		} else if (opt.id == CUSTOM_PLUGIN) {
			tooltip.addPara("This option shows a custom dialog driven by a CustomDialogDelegate implementation.", 0f);
		} else if (opt.id == STORY_ACTION) {
			tooltip.addPara("This option brings up a dialog where the player may spend some number "
					+ "of story points to perform an action, and possibly receive bonus experience.", 0f);
		} else if (opt.id == IMMEDIATE_ACTION) {
			tooltip.addPara("This option will not show a dialog but instead prints some text to standard out.", 0f);
		} else if (opt.id == DISABLED_OPTION) {
			tooltip.addPara("This option is disabled.", 0f);
		}
	}
	
	@Override
	public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
		if (opt.id == INTERACTION_PLUGIN) {
			PlanetAPI planet = Global.getSector().getStarSystem("corvus").getStar();
			PlanetInteractionDialogPluginImpl plugin = new PlanetInteractionDialogPluginImpl();
			// unpausing when exiting this dialog makes the outer interaction dialog 
			// (for the initial interaction with th market) unresponsive
			// since this is not a valid thing to do anyway - the game should not be unpaused 
			// while the market dialog is open - the cause remains uninvestigated -am
			plugin.setUnpauseOnExit(false);
			ui.showDialog(planet, plugin);
		} else if (opt.id == INTERACTION_TRIGGER) {
			SectorEntityToken station = Global.getSector().getEntityById("corvus_abandoned_station");
			ui.showDialog(station, "OpenInteractionDialog");
		} else if (opt.id == CUSTOM_PLUGIN) {
			CustomDialogDelegate delegate = new BaseCustomDialogDelegate() {
				@Override
				public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
					TooltipMakerAPI info = panel.createUIElement(800f, 500f, false);
					info.addPara("Minimalistic custom dialog implementation.", 0f);
					panel.addUIElement(info).inTL(0, 0);
				}
				
				@Override
				public boolean hasCancelButton() {
					return true;
				}

				@Override
				public void customDialogConfirm() {
					System.out.println("customDialogConfirm() called");
				}

				@Override
				public void customDialogCancel() {
					System.out.println("customDialogCancel() called");
				}
			};
			ui.showDialog(800f, 500f, delegate);
		} else if (opt.id == STORY_ACTION) {
			StoryOptionParams params = new StoryOptionParams(null, 1, "bonusXP_if_any_in_settings.json", 
					Sounds.STORY_POINT_SPEND, 
					"Performed a test action in TestIndustryOptionProvider");
			StoryPointActionDelegate delegate = new BaseOptionStoryPointActionDelegate(null, params) {
				@Override
				public void confirm() {
				}

				@Override
				public String getTitle() {
					return null;
				}
				@Override
				public void createDescription(TooltipMakerAPI info) {
					info.setParaInsigniaLarge();
					info.addPara("Test action that costs one story point.", -10f);
					info.addSpacer(20f);
					super.createDescription(info);
				}
			};
			ui.showDialog(delegate);
		} else if (opt.id == IMMEDIATE_ACTION) {
			System.out.println("IMMEDIATE ACTION TAKEN");
		}
		
	}
	
	@Override
	public void addToIndustryTooltip(Industry ind, IndustryTooltipMode mode, TooltipMakerAPI tooltip, float width, boolean expanded) {
		if (getIndustryOptions(ind) == null) return;
		
		float opad = 10f;
		tooltip.addSectionHeading("TestIndustryOptionProvider", Alignment.MID, opad);
		tooltip.addPara("Information about changes made to the this industry by "
				+ "any of the custom options would go here.", opad);
	}
	
}




