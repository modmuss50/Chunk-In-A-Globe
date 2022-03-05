package me.modmuss50.dg;

import me.modmuss50.dg.crafting.GlobeCraftingRecipe;
import me.modmuss50.dg.globe.GlobeBlock;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import me.modmuss50.dg.globe.GlobeBlockItem;
import me.modmuss50.dg.globe.VoidChunkGenerator;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@SuppressWarnings("deprecation")
public class DimensionGlobe implements ModInitializer {

	public static final String MOD_ID = "globedimension";

	public static GlobeBlock globeBlock;
	public static GlobeBlockItem globeBlockItem;
	public static BlockEntityType<GlobeBlockEntity> globeBlockEntityType;

	public static RegistryKey<World> globeDimension;

	public static final ItemGroup GLOBE_ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "globes"), () -> globeBlockItem.getWithBase(Blocks.OAK_PLANKS));
	public static final Tag<Block> BASE_BLOCK_TAG = TagRegistry.block(new Identifier(MOD_ID, "base_blocks"));

	public static final SpecialRecipeSerializer<GlobeCraftingRecipe> GLOBE_CRAFTING = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "globe_crafting"), new SpecialRecipeSerializer<>(GlobeCraftingRecipe::new));

	@Override
	public void onInitialize() {
		Identifier globeID = new Identifier(MOD_ID, "globe");

		Registry.register(Registry.BLOCK, globeID, globeBlock = new GlobeBlock());

		globeBlockItem = new GlobeBlockItem(globeBlock, new Item.Settings().group(GLOBE_ITEM_GROUP));
		globeBlockItem.appendBlocks(Item.BLOCK_ITEMS, globeBlockItem);
		Registry.register(Registry.ITEM, globeID, globeBlockItem);

		globeBlockEntityType = BlockEntityType.Builder.create(GlobeBlockEntity::new, globeBlock).build(null);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, globeID, globeBlockEntityType);

		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("globedimension", "globe"), VoidChunkGenerator.CODEC);

		globeDimension = RegistryKey.of(Registry.WORLD_KEY, new Identifier("globedimension", "globe"));

		WorldTickCallback.EVENT.register(world -> {
			if (!world.isClient && world.getRegistryKey().equals(DimensionType.OVERWORLD_REGISTRY_KEY)) {
				GlobeManager.getInstance((ServerWorld) world).tick();
			}
		});

		AttackBlockCallback.EVENT.register((playerEntity, world, hand, blockPos, direction) -> {
			if (world.getRegistryKey() == globeDimension) {
				BlockState state = world.getBlockState(blockPos);
				if (state.getBlock() == globeBlock || state.getBlock() == Blocks.BARRIER) {
					return ActionResult.FAIL;
				}
			}
			return ActionResult.PASS;
		});

		UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
			if (world.getRegistryKey() == globeDimension) {
				ItemStack stack = playerEntity.getStackInHand(hand);
				if (stack.getItem() == Items.BARRIER) {
					return ActionResult.FAIL;
				}
			}
			return ActionResult.PASS;
		});

		GlobeSectionManagerServer.register();
	}
}
