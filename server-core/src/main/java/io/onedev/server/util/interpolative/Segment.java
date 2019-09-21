package io.onedev.server.util.interpolative;

import java.io.Serializable;

public class Segment implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {LITERAL, VARIABLE};
	
	private final Type type;
	
	private final String content;
	
	public Segment(Type type, String content) {
		this.type = type;
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public String getContent() {
		return content;
	}
	
}
