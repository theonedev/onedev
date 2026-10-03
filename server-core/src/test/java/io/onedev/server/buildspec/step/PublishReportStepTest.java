package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;

class PublishReportStepTest {

	@TempDir
	Path temp;

	private final PublishReportStep step = new PublishReportStep() {
		@Override
		public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
			throw new UnsupportedOperationException();
		}
	};

	@Test
	void rejectsParentSegmentsAndAbsolutePaths() {
		for (var path: List.of("..", "../outside.md", "docs/../report.md", "docs/..",
				"..\\outside.md", "docs\\..\\report.md", "docs/..\\report.md",
				"/outside.md", "C:\\outside.md", "C:outside.md", "\\\\host\\share\\report.md")) {
			assertThrows(ExplicitException.class, () -> step.getReportFile(temp.toFile(), path), path);
		}
	}

	@Test
	void allowsRegularRelativePathsAndDotsWithinNames() throws Exception {
		var file = Files.createDirectories(temp.resolve("docs")).resolve("release..notes.md");
		Files.writeString(file, "report");
		assertEquals(file.toFile(), step.getReportFile(temp.toFile(), "docs/release..notes.md"));
		assertEquals(temp.resolve("missing.md").toFile(), step.getReportFile(temp.toFile(), "missing.md"));
	}

	@Test
	void rejectsFileSymlinksIncludingDanglingLinks() throws Exception {
		assumeTrue(Files.getFileStore(temp).supportsFileAttributeView("posix"));
		var target = Files.writeString(temp.resolve("target.md"), "report");
		Files.createSymbolicLink(temp.resolve("link.md"), target);
		assertThrows(ExplicitException.class, () -> step.getReportFile(temp.toFile(), "link.md"));
		Files.delete(target);
		assertThrows(ExplicitException.class, () -> step.getReportFile(temp.toFile(), "link.md"));
	}

	@Test
	void rejectsSymlinkedParentDirectories() throws Exception {
		assumeTrue(Files.getFileStore(temp).supportsFileAttributeView("posix"));
		var input = Files.createDirectory(temp.resolve("input"));
		var target = Files.createDirectory(temp.resolve("target"));
		Files.writeString(target.resolve("report.md"), "outside input directory");
		Files.createSymbolicLink(input.resolve("link"), target);
		for (var path: List.of("link/report.md", "link/./report.md", "link/missing/report.md"))
			assertThrows(ExplicitException.class, () -> step.getReportFile(input.toFile(), path), path);
		Files.createSymbolicLink(input.resolve("dangling"), temp.resolve("missing"));
		assertThrows(ExplicitException.class, () -> step.getReportFile(input.toFile(), "dangling/report.md"));
	}
}
