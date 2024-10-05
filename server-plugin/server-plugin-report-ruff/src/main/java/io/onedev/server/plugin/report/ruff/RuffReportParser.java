package io.onedev.server.plugin.report.ruff;

import com.fasterxml.jackson.databind.JsonNode;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;

import java.util.*;

import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class RuffReportParser {
	
	public static List<CodeProblem> parse(Build build, JsonNode report, TaskLogger logger) {
		List<CodeProblem> problems = new ArrayList<>();
		Map<String, Optional<String>> blobPaths = new HashMap<>();
		for (var problemNode: report) {
			var message = problemNode.get("code").asText() + ": " + problemNode.get("message").asText();
			var filePath = problemNode.get("filename").asText();
			var blobPath = blobPaths.get(filePath);
			if (blobPath == null) {
				blobPath = Optional.ofNullable(build.getBlobPath(filePath));
				if (blobPath.isEmpty())
					logger.warning("Unable to find blob path for file: " + filePath);
				blobPaths.put(filePath, blobPath);
			}
			if (blobPath.isPresent()) {
				int row = parseInt(problemNode.get("location").get("row").asText());
				int column = parseInt(problemNode.get("location").get("column").asText());
				int endRow = parseInt(problemNode.get("end_location").get("row").asText());
				int endColumn = parseInt(problemNode.get("end_location").get("column").asText());
				
				var location = new PlanarRange(row-1, column-1, endRow-1, endColumn-1);
				problems.add(new CodeProblem(CodeProblem.Severity.MEDIUM, new BlobTarget(blobPath.get(), location), escapeHtml5(message)));
			}
		}
		return problems;
	}

}
