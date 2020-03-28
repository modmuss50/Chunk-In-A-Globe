package me.modmuss50.dg.dim;

import net.fabricmc.fabric.api.dimension.v1.EntityPlacer;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ExitPlacer implements EntityPlacer {

	private BlockPos blockPos;

	public ExitPlacer(BlockPos blockPos) {
		this.blockPos = blockPos;
	}

	@Override
	public BlockPattern.TeleportTarget placeEntity(Entity teleported, ServerWorld destination, Direction portalDir, double horizontalOffset, double verticalOffset) {
		if (blockPos == null) {
			if (teleported instanceof PlayerEntity) {
				blockPos = getBedLocation((PlayerEntity) teleported, destination);
			}
			if (blockPos == null) {
				blockPos = destination.getSpawnPos();
			}
		}
		return new BlockPattern.TeleportTarget(new Vec3d(blockPos), new Vec3d(0, 0, 0), 0);
	}

	private static BlockPos getBedLocation(PlayerEntity playerEntity, ServerWorld destination) {
		BlockPos bedLocation = playerEntity.getSpawnPosition();
		if (bedLocation == null) {
			return null;
		}
		return PlayerEntity.findRespawnPosition(destination, bedLocation, true)
				.map(BlockPos::new)
				.orElse(null);
	}
}
