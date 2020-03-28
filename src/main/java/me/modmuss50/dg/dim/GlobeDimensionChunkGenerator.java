package me.modmuss50.dg.dim;

import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class GlobeDimensionChunkGenerator extends ChunkGenerator<ChunkGeneratorConfig> {
	public GlobeDimensionChunkGenerator(IWorld world, BiomeSource biomeSource, ChunkGeneratorConfig config) {
		super(world, biomeSource, config);
	}

	@Override
	public void buildSurface(ChunkRegion chunkRegion, Chunk chunk) {

	}

	@Override
	public int getSpawnHeight() {
		return 128;
	}

	@Override
	public void populateNoise(IWorld world, Chunk chunk) {

	}

	@Override
	public int getHeightOnGround(int x, int z, Heightmap.Type heightmapType) {
		return 0;
	}
}
