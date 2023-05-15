package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class RecoverAPlanetkiller extends HubMissionWithSearch {

	public static enum Stage {
		GO_TO_NEXUS,
		RECOVER_PK,
		COMPLETED,
		FAILED,
	}
	
	public static boolean startedAtPather() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$pk_startedAtPather");
	}
	public static boolean startedAtNexus() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$pk_startedAtNexus");
	}
	public static boolean gotDataFromMysteryAI() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$pk_gotDataFromMysteryAI");
	}
	public CampaignFleetAPI getNexus() {
		return (CampaignFleetAPI) Global.getSector().getPersistentData().get(MiscellaneousThemeGenerator.PK_NEXUS_KEY);
	}
	public StarSystemAPI getNexusSystem() {
		CampaignFleetAPI nexus = getNexus();
		if (nexus != null && nexus.isAlive()) {
			return nexus.getStarSystem();
		}
		return null;
	}
	public static MarketAPI getTundraMarket() {
		PlanetAPI p = getTundra();
		if (p == null) return null;
		return p.getMarket();
	}
	public static PlanetAPI getTundra() {
		return (PlanetAPI) Global.getSector().getPersistentData().get(MiscellaneousThemeGenerator.PK_PLANET_KEY);
	}
	public static StarSystemAPI getPKSystem() {
		return (StarSystemAPI) Global.getSector().getPersistentData().get(MiscellaneousThemeGenerator.PK_SYSTEM_KEY);
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		setPersonOverride(null);
		
		setStoryMission();
		setNoAbandon();
		
		if (!setGlobalReference("$pk_ref", "$pk_inProgress")) {
			return false;
		}
		
		CampaignFleetAPI nexus = getNexus();
		if (nexus != null && nexus.isAlive()) {
			makeImportant(nexus, "$pk_nexus", Stage.GO_TO_NEXUS);
		}
		
		if (startedAtNexus()) {
			setStartingStage(Stage.RECOVER_PK);
		} else {
			if (nexus == null || !nexus.isAlive() || getNexusSystem() == null) {
				return false;
			}
			setStartingStage(Stage.GO_TO_NEXUS);
		}
		
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
//		setRepFactionChangesVeryHigh();
//		setRepPersonChangesVeryHigh();
		
		setRepPenaltyFaction(0f);
		setRepPenaltyPerson(0f);
		
		connectWithGlobalFlag(Stage.GO_TO_NEXUS, Stage.RECOVER_PK, "$pk_nexusDataGained");
		if (nexus != null) {
			connectWithEntityNotAlive(Stage.GO_TO_NEXUS, Stage.RECOVER_PK, nexus);
		}
		setStageOnGlobalFlag(Stage.COMPLETED, "$pk_completed");

		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$pk_recovered", true);
		triggerSetGlobalMemoryValue("$pk_missionCompleted", true);
		endTrigger();
		
		return true;
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map, Object currentStage) {
		if (currentStage == Stage.RECOVER_PK) {
			return getPKSystem().getCenter();
		}
		return super.getMapLocation(map, currentStage);
	}
	
//	@Override
//	protected void endAbandonImpl() {
//		super.endAbandonImpl();
//		endFailureImpl(null, null);
//	}

	@Override
	protected void endFailureImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		if (currentStage == Stage.FAILED) {
			//KantaCMD.loseProtection(dialog);
		}
	}
	
	
	protected void updateInteractionDataImpl() {
		CampaignFleetAPI nexus = getNexus();
		StarSystemAPI ns = getNexusSystem();
		StarSystemAPI pks = getPKSystem();
		
		if (getCurrentStage() != null) {
			set("$pk_stage", ((Enum)getCurrentStage()).name());
		}
		
		if (ns != null) {
			set("$pk_nexusSystemName", ns.getNameWithLowercaseTypeShort());
		}
		if (pks != null) {
			set("$pk_pkSystemName", pks.getNameWithLowercaseTypeShort());
		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_NEXUS) {
			StarSystemAPI ns = getNexusSystem();
			if (ns != null) {
				info.addPara("Recover information about a Hegemony fleet last seen in a Tri-Tachyon held "
						+ "system during the first AI War, more than a hundred cycles ago. The fleet is rumored to "
						+ "have been carrying a planetkiller weapon.", opad);
				info.addPara("The information should be contained in the data banks of a remnant Nexus"
						+ " located in the " + 
						ns.getNameWithLowercaseType() + ".", opad);
			}
		} else if (currentStage == Stage.RECOVER_PK) {
			StarSystemAPI pks = getPKSystem();
			if (pks != null) {
				if (startedAtNexus()) {
					info.addPara("You've destroyed a remnant Nexus and recovered information about "
							+ "a Hegemony fleet estimated to be carrying a planetkiller weapon.", opad);
					info.addPara("A log entry records "
						+ "a large Ordo sent in pursuit; its last hyperwave report placed it "
						+ "near the " + pks.getNameWithLowercaseTypeShort() + 
						". The log entry dates to the first AI War, more than a hundred cycles ago", opad);
				if (gotDataFromMysteryAI()){
					info.addPara("A mysterious voice, some kind of AI, provided you with coordinates for the " + pks.getNameWithLowercaseTypeShort() +", and implied that the planetkiller weapon could be found there.", opad);
					info.addPara("Perhaps it brings 'much amusement' to this AI to not provide more detailed instructions.", opad);
				}
				} else {
					info.addPara("The data banks of the remnant Nexus contained information about the Hegemony fleet rumored "
							+ "to be carrying a planetkiller weapon. ", opad);
					info.addPara("A log entry records "
							+ "a large Ordo sent in pursuit; its last hyperwave report placed it "
							+ "near the " + pks.getNameWithLowercaseTypeShort() + 
							". The log entry dates to the first AI War, more than a hundred cycles ago", opad);
				}
			}
		} else if (currentStage == Stage.FAILED) {
			// not actually possible to fail
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_NEXUS) {
			StarSystemAPI ns = getNexusSystem();
			if (ns != null) {
				info.addPara("Recover information from remnant Nexus in the " + ns.getNameWithLowercaseTypeShort(), tc, pad);
			}
			return true;
		} else if (currentStage == Stage.RECOVER_PK) {
			StarSystemAPI pks = getPKSystem();
			if (pks != null) {
				info.addPara("Investigate the " + pks.getNameWithLowercaseTypeShort(), tc, pad);
			}
			return true;
		}
		return false;
	}
	
	protected String getMissionTypeNoun() {
		return "task";
	}
	
	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	@Override
	public String getBaseName() {
		return "The Scythe of Orion";
	}
	
//	Global.getSector().getListenerManager().removeListener(this);
//	endAfterDelay();
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		//Global.getSector().getListenerManager().addListener(this);
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		//Global.getSector().getListenerManager().removeListener(this);
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		if (action.equals("showNexusSystem")) {
			StarSystemAPI system = getNexusSystem();
			if (system != null) {
				dialog.getVisualPanel().showMapMarker(system.getHyperspaceAnchor(), 
						system.getNameWithLowercaseTypeShort(), getFactionForUIColors().getBaseUIColor(), 
						true, getIcon(), null, getIntelTags(null));
			}
			return true;
		} else if (action.equals("hideNexusSystem")) {
			dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
			return true;
		}
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	
}





