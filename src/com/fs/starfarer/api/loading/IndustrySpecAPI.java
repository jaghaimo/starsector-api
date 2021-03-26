package com.fs.starfarer.api.loading;

import java.util.Set;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;

public interface IndustrySpecAPI {

	String getId();
	void setId(String id);
	
	Industry getNewPluginInstance(MarketAPI market);
	String getPluginClass();
	void setPluginClass(String effectClass);
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	String getName();
	String getDesc();
	void setDesc(String desc);
	float getCost();
	void setCost(float costMult);
	float getBuildTime();
	void setBuildTime(float buildTime);
	float getUpkeep();
	void setUpkeep(float upkeep);
	float getIncome();
	void setIncome(float income);
	String getImageName();
	void setImageName(String imageName);
	void setName(String name);
	String getUpgrade();
	void setUpgrade(String upgrade);
	String getDowngrade();
	void setDowngrade(String downgrade);
	int getOrder();
	void setOrder(int order);
	Industry getDowngradePluginInstance(MarketAPI market);
	Industry getUpgradePluginInstance(MarketAPI market);
	String getData();
	void setData(String data);
	RaidDangerLevel getDisruptDanger();
	void setDisruptDanger(RaidDangerLevel disruptDanger);
	

}
