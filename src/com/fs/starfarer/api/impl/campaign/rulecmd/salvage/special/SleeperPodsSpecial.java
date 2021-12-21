package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;

public class SleeperPodsSpecial extends BaseSalvageSpecial {

	public static final String OPEN = "open";
	public static final String NOT_NOW = "not_now";
	
	public static enum SleeperSpecialType {
		CREW,
		MARINES,
		OFFICER,
		ADMIN,
		ORGANS,
	}
//	public static enum SleeperSpecialQuantity {
//		LOW,
//		MEDIUM,
//		HIGH,
//	}
	
	public static class SleeperPodsSpecialData implements SalvageSpecialData {
		public SleeperSpecialType type;
		//public SleeperSpecialQuantity quantity;
		public int min, max;
		public PersonAPI officer;
		public SleeperPodsSpecialData(SleeperSpecialType type, PersonAPI officer) {
			this.type = type;
			this.officer = officer;
			min = max = 1;
		}
		public SleeperPodsSpecialData(SleeperSpecialType type, int min, int max) {
			this.type = type;
			this.min = min;
			this.max = max;
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new SleeperPodsSpecial();
		}
	}
	
	private SleeperPodsSpecialData data;
	
	private int quantity = 1;
	
	public SleeperPodsSpecial() {
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);

		data = (SleeperPodsSpecialData) specialData;
		
//		if (data.quantity != null) {
//			switch (data.quantity) {
//			case LOW:
//				quantity = random.nextInt(10) + 5;
//				break;
//			case MEDIUM:
//				quantity = random.nextInt(20) + 10;
//				break;
//			case HIGH:
//				quantity = random.nextInt(50) + 25;
//				break;
//			}
//		}
		quantity = data.min + random.nextInt(data.max - data.min + 1);
		
		if (data.type == SleeperSpecialType.ORGANS) {
			//quantity *= 0.5f;
			if (quantity < 1) quantity = 1;
		}
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		
		int crewBerths = (int) (player.getCargo().getMaxPersonnel() - player.getCargo().getTotalPersonnel());
//		int officerBerths = (int) (Global.getSector().getPlayerPerson().getStats().getOfficerNumber().getModifiedValue() - 
//				player.getFleetData().getOfficersCopy().size());
		if (crewBerths < 0) crewBerths = 0;
//		if (officerBerths < 0) officerBerths = 0;
		
		SleeperSpecialType type = data.type;
		
		if (type == SleeperSpecialType.CREW) quantity = Math.min(crewBerths, quantity);
		if (type == SleeperSpecialType.MARINES) quantity = Math.min(crewBerths, quantity);
		//if (type == SleeperSpecialType.OFFICER) quantity = Math.min(officerBerths, quantity);
		if (type == SleeperSpecialType.ADMIN || type == SleeperSpecialType.OFFICER) quantity = 1;
		
		switch (type) {
		case CREW:
			initCrew();
			break;
		case MARINES:
			initMarines();
			break;
		case OFFICER:
			initOfficer();
			break;
		case ADMIN:
			initOfficer();
			break;
		case ORGANS:
			initOrgans();
			break;
		}
	}

	
	protected void initCrew() {
		if (quantity <= 0) { 
			initNothing();
		} else {
			addText("While making a preliminary assessment, your salvage crews " +
					"find some occupied sleeper pods still running on backup power.");
			
			options.clearOptions();
			options.addOption("Attempt to open the pods", OPEN);
			options.addOption("Not now", NOT_NOW);
		}			
	}
	
	protected void initMarines() {
		if (quantity <= 0) { 
			initNothing();
		} else {
			addText("While making a preliminary assessment, your salvage crews " +
			"find some occupied mil-grade sleeper pods still running on backup power.");
			
			options.clearOptions();
			options.addOption("Attempt to open the pods", OPEN);
			options.addOption("Not now", NOT_NOW);
		}			
	}
	
	protected void initOfficer() {
		if (quantity <= 0) { 
			initNothing();
		} else {
			addText("While making a preliminary assessment, your salvage crews " +
					"find a single occupied sleeper pod still running on backup power.");
			
			options.clearOptions();
			options.addOption("Attempt to open the pod", OPEN);
			options.addOption("Not now", NOT_NOW);
		}			
		
	}
	
	protected void initOrgans() {
		if (quantity <= 0) { 
			initNothing();
		} else {
			addText("While making a preliminary assessment, your salvage crews " +
					"find some occupied sleeper pods still running on backup power.");
	
			options.clearOptions();
			options.addOption("Attempt to open the pods", OPEN);
			options.addOption("Not now", NOT_NOW);
		}			
		
	}
	
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if (OPEN.equals(optionData)) {
			
			switch (data.type) {
			case CREW:
				addText("One by one, the pods begin to open as the thawing process completes. " +
						"Most of the occupants come through alive, if somewhat dazed.");
				
				playerFleet.getCargo().addCommodity(Commodities.CREW, quantity);
				AddRemoveCommodity.addCommodityGainText(Commodities.CREW, quantity, text);
				
				break;
			case MARINES:
				addText("One by one, the pods begin to open as the thawing process completes. " +
				"Most of the occupants come through alive, if somewhat dazed.");
				
				playerFleet.getCargo().addCommodity(Commodities.MARINES, quantity);
				AddRemoveCommodity.addCommodityGainText(Commodities.MARINES, quantity, text);
				
				break;
			case OFFICER:
				addText("The thawing process completes, and the pod opens. " +
						"It contains an experienced officer, who joins you out of gratitude for being rescued.");

				playerFleet.getFleetData().addOfficer(data.officer);
				AddRemoveCommodity.addOfficerGainText(data.officer, text);
				break;
			case ADMIN:
				addText("The thawing process completes, and the pod opens. " +
					"It contains an experienced planetary administrator, who joins you out of gratitude for being rescued.");

				Global.getSector().getCharacterData().addAdmin(data.officer);
				AddRemoveCommodity.addAdminGainText(data.officer, text);
				break;
			case ORGANS:
				addText("One by one, the pods begin to open as the thawing process completes. " +
						"Unfortunately, it's been too long, or something went wrong along the way, and there are no survivors - at least, not with any brain activity.");
		
				playerFleet.getCargo().addCommodity(Commodities.ORGANS, quantity);
				AddRemoveCommodity.addCommodityGainText(Commodities.ORGANS, quantity, text);
				break;
			}
			
			
			setDone(true);
			setShowAgain(false);	
		} else if (NOT_NOW.equals(optionData)) {
			setDone(true);
			setEndWithContinue(false);
			setShowAgain(true);
		}
	}

	
	
}


