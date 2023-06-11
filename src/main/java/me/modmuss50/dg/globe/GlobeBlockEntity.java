package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.dim.ExitPlacer;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class GlobeBlockEntity extends BlockEntity {

	private int globeID = -1;
	private Block baseBlock;

	private BlockPos returnPos;
	private RegistryKey<World> returnDimType;

	public GlobeBlockEntity(BlockPos pos, BlockState state) {
		super(DimensionGlobe.globeBlockEntityType, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, GlobeBlockEntity entity) {
		if (!world.isClient && entity.globeID != -1) {
			if (!entity.isInner()) {
				GlobeManager.getInstance((ServerWorld) world)
						.markGlobeForTicking(entity.globeID);
			}
		}
		if (!world.isClient) {
			if (world.getTime() % 20 == 0) {
				GlobeSectionManagerServer.updateAndSyncToPlayers(entity, true);
			} else {
				GlobeSectionManagerServer.updateAndSyncToPlayers(entity, false);
			}
		}
	}

	@Override
	 public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		globeID = tag.getInt("globe_id");
		if (tag.contains("base_block")) {
			Identifier identifier = new Identifier(tag.getString("base_block"));
			if (Registries.BLOCK.getOrEmpty(identifier).isPresent()) {
				baseBlock = Registries.BLOCK.get(identifier);
			}
		}
		if (tag.contains("return_x")) {
			returnPos = new BlockPos(tag.getInt("return_x"), tag.getInt("return_y"), tag.getInt("return_z"));

			Identifier returnType = new Identifier(tag.getString("return_dim"));
			returnDimType = RegistryKey.of(RegistryKeys.WORLD, returnType);
		}
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		tag.putInt("globe_id", globeID);
		if (baseBlock != null) {
			tag.putString("base_block", Registries.BLOCK.getId(baseBlock).toString());
		}

		if (returnPos != null && returnDimType != null) {
			tag.putInt("return_x", returnPos.getX());
			tag.putInt("return_y", returnPos.getY());
			tag.putInt("return_z", returnPos.getZ());
			tag.putString("return_dim", returnDimType.getValue().toString());
		}

		super.writeNbt(tag);
	}


	private void newGlobe() {
		if (world.isClient) {
			throw new RuntimeException();
		}

		globeID = GlobeManager.getInstance((ServerWorld) world).getNextGlobe().getId();
		markDirty();
		//TODO sync();
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
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = createNbt();
		writeNbt(tag);
		return tag;
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
