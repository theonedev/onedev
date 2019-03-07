package io.onedev.server.util;

import java.util.Stack;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.google.common.base.Preconditions;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;

public class OneContext {

	private static ThreadLocal<Stack<OneContext>> stack =  new ThreadLocal<Stack<OneContext>>() {

		@Override
		protected Stack<OneContext> initialValue() {
			return new Stack<OneContext>();
		}
	
	};
	
	private Component component;
	
	public OneContext(Component component) {
		this.component = component;
	}
	
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
		return OneDev.getInstance().getDocRoot();
	}

	public EditContext getEditContext() {
		return getEditContext(0);
	}
	
	public Project getProject() {
		ProjectAware projectAware = component.findParent(ProjectAware.class);
		if (projectAware != null) 
			return projectAware.getProject();
		else
			return null;
	}

	public EditContext getEditContext(int level) {
		return WicketUtils.findParents(getComponent(), EditContext.class).get(level);
	}

	public InputContext getInputContext() {
		return Preconditions.checkNotNull(WicketUtils.findInnermost(component, InputContext.class));
	}
	
	public Component getComponent() {
		return component;
	}
	
	@Nullable
	public OneContext getPropertyContext(String propertyName) {
		return null;
	}
	
}
