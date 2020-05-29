package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import white_blizz.ender_torment.common.conduit.io.IConduitIO;

import java.util.Optional;

public abstract class Node<Cap> extends NetworkPart<Cap> {
	protected final Link<Cap> parent;

	protected Node(Link<Cap> parent, DimensionType dim, BlockPos pos) {
		super(dim, pos, parent.type, parent.isClient);
		this.parent = parent;
	}

	public final Optional<Node<Cap>> asNode() {
		return Optional.of(this);
	}

	public Optional<IConduitIO<Cap>> asIO() { return Optional.empty(); }

	public abstract void invalidate();
}
