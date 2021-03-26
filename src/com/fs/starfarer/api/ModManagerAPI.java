package com.fs.starfarer.api;

import java.util.List;

public interface ModManagerAPI {
	List<ModSpecAPI> getAvailableModsCopy();
	List<ModSpecAPI> getEnabledModsCopy();
	boolean isModEnabled(String id);
	List<ModPlugin> getEnabledModPlugins();
	ModSpecAPI getModSpec(String id);
	int getRequiredMemory();
}
