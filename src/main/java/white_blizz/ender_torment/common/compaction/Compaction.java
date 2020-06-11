package white_blizz.ender_torment.common.compaction;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Constants;
import white_blizz.ender_torment.utils.ETNBTUtil;

import java.util.*;

public class Compaction implements ICompaction {
	private UUID id;
	private final Map<BlockPos, BlockInfo> map = new HashMap<>();

	public Compaction(UUID id) {
		this.id = id;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		ETNBTUtil.UUID.put(tag, id);
		tag.put("map", ETNBTUtil.serializeMap(map, NBTUtil::writeBlockPos, BlockInfo::serializeNBT));
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT tag) {
		ETNBTUtil.UUID.tryRunAs2(tag, id -> this.id = id);
		ETNBTUtil.deserializeMapCC(tag.getList("map", Constants.NBT.TAG_COMPOUND),
				map,
				NBTUtil::readBlockPos,
				this::newInfo
		);
	}

	@Override
	public UUID getID() {
		return id;
	}

	@Override
	public BlockInfo newInfo(IBlockReader world, BlockPos pos) {
		return new BlockInfo(world, pos);
	}

	@Override
	public BlockInfo newInfo(CompoundNBT tag) {
		return new BlockInfo(tag);
	}

	@Override
	public void add(BlockInfo bInfo) {
		map.put(bInfo.getPos(), bInfo);
	}

	@Override
	public void remove(BlockPos pos, BlockPos rel) {
		map.remove(pos.toImmutable().subtract(rel));
	}

	/*@Override
	public void remove(BlockInfo bInfo) {
		map.remove(bInfo.getPos(), bInfo);
	}*/

	@Override
	public Collection<BlockInfo> infoList() {
		return map.values();
	}

	@Override
	public boolean canCollect() {
		return map.isEmpty();
	}

	@Override
	public boolean canRelease() {
		return !map.isEmpty();
	}
}
