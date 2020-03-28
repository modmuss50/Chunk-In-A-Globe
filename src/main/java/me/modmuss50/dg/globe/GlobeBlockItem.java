package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class GlobeBlockItem extends BlockItem {
	public GlobeBlockItem(Block block, Settings settings) {
		super(block, settings);
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isIn(group)) {
			for (Block block : DimensionGlobe.BASE_BLOCK_TAG.values()) {
				stacks.add(getWithBase(block));
			}
		}
	}

	public ItemStack getWithBase(Block base) {
		Identifier identifier = Registry.BLOCK.getId(base);
		ItemStack stack = new ItemStack(this);
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("base_block", identifier.toString());
		stack.setTag(compoundTag);

		return stack;
	}

	@Override
	protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
		if (stack.hasTag() && stack.getTag().contains("base_block")) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof GlobeBlockEntity) {
				Identifier identifier = new Identifier(stack.getTag().getString("base_block"));
				if (Registry.BLOCK.containsId(identifier)) {
					((GlobeBlockEntity) blockEntity).setBaseBlock(Registry.BLOCK.get(identifier));
				}
			}
		}
		return super.postPlacement(pos, world, player, stack, state);
	}
}
