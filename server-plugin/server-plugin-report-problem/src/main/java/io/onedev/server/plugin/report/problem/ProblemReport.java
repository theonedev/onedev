package io.onedev.server.plugin.report.problem;

import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.*;

public class ProblemReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "problem";
	
	private static final String REPORT = "report.ser";

	public static final String FILES = "files";
	
	private final Collection<CodeProblem> problems;
	
	private transient Collection<ProblemFile> problemFiles;
	
	public ProblemReport(Collection<CodeProblem> problems) {
		this.problems = problems;
	}

	public Collection<CodeProblem> getProblems() {
		return problems;
	} 

	public Collection<ProblemFile> getProblemFiles() {
		if (problemFiles == null) {
			Map<String, ProblemFile> map = new LinkedHashMap<>();
			for (CodeProblem problem: problems) {
				ProblemFile file = map.get(problem.getBlobPath());
				if (file == null) {
					file = new ProblemFile(problem.getBlobPath());
					map.put(problem.getBlobPath(), file);
				}
				file.getProblems().add(problem);
			}
			problemFiles = map.values();
		}
		return problemFiles;
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
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile))) {
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
	
	public Comparator<ProblemFile> newProblemFileComparator() {
		Map<String, Long> highSeverityCounts = new HashMap<>();
		Map<String, Long> mediumSeverityCounts = new HashMap<>();
		Map<String, Long> lowSeverityCounts = new HashMap<>();
		
		for (var problemFile: problemFiles) {
			highSeverityCounts.put(problemFile.getBlobPath(), problemFile.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.HIGH).count());
			mediumSeverityCounts.put(problemFile.getBlobPath(), problemFile.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.MEDIUM).count());
			lowSeverityCounts.put(problemFile.getBlobPath(), problemFile.getProblems().stream().filter(it -> it.getSeverity() == CodeProblem.Severity.LOW).count());
		}

		return new Comparator<ProblemFile>() {

			private int compareSeverityCount(Map<String, Long> severityCounts, ProblemFile file1, ProblemFile file2) {
				var severityCount1 = severityCounts.getOrDefault(file1.getBlobPath(), 0L);
				var severityCount2 = severityCounts.getOrDefault(file2.getBlobPath(), 0L);
				return severityCount2.compareTo(severityCount1);
			}

			@Override
			public int compare(ProblemFile o1, ProblemFile o2) {
				var order = compareSeverityCount(highSeverityCounts, o1, o2);
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
