package io.onedev.server.plugin.report.playwright;

import static io.onedev.server.codequality.UnitTestReport.ARTIFACTS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import io.onedev.server.model.Build;
import io.onedev.server.codequality.UnitTestReport;
import io.onedev.server.codequality.UnitTestReport.Status;
import io.onedev.server.codequality.UnitTestReport.TestCase;
import io.onedev.server.util.patternset.PatternSet;

public class PlaywrightReportParserTest {

	private static final String ARTIFACT_DIR =
			"confidential-issue-Issue-R-14762-d-view-a-confidential-issue-chromium";

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void shouldParseReport() {
		try (InputStream is = Resources.getResource(
				PlaywrightReportParserTest.class, "test-results.json").openStream()) {
			JsonNode rootNode = new ObjectMapper().readTree(is);
			Build build = new Build() {

				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}

				@Override
				public String getWorkDirPath() {
					return "/onedev-build/work";
				}

			};

			File inputDir = temp.newFolder("input");
			File reportDir = temp.newFolder("report");
			copyResource(ARTIFACT_DIR + "/test-failed-1.png", inputDir);
			copyResource(ARTIFACT_DIR + "/error-context.md", inputDir);
			UnitTestReport report = new UnitTestReport(
					PlaywrightReportParser.parse(build, rootNode, inputDir, reportDir, null), true);
			assertEquals(1, report.getTestCases(null, null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(1, report.getTestCases(null, null, Sets.newHashSet(Status.NOT_PASSED)).size());
			assertEquals(0, report.getTestCases(null, null, Sets.newHashSet(Status.NOT_RUN)).size());
			assertEquals(0, report.getTestCases(null, null, Sets.newHashSet(Status.OTHER)).size());
			assertEquals(2, report.getTestCases().size());

			assertEquals(1, report.getTestSuites(null, Sets.newHashSet(Status.NOT_PASSED)).size());
			assertEquals(1, report.getTestSuites(null, Sets.newHashSet(Status.PASSED)).size());
			assertEquals(2, report.getTestSuites().size());
			assertEquals(120057, report.getTestSuites().stream()
					.filter(it -> it.getName().equals("confidential-issue.spec.js"))
					.findFirst().orElseThrow().getDuration());

			TestCase timedOut = report.getTestCases().stream()
					.filter(it -> it.getName().equals(
							"Issue Reporter can create and view a confidential issue"))
					.findFirst().orElseThrow();
			assertEquals(Status.NOT_PASSED, timedOut.getStatus());
			assertEquals("timedOut", timedOut.getStatusText());
			assertEquals(120057, timedOut.getDuration());
			List<Map<String, Object>> artifacts = getArtifacts(timedOut);
			assertEquals(2, artifacts.size());
			assertEquals("screenshot", artifacts.get(0).get("name"));
			assertEquals("image/png", artifacts.get(0).get("contentType"));
			assertEquals(ARTIFACTS + "/" + ARTIFACT_DIR + "/test-failed-1.png",
					artifacts.get(0).get("path"));

			TestCase passed = report.getTestCases().stream()
					.filter(it -> it.getName().equals("serves the OneDev web interface"))
					.findFirst().orElseThrow();
			assertEquals(Status.PASSED, passed.getStatus());
			assertEquals(153, passed.getDuration());

			assertEquals(1, report.getTestCases(
					new PatternSet(Sets.newHashSet("confidential-issue.spec.js"), new HashSet<>()),
					null, null).size());
			assertEquals(1, report.getTestCases(
					null,
					new PatternSet(Sets.newHashSet("Issue Reporter*"), new HashSet<>()),
					null).size());
			assertTrue(report.hasTestCaseDuration());

			File copiedScreenshot = new File(reportDir,
					ARTIFACTS + "/" + ARTIFACT_DIR + "/test-failed-1.png");
			assertTrue(copiedScreenshot.isFile());
			assertArrayEquals(
					Resources.toByteArray(Resources.getResource(
							PlaywrightReportParserTest.class, ARTIFACT_DIR + "/test-failed-1.png")),
					Files.readAllBytes(copiedScreenshot.toPath()));
			report.writeTo(reportDir);
			UnitTestReport persistedReport = UnitTestReport.readFrom(reportDir);
			assertEquals(2, persistedReport.getTestCases().size());
			assertEquals(2, getArtifacts(persistedReport.getTestCases().stream()
					.filter(it -> it.getName().equals(
							"Issue Reporter can create and view a confidential issue"))
					.findFirst().orElseThrow()).size());

			JsonNode unsafeRootNode = rootNode.deepCopy();
			((com.fasterxml.jackson.databind.node.ObjectNode) unsafeRootNode.path("suites").get(0)
					.path("specs").get(0).path("tests").get(0).path("results").get(0)
					.path("attachments").get(0)).put("path", "/outside-workdir/screenshot.png");
			File unsafeReportDir = temp.newFolder("unsafe-report");
			PlaywrightReportParser.parse(build, unsafeRootNode, inputDir, unsafeReportDir, null);
			assertFalse(new File(unsafeReportDir,
					ARTIFACTS + "/" + ARTIFACT_DIR + "/test-failed-1.png").exists());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getArtifacts(TestCase testCase) {
		return (List<Map<String, Object>>) testCase.getDetailData().get("artifacts");
	}

	@Test
	public void shouldOmitPassedAttemptWithoutDetail() {
		assertFalse(PlaywrightReportParser.shouldRenderResults(singlePassedResult()));
		assertTrue(PlaywrightReportParser.shouldRenderResults(failedResultWithErrors()));
		assertTrue(PlaywrightReportParser.shouldRenderResults(retriedResults()));
	}

	@Test
	public void shouldSanitizeOutputForAjaxResponse() {
		assertEquals("", PlaywrightReportParser.sanitizeText(null));
		assertEquals("red\n\t😀", PlaywrightReportParser.sanitizeText(
				"\u001B[31mred\u001B[0m\u0000\n\t😀"));
	}

	private void copyResource(String resourcePath, File inputDir) throws IOException {
		File targetFile = new File(inputDir, resourcePath);
		Files.createDirectories(targetFile.getParentFile().toPath());
		try (InputStream is = Resources.getResource(
				PlaywrightReportParserTest.class, resourcePath).openStream()) {
			Files.copy(is, targetFile.toPath());
		}
	}

	private static List<PlaywrightReportParser.ResultData> singlePassedResult() {
		PlaywrightReportParser.ResultData result = new PlaywrightReportParser.ResultData();
		result.status = "passed";
		return List.of(result);
	}

	private static List<PlaywrightReportParser.ResultData> failedResultWithErrors() {
		PlaywrightReportParser.ResultData result = new PlaywrightReportParser.ResultData();
		result.status = "timedOut";
		result.errors.add("Test timeout of 120000ms exceeded.");
		return List.of(result);
	}

	private static List<PlaywrightReportParser.ResultData> retriedResults() {
		PlaywrightReportParser.ResultData failed = new PlaywrightReportParser.ResultData();
		failed.status = "failed";
		failed.errors.add("expected true to be false");
		PlaywrightReportParser.ResultData passed = new PlaywrightReportParser.ResultData();
		passed.retry = 1;
		passed.status = "passed";
		List<PlaywrightReportParser.ResultData> results = new ArrayList<>();
		results.add(failed);
		results.add(passed);
		return results;
	}

}
