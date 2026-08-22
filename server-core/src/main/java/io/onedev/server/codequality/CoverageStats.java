package io.onedev.server.codequality;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.server.util.SiteSyncUtils.isVersionFile;

public class CoverageStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(CoverageStats.class);

	public static final String CATEGORY = "coverage";
	
	public static final String FILES = "files";
	
	private static final String REPORT = "report.ser";
	
	private final Coverage overallCoverage;
	
	private final List<GroupCoverage> groupCoverages;
	
	public CoverageStats(Coverage overallCoverage, List<GroupCoverage> groupCoverages) {
		this.overallCoverage = overallCoverage;
		this.groupCoverages = groupCoverages;
	}

	public Coverage getOverallCoverage() {
		return overallCoverage;
	}

	public List<GroupCoverage> getGroupCoverages() {
		return groupCoverages;
	}
	
	public static CoverageStats readFrom(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean existsIn(File reportDir) {
		return new File(reportDir, REPORT).isFile();
	}

	@Nullable
	public static CoverageStats readFrom(Build build, String reportName) {
		checkReportName(reportName);
		Long projectId = build.getProject().getId();
		return OneDev.getInstance(ProjectService.class).runOnActiveServer(projectId,
				new ReadReport(projectId, build.getNumber(), reportName));
	}

	public static Map<Integer, CoverageStatus> getLineCoverages(Build build, String blobPath,
			@Nullable String reportName) {
		if (reportName != null)
			checkReportName(reportName);
		Long projectId = build.getProject().getId();
		Map<String, Map<Integer, CoverageStatus>> coveragesMap = OneDev.getInstance(ProjectService.class)
				.runOnActiveServer(projectId, new GetLineCoverages(projectId, build.getNumber(),
						blobPath, reportName));
		Map<Integer, CoverageStatus> coverages = new HashMap<>();
		for (var entry: coveragesMap.entrySet()) {
			if (SecurityUtils.canAccessReport(build, entry.getKey())) {
				entry.getValue().forEach((key, value) -> {
					coverages.merge(key, value, CoverageStatus::mergeWith);
				});
			}
		}
		return coverages;
	}

	private static void checkReportName(String reportName) {
		if (reportName.contains(".."))
			throw new ExplicitException("Invalid report name");
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (var os = new BufferedOutputStream(new FileOutputStream(reportFile), BUFFER_SIZE)) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return CoverageStats.class.getName() + ":"	+ projectId + ":" + buildNumber;
	}

	private static class ReadReport implements ClusterTask<CoverageStats> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;

		private final Long buildNumber;

		private final String reportName;

		private ReadReport(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}

		@Override
		public CoverageStats call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				File reportDir = getReportDir(projectId, buildNumber, reportName);
				if (existsIn(reportDir))
					return readFrom(reportDir);
				else
					return null;
			});
		}

	}

	private static File getReportDir(Long projectId, Long buildNumber, String reportName) {
		return new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber),
				CATEGORY + "/" + reportName);
	}

	private static class GetLineCoverages implements ClusterTask<Map<String, Map<Integer, CoverageStatus>>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;

		private final Long buildNumber;

		private final String blobPath;

		private final String reportName;

		private GetLineCoverages(Long projectId, Long buildNumber, String blobPath,
				@Nullable String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.blobPath = blobPath;
			this.reportName = reportName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Map<Integer, CoverageStatus>> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				Map<String, Map<Integer, CoverageStatus>> coverages = new HashMap<>();
				File categoryDir = new File(OneDev.getInstance(BuildService.class)
						.getBuildDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir)
								&& (reportName == null || reportName.equals(reportDir.getName()))) {
							File lineCoveragesFile = new File(reportDir, FILES + "/" + blobPath);
							if (lineCoveragesFile.exists()) {
								try (var is = new BufferedInputStream(new FileInputStream(lineCoveragesFile))) {
									coverages.put(reportDir.getName(),
											(Map<Integer, CoverageStatus>) SerializationUtils.deserialize(is));
								} catch (SerializationException e) {
									logger.error("Error reading coverage status: " + lineCoveragesFile, e);
								}
							}
						}
					}
				}
				return coverages;
			});
		}

	}

}
