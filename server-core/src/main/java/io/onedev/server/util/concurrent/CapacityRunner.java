package io.onedev.server.util.concurrent;

import java.util.concurrent.Callable;

import io.onedev.commons.utils.ExceptionUtils;

public class CapacityRunner {
	
	private final int capacity;
	
	private int active;
	
	public CapacityRunner(int capacity) {
		this.capacity = capacity;
	}
	
	public <T> T call(Callable<T> callable) {
		synchronized (this) {
			while (active >= capacity) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
			active++;
		}
		
		try {
			return callable.call();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			synchronized (this) {
				active--;
				notify();
			}
		}
	}

	public void run(Runnable runnable) {
		call(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
			
		});
	}
	
}
