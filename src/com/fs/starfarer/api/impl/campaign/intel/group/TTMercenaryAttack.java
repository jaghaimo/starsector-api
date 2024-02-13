package com.fs.starfarer.api.impl.campaign.intel.group;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;



public class TTMercenaryAttack extends GenericRaidFGI  {

	public static final String TTMA_FLEET = "$TTMA_fleet";
	public static final String TTMA_COMMAND = "$TTMA_command";
	
	public static String KEY = "$TTMA_ref";
	public static TTMercenaryAttack get() {
		return (TTMercenaryAttack) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	
	public TTMercenaryAttack(GenericRaidParams params) {
		super(params);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}
	

	@Override
	public String getNoun() {
		return "mercenary attack";
	}

	@Override
	public String getForcesNoun() {
		return super.getForcesNoun();
	}


	@Override
	public String getBaseName() {
		return "Tri-Tachyon Mercenary Attack";
	}


	transient protected boolean merc = false;
	@Override
	protected String getFleetCreationFactionOverride(int size) {
		if (getRandom().nextFloat() < 0.5f || size == 10) {
			merc = true;
			return Factions.MERCENARY;
		}
		merc = false;
		return Factions.TRITACHYON;
	}
	
	@Override
	protected void configureFleet(int size, FleetCreatorMission m) {
		
		// so that the merc source is not a TriTach market, if that's where they spawned
		// doesn't work (since based on spawn faction anyway) and gets weird w/ mercs going back to different markets -am
		//m.getPreviousCreateFleetAction().params.setSource(null, false);
		
		m.triggerSetFleetFlag(TTMA_FLEET);
		
		if (size == 10) { 
			m.triggerSetFleetQuality(FleetQuality.SMOD_3);
			m.triggerSetFleetFlag(TTMA_COMMAND);
		} else if (getRandom().nextFloat() < 0.5f) {
			m.triggerSetFleetQuality(FleetQuality.SMOD_1);
		} else {
			m.triggerSetFleetQuality(FleetQuality.SMOD_2);
		}
		
		// don't want to be shooting it out with the employer, who's normally hostile to independents
		m.triggerMakeNonHostileToFaction(Factions.TRITACHYON);
		
		
		// likely spawning from Nortia, try not to get sidetracked
		m.triggerMakeNonHostileToFaction(Factions.DIKTAT);
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		
		
		if (merc) {
			m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
		} else {
			m.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
			m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
		}
		
		if (size == 10) {
			m.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
			m.triggerFleetAddCommanderSkill(Skills.SUPPORT_DOCTRINE, 1);
		}
		
		int tugs = 0;
		if (size == 10) {
			tugs = 2;
		} else if (merc) {
			tugs = getRandom().nextInt(3);
		}
		
		boolean lightDetachment = size <= 5;
		if (lightDetachment) {
			m.triggerSetFleetMaxShipSize(3);
		}
		
		m.triggerFleetMakeFaster(true, tugs, true);
	}
	
	@Override
	protected void configureFleet(int size, CampaignFleetAPI fleet) {
		boolean hasCombatCapital = false;
		boolean hasCivCapital = false;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isCapital()) {
				hasCombatCapital |= !member.isCivilian();
				hasCivCapital |= member.isCivilian();
			}
		}
		
		if (size == 10) {
			fleet.setName("Tactistar Operations Command");
			fleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
			setNeverStraggler(fleet);
		} else if (hasCombatCapital) {
			fleet.setName("Tactistar Assault Detachment");
			fleet.getCommander().setRankId(Ranks.SPACE_CAPTAIN);
		} else if (hasCivCapital) {
			fleet.setName("Tactistar Support Detachment");
			fleet.getCommander().setRankId(Ranks.SPACE_CAPTAIN);
		} else {
			fleet.setName("Tactistar Light Detachment");
			fleet.getCommander().setRankId(Ranks.SPACE_COMMANDER);
		}
	}


	@Override
	public void abort() {
		if (!isAborted()) {
			for (CampaignFleetAPI curr : getFleets()) {
				curr.getMemoryWithoutUpdate().unset(TTMA_FLEET);
			}
		}
		super.abort();
	}
	
	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		interval.advance(days);
		
		// only hostile while in the target system and on the job, so to speak
		// unless the player is baseline hostile to independents
		if (interval.intervalElapsed()) {
			if (isCurrent(PAYLOAD_ACTION)) {
				String reason = "TTMA";
				for (CampaignFleetAPI curr : getFleets()) {
					Misc.setFlagWithReason(curr.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE,
											reason, true, 1f);
				}
				
			}
		}
	}

	@Override
	protected void addPostAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		TTMercenaryAttack attack = TTMercenaryAttack.get();
		StarSystemAPI target = TriTachyonHostileActivityFactor.getPrimaryTriTachyonSystem();
		boolean reversible = attack != null && !attack.isSpawning() && !attack.isFailed() &&
					!attack.isSucceeded() && !attack.isAborted() && !attack.isEnding() && !attack.isEnded() &&
					target != null;
		if (reversible) {
			info.addPara("Mercenary companies are notoriously flexible in their allegiances.", opad, 
					Misc.getHighlightColor(), "notoriously flexible");
		}
	}
}




