package com.pmease.commons.loader;

public class InheritableThreadLocalData {
	
	private static ThreadLocal<Object> valueHolder = new ThreadLocal<Object>(){

		@Override
		protected Object initialValue() {
			return null;
		}
		
	};
	
	public static Object get() {
		return valueHolder.get();
	}
	
	public static void set(Object value) {
		valueHolder.set(value);
	}
	
	public static void clear() {
		valueHolder.remove();
	}
	
}
