package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;

public class GlobeBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {

	private int globeID = -1;

	public GlobeBlockEntity() {
		super(DimensionGlobe.globeBlockEntityType);
	}

	@Override
	public void tick() {
		if (!world.isClient && globeID != -1) {
			GlobeManager.getInstance((ServerWorld) world)
					.markGlobeForTicking(globeID);
		}
		if (!world.isClient && world.getTime() % 20 == 0) {
			GlobeSectionManagerServer.updateAndSyncToPlayers(this);
		}
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		globeID = tag.getInt("globe_id");
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.putInt("globe_id", globeID);
		return super.toTag(tag);
	}


	private void newGlobe() {
		if (world.isClient) {
			throw new RuntimeException();
		}

		globeID = GlobeManager.getInstance((ServerWorld) world).getNextGlobe().getId();
		markDirty();
		sync();
	}

	public void transportPlayer(ServerPlayerEntity playerEntity) {
		if (world.isClient) {
			throw new RuntimeException();
		}

		if (playerEntity.world.getDimension().getType() == DimensionGlobe.globeDimension) {
			throw new RuntimeException();
		} else {
			if (globeID == -1) {
				newGlobe();
			}

			FabricDimensions.teleport(playerEntity, DimensionGlobe.globeDimension, new GlobeDimensionPlacer(globeID, world.getDimension().getType(), getPos()));
		}
	}

	public int getGlobeID() {
		return globeID;
	}

	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		fromTag(compoundTag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return toTag(compoundTag);
	}
}
