package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.enchantment.ETEnchantmentType;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;
import white_blizz.ender_torment.utils.Ref;

import java.util.Collection;

@MethodsReturnNonnullByDefault
public final class ETItems extends ETDeferredRegisterHandler {
	public static final ItemGroup ENDER_MISC = new ItemGroup(Ref.MOD.str.loc("misc")) {
		@OnlyIn(Dist.CLIENT)
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ETBlocks.ENDER_FLUX_COLLECTOR.getItem().get());
		}
	}.setRelevantEnchantmentTypes(
			ETEnchantmentType.ENDER_FLUX_ITEMS,
			ETEnchantmentType.ENDER_FLUX_GEN_ITEMS
	);

	public static final DeferredRegister<Item> ITEMS = New(ForgeRegistries.ITEMS);

	private static final class ItemReg extends ETDeferredRegister<Item> {
		protected ItemReg(IForgeRegistry<Item> reg, String mod_id) { super(reg, mod_id); }

		/*private RegistryObject<ETEnchantableBlockItem> newEnchantableBlockItem(RegistryObject<? extends Block> blockSupplier, Supplier<Item.Properties> propSupplier) {
			//noinspection unchecked
			return register(blockSupplier.getId().getPath(), () -> new ETEnchantableBlockItem(
					blockSupplier.get(),
					propSupplier.get()
			));
		}
		private RegistryObject<ETBlockItem> newBlockItem(RegistryObject<? extends Block> blockSupplier, Supplier<Item.Properties> propSupplier) {
			//noinspection unchecked
			return register(blockSupplier.getId().getPath(), () -> new ETBlockItem(
					blockSupplier.get(),
					propSupplier.get()
			));
		}*/
	}

	/*private static RegistryObject<ETEnchantableBlockItem> newEnchantableBlockItem(RegistryObject<? extends Block> blockSupplier, Supplier<Item.Properties> propSupplier) {
		return ITEMS.register(blockSupplier.getId().getPath(), () -> new ETEnchantableBlockItem(
				blockSupplier,
				propSupplier.get()
		));
	}
	private static RegistryObject<ETBlockItem> newBlockItem(RegistryObject<? extends Block> blockSupplier, Supplier<Item.Properties> propSupplier) {
		return ITEMS.register(blockSupplier.getId().getPath(), () -> new ETBlockItem(
				blockSupplier,
				propSupplier.get()
		));
	}*/

	//public static final RegistryObject<ETEnchantableBlockItem> ENDER_FLUX_COLLECTOR = newEnchantableBlockItem(ETBlocks.ENDER_FLUX_COLLECTOR, () -> new Item.Properties().group(ENDER_MISC));
	//public static final RegistryObject<ETEnchantableBlockItem> ENDER_FLUX_CONVERTER = newEnchantableBlockItem(ETBlocks.ENDER_FLUX_CONVERTER, () -> new Item.Properties().group(ENDER_MISC));
	//public static final RegistryObject<ETBlockItem> ENDER_FLUX_BATTERY = newBlockItem(ETBlocks.ENDER_FLUX_BATTERY, () -> new Item.Properties().group(ENDER_MISC));

	public static final RegistryObject<EnderGunItem> ENDER_GUN = ITEMS.register("ender_gun", EnderGunItem::new);
	public static final RegistryObject<EnderFluxCapacitorItem> ENDER_FLUX_CAPACITOR = ITEMS.register("ender_flux_capacitor", EnderFluxCapacitorItem::new);
	public static final RegistryObject<FluxVisionItem> FLUX_VISION = ITEMS.register("flux_vision", FluxVisionItem::new);
	public static final RegistryObject<BaseConduitItem> CONDUIT = ITEMS.register("conduit", BaseConduitItem::new);


	public ETItems(IEventBus bus) { super(bus, ITEMS); }

	public static Collection<RegistryObject<Item>> getItems() {
		return ITEMS.getEntries();
	}

	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return false; }
}
