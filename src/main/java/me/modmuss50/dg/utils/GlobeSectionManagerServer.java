package me.modmuss50.dg.utils;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class GlobeSectionManagerServer {

	@SuppressWarnings("resource")
	public static void updateAndSyncToPlayers(GlobeBlockEntity blockEntity, boolean blocks) {
		if (blockEntity.getWorld().isClient) {
			throw new RuntimeException();
		}

		if (blockEntity.getGlobeID() == -1) {
			return;
		}

		ServerWorld serverWorld = (ServerWorld) blockEntity.getWorld();
		List<ServerPlayerEntity> nearbyPlayers = new ArrayList<>();

		for (ServerPlayerEntity player : serverWorld.getPlayers()) {
			if (player.squaredDistanceTo(Vec3d.of(blockEntity.getPos())) < 64) {
				nearbyPlayers.add(player);
			}
		}

		if (nearbyPlayers.isEmpty()) {
			return;
		}

		GlobeManager.Globe globe = GlobeManager.getInstance(serverWorld).getGlobeByID(blockEntity.getGlobeID());

		ServerWorld updateWorld = serverWorld.getServer().getWorld(blockEntity.isInner() ? blockEntity.getReturnDimType() : DimensionGlobe.globeDimension);

		if (blocks) {
			globe.updateBlockSection(updateWorld, blockEntity.isInner(), blockEntity);
		} else {
			globe.updateEntitySection(updateWorld, blockEntity.isInner(), blockEntity);
			if (globe.getGlobeSection(blockEntity.isInner()).getEntities().isEmpty()) {
				return;
			}
		}

		GlobeSection section = globe.getGlobeSection(blockEntity.isInner());

		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(globe.getId());
		buf.writeBoolean(blockEntity.isInner());
		if (blocks) {
			buf.writeBoolean(true);
			buf.writeNbt(section.toBlockTag());
		} else {
			buf.writeBoolean(false);
			buf.writeNbt(section.toEntityTag(blockEntity.isInner() ? blockEntity.getInnerScanPos() : globe.getGlobeLocation()));
		}

		CustomPayloadS2CPacket clientBoundPacket = new CustomPayloadS2CPacket(new Identifier(DimensionGlobe.MOD_ID, "section_update"), buf);
		for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
			nearbyPlayer.networkHandler.sendPacket(clientBoundPacket);
		}
	}

	public static void register() {
		ServerSidePacketRegistry.INSTANCE.register(new Identifier(DimensionGlobe.MOD_ID, "update_request"), (packetContext, packetByteBuf) -> {
			final int amount = packetByteBuf.readInt();
			IntSet updateQueue = new IntOpenHashSet();
			for (int i = 0; i < amount; i++) {
				updateQueue.add(packetByteBuf.readInt());
			}
			packetContext.getTaskQueue().execute(() -> {
				for (Integer id : updateQueue) {
					updateAndSyncToPlayers((ServerPlayerEntity) packetContext.getPlayer(), id, true);
					updateAndSyncToPlayers((ServerPlayerEntity) packetContext.getPlayer(), id, false);
				}

			});
		});
	}

	public static void updateAndSyncToPlayers(ServerPlayerEntity playerEntity, int globeID, boolean blocks) {
		if (globeID == -1) {
			return;
		}
		ServerWorld serverWorld = (ServerWorld) playerEntity.world;

		GlobeManager.Globe globe = GlobeManager.getInstance(serverWorld).getGlobeByID(globeID);

		ServerWorld updateWorld = serverWorld.getServer().getWorld(DimensionGlobe.globeDimension);

		if (blocks) {
			globe.updateBlockSection(updateWorld, false, null);
		} else {
			globe.updateEntitySection(updateWorld, false, null);
		}

		GlobeSection section = globe.getGlobeSection(false);

		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(globe.getId());
		buf.writeBoolean(false);
		if (blocks) {
			buf.writeBoolean(true);
			buf.writeNbt(section.toBlockTag());
		} else {
			buf.writeBoolean(false);
			buf.writeNbt(section.toEntityTag(globe.getGlobeLocation()));
		}

		CustomPayloadS2CPacket clientBoundPacket = new CustomPayloadS2CPacket(new Identifier(DimensionGlobe.MOD_ID, "section_update"), buf);
		playerEntity.networkHandler.sendPacket(clientBoundPacket);
	}

}
