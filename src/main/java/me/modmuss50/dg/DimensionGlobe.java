package me.modmuss50.dg;

import me.modmuss50.dg.crafting.GlobeCraftingRecipe;
import me.modmuss50.dg.dim.GlobeDimension;
import me.modmuss50.dg.dim.GlobeDimensionChunkGenerator;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.globe.GlobeBlock;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import me.modmuss50.dg.globe.GlobeBlockItem;
import me.modmuss50.dg.utils.GlobeManager;
import me.modmuss50.dg.utils.GlobeSectionManagerServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensionType;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;

public class DimensionGlobe implements ModInitializer {

	public static final String MOD_ID = "globedimension";

	public static GlobeBlock globeBlock;
	public static GlobeBlockItem globeBlockItem;
	public static BlockEntityType<GlobeBlockEntity> globeBlockEntityType;

	public static FabricDimensionType globeDimension;
	public static ChunkGeneratorType<ChunkGeneratorConfig, GlobeDimensionChunkGenerator> globeChunkGenerator;

	public static final ItemGroup GLOBE_ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "globes"), () -> globeBlockItem.getWithBase(Blocks.OAK_PLANKS));
	public static final Tag<Block> BASE_BLOCK_TAG = TagRegistry.block(new Identifier(MOD_ID, "base_blocks"));

	public static final SpecialRecipeSerializer<GlobeCraftingRecipe> GLOBE_CRAFTING = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "globe_crafting"), new SpecialRecipeSerializer<>(GlobeCraftingRecipe::new));

	@Override
	public void onInitialize() {
		Identifier globeID = new Identifier(MOD_ID, "globe");
		Identifier exitID = new Identifier(MOD_ID, "exit");
		
		Registry.register(Registry.BLOCK, globeID, globeBlock = new GlobeBlock());

		globeBlockItem = new GlobeBlockItem(globeBlock, new Item.Settings().group(GLOBE_ITEM_GROUP));
		globeBlockItem.appendBlocks(Item.BLOCK_ITEMS, globeBlockItem);
		Registry.register(Registry.ITEM, globeID, globeBlockItem);

		globeBlockEntityType = BlockEntityType.Builder.create(GlobeBlockEntity::new, globeBlock).build(null);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, globeID, globeBlockEntityType);

		globeDimension = FabricDimensionType.builder()
				.factory(GlobeDimension::new)
				.defaultPlacer(new GlobeDimensionPlacer())
				.buildAndRegister(globeID);

		globeChunkGenerator = FabricChunkGeneratorType.register(globeID, GlobeDimensionChunkGenerator::new, ChunkGeneratorConfig::new, false);

		WorldTickCallback.EVENT.register(world -> {
			if (!world.isClient && world.getDimension().getType() == DimensionType.OVERWORLD) {
				GlobeManager.getInstance((ServerWorld) world).tick();
			}
		});

		GlobeSectionManagerServer.register();
	}
}
