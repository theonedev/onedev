package io.onedev.server.codequality;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Build;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;

import org.jspecify.annotations.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.k8shelper.KubernetesHelper.checkStatus;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class UnitTestReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "unit-test";

	public static final String ARTIFACTS = "artifacts";
	
	private static final String REPORT = "report.ser";
	
	public enum Status {

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

	private static boolean existsIn(File reportDir) {
		return new File(reportDir, REPORT).isFile();
	}

	@Nullable
	public static UnitTestReport readFrom(Build build, String reportName) {
		checkReportName(reportName);
		Long projectId = build.getProject().getId();
		return OneDev.getInstance(ProjectService.class).runOnActiveServer(projectId,
				new ReadReport(projectId, build.getNumber(), reportName));
	}

	public static void downloadArtifact(Long projectId, Long buildNumber, String reportName,
			String artifactPath, OutputStream os) {
		checkReportName(reportName);
		if (artifactPath.contains("..") || !artifactPath.startsWith(ARTIFACTS + "/"))
			throw new ExplicitException("Invalid request path");

		var clusterService = OneDev.getInstance(ClusterService.class);
		var activeServer = OneDev.getInstance(ProjectService.class).getActiveServer(projectId, true);
		if (activeServer.equals(clusterService.getLocalServerAddress())) {
			read(getReportLockName(projectId, buildNumber), () -> {
				File reportDir = getReportDir(projectId, buildNumber, reportName);
				File artifactFile = new File(reportDir, artifactPath).getCanonicalFile();
				if (!artifactFile.toPath().startsWith(reportDir.getCanonicalFile().toPath())
						|| !artifactFile.isFile()) {
					throw new ExplicitException("Invalid request path");
				}
				try (var is = new FileInputStream(artifactFile)) {
					IOUtils.copy(is, os, BUFFER_SIZE);
				}
				return null;
			});
		} else {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterService.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl).path("~api/cluster/unit-test-artifact")
						.queryParam("projectId", projectId)
						.queryParam("buildNumber", buildNumber)
						.queryParam("reportName", reportName)
						.queryParam("artifactPath", artifactPath);
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION, BEARER + " "
						+ clusterService.getCredential());
				try (Response response = builder.get()) {
					checkStatus(response);
					try (var is = response.readEntity(InputStream.class)) {
						IOUtils.copy(is, os, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}
	}

	private static void checkReportName(String reportName) {
		if (reportName.contains(".."))
			throw new ExplicitException("Invalid report name");
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(reportFile), BUFFER_SIZE)) {
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
		
		public TestSuite(String name, Status status, long duration, @Nullable String blobPath, 
						 @Nullable PlanarRange position) {
			this.name = name;
			this.status = status;
			this.duration = duration;
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
		public abstract Component renderDetail(String componentId, Build build);
		
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

		public Map<String, Object> getDetailData() {
			return Map.of();
		}
		
		@Nullable
		public abstract Component renderDetail(String componentId, Build build, String reportName);
		
	}

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return UnitTestReport.class.getName() + ":" + projectId + ":" + buildNumber;
	}

	private static class ReadReport implements ClusterTask<UnitTestReport> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;

		private final Long buildNumber;

		private final String reportName;

		private ReadReport(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}

		@Override
		public UnitTestReport call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				File reportDir = getReportDir(projectId, buildNumber, reportName);
				if (existsIn(reportDir))
					return readFrom(reportDir);
				else
					return null;
			});
		}

	}

	private static File getReportDir(Long projectId, Long buildNumber, String reportName) {
		return new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber),
				CATEGORY + "/" + reportName);
	}
	
}
