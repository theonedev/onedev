package io.onedev.server.util;

import java.util.Stack;

import javax.annotation.Nullable;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputContext;

public abstract class OneContext {

	private static ThreadLocal<Stack<OneContext>> stack =  new ThreadLocal<Stack<OneContext>>() {

		@Override
		protected Stack<OneContext> initialValue() {
			return new Stack<OneContext>();
		}
	
	};
	
	public static void push(OneContext context) {
		stack.get().push(context);
	}

	public static void pop() {
		stack.get().pop();
	}

	@Nullable
	public static OneContext get() {
		if (!stack.get().isEmpty()) 
			return stack.get().peek();
		else
			return null;
	}
	
	public <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}
	
	public String getDocLink() {
		return OneDev.getInstance().getDocLink();
	}

	public EditContext getEditContext() {
		return getEditContext(0);
	}
	
	public abstract Project getProject();

	public abstract EditContext getEditContext(int level);

	public abstract InputContext getInputContext();
	
}
