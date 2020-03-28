package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.utils.GlobeSection;
import me.modmuss50.dg.utils.GlobeSectionManagerClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class GlobeBlockEntityRenderer extends BlockEntityRenderer<GlobeBlockEntity> {
	public GlobeBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	private static int renderDepth = 0;

	@Override
	public void render(GlobeBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		final boolean inner = blockEntity.getWorld().getDimension().getType() == DimensionGlobe.globeDimension;
		renderGlobe(inner, blockEntity.getGlobeID(), tickDelta, matrices, vertexConsumers, light, overlay);
		if (inner) {
			matrices.push();
			matrices.translate(0, 1, 0);
			renderBase(blockEntity.getBaseBlock(), matrices, vertexConsumers, light, overlay);
			matrices.pop();

			matrices.push();
			matrices.translate(-7.5, 0, -7.5);
			matrices.scale(16F, 16F, 16F);
			renderBase(blockEntity.getBaseBlock(), matrices, vertexConsumers, light, overlay);
			matrices.pop();
		} else {
			renderBase(blockEntity.getBaseBlock(), matrices, vertexConsumers, light, overlay);
		}
	}

	public static void renderGlobe(boolean inner, int globeID, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (renderDepth > 2) {
			return;
		}
		renderDepth ++;
		if (globeID != -1) {
			final float scale = inner ? 16F : 1 / 16F;

			final BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
			final GlobeSection section = GlobeSectionManagerClient.getGlobeSection(globeID, inner);
			matrices.push();
			if (inner) {
				matrices.translate(-8 * scale, -8 * scale, -8 * scale);
				matrices.translate(-7.5, 0, -7.5);
			} else {
				matrices.translate(-1 / 32F, 0, -1/32F);
			}

			matrices.scale(scale, scale, scale);
			for (Map.Entry<BlockPos, BlockState> entry : section.getStateMap().entrySet()) {
				matrices.push();
				matrices.translate(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
				renderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}

			for (Entity entity : section.getEntities()) {
				Vec3d position = section.getEntityVec3dMap().get(entity);

				matrices.push();

				if (inner) {
					matrices.translate(position.getX(), position.getY(), position.getZ());
				} else {
					matrices.translate(position.getX(), position.getY(), position.getZ());
				}

				entity.setPos(0, 0, 0);
				entity.prevX = 0;
				entity.prevY = 0;
				entity.prevZ = 0;
				MinecraftClient.getInstance().getEntityRenderManager().render(entity, 0.0D, 0.0D, 0.0D, 0, 1, matrices, vertexConsumers, light);
				matrices.pop();

			}
			matrices.pop();
		}
		renderDepth --;
	}

	public static void renderBase(Block baseBlock, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		final BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();

		matrices.push();
		BakedModel bakedModel = renderManager.getModel(baseBlock.getDefaultState());
		Sprite blockSprite =  bakedModel.getSprite();
		Identifier blockTexture = new Identifier(blockSprite.getId().getNamespace(), "textures/" + blockSprite.getId().getPath() + ".png");
		BaseModel baseModel = new BaseModel(blockSprite);
		baseModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(blockTexture)), light, overlay, 1F, 1F, 1F, 1F);
		matrices.pop();
	}

	private static class BaseModel extends Model {

		private final ModelPart base;

		public BaseModel(Sprite sprite) {
			super(RenderLayer::getEntityCutoutNoCull);
			textureHeight = sprite.getHeight();
			textureWidth = sprite.getWidth();

			base = new ModelPart(this);

			base.addCuboid(null, 0, 0, 0,
			textureHeight, 1, textureWidth,
			0F, 0, 0);
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
			base.render(matrices, vertexConsumer, light, overlay);
		}
	}
}
