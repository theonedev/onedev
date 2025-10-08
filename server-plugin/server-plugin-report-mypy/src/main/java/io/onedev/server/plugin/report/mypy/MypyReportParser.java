package io.onedev.server.plugin.report.mypy;

import static io.onedev.server.codequality.CodeProblem.Severity.LOW;
import static io.onedev.server.codequality.CodeProblem.Severity.MEDIUM;
import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;

public class MypyReportParser {
	
	private static ParsedLine parseLine(String line) {
		ParsedLine parsedLine = new ParsedLine();
		var fields = Splitter.on(':').splitToList(line); 
		List<Integer> locations = new ArrayList<>();
		for (int i=0; i<fields.size(); i++) {
			var field = fields.get(i);
			if (i == 0) {
				parsedLine.filePath = field;
			} else if (NumberUtils.isDigits(field)) {
				locations.add(parseInt(field));
			} else if (locations.isEmpty()) {
				parsedLine.filePath += ":" + field;
			} else {
				if (locations.size() == 4) {
					parsedLine.fromRow = locations.get(0);
					parsedLine.fromColumn = locations.get(1);
					parsedLine.toRow = locations.get(2);
					parsedLine.toColumn = locations.get(3);
				} else if (locations.size() == 2) {
					parsedLine.fromRow = locations.get(0);
					parsedLine.fromColumn = locations.get(1);
					parsedLine.toRow = parsedLine.fromRow;
					parsedLine.toColumn = -1;
				} else {
					parsedLine.fromRow = locations.get(0);
					parsedLine.fromColumn = 1;
					parsedLine.toRow = parsedLine.fromRow;
					parsedLine.toColumn = -1;
				}
				parsedLine.error = field.trim().equals("error");
				var message = Joiner.on(':').join(fields.subList(i+1, fields.size()));
				if (message.startsWith("  ")) {
					parsedLine.complementary = true;
					parsedLine.message = message.substring(2);
				} else {
					parsedLine.message = message.substring(1);
				}
				return parsedLine;
			}
		}
		throw new ExplicitException("Error parsing mypy output: no message found");
	}
	
	private static void populateCodeProblems(List<CodeProblem> problems, Build build, Map<String, Optional<String>> blobPaths, 
									   ParsedLine parsedLine, TaskLogger logger) {
		var blobPath = blobPaths.get(parsedLine.filePath);
		if (blobPath == null) {
			blobPath = Optional.ofNullable(build.getBlobPath(parsedLine.filePath));
			if (blobPath.isEmpty())
				logger.warning("Unable to find blob path for file: " + parsedLine.filePath);
			blobPaths.put(parsedLine.filePath, blobPath);
		}
		if (blobPath.isPresent()) {
			var location = new PlanarRange(parsedLine.fromRow-1, parsedLine.fromColumn-1, parsedLine.toRow-1, parsedLine.toColumn, 1);
			var severity = parsedLine.error?MEDIUM:LOW;
			problems.add(new CodeProblem(severity, new BlobTarget(blobPath.get(), location), escapeHtml5(parsedLine.message)));
		}
	}
	
	public static List<CodeProblem> parse(Build build, List<String> output, TaskLogger logger) {
		List<CodeProblem> problems = new ArrayList<>();
		Map<String, Optional<String>> blobPaths = new HashMap<>();
		ParsedLine leadingParsedLine = null;
		for (var line: output) {
			if (line.contains(":")) {
				var parsedLine = parseLine(line);
				if (leadingParsedLine == null) {
					leadingParsedLine = parsedLine;
				} else if (parsedLine.complementary) {
					leadingParsedLine.message += "\n" + parsedLine.message;
				} else {
					populateCodeProblems(problems, build, blobPaths, leadingParsedLine, logger);
					leadingParsedLine = parsedLine;
				}
			}
		}
		if (leadingParsedLine != null)
			populateCodeProblems(problems, build, blobPaths, leadingParsedLine, logger);			
		return problems;
	}

	private static class ParsedLine {

		String filePath;
		
		int fromRow, fromColumn, toRow, toColumn;
		
		boolean error;
		
		boolean complementary;
		
		String message;
		
	}
}
