package io.onedev.server.web.util;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.jspecify.annotations.Nullable;

/** Default messages shared by repository file and wiki page editing. */
public class DefaultCommitMessage {

	public enum Operation { ADD, EDIT, RENAME, DELETE }

	public static String generate(Operation operation, @Nullable String name,
			String unnamedAddMessage, boolean conventional) {
		String message;
		switch (operation) {
		case ADD:
			message = name != null ? MessageFormat.format(_T("Add {0}"), name) : unnamedAddMessage;
			break;
		case EDIT:
			message = MessageFormat.format(_T("Edit {0}"), name);
			break;
		case RENAME:
			message = MessageFormat.format(_T("Rename {0}"), name);
			break;
		case DELETE:
			message = "Delete " + name;
			break;
		default:
			throw new IllegalArgumentException("Unsupported operation: " + operation);
		}
		return conventional ? "chore: " + message : message;
	}
}
