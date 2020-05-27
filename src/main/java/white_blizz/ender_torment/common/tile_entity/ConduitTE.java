package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import white_blizz.ender_torment.common.conduit.*;
import white_blizz.ender_torment.common.block.ETBlocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConduitTE extends ETTileEntity implements ILinkable {

	public ConduitTE() { super(ETBlocks.CONDUIT_TYPE); }

	private final BlockMap links = new BlockMap();

	private class ALink<C> extends Link<C> {
		protected ALink(DimensionType dim, BlockPos pos, ConduitType<C> type, boolean isClient) {
			super(dim, pos, type, isClient);
		}
		@Override public boolean hasInput() { return false; }
		@Override public boolean hasOutput() { return false; }
		@Override protected boolean canConnectTo(Direction dir) { return true; }

		@Override protected CompoundNBT serializeExtraNBT(CompoundNBT tag) { return tag; }
		@Override protected void deserializeExtraNBT(CompoundNBT tag) { }

		@Override protected void markDirty() { ConduitTE.this.markDirty(); }
	}

	@Override
	public boolean addType(ConduitType<?> type) {
		World world = getWorld();
		if (world == null) return false;
		if (links.containsKey(type)) return false;

		Link<?> link = new ALink<>(world.dimension.getType(), getPos(), type, world.isRemote);
		links.add(link);
		Network.addLink(link);
		link.updateNetwork();
		markDirty();
		return true;
	}

	@Override
	public boolean removeType(ConduitType<?> type) {
		World world = getWorld();
		if (world == null) return false;
		Link<?> link = links.get(type);
		if (link != null) {
			links.remove(link);
			link.remove(true);
			markDirty();
			return true;
		}
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		links.clear(true, true);
	}



	@Nullable @Override
	public <C> Link<C> getLink(ConduitType<C> type) {
		return links.get(type);
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public Map<ConduitType<?>, Link<?>> getLinks() {
		return ImmutableMap.copyOf(links);
	}

	@Override protected void extraRead(CompoundNBT tag) {
		links.clear(true, false);
		boolean isClient;
		if (world != null) isClient = world.isRemote();
		else isClient = EffectiveSide.get().isClient();
		ListNBT list = tag.getList("links", Constants.NBT.TAG_COMPOUND);
		list.forEach((nbt) -> {
			ALink<?> link = Link.deserialize((CompoundNBT) nbt, ALink::new, isClient);
			Network.addLinkLazy(link);
			links.add(link);
		});
	}
	@Override protected void extraWrite(CompoundNBT tag) {
		ListNBT list = new ListNBT();
		for (BlockMap.TypeLink<?> link : links) {
			list.add(link.getValue().serializeNBT());
		}
		tag.put("links", list);
	}

	@Override protected List<Cap<?>> getCaps() { return Lists.newArrayList(); }

	/*public Collection<Link> getLinks() {
		return links.values();
	}*/
}
