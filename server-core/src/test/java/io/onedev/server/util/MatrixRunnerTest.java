package io.onedev.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

public class MatrixRunnerTest {

	@Test
	public void test() {
		Map<String, List<String>> matrix = new LinkedHashMap<>();
		
		List<Map<String, String>> usedParams = new ArrayList<>();
		new MatrixRunner<String>(matrix) {
			
			@Override
			public void run(Map<String, String> params) {
				usedParams.add(params);
			}
			
		}.run();
		
		assertEquals(1, usedParams.size());
		assertTrue(usedParams.iterator().next().isEmpty());
		
		matrix.put("db", Lists.newArrayList("oracle", "mysql"));
		matrix.put("os", Lists.newArrayList("windows", "linux"));
		
		usedParams.clear();
		
		new MatrixRunner<String>(matrix) {
			
			@Override
			public void run(Map<String, String> params) {
				usedParams.add(params);
			}
			
		}.run();
		assertEquals(4, usedParams.size());
		Map<String, String> params = new HashMap<>();
		params.put("db", "oracle");
		params.put("os", "windows");
		assertEquals(params, usedParams.get(0));

		params.put("db", "oracle");
		params.put("os", "linux");
		assertEquals(params, usedParams.get(1));
		
		params.put("db", "mysql");
		params.put("os", "windows");
		assertEquals(params, usedParams.get(2));

		params.put("db", "mysql");
		params.put("os", "linux");
		assertEquals(params, usedParams.get(3));
	}

}
