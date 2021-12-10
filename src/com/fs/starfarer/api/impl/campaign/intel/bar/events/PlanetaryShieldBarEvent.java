package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PlanetaryShieldBarEvent extends BaseBarEventWithPerson {
	
	public static enum OptionId {
		INIT,
		APOLOGIZE,
		CONTINUE_1,
		CONTINUE_2,
		WHERE_WAS_SYSTEM,
		CONTINUE_3,
		LEAVE,
		
	}
	
	public static PlanetAPI getTargetPlanet() {
		return (PlanetAPI) Global.getSector().getMemoryWithoutUpdate().get(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET_KEY);
	}
	
	
	public PlanetaryShieldBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		
		
		if (market.getFactionId().equals(Factions.LUDDIC_CHURCH) ||
				market.getFactionId().equals(Factions.LUDDIC_PATH)) {
			return false;
		}
		
		if (getTargetPlanet() == null) return false;
		
		if (Global.getSector().getPlayerStats().getLevel() < 10 && !DebugFlags.BAR_DEBUG) return false;
		
		return true;
	}
	
	protected PersonAPI pilot;
	protected MarketAPI pilotMarket = null;
	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		super.regen(market);

		if (person.getGender() == Gender.MALE) {
			person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "old_spacer_male"));
		} else {
			person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "old_spacer_female"));
		}
		
		pilot = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(random);
		pilot.setRankId(Ranks.PILOT);
		pilot.setPostId(Ranks.POST_CITIZEN);
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsInGroup(null)) {
			if (curr == market) continue;
			if (curr.isPlayerOwned()) continue;
			if (curr.isHidden()) continue;
			if (curr.getStabilityValue() <= 0) continue;
			
			float w = curr.getSize();
			if (curr.isFreePort()) w += 10f;
			picker.add(curr, w);
		}
		
		if (picker.isEmpty()) picker.add(market, 1f);
		
		pilotMarket = picker.pick();
	}
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		regen(dialog.getInteractionTarget().getMarket());
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A rough-looking veteran spacer with cybernetic eyes seems to be staring intently at you; " +
					 "it's a little unnerving.");

//		Color c = Misc.getHighlightColor();
//		c = Misc.getHighlightedOptionColor();
		
		dialog.getOptionPanel().addOption("Ask the veteran spacer what " + getHeOrShe() + "'s looking at", this, 
				null);
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
		
//		continue
//		> Offer to apologize for the misunderstanding by buying $himOrHer a drink.
//
//		exit
//		> Suggest that they read the manual then leave.
		
		switch (option) {
		case INIT:
			text.addPara("\"Ain't nothing, I'm just reading my mail,\" " + getHeOrShe() + 
						 " growls back. Then laughs, and taps " + getHisOrHer() + " temple. " +
						 "\"Swear I'll never get proper used to these things.\"");
			options.addOption("Offer to apologize for the misunderstanding by buying " + getHimOrHer() + " a drink", OptionId.APOLOGIZE);
			options.addOption("Suggest that " + getHeOrShe() + " read the manual then leave", OptionId.LEAVE);
			break;
		case APOLOGIZE:
			text.addPara("The old space-hand opens up as one drink turns to two or three. " + 
					Misc.ucFirst(getHeOrShe()) + " tells stories of old injuries earned, including that of " +
					getHisOrHer() + " lost eyes.\n\n\"Was just two survivors from that mission, me and the pilot. " +
					"Lucky bastards, we two.\" It all began when " + getHeOrShe() + " was hired on to " +
					"a salvage fleet trawling decivilized systems outside the Core Worlds. " +
					"The loot was good, and any brigand they couldn't fight, they could flee." +
					"\n\nThen the officers found something new in a data-cache. " +
					"Rumours flew among the crew, who were told nothing, as the fleet suddenly left a rich " +
					"asteroid belt to make for the outer system jump-point. " +
					"They fared two hyperspace storms before arriving at a distant star system.");
			
			options.addOption("Continue", OptionId.CONTINUE_1);
			break;
		case CONTINUE_1:
			text.addPara("There they found a planet all shining and red. \"It weren't anything natural. " +
					"It was all... shapes, angles, glowing like plasma. That's truth,\" " + getHeOrShe() + 
					" says quietly, \"I've seen Gates, sure, and orbital works big as you like. Ain't " +
					"never seen anything Domain-made glow like that across a whole planet's face. " +
				    "Not anything that weren't a weapon, I mean.\"");
			
			options.addOption("Continue", OptionId.CONTINUE_2);
			break;
		case CONTINUE_2:
			text.addPara("\"While we were gawkin', the prox alarm goes and it's battle stations\", " + getHeOrShe() + 
						 " continues. Hostile ships came fast upon the salvage fleet, flitting with agility " +
						 "belying advanced tech. They had equally advanced weapons, too, and with those they " +
						 "attacked with no mercy. \"It weren't pirates, nor military,\" here the spacer loses " + 
						 getHisOrHer() + " cheer at a good story. \"Ludd's hells, I swear to you it weren't anything human.\"");

			text.addPara("The spacer explains that just " + getHeOrShe() + " and the pilot got away in an " +
						 "escape pod which was only \"mostly\" malfunctioning. \"The miracle wasn't that I " +
						 "fixed it, it's that they could thaw enough of me out at the end of it for me to keep " +
						 "livin', if you call this livin'.\"");

			options.addOption("\"Where was this system with the red planet?\"", OptionId.WHERE_WAS_SYSTEM);
			break;
		case WHERE_WAS_SYSTEM:
			text.addPara("You emphasize your interest in the subject by having the spacer's drink refreshed. " +
					Misc.ucFirst(getHeOrShe()) + " shakes " + getHisOrHer() + " head, \"Captain, " +
					"I wouldn't wish my fate on you or anyone. Besides, I have no idea.\" A pause, then, " +
					"\"" + pilot.getNameString() + " would - that's the pilot,\" " + getHeOrShe() + " finally admits.");

			text.addPara("The old spacer tells you that " + pilot.getName().getFirst() + " did not live the experience in such " +
						 "stride as " + getHimOrHerself() + ", and has taken to drinking themselves senseless " +
						 "in semi-retirement " + pilotMarket.getOnOrAt() + " " + pilotMarket.getName() + ". " +
						 "\"Some folks, I don't think they react well " +
						 "to the emergency cryo-pods. Like a bit o' their brain is still froze up and not coming back.\"");
			
			String icon = Global.getSettings().getSpriteName("intel", "red_planet");
			Set<String> tags = new LinkedHashSet<String>();
			tags.add(Tags.INTEL_MISSIONS);
			
			dialog.getVisualPanel().showMapMarker(pilotMarket.getPrimaryEntity(), 
						"Destination: " + pilotMarket.getName(), pilotMarket.getFaction().getBaseUIColor(), 
						true, icon, null, tags);
			
			options.addOption("Continue", OptionId.CONTINUE_3);
			break;
		case CONTINUE_3:
			text.addPara("You consider a trip to " + pilotMarket.getName() + " to see if you can get the exact location of " +
						 "this mysterious planet with its unknown technology. You also realize that the old spacer " +
						 "has fallen asleep in " + getHisOrHer() + " seat, cybernetic eyes blanked out in standby mode.");

			BarEventManager.getInstance().notifyWasInteractedWith(this);
			addIntel();
			
			options.addOption("Leave the old spacer to " + getHisOrHer() + " rest", OptionId.LEAVE);
			break;
		case LEAVE:
			noContinue = true;
			done = true;
			break;
		}
	}
	

	protected void addIntel() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		TextPanelAPI text = dialog.getTextPanel();
		
		PlanetAPI planet = getTargetPlanet();
		boolean success = false;
		if (planet != null) {
			PlanetaryShieldIntel intel = new PlanetaryShieldIntel(planet, this);
			if (!intel.isDone()) {
				Global.getSector().getIntelManager().addIntel(intel, false, text);
				success = true;
			}
		}
		
		if (!success) {
			text.addPara("For a minute there, you were caught by the story, but you now see that following up " +
						 "on it would be a fool's errand.");
		}
	}

	@Override
	protected String getPersonFaction() {
		return Factions.INDEPENDENT;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.SPACE_SAILOR;
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

	public PersonAPI getPilot() {
		return pilot;
	}

	public MarketAPI getPilotMarket() {
		return pilotMarket;
	}
	
}


