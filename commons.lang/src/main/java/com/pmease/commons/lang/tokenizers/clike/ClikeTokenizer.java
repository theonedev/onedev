package com.pmease.commons.lang.tokenizers.clike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import com.pmease.commons.lang.AbstractTokenizer;
import com.pmease.commons.lang.StringStream;

public abstract class ClikeTokenizer extends
		AbstractTokenizer<ClikeTokenizer.State> {

	private static Pattern IS_OPERATOR_CHAR = Pattern
			.compile("[+\\-*&%=<>!?|\\/]");

	static Set<String> C_KEYWORDS = Sets.newHashSet("auto", "if",
			"break", "int", "case", "long", "char", "register", "continue",
			"return", "default", "short", "do", "sizeof", "double", "static",
			"else", "struct", "entry", "switch", "extern", "typedef", "float",
			"union", "for", "unsigned", "goto", "while", "enum", "void",
			"const", "signed", "volatile");

	static class State {
		Processor tokenize;
		Context context;
		int indented;
		boolean startOfLine;
		String curPunc;
		String cpp11RawStringDelim;

		State(Processor tokenize, Context context, int indented,
				boolean startOfLine) {
			this.tokenize = tokenize;
			this.context = context;
			this.indented = indented;
			this.startOfLine = startOfLine;
		}
	}

	static class Context {
		int indented;
		int column;
		String type;
		Boolean align;
		Context prev;

		Context(int indented, int column, String type, Boolean align,
				Context prev) {
			this.indented = indented;
			this.column = column;
			this.type = type;
			this.align = align;
			this.prev = prev;
		}

	}

	private Context pushContext(State state, int col, String type) {
		int indent = state.indented;
		if (state.context != null && state.context.type.equals("statement"))
			indent = state.context.indented;
		return state.context = new Context(indent, col, type, null,
				state.context);
	}

	private Context popContext(State state) {
		String t = state.context.type;
		if (t.equals(")") || t.equals("]") || t.equals("}"))
			state.indented = state.context.indented;
		return state.context = state.context.prev;
	}

	static interface Processor {
		String process(StringStream stream, State state);
	}

	protected int statementIndentUnit() {
		return indentUnit();
	}

	protected Set<String> keywords() {
		return new HashSet<>();
	}

	protected Set<String> builtin() {
		return new HashSet<>();
	}

	protected Set<String> blockKeywords() {
		return new HashSet<>();
	}

	protected Set<String> atoms() {
		return new HashSet<>();
	}

	protected Map<String, Processor> hooks() {
		return new HashMap<>();
	}

	protected boolean multiLineStrings() {
		return false;
	}

	protected boolean indentStatements() {
		return true;
	}

	@Override
	protected State startState() {
		return new State(null,
				new Context(-indentUnit(), 0, "top", false, null), 0, true);
	}

	@Override
	protected String token(StringStream stream, State state) {
		Context ctx = state.context;
		if (stream.sol()) {
			if (ctx.align == null)
				ctx.align = false;
			state.indented = stream.indentation();
			state.startOfLine = true;
		}
		if (stream.eatSpace())
			return "";

		state.curPunc = "";

		String style;
		if (state.tokenize != null)
			style = state.tokenize.process(stream, state);
		else
			style = tokenBase(stream, state);

		if (style.equals("comment") || style.equals("meta"))
			return style;

		if (ctx.align == null)
			ctx.align = true;

		if ((state.curPunc.equals(";") || state.curPunc.equals(":") || state.curPunc
				.equals(",")) && ctx.type.equals("statement"))
			popContext(state);
		else if (state.curPunc.equals("{"))
			pushContext(state, stream.column(), "}");
		else if (state.curPunc.equals("["))
			pushContext(state, stream.column(), "]");
		else if (state.curPunc.equals("("))
			pushContext(state, stream.column(), ")");
		else if (state.curPunc.equals("}")) {
			while (ctx.type.equals("statement"))
				ctx = popContext(state);
			if (ctx.type.equals("}"))
				ctx = popContext(state);
			while (ctx.type.equals("statement"))
				ctx = popContext(state);
		} else if (state.curPunc.equals(ctx.type))
			popContext(state);
		else if (indentStatements()
				&& (((ctx.type.equals("}") || ctx.type.equals("top")) && !state.curPunc
						.equals(';')) || (ctx.type.equals("statement") && state.curPunc
						.equals("newstatement"))))
			pushContext(state, stream.column(), "statement");
		state.startOfLine = false;
		return style;
	}

	private static Pattern TOKEN_BASE_PATTERN1 = Pattern
			.compile("[\\[\\]{}\\(\\),;\\:\\.]");

	private static Pattern TOKEN_BASE_PATTERN2 = Pattern.compile("\\d");

	private static Pattern TOKEN_BASE_PATTERN3 = Pattern.compile("[\\w\\.]");

	private static Pattern TOKEN_BASE_PATTERN4 = Pattern
			.compile("[\\w\\$_\\xa1-\\uffff]");

	private String tokenBase(StringStream stream, State state) {
		String ch = stream.next();
		if (hooks().get(ch) != null) {
			String result = hooks().get(ch).process(stream, state);
			if (result.length() != 0)
				return result;
		}

		if (ch.equals("\"") || ch.equals(("'"))) {
			state.tokenize = new TokenString(ch);
			return state.tokenize.process(stream, state);
		}
		if (TOKEN_BASE_PATTERN1.matcher(ch).matches()) {
			state.curPunc = ch;
			return "";
		}
		if (TOKEN_BASE_PATTERN2.matcher(ch).matches()) {
			stream.eatWhile(TOKEN_BASE_PATTERN3);
			return "number";
		}
		if (ch.equals("/")) {
			if (stream.eat("*").length() != 0) {
				state.tokenize = new TokenComment();
				return state.tokenize.process(stream, state);
			}
			if (stream.eat("/").length() != 0) {
				stream.skipToEnd();
				return "comment";
			}
		}
		if (IS_OPERATOR_CHAR.matcher(ch).matches()) {
			stream.eatWhile(IS_OPERATOR_CHAR);
			return "operator";
		}
		stream.eatWhile(TOKEN_BASE_PATTERN4);
		String cur = stream.current();
		if (keywords().contains(cur)) {
			if (blockKeywords().contains(cur))
				state.curPunc = "newstatement";
			return "keyword";
		}
		if (builtin().contains(cur)) {
			if (blockKeywords().contains(cur))
				state.curPunc = "newstatement";
			return "builtin";
		}
		if (atoms().contains(cur))
			return "atom";

		return "variable";
	}

	class TokenComment implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			boolean maybeEnd = false;
			String ch;
			while ((ch = stream.next()).length() != 0) {
				if (ch.equals("/") && maybeEnd) {
					state.tokenize = null;
					break;
				}
				maybeEnd = (ch.equals("*"));
			}
			return "comment";
		}

	}

	class TokenString implements Processor {

		private String quote;

		TokenString(String quote) {
			this.quote = quote;
		}

		@Override
		public String process(StringStream stream, State state) {
			boolean escaped = false;
			String next;
			boolean end = false;
			while ((next = stream.next()).length() != 0) {
				if (next.equals(quote) && !escaped) {
					end = true;
					break;
				}
				escaped = !escaped && next.equals("\\");
			}
			if (end || !(escaped || multiLineStrings()))
				state.tokenize = null;
			return "string";
		}

	}

	static class TokenRawString implements Processor {

		private static Pattern PATTERN1 = Pattern.compile("[^\\w\\s]");

		@Override
		public String process(StringStream stream, State state) {
			// Escape characters that have special regex meanings.
			String delim = PATTERN1.matcher(state.cpp11RawStringDelim)
					.replaceAll("\\\\$0");
			if (!stream.match(Pattern.compile(".*?\\)" + delim + "\""))
					.isEmpty())
				state.tokenize = null;
			else
				stream.skipToEnd();
			return "string";
		}

	}

	static class TokenAtString implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			String next;
			while ((next = stream.next()).length() != 0) {
				if (next.equals("\"") && stream.eat("\"").length() == 0) {
					state.tokenize = null;
					break;
				}
			}
			return "string";
		}

	}

	static class CppHook implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			if (!state.startOfLine)
				return "";
			for (;;) {
				if (stream.skipTo("\\")) {
					stream.next();
					if (stream.eol()) {
						state.tokenize = new CppHook();
						break;
					}
				} else {
					stream.skipToEnd();
					state.tokenize = null;
					break;
				}
			}
			return "meta";
		}

	}

}
