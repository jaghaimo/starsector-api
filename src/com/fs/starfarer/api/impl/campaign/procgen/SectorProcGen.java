package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorGenProgress;
import com.fs.starfarer.api.campaign.SectorProcGenPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.CustomConstellationParams;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SectorProcGen implements SectorProcGenPlugin {
	
	public static final float CELL_SIZE = 2000;
	public static final int CONSTELLATION_CELLS = 10;

	public void prepare(CharacterCreationData data) {
		// do this here so that hand-crafted systems using
		// procgen use the proper seed
		if (data.getSeed() > 0) {
			StarSystemGenerator.random.setSeed(data.getSeed());
		}
		StarSystemGenerator.updateBackgroundPickers();
		
//		List<String> names = new ArrayList<String>();
//		
//		Collection<Object> specs = Global.getSettings().getAllSpecs(NameGenData.class);
//		for (Object curr : specs) {
//			NameGenData spec = (NameGenData) curr;
//			names.add(spec.getName());
//		}
//		MarkovNames.load(names, 3);
		
		MarkovNames.loadIfNeeded();
	}
	
	
	public void generate(CharacterCreationData data, SectorGenProgress progress) {
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight"); 

		
		boolean small = "small".equals(data.getSectorSize());
		StarAge sectorAge = data.getSectorAge();
		if (sectorAge == null) {
			sectorAge = StarAge.ANY;
		}
		
		int cellsWide = (int) (w / CELL_SIZE);
		int cellsHigh = (int) (h / CELL_SIZE);
		
		
		boolean [][] cells = new boolean [cellsWide][cellsHigh];
		int count = 100; 

		int vPad = CONSTELLATION_CELLS / 2;
		int hPad = CONSTELLATION_CELLS / 2;
		if (small) {
			hPad = (int) (31000 / CELL_SIZE); 
			vPad = (int) (19000 / CELL_SIZE); 
		}
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				if (i <= hPad || j <= vPad || i >= cellsWide - hPad || j >= cellsHigh - vPad) {
					cells[i][j] = true;
				}
			}
		}
		
		//System.out.println("EXISTING SYSTEMS: ");
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			int [] index = getIndex(system.getLocation());
			int x = index[0];
			int y = index[1];
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			if (x > cellsWide - 1) x = cellsWide - 1;
			if (y > cellsHigh - 1) y = cellsHigh - 1;
			
//			if (system.getName().toLowerCase().startsWith("groom")) {
//				System.out.println("ewfwefwe");
//			}
			//System.out.println(system.getName());
			
			blotOut(cells, x, y, 8);
		}
		
		// for the Orion-Perseus Abyss label/the Abyss itself
//		blotOut(cells, 0, 0, 12);
//		blotOut(cells, 6, 0, 12);
//		blotOut(cells, 12, 0, 12);
		blotOut(cells, 0, 0, 22);
		blotOut(cells, 16, 3, 16);
		blotOut(cells, 5, 11, 12);
		
		progress.render("Generating sector...", 0.1f);
		
		List<CustomConstellationParams> custom = getCustomConstellations();
		
		List<Constellation> constellations = new ArrayList<Constellation>();
		for (int k = 0; k < count; k++) {
			WeightedRandomPicker<Pair<Integer, Integer>> picker = new WeightedRandomPicker<Pair<Integer,Integer>>(StarSystemGenerator.random);
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					if (cells[i][j]) continue;
					
					Pair<Integer, Integer> p = new Pair<Integer, Integer>(i, j);
					picker.add(p);
				}
			}
			
			Pair<Integer, Integer> pick = picker.pick();
			if (pick == null) continue;
			
			blotOut(cells, pick.one, pick.two, CONSTELLATION_CELLS);
			
			float x = pick.one * CELL_SIZE - w / 2f;
			float y = pick.two * CELL_SIZE - h / 2f;
			
			CustomConstellationParams params = new CustomConstellationParams(StarAge.ANY);
			if (!custom.isEmpty()) params = custom.remove(0);
			
			StarAge age = sectorAge;
			if (age == StarAge.ANY) {
//				if (x < -w/6f) {
//					age = StarAge.YOUNG;
//				} else if (x > w/6f) {
//					age = StarAge.OLD;
//				} else {
//					age = StarAge.AVERAGE;
//				}
				WeightedRandomPicker<StarAge> agePicker = new WeightedRandomPicker<StarAge>(StarSystemGenerator.random);
				agePicker.add(StarAge.YOUNG);
				agePicker.add(StarAge.AVERAGE);
				agePicker.add(StarAge.OLD);
				age = agePicker.pick();
			}
			
			params.age = age;
			
			params.location = new Vector2f(x, y);
			Constellation c = new StarSystemGenerator(params).generate();
			constellations.add(c);
			
			progress.render("Generating constellations...", 0.1f + 0.8f * (float)k / (float) count);
		}
		
		

		HyperspaceTerrainPlugin hyper = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyper);
		editor.regenNoise();
		editor.noisePrune(0.8f);
		editor.regenNoise();
		
		
		Random random = StarSystemGenerator.random;
		
		// add a spiral going from the outside towards the center
		float angleOffset = random.nextFloat() * 360f;
		editor.clearArc(0f, 0f, w / 2f, w / 2f + 3000, 
						angleOffset + 0f, angleOffset + 360f * (2f + random.nextFloat() * 2f), 0.01f, 0.33f);
		
		// do some random arcs
		int numArcs = (int) (20f + 8f * random.nextFloat());
		
		for (int i = 0; i < numArcs; i++) {
			float dist = w/2f + w/2f * random.nextFloat();
			float angle = random.nextFloat() * 360f;
			
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dir.scale(dist - (w/12f + w/3f * random.nextFloat()));
			
			//float tileSize = nebulaPlugin.getTileSize();
			//float width = tileSize * (2f + 4f * random.nextFloat());
			float width = 800f * (1f + 2f * random.nextFloat());
			
			float clearThreshold = 0f + 0.5f * random.nextFloat();
			//clearThreshold = 0f;
			
			editor.clearArc(dir.x, dir.y, dist - width/2f, dist + width/2f, 0, 360f, clearThreshold);
		}
		
		clearAbyssalHyperspaceAndSetSystemTags();
		
		progress.render("Generating objects...", 0.9f);
		
		
		ThemeGenContext context = new ThemeGenContext();
		context.constellations = constellations;
		SectorThemeGenerator.generate(context);
		
		progress.render("Finishing generation...", 1f);
		
		//MarkovNames.clear();
		
		//System.out.println("Generated " + constellations.size() + " constellations");
		
		
//		List constellations = Global.getSettings().getConstellations();
//		for (int i = 0; i < constellations.size(); i++) {
//			CustomConstellationParams params = (CustomConstellationParams) constellations.get(i);
//			new StarSystemGenerator(params).generate();
//		}
	}
	
	public static void clearAbyssalHyperspaceAndSetSystemTags() {
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		
		HyperspaceTerrainPlugin hyper = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyper);
		
		HyperspaceAbyssPlugin ac = hyper.getAbyssPlugin();
		float ts = editor.getTileSize();
		for (float x = -w/2f; x < w/2f; x += ts * 0.8f) { 
			for (float y = -h/2f; y < h/2f; y += ts * 0.8f) {
				if (ac.isInAbyss(new Vector2f(x, y))) {
					editor.setTileAt(x, y, -1, 0f, false);
				}
			}
		}
		
		for (StarSystemAPI system : Misc.getAbyssalSystems()) {
			system.addTag(Tags.SYSTEM_ABYSSAL);
		}
	}
	
	
	
	
	
	public static void blotOut(boolean [][] cells, int x, int y, int c) {
		//int c = CONSTELLATION_CELLS;
		for (int i = Math.max(0, x - c / 2); i <= x + c / 2 && i < cells.length; i++) {
			for (int j = Math.max(0, y - c / 2); j <= y + c / 2 && j < cells[0].length; j++) {
				cells[i][j] = true;
			}
		}
	}
	
	public static int [] getIndex(Vector2f loc) {
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		
		int x = (int) ((loc.x + w / 2f) / CELL_SIZE);
		int y = (int) ((loc.y + h / 2f) / CELL_SIZE);
		
		return new int []{x, y};
	}
	
	
	public static List<CustomConstellationParams> getCustomConstellations() {
		List<CustomConstellationParams> result = new ArrayList<CustomConstellationParams>();

		for (StarSystemType type : EnumSet.allOf(StarSystemType.class)) {
			CustomConstellationParams params = new CustomConstellationParams(StarAge.ANY);
			params.systemTypes.add(type);
			if (type == StarSystemType.NEBULA) params.forceNebula = true;
			result.add(params);
		}
		
		CustomConstellationParams params = new CustomConstellationParams(StarAge.ANY);
		params.starTypes.add(StarTypes.BLACK_HOLE);
		result.add(params);
		
		params = new CustomConstellationParams(StarAge.ANY);
		params.starTypes.add(StarTypes.NEUTRON_STAR);
		result.add(params);
		
		return result;
	}
	
}


















