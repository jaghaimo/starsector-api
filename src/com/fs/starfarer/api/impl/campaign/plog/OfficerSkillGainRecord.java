package com.fs.starfarer.api.impl.campaign.plog;

public class OfficerSkillGainRecord {

	protected String personId;
	protected String skillId;
	protected boolean elite;
	
	public OfficerSkillGainRecord(String personId, String skillId, boolean elite) {
		this.personId = personId;
		this.skillId = skillId;
		this.elite = elite;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public boolean isElite() {
		return elite;
	}

	public void setElite(boolean elite) {
		this.elite = elite;
	}
	

	
	
}
