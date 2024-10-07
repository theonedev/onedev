package io.onedev.server.plugin.report.mypy;

import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.model.Build;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class MypyReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(MypyReportParserTest.class, "mypy-output").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			build.setCommitHash(ObjectId.zeroId().name());
			var output = IOUtils.readLines(is, StandardCharsets.UTF_8);
			var problems = MypyReportParser.parse(build, output, new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			
			assertEquals(10, problems.size());
			
			var problem = problems.get(0);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			assertEquals("6.1-6.2-1", ((BlobTarget)problem.getTarget()).getLocation().toString());
			assertEquals("torchtune/utils/_version.py", problem.getTarget().getGroupKey().getName());
			assertEquals(escapeHtml5("Cannot find implementation or library stub for module named \"torch\"  [import-not-found]"), problem.getMessage());

			problem = problems.get(1);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			assertEquals("10.1-10.2-1", ((BlobTarget)problem.getTarget()).getLocation().toString());
			assertEquals("torchtune/utils:/_device.py", problem.getTarget().getGroupKey().getName());
			assertEquals(escapeHtml5("Cannot find implementation or library stub for module named \"torch\"  [import-not-found]"), problem.getMessage());

			problem = problems.get(2);
			assertEquals(CodeProblem.Severity.LOW, problem.getSeverity());
			assertEquals(escapeHtml5("PEP 484 prohibits implicit Optional. Accordingly, mypy has changed its default to no_implicit_optional=True"), problem.getMessage());

			problem = problems.get(4);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			assertEquals("88.5-135.22-1", ((BlobTarget)problem.getTarget()).getLocation().toString());
			assertEquals(
					escapeHtml5("Signature of \"encode\" incompatible with supertype \"BaseTokenizer\"  [override]\n" +
					"    Superclass:\n" +
					"        def encode(self, text: str, **kwargs: dict[str, Any]) -> list[int]\n" +
					"    Subclass:\n" +
					"        def encode(self, text: str, add_bos: bool = ..., add_eos: bool = ...) -> list[int]"), 
					problem.getMessage());
			
			assertEquals(CodeProblem.Severity.MEDIUM, problems.get(5).getSeverity());
			assertEquals(CodeProblem.Severity.LOW, problems.get(6).getSeverity());
			
			problem = problems.get(8);
			assertEquals("174.21-174.0-1", ((BlobTarget)problem.getTarget()).getLocation().toString());
			
			problem = problems.get(9);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			assertEquals("109.1-109.0-1", ((BlobTarget)problem.getTarget()).getLocation().toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}