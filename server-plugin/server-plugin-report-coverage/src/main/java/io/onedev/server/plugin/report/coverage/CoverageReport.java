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
	
	public static final String FILES_DIR = "files";
	
	private static final String FILE_NAME = "report.ser";
	
	private final CoverageInfo coverages;
	
	private final List<PackageCoverageInfo> packageCoverages;
	
	public CoverageReport(CoverageInfo coverages, List<PackageCoverageInfo> packageCoverages) {
		this.coverages = coverages;
		this.packageCoverages = packageCoverages;
	}

	public CoverageInfo getOverallCoverages() {
		return coverages;
	}

	public List<PackageCoverageInfo> getPackageCoverages() {
		return packageCoverages;
	}
	
	public static CoverageReport readFrom(File reportDir) {
		File reportFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(reportFile))) {
			return (CoverageReport) SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File reportFile = new File(reportDir, FILE_NAME);
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
