package io.onedev.server.plugin.outcome.htmlreport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.model.Build;
import io.onedev.server.util.JobLogger;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Html Report")
public class JobHtmlReport extends JobOutcome {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "html-reports";
	
	public static final String START_PAGES = "$onedev-htmlreport-startpages$";

	private String reportName;
	
	private String startPage;

	@Editable(order=1000)
	@NotEmpty
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	@Editable(order=1100, description="Specify start page of the report relative to workspace, for instance: api/index.html")
	@NotEmpty
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@Override
	public void process(Build build, File workspace, JobLogger logger) {
		File outcomeDir = getOutcomeDir(build, DIR);
		FileUtils.createDir(outcomeDir);

		LockUtils.write(getLockKey(build, DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File startPage = new File(workspace, getStartPage()); 
				if (startPage.exists()) {
					HashMap<String, String> startPages;
					File startPagesFile = new File(outcomeDir, START_PAGES);
					if (startPagesFile.exists()) 
						startPages = SerializationUtils.deserialize(FileUtils.readFileToByteArray(startPagesFile));
					else
						startPages = new LinkedHashMap<>();
					startPages.put(getReportName(), getStartPage());
					FileUtils.writeByteArrayToFile(startPagesFile, SerializationUtils.serialize(startPages));
					
					int baseLen = workspace.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(workspace)) {
						try {
							FileUtils.copyFile(file, new File(outcomeDir, file.getAbsolutePath().substring(baseLen)));
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
