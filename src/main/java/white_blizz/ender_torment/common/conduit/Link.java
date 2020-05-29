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
import java.util.concurrent.atomic.AtomicBoolean;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"UnusedReturnValue"})
public abstract class Link<Cap> extends NetworkPart<Cap> implements INBTSerializable<CompoundNBT> {
	private static final UUID DEFAULT_ID = new UUID(0L, 0L);

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
		if (a.connections.get(dirA).isPresent()) return false;
		if (b.connections.get(dirB).isPresent()) return false;
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
		if (a.connections.get(dirA).flatMap(Connection::getPart).map(o -> o != b).orElse(true)) return false;
		if (b.connections.get(dirB).flatMap(Connection::getPart).map(o -> o != a).orElse(true)) throw new IllegalStateException("Link \"a\" was connected to Link \"b\", but Link \"b\" was not connected to Link \"a\"!");
		a.unlink(dirA);
		b.unlink(dirB);
		return true;
	}

	private static <Cap> boolean tryForceLink(Link<Cap> a, Direction dir, Link<Cap> b) {
		if (a.connections.get(dir).isPresent()) return false;
		if (b.connections.get(dir.getOpposite()).isPresent()) return false;
		a.link(dir, b);
		b.link(dir.getOpposite(), a);
		return true;
	}

	private static <Cap> boolean tryForceUnlink(Link<Cap> a, Direction dir, Link<Cap> b) {
		if (a.connections.get(dir).flatMap(Connection::getPart).map(o -> o != b).orElse(true)) return false;
		if (b.connections.get(dir.getOpposite()).flatMap(Connection::getPart).map(o -> o != a).orElse(true)) throw new IllegalStateException("Link \"a\" was connected to Link \"b\", but Link \"b\" was not connected to Link \"a\"!");
		a.unlink(dir);
		b.unlink(dir.getOpposite());
		return true;
	}

	static <Cap> void unlink(Link<Cap> link) {
		for (Direction dir : Direction.values()) {
			link.connections.get(dir).ifPresent(
					c -> {
						c.getPart().flatMap(NetworkPart::asLink).ifPresent(l -> l.unlink(dir.getOpposite()));
						link.unlink(dir);
					}
			);
		}
	}



	private void link(Direction direction, NetworkPart<Cap> other) {
		connections.compute(direction, (dir, connection) -> {
			if (connection == null) connection = new Connection<>(this, dir, type, isClient);
			connection.set(other);
			return connection;
		});
		markDirty();
	}
	private void unlink(Direction direction) {
		connections.remove(direction);
		markDirty();
	}

	final ConnectionMap<Cap> connections = new ConnectionMap<>();

	protected Link(DimensionType dim, BlockPos pos, ConduitType<Cap> type, boolean isClient) {
		super(dim, pos, type, isClient);
	}

	protected abstract boolean canConnectTo(Direction dir);

	protected boolean isInterdimensional() { return false; }
	protected boolean isLong() { return false; }

	private static RuntimeException notOverridden() {
		return new IllegalStateException("Very bad long distance relationship!");
	}

	protected boolean handleInterdimensionalConnection(NetworkPart<Cap> other) { throw notOverridden(); }
	protected boolean handleInterdimensionalDisconnection(NetworkPart<Cap> other) { throw notOverridden(); }

	protected boolean handleLongConnection(NetworkPart<Cap> other) { throw notOverridden(); }
	protected boolean handleLongDisconnect(NetworkPart<Cap> other) { throw notOverridden(); }

	private void addToNetwork(Network<Cap> network) {
		if (network.links.contains(this)) return; //Already added.
		if (this.network != null) this.network.remove(this);
		this.setNetwork(network);
		connections.forEach((dir, connection) -> {
			if (connection != null) {
				connection.getPart()
						.flatMap(NetworkPart::asLink)
						.ifPresent(l -> l.addToNetwork(network));
			}
		});
		markDirty();
	}

	public final Network<Cap> getNetwork() {
		if (network == null) {
			Network<Cap> network;
			if (networkID == null) network = new Network<>(type, isClient);
			else if (Network.getNetworks(isClient).NETWORKS.containsKey(networkID)) {
				network = Network.getNetworks(isClient).NETWORKS.get(networkID).asE(type);
			} else network = new Network<>(type, networkID, isClient);
			addToNetwork(network);
		}
		return network;
	}

	@Nullable
	private Network<Cap> tryGetNetwork() {
		if (network == null) {
			if (networkID == null) return null;
			Network.Networks networks = Network.getNetworks(isClient);
			if (!networks.NETWORKS.containsKey(networkID)) return null;
			network = networks.NETWORKS.get(networkID).asE(type);
			network.links.add(this);
		}
		return network;
	}

	public final UUID getNetworkID() {
		if (network != null) return network.getId();
		if (networkID != null) return networkID;
		return DEFAULT_ID;
	}

	public final void setNetworkLazily(Network<Cap> network) {
		this.network = network;
		this.networkID = network.getId();
		this.network.links.add(this);
	}

	public final void setNetwork(Network<Cap> network) {
		setNetworkLazily(network);
		updateNetwork();
	}

	public final void updateNetwork() {
		updateConnections();
		Network<Cap> network = tryGetNetwork();
		if (network != null) network.updateLink(this);
	}

	/*public abstract boolean hasInput();
	public abstract boolean hasBuffer();
	public abstract boolean hasOutput();

	public abstract List getInputs();
	public abstract List getBuffers();
	public abstract List getOutputs();*/

	protected final void updateConnections() {
		for (Direction direction : Direction.values()) {
			AtomicBoolean skip = new AtomicBoolean(false);
			Optional<NetworkPart<Cap>> part = connections.get(direction)
					.flatMap(Connection::getPart);

			part.flatMap(NetworkPart::asLink)
					.filter(link -> {
						skip.set(true);
						return !canConnectTo(direction)
								|| !link.canConnectTo(direction.getOpposite())
								|| !link.validate();
					})
					.ifPresent(link -> tryForceUnlink(this, direction, link));
			if (skip.get()) continue;
			part.flatMap(NetworkPart::asNode)
					.filter(node -> {
						skip.set(true);
						return !canConnectTo(direction) || !node.validate();
					})
					.ifPresent(node -> {
						connections.remove(direction);
						markDirty();
					});
			if (skip.get()) continue;
			Link<Cap> link = Network.getLink(dim, pos.offset(direction), type, isClient);
			if (link != null) {
				if (canConnectTo(direction) && link.canConnectTo(direction.getOpposite())) {
					//link(this, link);
					tryForceLink(this, direction, link);
				}
			} else if (Connection.canBeNode(dim, pos.offset(direction), type, direction))
				connections.compute(direction, this::updateNodeConnection);
		}
	}

	@Nullable
	protected Connection<Cap> updateNodeConnection(Direction dir, @Nullable Connection<Cap> connection) {
		if (!canConnectTo(dir)) return connection;
		if (connection == null) connection = new Connection<>(this, dir, type, isClient);
		connection.part = null;
		connection.dim = dim;
		connection.pos = pos.offset(dir);
		markDirty();
		return connection;
	}

	protected abstract Node<Cap> makeNode(DimensionType dim, BlockPos pos, @Nullable Direction dir, Cap cap);

	public final Map<Direction, Link<Cap>> getLinks() {
		return ETMaps.newHashMap(true, dir -> connections
						.get(dir)
						.flatMap(Connection::getPart)
						.flatMap(NetworkPart::asLink)
						.orElse(null),
				Direction.values());
	}

	public final Map<Direction, NetworkPart<Cap>> getParts() {
		return ETMaps.newHashMap(true, dir -> connections
						.get(dir)
						.flatMap(Connection::getPart)
						.orElse(null),
				Direction.values());
	}

	public final Map<Direction, Connection<Cap>> getConnections() {
		return ETMaps.newHashMap(true, dir -> connections
						.get(dir)
						.orElse(null),
				Direction.values());
	}

	public final Map<Direction, Node<Cap>> getNodes() {
		return ETMaps.newHashMap(true, dir -> connections
						.get(dir)
						.flatMap(Connection::getPart)
						.flatMap(NetworkPart::asNode)
						.orElse(null),
				Direction.values());
	}

	public ConduitType<Cap> getType() { return type; }

	public final Optional<Link<Cap>> asLink() {
		return Optional.of(this);
	}

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
		ETNBTUtil.COMPOUND.tryRunAs(tag, "network", NBTUtil::readUniqueId,
				id -> setNetworkLazily(Network.getNetworks(isClient).NETWORKS.computeIfAbsent(
						id, id1 -> new Network<>(type, id1, isClient)).asE(type)
				));
		connections.clear();
		ListNBT list = tag.getList("connections", Constants.NBT.TAG_COMPOUND);
		list.forEach(nbt -> {
			Connection<Cap> connection = Connection.deserializeConnection((CompoundNBT) nbt, direction -> new Connection<>(this, direction, type, isClient));
			connections.add(connection);
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
		if (dissolve) Link.unlink(this);
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


	@Override
	protected boolean validate() {
		return this.equals(Network.getLink(dim, pos, type, isClient));
	}
}
