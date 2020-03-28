package me.modmuss50.dg.utils;

import io.netty.buffer.Unpooled;
import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GlobeSectionManagerServer {

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
			if (player.squaredDistanceTo(new Vec3d(blockEntity.getPos())) < 64) {
				nearbyPlayers.add(player);
			}
		}

		if (nearbyPlayers.isEmpty()) {
			return;
		}

		GlobeManager.Globe globe = GlobeManager.getInstance(serverWorld).getGlobeByID(blockEntity.getGlobeID());
		if (blocks) {
			globe.updateBlockSection(serverWorld.getServer().getWorld(DimensionGlobe.globeDimension));
		} else {
			globe.updateEntitySection(serverWorld.getServer().getWorld(DimensionGlobe.globeDimension));
			if (globe.getGlobeSection().getEntities().isEmpty()) {
				return;
			}
		}

		GlobeSection section = globe.getGlobeSection();

		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(globe.getId());
		if (blocks) {
			buf.writeBoolean(true);
			buf.writeCompoundTag(section.toBlockTag());
		} else {
			buf.writeBoolean(false);
			buf.writeCompoundTag(section.toEntityTag(globe.getGlobeLocation()));
		}

		CustomPayloadS2CPacket clientBoundPacket = new CustomPayloadS2CPacket(new Identifier(DimensionGlobe.MOD_ID, "section_update"), buf);
		for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
			nearbyPlayer.networkHandler.sendPacket(clientBoundPacket);
		}
	}

}
