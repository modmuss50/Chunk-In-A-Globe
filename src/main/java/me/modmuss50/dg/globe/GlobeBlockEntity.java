package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.registry.Registry;

public class GlobeBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {

	private int globeID = -1;
	private Block baseBlock;

	public GlobeBlockEntity() {
		super(DimensionGlobe.globeBlockEntityType);
	}

	@Override
	public void tick() {
		if (!world.isClient && globeID != -1) {
			GlobeManager.getInstance((ServerWorld) world)
					.markGlobeForTicking(globeID);
		}
		if (!world.isClient) {
			if (world.getTime() % 20 == 0) {
				GlobeSectionManagerServer.updateAndSyncToPlayers(this, true);
			} else {
				GlobeSectionManagerServer.updateAndSyncToPlayers(this, false);
			}

		}
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		globeID = tag.getInt("globe_id");
		if (tag.contains("base_block")) {
			Identifier identifier = new Identifier(tag.getString("base_block"));
			if (Registry.BLOCK.containsId(identifier)) {
				baseBlock = Registry.BLOCK.get(identifier);
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.putInt("globe_id", globeID);
		if (baseBlock != null) {
			tag.putString("base_block", Registry.BLOCK.getId(baseBlock).toString());
		}
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

	public Block getBaseBlock() {
		if (baseBlock == null) {
			return Blocks.OAK_PLANKS;
		}
		return baseBlock;
	}

	public void setBaseBlock(Block baseBlock) {
		this.baseBlock = baseBlock;
		markDirty();
	}
}
