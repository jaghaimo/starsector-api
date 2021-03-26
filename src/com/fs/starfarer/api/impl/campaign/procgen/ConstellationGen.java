package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Misc;

public class ConstellationGen {
	
	public static class SpringConnection {
		public float k;
		public float d;
		public boolean push = true;
		public boolean pull = true;
		public SpringNode from;
		public SpringNode to;
	}
	
	public static class SpringNode {
		public Object custom;
		public float radius;
		public Vector2f loc = new Vector2f();
		public Vector2f vel = new Vector2f();
		public Vector2f force = new Vector2f();
		public float mass;
		
		public boolean moveable = true;
		public List<SpringConnection> connections = new ArrayList<SpringConnection>(); 
	}
	
	public static class SpringSystem {
		public List<SpringNode> nodes = new ArrayList<SpringNode>();
		public List<SpringConnection> connections = new ArrayList<SpringConnection>();
		public int iter = 0;
		
		
		public SpringNode addNode(Object custom, float radius, float mass, float x, float y) {
			SpringNode node = new SpringNode();
			node.custom = custom;
			node.radius = radius;
			node.mass = mass;
			node.loc.x = x;
			node.loc.y = y;
			nodes.add(node);
			
			return node;
		}

		public boolean connExists(SpringNode from, SpringNode to) {
			for (SpringConnection conn : connections) {
				if (conn.from == from && conn.to == to) return true;
				if (conn.from == to && conn.to == from) return true;
			}
			return false;
		}
		
		
		public void addConnection(SpringNode from, SpringNode to, float k, float d, boolean push, boolean pull) {
			if (from == to) return;
			if (connExists(from, to)) return;
			
			SpringConnection conn = new SpringConnection();
			conn.from = from;
			conn.to = to;
			conn.k = k;
			conn.d = d;
			conn.push = push;
			conn.pull = pull;
			
			from.connections.add(conn);
			to.connections.add(conn);
			
			connections.add(conn);
		}
		
		public void advance(float amount) {
			if (amount <= 0) return;
			iter++;
			
			for (SpringNode node : nodes) {
				node.force.set(0, 0);
			}
			
			for (SpringConnection conn : connections) {
				float dist = Misc.getDistance(conn.from.loc, conn.to.loc);
				if (!conn.push && dist < conn.d) continue;
				if (!conn.pull && dist > conn.d) continue;
				
				float diff = conn.d - dist;
				
				Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(conn.from.loc, conn.to.loc));
				
				float force = conn.k * diff;
				
				conn.to.force.x += dir.x * force;
				conn.to.force.y += dir.y * force;
				conn.from.force.x -= dir.x * force;
				conn.from.force.y -= dir.y * force;
			}
			
			float cof = 500f;
			for (SpringNode node : nodes) {
				float friction = node.vel.length() * cof;
				float max = node.vel.length() * node.mass / amount;
				if (friction > max) friction = max;
				
				if (friction > 0) {
					Vector2f dir = new Vector2f(node.vel);
					dir.negate();
					dir.normalise();
					dir.scale(friction);
					node.force.x += dir.x;
					node.force.y += dir.y;
				}
				
//				if (node.force.length() < 10f && node.vel.length() > 2f) {
//					Vector2f dir = new Vector2f(node.vel);
//					dir.negate();
//					dir.normalise();
//					
//					dir.scale(node.vel.length() * node.mass / amount);
//					node.force.set(dir);
//				}
			}
			
			for (SpringNode node : nodes) {
				if (!node.moveable) continue;
				
				float ax = node.force.x / node.mass;
				float ay = node.force.y / node.mass;
				
				node.vel.x += ax * amount;
				node.vel.y += ay * amount;
				
				node.loc.x += node.vel.x * amount;
				node.loc.y += node.vel.y * amount;
			}
		}
		
		public boolean isDone() {
			for (SpringNode n1 : nodes) {
				for (SpringNode n2 : nodes) {
					if (n1 == n2) continue;
					float dist = Misc.getDistance(n1.loc, n2.loc);
					if (dist < n1.radius + n2.radius) {
						return false;
					}
				}
			}
			for (SpringConnection conn : connections) {
				if (!conn.pull) continue;
				
				float dist = Misc.getDistance(conn.from.loc, conn.to.loc);
				if (dist > conn.d + 1000f) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	
	public static class StarNode {
		public StarSystemAPI system;
		public Vector2f location;
		public StarNode(StarSystemAPI system, Vector2f location) {
			this.system = system;
			this.location = location;
		}
	}
	
//	public static void addSpringScript(final List<StarSystemAPI> systems) {
//		
//		final SpringSystem springs = createSpringSystem(systems, new Random());
//		
//		Global.getSector().addScript(new EveryFrameScript() {
//			public boolean runWhilePaused() {
//				return false;
//			}
//			public boolean isDone() {
//				boolean done = springs.isDone();
//				if (done) {
//					for (int i = 0; i < 100; i++) {
//						System.out.println("Done in " + springs.iter + " iterations");
//					}
//					for (SpringNode node : springs.nodes) {
//						StarSystemAPI system = (StarSystemAPI) node.custom;
//						system.getLocation().set(node.loc);
//					}
//					Global.getSector().setPaused(true);
//					Global.getSector().getHyperspace().updateAllOrbits();
//					return true;
//				}
//				return false;
//			}
//			public void advance(float amount) {
//				if (isDone()) return;
//				
//				for (int i = 0; i < 1; i++) {
//					springs.advance(0.25f);
//				}
//				
//				for (SpringNode node : springs.nodes) {
//					StarSystemAPI system = (StarSystemAPI) node.custom;
//					system.getLocation().set(node.loc);
//				}
//			}
//		});
//		
//	}
	
	public static SpringSystem createSpringSystem(List<StarSystemAPI> systems, Random random) {
		final SpringSystem springs = new SpringSystem();
		if (systems.isEmpty()) return springs;
		
		Vector2f loc = new Vector2f(0, 0);
		//loc = new Vector2f(systems.get(0).getLocation());
		boolean first = true;
		for (StarSystemAPI system : systems) {
			float radius = system.getMaxRadiusInHyperspace();
			if (radius < 500) radius = 500;
			
			float mass = radius;
			SpringNode node = springs.addNode(system, radius, mass,
									loc.x + random.nextFloat(), loc.y + random.nextFloat());
			if (first) {
				node.moveable = false;
				first = false;
			}
		}
		
		List<SpringNode> copy = new ArrayList<SpringNode>(springs.nodes);
		SpringNode curr = copy.get((int) (copy.size() * random.nextDouble()));
		copy.remove(curr);
		
		float pullK = 1000f;
		float pushK = 500f;
		// create constellation shape via branching spring thing
		while (!copy.isEmpty()) {
			int numBranches = random.nextInt(3) + 1;
			//numBranches = 3;
			
			for (int i = 0; i < numBranches; i++) {
				if (copy.isEmpty()) break;
				
				SpringNode other = copy.get((int) (copy.size() * random.nextDouble()));
				copy.remove(other);
				
				//float d = (curr.radius + other.radius) + 250f + random.nextFloat() * 1000f;
				float d = (curr.radius + other.radius) + StarSystemGenerator.MIN_STAR_DIST + 
										random.nextFloat() * (StarSystemGenerator.MAX_STAR_DIST - StarSystemGenerator.MIN_STAR_DIST);
				springs.addConnection(curr, other, pullK, d, true, true);
				//System.out.println("Dist: " + d);
				if (i == numBranches - 1) {
					curr = other;
				}
			}
		}
		
		// and add push-only connections for anything not connected by the main shape
		for (SpringNode n1 : springs.nodes) {
			for (SpringNode n2 : springs.nodes) {
				if (n1 == n2) continue;
				float d = (n1.radius + n2.radius) + 5000f;
				springs.addConnection(n1, n2, pushK, d, true, false);
			}
		}
		
		for (SpringNode node : springs.nodes) {
			StarSystemAPI system = (StarSystemAPI) node.custom;
			system.getLocation().set(node.loc);
		}
		return springs;
	}
	
	
	public static SpringSystem doConstellationLayout(List<StarSystemAPI> systems, Random random, Vector2f centerPoint) {
		SpringSystem springs = createSpringSystem(systems, random);
		
		for (int i = 0; i < 1000; i++) {
			springs.advance(0.25f);
			if (springs.isDone()) break;
		}
		
		if (!springs.isDone()) {
			springs = createSpringSystem(systems, random);
			for (int i = 0; i < 1000; i++) {
				springs.advance(0.25f);
				if (springs.isDone()) break;
			}
		}
		
		//if (!springs.isDone()) return springs;

		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		
		for (SpringNode node : springs.nodes) {
			float x = node.loc.x;
			float y = node.loc.y;
			if (x < minX) minX = x;
			if (x > maxX) maxX = x;
			if (y < minY) minY = y;
			if (y > maxY) maxY = y;
		}
		
		float midX = minX + (maxX - minX) * 0.5f;
		float midY = minY + (maxY - minY) * 0.5f;
		
		for (SpringNode node : springs.nodes) {
			node.loc.x = (int)(node.loc.x - midX + centerPoint.x);
			node.loc.y = (int)(node.loc.y - midY + centerPoint.y);
		}

		
		for (SpringNode node : springs.nodes) {
			StarSystemAPI system = (StarSystemAPI) node.custom;
			system.getLocation().set(node.loc);
		}
		
		return springs;
	}
	
	
	
	
}
















