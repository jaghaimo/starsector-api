package com.fs.starfarer.api.loading;

import java.util.ArrayList;
import java.util.List;

public class WeaponGroupSpec implements Cloneable {

	private WeaponGroupType type = WeaponGroupType.LINKED;
	private boolean autofireOnByDefault = false;
	//private boolean isModuleGroup = false;
	
	private List<String> slots = new ArrayList<String>(); // list of slot ids

	@Override
	public WeaponGroupSpec clone() {
		try {
			WeaponGroupSpec copy = (WeaponGroupSpec) super.clone();
			copy.slots = new ArrayList<String>();
			copy.slots.addAll(slots);
			return copy;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	public WeaponGroupSpec() {
	}

	public WeaponGroupSpec(WeaponGroupType type) {
		this.type = type;
	}

	public WeaponGroupType getType() {
		return type;
	}

	public void setType(WeaponGroupType type) {
		this.type = type;
	}

	public List<String> getSlots() {
		return slots;
	}
	
	public void addSlot(String slotId) {
		slots.add(slotId);
	}
	
	public void removeSlot(String slotId) {
		slots.remove(slotId);
	}

	public boolean isAutofireOnByDefault() {
		return autofireOnByDefault;
	}

	public void setAutofireOnByDefault(boolean autofireOnByDefault) {
		this.autofireOnByDefault = autofireOnByDefault;
	}

//	public boolean isModuleGroup() {
//		return isModuleGroup;
//	}
//
//	public void setModuleGroup(boolean isModuleGroup) {
//		this.isModuleGroup = isModuleGroup;
//	}
	

}
