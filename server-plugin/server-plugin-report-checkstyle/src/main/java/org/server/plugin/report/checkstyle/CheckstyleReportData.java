package org.server.plugin.report.checkstyle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.server.plugin.report.checkstyle.ViolationFile.Violation;

public class CheckstyleReportData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String FILE_NAME = "data.ser";

	private final List<CheckstyleViolation> violations;
	
	private transient List<ViolationFile> violationFiles;
	
	private transient List<ViolationRule> violationRules;
	
	public CheckstyleReportData(List<CheckstyleViolation> violations) {
		this.violations = violations;
	}

	public List<CheckstyleViolation> getViolations() {
		return violations;
	}

	public List<ViolationFile> getViolationFiles() {
		if (violationFiles == null) {
			Map<String, ViolationFile> filesMap = new LinkedHashMap<>();
			for (CheckstyleViolation violation: violations) {
				ViolationFile file = filesMap.get(violation.getFile());
				if (file == null) {
					file = new ViolationFile(violation.getFile());
					filesMap.put(violation.getFile(), file);
				}
				file.getViolations().add(new ViolationFile.Violation(
						violation.getSeverity(), violation.getMessage(), 
						violation.getLine(), violation.getColumn(), 
						violation.getRule()));
			}
			
			violationFiles = new ArrayList<>(filesMap.values());
			for (ViolationFile file: violationFiles) {
				file.getViolations().sort(new Comparator<ViolationFile.Violation>() {
	
					@Override
					public int compare(Violation o1, Violation o2) {
						return o1.getSeverity().ordinal() - o2.getSeverity().ordinal();
					}
					
				});
			}
			violationFiles.sort(new Comparator<ViolationFile>() {
	
				@Override
				public int compare(ViolationFile o1, ViolationFile o2) {
					if (o1.getNumOfErrors() != o2.getNumOfErrors())
						return o2.getNumOfErrors() - o1.getNumOfErrors();
					else if (o1.getNumOfWarnings() != o2.getNumOfWarnings())
						return o2.getNumOfWarnings() - o1.getNumOfWarnings();
					else
						return o2.getNumOfInfos() - o1.getNumOfInfos();
				}
				
			});
		}
		return violationFiles;
	}
	
	public List<ViolationRule> getViolationRules() {
		if (violationRules == null) {
			Map<String, ViolationRule> rulesMap = new LinkedHashMap<>();
			for (CheckstyleViolation violation: violations) {
				ViolationRule rule = rulesMap.get(violation.getRule());
				if (rule == null) {
					rule = new ViolationRule(violation.getRule(), violation.getSeverity());
					rulesMap.put(violation.getRule(), rule);
				}
				rule.getViolations().add(new ViolationRule.Violation(
						violation.getMessage(), violation.getLine(), 
						violation.getColumn(), violation.getFile()));
			}
			
			violationRules = new ArrayList<>(rulesMap.values());
			
			violationRules.sort(new Comparator<ViolationRule>() {

				@Override
				public int compare(ViolationRule o1, ViolationRule o2) {
					if (o1.getSeverity() != o2.getSeverity())
						return o1.getSeverity().ordinal() - o2.getSeverity().ordinal();
					else
						return o2.getViolations().size() - o1.getViolations().size();
				}
				
			});
		}
		return violationRules;
	}
	
	public static CheckstyleReportData readFrom(File reportDir) {
		File dataFile = new File(reportDir, FILE_NAME);
		try (InputStream is = new BufferedInputStream(new FileInputStream(dataFile))) {
			return (CheckstyleReportData) SerializationUtils.deserialize(is);
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
