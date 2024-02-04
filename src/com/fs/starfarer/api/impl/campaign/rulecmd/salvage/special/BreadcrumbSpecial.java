package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;

public class BreadcrumbSpecial extends BaseSalvageSpecial {

	
	public static class BreadcrumbSpecialData implements SalvageSpecialData {
		public String targetId = null;
		public String targetName = null;
		public BreadcrumbSpecialData(String targetId) {
			this.targetId = targetId;
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new BreadcrumbSpecial();
		}
	}
	
	private BreadcrumbSpecialData data;
	
	public BreadcrumbSpecial() {
	}
	

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (BreadcrumbSpecialData) specialData;
		
		initEntityLocation();
	}

	private void initEntityLocation() {
		if (data.targetId == null) {
			initNothing();
			return;
		}
		
		SectorEntityToken target = Global.getSector().getEntityById(data.targetId);
		if (target == null) { 
			initNothing();
			return;
		}
		
		// already discovered
		if (!target.hasSensorProfile() && !target.isDiscoverable()) {
			initNothing();
			return;
		}
		
		
		String targetName = getNameWithAOrAn(target, data.targetName, true, false);
		String targetNameUC = getNameWithAOrAn(target, data.targetName, false, true);
		//String entityName = getNameWithAOrAn(entity, null);
		String located = getLocatedString(target, true);
		
		String nameForTitle = targetNameUC.substring(targetNameUC.indexOf(" ") + 1);
		//if (target.getCu)
		String subject = getString("Location: " + nameForTitle);
		
		
		String text1 = "The $shortName's memory banks are partially accessible, and ";
		text1 += "contain information indicating that " + targetName + " is " + located + ".";
		
		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found a " +
							   "partially accessible memory bank " + 
							   "containing information that indicates " + targetName + " is " + located + ".";
		
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (debris) {
			text1 = "Your salvage crews find a functional memory bank in the debris. It contains information " +
					"indicating that " + targetName.toLowerCase() + " is " + located + ".";
		}
		
		
		if (target.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
			DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) target.getCustomPlugin();
			ShipVariantAPI variant = dsep.getData().ship.variant;
			if (variant == null && dsep.getData().ship.variantId != null) {
				variant = Global.getSettings().getVariant(dsep.getData().ship.variantId);
			}
			if (variant != null) {
				String size = null;
				if (variant.getHullSize() == HullSize.FRIGATE ||
						variant.getHullSize() == HullSize.DESTROYER) {
					size = "Based on the information, it's likely the ship is small, a frigate or a destroyer at the largest.";
				} else {
					size = "The vessel is likely to be at least cruiser-sized.";
				}
				
				if (size != null) {
					text1 += "\n\n" + size;
					text1ForIntel += "\n\n" + size;
				}
			}
		}
		
		addText(text1);
		
		BreadcrumbIntel intel = new BreadcrumbIntel(entity, target);
		intel.setTitle(getString(subject));
		intel.setText(getString(text1ForIntel));
		Global.getSector().getIntelManager().addIntel(intel, false, text);
		
//		CommMessageAPI message = FleetLog.beginEntry(subject, target);
//		message.getSection1().addPara(getString(text1));
//		FleetLog.addToLog(message, text);
		
		//unsetData();
		setDone(true);
		//setShowAgain(true);
	}
	
	public static String getNameWithAOrAn(SectorEntityToken target, String override, boolean lowercaseDebris, boolean forTitle) {
		String targetAOrAn = "a";
		String targetName = override;
		if (targetName == null) {
			if (target instanceof CustomCampaignEntityAPI) {
				CustomCampaignEntityAPI custom = (CustomCampaignEntityAPI) target;
				targetName = custom.getCustomEntitySpec().getNameInText();
				if (forTitle) targetName = custom.getName();
				targetAOrAn = custom.getCustomEntitySpec().getAOrAn();
			} else if (target instanceof PlanetAPI) {
				PlanetAPI planet = (PlanetAPI) target;
				targetName = planet.getTypeNameWithLowerCaseWorld().toLowerCase();
				if (forTitle) targetName = planet.getTypeNameWithWorld();
				targetAOrAn = planet.getSpec().getAOrAn();
			} else {
				targetName = target.getName();
			}
		}
		if (lowercaseDebris && target.hasTag(Tags.DEBRIS_FIELD)) {
			targetName = targetName.toLowerCase();
		}
		return targetAOrAn + " " + targetName;
	}
	
	public static String getLocatedString(SectorEntityToken target) {
		return getLocatedString(target, false);
	}
	public static String getLocatedString(SectorEntityToken target, boolean withSystem) {
		String loc = getLocationDescription(target, withSystem);
		
		String orbiting = "";
		boolean useTerrain = false;
		if (target.getOrbitFocus() != null) {
			if (target.getOrbitFocus() instanceof PlanetAPI) {
				PlanetAPI focus = (PlanetAPI) target.getOrbitFocus();
				boolean isPrimary = target.getContainingLocation() instanceof StarSystemAPI && 
									focus == ((StarSystemAPI)target.getContainingLocation()).getStar();
				if (!focus.isStar() || !isPrimary) {
					orbiting = "orbiting " + focus.getSpec().getAOrAn() + " " + focus.getTypeNameWithLowerCaseWorld().toLowerCase() + " in ";
				} else {
					float dist = Misc.getDistance(focus.getLocation(), target.getLocation());
					//float dist = Misc.getDistance(new Vector2f(), target.getLocation());
					if (dist < 3000) {
						orbiting = "located in the heart of ";
					} else if (dist > 12000) {
						orbiting = "located in the outer reaches of ";
					} else {
						//orbiting = "located in ";
						orbiting = "located some distance away from the center of ";
					}
					useTerrain = true;
				}
			} else if (target.getOrbitFocus() instanceof CustomCampaignEntityAPI) {
				CustomCampaignEntityAPI custom = (CustomCampaignEntityAPI) target.getOrbitFocus();
				orbiting = "orbiting " + custom.getCustomEntitySpec().getAOrAn() + " " + custom.getCustomEntitySpec().getNameInText() + " in ";
			} else if (target.getOrbitFocus() instanceof JumpPointAPI) {
				orbiting = "orbiting a jump-point in ";
			} else if (target.getOrbitFocus() instanceof CampaignTerrainAPI) {
				CampaignTerrainAPI t = (CampaignTerrainAPI) target.getOrbitFocus();
				String n = t.getPlugin().getNameForTooltip().toLowerCase();
				String a = Misc.getAOrAnFor(n);
				orbiting = "located inside " + a + " " + n + " ";
				
				float dist = Misc.getDistance(new Vector2f(), target.getLocation());
				if (dist < 3000) {
					orbiting += "in the heart of ";
				} else if (dist > 12000) {
					orbiting += "in the outer reaches of ";
				} else {
					orbiting += "some distance away from the center of ";
				}
			} else { // center of a binary/nebula/etc
				//float dist = Misc.getDistance(target.getOrbitFocus().getLocation(), target.getLocation());
				float dist = Misc.getDistance(new Vector2f(), target.getLocation());
				if (dist < 3000) {
					orbiting = "located in the heart of ";
				} else if (dist > 12000) {
					orbiting = "located in the outer reaches of ";
				} else {
					//orbiting = "located in ";
					orbiting = "located some distance away from the center of ";
				}
				useTerrain = true;
			}
		} else if (target.getContainingLocation() != null && target.getContainingLocation().isNebula()) {
			float dist = Misc.getDistance(new Vector2f(), target.getLocation());
			if (dist < 3000) {
				orbiting = "located in the heart of ";
			} else if (dist > 12000) {
				orbiting = "located on the outskirts of ";
			} else {
				//orbiting = "located in ";
				orbiting = "located some distance away from the center of ";
			}
			useTerrain = true;
		}
		
		if (useTerrain) {
			String terrainString = getTerrainString(target);
			if (terrainString != null) {
				orbiting = "located in " + terrainString + " in ";
			}
		}
		
		if (orbiting == null || orbiting.isEmpty()) orbiting = "located in ";
		return orbiting + loc;
	}
	
	public static String getTerrainString(SectorEntityToken entity) {
		if (!(entity.getContainingLocation() instanceof StarSystemAPI)) return null;
		
		StarSystemAPI system = (StarSystemAPI) entity.getContainingLocation();
		for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
			if (!terrain.getPlugin().containsEntity(entity)) continue;
			
			String type = terrain.getType();
			
			if (Terrain.ASTEROID_BELT.equals(type)) return "an asteroid belt";
			if (Terrain.ASTEROID_FIELD.equals(type)) return "an asteroid field";
			//if (Terrain.MAGNETIC_FIELD.equals(type)) return "a magnetic field";
			if (terrain.hasTag(Tags.ACCRETION_DISK)) return "an accretion disk";
			if (Terrain.RING.equals(type)) return "a ring system";
		}
		
		return null;
	}

	public static String getLocationDescription(SectorEntityToken entity, boolean withSystem) {
		LocationAPI loc = entity.getContainingLocation();
		if (loc == null) {
			return "an unknown location"; 
		}
		if (loc.isHyperspace()) {
			return "hyperspace";
		}
		StarSystemAPI system = (StarSystemAPI) loc;
		
//		if (system == Global.getSector().getCurrentLocation()) {
//			return "the " + system.getNameWithLowercaseType();
//		}

		//if (entity.getConstellation() != null && entity.getConstellation() != Global.getSector().getCurrentLocation().getConstellation()) {
		if (withSystem || entity.getConstellation() == null || entity.getConstellation().getSystems().size() == 1 ||
				entity.isInCurrentLocation()) {
			return "the " + system.getNameWithLowercaseType();
		}
		
		Constellation c = entity.getConstellation();
		String cText = "in the " + c.getNameWithLowercaseType();
		if (c.getSystems().size() == 1) {
			return "the " + system.getNameWithLowercaseType();
		}
		
		if (system.isNebula()) {
			return "a nebula " + cText;
		}
		
		if (system.getTertiary() != null) {
			return "a trinary star system " + cText;
		}
		
		if (system.getSecondary() != null) {
			return "a binary star system " + cText;
		}
		
		PlanetAPI star = system.getStar();
		if (star != null) {
			if (star.getSpec().isBlackHole()) {
				return "a black hole system " + cText;
			}
			
			//String sysText = star.getSpec().getAOrAn() + " " + star.getSpec().getName().toLowerCase() + " system ";
			
			String type = getStarTypeName(star);
			String color = getStarColorName(star);
			
			String sysText = null;
			CountingMap<String> counts = getTypeAndColorCounts(c);
			int cColor = counts.getCount(color);
			int cType = counts.getCount(type);
			if (cColor > 1 && cType > cColor) {
				sysText = "a system with " + star.getSpec().getAOrAn() + " " + color + " primary star ";
			} else if (cType > 0) {
				sysText = "a system with " + "a" + " " + type + " primary star ";
			} else if (cColor > 0) {
				sysText = "a system with " + star.getSpec().getAOrAn() + " " + color + " primary star ";
			}
			
			if (sysText != null) {
				return sysText + cText;
			}
		}
		
		//if (system.getType() == StarSystemType.SINGLE) {
		return "the " + system.getNameWithLowercaseType() + " " + cText;
			//return "orbit around " + getStarDescription(system.getStar()) + " " + cText;
	}
	
	public static CountingMap<String> getTypeAndColorCounts(Constellation c) {
		CountingMap<String> map = new CountingMap<String>();
		for (StarSystemAPI system : c.getSystems()) {
			PlanetAPI star = system.getStar();
			if (system.isNebula()) continue;
			if (system.getSecondary() != null) continue;
			if (system.getTertiary() != null) continue;
			
			String type = getStarTypeName(star);
			String color = getStarColorName(star);
			if (type != null) map.add(type);
			if (color != null) map.add(color);
		}
		return map;
	}
	
	public static String getStarTypeName(PlanetAPI star) {
		String name = star.getSpec().getName().toLowerCase();
		if (name.contains(" dwarf")) {
			return "dwarf";
		} else if (name.contains(" star")) {
			return null;
		} else if (name.contains(" giant")) {
			return "giant";
		} else if (name.contains(" supergiant")) {
			return "supergiant";
		}
		return null;
	}
	public static String getStarColorName(PlanetAPI star) {
		String name = star.getSpec().getName().toLowerCase();
		if (name.contains(" dwarf")) {
			name = name.replace(" dwarf", "");
		} else if (name.contains(" star")) {
			name = name.replace(" star", "");
		} else if (name.contains(" giant")) {
			name = name.replace(" giant", "");
		} else if (name.contains(" supergiant")) {
			name = name.replace(" supergiant", "");
		}
		if (!name.equals(star.getSpec().getName().toLowerCase())) {
			return name;
		}
		return null;
	}
	
	/*
	public static String getStarDescription(PlanetAPI star) {
		String type = star.getTypeId();
		
		if (type.equals(StarTypes.BLACK_HOLE)) return "a black hole";
		if (type.equals(StarTypes.NEUTRON_STAR)) return "a neutron star";
		
		if (type.equals(StarTypes.ORANGE) ||
			type.equals(StarTypes.ORANGE_GIANT)) {
			return "an orange star";
		}
		
		if (type.equals(StarTypes.RED_DWARF) ||
			type.equals(StarTypes.RED_SUPERGIANT) ||
				type.equals(StarTypes.RED_GIANT)) {
			return "a red star";
		}
		
		if (type.equals(StarTypes.BLUE_GIANT) ||
				type.equals(StarTypes.BLUE_SUPERGIANT)) {
			return "a blue star";
		}
		
		if (type.equals(StarTypes.BROWN_DWARF) ||
				type.equals(StarTypes.WHITE_DWARF)) {
			return "a dim star";
		}
		
		if (type.equals(StarTypes.YELLOW)) {
			return "a yellow star";
		}
		
		return "a star of unknown type";
	}
	*/
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
	}

	
}



