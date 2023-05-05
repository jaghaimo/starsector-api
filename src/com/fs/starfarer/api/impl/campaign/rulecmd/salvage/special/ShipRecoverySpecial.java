package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialDialogPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShipRecoverySpecial extends BaseSalvageSpecial {

	public static final String RECOVER = "recover";
	public static final String NOT_NOW = "not_now";
	public static final String RECOVERY_FINISHED = "finished";
	public static final String ABORT_CONTINUE = "abort_continue";
	

	public static enum ShipCondition {
		PRISTINE,
		GOOD,
		AVERAGE,
		BATTERED,
		WRECKED,
	}
	
	public static class PerShipData implements Cloneable {
		public ShipCondition condition = ShipCondition.AVERAGE;
		public String variantId = null;
		public ShipVariantAPI variant = null;
		public String shipName = null;
		public String fleetMemberId = null;
		public boolean addDmods = true;
		public boolean pruneWeapons = true;
		public Boolean nameAlwaysKnown = null;
		public float sModProb = 0f;
		public PerShipData(String variantId, ShipCondition condition) {
			this(variantId, condition, 0f);
		}
		public PerShipData(String variantId, ShipCondition condition, float sModProb) {
			this(variantId, condition, Factions.INDEPENDENT, sModProb);
		}
		public PerShipData(ShipVariantAPI variant, ShipCondition condition, String shipName, String factionIdForShipName, float sModProb) {
			this.variant = variant;
			this.condition = condition;
			
			if (shipName != null) {
				this.shipName = shipName;
			} else {
				FactionAPI faction = Global.getSector().getFaction(factionIdForShipName);
				this.shipName = faction.pickRandomShipName();
			}
			
			this.sModProb = sModProb;
		}
		
		public PerShipData(String variantId, ShipCondition condition, String factionIdForShipName, float sModProb) {
			this.variantId = variantId;
			this.condition = condition;
			
			FactionAPI faction = Global.getSector().getFaction(factionIdForShipName);
			this.shipName = faction.pickRandomShipName();
			
			this.sModProb = sModProb;
		}
		
		public ShipVariantAPI getVariant() {
			ShipVariantAPI result = variant;
			if (result == null && variantId != null) {
				result = Global.getSettings().getVariant(variantId);
			}
			return result;
		}
		
		@Override
		public PerShipData clone() {
			try { return (PerShipData) super.clone(); } catch (CloneNotSupportedException e) { return null; }
		}
		
	}
	
	public static class ShipRecoverySpecialData implements SalvageSpecialData {
		public List<PerShipData> ships = new ArrayList<PerShipData>();
		public String desc = null;
		public Boolean storyPointRecovery = null;
		public Boolean notNowOptionExits = null;
		public Boolean noDescriptionText = null;
		
		public ShipRecoverySpecialData(String desc) {
			this.desc = desc;
		}
		
		public void addShip(PerShipData ship) {
			ships.add(ship);
		}
		public void addShip(String variantId, ShipCondition condition, float sModProb) {
			ships.add(new PerShipData(variantId, condition, sModProb));
		}
		public void addShip(String variantId, ShipCondition condition, String factionIdForShipName, float sModProb) {
			ships.add(new PerShipData(variantId, condition, factionIdForShipName, sModProb));
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new ShipRecoverySpecial();
		}
	}
	
	protected ShipRecoverySpecialData data;
	
	public static ShipRecoverySpecialData getSpecialData(SectorEntityToken entity, String desc, boolean create, boolean replace) {
		Object o = Misc.getSalvageSpecial(entity);
		ShipRecoverySpecialData data = null;
		if (o instanceof ShipRecoverySpecialData) {
			data = (ShipRecoverySpecialData) o;
		}
		
		if (data == null && !create) return null;
		if (o != null && data == null && !replace) return null;
		
		if (data == null) {
			data = new ShipRecoverySpecialData(desc);
			Misc.setSalvageSpecial(entity, data);
		}
		
		return data;
	}
	
	public ShipRecoverySpecial() {
	}
	

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (ShipRecoverySpecialData) specialData;
		
//		int max = Global.getSettings().getMaxShipsInFleet() - Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size();
//		while (data.ships.size() > max && !data.ships.isEmpty()) {
//			data.ships.remove(0);
//		}
		
		if (data.ships.isEmpty()) {
			initNothing();
		} else {
			init();
		}
	}

	protected List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
	protected List<FleetMemberAPI> recovered = new ArrayList<FleetMemberAPI>();
	protected void init() {
		members.clear();
		
		for (PerShipData curr : data.ships) {
			addMember(curr);
		}
		
		if (members.isEmpty()) {
			initNothing();
			return;
		}
		
		CampaignFleetAPI recoverable = Global.getFactory().createEmptyFleet(Factions.NEUTRAL, FleetTypes.PATROL_SMALL, true);
		for (FleetMemberAPI member : members) {
			recoverable.getFleetData().addFleetMember(member);
		}
		
		if (recoverable.getFleetData().getMembersListCopy().size() == 1) {
			visual.showFleetMemberInfo(recoverable.getFleetData().getMembersListCopy().get(0), true);
		} else {
			visual.showFleetInfo("Your fleet", playerFleet, "Recoverable ships", recoverable, null, true);
		}
		
		addInitialText();
		
		if (isStoryPointRecovery()) {
			addStoryOptions();
		} else {
			options.clearOptions();
			options.addOption("Consider ship recovery", RECOVER);
			
			if (data.notNowOptionExits != null && data.notNowOptionExits) {
				options.addOption("Leave", NOT_NOW);
				options.setShortcut(NOT_NOW, Keyboard.KEY_ESCAPE, false, false, false, true);
			} else {
				options.addOption("Not now", NOT_NOW);
			}
		}
	}
	
	
	protected void addStoryOptions() {
		options.clearOptions();
		options.addOption("Take a look the chief engineer's report and make a decision", RECOVER);
		options.addOption("\"I'll need to consider my options.\"", NOT_NOW);
		dialog.setOptionColor(RECOVER, Misc.getStoryOptionColor());
	}
	
	protected boolean isStoryPointRecovery() {
		return data != null && data.storyPointRecovery != null && data.storyPointRecovery;
	}
	
	
	protected void addInitialText() {
		
		if (data.noDescriptionText != null && data.noDescriptionText) {
			return;
		}
		
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		boolean wreck = Entities.WRECK.equals(entity.getCustomEntityType());
		wreck |= entity.hasTag(Tags.WRECK);
		//boolean dock = Entities.WRECK.equals(entity.getCustomEntityType());
		
		boolean withDesc = !debris && !wreck;

		if (isStoryPointRecovery()) {
			addText("Some time later, you hear back from your chief engineer, " +
					"who mumbles about being a miracle worker to someone offscreen before " +
			"noticing you've picked up the call.");
			if (members.size() == 1) {
				addText("\"Commander, looks like we might be able to pull this off, though I'll say you're not going to " +
						"find what I'm going to do in any manual. And it wouldn't pass any inspection, but then again " +
						"we're not in the Hegemony fleet. It'll fly, though.");
			} else {
				addText("\"Commander, looks like we might be able to pull this off, though I'll say you're not going to " +
						"find what I'm going to do in any manual. And it wouldn't pass any inspection, but then again " +
						"we're not in the Hegemony fleet. These ships can be made to fly again, though.");
			}
			return;
		}
		
//		
//
//		
		
		String ships = "several ships";
		String they = "they";
		if (members.size() == 1) {
			ships = "a ship";
			they = "it";
		}
		
		if (wreck) {
			if (data.storyPointRecovery != null && data.storyPointRecovery) {
				addText("A crack engineering team sent to the wreck reports successfully preparing it " +
						"for recovery.");
			} else {
				if (!FireBest.fire(null, dialog, memoryMap, "ShipRecoveryCustomText")) {
					addText("Salvage crews boarding the wreck discover that many essential systems " +
							"are undamaged and the ship could be restored to basic functionality.");
					
					ExtraSalvage es = BaseSalvageSpecial.getExtraSalvage(entity);
					if (es != null && !es.cargo.isEmpty()) {
						addText("There are also indications that it has some sort of cargo on board.");
					}
				}
			}
		} else if (debris) {
			addText("Close-range scans of the debris field reveal " + ships + 
					" that could be restored to basic functionality.");
		} else if (withDesc) {
			String desc = data.desc;
			if (desc == null) desc = "floating near";
			//desc = "docked with";
			
			if (entity instanceof PlanetAPI) {
				addText("Salvage crews report " + ships + " " + desc + ". " +
						"Closer inspection reveals " + they + " could be restored to basic functionality.");
			} else {
				addText("Salvage crews report " + ships + " " + desc + " the $shortName. " +
						"Closer inspection reveals " + they + " could be restored to basic functionality.");
			}
		}
		
		if (members.size() == 1) {
			if (members.size() > 0 && Misc.getCurrSpecialMods(members.get(0).getVariant()) > 0) {
				text.addPara("The crew chief also reports that the hull has undergone %s, which appear to have " +
						"survived its present state.", 
						Misc.getStoryOptionColor(), "special modifications");
			}
//			if (wreck) {
//				addText("");
//			} else {
				addText("If not recovered, the ship will be scuttled, " +
						"and any fitted weapons and fighter LPCs will be retrieved.");
//			}
		} else {
			addText("Any ships that aren't recovered will be scuttled, " +
				"and any fitted weapons and fighter LPCs will be retrieved.");
		}
	}
		
	protected FleetMemberAPI first = null;
	protected void addMember(PerShipData shipData) {
		if (shipData.variant == null && shipData.variantId == null) {
			return;
		}
		
		FleetMemberAPI member = null;
		if (shipData.variantId != null) {
			member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.variantId);
		} else {
			member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.variant);
		}
		
		//if (member.getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)) {
		if (Misc.isUnboardable(member)) {
			return;
		}
		
		members.add(member);
		
		if (first == null) first = member;
		
		member.setOwner(1);
		if (shipData.fleetMemberId != null) {
			member.setId(shipData.fleetMemberId);
		}
		
		if (isNameKnown(shipData.condition) || (shipData.nameAlwaysKnown != null && shipData.nameAlwaysKnown)) {
			member.setShipName(shipData.shipName);
		} else {
			member.setShipName("<name unknown>");
		}
		
		prepareMember(member, shipData);
	}
	
	public static boolean isNameKnown(ShipCondition condition) {
		return condition == ShipCondition.PRISTINE || condition == ShipCondition.GOOD;
	}
	
	public void prepareMember(FleetMemberAPI member, PerShipData shipData) {
		
		int hits = getHitsForCondition(member, shipData.condition);
		int dmods = getDmodsForCondition(shipData.condition);
		
		int reduction = (int) playerFleet.getStats().getDynamic().getValue(Stats.SHIP_DMOD_REDUCTION, 0);
		reduction = random.nextInt(reduction + 1);
		dmods -= reduction;
		
		
		member.getStatus().setRandom(random);
		
		for (int i = 0; i < hits; i++) {
			member.getStatus().applyDamage(1000000f);
		}

		member.getStatus().setHullFraction(getHullForCondition(shipData.condition));
		member.getRepairTracker().setCR(0f);
		
		
		ShipVariantAPI variant = member.getVariant();
		variant = variant.clone();
		variant.setOriginalVariant(null);
		
		int dModsAlready = DModManager.getNumDMods(variant);
		dmods = Math.max(0, dmods - dModsAlready);
		
		if (dmods > 0 && shipData.addDmods) {
			DModManager.setDHull(variant);
		}
		member.setVariant(variant, false, true);
		
		if (dmods > 0 && shipData.addDmods) {
			DModManager.addDMods(member, true, dmods, random);
		}
		
		if (shipData.sModProb > 0 && random.nextFloat() < shipData.sModProb) {
			int num = 1;
			float r = random.nextFloat();
			if (r > 0.85f) {
				num = 3;
			} else if (r > 0.5f) {
				num = 2;
			}
			
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			for (String id : variant.getHullMods()) {
				HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
				if (spec.isHidden()) continue;
				if (spec.isHiddenEverywhere()) continue;
				if (spec.hasTag(Tags.HULLMOD_DMOD)) continue;
				if (spec.hasTag(Tags.HULLMOD_NO_BUILD_IN)) continue;
				if (variant.getPermaMods().contains(spec.getId())) continue;
				picker.add(id, spec.getCapitalCost());
			}
			for (int i = 0; i < num && !picker.isEmpty(); i++) {
				String id = picker.pickAndRemove();
				variant.addPermaMod(id, true);
				//variant.getPermaMods().add(id);
			}
		}
		
		
		if (shipData.pruneWeapons) {
			float retain = getFighterWeaponRetainProb(shipData.condition);
			FleetEncounterContext.prepareShipForRecovery(member, false, false, false, retain, retain, random);
			member.getVariant().autoGenerateWeaponGroups();
		}
	}
	

	protected float getHullForCondition(ShipCondition condition) {
		switch (condition) {
		case PRISTINE: return 1f;
		case GOOD: return 0.6f + random.nextFloat() * 0.2f;
		case AVERAGE: return 0.4f + random.nextFloat() * 0.2f;
		case BATTERED: return 0.2f + random.nextFloat() * 0.2f;
		case WRECKED: return random.nextFloat() * 0.1f;
		}
		return 1;
	}
	
	
	protected int getDmodsForCondition(ShipCondition condition) {
		if (condition == ShipCondition.PRISTINE) return 0;
		
		switch (condition) {
		case GOOD: return 1;
		case AVERAGE: return 1 + random.nextInt(2);
		case BATTERED: return 2 + random.nextInt(2);
		case WRECKED: return 3 + random.nextInt(2);
		}
		return 1;
	}
	
	protected float getFighterWeaponRetainProb(ShipCondition condition) {
		switch (condition) {
		case PRISTINE: return 1f;
		case GOOD: return 0.67f;
		case AVERAGE: return 0.5f;
		case BATTERED: return 0.33f;
		case WRECKED: return 0.2f;
		}
		return 0f;
	}
	
	protected int getHitsForCondition(FleetMemberAPI member, ShipCondition condition) {
		if (condition == ShipCondition.PRISTINE) return 0;
		if (condition == ShipCondition.WRECKED) return 20;
		
		switch (member.getHullSpec().getHullSize()) {
		case CAPITAL_SHIP:
			switch (condition) {
			case GOOD: return 2 + random.nextInt(2);
			case AVERAGE: return 4 + random.nextInt(3);
			case BATTERED: return 7 + random.nextInt(6);
			}
			break;
		case CRUISER:
			switch (condition) {
			case GOOD: return 1 + random.nextInt(2);
			case AVERAGE: return 2 + random.nextInt(3);
			case BATTERED: return 4 + random.nextInt(4);
			}
			break;
		case DESTROYER:
			switch (condition) {
			case GOOD: return 1 + random.nextInt(2);
			case AVERAGE: return 2 + random.nextInt(2);
			case BATTERED: return 3 + random.nextInt(3);
			}
			break;
		case FRIGATE:
			switch (condition) {
			case GOOD: return 1;
			case AVERAGE: return 2;
			case BATTERED: return 3;
			}
			break;
		}
		return 1;
	}
	
	
//	public void doRecovery() {
//		for (FleetMemberAPI member : new ArrayList<FleetMemberAPI>(members)) {
//			int index = members.indexOf(member);
//			if (index >= 0) {
//				PerShipData shipData = data.ships.get(index);
//				data.ships.remove(index);
//				members.remove(index);
//				
//				
//				member.setShipName(shipData.shipName);
//				
//				float minHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f);
//				float maxHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f);
//				float minCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f);
//				float maxCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f);
//				
//				float hull = (float) Math.random() * (maxHull - minHull) + minHull;
//				hull = Math.max(hull, member.getStatus().getHullFraction());
//				member.getStatus().setHullFraction(hull);
//				
//				float cr = (float) Math.random() * (maxCR - minCR) + minCR;
//				member.getRepairTracker().setCR(cr);
//				
//				playerFleet.getFleetData().addFleetMember(member);
//			}
//		}
//		optionSelected(null, RECOVERY_FINISHED);
//	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if (RECOVER.equals(optionData)) {
			
			if (isStoryPointRecovery()) {
				addStoryOptions();
			} else {
				options.clearOptions();
				options.addOption("Consider ship recovery", RECOVER);
				if (data.notNowOptionExits != null && data.notNowOptionExits) {
					options.addOption("Leave", NOT_NOW);
					options.setShortcut(NOT_NOW, Keyboard.KEY_ESCAPE, false, false, false, true);
				} else {
					options.addOption("Not now", NOT_NOW);
				}
			}
			
			List<FleetMemberAPI> pool = members;
			List<FleetMemberAPI> storyPool = new ArrayList<FleetMemberAPI>();
			if (isStoryPointRecovery()) {
				pool = storyPool;
				storyPool = members;
			}
			
			dialog.showFleetMemberRecoveryDialog("Select ships to recover", pool, storyPool,
					new FleetMemberPickerListener() {
				public void pickedFleetMembers(List<FleetMemberAPI> selected) {
					if (selected.isEmpty()) return;
					
					new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
					
					for (FleetMemberAPI member : selected) {
						int index = members.indexOf(member);
						if (index >= 0) {
							PerShipData shipData = data.ships.get(index);
							data.ships.remove(index);
							members.remove(index);
							
							
							member.setShipName(shipData.shipName);
							if (shipData.fleetMemberId != null) {
								member.setId(shipData.fleetMemberId);
							}
							
							float minHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f);
							float maxHull = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f);
							float minCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f);
							float maxCR = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f);
							
							float hull = (float) Math.random() * (maxHull - minHull) + minHull;
							hull = Math.max(hull, member.getStatus().getHullFraction());
							member.getStatus().setHullFraction(hull);
							
							float cr = (float) Math.random() * (maxCR - minCR) + minCR;
							member.getRepairTracker().setCR(cr);
							
							playerFleet.getFleetData().addFleetMember(member);
							recovered.add(member);
						}
					}
					
//					setExtraSalvageFromUnrecoveredShips();
//					setDone(true);
//					setShowAgain(!data.ships.isEmpty());
					if (dialog.getPlugin() instanceof SalvageSpecialDialogPlugin) {
						SalvageSpecialDialogPlugin plugin = (SalvageSpecialDialogPlugin) dialog.getPlugin();
						plugin.optionSelected(null, RECOVERY_FINISHED);
					} else {
						// bad state, exit dialog
						// apparently possible? maybe mods involved
						// http://fractalsoftworks.com/forum/index.php?topic=12492.0
						dialog.dismiss();
					}
				}
				public void cancelledFleetMemberPicking() {
				}
			});
			
		} else if (NOT_NOW.equals(optionData)) {
			if (data.notNowOptionExits != null && data.notNowOptionExits) {
//				setDone(true);
//				setShowAgain(true);
//				setEndWithContinue(false);
				dialog.dismiss();
			} else {
				if (isStoryPointRecovery()) {
					//Misc.setSalvageSpecial(entity, null);
					Misc.setSalvageSpecial(entity, Misc.getPrevSalvageSpecial(entity));
				}
				
				new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
				
				// only get extra salvage when it's not story-point recovery
				// since unless the story point is spent and the ship is recovered
				// it's not in a "recoverable" state an should grant no bonus stuff when salvaged
				if (!isStoryPointRecovery()) {
					addExtraSalvageFromUnrecoveredShips();
				}
				setDone(true);
				setEndWithContinue(false);
				setShowAgain(true);
			}
		} else if (RECOVERY_FINISHED.equals(optionData)) {
			new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
			
			boolean wreck = Entities.WRECK.equals(entity.getCustomEntityType());
			wreck |= entity.hasTag(Tags.WRECK);
			
			if (wreck) {
				//ExtraSalvage es = BaseSalvageSpecial.getExtraSalvage(entity);
				//if (es != null && !es.cargo.isEmpty()) {
				CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(entity);
				if (extra != null && !extra.isEmpty()) {
					addText("Your crews find some securely stowed cargo during the recovery operation.");
					
					extra.sort();
					playerFleet.getCargo().addAll(extra);
					
					
					for (CargoStackAPI stack : extra.getStacksCopy()) {
						AddRemoveCommodity.addStackGainText(stack, text);
					}
					clearExtraSalvage(entity);
					
					ListenerUtil.reportSpecialCargoGainedFromRecoveredDerelict(extra, dialog);
					//addText("The recovery operation is finished without any further surprises.");
				}
				
				addText("The " + first.getShipName() + " is now part of your fleet.");
				
				setShouldAbortSalvageAndRemoveEntity(true);
				options.clearOptions();
				options.addOption("Leave", ABORT_CONTINUE);
				options.setShortcut(ABORT_CONTINUE, Keyboard.KEY_ESCAPE, false, false, false, true);
				
				ListenerUtil.reportShipsRecovered(recovered, dialog);
				
				for (FleetMemberAPI member : recovered) {
					dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$srs_memberId", member.getId(), 0);
					dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$srs_hullId", member.getHullId(), 0);
					dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$srs_baseHullId", member.getHullSpec().getBaseHullId(), 0);
					FireAll.fire(null, dialog, memoryMap, "PostShipRecoverySpecial");
				}
				
			} else {
				addExtraSalvageFromUnrecoveredShips();
				setEndWithContinue(false);
				setDone(true);
				setShowAgain(!data.ships.isEmpty());
				
//				setExtraSalvageFromUnrecoveredShips();
//				setDone(true);
//				setEndWithContinue(false);
//				setShowAgain(true);
			}
		} else if (ABORT_CONTINUE.equals(optionData)) {
			setDone(true);
			setEndWithContinue(false);
		}
	}
	
	protected void addExtraSalvageFromUnrecoveredShips() {
		if (members.isEmpty()) return;
		
		CargoAPI extra = Global.getFactory().createCargo(true);
		for (FleetMemberAPI member : members) {
			addStuffFromMember(extra, member);
		}
		addTempExtraSalvage(extra);
	}
	
	protected void addStuffFromMember(CargoAPI cargo, FleetMemberAPI member) {
		cargo.addCommodity(Commodities.SUPPLIES, member.getRepairTracker().getSuppliesFromScuttling());
		cargo.addCommodity(Commodities.FUEL, member.getRepairTracker().getFuelFromScuttling());
		cargo.addCommodity(Commodities.HEAVY_MACHINERY, member.getRepairTracker().getHeavyMachineryFromScuttling());
		
		ShipVariantAPI variant = member.getVariant();
		for (String slotId : variant.getNonBuiltInWeaponSlots()) {
			cargo.addWeapons(variant.getWeaponId(slotId), 1);
		}
		
		int index = 0;
		for (String wingId : variant.getWings()) {
			if (wingId != null && !wingId.isEmpty() && !variant.getHullSpec().isBuiltInWing(index)) {
				cargo.addFighters(wingId, 1);
			}
			index++;
		}
	}

	
}





