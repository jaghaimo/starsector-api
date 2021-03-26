package com.fs.starfarer.api.campaign;

public interface CommDirectoryEntryAPI {
	
	public static enum EntryType {
		PERSON,
	}
	
	String getId();
	
	EntryType getType();

	Object getEntryData();

	String getTitle();

	String getText();

	void setEntryData(Object entryData);

	boolean isHidden();
	void setHidden(boolean hidden);
}
