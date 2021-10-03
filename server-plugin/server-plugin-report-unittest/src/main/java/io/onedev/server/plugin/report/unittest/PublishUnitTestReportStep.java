package io.onedev.server.plugin.report.unittest;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.model.Build;
import io.onedev.server.model.UnitTestMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class PublishUnitTestReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, byte[]> run(Build build, File filesDir, TaskLogger logger) {
		File reportDir = new File(build.getReportCategoryDir(UnitTestReport.CATEGORY), getReportName());

		UnitTestReport reportData = LockUtils.write(build.getReportCategoryLockKey(UnitTestReport.CATEGORY), new Callable<UnitTestReport>() {

			@Override
			public UnitTestReport call() throws Exception {
				UnitTestReport reportData = processReports(build, filesDir, logger);
				if (reportData != null) {
					FileUtils.createDir(reportDir);
					reportData.writeTo(reportDir);
					return reportData;
				} else {
					return null;
				}
			}
			
		});
		
		if (reportData != null) {
			UnitTestMetric metric = new UnitTestMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			metric.setTestCaseSuccessRate(reportData.getTestCaseSuccessRate());
			metric.setTestSuiteSuccessRate(reportData.getTestSuiteSuccessRate());
			metric.setNumOfTestCases(reportData.getTestCases().size());
			metric.setNumOfTestSuites(reportData.getTestSuites().size());
			metric.setTotalTestDuration(reportData.getTestDuration());
			OneDev.getInstance(Dao.class).persist(metric);
		}	
		
		return null;
	}

	@Nullable
	protected abstract UnitTestReport processReports(Build build, File filesDir, TaskLogger logger);
	
}
