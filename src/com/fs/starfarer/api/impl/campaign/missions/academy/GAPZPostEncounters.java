package com.fs.starfarer.api.impl.campaign.missions.academy;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.SetMemoryValueAfterDelay;
import com.fs.starfarer.api.util.Misc;

public class GAPZPostEncounters implements EveryFrameScript {

	public static boolean debug = false;
	public static String id = "gaPZ";
	
	public static void init() {
		//debug = true;
		
		float cottonDelay = 30f + (float) Math.random() * 30f;
		if (debug) cottonDelay = 0f;
		SetMemoryValueAfterDelay action = new SetMemoryValueAfterDelay(cottonDelay,
				Global.getSector().getMemoryWithoutUpdate(), "$gaPZ_brotherCottonEncounter", true);
		action.doAction(null);
		
		
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelayShort();
			}
			e.setLocationCoreOnly(true, Factions.HEGEMONY);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(Factions.HEGEMONY);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.VERY_HIGH, Factions.HEGEMONY, FleetTypes.TASK_FORCE, new Vector2f());
			e.setFleetSource("coatl", "chicomoztoc");
			
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerSetFleetFlag("$gaPZ_hegemony");
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
		}
		
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelayShort();
			}
			e.setLocationCoreOnly(true, Factions.TRITACHYON);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(Factions.TRITACHYON);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.SMOD_1, Factions.TRITACHYON, FleetTypes.TASK_FORCE, new Vector2f());
			e.setFleetSource("culann", "eochu_bres");
			
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerSetFleetFlag("$gaPZ_tritachyon");
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
		}
		
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelayShort();
			}
			String factionId = Factions.LUDDIC_CHURCH;
			e.setLocationCoreOnly(true, factionId);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(factionId);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.HIGHER, factionId, FleetTypes.TASK_FORCE, new Vector2f());
			e.setFleetSource("baetis");
			
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.DEFAULT);
			e.triggerSetFleetFlag("$gaPZ_luddic_church");
			e.triggerFleetAllowLongPursuit();
			e.triggerSetFleetAlwaysPursue();
			e.triggerOrderFleetInterceptPlayer();
			e.triggerMakeFleetAllowDisengage();
			e.triggerOrderFleetMaybeEBurn();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
		}
		
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelayShort();
			}
			String factionId = Factions.PERSEAN;
			e.setLocationCoreOnly(true, factionId);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(factionId);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, factionId, FleetTypes.PATROL_MEDIUM, new Vector2f());
			e.triggerSetFleetFaction(Factions.PERSEAN);
			e.triggerSetFleetCommander(People.getPerson(People.SIYAVONG));
			//e.triggerSet
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerMakeNonHostile();
			e.triggerFleetAllowLongPursuit();
			e.triggerSetFleetAlwaysPursue();
			e.triggerOrderFleetInterceptPlayer();
			e.triggerMakeFleetAllowDisengage();
			e.triggerOrderFleetMaybeEBurn();
			
			e.setFleetSource("kazeron");
			
			e.triggerSetFleetFlag("$gaPZ_persean");
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
		}
		
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelayShort();
			}
			
			String factionId = Factions.DIKTAT;
			e.setLocationCoreOnly(true, factionId);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(factionId);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_1, Factions.LIONS_GUARD, FleetTypes.PATROL_LARGE, new Vector2f());
			e.setFleetSource("sindria");
			e.triggerSetFleetFaction(factionId);
			e.triggerFleetSetShipPickMode(ShipPickMode.PRIORITY_THEN_ALL);
			e.triggerFleetSetNoFactionInName();
			e.triggerMakeNoRepImpact();
			
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerSetFleetFlag("$gaPZ_diktat");
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
		}
	}

	public static DelayedFleetEncounter createSecondDiktatEncounter() {
		{
			DelayedFleetEncounter e = new DelayedFleetEncounter(Misc.getRandom(Misc.genRandomSeed(), 0), id);
			if (debug) {
				e.setDelayNone();
			} else {
				e.setDelay(10, 15);
			}
			
			String factionId = Factions.DIKTAT;
			e.setLocationCoreOnly(true, factionId);
			e.setEncounterFromSomewhereInSystem();
			e.setRequireFactionPresence(factionId);
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.DIKTAT, FleetTypes.INSPECTION_FLEET, new Vector2f());
			e.setFleetSource("sindria");
			e.triggerSetFleetFaction(factionId);
			//e.triggerFleetSetShipPickMode(ShipPickMode.PRIORITY_THEN_ALL);
			//e.triggerFleetSetNoFactionInName();
			//e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerSetFleetFlag("$gaPZ_diktatSecond");
			//e.triggerSetStandardAggroInterceptFlags();
			e.triggerFleetAllowLongPursuit();
			e.triggerSetFleetAlwaysPursue();
			e.triggerOrderFleetInterceptPlayer();
			e.triggerOrderFleetMaybeEBurn();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetAddDefeatTriggerPermanent("GAPZGoAwayTrigger");
			e.endCreate();
			
			return e;
		}
	}

	public void advance(float amount) {
		
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
