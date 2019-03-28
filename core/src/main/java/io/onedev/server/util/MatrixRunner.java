package io.onedev.server.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatrixRunner {

	private final Map<String, String> params;
	
	private final Map<String, List<String>> paramMatrix;
	
	public MatrixRunner(Map<String, List<String>> paramMatrix) {
		this(new LinkedHashMap<>(), paramMatrix);
	}
	
	private MatrixRunner(Map<String, String> params, Map<String, List<String>> paramMatrix) {
		this.params = params;
		this.paramMatrix = paramMatrix;
	}
	
	public void run(Runnable runnable) {
		if (!paramMatrix.isEmpty()) {
			Map.Entry<String, List<String>> entry = paramMatrix.entrySet().iterator().next();
			for (String value: entry.getValue()) {
				Map<String, String> paramsCopy = new LinkedHashMap<>(params);
				paramsCopy.put(entry.getKey(), value);
				Map<String, List<String>> matrixCopy = new LinkedHashMap<>(paramMatrix);
				matrixCopy.remove(entry.getKey());
				new MatrixRunner(paramsCopy, matrixCopy).run(runnable);
			}
		} else {
			runnable.run(params);
		}
	}
	
	public static interface Runnable {
		
		void run(Map<String, String> params);
		
	}
	
}
