package me.modmuss50.dg;

import me.modmuss50.dg.dim.GlobeDimension;
import me.modmuss50.dg.dim.GlobeDimensionChunkGenerator;
import me.modmuss50.dg.dim.GlobeDimensionPlacer;
import me.modmuss50.dg.exit.ExitBlock;
import me.modmuss50.dg.exit.ExitBlockEntity;
import me.modmuss50.dg.globe.GlobeBlock;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import me.modmuss50.dg.utils.GlobeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensionType;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;

public class DimensionGlobe implements ModInitializer {

	public static final String MOD_ID = "globedimension";

	public static GlobeBlock globeBlock;
	public static BlockEntityType<GlobeBlockEntity> globeBlockEntityType;

	public static ExitBlock exitBlock;
	public static BlockEntityType<ExitBlockEntity> exitBlockEntityType;

	public static FabricDimensionType globeDimension;
	public static ChunkGeneratorType<ChunkGeneratorConfig, GlobeDimensionChunkGenerator> globeChunkGenerator;

	@Override
	public void onInitialize() {
		Identifier globeID = new Identifier(MOD_ID, "globe");
		Identifier exitID = new Identifier(MOD_ID, "exit");
		
		Registry.register(Registry.BLOCK, globeID, globeBlock = new GlobeBlock());
		Registry.register(Registry.BLOCK, exitID, exitBlock = new ExitBlock());

		BlockItem globeBlockItem = new BlockItem(globeBlock, new Item.Settings().group(ItemGroup.TRANSPORTATION));
		globeBlockItem.appendBlocks(Item.BLOCK_ITEMS, globeBlockItem);
		Registry.register(Registry.ITEM, globeID, globeBlockItem);

		BlockItem exitBlockItem = new BlockItem(exitBlock, new Item.Settings().group(ItemGroup.TRANSPORTATION));
		exitBlockItem.appendBlocks(Item.BLOCK_ITEMS, exitBlockItem);
		Registry.register(Registry.ITEM, exitID, exitBlockItem);

		globeBlockEntityType = BlockEntityType.Builder.create(GlobeBlockEntity::new, globeBlock).build(null);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, globeID, globeBlockEntityType);

		exitBlockEntityType = BlockEntityType.Builder.create(ExitBlockEntity::new, exitBlock).build(null);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, exitID, exitBlockEntityType);

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
	}
}
