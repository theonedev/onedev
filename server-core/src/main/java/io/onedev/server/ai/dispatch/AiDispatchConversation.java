package io.onedev.server.ai.dispatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class AiDispatchConversation {

	private static final String START_PREFIX = "\n[[[ONDEV_AI_MESSAGE:";

	private static final String START_SUFFIX = "]]]\n";

	private static final String END_MARKER = "\n[[[ONDEV_AI_MESSAGE_END]]]\n";

	private static final String EVENT_PREFIX = "[[[ONDEV_AI_EVENT:";

	private static final String EVENT_SUFFIX = "]]]\n";

	public static String newMessageBlock(Role role, String content) {
		return START_PREFIX + role.name() + START_SUFFIX + StringUtils.defaultString(content) + END_MARKER;
	}

	public static String newTypedMessageBlock(Role role, EventType eventType, @Nullable String tag, String content) {
		var header = new StringBuilder(EVENT_PREFIX).append(eventType.name());
		if (StringUtils.isNotBlank(tag))
			header.append(":").append(tag);
		header.append(EVENT_SUFFIX);
		return newMessageBlock(role, header + StringUtils.defaultString(content));
	}

	public static String newProgressBlock(String content) {
		return newTypedMessageBlock(Role.SYSTEM, EventType.PROGRESS, null, content);
	}

	public static String newThinkingBlock(String content) {
		return newTypedMessageBlock(Role.ASSISTANT, EventType.THINKING, null, content);
	}

	public static String newErrorBlock(@Nullable String code, String content) {
		return newTypedMessageBlock(Role.SYSTEM, EventType.ERROR, code, content);
	}

	public static String startAssistantBlock() {
		return START_PREFIX + Role.ASSISTANT.name() + START_SUFFIX;
	}

	public static String endMessageBlock() {
		return END_MARKER;
	}

	public static boolean containsMarkup(@Nullable String text) {
		return text != null && text.contains(START_PREFIX);
	}

	public static boolean isSystemOutput(@Nullable String text) {
		var trimmed = StringUtils.stripStart(text, null);
		return trimmed != null && trimmed.startsWith("[system] ");
	}

	public static List<Message> parseMessages(String log) {
		List<Message> messages = new ArrayList<>();
		if (log == null)
			return messages;
		int index = 0;
		while (true) {
			int start = log.indexOf(START_PREFIX, index);
			if (start == -1)
				break;
			int markerEnd = log.indexOf(START_SUFFIX, start);
			if (markerEnd == -1)
				break;
			var roleValue = log.substring(start + START_PREFIX.length(), markerEnd);
			Role role = Role.valueOf(roleValue);
			int contentStart = markerEnd + START_SUFFIX.length();
			int contentEnd = log.indexOf(END_MARKER, contentStart);
			if (contentEnd == -1) {
				messages.add(parseMessage(role, log.substring(contentStart)));
				break;
			} else {
				messages.add(parseMessage(role, log.substring(contentStart, contentEnd)));
				index = contentEnd + END_MARKER.length();
			}
		}
		return messages;
	}

	public static List<Message> parseFeed(String log) {
		List<Message> messages = new ArrayList<>();
		if (log == null)
			return messages;
		int index = 0;
		while (true) {
			int start = log.indexOf(START_PREFIX, index);
			if (start == -1) {
				appendPlainText(messages, log.substring(index));
				break;
			}
			appendPlainText(messages, log.substring(index, start));
			int markerEnd = log.indexOf(START_SUFFIX, start);
			if (markerEnd == -1) {
				appendPlainText(messages, log.substring(start));
				break;
			}
			var roleValue = log.substring(start + START_PREFIX.length(), markerEnd);
			Role role = Role.valueOf(roleValue);
			int contentStart = markerEnd + START_SUFFIX.length();
			int contentEnd = log.indexOf(END_MARKER, contentStart);
			if (contentEnd == -1) {
				messages.add(parseMessage(role, log.substring(contentStart)));
				break;
			} else {
				messages.add(parseMessage(role, log.substring(contentStart, contentEnd)));
				index = contentEnd + END_MARKER.length();
			}
		}
		return dedupeAdjacent(messages);
	}

	public static String stripMarkup(String log) {
		if (log == null)
			return null;
		StringBuilder visible = new StringBuilder();
		int index = 0;
		while (true) {
			int start = log.indexOf(START_PREFIX, index);
			if (start == -1) {
				visible.append(log.substring(index));
				break;
			}
			visible.append(log, index, start);
			int markerEnd = log.indexOf(START_SUFFIX, start);
			if (markerEnd == -1)
				break;
			var roleValue = log.substring(start + START_PREFIX.length(), markerEnd);
			Role role = Role.valueOf(roleValue);
			int contentStart = markerEnd + START_SUFFIX.length();
			int contentEnd = log.indexOf(END_MARKER, contentStart);
			if (contentEnd == -1) {
				visible.append(asTerminalText(parseMessage(role, log.substring(contentStart))));
				break;
			}
			visible.append(asTerminalText(parseMessage(role, log.substring(contentStart, contentEnd))));
			index = contentEnd + END_MARKER.length();
		}
		return visible.toString();
	}

	/**
	 * Build a markdown summary of the conversation suitable for posting as a PR comment.
	 * Includes assistant responses (truncated) and the final status.
	 */
	public static String buildSummary(List<Message> messages, int maxLength) {
		var sb = new StringBuilder();
		for (var msg : messages) {
			if (!msg.isTranscriptMessage())
				continue;
			if (msg.isUser()) {
				sb.append("**Prompt:** ").append(StringUtils.abbreviate(msg.getContent().strip(), 500)).append("\n\n");
			} else if (msg.isAssistant()) {
				sb.append(msg.getContent().strip()).append("\n\n");
			}
		}
		var result = sb.toString().strip();
		if (result.length() > maxLength)
			result = result.substring(0, maxLength) + "\n\n…(truncated)";
		return result;
	}

	private static Message parseMessage(Role role, String content) {
		var eventInfo = parseEventInfo(content);
		var actualContent = eventInfo.content();
		var eventType = eventInfo.eventType();
		var tag = eventInfo.tag();
		if (eventType == EventType.MESSAGE) {
			if (role == Role.SYSTEM) {
				var systemMessage = classifySystemContent(actualContent);
				eventType = systemMessage.getEventType();
				tag = systemMessage.getTag();
				actualContent = systemMessage.getContent();
			} else if (role == Role.USER) {
				tag = detectSlashCommand(actualContent);
				if (tag != null)
					eventType = EventType.COMMAND;
			}
		}
		return new Message(role, actualContent, eventType, tag);
	}

	private static EventInfo parseEventInfo(String content) {
		if (content.startsWith(EVENT_PREFIX)) {
			int markerEnd = content.indexOf(EVENT_SUFFIX);
			if (markerEnd != -1) {
				var header = content.substring(EVENT_PREFIX.length(), markerEnd);
				var actualContent = content.substring(markerEnd + EVENT_SUFFIX.length());
				var typeName = StringUtils.substringBefore(header, ":");
				var tag = StringUtils.trimToNull(StringUtils.substringAfter(header, ":"));
				try {
					return new EventInfo(EventType.valueOf(typeName), tag, actualContent);
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
		return new EventInfo(EventType.MESSAGE, null, content);
	}

	private static void appendPlainText(List<Message> messages, String plainText) {
		if (StringUtils.isBlank(plainText))
			return;
		var outputBuffer = new StringBuilder();
		for (var line : plainText.split("\\r?\\n", -1)) {
			if (line.isBlank()) {
				flushOutput(messages, outputBuffer);
				continue;
			}
			if (line.startsWith("> "))
				continue;
			if (line.startsWith("[system] ")) {
				flushOutput(messages, outputBuffer);
				messages.add(classifySystemContent(line.substring("[system] ".length()).trim()));
			} else {
				if (outputBuffer.length() != 0)
					outputBuffer.append('\n');
				outputBuffer.append(line);
			}
		}
		flushOutput(messages, outputBuffer);
	}

	private static void flushOutput(List<Message> messages, StringBuilder outputBuffer) {
		if (outputBuffer.length() == 0)
			return;
		messages.add(new Message(Role.SYSTEM, outputBuffer.toString(), EventType.OUTPUT, "terminal"));
		outputBuffer.setLength(0);
	}

	private static Message classifySystemContent(String content) {
		var normalized = StringUtils.defaultString(content).trim();
		if (normalized.isEmpty())
			return new Message(Role.SYSTEM, normalized, EventType.PROGRESS, null);
		if (normalized.startsWith("Model invocation failed:")) {
			var details = StringUtils.trimToEmpty(StringUtils.substringAfter(normalized, "Model invocation failed:"));
			return new Message(Role.SYSTEM, details, EventType.ERROR, classifyErrorTag(details));
		}
		if (looksLikeError(normalized))
			return new Message(Role.SYSTEM, normalized, EventType.ERROR, classifyErrorTag(normalized));
		var tag = normalized.startsWith("Response complete.") ? "awaiting-input" : null;
		return new Message(Role.SYSTEM, normalized, EventType.PROGRESS, tag);
	}

	private static boolean looksLikeError(String content) {
		var lowerCase = content.toLowerCase();
		return lowerCase.startsWith("failed")
				|| lowerCase.contains("timed out")
				|| lowerCase.contains("not reachable")
				|| lowerCase.contains("unauthorized")
				|| lowerCase.contains("forbidden")
				|| lowerCase.contains("maximum concurrent")
				|| lowerCase.contains("session closed")
				|| lowerCase.contains("session cancelled")
				|| lowerCase.contains("dispatch is disabled");
	}

	@Nullable
	private static String classifyErrorTag(String content) {
		var lowerCase = StringUtils.defaultString(content).toLowerCase();
		if (lowerCase.contains("unauthorized") || lowerCase.contains("api key") || lowerCase.contains("token"))
			return "AUTH_FAILED";
		if (lowerCase.contains("rate limit") || lowerCase.contains("quota"))
			return "RATE_LIMITED";
		if (lowerCase.contains("timed out") || lowerCase.contains("timeout"))
			return "TIMEOUT";
		if (lowerCase.contains("not reachable") || lowerCase.contains("connection"))
			return "NETWORK_ERROR";
		if (lowerCase.contains("docker"))
			return "BACKEND_START_FAILED";
		return "SESSION_ERROR";
	}

	@Nullable
	private static String detectSlashCommand(String content) {
		var normalized = StringUtils.trimToEmpty(content);
		if (!normalized.startsWith("/") || normalized.length() == 1)
			return null;
		var commandToken = StringUtils.substringBefore(normalized.substring(1), " ");
		return StringUtils.isNotBlank(commandToken) ? commandToken : null;
	}

	private static List<Message> dedupeAdjacent(List<Message> messages) {
		List<Message> deduped = new ArrayList<>();
		for (var message : messages) {
			if (!deduped.isEmpty() && deduped.get(deduped.size() - 1).hasSamePresentation(message))
				continue;
			deduped.add(message);
		}
		return deduped;
	}

	private static String asTerminalText(Message message) {
		return switch (message.getEventType()) {
		case MESSAGE -> {
			if (message.isAssistant())
				yield message.getContent();
			yield "";
		}
		case PROGRESS -> "[system] " + message.getContent() + "\n";
		case THINKING -> "[thinking] " + message.getContent() + "\n";
		case ERROR -> "[error" + (message.getTag() != null ? ":" + message.getTag() : "") + "] "
				+ message.getContent() + "\n";
		case OUTPUT -> message.getContent() + "\n";
		case COMMAND -> "";
		};
	}

	public enum Role {
		USER,
		ASSISTANT,
		SYSTEM
	}

	public enum EventType {
		MESSAGE,
		PROGRESS,
		THINKING,
		ERROR,
		OUTPUT,
		COMMAND
	}

	public static class Message {

		private final Role role;

		private final String content;

		private final EventType eventType;

		private final String tag;

		public Message(Role role, String content) {
			this(role, content, EventType.MESSAGE, null);
		}

		public Message(Role role, String content, EventType eventType, @Nullable String tag) {
			this.role = role;
			this.content = content;
			this.eventType = eventType;
			this.tag = tag;
		}

		public Role getRole() {
			return role;
		}

		public String getContent() {
			return content;
		}

		public EventType getEventType() {
			return eventType;
		}

		@Nullable
		public String getTag() {
			return tag;
		}

		public boolean isUser() {
			return role == Role.USER;
		}

		public boolean isAssistant() {
			return role == Role.ASSISTANT;
		}

		public boolean isSystem() {
			return role == Role.SYSTEM;
		}

		public boolean isThinking() {
			return eventType == EventType.THINKING;
		}

		public boolean isCommand() {
			return eventType == EventType.COMMAND;
		}

		public boolean isTranscriptMessage() {
			if (isSystem())
				return false;
			if (isAssistant())
				return eventType == EventType.MESSAGE;
			return eventType == EventType.MESSAGE || eventType == EventType.COMMAND;
		}

		private boolean hasSamePresentation(Message other) {
			return role == other.role
					&& eventType == other.eventType
					&& StringUtils.equals(tag, other.tag)
					&& StringUtils.equals(content, other.content);
		}

	}

	private record EventInfo(EventType eventType, @Nullable String tag, String content) {
	}

}
