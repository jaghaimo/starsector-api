package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.combat.ShipVariantAPI;

public interface AutofitVariantsAPI {
	List<ShipVariantAPI> getTargetVariants(String hullId);
}
