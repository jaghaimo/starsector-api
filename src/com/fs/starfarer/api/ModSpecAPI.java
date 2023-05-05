package com.fs.starfarer.api;

import java.util.List;
import java.util.Set;

public interface ModSpecAPI {
	boolean isUtility();
	String getModPluginClassName();
	boolean isTotalConversion();
	String getName();
	String getId();
	String getVersion();
	VersionInfoAPI getVersionInfo();
	String getDesc();
	String getPath();
	String getDirName();
	VersionInfoAPI getGameVersionInfo();
	String getGameVersion();
	Set<String> getFullOverrides();
	List<String> getJars();
	String getAuthor();
	int getRequiredMemoryMB();
	void setRequiredMemoryMB(int requiredMemoryMB);
	List<ModDependencyAPI> getDependencies();
	List<ModDependencyAPI> getAllDependencies();
	String getSortString();
	void setSortString(String sortString);
}
