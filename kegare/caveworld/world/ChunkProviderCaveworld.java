package kegare.caveworld.world;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kegare.caveworld.core.Caveworld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkProviderCaveworld implements IChunkProvider
{
	private final World worldObj;
	private final long seed;
	private final Random random;
	private final boolean generateStructures;

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();

	{
		caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, InitMapGenEvent.EventType.CAVE);
		ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, InitMapGenEvent.EventType.RAVINE);
		mineshaftGenerator = (MapGenMineshaft)TerrainGen.getModdedMapGen(mineshaftGenerator, InitMapGenEvent.EventType.MINESHAFT);
	}

	public ChunkProviderCaveworld(World world)
	{
		this.worldObj = world;
		this.seed = world.getSeed();
		this.random = new Random(seed);
		this.generateStructures = world.getWorldInfo().isMapFeaturesEnabled();
	}

	@Override
	public Chunk provideChunk(int chunkX, int chunkZ)
	{
		random.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

		byte[] blocks = new byte[65536];
		Arrays.fill(blocks, (byte)Block.stone.blockID);

		if (Caveworld.generateCaves)
		{
			caveGenerator.generate(this, worldObj, chunkX, chunkZ, blocks);
		}

		if (Caveworld.generateRavine)
		{
			ravineGenerator.generate(this, worldObj, chunkX, chunkZ, blocks);
		}

		if (Caveworld.generateMineshaft && generateStructures)
		{
			mineshaftGenerator.generate(this, worldObj, chunkX, chunkZ, blocks);
		}

		Chunk chunk = new Chunk(worldObj, blocks, chunkX, chunkZ);
		chunk.resetRelightChecks();

		return chunk;
	}

	@Override
	public Chunk loadChunk(int chunkX, int chunkZ)
	{
		return provideChunk(chunkX, chunkZ);
	}

	@Override
	public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		BlockSand.fallInstantly = true;

		int var1 = chunkX * 16;
		int var2 = chunkZ * 16;
		Chunk chunk = worldObj.getChunkFromChunkCoords(chunkX, chunkZ);
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(var1 + 16, var2 + 16);
		BiomeDecorator decorator = biome.createBiomeDecorator();
		random.setSeed(seed);
		long var3 = random.nextLong() / 2L * 2L + 1L;
		long var4 = random.nextLong() / 2L * 2L + 1L;
		random.setSeed((long)chunkX * var3 + (long)chunkZ * var4 ^ seed);

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, worldObj, random, chunkX, chunkZ, false));

		if (Caveworld.generateMineshaft && generateStructures)
		{
			mineshaftGenerator.generateStructuresInChunk(worldObj, random, chunkX, chunkZ);
		}

		if (Caveworld.generateLakes)
		{
			if (TerrainGen.populate(chunkProvider, worldObj, random, chunkX, chunkZ, false, EventType.LAKE) && random.nextInt(4) == 0)
			{
				int x = var1 + random.nextInt(16) + 8;
				int y = random.nextInt(224);
				int z = var2 + random.nextInt(16) + 8;

				(new WorldGenLakes(Block.waterStill.blockID)).generate(worldObj, random, x, y, z);
			}

			if (TerrainGen.populate(chunkProvider, worldObj, random, chunkX, chunkZ, false, EventType.LAVA) && random.nextInt(8) == 0)
			{
				int x = var1 + random.nextInt(16) + 8;
				int y = random.nextInt(random.nextInt(120) + 8);
				int z = var2 + random.nextInt(16) + 8;

				if (y < 63 || random.nextInt(10) == 0)
				{
					(new WorldGenLakes(Block.lavaStill.blockID)).generate(worldObj, random, x, y, z);
				}
			}
		}

		if (Caveworld.generateDungeon && TerrainGen.populate(chunkProvider, worldObj, random, chunkX, chunkZ, false, EventType.DUNGEON))
		{
			for (int i = 0; i < 8; ++i)
			{
				int x = var1 + random.nextInt(16) + 8;
				int y = random.nextInt(224);
				int z = var2 + random.nextInt(16) + 8;

				(new WorldGenDungeons()).generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 20; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 128) + 128;
			int z = var2 + random.nextInt(16);

			if (TerrainGen.generateOre(worldObj, random, decorator.dirtGen, chunkX, chunkZ, GenerateMinable.EventType.DIRT))
			{
				decorator.dirtGen.generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 10; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 128) + 128;
			int z = var2 + random.nextInt(16);

			if (TerrainGen.generateOre(worldObj, random, decorator.gravelGen, chunkX, chunkZ, GenerateMinable.EventType.GRAVEL))
			{
				decorator.gravelGen.generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 20; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 128) + 128;
			int z = var2 + random.nextInt(16);

			if (TerrainGen.generateOre(worldObj, random, decorator.coalGen, chunkX, chunkZ, GenerateMinable.EventType.COAL))
			{
				decorator.coalGen.generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 20; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 128) + 128;
			int z = var2 + random.nextInt(16);

			if (TerrainGen.generateOre(worldObj, random, decorator.ironGen, chunkX, chunkZ, GenerateMinable.EventType.IRON))
			{
				decorator.ironGen.generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 3; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 200) + 200;
			int z = var2 + random.nextInt(16);

			if (TerrainGen.generateOre(worldObj, random, decorator.goldGen, chunkX, chunkZ, GenerateMinable.EventType.GOLD))
			{
				decorator.goldGen.generate(worldObj, random, x, y, z);
			}
		}

		for (int i = 0; i < 3; ++i)
		{
			int x = var1 + random.nextInt(16);
			int y = random.nextInt(255 - 200) + 200;
			int z = var2 + random.nextInt(16);

			(new WorldGenMinable(Block.oreEmerald.blockID, 8)).generate(worldObj, random, x, y, z);
		}

		biome.decorate(worldObj, random, var1, var2);

		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				chunk.setBlockIDWithMetadata(x, 0, z, Block.bedrock.blockID, 0);
				chunk.setBlockIDWithMetadata(x, 255, z, Block.bedrock.blockID, 0);
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, worldObj, random, chunkX, chunkZ, false));

		BlockSand.fallInstantly = false;
	}

	@Override
	public boolean chunkExists(int chunkX, int chunkZ)
	{
		return true;
	}

	@Override
	public boolean saveChunks(boolean flag, IProgressUpdate progress)
	{
		return true;
	}

	@Override
	public boolean unloadQueuedChunks()
	{
		return false;
	}

	@Override
	public boolean canSave()
	{
		return true;
	}

	@Override
	public String makeString()
	{
		return "CaveworldRandomLevelSource";
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType creature, int x, int y, int z)
	{
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(x, z);

		return biome == null ? null : biome.getSpawnableList(creature);
	}

	@Override
	public ChunkPosition findClosestStructure(World world, String name, int x, int y, int z)
	{
		return "Mineshaft".equals(name) ? mineshaftGenerator.getNearestInstance(world, x, y, z) : null;
	}

	@Override
	public int getLoadedChunkCount()
	{
		return 0;
	}

	@Override
	public void recreateStructures(int chunkX, int chunkZ)
	{
		if (Caveworld.generateMineshaft && generateStructures)
		{
			mineshaftGenerator.generate(this, worldObj, chunkX, chunkZ, (byte[])null);
		}
	}

	@Override
	public void saveExtraData() {}
}