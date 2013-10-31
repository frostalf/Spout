/*
 * This file is part of Spout.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spout is licensed under the Spout License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spout is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.engine.protocol.builtin.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.spout.api.geo.cuboid.ChunkSnapshot;
import org.spout.api.util.SpoutToStringStyle;
import org.spout.api.util.cuboid.CuboidLightBuffer;

public class ChunkDataMessage extends SpoutMessage {
	private final boolean unload;
	// Block x, y, z
	private final int x, y, z;
	private final int[] blockInfo;
	private final Map<Short, byte[]> light;
	//private final byte[] blockLight, skyLight;
	private final byte[] biomeData;
	private final String biomeManagerClass;

	// TODO: protocol - what to do with light
	public ChunkDataMessage(int x, int y, int z) {
		this.unload = true;
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockInfo = null;
		this.biomeData = null;
		this.biomeManagerClass = null;
		light = null;
	}

	public ChunkDataMessage(ChunkSnapshot snapshot) {
		this.unload = false;
		this.x = snapshot.getX();
		this.y = snapshot.getY();
		this.z = snapshot.getZ();
		this.blockInfo = snapshot.getBlocks();
		light = new HashMap<>();
		for (CuboidLightBuffer b : snapshot.getLightBuffers()) {
			light.put(b.getManagerId(), b.serialize());
		}
		this.biomeData = snapshot.getBiomeManager() != null ? snapshot.getBiomeManager().serialize() : null;
		this.biomeManagerClass = snapshot.getBiomeManager() != null ? snapshot.getBiomeManager().getClass().getCanonicalName() : null;
	}

	public ChunkDataMessage(int x, int y, int z, int[] blocks, byte[] biomeData, String biomeManagerClass, Map<Short, byte[]> map) {
		this.unload = false;
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockInfo = blocks;
		this.biomeData = biomeData;
		this.biomeManagerClass = biomeManagerClass;
		this.light = map;
	}

	public boolean isUnload() {
		return unload;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int[] getBlockInfo() {
		return blockInfo;
	}

	public byte[] getBiomeData() {
		return biomeData;
	}

	public String getBiomeManagerClass() {
		return biomeManagerClass;
	}

	public boolean hasBiomes() {
		return biomeData != null && biomeManagerClass != null;
	}

	public Map<Short, byte[]> getLight() {
		return Collections.unmodifiableMap(light);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SpoutToStringStyle.INSTANCE)
				.append("unload", unload)
				.append("x", x)
				.append("y", y)
				.append("z", z)
				.append("blockInfo", blockInfo, false)
						//.append("blockLight", blockLight, false)
						//.append("skyLight", skyLigh, false)
				.append("biomeData", biomeData, false)
				.append("biomeManagerClass", biomeManagerClass)
				.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(47, 91)
				.append(unload)
				.append(x)
				.append(y)
				.append(z)
				.append(blockInfo)
						//.append(blockLight)
						//.append(skyLight)
				.append(biomeData)
				.append(biomeManagerClass)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChunkDataMessage) {
			final ChunkDataMessage other = (ChunkDataMessage) obj;
			return new EqualsBuilder()
					.append(unload, other.unload)
					.append(x, other.x)
					.append(y, other.y)
					.append(z, other.z)
					.append(blockInfo, other.blockInfo)
					//.append(blockIds, other.blockIds)
					//.append(blockData, other.blockData)
							//.append(blockLight, other.blockLight)
							//.append(skyLight, other.skyLight)
					.append(biomeData, other.biomeData)
					.append(biomeManagerClass, other.biomeManagerClass)
					.isEquals();
		} else {
			return false;
		}
	}
}
