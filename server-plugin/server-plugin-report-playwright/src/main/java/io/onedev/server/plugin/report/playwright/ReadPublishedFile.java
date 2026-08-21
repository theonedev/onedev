package io.onedev.server.plugin.report.playwright;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.plugin.report.unittest.UnitTestReport.CATEGORY;
import static io.onedev.server.plugin.report.unittest.UnitTestReport.getReportLockName;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

import java.io.File;
import java.io.IOException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.BuildService;

class ReadPublishedFile implements ClusterTask<byte[]> {

	private static final long serialVersionUID = 1L;

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
			File reportDir = new File(
					OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber),
					CATEGORY + "/" + reportName);
			try {
				File file = new File(reportDir, filePath).getCanonicalFile();
				if (!file.toPath().startsWith(reportDir.getCanonicalFile().toPath()) || !file.isFile())
					throw new ExplicitException("Invalid request path");
				return readFileToByteArray(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
