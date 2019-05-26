package io.onedev.server.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MatrixRunner<T> {

	private final Map<String, T> paramMap;
	
	private final Map<String, List<T>> paramMatrix;
	
	public MatrixRunner(Map<String, List<T>> paramMatrix) {
		this(new LinkedHashMap<>(), paramMatrix);
	}
	
	private MatrixRunner(Map<String, T> paramMap, Map<String, List<T>> paramMatrix) {
		this.paramMap = paramMap;
		this.paramMatrix = paramMatrix;
	}
	
	public void run() {
		if (!paramMatrix.isEmpty()) {
			Map.Entry<String, List<T>> entry = paramMatrix.entrySet().iterator().next();
			for (T value: entry.getValue()) {
				Map<String, T> paramsCopy = new LinkedHashMap<>(paramMap);
				paramsCopy.put(entry.getKey(), value);
				Map<String, List<T>> matrixCopy = new LinkedHashMap<>(paramMatrix);
				matrixCopy.remove(entry.getKey());
				new MatrixRunner<T>(paramsCopy, matrixCopy) {

					@Override
					protected void run(Map<String, T> paramMap) {
						MatrixRunner.this.run(paramMap);
					}
					
				}.run();
			}
		} else {
			run(paramMap);
		}
	}
	
	protected abstract void run(Map<String, T> paramMap);
	
}
