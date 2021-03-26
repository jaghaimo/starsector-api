package com.fs.starfarer.api;

public interface VersionInfoAPI {
	String getMajor();
	void setMajor(String major);
	String getMinor();
	void setMinor(String minor);
	String getPatch();
	void setPatch(String patch);
	
	void setFromString(String str);
	String getString();
	
	boolean isSet();

}
