package io.onedev.server.plugin.report.unittest;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.UnitTestMetric;
import io.onedev.server.persistence.dao.Dao;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.plugin.report.unittest.UnitTestReport.getReportLockName;

@Editable
public abstract class PublishUnitTestReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		File reportDir = new File(build.getStorageDir(), UnitTestReport.CATEGORY + "/" + getReportName());

		UnitTestReport report = write(getReportLockName(build), () -> {
			UnitTestReport aReport = createReport(build, inputDir, logger);
			if (aReport != null) {
				FileUtils.createDir(reportDir);
				aReport.writeTo(reportDir);
				OneDev.getInstance(ProjectManager.class).directoryModified(
						build.getProject().getId(), reportDir.getParentFile());
				return aReport;
			} else {
				return null;
			}
		});
		
		if (report != null) {
			var metric = OneDev.getInstance(BuildMetricManager.class).find(UnitTestMetric.class, build, getReportName());
			if (metric == null) {
				metric = new UnitTestMetric();
				metric.setBuild(build);
				metric.setReportName(getReportName());
			}
			metric.setTestCaseSuccessRate(report.getTestCaseSuccessRate());
			metric.setTestSuiteSuccessRate(report.getTestSuiteSuccessRate());
			metric.setNumOfTestCases(report.getTestCases().size());
			metric.setNumOfTestSuites(report.getTestSuites().size());
			metric.setTotalTestDuration(report.getTestDuration());
			OneDev.getInstance(Dao.class).persist(metric);
		}	
		
		return null;
	}

	@Nullable
	protected abstract UnitTestReport createReport(Build build, File inputDir, TaskLogger logger);
	
}
