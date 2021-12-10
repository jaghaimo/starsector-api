package com.fs.starfarer.api.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionGridAPI;


public class CollisionGridUtil implements CollisionGridAPI {
	
	protected class BucketIterator implements Iterator<Object> {
		protected Iterator<Object> curr = null;
		protected Set<Object> objects;
		protected BucketIterator() {
		}
		protected BucketIterator(int startX, int endX, int startY, int endY) {
			objects = new LinkedHashSet<Object>();
			if (startX < 0) startX = 0;
			if (endX >= width) endX = width - 1;
			if (startY < 0) startY = 0;
			if (endY >= height) endY = height - 1;
			
			for (int i = startX; i <= endX; i++) {
				for (int j = startY; j <= endY; j++) {
					if (buckets[i][j] == null) continue;
					objects.addAll(buckets[i][j]);
				}
			}
			curr = objects.iterator();
        }
		public BucketIterator createCopy() {
			BucketIterator copy = new BucketIterator();
			copy.objects = objects;
			copy.curr = copy.objects.iterator();
			return copy;
		}
		public boolean hasNext() {
			return curr.hasNext();
		}
		public Object next() {
			return curr.next();
		}
        public void remove() {
			throw new UnsupportedOperationException();
        }
	}


	protected float cellSize;
	protected List<Object>[][] buckets;
	
	protected int width, height, leftOf, rightOf, below, above;
	
	@SuppressWarnings("unchecked")
    public CollisionGridUtil(float minX, float maxX, float minY, float maxY, float cellSize) {
	    this.cellSize = cellSize;
	    
	    leftOf = -(int) Math.floor(minX / cellSize);
	    rightOf = (int) Math.ceil(maxX / cellSize);
	    below = -(int) Math.floor(minY / cellSize);
	    above = (int) Math.ceil(maxY / cellSize);
	    
	    width = leftOf + rightOf;
	    height = below + above;
	    buckets = new List[width][height];
    }
	
	public void addObject(Object object, Vector2f loc, float objWidth, float objHeight) {
		int startX = (int) (leftOf + ((loc.x - objWidth/2f) / cellSize));
		int endX = (int) (leftOf + (loc.x + objWidth/2f) / cellSize);
		
		int startY = (int) (below + ((loc.y - objHeight/2f) / cellSize));
		int endY = (int) (below + (loc.y + objHeight/2f) / cellSize);
		
		for (int i = startX; i <= endX; i++) {
			for (int j = startY; j <= endY; j++) {
				addToBucket(i, j, object);
			}
		}
	}
	
	public void removeObject(Object object, Vector2f loc, float objWidth, float objHeight) {
		int startX = (int) (leftOf + ((loc.x - objWidth/2f) / cellSize));
		int endX = (int) (leftOf + (loc.x + objWidth/2f) / cellSize);
		
		int startY = (int) (below + ((loc.y - objHeight/2f) / cellSize));
		int endY = (int) (below + (loc.y + objHeight/2f) / cellSize);
		for (int i = startX; i <= endX; i++) {
			for (int j = startY; j <= endY; j++) {
				removeFromBucket(i, j, object);
			}
		}
	}
	
	protected void addToBucket(int cellX, int cellY, Object object) {
		if (cellX < 0 || cellX >= width || cellY < 0 || cellY >= height) return;
		if(buckets[cellX][cellY] == null) {
			buckets[cellX][cellY] = new ArrayList<Object>();
		}
		if (!buckets[cellX][cellY].contains(object)) {
			buckets[cellX][cellY].add(object);
		}
	}
	
	protected void removeFromBucket(int cellX, int cellY, Object object) {
		if (cellX < 0 || cellX >= width || cellY < 0 || cellY >= height) return;
		if(buckets[cellX][cellY] == null) return;
		buckets[cellX][cellY].remove(object);
	}

	public Iterator<Object> getCheckIterator(Vector2f loc, float objWidth, float objHeight) {
		int startX = (int) (leftOf + ((loc.x - objWidth/2f) / cellSize));
		//int endX = (int) (leftOf + Math.ceil((loc.x + objWidth/2f) / cellSize));
		int endX = (int) (leftOf + (loc.x + objWidth/2f) / cellSize);
		
		int startY = (int) (below + ((loc.y - objHeight/2f) / cellSize));
		//int endY = (int) (below + Math.ceil((loc.y + objHeight/2f) / cellSize));
		int endY = (int) (below + (loc.y + objHeight/2f) / cellSize);

		BucketIterator result = new BucketIterator(startX, endX, startY, endY);
		return result;
	}
	

}
