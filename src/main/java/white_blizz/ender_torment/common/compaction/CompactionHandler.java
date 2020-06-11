package white_blizz.ender_torment.common.compaction;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@Mod.EventBusSubscriber
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompactionHandler {
	@CapabilityInject(ICompaction.class)
	public static Capability<ICompaction> COMPACTION;
	@CapabilityInject(IWorldCompaction.class)
	public static Capability<IWorldCompaction> WORLD_COMPACTION;

	public static class WorldCompactor extends WorldCompaction implements ICapabilitySerializable<ListNBT> {

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			return WORLD_COMPACTION.orEmpty(cap, LazyOptional.of(() -> this));
		}
	}



	@SubscribeEvent
	public static void attachWorldCap(AttachCapabilitiesEvent<World> evt) {
		if (evt.getObject().getDimension().getType() == DimensionType.OVERWORLD)
			evt.addCapability(
					Ref.MOD.rl.loc("compaction"),
					new WorldCompactor()
			);
	}
}
