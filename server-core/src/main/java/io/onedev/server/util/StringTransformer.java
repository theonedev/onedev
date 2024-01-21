package io.onedev.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringTransformer {

	private final Pattern pattern;
	
	public StringTransformer(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public final String transform(String string) {
		StringBuffer buffer = new StringBuffer();
		int lastPos = 0;
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			buffer.append(transformUnmatched(string.substring(lastPos, matcher.start())));
			lastPos = matcher.end();
			buffer.append(transformMatched(matcher));
		}
		buffer.append(transformUnmatched(string.substring(lastPos)));
		return buffer.toString();
	}
	
	protected abstract String transformUnmatched(String string);
	
	protected abstract String transformMatched(Matcher matcher);
	
}
