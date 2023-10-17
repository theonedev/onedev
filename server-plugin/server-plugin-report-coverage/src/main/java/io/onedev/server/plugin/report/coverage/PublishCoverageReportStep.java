package io.onedev.server.plugin.report.coverage;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.persistence.dao.Dao;
import org.apache.commons.lang.SerializationUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;

import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.plugin.report.coverage.CoverageReport.getReportLockName;

@Editable
public abstract class PublishCoverageReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		ProcessResult result = write(getReportLockName(build), () -> {
			File reportDir = new File(build.getStorageDir(), CoverageReport.CATEGORY + "/" + getReportName());

			FileUtils.createDir(reportDir);
			try {
				ProcessResult aResult = process(build, inputDir, logger);
				if (aResult != null) {
					aResult.getReport().writeTo(reportDir);
					for (var entry: aResult.getStatuses().entrySet()) 
						writeLineStatuses(build, entry.getKey(), entry.getValue());
					
					OneDev.getInstance(ProjectManager.class).directoryModified(
							build.getProject().getId(), reportDir.getParentFile());
					return aResult;
				} else {
					FileUtils.deleteDir(reportDir);
					return null;
				}
			} catch (Exception e) {
				FileUtils.deleteDir(reportDir);
				throw ExceptionUtils.unchecked(e);
			}
		});
		
		if (result != null) {
			var metric = OneDev.getInstance(BuildMetricManager.class).find(CoverageMetric.class, build, getReportName());
			if (metric == null) {
				metric = new CoverageMetric();
				metric.setBuild(build);
				metric.setReportName(getReportName());
			}
			
			CoverageInfo coverages = result.getReport().getOverallCoverages();
			metric.setBranchCoverage(coverages.getBranchCoverage());
			metric.setLineCoverage(coverages.getLineCoverage());
			
			OneDev.getInstance(Dao.class).persist(metric);
		}	
		
		return null;
	}

	@Nullable
	protected abstract ProcessResult process(Build build, File inputDir, TaskLogger logger);

	private void writeLineStatuses(Build build, String blobPath, Map<Integer, CoverageStatus> lineStatuses) {
		lineStatuses.entrySet().removeIf(it -> it.getValue() == CoverageStatus.NOT_COVERED);
		if (!lineStatuses.isEmpty()) {
			File reportDir = new File(build.getStorageDir(), CoverageReport.CATEGORY + "/" + getReportName());
			File lineCoverageFile = new File(reportDir, CoverageReport.FILES + "/" + blobPath);
			FileUtils.createDir(lineCoverageFile.getParentFile());
			try (OutputStream os = new FileOutputStream(lineCoverageFile)) {
				SerializationUtils.serialize((Serializable) lineStatuses, os);
			} catch (IOException e) {
				throw new RuntimeException(e);
			};
		}
	}
	
}
