package io.onedev.server.plugin.report.jest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.jest.JestTestReportData.Status;
import io.onedev.server.util.patternset.PatternSet;

public class JestReportDataTest {

	@Test
	public void test() {
		try (InputStream is = Resources.getResource(JestReportDataTest.class, "testResults.json").openStream()) {
			JsonNode rootNode = new ObjectMapper().readTree(is);
			Collection<JsonNode> rootNodes = new ArrayList<>();
			rootNodes.add(rootNode);
			
			Build build = new Build();
			build.setJobWorkspace("/Users/robin/Projects/onedev/reports/jest-demo");
			JestTestReportData reportData = new JestTestReportData(build, rootNodes);
			assertEquals(1, reportData.getTestCases(null, null, Sets.newHashSet(Status.FAILED)).size());
			assertEquals(1, reportData.getTestCases(null, null, Sets.newHashSet(Status.SKIPPED)).size());
			assertEquals(1, reportData.getTestCases(null, null, Sets.newHashSet(Status.TODO)).size());
			assertEquals(4, reportData.getTestCases(null, null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(7, reportData.getTestCases(null, null, null).size());
			assertEquals(1, reportData.getTestCases(
					new PatternSet(Sets.newHashSet("src/utils/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("mul"), new HashSet<>()), null).size());
			assertEquals(1, reportData.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("should return the product"), new HashSet<>()), null).size());
			assertEquals(2, reportData.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("NewsContent/"), new HashSet<>()), null).size());
			assertEquals(5, reportData.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet(), Sets.newHashSet("NewsContent/")), null).size());
			
			assertEquals(1, reportData.getTestSuites(null, Sets.newHashSet(Status.FAILED)).size());
			assertEquals(0, reportData.getTestSuites(null, Sets.newHashSet(Status.SKIPPED)).size());
			assertEquals(0, reportData.getTestSuites(null, Sets.newHashSet(Status.TODO)).size());
			assertEquals(3, reportData.getTestSuites(null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(4, reportData.getTestSuites(null, null).size());
			assertEquals(4, reportData.getTestSuites(new PatternSet(Sets.newHashSet("**"), new HashSet<>()), null).size());
			assertEquals(1, reportData.getTestSuites(new PatternSet(Sets.newHashSet("src/components/"), new HashSet<>()), null).size());
			assertEquals(3, reportData.getTestSuites(new PatternSet(Sets.newHashSet("src/"), Sets.newHashSet("src/components/")), null).size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

}
