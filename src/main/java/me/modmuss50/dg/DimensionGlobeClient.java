package me.modmuss50.dg;

import me.modmuss50.dg.globe.GlobeBlockEntityRenderer;
import me.modmuss50.dg.utils.GlobeSection;
import me.modmuss50.dg.utils.GlobeSectionManagerClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class DimensionGlobeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(DimensionGlobe.globeBlockEntityType, GlobeBlockEntityRenderer::new);
		ClientSidePacketRegistry.INSTANCE.register(new Identifier(DimensionGlobe.MOD_ID, "section_update"), new PacketConsumer() {
			@Override
			public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
				final int id = packetByteBuf.readInt();
				final CompoundTag data = packetByteBuf.readCompoundTag();
				final GlobeSection section = new GlobeSection();
				section.fromTag(data);
				packetContext.getTaskQueue().execute(() -> GlobeSectionManagerClient.provideGlobeSectionUpdate(id, section));
			}
		});
	}
}
