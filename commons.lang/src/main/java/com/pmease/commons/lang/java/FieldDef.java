package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class FieldDef {
	
	public String name;

	/**
	 * This field will be null for a enum constant
	 */
	@Nullable
	public String type;
	
	public int line;
	
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
	
}
