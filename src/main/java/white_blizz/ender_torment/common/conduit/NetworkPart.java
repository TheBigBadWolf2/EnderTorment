package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public abstract class NetworkPart<Cap> {

	//ToDo: interdimensional connections?
	static <Cap> boolean link(NetworkPart<Cap> a, NetworkPart<Cap> b) { return false; }

	static <Cap> boolean unlink(NetworkPart<Cap> a, NetworkPart<Cap> b) { return false; }

	final DimensionType dim;
	final BlockPos pos;
	final ConduitType<Cap> type;
	protected final boolean isClient;
	@Nullable Network<Cap> network;
	@Nullable UUID networkID;

	public DimensionType getDim() { return dim; }
	public BlockPos getPos() { return pos; }

	protected NetworkPart(DimensionType dim, BlockPos pos, ConduitType<Cap> type, boolean isClient) {
		this.dim = dim;
		this.pos = pos;
		this.type = type;
		this.isClient = isClient;
	}

	@SuppressWarnings("rawtypes")
	private static Supplier<RuntimeException> unexpected(
			Class<? extends NetworkPart> expected,
			Class<? extends NetworkPart> got
	) {
		return () -> new IllegalStateException(String.format(
				"Was expecting %s but got %s instead!",
				expected, got
		));
	}

	protected abstract boolean validate();

	public Optional<Link<Cap>> asLink() {
		return Optional.empty();
	}

	public Link<Cap> asLinkE() {
		return asLink().orElseThrow(unexpected(Link.class, getClass()));
	}

	public Optional<Node<Cap>> asNode() {
		return Optional.empty();
	}

	public Node<Cap> asNodeE() {
		return asNode().orElseThrow(unexpected(Node.class, getClass()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NetworkPart)) return false;
		NetworkPart<?> that = (NetworkPart<?>) o;
		return dim.equals(that.dim) &&
				pos.equals(that.pos) &&
				type.equals(that.type) &&
				Objects.equals(network, that.network) &&
				Objects.equals(networkID, that.networkID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dim, pos, type);
	}
}
