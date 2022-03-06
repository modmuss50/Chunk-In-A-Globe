package me.modmuss50.dg.globe;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep.Carver;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

public class VoidChunkGenerator extends ChunkGenerator {

	public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
		VoidChunkGenerator.method_41042(instance)
		.and(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> generator.biomeSource))
		.apply(instance, instance.stable(VoidChunkGenerator::new)));

	public VoidChunkGenerator(Registry<StructureSet> registry, BiomeSource biomeSource) {
		super(registry, Optional.empty(), biomeSource);
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return this;
	}

	@Override
	public MultiNoiseSampler getMultiNoiseSampler() {
		return MultiNoiseUtil.method_40443();
	}

	@Override
	public void carve(ChunkRegion var1, long var2, BiomeAccess var4, StructureAccessor var5, Chunk var6, Carver var7) {
	}

	@Override
	public void buildSurface(ChunkRegion var1, StructureAccessor var2, Chunk var3) {
	}

	@Override
	public void populateEntities(ChunkRegion var1) {
	}

	@Override
	public int getWorldHeight() {
		return 384;
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor var1, Blender var2, StructureAccessor var3, Chunk var4) {
		return CompletableFuture.completedFuture(var4);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinimumY() {
		return 0;
	}

	@Override
	public int getHeight(int var1, int var2, Type var3, HeightLimitView var4) {
		return 0;
	}

	@Override
	public VerticalBlockSample getColumnSample(int var1, int var2, HeightLimitView var3) {
		return new VerticalBlockSample(0, new BlockState[0]);
	}

	@Override
	public void getDebugHudText(List<String> var1, BlockPos var2) {
	}
}