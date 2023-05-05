package com.fs.starfarer.api.campaign.comm;

import java.util.List;

import com.fs.starfarer.api.campaign.TextPanelAPI;

public interface IntelManagerAPI {
//	public interface IntelCommQueueItemAPI {
//		float getDaysLeft();
//		void setDaysLeft(float daysLeft);
//		IntelInfoPlugin getPlugin();
//		void setPlugin(IntelInfoPlugin plugin);
//	}

	boolean hasIntel(IntelInfoPlugin plugin);
	boolean hasIntelQueued(IntelInfoPlugin plugin);

	List<IntelInfoPlugin> getIntel();
	List<IntelInfoPlugin> getIntel(Class c);
	
	List<IntelInfoPlugin> getCommQueue();
	List<IntelInfoPlugin> getCommQueue(Class c);

	boolean unqueueIntel(IntelInfoPlugin plugin);
	
	/**
	 * Will become known to the player as soon as they're in comm relay range. Will be unqueued
	 * if not received by the player within maxCommQueueDays days.
	 * @param plugin
	 * @param maxCommQueueDays
	 */
	void queueIntel(IntelInfoPlugin plugin, float maxCommQueueDays);
	
	/**
	 * Will become known to the player as soon as they're in comm relay range. Will remain queued
	 * until it's either received or plugin.shouldRemoveIntel() returns true.
	 * 
	 * @param plugin
	 */
	void queueIntel(IntelInfoPlugin plugin);

	void addIntel(IntelInfoPlugin plugin);
	void addIntel(IntelInfoPlugin plugin, boolean forceNoMessage);
	void addIntel(IntelInfoPlugin plugin, boolean forceNoMessage, TextPanelAPI textPanel);
	
	/**
	 * Removes and unqueues.
	 * @param plugin
	 */
	void removeIntel(IntelInfoPlugin plugin);
	void removeAllThatShouldBeRemoved();
	void clear();
	
	void addIntelToTextPanel(IntelInfoPlugin plugin, TextPanelAPI textPanel);
	
	int getIntelCount(Class c, boolean includeQueued);
	
	boolean isPlayerInRangeOfCommRelay();
	IntelInfoPlugin getFirstIntel(Class c);
	boolean hasIntelOfClass(Class<?> c);
	void sortIntel(List<IntelInfoPlugin> toSort);




	
}
