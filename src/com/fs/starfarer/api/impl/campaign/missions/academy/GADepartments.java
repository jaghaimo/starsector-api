package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GADepartments {

	public static String SOCIAL = "social";
	public static String INDUSTRIAL = "industrial";
	public static String MILITARY = "military";
	public static String SCIENCE = "science";
	public static String WEIRD = "weird";

	
	public static class Department {
		public String name;
		public Set<String> tags = new HashSet<String>();
		
		public Department(String name, String ... tags) {
			this.name = name;
			for (String tag : tags) {
				this.tags.add(tag);
			}
		}
	}
	
	public static List<Department> departments = new ArrayList<Department>();
	public static void add(String name, String ... tags) {
		departments.add(new Department(name, tags));
	}
	
	static {
		add("Ancient Earth Studies", SOCIAL);
		add("Ancient Terran Philosophy", SOCIAL);
		add("Applied Anorthic Geometry", SCIENCE, WEIRD);
		add("Applied Holoarts", SOCIAL);
		add("Astrochemistry", INDUSTRIAL, SCIENCE);
		add("Astrometrics", SCIENCE);
		add("AI Defense Studies", SCIENCE, WEIRD, MILITARY);
		add("Classical Terran Studies", SOCIAL);
		add("Cybermedical Engineering", INDUSTRIAL, SOCIAL);
		add("Cybernetic Physiology", SCIENCE, SOCIAL); 
		add("Demographic Calculus", SOCIAL);
		add("Domain Sociology", SOCIAL);
		add("Domain History", SOCIAL);
		add("Environmental Engineering", INDUSTRIAL);
		add("Exotic Manifold Engineering", INDUSTRIAL, WEIRD);
		add("Gravity Control", INDUSTRIAL, SCIENCE, MILITARY);
		add("Gravity Dynamics", SCIENCE);
		add("Holoart Studies", SOCIAL);
		add("Hyperspace Physics", SCIENCE);
		add("LAMBDA-F Lab", SCIENCE, WEIRD);
		add("Nanotech Engineering", INDUSTRIAL);
		add("Nanorobotics Lab", INDUSTRIAL, SCIENCE, WEIRD);
		add("Nonlinear Mathematics", SCIENCE, WEIRD);
		add("Orbital Architecture", SOCIAL, INDUSTRIAL, MILITARY);
		add("Post-Expansion Philosophy", SOCIAL);
		add("Persean Sociology", SOCIAL);
		add("Phase Physics", SCIENCE);
		add("Quantum Chemistry", SCIENCE, WEIRD);
		add("N-Space Resonance Lab", SCIENCE, WEIRD);
		add("Special Collections Library", SOCIAL, INDUSTRIAL, SCIENCE, WEIRD, MILITARY);
		add("Tau Hyperphysics", SCIENCE, WEIRD);
		add("Templeman Hyperwave Theory", WEIRD);
		add("Terran Biology", SCIENCE); 
		add("Xenobiology", SCIENCE); 
	}
	
	
	public static String pick(Random random, ReqMode mode, String ... tags) {
		WeightedRandomPicker<Department> picker = new WeightedRandomPicker<Department>(random);
		
		Set<String> tagSet = new HashSet<String>();
		tagSet.addAll(Arrays.asList(tags));
		
		for (Department d : departments) {
			if (!matchesTags(mode, tagSet, d.tags)) {
				continue;
			}
			picker.add(d);
			break;
		}
		
		Department d = picker.pick();
		if (d == null) d = departments.get(0);
		
		return d.name;
	}
	
	protected static boolean matchesTags(ReqMode mode, Collection<String> tags, Collection<String> set) {
		switch (mode) {
		case ALL:
			for (String tag : tags) if (!set.contains(tag)) return false;
			return true;
		case ANY:
			for (String tag : tags) if (set.contains(tag)) return true;
			return false;
		case NOT_ALL:
			for (String tag : tags) if (!set.contains(tag)) return true;
			return false;
		case NOT_ANY:
			for (String tag : tags) if (set.contains(tag)) return false;
			return true;
		}
		return false;
	}
	
	public static String pick(Random random, String ... tags) {
		WeightedRandomPicker<Department> picker = new WeightedRandomPicker<Department>(random);
		
		for (Department d : departments) {
			for (String tag : tags) {
				if (d.tags.contains(tag)) {
					picker.add(d);
					break;
				}
			}
		}
		
		Department d = picker.pick();
		if (d == null) d = departments.get(0);
		
		return d.name;
	}
	
	public static String pickWithAllTags(Random random, String ... tags) {
		WeightedRandomPicker<Department> picker = new WeightedRandomPicker<Department>(random);
		
		OUTER: for (Department d : departments) {
			for (String tag : tags) {
				if (!d.tags.contains(tag)) {
					continue OUTER;
				}
			}
			picker.add(d);
		}
		
		Department d = picker.pick();
		if (d == null) d = departments.get(0);
		
		return d.name;
	}
	
	
	
	
}



















