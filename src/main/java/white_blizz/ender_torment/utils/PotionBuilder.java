package white_blizz.ender_torment.utils;

import com.google.common.collect.Lists;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PotionBuilder {
	public static class DurAmp {
		private final int dur;
		private final int amp;

		public DurAmp(int dur, int amp) {
			this.dur = dur;
			this.amp = amp;
		}
	}

	private final Effect effect;
	private final boolean ambient;
	private final boolean showParticles;
	private final boolean showIcon;
	private final List<DurAmp> durations = new ArrayList<>();

	public PotionBuilder(Effect effect, boolean ambient, boolean showParticles, boolean showIcon) { this.effect = effect;
		this.ambient = ambient;
		this.showParticles = showParticles;
		this.showIcon = showIcon;
	}

	public PotionBuilder map(Function<Integer, DurAmp> op) {
		int last = -1;
		if (!durations.isEmpty()) {
			last = durations.get(durations.size() - 1).amp;
		}
		DurAmp dur;
		while ((dur = op.apply(last)) != null) {
			durations.add(dur);
			last = dur.amp;
		}
		return this;
	}

	public EffectInstance build() {
		AtomicInteger time = new AtomicInteger();


		return Lists.reverse(
			durations.stream()
					.map(durAmp -> new DurAmp(time.addAndGet(durAmp.dur), durAmp.amp))
					.collect(Collectors.toList())
		).stream().reduce(
				null,
				(eff, dur) -> new EffectInstance(
						effect,
						dur.dur, dur.amp,
						ambient, showParticles, showIcon,
						eff
				),
				(a, b) -> a
		);

		/*for (int i = durations.size() - 1; i >= 0; i--) {
			DurAmp durAmp = durations.get(i);
			time.addAndGet(durAmp.dur);
			instance = new EffectInstance(
					effect,
					time.get(), durAmp.amp,
					true, true, true,
					instance
			);
		}

		return instance;*/
	}
}
