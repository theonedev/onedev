package io.onedev.server.plugin.report.jest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.util.patternset.PatternSet;

public class JestReportParserTest {

	@Test
	public void test() {
		try (InputStream is = Resources.getResource(JestReportParserTest.class, "testResults.json").openStream()) {
			JsonNode rootNode = new ObjectMapper().readTree(is);
			
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath.substring("/Users/robin/Projects/onedev/reports/jest-demo/".length());
				}
			};
			UnitTestReport report = new UnitTestReport(JestReportParser.parse(build, rootNode), false);
			assertEquals(1, report.getTestCases(null, null, Sets.newHashSet(Status.NOT_PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newHashSet(Status.NOT_RUN)).size());
			assertEquals(4, report.getTestCases(null, null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(7, report.getTestCases(null, null, null).size());
			assertEquals(1, report.getTestCases(
					new PatternSet(Sets.newHashSet("src/utils/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("mul"), new HashSet<>()), null).size());
			assertEquals(1, report.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("should return the product"), new HashSet<>()), null).size());
			assertEquals(2, report.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet("NewsContent/"), new HashSet<>()), null).size());
			assertEquals(5, report.getTestCases(
					new PatternSet(Sets.newHashSet("src/"), new HashSet<>()), 
					new PatternSet(Sets.newHashSet(), Sets.newHashSet("NewsContent/")), null).size());
			
			assertEquals(1, report.getTestSuites(null, Sets.newHashSet(Status.NOT_PASSED)).size());
			assertEquals(1, report.getTestSuites(null, Sets.newHashSet(Status.NOT_RUN)).size());
			assertEquals(2, report.getTestSuites(null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(4, report.getTestSuites(null, null).size());
			assertEquals(4, report.getTestSuites(new PatternSet(Sets.newHashSet("**"), new HashSet<>()), null).size());
			assertEquals(1, report.getTestSuites(new PatternSet(Sets.newHashSet("src/components/"), new HashSet<>()), null).size());
			assertEquals(3, report.getTestSuites(new PatternSet(Sets.newHashSet("src/"), Sets.newHashSet("src/components/")), null).size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

}
