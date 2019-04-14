package io.onedev.server.ci.job.outcome.htmlreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.outcome.JobOutcome;
import io.onedev.server.model.Build2;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Html Report")
public class JobHtmlReport extends JobOutcome {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "html";
	
	public static final String INFO = "$onedev-htmlreport-info$";

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
	public void process(Build2 build, File workspace, Logger logger) {
		File outcomeDir = getOutcomeDir(build, DIR);
		FileUtils.createDir(outcomeDir);

		LockUtils.write(getLockKey(build, DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File startPage = new File(workspace, getStartPage()); 
				if (startPage.exists()) {
					ArrayList<HtmlReportInfo> infos;
					File infoFile = new File(outcomeDir, INFO);
					if (infoFile.exists()) 
						infos = SerializationUtils.deserialize(FileUtils.readFileToByteArray(infoFile));
					else
						infos = new ArrayList<>();
					infos.add(new HtmlReportInfo(getReportName(), getStartPage()));
					FileUtils.writeByteArrayToFile(infoFile, SerializationUtils.serialize(infos));
					
					int baseLen = workspace.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(workspace)) {
						try {
							FileUtils.copyFile(file, new File(outcomeDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					logger.warn("Html report start page not found: " + startPage.getAbsolutePath());
				}
				return null;
			}
			
		});
	}
}
