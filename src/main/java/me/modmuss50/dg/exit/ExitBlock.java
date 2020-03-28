package me.modmuss50.dg.exit;

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

public class ExitBlock extends BlockWithEntity {
	public ExitBlock() {
		super(FabricBlockSettings.of(Material.METAL)
				.build());
	}

	@Override
	public BlockEntity createBlockEntity(BlockView view) {
		return new ExitBlockEntity();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof ExitBlockEntity) {
				((ExitBlockEntity) blockEntity).transportPlayer((ServerPlayerEntity) player);
			}
		}
		return ActionResult.SUCCESS;
	}
}
