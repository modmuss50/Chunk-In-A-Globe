package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.dim.ExitPlacer;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class GlobeBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {

	private int globeID = -1;
	private Block baseBlock;

	private BlockPos returnPos;
	private DimensionType returnDimType;

	public GlobeBlockEntity() {
		super(DimensionGlobe.globeBlockEntityType);
	}

	@Override
	public void tick() {
		if (!world.isClient && globeID != -1) {
			if (!isInner()) {
				GlobeManager.getInstance((ServerWorld) world)
						.markGlobeForTicking(globeID);
			}
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
			if (Registry.BLOCK.getOrEmpty(identifier).isPresent()) {
				baseBlock = Registry.BLOCK.get(identifier);
			}
		}
		if (tag.contains("return_x")) {
			returnPos = new BlockPos(tag.getInt("return_x"), tag.getInt("return_y"), tag.getInt("return_z"));

			Identifier returnType = new Identifier(tag.getString("return_dim"));
			if (Registry.DIMENSION_TYPE.getOrEmpty(returnType).isPresent()) {
				returnDimType = Registry.DIMENSION_TYPE.get(returnType);
			} else {
				returnPos = null;
				returnDimType = null;
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.putInt("globe_id", globeID);
		if (baseBlock != null) {
			tag.putString("base_block", Registry.BLOCK.getId(baseBlock).toString());
		}

		if (returnPos != null && returnDimType != null) {
			tag.putInt("return_x", returnPos.getX());
			tag.putInt("return_y", returnPos.getY());
			tag.putInt("return_z", returnPos.getZ());
			tag.putString("return_dim", Registry.DIMENSION_TYPE.getId(returnDimType).toString());
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
			transportPlayerOut(playerEntity);
		} else {
			if (globeID == -1) {
				newGlobe();
			}

			FabricDimensions.teleport(playerEntity, DimensionGlobe.globeDimension, new GlobeDimensionPlacer(globeID, world.getDimension().getType(), getPos(), baseBlock));
		}
	}

	public void setReturnPos(BlockPos returnPos, DimensionType returnDimType) {
		this.returnPos = returnPos;
		this.returnDimType = returnDimType;
		markDirty();
	}

	public void transportPlayerOut(ServerPlayerEntity playerEntity) {
		if (getWorld().getDimension().getType() == DimensionGlobe.globeDimension) {
			DimensionType teleportDim = returnDimType == null ? DimensionType.OVERWORLD : returnDimType;
			FabricDimensions.teleport(playerEntity, teleportDim, new ExitPlacer(returnPos));
		}
	}

	public DimensionType getReturnDimType() {
		return returnDimType == null ? DimensionType.OVERWORLD : returnDimType;
	}

	public BlockPos getInnerScanPos() {
		if (returnPos == null) {
			return BlockPos.ORIGIN;
		}
		return returnPos.subtract(new Vec3i(8, 8 , 8));
	}

	public boolean isInner() {
		return getWorld().getDimension().getType() == DimensionGlobe.globeDimension;
	}

	public int getGlobeID() {
		return globeID;
	}

	public void setGlobeID(int globeID) {
		this.globeID = globeID;
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
