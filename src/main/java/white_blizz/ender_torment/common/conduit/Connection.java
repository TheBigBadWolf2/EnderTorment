package white_blizz.ender_torment.common.conduit;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.utils.ETNBTUtil;
import white_blizz.ender_torment.utils.ETWorldUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class Connection<Cap> {
	public interface ConnectionFactory<Cap, T extends Connection<Cap>> {
		T make(@Nullable Direction direction);
	}

	protected static  <Cap, T extends Connection<Cap>> T deserializeConnection(CompoundNBT tag, ConnectionFactory<Cap, T> factory) {
		Direction dir = ETNBTUtil.STRING.tryGetAs(tag, "dir", Direction::byName).orElse(null);
		T connection = factory.make(dir);
		connection.deserializeNBT(tag);
		return connection;
	}

	protected final Link<Cap> parent;
	@Nullable protected final Direction direction;
	private final boolean isClient;
	protected final ConduitType<Cap> type;
	protected DimensionType dim;
	protected BlockPos pos;
	@Nullable protected NetworkPart<Cap> part;

	protected Connection(Link<Cap> parent, @Nullable Direction direction, ConduitType<Cap> type, boolean isClient) {
		this.parent = parent;
		this.direction = direction;
		this.type = type;
		this.isClient = isClient;
	}

	protected void set(@Nullable NetworkPart<Cap> other) {
		part = other;
		if (other != null) {
			dim = other.dim;
			pos = other.pos;
		}
	}


	@Nullable
	protected Link<Cap> createLink() {
		if (dim == null || pos == null) return null;
		return Network.getNetworks(isClient).MAPPED_NETWORKS.get(dim, pos, type);
	}

	@Nullable
	private static Direction getOpposite(@Nullable Direction dir) {
		if (dir != null) return dir.getOpposite();
		return null;
	}

	static <Cap> boolean canBeNode(DimensionType dim, BlockPos pos, ConduitType<Cap> type, Direction direction) {
		World world = ETWorldUtils.getWorld(dim);
		if (world == null) return false;
		if (!world.isAreaLoaded(pos, 0)) return false; //Retrieve only if loaded...

		TileEntity te = world.getTileEntity(pos);
		if (te == null) return false;
		LazyOptional<? extends Cap> cap = type.getCap(te, getOpposite(direction));
		return cap.isPresent();
	}

	@Nullable
	protected Node<Cap> createNode() {
		if (dim == null || pos == null) return null;
		World world = ETWorldUtils.getWorld(dim);
		if (world == null) return null;
		if (!world.isAreaLoaded(pos, 0)) return null; //Retrieve only if loaded...

		TileEntity te = world.getTileEntity(pos);
		if (te == null) return null;
		LazyOptional<? extends Cap> cap = type.getCap(te, getOpposite(direction));
		if (!cap.isPresent()) return null;

		return parent.makeNode(dim, pos, getOpposite(direction), cap.orElseThrow(() -> new IllegalStateException("Cap was present, but could not get!")));
	}

	public Optional<NetworkPart<Cap>> getPart() {
		if (part == null) part = createLink();
		if (part == null) part = createNode();

		return Optional.ofNullable(part);
	}

	public boolean validate() {
		if (part != null) {
			if (!part.validate()) {
				part = null;
				return false;
			}
			return true;
		}
		return false;
	}

	/*@Nullable
	public Link<Cap> getLink() {
		Link<Cap> link = null;
		if (part == null) part = link = createLink();
		else if (part instanceof Link) link = (Link<Cap>) part;
		return link;
	}*/

	protected CompoundNBT serializeExtraNBT(CompoundNBT tag) { return tag; }
	protected void deserializeExtraNBT(CompoundNBT tag) {}


	final CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		if (dim != null)
			tag.putString("dim", Objects.requireNonNull(dim.getRegistryName()).toString());
		if (pos != null)
			tag.put("pos", NBTUtil.writeBlockPos(pos));
		if (type != null)
			tag.putString("type", Objects.requireNonNull(type.getRegistryName()).toString());
		if (direction != null)
			tag.putString("dir", direction.getName());
		return serializeExtraNBT(tag);
	}

	final void deserializeNBT(CompoundNBT tag) {
		dim = ETNBTUtil.STRING.tryGetAs(tag, "dim", (ETNBTUtil.RSToObj<DimensionType>) DimensionType::byName).orElse(null);
		pos = ETNBTUtil.COMPOUND.tryGetAs(tag, "pos", NBTUtil::readBlockPos).orElse(null);
		deserializeExtraNBT(tag);
	}

}
