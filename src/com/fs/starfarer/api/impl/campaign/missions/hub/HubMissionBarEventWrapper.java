package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.BarEventSpec;
import com.fs.starfarer.api.util.Misc;

public class HubMissionBarEventWrapper extends BaseBarEvent {

	protected long seed;
	//protected PersonAPI person;
	
	protected transient BarEventSpec spec = null;
	protected String specId = null;
	
	protected transient Random genRandom;
	protected transient HubMissionWithBarEvent mission;
	
	public HubMissionBarEventWrapper(String specId) {
		this.specId = specId;
		seed = Misc.genRandomSeed();
		readResolve();
	}
	
	protected Object readResolve() {
		spec = Global.getSettings().getBarEventSpec(specId);
		return this;
	}
	
	public String getBarEventId() {
		return specId;
	}
	
	
	@Override
	public boolean shouldShowAtMarket(MarketAPI market) {
		//return super.shouldShowAtMarket(market);
//		if (spec.getId().equals("extr")) {
//			System.out.println("wfwefwe");
//		}
		if (shownAt != null && shownAt != market) return false;
		
		abortMission();
		
		//if (mission == null) {
			genRandom = new Random(seed + market.getId().hashCode() * 181783497276652981L);
			
			if (genRandom.nextFloat() > spec.getProb()) return false;
			
			mission = spec.createMission();
			mission.setMissionId(specId);
			mission.setGenRandom(genRandom);
		//}
		
		return mission.shouldShowAtMarket(market);
	}
	

	public String getSpecId() {
		return specId;
	}

	public HubMission getMission() {
		return mission;
	}

	public void abortMission() {
		if (mission != null) {
			mission.abort();
			mission = null;
		}
	}
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
//		abortMission();
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
//		genRandom = new Random(seed + market.getId().hashCode());
//		
//		mission = spec.createMission();
//		mission.setMissionId(specId);
//		mission.setGenRandom(genRandom);
		mission.createAndAbortIfFailed(market, true);
		//mission.setGenRandom(null);
		if (mission.isMissionCreationAborted()) {
			mission = null;
			return;
		}
		
		MemoryAPI prev = memoryMap.get(MemKeys.LOCAL);
		if (mission.getPerson() != null) {
			memoryMap.put(MemKeys.ENTITY, prev);
			memoryMap.put(MemKeys.LOCAL, mission.getPerson().getMemoryWithoutUpdate());
			memoryMap.put(MemKeys.PERSON_FACTION, mission.getPerson().getFaction().getMemory());
		}
		mission.updateInteractionData(dialog, memoryMap);
		
		FireBest.fire(null, dialog, memoryMap, mission.getTriggerPrefix() + "_blurbBar true");		
		FireBest.fire(null, dialog, memoryMap, mission.getTriggerPrefix() + "_optionBar true");
		
		memoryMap.put(MemKeys.LOCAL, prev);
		memoryMap.remove(MemKeys.ENTITY);
		memoryMap.remove(MemKeys.PERSON_FACTION);
		
		
		//BaseMissionHub.getCreatedMissionsList(mission.getPerson(), market).add((BaseHubMission) mission);
	}
	
	
	
}








