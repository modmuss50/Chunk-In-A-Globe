package me.modmuss50.dg.globe;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GlobeItemRenderer {

	public static void render(ItemStack stack, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
		Block baseBlock = Blocks.BLUE_CONCRETE;
		if (stack.hasTag() && stack.getTag().contains("base_block")) {
			Identifier blockID = new Identifier(stack.getTag().getString("base_block"));
			if (Registry.BLOCK.containsId(blockID)) {
				baseBlock = Registry.BLOCK.get(blockID);
			}
		}
		GlobeBlockEntityRenderer.renderBase(baseBlock, matrix, vertexConsumerProvider, light, overlay);
	}

}
