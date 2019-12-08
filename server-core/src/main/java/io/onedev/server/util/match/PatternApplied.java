package io.onedev.server.util.match;

import java.io.Serializable;

import io.onedev.commons.utils.LinearRange;

public class PatternApplied implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	
	private final LinearRange match;
	
	public PatternApplied(String text, LinearRange match) {
		this.text = text;
		this.match = match;
	}

	public String getText() {
		return text;
	}

	public LinearRange getMatch() {
		return match;
	}

	@Override
	public String toString() {
		return text + ":" + match;
	}
}
