package com.fs.starfarer.api.impl.codex;

import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TagDisplayAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public interface CodexEntryPlugin {
	
	public static enum ListMode {
		ITEM_LIST,
		RELATED_ENTRIES,
	}
	

	public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode);

	
	public String getId();
	public String getTitle();
	public String getSortTitle();
	public String getSearchString();
	public String getIcon();
	public boolean isVignetteIcon();
	default Color getIconColor() {
		return Color.white;
	}
	
	public CodexEntryPlugin getParent();
	public void setParent(CodexEntryPlugin parent);
	

	
	public boolean isRetainOrderOfChildren();
	public void setRetainOrderOfChildren(boolean retainOrderOfChildren);
	public List<CodexEntryPlugin> getChildren();
	public void addChild(CodexEntryPlugin entry);

	public boolean isCategory();
	public boolean hasDetail();
	
	public boolean isVisible();
	public Object getParam();
	public Object getParam2();
	
	public boolean isLocked();


	boolean matchesTags(Set<String> tags);
	boolean hasTagDisplay();
	public void configureTagDisplay(TagDisplayAPI tags);

	public Set<CodexEntryPlugin> getRelatedEntries();
	public void addRelatedEntry(CodexEntryPlugin entry);
	public void addRelatedEntry(String id);
	public void removeRelatedEntry(CodexEntryPlugin entry);
	public void removeRelatedEntry(String id);
	public boolean isRetainOrderOfRelatedEntries();
	public void setRetainOrderOfRelatedEntries(boolean retainOrderOfRelatedEntries);
	
	/**
	 * Checked for the *parent* of the related entry.
	 * @return
	 */
	public float getCategorySortTierForRelatedEntries();
	public void setCategorySortTierForRelatedEntries(float categorySortTierForRelatedEntries);


	List<CodexEntryPlugin> getChildrenRecursive(boolean includeCategories);


	boolean hasCustomDetailPanel();
	
	/**
	 * Optional, can be null.
	 * @return
	 */
	CustomUIPanelPlugin getCustomPanelPlugin();
	void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex);
	void destroyCustomDetail();


	Set<String> getRelatedEntryIds();


	boolean skipForTags();


	/**
	 * These tags are completely unrelated to the matchesTags() method; these are not player-facing at all.
	 * @return
	 */
	Set<String> getTags();
	/**
	 * These tags are completely unrelated to the matchesTags() method; these are not player-facing at all.
	 * @return
	 */
	void addTag(String tag);
	/**
	 * These tags are completely unrelated to the matchesTags() method; these are not player-facing at all.
	 * @return
	 */
	boolean hasTag(String tag);
	void setIcon(String icon);
	boolean checkTagsWhenLocked();
	ModSpecAPI getSourceMod();

}








