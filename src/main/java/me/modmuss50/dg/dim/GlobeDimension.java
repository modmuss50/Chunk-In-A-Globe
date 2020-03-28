package me.modmuss50.dg.dim;

import me.modmuss50.dg.DimensionGlobe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class GlobeDimension extends Dimension {
	public GlobeDimension(World world, DimensionType type) {
		super(world, type, 0f);
	}

	@Override
	public ChunkGenerator<?> createChunkGenerator() {
		return DimensionGlobe.globeChunkGenerator.create(world, BiomeSourceType.FIXED.applyConfig(BiomeSourceType.FIXED.getConfig(world.getLevelProperties()).setBiome(Biomes.PLAINS)), DimensionGlobe.globeChunkGenerator.createSettings());
	}

	@Override
	public BlockPos getSpawningBlockInChunk(ChunkPos chunkPos, boolean checkMobSpawnValidity) {
		return null;
	}

	@Override
	public BlockPos getTopSpawningBlockPosition(int x, int z, boolean checkMobSpawnValidity) {
		return null;
	}

	@Override
	public float getSkyAngle(long timeOfDay, float tickDelta) {
		return 0;
	}

	@Override
	public boolean hasVisibleSky() {
		return false;
	}

	@Override
	public Vec3d getFogColor(float skyAngle, float tickDelta) {
		float f = MathHelper.cos(skyAngle * 6.2831855F) * 2.0F + 0.5F;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		float g = 0.7529412F;
		float h = 0.84705883F;
		float i = 1.0F;
		g *= f * 0.94F + 0.06F;
		h *= f * 0.94F + 0.06F;
		i *= f * 0.91F + 0.09F;
		return new Vec3d(g, h, i);
	}

	@Override
	public boolean canPlayersSleep() {
		return false;
	}

	@Override
	public boolean isFogThick(int x, int z) {
		return false;
	}

	@Override
	public DimensionType getType() {
		return DimensionGlobe.globeDimension;
	}

	@Override
	public float getBrightness(int lightLevel) {
		return 15F;
	}
}
