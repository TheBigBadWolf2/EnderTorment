package white_blizz.ender_torment.intergration.top;

import com.google.common.collect.ImmutableList;
import mcjty.theoneprobe.api.*;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import white_blizz.ender_torment.common.block.IEnderFluxBlock;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxGenerator;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.Conversion;
import white_blizz.ender_torment.utils.ETUtils;
import white_blizz.ender_torment.utils.InfoUtils;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TOPHandler {
	@CapabilityInject(IEnderFluxStorage.class)
	public static Capability<IEnderFluxStorage> ENDER_FLUX = null;

	private static class EnderFluxStyle implements IReadOnlyProgressStyle {
		private int borderColor = 0xFF880088,
				backgroundColor = 0xFF000000,
				filledColor = 0xFF440044,
				altFilledColor = 0xFF880088;
		private boolean showText = true;
		private NumberFormat format = NumberFormat.COMPACT;
		private String prefix = "", suffix = " EF";
		private int width = 100, height = 10;

		private EnderFluxStyle() {}

		/*@Override public IProgressStyle borderColor(int i) { borderColor = i;return this; }
		@Override public IProgressStyle backgroundColor(int i) { backgroundColor = i;return this; }

		@Override public IProgressStyle filledColor(int i) { filledColor = i;return this; }
		@Override public IProgressStyle alternateFilledColor(int i) { altFilledColor = i;return this; }

		@Override public IProgressStyle showText(boolean b) { showText = b;return this; }

		@Override public IProgressStyle numberFormat(NumberFormat numberFormat) { format = numberFormat;return this; }

		@Override public IProgressStyle prefix(String s) { prefix = s;return this; }
		@Override public IProgressStyle suffix(String s) { suffix = s;return this; }

		@Override public IProgressStyle width(int i) { width = i;return this; }
		@Override public IProgressStyle height(int i) { height = i;return this; }

		@Override public IProgressStyle lifeBar(boolean b) { isLifeBar = b;return this; }
		@Override public IProgressStyle armorBar(boolean b) { isArmorBar = b;return this; }*/

		@Override public int getBorderColor() { return borderColor; }
		@Override public int getBackgroundColor() { return backgroundColor; }

		@Override public int getFilledColor() { return filledColor; }
		@Override public int getAlternatefilledColor() { return altFilledColor; }

		@Override public boolean isShowText() { return showText; }

		@Override public NumberFormat getNumberFormat() { return format; }

		@Override public String getPrefix() { return prefix; }
		@Override public String getSuffix() { return suffix; }

		@Override public int getWidth() { return width; }
		@Override public int getHeight() { return height; }

		@Override public boolean isLifeBar() { return false; }
		@Override public boolean isArmorBar() { return false; }
	}

	public static void displayEnderFlux(
			ProbeMode probeMode, IProbeInfo iProbeInfo,
			IEnderFluxStorage fluxStorage
	) {

		EnderFluxStyle style = new EnderFluxStyle();

		IProbeInfo v = iProbeInfo.vertical();
		v.horizontal().progress(
				fluxStorage.getEnderFluxStored(),
				fluxStorage.getMaxEnderFluxStored(),
				style
		);

		InfoUtils.parseInfo2(fluxStorage, txtStyle -> {
			switch (txtStyle) {
				case INFO: return TextStyleClass.INFO;
				case WARNING: return TextStyleClass.WARNING;
				case ERROR: return TextStyleClass.ERROR;
				case POSITIVE: return TextFormatting.GREEN;
				case NEUTRAL: return TextFormatting.BLUE;
				case NEGATIVE: return TextFormatting.RED;
				case RESET: return TextFormatting.RESET;
				default: return null;
			}
		}).forEach(v::text);

		/*if (fluxStorage.canReceiveFlux())
			v.text(String.format("%sMax Input: %s%d", TextStyleClass.INFO, TextStyleClass.OK, fluxStorage.getMaxReceive()));
		if (fluxStorage.canExtractFlux())
			v.text(String.format("%sMax Output: %s%d", TextStyleClass.INFO, TextStyleClass.OK, fluxStorage.getMaxExtract()));

		if (fluxStorage.getDecayRate() > 0)
			v.text(String.format("%sDecay: %s%f%%", TextStyleClass.WARNING, TextStyleClass.ERROR, fluxStorage.getDecayRate() * 100));

		if (fluxStorage instanceof IEnderFluxGenerator) {
			IEnderFluxGenerator generator = (IEnderFluxGenerator) fluxStorage;
			Conversion converter = generator.getConverter();
			v.text(String.format(
					"Burn Time: %d", generator.getBurnTime()
			)).text(String.format(
					"%4$s%1$d%7$s -> %5$s%2$d%7$s @ %6$s%3$d",
					converter.getModdedRatioIn(),
					converter.getModdedRatioOut(),
					converter.getModdedRate(),
					TextFormatting.RED,
					TextFormatting.GREEN,
					TextFormatting.BLUE,
					TextFormatting.RESET
			));

		}*/
	}

	public static class DataPacket<C> {
		private DataPacket(ProbeMode probeMode, IProbeInfo iProbeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData, C cap) {
			this.probe = new Probe(probeMode, iProbeInfo, iProbeHitData);
			this.world = new WorldData(playerEntity, world, blockState);
			this.cap = cap;
		}

		public static class Probe {
			public final ProbeMode mode;
			public final IProbeInfo info;
			public final IProbeHitData hitData;

			private Probe(ProbeMode probeMode, IProbeInfo iProbeInfo, IProbeHitData iProbeHitData) {
				this.mode = probeMode;
				this.info = iProbeInfo;
				this.hitData = iProbeHitData;
			}
		}

		public static class WorldData {
			public final PlayerEntity playerEntity;
			public final World world;
			public final BlockState blockState;

			private WorldData(PlayerEntity playerEntity, World world, BlockState blockState) {
				this.playerEntity = playerEntity;
				this.world = world;
				this.blockState = blockState;
			}
		}

		public final Probe probe;
		public final WorldData world;
		public final C cap;
	}

	@MethodsReturnNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class ProbeData<C> {
		private final Function<TileEntity, LazyOptional<C>> mapper;
		private final Consumer<DataPacket<C>> user;

		public ProbeData(Supplier<Capability<C>> cap, Consumer<DataPacket<C>> user) {
			this.mapper = te -> te.getCapability(cap.get());
			this.user = user;
		}

		public ProbeData(Capability<C> cap, Consumer<DataPacket<C>> user) {
			this.mapper = te -> te.getCapability(cap);
			this.user = user;
		}

		public ProbeData(Function<TileEntity, LazyOptional<C>> mapper, Consumer<DataPacket<C>> user) {
			this.mapper = mapper;
			this.user = user;
		}

		private Function<TileEntity, LazyOptional<C>> getMapper() {
			return mapper;
		}
	}

	private static final List<ProbeData<?>> datas = ImmutableList.of(
			new ProbeData<>(() -> ENDER_FLUX,
					packet -> displayEnderFlux(packet.probe.mode, packet.probe.info, packet.cap)
			)
	);

	private static class ETTileEntityInfoProvider implements IProbeInfoProvider {
		@Override public String getID() { return Ref.MOD_ID; }


		@Override
		public void addProbeInfo(
				ProbeMode probeMode, IProbeInfo iProbeInfo,
				PlayerEntity playerEntity, World world,
				BlockState blockState, IProbeHitData iProbeHitData) {
			Optional<TileEntity> te = ETUtils.getTileEntity(world, iProbeHitData.getPos());
			if (!te.isPresent()) return;

			Consumer<ProbeData<?>> dataConsumer = new Consumer<ProbeData<?>>() {
				private <T> void doData(ProbeData<T> data) {
					ETUtils.mapToLazy(te, data.getMapper()).ifPresent(
							cap -> data.user.accept(new DataPacket<>(
									probeMode, iProbeInfo,
									playerEntity, world,
									blockState, iProbeHitData,
									cap
							))
					);
				}

				@Override public void accept(ProbeData<?> data) { doData(data); }
			};
			datas.forEach(dataConsumer);
		}
	}

	private static class ETVisionImpaired implements IBlockDisplayOverride, IEntityDisplayOverride {

		private boolean isVisionImpaired(PlayerEntity player) {
			return player.isPotionActive(ETEffects.FLUX_VISION.get());
		}

		@Override
		public boolean overrideStandardInfo(
				ProbeMode probeMode, IProbeInfo iProbeInfo,
				PlayerEntity player, World world,
				BlockState blockState, IProbeHitData iProbeHitData) {
			return isVisionImpaired(player);
		}

		@Override
		public boolean overrideStandardInfo(
				ProbeMode probeMode, IProbeInfo iProbeInfo,
				PlayerEntity player, World world,
				Entity entity, IProbeHitEntityData iProbeHitEntityData) {
			return isVisionImpaired(player);
		}
	}

	@SuppressWarnings("unused")
	public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {
		@Override
		public Void apply(ITheOneProbe iTheOneProbe) {
			iTheOneProbe.registerProvider(new ETTileEntityInfoProvider());
			iTheOneProbe.registerProbeConfigProvider(new IProbeConfigProvider() {
				@Override
				public void getProbeConfig(IProbeConfig iProbeConfig,
										   PlayerEntity player,
										   World world, Entity entity,
										   IProbeHitEntityData hitEntityData) {}

				@Override
				public void getProbeConfig(IProbeConfig iProbeConfig,
										   PlayerEntity player,
										   World world, BlockState blockState,
										   IProbeHitData hitData) {
					if (blockState.getBlock() instanceof IEnderFluxBlock)
						iProbeConfig.showChestContents(IProbeConfig.ConfigMode.NORMAL)
								.showChestContentsDetailed(IProbeConfig.ConfigMode.EXTENDED);
				}
			});
			ETVisionImpaired impaired = new ETVisionImpaired();
			iTheOneProbe.registerBlockDisplayOverride(impaired);
			iTheOneProbe.registerEntityDisplayOverride(impaired);
			return null;
		}
	}
}
