package io.onedev.server.util;

import java.io.Serializable;
import java.util.Stack;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;

import io.onedev.server.web.util.WicketUtils;

public class ComponentContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<Stack<ComponentContext>> stack =  new ThreadLocal<Stack<ComponentContext>>() {

		@Override
		protected Stack<ComponentContext> initialValue() {
			return new Stack<ComponentContext>();
		}
	
	};
	
	private Component component;
	
	public ComponentContext(Component component) {
		this.component = component;
	}
	
	public static void push(ComponentContext context) {
		stack.get().push(context);
	}

	public static void pop() {
		stack.get().pop();
	}

	@Nullable
	public static ComponentContext get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			Page page = WicketUtils.getPage();
			return page!=null? new ComponentContext(page): null;
		}
	}
	
	public Component getComponent() {
		return component;
	}
	
	@Nullable
	public ComponentContext getChildContext(String childName) {
		return null;
	}
	
}
