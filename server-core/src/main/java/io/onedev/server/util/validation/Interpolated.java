package io.onedev.server.util.validation;

import java.util.Stack;

public class Interpolated {
	
	private static ThreadLocal<Stack<Boolean>> stack =  new ThreadLocal<Stack<Boolean>>() {

		@Override
		protected Stack<Boolean> initialValue() {
			return new Stack<Boolean>();
		}
	
	};
	
	public static void push(boolean interpolated) {
		stack.get().push(interpolated);
	}

	public static void pop() {
		stack.get().pop();
	}

	public static boolean get() {
		if (!stack.get().isEmpty()) 
			return stack.get().peek();
		else
			return false;
	}
	
}
