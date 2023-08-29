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

import org.apache.commons.lang.SerializationUtils;

import io.onedev.server.model.Build;

public class CoverageReport implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String CATEGORY = "coverage";
	
	public static final String FILES = "files";
	
	private static final String REPORT = "report.ser";
	
	private final CoverageInfo overallCoverages;
	
	private final List<CategoryCoverageInfo> packageCoverages;
	
	public CoverageReport(CoverageInfo overallCoverages, List<CategoryCoverageInfo> packageCoverages) {
		this.overallCoverages = overallCoverages;
		this.packageCoverages = packageCoverages;
	}

	public CoverageInfo getOverallCoverages() {
		return overallCoverages;
	}

	public List<CategoryCoverageInfo> getPackageCoverages() {
		return packageCoverages;
	}
	
	public static CoverageReport readFrom(File reportDir) {
		File reportFile = new File(reportDir, REPORT);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return (CoverageReport) SerializationUtils.deserialize(is);
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
