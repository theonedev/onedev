package io.onedev.server.plugin.report.coverage;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.dao.Dao;
import org.apache.commons.lang.SerializationUtils;

import org.jspecify.annotations.Nullable;
import java.io.*;
import java.util.Map;

import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.plugin.report.coverage.CoverageStats.getReportLockName;

@Editable
public abstract class PublishCoverageReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(SessionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			CoverageReport result = write(getReportLockName(build), () -> {
				File reportDir = new File(build.getDir(), CoverageStats.CATEGORY + "/" + getReportName());

				FileUtils.createDir(reportDir);
				try {
					CoverageReport aResult = process(build, inputDir, logger);
					if (aResult != null) {
						aResult.getStats().writeTo(reportDir);
						for (var entry: aResult.getStatuses().entrySet())
							writeLineStatuses(build, entry.getKey(), entry.getValue());

						OneDev.getInstance(ProjectService.class).directoryModified(
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
				var metric = OneDev.getInstance(BuildMetricService.class).find(CoverageMetric.class, build, getReportName());
				if (metric == null) {
					metric = new CoverageMetric();
					metric.setBuild(build);
					metric.setReportName(getReportName());
				}

				Coverage coverages = result.getStats().getOverallCoverage();
				metric.setBranchCoverage(coverages.getBranchPercentage());
				metric.setLineCoverage(coverages.getLinePercentage());

				OneDev.getInstance(Dao.class).persist(metric);
			}
			return new ServerStepResult(true);
		});
	}

	@Nullable
	protected abstract CoverageReport process(Build build, File inputDir, TaskLogger logger);

	private void writeLineStatuses(Build build, String blobPath, Map<Integer, CoverageStatus> lineStatuses) {
		if (!lineStatuses.isEmpty()) {
			File reportDir = new File(build.getDir(), CoverageStats.CATEGORY + "/" + getReportName());
			File lineCoverageFile = new File(reportDir, CoverageStats.FILES + "/" + blobPath);
			FileUtils.createDir(lineCoverageFile.getParentFile());
			try (var os = new BufferedOutputStream(new FileOutputStream(lineCoverageFile))) {
				SerializationUtils.serialize((Serializable) lineStatuses, os);
			} catch (IOException e) {
				throw new RuntimeException(e);
			};
		}
	}
	
}
