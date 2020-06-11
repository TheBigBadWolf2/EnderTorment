package white_blizz.ender_torment.common.conduit.io;

import java.util.Optional;

public interface IConduitIO<Cap> {
	default Optional<IConduitInput<Cap>> asInput() { return Optional.empty(); }
	default Optional<IConduitBuffer<Cap>> asBuffer() { return Optional.empty(); }
	default Optional<IConduitOutput<Cap>> asOutput() { return Optional.empty(); }

	default boolean isInput() { return false; }
	default boolean isBuffer() { return false; }
	default boolean isOutput() { return false; }

	Cap getCap();
}