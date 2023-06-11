package me.modmuss50.dg;

import me.modmuss50.dg.crafting.GlobeCraftingRecipe;
import me.modmuss50.dg.globe.GlobeBlock;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import me.modmuss50.dg.globe.GlobeBlockItem;
import me.modmuss50.dg.globe.VoidChunkGenerator;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionGlobe implements ModInitializer {

	public static final String MOD_ID = "globedimension";

	public static GlobeBlock globeBlock;
	public static GlobeBlockItem globeBlockItem;
	public static BlockEntityType<GlobeBlockEntity> globeBlockEntityType;

	public static RegistryKey<World> globeDimension;

	public static ItemGroup GLOBE_ITEM_GROUP;
	public static final TagKey<Block> BASE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "base_blocks"));

	public static final SpecialRecipeSerializer<GlobeCraftingRecipe> GLOBE_CRAFTING = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "globe_crafting"), new SpecialRecipeSerializer<>(GlobeCraftingRecipe::new));

	@Override
	public void onInitialize() {
		Identifier globeID = new Identifier(MOD_ID, "globe");

		Registry.register(Registries.BLOCK, globeID, globeBlock = new GlobeBlock());

		globeBlockItem = new GlobeBlockItem(globeBlock, new Item.Settings());
		globeBlockItem.appendBlocks(Item.BLOCK_ITEMS, globeBlockItem);
		Registry.register(Registries.ITEM, globeID, globeBlockItem);

		GLOBE_ITEM_GROUP = FabricItemGroup.builder(new Identifier("example", "test_group"))
			    .displayName(Text.of("Globes"))
			    .icon(() -> new ItemStack(globeBlockItem))
			    .entries((context, entries) -> {
			    	Iterable<RegistryEntry<Block>> entries2 = Registries.BLOCK.iterateEntries(DimensionGlobe.BASE_BLOCK_TAG);
					for (RegistryEntry<Block> block : entries2) {
						entries.add(globeBlockItem.getWithBase(block.value()));
					}
			        entries.add(globeBlockItem);
			    })
			    .build();

		globeBlockEntityType = FabricBlockEntityTypeBuilder.create(GlobeBlockEntity::new, globeBlock).build(null);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, globeID, globeBlockEntityType);

		Registry.register(Registries.CHUNK_GENERATOR, new Identifier("globedimension", "globe"), VoidChunkGenerator.CODEC);

		globeDimension = RegistryKey.of(RegistryKeys.WORLD, new Identifier("globedimension", "globe"));

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			if (!world.isClient && world.getRegistryKey().equals(World.OVERWORLD)) {
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
