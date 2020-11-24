package com.fs.starfarer.api.campaign.econ;

import java.util.List;

public interface CommodityFlowAPI {

	List<TransferTotalAPI> getTopImports(int count);
	List<TransferTotalAPI> getTopExports(int count);
	
	float getTotalIncomingPrice();
	float getTotalOutgoingPrice();
	
	float getTotalIncoming();
	float getTotalOutgoing();
	
	List<TransferTotalAPI> getIncoming();
	List<TransferTotalAPI> getOutgoing();
}
