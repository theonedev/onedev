package io.onedev.server.plugin.report.jest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;

public class JestTestReportData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static enum Status {
		
		PASSED("#1BC5BD"), FAILED("#F64E60"), SKIPPED("#8950FC"), TODO("#3699FF");
		
		private final String color;
		
		Status(String color) {
			this.color = color;
		};

		public String getColor() {
			return color;
		}
		
	};
	
	private static final String FILE_NAME = "data.ser";
			
	private final List<TestSuite> testSuites;
	
	public JestTestReportData(Build build, Collection<JsonNode> rootNodes) {
		testSuites = new ArrayList<>();
		for (JsonNode rootNode: rootNodes) {
			for (JsonNode testSuiteNode: rootNode.get("testResults")) 
				testSuites.add(new TestSuite(build, testSuiteNode));
		}
	}
	
	public List<TestSuite> getTestSuites(@Nullable PatternSet filePatterns, Collection<Status> statuses) {
		Matcher matcher = new PathMatcher();
		return testSuites.stream().filter(it->{
			return (filePatterns == null || filePatterns.matches(matcher, it.getName())) 
					&& (statuses == null || statuses.contains(it.getStatus()));
		}).collect(Collectors.toList());
	}
	
	public List<TestCase> getTestCases(@Nullable PatternSet filePatterns, 
			@Nullable PatternSet namePatterns, Collection<Status> statuses) {
		Matcher matcher = new PathMatcher();
		
		return  testSuites.stream().flatMap(it->it.getTestCases().stream()).filter(it->{
			return (filePatterns == null || filePatterns.matches(matcher, it.getTestSuite().getName()))
					&& (namePatterns == null || namePatterns.matches(matcher, it.getName()))
					&& (statuses == null || statuses.contains(it.getStatus()));
		}).collect(Collectors.toList());
	}
	
	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	@Nullable
	public static JestTestReportData readFrom(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(dataFile))) {
			return (JestTestReportData) SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile))) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getNumOfTestSuites() {
		return testSuites.size();
	}
	
	public int getNumOfTestCases() {
		int numOfTestCases = 0;
		for (TestSuite testSuite: testSuites)
			numOfTestCases += testSuite.getTestCases().size();
		return numOfTestCases;
	}
	
	public int getTotalTestDuration() {
		int totalTestDuration = 0;
		for (TestSuite testSuite: testSuites)
			totalTestDuration += testSuite.getDuration()/1000;
		return totalTestDuration;
	}
	
	public int getTestSuiteSuccessRate() {
		int numOfFailed = 0;
		int numOfPassed = 0;
		for (TestSuite testSuite: testSuites) {
			if (testSuite.getStatus() == Status.FAILED) 
				numOfFailed++;
			else if (testSuite.getStatus() == Status.PASSED)
				numOfPassed++;
		}
		return numOfPassed*100/(numOfPassed+numOfFailed);
	}
	
	public int getTestCaseSuccessRate() {
		int numOfFailed = 0;
		int numOfPassed = 0;
		for (TestSuite testSuite: testSuites) {
			for (TestCase testCase: testSuite.getTestCases()) {
				if (testCase.getStatus() == Status.FAILED) 
					numOfFailed++;
				else if (testCase.getStatus() == Status.PASSED)
					numOfPassed++;
			}
		}
		return numOfPassed*100/(numOfPassed+numOfFailed);
	}
	
	public static class TestSuite implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final String name;
		
		private final Status status;
		
		private final long duration;
		
		private final String message;
		
		private final List<TestCase> testCases;
		
		public TestSuite(Build build, JsonNode rootNode) {
			String name = rootNode.get("name").asText();
			if (build.getJobWorkspace() != null && name.startsWith(build.getJobWorkspace()))
				name = name.substring(build.getJobWorkspace().length()+1);
			
			this.name = name;
			
			String message = rootNode.get("message").asText(null);
			if (StringUtils.isBlank(message))
				message = null;
			this.message = message;
			
			duration = rootNode.get("endTime").asLong() - rootNode.get("startTime").asLong();
			
			testCases = new ArrayList<>();
			for (JsonNode testCaseNode: rootNode.get("assertionResults")) 
				testCases.add(new TestCase(this, testCaseNode));

			switch(rootNode.get("status").asText()) {
			case "pending":
				status = Status.SKIPPED;
				break;
			case "todo":
				status = Status.TODO;
				break;
			case "failed":
				status = Status.FAILED;
				break;
			default:
				if (!testCases.isEmpty()) {
					if (testCases.stream().allMatch(it->it.getStatus() == Status.TODO))
						status = Status.TODO;
					else if (testCases.stream().allMatch(it->it.getStatus() == Status.SKIPPED))
						status = Status.SKIPPED;
					else
						status = Status.PASSED;
				} else {
					status = Status.PASSED;
				}
			}
		}

		public String getName() {
			return name;
		}

		public long getDuration() {
			return duration;
		}

		public List<TestCase> getTestCases() {
			return testCases;
		}

		public Status getStatus() {
			return status;
		}
		
		@Nullable
		public String getMessage() {
			return message;
		}
		
	}
	
	public static class TestCase implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final TestSuite testSuite;
		
		private final String name;
		
		private final Status status;
		
		private final List<String> messages;
		
		public TestCase(TestSuite testSuite, JsonNode rootNode) {
			this.testSuite = testSuite;
			
			StringBuilder nameBuilder = new StringBuilder();
			for (JsonNode ancestorTitleNode: rootNode.get("ancestorTitles")) 
				nameBuilder.append(ancestorTitleNode.asText()).append("/");
			nameBuilder.append(rootNode.get("title").asText());
			
			name = nameBuilder.toString();

			messages = new ArrayList<>();
			for (JsonNode messageNode: rootNode.get("failureMessages")) 
				messages.add(messageNode.asText());
			
			switch (rootNode.get("status").asText()) {
			case "passed":
				status = Status.PASSED;
				break;
			case "pending":
				status = Status.SKIPPED;
				break;
			case "todo":
				status = Status.TODO;
				break;
			default:
				status = Status.FAILED;
			}
		}

		public TestSuite getTestSuite() {
			return testSuite;
		}

		public String getName() {
			return name;
		}

		public Status getStatus() {
			return status;
		}

		public List<String> getMessages() {
			return messages;
		}
		
	}
}
