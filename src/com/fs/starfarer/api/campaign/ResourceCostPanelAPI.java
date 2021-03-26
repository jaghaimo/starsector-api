package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.ui.Alignment;


public interface ResourceCostPanelAPI {
	public static interface ResourceColorGetter {
		public Color getColorFor(String commodityId, CargoAPI cargo);
	}
	
	boolean isNumberOnlyMode();
	void setNumberOnlyMode(boolean numberOnlyMode);
	boolean isWithBorder();
	void setWithBorder(boolean withBorder);
	boolean isEnabled();
	void setEnabled(boolean enabled);
	
	void showResources(CargoAPI cargo, Color color, List<String> commodities, ResourceColorGetter colorGetter);
	void addCost(String commodityId, int quantity, Color color);
	void addOrUpdateCost(String commodityId, int quantity, Color color);
	void flashCost(String commodityId);
	void update();
	
	Alignment getAlignment();
	
	/**
	 * Only MID, LMID, and RMID are supported. 
	 * @param alignment
	 */
	void setAlignment(Alignment alignment);
	void setSecondTitle(String secondTitle);
	void setSecondTitleColor(Color secondTitleColor);
	void addOrUpdateCost(String commodityId, int quantity);
	void addCost(String commodityId, int quantity);
	void setComWidthOverride(float comWidthOverride);
	
	void addCost(String commodityId, String text);
	void addCost(String commodityId, String text, Color color);
	void setLastCostConsumed(boolean consumed);

}
