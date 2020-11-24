package com.fs.starfarer.api;


public class InteractionDialogImageVisual {

	private String spriteName = null;
	private SpriteId spriteId;
	private float subImageWidth, subImageHeight;
	private boolean showRandomSubImage = true;
	private float subImageX, subImageY;
	private float subImageDisplayWidth, subImageDisplayHeight;
	
	
	public InteractionDialogImageVisual(String category, String key, float subImageWidth, float subImageHeight) {
		this(new SpriteId(category, key), subImageWidth, subImageHeight, 480, 300);
	}
	public InteractionDialogImageVisual(SpriteId spriteId, float subImageWidth, float subImageHeight) {
		this(spriteId, subImageWidth, subImageHeight, 480, 300);
	}
	
	public InteractionDialogImageVisual(String spriteName, float subImageWidth, float subImageHeight) {
		this.spriteName = spriteName;
		this.subImageWidth = subImageWidth;
		this.subImageHeight = subImageHeight;
		
		showRandomSubImage = true;
		this.subImageDisplayWidth = 480;
		this.subImageDisplayHeight = 300;
	}
	
	public InteractionDialogImageVisual(SpriteId spriteId, float subImageWidth, float subImageHeight, 
				float subImageDisplayWidth, float subImageDisplayHeight) {
		this.spriteId = spriteId;
		this.spriteName = Global.getSettings().getSpriteName(spriteId.getCategory(), spriteId.getKey());
		this.subImageWidth = subImageWidth;
		this.subImageHeight = subImageHeight;
		
		showRandomSubImage = true;
//		subImageDisplayWidth = 400;
//		subImageDisplayHeight = 400;
		this.subImageDisplayWidth = subImageDisplayWidth;
		this.subImageDisplayHeight = subImageDisplayHeight;
	}
	
	public SpriteId getSpriteId() {
		return spriteId;
	}
	public void setSpriteId(SpriteId spriteId) {
		this.spriteId = spriteId;
	}
	public float getSubImageWidth() {
		return subImageWidth;
	}
	public void setSubImageWidth(float subImageWidth) {
		this.subImageWidth = subImageWidth;
	}
	public float getSubImageHeight() {
		return subImageHeight;
	}
	public void setSubImageHeight(float subImageHeight) {
		this.subImageHeight = subImageHeight;
	}
	public boolean isShowRandomSubImage() {
		return showRandomSubImage;
	}
	public void setShowRandomSubImage(boolean showRandomSubImage) {
		this.showRandomSubImage = showRandomSubImage;
	}
	public float getSubImageX() {
		return subImageX;
	}
	public void setSubImageX(float subImageX) {
		this.subImageX = subImageX;
	}
	public float getSubImageY() {
		return subImageY;
	}
	public void setSubImageY(float subImageY) {
		this.subImageY = subImageY;
	}
	public float getSubImageDisplayWidth() {
		return subImageDisplayWidth;
	}
	public void setSubImageDisplayWidth(float subImageDisplayWidth) {
		this.subImageDisplayWidth = subImageDisplayWidth;
	}
	public float getSubImageDisplayHeight() {
		return subImageDisplayHeight;
	}
	public void setSubImageDisplayHeight(float subImageDisplayHeight) {
		this.subImageDisplayHeight = subImageDisplayHeight;
	}
	public String getSpriteName() {
		return spriteName;
	}
	
	
}
