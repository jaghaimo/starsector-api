package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicPathBaseBarEvent extends BaseBarEventWithPerson {
	
	public static enum OptionId {
		INIT,
		AGREE,
		HIRE,
		HIRE_CONTINUE,
		REJECT,
		LEAVE,
		
	}
	
	public static int COST = 10000;
	
	protected LuddicPathBaseIntel intel;
	
	public boolean isAlwaysShow() {
		return true;
	}
	
	public LuddicPathBaseBarEvent(LuddicPathBaseIntel intel) {
		this.intel = intel;
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		for (LuddicPathCellsIntel cell : LuddicPathCellsIntel.getCellsForBase(intel, true)) {
			if (cell.isSleeper()) continue;
			if (cell.getSleeperTimeout() > 0) continue;
			if (cell.getMarket() == market) return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
	}

	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		super.regen(market);
//		random = new Random(seed + market.getId().hashCode());
//		person = createPerson();
	}
	
	@Override
	protected PersonAPI createPerson() {
		FactionAPI faction = Global.getSector().getFaction(Factions.LUDDIC_PATH);
		int level = 1;
		PersonAPI person = OfficerManagerEvent.createOfficer(faction, level, SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE, 
				true, null, true, false, 0, random);
		return person;
	}
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		regen(dialog.getInteractionTarget().getMarket());
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A " + getManOrWoman() + " with Pather tattoos is staring at you from across the bar " +
				"with a desperate gleam in " + getHisOrHer() + " eyes.");

		dialog.getOptionPanel().addOption("Make eye contact with the Pather and walk out into the back alley", this, null);
	}
	
	@Override
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);
		
		done = false;
		dialog.getVisualPanel().showPersonInfo(person, true);
		
		optionSelected(null, OptionId.INIT);
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if (!(optionData instanceof OptionId)) {
			return;
		}
		OptionId option = (OptionId) optionData;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		options.clearOptions();
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		int credits = (int) cargo.getCredits().get();
		
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();
		//COST = 10000;
		
		switch (option) {
		case INIT:
			text.addPara("After a minute or two of waiting in the alley, the door opens and the Pather " +
					"walks out.");
			text.addPara("\"I know who you are\", " + getHeOrShe() + " says. \"I need your help. If they find " +
					"out what I've done... well, let's just say my future with the Path is likely to be a short one. " +
					"So, how about this: you arrange a new identity for me, and I tell you where " +
					"the base supplying the Pather cells at this colony is located.\"");
			
			boolean canAccept = COST <= credits;
			LabelAPI label = text.addPara("You estimate that doing as " + getHeOrShe() + 
								" asks will run you about %s. You have %s available.",
					h,
					Misc.getDGSCredits(COST),	
					Misc.getDGSCredits(credits));
			label.setHighlightColors(canAccept ? h : n, h);
			label.setHighlight(Misc.getDGSCredits(COST), Misc.getDGSCredits(credits));
			
			options.addOption("Agree to " + getHisOrHer() + " terms", OptionId.AGREE);
			if (!canAccept) {
				options.setEnabled(OptionId.AGREE, false);
				options.setTooltip(OptionId.AGREE, "Not enough credits.");
			}
			addStoryOption();
			options.addOption("Suggest that " + getHeOrShe() + " handle " + getHisOrHer() + " own problems", OptionId.REJECT);
			break;
		case AGREE:
			text.addPara("You agree to the " + getManOrWoman() + "'s terms and make the necessary arrangements.");
			
			cargo.getCredits().subtract(COST);
			AddRemoveCommodity.addCreditsLossText(COST, dialog.getTextPanel());
			
			done = true;
			intel.makeKnown();
			intel.sendUpdate(PirateBaseIntel.DISCOVERED_PARAM, text);
			
			PortsideBarData.getInstance().removeEvent(this);
			options.addOption("Continue", OptionId.LEAVE);
			break;
		case REJECT:
			text.addPara("You leave the back alley and return to the bar.");
			
			options.addOption("Continue", OptionId.LEAVE);
			break;
		case LEAVE:
			noContinue = true;
			done = true;
			break;
		}
	}
	
	protected void addStoryOption() {
		String id = "join_id";
		options.addOption("Offer for " + getHimOrHer() + " to join your fleet instead", id);
		
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		int num = Misc.getNumNonMercOfficers(playerFleet);
		int max = Misc.getMaxOfficers(playerFleet);
		
		if (num >= max) {
			options.setEnabled(id, false);
			options.setTooltip(id, "Maximum number of officers reached.");
		}
		
		StoryOptionParams params = new StoryOptionParams(id, 1, "patherJoinFleet", Sounds.STORY_POINT_SPEND_LEADERSHIP,
				"Allowed " + person.getNameString() + ", a Luddic Path defector, to join your fleet");
		
		SetStoryOption.set(dialog, params, 
			new BaseOptionStoryPointActionDelegate(dialog, params) {

				@Override
				public void confirm() {
					super.confirm();
					
					text.addPara(Misc.ucFirst(getHeOrShe()) + " is mistrustful at first, but that turns to surprise and " +
							"gratitude when " + getHeOrShe() + " realizes you're serious.");
					
					CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
					playerFleet.getFleetData().addOfficer(getPerson());
					getPerson().setPostId(Ranks.POST_OFFICER);
					AddRemoveCommodity.addOfficerGainText(getPerson(), dialog.getTextPanel());
					
					done = true;
					intel.makeKnown();
					intel.sendUpdate(PirateBaseIntel.DISCOVERED_PARAM, text);
					
					PortsideBarData.getInstance().removeEvent(LuddicPathBaseBarEvent.this);
					options.addOption("Continue", OptionId.LEAVE);
					
					OptionPanelAPI options = dialog.getOptionPanel();
					options.clearOptions();
					options.addOption("Continue", OptionId.LEAVE);
				}
				
				@Override
				public String getTitle() {
					//return "Taking on a new officer";
					return "Taking in Luddic Path defector";
					//return null;
				}

				@Override
				public void createDescription(TooltipMakerAPI info) {
					float opad = 10f;
					
					info.addSpacer(-opad);
					
					MutableCharacterStatsAPI stats = person.getStats();
					//TextPanelAPI text = dialog.getTextPanel();
					TooltipMakerAPI text = info;
					
					//text.setFontSmallInsignia();
					
					Color hl = Misc.getHighlightColor();
					Color red = Misc.getNegativeHighlightColor();
					
//					text.addPara("You consider letting the Pather join your fleet. It's taking a risk, but " +
//							"something makes you feel that " + getHeOrShe() + " would be " +
//							"trustworthy if given a second chance.", opad);
					
					text.addPara("You consider letting the Luddic Path defector, " + person.getNameString() + ", join your fleet.", opad);
					
					//text.addPara("----------------------------------------------------------------------------------", opad);
					
					text.addPara("Level: %s", opad, hl, "" + (int) stats.getLevel());
					//text.highlightInLastPara(hl, "" + (int) stats.getLevel());
					
					for (String skillId : Global.getSettings().getSortedSkillIds()) {
						int level = (int) stats.getSkillLevel(skillId);
						if (level > 0) {
							SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
							String skillName = spec.getName();
							if (spec.isAptitudeEffect()) {
								skillName += " Aptitude";
							}
							
							if (level <= 1) {
								text.addPara(skillName, opad);
							} else {
								text.addPara(skillName + " (Elite)", opad);
							}
							//text.highlightInLastPara(hl, "" + level);
						}
					}
					
					String personality = Misc.lcFirst(person.getPersonalityAPI().getDisplayName());
					text.addPara("Personality: %s", opad, Misc.getHighlightColor(), personality);
					text.addPara(person.getPersonalityAPI().getDescription(), opad);
					
					//text.addPara("----------------------------------------------------------------------------------", opad);
					
					info.addSpacer(opad * 2f);
					addActionCostSection(info);
				}
			
		});
	}

	@Override
	protected String getPersonFaction() {
		return Factions.LUDDIC_PATH;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected String getPersonPortrait() {
		return null;
	}
	
	@Override
	protected Gender getPersonGender() {
		return Gender.ANY;
	}

	
}



