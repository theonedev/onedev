package io.onedev.server.plugin.report.roslynator;

import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.GeneralTarget;
import io.onedev.server.model.Build;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PublishRoslynatorReportStepTest {

	@Test
	public void testParseDiagnosticsWithoutFilePath() {
		try (var is = Resources.getResource(PublishRoslynatorReportStepTest.class, "report.xml").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			build.setCommitHash(ObjectId.zeroId().name());

			var problems = PublishRoslynatorReportStep.parse(build, new SAXReader().read(is), new HashMap<String, Optional<String>>(), new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});

			assertEquals(3, problems.size());
			var problem = problems.get(0);
			assertEquals(CodeProblem.Severity.HIGH, problem.getSeverity());
			assertEquals("supermario", problem.getTarget().getGroupKey().getName());
			assertEquals(GeneralTarget.class, problem.getTarget().getClass());

			problem = problems.get(1);
			assertEquals(CodeProblem.Severity.LOW, problem.getSeverity());
			assertEquals("/onedev-build/work/supermario/Dialogue.cs", problem.getTarget().getGroupKey().getName());
			assertEquals("11.17-11.18-1", ((BlobTarget) problem.getTarget()).getLocation().toString());

			problem = problems.get(2);
			assertEquals(CodeProblem.Severity.MEDIUM, problem.getSeverity());
			assertEquals("/onedev-build/work/supermario/Program.cs", problem.getTarget().getGroupKey().getName());
			assertNull(((BlobTarget) problem.getTarget()).getLocation());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
