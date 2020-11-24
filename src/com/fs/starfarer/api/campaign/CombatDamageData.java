package com.fs.starfarer.api.campaign;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class CombatDamageData {
	
	public static class DamageToFleetMember {
		public float hullDamage;
	}
	
	public static class DealtByFleetMember {
		private FleetMemberAPI member;
		private Map<FleetMemberAPI, DamageToFleetMember> damage = new HashMap<FleetMemberAPI, DamageToFleetMember>();
		public DealtByFleetMember(FleetMemberAPI member) {
			this.member = member;
		}
		
		public DamageToFleetMember getDamageTo(FleetMemberAPI target) {
			DamageToFleetMember damageTo = damage.get(target);
			if (damageTo == null) {
				damageTo = new DamageToFleetMember();
				damage.put(target, damageTo);
			}
			return damageTo;
		}
		
		public void addHullDamage(FleetMemberAPI target, float damage) {
			getDamageTo(target).hullDamage += damage;
		}

		public FleetMemberAPI getMember() {
			return member;
		}

		public Map<FleetMemberAPI, DamageToFleetMember> getDamage() {
			return damage;
		}
	}

	private Map<FleetMemberAPI, DealtByFleetMember> dealt = new HashMap<FleetMemberAPI, DealtByFleetMember>();
	
	public Map<FleetMemberAPI, DealtByFleetMember> getDealt() {
		return dealt;
	}

	public DealtByFleetMember getDealtBy(FleetMemberAPI member) {
		DealtByFleetMember dealtBy = dealt.get(member);
		if (dealtBy == null) {
			dealtBy = new DealtByFleetMember(member);
			dealt.put(member, dealtBy);
		}
		return dealtBy;
	}
	
	public void add(CombatDamageData other) {
		for (FleetMemberAPI member : other.dealt.keySet()) {
			DealtByFleetMember curr = getDealtBy(member);
			DealtByFleetMember adding = other.getDealtBy(member);
			
			for (FleetMemberAPI target : adding.damage.keySet()) {
				curr.addHullDamage(target, adding.getDamageTo(target).hullDamage);
			}
		}
	}
	
}



