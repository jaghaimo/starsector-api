package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;

public abstract class SDFBase extends PersonalFleetScript {

	// list of all relevant fleet commander skills, for reference
//	m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
//	m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
//	m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
//	m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
//	m.triggerFleetAddCommanderSkill(Skills.FIGHTER_UPLINK, 1);
//	m.triggerFleetAddCommanderSkill(Skills.WOLFPACK_TACTICS, 1);
//	m.triggerFleetAddCommanderSkill(Skills.OFFICER_TRAINING, 1);
//	m.triggerFleetAddCommanderSkill(Skills.SUPPORT_DOCTRINE, 1);
//	m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
//	m.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
//	m.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
//	m.triggerFleetAddCommanderSkill(Skills.CYBERNETIC_AUGMENTATION, 1);
//	m.triggerFleetAddCommanderSkill(Skills.DERELICT_CONTINGENT, 1);
	
	protected PersonAPI person;
	
	public SDFBase() {
		super(null); // when/if there's a specific person, comment out anything using "person" here
		setMinRespawnDelayDays(10f);
		setMaxRespawnDelayDays(20f);
		
		person = createOrGetPerson();
		setDefeatTrigger(getDefeatTriggerToUse());
	}
	
	protected abstract String getFactionId();
	
	protected String getDefeatTriggerToUse() {
		return null;
	}

	protected SkillPickPreference getCommanderShipSkillPreference() {
		return SkillPickPreference.ANY;
	}
	
	protected PersonAPI createOrGetPerson() {
		int commanderLevel = 7;
		SkillPickPreference pref = getCommanderShipSkillPreference();
		PersonAPI commander = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(getFactionId()),
								commanderLevel, pref, false, null, true, true, -1, random);
		if (commander.getPersonalityAPI().getId().equals(Personalities.TIMID)) {
			commander.setPersonality(Personalities.CAUTIOUS);
		}
		commander.setRankId(Ranks.SPACE_ADMIRAL);
		commander.setPostId(Ranks.POST_FLEET_COMMANDER);
		return commander;
	}

	@Override
	public PersonAPI getPerson() {
		return person;
	}

	public ShipVariantAPI getVariant(String id) {
		return Global.getSettings().getVariant(id);
	}

	@Override
	public boolean canSpawnFleetNow() {
		MarketAPI source = getSourceMarket();
		if (source == null || source.hasCondition(Conditions.DECIVILIZED)) return false;
		if (!source.hasIndustry(Industries.MILITARYBASE) &&
				!source.hasIndustry(Industries.HIGHCOMMAND)) return false;
		if (!source.getFactionId().equals(getFactionId())) return false;
		return true;
	}

	@Override
	public boolean shouldScriptBeRemoved() {
		return false;
	}

}




