package com.fs.starfarer.api.characters;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;

public interface RelationshipAPI {

	public static enum RelationshipTargetType {
		FACTION,
		PERSON,
		PLAYER,
	}

	public static class RelationshipTarget {
		private RelationshipTargetType type;
		private PersonAPI person;
		private FactionAPI faction;
		public RelationshipTarget(RelationshipTargetType type) {
			this.type = type;
		}
		public RelationshipTarget(RelationshipTargetType type, FactionAPI faction) {
			this.type = type;
			this.faction = faction;
		}
		public RelationshipTarget(RelationshipTargetType type, PersonAPI person) {
			this.type = type;
			this.person = person;
		}
		public RelationshipTargetType getType() {
			return type;
		}
		public void setType(RelationshipTargetType type) {
			this.type = type;
		}
		public PersonAPI getPerson() {
			return person;
		}
		public void setPerson(PersonAPI person) {
			this.person = person;
		}
		public FactionAPI getFaction() {
			return faction;
		}
		public void setFaction(FactionAPI faction) {
			this.faction = faction;
		}
		public boolean isPlayer() {
			return type == RelationshipTargetType.PLAYER || 
					(person != null && person == Global.getSector().getPlayerPerson());
		}
//		public String getTargetId() {
//			if (faction != null) return faction.getId();
//			//if (person != null) return person.get
//		}
	}
	
	
	RelationshipTarget getTarget();
	void setTarget(RelationshipTarget target);

	float getRel();
	void setRel(float rel);
	
	RepLevel getLevel();
	void setLevel(RepLevel level);

	boolean isAtWorst(RepLevel level);
	boolean isAtBest(RepLevel level);
	
	boolean ensureAtBest(RepLevel level);
	boolean ensureAtWorst(RepLevel level);

	boolean adjustRelationship(float delta, RepLevel limit);

	boolean isHostile();

	Color getRelColor();
	Color getRelColor(RepLevel level);
	
}
