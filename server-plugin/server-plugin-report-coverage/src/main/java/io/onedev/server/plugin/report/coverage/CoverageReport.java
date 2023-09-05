package io.onedev.server.plugin.report.coverage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.Build;
import org.apache.commons.lang3.SerializationUtils;

public class CoverageReport implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String CATEGORY = "coverage";
	
	public static final String FILES = "files";
	
	private static final String REPORT = "report.ser";
	
	private final CoverageInfo overallCoverages;
	
	private final List<GroupCoverageInfo> groupCoverages;
	
	public CoverageReport(CoverageInfo overallCoverages, List<GroupCoverageInfo> groupCoverages) {
		this.overallCoverages = overallCoverages;
		this.groupCoverages = groupCoverages;
	}

	public CoverageInfo getOverallCoverages() {
		return overallCoverages;
	}

	public List<GroupCoverageInfo> getGroupCoverages() {
		return groupCoverages;
	}
	
	public static CoverageReport readFrom(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(reportFile))) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return CoverageReport.class.getName() + ":"	+ projectId + ":" + buildNumber;
	}

}
