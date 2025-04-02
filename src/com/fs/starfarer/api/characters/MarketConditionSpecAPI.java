package com.fs.starfarer.api.characters;

import java.util.Set;

import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.loading.WithSourceMod;

public interface MarketConditionSpecAPI extends WithSourceMod {

	float getOrder();
	String getId();
	String getName();
	String getDesc();
	String getIcon();
	String getScriptClass();
	void setIcon(String icon);
	boolean isPlanetary();
	boolean isDecivRemove();
	void setDecivRemove(boolean decivRemove);
	void setId(String id);
	void setName(String name);
	void setDesc(String desc);
	void setOrder(float order);
	void setPlanetary(boolean planetary);
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	ConditionGenDataSpec getGenSpec();
}
