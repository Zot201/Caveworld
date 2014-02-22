/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 * Please check the contents of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package com.kegare.caveworld.world.gen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.MapGenRavine;

import java.util.Random;

public class MapGenRavineCaveworld extends MapGenRavine
{
	private final float[] field_75046_d = new float[1024];

	@Override
	protected void func_151540_a(long ravineSeed, int chunkX, int chunkZ, Block[] blocks, double blockX, double blockY, double blockZ, float scale, float leftRightRadian, float upDownRadian, int currentY, int targetY, double scaleHeight)
	{
		int worldHeight = worldObj.getActualHeight();
		Random random = new Random(ravineSeed);
		double centerX = (chunkX << 4) + 8;
		double centerZ = (chunkZ << 4) + 8;
		float leftRightChange = 0.0F;
		float upDownChange = 0.0F;

		if (targetY <= 0)
		{
			int blockRangeY = range * 16 - 16;
			targetY = blockRangeY - random.nextInt(blockRangeY / 4);
		}

		boolean createFinalRoom = false;

		if (currentY == -1)
		{
			currentY = targetY / 2;
			createFinalRoom = true;
		}

		float nextInterHeight = 1.0F;

		for (int y = 0; y < worldHeight; ++y)
		{
			if (y == 0 || random.nextInt(3) == 0)
			{
				nextInterHeight = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
			}

			field_75046_d[y] = nextInterHeight * nextInterHeight;
		}

		for (; currentY < targetY; ++currentY)
		{
			double roomWidth = 1.5D + MathHelper.sin(currentY * (float)Math.PI / targetY) * scale * 1.0F;
			double roomHeight = roomWidth * scaleHeight;
			roomWidth *= random.nextFloat() * 0.25D + 0.75D;
			roomHeight *= random.nextFloat() * 0.25D + 0.75D;
			float moveHorizontal = MathHelper.cos(upDownRadian);
			float moveVertical = MathHelper.sin(upDownRadian);
			blockX += MathHelper.cos(leftRightRadian) * moveHorizontal;
			blockY += moveVertical;
			blockZ += MathHelper.sin(leftRightRadian) * moveHorizontal;
			upDownRadian *= 0.7F;
			upDownRadian += upDownChange * 0.05F;
			leftRightRadian += leftRightChange * 0.05F;
			upDownChange *= 0.8F;
			leftRightChange *= 0.5F;
			upDownChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			leftRightChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if (createFinalRoom || random.nextInt(4) != 0)
			{
				double distanceX = blockX - centerX;
				double distanceZ = blockZ - centerZ;
				double distanceY = targetY - currentY;
				double maxDistance = scale + 2.0F + 16.0F;

				if (distanceX * distanceX + distanceZ * distanceZ - distanceY * distanceY > maxDistance * maxDistance)
				{
					return;
				}

				if (blockX >= centerX - 16.0D - roomWidth * 2.0D && blockZ >= centerZ - 16.0D - roomWidth * 2.0D && blockX <= centerX + 16.0D + roomWidth * 2.0D && blockZ <= centerZ + 16.0D + roomWidth * 2.0D)
				{
					int xLow = Math.max(MathHelper.floor_double(blockX - roomWidth) - (chunkX << 4) - 1, 0);
					int xHigh = Math.min(MathHelper.floor_double(blockX + roomWidth) - (chunkX << 4) + 1, 16);
					int yLow = Math.max(MathHelper.floor_double(blockY - roomHeight) - 1, 1);
					int yHigh = Math.min(MathHelper.floor_double(blockY + roomHeight) + 1, worldHeight - 8);
					int zLow = Math.max(MathHelper.floor_double(blockZ - roomWidth) - (chunkZ << 4) - 1, 0);
					int zHigh = Math.min(MathHelper.floor_double(blockZ + roomWidth) - (chunkZ << 4) + 1, 16);

					for (int x = xLow; x < xHigh; ++x)
					{
						double xScale = ((chunkX << 4) + x + 0.5D - blockX) / roomWidth;

						for (int z = zLow; z < zHigh; ++z)
						{
							double zScale = ((chunkZ << 4) + z + 0.5D - blockZ) / roomWidth;
							int index = ((x << 4) + z) * 128 + yHigh;

							if (xScale * xScale + zScale * zScale < 1.0D)
							{
								for (int y = yHigh - 1; y >= yLow; --y)
								{
									double yScale = (y + 0.5D - blockY) / roomHeight;

									if ((xScale * xScale + zScale * zScale) * field_75046_d[y] + yScale * yScale / 6.0D < 1.0D)
									{
										digBlock(blocks, index, x, y, z, chunkX, chunkZ, false);
									}

									--index;
								}
							}
						}
					}

					if (createFinalRoom)
					{
						break;
					}
				}
			}
		}
	}

	@Override
	protected void digBlock(Block[] blocks, int index, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop)
	{
		BiomeGenBase biome = worldObj.getBiomeGenForCoords((chunkX << 4) + x, (chunkZ << 4) + z);
		Block top = biome.topBlock;
		Block filler = biome.fillerBlock;
		Block block = blocks[index];

		if (block == Blocks.stone || block == Blocks.netherrack || block == Blocks.end_stone || block == top || block == filler)
		{
			if (y < 10)
			{
				blocks[index] = Blocks.flowing_lava;
			}
			else
			{
				blocks[index] = null;
			}
		}
	}
}