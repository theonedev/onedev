package io.onedev.server.util;

import java.io.Closeable;

public abstract class ObjectReference<T> implements Closeable {
	
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

	@Override
	public synchronized void close() {
		if (count == 1) {
			closeObject(object);
			object = null;
		}
		count--;
	}
	
	public synchronized T get() {
		if (object == null)
			throw new RuntimeException("Objerct not initialized yet");
		else
			return object;
	}
	
}
