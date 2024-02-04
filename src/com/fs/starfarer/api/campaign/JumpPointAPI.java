package com.fs.starfarer.api.campaign;

import java.util.List;


public interface JumpPointAPI extends SectorEntityToken {
	
	public static class JumpDestination {
		private SectorEntityToken destination;
		private String labelInInteractionDialog;
		
		private float minDistFromToken = 0;
		private float maxDistFromToken = 0;
		
		public JumpDestination(SectorEntityToken destination, String labelInInteractionDialog) {
			this.destination = destination;
			this.labelInInteractionDialog = labelInInteractionDialog;
		}
		
		public SectorEntityToken getDestination() {
			return destination;
		}
		public void setDestination(SectorEntityToken destination) {
			this.destination = destination;
		}
		public String getLabelInInteractionDialog() {
			return labelInInteractionDialog;
		}
		public void setLabelInInteractionDialog(String labelInInteractionDialog) {
			this.labelInInteractionDialog = labelInInteractionDialog;
		}
		public float getMinDistFromToken() {
			return minDistFromToken;
		}
		public void setMinDistFromToken(float minDistFromToken) {
			this.minDistFromToken = minDistFromToken;
		}
		public float getMaxDistFromToken() {
			return maxDistFromToken;
		}
		public void setMaxDistFromToken(float maxDistFromToken) {
			this.maxDistFromToken = maxDistFromToken;
		}
	}
	
	void setRadius(float radius);
	
	void addDestination(JumpDestination destination);
	void clearDestinations();
	void removeDestination(SectorEntityToken destination);
	List<JumpDestination> getDestinations();
	
	
	/**
	 * Also automatically sets the jump point size.
	 * @param category in settings.json
	 * @param id under category in settings.json
	 * @param entity can be null or a star/planet. Entity will be displayed at the end of the wormhole, above the background.
	 */
	void setDestinationVisual(String category, String id, SectorEntityToken entity);
	void setStandardWormholeToStarOrPlanetVisual(SectorEntityToken entity);
	void setStandardWormholeToHyperspaceVisual();
	void setStandardWormholeToStarfieldVisual();
	void setStandardWormholeToNothingVisual();
	
	boolean isAutoCreateEntranceFromHyperspace();
	
	/**
	 * @return Whether the jump point leads to a star.
	 */
	boolean isStarAnchor();
	
	/**
	 * @return Whether the jump point leads to a gas giant.
	 */
	boolean isGasGiantAnchor();
	
	void setAutoCreateEntranceFromHyperspace(boolean autoCreateEntrance);

	SectorEntityToken getDestinationVisualEntity();
	
	
	SectorEntityToken getRelatedPlanet();
	
	/**
	 * This planet will be displayed by auto-generated jump points using this jump point as an exit.
	 * Should only be set for a jump point in the same location as the planet; only used for visuals.
	 * ONLY necessary if StatSystemAPI.autogenerateHyperspaceJumpPoints() is subsequently called.
	 * @param relatedPlanet
	 */
	void setRelatedPlanet(SectorEntityToken relatedPlanet);
	
	
	/**
	 * May only be called after this jump point was added to the system, and also after
	 * StarSystemAPI.autogenerateHyperspaceAnchors() was called for the system, OR initStar() was called
	 * with the "location in hyperspace" parameters (which auto-generates the main hyperspace anchor).
	 * @param entity
	 * @param radius
	 */
	void autoUpdateHyperJumpPointLocationBasedOnInSystemEntityAtRadius(SectorEntityToken entity, float radius);
	
	
	/**
	 * Purely visual.
	 */
	void open();
	
	/**
	 * Purely visual.
	 */
	void close();
	
	/**
	 * Skips animation.
	 */
	void forceOpen();
	
	/**
	 * Skips animation.
	 */
	void forceClose();

	StarSystemAPI getDestinationStarSystem();

	boolean isWormhole();


}


