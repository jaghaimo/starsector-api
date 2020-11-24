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
	String getDesc();
	String getPath();
	String getDirName();
	String getGameVersion();
	Set<String> getFullOverrides();
	List<String> getJars();
	String getAuthor();
}
