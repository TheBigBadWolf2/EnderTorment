package white_blizz.ender_torment.utils;

import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkRocketItem.Shape;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FireworkMaker {

	public static FireworkMaker New() { return new FireworkMaker(); }

	private byte flight;
	private final List<Explosion> explosions = new ArrayList<>();

	private FireworkMaker() {}

	public class Explosion {
		private boolean flicker, trail;
		private Shape type = Shape.SMALL_BALL;
		private final List<Integer> colors = new ArrayList<>(), fadeColors = new ArrayList<>();

		private Explosion() { explosions.add(this); }

		public Explosion setFlicker() {
			flicker = true;
			return this;
		}

		public Explosion setTrail() {
			trail = true;
			return this;
		}

		public Explosion setSmallBall() { return setType(Shape.SMALL_BALL); }
		public Explosion setLargeBall() { return setType(Shape.LARGE_BALL); }
		public Explosion setStar() { return setType(Shape.STAR); }
		public Explosion setCreeper() { return setType(Shape.CREEPER); }
		public Explosion setBurst() { return setType(Shape.BURST); }

		public Explosion setType(Shape type) {
			this.type = type;
			return this;
		}

		public Explosion addColor(int rgb) {
			colors.add(rgb);
			return this;
		}

		public Explosion addFadeColor(int rgb) {
			fadeColors.add(rgb);
			return this;
		}

		public FireworkMaker done() { return FireworkMaker.this; }

		private CompoundNBT save() {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean("Flicker", flicker);
			tag.putBoolean("Trail", trail);
			tag.putByte("Type", (byte) type.getIndex());
			tag.putIntArray("Colors", colors);
			tag.putIntArray("FadeColors", fadeColors);
			return tag;
		}
	}

	public FireworkMaker setFlight(byte flight) {
		this.flight = flight;
		return this;
	}

	public Explosion newStar() { return new Explosion(); }

	private CompoundNBT save() {
		CompoundNBT tag = new CompoundNBT();
		tag.putByte("Flight", flight);
		tag.put("Explosions", explosions
				.stream()
				.map(Explosion::save)
				.collect(ListNBT::new, AbstractList::add, AbstractCollection::addAll)
		);
		return tag;
	}

	public ItemStack makeItem() {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		stack.setTagInfo("Fireworks", save());
		return stack;
	}

	public FireworkRocketEntity spawn(World world, Vec3d pos) {
		return spawn(world, pos, null);
	}
	public FireworkRocketEntity spawn(
			World world, Vec3d pos,
			ISpawnHelper<FireworkRocketEntity> spawn) {
		return spawn(world, pos, false, spawn);
	}

	public FireworkRocketEntity spawn(
			World world, Vec3d pos, boolean atAngle,
			ISpawnHelper<FireworkRocketEntity> spawn) {
		FireworkRocketEntity rocket;
		if (atAngle) rocket = new FireworkRocketEntity(
				world, makeItem(),
				pos.x, pos.y, pos.z,
				true
		);
		else rocket = new FireworkRocketEntity(world, pos.x, pos.y, pos.z, makeItem());
		if (spawn != null) spawn.pre(rocket);
		world.addEntity(rocket);
		if (spawn != null) spawn.post(rocket);
		return rocket;
	}
}
