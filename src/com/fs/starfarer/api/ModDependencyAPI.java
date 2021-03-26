package com.fs.starfarer.api;

public interface ModDependencyAPI {
	String getId();
	void setId(String id);
	String getName();
	void setName(String name);
	VersionInfoAPI getVersionInfo();
}
