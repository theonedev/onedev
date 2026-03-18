package io.onedev.server.ai.dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
public class AiDispatchCommentParser {

	private static final Pattern DISPATCH_PATTERN = Pattern.compile("(?im)^\\s*@(?<agent>"
			+ Arrays.stream(AiDispatchAgent.values())
			.map(AiDispatchAgent::getMentionName)
			.collect(Collectors.joining("|"))
			+ ")\\b");

	public Optional<AiDispatchCommand> parse(String comment) {
		Matcher matcher = DISPATCH_PATTERN.matcher(comment);
		if (!matcher.find())
			return Optional.empty();

		var agent = parseAgent(matcher.group("agent"));
		int lineEnd = comment.indexOf('\n', matcher.end());
		String firstLineRemainder;
		String trailingLines;
		if (lineEnd != -1) {
			firstLineRemainder = comment.substring(matcher.end(), lineEnd);
			trailingLines = comment.substring(lineEnd + 1).strip();
		} else {
			firstLineRemainder = comment.substring(matcher.end());
			trailingLines = "";
		}

		var parsed = parsePrompt(firstLineRemainder);
		String prompt = parsed.prompt();
		if (!trailingLines.isEmpty()) {
			if (!prompt.isEmpty())
				prompt += "\n";
			prompt += trailingLines;
		}
		prompt = prompt.strip();
		if (prompt.isEmpty())
			return Optional.empty();

		return Optional.of(new AiDispatchCommand(agent, parsed.flags(), prompt, parsed.modelName()));
	}

	private AiDispatchAgent parseAgent(String agent) {
		var parsed = AiDispatchAgent.fromMentionName(agent);
		if (parsed == null)
			throw new IllegalStateException("Unsupported AI dispatch agent: " + agent);
		return parsed;
	}

	private ParsedPrompt parsePrompt(String remainder) {
		String normalized = remainder.strip();
		var flags = new ArrayList<String>();
		String modelName = null;
		while (normalized.startsWith("--")) {
			if (normalized.startsWith("--model=")) {
				int spaceIndex = normalized.indexOf(' ');
				if (spaceIndex == -1)
					return new ParsedPrompt(flags, normalized.substring("--model=".length()).strip(), "");
				modelName = normalized.substring("--model=".length(), spaceIndex).strip();
				normalized = normalized.substring(spaceIndex + 1).stripLeading();
				continue;
			} else if (normalized.equals("--model")) {
				return new ParsedPrompt(flags, null, "");
			} else if (normalized.startsWith("--model ")) {
				normalized = normalized.substring("--model".length()).stripLeading();
				int spaceIndex = normalized.indexOf(' ');
				if (spaceIndex == -1)
					return new ParsedPrompt(flags, normalized.strip(), "");
				modelName = normalized.substring(0, spaceIndex).strip();
				normalized = normalized.substring(spaceIndex + 1).stripLeading();
				continue;
			}
			int spaceIndex = normalized.indexOf(' ');
			if (spaceIndex == -1) {
				flags.add(normalized.toLowerCase(Locale.ROOT));
				return new ParsedPrompt(flags, modelName, "");
			}
			flags.add(normalized.substring(0, spaceIndex).toLowerCase(Locale.ROOT));
			normalized = normalized.substring(spaceIndex + 1).stripLeading();
		}
		return new ParsedPrompt(flags, modelName, normalized);
	}

	private record ParsedPrompt(List<String> flags, String modelName, String prompt) {
	}

}
