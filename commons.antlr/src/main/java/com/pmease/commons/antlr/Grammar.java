package com.pmease.commons.antlr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Grammar implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, Rule> rules = new HashMap<>();

	public Map<String, Rule> getRules() {
		return rules;
	}

}
