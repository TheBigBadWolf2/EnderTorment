package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class Network {
	static final Logger LOGGER = LogManager.getLogger();

	static class Networks {
		final DimMap MAPPED_NETWORKS = new DimMap();
		final HashMap<UUID, Network> NETWORKS = new HashMap<>();

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
			Iterator<Map.Entry<UUID, Network>> ittr = SERVER.NETWORKS.entrySet().iterator();
			while (ittr.hasNext()) {
				Network network = ittr.next().getValue();
				if (network.links.isEmpty()) {
					ittr.remove();
					LOGGER.debug("Network of type \"{}\" and id \"{}\" destroyed due to being empty!", network.type, network.id);
				}
			}
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent evt) {
		if (evt.phase == TickEvent.Phase.START) {
			Iterator<Map.Entry<UUID, Network>> ittr = CLIENT.NETWORKS.entrySet().iterator();
			while (ittr.hasNext()) {
				Network network = ittr.next().getValue();
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
		List<Network> networks = new ArrayList<>();
		DimensionType dim = link.dim;
		BlockPos pos = link.pos;
		ConduitType<Cap> type = link.type;
		for (Direction direction : Direction.values()) {
			final Link<Cap> other = getLink(dim, pos.offset(direction), type, isClient);
			if (other != null) {
				final Network network = other.getNetwork();
				if (!networks.contains(network))
					networks.add(network);
			}
		}
		final int count = networks.size();
		if (count == 0) {
			final Network network = new Network(type, isClient);
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
		} else if (count == 1) {
			final Network network = networks.get(0);
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
		} else {
			final Network network = networks.get(0);
			StringBuilder log = new StringBuilder("Network(s) ");
			link.setNetwork(network);
			getNetworks(isClient).MAPPED_NETWORKS.add(link);
			for (int i = 1; i < networks.size(); i++) {
				final Network other = networks.get(i);
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
		Network network = link.network;
		if (network == null) return;
		if (dissolve) {
			if (link.getConnections().size() > 1) {
				network.invalidate();
				getNetworks(isClient).NETWORKS.remove(network.id);
				LOGGER.debug("Network of type \"{}\" and id \"{}\" destroyed!", network.type, network.id);
			}
		} else network.remove(link);
	}

	private void merge(Network other) {
		final Iterator<Link<?>> ittr = other.links.iterator();
		while (ittr.hasNext()) {
			final Link<?> link = ittr.next();
			ittr.remove();
			link.setNetwork(this);
			link.markDirty();
		}
	}

	final ConduitType<?> type;
	final List<Link<?>> links = new ArrayList<>();
	private final List<Link<?>> inputs = new ArrayList<>();
	private final List<Link<?>> outputs = new ArrayList<>();
	private final UUID id;

	Network(ConduitType<?> type, UUID id, boolean isClient) {
		this.type = type;
		this.id = id;
		getNetworks(isClient).NETWORKS.put(id, this);
		LOGGER.debug("Network of type \"{}\" and id \"{}\" recreated!", type, id);
	}

	Network(ConduitType<?> type, boolean isClient) {
		this.type = type;
		id = UUID.randomUUID();
		getNetworks(isClient).NETWORKS.put(id, this);
		LOGGER.debug("New network of type \"{}\" and id \"{}\" created!", type, id);
	}

	void updateLink(Link<?> link) {
		boolean wasIn, isIn, wasOut, isOut;
		wasIn = inputs.contains(link);
		isIn = link.hasInput();
		wasOut = outputs.contains(link);
		isOut = link.hasOutput();
		if (wasIn != isIn) {
			if (isIn) inputs.add(link);
			else inputs.remove(link);
		}
		if (wasOut != isOut) {
			if (isOut) outputs.add(link);
			else outputs.remove(link);
		}
	}

	void updateLinks() { links.forEach(Link::updateNetwork); }

	public UUID getId() { return id; }

	private void invalidate() {
		Iterator<Link<?>> ittr = links.iterator();
		while (ittr.hasNext()) {
			Link<?> link = ittr.next();
			link.network = null;
			link.networkID = null;
			inputs.remove(link);
			outputs.remove(link);
			ittr.remove();
			link.markDirty();
		}
	}

	boolean remove(Link<?> link) {
		if (links.remove(link)) {
			link.network = null;
			link.networkID = null;
			link.markDirty();
			inputs.remove(link);
			outputs.remove(link);
			return true;
		}
		return false;
	}
}
