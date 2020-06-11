package white_blizz.ender_torment.common.compaction;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BooleanSupplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FakeWorld extends World {
	private static class FakeChunkProvider extends AbstractChunkProvider {
		private final World world;
		private final WorldLightManager lightManager;

		private FakeChunkProvider(World world) {
			this.world = world;
			this.lightManager = new WorldLightManager(this, true, world.getDimension().hasSkyLight());
		}

		@Nullable
		@Override
		public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
			return null;
		}

		@Override public void tick(BooleanSupplier hasTimeLeft) { }

		@Override
		public String makeString() {
			return "Fake chunks!";
		}

		@Override
		public WorldLightManager getLightManager() {
			return lightManager;
		}

		@Override
		public IBlockReader getWorld() {
			return world;
		}
	}

	public FakeWorld(World other) {
		super(other.getWorldInfo(), other.getDimension().getType(), (world, b) -> new FakeChunkProvider(world), other.getProfiler(), other.isRemote);
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

	}

	@Override
	public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {

	}

	@Nullable
	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Nullable
	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public void registerMapData(MapData mapDataIn) {

	}

	@Override
	public int getNextMapId() {
		return -1;
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	@Override
	public Scoreboard getScoreboard() {
		return null;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return null;
	}

	@Override
	public NetworkTagManager getTags() {
		return null;
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return null;
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return null;
	}

	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return null;
	}

	@Override
	public Biome getNoiseBiomeRaw(int x, int y, int z) {
		return null;
	}
}
