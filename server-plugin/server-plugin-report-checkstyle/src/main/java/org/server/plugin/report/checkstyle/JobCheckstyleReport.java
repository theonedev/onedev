package org.server.plugin.report.checkstyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.code.CodeProblem.Severity;
import io.onedev.server.model.Build;
import io.onedev.server.model.CheckstyleMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(name="Checkstyle Report")
public class JobCheckstyleReport extends JobReport {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "checkstyle-reports";
	
	public static final String VIOLATION_FILES = "violation-files";
	
	@Editable(order=100, description="Specify checkstyle result xml file. This should be a "
			+ "relative path under OneDev workspace. For instance, <tt>target/checkstyle-result.xml</tt>. "
			+ "Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> "
			+ "on how to generate the result xml file. Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

	@Override
	public void process(Build build, File workspace, SimpleLogger logger) {
		File reportDir = new File(build.getReportDir(DIR), getReportName());
		
		CheckstyleReportData reportData = LockUtils.write(build.getReportLockKey(DIR), new Callable<CheckstyleReportData>() {

			@Override
			public CheckstyleReportData call() throws Exception {
				int baseLen = workspace.getAbsolutePath().length() + 1;
				SAXReader reader = new SAXReader();
				
				// Prevent XXE attack as the xml might be provided by malicious users
				reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

				List<CheckstyleViolation> violations = new ArrayList<>();
				
				for (File file: getPatternSet().listFiles(workspace)) {
					logger.log("Processing checkstyle report: " + file.getAbsolutePath().substring(baseLen));
					Document doc = reader.read(file);
					for (Element fileElement: doc.getRootElement().elements("file")) {
						String filePath = fileElement.attributeValue("name");
						if (build.getJobWorkspace() != null && filePath.startsWith(build.getJobWorkspace())) { 
							filePath = filePath.substring(build.getJobWorkspace().length()+1);
							List<ViolationFile.Violation> violationsOfFile = new ArrayList<>();
							for (Element violationElement: fileElement.elements()) {
								Severity severity = Severity.valueOf(violationElement.attributeValue("severity").toUpperCase());
								String message = violationElement.attributeValue("message");
								String rule = violationElement.attributeValue("source");
								String line = violationElement.attributeValue("line");
								String column = violationElement.attributeValue("column");
								violationsOfFile.add(new ViolationFile.Violation(severity, message, line, column, rule));
								violations.add(new CheckstyleViolation(severity, message, line, column, filePath, rule));
							}
							if (!violationsOfFile.isEmpty()) {
								File violationsFile = new File(reportDir, VIOLATION_FILES + "/" + filePath);
								FileUtils.createDir(violationsFile.getParentFile());
								try (OutputStream os = new FileOutputStream(violationsFile)) {
									SerializationUtils.serialize((Serializable) violationsOfFile, os);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
				}
				
				return new CheckstyleReportData(violations);
			}
			
		});
		
		if (!reportData.getViolations().isEmpty()) {
			FileUtils.createDir(reportDir);
			reportData.writeTo(reportDir);
			
			CheckstyleMetric metric = new CheckstyleMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			metric.setTotalErrors((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.ERROR)
					.count());
			metric.setTotalWarnings((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.WARNING)
					.count());
			metric.setTotalInfos((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.INFO)
					.count());
						
			OneDev.getInstance(Dao.class).persist(metric);
		}
	}

}
