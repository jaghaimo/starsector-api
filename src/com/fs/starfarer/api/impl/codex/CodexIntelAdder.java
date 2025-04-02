package com.fs.starfarer.api.impl.codex;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public class CodexIntelAdder implements EveryFrameScript {

	public static String KEY = "$core_codexIntelAdder";
	
	public static CodexIntelAdder get() {
		CodexIntelAdder adder = (CodexIntelAdder) Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (adder == null) {
			adder = new CodexIntelAdder();
		}
		return adder;
	}
	
	protected float delay = 0f;
	protected LinkedHashSet<String> unlockedEntries = new LinkedHashSet<>();
	
	public CodexIntelAdder() {
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		Global.getSector().addScript(this);
	}
	
	
	public void addEntry(String entryId) {
		CodexEntryPlugin entry = CodexDataV2.getEntry(entryId);
		if (entry == null || entry.isLocked()) return;
		unlockedEntries.add(entryId);
		delay = 1f;
	}
	
	@Override
	public void advance(float amount) {
		delay -= amount;
		if (delay <= 0) {
			delay = 0f;
			if (!unlockedEntries.isEmpty()) {
				if (!CodexDataV2.codexFullyUnlocked()) {
					new CodexUpdateIntel(unlockedEntries);
				}
				unlockedEntries = new LinkedHashSet<>();
			}
		}
	}	
	
	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean runWhilePaused() {
		return false;
	}

}








