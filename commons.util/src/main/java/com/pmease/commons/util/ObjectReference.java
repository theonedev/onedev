package com.pmease.commons.util;

import com.google.common.base.Preconditions;

public abstract class ObjectReference<T> {
	
	private int count = 0;
	
	private T object = null;

	protected abstract T openObject();
	
	public synchronized T getObject() {
		if (object != null) {
			return object;
		} else if (count > 0) {
			object = openObject();
			Preconditions.checkNotNull(object);
			return object;
		} else {
			return null;
		}
		
	}
	
	public synchronized void increase() {
		count++;
	}
	
	public synchronized void decrease() {
		if (count > 0)
			count--;
		if (count == 0 && object != null) {
			closeObject(object);
			object = null;
		}
	}
	
	public synchronized void reset() {
		count = 0;
		if (object != null) {
			closeObject(object);
			object = null;
		}
	}

	protected abstract void closeObject(T object);
}
