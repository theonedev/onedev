package io.onedev.server.util.script.identity;

import java.util.Stack;

import javax.annotation.Nullable;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;

public interface ScriptIdentity {
	
	static ThreadLocal<Stack<ScriptIdentity>> stack =  new ThreadLocal<Stack<ScriptIdentity>>() {

		@Override
		protected Stack<ScriptIdentity> initialValue() {
			return new Stack<ScriptIdentity>();
		}
	
	};
	
	public static void push(ScriptIdentity scriptIdentity) {
		stack.get().push(scriptIdentity);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static ScriptIdentity get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				ScriptIdentityAware scriptIdentityAware = WicketUtils.findInnermost(componentContext.getComponent(), ScriptIdentityAware.class);
				if (scriptIdentityAware != null) 
					return scriptIdentityAware.getScriptIdentity();
			}
			return null;
		}
	}
	
}
