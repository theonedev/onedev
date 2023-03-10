package io.onedev.server.plugin.report.coverage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.entitymanager.ProjectManager;
import org.apache.commons.lang.SerializationUtils;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.annotation.Editable;

import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.plugin.report.coverage.CoverageReport.getReportLockName;

@Editable
public abstract class PublishCoverageReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		CoverageReport report = write(getReportLockName(build), () -> {
			File reportDir = new File(build.getStorageDir(), CoverageReport.DIR_CATEGORY + "/" + getReportName());

			FileUtils.createDir(reportDir);
			try {
				CoverageReport aReport = createReport(build, inputDir, reportDir, logger);
				if (aReport != null) {
					aReport.writeTo(reportDir);
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
			CoverageMetric metric = new CoverageMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			
			CoverageInfo coverages = report.getOverallCoverages();
			metric.setBranchCoverage(coverages.getBranchCoverage().getPercent());
			metric.setLineCoverage(coverages.getLineCoverage().getPercent());
			metric.setMethodCoverage(coverages.getMethodCoverage().getPercent());
			metric.setStatementCoverage(coverages.getStatementCoverage().getPercent());
			metric.setTotalBranches(coverages.getBranchCoverage().getTotal());
			metric.setTotalLines(coverages.getLineCoverage().getTotal());
			metric.setTotalMethods(coverages.getMethodCoverage().getTotal());
			metric.setTotalStatements(coverages.getStatementCoverage().getTotal());
			
			OneDev.getInstance(Dao.class).persist(metric);
		}	
		
		return null;
	}

	@Nullable
	protected abstract CoverageReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger);

	protected void writeLineCoverages(Build build, String blobPath, Map<Integer, CoverageStatus> lineCoverages) {
		File reportDir = new File(build.getStorageDir(), CoverageReport.DIR_CATEGORY + "/" + getReportName());
		File lineCoverageFile = new File(reportDir, CoverageReport.DIR_FILES + "/" + blobPath);
		FileUtils.createDir(lineCoverageFile.getParentFile());
		try (OutputStream os = new FileOutputStream(lineCoverageFile)) {
			SerializationUtils.serialize((Serializable) lineCoverages, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}
	
}
