package me.modmuss50.dg.utils;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeBlockItem;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class GlobeSectionManagerClient {

	private static Int2ObjectMap<GlobeSection> selectionMap = new Int2ObjectArrayMap<>();
	private static Int2ObjectMap<GlobeSection> innerSelectionMap = new Int2ObjectArrayMap<>();
	private static IntSet updateQueue = new IntOpenHashSet();

	public static GlobeSection getGlobeSection(int globeID, boolean inner) {
		if (inner) {
			if (!innerSelectionMap.containsKey(globeID)) {
				return new GlobeSection();
			}
			return innerSelectionMap.get(globeID);
		} else {
			if (!selectionMap.containsKey(globeID)) {
				return new GlobeSection();
			}
			return selectionMap.get(globeID);
		}
	}

	public static void provideGlobeSectionUpdate(boolean inner, int globeID, GlobeSection globeSection) {
		if (inner) {
			innerSelectionMap.put(globeID, globeSection);
		} else {
			selectionMap.put(globeID, globeSection);
		}
	}

	public static void requestGlobeUpdate(int globeID) {
		updateQueue.add(globeID);
	}

	public static void register() {
		ClientTickCallback.EVENT.register(minecraftClient -> {
			if (minecraftClient.world == null) {
				//Ensure the state is not transfered across worlds
				updateQueue.clear();
				selectionMap.clear();
				innerSelectionMap.clear();
				return;
			}
			if (minecraftClient.world.getTime() % 20 == 0 || minecraftClient.player.inventory.getMainHandStack().getItem() instanceof GlobeBlockItem) {
				processUpdateQueue();
			}
		});
	}

	private static void processUpdateQueue() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

		buf.writeInt(updateQueue.size());
		for (Integer i : updateQueue) {
			buf.writeInt(i);
		}

		CustomPayloadC2SPacket serverBoundPacket = new CustomPayloadC2SPacket(new Identifier(DimensionGlobe.MOD_ID, "update_request"), buf);
		MinecraftClient.getInstance().player.networkHandler.sendPacket(serverBoundPacket);

		updateQueue.clear();
	}
}
