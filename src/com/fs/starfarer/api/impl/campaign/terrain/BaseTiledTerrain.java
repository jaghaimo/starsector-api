package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseTiledTerrain extends BaseTerrain {
	
	public static class TileParams {
		public String tiles;
		public int w;
		public int h;
		public String cat;
		public String key;
		public int tW;
		public int tH;
		public String name;
		public TileParams(String tiles, int width, int height,
				String tileTexCat, String tileTexKey, int tilesWide, int tilesHigh, String name) {
			this.tiles = tiles;
			this.w = width;
			this.h = height;
			this.cat = tileTexCat;
			this.key = tileTexKey;
			this.tW = tilesWide;
			this.tH = tilesHigh;
			this.name = name;
		}
		
	}
	
//	public static class Tile {
//		public int cellX;
//		public int cellY;
//		public int texCellX;
//		public int texCellY;
//	}
	
	protected TileParams params;
	protected transient SpriteAPI texture;
	protected transient SpriteAPI mapTexture;
	//protected Tile[][] tiles;
	protected transient int [][] tiles;
	
	protected long tileSeed;
	protected String savedTiles;
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		
		this.params = (TileParams) param;
		name = params.name;
		if (name == null) name = "Unknown";
		
		tiles = new int [params.w][params.h];
		
		
		tileSeed = new Random().nextLong();
//		Random random = new Random(tileSeed);
		
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				int index = i + (tiles[0].length - j - 1) * tiles.length;
				char c = params.tiles.charAt(index);
				if (!Character.isWhitespace(c)) {
//					int texX = (int) (Math.random() * params.tW);
//					int texY = (int) (Math.random() * params.tH);
//					int texX = (int) (random.nextFloat() * params.tW);
//					int texY = (int) (random.nextFloat() * params.tH);
//					tiles[i][j] = texX + texY * params.tW;
					tiles[i][j] = 1;
				} else {
					tiles[i][j] = -1;
				}
			}
		}
		
		savedTiles = encodeTiles(tiles);
		readResolve();
		
		params.tiles = null; // don't need to save this
	}
	
	protected void regenTiles() {
		Random random = new Random(tileSeed);
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				if (tiles[i][j] >= 0) {
					int texX = (int) (random.nextFloat() * params.tW);
					int texY = (int) (random.nextFloat() * params.tH);
					tiles[i][j] = texX + texY * params.tW;
				} else {
					tiles[i][j] = -1;
				}
			}
		}
	}
	
	
	public int[][] getTiles() {
		return tiles;
	}

	public TileParams getParams() {
		return params;
	}


	Object readResolve() {
		texture = Global.getSettings().getSprite(params.cat, params.key);
		mapTexture = Global.getSettings().getSprite(params.cat, params.key + "_map");

		if (savedTiles != null) {
			try {
				tiles = decodeTiles(savedTiles, params.w, params.h);
			} catch (DataFormatException e) {
				throw new RuntimeException("Error decoding tiled terrain tiles", e);
			}
		} else {
			 // shouldn't be here, if we are then savedTiles == null and something went badly wrong
			tiles = new int [params.w][params.h];
		}
		regenTiles();
		
		return this;
	}
	
	Object writeReplace() {
		params.tiles = null;
		savedTiles = encodeTiles(tiles);
		return this;
	}
	
	@Override
	public boolean containsEntity(SectorEntityToken other) {
		if (other.getContainingLocation() != this.entity.getContainingLocation()) return false;
		return containsPoint(other.getLocation(), other.getRadius());
	}
	
	public boolean containsPoint(Vector2f test, float r) {
		
		float dist = Misc.getDistance(this.entity.getLocation(), test) - r;
		if (dist > getRenderRange()) return false;
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float containsSize = getTileContainsSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;

		x -= w/2f;
		y -= h/2f;
		
		float extra = (containsSize - size) / 2f;
		
		if (test.x + r + extra < x) return false;
		if (test.y + r + extra < y) return false;
		if (test.x > x + w + r + extra) return false;
		if (test.y > y + h + r + extra) return false;
		
		int xIndex = (int) ((test.x - x) / size);
		int yIndex = (int) ((test.y - y) / size);
		
		if (xIndex < 0) xIndex = 0;
		if (yIndex < 0) yIndex = 0;
		
		if (xIndex >= tiles.length) xIndex = tiles.length - 1;
		if (yIndex >= tiles[0].length) yIndex = tiles[0].length - 1;
		
//		if (entity.isPlayerFleet()) {
//			System.out.println(this + " " + xIndex + "," + yIndex);
//		}
		
		for (float i = Math.max(0, xIndex - 1); i <= xIndex + 1 && i < tiles.length; i++) {
			for (float j = Math.max(0, yIndex - 1); j <= yIndex + 1 && j < tiles[0].length; j++) {
				int texIndex = tiles[(int) i][(int) j];
				if (texIndex >= 0) {
					float tx = x + i * size + size/2f - containsSize/2f;
					float ty = y + j * size + size/2f - containsSize/2f;
					 
					if (test.x + r < tx) continue;
					if (test.y + r < ty) continue;
					if (test.x > tx + containsSize + r) continue;
					if (test.y > ty + containsSize + r) continue;
					return true;
				}
			}
		}
		return false;
	}
	

	public abstract float getTileSize();
	public abstract float getTileRenderSize();
	public abstract float getTileContainsSize();
	public abstract void preRender(CampaignEngineLayers layer, float alphaMult);
	public abstract void preMapRender(float alphaMult);
	public abstract Color getRenderColor();
	
	public float getRenderRange() {
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		float w = tiles.length * size * 0.5f + (renderSize - size) * 0.5f;
		float h = tiles[0].length * size * 0.5f + (renderSize - size) * 0.5f;
		return Math.max(w, h) * 1.5f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI v) {
		texture.bindTexture();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		preRender(layer, v.getAlphaMult());
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;
		x -= w/2f;
		y -= h/2f;
		float extra = (renderSize - size) / 2f + 100f;
		
		float llx = v.getLLX();
		float lly = v.getLLY();
		float vw = v.getVisibleWidth();
		float vh = v.getVisibleHeight();
		
		if (llx > x + w + extra) return;
		if (lly > y + h + extra) return;
		if (llx + vw + extra < x) return;
		if (lly + vh + extra < y) return;
		
		float xStart = (int)((llx - x - extra) / size);
		if (xStart < 0) xStart = 0;
		float yStart = (int)((lly - y - extra) / size);
		if (yStart < 0) yStart = 0;
		
		float xEnd = (int)((llx + vw - x + extra) / size) + 1;
		if (xEnd >= tiles.length) xEnd = tiles.length - 1;
		float yEnd = (int)((lly + vw - y + extra) / size) + 1;
		if (yEnd >= tiles.length) yEnd = tiles[0].length - 1;
		
		renderSubArea(xStart, xEnd, yStart, yEnd, 1f, 1, v.getAlphaMult());
		
		//renderSubArea(0, tiles.length, 0, tiles[0].length, 1f);
	}
	
	public boolean isTileVisible(int i, int j) {
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;
		x -= w/2f;
		y -= h/2f;
		float extra = (renderSize - size) / 2f + 100f;
		
		ViewportAPI v = Global.getSector().getViewport();
		float llx = v.getLLX();
		float lly = v.getLLY();
		float vw = v.getVisibleWidth();
		float vh = v.getVisibleHeight();
		
		if (llx > x + w + extra) return false;
		if (lly > y + h + extra) return false;
		if (llx + vw + extra < x) return false;
		if (lly + vh + extra < y) return false;
		
		float xStart = (int)((llx - x - extra) / size);
		if (xStart < 0) xStart = 0;
		float yStart = (int)((lly - y - extra) / size);
		if (yStart < 0) yStart = 0;
		
		float xEnd = (int)((llx + vw - x + extra) / size) + 1;
		if (xEnd >= tiles.length) xEnd = tiles.length - 1;
		float yEnd = (int)((lly + vw - y + extra) / size) + 1;
		if (yEnd >= tiles.length) yEnd = tiles[0].length - 1;
		
		if (i < xStart) return false;
		if (i > xEnd) return false;
		if (j < yStart) return false;
		if (j > yEnd) return false;
		
		return true;
	}
	
	public void renderOnMap(float factor, float alphaMult) {
		mapTexture.bindTexture();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		preMapRender(alphaMult);
		renderSubArea(0, tiles.length, 0, tiles[0].length, factor, getNumMapSamples(), alphaMult);
	}
	
	public int getNumMapSamples() {
		return 5;
	}

	public void renderOnMapAbove(float factor, float alphaMult) {
		
	}
	
	public float[] getTileCenter(int i, int j) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		float size = getTileSize();
			
		float w = tiles.length * size;
		float h = tiles[0].length * size;
			
		float [] result = new float[2];
		result[0] = x - w / 2f + (float)i * size + size / 2f;
		result[1] = y - h / 2f + (float)j * size + size / 2f;
		return result;
	}
	
	protected void renderSubArea(float startColumn, float endColumn, float startRow, float endRow, float factor, int samples, float alphaMult) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;
		//if (true) return;
		
		//Random rand = new Random(tiles.length + tiles[0].length);
		//Random rand = new Random();
		
		if (samples == 1) {
			GL11.glBegin(GL11.GL_QUADS);
			for (float i = startColumn; i <= endColumn; i++) {
				if (i < 0 || i >= tiles.length) continue;
				for (float j = startRow; j <= endRow; j++) {
					if (j < 0 || j >= tiles[0].length) continue;
					int texIndex = tiles[(int) i][(int) j];
					if (texIndex >= 0) {
						int texCellX = texIndex % params.tW;
						int texCellY = texIndex / params.tW;
						
						Random rand = new Random((long) (i + j * tiles.length) * 1000000);
						float angle = rand.nextFloat() * 360f;
						float offRange = renderSize * 0.25f;
						float xOff = -offRange / 2f + offRange * rand.nextFloat();
						float yOff = -offRange / 2f + offRange * rand.nextFloat();
//						if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
//						  xOff = yOff = 0f;
//						}
	//					angle += angleOffset;
	//					angle = Misc.normalizeAngle(angle);
						renderQuad((int)i, (int)j, 
								   (x + xOff - w / 2f + i * size + size/2f - renderSize/2f) * factor,
								   (y + yOff - h / 2f +  j * size + size/2f - renderSize/2f) * factor,
								   renderSize * factor, renderSize * factor,
								   texCellX * 0.25f, texCellY * 0.25f,
								   0.25f, 0.25f,
								   angle);
					}
				}
			}
			GL11.glEnd();
		} else {
			//renderSize = (size * samples) + (renderSize - size);
			renderSize *= samples;
			size *= samples;
			alphaMult *= 0.67f;
			//alphaMult = 1f;
			GL11.glBegin(GL11.GL_QUADS);
			float max = samples * samples;
			for (float i = startColumn; i <= endColumn; i+=samples) {
				if (i < 0 || i >= tiles.length) continue;
				for (float j = startRow; j <= endRow; j+=samples) {
					int texIndex = -1;
					float angle = 0;
					float xOff = 0;
					float yOff = 0;
					float weight = 0;
					for (int m = 0; m < samples && i + m <= endColumn; m++) {
						if (i + m < 0 || i + m >= tiles.length) continue;
						for (int n = 0; n < samples && j + n < endRow; n++) {
							if (j + n < 0 || j + n >= tiles[0].length) continue;
							int currIndex = tiles[(int) i + m][(int) j + n];
							if (currIndex >= 0 && texIndex < 0) {
								texIndex = currIndex;
								Random rand = new Random((long) (i + j * tiles.length) * 1000000);
								angle = rand.nextFloat() * 360f;
								float offRange = renderSize * 0.25f;
								xOff = -offRange / 2f + offRange * rand.nextFloat();
								yOff = -offRange / 2f + offRange * rand.nextFloat();
//								if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
//									xOff = yOff = 0f;
//								}
							}
							if (currIndex >= 0) {
								weight++;
							}
						}
					}
					if (texIndex >= 0) {
						int texCellX = texIndex % params.tW;
						int texCellY = texIndex / params.tW;
						
						Color color = getRenderColor();
						float b = alphaMult * weight / max;
//						if (tiles.length > 30) {
//							b *= weight / max;
//							b *= weight / max;
//						}
						//b = alphaMult;
						GL11.glColor4ub((byte)color.getRed(),
								(byte)color.getGreen(),
								(byte)color.getBlue(),
								(byte)((float)color.getAlpha() * b));
						//xOff = yOff = 0f;
						renderQuad((int)i, (int)j, (x + xOff - w / 2f + i/samples * size + size/2f - renderSize/2f) * factor,
								   (y + yOff - h / 2f +  j/samples * size + size/2f - renderSize/2f) * factor,
								   renderSize * factor, renderSize * factor,
								   texCellX * 0.25f, texCellY * 0.25f,
								   0.25f, 0.25f,
								   angle);
					}
				}
			}
			GL11.glEnd();
		}
	}
	
	
	protected float elapsed = 0f;
	@Override
	public void advance(float amount) {
		super.advance(amount);
//		angleOffset += days * 20f;
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsed += days; 
	}

	protected void renderQuad(int i, int j, float x, float y, float width, float height, float texX, float texY, float texW, float texH, float angle) {
		if (angle != 0) {
			float vw = width / 2f;
			float vh = height / 2f;
			float cx = x + vw;
			float cy = y + vh;

			float cos = (float) Math.cos(angle * Misc.RAD_PER_DEG);
			float sin = (float) Math.sin(angle * Misc.RAD_PER_DEG);
			
			GL11.glTexCoord2f(texX, texY);
			GL11.glVertex2f(cx + (-vw * cos + vh * sin), cy + (-vw * sin - vh * cos));
	
			GL11.glTexCoord2f(texX, texY + texH);
			GL11.glVertex2f(cx + (-vw * cos - vh * sin), cy + (-vw * sin + vh * cos));
	
			GL11.glTexCoord2f(texX + texW, texY + texH);
			GL11.glVertex2f(cx + (vw * cos - vh * sin), cy + (vw * sin + vh * cos));
	
			GL11.glTexCoord2f(texX + texW, texY);
			GL11.glVertex2f(cx + (vw * cos + vh * sin), cy + (vw * sin - vh * cos));
			
		} else {
			GL11.glTexCoord2f(texX, texY);
			GL11.glVertex2f(x, y);
	
			GL11.glTexCoord2f(texX, texY + texH);
			GL11.glVertex2f(x, y + height);
	
			GL11.glTexCoord2f(texX + texW, texY + texH);
			GL11.glVertex2f(x + width, y + height);
	
			GL11.glTexCoord2f(texX + texW, texY);
			GL11.glVertex2f(x + width, y);
		}
	}
	
	public float getMaxEffectRadius(Vector2f locFrom) {
		// TODO: do intersection check from locFrom to an actual filled tile?
		
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		float w = tiles.length * size * 0.5f + (renderSize - size) * 0.5f;
		float h = tiles[0].length * size * 0.5f + (renderSize - size) * 0.5f;
		return Math.max(w, h) * 1.5f;
	}
	public float getMinEffectRadius(Vector2f locFrom) {
		return 0f;
	}
	
	public float getOptimalEffectRadius(Vector2f locFrom) {
		return getMaxEffectRadius(locFrom);
	}
	
	
	
	@Override
	protected float getExtraSoundRadius() {
		return 200f;
	}


	public float getProximitySoundFactor() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float r = player.getRadius() + getExtraSoundRadius();
		Vector2f test = player.getLocation();
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float containsSize = getTileContainsSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;

		x -= w/2f;
		y -= h/2f;
		
		float extra = (containsSize - size) / 2f;
		
		if (test.x + r + extra < x) return 0f;
		if (test.y + r + extra < y) return 0f;
		if (test.x > x + w + r + extra) return 0f;
		if (test.y > y + h + r + extra) return 0f;
		
		int xIndex = (int) ((test.x - x) / size);
		int yIndex = (int) ((test.y - y) / size);
		
		if (xIndex < 0) xIndex = 0;
		if (yIndex < 0) yIndex = 0;
		
		if (xIndex >= tiles.length) xIndex = tiles.length - 1;
		if (yIndex >= tiles[0].length) yIndex = tiles[0].length - 1;
		

		float closestDist = Float.MAX_VALUE;
		
		for (float i = Math.max(0, xIndex - 2); i <= xIndex + 2 && i < tiles.length; i++) {
			for (float j = Math.max(0, yIndex - 2); j <= yIndex + 2 && j < tiles[0].length; j++) {
				int texIndex = tiles[(int) i][(int) j];
				if (texIndex >= 0) {
					float tx = x + i * size + size/2f - containsSize/2f;
					float ty = y + j * size + size/2f - containsSize/2f;
					 
					if (test.x + r < tx) continue;
					if (test.y + r < ty) continue;
					if (test.x > tx + containsSize + r) continue;
					if (test.y > ty + containsSize + r) continue;
					
					//float dist = Misc.getDistance(test, new Vector2f(tx + containsSize/2f, ty + containsSize/2f));
					float dx = Math.abs(test.x - tx - containsSize / 2f);
					float dy = Math.abs(test.y - ty - containsSize / 2f);
					float dist = Math.max(dx, dy);
					if (dist < closestDist) {
						closestDist = dist;
					}
				}
			}
		}
		
		//System.out.println("Closest: " + closestDist);
		//float max = containsSize * 0.5f * 1.41f + EXTRA_SOUND_RADIUS;
		float max = containsSize * 0.5f + getExtraSoundRadius();
		if (closestDist < containsSize * 0.5f) return 1f;
		
		float p = (max - closestDist) / (max - containsSize * 0.5f);
		if (p < 0) p = 0;
		if (p > 1) p = 1;
		
		return p;
	}
	
	
	
	public static String encodeTiles(int [][] tiles) {
		int w = tiles.length;
		int h = tiles[0].length;
		int total = w * h;
		
//		int [] masks = new int [] {
//			1,
//			2,
//			4,
//			8,
//			16,
//			32,
//			64,
//			128,
//		};
		int [] masks = new int [] {
				128,
				64,
				32,
				16,
				8,
				4,
				2,
				1,
			};
		
		int bit = 0;
		int curr = 0;
		//List<Byte> bytes = new ArrayList<Byte>();
		byte [] input = new byte [(int) Math.ceil(total / 8f)];
		for (int i = 0; i < total; i++) {
			int x = i % w;
			int y = i / w;
			int val = tiles[x][y];
			int mask = masks[bit];
		
			if (val >= 0) {
				curr = (curr | mask);
			}
			
			bit++;
			bit %= 8;
			
			if (bit == 0) {
				input[i/8] = ((byte) curr);
				curr = 0;
			}
		}
		if (bit != 0) {
			input[input.length - 1] = ((byte) curr);
			curr = 0;
		}

		/*
		List<Byte> bytes = new ArrayList<Byte>();
		String seq = "";
		for (int i = 0; i < total; i++) {
			int x = i % w;
			int y = i / w;
			int val = tiles[x][y];
			String curr = "0";
			if (val >= 0) curr = "1";
			seq += curr;
			if (seq.length() == 8) {
				//byte b = Byte.parseByte(seq, 2);
				int b = Integer.parseInt(seq, 2);
				bytes.add((byte) b);
				seq = "";
			}
		}
		if (seq.length() > 0) {
			while (seq.length() < 8) {
				seq = seq + "0";
			}
			//byte b = Byte.parseByte(seq, 2);
			//bytes.add(b);
			int b = Integer.parseInt(seq, 2);
			bytes.add((byte) b);
		}
		
		byte [] input = new byte [bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			input[i] = bytes.get(i);
		}
		*/
		
		Deflater compressor = new Deflater();
		//compresser.setLevel(Deflater.BEST_COMPRESSION);
		//compresser.setStrategy(Deflater.HUFFMAN_ONLY);
		compressor.setInput(input);
		compressor.finish();

		StringBuilder result = new StringBuilder();
		byte [] temp = new byte[100];
		
		while (!compressor.finished()) {
			int read = compressor.deflate(temp);
			result.append(toHexString(Arrays.copyOf(temp, read)));
		}
		
//		result = new StringBuilder();
//		result.append(toHexString(input));

		compressor.end();
		
		return result.toString();
	}
	
	public static int [][] decodeTiles(String string, int w, int h) throws DataFormatException {
		byte [] input = toByteArray(string);
		
		Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		
		int [][] tiles = new int [w][h];
		int total = w * h;
		int curr = 0;
		
		byte [] temp = new byte[100];
		OUTER: while (!decompressor.finished()) {
			int read = decompressor.inflate(temp);
			for (int i = 0; i < read; i++) {
				byte b = temp[i];
				
				for (int j = 7; j >= 0; j--) {
					int x = curr % w;
					int y = curr / w;
					curr++;
				
					if (curr > total) break OUTER;
					//System.out.println("bytes read: " + curr);
					if ((b & (0x01 << j)) > 0) {
						tiles[x][y] = 1;
					} else {
						tiles[x][y] = -1;	
					}
				}
			}
		}

		decompressor.end();

		return tiles;
	}
	
	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printBase64Binary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseBase64Binary(s);
	}
	
	
	
	public static void main(String[] args) throws DataFormatException {
		
//		int [][] tiles = new int[][] {
//			{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//		};
		
		int [][] tiles = new int[][] {
				{-1, 1, 1, 1, -1},
				{1, 1, 1, 1, 1},
				{1, 1, 1, 1, -1},
				{1, 1, 1, -1, 1},
		};
		
//		int w = 128;
//		int h = 128;
//		int [][] tiles = new int [w][h];
//		
//		for (int i = 0; i < w; i++) {
//			for (int j = 0; j < h; j++) {
//				if ((float) Math.random() > 0.8f) {
//					tiles[i][j] = 0;
//				} else {
//					tiles[i][j] = -1;
//				}
//			}
//		}
		
		
		System.out.println("Original:");
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				System.out.print(String.format("% 2d,", tiles[i][j]));
			}
			System.out.println();
		}
		
		String result = encodeTiles(tiles);
		System.out.println(result);
		//System.out.println(result.length() + ", would be " + (w * h / 4) + " without compression");
		int [][] tilesBack = decodeTiles(result, tiles.length, tiles[0].length);
		
		System.out.println("Decoded:");
		for (int i = 0; i < tilesBack.length; i++) {
			for (int j = 0; j < tilesBack[0].length; j++) {
				System.out.print(String.format("% 2d,", tilesBack[i][j]));
			}
			System.out.println();
		}
		
		boolean equals = true;
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				if (tiles[i][j] != tilesBack[i][j]) {
					equals = false;
				}
			}
		}
		
		System.out.println("Equal: " + equals);
		
//		for (int x = 0; x < tilesBack.length; x++) {
//			for (int y = 0; y < tilesBack.length; y++) {
//				System.out.print(tilesBack[x][y] + " ");
//			}
//			System.out.println();
//		}
	}
}







