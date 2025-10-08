package io.onedev.server.plugin.report.cppcheck;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;
import org.dom4j.Document;

import org.jspecify.annotations.Nullable;
import java.util.*;

import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class CppcheckReportParser {
	
	@Nullable
	private static String getBlobPath(Build build, Map<String, Optional<String>> blobPaths, 
									  String filePath, TaskLogger logger) {
		var blobPath = blobPaths.get(filePath);
		if (blobPath == null) {
			blobPath = Optional.ofNullable(build.getBlobPath(filePath));
			if (blobPath.isEmpty())
				logger.warning("Unable to find blob path for file: " + filePath);
			blobPaths.put(filePath, blobPath);
		}
		return blobPath.orElse(null);
	}
	
	public static List<CodeProblem> parse(Build build, Document report, TaskLogger logger) {
		List<CodeProblem> problems = new ArrayList<>();
		Map<String, Optional<String>> blobPaths = new HashMap<>();
		for (var errorElement: report.getRootElement().element("errors").elements()) {
			CodeProblem.Severity severity;
			switch (errorElement.attributeValue("severity")) {
				case "error":
					severity = CodeProblem.Severity.HIGH;
					break;
				case "warning":
					severity = CodeProblem.Severity.MEDIUM;
					break;
				default:
					severity = CodeProblem.Severity.LOW;
			}
			StringBuilder message = new StringBuilder(errorElement.attributeValue("id"));
			if (errorElement.attributeValue("verbose") != null)
				message.append(": ").append(errorElement.attributeValue("verbose"));
			else if (errorElement.attributeValue("msg") != null)
				message.append(": ").append(errorElement.attributeValue("msg"));
			var locationElements = errorElement.elements("location");
			if (!locationElements.isEmpty()) {
				var locationElement = locationElements.get(0);
				var blobPath = getBlobPath(build, blobPaths, locationElement.attributeValue("file"), logger);
				if (blobPath != null) {
					if (locationElements.size() > 1) {
						message.append("\n\nOther locations:");
						for (int i=1; i<locationElements.size(); i++) {
							var otherLocationElement = locationElements.get(i);
							var otherBlobPath = getBlobPath(build, blobPaths, otherLocationElement.attributeValue("file"), logger);
							if (otherBlobPath != null) 
								message.append("\n    ").append(otherBlobPath).append(", line:").append(otherLocationElement.attributeValue("line")).append(", column:").append(otherLocationElement.attributeValue("column"));
						}
					}
					
					PlanarRange location;
					int line = parseInt(locationElement.attributeValue("line"));
					int column = parseInt(locationElement.attributeValue("column"));
					if (column != 0)
						location = new PlanarRange(line-1, column-1, line-1, column);
					else
						location = new PlanarRange(line-1, -1, line-1, -1);						
					problems.add(new CodeProblem(severity, new BlobTarget(blobPath, location), escapeHtml5(message.toString())));
				}
			}
		}
		return problems;
	}

}
