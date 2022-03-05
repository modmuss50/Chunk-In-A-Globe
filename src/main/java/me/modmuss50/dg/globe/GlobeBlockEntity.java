package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.dim.ExitPlacer;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class GlobeBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {

	private int globeID = -1;
	private Block baseBlock;

	private BlockPos returnPos;
	private RegistryKey<World> returnDimType;

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
	public void readNbt(BlockState state, NbtCompound tag) {
		super.readNbt(state, tag);
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
			returnDimType = RegistryKey.of(Registry.WORLD_KEY, returnType);
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.putInt("globe_id", globeID);
		if (baseBlock != null) {
			tag.putString("base_block", Registry.BLOCK.getId(baseBlock).toString());
		}

		if (returnPos != null && returnDimType != null) {
			tag.putInt("return_x", returnPos.getX());
			tag.putInt("return_y", returnPos.getY());
			tag.putInt("return_z", returnPos.getZ());
			tag.putString("return_dim", returnDimType.getValue().toString());
		}

		return super.writeNbt(tag);
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

		if (playerEntity.world.getRegistryKey() == DimensionGlobe.globeDimension) {
			transportPlayerOut(playerEntity);
		} else {
			if (globeID == -1) {
				newGlobe();
			}
			ServerWorld targetWorld = playerEntity.getServer().getWorld(DimensionGlobe.globeDimension);
			GlobeDimensionPlacer placer = new GlobeDimensionPlacer(globeID, world.getRegistryKey(), getPos(), baseBlock);
			FabricDimensions.teleport(playerEntity, targetWorld, placer.placeEntity(playerEntity, targetWorld, Direction.NORTH, 0, 0));
		}
	}

	public void setReturnPos(BlockPos returnPos, RegistryKey<World> returnDimType) {
		this.returnPos = returnPos;
		this.returnDimType = returnDimType;
		markDirty();
	}

	public void transportPlayerOut(ServerPlayerEntity playerEntity) {
		if (getWorld().getRegistryKey() == DimensionGlobe.globeDimension) {
			RegistryKey<World> teleportDim = returnDimType == null ? World.OVERWORLD : returnDimType;
			ServerWorld world = playerEntity.getServer().getWorld(teleportDim);
			ExitPlacer placer = new ExitPlacer(returnPos);
			FabricDimensions.teleport(playerEntity, world, placer.placeEntity(playerEntity, world, Direction.NORTH, 0, 0));
		}
	}

	public RegistryKey<World> getReturnDimType() {
		return returnDimType == null ? World.OVERWORLD : returnDimType;
	}

	public BlockPos getInnerScanPos() {
		if (returnPos == null) {
			return BlockPos.ORIGIN;
		}
		return returnPos.subtract(new Vec3i(8, 8 , 8));
	}

	public boolean isInner() {
		return getWorld().getRegistryKey() == DimensionGlobe.globeDimension;
	}

	public int getGlobeID() {
		return globeID;
	}

	public void setGlobeID(int globeID) {
		this.globeID = globeID;
	}

	@Override
	public void fromClientTag(NbtCompound compoundTag) {
		readNbt(null, compoundTag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound compoundTag) {
		return writeNbt(compoundTag);
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
