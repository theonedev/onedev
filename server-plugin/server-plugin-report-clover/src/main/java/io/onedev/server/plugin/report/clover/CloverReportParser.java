package io.onedev.server.plugin.report.clover;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.coverage.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class CloverReportParser {

	public static CoverageReport parse(Build build, Document doc, TaskLogger logger) {
		var overallCoverage = new Coverage();

		List<GroupCoverage> packageCoverages = new ArrayList<>();
		Map<String, Map<Integer, CoverageStatus>> coverageStatuses = new HashMap<>();

		for (Element projectElement : doc.getRootElement().elements("project")) {
			for (Element packageElement : projectElement.elements("package")) {
				String packageName = packageElement.attributeValue("name");
				var pair = parsePackageCoverage(packageName, packageElement.elements("file"), build, logger);
				var packageCoverage = pair.getLeft();
				if (!packageCoverage.getFileCoverages().isEmpty()) {
					packageCoverages.add(packageCoverage);
					coverageStatuses.putAll(pair.getRight());
					overallCoverage.mergeWith(packageCoverage);
				}
			}

			var pair = parsePackageCoverage("[root]", projectElement.elements("file"), build, logger);
			var packageCoverage = pair.getLeft();
			if (!packageCoverage.getFileCoverages().isEmpty()) {
				packageCoverages.add(packageCoverage);
				coverageStatuses.putAll(pair.getRight());
				overallCoverage.mergeWith(packageCoverage);
			}
		}
		
		return new CoverageReport(
				new CoverageStats(overallCoverage, packageCoverages), 
				coverageStatuses);
	}
	
	private static Pair<GroupCoverage, Map<String, Map<Integer, CoverageStatus>>> parsePackageCoverage(
			String packageName, List<Element> fileElements, Build build, TaskLogger logger) {
		var packageCoverage = new Coverage();
		List<FileCoverage> fileCoverages = new ArrayList<>();
		Map<String, Map<Integer, CoverageStatus>> coverageStatuses = new HashMap<>();
		for (Element fileElement : fileElements) {
			var filePath = fileElement.attributeValue("path");
			if (filePath == null)
				filePath = fileElement.attributeValue("name");
			String blobPath = build.getBlobPath(filePath);
			if (blobPath != null) {
				var metricsElement = fileElement.element("metrics");

				int totalBranches = parseInt(metricsElement.attributeValue("conditionals"));
				int coveredBranches = parseInt(metricsElement.attributeValue("coveredconditionals"));

				Map<Integer, CoverageStatus> coverageStatusesOfFile = new HashMap<>();
				for (Element lineElement : fileElement.elements("line")) {
					int lineNum = parseInt(lineElement.attributeValue("num")) - 1;
					CoverageStatus prevStatus = coverageStatusesOfFile.get(lineNum);

					String countStr = lineElement.attributeValue("count");
					if (countStr != null)
						coverageStatusesOfFile.put(lineNum, getCoverageStatus(prevStatus, countStr));

					countStr = lineElement.attributeValue("truecount");
					if (countStr != null)
						coverageStatusesOfFile.put(lineNum, getCoverageStatus(prevStatus, countStr));

					countStr = lineElement.attributeValue("falsecount");
					if (countStr != null)
						coverageStatusesOfFile.put(lineNum, getCoverageStatus(prevStatus, countStr));
				}
				int totalLines = coverageStatusesOfFile.size();
				int coveredLines = (int) coverageStatusesOfFile.entrySet().stream().filter(it -> it.getValue() != CoverageStatus.NOT_COVERED).count();

				var fileCoverage = new FileCoverage(blobPath, totalBranches, coveredBranches, totalLines, coveredLines);
				fileCoverages.add(fileCoverage);
				if (!coverageStatusesOfFile.isEmpty())
					coverageStatuses.put(blobPath, coverageStatusesOfFile);
				packageCoverage.mergeWith(fileCoverage);
			} else {
				logger.warning("Unable to find blob path for file: " + filePath);
			}
		}
		return new ImmutablePair<>(new GroupCoverage(packageName, packageCoverage, fileCoverages), coverageStatuses);
	}

	private static CoverageStatus getCoverageStatus(CoverageStatus prevStatus, String countStr) {
		int count = parseInt(countStr);
		if (count != 0)
			return CoverageStatus.COVERED.mergeWith(prevStatus);
		else  
			return CoverageStatus.NOT_COVERED.mergeWith(prevStatus);
	}
	
}
