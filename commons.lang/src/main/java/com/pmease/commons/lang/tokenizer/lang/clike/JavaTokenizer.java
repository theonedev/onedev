package com.pmease.commons.lang.tokenizer.lang.clike;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmease.commons.lang.tokenizer.StringStream;

public class JavaTokenizer extends ClikeTokenizer {

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