package io.onedev.server.plugin.report.unittest;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class UnitTestReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "unit-test";
	
	private static final String REPORT = "report.ser";
	
	public static enum Status {

		NOT_PASSED("#F64E60"), OTHER("#FFA800"), NOT_RUN("#8950FC"), PASSED("#1BC5BD");
		
		private final String color;
		
		Status(String color) {
			this.color = color;
		};

		public String getColor() {
			return color;
		}

		@Nullable
		public static Status getOverallStatus(Collection<Status> statuses) {
			for (var status: newArrayList(NOT_PASSED, OTHER, PASSED, NOT_RUN)) {
				if (statuses.contains(status))
					return status;
			}
			return Status.NOT_RUN;
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
		}).collect(toList());
	}
	
	public List<TestCase> getTestCases(@Nullable PatternSet testSuitePatterns, 
			@Nullable PatternSet testCasePatterns, Collection<Status> statuses) {
		Matcher matcher = new PathMatcher();
		
		return testCases.stream().filter(it->{
			return (testSuitePatterns == null || testSuitePatterns.matches(matcher, it.getTestSuite().getName()))
					&& (testCasePatterns == null || testCasePatterns.matches(matcher, it.getName()))
					&& (statuses == null || statuses.contains(it.getStatus()));
		}).collect(toList());
	}
	
	public List<TestSuite> getTestSuites() {
		if (testSuites == null) 
			testSuites = testCases.stream().map(TestCase::getTestSuite).distinct().collect(toList());
		return testSuites;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}
	
	public boolean hasTestCaseDuration() {
		return hasTestCaseDuration;
	}

	public static UnitTestReport readFrom(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
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
		int numOfNotPassed = 0;
		int numOfPassed = 0;
		for (TestSuite testSuite: getTestSuites()) {
			if (testSuite.getStatus() == Status.NOT_PASSED) 
				numOfNotPassed++;
			else if (testSuite.getStatus() == Status.PASSED)
				numOfPassed++;
		}
		if (numOfPassed + numOfNotPassed != 0)
			return numOfPassed*100 / (numOfPassed + numOfNotPassed);
		else 
			return 100;
	}
	
	public int getTestCaseSuccessRate() {
		int numOfNotPassed = 0;
		int numOfPassed = 0;
		for (TestCase testCase: getTestCases()) {
			if (testCase.getStatus() == Status.NOT_PASSED) 
				numOfNotPassed++;
			else if (testCase.getStatus() == Status.PASSED)
				numOfPassed++;
		}
		if (numOfPassed + numOfNotPassed != 0)
			return numOfPassed*100 / (numOfPassed + numOfNotPassed);
		else
			return 100;
	}
	
	public static abstract class TestSuite implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final String name;
		
		private final Status status;
		
		private final long duration;
		
		private final String blobPath;
		
		private final PlanarRange position;
		
		public TestSuite(String name, Status status, long duration, @Nullable String blobPath, 
						 @Nullable PlanarRange position) {
			this.name = name;
			this.status = status;
			this.duration = duration;
			this.blobPath = blobPath;
			this.position = position;
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
		public String getBlobPath() {
			return blobPath;
		}

		@Nullable
		public PlanarRange getPosition() {
			return position;
		}

		@Nullable
		protected abstract Component renderDetail(String componentId, Build build);
		
	}
	
	public static abstract class TestCase implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final TestSuite testSuite;
		
		private final String name;
		
		private final Status status;
		
		private final String statusText;
		
		private final long duration;
		
		public TestCase(TestSuite testSuite, String name, Status status, @Nullable String statusText, long duration) {
			this.testSuite = testSuite;
			this.name = name;
			this.status = status;
			this.statusText = statusText;
			this.duration = duration;
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

		public String getStatusText() {
			return statusText;
		}

		public long getDuration() {
			return duration;
		}
		
		@Nullable
		protected abstract Component renderDetail(String componentId, Build build);
		
	}
	
	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return UnitTestReport.class.getName() + ":" + projectId + ":" + buildNumber;
	}
	
}
