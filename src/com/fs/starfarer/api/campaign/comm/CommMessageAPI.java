package com.fs.starfarer.api.campaign.comm;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.OnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin.PriceUpdatePlugin;
import com.fs.starfarer.api.util.Highlights;

public interface CommMessageAPI {
	
	public static enum MessageClickAction {
		FLEET_TAB,
		REFIT_TAB,
		INTEL_TAB,
		CHARACTER_TAB,
		INCOME_TAB,
		COLONY_INFO,
		INTERACTION_DIALOG,
		NOTHING,
	}
	
	
	public static final String MESSAGE_FACTION_ID_KEY = "CMAPIfactionId";
	public static final String MESSAGE_PERSON_ID_KEY = "CMAPIpersonId";
	
	
	Color getSubjectColor();
	void setSubjectColor(Color defaultColor);
	OnMessageDeliveryScript getOnDelivery();
	void setOnDelivery(OnMessageDeliveryScript onDelivery);
	String getId();
	boolean isAddToIntelTab();
	void setAddToIntelTab(boolean addToIntelTab);
	String getType();
	void setType(String type);
	String getShortType();
	void setShortType(String shortType);
	String getDeliveredBy();
	void setDeliveredBy(String deliveredBy);
	String getSender();
	void setSender(String sender);
	long getTimeSent();
	void setTimeSent(long timeSent);
	long getTimeReceived();
	void setTimeReceived(long timeReceived);
	String getSubject();
	void setSubject(String subject);
	MessageSectionAPI getSection1();
	MessageSectionAPI getSection2();
	MessageSectionAPI getSection3();
	String getSound();
	void setSound(String sound);
	String getSmallIcon();
	void setSmallIcon(String smallIcon);
	String getImage();
	void setImage(String largeIcon);
	String getStarSystemId();
	void setStarSystemId(String starSystemId);
	String getLocationString();
	void setLocationString(String locationString);
	Highlights getSubjectHighlights();
	void setSubjectHighlights(Highlights subjectHighlights);
	String getChannel();
	void setChannel(String channel);
	Object getCustomData();
	void setCustomData(Object customData);
	CampaignEventPlugin getEvent();
	String getNote();
	void setNote(String note);
	Color getNoteColor();
	void setNoteColor(Color noteColor);


	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	Collection<String> getTags();
	void clearTags();
	Map<String, Object> getCustomMap();
	String getMarketId();
	void setMarketId(String marketId);
	MarketAPI getMarket();
	List<PriceUpdatePlugin> getPriceUpdates();
	void setPriceUpdates(List<PriceUpdatePlugin> priceUpdates);
	boolean isShowInCampaignList();
	void setShowInCampaignList(boolean showInCampaignList);
	MessageClickAction getAction();
	void setAction(MessageClickAction action);
	Vector2f getLocInHyper();
	void setLocInHyper(Vector2f locInHyper);
	SectorEntityToken getCenterMapOnEntity();
	void setCenterMapOnEntity(SectorEntityToken centerMapOnEntity);
	
}
