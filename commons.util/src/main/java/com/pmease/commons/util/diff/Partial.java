package com.pmease.commons.util.diff;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Partial implements Serializable {
	
	private final String content;
	
	private final boolean emphasized;
	
	public Partial(final String content, final boolean emphasized) {
		this.content = content;
		this.emphasized = emphasized;
	}

	public String getContent() {
		return content;
	}

	public boolean isEmphasized() {
		return emphasized;
	}
	
	@Override
	public String toString() {
		if (emphasized)
			return "*" + content + "*";
		else
			return content;
	}
	
}