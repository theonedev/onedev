package com.pmease.commons.util;

import com.google.common.base.Preconditions;

public abstract class ObjectReference<T> {
	
	private int count = 0;
	
	private T object = null;

	protected abstract T openObject();
	
	public synchronized T getObject() {
		Preconditions.checkState(count > 0, "Reference count has to be increased first.");
		
		if (object == null) {
			object = openObject();
			Preconditions.checkState(object != null);
		}
		
		return object;
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

	protected abstract void closeObject(T object);
}
