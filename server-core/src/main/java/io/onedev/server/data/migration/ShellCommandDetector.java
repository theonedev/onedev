package io.onedev.server.data.migration;

import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

public class ShellCommandDetector {

	private static final Pattern WINDOWS_BATCH_PATTERN = Pattern.compile(
			"(?im)(^\\s*(?:(?:@@|@)echo(?:\\s|$)|echo\\s+off\\b)"
					+ "|^\\s*(?:call\\s+|goto\\s+|exit\\s+/b\\b|rem(?:\\s|$)|::)"
					+ "|^\\s*if\\s+(?:not\\s+)?exist\\s+"
					+ "|^\\s*for\\s+%%[a-z]\\s+in\\s*\\("
					+ "|^\\s*set\\s+\"?[^\\s=\"]+="
					+ "|%[a-z_][a-z0-9_]*%"
					+ "|\\b(?:cmd(?:\\.exe)?\\s+/c|\\.bat\\b)"
					+ "|\\b[a-z]:\\\\)");

	private ShellCommandDetector() {
	}

	public static boolean isWindowsBatch(@Nullable String commands) {
		return commands != null && WINDOWS_BATCH_PATTERN.matcher(commands).find();
	}

}
