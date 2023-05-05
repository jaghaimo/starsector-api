package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.LagrangePointType;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ProcgenUsedNames {
	
	public static class NamePick {
		public NameGenData spec;
		public String nameWithRomanSuffixIfAny;
		public String secondaryWithRomanSuffixIfAny;
		public NamePick(NameGenData spec, String nameWithRomanSuffixIfAny, String secondaryWithRomanSuffixIfAny) {
			this.spec = spec;
			this.nameWithRomanSuffixIfAny = nameWithRomanSuffixIfAny;
			this.secondaryWithRomanSuffixIfAny = secondaryWithRomanSuffixIfAny;
		}
		
	}
	
	public static final String KEY = "ProcgenUsedNames_key"; 
	private Set<String> names = new HashSet<String>();
	
	public static void notifyUsed(String name) {
		getUsed().names.add(name);
	}
	
	public static boolean isUsed(String name) {
		return getUsed().names.contains(name);
	}
	
	public static ProcgenUsedNames getUsed() { //for (String name : ((ProcgenUsedNames) test).names) System.out.println(name);
		Object test = Global.getSector().getPersistentData().get(KEY);
		if (test == null) {
			test = new ProcgenUsedNames();
			Global.getSector().getPersistentData().put(KEY, test);
		}
		return (ProcgenUsedNames) test;
	}
	
	
	public static NamePick pickName(String tag, String parent, LagrangePointType lagrangePoint) {
		WeightedRandomPicker<NamePick> picker = new WeightedRandomPicker<NamePick>(StarSystemGenerator.random);
		
		
		Collection<NameGenData> all = Global.getSettings().getAllSpecs(NameGenData.class);
		
		// names for child of parent, if any
		if (parent != null) {
			for (NameGenData spec : all) {
				if (isUsed(spec.getName())) continue;
				if (!spec.hasTag(tag)) continue;
				if (parent == null && spec.getName().contains("$parent")) continue;
				if (spec.hasParent(parent)) {
					picker.add(new NamePick(spec, spec.getName(), spec.getSecondary()), spec.getFrequency());
				}
			}
		}
		
		// if needed, add names w/o parent
		if (picker.isEmpty()) {
			for (NameGenData spec : all) {
				if (isUsed(spec.getName())) continue;
				if (!spec.hasTag(tag)) continue;
				if (!spec.getParents().isEmpty()) continue;
				if (parent == null && spec.getName().contains("$parent")) continue;
				picker.add(new NamePick(spec, spec.getName(), spec.getSecondary()), spec.getFrequency());
			}
		}
		
		// if there's nothing, try to create a name using markov chains
		// before moving on to roman numerals
		if (picker.isEmpty()) {
			int attempts = 10;
			for (int i = 0; i < attempts; i++) {
				MarkovNameResult name = MarkovNames.generate(picker.getRandom());
				if (name == null || name.name == null) continue;
				if (isUsed(name.name)) continue;
				
				NameGenData data = new NameGenData(name.name, null);
				NamePick pick = new NamePick(data, name.name, null);
				return pick;
			}
		}
		
		
		
		// if still no names, we're out of names. start adding roman numerals.
		// (or there's nothing at all for the tag, but that's a bug elsewhere)
		if (picker.isEmpty()) {
			OUTER: for (Object obj : all) {
				NameGenData spec = (NameGenData) obj;
				if (!spec.hasTag(tag)) continue;
				
				String base = spec.getName();
				for (int i = 2; i < 4000; i++) {
					String name = base + " " + Global.getSettings().getRoman(i);
					if (isUsed(name)) continue;
					if (parent == null && spec.getName().contains("$parent")) continue;
					
					String secondary = null;
					if (spec.getSecondary() != null) {
						secondary = spec.getSecondary() + " " + Global.getSettings().getRoman(i);
					}
					picker.add(new NamePick(spec, name, secondary), (4000f - i) * spec.getFrequency()); // lower numbers more likely to be picked
					continue OUTER;
				}
			}
		}
		
		NamePick pick = picker.pick();
		
		if (pick != null) {
			pick.nameWithRomanSuffixIfAny = doTokenReplacement(pick.nameWithRomanSuffixIfAny, parent, lagrangePoint);
			if (pick.secondaryWithRomanSuffixIfAny != null) {
				pick.secondaryWithRomanSuffixIfAny = doTokenReplacement(pick.secondaryWithRomanSuffixIfAny, parent, lagrangePoint);
			}
			
		}
		
		return pick;
	}
	
	public static String doTokenReplacement(String name, String parent, LagrangePointType lagrange) {
		if (parent != null) {
			name = name.replaceAll("\\$parent", parent);
			name = name.replaceAll("s's", "s'");
		}
		if (lagrange != null) {
			name = name.replaceAll("\\$L", lagrange.name());
		} else {
			name = name.replaceAll("\\$L ", "");
		}
		return name;
	}
}



































