package io.onedev.server.plugin.report.coverage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class PublishCoverageReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		CoverageReport report = LockUtils.write(CoverageReport.getReportLockName(build), new Callable<CoverageReport>() {

			@Override
			public CoverageReport call() throws Exception {
				File reportDir = new File(build.getDir(), CoverageReport.CATEGORY + "/" + getReportName());

				FileUtils.createDir(reportDir);
				try {
					CoverageReport report = createReport(build, inputDir, reportDir, logger);
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
		File reportDir = new File(build.getDir(), CoverageReport.CATEGORY + "/" + getReportName());
		File lineCoverageFile = new File(reportDir, CoverageReport.FILES_DIR + "/" + blobPath);
		FileUtils.createDir(lineCoverageFile.getParentFile());
		try (OutputStream os = new FileOutputStream(lineCoverageFile)) {
			SerializationUtils.serialize((Serializable) lineCoverages, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}
	
}
