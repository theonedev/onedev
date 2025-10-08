package io.onedev.server.plugin.report.clippy;

import com.fasterxml.jackson.databind.JsonNode;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;

import org.jspecify.annotations.Nullable;
import java.util.*;

import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class ClippyReportParser {
	
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
	
	public static List<CodeProblem> parse(Build build, List<JsonNode> problemNodes, TaskLogger logger) {
		var problems = new ArrayList<CodeProblem>();
		Map<String, Optional<String>> blobPaths = new HashMap<>();
		for (var problemNode: problemNodes) {
			var messageNode = problemNode.get("message");
			if (messageNode != null && messageNode.hasNonNull("spans") 
					&& !messageNode.get("spans").isEmpty() && messageNode.hasNonNull("rendered")) {
				CodeProblem.Severity severity = CodeProblem.Severity.LOW;
				var levelNode = messageNode.get("level");
				if (levelNode != null) {
					switch (levelNode.asText()) {
						case "error":
							severity = CodeProblem.Severity.HIGH;
							break;
						case "warning":
							severity = CodeProblem.Severity.MEDIUM;
					}
				}
				var message = new StringBuilder();
				message.append(messageNode.get("rendered").asText());
				var spanNode = messageNode.get("spans").iterator().next();
				var blobPath = getBlobPath(build, blobPaths, spanNode.get("file_name").asText(), logger);
				if (blobPath != null) {
					PlanarRange location;
					int lineStart = parseInt(spanNode.get("line_start").asText());
					int columnStart = parseInt(spanNode.get("column_start").asText());
					int lineEnd = parseInt(spanNode.get("line_end").asText());
					int columnEnd = parseInt(spanNode.get("column_end").asText());
					if (lineStart == lineEnd && columnStart == columnEnd)
						columnEnd++;
					location = new PlanarRange(lineStart - 1, columnStart - 1, lineEnd - 1, columnEnd - 1);
					problems.add(new CodeProblem(severity, new BlobTarget(blobPath, location), escapeHtml5(message.toString())));
				}
			}
		}
		return problems;
	}

}
