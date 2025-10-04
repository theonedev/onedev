package io.onedev.server.plugin.report.html;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.BuildService;

import java.io.File;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.plugin.report.html.PublishHtmlReportStep.CATEGORY;
import static io.onedev.server.plugin.report.html.PublishHtmlReportStep.getReportLockName;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

class ReadPublishedFile implements ClusterTask<byte[]> {

	private final Long projectId;

	private final Long buildNumber;

	private final String reportName;
	
	private final String filePath;

	ReadPublishedFile(Long projectId, Long buildNumber, String reportName, String filePath) {
		this.projectId = projectId;
		this.buildNumber = buildNumber;
		this.reportName = reportName;
		this.filePath = filePath;
	}

	@Override
	public byte[] call() {
		return read(getReportLockName(projectId, buildNumber), () -> {
			File reportDir = new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), CATEGORY + "/" + reportName);
			return readFileToByteArray(new File(reportDir, filePath));
		});
	}

}
