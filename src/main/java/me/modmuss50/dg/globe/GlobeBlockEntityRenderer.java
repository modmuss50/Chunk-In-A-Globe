package me.modmuss50.dg.globe;

import me.modmuss50.dg.utils.GlobeSection;
import me.modmuss50.dg.utils.GlobeSectionManagerClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class GlobeBlockEntityRenderer extends BlockEntityRenderer<GlobeBlockEntity> {
	public GlobeBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(GlobeBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		final float scale = 1 / 16F;
		final BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		final GlobeSection section = GlobeSectionManagerClient.getGlobeSection(blockEntity.getGlobeID());
		matrices.push();
		matrices.scale(scale, scale, scale);
		for (Map.Entry<BlockPos, BlockState> entry : section.getStateMap().entrySet()) {
			matrices.push();
			matrices.translate(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
			renderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
			matrices.pop();
		}
		matrices.pop();
	}
}
