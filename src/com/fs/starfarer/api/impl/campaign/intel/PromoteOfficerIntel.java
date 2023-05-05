package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PromoteOfficerIntel extends BaseIntelPlugin {
	
	public static String BUTTON_PROMOTE = "button_promote";
	public static String BUTTON_DELETE = "button_delete";
	public static float DURATION = 180f;
	
	protected PersonAPI person;

	public PromoteOfficerIntel(TextPanelAPI text) {
		
		if (Global.getSector().getPlayerFleet().getCargo().getCrew() <= 0) {
			endImmediately();
			return;
		}
		
		person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.PLAYER), 1,
									SkillPickPreference.ANY, true, null, false, false, -1, null);
		person.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(person.getFaction(), person.getGender()));
		if (text != null) {
			text.addPara(getDescText());
		}
		
		setImportant(true);
	}
	
	public boolean shouldRemoveIntel() {
		if (Global.getSector().getPlayerFleet().getCargo().getCrew() <= 0) {
			return true;
		}
		
		float days = getDaysSincePlayerVisible();
		return isEnded() || days >= DURATION;
	}
	
	public String getName() {
		return "Officer Promotion Candidate";
	}
	
	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = getBulletColorForMode(mode);
		
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		String pName = Misc.lcFirst(Misc.getPersonalityName(person));
		
		bullet(info);
		MutableCharacterStatsAPI stats = person.getStats();
		for (String skillId : Global.getSettings().getSortedSkillIds()) {
			int level = (int) stats.getSkillLevel(skillId);
			if (level > 0) {
				SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
				String skillName = spec.getName();
				if (level > 1) {
					skillName += " (Elite)";
				}
				info.addPara("Skill: " + skillName, initPad, tc, h, skillName);
				initPad = 0f;
			}
		}
		info.addPara("Personality: %s", initPad, tc, h, pName);
		unindent(info);
	}
	
	public String getDescText() {
		String themselves = "himself";
		if (person.isFemale()) themselves = "herself";
		return "A junior officer, " + person.getNameString() + ", has distinguished " + themselves + 
				 " recently and is worthy of consideration for " +
				 "the command of a ship.";
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		String pName = Misc.getPersonalityName(person);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(person.getPortraitSprite(), width, 128, opad);
		
		info.addPara(getDescText(), tc, opad);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		info.addPara(person.getPersonalityAPI().getDescription(), opad);
		
		float days = DURATION - getDaysSincePlayerVisible();
		info.addPara("This opportunity will be available for %s more " + getDaysString(days) + ".", 
				opad, tc, h, getDays(days));
		
		int max = Misc.getMaxOfficers(Global.getSector().getPlayerFleet());
		int curr = Misc.getNumNonMercOfficers(Global.getSector().getPlayerFleet());
		
		Color hNum = h;
		if (curr > max) hNum = Misc.getNegativeHighlightColor();
		LabelAPI label = info.addPara("Officers already under your command: %s %s %s", opad, tc, h, 
				"" + curr, "/", "" + max);
		label.setHighlightColors(hNum, h, h);
		
		
		Color color = Misc.getStoryOptionColor();
		Color dark = Misc.getStoryDarkColor();
		
		ButtonAPI button = addGenericButton(info, width, color, dark, "Promote to ship command", BUTTON_PROMOTE);
		button.setShortcut(Keyboard.KEY_T, true);

		info.addSpacer(-10f);
		addDeleteButton(info, width, "Disregard");
//		ButtonAPI delete = addGenericButton(info, width, "Disregard", BUTTON_DELETE);
//		delete.setShortcut(Keyboard.KEY_G, true);
		
		if (curr >= max) {
			button.setEnabled(false);
			//info.addPara("Maximum number of officers reached.", tc, opad);
		}
		
	}
	
	@Override
	protected void createDeleteConfirmationPrompt(TooltipMakerAPI prompt) {
		prompt.addPara("Are you sure? This action can not be undone.", Misc.getTextColor(), 0f);
	}
	
//	@Override
//	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
//		if (buttonId == BUTTON_DELETE) {
//			endImmediately();
//			ui.recreateIntelUI();
//		}
//	}
	
	public void storyActionConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_PROMOTE) {
			endImmediately();
			ui.recreateIntelUI();
		}
	}
	
//	@Override
//	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
//		prompt.setParaInsigniaLarge();
//		if (buttonId == BUTTON_DELETE) {
//			prompt.addPara("Are you sure? Disregarding this promotion opportunity is permanent.", 0f);				
//		} else if (buttonId == BUTTON_DEVELOP) {
//			prompt.addPara("Develop a relationship with this contact?", 0f);
//			return;	
//		}
//		super.createConfirmationPrompt(buttonId, prompt);
//		return;
//	}
//	
//	@Override
//	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
//		if (buttonId == BUTTON_DELETE) return true;
//		if (buttonId == BUTTON_DEVELOP) return true;
//		return super.doesButtonHaveConfirmDialog(buttonId);
//	}
	
	
	public StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId) {
		if (buttonId == BUTTON_PROMOTE) {
			StoryOptionParams params = new StoryOptionParams(null, 1, "promoteCrewMember", 
											Sounds.STORY_POINT_SPEND_LEADERSHIP, 
											"Promoted promising junior officer to ship command");
			return new BaseOptionStoryPointActionDelegate(null, params) {
				@Override
				public void confirm() {
					CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
					playerFleet.getCargo().removeCrew(1);
					
					playerFleet.getFleetData().addOfficer(person);
					person.setPostId(Ranks.POST_OFFICER);
				}
				
				@Override
				public String getTitle() {
					//return "Promoting junior officer to ship command";
					return null;
				}

				@Override
				public void createDescription(TooltipMakerAPI info) {
					info.setParaInsigniaLarge();
					super.createDescription(info);
				}
			};
		}
		return null;
	}


	@Override
	public String getIcon() {
		return person.getPortraitSprite();
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_LOG);
		return tags;
	}

	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
		//return getSoundMajorPosting();
	}
	
	
}



