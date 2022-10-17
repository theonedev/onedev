package io.onedev.server.plugin.report.problem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class PublishProblemReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		File reportDir = new File(build.getDir(), ProblemReport.CATEGORY + "/" + getReportName());
		
		ProblemReport report = LockUtils.write(ProblemReport.getReportLockName(build), new Callable<ProblemReport>() {

			@Override
			public ProblemReport call() throws Exception {
				FileUtils.createDir(reportDir);
				try {
					ProblemReport report = createReport(build, inputDir, reportDir, logger);
					if (report != null) {
						report.writeTo(reportDir);
						return report;
					} else {
						FileUtils.deleteDir(reportDir);
						return null;
					}
				} catch (Exception e) {
					FileUtils.deleteDir(reportDir);
					throw ExceptionUtils.unchecked(e);
				}
			}
			
		});
		
		if (report != null) {
			FileUtils.createDir(reportDir);
			report.writeTo(reportDir);
			
			ProblemMetric metric = new ProblemMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
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
	
	protected void writeFileProblems(Build build, String blobPath, List<CodeProblem> problemsOfFile) {
		File reportDir = new File(build.getDir(), ProblemReport.CATEGORY + "/" + getReportName());
		File violationsFile = new File(reportDir, ProblemReport.FILES_DIR + "/" + blobPath);
		FileUtils.createDir(violationsFile.getParentFile());
		try (OutputStream os = new FileOutputStream(violationsFile)) {
			SerializationUtils.serialize((Serializable) problemsOfFile, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract ProblemReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger);
	
}
