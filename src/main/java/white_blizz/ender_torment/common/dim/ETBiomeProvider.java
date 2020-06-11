package white_blizz.ender_torment.common.dim;

import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.IBiomeProviderSettings;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.storage.WorldInfo;

import java.util.Set;

@MethodsReturnNonnullByDefault
public class ETBiomeProvider extends BiomeProvider {
	private final Biome biome;

	public ETBiomeProvider(Settings settings) {
		super(Sets.newHashSet(settings.biome));
		this.biome = settings.biome;
	}

	@Override
	public Biome getNoiseBiome(int x, int y, int z) {
		return biome;
	}

	public static class Settings implements IBiomeProviderSettings {
		private Biome biome = ETBiomes.DARK.get();
		public Settings(WorldInfo info) {}

		public Settings setBiome(Biome biome) {
			this.biome = biome;
			return this;
		}
	}
}
