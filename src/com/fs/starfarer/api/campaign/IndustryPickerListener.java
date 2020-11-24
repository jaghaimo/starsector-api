package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.econ.Industry;



public interface IndustryPickerListener {
	void pickedIndustry(Industry industry);
	void cancelledIndustryPicking();
}
