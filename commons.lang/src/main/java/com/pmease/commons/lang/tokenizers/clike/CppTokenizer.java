package com.pmease.commons.lang.tokenizers.clike;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmease.commons.lang.tokenizers.StringStream;

public class CppTokenizer extends ClikeTokenizer {

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