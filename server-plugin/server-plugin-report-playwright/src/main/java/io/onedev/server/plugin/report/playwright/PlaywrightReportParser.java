package io.onedev.server.plugin.report.playwright;

import static io.onedev.server.plugin.report.unittest.UnitTestReport.Status.getOverallStatus;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.StringTransformer;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class PlaywrightReportParser {

	static final String FILES = "files";

	private static final Pattern PATTERN_LOCATION = Pattern.compile("([^\\s()]+):(\\d+):(\\d+)");

	private static final Pattern PATTERN_ANSI_ESCAPE = Pattern.compile(
			"\u001B(?:\\[[0-?]*[ -/]*[@-~]|\\][^\u0007]*(?:\u0007|\u001B\\\\)|[@-_])");

	public static List<TestCase> parse(Build build, JsonNode rootNode) {
		return parse(build, rootNode, null, null, null);
	}

	public static List<TestCase> parse(Build build, JsonNode rootNode, @Nullable File inputDir,
			@Nullable File reportDir, @Nullable TaskLogger logger) {
		Map<String, TestSuiteData> testSuiteDatum = new LinkedHashMap<>();
		for (JsonNode testSuiteNode: rootNode.path("suites"))
			parseTestSuite(build, testSuiteNode, new ArrayList<>(), true, testSuiteDatum,
					inputDir, reportDir, logger);

		List<TestCase> testCases = new ArrayList<>();
		for (TestSuiteData testSuiteData: testSuiteDatum.values()) {
			Status status = getOverallStatus(testSuiteData.testCaseDatum.stream()
					.map(it -> it.status).collect(Collectors.toSet()));
			long duration = testSuiteData.testCaseDatum.stream().mapToLong(it -> it.duration).sum();
			TestSuite testSuite = new TestSuite(testSuiteData.name, status, duration,
					testSuiteData.blobPath, testSuiteData.position) {

				private static final long serialVersionUID = 1L;

				@Nullable
				@Override
				protected Component renderDetail(String componentId, Build build) {
					return null;
				}

			};

			for (TestCaseData testCaseData: testSuiteData.testCaseDatum) {
				var detail = testCaseData.detail;
				var results = testCaseData.results;
				testCases.add(new TestCase(testSuite, testCaseData.name, testCaseData.status,
						testCaseData.statusText, testCaseData.duration) {

					private static final long serialVersionUID = 1L;

					@Nullable
					@Override
					protected Component renderDetail(String componentId, Build build, String reportName) {
						if (shouldRenderResults(results)) {
							return new PlaywrightTestCaseDetailPanel(
									componentId, build, reportName, results);
						} else {
							return PlaywrightReportParser.renderDetail(componentId, build, detail);
						}
					}

				});
			}
		}
		return testCases;
	}

	private static void parseTestSuite(Build build, JsonNode testSuiteNode, List<String> ancestorTitles,
			boolean rootSuite, Map<String, TestSuiteData> testSuiteDatum, @Nullable File inputDir,
			@Nullable File reportDir, @Nullable TaskLogger logger) {
		List<String> titles = new ArrayList<>(ancestorTitles);
		String title = testSuiteNode.path("title").asText();
		String file = testSuiteNode.path("file").asText();
		if (StringUtils.isNotBlank(title) && (!rootSuite || StringUtils.isBlank(file)))
			titles.add(title);

		for (JsonNode specNode: testSuiteNode.path("specs"))
			parseSpec(build, specNode, file, titles, testSuiteDatum, inputDir, reportDir, logger);
		for (JsonNode childSuiteNode: testSuiteNode.path("suites"))
			parseTestSuite(build, childSuiteNode, titles, false, testSuiteDatum,
					inputDir, reportDir, logger);
	}

	private static void parseSpec(Build build, JsonNode specNode, String suiteFile, List<String> titles,
			Map<String, TestSuiteData> testSuiteDatum, @Nullable File inputDir,
			@Nullable File reportDir, @Nullable TaskLogger logger) {
		String file = specNode.path("file").asText(suiteFile);
		String blobPath = StringUtils.isNotBlank(file)? build.getBlobPath(file): null;
		String suiteName;
		if (blobPath != null) {
			suiteName = blobPath;
		} else if (StringUtils.isNotBlank(file)) {
			suiteName = file;
		} else if (!titles.isEmpty()) {
			suiteName = StringUtils.join(titles, "/");
		} else {
			suiteName = "Playwright";
		}

		TestSuiteData testSuiteData = testSuiteDatum.computeIfAbsent(suiteName, it -> {
			var data = new TestSuiteData();
			data.name = suiteName;
			data.blobPath = blobPath;
			int line = specNode.path("line").asInt();
			int column = specNode.path("column").asInt();
			if (line > 0)
				data.position = new PlanarRange(line - 1, Math.max(column - 1, -1), line - 1,
						Math.max(column - 1, -1));
			return data;
		});

		List<String> nameParts = new ArrayList<>(titles);
		nameParts.add(specNode.path("title").asText());
		String baseName = StringUtils.join(nameParts, "/");
		JsonNode testsNode = specNode.path("tests");
		boolean includeProject = testsNode.size() > 1;
		for (JsonNode testNode: testsNode) {
			TestCaseData testCaseData = new TestCaseData();
			String projectName = testNode.path("projectName").asText();
			if (includeProject && StringUtils.isNotBlank(projectName))
				testCaseData.name = baseName + " [" + projectName + "]";
			else
				testCaseData.name = baseName;
			testCaseData.duration = getDuration(testNode);
			testCaseData.detail = getDetail(testNode);
			testCaseData.results = getResults(build, testNode, inputDir, reportDir, logger);
			setStatus(testCaseData, testNode);
			testSuiteData.testCaseDatum.add(testCaseData);
		}
	}

	private static long getDuration(JsonNode testNode) {
		long duration = 0;
		for (JsonNode resultNode: testNode.path("results"))
			duration += resultNode.path("duration").asLong();
		return duration;
	}

	private static List<ResultData> getResults(Build build, JsonNode testNode,
			@Nullable File inputDir, @Nullable File reportDir, @Nullable TaskLogger logger) {
		List<ResultData> results = new ArrayList<>();
		for (JsonNode resultNode: testNode.path("results")) {
			ResultData result = new ResultData();
			result.retry = resultNode.path("retry").asInt();
			result.status = resultNode.path("status").asText();
			result.duration = resultNode.path("duration").asLong();
			result.errors = getErrors(resultNode);
			result.stdout = getOutput(resultNode.path("stdout"));
			result.stderr = getOutput(resultNode.path("stderr"));
			for (JsonNode attachmentNode: resultNode.path("attachments")) {
				String path = attachmentNode.path("path").asText();
				if (StringUtils.isNotBlank(path)) {
					String artifactPath = getArtifactPath(build, path, logger);
					if (artifactPath != null
							&& copyArtifact(inputDir, reportDir, artifactPath, logger)) {
						AttachmentData attachment = new AttachmentData();
						attachment.name = attachmentNode.path("name").asText();
						attachment.contentType = attachmentNode.path("contentType").asText();
						attachment.path = FILES + "/" + artifactPath;
						result.attachments.add(attachment);
					}
				}
			}
			results.add(result);
		}
		return results;
	}

	private static List<String> getErrors(JsonNode resultNode) {
		List<String> errors = new ArrayList<>();
		for (JsonNode errorNode: resultNode.path("errors")) {
			String error = getErrorText(errorNode);
			if (error != null)
				errors.add(error);
		}
		if (errors.isEmpty()) {
			String error = getErrorText(resultNode.path("error"));
			if (error != null)
				errors.add(error);
		}
		return errors;
	}

	private static String getOutput(JsonNode outputNode) {
		List<String> output = new ArrayList<>();
		for (JsonNode entryNode: outputNode) {
			String text;
			if (entryNode.isTextual())
				text = entryNode.asText();
			else
				text = entryNode.path("text").asText();
			if (StringUtils.isNotEmpty(text))
				output.add(text);
		}
		return StringUtils.join(output, "");
	}

	@Nullable
	private static String getArtifactPath(Build build, String path, @Nullable TaskLogger logger) {
		String normalizedPath = normalizePath(path);
		if (normalizedPath == null) {
			warn(logger, "Ignoring Playwright artifact with invalid path: " + path);
			return null;
		}

		if (FilenameUtils.getPrefixLength(normalizedPath) != 0) {
			String workDirPath = normalizePath(build.getWorkDirPath());
			String workDirPrefix = workDirPath != null? StringUtils.stripEnd(workDirPath, "/") + "/": null;
			if (workDirPrefix != null && normalizedPath.startsWith(workDirPrefix)) {
				normalizedPath = normalizedPath.substring(workDirPrefix.length());
			} else {
				warn(logger, "Ignoring Playwright artifact outside build work directory: " + path);
				return null;
			}
		}

		normalizedPath = normalizePath(normalizedPath);
		if (normalizedPath == null || FilenameUtils.getPrefixLength(normalizedPath) != 0) {
			warn(logger, "Ignoring Playwright artifact with unsafe path: " + path);
			return null;
		}
		return normalizedPath;
	}

	@Nullable
	private static String normalizePath(@Nullable String path) {
		if (StringUtils.isBlank(path))
			return null;
		return FilenameUtils.normalize(path.replace('\\', '/'), true);
	}

	private static boolean copyArtifact(@Nullable File inputDir, @Nullable File reportDir,
			String artifactPath, @Nullable TaskLogger logger) {
		if (inputDir == null || reportDir == null)
			return true;
		File artifactFile = new File(inputDir, artifactPath);
		if (!artifactFile.isFile()) {
			warn(logger, "Playwright artifact not found: " + artifactPath);
			return false;
		}
		File targetFile = new File(reportDir, FILES + "/" + artifactPath);
		FileUtils.createDir(targetFile.getParentFile());
		try {
			FileUtils.copyFile(artifactFile, targetFile);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void warn(@Nullable TaskLogger logger, String message) {
		if (logger != null)
			logger.warning(message);
	}

	@Nullable
	private static String getDetail(JsonNode testNode) {
		StringBuilder builder = new StringBuilder();
		for (JsonNode resultNode: testNode.path("results")) {
			List<String> errors = getErrors(resultNode);
			if (!errors.isEmpty()) {
				if (builder.length() != 0)
					builder.append("\n\n");
				int retry = resultNode.path("retry").asInt();
				builder.append(retry != 0? "Retry " + retry: "Initial attempt");
				String resultStatus = resultNode.path("status").asText();
				if (StringUtils.isNotBlank(resultStatus))
					builder.append(" (").append(resultStatus).append(")");
				builder.append(":\n").append(StringUtils.join(errors, "\n\n"));
			}
		}
		return StringUtils.trimToNull(builder.toString());
	}

	@Nullable
	private static String getErrorText(JsonNode errorNode) {
		if (errorNode.isMissingNode() || errorNode.isNull())
			return null;
		String text = errorNode.path("stack").asText();
		if (StringUtils.isBlank(text))
			text = errorNode.path("message").asText();
		if (StringUtils.isBlank(text))
			return null;

		JsonNode locationNode = errorNode.path("location");
		String file = locationNode.path("file").asText();
		int line = locationNode.path("line").asInt();
		int column = locationNode.path("column").asInt();
		if (StringUtils.isNotBlank(file) && line > 0 && !text.contains(file + ":" + line)) {
			StringBuilder builder = new StringBuilder(file).append(":").append(line);
			if (column > 0)
				builder.append(":").append(column);
			return builder.append("\n").append(text).toString();
		}
		return text;
	}

	private static void setStatus(TestCaseData testCaseData, JsonNode testNode) {
		switch (testNode.path("status").asText()) {
			case "expected":
				testCaseData.status = Status.PASSED;
				if (!"passed".equals(testNode.path("expectedStatus").asText()))
					testCaseData.statusText = "expected";
				break;
			case "unexpected":
				testCaseData.status = Status.NOT_PASSED;
				testCaseData.statusText = getLastResultStatus(testNode, "unexpected");
				break;
			case "skipped":
				testCaseData.status = Status.NOT_RUN;
				testCaseData.statusText = "skipped";
				break;
			case "flaky":
				testCaseData.status = Status.OTHER;
				testCaseData.statusText = "flaky";
				break;
			default:
				testCaseData.status = Status.OTHER;
				testCaseData.statusText = testNode.path("status").asText("unknown");
		}
	}

	private static String getLastResultStatus(JsonNode testNode, String defaultStatus) {
		JsonNode resultsNode = testNode.path("results");
		if (!resultsNode.isEmpty()) {
			String status = resultsNode.get(resultsNode.size() - 1).path("status").asText();
			if (StringUtils.isNotBlank(status))
				return status;
		}
		return defaultStatus;
	}

	static boolean shouldRenderResults(List<ResultData> results) {
		return results.size() > 1 || results.stream().anyMatch(ResultData::hasContent);
	}

	@Nullable
	private static Component renderDetail(String componentId, Build build, @Nullable String detail) {
		if (detail == null)
			return null;
		return new Label(componentId, formatDetail(build, detail)).setEscapeModelStrings(false);
	}

	static String formatDetail(Build build, String detail) {
		detail = sanitizeText(detail);
		if (SecurityUtils.canReadCode(build.getProject())) {
			return new StringTransformer(PATTERN_LOCATION) {

				@Override
				protected String transformUnmatched(String string) {
					return escapeHtml5(string);
				}

				@Override
				protected String transformMatched(Matcher matcher) {
					String file = matcher.group(1);
					int line = Integer.parseInt(matcher.group(2));
					int column = Integer.parseInt(matcher.group(3));
					String blobPath = build.getBlobPath(file);
					if (blobPath != null) {
						ProjectBlobPage.State state = new ProjectBlobPage.State();
						state.blobIdent = new BlobIdent(build.getCommitHash(), blobPath);
						PlanarRange range = new PlanarRange(line - 1, column - 1, line - 1, column);
						state.position = BlobRenderer.getSourcePosition(range);
						PageParameters params = ProjectBlobPage.paramsOf(build.getProject(), state);
						String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
						return String.format("<a href='%s'>%s:%d:%d</a>", escapeHtml5(url),
								escapeHtml5(blobPath), line, column);
					} else {
						return escapeHtml5(matcher.group());
					}
				}
			}.transform(detail);
		} else {
			return escapeHtml5(detail);
		}
	}

	static String sanitizeText(String text) {
		if (text == null)
			return "";
		text = PATTERN_ANSI_ESCAPE.matcher(text).replaceAll("");
		StringBuilder builder = new StringBuilder(text.length());
		for (int index = 0; index < text.length();) {
			int codePoint = text.codePointAt(index);
			if (codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD
					|| codePoint >= 0x20 && codePoint <= 0xD7FF
					|| codePoint >= 0xE000 && codePoint <= 0xFFFD
					|| codePoint >= 0x10000 && codePoint <= 0x10FFFF) {
				builder.appendCodePoint(codePoint);
			}
			index += Character.charCount(codePoint);
		}
		return builder.toString();
	}

	private static class TestSuiteData {

		String name;

		String blobPath;

		PlanarRange position;

		List<TestCaseData> testCaseDatum = new ArrayList<>();

	}

	private static class TestCaseData {

		String name;

		Status status;

		String statusText;

		long duration;

		String detail;

		List<ResultData> results = new ArrayList<>();

	}

	static class ResultData implements Serializable {

		private static final long serialVersionUID = 1L;

		int retry;

		String status;

		long duration;

		List<String> errors = new ArrayList<>();

		String stdout;

		String stderr;

		List<AttachmentData> attachments = new ArrayList<>();

		boolean hasContent() {
			return !errors.isEmpty()
					|| StringUtils.isNotEmpty(stdout)
					|| StringUtils.isNotEmpty(stderr)
					|| !attachments.isEmpty();
		}

	}

	static class AttachmentData implements Serializable {

		private static final long serialVersionUID = 1L;

		String name;

		String contentType;

		String path;

	}

}
