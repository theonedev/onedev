package io.onedev.server.util.concurrent;

public class DynamicSemaphore {

	private int maxPermits;

	private int usedPermits;

	public DynamicSemaphore() {
		maxPermits = 0;
	}

	public synchronized void setMaxPermits(int maxPermits) {
		this.maxPermits = maxPermits;
		notifyAll();
	}

	public synchronized void acquire() throws InterruptedException {
		while (usedPermits >= maxPermits)
			wait();
		usedPermits++;
	}

	public synchronized void release() {
		usedPermits--;
		notifyAll();
	}

}
