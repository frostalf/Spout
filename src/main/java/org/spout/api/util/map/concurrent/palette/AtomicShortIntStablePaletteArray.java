/*
 * This file is part of SpoutAPI.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * SpoutAPI is licensed under the SpoutDev License Version 1.
 *
 * SpoutAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.api.util.map.concurrent.palette;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.spout.api.math.MathHelper;
import org.spout.api.util.map.concurrent.AtomicVariableWidthArray;

public class AtomicShortIntStablePaletteArray extends AtomicShortIntStableArray {
	
	private final static int CALCULATE_UNIQUE = -1;
	
	private final int width;
	private final int paletteSize;
	// TODO - the id lookup should be replaced by a "throw-away" stable hashmap class
	private final Map<Integer, Short> idLookup = new ConcurrentHashMap<Integer, Short>();
	private final AtomicVariableWidthArray store;
	private final AtomicIntegerArray palette;
	private final AtomicInteger paletteCounter;

	public AtomicShortIntStablePaletteArray(int length) {
		this(null, length, false, false, CALCULATE_UNIQUE);
	}
	
	public AtomicShortIntStablePaletteArray(AtomicShortIntStableArray previous, boolean expand) {
		this(previous, previous.length(), previous.shouldCompress(), expand, CALCULATE_UNIQUE);
	}
	
	public AtomicShortIntStablePaletteArray(AtomicShortIntStableArray previous, int length, boolean compress, boolean expand, int unique) {
		super(length);
		if (previous == null) {
			width = 1;
		} else {
			if (compress) {
				if (unique == CALCULATE_UNIQUE) {
					unique = previous.getUnique();
				}
				width = roundUpWidth(expand ? unique : (unique - 1));
			} else {
				int oldWidth = previous.width();
				width = oldWidth <= 8 ? (oldWidth << 1) : (16);
			}
		}
		paletteSize = Math.min(widthToPaletteSize(width), length1p5());
		store = new AtomicVariableWidthArray(length, width);
		palette = new AtomicIntegerArray(paletteSize);
		paletteCounter = new AtomicInteger(1);
		idLookup.put(0, (short) 0);
		if (previous != null) {
			try {
				for (int i = 0; i < length; i++) {
					set(i, previous.get(i));
				}
			} catch (PaletteFullException pfe) {
				throw new IllegalStateException("Unable to copy old array to new array, as palette was filled, length " + length + ", paletteSize " + paletteSize + ", unique " + unique);
			}
		}
	}

	@Override
	public int width() {
		return width;
	}
	
	@Override
	public int getPaletteSize() {
		return paletteSize;
	}

	@Override
	public int getPaletteUsage() {
		return paletteCounter.get();
	}
	
	@Override
	public boolean shouldCompress() {
		return paletteSize >= length1p5() && getPaletteUsage() > length();
	}

	@Override
	public int get(int i) {
		return palette.get(store.get(i));
	}

	@Override
	public int set(int i, int newValue) throws PaletteFullException {
		int id = getId(newValue);
		int oldId = store.getAndSet(i, id);
		return palette.get(oldId);
	}

	@Override
	public boolean compareAndSet(int i, int expect, int update) throws PaletteFullException {
		Short expId = idLookup.get(expect);
		if (expId == null) {
			return false;
		}
		int newId = getId(update);
		return compareAndSet(i, expId, newId);
	}
	
	/**
	 * Gets the id for the given value, allocating an id if required
	 * 
	 * @param value
	 * @return the id
	 * @throws PaletteFullException
	 */
	private int getId(int value) throws PaletteFullException {
		Short id = idLookup.get(value);
		if (id != null) {
			return id;
		} else {
			id = (short) paletteCounter.getAndIncrement();
			if (id >= paletteSize) {
				throw paletteFull;
			}
			idLookup.put(value, id);
			palette.set(id, value);
			return id;
		}
	}
	
	private static final int[] roundLookup = new int[65537];
	
	static {
		roundLookup[0] = 0;
		roundLookup[1] = 1;
		roundLookup[2] = 1;
		roundLookup[4] = 2;
		roundLookup[8] = 4;
		roundLookup[16] = 4;
		roundLookup[32] = 8;
		roundLookup[64] = 8;
		roundLookup[128] = 8;
		roundLookup[256] = 8;
		roundLookup[512] = 16;
		roundLookup[1024] = 16;
		roundLookup[2048] = 16;
		roundLookup[4096] = 16;
		roundLookup[8192] = 16;
		roundLookup[16384] = 16;
	}
	
	
	public static int roundUpWidth(int i) {
		return roundLookup[MathHelper.roundUpPow2(i + 1)];
	}
	
	public static int widthToPaletteSize(int width) {
		return 1 << width;
	}
}
