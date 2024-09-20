package io.onedev.server.plugin.report.pylint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.model.Build;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class PylintReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(PylintReportParserTest.class, "pylint-result.json").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			build.setCommitHash(ObjectId.zeroId().name());
			ObjectMapper mapper = new ObjectMapper();
			var problems = PylintReportParser.parse(build, mapper.readTree(is), new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			var problem = problems.get(0);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			var target = (BlobTarget) problem.getTarget();
			assertEquals(31, target.getLocation().getFromRow());
			assertEquals(8, target.getLocation().getFromColumn());
			assertEquals(31, target.getLocation().getToRow());
			assertEquals(11, target.getLocation().getToColumn());
			
			problem = problems.get(problems.size()-1);
			assertEquals(CodeProblem.Severity.LOW, problem.getSeverity());
			target = (BlobTarget) problem.getTarget();
			assertEquals(0, target.getLocation().getFromRow());
			assertEquals(0, target.getLocation().getFromColumn());
			assertEquals(0, target.getLocation().getToRow());
			assertEquals(-1, target.getLocation().getToColumn());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}