package com.fs.starfarer.api.campaign.econ;

public interface MarketShareDataAPI {

	boolean isSourceIsIllegal();
	void setSourceIsIllegal(boolean sourceIsIllegal);
	float getExportMarketShare();
	void setExportMarketShare(float marketShare);
	CommoditySourceType getSource();
	void setSource(CommoditySourceType source);
	float getMarketValueFraction();
	void setMarketValueFraction(float marketValueFraction);

}
