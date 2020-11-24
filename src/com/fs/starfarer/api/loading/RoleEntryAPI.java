package com.fs.starfarer.api.loading;

public interface RoleEntryAPI {
	String getVariantId();
	void setWeight(float weight);
	void setVariantId(String variantId);
	float getWeight();
	boolean isFighterWing();
	float getFPCost();
}
