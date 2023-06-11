package me.modmuss50.dg.dim;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSection;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class GlobeDimensionPlacer {

	private int globeId = -1;
	private RegistryKey<World> returnDimension = null;
	private BlockPos returnPos = null;
	private Block baseBlock = null;

	public GlobeDimensionPlacer() {
	}

	public GlobeDimensionPlacer(int globeId, RegistryKey<World> dimensionType, BlockPos returnPos, Block baseBlock) {
		this.globeId = globeId;
		this.returnDimension = dimensionType;
		this.returnPos = returnPos;
		this.baseBlock = baseBlock;
	}

	public TeleportTarget placeEntity(Entity entity, ServerWorld serverWorld, Direction direction, double v, double v1) {
		if (globeId == -1) {
			throw new RuntimeException("Unknown globe: " + globeId);
		}
		GlobeManager.Globe globe = GlobeManager.getInstance(serverWorld).getGlobeByID(globeId);

		BlockPos globePos = globe.getGlobeLocation();
		BlockPos spawnPos = globePos.add(8, 1, 8);
		buildGlobe(serverWorld, globePos, spawnPos);

		return new TeleportTarget(Vec3d.of(spawnPos).add(0.5, 0,0.5), new Vec3d(0, 0, 0), 0, 0);
	}

	private void buildGlobe(ServerWorld world, BlockPos globePos, BlockPos spawnPos) {
		final BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = 0; x < GlobeSection.GLOBE_SIZE; x++) {
			for (int y = 0; y < GlobeSection.GLOBE_SIZE; y++) {
				for (int z = 0; z < GlobeSection.GLOBE_SIZE; z++) {
					if (x == 0 || x == GlobeSection.GLOBE_SIZE -1 || y == 0 || y == GlobeSection.GLOBE_SIZE -1 || z == 0 || z == GlobeSection.GLOBE_SIZE -1) {
						mutable.set(globePos.getX() + x, globePos.getY() + y, globePos.getZ() + z);
						world.setBlockState(mutable, Blocks.BARRIER.getDefaultState());
					}

				}
			}
		}

		world.setBlockState(spawnPos.down(), DimensionGlobe.globeBlock.getDefaultState());
		GlobeBlockEntity exitBlockEntity = (GlobeBlockEntity) world.getBlockEntity(spawnPos.down());
		exitBlockEntity.setGlobeID(globeId);
		exitBlockEntity.setBaseBlock(baseBlock);
		if (returnPos != null && returnDimension != null) {
			exitBlockEntity.setReturnPos(returnPos, returnDimension);
		}
	}
}
