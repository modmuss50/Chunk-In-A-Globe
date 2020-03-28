package me.modmuss50.dg.exit;

import me.modmuss50.dg.DimensionGlobe;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class ExitBlockEntity extends BlockEntity {

	private BlockPos returnPos;
	private DimensionType returnDimType;

	public ExitBlockEntity() {
		super(DimensionGlobe.exitBlockEntityType);
	}

	public void setReturnPos(BlockPos returnPos, DimensionType returnDimType) {
		this.returnPos = returnPos;
		this.returnDimType = returnDimType;
		markDirty();
	}

	public void transportPlayer(ServerPlayerEntity playerEntity) {
		if (getWorld().getDimension().getType() == DimensionGlobe.globeDimension) {
			DimensionType teleportDim = returnDimType == null ? DimensionType.OVERWORLD : returnDimType;
			FabricDimensions.teleport(playerEntity, teleportDim, new ExitPlacer(returnPos));
		}
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		if (tag.contains("return_x")) {
			returnPos = new BlockPos(tag.getInt("return_x"), tag.getInt("return_y"), tag.getInt("return_z"));

			Identifier returnType = new Identifier(tag.getString("return_dim"));
			if (Registry.DIMENSION_TYPE.containsId(returnType)) {
				returnDimType = Registry.DIMENSION_TYPE.get(returnType);
			} else {
				returnPos = null;
				returnDimType = null;
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		if (returnPos != null && returnDimType != null) {
			tag.putInt("return_x", returnPos.getX());
			tag.putInt("return_y", returnPos.getY());
			tag.putInt("return_z", returnPos.getZ());
			tag.putString("return_dim", Registry.DIMENSION_TYPE.getId(returnDimType).toString());
		}
		return super.toTag(tag);
	}
}
