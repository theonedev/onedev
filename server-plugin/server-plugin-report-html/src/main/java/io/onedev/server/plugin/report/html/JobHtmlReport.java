package io.onedev.server.plugin.report.html;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.util.JobLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(name="Html Report")
public class JobHtmlReport extends JobReport {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "html-reports";
	
	public static final String START_PAGES = "$onedev-htmlreport-startpages$";

	private String reportName;
	
	private String startPage;

	@Editable(order=1000, description="Specify report name. "
			+ "<b>Note:</b> Type '@' to <a href='https://github.com/theonedev/onedev/wiki/Variable-Substitution'>insert variable</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	@Editable(order=1100, description="Specify start page of the report relative to workspace, for instance: api/index.html. "
			+ "<b>Note:</b> Type '@' to <a href='https://github.com/theonedev/onedev/wiki/Variable-Substitution'>insert variable</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@SuppressWarnings("unused")
	private List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

	@Override
	public void process(Build build, File workspace, JobLogger logger) {
		File reportDir = build.getReportDir(DIR);
		FileUtils.createDir(reportDir);

		LockUtils.write(build.getReportLockKey(DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File startPage = new File(workspace, getStartPage()); 
				if (startPage.exists()) {
					HashMap<String, String> startPages;
					File startPagesFile = new File(reportDir, START_PAGES);
					if (startPagesFile.exists()) 
						startPages = SerializationUtils.deserialize(FileUtils.readFileToByteArray(startPagesFile));
					else
						startPages = new LinkedHashMap<>();
					startPages.put(getReportName(), getStartPage());
					FileUtils.writeByteArrayToFile(startPagesFile, SerializationUtils.serialize(startPages));
					
					int baseLen = workspace.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(workspace)) {
						try {
							FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					logger.log("WARNING: Html report start page not found: " + startPage.getAbsolutePath());
				}
				return null;
			}
			
		});
	}

}
