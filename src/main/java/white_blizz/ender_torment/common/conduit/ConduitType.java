package white_blizz.ender_torment.common.conduit;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.conduit.io.IConduitBuffer;
import white_blizz.ender_torment.common.conduit.io.IConduitIO;
import white_blizz.ender_torment.common.conduit.io.IConduitInput;
import white_blizz.ender_torment.common.conduit.io.IConduitOutput;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConduitType<Cap> extends ForgeRegistryEntry<ConduitType<?>> {
	@Nullable private final TransferHandler<Cap> transfer;
	@Nullable private final WrapHandler<Cap> wrapper;

	public final int color;
	public final Class<Cap> type;
	protected final Supplier<Capability<Cap>> capSupplier;
	@Nullable private String translationKey;

	public <T extends TransferHandler<Cap> & WrapHandler<Cap>> ConduitType(
			int color, Class<Cap> type,
			Supplier<Capability<Cap>> capSupplier,
			@Nullable T handler) {
		this(color, type, capSupplier, handler, handler);
	}
	public ConduitType(int color, Class<Cap> type,
					   Supplier<Capability<Cap>> capSupplier,
					   @Nullable TransferHandler<Cap> transfer, @Nullable WrapHandler<Cap> wrapper) {
		this.color = color;
		this.type = type;
		this.capSupplier = capSupplier;
		this.transfer = transfer;
		this.wrapper = wrapper;
	}

	public ConduitType<Cap> setRegName(String name) {
		setRegistryName(name);
		return this;
	}

	public Capability<Cap> getCap() { return capSupplier.get(); }

	public LazyOptional<? extends Cap> getCap(TileEntity te, @Nullable Direction dir) {
		LazyOptional<? extends Cap> opt = te.getCapability(getCap(), dir);
		if (opt.isPresent()) return opt;
		if (wrapper != null) return wrapper.getCap(te, dir);
		return LazyOptional.empty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConduitType<?> that = (ConduitType<?>) o;
		return type.equals(that.type) &&
				Objects.equals(getRegistryName(), that.getRegistryName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, getRegistryName());
	}

	@Override
	public String toString() {
		ResourceLocation registryName = getRegistryName();
		return registryName != null ? registryName.toString()
				: String.format("Unregistered Conduit Type of \"%s\"", type);
	}

	protected String getDefaultTranslationKey() {
		if (this.translationKey == null) {
			this.translationKey = Util.makeTranslationKey("conduit", ETRegistry.CONDUIT_TYPE.getKey(this));
		}

		return this.translationKey;
	}

	public void tick(List<IConduitIO<Cap>> ios) {
		List<IConduitInput<Cap>> ins = new ArrayList<>();
		List<IConduitBuffer<Cap>> buffs = new ArrayList<>();
		List<IConduitOutput<Cap>> outs = new ArrayList<>();
		ios.forEach(io -> {
			io.asInput().ifPresent(ins::add);
			io.asBuffer().ifPresent(buffs::add);
			io.asOutput().ifPresent(outs::add);
		});
		tick(ins, buffs, outs);
	}

	public void tick(List<IConduitInput<Cap>> ins,
					 List<IConduitBuffer<Cap>> buffs,
					 List<IConduitOutput<Cap>> outs) {
		if (transfer != null) transfer.handle(ins, buffs, outs);
	}

	/**
	 * Returns the unlocalized name of this conduit.
	 */
	public String getTranslationKey() {
		return this.getDefaultTranslationKey();
	}
}
