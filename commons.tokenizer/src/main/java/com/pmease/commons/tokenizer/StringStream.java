package com.pmease.commons.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringStream {
	
	private static final Pattern PATTERN_SPACE = Pattern.compile("[\\s\\u00a0]");
	
	private int pos;
	
	private int start;

	private int lineStart;
	
	private String string;

	public StringStream(String string) {
		this.string = string;
		pos = start = lineStart = 0;
	}
	
	public int pos() {
		return pos;
	}

	public void pos(int pos) {
		this.pos = pos;
	}

	public int start() {
		return start;
	}

	public void start(int start) {
		this.start = start;
	}

	public int lineStart() {
		return lineStart;
	}

	public void lineStart(int lineStart) {
		this.lineStart = lineStart;
	}

	public String string() {
		return string;
	}

	public void string(String string) {
		this.string = string;
	}
	
	public boolean eol() {
		return pos >= string.length();
	}
	
	public boolean sol() {
		return pos == 0;
	}

	public String peek() {
		if (pos<string.length())
			return String.valueOf(string.charAt(pos));
		else
			return "";
	}

	public String next() {
		if (pos < string.length())
			return String.valueOf(string.charAt(pos++));
		else
			return "";
	}

	public String eat(String match) {
		String ch = peek();
		if (match.equals(ch)) {
			++pos;
			return ch;
		} else {
			return "";
		}
	}
	
	public String eat(Pattern pattern) {
		String ch = peek();
		if (pattern.matcher(ch).find()) {
			++pos;
			return ch;
		} else {
			return null;
		}
	}
	
	public boolean eatWhile(String match) {
		int start = pos;
		while (eat(match) != null);
		return pos>start;
	}
	
	public boolean eatWhile(Pattern pattern) {
		int start = pos;
		while (eat(pattern) != null);
		return pos>start;
	}
	
	public boolean eatSpace() {
		int start = pos;
		String ch = peek();
		while (PATTERN_SPACE.matcher(ch).find()) {
			++pos;
			ch = peek();
		}
		return pos>start;
	}
	
	public void skipToEnd() {
		pos = string.length();
	}
	
	public boolean skipTo(String ch) {
		int found = string.indexOf(ch, pos);
		if (found > -1) {
			pos = found;
			return true;
		} else {
			return false;
		}
	}

	public void backUp(int n) {
		pos -= n;
	}

	public int column() {
		return start - lineStart;
	}

	public int indentation() {
		return 0;
	}
	
	public boolean match(String match, boolean consume, boolean caseInsensitive) {
		String casedMatch = caseInsensitive?match.toLowerCase():match;
		String substr = string.substring(pos, match.length()+pos);
		String casedSubstr = caseInsensitive?substr.toLowerCase():substr;
		if (casedMatch.equals(casedSubstr)) {
			if (consume)
				pos += match.length();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean match(String match) {
		return match(match, true, false);
	}

	public List<String> match(Pattern pattern, boolean consume) {
		Matcher matcher = pattern.matcher(string.substring(pos));
		if (matcher.find()) {
			if (matcher.start() > 0)
				return new ArrayList<>();
			if (consume) 
				pos += matcher.group().length();

			List<String> matches = new ArrayList<>();
			for (int i=0; i<=matcher.groupCount(); i++)
				matches.add(matcher.group(i));
			return matches;
		} else {
			return new ArrayList<>();
		}
	}
	
	public List<String> match(Pattern pattern) {
		return match(pattern, true);
	}
	
	public String current() {
		return string.substring(start, pos);
	}

	public <T> Object hideFirstChars(int n, Callable<T> inner) {
		lineStart += n;
		try {
			return inner.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			lineStart -= n;
		}
	}

}
