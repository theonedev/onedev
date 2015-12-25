package com.pmease.commons.util;

import com.google.common.base.Preconditions;

public abstract class ObjectReference<T> {
	
	private int count;
	
	private T object;

	protected abstract T openObject();
	
	protected abstract void closeObject(T object);
	
	public synchronized T open() {
		if (count++ == 0)
			object = openObject();
		return object;
	}

	public synchronized void close() {
		if (--count == 0) {
			closeObject(object);
			object = null;
		}
	}
	
	public synchronized T get() {
		return Preconditions.checkNotNull(object);
	}
	
}
