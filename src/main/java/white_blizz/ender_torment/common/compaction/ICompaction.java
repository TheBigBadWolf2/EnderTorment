package white_blizz.ender_torment.common.compaction;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.INBTSerializable;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.utils.ETNBTUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public interface ICompaction extends INBTSerializable<CompoundNBT> {
	class BlockInfo {
		private final BlockState state;
		private final BlockPos pos;
		@Nullable private final CompoundNBT tag;
		private final boolean relative;

		public BlockInfo(CompoundNBT tag) {
			state = ETNBTUtil.STATE.getAs(tag);
			pos = ETNBTUtil.POS.getAs(tag);
			this.tag = ETNBTUtil.COMPOUND.tryGet(tag, "tag").orElse(null);
			relative = tag.getBoolean("relative");
		}

		protected BlockInfo(IBlockReader world, BlockPos pos) {
			this.pos = pos;
			relative = false;
			state = world.getBlockState(pos);

			/*fState = world.getFluidState(pos);
			if (fState.isEmpty()) fState = null;*/
			TileEntity te = world.getTileEntity(pos);
			if (te != null) tag = te.write(new CompoundNBT());
			else tag = null;
		}

		public BlockInfo(BlockState state, BlockPos pos, @Nullable CompoundNBT tag, boolean relative) {
			this.state = state;
			this.pos = pos;
			this.tag = tag;
			this.relative = relative;
		}

		public BlockPos getPos(BlockPos rel) {
			if (relative) return rel.toImmutable().add(pos);
			return pos;
		}
		public BlockPos getPos() { return pos; }
		public BlockState getState() { return state; }

		public boolean isValid(IBlockReader world) {
			return !state.isAir(world, pos);
		}

		public BlockInfo relativeTo(BlockPos to) {
			if (relative) throw new IllegalStateException("Already relative!");
			BlockPos pos = this.pos.subtract(to);
			CompoundNBT copy = null;
			if (tag != null) {
				copy = tag.copy();
				copy.putInt("x", pos.getX());
				copy.putInt("y", pos.getY());
				copy.putInt("z", pos.getZ());
			}
			return new BlockInfo(state, pos, copy, true);
		}

		public BlockInfo localFrom(BlockPos from) {
			if (!relative) throw new IllegalStateException(String.format("Already local @ %s!", this.pos));
			BlockPos pos = this.pos.add(from);
			CompoundNBT copy = null;
			if (tag != null) {
				copy = tag.copy();
				copy.putInt("x", pos.getX());
				copy.putInt("y", pos.getY());
				copy.putInt("z", pos.getZ());
			}
			return new BlockInfo(state, pos, copy, false);
		}

		public boolean isRelative() { return relative; }

		public void tryPlace(IWorld world, BlockPos rel, int flags) {
			if (relative) place(world, rel, flags);
			else place(world, flags);
		}

		public void place(IWorld world, int flags) {
			if (relative) throw new IllegalStateException("Cannot place as non-relative!");
			placeInternal(world, pos, flags);
		}

		public void place(IWorld world, BlockPos rel, int flags) {
			if (!relative) throw new IllegalStateException("Cannot place as relative!");
			placeInternal(world, pos.add(rel), flags);
		}

		private void placeInternal(IWorld world, BlockPos pos, int flags) {
			world.getWorld().removeTileEntity(pos); //Because it seams to stick...
			world.setBlockState(pos, state, flags);
			if (tag != null) {
				TileEntity te = world.getTileEntity(pos);
				if (te != null) {
					CompoundNBT copy = tag.copy();
					copy.putInt("x", pos.getX());
					copy.putInt("y", pos.getY());
					copy.putInt("z", pos.getZ());
					te.read(copy);
				}
				else EnderTorment.LOGGER.error("Info had tag {}, but no TileEntity was found at {}", tag, pos);
			}
		}

		public CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			ETNBTUtil.STATE.put(tag, state);
			ETNBTUtil.POS.put(tag, pos);
			if (this.tag != null) tag.put("tag", this.tag);
			tag.putBoolean("relative", relative);
			return tag;
		}

		public CompoundNBT getTag() {
			if (tag == null) return null;
			return tag.copy();
		}
	}

	UUID getID();
	BlockInfo newInfo(IBlockReader world, BlockPos pos);
	BlockInfo newInfo(CompoundNBT tag);

	void add(BlockInfo bInfo);
	void remove(BlockPos pos, BlockPos rel);
	default void remove(BlockPos pos) {
		remove(pos, BlockPos.ZERO);
	}

	Collection<BlockInfo> infoList();
	boolean canCollect();
	boolean canRelease();
}