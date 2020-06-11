package white_blizz.ender_torment.common.conduit.io;

import java.util.Optional;

public interface IConduitOutput<Cap> extends IConduitIO<Cap> {
	@Override default Optional<IConduitOutput<Cap>> asOutput() { return Optional.of(this); }
	@Override default boolean isOutput() { return true; }
}