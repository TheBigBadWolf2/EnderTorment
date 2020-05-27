package white_blizz.ender_torment.common.conduit;

import com.google.common.base.Preconditions;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.utils.ETMaps;
import white_blizz.ender_torment.utils.ETNBTUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"UnusedReturnValue"})
public abstract class Link<Cap> implements INBTSerializable<CompoundNBT> {
	private static final UUID DEFAULT_ID = new UUID(0L, 0L);

	//ToDo: interdimensional connections?
	static <Cap> boolean link(Link<Cap> a, Link<Cap> b) {
		if (a.dim != b.dim) {
			if (!a.isInterdimensional() || !b.isInterdimensional()) return false;
			return a.handleInterdimensionalConnection(b);
		}
		if (a.type != b.type) return false;
		BlockPos dif = b.pos.subtract(a.pos);
		Direction dirA = Direction.byLong(dif.getX(), dif.getY(), dif.getZ());
		if (dirA == null) {
			if (!a.isLong() || !b.isLong()) return false;
			return a.handleLongConnection(b);
		}
		Direction dirB = dirA.getOpposite();
		if (a.connections.get(dirA) != null) return false;
		if (b.connections.get(dirB) != null) return false;
		a.link(dirA, b);
		b.link(dirB, a);
		return true;
	}
	static <Cap> boolean unlink(Link<Cap> a, Link<Cap> b) {
		if (a.network != b.network) return false;
		if (a.dim != b.dim) {
			if (a.isInterdimensional() && b.isInterdimensional())
				return a.handleInterdimensionalDisconnection(b);
			throw new IllegalStateException("Bad long distance relationship!");
		}
		BlockPos dif = b.pos.subtract(a.pos);
		Direction dirA = Direction.byLong(dif.getX(), dif.getY(), dif.getZ());
		if (dirA == null) {
			if (a.isLong() && b.isLong()) return a.handleLongDisconnect(b);
			throw new IllegalStateException("Bad long distance relationship!");
		}
		Direction dirB = dirA.getOpposite();
		Link<Cap>.Connection connection = a.connections.get(dirA);
		if (connection == null || connection.getLink() != b) return false;
		connection = b.connections.get(dirB);
		if (connection == null || connection.getLink() != a) throw new IllegalStateException("Link \"a\" was connected to Link \"b\", but Link \"b\" was not connected to Link \"a\"!");
		a.unlink(dirA);
		b.unlink(dirB);
		return true;
	}

	static <Cap> void unlink(Link<Cap> link) {
		for (Direction dir : Direction.values()) {
			Link<Cap>.Connection connection = link.connections.get(dir);
			if (connection != null) {
				Link<Cap> other = connection.getLink();
				if (other!= null) other.unlink(dir.getOpposite());
				link.unlink(dir);
			}
		}
	}

	public class Connection {
		private final Direction direction;
		protected BlockPos pos;
		@Nullable protected Link<Cap> link;

		Connection(@Nullable Direction direction) { this.direction = direction; }

		protected void set(@Nullable Link<Cap> other) {
			link = other;
			if (other != null) pos = other.pos;
		}

		private CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			if (pos != null)
				tag.put("pos", NBTUtil.writeBlockPos(pos));
			if (direction != null)
				tag.putString("dir", direction.getName());
			return serializeExtraNBT(tag);
		}

		@Nullable
		protected Link<Cap> createLink() {
			return Network.getNetworks(isClient).MAPPED_NETWORKS.get(dim, pos, type);
		}

		@Nullable
		public Link<Cap> getLink() {
			if (link == null) link = createLink();
			return link;
		}

		protected CompoundNBT serializeExtraNBT(CompoundNBT tag) { return tag; }
		protected void deserializeExtraNBT(CompoundNBT tag) {}

		void deserializeNBT(CompoundNBT tag) {
			pos = ETNBTUtil.COMPOUND.tryGetAs(tag, "pos", NBTUtil::readBlockPos).orElse(null);
			deserializeExtraNBT(tag);
		}

	}

	protected final <T extends Connection> T deserializeConnection(CompoundNBT tag, Function<Direction, T> factory) {
		Direction dir = ETNBTUtil.STRING.tryGetAs(tag, "dir", Direction::byName).orElse(null);
		T connection = factory.apply(dir);
		connection.deserializeNBT(tag);
		return connection;
	}

	private void link(Direction direction, Link<Cap> other) {
		connections.compute(direction, (dir, connection) -> {
			if (connection == null) connection = new Connection(dir);
			connection.set(other);
			return connection;
		});
	}
	private void unlink(Direction direction) {
		connections.remove(direction);
	}

	final HashMap<Direction, Connection> connections = new HashMap<>(6);
	final DimensionType dim;
	final BlockPos pos;
	final ConduitType<Cap> type;
	final boolean isClient;
	@Nullable Network network;
	@Nullable UUID networkID;

	public DimensionType getDim() { return dim; }
	public BlockPos getPos() { return pos; }

	protected Link(DimensionType dim, BlockPos pos, ConduitType<Cap> type, boolean isClient) {
		this.dim = dim;
		this.pos = pos;
		this.type = type;
		this.isClient = isClient;
	}

	protected boolean isInterdimensional() { return false; }
	protected boolean isLong() { return false; }

	private static RuntimeException notOverridden() {
		return new IllegalStateException("Very bad long distance relationship!");
	}

	protected boolean handleInterdimensionalConnection(Link<Cap> other) { throw notOverridden(); }
	protected boolean handleInterdimensionalDisconnection(Link<Cap> other) { throw notOverridden(); }
	protected boolean handleLongConnection(Link<Cap> other) { throw notOverridden(); }
	protected boolean handleLongDisconnect(Link<Cap> other) { throw notOverridden(); }

	private void addToNetwork(Network network) {
		if (network.links.contains(this)) return; //Already added.
		if (this.network != null) this.network.remove(this);
		this.setNetwork(network);
		connections.forEach((dir, connection) -> {
			if (connection != null) {
				Link<Cap> other = connection.getLink();
				if (other != null) {
					other.addToNetwork(network);
				}
			}
		});
		markDirty();
	}

	public final Network getNetwork() {
		if (network == null) {
			Network network;
			if (networkID == null) network = new Network(type, isClient);
			else if (Network.getNetworks(isClient).NETWORKS.containsKey(networkID)) network = Network.getNetworks(isClient).NETWORKS.get(networkID);
			else network = new Network(type, networkID, isClient);
			addToNetwork(network);
		}
		return network;
	}

	@Nullable
	private Network tryGetNetwork() {
		if (network == null) {
			if (networkID == null) return null;
			Network.Networks networks = Network.getNetworks(isClient);
			if (!networks.NETWORKS.containsKey(networkID)) return null;
			network = networks.NETWORKS.get(networkID);
			network.links.add(this);
		}
		return network;
	}

	public final UUID getNetworkID() {
		if (network != null) return network.getId();
		if (networkID != null) return networkID;
		return DEFAULT_ID;
	}

	public final void setNetwork(Network network) {
		this.network = network;
		this.networkID = network.getId();
		this.network.links.add(this);
		updateNetwork();
	}

	public final void updateNetwork() {
		Network network = tryGetNetwork();
		if (network != null) network.updateLink(this);
		updateConnections();
	}

	public abstract boolean hasInput();
	public abstract boolean hasOutput();

	protected final void updateConnections() {
		for (Direction direction : Direction.values()) {
			Link<Cap>.Connection connection = connections.get(direction);
			if (connection != null) {
				Link<Cap> other = connection.getLink();
				if (other != null) {
					if (!canConnectTo(direction) || !other.canConnectTo(direction.getOpposite())) {
						unlink(this, other);
					}
					continue;
				}
			}
			{
				Link<Cap> link = Network.getLink(dim, pos.offset(direction), type, isClient);
				if (link != null) {
					if (canConnectTo(direction) && link.canConnectTo(direction.getOpposite())) {
						link(this, link);
					}
				}
			}
		}
	}

	protected abstract boolean canConnectTo(Direction dir);

	public final Map<Direction, Link<Cap>> getConnections() {
		return ETMaps.newHashMap(true, dir -> {
			Connection connection = connections.get(dir);
			if (connection != null) return connection.getLink();
			return null;
		}, Direction.values());
	}

	public ConduitType<Cap> getType() { return type; }

	@Override
	public final CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString("dim", Objects.requireNonNull(dim.getRegistryName()).toString());
		tag.put("pos", NBTUtil.writeBlockPos(pos));
		tag.putString("type", Objects.requireNonNull(type.getRegistryName()).toString());
		if (network != null) tag.put("network", NBTUtil.writeUniqueId(network.getId()));
		else if (networkID != null) tag.put("network", NBTUtil.writeUniqueId(networkID));
		ListNBT list = new ListNBT();
		connections.forEach((dir, con) -> {
			if (con != null) {
				list.add(con.serializeNBT());
			}
		});
		tag.put("connections", list);
		return serializeExtraNBT(tag);
	}

	@Override
	public final void deserializeNBT(CompoundNBT tag) {
		ETNBTUtil.COMPOUND.tryRunAs(tag, "network", NBTUtil::readUniqueId, id -> {
			setNetwork(Network.getNetworks(isClient).NETWORKS.computeIfAbsent(
					id, id1 -> new Network(type, id1, isClient)));
			this.networkID = id;
		});
		connections.clear();
		ListNBT list = tag.getList("connections", Constants.NBT.TAG_COMPOUND);
		list.forEach(nbt -> {
			Connection connection = deserializeConnection((CompoundNBT) nbt, Connection::new);
			connections.put(connection.direction, connection);
		});
		deserializeExtraNBT(tag);
	}

	protected abstract CompoundNBT serializeExtraNBT(CompoundNBT tag);
	protected abstract void deserializeExtraNBT(CompoundNBT tag);

	public interface LinkFactory<L  extends Link<?>> {
		L make(DimensionType dim, BlockPos pos, ConduitType<?> type, boolean isClient);
	}

	public void remove(boolean dissolve) {
		Network.removeLink(this, dissolve);
		Link.unlink(this);
	}

	protected abstract void markDirty();

	public static <L extends Link<?>> L deserialize(
			CompoundNBT tag,
			LinkFactory<L> factory, boolean isClient
	) {
		DimensionType dim = DimensionType.byName(new ResourceLocation(tag.getString("dim")));
		BlockPos pos = NBTUtil.readBlockPos(tag.getCompound("pos"));
		ConduitType<?> type = ETRegistry.CONDUIT_TYPE.getValue(new ResourceLocation(tag.getString("type")));
		Preconditions.checkNotNull(dim, "dim %s was not found!", tag.getString("dim"));
		Preconditions.checkNotNull(type, "type %s was not found!", tag.getString("type"));
		L link = factory.make(dim, pos, type, isClient);
		link.deserializeNBT(tag);
		return link;
	}

}
