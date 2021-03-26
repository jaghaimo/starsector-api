package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ScientistAICoreBarEvent extends BaseGetCommodityBarEvent {
	
	public ScientistAICoreBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		
		if (market.getFactionId().equals(Factions.LUDDIC_CHURCH) ||
				market.getFactionId().equals(Factions.LUDDIC_PATH)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		super.regen(market);
	}

	@Override
	protected void doStandardConfirmActions() {
		// we want to do nothing here, real work in doConfirmActionsPreAcceptText()
	}
	
	@Override
	protected void doConfirmActionsPreAcceptText() {
		// spawn entity with AI core etc here
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		TextPanelAPI text = dialog.getTextPanel();
		
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_MAIN)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_RESURGENT)) continue;
			if (system.hasTag(Tags.THEME_HIDDEN)) continue;
			
//			if (system.getStar() != null && system.getStar().getSpec().isBlackHole()) continue;
//			if (system.getSecondary() != null && system.getSecondary().getSpec().isBlackHole()) continue;
//			if (system.getTertiary() != null && system.getTertiary().getSpec().isBlackHole()) continue;
			
			float sinceVisit = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (sinceVisit < 60) continue;
			
			//if (!Misc.getMarketsInLocation(system).isEmpty()) continue;
			
			picker.add(system);
		}
		
		StarSystemAPI system = picker.pick();
		if (system == null) {
			doDataFail();
			return;
		}

		//EntityLocation loc = BaseThemeGenerator.pickHiddenLocation(random, system, 100f, null);
		EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(random, system, 100f, null);
		if (loc == null) {
			doDataFail();
			return;
		}
		
		//AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.STABLE_LOCATION, Factions.NEUTRAL);
		AddedEntity added = BaseThemeGenerator.addEntity(random, system, loc, Entities.TECHNOLOGY_CACHE, Factions.NEUTRAL);
		
		if (added == null || added.entity == null) {
			doDataFail();
			return;
		}
		//added.entity.setName("Technology Cache");
		
		//added.entity.removeTag(Tags.SALVAGEABLE);
		added.entity.setDiscoverable(null);
		added.entity.setDiscoveryXP(null);
		added.entity.setSensorProfile(null);
		
		added.entity.addTag(Tags.EXPIRES); // so it doesn't get targeted by "analyze entity" missions
		
		ScientistAICoreIntel intel = new ScientistAICoreIntel(added.entity, this);
		//intel.setImportant(true);
		Global.getSector().getIntelManager().addIntel(intel, false, text);
	}

	protected transient boolean failed = false;
	protected void doDataFail() {
		failed = true;
	}

	@Override
	protected String getPersonFaction() {
		return Factions.INDEPENDENT;
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
	protected float getPriceMult() {
		return 0;
	}
	
	@Override
	protected String getPrompt() {
		return "A disheveled " + getManOrWoman() + " in academic uniform is bothering some of the " +
				"better-dressed spacers with " + getHisOrHer() + " glowing TriPad.";
	}
	
	@Override
	protected String getOptionText() {
		return "Flag down the academic with the TriPad";
	}
	
	@Override
	protected String getMainText() {
		return Misc.ucFirst(getHeOrShe()) + " turns out to be a scientist, maybe a \"scientist\", " +
			"and is delighted to have someone finally listen to " + getHisOrHer() + " story. " +
			"You set your face to a practiced stern-but-receptive look as " +
			"you hear " + getHisOrHer() + " pitch through to the end.\n\n" +

			"\"The data doesn’t lie!\" " + getHeOrShe() + " cries again and again, " +
			"though you can’t make sense of the arcane tables scrolling " +
			"on " + getHisOrHer() + " greasy TriPad. \"There’s an enormous stock of " +
			"unrecovered Domain-era technology in this system. No one else can see it! " +
			"All I ask is,\" " + getHisOrHer() + " voice drops to a whisper, " +
			"\"you retrieve for me the AI core hidden in the midst of the trove. " +
			"That, I get to keep. I’ll tell you exactly where to look if you agree to my terms.\"";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		return new String [] {};
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] {};
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept and promise to ship the AI core to the given address in a specially shielded crate";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline " + getHisOrHer() + " proposal and walk away";
	}

	@Override
	protected String getAcceptText() {
		if (failed) {
			return "Unfortunately, a closer analysis of the data brings to light several glaring inconsistencies. " +
					"Looks like " + getHeOrShe() + " was a bit unhinged, after all - " +
					"there's nothing here worth investigating.";
		}
		return null;
	}
	
	@Override
	protected String getDeclineText() {
		return "You hear the scientist's offended, quavering voice at your back, \"The truth is out there! " +
				"I won't be held back by small-minds like all of you!\" Most of the other patrons pretend extreme " +
				"interest in their drinks, though at least one looses a mocking laugh.";
	}

	
	protected boolean showCargoCap() {
		return false;
	}
}



