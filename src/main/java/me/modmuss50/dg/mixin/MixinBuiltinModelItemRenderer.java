package me.modmuss50.dg.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(BuiltinModelItemRenderer.class)
public class MixinBuiltinModelItemRenderer {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, CallbackInfo info) {
		if (stack.getItem() instanceof BlockItem) {
			Block block = ((BlockItem) stack.getItem()).getBlock();
			if (block == DimensionGlobe.globeBlock) {
				GlobeItemRenderer.render(stack, matrix, vertexConsumerProvider, light, overlay);
				info.cancel();
			}
		}
	}
}
