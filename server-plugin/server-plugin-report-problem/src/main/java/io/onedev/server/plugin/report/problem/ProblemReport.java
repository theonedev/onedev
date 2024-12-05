package io.onedev.server.plugin.report.problem;

import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.ProblemTarget;
import io.onedev.server.model.Build;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.*;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;

public class ProblemReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
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
	
	public void writeTo(File reportDir) {
		File dataFile = new File(reportDir, REPORT);
		try (var os = new BufferedOutputStream(new FileOutputStream(dataFile), BUFFER_SIZE)) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
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
}
