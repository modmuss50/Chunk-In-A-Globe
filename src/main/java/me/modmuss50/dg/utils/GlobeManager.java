package me.modmuss50.dg.utils;

import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.modmuss50.dg.DimensionGlobe;
import me.modmuss50.dg.globe.GlobeBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class GlobeManager extends PersistentState {

	private static final String SAVE_KEY = DimensionGlobe.MOD_ID + "_globes";
	private static long KEEP_ALIVE_TIME = 20 * 10; // 10 seconds

	public static GlobeManager getInstance(ServerWorld world) {
		if (world.getRegistryKey() != World.OVERWORLD) {
			world = world.getServer().getWorld(World.OVERWORLD);
		}
		final ServerWorld serverWorld = world;
		return serverWorld.getPersistentStateManager()
				.getOrCreate((NbtCompound tag) -> GlobeManager.fromNbt(new GlobeManager(serverWorld), tag), () -> new GlobeManager(serverWorld), SAVE_KEY);
	}

	private Int2ObjectMap<Globe> globes = new Int2ObjectArrayMap<>();
	private Int2LongMap tickingGlobes = new Int2LongArrayMap();

	private final ServerWorld world;

	public GlobeManager(ServerWorld world) {
		super();
		this.world = world;
	}

	public Globe getNextGlobe() {
		final Globe globe = new Globe(globes.size());
		globes.put(globe.id, globe);
		return globe;
	}

	public Globe getGlobeByID(int id) {
		if (!globes.containsKey(id)) {
			throw new RuntimeException("Could not find globe with id: " + id);
		}
		return globes.get(id);
	}

	@SuppressWarnings("deprecation")
	public void tick() {
		final long currentTime = world.getTime();
		final IntList destoryQueue = new IntArrayList();
		for (Int2LongMap.Entry entry : tickingGlobes.int2LongEntrySet()) {
			if (currentTime - entry.getLongValue() > KEEP_ALIVE_TIME) {
				destoryQueue.add(entry.getIntKey());
			}
		}
		for (Integer forRemoval : destoryQueue) {
			unloadGlobe(forRemoval);
			tickingGlobes.remove(forRemoval);
		}
	}

	public void markGlobeForTicking(int id) {
		if (!tickingGlobes.containsKey(id)) {
			chunkLoadGlobe(id);
		}
		tickingGlobes.put(id, world.getTime());
	}

	private void chunkLoadGlobe(int id) {
		ChunkPos chunk = getGlobeByID(id).getChunkPos();
		getGlobeWorld().setChunkForced(chunk.x, chunk.z, true);
	}

	private void unloadGlobe(int id) {
		ChunkPos chunk = getGlobeByID(id).getChunkPos();
		getGlobeWorld().setChunkForced(chunk.x, chunk.z, false);
	}

	@Override
	public boolean isDirty() {
		//Always save please
		return true;
	}

	private ServerWorld getGlobeWorld() {
		return world.getServer().getWorld(DimensionGlobe.globeDimension);
	}

	public static GlobeManager fromNbt(GlobeManager manager, NbtCompound tag) {
		manager.globes.clear();
		NbtCompound globesTag = tag.getCompound("globes");
		for (String key : globesTag.getKeys()) {
			int keyID = Integer.parseInt(key);
			manager.globes.put(keyID, new Globe(keyID, globesTag.getCompound(key)));
		}

		NbtCompound tickingGlobesTag = tag.getCompound("ticking_globes");
		for (String key : tickingGlobesTag.getKeys()) {
			int keyID = Integer.parseInt(key);
			manager.tickingGlobes.put(keyID, manager.world.getTime());
		}
		return manager;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		NbtCompound globesTag = new NbtCompound();
		for (Int2ObjectMap.Entry<Globe> entry : globes.int2ObjectEntrySet()) {
			globesTag.put(entry.getIntKey() + "", entry.getValue().toTag());
		}
		tag.put("globes", globesTag);

		NbtCompound tickingGlobesTag = new NbtCompound();
		for (Int2LongMap.Entry entry : tickingGlobes.int2LongEntrySet()) {
			tickingGlobesTag.putBoolean(entry.getIntKey() + "", true);
		}
		tag.put("ticking_globes", tickingGlobesTag);
		return tag;
	}

	public static class Globe {
		private final int id;
		private GlobeSection globeSection = null;
		private GlobeSection innerGlobeSection = null;

		public Globe(int id) {
			this.id = id;
		}

		public Globe(int id, NbtCompound compoundTag) {
			this(id);
			fromTag(compoundTag);
		}

		public ChunkPos getChunkPos() {
			return new ChunkPos(0, id * 100);
		}

		public BlockPos getGlobeLocation() {
			BlockPos chunkPos = getChunkPos().getStartPos();
			return new BlockPos(chunkPos.getX(), 128, chunkPos.getZ());
		}

		public void fromTag(NbtCompound tag) {

		}

		public NbtCompound toTag() {
			return new NbtCompound();
		}

		public void updateBlockSection(ServerWorld world, boolean inner, GlobeBlockEntity blockEntity) {
			if (inner) {
				if (innerGlobeSection == null) {
					innerGlobeSection = new GlobeSection();
				}
				innerGlobeSection.buildBlockMap(world, blockEntity.getInnerScanPos());
			} else {
				if (globeSection == null) {
					globeSection = new GlobeSection();
				}
				globeSection.buildBlockMap(world, getGlobeLocation());
			}

		}

		public void updateEntitySection(ServerWorld world, boolean inner, GlobeBlockEntity blockEntity) {
			if (inner) {
				if (innerGlobeSection == null) {
					innerGlobeSection = new GlobeSection();
				}
				innerGlobeSection.buildEntityList(world, blockEntity.getInnerScanPos());
			} else {
				if (globeSection == null) {
					globeSection = new GlobeSection();
				}
				globeSection.buildEntityList(world, getGlobeLocation());
			}

		}

		public GlobeSection getGlobeSection(boolean inner) {
			if (inner) {
				return innerGlobeSection;
			}
			return globeSection;
		}

		public int getId() {
			return id;
		}
	}
}
