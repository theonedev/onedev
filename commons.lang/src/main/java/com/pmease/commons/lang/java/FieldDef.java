package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.pmease.commons.lang.Symbol;

public class FieldDef extends Symbol {

	private static final long serialVersionUID = 1L;

	/**
	 * This field will be null for a enum constant
	 */
	@Nullable
	public String type;
	
	public List<Modifier> modifiers = new ArrayList<>();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");
		if (type != null)
			builder.append(type).append(" ");
		builder.append(name).append(";");
		return builder.toString();
	}

	@Override
	public Component render(String componentId) {
		return null;
	}
	
}
