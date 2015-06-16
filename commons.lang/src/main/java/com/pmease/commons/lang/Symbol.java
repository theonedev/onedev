package com.pmease.commons.lang;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;

public abstract class Symbol implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Symbol parent;
	
	private final String name;
	
	private final TokenPosition pos;
	
	public Symbol(@Nullable Symbol parent, @Nullable String name, TokenPosition pos) {
		this.parent = parent;
		this.name = name;
		this.pos = pos;
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
	
	public TokenPosition getPos() {
		return pos;
	}
	
	public abstract Component render(String componentId);
	
	public abstract ResourceReference getIcon();
	
	public abstract String describe(List<Symbol> symbols);

	public int score() {
		int relevance = 1;
		Symbol parent = this.parent;
		while (parent != null) {
			if (parent.getName() != null)
				relevance++;
			parent = parent.parent;
		}
		return relevance*name.length();
	}
	
	@Nullable
	public abstract String getScope();

	public abstract boolean isPrimary();
	
}
