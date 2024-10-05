package io.onedev.server.plugin.report.pylint;

import com.fasterxml.jackson.databind.JsonNode;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;

import java.util.*;

import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class PylintReportParser {
	
	public static List<CodeProblem> parse(Build build, JsonNode report, TaskLogger logger) {
		List<CodeProblem> problems = new ArrayList<>();
		Map<String, Optional<String>> blobPaths = new HashMap<>();
		for (var problemNode: report) {
			var type = problemNode.get("type").asText();
			CodeProblem.Severity severity;
			switch (type.toLowerCase()) {
				case "error":
					severity = CodeProblem.Severity.HIGH;
					break;
				case "warning":
					severity = CodeProblem.Severity.MEDIUM;
					break;
				default:
					severity = CodeProblem.Severity.LOW;
			}
			var message = problemNode.get("message-id").asText() + ": " + problemNode.get("message").asText();
			var filePath = problemNode.get("path").asText();
			var blobPath = blobPaths.get(filePath);
			if (blobPath == null) {
				blobPath = Optional.ofNullable(build.getBlobPath(filePath));
				if (blobPath.isEmpty())
					logger.warning("Unable to find blob path for file: " + filePath);
				blobPaths.put(filePath, blobPath);
			}
			if (blobPath.isPresent()) {
				int line = parseInt(problemNode.get("line").asText());
				int column = parseInt(problemNode.get("column").asText());
				int endLine = line;
				if (problemNode.hasNonNull("endLine"))
					endLine = parseInt(problemNode.get("endLine").asText());
				int endColumn = -1;
				if (problemNode.hasNonNull("endColumn"))
					endColumn = parseInt(problemNode.get("endColumn").asText());
				var location = new PlanarRange(line-1, column, endLine-1, endColumn);
				problems.add(new CodeProblem(severity, new BlobTarget(blobPath.get(), location), escapeHtml5(message)));
			}
		}
		return problems;
	}

}
