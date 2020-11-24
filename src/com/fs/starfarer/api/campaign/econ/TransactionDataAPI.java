package com.fs.starfarer.api.campaign.econ;

public interface TransactionDataAPI {
	float getAverage();
	void setAverage(float averageQuantity);

	float getCurrent();
	void setCurrent(float currentQuantity);

	String getMarketId();
	String getCommodityId();

}