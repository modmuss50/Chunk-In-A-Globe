package me.modmuss50.dg.globe;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GlobeBlock extends BlockWithEntity {
	public GlobeBlock() {
		super(FabricBlockSettings.of(Material.GLASS)
				.nonOpaque()
				.build());
	}

	@Override
	public BlockEntity createBlockEntity(BlockView view) {
		return new GlobeBlockEntity();
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
		return true;
	}

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
}
