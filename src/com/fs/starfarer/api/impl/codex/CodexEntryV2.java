package com.fs.starfarer.api.impl.codex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.WithSourceMod;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TagDisplayAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class CodexEntryV2 implements CodexEntryPlugin {

	protected String id;
	protected String title;
	protected String icon;
	protected CodexEntryPlugin parent;
	protected Object param;
	protected Object param2;
	protected List<CodexEntryPlugin> children = new ArrayList<>();
	//protected Set<CodexEntryPlugin> related = new LinkedHashSet<>();
	protected Set<String> related = new LinkedHashSet<>();
	protected boolean retainOrderOfChildren = false;
	protected boolean retainOrderOfRelatedEntries = false;
	protected float categorySortTierForRelatedEntries = 1000;
	protected Set<String> tags = new LinkedHashSet<>();
	
	public CodexEntryV2(String id, String title, String icon) {
		this.id = id;
		this.title = title;
		this.icon = icon;
	}
	
	public CodexEntryV2(String id, String title, String icon, Object param) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.param = param;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getSortTitle() {
		return getTitle();
	}
	
	@Override
	public String getSearchString() {
		return getTitle();
	}

	@Override
	public String getIcon() {
		return icon;
	}
	
	@Override
	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public CodexEntryPlugin getParent() {
		return parent;
	}

	@Override
	public void setParent(CodexEntryPlugin parent) {
		this.parent = parent;
	}

	@Override
	public Object getParam() {
		return param;
	}
	
	public void setParam(Object param) {
		this.param = param;
	}

	@Override
	public List<CodexEntryPlugin> getChildren() {
		return children;
	}

	@Override
	public void addChild(CodexEntryPlugin entry) {
		if (entry == null) return;
		entry.setParent(this);
		children.add(entry);
	}

	@Override
	public boolean isRetainOrderOfChildren() {
		return retainOrderOfChildren;
	}
	
	@Override
	public void setRetainOrderOfChildren(boolean retainOrderOfChildren) {
		this.retainOrderOfChildren = retainOrderOfChildren;
	}

	@Override
	public boolean isCategory() {
		return !getChildren().isEmpty() || getParam() == null;
	}
	
	
	@Override
	public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
		if (isCategory()) {
			info.setParaSmallInsignia();
		}
		info.addPara(getTitle(), Misc.getBasePlayerColor(), 0f);
	}

	@Override
	public boolean hasDetail() {
		return !isCategory();
	}

	
	@Override
	public boolean matchesTags(Set<String> tags) {
		return true;
	}
	
	@Override
	public boolean hasTagDisplay() {
		return false;
	}
	
	@Override
	public void configureTagDisplay(TagDisplayAPI tags) {
		
	}

	@Override
	public Set<String> getRelatedEntryIds() {
		return related;
	}
	
	@Override
	public Set<CodexEntryPlugin> getRelatedEntries() {
		Set<CodexEntryPlugin> result = new LinkedHashSet<>();
		for (String id : related) {
			CodexEntryPlugin entry = CodexDataV2.getEntry(id);
			if (entry != null && entry != this) {
				result.add(entry);
			}
		}
		return result;
	}
	
	@Override
	public void addRelatedEntry(CodexEntryPlugin entry) {
		if (entry == null || entry.getId().equals(getId())) return;
		//related.add(entry);
		related.add(entry.getId());
	}
	
	@Override
	public void addRelatedEntry(String id) {
		if (id == null) return;
		//related.add(entry);
		related.add(id);
	}
	
	public void removeRelatedEntry(CodexEntryPlugin entry) {
		if (entry == null) return;
		related.remove(entry.getId());
	}
	
	public void removeRelatedEntry(String id) {
		related.remove(id);
	}

	@Override
	public boolean isRetainOrderOfRelatedEntries() {
		return retainOrderOfRelatedEntries;
	}

	@Override
	public void setRetainOrderOfRelatedEntries(boolean retainOrderOfRelatedEntries) {
		this.retainOrderOfRelatedEntries = retainOrderOfRelatedEntries;		
	}

	@Override
	public float getCategorySortTierForRelatedEntries() {
		return categorySortTierForRelatedEntries;
	}

	@Override
	public void setCategorySortTierForRelatedEntries(float categorySortTierForRelatedEntries) {
		this.categorySortTierForRelatedEntries = categorySortTierForRelatedEntries;
	}
	
	
	@Override
	public List<CodexEntryPlugin> getChildrenRecursive(boolean includeCategories) {
		List<CodexEntryPlugin> result = new ArrayList<>();
		findChildren(this, result, includeCategories);
		return result;
	}
	
	public void findChildren(CodexEntryPlugin curr, List<CodexEntryPlugin> result, boolean includeCategories) {
		if (includeCategories || !curr.isCategory()) {
			result.add(curr);
		}
		for (CodexEntryPlugin child : curr.getChildren()) {
			findChildren(child, result, includeCategories);
		}
	}

	
	@Override
	public boolean hasCustomDetailPanel() {
		return false;
	}
	
	@Override
	public CustomUIPanelPlugin getCustomPanelPlugin() {
		return null;
	}
	
	@Override
	public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
		
	}

	public void destroyCustomDetail() {
		
	}

	@Override
	public boolean isVignetteIcon() {
		return false;
	}
	
	protected boolean checking = false;
	public boolean areAnyRelatedEntriesVisible() {
		// recursed back to this entry due to something else circularly checking its visibility
		if (checking) return false;
		checking = true;
		boolean found = false;
		for (CodexEntryPlugin rel : getRelatedEntries()) {
			if (rel.isVisible()) {
				found = true;
				break;
			}
		}
		checking = false;
		return found;
	}
	
	public boolean areAnyRelatedEntriesUnlocked() {
		// recursed back to this entry due to something else circularly checking its locked status
		if (checking) return isUnlockedIfRequiresUnlock();
		checking = true;
		boolean found = false;
		for (CodexEntryPlugin rel : getRelatedEntries()) {
			if (!rel.isLocked()) {
				found = true;
				break;
			}
		}
		checking = false;
		return found;
	}
	
	@Override
	public boolean isVisible() {
		return isVisibleStandard(getUnlockRelatedTags(), isUnlockedIfRequiresUnlock());
	}
	
	@Override
	public boolean isLocked() {
		return isLockedStandard(getUnlockRelatedTags(), isUnlockedIfRequiresUnlock());
	}
	
	@Override
	public boolean checkTagsWhenLocked() {
		return false;
	}
	
	public Set<String> getUnlockRelatedTags() {
		return null;
	}
	
	public boolean isUnlockedIfRequiresUnlock() {
		return true;
	}
	
	public boolean isVisibleStandard(Set<String> tags, boolean thingUnlocked) {
		if (tags == null) return true;
		
		if (tags.contains(Tags.INVISIBLE_IN_CODEX)) return false;
		if (tags.contains(Tags.CODEX_UNLOCKABLE)) return true;
		
		if (tags.contains(Tags.CODEX_REQUIRE_RELATED)) {
			return areAnyRelatedEntriesVisible();
		}
		return true;
	}
	
	public boolean isLockedStandard(Set<String> tags, boolean thingUnlocked) {
		if (tags == null) return false;
		
		if (CodexDataV2.codexFullyUnlocked()) return false;
		if (tags.contains(Tags.CODEX_UNLOCKABLE) && !thingUnlocked) {
			return true;
		}
		if (tags.contains(Tags.CODEX_REQUIRE_RELATED)) {
			return !areAnyRelatedEntriesUnlocked();
		}
		return false;
	}

	@Override
	public Object getParam2() {
		return param2;
	}

	public void setParam2(Object param2) {
		this.param2 = param2;
	}
	
	@Override
	public boolean skipForTags() {
		return false;
	}
	
	
	@Override
	public Set<String> getTags() {
		return tags;
	}
	
	@Override
	public void addTag(String tag) {
		tags.add(tag);
	}

	@Override
	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
	
	@Override
	public ModSpecAPI getSourceMod() {
		if (getParam() instanceof WithSourceMod) {
			return ((WithSourceMod)getParam()).getSourceMod();
		}
		return null;
	}
}












