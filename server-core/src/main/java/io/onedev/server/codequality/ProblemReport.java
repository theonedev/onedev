package io.onedev.server.codequality;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import java.io.*;
import java.util.*;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.server.util.SiteSyncUtils.isVersionFile;

public class ProblemReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ProblemReport.class);
	
	public static final String CATEGORY = "problem";
	
	private static final String REPORT = "report.ser";

	public static final String FILES = "files";
	
	private final Collection<CodeProblem> problems;
	
	private transient Collection<ProblemGroup> problemGroups;

	public ProblemReport(Collection<CodeProblem> problems) {
		this.problems = problems;
	}
	
	public Collection<CodeProblem> getProblems() {
		return problems;
	}
	
	public Collection<ProblemGroup> getProblemGroups() {
		if (problemGroups == null) {
			Map<ProblemTarget.GroupKey, ProblemGroup> map = new LinkedHashMap<>();
			for (CodeProblem problem: problems) {
				ProblemGroup group = map.get(problem.getTarget().getGroupKey());
				if (group == null) {
					group = new ProblemGroup(problem.getTarget().getGroupKey());
					map.put(problem.getTarget().getGroupKey(), group);
				}
				group.getProblems().add(problem);
			}
			problemGroups = map.values();
		}
		return problemGroups;
	}
	
	public static ProblemReport readFrom(File reportDir) {
		File dataFile = new File(reportDir, REPORT);
		try (InputStream is = new BufferedInputStream(new FileInputStream(dataFile))) {
			return SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean existsIn(File reportDir) {
		return new File(reportDir, REPORT).isFile();
	}

	@Nullable
	public static ProblemReport readFrom(Build build, String reportName) {
		checkReportName(reportName);
		Long projectId = build.getProject().getId();
		return OneDev.getInstance(ProjectService.class).runOnActiveServer(projectId,
				new ReadReport(projectId, build.getNumber(), reportName));
	}

	public static List<CodeProblem> getCodeProblems(Build build, String blobPath,
			@Nullable String reportName) {
		if (reportName != null)
			checkReportName(reportName);
		Long projectId = build.getProject().getId();
		Map<String, Collection<CodeProblem>> problemsMap = OneDev.getInstance(ProjectService.class)
				.runOnActiveServer(projectId, new GetCodeProblems(projectId, build.getNumber(),
						blobPath, reportName));
		List<CodeProblem> problems = new ArrayList<>();
		for (var entry: problemsMap.entrySet()) {
			if (SecurityUtils.canAccessReport(build, entry.getKey()))
				problems.addAll(entry.getValue());
		}
		return problems;
	}

	private static void checkReportName(String reportName) {
		if (reportName.contains(".."))
			throw new ExplicitException("Invalid report name");
	}
	
	public void writeTo(File reportDir) {
		File dataFile = new File(reportDir, REPORT);
		try (var os = new BufferedOutputStream(new FileOutputStream(dataFile), BUFFER_SIZE)) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}

	public static String getReportLockName(Long projectId, Long buildNumber) {
		return ProblemReport.class.getName() + ":" + projectId + ":" +  buildNumber;
	}
	
	public Comparator<ProblemGroup> newProblemGroupComparator() {
		Map<ProblemTarget.GroupKey, Long> criticalSeverityCounts = new HashMap<>();
		Map<ProblemTarget.GroupKey, Long> highSeverityCounts = new HashMap<>();
		Map<ProblemTarget.GroupKey, Long> mediumSeverityCounts = new HashMap<>();
		Map<ProblemTarget.GroupKey, Long> lowSeverityCounts = new HashMap<>();
		
		for (var problemGroup: problemGroups) {
			criticalSeverityCounts.put(problemGroup.getKey(), problemGroup.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.CRITICAL).count());
			highSeverityCounts.put(problemGroup.getKey(), problemGroup.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.HIGH).count());
			mediumSeverityCounts.put(problemGroup.getKey(), problemGroup.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.MEDIUM).count());
			lowSeverityCounts.put(problemGroup.getKey(), problemGroup.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.LOW).count());
		}

		return new Comparator<>() {

			private int compareSeverityCount(Map<ProblemTarget.GroupKey, Long> severityCounts, ProblemGroup group1, ProblemGroup group2) {
				var severityCount1 = severityCounts.getOrDefault(group1.getKey(), 0L);
				var severityCount2 = severityCounts.getOrDefault(group2.getKey(), 0L);
				return severityCount2.compareTo(severityCount1);
			}

			@Override
			public int compare(ProblemGroup o1, ProblemGroup o2) {
				var order = compareSeverityCount(criticalSeverityCounts, o1, o2);
				if (order != 0)
					return order;
				order = compareSeverityCount(highSeverityCounts, o1, o2);
				if (order != 0)
					return order;
				order = compareSeverityCount(mediumSeverityCounts, o1, o2);
				if (order != 0)
					return order;
				return compareSeverityCount(lowSeverityCounts, o1, o2);
			}

		};
	}

	private static class ReadReport implements ClusterTask<ProblemReport> {

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
		public ProblemReport call() {
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

	private static class GetCodeProblems implements ClusterTask<Map<String, Collection<CodeProblem>>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;

		private final Long buildNumber;

		private final String blobPath;

		private final String reportName;

		private GetCodeProblems(Long projectId, Long buildNumber, String blobPath,
				@Nullable String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.blobPath = blobPath;
			this.reportName = reportName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Collection<CodeProblem>> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				Map<String, Collection<CodeProblem>> problems = new HashMap<>();
				File categoryDir = new File(OneDev.getInstance(BuildService.class)
						.getBuildDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir)
								&& (reportName == null || reportName.equals(reportDir.getName()))) {
							File file = new File(reportDir, FILES + "/" + blobPath);
							if (file.exists()) {
								try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
									problems.put(reportDir.getName(),
											(Collection<CodeProblem>) SerializationUtils.deserialize(is));
								} catch (SerializationException e) {
									logger.error("Error reading problem report: " + file, e);
								}
							}
						}
					}
				}
				return problems;
			});
		}

	}

}
