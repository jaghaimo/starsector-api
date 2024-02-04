package com.fs.starfarer.api.campaign;

import java.util.LinkedHashSet;
import java.util.List;


public interface PersistentUIDataAPI {

	public static interface AbilitySlotAPI {
		int getSlotId();
		String getAbilityId();
		void setAbilityId(String abilityId);
		String getInHyperAbilityId();
		void setInHyperAbilityId(String inHyperAbilityId);
		
	}
	public static interface AbilitySlotsAPI {
		List<AbilitySlotAPI> getCurrSlotsCopy();
		int getCurrBarIndex();
		void setCurrBarIndex(int currBarIndex);
		boolean isLocked();
		void setLocked(boolean locked);
	}
	
	
	AbilitySlotsAPI getAbilitySlotsAPI();


	LinkedHashSet<String> getCheckedRefitTags();
	LinkedHashSet<String> getAllRefitTags();

	SectorEntityToken getCourseTarget();


	ControlGroupsAPI getControlGroups();
	
}
