package me.modmuss50.dg;

import me.modmuss50.dg.globe.GlobeBlockEntityRenderer;
import me.modmuss50.dg.utils.GlobeSection;
import me.modmuss50.dg.utils.GlobeSectionManagerClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class DimensionGlobeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(DimensionGlobe.globeBlockEntityType, (context) -> new GlobeBlockEntityRenderer());
		ClientPlayNetworking.registerGlobalReceiver(new Identifier(DimensionGlobe.MOD_ID, "section_update"), (client, packetContext, packetByteBuf, sender) -> {
			final int id = packetByteBuf.readInt();
			final boolean inner = packetByteBuf.readBoolean();
			final boolean blocks = packetByteBuf.readBoolean();
			final NbtCompound data = packetByteBuf.readNbt();

			client.execute(() -> {
				final GlobeSection section = GlobeSectionManagerClient.getGlobeSection(id, inner);
				if (blocks) {
					section.fromBlockTag(data);
				} else {
					section.fromEntityTag(data, packetContext.getWorld());
				}
				GlobeSectionManagerClient.provideGlobeSectionUpdate(inner, id, section);
			});

		});
		GlobeSectionManagerClient.register();
	}
}
