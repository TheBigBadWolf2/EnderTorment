package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Network<Cap> {
	static final Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	public <C> Optional<Network<C>> as(ConduitType<C> type) {
		if (this.type.equals(type)) return Optional.of((Network<C>) this);
		return Optional.empty();
	}

	public <C> Network<C> asE(ConduitType<C> type) {
		return as(type).orElseThrow(() -> new IllegalArgumentException(String.format(
				"Type \"%s\" was requested, but got type \"%s\" with ud \"%s\"",
				type, this.type, id
		)));
	}

	static class Networks {
		final DimMap MAPPED_NETWORKS = new DimMap();
		final HashMap<UUID, Network<?>> NETWORKS = new HashMap<>();

		/**
		 * Just sweep it under the rug...
		 */
		private void invalidate() {
			LOGGER.info("Invalidating cash!");
			MAPPED_NETWORKS.clear();
			NETWORKS.clear();
		}
	}

	private static final Networks CLIENT = new Networks();
	private static final Networks SERVER = new Networks();

	static Networks getNetworks(boolean isClient) {
		return isClient ? CLIENT : SERVER;
	}

	@SubscribeEvent
	public static void serverTick(TickEvent.ServerTickEvent evt) {
		if (evt.phase == TickEvent.Phase.START) {
			Iterator<Map.Entry<UUID, Network<?>>> ittr = SERVER.NETWORKS.entrySet().iterator();
			while (ittr.hasNext()) {
				Network<?> network = ittr.next().getValue();
				if (network.links.isEmpty()) {
					ittr.remove();
					LOGGER.debug("Network of type \"{}\" and id \"{}\" destroyed due to being empty!", network.type, network.id);
				} else network.tick();
			}
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent evt) {
		if (evt.phase == TickEvent.Phase.START) {
			Iterator<Map.Entry<UUID, Network<?>>> ittr = CLIENT.NETWORKS.entrySet().iterator();
			while (ittr.hasNext()) {
				Network<?> network = ittr.next().getValue();
				if (network.links.isEmpty()) {
					ittr.remove();
					LOGGER.debug("Network of type \"{}\" and id \"{}\" destroyed due to being empty!", network.type, network.id);
				}
			}
		}
	}

	@SubscribeEvent
	public static void invalidateServer(FMLServerStoppingEvent evt) {
		getNetworks(false).invalidate();
	}

	@SubscribeEvent
	public static void invalidateClient(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
		getNetworks(true).invalidate();
	}

	@Nullable
	public static <Cap> Link<Cap> getLink(DimensionType dim, BlockPos pos, ConduitType<Cap> type, boolean isClient) {
		return getNetworks(isClient).MAPPED_NETWORKS.get(dim, pos, type);
	}

	/**
	 * For use on world load so networks aren't constantly being created and destroyed.
	 * @param link the new link.
	 */
	public static <Cap> void addLinkLazy(Link<Cap> link) {
		getNetworks(link.isClient).MAPPED_NETWORKS.add(link);
	}

	public static <Cap> void addLink(Link<Cap> link) {
		boolean isClient = link.isClient;
		List<Network<Cap>> networks = new ArrayList<>();
		DimensionType dim = link.dim;
		BlockPos pos = link.pos;
		ConduitType<Cap> type = link.type;
		for (Direction direction : Direction.values()) {
			final Link<Cap> other = getLink(dim, pos.offset(direction), type, isClient);
			if (other != null && other.canConnectTo(direction.getOpposite())) {
				final Network<Cap> network = other.getNetwork();
				if (!networks.contains(network))
					networks.add(network);
			}
		}
		final int count = networks.size();
		if (count == 0) {
			final Network<Cap> network = new Network<>(type, isClient);
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
		} else if (count == 1) {
			final Network<Cap> network = networks.get(0);
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
		} else {
			final Network<Cap> network = networks.get(0);
			StringBuilder log = new StringBuilder("Network(s) ");
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
			for (int i = 1; i < networks.size(); i++) {
				final Network<Cap> other = networks.get(i);
				if (i != 1) log.append(", ");
				log.append('"').append(other.id).append('"');
				network.merge(other);
				getNetworks(isClient).NETWORKS.remove(other.id);
			}
			LOGGER.debug(log.append(" merged with ").append('"').append(network.id).append('"').toString());
		}
	}

	public static <Cap> void removeLink(Link<Cap> link, boolean dissolve) {
		boolean isClient = link.isClient;
		getNetworks(isClient).MAPPED_NETWORKS.remove(link);
		Network<Cap> network = link.network;
		if (network == null) return;
		network.remove(link);
		if (dissolve) {
			if (link.getLinks().size() <= 1) return;
			network.invalidate();
			getNetworks(isClient).NETWORKS.remove(network.id);
			LOGGER.debug("Network of type \"{}\" and id \"{}\" destroyed!", network.type, network.id);
		}
	}

	private void merge(Network<Cap> other) {
		final Iterator<Link<Cap>> ittr = other.links.iterator();
		while (ittr.hasNext()) {
			final Link<Cap> link = ittr.next();
			ittr.remove();
			link.setNetwork(this);
			link.markDirty();
		}
	}

	final ConduitType<Cap> type;
	final Set<Link<Cap>> links = new HashSet<>();
	//private final List<Link<Cap>> inputs = new ArrayList<>();
	//private final List<Link<Cap>> outputs = new ArrayList<>();
	private final Set<Node<Cap>> nodes = new HashSet<>();
	private final UUID id;

	Network(ConduitType<Cap> type, UUID id, boolean isClient) {
		this.type = type;
		this.id = id;
		getNetworks(isClient).NETWORKS.put(id, this);
		LOGGER.debug("Network of type \"{}\" and id \"{}\" recreated!", type, id);
	}

	Network(ConduitType<Cap> type, boolean isClient) {
		this.type = type;
		id = UUID.randomUUID();
		getNetworks(isClient).NETWORKS.put(id, this);
		LOGGER.debug("New network of type \"{}\" and id \"{}\" created!", type, id);
	}

	void updateLink(Link<Cap> link) {
		nodes.removeIf(node -> node.parent == link);
		nodes.addAll(link.getNodes().values());
	}

	void updateLinks() { links.forEach(Link::updateNetwork); }

	public UUID getId() { return id; }

	private void invalidate() {
		Iterator<Link<Cap>> ittr = links.iterator();
		while (ittr.hasNext()) {
			Link<?> link = ittr.next();
			link.network = null;
			link.networkID = null;
			ittr.remove();
			link.markDirty();
		}
		nodes.clear();
	}

	@SuppressWarnings("UnusedReturnValue")
	boolean remove(Link<Cap> link) {
		if (links.remove(link)) {
			link.network = null;
			link.networkID = null;
			nodes.removeIf(node -> node.parent == link);
			link.markDirty();
			return true;
		}
		return false;
	}

	private void tick() {
		type.tick(
			nodes.stream()
					.filter(NetworkPart::validate)
					.map(Node::asIO)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList())
		);
	}
}
