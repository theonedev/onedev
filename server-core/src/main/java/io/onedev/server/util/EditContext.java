package io.onedev.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.web.util.WicketUtils;

public interface EditContext {

	static ThreadLocal<Stack<EditContext>> stack = ThreadLocal.withInitial(Stack::new);

	public static void push(EditContext context) {
		stack.get().push(context);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	Object getInputValue(String name);

	@Nullable
	public static EditContext get(int level) {
		List<EditContext> list = list();
		if (list.size() > level)
			return list().get(level);
		else
			return null;
	}

	@Nullable
	public static EditContext get() {
		return get(0);
	}

	public static List<EditContext> list() {
		if (!stack.get().isEmpty()) {
			var list = new ArrayList<>(stack.get());
			Collections.reverse(list);
			return list;
		} 
		
		ComponentContext componentContext = ComponentContext.get();
		if (componentContext != null)
			return WicketUtils.findParents(componentContext.getComponent(), EditContext.class);
		else
			return new ArrayList<>();
	}
}
