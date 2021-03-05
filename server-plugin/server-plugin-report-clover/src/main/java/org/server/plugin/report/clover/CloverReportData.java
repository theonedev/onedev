package org.server.plugin.report.clover;

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

public class CloverReportData implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String FILE_NAME = "data.ser";
	
	private final CoverageInfo coverages;
	
	private final List<PackageCoverageInfo> packageCoverages;
	
	public CloverReportData(CoverageInfo coverages, List<PackageCoverageInfo> packageCoverages) {
		this.coverages = coverages;
		this.packageCoverages = packageCoverages;
	}

	public CoverageInfo getOverallCoverages() {
		return coverages;
	}

	public List<PackageCoverageInfo> getPackageCoverages() {
		return packageCoverages;
	}
	
	public static CloverReportData readFrom(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(dataFile))) {
			return (CloverReportData) SerializationUtils.deserialize(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeTo(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile))) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}
	
}
