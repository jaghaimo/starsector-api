package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.ExpressionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.Option;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll.OptionAdder;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

public class FireBest extends BaseCommandPlugin {


	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected List<Token> params;
	protected String ruleId;
	protected boolean keepOptions = false;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.ruleId = ruleId;
		this.dialog = dialog;
		this.params = params;
		this.memoryMap = memoryMap;
		String trigger = params.get(0).string;
		if (params.get(0).isVariable()) {
			VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
			trigger = var.memory.getString(var.name);
		}
		if (params.size() > 1) {
			keepOptions = params.get(1).getBoolean(memoryMap);
		}
		
		RulesAPI rules = Global.getSector().getRules();
		RuleAPI rule = rules.getBestMatching(ruleId, trigger, dialog, memoryMap);
		
		if (rule == null) return false;
		
		applyRule(rule);
		
		return true;
	}
	
	public static boolean fire(String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String params) {
		return new FireBest().execute(ruleId, dialog, Misc.tokenize(params), memoryMap);
	}
	
	private void applyRule(RuleAPI rule) {
//		if (!rule.getOptions().isEmpty()) {
//			dialog.getOptionPanel().clearOptions();
//			for (Option option : rule.getOptions()) {
//				dialog.getOptionPanel().addOption(option.text, option.id, null);
//			}
//			if (Global.getSettings().isDevMode()) {
//				DumpMemory.addOption(dialog);
//			}
//		}
		
		List<OptionAdder> options = new ArrayList<OptionAdder>();
		for (Option option : rule.getOptions()) {
			if (option.id.startsWith("(dev)") && !Global.getSettings().isDevMode()) continue;
			options.add(new OptionAdder(option));
		}
		for (ExpressionAPI e : rule.getScriptCopy()) {
			if (e.doesCommandAddOptions()) {
				options.add(new OptionAdder(e));
			}
		}
		
		if (!options.isEmpty()) {
			Collections.sort(options, new Comparator<OptionAdder>() {
				public int compare(OptionAdder o1, OptionAdder o2) {
					float diff = o1.getOrder(memoryMap) - o2.getOrder(memoryMap);
					if (diff < 0) return -1;
					if (diff > 0) return 1;
					return 0;
				}
			});
			
			if (!keepOptions) {
				dialog.getOptionPanel().clearOptions();
			}
			for (OptionAdder option : options) {
				option.add(ruleId, dialog, params, memoryMap);
			}
			if (!keepOptions) {
				if (Global.getSettings().isDevMode()) {
					DevMenuOptions.addOptions(dialog);
				}
			}
		}		
		
		addText(rule.getId(), rule.pickText());
		rule.runScript(dialog, memoryMap);
	}
	
	private void addText(String ruleId, String text) {
		if (text == null || text.isEmpty()) return;

		
		text = Global.getSector().getRules().performTokenReplacement(ruleId, text, dialog.getInteractionTarget(), memoryMap);
//		Map<String, String> tokens = Global.getSector().getRules().getTokenReplacements(ruleId, dialog.getInteractionTarget(), memoryMap);
//		for (String token : tokens.keySet()) {
//			String value = tokens.get(token);
//			text = text.replaceAll("(?s)\\" + token, value);
//		}
//		
//		text = Misc.replaceTokensFromMemory(text, memoryMap);
		
		dialog.getTextPanel().addParagraph(text);
	}
}




