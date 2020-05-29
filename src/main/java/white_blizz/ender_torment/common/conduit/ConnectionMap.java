package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.Direction;

import java.util.HashMap;
import java.util.Optional;

public class ConnectionMap<Cap> extends HashMap<Direction, Connection<Cap>> {
	public ConnectionMap() { super(6); }

	public Optional<Connection<Cap>> get(Direction dir) {
		return Optional.ofNullable(super.get(dir));
	}

	public void add(Connection<Cap> connection) {
		put(connection.direction, connection);
	}
}
