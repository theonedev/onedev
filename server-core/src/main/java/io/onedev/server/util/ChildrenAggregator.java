package io.onedev.server.util;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

public abstract class ChildrenAggregator<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public List<T> getAggregatedChildren(@Nullable T node) {
		var children = getChildren(node);
		for (int i=0; i<children.size(); i++) {
			var child = children.get(i);
			while (true) {
				var childChildren = getChildren(child);
				if (childChildren.size() == 1) {
					child = childChildren.iterator().next();
					children.set(i, child);
				} else {
					break;
				}
			}
		}
		return children;
	}
	
	protected abstract List<T> getChildren(@Nullable T node);
	
}

