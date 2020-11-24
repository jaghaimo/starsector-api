package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	SalvageSpecialInteraction
 */
public class SalvageSpecialInteraction extends BaseCommandPlugin {

	public static interface SalvageSpecialData {
		SalvageSpecialPlugin createSpecialPlugin();
	}
	
	
	public static interface SalvageSpecialPlugin {
		void init(InteractionDialogAPI dialog, Object specialData);
		void optionSelected(String optionText, Object optionData);
		boolean isDone();
		boolean endWithContinue();
		boolean shouldShowAgain();
		boolean shouldAbortSalvageAndRemoveEntity();
	}
	
	
	public static class SalvageSpecialDialogPlugin implements InteractionDialogPlugin {
		private InteractionDialogAPI dialog;
		private InteractionDialogPlugin originalPlugin;
		private Map<String, MemoryAPI> memoryMap;
		private final SalvageSpecialPlugin special;
		private final Object specialData;

		public SalvageSpecialDialogPlugin(InteractionDialogPlugin originalPlugin, SalvageSpecialPlugin special, Object specialData, Map<String, MemoryAPI> memoryMap) {
			this.originalPlugin = originalPlugin;
			this.special = special;
			this.specialData = specialData;
			this.memoryMap = memoryMap;
		}

		public void init(InteractionDialogAPI dialog) {
			this.dialog = dialog;
			
			special.init(dialog, specialData);
			if (special.isDone()) {
				endSpecial(special.endWithContinue(), special.shouldAbortSalvageAndRemoveEntity());
			} else {
				if (Global.getSettings().isDevMode()) {
					DevMenuOptions.addOptions(dialog);
				}
			}
		}
		public void optionSelected(String optionText, Object optionData) {
			if (optionText != null) {
				dialog.getTextPanel().addParagraph(optionText, Global.getSettings().getColor("buttonText"));
			}
			if (optionData == DumpMemory.OPTION_ID) {
				new DumpMemory().execute(null, dialog, null, getMemoryMap());
				return;
			} else if (DevMenuOptions.isDevOption(optionData)) {
				DevMenuOptions.execute(dialog, (String) optionData);
				return;
			}
			
			
			special.optionSelected(optionText, optionData);
			if (special.isDone()) {
				endSpecial(special.endWithContinue(), special.shouldAbortSalvageAndRemoveEntity());
			} else {
				if (Global.getSettings().isDevMode()) {
					DevMenuOptions.addOptions(dialog);
				}
			}
		}
		
		public void endSpecial(boolean withContinue, boolean withAbort) {
			if (!special.shouldShowAgain()) {
				BaseCommandPlugin.getEntityMemory(memoryMap).unset(MemFlags.SALVAGE_SPECIAL_DATA);
			}
			
			
			dialog.setPlugin(originalPlugin);
			if (withAbort) {
				Misc.fadeAndExpire(dialog.getInteractionTarget(), 1f);
				dialog.dismiss();
			} else {
				if (withContinue) {
					FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinished");
				} else {
					FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinishedNoContinue");
				}
			}
		}
		
		public void advance(float amount) {
		}
		public void backFromEngagement(EngagementResultAPI battleResult) {
		}
		public Object getContext() {
			return null;
		}
		public Map<String, MemoryAPI> getMemoryMap() {
			return memoryMap;
		}
		public void optionMousedOver(String optionText, Object optionData) {
		}
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		//SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(entity.getCustomEntityType());
		
		MemoryAPI memory = getEntityMemory(memoryMap);
//		long seed = memory.getLong(MemFlags.MEMORY_KEY_SALVAGE_SEED);
//		Random random = Misc.getRandom(seed, 50);
		
		//dialog.getTextPanel().addParagraph("Test special interactions");

		
		final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		
		
		Object specialData = memory.get(MemFlags.SALVAGE_SPECIAL_DATA);
		
//		Class specialClass = dataToSpecialPlugin.get(specialData.getClass());
//		if (specialClass == null) {
//			FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinishedNoContinue");
//			return true;
//		}
		SalvageSpecialPlugin special = null;
		if (specialData instanceof SalvageSpecialData) {
			special = ((SalvageSpecialData) specialData).createSpecialPlugin();
		}
		
		if (special == null) {
			FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinishedNoContinue");
			return true;
		}

		
		//SalvageSpecialPlugin special = new BaseSalvageSpecial();
		//SalvageSpecialPlugin special = new DomainSurveyDerelictSpecial();
		//try {
			//SalvageSpecialPlugin special = (SalvageSpecialPlugin) specialClass.newInstance();
			
			//SalvageSpecialDialogPlugin plugin = new SalvageSpecialDialogPlugin(originalPlugin, special, specialData, memoryMap);
			SalvageSpecialDialogPlugin plugin = new SalvageSpecialDialogPlugin(originalPlugin, special, specialData, memoryMap);
			dialog.setPlugin(plugin);
			plugin.init(dialog);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (InstantiationException e) {
//			throw new RuntimeException(e);
//		}
	
		return true;
	}

	
	
	
	
	
	
	
	
	
	
}


















