package io.onedev.server.data.migration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ShellCommandDetectorTest {

	@Test
	public void shouldDetectWindowsBatchCommands() {
		assertTrue(ShellCommandDetector.isWindowsBatch("@echo off\r\nset VERSION=1\r\necho %VERSION%"));
		assertTrue(ShellCommandDetector.isWindowsBatch("@@echo off\r\nset VERSION=1"));
		assertTrue(ShellCommandDetector.isWindowsBatch("@@echo hello"));
		assertTrue(ShellCommandDetector.isWindowsBatch("if exist C:\\temp\\result.txt exit /b 1"));
	}

	@Test
	public void shouldDefaultToPosixCommands() {
		assertFalse(ShellCommandDetector.isWindowsBatch("set -e\nVERSION=1\necho \"$VERSION\""));
		assertFalse(ShellCommandDetector.isWindowsBatch("echo hello"));
		assertFalse(ShellCommandDetector.isWindowsBatch("mvn clean test"));
		assertFalse(ShellCommandDetector.isWindowsBatch(null));
	}

}
