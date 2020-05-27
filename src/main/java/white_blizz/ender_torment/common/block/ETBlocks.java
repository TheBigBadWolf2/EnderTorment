package white_blizz.ender_torment.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import white_blizz.ender_torment.common.item.ETBlockItem;
import white_blizz.ender_torment.common.item.ETEnchantableBlockItem;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.tile_entity.*;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ETBlocks extends ETDeferredRegisterHandler {
	//private static final ItemReg ITEMS = New(ForgeRegistries.ITEMS, ItemReg::new);
	private static final BlockReg BLOCKS = New(ForgeRegistries.BLOCKS, (reg, mod_id) -> new BlockReg(reg, mod_id, () -> ETItems.ITEMS));

	private static final TEReg TILE_ENTITIES = New(ForgeRegistries.TILE_ENTITIES, TEReg::new);

	public static final class BlockItemObject<TBlock extends Block & IETBlock, TItem extends Item> extends ETRegistryObject<TBlock> {
		private final RegistryObject<TItem> item;

		private BlockItemObject(RegistryObject<TBlock> block, RegistryObject<TItem> item) {
			super(block);
			this.item = item;
		}
		public RegistryObject<TItem> getItem() { return item; }
	}

	private static final class BlockReg extends ETDeferredRegister<Block> {
		private final Supplier<DeferredRegister<Item>> items;
		protected BlockReg(IForgeRegistry<Block> reg, String mod_id, Supplier<DeferredRegister<Item>> items) {
			super(reg, mod_id);
			this.items = items;
		}

		private <TBlock extends Block & IETBlock, TItem extends Item> BlockItemObject<TBlock, TItem> reg(
				String name,
				Supplier<TBlock> blockFactory,
				BiFunction<Supplier<? extends Block>, Item.Properties, TItem> itemFactory,
				Supplier<Item.Properties> propFactory
		) {
			RegistryObject<TBlock> block = register(name, blockFactory);
			RegistryObject<TItem> item = items.get().register(name, () -> itemFactory.apply(block, propFactory.get()));
			return new BlockItemObject<>(block, item);
		}
	}

	public static final class TEReg extends ETDeferredRegister<TileEntityType<?>> {
		protected TEReg(IForgeRegistry<TileEntityType<?>> reg, String mod_id) { super(reg, mod_id); }

		@SafeVarargs
		private final <T extends TileEntity & IETTileEntity> RegistryObject<TileEntityType<T>> register(
				String name,
				Supplier<T> factory,
				Supplier<? extends Block>... validBlocks
		) {
			return register(name, () -> TileEntityType.Builder.create(factory, Arrays.stream(validBlocks).map(Supplier::get).toArray(Block[]::new)).build(null));
		}
	}

	private static Item.Properties getDefault() {
		return new Item.Properties().group(ETItems.ENDER_MISC);
	}

	public static final BlockItemObject<EnderFluxCollectorBlock, ETEnchantableBlockItem> ENDER_FLUX_COLLECTOR =
			BLOCKS.reg("ender_flux_collector", EnderFluxCollectorBlock::new, ETEnchantableBlockItem::new, ETBlocks::getDefault);
	public static final BlockItemObject<EnderFluxConverterBlock, ETEnchantableBlockItem> ENDER_FLUX_CONVERTER =
			BLOCKS.reg("ender_flux_converter", EnderFluxConverterBlock::new, ETEnchantableBlockItem::new, ETBlocks::getDefault);
	public static final BlockItemObject<EnderFluxBatteryBlock, ETBlockItem> ENDER_FLUX_BATTERY =
			BLOCKS.reg("ender_flux_battery", EnderFluxBatteryBlock::new, ETBlockItem::new, ETBlocks::getDefault);
	public static final RegistryObject<ConduitBlock> CONDUIT = BLOCKS.register("conduit" , ConduitBlock::new);


	public static final RegistryObject<TileEntityType<EnderFluxCollectorTE>> ENDER_FLUX_COLLECTOR_TYPE = TILE_ENTITIES.register("ender_flux_collector", EnderFluxCollectorTE::new, ENDER_FLUX_COLLECTOR);
	public static final RegistryObject<TileEntityType<EnderFluxConverterTE>> ENDER_FLUX_CONVERTER_TYPE = TILE_ENTITIES.register("ender_flux_converter", EnderFluxConverterTE::new, ENDER_FLUX_CONVERTER);
	public static final RegistryObject<TileEntityType<EnderFluxBatteryTE>> ENDER_FLUX_BATTERY_TYPE = TILE_ENTITIES.register("ender_flux_battery", EnderFluxBatteryTE::new, ENDER_FLUX_BATTERY);
	public static final RegistryObject<TileEntityType<ConduitTE>> CONDUIT_TYPE = TILE_ENTITIES.register("conduit", ConduitTE::new, CONDUIT);

	public ETBlocks(IEventBus bus) { super(bus, BLOCKS, TILE_ENTITIES); }

	public static Collection<RegistryObject<Block>> getBlocks() {
		return BLOCKS.getEntries();
	}

	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return false; }
}
