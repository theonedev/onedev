package io.onedev.server.util;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.util.WicketUtils;

public interface EditContext {
	
	Object getInputValue(String name);

	public static EditContext get(int level) {
		return list().get(level);
	}
	
	public static EditContext get() {
		return get(0);
	}

	public static List<EditContext> list() {
		ComponentContext componentContext = ComponentContext.get();
		if (componentContext != null)
			return WicketUtils.findParents(componentContext.getComponent(), EditContext.class);
		else
			return new ArrayList<>();
	}
}
