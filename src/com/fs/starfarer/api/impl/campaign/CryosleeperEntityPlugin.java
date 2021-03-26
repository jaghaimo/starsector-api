package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.SectorEntityToken;

public class CryosleeperEntityPlugin extends BaseCustomEntityPlugin {

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
//		for (MarketAPI market : Misc.getMarketsInLocation(entity.getContainingLocation())) {
//			
//		}
	}
	
}





