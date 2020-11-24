package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;



public interface CargoPickerListener {
	void pickedCargo(CargoAPI cargo);
	void cancelledCargoSelection();
	void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined);
}
