package me.modmuss50.dg.globe;

import me.modmuss50.dg.DimensionGlobe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GlobeBlockItem extends BlockItem {
	public GlobeBlockItem(Block block, Settings settings) {
		super(block, settings);
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isIn(group)) {
			Iterable<RegistryEntry<Block>> entries = Registries.BLOCK.iterateEntries(DimensionGlobe.BASE_BLOCK_TAG);
			for (RegistryEntry<Block> block : entries) {
				stacks.add(getWithBase(block.value()));
			}
		}
	}

	public ItemStack getWithBase(Block base) {
		Identifier identifier = Registries.BLOCK.getId(base);
		ItemStack stack = new ItemStack(this);
		NbtCompound compoundTag = new NbtCompound();
		compoundTag.putString("base_block", identifier.toString());
		stack.setNbt(compoundTag);

		return stack;
	}

	@Override
	public ActionResult place(ItemPlacementContext context) {
		if (context.getPlayer().world.getRegistryKey().equals(DimensionGlobe.globeDimension)) {
			if (!context.getPlayer().world.isClient) {
				context.getPlayer().sendMessage(Text.translatable("globedimension.block.error"), false);
			}
			return ActionResult.FAIL;
		}
		return super.place(context);
	}

	@Override
	protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
		if (stack.hasNbt() && stack.getNbt().contains("base_block")) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof GlobeBlockEntity) {
				Identifier identifier = new Identifier(stack.getNbt().getString("base_block"));
				if (Registries.BLOCK.getOrEmpty(identifier).isPresent()) {
					((GlobeBlockEntity) blockEntity).setBaseBlock(Registries.BLOCK.get(identifier));
				}
				if (stack.getNbt().contains("globe_id")) {
					((GlobeBlockEntity) blockEntity).setGlobeID(stack.getNbt().getInt("globe_id"));
				}
			}
		}
		return super.postPlacement(pos, world, player, stack, state);
	}
}
