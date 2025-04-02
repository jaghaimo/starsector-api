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
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

public class FireAll extends BaseCommandPlugin {

	public static class OptionAdder {
		private Option option;
		private ExpressionAPI expression;
		public OptionAdder(ExpressionAPI expression) {
			this.expression = expression;
		}
		public OptionAdder(Option option) {
			this.option = option;
		}
		
		public int getOrder(Map<String, MemoryAPI> memoryMap) {
			if (option != null) {
				return (int) option.order;
			}
			if (expression != null) {
				return expression.getOptionOrder(memoryMap);
			}
			return 0;
		}
		
		public void add(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
			if (option != null) {
				//String text = Misc.replaceTokensFromMemory(option.text, memoryMap);
				String text = Global.getSector().getRules().performTokenReplacement(ruleId, option.text, dialog.getInteractionTarget(), memoryMap);
				
				dialog.getOptionPanel().addOption(text, option.id, null);
			} else if (expression != null) {
				expression.execute(ruleId, dialog, memoryMap);
			}
		}
	}
	
	public static boolean fire(String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String params) {
		return new FireAll().execute(ruleId, dialog, Misc.tokenize(params), memoryMap);
	}

	private InteractionDialogAPI dialog;
	private Map<String, MemoryAPI> memoryMap;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		String trigger = params.get(0).string;
		if (params.get(0).isVariable()) {
			VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
			trigger = var.memory.getString(var.name);
		}
		
		
		MemoryAPI mem = getEntityMemory(memoryMap);
		mem.set("$fireAllTrigger", trigger, 0);
		boolean intercepted = FireBest.fire(null, dialog, memoryMap, "FireAllIntercept");
		if (intercepted) {
			return true; // we did *something*, so: return true
		}
		
		
		RulesAPI rules = Global.getSector().getRules();
		List<RuleAPI> matches = rules.getAllMatching(ruleId, trigger, dialog, memoryMap);
		if (matches.isEmpty()) return false;
		
		List<OptionAdder> options = new ArrayList<OptionAdder>();
		for (RuleAPI rule : matches) {
			//options.addAll(rule.getOptions());
			for (Option option : rule.getOptions()) {
				if (option.id.startsWith("(dev)") && !Global.getSettings().isDevMode()) continue;
				options.add(new OptionAdder(option));
			}
			
			// this actually doesn't work I think? since rule gets applied regardless
			// and so the expression gets executed twice
			for (ExpressionAPI e : rule.getScriptCopy()) {
				if (e.doesCommandAddOptions()) {
					options.add(new OptionAdder(e));
				}
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
			
			dialog.getOptionPanel().clearOptions();
			for (OptionAdder option : options) {
				//dialog.getOptionPanel().addOption(option.text, option.id, null);
				option.add(ruleId, dialog, params, memoryMap);
			}
			if (Global.getSettings().isDevMode()) {
				DevMenuOptions.addOptions(dialog);
			}
		}
		
		for (RuleAPI rule : matches) {
			applyRule(rule);
		}
		
		return true;
	}
	
	private void applyRule(RuleAPI rule) {
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
