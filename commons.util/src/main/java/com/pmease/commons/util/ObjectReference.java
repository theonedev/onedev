package com.pmease.commons.util;

import com.google.common.base.Preconditions;

public abstract class ObjectReference<T> {
	
	private int count;
	
	private T object;

	protected abstract T openObject();
	
	protected abstract void closeObject(T object);
	
	public synchronized T open() {
		if (count == 0)
			object = openObject();
		count++;
		return object;
	}

	public synchronized void close() {
		if (count == 1) {
			closeObject(object);
			object = null;
		}
		count--;
	}
	
	public synchronized T get() {
		return Preconditions.checkNotNull(object);
	}
	
}
