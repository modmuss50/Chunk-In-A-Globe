package me.modmuss50.dg.utils;

import me.modmuss50.dg.globe.GlobeBlock;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobeSection {

	public static final int GLOBE_SIZE = 17;

	private final Map<BlockPos, BlockState> stateMap = new HashMap<>();
	private final Map<BlockPos, Integer> globeData = new HashMap<>();

	private final List<Entity> entities = new ArrayList<>();
	private final Map<Entity, Vec3d> entityVec3dMap = new HashMap<>();

	public Map<BlockPos, BlockState> getStateMap() {
		return stateMap;
	}

	public void buildBlockMap(World world, BlockPos origin) {
		stateMap.clear();
		final BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = 1; x < GLOBE_SIZE -1; x++) {
			for (int y = 1; y < GLOBE_SIZE -1; y++) {
				for (int z = 1; z < GLOBE_SIZE -1; z++) {
					mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
					BlockState state = world.getBlockState(mutable);
					if (!state.isAir()) {
						stateMap.put(new BlockPos(x, y, z), state);
					}
					if (state.getBlock() instanceof GlobeBlock) {
						GlobeBlockEntity globeBlockEntity = (GlobeBlockEntity) world.getBlockEntity(mutable);
						if (globeBlockEntity.getGlobeID() != -1) {
							globeData.put(new BlockPos(x, y, z), globeBlockEntity.getGlobeID());
						}
					}
				}
			}
		}
	}

	public void buildEntityList(World world, BlockPos origin) {
		entities.clear();
		entityVec3dMap.clear();
		for (Entity entity : world.getEntities(null, new Box(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + GLOBE_SIZE, origin.getY() + GLOBE_SIZE, origin.getZ() + GLOBE_SIZE))) {
			entities.add(entity);
		}
	}

	public void fromBlockTag(CompoundTag tag) {
		stateMap.clear();
		globeData.clear();
		for (String key : tag.getKeys()) {
			CompoundTag entryTag = tag.getCompound(key);
			BlockState state = NbtHelper.toBlockState(entryTag.getCompound("state"));
			BlockPos pos = NbtHelper.toBlockPos(entryTag.getCompound("pos"));
			stateMap.put(pos, state);
			if (entryTag.contains("globe_data")) {
				globeData.put(pos, entryTag.getInt("globe_data"));
			}
		}
	}

	public CompoundTag toBlockTag() {
		CompoundTag compoundTag = new CompoundTag();
		for (Map.Entry<BlockPos, BlockState> entry : stateMap.entrySet()) {
			BlockState state = entry.getValue();
			CompoundTag entryTag = new CompoundTag();

			entryTag.put("state", NbtHelper.fromBlockState(state));
			entryTag.put("pos", NbtHelper.fromBlockPos(entry.getKey()));

			if (globeData.containsKey(entry.getKey())) {
				entryTag.putInt("globe_data", globeData.get(entry.getKey()));
			}

			compoundTag.put("entry_" + entry.getKey().toString(), entryTag);
		}
		return compoundTag;
	}

	public void fromEntityTag(CompoundTag tag, World world) {
		entities.clear();
		entityVec3dMap.clear();
		for (String uuid : tag.getKeys()) {
			CompoundTag entityData = tag.getCompound(uuid);
			Identifier entityType = new Identifier(entityData.getString("entity_type"));

			if (entityType.toString().equals("minecraft:player")) {

				OtherClientPlayerEntity entity = new OtherClientPlayerEntity((ClientWorld) world, MinecraftClient.getInstance().getSession().getProfile());
				entity.fromTag(entityData.getCompound("entity_data"));


				entities.add(entity);
				Vec3d pos = new Vec3d(entityData.getDouble("entity_x"), entityData.getDouble("entity_y"), entityData.getDouble("entity_z"));
				entityVec3dMap.put(entity, pos);
				continue;
			}

			if (Registry.ENTITY_TYPE.getOrEmpty(entityType).isPresent()) {
				EntityType<?> type = Registry.ENTITY_TYPE.get(entityType);
				Entity entity = type.create(world);

				if (entity == null) {
					System.out.println("Failed to create " + entityType);
					continue;
				}

				entity.fromTag(entityData.getCompound("entity_data"));
				entity.setPos(0, 0, 0);

				entities.add(entity);

				Vec3d pos = new Vec3d(entityData.getDouble("entity_x"), entityData.getDouble("entity_y"), entityData.getDouble("entity_z"));
				entityVec3dMap.put(entity, pos);
			}
		}
	}

	public CompoundTag toEntityTag(BlockPos origin) {
		CompoundTag compoundTag = new CompoundTag();
		for (Entity entity : entities) {
			CompoundTag entityTag = new CompoundTag();
			Identifier entityType = Registry.ENTITY_TYPE.getId(entity.getType());
			entityTag.putString("entity_type", entityType.toString());
			CompoundTag entityData = new CompoundTag();
			entity.toTag(entityData);

			entityData.remove("Passengers");

			entityTag.put("entity_data", entityData);

			Vec3d relativePos = entity.getPos().subtract(new Vec3d(origin));
			entityTag.putDouble("entity_x", relativePos.getX());
			entityTag.putDouble("entity_y", relativePos.getY());
			entityTag.putDouble("entity_z", relativePos.getZ());

			compoundTag.put(entity.getUuidAsString(), entityTag);
		}
		return compoundTag;
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public Map<Entity, Vec3d> getEntityVec3dMap() {
		return entityVec3dMap;
	}

	public Map<BlockPos, Integer> getGlobeData() {
		return globeData;
	}
}
