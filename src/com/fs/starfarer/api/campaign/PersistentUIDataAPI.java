package com.fs.starfarer.api.campaign;

import java.util.LinkedHashSet;
import java.util.List;


public interface PersistentUIDataAPI {

	public static interface AbilitySlotAPI {
		int getSlotId();
		String getAbilityId();
		void setAbilityId(String abilityId);
		
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


	SectorEntityToken getCourseTarget();
	
}
