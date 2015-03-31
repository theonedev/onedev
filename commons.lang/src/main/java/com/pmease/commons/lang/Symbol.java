package com.pmease.commons.lang;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

public abstract class Symbol implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Symbol parent;
	
	private final String name;
	
	private final int lineNo;

	public Symbol(@Nullable Symbol parent, @Nullable String name, int lineNo) {
		this.parent = parent;
		this.name = name;
		this.lineNo = lineNo;
	}
	
	public Symbol getParent() {
		return parent;
	}

	/**
	 * Get name of this symbol.
	 * 
	 * @return
	 * 			name of this symbol, or <tt>null</tt> if this symbol 
	 * 			does not have a name
	 */
	@Nullable
	public String getName() {
		return name;
	}
	
	public int getLineNo() {
		return lineNo;
	}
	
	public abstract Component render(String componentId);
	
	public abstract String describe(List<Symbol> symbols);
	
}
