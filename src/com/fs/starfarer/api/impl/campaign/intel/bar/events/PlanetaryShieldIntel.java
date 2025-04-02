package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class PlanetaryShieldIntel extends BaseIntelPlugin {

	public static enum PSIStage {
		TALK_TO_PILOT,
		GO_TO_PLANET,
		DONE,
		;
	}
	
	public static int FINISHED_XP = 20000;
	public static int PAY_PILOT_XP = 5000;
	
	protected PlanetAPI planet;
	protected PlanetaryShieldBarEvent event;
	
	protected PSIStage stage;
	protected int pilotCredits;
	
	public PlanetaryShieldIntel(PlanetAPI planet, PlanetaryShieldBarEvent event) {
		this.planet = planet;
		this.event = event;
		
		PersonAPI pilot = event.getPilot();
		Misc.makeImportant(pilot, "psi");
		MarketAPI market = event.getPilotMarket();
		market.addPerson(pilot);
		market.getCommDirectory().addPerson(pilot);
		
		pilotCredits = 10000 + 1000 * Misc.random.nextInt(10);
		
		pilot.getMemoryWithoutUpdate().set("$psi_isPilot", true);
		pilot.getMemoryWithoutUpdate().set("$psi_eventRef", this);
		pilot.getMemoryWithoutUpdate().set("$psi_credits", Misc.getDGSCredits(pilotCredits));
		
		//Misc.makeImportant(planet, "saci");
		//cache.getMemoryWithoutUpdate().set("$saic_eventRef", this);
		//Global.getSector().addScript(this);
		
		stage = PSIStage.TALK_TO_PILOT;
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
		
		PersonAPI pilot = event.getPilot();
		MarketAPI market = event.getPilotMarket();
		market.removePerson(pilot);
		market.getCommDirectory().removePerson(pilot);
		Misc.makeUnimportant(planet, "psi");
	}



	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog,
							 List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		//MemoryAPI memory = planet.getMemoryWithoutUpdate();
		
		PersonAPI pilot = event.getPilot();
		MarketAPI market = event.getPilotMarket();
		
		if (action.equals("prepare")) {
			pilot.getMemoryWithoutUpdate().set("$psi_credits", Misc.getDGSCredits(pilotCredits), 0);
			pilot.getMemoryWithoutUpdate().set("$psi_playerCredits", Misc.getDGSCredits(cargo.getCredits().get()), 0);
		} else if (action.equals("canPay")) {
			return cargo.getCredits().get() >= pilotCredits;
		} else if (action.equals("payPilot")) {
			market.removePerson(pilot);
			market.getCommDirectory().removePerson(pilot);
			
			cargo.getCredits().subtract(pilotCredits);
			AddRemoveCommodity.addCreditsLossText(pilotCredits, dialog.getTextPanel());
			Global.getSector().getPlayerPerson().getStats().addXP(PAY_PILOT_XP, dialog.getTextPanel());

			Misc.makeImportant(planet, "psi");
			stage = PSIStage.GO_TO_PLANET;
			sendUpdate(PSIStage.GO_TO_PLANET, dialog.getTextPanel());
		}
		
		return true;
	}
	
	@Override
	public void endAfterDelay() {
		stage = PSIStage.DONE;
		Misc.makeUnimportant(planet, "psi");
		super.endAfterDelay();
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
	}


	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		MarketAPI market = event.getPilotMarket();
		
		if (stage == PSIStage.TALK_TO_PILOT) {
			info.addPara("Talk to the pilot at %s", initPad, tc, market.getFaction().getBaseUIColor(), market.getName());
		} else if (stage == PSIStage.GO_TO_PLANET) {
			info.addPara("Explore the planet", tc, initPad);
		}
		
		initPad = 0f;
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.setParaSmallInsignia();
		info.addPara(getName(), c, 0f);
		info.setParaFontDefault();
		addBulletPoints(info, mode);
		
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		if (stage == PSIStage.TALK_TO_PILOT) {
			info.addPara("An old spacer told you a tale about a mysterious red planet. " +
						 "The pilot - the only other survivor of the salvage expedition - may know the planet's location.", opad);
		} else if (stage == PSIStage.GO_TO_PLANET) {
			info.addPara("You've talked to the old spacer's pilot comrade and convinced " +
						 "them to divulge the location of the planet.", opad);
		} else {
			info.addPara("You've found the planet and uncovered its secret.", opad);
		}

		addBulletPoints(info, ListInfoMode.IN_DESC);
		
	}
	
	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "red_planet");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_STORY);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(Tags.INTEL_ACCEPTED);
		tags.add(Tags.INTEL_MISSIONS);
		return tags;
	}
	
	@Override
	public IntelSortTier getSortTier() {
		return IntelSortTier.TIER_2;
	}

	public String getSortString() {
		return "Red Planet";
	}
	
	public String getName() {
		if (isEnded() || isEnding()) {
			return "Red Planet - Completed";
		}
		return "Red Planet";
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (stage == PSIStage.TALK_TO_PILOT) {
			return event.getPilotMarket().getPrimaryEntity();
		}
		return planet;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
	}

	@Override
	public String getCommMessageSound() {
		return getSoundMajorPosting();
	}
		
}







