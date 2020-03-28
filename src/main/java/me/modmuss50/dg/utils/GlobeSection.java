package me.modmuss50.dg.utils;

import me.modmuss50.dg.globe.GlobeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class GlobeSection {

	public static final int GLOBE_SIZE = 17;

	private final Map<BlockPos, BlockState> stateMap = new HashMap<>();

	public Map<BlockPos, BlockState> getStateMap() {
		return stateMap;
	}

	public void build(World world, BlockPos origin) {
		stateMap.clear();
		final BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = 1; x < GLOBE_SIZE -1; x++) {
			for (int y = 1; y < GLOBE_SIZE-1; y++) {
				for (int z = 1; z < GLOBE_SIZE-1; z++) {
					mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
					BlockState state = world.getBlockState(mutable);
					if (!state.isAir() && !(state.getBlock() instanceof GlobeBlock)) {
						stateMap.put(new BlockPos(x, y, z), state);
					}
				}
			}
		}
	}

	public void fromTag(CompoundTag tag) {
		stateMap.clear();
		for (String key : tag.getKeys()) {
			CompoundTag entryTag = tag.getCompound(key);
			BlockState state = NbtHelper.toBlockState(entryTag.getCompound("state"));
			BlockPos pos = NbtHelper.toBlockPos(entryTag.getCompound("pos"));
			stateMap.put(pos, state);
		}
	}

	public CompoundTag toTag() {
		CompoundTag compoundTag = new CompoundTag();
		for (Map.Entry<BlockPos, BlockState> entry : stateMap.entrySet()) {
			BlockState state = entry.getValue();
			CompoundTag entryTag = new CompoundTag();

			entryTag.put("state", NbtHelper.fromBlockState(state));
			entryTag.put("pos", NbtHelper.fromBlockPos(entry.getKey()));

			compoundTag.put("entry_" + entry.getKey().toShortString(), entryTag);
		}
		return compoundTag;
	}

}
