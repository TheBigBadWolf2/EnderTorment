package white_blizz.ender_torment.common.math;

import java.util.Arrays;
import java.util.List;

public class CalcVec3f implements ISelfUpdatingVec3f {
	//public static void main(String[] args) {}

	private final IVec3f x1, x2;
	private final IVec3f y1, y2;
	private final IVec3f z1, z2;

	private float x, y, z;

	private final IVec3f[][] vecs = new IVec3f[3][2];
	private final int[][] mods = new int[3][2];

	public CalcVec3f(
			IVec3f x1, IVec3f x2,
			IVec3f y1, IVec3f y2,
			IVec3f z1, IVec3f z2
	) {
		this.x1 = vecs[0][0] = x1;
		this.x2 = vecs[0][1] = x2;
		this.y1 = vecs[1][0] = y1;
		this.y2 = vecs[1][1] = y2;
		this.z1 = vecs[2][0] = z1;
		this.z2 = vecs[2][1] = z2;
		for (int[] mod : mods) { Arrays.fill(mod, -1); }
		recalculate();
	}

	private boolean needsRecalculation() {
		boolean needsUpdate = false;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				int last = mods[i][j];
				IVec3f vec = vecs[i][j];
				int cur = 0;
				if (vec instanceof ModCountTracker) cur = ((ModCountTracker) vec).getMods();
				needsUpdate |= last != cur;
				mods[i][j] = cur;
			}
		}
		return needsUpdate;
	}

	private static class Line {
		private final StaticVec3f p, d;
		private final float t;

		public Line(IVec3f vec1, IVec3f vec2) {
			StaticVec3f a = vec1.toStatic();
			StaticVec3f b = vec2.toStatic();
			p = a;
			StaticVec3f d = b.sub(a);
			t = d.length();
			this.d = d.normalized();
		}
	}

	private static IVec3f mid(Line v1, Line v2) {
		StaticVec3f n = v1.d.cross(v2.d);
		StaticVec3f n1 = v1.d.cross(n);
		StaticVec3f n2 = v2.d.cross(n);

		StaticVec3f c1 = v1.p.add(
				v2.p.sub(v1.p).mul(n2)
				.div(v1.d.mul(n2))
				.mulNaNCheck(v1.d)
		);
		StaticVec3f c2 = v2.p.add(
				v1.p.sub(v2.p).mul(n1)
				.div(v2.d.mul(n1))
				.mulNaNCheck(v2.d)
		);

		return c1.lerp(c2, 0.5F);
	}

	private void recalculate() {
		if (needsRecalculation()) {
			x = mid(new Line(y1, y2), new Line(z1, z2)).getX();
			y = mid(new Line(x1, x2), new Line(z1, z2)).getY();
			z = mid(new Line(x1, x2), new Line(y1, y2)).getZ();
		}
	}

	@Override public float getX() { return x; }
	@Override public float getY() { return y; }
	@Override public float getZ() { return z; }
}
