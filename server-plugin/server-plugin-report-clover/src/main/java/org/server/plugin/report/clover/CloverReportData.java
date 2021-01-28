package org.server.plugin.report.clover;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;

public class CloverReportData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String FILE_NAME = "data.ser";

	private final int totalStatements;
	
	private final int coveredStatements;
	
	private final int totalMethods;
	
	private final int coveredMethods;
	
	private final int totalBranches;
	
	private final int coveredBranches;
	
	private final int totalLines;
	
	private final int coveredLines;
	
	public CloverReportData(int totalStatements, int coveredStatements, int totalMethods, int coveredMethods, 
			int totalBranches, int coveredBranches, int totalLines, int coveredLines) {
		this.totalStatements = totalStatements;
		this.coveredStatements = coveredStatements;
		this.totalMethods = totalMethods;
		this.coveredMethods = coveredMethods;
		this.totalBranches = coveredBranches;
		this.coveredBranches = coveredBranches;
		this.totalLines = totalLines;
		this.coveredLines = coveredLines;
	}

	public int getTotalStatements() {
		return totalStatements;
	}

	public int getCoveredStatements() {
		return coveredStatements;
	}

	public int getTotalMethods() {
		return totalMethods;
	}

	public int getCoveredMethods() {
		return coveredMethods;
	}

	public int getTotalBranches() {
		return totalBranches;
	}

	public int getCoveredBranches() {
		return coveredBranches;
	}

	public int getTotalLines() {
		return totalLines;
	}

	public int getCoveredLines() {
		return coveredLines;
	}

	@Nullable
	public static CloverReportData readFrom(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		if (dataFile.exists()) {
			try (InputStream is = new FileInputStream(dataFile)) {
				return (CloverReportData) SerializationUtils.deserialize(is);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}
	
	public void writeTo(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (OutputStream os = new FileOutputStream(dataFile)) {
			SerializationUtils.serialize(this, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}
	
	public int getMethodCoverage() {
		if (totalMethods != 0)
			return coveredMethods*100/totalMethods;
		else
			return 0;
	}
	
	public int getStatementCoverage() {
		if (totalStatements != 0)
			return coveredStatements*100/totalStatements;
		else
			return 0;
	}
	
	public int getBranchCoverage() {
		if (totalBranches != 0)
			return coveredBranches*100/totalBranches;
		else
			return 0;
	}
	
	public int getLineCoverage() {
		if (totalLines != 0)
			return coveredLines*100/totalLines;
		else
			return 0;
	}
	
}
