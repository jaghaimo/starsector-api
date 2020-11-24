package com.fs.starfarer.api.campaign.econ;

public interface TransferTotalAPI {

	float getQuantity();
	float getPrice();
	String getMarketId();
	boolean isSmuggled();
	String getCommodityId();
	void setQuantity(int quantity);
	void setPrice(int price);

}
