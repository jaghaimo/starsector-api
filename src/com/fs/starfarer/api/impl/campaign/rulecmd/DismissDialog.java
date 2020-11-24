package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class DismissDialog extends BaseCommandPlugin {

	public static final String DISMISS_PARAM = "$core_dismissParam";

	/**
	 * Unsets it as well - subsequent call will return null.
	 * @return
	 */
	public static String getDismissParam() {
		String result = Global.getSector().getMemoryWithoutUpdate().getString(DISMISS_PARAM);
		Global.getSector().getMemoryWithoutUpdate().unset(DISMISS_PARAM);
		return result;
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog != null) {
			
			if (params != null && params.size() > 0) {
				String str = params.get(0).getString(memoryMap);
				Global.getSector().getMemoryWithoutUpdate().set(DISMISS_PARAM, str);
			}
			
			dialog.dismiss();
			return true;
		}
		return false;
	}

}
