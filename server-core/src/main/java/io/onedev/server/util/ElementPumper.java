package io.onedev.server.util;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;

/**
 * A pumper generates elements in a different thread, and consumes the element 
 * in current thread. It is designed as some scenario requires that elements 
 * should be consumed in current thread, and collecting all generated elements 
 * into a collection before processing is not acceptable due to memory constraint
 * 
 * @author robin
 *
 * @param <T>
 */
public abstract class ElementPumper<T> {

	public void pump() {
		SynchronousQueue<Optional<T>> queue = new SynchronousQueue<>(); 
		AtomicReference<Exception> exceptionRef = new AtomicReference<>(null);

		OneDev.getInstance(ExecutorService.class).execute(new Runnable() {

			@Override
			public void run() {
				try {
					generate(new Consumer<T>() {

						@Override
						public void accept(T t) {
							try {
								queue.put(Optional.of(t));
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
						
					});
				} catch (Exception e) {
					exceptionRef.set(e);
				} finally {
					try {
						queue.put(Optional.empty());
					} catch (InterruptedException e) {
					}
				}
			}
			
		});
		
		try {
			Optional<T> element = queue.take();
			while (element.isPresent()) {
				process(element.get());
				element = queue.take();
			}
			if (exceptionRef.get() != null)
				throw exceptionRef.get();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}		
	}
	
	public abstract void generate(Consumer<T> consumer);
	
	public abstract void process(T element);
	
}
