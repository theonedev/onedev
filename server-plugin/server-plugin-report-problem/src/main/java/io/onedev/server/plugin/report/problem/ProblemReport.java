package io.onedev.server.plugin.report.problem;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;

import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;

public class ProblemReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "problem";
	
	private static final String FILE_NAME = "report.ser";

	public static final String FILES_DIR = "files";
	
	private final List<CodeProblem> problems;
	
	private transient List<ProblemFile> problemFiles;
	
	public ProblemReport(List<CodeProblem> problems) {
		this.problems = problems;
	}

	public List<CodeProblem> getProblems() {
		return problems;
	} 

	public List<ProblemFile> getProblemFiles() {
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
			
			problemFiles = new ArrayList<>(map.values());
			
			problemFiles.sort(new Comparator<ProblemFile>() {
	
				@Override
				public int compare(ProblemFile o1, ProblemFile o2) {
					return o2.getProblems().size() - o1.getProblems().size();
				}
				
			});
		}
		return problemFiles;
	}
	
	public static ProblemReport readFrom(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(dataFile))) {
			return (ProblemReport) SerializationUtils.deserialize(is);
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
		};
	}
	
	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}

	public static String getReportLockName(Long projectId, Long buildNumber) {
		return ProblemReport.class.getName() + ":" + projectId + ":" +  buildNumber;
	}
	
}
