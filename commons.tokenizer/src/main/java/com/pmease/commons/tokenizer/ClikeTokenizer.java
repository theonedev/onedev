package com.pmease.commons.tokenizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class ClikeTokenizer extends
		AbstractTokenizer<ClikeTokenizer.State> {

	private static Pattern IS_OPERATOR_CHAR = Pattern
			.compile("[+\\-*&%=<>!?|\\/]");

	private static Set<String> C_KEYWORDS = Sets.newHashSet("auto", "if",
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

	static class Cpp11StringHook implements Processor {

		private static Pattern PATTERN1 = Pattern.compile("(R|u8R|uR|UR|LR)");

		private static Pattern PATTERN2 = Pattern
				.compile("\"([^\\s\\\\()]{0,16})\\(");

		private static Pattern PATTERN3 = Pattern.compile("(u8|u|U|L)");

		private static Pattern PATTERN4 = Pattern.compile("[\"']");

		@Override
		public String process(StringStream stream, State state) {
			stream.backUp(1);
			// Raw strings.
			if (!stream.match(PATTERN1).isEmpty()) {
				List<String> match = stream.match(PATTERN2);
				if (match.isEmpty()) {
					return "";
				}
				state.cpp11RawStringDelim = match.get(1);
				state.tokenize = new TokenRawString();
				return state.tokenize.process(stream, state);
			}
			// Unicode strings/chars.
			if (!stream.match(PATTERN3).isEmpty()) {
				if (!stream.match(PATTERN4, /* eat */false).isEmpty()) {
					return "string";
				}
				return "";
			}
			// Ignore this hook.
			stream.next();
			return "";
		}

	}

	public static class C extends ClikeTokenizer {

		private static Set<String> BLOCK_KEYWORDS = Sets.newHashSet("case",
				"do", "else", "for", "if", "switch", "while", "struct");

		private static Set<String> ATOMS = Sets.newHashSet("null");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		static {
			HOOKS.put("#", new CppHook());
		}

		@Override
		protected Set<String> keywords() {
			return C_KEYWORDS;
		}

		@Override
		protected Set<String> blockKeywords() {
			return BLOCK_KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "c", "h");
		}

	}

	public static class Cpp extends ClikeTokenizer {

		private static Set<String> KEYWORDS = Sets.newHashSet("asm",
				"dynamic_cast", "namespace", "reinterpret_cast", "try", "bool",
				"explicit", "new", "static_cast", "typeid", "catch",
				"operator", "template", "typename", "class", "friend",
				"private", "this", "using", "const_cast", "inline", "public",
				"throw", "virtual", "delete", "mutable", "protected",
				"wchar_t", "alignas", "alignof", "constexpr", "decltype",
				"nullptr", "noexcept", "thread_local", "final",
				"static_assert", "override");

		static {
			KEYWORDS.addAll(C_KEYWORDS);
		}

		private static Set<String> BLOCK_KEYWORDS = Sets.newHashSet("catch",
				"class", "do", "else", "finally", "for", "if", "struct",
				"switch", "try", "while");

		private static Set<String> ATOMS = Sets.newHashSet("true", "false",
				"null");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		static {
			HOOKS.put("#", new CppHook());
			HOOKS.put("u", new Cpp11StringHook());
			HOOKS.put("U", new Cpp11StringHook());
			HOOKS.put("L", new Cpp11StringHook());
			HOOKS.put("R", new Cpp11StringHook());
		}

		@Override
		protected Set<String> keywords() {
			return KEYWORDS;
		}

		@Override
		protected Set<String> blockKeywords() {
			return BLOCK_KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "cpp", "c++", "cc", "cxx", "hpp",
					"h++", "hh", "hxx");
		}

	}

	public static class Java extends ClikeTokenizer {

		private static Set<String> KEYWORDS = Sets.newHashSet("abstract",
				"assert", "boolean", "break", "byte", "case", "catch", "char",
				"class", "const", "continue", "default", "do", "double",
				"else", "enum", "extends", "final", "finally", "float", "for",
				"goto", "if", "implements", "import", "instanceof", "int",
				"interface", "long", "native", "new", "package", "private",
				"protected", "public", "return", "short", "static", "strictfp",
				"super", "switch", "synchronized", "this", "throw", "throws",
				"transient", "try", "void", "volatile", "while");

		private static Set<String> BLOCK_KEYWORDS = Sets.newHashSet("catch",
				"class", "do", "else", "finally", "for", "if", "switch", "try",
				"while");

		private static Set<String> ATOMS = Sets.newHashSet("true", "false",
				"null");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		private static Pattern PATTERN = Pattern.compile("[\\w\\$_]");

		static {
			HOOKS.put("@", new Processor() {

				@Override
				public String process(StringStream stream, State state) {
					stream.eatWhile(PATTERN);
					return "meta";
				}

			});
		}

		@Override
		protected Set<String> keywords() {
			return KEYWORDS;
		}

		@Override
		protected Set<String> blockKeywords() {
			return BLOCK_KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "java");
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

	public static class CSharp extends ClikeTokenizer {

		private static Set<String> KEYWORDS = wordsOf("abstract as base break case catch checked class const continue"
				+ " default delegate do else enum event explicit extern finally fixed for"
				+ " foreach goto if implicit in interface internal is lock namespace new"
				+ " operator out override params private protected public readonly ref return sealed"
				+ " sizeof stackalloc static struct switch this throw try typeof unchecked"
				+ " unsafe using virtual void volatile while add alias ascending descending dynamic from get"
				+ " global group into join let orderby partial remove select set value var yield");

		private static Set<String> BLOCK_KEYWORDS = wordsOf("catch class do else finally for foreach if struct switch try while");

		private static Set<String> BUILT_IN = wordsOf("Boolean Byte Char DateTime DateTimeOffset Decimal Double"
				+ " Guid Int16 Int32 Int64 Object SByte Single String TimeSpan UInt16 UInt32"
				+ " UInt64 bool byte char decimal double short int long object"
				+ " sbyte float string ushort uint ulong");

		private static Set<String> ATOMS = Sets.newHashSet("true", "false",
				"null");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		private static Pattern PATTERN = Pattern.compile("[\\w\\$_]");

		static {
			HOOKS.put("@", new Processor() {

				@Override
				public String process(StringStream stream, State state) {
					if (stream.eat("\"").length() != 0) {
						state.tokenize = new TokenAtString();
						return state.tokenize.process(stream, state);
					}
					stream.eatWhile(PATTERN);
					return "meta";
				}

			});
		}

		@Override
		protected Set<String> keywords() {
			return KEYWORDS;
		}

		@Override
		protected Set<String> blockKeywords() {
			return BLOCK_KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected Set<String> builtin() {
			return BUILT_IN;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "cs");
		}

	}

	static class TokenTripleString implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			boolean escaped = false;
			while (!stream.eol()) {
				if (!escaped && stream.match("\"\"\"")) {
					state.tokenize = null;
					break;
				}
				escaped = !stream.next().equals("\\") && !escaped;
			}
			return "string";
		}
	}

	public static class Scala extends ClikeTokenizer {

		private static Set<String> KEYWORDS = wordsOf(
		/* scala */
		"abstract case catch class def do else extends false final finally for forSome if "
				+ "implicit import lazy match new null object override package private protected return "
				+ "sealed super this throw trait try trye type val var while with yield _ : = => <- <: "
				+ "<% >: # @ "
				+

				/* package scala */
				"assert assume require print println printf readLine readBoolean readByte readShort "
				+ "readChar readInt readLong readFloat readDouble "
				+

				"AnyVal App Application Array BufferedIterator BigDecimal BigInt Char Console Either "
				+ "Enumeration Equiv Error Exception Fractional Function IndexedSeq Integral Iterable "
				+ "Iterator List Map Numeric Nil NotNull Option Ordered Ordering PartialFunction PartialOrdering "
				+ "Product Proxy Range Responder Seq Serializable Set Specializable Stream StringBuilder "
				+ "StringContext Symbol Throwable Traversable TraversableOnce Tuple Unit Vector :: #:: "
				+

				/* package java.lang */
				"Boolean Byte Character CharSequence Class ClassLoader Cloneable Comparable "
				+ "Compiler Double Exception Float Integer Long Math Number Object Package Pair Process "
				+ "Runtime Runnable SecurityManager Short StackTraceElement StrictMath String "
				+ "StringBuffer System Thread ThreadGroup ThreadLocal Throwable Triple Void");

		private static Set<String> BLOCK_KEYWORDS = wordsOf("catch class do else finally for forSome if match switch try while");

		private static Set<String> ATOMS = Sets.newHashSet("true", "false", "null");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		private static Pattern PATTERN = Pattern.compile("[\\w\\$_]");

		static {
			HOOKS.put("@", new Processor() {

				@Override
				public String process(StringStream stream, State state) {
					stream.eatWhile(PATTERN);
					return "meta";
				}

			});
			HOOKS.put("\"", new Processor() {

				@Override
				public String process(StringStream stream, State state) {
			        if (!stream.match("\"\"")) 
			        	return "";
			        state.tokenize = new TokenTripleString();
			        return state.tokenize.process(stream, state);
				}
				
			});
		}

		@Override
		protected Set<String> keywords() {
			return KEYWORDS;
		}

		@Override
		protected Set<String> blockKeywords() {
			return BLOCK_KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected boolean indentStatements() {
			return false;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		protected boolean multiLineStrings() {
			return true;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "scala");
		}

	}

	public static class ObjectiveC extends ClikeTokenizer {

		private static Set<String> KEYWORDS = wordsOf(C_KEYWORDS, "inline restrict _Bool _Complex _Imaginery BOOL Class bycopy byref id IMP in " +
                "inout nil oneway out Protocol SEL self super atomic nonatomic retain copy readwrite readonly");

		private static Set<String> ATOMS = wordsOf("YES NO NULL NILL ON OFF");

		private static Map<String, Processor> HOOKS = Maps.newHashMap();

		private static Pattern PATTERN = Pattern.compile("[\\w\\$]");

		static {
			HOOKS.put("@", new Processor() {

				@Override
				public String process(StringStream stream, State state) {
					stream.eatWhile(PATTERN);
					return "keyword";
				}

			});
			HOOKS.put("#", new CppHook());
		}

		@Override
		protected Set<String> keywords() {
			return KEYWORDS;
		}

		@Override
		protected Set<String> atoms() {
			return ATOMS;
		}

		@Override
		protected Map<String, Processor> hooks() {
			return HOOKS;
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "m", "mm");
		}

	}
}
