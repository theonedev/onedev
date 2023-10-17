package io.onedev.server.plugin.report.problem;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
import io.onedev.server.persistence.dao.Dao;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.Collection;
import java.util.Map;

@Editable
public abstract class PublishProblemReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		File reportDir = new File(build.getStorageDir(), ProblemReport.CATEGORY + "/" + getReportName());
		
		ProblemReport report = LockUtils.write(ProblemReport.getReportLockName(build), () -> {
			FileUtils.createDir(reportDir);
			try {
				ProblemReport aReport = process(build, inputDir, reportDir, logger);
				if (aReport != null) {
					aReport.writeTo(reportDir);
					for (var problemFile: aReport.getProblemFiles())
						writeFileProblems(build, problemFile.getBlobPath(), problemFile.getProblems());
					OneDev.getInstance(ProjectManager.class).directoryModified(
							build.getProject().getId(), reportDir.getParentFile());
					return aReport;
				} else {
					FileUtils.deleteDir(reportDir);
					return null;
				}
			} catch (Exception e) {
				FileUtils.deleteDir(reportDir);
				throw ExceptionUtils.unchecked(e);
			}
		});
		
		if (report != null) {
			FileUtils.createDir(reportDir);
			report.writeTo(reportDir);

			var metric = OneDev.getInstance(BuildMetricManager.class).find(ProblemMetric.class, build, getReportName());
			if (metric == null) {
				metric = new ProblemMetric();
				metric.setBuild(build);
				metric.setReportName(getReportName());
			}
			metric.setHighSeverities((int) report.getProblems().stream()
					.filter(it->it.getSeverity()==Severity.HIGH)
					.count());
			metric.setMediumSeverities((int) report.getProblems().stream()
					.filter(it->it.getSeverity()==Severity.MEDIUM)
					.count());
			metric.setLowSeverities((int) report.getProblems().stream()
					.filter(it->it.getSeverity()==Severity.LOW)
					.count());
						
			OneDev.getInstance(Dao.class).persist(metric);
		}
		
		return null;
	}
	
	private void writeFileProblems(Build build, String blobPath, Collection<CodeProblem> problemsOfFile) {
		File reportDir = new File(build.getStorageDir(), ProblemReport.CATEGORY + "/" + getReportName());
		File violationsFile = new File(reportDir, ProblemReport.FILES + "/" + blobPath);
		FileUtils.createDir(violationsFile.getParentFile());
		try (OutputStream os = new FileOutputStream(violationsFile)) {
			SerializationUtils.serialize((Serializable) problemsOfFile, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract ProblemReport process(Build build, File inputDir, File reportDir, TaskLogger logger);
	
}
