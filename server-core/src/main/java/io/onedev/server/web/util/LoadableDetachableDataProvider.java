package io.onedev.server.web.util;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;

@SuppressWarnings("serial")
public abstract class LoadableDetachableDataProvider<T, S> extends SortableDataProvider<T, S> {
	
	private Long size;
	
	@Override
	public final long size() {
		if (size == null)
			size = calcSize();
		return size;
	}

	@Override
	public void detach() {
		size = null;
	}

	protected abstract long calcSize();
}
