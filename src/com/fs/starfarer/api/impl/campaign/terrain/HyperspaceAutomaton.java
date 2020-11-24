package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.fs.starfarer.api.util.IntervalUtil;

public class HyperspaceAutomaton {

	protected transient int [][] cells;
	protected transient int [][] next;
	
	protected String savedCells, savedNext;

	protected IntervalUtil interval;
	protected boolean doneWithIteration = false;
	protected int currentColumn = 0;
	protected int width;
	protected int height;
	public HyperspaceAutomaton(int width, int height, float minInterval, float maxInterval) {
		this.width = width;
		this.height = height;
		cells = new int [width][height];
		next = new int [width][height];
		
		interval = new IntervalUtil(minInterval, maxInterval);
		
		setLive(0.1f);
		interval.forceIntervalElapsed();
	}
	
	Object readResolve() {
		if (savedCells != null) {
			try {
				cells = decodeTiles(savedCells, width, height);
			} catch (DataFormatException e) {
				throw new RuntimeException("Error decoding hyperspace automaton tiles", e);
			}
		} else {
			 // shouldn't be here, if we are then savedTiles == null and something went badly wrong
			cells = new int [width][height];
		}
		if (savedNext != null) {
			try {
				next = decodeTiles(savedNext, width, height);
			} catch (DataFormatException e) {
				throw new RuntimeException("Error decoding hyperspace automaton tiles", e);
			}
		} else {
			// shouldn't be here, if we are then savedTiles == null and something went badly wrong
			next = new int [width][height];
		}
		
		return this;
	}
	
	Object writeReplace() {
		savedCells = encodeTiles(cells);
		savedNext = encodeTiles(next);
		return this;
	}
	
	
	public IntervalUtil getInterval() {
		return interval;
	}

	public void advance(float days) {
		updateCells(days);
		
		interval.advance(days);
		if (interval.intervalElapsed()) {
			//System.out.println("Max reached:" + currentColumn);
			doneWithIteration = false;
			setLive(0.01f);
			//setLive(0.05f);
			cells = next;
			next = new int [cells.length][cells[0].length];
			for (int i = 0; i < next.length; i++) {
				for (int j = 0; j < next[0].length; j++) {
					next[i][j] = cells[i][j];
				}
			}
			currentColumn = 0;
		}
	}
	
	
	public int[][] getCells() {
		return cells;
	}

	protected void updateCells(float days) {
		if (currentColumn >= cells.length) return;
		
		int chunk = (int) ((days * 1.5f) / interval.getMinInterval() * cells.length);
		if (chunk < 1) chunk = 1;
		int maxColumn = currentColumn + chunk;
		
		// 0: dead
		// 1: live
		// 2: dying
		for (int i = currentColumn; i < cells.length && i < maxColumn; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				int val = (int) cells[i][j];
				if (val == 0) {
					int count = getLiveCountAround(i, j);
					if (count == 2) {
 						next[i][j] = 1;
					}
//					else if ((float) Math.random() < getRandomLifeChance()) {
//						next[i][j] = 1;
//					}
				} else if (val == 1) {
					next[i][j] = 2;
				} else if (val == 2) {
					next[i][j] = 0;
				}
				
//				int val = (int) cells[i][j];
//				int count = getLiveCountAround(i, j);
//				if (count < 2) {
//					next[i][j] = 0;
//				} else if (val != 0 && (count == 2 || count == 3)) {
//					// nothing happens
//				} else if (count > 3) { 
//					next[i][j] = 0;
//				} else if (val == 0 && count == 3) {
//					next[i][j] = 1;
//				}
//				
//				if ((float) Math.random() < getRandomLifeChance()) {
//					next[i][j] = 1;
//				}
			}
		}
		
		currentColumn = maxColumn;
	}
	
	public void setLive(float fraction) {
		float count = (float) (next.length * next[0].length) * fraction;
		if (count <= 0) {
			count = (float) Math.random() < count ? 1 : 0;
		}
		
		Random r = new Random();
		for (int i = 0; i < count; i++) {
			int x = r.nextInt(next.length);
			int y = r.nextInt(next[0].length);
			next[x][y] = 1;
		}
	}
	
//	protected float getRandomLifeChance() {
//		return 0.001f;
//	}
	
	protected int getLiveCountAround(int x, int y) {
		int count = 0;
		for (int i = Math.max(0, x - 1); i <= Math.min(x + 1, cells.length - 1); i++) {
			for (int j = Math.max(0, y - 1); j <= Math.min(y + 1, cells[0].length - 1); j++) {
				if (i == x && j == y) continue;
				if (cells[i][j] == 1) {
					count++;
				}
			}
		}
		return count;
	}
	
	
	public static String encodeTiles(int [][] tiles) {
		int w = tiles.length;
		int h = tiles[0].length;
		int total = w * h;
		
		int [] masks = new int [] {
				128 + 64,
				32 + 16,
				8 + 4,
				2 + 1,
			};
		
		int bitPair = 0;
		int curr = 0;
		//List<Byte> bytes = new ArrayList<Byte>();
		byte [] input = new byte [(int) Math.ceil(total / 4)];
		for (int i = 0; i < total; i++) {
			int x = i % w;
			int y = i / w;
			int val = tiles[x][y];
			int mask = masks[bitPair];
		
			if (val >= 0) {
				//curr = (curr | mask);
				curr = curr | ((val << (8 - (bitPair + 1) * 2)) & mask);
			}
			bitPair++;
			bitPair %= 4;
			
			if (bitPair == 0) {
				input[i/4] = ((byte) curr);
				curr = 0;
			}
		}
		if (bitPair != 0) {
			input[(total - 1) / 8] = ((byte) curr);
			curr = 0;
		}
		
		/*
		List<Byte> bytes = new ArrayList<Byte>();
		String seq = "";
		for (int i = 0; i < total; i++) {
			int x = i % w;
			int y = i / w;
			int val = tiles[x][y];
			String curr = "00";
			if (val == 1) curr = "01";
			if (val == 2) curr = "10";
			if (val == 3) curr = "11";
			seq += curr;
			if (seq.length() == 8) {
				int b = Integer.parseInt(seq, 2);
				bytes.add((byte) b);
				seq = "";
			}
		}
		if (seq.length() > 0) {
			while (seq.length() < 8) {
				seq = seq + "0";
			}
			int b = Integer.parseInt(seq, 2);
			bytes.add((byte) b);
		}
		
		byte [] input = new byte [bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			input[i] = bytes.get(i);
		}
		*/
		
		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();

		StringBuilder result = new StringBuilder();
		byte [] temp = new byte[100];
		
		while (!compresser.finished()) {
			int read = compresser.deflate(temp);
			result.append(BaseTiledTerrain.toHexString(Arrays.copyOf(temp, read)));
		}

		compresser.end();
		
		return result.toString();
	}
	
	public static int [][] decodeTiles(String string, int w, int h) throws DataFormatException {
		byte [] input = BaseTiledTerrain.toByteArray(string);
		
		Inflater decompresser = new Inflater();
		decompresser.setInput(input);
		
		int [] masks = new int [] {
				128 + 64,
				32 + 16,
				8 + 4,
				2 + 1,
			};
		
		int [][] tiles = new int [w][h];
		int total = w * h;
		int curr = 0;
		
		byte [] temp = new byte[100];
		OUTER: while (!decompresser.finished()) {
			int read = decompresser.inflate(temp);
			for (int i = 0; i < read; i++) {
				byte b = temp[i];
				for (int j = 3; j >= 0; j--) {
					int x = curr % w;
					int y = curr / w;
					curr++;
				
					if (curr > total) break OUTER;
					
					tiles[x][y] = (b >> j * 2) & 3;
				}
				/*
				for (int j = 7; j >= 0; j-=2) {
					int x = curr % w;
					int y = curr / w;
					curr++;
				
					if (curr > total) break OUTER;
					
					if ((b & (0x01 << j)) == 0 && (b & (0x01 << j)) == 0) {
						tiles[x][y] = 0;
					} else if ((b & (0x01 << j)) == 0 && (b & (0x01 << j)) > 0) {
						tiles[x][y] = 1;
					}  else if ((b & (0x01 << j)) > 0 && (b & (0x01 << j)) == 0) {
						tiles[x][y] = 2;
					} else if ((b & (0x01 << j)) > 0 && (b & (0x01 << j)) > 0) {
						tiles[x][y] = 3;
					}
				}
				*/
			}
		}

		decompresser.end();

		return tiles;
	}
	
	
	public static void main(String[] args) throws DataFormatException {
		
		int [][] tiles = new int[][] {
				{0, 1, 1, 3, 1},
				{3, 1, 2, 3, 2},
				{2, 2, 1, 3, 3},
				{1, 1, 2, 3, 2},
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






