package me.modmuss50.dg.dim;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.Optional;

public class ExitPlacer {

	private BlockPos blockPos;

	public ExitPlacer(BlockPos blockPos) {
		this.blockPos = blockPos;
	}

	public TeleportTarget placeEntity(Entity teleported, ServerWorld destination, Direction portalDir, double horizontalOffset, double verticalOffset) {
		if (blockPos == null) {
			if (teleported instanceof PlayerEntity) {
				blockPos = getBedLocation((ServerPlayerEntity) teleported, destination);
			}
			if (blockPos == null) {
				blockPos = destination.getSpawnPos();
			}
		}
		return new TeleportTarget(Vec3d.of(blockPos), new Vec3d(0, 0, 0), 0, 0);
	}

	private static BlockPos getBedLocation(ServerPlayerEntity playerEntity, ServerWorld destination) {
		Optional<BlockPos> bedLocation = playerEntity.getSleepingPosition();
		return bedLocation.flatMap(pos -> PlayerEntity.findRespawnPosition(destination, pos, 0, playerEntity.isSpawnForced(), true)
				.map(BlockPos::ofFloored))
				.orElse(null);
	}
}
