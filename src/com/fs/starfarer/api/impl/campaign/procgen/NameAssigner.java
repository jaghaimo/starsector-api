package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames.NamePick;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.LagrangePointType;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class NameAssigner {
	
	public static class NamingTreeNode {
		public NamingTreeNode parent;
		public LagrangePointType lagrangePointType = null;
		public NamePick name;
		public SectorEntityToken entity;
		public List<NamingTreeNode> children = new ArrayList<NamingTreeNode>();
		public StarSystemAPI system;
		public NamingTreeNode(StarSystemAPI system) {
			this.system = system;
		}
		
		public boolean isPrimaryStar() {
			return entity == system.getStar();
		}
		public boolean isSecondaryStar() {
			return entity == system.getSecondary();
		}
		public boolean isTertiaryStar() {
			return entity == system.getTertiary();
		}
		public boolean isMoon() {
			return !isTerrain() && parent != null &&
					parent.entity instanceof PlanetAPI && 
					!((PlanetAPI)parent.entity).isStar();
		}
		public boolean isTerrain() {
			return entity instanceof CampaignTerrainAPI;
		}
	}
	
	private Constellation constellation;
	private NamePick constellationName;
	private NamingTreeNode root;

	private float specialNamesProbability = 0.5f;
	private boolean renameSystem = true;
	private int structuralNameOffset = 0;
	
	public NameAssigner(Constellation constellation) {
		this.constellation = constellation;
	}
	
	public void setSpecialNamesProbability(float specialNamesProbability) {
		this.specialNamesProbability = specialNamesProbability;
	}

	public void setRenameSystem(boolean renameStar) {
		this.renameSystem = renameStar;
	}
	
	public void setStructuralNameOffset(int structuralNameOffset) {
		this.structuralNameOffset = structuralNameOffset;
	}

	public void assignNames(String name, String secondary) {
		if (constellation.getSystems().isEmpty()) return;
		
		if (name == null) {
			constellationName = ProcgenUsedNames.pickName(NameGenData.TAG_CONSTELLATION, null, null);
		} else {
			NameGenData data = (NameGenData) Global.getSettings().getSpec(NameGenData.class, name, true);
			if (data == null) {
				data = new NameGenData(name, null);
			} else {
				ProcgenUsedNames.notifyUsed(name);
			}
			constellationName = new NamePick(data, name, secondary);
		}
		if (!constellationName.spec.isReusable() && !constellation.isLeavePickedNameUnused()) {
			ProcgenUsedNames.notifyUsed(constellationName.nameWithRomanSuffixIfAny);
		}
		Global.getSettings().greekLetterReset();

		constellation.setNamePick(constellationName);
		
		Collections.sort(constellation.getSystems(), new Comparator<StarSystemAPI>() {
			public int compare(StarSystemAPI o1, StarSystemAPI o2) {
				if (o1.getStar() == null || o2.getStar() != null) return 1; 
				if (o1.getStar() != null || o2.getStar() == null) return -1; 
				return (int) Math.signum(o1.getStar().getRadius() - o2.getStar().getRadius());
			}
		});
		
		
		for (StarSystemAPI system : constellation.getSystems()) {
			String base = constellationName.nameWithRomanSuffixIfAny;
			if (constellationName.secondaryWithRomanSuffixIfAny != null) {
				base = constellationName.secondaryWithRomanSuffixIfAny;
			}
			String n = Global.getSettings().getNextGreekLetter(constellationName) + " " + base;

			computeNamingTree(system);
			assignStructuralNames(system, n);
			if (StarSystemGenerator.random.nextFloat() < specialNamesProbability || (constellation.systems.size() <= 1 && specialNamesProbability > 0)) {
				assignSpecialNames(root);
			}
			
			updateJumpPointDestinationNames(system);
		}
//		String tag = NameGenData.TAG_PLANET;
//		if (isMoon) tag = NameGenData.TAG_MOON;
//		NamePick namePick = ProcgenUsedNames.pickName(tag, null);
	}
	
	
	public void assignSpecialNames(NamingTreeNode curr) {
		String tag = null;
		CampaignTerrainPlugin plugin = null;
		if (curr.entity != null) {
			if (curr.isTerrain()) {
				CampaignTerrainAPI terrain = (CampaignTerrainAPI) curr.entity;
				plugin = terrain.getPlugin();
				if (plugin instanceof AsteroidFieldTerrainPlugin) {
					tag = NameGenData.TAG_ASTEROID_FIELD;
				} else if (plugin instanceof AsteroidBeltTerrainPlugin) {
					tag = NameGenData.TAG_ASTEROID_BELT;
				} else if (plugin instanceof RingSystemTerrainPlugin) {
					RingSystemTerrainPlugin ringPlugin = (RingSystemTerrainPlugin) plugin;
					if (ringPlugin.getNameForTooltip() != null && ringPlugin.getNameForTooltip().contains("Accretion")) {
						tag = NameGenData.TAG_ACCRETION;
					}
				} else if (plugin instanceof NebulaTerrainPlugin) {
					tag = NameGenData.TAG_NEBULA;
				} else if (plugin instanceof MagneticFieldTerrainPlugin) {
					tag = NameGenData.TAG_MAGNETIC_FIELD;
				}
			} else if (curr.isMoon()) {
				tag = NameGenData.TAG_MOON;
			} else if (curr.isPrimaryStar() || curr.isSecondaryStar() || curr.isTertiaryStar()) {
				tag = NameGenData.TAG_STAR;
			} else if (curr.entity instanceof PlanetAPI) {
				tag = NameGenData.TAG_PLANET;
			}
		}
		
		if (tag != null) {
			String parent = null;
			if (curr.parent != null && curr.parent.name != null) {
				parent = curr.parent.name.spec.getName();
			}
			if (curr == root) {
				parent = constellationName.spec.getName();
			}
			
			
			boolean noRename = curr.system.getStar() == curr.entity && !renameSystem;
			NamePick pick = ProcgenUsedNames.pickName(tag, parent, curr.lagrangePointType);
			if (!pick.spec.isReusable() && !noRename) {
				ProcgenUsedNames.notifyUsed(pick.nameWithRomanSuffixIfAny);
			}
			
			if (pick != null) {
				if (noRename) {
					String name = curr.system.getBaseName();
					NameGenData data = (NameGenData) Global.getSettings().getSpec(NameGenData.class, name, true);
					if (data == null) {
						data = new NameGenData(name, null);
					} else {
						ProcgenUsedNames.notifyUsed(name);
					}
					curr.name = new NamePick(data, name, null);
				} else {
					curr.name = pick;
					String name = pick.nameWithRomanSuffixIfAny;
					List<SectorEntityToken> all = constellation.allEntitiesAdded.get(curr.entity);
					for (SectorEntityToken entity : all) {
	//					if (tag != null && tag.equals(NameGenData.TAG_RING) && pick.spec.isReusable()) {
	//						if (entity instanceof CampaignTerrainAPI) {
	//							((CampaignTerrainAPI) entity).getPlugin().setTerrainName(name);
	//						}
	//					}
						entity.setName(name);
						if (entity.getMarket() != null) entity.getMarket().setName(name);
						updateJumpPointNameFor(entity);
						if (entity instanceof CampaignTerrainAPI) {
							((CampaignTerrainAPI) entity).getPlugin().setTerrainName(name);
						}
					}
					if (curr.system.getStar() == curr.entity) {
						curr.system.setBaseName(name);
					}
				}
			}
		}
		
		for (NamingTreeNode node : curr.children) {
			assignSpecialNames(node);
		}
		
	}
	
	
	public void assignStructuralNames(StarSystemAPI system, String name) {
//		if (system.getSecondary() != null) {
//			System.out.println("wefwefe");
//		}
		if (renameSystem) {
			system.setBaseName(name);
			if (system.getStar() != null) {
				system.getStar().setName(name);
				updateJumpPointNameFor(system.getStar());
			}
		} else {
			name = system.getBaseName();
		}
		
		int i = structuralNameOffset;
		for (NamingTreeNode node : root.children) {
//			if (node.entity instanceof PlanetAPI && ((PlanetAPI)node.entity).isStar()) {
//				System.out.println("wefwefwefwe");
//			}
			assignSNamesHelper(node, name, i);
			
			if (node.entity instanceof PlanetAPI) {
				PlanetAPI planet = (PlanetAPI) node.entity;
				if (!planet.isStar()) {
					i++;
				}
			}
		}
	}
	
	public void assignSNamesHelper(NamingTreeNode curr, String parentName, int index) {
		if (parentName == null) return;
		
		String name = parentName;
		
		if (curr.entity != null) {
			CampaignTerrainAPI terrain = null;
			CampaignTerrainPlugin plugin = null;
			if (curr.entity instanceof CampaignTerrainAPI) {
				terrain = (CampaignTerrainAPI) curr.entity;
				plugin = terrain.getPlugin();
			}
			boolean rename = true;
			if (curr.isSecondaryStar()) {
				name += " B";
			} else if (curr.isTertiaryStar()) {
				name += " C";
			} else {
				String [] moonPostfixes = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
				if (curr.isTerrain()) {
					if (curr.lagrangePointType != null) {
						name += " " + curr.lagrangePointType.name();
						if (plugin instanceof AsteroidFieldTerrainPlugin) {
							name += " Asteroid Field";
						} else if (plugin instanceof NebulaTerrainPlugin) {
							name += " Nebula";
						}
					} else {
						rename = false;
					}
				} else if (curr.isMoon()) {
					if (curr.lagrangePointType != null) {
						name += "-" + curr.lagrangePointType.name();
					} else {
						name += "-" + moonPostfixes[index];
					}
				} else {
					name += " " + Global.getSettings().getRoman(index + 1);
				}
			}
			
			if (rename) {
//				curr.entity.setName(name);
//				if (plugin != null) {
//					plugin.setTerrainName(name);
//				}
				List<SectorEntityToken> all = constellation.allEntitiesAdded.get(curr.entity);
				for (SectorEntityToken entity : all) {
					entity.setName(name);
					if (entity.getMarket() != null) entity.getMarket().setName(name);
					updateJumpPointNameFor(entity);
					if (entity instanceof CampaignTerrainAPI) {
						((CampaignTerrainAPI) entity).getPlugin().setTerrainName(name);
					}
				}
			}
		}
		
		int i = 0;
		for (NamingTreeNode node : curr.children) {
			assignSNamesHelper(node, name, i);
			
			if (node.entity instanceof PlanetAPI) {
				PlanetAPI planet = (PlanetAPI) node.entity;
				if (!planet.isStar()) {
					i++;
				}
			}
		}
	}
	
	public void updateJumpPointNameFor(SectorEntityToken entity) {
		if (!(entity instanceof PlanetAPI)) return;
		if (!(entity.getContainingLocation() instanceof StarSystemAPI)) return;
		
		StarSystemAPI system = (StarSystemAPI) entity.getContainingLocation();
		
		for (JumpPointAPI point : system.getAutogeneratedJumpPointsInHyper()) {
			if (point.getDestinationVisualEntity() == entity) {
				PlanetAPI planet = (PlanetAPI) entity;
				String name = null;
				if (planet.isGasGiant()) {
					name = planet.getName() + " Gravity Well";
				} else if (planet.isStar()) {
					name = planet.getName() + ", " + planet.getSpec().getName();
				}
				if (name != null) {
					point.setName(name);
				}
				
//				for (JumpDestination dest : point.getDestinations()) {
//					dest.setLabelInInteractionDialog(system.getBaseName());
//				}
				
				break;
			}
		}
	}
	
	public void updateJumpPointDestinationNames(StarSystemAPI system) {
		for (JumpPointAPI point : system.getAutogeneratedJumpPointsInHyper()) {
			for (JumpDestination dest : point.getDestinations()) {
				//dest.setLabelInInteractionDialog(system.getBaseName());
//				String name = "the " + system.getName();
//				name = name.replaceAll("Star System", "star system");
//				name = name.replaceAll("Nebula", "nebula");
				String name = "the " + system.getNameWithLowercaseType();
				dest.setLabelInInteractionDialog(name);
			}
		}
	}
	
	
	
	
	public void computeNamingTree(StarSystemAPI system) {
		root = new NamingTreeNode(system);
		if (system.getStar() != null && system.getType() != StarSystemType.NEBULA) {
			root.entity = system.getStar();
		}
		
		for (PlanetAPI planet : system.getPlanets()) {
			if (!planet.isStar()) continue;
			if (planet == system.getStar()) continue;
			if (!constellation.allEntitiesAdded.containsKey(planet)) continue;
			
			NamingTreeNode node = new NamingTreeNode(system);
			node.entity = planet;
			node.parent = root;
			root.children.add(node);
		}

		addChildren(system, root);
		for (NamingTreeNode node : root.children) {
			addChildren(system, node);
		}
	}
	
	public void addChildren(StarSystemAPI system, NamingTreeNode curr) {
		OUTER: for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			if (planet == curr.entity) continue;
			if (!constellation.allEntitiesAdded.containsKey(planet)) continue;
			for (NamingTreeNode n : curr.children) {
				if (n.entity == planet) continue OUTER;
			}
			
			PlanetAPI lagrangeParent = constellation.lagrangeParentMap.get(planet);
			boolean mainStarAndOrbitingSystemCenter = curr.entity == curr.system.getStar() &&
													  planet.getOrbitFocus() == curr.system.getCenter() &&
													  lagrangeParent == null;
			
			if (mainStarAndOrbitingSystemCenter || 
					(planet.getOrbitFocus() == curr.entity && lagrangeParent == null) ||
					lagrangeParent == curr.entity) {
				NamingTreeNode node = new NamingTreeNode(system);
				node.entity = planet;
				node.parent = curr;
				curr.children.add(node);
				
				if (lagrangeParent != null && planet.getOrbitFocus() != null) {
					//float angle1 = planet.getOrbitFocus().getCircularOrbitAngle();
					float angle1 = lagrangeParent.getCircularOrbitAngle();
					float angle2 = planet.getCircularOrbitAngle();
					
					if (Misc.getClosestTurnDirection(angle1, angle2) < 0) {
						node.lagrangePointType = LagrangePointType.L4;
					} else {
						node.lagrangePointType = LagrangePointType.L5;
					}
				}
				
				addChildren(system, node);
			}
		}
		
		OUTER: for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
			if (terrain == curr.entity) continue;
			if (!constellation.allEntitiesAdded.containsKey(terrain)) continue;
			for (NamingTreeNode n : curr.children) {
				if (n.entity == terrain) continue OUTER;
			}
			
			PlanetAPI lagrangeParent = constellation.lagrangeParentMap.get(terrain);
			boolean mainStarAndOrbitingSystemCenter = curr.entity == curr.system.getStar() &&
			  										  terrain.getOrbitFocus() == curr.system.getCenter() &&
			  										  lagrangeParent == null;
			if (mainStarAndOrbitingSystemCenter || 
					(terrain.getOrbitFocus() == curr.entity && lagrangeParent == null) ||
					lagrangeParent == curr.entity) {
				NamingTreeNode node = new NamingTreeNode(system);
				node.entity = terrain;
				node.parent = curr;
				curr.children.add(node);
				
				if (lagrangeParent != null && terrain.getOrbitFocus() != null) {
					//float angle1 = terrain.getOrbitFocus().getCircularOrbitAngle();
					float angle1 = lagrangeParent.getCircularOrbitAngle();
					float angle2 = terrain.getCircularOrbitAngle();
					
					if (Misc.getClosestTurnDirection(angle1, angle2) < 0) {
						node.lagrangePointType = LagrangePointType.L4;
					} else {
						node.lagrangePointType = LagrangePointType.L5;
					}
				}
				
				addChildren(system, node);
			}
		}
		
		Collections.sort(curr.children, new Comparator<NamingTreeNode>() {
			public int compare(NamingTreeNode o1, NamingTreeNode o2) {
				Vector2f from = new Vector2f();
				if (o1.parent != null && o1.parent.entity != null) {
					from = o1.parent.entity.getLocation();
					if (o1.parent == o1.system.getStar()) {
						from = o1.system.getCenter().getLocation();
					}
				}
				float d1 = Misc.getDistance(from, o1.entity.getLocation());
				float d2 = Misc.getDistance(from, o2.entity.getLocation());
				return (int) Math.signum(d1 - d2);
			}
		});
	}
	
	
	//public static boolean isNameSpecial(String name, StarSystemAPI system) {			
	public static boolean isNameSpecial(StarSystemAPI system) {			
		if (system.getConstellation() == null) return true;
		
		String name = system.getBaseName();
		
		NamePick constellationName = system.getConstellation().getNamePick();
		String base = constellationName.nameWithRomanSuffixIfAny;
		if (constellationName.secondaryWithRomanSuffixIfAny != null) {
			base = constellationName.secondaryWithRomanSuffixIfAny;
		}
		if (name.toLowerCase().contains(base.toLowerCase())) {
			return false;
		}
		return true;
	}
	
	
	public static void assignSpecialNames(StarSystemAPI system) {
		Constellation actual = system.getConstellation();
		if (actual == null || actual.getNamePick() == null) return;
		
//		if (system.getName().toLowerCase().contains("alpha cirrus")) {
//			System.out.println("fwefwefwefe");
//		}
		
		Constellation c = new Constellation(ConstellationType.NORMAL, actual.getAge());
		//c.setNamePick(actual.getNamePick());
		c.getSystems().add(system);
		c.setLagrangeParentMap(actual.getLagrangeParentMap());
		c.setAllEntitiesAdded(actual.getAllEntitiesAdded());
		c.setLeavePickedNameUnused(true);
		
		NameAssigner namer = new NameAssigner(c);
		namer.setSpecialNamesProbability(1f);
		
		//namer.setRenameSystem(false);
		//namer.setStructuralNameOffset(nameOffset);
		namer.assignNames(actual.getNamePick().spec.getName(), actual.getNamePick().spec.getSecondary());
	}
}








