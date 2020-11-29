package me.modmuss50.dg.mixin;

import me.modmuss50.dg.DimensionGlobe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {

	@Shadow public World world;

	@Inject(method = "canExplosionDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void canExplosionDestroyBlock(Explosion explosion, BlockView blockView, BlockPos pos, BlockState state, float explosionPower, CallbackInfoReturnable<Boolean> infoReturnable) {
		if (world.getRegistryKey() == DimensionGlobe.globeDimension && state.getBlock() == DimensionGlobe.globeBlock) {
			infoReturnable.setReturnValue(false);
		}
	}
}
