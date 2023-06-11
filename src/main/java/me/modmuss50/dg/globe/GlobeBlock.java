package me.modmuss50.dg.globe;

import java.util.Collections;
import java.util.List;

import me.modmuss50.dg.DimensionGlobe;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GlobeBlock extends BlockWithEntity {
	public GlobeBlock() {
		super(FabricBlockSettings.of(Material.GLASS).nonOpaque());
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GlobeBlockEntity(pos, state);
	}

	//@Override
	//public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
	//	return true;
	//}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof GlobeBlockEntity) {
				((GlobeBlockEntity) blockEntity).transportPlayer((ServerPlayerEntity) player);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		dropStack(world, pos, getDroppedStack(world, pos));
		super.onBreak(world, pos, state, player);
	}

	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return getDroppedStack(world, pos);
	}

	private ItemStack getDroppedStack(BlockView world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof GlobeBlockEntity) {
			ItemStack stack = DimensionGlobe.globeBlockItem.getWithBase(((GlobeBlockEntity) blockEntity).getBaseBlock());
			stack.getNbt().putInt("globe_id", ((GlobeBlockEntity) blockEntity).getGlobeID());
			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
	    return checkType(type, DimensionGlobe.globeBlockEntityType, (world1, pos, state1, be) -> GlobeBlockEntity.tick(world1, pos, state1, be));
	}
}
