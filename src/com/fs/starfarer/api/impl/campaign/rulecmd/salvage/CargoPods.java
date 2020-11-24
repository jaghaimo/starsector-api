package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class CargoPods extends BaseCommandPlugin {
	
	public static final String TRAPPED = "$trapped";
	public static final String LOCKED = "$locked";
	public static final String CAN_UNLOCK = "$canUnlock";
	
	
	public static final float BREAK_KEEP_FRACTION = 0.5f;
	
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected CargoAPI podsCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected CargoPodsEntityPlugin plugin;
	protected Map<String, MemoryAPI> memoryMap;

	
	protected boolean isLocked() {
		return memory.getBoolean(LOCKED);
	}
	protected boolean isTrapped() {
		return memory.getBoolean(TRAPPED);
	}
	protected boolean canUnlock() {
		return memory.getBoolean(CAN_UNLOCK);
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		podsCargo = entity.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		plugin = (CargoPodsEntityPlugin) entity.getCustomPlugin();
		
		if (command.equals("printDesc")) {
			showDescription();
		}
		else if (command.equals("openCargo")) {
			openCargo();
		}
		else if (command.equals("breakLocks")) {
			breakLocks();
		}
		else if (command.equals("destroy")) {
			destroy();
		}
		else if (command.equals("stabilize")) {
			stabilize();
		}
		else if (command.equals("computeStabilizeData")) {
			computeStabilizeData();
		}
		
		return true;
	}

	protected void computeStabilizeData() {
		float total = podsCargo.getTotalPersonnel() + podsCargo.getSpaceUsed() + podsCargo.getFuel();
		float stabilizeSupplies = Math.max((int) total / 20, 2);
		
		memory.set("$stabilizeSupplies", (int) stabilizeSupplies, 0f);
		
		float stabilizeDays = 150;
		memory.set("$stabilizeDays", (int) stabilizeDays, 0f);
	}
	
	
	public static SectorEntityToken findOrbitFocus(SectorEntityToken entity) {
		if (entity.isInHyperspace()) return null;
		
		SectorEntityToken target = null;
		float minDist = Float.MAX_VALUE;
		List<SectorEntityToken> potential = new ArrayList<SectorEntityToken>(entity.getContainingLocation().getPlanets());
		SectorEntityToken center = ((StarSystemAPI)entity.getContainingLocation()).getCenter();
		potential.add(center);
		potential.addAll(entity.getContainingLocation().getJumpPoints());
		for (SectorEntityToken curr : potential) {
			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
			dist -= curr.getRadius();
			
			float maxDist = 400f;
			if ((curr instanceof PlanetAPI && ((PlanetAPI)curr).isStar()) || curr == center) {
				maxDist = 100000000000f;
			}
			
			if (dist < maxDist && dist < minDist) {
				target = curr;
				minDist = dist;
			}
		}
		return target;
	}

	public static void stabilizeOrbit(SectorEntityToken entity, boolean makeDiscovered) {
		SectorEntityToken focus = findOrbitFocus(entity);
		if (focus != null) {
			float radius = Misc.getDistance(focus.getLocation(), entity.getLocation());
			float orbitDays = radius / (5f + StarSystemGenerator.random.nextFloat() * 20f);
			float angle = Misc.getAngleInDegreesStrict(focus.getLocation(), entity.getLocation());
			entity.setCircularOrbit(focus, angle, radius, orbitDays);
		} else {
			entity.getVelocity().set(0, 0);
		}
		
		if (makeDiscovered) {
			entity.setDiscoverable(null);
			entity.setSensorProfile(null);
		}
		
		entity.getMemoryWithoutUpdate().set("$stabilized", true);
	}
	
	protected void stabilize() {
		stabilizeOrbit(entity, true);
		
		float stabilizeSupplies = memory.getFloat("$stabilizeSupplies");
		float stabilizeDays = memory.getFloat("$stabilizeDays");
		
		playerCargo.removeSupplies(stabilizeSupplies);
		
		plugin.setElapsed(0);
		plugin.setExtraDays(stabilizeDays);
		plugin.updateBaseMaxDays();
		
		AddRemoveCommodity.updatePlayerMemoryQuantity(Commodities.SUPPLIES);
		AddRemoveCommodity.addCommodityLossText(Commodities.SUPPLIES, (int) stabilizeSupplies, text);
		
		text.addParagraph("Your crew busy themselves attaching micro-thrusters and performing " +
						  "the requisite calculations to make sure the pods remain in a stable orbit.");
		float daysLeft = plugin.getDaysLeft();
		String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
		text.addParagraph("When the work is done, your systems estimate they'll be able to predict the location of the pods for " + atLeastTime + ".");
		
		memory.set("$stabilized", true);
		memory.set("$daysLeft", (int) daysLeft, 0f);
	}
	
	protected void destabilize() {
		if (!memory.getBoolean("$stabilized")) return;
		
		entity.setOrbit(null);
		
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		vel.scale(5f + 10f * (float) Math.random());
		entity.getVelocity().set(vel);
		
		entity.setDiscoverable(null);
		entity.setSensorProfile(1f);
		
		plugin.setElapsed(0);
		plugin.setExtraDays(0);
		plugin.updateBaseMaxDays();
		
		float daysLeft = plugin.getDaysLeft();
		String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
		text.addParagraph("Working with the cargo has destablized the orbit, but the pods should still be trackable for " + atLeastTime + ".");
		
		memory.unset("$stabilized");
	}
	
	
	protected void breakLocks() {
		pruneCargo(BREAK_KEEP_FRACTION);
		openCargo();
	}
	
	
	protected void pruneCargo(float fractionKeep) {
		CargoAPI keep = Global.getFactory().createCargo(true);
		
		Random random = new Random();
		if (memory.contains(MemFlags.SALVAGE_SEED)) {
			long seed = memory.getLong(MemFlags.SALVAGE_SEED);
			random = new Random(seed);
		}
		
		for (CargoStackAPI stack : podsCargo.getStacksCopy()) {
			int qty = (int) stack.getSize();
			for (int i = 0; i < qty; i++) {
				if (random.nextFloat() < fractionKeep) {
					keep.addItems(stack.getType(), stack.getData(), 1);
				}
			}
		}
		podsCargo.clear();
		podsCargo.addAll(keep);
	}
	
	
	protected void destroy() {
		podsCargo.clear();
		Misc.fadeAndExpire(entity, 1f);
	}

	protected void showDescription() {
		float daysLeft = plugin.getDaysLeft();
		memory.set("$daysLeft", (int) daysLeft, 0f);
		
		boolean stabilized = memory.getBoolean("$stabilized");
		
		if (daysLeft >= 5000) {
			text.addParagraph("The cargo pods are in a stable orbit and are unlikely to drift apart any time soon.");
		} else {
			String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
			if (stabilized && daysLeft > 20) {
				text.addParagraph("The cargo pods are in a stabilized orbit, and your systems should be able to keep track of them for " + atLeastTime + ".");
			} else {
				text.addParagraph("The cargo pods are in an unstable orbit, but should not drift apart and be lost for " + atLeastTime + ".");
			}
		}
		
		if (podsCargo.getTotalPersonnel() > 0) {
			String crewText = "Sensor readings are indicative of the presence of active life support and cryosleep equipment.";
			text.addParagraph(crewText);
		}
		
		if (isLocked()) {
			if (canUnlock()) {
				text.addParagraph("The pods are locked, but you are in posession of the keycode.");
			} else {
				text.addParagraph("The pods are locked, and you don't have the keycode. " +
						"It's possible to force the locks, but this carries a risk to the cargo. " +
						"Some pods are also fitted with a self-destruct mechanism, " +
						"ensuring the total loss of all cargo in the event of a breach attempt.");
			}
		} else {
			//text.addParagraph("The pods are not locked.");
		}
		
	}

	public void openCargo() {
		final CargoAPI preOpen = Global.getFactory().createCargo(true);
		preOpen.addAll(podsCargo);
		
		dialog.getVisualPanel().showLoot("Cargo Pods", podsCargo, true, false, false, new CoreInteractionListener() {
			public void coreUIDismissed() {
				plugin.updateBaseMaxDays();
				
				if (podsCargo.isEmpty()) {
					Misc.fadeAndExpire(entity, 1f);
					
					dialog.dismiss();
					dialog.hideTextPanel();
					dialog.hideVisualPanel();
				} else {
					if (!Misc.isSameCargo(preOpen, podsCargo)) {
						destabilize();
					}
				}
				dialog.setPromptText("You decide to...");
				FireAll.fire(null, dialog, memoryMap, "CargoPodsOptions");
				FireAll.fire(null, dialog, memoryMap, "CargoPodsOptionsUpdate");
			}
		});
		options.clearOptions();
		dialog.setPromptText("");
	}
	
}















