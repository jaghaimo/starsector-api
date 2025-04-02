package com.fs.starfarer.api.ui;

import java.util.Collection;
import java.util.Set;

import java.awt.Color;

public interface TagDisplayAPI {
	void reset();
	void beginGroup(boolean mutuallyExclusive, String allText);
	void beginGroup(boolean mutuallyExclusive, String allText, float fixedWidth);
	Set<String> getAllTags();
	void addTag(String tag);
	void addTag(String tag, int count);
	void addTag(String tag, String name, int count);
	void addTag(String tag, String name, int count, float fixedWidth);
	void addTag(String tag, String name, int count, float fixedWidth, Color color, Color bright, Color dark);
	void addLineBreakToCurrentGroup(float pad);
	void addGroup(float pad);
	float getTagPad();
	void setTagPad(float tagPad);
	float getMinTagWidth();
	void setMinTagWidth(float minTagWidth);
	void setGroupChecked(int index, boolean checked);
	void setTotalOverrideForCurrentGroup(int totalOverrideForCurrentGroup);
	void check(Collection<String> tags);
	void check(String... tags);
	void uncheck(String... tags);
	void checkAll();
	void uncheckAll();
}
