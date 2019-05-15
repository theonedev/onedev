package io.onedev.server.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MatrixRunner {

	private final Map<String, String> params;
	
	private final Map<String, List<String>> paramMatrix;
	
	public MatrixRunner(Map<String, List<String>> paramMatrix) {
		this(new LinkedHashMap<>(), paramMatrix);
	}
	
	private MatrixRunner(Map<String, String> params, Map<String, List<String>> paramMatrix) {
		this.params = params;
		this.paramMatrix = paramMatrix;
	}
	
	public void run() {
		if (!paramMatrix.isEmpty()) {
			Map.Entry<String, List<String>> entry = paramMatrix.entrySet().iterator().next();
			for (String value: entry.getValue()) {
				Map<String, String> paramsCopy = new LinkedHashMap<>(params);
				paramsCopy.put(entry.getKey(), value);
				Map<String, List<String>> matrixCopy = new LinkedHashMap<>(paramMatrix);
				matrixCopy.remove(entry.getKey());
				new MatrixRunner(paramsCopy, matrixCopy) {

					@Override
					protected void run(Map<String, String> paramMap) {
						MatrixRunner.this.run(paramMap);
					}
					
				}.run();
			}
		} else {
			run(params);
		}
	}
	
	protected abstract void run(Map<String, String> paramMap);
	
}
