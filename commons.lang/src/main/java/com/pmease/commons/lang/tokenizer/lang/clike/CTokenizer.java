package com.pmease.commons.lang.tokenizer.lang.clike;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CTokenizer extends ClikeTokenizer {

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