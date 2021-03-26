package com.fs.starfarer.api.campaign.rules;

public interface HasMemory {
	MemoryAPI getMemory();
	MemoryAPI getMemoryWithoutUpdate();
}
