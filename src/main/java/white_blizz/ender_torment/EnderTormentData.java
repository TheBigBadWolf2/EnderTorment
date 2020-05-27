package white_blizz.ender_torment;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.CopyNbt;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.item.EnderFluxCapacitorItem;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public final class EnderTormentData {
	private static <T extends IDataProvider> T add(
			DataGenerator gen, Function<DataGenerator, T> factory) {
		T t = factory.apply(gen);
		gen.addProvider(t);
		return t;
	}
	private static <T extends IDataProvider, P> T add(
			DataGenerator gen, P param,
			BiFunction<DataGenerator, P, T> factory) {
		T t = factory.apply(gen, param);
		gen.addProvider(t);
		return t;
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();


		if (event.includeClient()) {
			add(gen, Lang::new);
			fileHelper = add(gen, fileHelper, BlockStates::new).models().existingFileHelper;
			add(gen, fileHelper, ItemModels::new);
		}

		if (event.includeServer()) {
			add(gen, LootTables::new);
		}
	}

	private static class Lang extends LanguageProvider {

		public Lang(DataGenerator gen) {
			super(gen, Ref.MOD_ID, "en_us");
		}

		@Override
		protected void addTranslations() {
			ETBlocks.getBlocks().forEach(this::addBlock);
			ETItems.getItems()
					.stream()
					.filter(item -> !(item.get() instanceof BlockItem))
					.forEach(this::addItem);
			ETEnchantments.getEnchantments().forEach(this::addEnchantment);
			addItemGroup(ETItems.ENDER_MISC);
			addEffect(ETEffects.FLUX_VISION);
		}

		private String capitalize(String in) {
			String[] parts = in.split("[_:.]");
			for (int i = 0; i < parts.length; i++) {
				char[] part = parts[i].toCharArray();
				part[0] = Character.toUpperCase(part[0]);
				parts[i] = String.valueOf(part);
			}
			return String.join(" ", parts);
		}

		private <T extends IForgeRegistryEntry<? super T>> void add(RegistryObject<T> obj, BiConsumer<Supplier<T>, String> adder) {
			adder.accept(obj, capitalize(obj.getId().getPath()));
		}

		private <T extends Effect> void addEffect(RegistryObject<T> obj) {
			add(obj, this::addEffect);
		}

		private <T extends Item> void addItem(RegistryObject<T> obj) {
			add(obj, this::addItem);
		}

		private <T extends Block> void addBlock(RegistryObject<T> obj) {
			add(obj, this::addBlock);
		}

		private <T extends Enchantment> void addEnchantment(RegistryObject<T> obj) {
			add(obj, this::addEnchantment);
		}

		private <T extends ItemGroup> void addItemGroup(T group) {
			add(group.getTranslationKey(), capitalize(group.getPath()));
		}
	}

	private static class ItemModels extends ItemModelProvider {

		public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
			super(generator, Ref.MOD_ID, existingFileHelper);
		}

		@Override
		protected void registerModels() {
			ETItems.getItems().forEach(item -> {
				String name = item.getId().getPath();
				if (item.get() instanceof BlockItem) blockItem(name);
				//else if (item.get() instanceof EnderFluxCapacitorItem) withExistingParent(item.getId().getPath(), "redstone");
				else try { this.item(name); }
				catch (IllegalArgumentException ignored) { this.wip(name); }
				//withExistingParent(item.getId().getPath(), "stick");
			});
		}

		private ItemModelBuilder blockItem(String name) {
			return withExistingParent(name, modLoc(BLOCK_FOLDER + "/" + name));
		}

		private void item(String name) {
			this.singleTexture(
					name,
					Ref.MC.rl.loc("item/generated"),
					"layer0",
					Ref.MOD.rl.loc(ITEM_FOLDER, name)

			);
		}

		private void wip(String name) {
			this.singleTexture(
					name,
					Ref.MC.rl.loc("item/generated"),
					"layer0",
					Ref.MOD.rl.loc("wip")

			);
		}

		@Override
		public String getName() {
			return Ref.MOD_ID+" Item Model";
		}
	}

	private static class BlockStates extends BlockStateProvider {

		public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
			super(gen, Ref.MOD_ID, exFileHelper);
		}

		@Override
		protected void registerStatesAndModels() {
			simpleBlock(ETBlocks.ENDER_FLUX_COLLECTOR.getRaw(), "piston_top");
			simpleBlock(ETBlocks.ENDER_FLUX_CONVERTER.getRaw(), "piston_top_sticky");
			simpleBlock(ETBlocks.ENDER_FLUX_BATTERY.getRaw(), "piston_bottom");
			terBlock(ETBlocks.CONDUIT);
		}

		@SuppressWarnings("unchecked")
		private void terBlock(RegistryObject<? extends Block> block) {
			BlockModelBuilder builder = models().getBuilder(Objects.requireNonNull(block.get().getRegistryName()).getPath())
					.texture("particle", "wip");
			super.simpleBlock(block.get(), builder);
		}

		@SuppressWarnings("unchecked")
		private void simpleBlock(RegistryObject<? extends Block> block, String texture) {
			BlockModelBuilder cubeAll = models().cubeAll(
					block.getId().getPath(),
					Ref.MC.rl.loc(ModelProvider.BLOCK_FOLDER, texture)
			);
			simpleBlock(block.get(), cubeAll);
		}
	}

	private static class LootTables extends LootTableProvider {
		public LootTables(DataGenerator gen) {
			super(gen);
		}

		private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
				Pair.of(BlockTables::new, LootParameterSets.BLOCK)
		);

		@Override
		protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
			return tables;
		}

		@Override
		protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
			map.forEach((p_218436_2_, p_218436_3_) -> LootTableManager.func_227508_a_(validationtracker, p_218436_2_, p_218436_3_));
		}

		private static class BlockTables extends BlockLootTables {
			private static class Mapping {
				private final String from, to;
				private final CopyNbt.Action action;

				private Mapping(String from) { this(from, "BlockEntityTag."+from, CopyNbt.Action.REPLACE); }
				private Mapping(String from, String to) { this(from, to, CopyNbt.Action.REPLACE); }

				private Mapping(String from, String to, CopyNbt.Action action) {
					this.from = from;
					this.to = to;
					this.action = action;
				}
			}

			private Function<Block, LootTable.Builder> withNBTs(Mapping... mappings) {
				CopyNbt.Builder copy = CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY);
				for (Mapping mapping : mappings)
					copy.addOperation(mapping.from, mapping.to, mapping.action);
				return block -> LootTable.builder()
						.addLootPool(LootPool.builder()
								.rolls(ConstantRange.of(1))
								.addEntry(ItemLootEntry.builder(block).acceptFunction(copy))
						);
			}

			@Override
			protected void addTables() {
				registerLootTable(ETBlocks.ENDER_FLUX_COLLECTOR.get(), this.withNBTs(
						new Mapping("flux"),
						new Mapping("Enchantments", "Enchantments")
				));
				registerLootTable(ETBlocks.ENDER_FLUX_CONVERTER.get(), this.withNBTs(
						new Mapping("flux"),
						new Mapping("Enchantments", "Enchantments")
				));
				registerLootTable(ETBlocks.ENDER_FLUX_BATTERY.get(), this.withNBTs(
						new Mapping("items")
				));
				registerLootTable(ETBlocks.CONDUIT.get(), new LootTable.Builder());
			}

			@Override
			protected Iterable<Block> getKnownBlocks() {
				return ETBlocks.getBlocks()
						.stream()
						.map(RegistryObject::get)
						.collect(Collectors.toList());
			}
		}
	}
}
