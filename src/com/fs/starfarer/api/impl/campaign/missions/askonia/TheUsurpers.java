package com.fs.starfarer.api.impl.campaign.missions.askonia;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.missions.academy.GATransverseJump.Stage;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class TheUsurpers extends HubMissionWithSearch {

	public static float EXTACT_AGENT_RAID_DIFFICULTY = 200f;
	
	public static enum Stage {
		MEET_RAM,
		INTERCEPT_FLEET,
		MEET_HYDER,
		RETURN_TO_MACARIO,
		EXTRACT_AGENT,
		MEET_CADEN,
		AGAIN_WTH_MACARIO,
		EMERGENCY_INTERCEPT,
		DELIVER_NEWS,
		COMPLETED,
	}
	
	protected PersonAPI ram;
	protected PersonAPI hyder;
	protected PersonAPI caden;
	protected PersonAPI macario;
	protected PersonAPI patrolSecond;

	protected MarketAPI umbra;
	protected MarketAPI sindria;
	protected MarketAPI volturn;
	
	protected SectorEntityToken debris;
	
	//public static float MISSION_DAYS = 120f;
	
	protected int marineLosses;
	protected int marineLossesMin;
	protected int marineLossesMax;
	
	protected int xpRewardLow;
	protected int xpRewardMedium;
	protected int xpRewardHigh;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$sdtu_ref", "$sdtu_inProgress")) {
			return false;
		}
		
		setPersonOverride(null);
		
		ram = getImportantPerson(People.RAM);
		if (ram == null) return false;
		
		hyder = getImportantPerson(People.HYDER);
		if (hyder == null) return false;
		
		caden = getImportantPerson(People.CADEN);
		if (caden == null) return false;
		
		macario = getImportantPerson(People.MACARIO);
		if (macario == null) return false;
		
		
		umbra =getMarket("umbra");
		if (umbra == null) return false;
		if (!umbra.getFactionId().equals("pirates")) return false;
		
		volturn = getMarket("volturn");
		if (volturn == null) return false;
		if (!volturn.getFactionId().equals("sindrian_diktat")) return false;
		
		sindria = getMarket("sindria");
		if (sindria == null) return false;
		if (!sindria.getFactionId().equals("sindrian_diktat")) return false;
		
		
		
		setStoryMission();
		setStartingStage(Stage.MEET_RAM);
		addSuccessStages(Stage.COMPLETED);
		
		marineLossesMax = 4;
		marineLossesMin = 1;
		marineLosses = marineLossesMin + genRandom.nextInt(marineLossesMax - marineLossesMin + 1);
		
		xpRewardLow = 2000;
		xpRewardMedium = 5000;
		xpRewardHigh = 12000;
		
		//payment = 10000;
		//paymentHigh = 17000;
		
		patrolSecond = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson(genRandom);
		patrolSecond.setRankId(Ranks.SPACE_LIEUTENANT);
		patrolSecond.setPostId(Ranks.POST_FLEET_COMMANDER);
		
		
		makeImportant(volturn, "$sdtu_meetRamOnVolturn", Stage.MEET_RAM);
		makeImportant(hyder, "$sdtu_meetHyder", Stage.MEET_HYDER);
		makeImportant(macario, "$sdtu_didHyderMeeting", Stage.RETURN_TO_MACARIO);
		makeImportant(umbra, "$sdtu_extractAgent", Stage.EXTRACT_AGENT);
		makeImportant(caden, "$sdtu_meetCaden", Stage.MEET_CADEN);
		makeImportant(macario, "$sdtu_didCadenMeeting", Stage.AGAIN_WTH_MACARIO);
		// debris field is made important in Action
		makeImportant(macario, "$sdtu_deliverNews", Stage.DELIVER_NEWS);
		
		connectWithGlobalFlag(Stage.MEET_RAM, Stage.INTERCEPT_FLEET, "$sdtu_interceptFleet");
		connectWithGlobalFlag(Stage.INTERCEPT_FLEET, Stage.MEET_HYDER, "$sdtu_meetHyder");
		connectWithGlobalFlag(Stage.MEET_HYDER, Stage.RETURN_TO_MACARIO, "$sdtu_reportToMacario1");
		connectWithGlobalFlag(Stage.RETURN_TO_MACARIO, Stage.EXTRACT_AGENT, "$sdtu_extractAgent");
		connectWithGlobalFlag(Stage.EXTRACT_AGENT, Stage.MEET_CADEN, "$sdtu_meetCaden");
		connectWithGlobalFlag(Stage.MEET_CADEN, Stage.AGAIN_WTH_MACARIO, "$sdtu_reportToMacario2");
		connectWithGlobalFlag(Stage.AGAIN_WTH_MACARIO, Stage.EMERGENCY_INTERCEPT, "$sdtu_emergencyIntercept");
		connectWithGlobalFlag(Stage.EMERGENCY_INTERCEPT, Stage.DELIVER_NEWS, "$sdtu_deliverNews");

		setStageOnGlobalFlag(Stage.COMPLETED, "$sdtu_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		
		// Diktat patrol with "traitor" officer for player to intercept.
		beginStageTrigger(Stage.INTERCEPT_FLEET);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.DIKTAT, FleetTypes.PATROL_MEDIUM, sindria.getStarSystem());
		triggerMakeNonHostile();
		triggerMakeNoRepImpact();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer(); // don't go chasing others, please.
		triggerPickLocationAroundEntity(volturn.getPlanetEntity(), 800f);
		triggerSetFleetMissionRef("$sdtu_ref"); // so they can be made unimportant
		triggerFleetMakeImportant(null, Stage.INTERCEPT_FLEET);
		triggerFleetAddDefeatTrigger("sdtuPatrolDefeated");
		triggerSaveGlobalFleetRef("$sdtu_traitorPatrol"); 
		triggerSetPatrol();
		triggerOrderFleetPatrol(sindria.getStarSystem());
		triggerSpawnFleetAtPickedLocation("$sdtu_interceptFleet", null);
		
		endTrigger();
		
		// Pirate/ANTIs fleet to distract player on approach to Umbra; otherwise irrelevant to plot.
		beginStageTrigger(Stage.EXTRACT_AGENT);	
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, umbra.getPlanetEntity());
		triggerFleetSetName("ARC Raider Flotilla");
		triggerSetFleetFaction(Factions.PIRATES);
        triggerAutoAdjustFleetStrengthMajor();
        triggerSetStandardAggroPirateFlags();
       // triggerFleetAllowLongPursuit(); // they're not *that* dedicated
        triggerPickLocationTowardsPlayer(umbra.getPlanetEntity(), 90f, getUnits(0.25f));
        triggerPickLocationAroundEntity(umbra.getPlanetEntity(), 200f);
        triggerSpawnFleetAtPickedLocation("$sdtu_antisFleet", null);
        triggerSetFleetMissionRef("$sdtu_ref");
        endTrigger();
		
		// Pirate/ANTIs fleet to distract player when leaving Umbra; otherwise irrelevant to plot.
		beginStageTrigger(Stage.MEET_CADEN);
		triggerCreateFleet(FleetSize.LARGER, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_LARGE, umbra.getPlanetEntity());
		triggerFleetSetName("ARC Raider Patrol");
		triggerSetFleetFaction(Factions.PIRATES);
        triggerAutoAdjustFleetStrengthMajor();
        triggerSetStandardAggroPirateFlags();
        //triggerFleetAllowLongPursuit();
        triggerPickLocationAroundEntity(umbra.getPlanetEntity(), 500f);
        triggerSpawnFleetAtPickedLocation("$sdtu_antisRevengeFleet", null);
        triggerSetFleetMissionRef("$sdtu_ref");
        endTrigger();
		
		// I guess you can go nuts once this is done, even though it isn't quite done, is it.
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$sdtu_missionCompleted", true);
		triggerMakeNonStoryCritical(volturn, umbra, sindria);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$sdtu_stage", getCurrentStage());
		set("$sdtu_extractAgentRaidDifficulty", EXTACT_AGENT_RAID_DIFFICULTY);
		set("$sdtu_marineLosses", marineLosses);
		set("$sdtu_patrolSecond", patrolSecond);
		//set("$sdtu_patrolFleet", diktatPatrolFleet)
		//set("$sdtu_payment", Misc.getWithDGS(payment));
		//set("$sdtu_paymentHigh", Misc.getWithDGS(paymentHigh));
		set("$sdtu_xpRewardLow", xpRewardLow);
		set("$sdtu_xpRewardMedium", xpRewardMedium);
		set("$sdtu_xpRewardHigh", xpRewardHigh);
	}

	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
//		if ("THEDUEL".equals(action)) {
//			TextPanelAPI text = dialog.getTextPanel();
//			text.setFontOrbitronUnnecessarilyLarge();
//			Color color = Misc.getBasePlayerColor();
//			color = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
//			text.addPara("THE DUEL", color);
//			text.setFontInsignia();
//			text.addImage("misc", "THEDUEL");
//			return true;
//		}
		
		if ("spawnDebris".equals(action)) {
			StarSystemAPI system = sindria.getStarSystem();
			DebrisFieldParams p = new DebrisFieldParams(
					150f, // field radius - should not go above 1000 for performance reasons
					-1f, // density, visual - affects number of debris pieces
					10000000f, // duration in days 
					0f); // days the field will keep generating glowing pieces
			p.source = DebrisFieldSource.MIXED;
			p.baseSalvageXP = 500; // base XP for scavenging in field
			debris = Misc.addDebrisField(system, p, StarSystemGenerator.random);
			SalvageSpecialAssigner.assignSpecialForDebrisField(debris);
			debris.getMemoryWithoutUpdate().set("$sdtuAgentDebris", true);
			
			List<SectorEntityToken> jumpPoints = system.getJumpPoints();
			debris.setCircularOrbit(jumpPoints.get(jumpPoints.size()-1), 90, 200, 100);

			
			Misc.makeImportant(debris, getMissionId());
			
			// Merc who blew away the agent.
	        beginStageTrigger(Stage.EMERGENCY_INTERCEPT);
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.MERC_PRIVATEER, system);
			triggerSetFleetFaction(Factions.INDEPENDENT);
			triggerFleetSetName("Mercenary Bounty Hunter");
			triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			triggerMakeHostileAndAggressive();
			triggerMakeNoRepImpact();
			//triggerFleetAllowLongPursuit();
			//triggerSetFleetAlwaysPursue();
			triggerPickLocationAroundEntity(debris, 500f);
			triggerSpawnFleetAtPickedLocation("$sdtu_merc", null);
			triggerOrderFleetPatrol(true, debris);
			endTrigger();
			
			return true;
		} else if ("makeDerbisUnimportant".equals(action)) {
			// need to probably store the debris in a member variable to get to it here
			if (debris != null)
			{
				Misc.makeUnimportant(debris, getMissionId());
			}
			return true;
		}
		else if ("shootEm".equals(action))
		{
			Global.getSoundPlayer().playSound("storyevent_diktat_execution", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("endMusic".equals(action))
		{
			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
			Global.getSoundPlayer().pauseMusic();
			//Global.getSoundPlayer().restartCurrentMusic();
			return true;
		}
		else if ("playMusicMacario".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_diktat_market_hostile", true);
			return true;
		}
		else if ("playMusicCaden".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_diktat_encounter_hostile", true);
			return true;
		}
		else if ("playMusicHyder".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_diktat_encounter_friendly", true);
			return true;
		} 
		else if ("playMusicSons".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_diktat_market_friendly", true);
			return true;
		} 
		else if ("resumeMusic".equals(action))
		{
			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
			Global.getSoundPlayer().restartCurrentMusic();
			return true;
		}
		else if ("giveKineticBlasters".equals(action)) {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addWeapons("kineticblaster", 3);
			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, Commodities.SHIP_WEAPONS, cargo);
			AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel());
			return true;
		}
		else if ("giveGigacannon".equals(action)) {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addWeapons("gigacannon", 2);
			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, Commodities.SHIP_WEAPONS, cargo);
			AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel());
			return true;
		}
		else if ("giveExecutor".equals(action)){
		    String variantId = "executor_Hull";
			ShipVariantAPI variant = Global.getSettings().getVariant(variantId).clone();
			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
			Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
			AddShip.addShipGainText(member, dialog.getTextPanel()); 
			return true;
		}
		else if ("replacePatrolLeader".equals(action)) {

			/*for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getMemoryWithoutUpdate().contains("$sdtu_interceptFleet")) {
					fleet.setCommander(patrolSecond);
				}
			}*/
			CampaignFleetAPI fleet = (CampaignFleetAPI)dialog.getInteractionTarget();
			fleet.setCommander(patrolSecond);
			fleet.getAI().addAssignmentAtStart(FleetAssignment.STANDING_DOWN, fleet, 0.5f + 0.5f * (float) Math.random(), null);
			//dialog.getInteractionTarget().setActivePerson(patrolSecond);
			
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			BattleAPI b = pf.getBattle();
			if (b != null) {
				b.leave(pf, false);
				//b.finish(null);
			}
			
			//fleet.setNoEngaging(3f); // not needed
			return true;
		}
		else if ("tryMakeHyderImportant".equals(action)){
			for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getName().equals("Askonia System Defense Armada")){
					Misc.makeImportant(fleet, "$sdtu_ref");
				}
			}
			return true;
		}
		else if ("tryMakeHyderUnimportant".equals(action)){
			for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getName().equals("Askonia System Defense Armada")){
					Misc.makeUnimportant(fleet, "$sdtu_ref");
				}
			}
			return true;
		}
		else if ("tryMakeCadenImportant".equals(action)){
			for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getName().equals("Lion's Guard Grand Armada")){
					Misc.makeImportant(fleet, "$sdtu_ref");
				}
			}
			return true;
		}
		else if ("tryMakeCadenUnimportant".equals(action)){
			for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getName().equals("Lion's Guard Grand Armada")){
					Misc.makeUnimportant(fleet, "$sdtu_ref");
				}
			}
			return true;
		}
		else if ("makeMercHostile".equals(action)){
			for (CampaignFleetAPI fleet : volturn.getStarSystem().getFleets()) {
				if (fleet.getMemoryWithoutUpdate().contains("$sdtu_merc")){
					//Misc.makeUnimportant(fleet, "$sdtu_ref");
					fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
					//fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
					Misc.makeHostile(fleet);
					
					AbilityPlugin eb = fleet.getAbility(Abilities.EMERGENCY_BURN);
					if (eb != null && eb.isUsable()) eb.activate();
				}
			}
			return true;
			
		}
		
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		//info.addImage(robed_man.getPortraitSprite(), width, 128, opad);
		
		if (currentStage == Stage.MEET_RAM) {
			info.addPara("Meet Yannick Ram at his safehouse on Volturn. He has a plan to save the Sindrian Diktat from itself.", opad);
			addStandardMarketDesc("Ram gave you coordinates to his safehouse " + volturn.getOnOrAt(), volturn, info, opad);
		}
		else if (currentStage == Stage.INTERCEPT_FLEET) {
			info.addPara("Find and intercept a Sindrian Diktat patrol fleet led by the 'treasonous' officer. It can be found in the Askonia system.", opad);
		}
		else if (currentStage == Stage.MEET_HYDER) {
			info.addPara("Use the 'treasonous' officer as leverage to gain access to Deputy Star Marshal Hyder. She can be found in command of the Askonia System Defense Armada, a large warfleet patrolling the Askonia star system.", opad);
		}
		else if (currentStage == Stage.RETURN_TO_MACARIO) {
			info.addPara("Report back to Macario about your conversation with Hyder.", opad);
			addStandardMarketDesc("Chief High Inspector-General Macario is based " + sindria.getOnOrAt(), sindria, info, opad);
		}
		else if (currentStage == Stage.EXTRACT_AGENT) {
			info.addPara("Extract a known double-agent who, while working for Horacio Caden, has betrayed the Sindrian Diktat.", opad);
			addStandardMarketDesc("The 'treasonous' double-agent is based " + umbra.getOnOrAt(), umbra, info, opad);
		}
		else if (currentStage == Stage.MEET_CADEN) {
			info.addPara("Use the 'treasonous' agent as leverage to gain access to Guard High Lieutenant-Executor Caden. He can be found in command of the Lion's Guard Grand Armada, a large warfleet patrolling the Askonia star system.", opad);
		}
		else if (currentStage == Stage.AGAIN_WTH_MACARIO) {
			info.addPara("Report back to Macario about your conversation with Caden.", opad);
			addStandardMarketDesc("Chief High Inspector-General Macario is based " + sindria.getOnOrAt(), sindria, info, opad);
		}
		else if (currentStage == Stage.EMERGENCY_INTERCEPT) {
			info.addPara("Intercept and assist or capture Macario's agent at the Fringe Jump-point of Askonia.", opad);
		}
		else if (currentStage == Stage.DELIVER_NEWS) {
			info.addPara("Report back to Macario with news of his agent's demise.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.MEET_RAM) {
			info.addPara("Meet Yannick Ram at his safehouse on Volturn.", tc, pad);
			return true;
		}
		if (currentStage == Stage.INTERCEPT_FLEET) {
			info.addPara("Intercept the traitor's patrol fleet in the Askonia system.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.MEET_HYDER) {
			info.addPara("Speak with Oxana Hyder. She commands the Askonia System Defense Armada.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_MACARIO) {
			info.addPara("Talk to Macario, on Sindria, about Hyder.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.EXTRACT_AGENT) {
			info.addPara("Extract the double agent from Umbra.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.MEET_CADEN) {
			info.addPara("Speak with Horacio Caden. He commands the Lion's Guard Grand Armada.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.AGAIN_WTH_MACARIO) {
			info.addPara("Talk to Macario, on Sindria, about Caden.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.EMERGENCY_INTERCEPT) {
			info.addPara("Intercept Macario's agent at the given location.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.DELIVER_NEWS) {
			info.addPara("Return to Macario, on Sindria, with news of his agent's death.", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "The Usurpers";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





