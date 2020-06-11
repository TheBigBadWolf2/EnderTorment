package white_blizz.ender_torment.common.conduit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Tracer<Cap> {
	private final List<Link<Cap>> connected = new ArrayList<>();
	private final List<Link<Cap>> collection = new ArrayList<>();
	private boolean done = false;

	public Tracer(Link<Cap> origin) { trace(origin); }

	private void trace(Link<Cap> link) {
		if (done || connected.contains(link)) return;
		connected.add(link);
		if (shouldCollect(link)) collection.add(link);
		if (hasCollectedAll(collection)) {
			done = true;
			return;
		}
		link.getLinks().values()
				.stream()
				.filter(Link::validate)
				.forEach(this::trace);
	}

	protected abstract boolean shouldCollect(Link<Cap> link);
	protected abstract boolean hasCollectedAll(Collection<Link<Cap>> links);


	public final Collection<Link<Cap>> get() { return collection; }
}
