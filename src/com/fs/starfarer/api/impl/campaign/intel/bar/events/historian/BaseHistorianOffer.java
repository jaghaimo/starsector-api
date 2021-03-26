package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOfferCreator;

//public class BaseHistorianOffer implements HistorianOffer {
public class BaseHistorianOffer extends BaseIntelPlugin implements HistorianOffer {

	transient protected boolean done = false;
	transient protected boolean remove = false;
	transient protected boolean endConversationOnReturning = true;
	transient protected InteractionDialogAPI dialog;
	transient protected TextPanelAPI text;
	transient protected OptionPanelAPI options;
	transient protected HistorianOfferCreator creator;
	
	public BaseHistorianOffer() {
		super();
	}
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		if (dialog != null) {
			text = dialog.getTextPanel();
			options = dialog.getOptionPanel();
		}
		done = false;
		remove = false;
	}
	
	public HistorianOfferCreator getCreator() {
		return creator;
	}

	public void setCreator(HistorianOfferCreator creator) {
		this.creator = creator;
	}

	public void addPromptAndOption(InteractionDialogAPI dialog) {
		
	}

	public boolean isInteractionFinished() {
		return done;
	}

	public void optionSelected(String optionText, Object optionData) {
		
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean shouldRemoveOffer() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public int getSortOrder() {
		return 0;
	}

	public boolean shouldEndConversationOnReturning() {
		return endConversationOnReturning;
	}

	public void setEndConversationOnReturning(boolean endConversationOnReturning) {
		this.endConversationOnReturning = endConversationOnReturning;
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "historian_intel_icon");
	}

	public void notifyAccepted() {
		if (creator != null) {
			String offerId = creator.getOfferId(this);
			if (offerId != null) {
				HistorianData hd = HistorianData.getInstance();
				hd.getGivenOffers().add(offerId);
			}
			creator.notifyAccepted(this);
		}
	}

//	public String getOfferId() {
//		return null;
//	}

//	@Override
//	public Set<String> getIntelTags(SectorMapAPI map) {
//		return super.getIntelTags(map);
//	}
	
}




