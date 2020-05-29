package white_blizz.ender_torment.common.conduit.io;

import java.util.Optional;

public interface IConduitInput<Cap> extends IConduitIO<Cap> {
	@Override default Optional<IConduitInput<Cap>> asInput() { return Optional.of(this); }
}
