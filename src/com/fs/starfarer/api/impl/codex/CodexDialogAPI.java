package com.fs.starfarer.api.impl.codex;

public interface CodexDialogAPI {
	void showEntryDetail(String entryId, boolean openParentCategory, boolean takeHistorySnapshot);
	void showEntryDetail(CodexEntryPlugin entry, boolean openParentCategory, boolean takeHistorySnapshot);
}
