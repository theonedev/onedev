package io.onedev.server.plugin.report.unittest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;

public class UnitTestReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "unit-test";
	
	private static final String FILE_NAME = "report.ser";
	
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
	
	private final List<TestCase> testCases;
	
	private final boolean hasTestCaseDuration;
	
	private transient List<TestSuite> testSuites;
	
	public UnitTestReport(List<TestCase> testCases, boolean hasTestCaseDuration) {
		this.testCases = testCases;
		this.hasTestCaseDuration = hasTestCaseDuration;
	}
	
	public List<TestSuite> getTestSuites(@Nullable PatternSet filePatterns, Collection<Status> statuses) {
		Matcher matcher = new PathMatcher();
		return getTestSuites().stream().filter(it-> {
			return (filePatterns == null || filePatterns.matches(matcher, it.getName())) 
					&& (statuses == null || statuses.contains(it.getStatus()));
		}).collect(Collectors.toList());
	}
	
	public List<TestCase> getTestCases(@Nullable PatternSet testSuitePatterns, 
			@Nullable PatternSet testCasePatterns, Collection<Status> statuses) {
		Matcher matcher = new PathMatcher();
		
		return testCases.stream().filter(it->{
			return (testSuitePatterns == null || testSuitePatterns.matches(matcher, it.getTestSuite().getName()))
					&& (testCasePatterns == null || testCasePatterns.matches(matcher, it.getName()))
					&& (statuses == null || statuses.contains(it.getStatus()));
		}).collect(Collectors.toList());
	}
	
	public List<TestSuite> getTestSuites() {
		if (testSuites == null) 
			testSuites = testCases.stream().map(it->it.getTestSuite()).distinct().collect(Collectors.toList());
		return testSuites;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}
	
	public boolean hasTestCaseDuration() {
		return hasTestCaseDuration;
	}

	@Nullable
	public static UnitTestReport readFrom(File reportDir) {
		File reportFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return (UnitTestReport) SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, FILE_NAME);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(reportFile))) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getTestDuration() {
		int testDuration = 0;
		for (TestSuite testSuite: getTestSuites())
			testDuration += testSuite.getDuration()/1000;
		return testDuration;
	}
	
	public int getTestSuiteSuccessRate() {
		int numOfFailed = 0;
		int numOfPassed = 0;
		for (TestSuite testSuite: getTestSuites()) {
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
		for (TestCase testCase: getTestCases()) {
			if (testCase.getStatus() == Status.FAILED) 
				numOfFailed++;
			else if (testCase.getStatus() == Status.PASSED)
				numOfPassed++;
		}
		return numOfPassed*100/(numOfPassed+numOfFailed);
	}
	
	public static class TestSuite implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final String name;
		
		private final Status status;
		
		private final long duration;
		
		private final String message;
		
		private final String blobPath;
		
		public TestSuite(String name, Status status, long duration, String message, 
				@Nullable String blobPath) {
			this.name = name;
			this.status = status;
			this.duration = duration;
			this.message = message;
			this.blobPath = blobPath;
		}

		public String getName() {
			return name;
		}

		public long getDuration() {
			return duration;
		}

		public Status getStatus() {
			return status;
		}
		
		@Nullable
		public String getMessage() {
			return message;
		}

		@Nullable
		public String getBlobPath() {
			return blobPath;
		}

		@Nullable
		protected Component renderMessage(String componentId, Build build) {
			if (getMessage() != null) 
				return new Label(componentId, getMessage());
			else 
				return null;
		}
		
	}
	
	public static class TestCase implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final TestSuite testSuite;
		
		private final String name;
		
		private final Status status;
		
		private final long duration;
		
		private final String message;
		
		public TestCase(TestSuite testSuite, String name, Status status, long duration, String message) {
			this.testSuite = testSuite;
			this.name = name;
			this.status = status;
			this.duration = duration;
			this.message = message;
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

		public long getDuration() {
			return duration;
		}

		@Nullable
		public String getMessage() {
			return message;
		}
		
		@Nullable
		protected Component renderMessage(String componentId, Build build) {
			if (getMessage() != null) 
				return new Label(componentId, getMessage());
			else 
				return null;
		}
		
	}
	
	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return UnitTestReport.class.getName() + ":" + projectId + ":" + buildNumber;
	}
	
}
