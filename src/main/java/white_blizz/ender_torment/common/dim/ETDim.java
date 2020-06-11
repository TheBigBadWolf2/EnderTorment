package white_blizz.ender_torment.common.dim;

import com.google.common.collect.Streams;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.IRenderHandler;
import white_blizz.ender_torment.client.render.DimRenderer;
import white_blizz.ender_torment.utils.ETNBTUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ETDim extends Dimension {
	private static final ETNBTUtil.NBTConverter<ETDim> CONVERT = new ETNBTUtil.NBTConverter<ETDim>()
			.addLong("time", 0, t -> t.time, (t, v) -> t.time = v)
			.addInt("clearWeatherTime", 0, t -> t.clearWeatherTime, (t, v) -> t.clearWeatherTime = v)
			.addInt("thunderTime", 10000, t -> t.thunderTime, (t, v) -> t.thunderTime = v)
			.addInt("rainTime", 10000, t -> t.rainTime, (t, v) -> t.rainTime = v)
			.addBool("isThundering", true, t -> t.thundering, (t, v) -> t.thundering = v)
			.addBool("isRaining", true, t -> t.raining, (t, v) -> t.raining = v)
			;

	private long time;
	private int clearWeatherTime, thunderTime, rainTime;
	private boolean thundering, raining;

	public ETDim(World world, DimensionType type) {
		super(world, type, 0F);
		CompoundNBT tag = world.getWorldInfo().getDimensionData(type);
		CONVERT.read(tag, this);
	}

	@Override
	public void onWorldSave() {
		world.getWorldInfo().setDimensionData(getType(), CONVERT.write(this));
	}

	@Override
	public ChunkGenerator<?> createChunkGenerator() {
		ChunkGeneratorType<ETChunkGen.Config, ETChunkGen> genType = ETDims.DARK_CHUNK_GEN.get();
		BiomeProviderType<ETBiomeProvider.Settings, ETBiomeProvider> providerType = ETDims.DARK_BIOME_PROVIDER.get();
		ETBiomeProvider.Settings providerSettings = providerType.createSettings(getWorld().getWorldInfo());
		ETChunkGen.Config genSettings = genType.createSettings();
		return genType.create(getWorld(), providerType.create(providerSettings), genSettings);
	}

	@Nullable
	@Override
	public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
		return null;
	}

	@Nullable
	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
		return null;
	}


	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {

		return 0.25F;
	}

	public float getSunBrightness(long worldTime, float partialTicks) {
		float brightness = 0f;
		for (SunInfo sun : getSuns()) {
			sun.update(worldTime, partialTicks);
			brightness = Math.max(brightness, sun.getBrightness());
		}
		return brightness;
	}

	private final float[] colorsSunriseSunset = new float[4];
	@Nullable
	@Override
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		/*float dd = MathHelper.cos(celestialAngle * ((float)Math.PI * 2F));
		if (dd >= -0.4F && dd <= 0.4F) {
			float f3 = (dd) / 0.4F * 0.5F + 0.5F;
			float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * (float)Math.PI)) * 0.99F;
			f4 = f4 * f4;
			this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
			this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
			this.colorsSunriseSunset[2] = 0.2F;
			this.colorsSunriseSunset[3] = f4;
			return this.colorsSunriseSunset;
		} else */{
			return null;
		}
	}

	@Override public boolean isSurfaceWorld() { return true; }

	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks) {
		return Vec3d.ZERO;
	}

	@Override
	public boolean isSkyColored() {
		return false;
	}

	@Override
	public boolean canRespawnHere() {
		return true;
	}

	@Override
	public boolean doesXZShowFog(int x, int z) {
		return true;
	}

	@Override
	public double getVoidFogYFactor() {
		return 1;
	}

	@Override
	public void getLightmapColors(
			float partialTicks, float sunBrightness,
			float skyLight, float blockLight,
			Vector3f colors) {
		//colors.apply(f -> blockLight + skyLight * 0.1F);
		float skyScale = 1F;
		float blockScale = 0.50F;

		float r = blockLight * blockScale + skyLight * skyScale;
		float g = blockLight * blockScale;
		float b = blockLight * blockScale;
		colors.set(r, g, b);
	}

	@Override
	public float getLightBrightness(int light) {
		return light / 15F;
	}

	@Nullable
	@Override
	public IRenderHandler getSkyRenderer() {
		IRenderHandler handler = super.getSkyRenderer();
		if (handler != null) return handler;
		return DimRenderer.INSTANCE::renderSky;
	}

	@Nullable
	@Override
	public IRenderHandler getWeatherRenderer() {
		IRenderHandler handler = super.getWeatherRenderer();
		if (handler != null) return handler;
		//return DimRenderer.INSTANCE::renderWeather;
		return null;
	}

	@Override public float getCloudHeight() { return Float.MAX_VALUE; }

	@Nullable
	@Override
	public IRenderHandler getCloudRenderer() {
		IRenderHandler handler = super.getCloudRenderer();
		if (handler != null) return handler;
		return DimRenderer.INSTANCE::renderClouds;
	}


	@Override
	public void tick() {

	}

	@Override
	public long getWorldTime() {
		return time;
	}

	@Override
	public void setWorldTime(long time) {
		this.time = time;
	}

	@Override
	public void calculateInitialWeather() {
		if (raining) {
			world.rainingStrength = 1.0F;
		}

		if (thundering) {
			world.thunderingStrength = 1.0F;
		}
	}


	@Override
	public void updateWeather(Runnable defaultLogic) {
		if (world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
			//int clearWeatherTime = this.clearWeatherTime;
			int thunderTime = this.thunderTime;
			int rainTime = this.rainTime;
			boolean thundering = this.thundering;
			boolean raining = this.raining;
			/*if (clearWeatherTime > 0) {
				--clearWeatherTime;
				thunderTime = thundering ? 0 : 1;
				rainTime = raining ? 0 : 1;
				thundering = false;
				raining = false;
			} else */{
				if (thunderTime > 0) {
					--thunderTime;
					if (thunderTime == 0) thundering = !thundering;
				} else if (thundering) thunderTime = world.rand.nextInt(12000) + 3600;
				else thunderTime = world.rand.nextInt(168000) + 12000;

				if (rainTime > 0) {
					--rainTime;
					if (rainTime == 0) raining = !raining;
				} else if (raining) rainTime = world.rand.nextInt(12000) + 12000;
				else rainTime = world.rand.nextInt(168000) + 12000;
			}

			this.thunderTime = (thunderTime);
			this.rainTime = (rainTime);
			//this.clearWeatherTime = (clearWeatherTime);
			this.thundering = (thundering);
			this.raining = (raining);
		}

		double step = 0.01D;
		float min = 0.0F;
		float max = 1.0F;

		world.prevThunderingStrength = world.thunderingStrength;
		if (thundering) world.thunderingStrength = (float) ((double) world.thunderingStrength + step);
		else world.thunderingStrength = (float) ((double) world.thunderingStrength - step);

		world.thunderingStrength = MathHelper.clamp(world.thunderingStrength, min, max);
		world.prevRainingStrength = world.rainingStrength;
		if (raining) world.rainingStrength = (float) ((double) world.rainingStrength + step);
		else world.rainingStrength = (float) ((double) world.rainingStrength - step);

		world.rainingStrength = MathHelper.clamp(world.rainingStrength, min, max);
	}

	@Override
	public void resetRainAndThunder() {
		/*rainTime = thunderTime = 0;
		raining = thundering = false;*/
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		if (world instanceof ServerWorld && world.isRaining() && world.isThundering() && world.rand.nextInt(10000) == 0) {
			BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunk.getPos().getXStart(), 0, chunk.getPos().getZStart(), 15));
			/*if (world.isRainingAt(blockpos))*/ {
				DifficultyInstance difficultyinstance = world.getDifficultyForLocation(blockpos);
				boolean flag1 = world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && world.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.01D;
				if (flag1) {
					SkeletonHorseEntity skeletonhorseentity = EntityType.SKELETON_HORSE.create(world);
					skeletonhorseentity.setTrap(true);
					skeletonhorseentity.setGrowingAge(0);
					skeletonhorseentity.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
					world.addEntity(skeletonhorseentity);
				}

				((ServerWorld) world).addLightningBolt(new LightningBoltEntity(world, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, flag1));

				class C {
					void fall(BlockPos pos, Vec3i motion) {
						fall(pos, new Vec3d(motion).add(0, 1, 0)
								.normalize());
					}

					void fall(BlockPos pos, Vec3d motion) {
						BlockState state = world.getBlockState(pos);
						if (state.isAir(world, pos)) return;
						FallingBlockEntity falling;
						CompoundNBT tag = null;
						TileEntity te = world.getTileEntity(pos);
						if (te != null) tag = te.write(new CompoundNBT());
						falling = new FallingBlockEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
						falling.tileEntityData = tag;
						falling.setMotion(motion);
						world.addEntity(falling);
					}
				}
				C falling = new C();

				BlockPos pos = blockpos.down();
				falling.fall(pos, Vec3i.NULL_VECTOR);
				for (int i = 0; i < 4; i++) {
					Direction dir = Direction.byHorizontalIndex(i);
					BlockPos offset = pos.offset(dir);
					falling.fall(offset, dir.getDirectionVec());
				}
			}
		}
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		boolean flag = world.isRaining();
		/*for (int l = 0; l < 16; l++)*/ {
			if (world.rand.nextInt(4) == 0) {
				BlockPos target = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunk.getPos().getXStart(), 0, chunk.getPos().getZStart(), 15));
				BlockPos below = target.down();
				Biome biome = world.getBiome(target);
				if (world.isAreaLoaded(target, 1) && biome.doesWaterFreeze(world, below))
					world.setBlockState(below, Blocks.ICE.getDefaultState());

				if (flag && biome.doesSnowGenerate(world, target)) {
					BlockState state = world.getBlockState(target);
					if (state.getBlock() == Blocks.SNOW) {
						if (state.get(SnowBlock.LAYERS) < 8) {
							world.setBlockState(target, state.cycle(SnowBlock.LAYERS));
						} else {
							final Block[] shift = {
									Blocks.SNOW,
									Blocks.SNOW_BLOCK,
									Blocks.ICE,
									Blocks.PACKED_ICE,
									Blocks.BLUE_ICE
							};
							for (int i = 0; i < shift.length - 1; i++) {
								BlockPos pos = target.down(i);
								if (world.getBlockState(pos).getBlock() == shift[i]) {
									world.setBlockState(pos, shift[i + 1].getDefaultState());
								} else break;
							}
							//world.setBlockState(target, Blocks.SNOW_BLOCK.getDefaultState());

						}
					} else world.setBlockState(target, Blocks.SNOW.getDefaultState());
				}

				if (flag && world.getBiome(below).getPrecipitation() == Biome.RainType.RAIN)
					world.getBlockState(below).getBlock().fillWithRain(world, below);
			}
		}
		return false;
	}

	public void updateCelestials(long worldTime, float partialTicks) {
		getCelestialStream().forEach(celestial -> celestial.update(worldTime, partialTicks));
	}


	public CelestialInfo[] getCelestials() {
		return getCelestialStream().toArray(CelestialInfo[]::new);
	}

	@SuppressWarnings("UnstableApiUsage")
	public Stream<CelestialInfo> getCelestialStream() {
		return Streams.concat(
				getSunStream(),
				getMoonStream(),
				getStarStream()
		);
	}

	private final SunInfo[] suns = {
			new SunInfo() {
				private float brightness;
				@Override
				public void update(long worldTime, float partialTicks) {
					noonAngle = (float)MathHelper.frac((double)worldTime / 24000.0D - 0.25D);

					float f1 = 1.0F - (MathHelper.cos(noonAngle * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
					f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
					f1 = 1.0F - f1;
					f1 = (float)((double)f1 * (1.0D - (double)(getWorld().getRainStrength(partialTicks) * 5.0F) / 16.0D));
					f1 = (float)((double)f1 * (1.0D - (double)(getWorld().getThunderStrength(partialTicks) * 5.0F) / 16.0D));
					brightness = f1 * 0.8F + 0.2F;
				}

				@Override public float getBrightness() { return brightness * (size / 30F); }
			}
	};
	private final MoonInfo[] moons = {
			new MoonInfo() {
				@Override
				public void update(long worldTime, float partialTicks) {
					noonAngle = (float)MathHelper.frac((double)worldTime / 24000.0D - 0.25D);

					phase = (int)(worldTime / 24000L % 8L + 8L) % 8;
				}

				@Override
				protected void init() {
					size = 20F;
				}
			}
	};
	private final StarInfo[] stars = {
			new StarInfo() {
				private float brightness;

				@Override
				public void update(long worldTime, float partialTicks) {
					noonAngle = (float)MathHelper.frac((double)worldTime / 24000.0D - 0.25D);

					float f1 = 1.0F - (MathHelper.cos(noonAngle * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
					f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
					brightness = f1 * f1 * 0.5F;
				}

				@Override public float getBrightness() { return brightness; }
			}
	};

	public SunInfo[] getSuns() { return suns; }
	public MoonInfo[] getMoons() { return moons; }
	public StarInfo[] getStars() { return stars; }

	public Stream<SunInfo> getSunStream() { return Arrays.stream(getSuns()); }
	public Stream<MoonInfo> getMoonStream() { return Arrays.stream(getMoons()); }
	public Stream<StarInfo> getStarStream() { return Arrays.stream(getStars()); }

	public abstract static class CelestialInfo {
		protected float size = 30;
		protected int color = -1;
		protected float noonAngle;
		protected float meridianAngle = 0;

		protected CelestialInfo() { this.init(); }

		protected void init() {}

		abstract void update(long worldTime, float partialTicks);

		public float getSize() { return size; }
		public int getColor() { return color; }

		public float getNoonAngle() { return noonAngle; }
		public float getMeridianAngle() { return meridianAngle; }

		public float getRed() { return ((color >> 16) & 255) / 255F; }
		public float getGreen() { return ((color >> 8) & 255) / 255F; }
		public float getBlue() { return ((color) & 255) / 255F; }
	}

	public abstract static class SunInfo extends CelestialInfo {

		public float getBrightness() { return getSize() / 30F; }
	}

	public abstract static class MoonInfo extends CelestialInfo {
		protected int phase;
		public int getPhase() { return phase; }
	}

	public abstract static class StarInfo extends CelestialInfo {
		public float getBrightness() { return 1F; }
	}
}
