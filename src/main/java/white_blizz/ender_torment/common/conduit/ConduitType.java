package white_blizz.ender_torment.common.conduit;

import com.google.common.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.*;
import white_blizz.ender_torment.common.ETRegistry;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class ConduitType<Cap> extends ForgeRegistryEntry<ConduitType<?>> {
	public final int color;
	public final Class<Cap> type;
	protected final Supplier<Capability<Cap>> capSupplier;
	@Nullable private String translationKey;

	public ConduitType(int color, Class<Cap> type, Supplier<Capability<Cap>> capSupplier, String name) {
		this.color = color;
		this.type = type;
		this.capSupplier = capSupplier;
		setRegistryName(name);
	}

	public Capability<Cap> getCap() { return capSupplier.get(); }

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

	/**
	 * Returns the unlocalized name of this conduit.
	 */
	public String getTranslationKey() {
		return this.getDefaultTranslationKey();
	}
}
