package com.pmease.commons.lang.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.pmease.commons.lang.Symbol;

public class FieldDef extends Symbol {

	private static final long serialVersionUID = 1L;

	private final String type;
	
	private final List<Modifier> modifiers;
	
	public FieldDef(TypeDef parent, String name, int lineNo, 
			@Nullable String type, List<Modifier> modifiers) {
		super(parent, name, lineNo);
		
		this.type = type;
		this.modifiers = modifiers;
	}
	
	/**
	 * Get type of this field.
	 * 
	 * @return 
	 * 			type of this field, or <tt>null</tt> for enum constant
	 */
	@Nullable
	public String getType() {
		return type;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public Component render(String componentId) {
		return null;
	}

	@Override
	public String describe(List<Symbol> symbols) {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");
		if (type != null)
			builder.append(type).append(" ");
		builder.append(getName()).append(";");
		return builder.toString();
	}
	
}
