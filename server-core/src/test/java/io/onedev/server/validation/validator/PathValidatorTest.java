package io.onedev.server.validation.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.onedev.server.annotation.Path;

public class PathValidatorTest {

	@Test
	public void allowsDotsWithinPathSegments() {
		for (String path : new String[] {"release..notes", "docs/release..notes", "..notes", "notes..", "..."}) {
			assertNull(path, PathValidator.checkPath(Path.Type.RELATIVE, path));
			assertNull(path, PathValidator.checkPath(Path.Type.ABSOLUTE, "/" + path));
		}
	}

	@Test
	public void rejectsParentDirectorySegments() {
		for (String path : new String[] {"..", "../notes", "docs/../notes", "docs/..", "..\\notes",
				"docs\\..\\notes", "docs\\..", "docs/..\\notes", "docs\\../notes"}) {
			assertEquals(path, "'..' is not allowed", PathValidator.checkPath(Path.Type.RELATIVE, path));
			assertEquals(path, "'..' is not allowed", PathValidator.checkPath(Path.Type.ABSOLUTE, "/" + path));
		}
	}

	@Test
	public void preservesPathTypeValidation() {
		assertEquals("Relative path is required", PathValidator.checkPath(Path.Type.RELATIVE, "/release..notes"));
		assertEquals("Relative path is required", PathValidator.checkPath(Path.Type.RELATIVE, "C:\\release..notes"));
		assertEquals("Absolute path is required", PathValidator.checkPath(Path.Type.ABSOLUTE, "release..notes"));
		assertNull(PathValidator.checkPath(Path.Type.ABSOLUTE, "C:\\release..notes"));
	}
}
