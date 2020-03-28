package me.modmuss50.dg;

import me.modmuss50.dg.globe.GlobeBlockEntityRenderer;
import me.modmuss50.dg.utils.GlobeSection;
import me.modmuss50.dg.utils.GlobeSectionManagerClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class DimensionGlobeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(DimensionGlobe.globeBlockEntityType, GlobeBlockEntityRenderer::new);
		ClientSidePacketRegistry.INSTANCE.register(new Identifier(DimensionGlobe.MOD_ID, "section_update"), (packetContext, packetByteBuf) -> {
			final int id = packetByteBuf.readInt();
			final boolean inner = packetByteBuf.readBoolean();
			final boolean blocks = packetByteBuf.readBoolean();
			final CompoundTag data = packetByteBuf.readCompoundTag();

			packetContext.getTaskQueue().execute(() -> {
				final GlobeSection section = GlobeSectionManagerClient.getGlobeSection(id, inner);
				if (blocks) {
					section.fromBlockTag(data);
				} else {
					if (packetContext.getPlayer() != null) {
						section.fromEntityTag(data, packetContext.getPlayer().world);
					}
				}
				GlobeSectionManagerClient.provideGlobeSectionUpdate(inner, id, section);
			});

		});
		GlobeSectionManagerClient.register();
	}
}
