package io.onedev.server.plugin.report.clippy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;
import io.onedev.server.util.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ClippyReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(ClippyReportParserTest.class, "check-result.json").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			
			build.setCommitHash(ObjectId.zeroId().name());

			var mapper = new ObjectMapper();
			var problemNodes = new ArrayList<JsonNode>();
			for (var line: IOUtils.readLines(is, StandardCharsets.UTF_8))
				problemNodes.add(mapper.readTree(line));
			var problems = ClippyReportParser.parse(build, problemNodes, new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			assertEquals(5, problems.size());
			assertEquals(CodeProblem.Severity.MEDIUM, problems.get(0).getSeverity());
			assertEquals("29.23-29.54-1", ((BlobTarget)problems.get(0).getTarget()).getLocation().toString());
			assertEquals(CodeProblem.Severity.MEDIUM, problems.get(3).getSeverity());
			assertEquals("78.5-78.6-1", ((BlobTarget)problems.get(3).getTarget()).getLocation().toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}