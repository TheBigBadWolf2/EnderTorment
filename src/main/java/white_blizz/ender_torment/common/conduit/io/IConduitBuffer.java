package white_blizz.ender_torment.common.conduit.io;

import java.util.Optional;

public interface IConduitBuffer<Cap> extends IConduitIO<Cap> {
	@Override default Optional<IConduitBuffer<Cap>> asBuffer() { return Optional.of(this); }
	@Override default boolean isBuffer() { return true; }
}