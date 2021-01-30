package org.server.plugin.report.clover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.model.CloverMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(name="Clover Coverage Metrics", description="Collected coverage information will be displayed "
		+ "when view source files")
public class JobCloverReport extends JobReport {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "clover-reports";
	
	public static final String LINE_COVERAGES_DIR = "line-coverages";
	
	@Editable(order=100, description="Specify clover xml file containing coverage metrics. This should be a "
			+ "relative path under OneDev workspace. For instance, <tt>target/site/clover/clover.xml</tt>. "
			+ "Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> "
			+ "on how to generate clover report. Use * or ? for pattern match")
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

		CloverReportData report = LockUtils.write(build.getReportLockKey(DIR), new Callable<CloverReportData>() {

			@Override
			public CloverReportData call() throws Exception {
				int baseLen = workspace.getAbsolutePath().length() + 1;
				SAXReader reader = new SAXReader();
				
				// Prevent XXE attack as the xml might be provided by malicious users
				reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

				int totalStatements = 0;
				int coveredStatements = 0;
				int totalMethods = 0;
				int coveredMethods = 0;
				int totalBranches = 0;
				int coveredBranches = 0;
				int totalLines = 0;
				int coveredLines = 0;

				boolean hasReport = false;
				for (File file: getPatternSet().listFiles(workspace)) {
					logger.log("Processing clover coverage report: " + file.getAbsolutePath().substring(baseLen));
					try {
						Document doc = reader.read(file);
						for (Element projectElement: doc.getRootElement().elements("project")) {
							Element metricsElement = projectElement.element("metrics");
							
							totalStatements += Integer.parseInt(metricsElement.attributeValue("statements"));
							totalMethods += Integer.parseInt(metricsElement.attributeValue("methods"));
							totalBranches += Integer.parseInt(metricsElement.attributeValue("conditionals"));
							
							coveredStatements += Integer.parseInt(metricsElement.attributeValue("coveredstatements"));
							coveredMethods += Integer.parseInt(metricsElement.attributeValue("coveredmethods"));
							coveredBranches += Integer.parseInt(metricsElement.attributeValue("coveredconditionals"));
							
							for (Element packageElement: projectElement.elements("package")) {
								for (Element fileElement: packageElement.elements("file")) {
									String path = fileElement.attributeValue("path");
									if (build.getJobWorkspace() != null && path.startsWith(build.getJobWorkspace())) { 
										path = path.substring(build.getJobWorkspace().length()+1);
										Map<Integer, Integer> lineCoverages = new HashMap<>();
										for (Element lineElement: fileElement.elements("line")) {
											int lineNum = Integer.parseInt(lineElement.attributeValue("num")) - 1;
											String testCountStr = lineElement.attributeValue("count");
											if (testCountStr != null) {
												int testCount = Integer.parseInt(testCountStr);
												lineCoverages.merge(lineNum, testCount, (v1, v2)->v1+v2);
											}
										}
										totalLines += lineCoverages.size();
										coveredLines += lineCoverages.entrySet().stream().filter(it->it.getValue()!=0).count();
										
										File lineCoverateFile = new File(reportDir, LINE_COVERAGES_DIR + "/" + path);
										FileUtils.createDir(lineCoverateFile.getParentFile());
										try (OutputStream os = new FileOutputStream(lineCoverateFile)) {
											SerializationUtils.serialize((Serializable) lineCoverages, os);
										} catch (IOException e) {
											throw new RuntimeException(e);
										};
									}
								}
							}
						}
					} catch (Exception e) {
						throw ExceptionUtils.unchecked(e);
					}
					hasReport = true;
				}
				if (hasReport) {
					CloverReportData report = new CloverReportData(totalStatements, coveredStatements, totalMethods, coveredMethods, 
							totalBranches, coveredBranches, totalLines, coveredLines);
					FileUtils.createDir(reportDir);
					report.writeTo(reportDir);
					return report;
				} else {
					return null;
				}
			}
			
		});

		if (report != null) {
			CloverMetric metric = new CloverMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			metric.setBranchCoverage(report.getBranchCoverage());
			metric.setLineCoverage(report.getLineCoverage());
			metric.setMethodCoverage(report.getMethodCoverage());
			metric.setStatementCoverage(report.getStatementCoverage());
			metric.setTotalBranches(report.getTotalBranches());
			metric.setTotalLines(report.getTotalLines());
			metric.setTotalMethods(report.getTotalMethods());
			metric.setTotalStatements(report.getTotalStatements());
			
			OneDev.getInstance(Dao.class).persist(metric);
		} 
	}

}
