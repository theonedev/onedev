package io.onedev.server.plugin.report.html;

import static io.onedev.commons.utils.LockUtils.write;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.SessionService;

@Editable(order=1070, group= StepGroup.PUBLISH, name="Html Report")
public class PublishHtmlReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "html";
	
	public static final String START_PAGE = "$onedev-htmlreport-startpage$";

	private String startPage;

	@Editable(order=1000, description="Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(SessionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			JobContext jobContext = OneDev.getInstance(JobService.class).getJobContext(build.getId());
			if (jobContext.getJobExecutor().isHtmlReportPublishEnabled()) {
				write(getReportLockName(build), () -> {
					File reportDir = new File(build.getDir(), CATEGORY + "/" + getReportName());
					File startPage = new File(inputDir, getStartPage());
					if (startPage.exists()) {
						FileUtils.createDir(reportDir);
						File startPageFile = new File(reportDir, START_PAGE);
						FileUtils.writeFile(startPageFile, getStartPage());

						int baseLen = inputDir.getAbsolutePath().length() + 1;
						for (File file : getPatternSet().listFiles(inputDir)) {
							try {
								FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						OneDev.getInstance(ProjectService.class).directoryModified(
								build.getProject().getId(), reportDir.getParentFile());
					} else {
						logger.warning("Html report start page not found: " + startPage.getAbsolutePath());
					}
				});
			} else {
				logger.error("Html report publish is prohibited by current job executor");
				return new ServerStepResult(false);
			}
			return new ServerStepResult(true);
		});		
	}

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}

	public static String getReportLockName(Long projectId, Long buildNumber) {
		return PublishHtmlReportStep.class.getName() + ":"	+ projectId + ":" + buildNumber;
	}
	
}
