package white_blizz.ender_torment.utils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public abstract class ETDeferredRegisterHandler {
	protected static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> New(IForgeRegistry<T> registry) { return new DeferredRegister<>(registry, Ref.MOD_ID); }
	protected static <T extends IForgeRegistryEntry<T>, R extends ETDeferredRegister<T>> R New(IForgeRegistry<T> registry, BiFunction<IForgeRegistry<T>, String, R> constructor) { return constructor.apply(registry, Ref.MOD_ID); }

	protected static abstract class ETDeferredRegister<T extends IForgeRegistryEntry<T>> extends DeferredRegister<T> {
		protected ETDeferredRegister(IForgeRegistry<T> reg, String mod_id) { super(reg, mod_id); }
	}

	private static final class EmptyObj<T extends IForgeRegistryEntry<? super T>>
			extends ETRegistryObject<T> {}

	@SuppressWarnings({"unused", "SameParameterValue"})
	protected abstract static class ETRegistryObject<T extends IForgeRegistryEntry<? super T>>
			implements Supplier<T> {
		private static final ETRegistryObject<?> EMPTY = new EmptyObj<>();

		private static <T extends IForgeRegistryEntry<? super T>> ETRegistryObject<T> empty() {
			@SuppressWarnings("unchecked")
			ETRegistryObject<T> t = (ETRegistryObject<T>) EMPTY;
			return t;
		}

		protected final RegistryObject<T> obj;

		private ETRegistryObject() { this.obj = null; }
		protected ETRegistryObject(RegistryObject<T> obj) { this.obj = obj; }

		private <R> void exe(BiConsumer<RegistryObject<T>, R> action, R r) {
			if (obj != null) action.accept(obj, r);
		}

		private <R> R exeF(Function<RegistryObject<T>, R> func) { return exeF(func, null); }
		private <R> R exeF(Function<RegistryObject<T>, R> func, R _default) {
			if (obj != null) return func.apply(obj);
			return _default;
		}
		private <R> R exeFS(Function<RegistryObject<T>, R> func, Supplier<? extends R> _default) {
			if (obj != null) return func.apply(obj);
			return _default.get();
		}
		private <P, R> R exeF(BiFunction<RegistryObject<T>, P, R> func, P p) { return exeF(func, p, null); }

		private interface BiFuncExcept<P1, P2, R, X extends Throwable> extends BiFunction<P1, P2, R> {
			R applyX(P1 p1, P2 p2) throws X;

			@Override
			default R apply(P1 p1, P2 p2) throws RuntimeException {
				try { return applyX(p1, p2); }
				catch (Throwable e) { throw new RuntimeException(e); }
			}
		}

		private <P, R, X extends Throwable> R exeFX(BiFuncExcept<RegistryObject<T>, P, R, X> func, P p, Supplier<? extends X> exceptionSupplier) throws X {
			R r;
			try { r = exeF(func, p, null); }
			catch (RuntimeException e) {
				Throwable cause = e.getCause();
				try { if (cause != null) //noinspection unchecked
					throw (X) cause; }
				catch (ClassCastException ignored) {}
				throw  e;
			}
			if (r == null) throw exceptionSupplier.get();
			return r;
		}
		private <P, R> R exeF(BiFunction<RegistryObject<T>, P, R> func, P p, R _default) {
			if (obj != null) return func.apply(obj, p);
			return _default;
		}
		private <P, R> R exeFS(BiFunction<RegistryObject<T>, P, R> func, P p, Supplier<? extends R> _default) {
			if (obj != null) return func.apply(obj, p);
			return _default.get();
		}

		@Override public T get() { return exeF(RegistryObject::get); }
		public RegistryObject<T> getRaw() { return obj; }
		public ResourceLocation getId() { return exeF(RegistryObject::getId); }
		public Stream<T> stream() { return exeF(RegistryObject::stream, Stream.of()); }
		public boolean isPresent() { return exeF(RegistryObject::isPresent, false); }
		public void ifPresent(Consumer<? super T> consumer) { exe(RegistryObject::ifPresent, consumer); }
		public RegistryObject<T> filter(Predicate<? super T> predicate) { return exeF(RegistryObject::filter, predicate); }
		public ETRegistryObject<T> filterAlt(Predicate<? super T> predicate) {
			Objects.requireNonNull(predicate);
			if (!isPresent()) return this;
			else return predicate.test(get()) ? this : empty();
		}
		public<U> Optional<U> map(Function<? super T, ? extends U> mapper) { return exeFS(RegistryObject::map, mapper, Optional::empty); }
		public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) { return exeFS(RegistryObject::flatMap, mapper, Optional::empty); }
		public<U> Supplier<U> lazyMap(Function<? super T, ? extends U> mapper) { return exeF(RegistryObject::lazyMap, mapper); }
		public T orElse(T other) { return exeF(RegistryObject::orElse, other, other); }
		public T orElseGet(Supplier<? extends T> other) { return exeFS(RegistryObject::orElseGet, other, other); }
		public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X { return this.<Supplier<? extends X>, T, X>exeFX(RegistryObject::orElseThrow, exceptionSupplier, exceptionSupplier); }


		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof RegistryObject) {
				return Objects.equals(((RegistryObject<?>)obj).getId(), getId());
			} else if (obj instanceof ETRegistryObject) {
				return Objects.equals(((ETRegistryObject<?>) obj).getId(), getId());
			}
			return false;
		}

		@Override public int hashCode() { return exeF(RegistryObject::hashCode, 0); }
	}

	protected ETDeferredRegisterHandler(IEventBus bus, DeferredRegister<?> registry) { registry.register(bus); }

	protected ETDeferredRegisterHandler(IEventBus bus, DeferredRegister<?>... registries) { for (DeferredRegister<?> registry : registries) registry.register(bus); }

	public abstract boolean regModBus();
	public abstract boolean regForgeBus();
}
