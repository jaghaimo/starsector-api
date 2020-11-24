/**
 * 
 */
package com.fs.starfarer.api;

import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;


public class PluginPick<T> {
	public T plugin;
	public PickPriority priority;
	public PluginPick(T plugin, PickPriority priority) {
		this.plugin = plugin;
		this.priority = priority;
	}
}