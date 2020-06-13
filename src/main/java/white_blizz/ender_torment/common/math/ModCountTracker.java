package white_blizz.ender_torment.common.math;

abstract class ModCountTracker {
	private int mods = 0;

	protected ModCountTracker() {}

	protected void mod() { ++mods; }
	public int getMods() { return mods; }
}
