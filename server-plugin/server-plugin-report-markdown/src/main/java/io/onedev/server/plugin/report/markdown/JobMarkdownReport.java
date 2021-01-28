package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(name="Markdown Report")
public class JobMarkdownReport extends JobReport {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "markdown-reports";
	
	public static final String START_PAGE = "$onedev-markdownreport-startpage$";

	private String startPage;

	@Editable(order=1100, description="Specify start page of the report relative to <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>, for instance: <tt>manual/index.md</tt>")
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
		return Job.suggestVariables(matchWith);
	}

	@Override
	public void process(Build build, File workspace, SimpleLogger logger) {
		File reportDir = new File(build.getReportDir(DIR), getReportName());

		LockUtils.write(build.getReportLockKey(DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File startPage = new File(workspace, getStartPage()); 
				if (startPage.exists()) {
					FileUtils.createDir(reportDir);
					File startPageFile = new File(reportDir, START_PAGE);
					FileUtils.writeFile(startPageFile, getStartPage());
					
					int baseLen = workspace.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(workspace)) {
						try {
							FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					logger.log("WARNING: Markdown report start page not found: " + startPage.getAbsolutePath());
				}
				return null;
			}
			
		});
	}

}
